package work.lclpnet.lobby.decor.maze;

import net.minecraft.world.BlockView;
import org.slf4j.Logger;
import work.lclpnet.lobby.config.ConfigAccess;
import work.lclpnet.lobby.util.BlockStateWriter;
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

    public void create(BlockStateWriter writer, BlockView blockView) {
        for (MazeConfig mazeConfig : configAccess.getConfig().mazeConfigs) {
            createFromConfig(writer, blockView, mazeConfig);
        }
    }

    private void createFromConfig(BlockStateWriter writer, BlockView blockView, MazeConfig mazeConfig) {
        if (mazeConfig.start == null) {
            logger.warn("Maze is not configured, aborting maze generation");
            return;
        }

        synchronized (this) {
            logger.info("Generating maze...");

            LobbyMazeGeneratorProvider provider = new LobbyMazeGeneratorProvider(mazeConfig, blockView);
            MazeGenerationAlgorithm algorithm = RecursiveBacktrackingMazeGenerationAlgorithm.getInstance();
            LobbyMazeOutput output = new LobbyMazeOutput(mazeConfig, writer, logger);

            var creator = new MazeCreator<>(provider, algorithm, output);
            creator.create(new Random());

            logger.info("Maze has been generated");
        }
    }
}
