package work.lclpnet.lobby.activity;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.component.builtin.BossBarComponent;
import work.lclpnet.activity.component.builtin.BuiltinComponents;
import work.lclpnet.kibu.plugin.PluginContext;
import work.lclpnet.kibu.scheduler.api.RunningTask;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.kibu.scheduler.api.SchedulerAction;
import work.lclpnet.lobby.LobbyPlugin;
import work.lclpnet.lobby.game.conf.GameConfig;
import work.lclpnet.lobby.game.start.GameStarter;

public class GameStartingActivity extends ComponentActivity implements SchedulerAction {

    private final GameConfig gameConfig;
    private final GameStarter starter;
    private CommandBossBar bossBar;
    private int timer;
    private int colorIndex;

    public GameStartingActivity(PluginContext context, GameConfig gameConfig, GameStarter starter) {
        super(context);
        this.gameConfig = gameConfig;
        this.starter = starter;
    }

    @Override
    protected void registerComponents(ComponentBundle components) {
        components.add(BuiltinComponents.BOSS_BAR).add(BuiltinComponents.SCHEDULER);
    }

    @Override
    public void start() {
        super.start();

        timer = gameConfig.getStartDuration() * 20;
        colorIndex = 0;

        final BossBarComponent bossBars = component(BuiltinComponents.BOSS_BAR);

        bossBar = bossBars.createBossBar(LobbyPlugin.identifier("starting"), getBossBarTitle());
        bossBar.setColor(BossBar.Color.values()[colorIndex]);
        bossBar.addPlayers(PlayerLookup.all(getServer()));
        bossBar.setPercent(1f);

        bossBars.showOnJoin(bossBar);

        final Scheduler scheduler = component(BuiltinComponents.SCHEDULER).scheduler();

        scheduler.interval(this, 1);
    }

    private Text getBossBarTitle() {
        int seconds = timer / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        StringBuilder timeString = new StringBuilder();
        timeString.append(' ');

        if (minutes > 0) {
            timeString.append(minutes).append("min ");
        }

        timeString.append(seconds).append("sec");

        return Text.literal(gameConfig.title()).formatted(Formatting.AQUA, Formatting.BOLD)
                .append(Text.literal(timeString.toString()).formatted(Formatting.YELLOW));
    }

    private void updateBossBar() {
        colorIndex = (colorIndex + 1) % BossBar.Color.values().length;

        bossBar.setName(getBossBarTitle());
        bossBar.setColor(BossBar.Color.values()[colorIndex]);
        bossBar.setPercent(timer / (float) (gameConfig.getStartDuration() * 20));
    }

    @Override
    public void run(RunningTask task) {
        if (starter.isStarted()) {
            task.cancel();
            return;
        }

        if (timer-- == 0) {
            task.cancel();
            bossBar.setVisible(false);
            starter.start();
            return;
        }

        if (timer % 20 == 0) {
            updateBossBar();
        }
    }
}
