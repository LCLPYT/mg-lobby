package work.lclpnet.lobby.maze;

import net.minecraft.util.math.BlockPos;
import work.lclpnet.maze.Maze;
import work.lclpnet.maze.MazeOutput;
import work.lclpnet.maze.graph.Graph;
import work.lclpnet.maze.graph.Node;

public class LobbyMazeOutput implements MazeOutput<PositionedNode> {

    private final MazeConfig config;
    private final BlockStateWriter writer;

    public LobbyMazeOutput(MazeConfig config, BlockStateWriter writer) {
        this.config = config;
        this.writer = writer;
    }

    @Override
    public void writeMaze(Maze<PositionedNode> maze) {
        Graph graph = maze.getGraph();

        for (int i : graph.iterateNodes()) {
            Node node = maze.getNode(i);
            if (!(node instanceof PositionedNode posNode)) continue;

            BlockPos pos = posNode.getPosition();

            for (Node adj : (Iterable<Node>) node::getAdjacent) {
                if (!(adj instanceof PositionedNode adjPosNode)) continue;

                int j = maze.getNodeId(adjPosNode);
                if (!graph.hasEdge(i, j)) continue;  // passage is clear, therefore do not put a wall

                BlockPos adjPos = adjPosNode.getPosition();
                int dx = (int) Math.signum(adjPos.getX() - pos.getX());
                int dz = (int) Math.signum(adjPos.getZ() - pos.getZ());

                int x = pos.getX() + dx;
                int y = pos.getY();
                int z = pos.getZ() + dz;

                for (BlockPos wallPos : BlockPos.iterate(x, y, z, x, y + config.height - 1, z)) {
                    writer.setBlockState(wallPos, config.material);
                }
            }
        }
    }
}
