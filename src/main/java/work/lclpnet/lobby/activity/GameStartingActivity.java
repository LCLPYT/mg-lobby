package work.lclpnet.lobby.activity;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.component.builtin.BossBarComponent;
import work.lclpnet.activity.component.builtin.BuiltinComponents;
import work.lclpnet.kibu.cmd.type.CommandRegistrar;
import work.lclpnet.kibu.scheduler.api.RunningTask;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.kibu.scheduler.api.SchedulerAction;
import work.lclpnet.kibu.translate.Translations;
import work.lclpnet.kibu.translate.bossbar.TranslatedBossBar;
import work.lclpnet.lobby.LobbyMod;
import work.lclpnet.lobby.cmd.PauseCommand;
import work.lclpnet.lobby.cmd.ResumeCommand;
import work.lclpnet.lobby.cmd.StartCommand;
import work.lclpnet.lobby.game.LobbyWaitingManager;
import work.lclpnet.lobby.game.api.Game;
import work.lclpnet.lobby.game.api.GameConfig;
import work.lclpnet.lobby.game.start.GameStarter;
import work.lclpnet.lobby.util.LobbyGameContext;

import javax.inject.Named;

public class GameStartingActivity extends ComponentActivity implements SchedulerAction {

    private final Game game;
    private final GameConfig config;
    private final GameStarter starter;
    private final Translations translations;
    private final LobbyWaitingManager waitingManager;
    private TranslatedBossBar bossBar;
    private int timer;
    private int colorIndex;
    private boolean wasPaused = false;

    @AssistedInject
    public GameStartingActivity(MinecraftServer server, Logger logger, @Named("lobbyWorld") ServerWorld world,
                                @Assisted Game game, @Assisted GameStarter starter, @Assisted Translations translations) {
        super(server, logger);
        this.game = game;
        this.config = game.getConfig();
        this.starter = starter;
        this.translations = translations;

        var context = new LobbyGameContext(server, game.getConfig(), translations);
        this.waitingManager = new LobbyWaitingManager(world, context, starter);
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

        game.configureStatusManager(starter);

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
                .with(bossBars).formatted(Formatting.YELLOW);

        bossBar.setColor(BossBar.Color.values()[colorIndex]);
        bossBar.addPlayers(PlayerLookup.all(getServer()));
        bossBar.setPercent(1f);

        bossBars.showOnJoin(bossBar);

        initWaitingManager();

        final Scheduler scheduler = component(BuiltinComponents.SCHEDULER).scheduler();

        scheduler.interval(this, 1).whenComplete(() -> bossBar.setVisible(false));
    }

    private void initWaitingManager() {
        game.configureOptions(waitingManager);

        waitingManager.init(component(BuiltinComponents.HOOKS).hooks());
    }

    private Pair<String, Object[]> titleTranslation() {
        if (wasPaused) {
            return Pair.of("lobby.countdown.title.paused", new Object[] {
                    translations.translateText(config.titleKey()).formatted(Formatting.AQUA, Formatting.BOLD)
            });
        }

        int seconds = timer / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return Pair.of("lobby.countdown.title.minutes", new Object[] {
                    translations.translateText(config.titleKey()).formatted(Formatting.AQUA, Formatting.BOLD),
                    minutes,
                    seconds
            });
        }

        return Pair.of("lobby.countdown.title.seconds", new Object[] {
                translations.translateText(config.titleKey()).formatted(Formatting.AQUA, Formatting.BOLD),
                seconds
        });
    }

    private void updateBossBar() {
        colorIndex = (colorIndex + 1) % BossBar.Color.values().length;

        var titleTranslation = titleTranslation();

        bossBar.setTitle(titleTranslation.left(), titleTranslation.right());
        bossBar.setColor(BossBar.Color.values()[colorIndex]);
        bossBar.setPercent(timer / (float) (config.lobbyDurationSeconds() * 20));
    }

    @Override
    public void run(RunningTask task) {
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

        if (timer-- == 0) {
            task.cancel();
            starter.finish(waitingManager);
            return;
        }

        if (timer % 20 == 0) {
            updateBossBar();

            int remaining = timer / 20;

            if (remaining <= 5) {
                for (ServerPlayerEntity player : PlayerLookup.all(getServer())) {
                    player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), SoundCategory.BLOCKS, 2f, 1f);
                }
            }
        }
    }

    @AssistedFactory
    public interface Builder {
        GameStartingActivity create(Game game, GameStarter starter, Translations translations);
    }
}
