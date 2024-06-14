package beta.com.moderationdiscordbot.slashcommandsmanager;

import beta.com.moderationdiscordbot.startup.Information;
import net.dv8tion.jda.api.JDA;

public class RegisterSlashCommand {
    private JDA jda;
    private Information information;

    public RegisterSlashCommand(JDA jda, Information information) {
        this.jda = jda;
        this.information = information;
    }

    public RegisterSlashCommand register(String commandName, String commandDescription) {
        jda.upsertCommand(commandName, commandDescription).queue();
        information.incrementCommands();
        return this;
    }
}