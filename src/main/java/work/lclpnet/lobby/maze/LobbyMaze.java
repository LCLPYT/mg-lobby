package work.lclpnet.lobby.maze;

import net.minecraft.util.math.BlockPos;
import work.lclpnet.maze.graph.Graphs;
import work.lclpnet.maze.impl.SimpleMaze;

import java.util.Map;
import java.util.Objects;

public class LobbyMaze extends SimpleMaze<PositionedNode> {

    private final Map<BlockPos, PositionedNode> nodes;

    public LobbyMaze(Map<BlockPos, PositionedNode> nodes) {
        super(nodes.values(), Graphs::undirected);
        this.nodes = nodes;
    }

    public PositionedNode getNodeAt(BlockPos pos) {
        return nodes.get(pos);
    }

    public int getNodeIdAt(BlockPos pos) {
        return getNodeId(Objects.requireNonNull(getNodeAt(pos)));
    }
}
