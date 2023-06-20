package work.lclpnet.activity.component.builtin;

import work.lclpnet.activity.component.Component;
import work.lclpnet.kibu.hook.Hook;
import work.lclpnet.kibu.plugin.hook.HookListenerModule;
import work.lclpnet.kibu.plugin.hook.HookRegistrar;
import work.lclpnet.mplugins.ext.Unloadable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HookComponent implements Component {

    private final LockableHookContainer hooks = new LockableHookContainer();

    @Override
    public void mount() {
        // no-op
    }

    @Override
    public void dismount() {
        hooks.unload();
    }

    public HookRegistrar hooks() {
        return hooks;
    }

    private static class LockableHookContainer implements HookRegistrar, Unloadable {

        private final Object mutex = new Object();
        private final Map<Hook<?>, List<?>> eventListeners = new HashMap<>();
        private boolean locked = false;

        @Override
        @SuppressWarnings("unchecked")
        public final <T> void registerHook(Hook<T> hook, T listener) {
            if (hook == null || listener == null) return;

            synchronized (mutex) {
                if (locked) return;  // do not allow any registrations after this container was locked

                var listeners = (List<T>) eventListeners.computeIfAbsent(hook, h -> new ArrayList<T>());
                listeners.add(listener);
            }

            hook.register(listener);
        }

        @Override
        public final <T> void unregisterHook(Hook<T> hook, T listener) {
            if (listener == null) return;

            synchronized (mutex) {
                var listeners = eventListeners.get(hook);
                if (listeners == null) return;

                listeners.remove(listener);
            }
        }

        @Override
        public void registerHooks(HookListenerModule hooks) {
            if (hooks != null) {
                hooks.registerListeners(this);
            }
        }

        @SuppressWarnings("unchecked")
        private <T> void unregisterListenersOf(Hook<T> hook) {
            var listeners = (List<T>) eventListeners.get(hook);
            listeners.forEach(hook::unregister);
        }

        @Override
        public void unload() {
            synchronized (mutex) {
                locked = true;

                for (Hook<?> hook : eventListeners.keySet()) {
                    unregisterListenersOf(hook);
                }

                eventListeners.clear();
            }
        }
    }
}
