package work.lclpnet.activity.component.builtin;

import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import work.lclpnet.activity.component.Component;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.component.ComponentView;
import work.lclpnet.activity.component.DependentComponent;
import work.lclpnet.activity.util.BossBarHandler;
import work.lclpnet.kibu.hook.player.PlayerConnectionHooks;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.kibu.translate.bossbar.BossBarProvider;
import work.lclpnet.kibu.translate.bossbar.CustomBossBar;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BossBarComponent implements Component, DependentComponent, BossBarHandler, BossBarProvider {

    private final BossBarManager bossBarManager;
    private final Set<CommandBossBar> bars = new HashSet<>();
    private final Set<ServerBossBar> showOnJoin = new HashSet<>();
    private final Set<CustomBossBar> removeOnQuit = new HashSet<>();
    private HookRegistrar hookRegistrar;

    public BossBarComponent(BossBarManager bossBarManager) {
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
            for (ServerBossBar bossBar : showOnJoin) {
                bossBar.addPlayer(player);
            }
        });

        hookRegistrar.registerHook(PlayerConnectionHooks.QUIT, player -> {
            for (CommandBossBar bossBar : bars) {
                bossBar.removePlayer(player);
            }

            for (ServerBossBar bossBar : showOnJoin) {
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

        showOnJoin.forEach(ServerBossBar::clearPlayers);
        showOnJoin.clear();

        removeOnQuit.clear();
    }

    @Override
    public CommandBossBar createBossBar(Identifier id, Text text) {
        CommandBossBar bar = bossBarManager.add(id, text);
        bars.add(bar);
        return bar;
    }

    @Override
    public void removeBossBar(CommandBossBar bossBar) {
        removeBossBarInternal(bossBar);
        bars.remove(bossBar);
    }

    private void removeBossBarInternal(CommandBossBar bossBar) {
        bossBar.clearPlayers();
        bossBarManager.remove(bossBar);
        showOnJoin.remove(bossBar);
    }

    @Override
    public void showOnJoin(ServerBossBar bossBar) {
        Objects.requireNonNull(bossBar);
        showOnJoin.add(bossBar);
    }

    @Override
    public void removePlayersOnQuit(CustomBossBar bossBar) {
        removeOnQuit.add(bossBar);
    }
}
