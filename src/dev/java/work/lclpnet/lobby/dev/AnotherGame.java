package work.lclpnet.lobby.dev;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import work.lclpnet.game.api.Game;
import work.lclpnet.game.api.GameConfig;
import work.lclpnet.game.api.GameFactory;
import work.lclpnet.game.api.start.GameStartScope;
import work.lclpnet.game.api.start.GameStatusManager;
import work.lclpnet.game.impl.MinecraftGameConfig;
import work.lclpnet.game.impl.ModGameFactory;
import work.lclpnet.game.util.GameStartUtil;

public class AnotherGame implements Game {

    public static final int MIN_PLAYER_COUNT = 2;

    @Override
    public @NotNull GameConfig getConfig() {
        return new MinecraftGameConfig("another", new ItemStackTemplate(Items.RESIN_CLUMP));
    }

    @Override
    public boolean canBePlayed(@NonNull GameStartScope scope) {
        return scope.playerCount() >= 2;
    }

    @Override
    public @NonNull GameFactory createFactory() {
        return new ModGameFactory(TestGame.MOD_ID, TestGame.logger, AnotherGameInstance::new);
    }

    @Override
    public void configureStatusManager(@NotNull GameStatusManager manager) {
        GameStartUtil.configureNotEnoughPlayersMessage(manager, MIN_PLAYER_COUNT);
        GameStartUtil.configureWaitingForPlayersBossBar(manager);
    }
}
