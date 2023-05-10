package work.lclpnet.activity.component.builtin;

import work.lclpnet.activity.component.Component;
import work.lclpnet.kibu.plugin.hook.HookContainer;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;

public class HookComponent implements Component {

    private final HookContainer hooks = new HookContainer();

    @Override
    public void mount() {
        // no-op
    }

    @Override
    public void dismount() {
        hooks.unload();
    }

    public HookRegistrar hooks() {
        return hooks;
    }
}
