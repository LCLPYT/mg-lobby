package work.lclpnet.lobby.game.start;

import work.lclpnet.kibu.scheduler.api.RunningTask;
import work.lclpnet.kibu.scheduler.api.SchedulerAction;

import java.util.concurrent.atomic.AtomicBoolean;

public class PeriodicConditionBroadcast implements SchedulerAction {

    private final AtomicBoolean starting;
    private final AtomicBoolean started;
    private final int interval;
    private final Runnable action;
    private int t = 0;
    private boolean prevStarting = false;

    public PeriodicConditionBroadcast(AtomicBoolean starting, AtomicBoolean started, int interval, Runnable action) {
        this.starting = starting;
        this.started = started;
        this.interval = interval;
        this.action = action;
    }

    @Override
    public void run(RunningTask info) {
        if (started.get()) {
            info.cancel();
            return;
        }

        boolean starting = this.starting.get();

        if (starting != prevStarting) {
            prevStarting = starting;
            t = 0;
        }

        if (starting) return;

        if (t <= 0) {
            t = interval;
            action.run();
        } else {
            t--;
        }
    }
}
