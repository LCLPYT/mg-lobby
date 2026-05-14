package work.lclpnet.activity.component;

public interface DependentComponent {

    void declareDependencies(ComponentBundle bundle);

    void injectDependencies(ComponentView view);
}
