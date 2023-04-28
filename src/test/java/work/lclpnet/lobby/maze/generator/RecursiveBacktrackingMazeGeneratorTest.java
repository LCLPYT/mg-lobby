package work.lclpnet.lobby.maze.generator;

import org.junit.jupiter.api.Test;
import work.lclpnet.lobby.maze.graph.FixedUndirectedGraph;
import work.lclpnet.lobby.maze.graph.Graph;

import java.util.Arrays;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.IntPredicate;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecursiveBacktrackingMazeGeneratorTest {

    private static final int GRID_WIDTH = 5;
    private static final int GRID_HEIGHT = 5;
    private static final char WALL = '#', EMPTY = ' ';

    @Test
    void generateMaze_acyclic() {
        Graph graph = gridGraph();

        IntPredicate hasCycle = i -> new CycleChecker(graph).inCycle(i);

        // every node must be in some kind of cycle
        assertTrue(graph.streamNodes().parallel().allMatch(hasCycle));

        MazeGenerator generator = new RecursiveBacktrackingMazeGenerator();
        generator.generateMaze(graph, 0, new Random());

        // no node should be in a cycle, because every node was connected from the beginning
        assertTrue(graph.streamNodes().parallel().noneMatch(hasCycle));
    }

    @Test
    void deterministic() {
        Random random = new Random(123);
        Graph graph = gridGraph();
        MazeGenerator generator = new RecursiveBacktrackingMazeGenerator();

        // make sure the maze generation is reproducible with the same inputs
        assertEquals("""
###########
# # # # # #
###########
# # # # # #
###########
# # # # # #
###########
# # # # # #
###########
# # # # # #
###########
                """.trim(), stringifyGrid(graph).replaceAll("\\r\\n?", "\n"));

        generator.generateMaze(graph, 0, random);

        assertEquals("""
###########
# #       #
# ####### #
#   #   # #
### # # # #
# # # #   #
# # # #####
#   #     #
# ####### #
#         #
###########
                """.trim(), stringifyGrid(graph).replaceAll("\\r\\n?", "\n"));
    }

    private String stringifyGrid(Graph graph) {
        // prepare board
        char[][] board = getStringBoard();

        // carve walls
        BiFunction<Integer, Integer, Integer> node = (x, y) -> GRID_WIDTH * y + x;
        UnaryOperator<Integer> ix = (i) -> i % GRID_WIDTH;
        UnaryOperator<Integer> iy = (i) -> i / GRID_WIDTH;
        BiConsumer<Integer, Integer> removeIf = (a, b) -> {
            if (!graph.hasEdge(a, b)) {
                int x = ix.apply(a);
                int y = iy.apply(a);

                int dx = ix.apply(b) - x;
                int dy = iy.apply(b) - y;

                board[2 * y + 1 + dy][2 * x + 1 + dx] = EMPTY;
            }
        };

        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (x < GRID_WIDTH - 1) {  // right
                    removeIf.accept(node.apply(x, y), node.apply(x + 1, y));
                }
                if (x > 0) {  // left
                    removeIf.accept(node.apply(x, y), node.apply(x - 1, y));
                }
                if (y > 0) {  // up
                    removeIf.accept(node.apply(x, y), node.apply(x, y - 1));
                }
                if (y < GRID_HEIGHT - 1) {  // down
                    removeIf.accept(node.apply(x, y), node.apply(x, y + 1));
                }
            }
        }

        // to string
        StringBuilder builder = new StringBuilder();

        boolean first = true;

        for (char[] row : board) {
            if (first) first = false;
            else builder.append(System.lineSeparator());

            builder.append(new String(row));
        }

        return builder.toString();
    }

    private char[][] getStringBoard() {
        final int h = 2 * GRID_HEIGHT + 1;
        final int w = 2 * GRID_WIDTH + 1;

        char[][] board = new char[h][w];

        for (int y = 0; y < h; y++) {
            if (y % 2 == 0) {
                Arrays.fill(board[y], WALL);
                continue;
            }

            for (int x = 0; x < w; x++) {
                if (x % 2 == 0) {
                    board[y][x] = WALL;
                } else {
                    board[y][x] = EMPTY;
                }
            }
        }

        return board;
    }

    private Graph gridGraph() {
        FixedUndirectedGraph graph = new FixedUndirectedGraph(GRID_WIDTH * GRID_HEIGHT);

        BiFunction<Integer, Integer, Integer> node = (x, y) -> GRID_WIDTH * y + x;

        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH - 1; x++) {
                graph.addEdge(node.apply(x, y), node.apply(x + 1, y));
            }
        }

        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT - 1; y++) {
                graph.addEdge(node.apply(x, y), node.apply(x, y + 1));
            }
        }

        return graph;
    }

    private static class CycleChecker {

        private final Graph graph;
        private final boolean[] dfsVisited;

        private CycleChecker(Graph graph) {
            this.graph = graph;
            this.dfsVisited = new boolean[graph.getNodeCount()];
        }

        public boolean inCycle(int node) {
            return inCycle(node, -1);
        }

        private boolean inCycle(int node, int parent) {
            dfsVisited[node] = true;

            var iterator = graph.getAdjacent(node).iterator();

            while (iterator.hasNext()) {
                int n = iterator.nextInt();

                if (!dfsVisited[n]) {
                    if (inCycle(n, node)) {
                        return true;
                    }

                    continue;
                }

                if (n != parent) {
                    return true;
                }
            }

            return false;
        }
    }
}