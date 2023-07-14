package work.lclpnet.activity;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import work.lclpnet.activity.component.*;
import work.lclpnet.kibu.plugin.ext.PluginContext;
import work.lclpnet.plugin.graph.DAG;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ComponentActivity implements Activity, ComponentContext {

    private final PluginContext context;
    private final ComponentView components;
    private final DAG<ComponentKey<?>> dependencyTree;

    public ComponentActivity(PluginContext context) {
        this.context = context;

        final ComponentBundle componentBundle = new ListComponentBundle();
        registerComponents(componentBundle);

        dependencyTree = new DAG<>();
        Map<ComponentKey<?>, Component> componentMap = new HashMap<>();

        for (var componentKey : componentBundle.build()) {
            registerComponent(componentKey, componentMap);
        }

        this.components = new ComponentContainer(componentMap);

        // inject components view into dependant components
        for (var component : componentMap.values()) {
            if (!(component instanceof DependentComponent dependentComponent)) continue;

            dependentComponent.injectDependencies(this.components);
        }
    }

    private void registerComponent(ComponentKey<?> key, Map<ComponentKey<?>, Component> componentMap) {
        if (componentMap.containsKey(key)) return;

        final Component component = key.newInstance(this);

        final var node = dependencyTree.getOrCreateNode(key, key);
        final ListComponentBundle depsBundle = new ListComponentBundle();

        if (component instanceof DependentComponent dependentComponent) {
            dependentComponent.declareDependencies(depsBundle);

            var deps = depsBundle.build();

            for (var dep : deps) {
                registerComponent(dep, componentMap);

                var depNode = dependencyTree.getOrCreateNode(dep, dep);
                depNode.addChild(node);
            }

            deps.clear();
        }

        componentMap.put(key, component);
    }

    protected abstract void registerComponents(ComponentBundle components);

    public final <T extends Component> T component(ComponentKey<T> key) {
        return components.get(key);
    }

    @Override
    public final ComponentView getComponents() {
        return components;
    }

    @Override
    public final MinecraftServer getServer() {
        return context.getEnvironment().getServer();
    }

    @Override
    public final Logger getLogger() {
        return context.getLogger();
    }

    @Override
    public void start() {
        // load dependencies first, then their dependants
        var order = dependencyTree.getTopologicalOrder();

        for (var node : order) {
            ComponentKey<?> componentKey = node.getObj();
            Component component = components.get(componentKey);

            component.mount();
        }
    }

    @Override
    public void stop() {
        // unload dependants first, then their dependencies
        var order = dependencyTree.getTopologicalOrder();

        Collections.reverse(order);

        for (var node : order) {
            ComponentKey<?> componentKey = node.getObj();
            Component component = components.get(componentKey);

            component.dismount();
        }
    }
}
