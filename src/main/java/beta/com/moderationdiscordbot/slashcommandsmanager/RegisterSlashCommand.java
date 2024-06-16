package beta.com.moderationdiscordbot.slashcommandsmanager;

import beta.com.moderationdiscordbot.startup.Information;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

public class RegisterSlashCommand {
    private JDA jda;
    private Information information;

    public RegisterSlashCommand(JDA jda, Information information) {
        this.jda = jda;
        this.information = information;
    }

    public RegisterSlashCommand register(String commandName, String commandDescription, Object... optionsAndSubcommands) {
        CommandCreateAction commandAction = jda.upsertCommand(commandName, commandDescription);

        for (Object obj : optionsAndSubcommands) {
            if (obj instanceof OptionData) {
                commandAction = commandAction.addOptions((OptionData) obj);
            } else if (obj instanceof SubcommandData) {
                commandAction = commandAction.addSubcommands((SubcommandData) obj);
            }
        }

        commandAction.queue();
        information.incrementCommands();
        return this;
    }
}