package work.lclpnet.activity.component;

public interface ComponentKey<T extends Component> {

    T newInstance(ComponentContext context);
}
