package work.lclpnet.game.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.activity.Activity;
import work.lclpnet.game.api.start.GameStartArgs;
import work.lclpnet.translations.loader.TranslationLoader;

/**
 * A factory to create a {@link GameInstance}.
 * Every time a {@link Game} is selected by a game runtime, a {@link GameFactory} is created to construct a game instance.
 * Optionally, a {@link TranslationLoader} can be provided.
 * Translations are loaded by the game runtime and are available with {@link GameEnvironment#getTranslations()} at {@link #createInstance(GameEnvironment)}.
 * @implNote This class can have state, e.g. when creating objects while creating the translation loader,
 * those objects can be stored in a member variable for later use when creating the game instance.
 */
public interface GameFactory {

    /**
     * Provides an optional {@link TranslationLoader} that loads translations for this game.
     * @apiNote This method is called before creating the game instance.
     * @return A translation loader, or null if no translations should be loaded (default).
     */
    @Nullable TranslationLoader createTranslationLoader();

    /**
     * Creates an actual {@link GameInstance} from a {@link GameEnvironment}.
     * Additional state from the implementation member variables can be used as well to construct the instance.
     * @param environment The {@link GameEnvironment}, provided by the game runtime.
     * @return The {@link GameInstance} that the game will take place in.
     */
    @NotNull GameInstance createInstance(@NotNull GameEnvironment environment);

    /**
     * Creates an activity that is active while the lobby has this game selected.
     * The game may also not yet be starting, as start conditions may be not met yet.
     * This may be used to add a voting, a team selector etc.
     * The state resulting of such activities should be passed to {@link #createInstance(GameEnvironment)} in order for it to be used in the actual game instance.
     * @param args The game start arguments.
     * @return The activity that should be active while the lobby has this game selected, or null to skip it.
     */
    default @Nullable Activity createGameSelectedActivity(@NotNull GameStartArgs args) {
        return null;
    }
}
