package work.lclpnet.activity.component;

import java.util.Collection;

public interface ComponentBuilder {

    ComponentBuilder add(ComponentKey<?> component);

    Collection<ComponentKey<?>> build();
}
