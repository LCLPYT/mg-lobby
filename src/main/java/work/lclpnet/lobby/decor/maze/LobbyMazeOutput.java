package work.lclpnet.lobby.decor.maze;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import work.lclpnet.lobby.util.WorldModifier;
import work.lclpnet.maze.Maze;
import work.lclpnet.maze.MazeOutput;
import work.lclpnet.maze.algorithm.DijkstraAlgorithm;
import work.lclpnet.maze.graph.Graph;
import work.lclpnet.maze.graph.Node;

public class LobbyMazeOutput implements MazeOutput {

    private final MazeConfig config;
    private final WorldModifier writer;
    private final Logger logger;

    public LobbyMazeOutput(MazeConfig config, WorldModifier writer, Logger logger) {
        this.config = config;
        this.writer = writer;
        this.logger = logger;
    }

    @Override
    public void writeMaze(Maze maze) {
        Graph graph = maze.getGraph();

        writeWalls(maze, graph);

        if (maze instanceof LobbyMaze lobbyMaze) {
            writeExit(lobbyMaze);
        }
    }

    private void writeExit(LobbyMaze maze) {
        if (config.exits == null || config.exits.isEmpty()) {
            logger.info("No maze exits were configured, skipping...");
            return;
        }

        Graph passageGraph = maze.createPassageGraph();
        int start = maze.getNodeIdAt(config.start);

        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(passageGraph, start);

        Pair<BlockPos, BlockPos> furthestExit = null;
        int maxDistance = 0;

        for (var exit : config.exits) {
            BlockPos exitPos = exit.left();
            PositionedNode exitNode = maze.getNodeAt(exitPos);

            if (exitNode == null) {
                logger.warn("Invalid exit at {}", exitPos);
                continue;
            }

            int i = maze.getNodeId(exitNode);
            int distance = dijkstra.distanceTo(i);

            if (distance > maxDistance) {
                maxDistance = distance;
                furthestExit = exit;
            }
        }

        if (furthestExit == null) {
            logger.warn("No valid maze exit was found. Skipping...");
            return;
        }

        putExitAt(furthestExit.right());
    }

    private void putExitAt(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        BlockState air = Blocks.AIR.getDefaultState();

        for (BlockPos exitPos : BlockPos.iterate(x, y, z, x, y + 1, z)) {
            writer.setBlockState(exitPos, air);
        }
    }

    private void writeWalls(Maze maze, Graph graph) {
        for (int i : graph.iterateNodes()) {
            Node node = maze.getNode(i);
            if (!(node instanceof PositionedNode posNode)) continue;

            BlockPos pos = posNode.getPosition();

            for (Node adj : node.getAdjacent()) {
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
