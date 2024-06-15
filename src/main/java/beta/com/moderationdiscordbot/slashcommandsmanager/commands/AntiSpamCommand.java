package beta.com.moderationdiscordbot.slashcommandsmanager.commands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class AntiSpamCommand extends ListenerAdapter {

    private final ServerSettings serverSettings;

    public AntiSpamCommand(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("antispam")) {
            String discordServerId = event.getGuild().getId();
            String subcommand = event.getSubcommandName();

            if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                EmbedBuilder eb = new EmbedBuilder();

                eb.setColor(Color.RED);
                eb.setTitle("Error");
                eb.setDescription("You do not have the necessary permissions to use this command.");

                event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                return;
            }

            if (subcommand != null) {
                switch (subcommand) {
                    case "messagelimit":
                        int messageLimit = event.getOption("value").getAsInt();
                        serverSettings.setAntiSpamMessageLimit(discordServerId, messageLimit);
                        event.reply("Anti-spam message limit set to " + messageLimit).queue();
                        break;
                    case "timelimit":
                        int timeLimit = event.getOption("value").getAsInt();
                        serverSettings.setAntiSpamTimeLimit(discordServerId, timeLimit);
                        event.reply("Anti-spam time limit set to " + timeLimit).queue();
                        break;
                    default:
                        boolean antiSpamEnabled = serverSettings.getAntiSpam(discordServerId);
                        serverSettings.setAntiSpam(discordServerId, !antiSpamEnabled);
                        String message = !antiSpamEnabled ? "Anti-spam protection enabled." : "Anti-spam protection disabled.";
                        event.reply(message).queue();
                }
            }
        }
    }

    public boolean isAntiSpamEnabled(String discordServerId) {
        return serverSettings.getAntiSpam(discordServerId);
    }
}