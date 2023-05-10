package work.lclpnet.lobby.api.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ListComponentBundle implements ComponentBundle {

    private final List<ComponentKey<?>> components = new ArrayList<>();

    @Override
    public ListComponentBundle add(ComponentKey<?> component) {
        Objects.requireNonNull(component);
        components.add(component);
        return this;
    }

    @Override
    public Collection<ComponentKey<?>> build() {
        return components;
    }
}
