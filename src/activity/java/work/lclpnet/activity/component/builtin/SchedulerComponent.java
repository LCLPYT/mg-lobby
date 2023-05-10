package work.lclpnet.activity.component.builtin;

import work.lclpnet.activity.component.Component;
import work.lclpnet.activity.component.ComponentContext;
import work.lclpnet.kibu.scheduler.KibuScheduling;
import work.lclpnet.kibu.scheduler.api.Scheduler;

import java.util.function.Supplier;

public class SchedulerComponent implements Component, Supplier<Scheduler> {

    private final Scheduler scheduler;

    public SchedulerComponent(ComponentContext environment) {
        this.scheduler = new Scheduler(environment.getLogger());
    }

    @Override
    public void mount() {
        KibuScheduling.getRootScheduler().addChild(scheduler);
    }

    @Override
    public void dismount() {
        KibuScheduling.getRootScheduler().removeChild(scheduler);
    }

    @Override
    public Scheduler get() {
        return scheduler;
    }
}
