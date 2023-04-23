package work.lclpnet.lobby.config;

import org.json.JSONObject;
import work.lclpnet.config.json.JsonConfig;
import work.lclpnet.config.json.JsonConfigFactory;

import java.net.URI;
import java.nio.file.Path;

public class LobbyConfig implements JsonConfig {

    public URI lobbySource = Path.of("worlds", "lobby").toUri();

    public LobbyConfig() {}

    public LobbyConfig(JSONObject obj) {
        if (obj.has("lobbySource")) {
            String lobbySource = obj.getString("lobbySource");
            this.lobbySource = URI.create(lobbySource);
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        json.put("lobbySource", lobbySource.toString());

        return json;
    }

    public static final JsonConfigFactory<LobbyConfig> FACTORY = new JsonConfigFactory<>() {
        @Override
        public LobbyConfig createDefaultConfig() {
            return new LobbyConfig();
        }

        @Override
        public LobbyConfig createConfig(JSONObject json) {
            return new LobbyConfig(json);
        }
    };
}
