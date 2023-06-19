package work.lclpnet.activity.component;

import java.util.Collection;

public interface ComponentBundle {

    ComponentBundle add(ComponentKey<?> component);

    Collection<ComponentKey<?>> build();
}
