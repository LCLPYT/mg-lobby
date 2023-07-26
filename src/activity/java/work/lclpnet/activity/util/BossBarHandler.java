package work.lclpnet.activity.util;

import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import work.lclpnet.kibu.translate.bossbar.CustomBossBar;

public interface BossBarHandler {

    CommandBossBar createBossBar(Identifier id, Text text);

    void removeBossBar(CommandBossBar bossBar);

    /**
     * Configures a boss bar to be shown to new players in the future.
     * @param bossBar The boss bar to show to future players.
     */
    void showOnJoin(ServerBossBar bossBar);

    /**
     * Configures a custom boss bar to have players removed when they leave the server.
     * @param bossBar The custom boss bar to remove players from.
     */
    void removePlayersOnQuit(CustomBossBar bossBar);
}
