package work.lclpnet.lobby.decor.maze;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import org.json.JSONArray;
import org.json.JSONObject;
import work.lclpnet.config.json.JsonConfig;
import work.lclpnet.kibu.util.BlockStateUtils;
import work.lclpnet.lobby.config.ConfigUtil;
import work.lclpnet.lobby.decor.maze.geometry.Bounds;

import java.util.ArrayList;
import java.util.List;

public class MazeConfig implements JsonConfig {

    public BlockPos start = null;
    public Bounds bounds = new Bounds(BlockPos.ORIGIN, BlockPos.ORIGIN);
    public BlockState material = Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true);
    public int height = 4;
    public List<Pair<BlockPos, BlockPos>> forcePassages = new ArrayList<>();
    public List<Pair<BlockPos, BlockPos>> exits = new ArrayList<>();

    public MazeConfig() {}

    public MazeConfig(JSONObject json) {
        if (!json.isNull("start")) {
            this.start = ConfigUtil.getBlockPos(json.getJSONArray("start"));
        }

        if (json.has("bounds")) {
            JSONArray positions = json.getJSONArray("bounds");
            if (positions.length() < 2) throw new IllegalArgumentException("Bounds must be of length 2");

            if (positions.isNull(0) || positions.isNull(1)) {
                throw new IllegalArgumentException("bounds must contain two arrays");
            }

            this.bounds = new Bounds(
                    ConfigUtil.getBlockPos(positions.getJSONArray(0)),
                    ConfigUtil.getBlockPos(positions.getJSONArray(1))
            );
        }

        if (json.has("material")) {
            BlockState parsed = BlockStateUtils.parse(json.getString("material"));

            if (parsed != null) {
                this.material = parsed;
            }
        }

        if (json.has("height")) {
            this.height = json.getInt("height");
        }

        if (json.has("force_passages")) {
            JSONArray array = json.getJSONArray("force_passages");

            forcePassages = new ArrayList<>();

            for (Object tupleElement : array) {
                if (!(tupleElement instanceof JSONArray tuple)) continue;

                if (tuple.length() < 2) throw new IllegalArgumentException("Passage tuples must have two elements");

                BlockPos from = ConfigUtil.getBlockPos(tuple.getJSONArray(0));
                BlockPos to = ConfigUtil.getBlockPos(tuple.getJSONArray(1));

                var passage = Pair.of(from, to);
                forcePassages.add(passage);
            }
        }

        if (json.has("exits")) {
            JSONArray array = json.getJSONArray("exits");

            exits = new ArrayList<>();

            for (Object tupleElement : array) {
                if (!(tupleElement instanceof JSONArray tuple)) continue;

                if (tuple.length() < 2) throw new IllegalArgumentException("Exit tuples must have two elements");

                BlockPos from = ConfigUtil.getBlockPos(tuple.getJSONArray(0));
                BlockPos to = ConfigUtil.getBlockPos(tuple.getJSONArray(1));

                var exit = Pair.of(from, to);
                exits.add(exit);
            }
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        if (start != null) {
            json.put("start", ConfigUtil.writeBlockPos(start));
        } else {
            json.put("start", JSONObject.NULL);
        }

        JSONArray bounds = new JSONArray();
        bounds.put(ConfigUtil.writeBlockPos(BlockPos.ofFloored(this.bounds.getMin())));
        bounds.put(ConfigUtil.writeBlockPos(BlockPos.ofFloored(this.bounds.getMax())));

        json.put("bounds", bounds);
        json.put("material", BlockStateUtils.stringify(material));
        json.put("height", height);

        JSONArray forcePassages = new JSONArray();
        for (var passage : this.forcePassages) {
            JSONArray tuple = new JSONArray();
            tuple.put(ConfigUtil.writeBlockPos(passage.left()));
            tuple.put(ConfigUtil.writeBlockPos(passage.right()));

            forcePassages.put(tuple);
        }

        json.put("force_passages", forcePassages);

        JSONArray exits = new JSONArray();
        for (var exit : this.exits) {
            JSONArray tuple = new JSONArray();
            tuple.put(ConfigUtil.writeBlockPos(exit.left()));
            tuple.put(ConfigUtil.writeBlockPos(exit.right()));

            exits.put(tuple);
        }

        json.put("exits", exits);

        return json;
    }
}
