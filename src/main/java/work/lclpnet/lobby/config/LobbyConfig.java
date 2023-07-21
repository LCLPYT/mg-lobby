package work.lclpnet.lobby.config;

import org.json.JSONObject;
import work.lclpnet.config.json.JsonConfig;
import work.lclpnet.config.json.JsonConfigFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

public class LobbyConfig implements JsonConfig {

    public static final String DEFAULT_LOBBY_LEVEL_NAME = "lobby";
    public URI lobbySource = URI.create("https://lclpnet.work/dl/lobby-1.20.1");
    public String lobbyLevelName = "lobby";

    public LobbyConfig() {}

    public LobbyConfig(JSONObject obj) {
        if (obj.has("lobby_source")) {
            String lobbySource = obj.getString("lobby_source").replace('\\', '/');
            this.lobbySource = URI.create(lobbySource);
        }

        if (obj.has("lobby_level_name")) {
            this.lobbyLevelName = obj.getString("lobby_level_name");
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        if (lobbySource.getHost() == null) {
            // local path
            Path current = Path.of("").toAbsolutePath();
            Path lobbyPath;

            if (lobbySource.getScheme() == null) {
                // uri without scheme
                lobbyPath = Path.of(lobbySource.toString()).toAbsolutePath();
            } else {
                // file:/// uri
                lobbyPath = Path.of(lobbySource);
            }

            Path relativeLobbySource = current.relativize(lobbyPath);
            json.put("lobby_source", relativeLobbySource.toString());
        } else {
            json.put("lobby_source", lobbySource.toString());
        }

        json.put("lobby_level_name", lobbyLevelName);

        return json;
    }

    public String getSafeLobbyLevelName() {
        return Optional.ofNullable(lobbyLevelName).orElse(DEFAULT_LOBBY_LEVEL_NAME);
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
