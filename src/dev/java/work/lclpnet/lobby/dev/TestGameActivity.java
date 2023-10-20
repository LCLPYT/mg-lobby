package work.lclpnet.lobby.dev;

import net.minecraft.text.Text;
import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.component.builtin.BuiltinComponents;
import work.lclpnet.kibu.hook.world.BlockModificationHooks;
import work.lclpnet.kibu.plugin.ext.PluginContext;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;

public class TestGameActivity extends ComponentActivity {

    public TestGameActivity(PluginContext context) {
        super(context);
    }

    @Override
    protected void registerComponents(ComponentBundle components) {
        components.add(BuiltinComponents.HOOKS);
    }

    @Override
    public void start() {
        super.start();

        HookRegistrar hooks = component(BuiltinComponents.HOOKS).hooks();

        hooks.registerHook(BlockModificationHooks.BREAK_BLOCK, (world, pos, entity) -> {
            entity.sendMessage(Text.literal("You broke ").append(Text.translatable(world.getBlockState(pos).getBlock().getTranslationKey())));
            return false;
        });
    }
}
