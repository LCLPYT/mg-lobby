package work.lclpnet.activity;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import work.lclpnet.activity.component.*;
import work.lclpnet.kibu.plugin.PluginContext;

import java.util.HashMap;
import java.util.Map;

public abstract class ComponentActivity implements Activity, ComponentContext {

    private final PluginContext context;
    private final ComponentView components;

    public ComponentActivity(PluginContext context) {
        this.context = context;

        final ComponentBuilder componentBuilder = new ListComponentBuilder();
        buildComponents(componentBuilder);

        Map<ComponentKey<?>, Component> componentMap = new HashMap<>();

        for (var componentKey : componentBuilder.build()) {
            Component component = componentKey.newInstance(this);
            componentMap.put(componentKey, component);
        }

        this.components = new ComponentContainer(componentMap);
    }

    protected abstract void buildComponents(ComponentBuilder components);

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
        components.all().forEach(Component::mount);
    }

    @Override
    public void stop() {
        components.all().forEach(Component::dismount);
    }
}
