package work.lclpnet.lobby.activity;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.component.builtin.BossBarComponent;
import work.lclpnet.activity.component.builtin.BuiltinComponents;
import work.lclpnet.kibu.plugin.ext.PluginContext;
import work.lclpnet.kibu.scheduler.api.RunningTask;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.kibu.scheduler.api.SchedulerAction;
import work.lclpnet.kibu.translate.TranslationService;
import work.lclpnet.kibu.translate.bossbar.TranslatedBossBar;
import work.lclpnet.lobby.LobbyPlugin;
import work.lclpnet.lobby.game.conf.GameConfig;
import work.lclpnet.lobby.game.start.GameStarter;

import static work.lclpnet.kibu.translate.text.FormatWrapper.styled;

public class GameStartingActivity extends ComponentActivity implements SchedulerAction {

    private final GameConfig gameConfig;
    private final GameStarter starter;
    private final TranslationService translations;
    private TranslatedBossBar bossBar;
    private int timer;
    private int colorIndex;
    private boolean wasPaused = false;

    @AssistedInject
    public GameStartingActivity(PluginContext context, TranslationService translations,
                                @Assisted GameConfig gameConfig, @Assisted GameStarter starter) {
        super(context);
        this.gameConfig = gameConfig;
        this.starter = starter;
        this.translations = translations;
    }

    @Override
    protected void registerComponents(ComponentBundle components) {
        components.add(BuiltinComponents.BOSS_BAR).add(BuiltinComponents.SCHEDULER);
    }

    @Override
    public void start() {
        super.start();

        timer = gameConfig.startDuration() * 20;
        colorIndex = 0;

        final BossBarComponent bossBars = component(BuiltinComponents.BOSS_BAR);

        final Identifier bossBarId = LobbyPlugin.identifier("starting");
        final var titleTranslation = titleTranslation();

        bossBar = translations.translateBossBar(bossBarId, titleTranslation.left(), titleTranslation.right())
                .with(bossBars).formatted(Formatting.YELLOW);

        bossBar.setColor(BossBar.Color.values()[colorIndex]);
        bossBar.addPlayers(PlayerLookup.all(getServer()));
        bossBar.setPercent(1f);

        bossBars.showOnJoin(bossBar);

        final Scheduler scheduler = component(BuiltinComponents.SCHEDULER).scheduler();

        scheduler.interval(this, 1).whenComplete(() -> bossBar.setVisible(false));
    }

    private Pair<String, Object[]> titleTranslation() {
        if (wasPaused) {
            return Pair.of("lobby.countdown.title.paused", new Object[] {
                    styled(gameConfig.title(), Formatting.AQUA, Formatting.BOLD)
            });
        }

        int seconds = timer / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return Pair.of("lobby.countdown.title.minutes", new Object[] {
                    styled(gameConfig.title(), Formatting.AQUA, Formatting.BOLD),
                    minutes,
                    seconds
            });
        }

        return Pair.of("lobby.countdown.title.seconds", new Object[] {
                styled(gameConfig.title(), Formatting.AQUA, Formatting.BOLD),
                seconds
        });
    }

    private void updateBossBar() {
        colorIndex = (colorIndex + 1) % BossBar.Color.values().length;

        var titleTranslation = titleTranslation();

        bossBar.setTitle(titleTranslation.left(), titleTranslation.right());
        bossBar.setColor(BossBar.Color.values()[colorIndex]);
        bossBar.setPercent(timer / (float) (gameConfig.startDuration() * 20));
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
            starter.start();
            return;
        }

        if (timer % 20 == 0) {
            updateBossBar();
        }
    }

    @AssistedFactory
    public interface Builder {
        GameStartingActivity create(GameConfig gameConfig, GameStarter starter);
    }
}
