package beta.com.moderationdiscordbot.slashcommandsmanager.commands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.text.MessageFormat;

public class AntiSpamCommand extends ListenerAdapter {

    private final ServerSettings serverSettings;
    private LanguageManager languageManager;

    public AntiSpamCommand(ServerSettings serverSettings, LanguageManager languageManager) {
        this.serverSettings = serverSettings;
        this.languageManager = languageManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("antispam")) {
            String discordServerId = event.getGuild().getId();
            String subcommand = event.getSubcommandName();

            if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                EmbedBuilder eb = new EmbedBuilder();

                eb.setColor(Color.RED);
                eb.setTitle(languageManager.getMessage("commands.antispam.error.title", serverSettings.getLanguage(discordServerId)));
                eb.setDescription(languageManager.getMessage("commands.antispam.error.description", serverSettings.getLanguage(discordServerId)));

                event.replyEmbeds(eb.build()).setEphemeral(true).queue();
                return;
            }

            if (subcommand != null) {
                switch (subcommand) {
                    case "messagelimit":
                        int messageLimit = event.getOption("value").getAsInt();
                        serverSettings.setAntiSpamMessageLimit(discordServerId, messageLimit);

                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(Color.GREEN);
                        String title = languageManager.getMessage("commands.antispam.messagelimit.title", serverSettings.getLanguage(discordServerId));
                        String description = MessageFormat.format(languageManager.getMessage("commands.antispam.messagelimit.description", serverSettings.getLanguage(discordServerId)), messageLimit);
                        eb.setTitle(title);
                        eb.setDescription(description);

                        event.replyEmbeds(eb.build()).queue();
                        break;
                    case "timelimit":
                        int timeLimit = event.getOption("value").getAsInt();
                        serverSettings.setAntiSpamTimeLimit(discordServerId, timeLimit);

                        EmbedBuilder ebTime = new EmbedBuilder();
                        ebTime.setColor(Color.GREEN);
                        String titleTime = languageManager.getMessage("commands.antispam.timelimit.title", serverSettings.getLanguage(discordServerId));
                        String descriptionTime = MessageFormat.format(languageManager.getMessage("commands.antispam.timelimit.description", serverSettings.getLanguage(discordServerId)), timeLimit);
                        ebTime.setTitle(titleTime);
                        ebTime.setDescription(descriptionTime);

                        event.replyEmbeds(ebTime.build()).queue();
                        break;
                    default:
                        boolean antiSpamEnabled = serverSettings.getAntiSpam(discordServerId);
                        serverSettings.setAntiSpam(discordServerId, !antiSpamEnabled);

                        EmbedBuilder ebDefault = new EmbedBuilder();
                        ebDefault.setColor(Color.GREEN);
                        ebDefault.setTitle(languageManager.getMessage("commands.antispam.status.title", serverSettings.getLanguage(discordServerId)));
                        String message = !antiSpamEnabled ? "commands.antispam.status.enabled" : "commands.antispam.status.disabled";
                        ebDefault.setDescription(languageManager.getMessage(message, serverSettings.getLanguage(discordServerId)));

                        event.replyEmbeds(ebDefault.build()).queue();
                }
            }
        }
    }

    public boolean isAntiSpamEnabled(String discordServerId) {
        return serverSettings.getAntiSpam(discordServerId);
    }
}