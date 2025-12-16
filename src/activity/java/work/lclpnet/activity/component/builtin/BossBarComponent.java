package work.lclpnet.activity.component.builtin;

import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.component.ComponentView;
import work.lclpnet.activity.component.DependentComponent;
import work.lclpnet.activity.util.BossBarHandler;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.translate.bossbar.BossBarProvider;
import work.lclpnet.kibu.translate.bossbar.CustomBossBar;
import work.lclpnet.kibu.translate.util.TransientBossBars;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BossBarComponent implements work.lclpnet.activity.component.Component, DependentComponent, BossBarHandler, BossBarProvider {

    private final CustomBossEvents bossBarManager;
    private final Set<CustomBossEvent> bars = new HashSet<>();
    private final Set<ServerBossEvent> showOnJoin = new HashSet<>();
    private final Set<CustomBossBar> removeOnQuit = new HashSet<>();
    private HookRegistrar hookRegistrar;

    public BossBarComponent(CustomBossEvents bossBarManager) {
        this.bossBarManager = bossBarManager;
    }

    @Override
    public void declareDependencies(ComponentBundle bundle) {
        bundle.add(BuiltinComponents.HOOKS);
    }

    @Override
    public void injectDependencies(ComponentView view) {
        hookRegistrar = view.get(BuiltinComponents.HOOKS).hooks();
    }

    @Override
    public void mount() {
        hookRegistrar.registerHook(PlayerConnectionHooks.JOIN, player -> {
            for (ServerBossEvent bossBar : showOnJoin) {
                bossBar.addPlayer(player);
            }
        });

        hookRegistrar.registerHook(PlayerConnectionHooks.QUIT, player -> {
            for (CustomBossEvent bossBar : bars) {
                bossBar.removePlayer(player);
            }

            for (ServerBossEvent bossBar : showOnJoin) {
                bossBar.removePlayer(player);
            }

            for (CustomBossBar bossBar : removeOnQuit) {
                bossBar.removePlayer(player);
            }
        });
    }

    @Override
    public void dismount() {
        bars.forEach(this::removeBossBarInternal);
        bars.clear();

        showOnJoin.forEach(ServerBossEvent::removeAllPlayers);
        showOnJoin.clear();

        removeOnQuit.clear();
    }

    @Override
    public CustomBossEvent createBossBar(Identifier id, Component text) {
        CustomBossEvent bar = bossBarManager.create(id, text);

        TransientBossBars.setTransient(bar, true);

        bars.add(bar);

        return bar;
    }

    @Override
    public void removeBossBar(CustomBossEvent bossBar) {
        removeBossBarInternal(bossBar);
        bars.remove(bossBar);
    }

    private void removeBossBarInternal(CustomBossEvent bossBar) {
        bossBar.removeAllPlayers();
        bossBarManager.remove(bossBar);
        showOnJoin.remove(bossBar);
    }

    @Override
    public void showOnJoin(ServerBossEvent bossBar) {
        Objects.requireNonNull(bossBar);
        showOnJoin.add(bossBar);
    }

    @Override
    public void removePlayersOnQuit(CustomBossBar bossBar) {
        removeOnQuit.add(bossBar);
    }
}
