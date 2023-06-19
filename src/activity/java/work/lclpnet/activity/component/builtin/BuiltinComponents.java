package work.lclpnet.activity.component.builtin;

import work.lclpnet.activity.component.ComponentKey;

public class BuiltinComponents {

    public static final ComponentKey<HookComponent> HOOKS = context -> new HookComponent();
    public static final ComponentKey<CommandComponent> COMMANDS = context -> new CommandComponent();
    public static final ComponentKey<SchedulerComponent> SCHEDULER = SchedulerComponent::new;
    public static final ComponentKey<BossBarComponent> BOSS_BAR = context -> new BossBarComponent(context.getServer().getBossBarManager());

    private BuiltinComponents() {}
}
