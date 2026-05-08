package work.lclpnet.lobby.dev;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.lobby.game.api.Game;
import work.lclpnet.lobby.game.api.GameConfig;
import work.lclpnet.lobby.game.api.GameFactory;
import work.lclpnet.lobby.game.api.start.GameScope;
import work.lclpnet.lobby.game.api.start.GameStatusManager;
import work.lclpnet.lobby.game.impl.MinecraftGameConfig;
import work.lclpnet.lobby.game.impl.ModGameFactory;

import static work.lclpnet.kibu.translate.text.FormatWrapper.styled;

public class AnotherGame implements Game {

    public static final int MIN_PLAYER_COUNT = 2;

    @Override
    public GameConfig getConfig() {
        return new MinecraftGameConfig("another", new ItemStackTemplate(Items.RESIN_CLUMP));
    }

    @Override
    public boolean canBePlayed(GameScope scope) {
        return scope.playerCount() >= 2;
    }

    @Override
    public GameFactory createFactory() {
        return new ModGameFactory(TestGame.MOD_ID, TestGame.logger, AnotherGameInstance::new);
    }

    @Override
    public void configureStatusManager(GameStatusManager manager) {
        Translations translations = manager.getContext().getTranslations();

        var msg = translations.translateText("lobby.game.not_enough_players", styled(MIN_PLAYER_COUNT, ChatFormatting.YELLOW))
                .formatted(ChatFormatting.RED);

        manager.setCannotStartMessage(msg::translateFor);
        manager.setCannotStartBossBarValue(translations.translateText("lobby.game.waiting_for_players"));
    }
}
