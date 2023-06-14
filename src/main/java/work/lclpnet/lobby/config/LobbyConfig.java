package work.lclpnet.lobby.config;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.json.JSONArray;
import org.json.JSONObject;
import work.lclpnet.config.json.JsonConfig;
import work.lclpnet.config.json.JsonConfigFactory;
import work.lclpnet.lobby.decor.maze.MazeConfig;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LobbyConfig implements JsonConfig {

    public static final String DEFAULT_LOBBY_LEVEL_NAME = "lobby";
    public URI lobbySource = URI.create("https://lclpnet.work/dl/lobby-1.19.4");
    public String lobbyLevelName = "lobby";
    public List<MazeConfig> mazeConfigs = new ArrayList<>(List.of(new MazeConfig()));  // one maze by default; mutable
    public BlockPos kingOfLadderGoal = null;
    public List<Vec3d> kingOfLadderDisplays = new ArrayList<>();
    public List<BlockPos> geysers = new ArrayList<>();
    public BlockPos jumpAndRunStart = null;

    public LobbyConfig() {}

    public LobbyConfig(JSONObject obj) {
        if (obj.has("lobby_source")) {
            String lobbySource = obj.getString("lobby_source").replace('\\', '/');
            this.lobbySource = URI.create(lobbySource);
        }

        if (obj.has("lobby_level_name")) {
            this.lobbyLevelName = obj.getString("lobby_level_name");
        }

        if (obj.has("mazes")) {
            JSONArray mazes = obj.getJSONArray("mazes");
            List<MazeConfig> mazeConfigs = new ArrayList<>();

            for (Object entry : mazes) {
                if (!(entry instanceof JSONObject maze)) continue;

                MazeConfig mazeConfig = new MazeConfig(maze);
                mazeConfigs.add(mazeConfig);
            }

            this.mazeConfigs = mazeConfigs;
        }

        if (obj.has("king_of_ladder")) {
            JSONObject kol = obj.getJSONObject("king_of_ladder");

            if (kol.has("goal") && !kol.isNull("goal")) {
                JSONArray tuple = kol.getJSONArray("goal");
                kingOfLadderGoal = ConfigUtil.getBlockPos(tuple);
            }

            if (kol.has("displays")) {
                JSONArray displays = kol.getJSONArray("displays");

                kingOfLadderDisplays = new ArrayList<>();

                for (Object entry : displays) {
                    if (!(entry instanceof JSONArray tuple)) continue;

                    Vec3d pos = ConfigUtil.getVec3d(tuple);
                    kingOfLadderDisplays.add(pos);
                }
            }
        }

        if (obj.has("decoration")) {
            JSONObject decoration = obj.getJSONObject("decoration");

            if (decoration.has("geysers")) {
                JSONArray geysers = decoration.getJSONArray("geysers");

                this.geysers = new ArrayList<>();

                for (Object entry : geysers) {
                    if (!(entry instanceof JSONArray tuple)) continue;

                    BlockPos pos = ConfigUtil.getBlockPos(tuple);
                    this.geysers.add(pos);
                }
            }
        }

        if (obj.has("jump_and_run")) {
            JSONObject jumpAndRun = obj.getJSONObject("jump_and_run");

            if (jumpAndRun.has("start") && !jumpAndRun.isNull("start")) {
                JSONArray tuple = jumpAndRun.getJSONArray("start");
                jumpAndRunStart = ConfigUtil.getBlockPos(tuple);
            }
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

        JSONArray mazes = new JSONArray();
        for (MazeConfig mazeConfig : this.mazeConfigs) {
            mazes.put(mazeConfig.toJson());
        }
        json.put("mazes", mazes);

        JSONObject kol = new JSONObject();
        kol.put("goal", kingOfLadderGoal != null ? ConfigUtil.writeBlockPos(kingOfLadderGoal) : JSONObject.NULL);

        JSONArray displays = new JSONArray();
        for (Vec3d pos : kingOfLadderDisplays) {
            displays.put(ConfigUtil.writeVec3d(pos));
        }

        kol.put("displays", displays);

        json.put("king_of_ladder", kol);

        JSONObject decoration = new JSONObject();
        JSONArray geysers = new JSONArray();

        if (this.geysers != null) {
            for (BlockPos pos : this.geysers) {
                JSONArray tuple = ConfigUtil.writeBlockPos(pos);
                geysers.put(tuple);
            }
        }

        decoration.put("geysers", geysers);
        json.put("decoration", decoration);

        JSONObject jumpAndRun = new JSONObject();
        jumpAndRun.put("start", jumpAndRunStart != null ? ConfigUtil.writeBlockPos(jumpAndRunStart) : JSONObject.NULL);

        json.put("jump_and_run", jumpAndRun);

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
