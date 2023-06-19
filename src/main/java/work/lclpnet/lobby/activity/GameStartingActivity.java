package work.lclpnet.lobby.activity;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.text.Text;
import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.component.builtin.BossBarComponent;
import work.lclpnet.activity.component.builtin.BuiltinComponents;
import work.lclpnet.kibu.plugin.PluginContext;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.lobby.LobbyPlugin;
import work.lclpnet.lobby.game.conf.GameConfig;

import java.util.concurrent.atomic.AtomicInteger;

public class GameStartingActivity extends ComponentActivity {

    private final GameConfig gameConfig;
    private final Runnable start;

    public GameStartingActivity(PluginContext context, GameConfig gameConfig, Runnable start) {
        super(context);
        this.gameConfig = gameConfig;
        this.start = start;
    }

    @Override
    protected void buildComponents(ComponentBundle components) {
        components.add(BuiltinComponents.BOSS_BAR).add(BuiltinComponents.SCHEDULER);
    }

    @Override
    public void start() {
        super.start();

        System.out.printf("Starting '%s'...%n", gameConfig.title());

        final BossBarComponent bossBars = component(BuiltinComponents.BOSS_BAR);

        CommandBossBar bossBar = bossBars.createBossBar(LobbyPlugin.identifier("test"), Text.literal("Test"));
        bossBar.setColor(BossBar.Color.PURPLE);
        bossBar.setPercent(0.5f);
        bossBar.addPlayers(PlayerLookup.all(getServer()));

        bossBars.showOnJoin(bossBar);

        final Scheduler scheduler = component(BuiltinComponents.SCHEDULER).scheduler();
        AtomicInteger counter = new AtomicInteger(gameConfig.getStartDuration());

        scheduler.interval(info -> {
            int countDown = counter.getAndDecrement();

            if (countDown == 0) {
                info.cancel();
                return;
            }

            bossBar.setName(Text.literal("Starting in %d sec".formatted(countDown)));
        }, 20, 20);
    }
}
