package work.lclpnet.lobby.api.component.builtin;

import work.lclpnet.lobby.api.component.ComponentKey;

public class BuiltinComponents {

    public static final ComponentKey<HookComponent> HOOKS = context -> new HookComponent();
    public static final ComponentKey<CommandComponent> COMMANDS = context -> new CommandComponent();
    public static final ComponentKey<SchedulerComponent> SCHEDULER = SchedulerComponent::new;

    private BuiltinComponents() {}
}
