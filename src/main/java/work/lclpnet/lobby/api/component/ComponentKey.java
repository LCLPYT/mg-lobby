package work.lclpnet.lobby.api.component;

public interface ComponentKey<T extends Component> {

    T newInstance(ComponentContext context);
}
