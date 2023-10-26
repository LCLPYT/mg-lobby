package work.lclpnet.lobby.game;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import work.lclpnet.lobby.game.api.Game;

import javax.annotation.Nullable;
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

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();

        if (currentGame != null) {
            String gameId = currentGame.getConfig().identifier();
            nbt.putString(CURRENT_GAME_KEY, gameId);
        }

        return nbt;
    }

    public void fromNbt(NbtCompound nbt, GameMangerLoader loader) {
        if (nbt.contains(CURRENT_GAME_KEY, NbtElement.STRING_TYPE)) {
            String gameId = nbt.getString(CURRENT_GAME_KEY);

            Game game = loader.getGame(gameId);

            if (game != null) {
                setCurrentGame(game);
            }
        }
    }
}
