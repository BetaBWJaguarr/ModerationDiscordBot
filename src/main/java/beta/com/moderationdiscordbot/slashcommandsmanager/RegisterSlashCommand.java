package beta.com.moderationdiscordbot.slashcommandsmanager;

import net.dv8tion.jda.api.JDA;

public class RegisterSlashCommand {
    private JDA jda;

    public RegisterSlashCommand(JDA jda) {
        this.jda = jda;
    }

    public void register(String commandName, String commandDescription) {
        jda.upsertCommand(commandName, commandDescription).queue();
    }
}