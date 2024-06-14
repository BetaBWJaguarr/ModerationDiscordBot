package beta.com.moderationdiscordbot.slashcommandsmanager.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AntiSpamCommand extends ListenerAdapter {

    private boolean antiSpamEnabled = true;


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("antispam")) {
            antiSpamEnabled = !antiSpamEnabled;
            String message = antiSpamEnabled ? "Anti-spam protection enabled." : "Anti-spam protection disabled.";
            event.reply(message).queue();
        }
    }

    public boolean isAntiSpamEnabled() {
        return antiSpamEnabled;
    }
}