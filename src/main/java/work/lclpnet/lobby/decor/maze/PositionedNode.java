package work.lclpnet.lobby.decor.maze;

import net.minecraft.core.BlockPos;
import work.lclpnet.maze.graph.BasicNode;

public class PositionedNode extends BasicNode {

    private final BlockPos position;

    public PositionedNode(BlockPos position) {
        this.position = position;
    }

    public BlockPos getPosition() {
        return position;
    }
}
