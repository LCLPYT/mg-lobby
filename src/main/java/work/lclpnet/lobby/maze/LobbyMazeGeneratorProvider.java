package work.lclpnet.lobby.maze;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import work.lclpnet.maze.MazeGenerator;
import work.lclpnet.maze.MazeGeneratorProvider;
import work.lclpnet.maze.graph.Graph;
import work.lclpnet.maze.impl.BasicMazeGenerator;

import java.util.*;

import static net.minecraft.util.math.Direction.*;

public class LobbyMazeGeneratorProvider implements MazeGeneratorProvider<PositionedNode> {

    private final MazeConfig config;
    private final BlockView world;

    public LobbyMazeGeneratorProvider(MazeConfig config, BlockView world) {
        this.config = config;
        this.world = world;
    }

    @Override
    public MazeGenerator<PositionedNode> createGenerator() {
        if (config.start == null) throw new IllegalStateException("Start position is null");

        final Set<BlockPos> known = new HashSet<>();
        final List<BlockPos> queue = new ArrayList<>();
        queue.add(config.start);
        known.add(config.start);

        final Direction[] directions = new Direction[] {SOUTH, EAST, NORTH, WEST};
        final Map<BlockPos, PositionedNode> nodes = new HashMap<>();

        while (!queue.isEmpty()) {
            final BlockPos pos = queue.remove(0);

            final PositionedNode node = nodes.computeIfAbsent(pos, PositionedNode::new);

            BlockPos rel;

            for (Direction dir : directions) {
                rel = pos.offset(dir, 2);
                if (!config.bounds.contains(rel) || !isReachable(pos, rel)) continue;

                PositionedNode relNode = nodes.computeIfAbsent(rel, PositionedNode::new);
                node.connect(relNode);

                if (!known.contains(rel)) {
                    known.add(rel);
                    queue.add(rel);
                }
            }
        }

        final PositionedNode startNode = nodes.get(config.start);

        LobbyMaze maze = new LobbyMaze(nodes);

        // carve forced passages
        Graph graph = maze.getGraph();

        for (var passage : config.forcePassages) {
            PositionedNode from = nodes.get(passage.left());
            PositionedNode to = nodes.get(passage.right());

            if (from == null || to == null) continue;

            graph.removeEdge(maze.getNodeId(from), maze.getNodeId(to));
        }

        // find start node id
        int start = maze.getNodeId(startNode);

        return new BasicMazeGenerator<>(maze, start);
    }

    private boolean isReachable(BlockPos from, BlockPos to) {
        int minX = Math.min(from.getX(), to.getX());
        int minY = Math.min(from.getY(), to.getY());
        int minZ = Math.min(from.getZ(), to.getZ());
        int maxX = Math.max(from.getX(), to.getX());
        int maxY = Math.max(from.getY(), to.getY()) + 1;
        int maxZ = Math.max(from.getZ(), to.getZ());


        for (BlockPos pos : BlockPos.iterate(minX, minY, minZ, maxX, maxY, maxZ)) {
            BlockState state = world.getBlockState(pos);
            VoxelShape collisionShape = state.getCollisionShape(world, pos);
            if (!collisionShape.isEmpty()) return false;
        }

        return true;
    }
}
