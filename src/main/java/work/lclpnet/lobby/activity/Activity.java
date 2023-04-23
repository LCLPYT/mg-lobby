package work.lclpnet.lobby.activity;

import work.lclpnet.kibu.plugin.PluginContext;

public interface Activity {

    void startActivity(PluginContext context);

    void endActivity(PluginContext context);
}
