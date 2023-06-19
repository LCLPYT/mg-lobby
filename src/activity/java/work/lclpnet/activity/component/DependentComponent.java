package work.lclpnet.activity.component;

public interface DependentComponent {

    void defineDependencies(ComponentBundle bundle);

    void injectDependencies(ComponentView view);
}
