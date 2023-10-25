package work.lclpnet.lobby.game.component;

import work.lclpnet.activity.component.Component;
import work.lclpnet.activity.component.ComponentKey;
import work.lclpnet.lobby.game.impl.prot.BasicProtector;
import work.lclpnet.lobby.game.impl.prot.MutableProtectionConfig;

import java.util.function.Consumer;

public class ProtectorComponent implements Component {

    public static final ComponentKey<ProtectorComponent> KEY = context -> new ProtectorComponent();
    private final MutableProtectionConfig config;
    private final BasicProtector protector;

    public ProtectorComponent() {
        this.config = new MutableProtectionConfig();
        this.protector = new BasicProtector(this.config);
    }

    @Override
    public void mount() {
        protector.activate();
    }

    @Override
    public void dismount() {
        protector.unload();
    }

    public synchronized void configure(Consumer<MutableProtectionConfig> action) {
        protector.deactivate();

        action.accept(config);

        protector.activate();
    }
}
