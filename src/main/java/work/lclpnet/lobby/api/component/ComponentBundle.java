package work.lclpnet.lobby.api.component;

import java.util.Collection;

public interface ComponentBundle {

    ComponentBundle add(ComponentKey<?> component);

    Collection<ComponentKey<?>> build();
}
