package work.lclpnet.lobby.game;

import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

@Singleton
public class GameManager {

    public static final String EMPTY_GAME_ID = "none";
    private final Logger logger;
    private final ServiceLoader<GameProvider> serviceLoader;
    private final Map<String, Game> games = new HashMap<>();
    private Game currentGame = null;

    @Inject
    public GameManager(Logger logger) {
        this.logger = logger;
        this.serviceLoader = ServiceLoader.load(GameProvider.class, getClass().getClassLoader());
    }

    public void reload() {
        for (GameProvider provider : serviceLoader) {
            Game game = provider.provideGame();
            String id = game.getConfig().identifier();

            if (isReservedGameId(id)) {
                logger.error("The game id {} is reserved for internal use", id);
                continue;
            }

            if (games.containsKey(id)) {
                logger.error("A game with id {} already exists (trying to register {})", id, game.getClass().getName());
                continue;
            }

            games.put(id, game);
        }
    }

    private boolean isReservedGameId(String id) {
        return EMPTY_GAME_ID.equalsIgnoreCase(id);
    }

    public Collection<Game> getGames() {
        return games.values();
    }

    @Nullable
    public Game getGame(String id) {
        return games.get(id);
    }

    public void setCurrentGame(@Nullable Game currentGame) {
        this.currentGame = currentGame;
    }

    @Nullable
    public Game getCurrentGame() {
        return currentGame;
    }
}
