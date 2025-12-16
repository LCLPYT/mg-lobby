package work.lclpnet.activity.util;

import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import work.lclpnet.kibu.translate.bossbar.CustomBossBar;

public interface BossBarHandler {

    CustomBossEvent createBossBar(ResourceLocation id, Component text);

    void removeBossBar(CustomBossEvent bossBar);

    /**
     * Configures a boss bar to be shown to new players in the future.
     * @param bossBar The boss bar to show to future players.
     */
    void showOnJoin(ServerBossEvent bossBar);

    /**
     * Configures a custom boss bar to have players removed when they leave the server.
     * @param bossBar The custom boss bar to remove players from.
     */
    void removePlayersOnQuit(CustomBossBar bossBar);
}
