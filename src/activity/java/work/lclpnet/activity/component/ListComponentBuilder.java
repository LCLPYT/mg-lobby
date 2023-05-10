package work.lclpnet.activity.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ListComponentBuilder implements ComponentBuilder {

    private final List<ComponentKey<?>> components = new ArrayList<>();

    @Override
    public ListComponentBuilder add(ComponentKey<?> component) {
        Objects.requireNonNull(component);
        components.add(component);
        return this;
    }

    @Override
    public Collection<ComponentKey<?>> build() {
        return components;
    }
}
