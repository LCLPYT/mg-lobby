package work.lclpnet.lobby.activity;

import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.world.BossEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.component.builtin.BossBarComponent;
import work.lclpnet.activity.component.builtin.BuiltinComponents;
import work.lclpnet.kibu.access.entity.ServerPlayerAccess;
import work.lclpnet.kibu.cmd.type.CommandRegistrar;
import work.lclpnet.kibu.scheduler.api.RunningTask;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.bossbar.TranslatedBossBar;
import work.lclpnet.lobby.LobbyMod;
import work.lclpnet.lobby.cmd.PauseCommand;
import work.lclpnet.lobby.cmd.ResumeCommand;
import work.lclpnet.lobby.cmd.StartCommand;
import work.lclpnet.lobby.game.LobbyWaitingManager;
import work.lclpnet.game.api.Game;
import work.lclpnet.game.api.GameConfig;
import work.lclpnet.lobby.game.start.GameStarter;
import work.lclpnet.lobby.game.start.LobbyArgs;
import work.lclpnet.lobby.util.LobbyGameContext;

public class GameStartingActivity extends ComponentActivity {

    private final Game game;
    private final GameConfig config;
    private final GameStarter starter;
    private final Translations translations;
    private final LobbyWaitingManager waitingManager;
    private TranslatedBossBar bossBar;
    private int timer;
    private int colorIndex;
    private boolean wasPaused = false;

    public GameStartingActivity(MinecraftServer server, Logger logger, ServerLevel world, Game game,
                                GameStarter starter, Translations translations, LobbyArgs lobbyArgs) {
        super(server, logger);
        this.game = game;
        this.config = game.getConfig();
        this.starter = starter;
        this.translations = translations;

        var context = new LobbyGameContext(server, game.getConfig(), translations);
        this.waitingManager = new LobbyWaitingManager(world, context, starter, lobbyArgs.getPlayerStateManager());
    }

    @Override
    protected void registerComponents(ComponentBundle components) {
        components
                .add(BuiltinComponents.BOSS_BAR)
                .add(BuiltinComponents.SCHEDULER)
                .add(BuiltinComponents.HOOKS)
                .add(BuiltinComponents.COMMANDS);
    }

    @Override
    public void start() {
        super.start();

        CommandRegistrar commands = component(BuiltinComponents.COMMANDS).commands();

        new StartCommand(starter, waitingManager).register(commands);
        new PauseCommand(starter).register(commands);
        new ResumeCommand(starter).register(commands);

        timer = config.lobbyDurationSeconds() * 20;
        colorIndex = 0;

        final BossBarComponent bossBars = component(BuiltinComponents.BOSS_BAR);

        final Identifier bossBarId = LobbyMod.identifier("starting");
        final var titleTranslation = titleTranslation();

        bossBar = translations.translateBossBar(bossBarId, titleTranslation.left(), titleTranslation.right())
                .with(bossBars).formatted(ChatFormatting.YELLOW);

        bossBar.setColor(BossEvent.BossBarColor.values()[colorIndex]);
        bossBar.addPlayers(PlayerLookup.all(getServer()));
        bossBar.setProgress(1f);

        bossBars.showOnJoin(bossBar);

        initWaitingManager();

        final Scheduler scheduler = component(BuiltinComponents.SCHEDULER).scheduler();

        scheduler.interval(this::tick, 1)
                .whenComplete(() -> bossBar.setVisible(false));
    }

    private void initWaitingManager() {
        game.configureOptions(waitingManager);

        waitingManager.init(component(BuiltinComponents.HOOKS).hooks());
    }

    private Pair<String, Object[]> titleTranslation() {
        if (wasPaused) {
            return Pair.of("lobby.countdown.title.paused", new Object[] {
                    translations.translateText(config.titleKey()).formatted(ChatFormatting.AQUA, ChatFormatting.BOLD)
            });
        }

        int seconds = timer / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return Pair.of("lobby.countdown.title.minutes", new Object[] {
                    translations.translateText(config.titleKey()).formatted(ChatFormatting.AQUA, ChatFormatting.BOLD),
                    minutes,
                    seconds
            });
        }

        return Pair.of("lobby.countdown.title.seconds", new Object[] {
                translations.translateText(config.titleKey()).formatted(ChatFormatting.AQUA, ChatFormatting.BOLD),
                seconds
        });
    }

    private void updateBossBar() {
        colorIndex = (colorIndex + 1) % BossEvent.BossBarColor.values().length;

        var titleTranslation = titleTranslation();

        bossBar.setTitle(titleTranslation.left(), titleTranslation.right());
        bossBar.setColor(BossEvent.BossBarColor.values()[colorIndex]);
        bossBar.setProgress(timer / (float) (config.lobbyDurationSeconds() * 20));
    }

    public void tick(RunningTask task) {
        if (starter.isStarted()) {
            task.cancel();
            return;
        }

        if (starter.isPaused()) {
            if (!wasPaused) {
                wasPaused = true;
                updateBossBar();
            }

            return;
        }

        wasPaused = false;
        waitingManager.runTimedActions(timer);

        if (timer-- == 0) {
            task.cancel();
            starter.finish(waitingManager);
            return;
        }

        if (timer % 20 == 0) {
            updateBossBar();

            int remaining = timer / 20;

            if (remaining <= 5) {
                for (ServerPlayer player : PlayerLookup.all(getServer())) {
                    ServerPlayerAccess.playSoundToPlayer(player, SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.BLOCKS, 2f, 1f);
                }
            }
        }
    }

    @Override
    public void stop() {
        super.stop();

        for (ServerPlayer player : PlayerLookup.all(getServer())) {
            player.getInventory().clearContent();
        }
    }

    public interface Builder {
        GameStartingActivity create(Game game, GameStarter starter, Translations translations, LobbyArgs lobbyArgs);
    }
}
