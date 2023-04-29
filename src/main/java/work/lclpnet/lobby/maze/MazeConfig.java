package work.lclpnet.lobby.maze;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import org.json.JSONArray;
import org.json.JSONObject;
import work.lclpnet.config.json.JsonConfig;
import work.lclpnet.kibu.util.BlockStateUtils;
import work.lclpnet.lobby.config.ConfigUtil;

public class MazeConfig implements JsonConfig {

    public BlockPos start = null;
    public final BlockPos[] bounds = new BlockPos[2];
    public BlockState material = Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true);
    public int height = 4;

    public MazeConfig() {}

    public MazeConfig(JSONObject json) {
        if (!json.isNull("start")) {
            this.start = ConfigUtil.getBlockPos(json.getJSONArray("start"));
        }

        if (json.has("bounds")) {
            JSONArray positions = json.getJSONArray("bounds");
            if (positions.length() < 2) throw new IllegalArgumentException("Bounds must be of length 2");

            if (!positions.isNull(0)) {
                this.bounds[0] = ConfigUtil.getBlockPos(positions.getJSONArray(0));
            }

            if (!positions.isNull(1)) {
                this.bounds[1] = ConfigUtil.getBlockPos(positions.getJSONArray(1));
            }
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
        if (this.bounds[0] != null) {
            bounds.put(ConfigUtil.writeBlockPos(this.bounds[0]));
        } else {
            bounds.put(JSONObject.NULL);
        }

        if (this.bounds[1] != null) {
            bounds.put(ConfigUtil.writeBlockPos(this.bounds[1]));
        } else {
            bounds.put(JSONObject.NULL);
        }

        json.put("bounds", bounds);

        json.put("material", BlockStateUtils.stringify(material));

        json.put("height", height);

        return json;
    }
}
