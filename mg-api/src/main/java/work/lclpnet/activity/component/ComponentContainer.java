package work.lclpnet.activity.component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ComponentContainer implements ComponentView {

    private final Map<ComponentKey<?>, Component> components;

    public ComponentContainer(Map<ComponentKey<?>, Component> components) {
        this.components = Collections.unmodifiableMap(components);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Component> T get(ComponentKey<T> key) {
        Component component = components.get(key);
        if (component == null) throw new IllegalStateException("Component not found");

        return (T) component;
    }

    @Override
    public Collection<? extends Component> all() {
        return components.values();
    }
}
