package work.lclpnet.lobby.dev;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.component.builtin.BuiltinComponents;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.world.BlockModificationHooks;

public class TestGameActivity extends ComponentActivity {

    public TestGameActivity(MinecraftServer server, Logger logger) {
        super(server, logger);
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
            if (entity instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.literal("You broke ").append(Text.translatable(world.getBlockState(pos).getBlock().getTranslationKey())));
            }
            return false;
        });
    }
}
