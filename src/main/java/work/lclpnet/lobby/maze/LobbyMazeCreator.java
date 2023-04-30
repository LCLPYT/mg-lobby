package work.lclpnet.lobby.maze;

import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.slf4j.Logger;
import work.lclpnet.lobby.config.ConfigAccess;
import work.lclpnet.maze.MazeCreator;
import work.lclpnet.maze.algorithm.MazeGenerationAlgorithm;
import work.lclpnet.maze.algorithm.RecursiveBacktrackingMazeGenerationAlgorithm;

import java.util.Random;

public class LobbyMazeCreator {

    private final ConfigAccess configAccess;
    private final Logger logger;

    public LobbyMazeCreator(ConfigAccess configAccess, Logger logger) {
        this.configAccess = configAccess;
        this.logger = logger;
    }

    public void create(World world) {
        MazeConfig mazeConfig = configAccess.getConfig().mazeConfig;
        if (mazeConfig.start == null) {
            logger.warn("Maze is not configured, aborting maze generation");
            return;
        }

        logger.info("Generating maze...");

        LobbyMazeGeneratorProvider provider = new LobbyMazeGeneratorProvider(mazeConfig, world);
        MazeGenerationAlgorithm algorithm = RecursiveBacktrackingMazeGenerationAlgorithm.getInstance();
        LobbyMazeOutput output = new LobbyMazeOutput(mazeConfig, world);

        var creator = new MazeCreator<>(provider, algorithm, output);
        creator.create(new Random());

        logger.info("Maze has been generated");
    }

    public void reset(BlockView world) {

    }
}
