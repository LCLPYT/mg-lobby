package work.lclpnet.lobby.config;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.json.JSONArray;
import org.json.JSONObject;
import work.lclpnet.config.json.JsonConfig;
import work.lclpnet.config.json.JsonConfigFactory;
import work.lclpnet.lobby.decor.greet.GreetingConfig;
import work.lclpnet.lobby.decor.lava.LavaLevitation;
import work.lclpnet.lobby.decor.maze.MazeConfig;

import java.util.ArrayList;
import java.util.List;

public class LobbyWorldConfig implements JsonConfig {

    public List<MazeConfig> mazeConfigs = new ArrayList<>(List.of(new MazeConfig()));  // one maze by default; mutable
    public BlockPos kingOfLadderGoal = null;
    public List<Vec3d> kingOfLadderDisplays = new ArrayList<>();
    public List<BlockPos> geysers = new ArrayList<>();
    public BlockPos jumpAndRunStart = null;
    public List<Pair<BlockPos, BlockPos>> ticTacToeTables = new ArrayList<>();
    public LavaLevitation lavaLevitation = null;
    public GreetingConfig greetingConfig = null;

    public LobbyWorldConfig() {}

    public LobbyWorldConfig(JSONObject obj) {
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

            if (decoration.has("tic_tac_toe")) {
                JSONObject ticTacToe = decoration.getJSONObject("tic_tac_toe");

                if (ticTacToe.has("tables")) {
                    JSONArray tables = ticTacToe.getJSONArray("tables");

                    this.ticTacToeTables = new ArrayList<>();

                    for (Object tableEntry : tables) {
                        if (!(tableEntry instanceof JSONArray table)) continue;

                        if (table.length() < 2) throw new IllegalArgumentException("Table must be of length 2");

                        if (table.isNull(0) || table.isNull(1)) {
                            throw new IllegalArgumentException("table must contain two arrays");
                        }

                        this.ticTacToeTables.add(Pair.of(
                                ConfigUtil.getBlockPos(table.getJSONArray(0)),
                                ConfigUtil.getBlockPos(table.getJSONArray(1))
                        ));
                    }
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

        if (obj.has("lava_levitation") && !obj.isNull("lava_levitation")) {
            lavaLevitation = LavaLevitation.parse(obj.getJSONObject("lava_levitation"));
        }

        if (obj.has("welcome_hologram") && !obj.isNull("welcome_hologram")) {
            greetingConfig = GreetingConfig.parse(obj.getJSONObject("welcome_hologram"));
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        {
            JSONArray mazes = new JSONArray();
            for (MazeConfig mazeConfig : this.mazeConfigs) {
                mazes.put(mazeConfig.toJson());
            }
            json.put("mazes", mazes);
        }

        {
            JSONObject kol = new JSONObject();
            kol.put("goal", kingOfLadderGoal != null ? ConfigUtil.writeBlockPos(kingOfLadderGoal) : JSONObject.NULL);

            JSONArray displays = new JSONArray();
            for (Vec3d pos : kingOfLadderDisplays) {
                displays.put(ConfigUtil.writeVec3d(pos));
            }

            kol.put("displays", displays);

            json.put("king_of_ladder", kol);
        }

        {
            JSONObject decoration = new JSONObject();

            {
                JSONArray geysers = new JSONArray();

                if (this.geysers != null) {
                    for (BlockPos pos : this.geysers) {
                        JSONArray tuple = ConfigUtil.writeBlockPos(pos);
                        geysers.put(tuple);
                    }
                }

                decoration.put("geysers", geysers);
            }

            {
                JSONObject ticTacToe = new JSONObject();
                JSONArray tables = new JSONArray();

                for (var table : ticTacToeTables) {
                    JSONArray tuple = new JSONArray();

                    tuple.put(ConfigUtil.writeBlockPos(table.left()));
                    tuple.put(ConfigUtil.writeBlockPos(table.right()));

                    tables.put(tuple);
                }

                ticTacToe.put("tables", tables);
                decoration.put("tic_tac_toe", ticTacToe);
            }

            json.put("decoration", decoration);
        }

        {
            JSONObject jumpAndRun = new JSONObject();
            jumpAndRun.put("start", jumpAndRunStart != null ? ConfigUtil.writeBlockPos(jumpAndRunStart) : JSONObject.NULL);

            json.put("jump_and_run", jumpAndRun);
        }

        json.put("lava_levitation", lavaLevitation != null ? lavaLevitation.asJson() : JSONObject.NULL);

        json.put("welcome_hologram", greetingConfig != null ? greetingConfig.asJson() : JSONObject.NULL);

        return json;
    }

    public static final JsonConfigFactory<LobbyWorldConfig> FACTORY = new JsonConfigFactory<>() {
        @Override
        public LobbyWorldConfig createDefaultConfig() {
            return new LobbyWorldConfig();
        }

        @Override
        public LobbyWorldConfig createConfig(JSONObject json) {
            return new LobbyWorldConfig(json);
        }
    };
}
