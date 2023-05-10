package work.lclpnet.lobby.api.component.builtin;

import work.lclpnet.kibu.plugin.cmd.CommandContainer;
import work.lclpnet.kibu.plugin.cmd.CommandRegistrar;
import work.lclpnet.lobby.api.component.Component;

public class CommandComponent implements Component {

    private final CommandContainer commands = new CommandContainer();

    @Override
    public void mount() {
        // no-op
    }

    @Override
    public void dismount() {
        commands.unload();
    }

    public CommandRegistrar commands() {
        return commands;
    }
}
