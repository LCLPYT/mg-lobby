package work.lclpnet.lobby.api.component.builtin;

import work.lclpnet.kibu.plugin.hook.HookContainer;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.lobby.api.component.Component;

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
