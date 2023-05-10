package work.lclpnet.activity.component;

import java.util.Collection;

public interface ComponentView {

    <T extends Component> T get(ComponentKey<T> key);

    Collection<? extends Component> all();
}
