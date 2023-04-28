package work.lclpnet.lobby.maze.graph;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class FixedUndirectedGraph implements Graph {

    private final int vertexCount;
    private final boolean[][] adj;

    public FixedUndirectedGraph(int vertexCount) {
        if (vertexCount < 0) throw new IllegalArgumentException("Vertex count cannot be negative");

        this.vertexCount = vertexCount;
        this.adj = new boolean[vertexCount][vertexCount];

        for (int i = 0; i < vertexCount; i++) {
            Arrays.fill(adj[i], false);
        }
    }

    @Override
    public int getNodeCount() {
        return vertexCount;
    }

    @Override
    public IntStream getAdjacent(final int node) {
        Iterator<Integer> adjIterator = new Iterator<>() {
            private int cursor = 0;
            private int next = -1;

            @Override
            public boolean hasNext() {
                // find next adjacent node
                while (next == -1 && cursor < vertexCount) {
                    if (hasEdge(node, cursor)) {
                        next = cursor;
                    }
                    cursor++;
                }

                return next != -1;
            }

            @Override
            public Integer next() {
                if (next == -1) throw new IllegalStateException("Next element not calculated");
                int ret = next;
                next = -1;
                return ret;
            }
        };

        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(adjIterator, Spliterator.ORDERED), false)
                .mapToInt(i -> i);
    }

    @Override
    public boolean hasEdge(int x, int y) {
        return adj[x][y] || adj[y][x];
    }

    @Override
    public void addEdge(int x, int y) {
        if (x == y) throw new IllegalArgumentException("Reflexive nodes are not allowed");
        adj[x][y] = true;
        adj[y][x] = true;
    }

    @Override
    public void removeEdge(int x, int y) {
        adj[x][y] = false;
        adj[y][x] = false;
    }

    @Override
    public IntStream streamNodes() {
        return IntStream.range(0, getNodeCount());
    }
}
