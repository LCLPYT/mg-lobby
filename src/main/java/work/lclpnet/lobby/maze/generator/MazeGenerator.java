package work.lclpnet.lobby.maze.generator;

import work.lclpnet.lobby.maze.graph.Graph;

import java.util.Random;

public interface MazeGenerator {

    void generateMaze(Graph graph, int start, Random random);
}
