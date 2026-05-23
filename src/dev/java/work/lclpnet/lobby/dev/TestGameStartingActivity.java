package work.lclpnet.lobby.dev;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import work.lclpnet.activity.ComponentActivity;
import work.lclpnet.activity.component.ComponentBundle;
import work.lclpnet.activity.component.builtin.BuiltinComponents;
import work.lclpnet.game.api.GameContext;
import work.lclpnet.game.api.start.GameStartArgs;
import work.lclpnet.game.api.start.ItemReservationManager;
import work.lclpnet.game.impl.Voting;
import work.lclpnet.game.util.GameStartUtil;
import work.lclpnet.kibu.hook.HookRegistrar;
import work.lclpnet.kibu.scheduler.Ticks;

/**
 * An example activity that will be active while waiting for the game to start in the lobby.
 * It is started once the lobby selects this game and will be stopped either if the game starts or if the lobby changes the starting game to something else.
 */
public class TestGameStartingActivity extends ComponentActivity {

    private final GameStartArgs args;
    private final Voting<String> voting;

    public TestGameStartingActivity(GameStartArgs args, @NonNull Logger logger, Voting<String> voting) {
        GameContext context = args.options().getContext();

        super(context.getServer(), logger);
        
        this.args = args;
        this.voting = voting;
    }

    @Override
    protected void registerComponents(@NotNull ComponentBundle components) {
        // register components to use
        components.add(BuiltinComponents.HOOKS);
    }

    @Override
    public void start() {
        super.start();

        // reservation will be auto-freed when the game is changed / game starts
        ItemReservationManager.Reservation votingItemSlot = args.itemManager().reserve(4);

        if (votingItemSlot != null) {
            HookRegistrar hooks = component(BuiltinComponents.HOOKS).hooks();

            GameStartUtil.setupVoting(voting, hooks, votingItemSlot.slot(), args, Ticks.seconds(15));
        }
    }
}
