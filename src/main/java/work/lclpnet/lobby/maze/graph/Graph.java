package work.lclpnet.lobby.maze.graph;

import java.util.stream.IntStream;

public interface Graph {

    int getNodeCount();

    IntStream getAdjacent(int node);

    boolean hasEdge(int x, int y);

    void addEdge(int x, int y);

    void removeEdge(int x, int y);

    IntStream streamNodes();
}
