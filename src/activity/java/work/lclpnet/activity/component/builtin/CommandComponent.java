package work.lclpnet.activity.component.builtin;

import work.lclpnet.activity.component.Component;
import work.lclpnet.kibu.plugin.cmd.CommandContainer;
import work.lclpnet.kibu.plugin.cmd.CommandRegistrar;

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
