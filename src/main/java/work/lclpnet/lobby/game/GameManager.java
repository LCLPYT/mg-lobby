package work.lclpnet.lobby.game;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.slf4j.Logger;
import work.lclpnet.lobby.game.api.Game;
import work.lclpnet.lobby.game.api.GameProvider;
import work.lclpnet.plugin.load.PluginClassLoader;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
public class GameManager implements GameMangerLoader {

    public static final String EMPTY_GAME_ID = "none";
    private final Logger logger;
    private final ServiceLoader<GameProvider> serviceLoader;
    private final GameStateIo stateManager;
    private final Set<Runnable> stateChangeCallbacks = new ObjectOpenHashSet<>(4);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock(), writeLock = lock.writeLock();
    private Map<String, Game> games = Map.of();
    private boolean restored = false;

    @Inject
    public GameManager(Logger logger, GameStateIo stateManager) {
        this.logger = logger;
        this.stateManager = stateManager;
        this.serviceLoader = ServiceLoader.load(GameProvider.class, getClass().getClassLoader());
    }

    public void reload() {
        if (!(getClass().getClassLoader() instanceof PluginClassLoader)) {
            logger.warn("Class loader of {} should be an instance of {} but is {}. Some features may not work as expected",
                    GameManager.class.getName(), PluginClassLoader.class.getName(), getClass().getClassLoader().getClass().getName());
        }

        serviceLoader.reload();

        Map<String, Game> games = new HashMap<>();

        for (GameProvider provider : serviceLoader) {
            Game game = provider.provideGame();
            String id = game.getConfig().identifier();

            if (isReservedGameId(id)) {
                logger.error("The game path {} is reserved for internal use", id);
                continue;
            }

            if (games.containsKey(id)) {
                logger.error("A game with path {} already exists (trying to register {})", id, game.getClass().getName());
                continue;
            }

            games.put(id, game);
        }

        try {
            writeLock.lock();
            this.games = Map.copyOf(games);
        } finally {
            writeLock.unlock();
        }

        restoreState();
    }

    private boolean isReservedGameId(String id) {
        return EMPTY_GAME_ID.equalsIgnoreCase(id);
    }

    public Collection<Game> getGames() {
        try {
            readLock.lock();
            return games.values();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @Nullable
    public Game getGame(String id) {
        try {
            readLock.lock();
            return games.get(id);
        } finally {
            readLock.unlock();
        }
    }

    public void setCurrentGame(@Nullable Game currentGame) {
        stateManager.getState().setCurrentGame(currentGame);
        saveState();
    }

    @Nullable
    public Game getCurrentGame() {
        return stateManager.getState().getCurrentGame();
    }

    private void restoreState() {
        stateManager.restore(this)
                .thenRun(this::onRestored)
                .exceptionally(error -> {
                    logger.error("Failed to restore GameManager state", error);
                    return null;
                });
    }

    private void saveState() {
        if (!stateManager.getState().isDirty()) return;

        stateManager.store().exceptionally(error -> {
            logger.error("Failed to save GameManager state", error);
            return null;
        });
    }

    private void onRestored() {
        synchronized (this) {
            this.restored = true;

            for (Runnable callback : stateChangeCallbacks) {
                callback.run();
            }
        }
    }

    public void addStateChangeListener(Runnable callback) {
        Objects.requireNonNull(callback);

        boolean alreadyRestored;

        synchronized (this) {
            stateChangeCallbacks.add(callback);

            alreadyRestored = this.restored;
        }

        if (alreadyRestored) {
            callback.run();
        }
    }

    public void removeStateChangeListener(Runnable callback) {
        synchronized (this) {
            stateChangeCallbacks.remove(callback);
        }
    }
}
