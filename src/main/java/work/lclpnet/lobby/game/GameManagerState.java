package work.lclpnet.lobby.game;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.lobby.game.api.Game;

import java.util.Objects;

public class GameManagerState {

    public static final String CURRENT_GAME_KEY = "CurrentGame";
    @Nullable
    private Game currentGame = null;
    private boolean dirty = false;

    private void markDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setCurrentGame(@Nullable Game currentGame) {
        if (Objects.equals(this.currentGame, currentGame)) return;

        this.currentGame = currentGame;

        markDirty();
    }

    @Nullable
    public Game getCurrentGame() {
        return currentGame;
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();

        if (currentGame != null) {
            String gameId = currentGame.getConfig().identifier();
            nbt.putString(CURRENT_GAME_KEY, gameId);
        }

        return nbt;
    }

    public void fromNbt(CompoundTag nbt, GameMangerLoader loader) {
        if (!nbt.contains(CURRENT_GAME_KEY)) return;

        String gameId = nbt.getStringOr(CURRENT_GAME_KEY, null);

        if (gameId == null) return;

        Game game = loader.getGame(gameId);

        if (game != null) {
            setCurrentGame(game);
        }
    }
}
