package work.lclpnet.lobby.di;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.kibu.scheduler.api.Scheduler;
import work.lclpnet.lobby.decor.seat.DefaultSeatProvider;
import work.lclpnet.lobby.decor.seat.SeatProvider;
import work.lclpnet.lobby.util.ResetWorldModifier;
import work.lclpnet.lobby.util.WorldModifier;

@Module(includes = ActivityModule.Bindings.class)
public class ActivityModule {

    @Module
    interface Bindings {
        @Binds
        WorldModifier bindWorldModifier(ResetWorldModifier impl);
    }

    private final HookRegistrar hookRegistrar;
    private final Scheduler scheduler;

    public ActivityModule(HookRegistrar hookRegistrar, Scheduler scheduler) {
        this.hookRegistrar = hookRegistrar;
        this.scheduler = scheduler;
    }

    @Provides
    HookRegistrar provideHookRegistrar() {
        return hookRegistrar;
    }

    @Provides
    Scheduler provideScheduler() {
        return scheduler;
    }

    @Provides
    SeatProvider provideSeatProvider() {
        return DefaultSeatProvider.getInstance();
    }
}
