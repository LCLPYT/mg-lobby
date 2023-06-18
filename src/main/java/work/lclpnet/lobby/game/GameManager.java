package work.lclpnet.lobby.game;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import work.lclpnet.lobby.LobbyAPI;
import work.lclpnet.lobby.game.conf.GameConfig;
import work.lclpnet.lobby.game.conf.MinecraftGameConfig;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class GameManager {

    public static final String EMPTY_GAME_ID = "none";
    private final Logger logger;
    private final ServiceLoader<GameProvider> serviceLoader;
    private final Map<String, Game> games = new HashMap<>();
    private Game currentGame = null;

    public GameManager(Logger logger) {
        this.logger = logger;
        this.serviceLoader = ServiceLoader.load(GameProvider.class, getClass().getClassLoader());
    }

    public void reload() {
        for (GameProvider provider : serviceLoader) {
            Game game = provider.provideGame();
            String id = game.getConfig().identifier();

            if (reservedGameId(id)) {
                logger.error("The game id {} is reserved for internal use", id);
                continue;
            }

            if (games.containsKey(id)) {
                logger.error("A game with id {} already exists (trying to register {})", id, game.getClass().getName());
                continue;
            }

            games.put(id, game);
        }

        games.put("test", new Game() {  // remove after testing is done
            @Override
            public GameConfig getConfig() {
                return new MinecraftGameConfig("test", "Test Game", new ItemStack(Items.STRUCTURE_VOID));
            }

            @Override
            public boolean canStart() {
                MinecraftServer server = LobbyAPI.getInstance().getManager().getLobbyWorld().getServer();
                return !PlayerLookup.all(server).isEmpty();
            }

            @Override
            public void start() {
                System.out.println("The test game was started!");
            }
        });
    }

    private boolean reservedGameId(String id) {
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
