package work.lclpnet.lobby.maze.generator;

import work.lclpnet.lobby.maze.graph.Graph;

import java.util.Random;
import java.util.Stack;

public class RecursiveBacktrackingMazeGenerator implements MazeGenerator {

    @Override
    public void generateMaze(Graph graph, final int start, Random random) {
        boolean[] visited = new boolean[graph.getNodeCount()];

        Stack<Integer> stack = new Stack<>();
        stack.push(start);
        visited[start] = true;

        while (!stack.isEmpty()) {
            int node = stack.peek();

            int[] adj = graph.getAdjacent(node).filter(i -> !visited[i]).toArray();
            if (adj.length == 0) {
                stack.pop();
                continue;
            }

            int randomIdx = random.nextInt(adj.length);
            int randomAdjNode = adj[randomIdx];

            graph.removeEdge(node, randomAdjNode);

            stack.push(randomAdjNode);
            visited[randomAdjNode] = true;
        }
    }
}
