package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class AntiSpamCommand extends ListenerAdapter {

    private final ServerSettings serverSettings;
    private final EmbedBuilderManager embedManager;

    public AntiSpamCommand(ServerSettings serverSettings, LanguageManager languageManager) {
        this.serverSettings = serverSettings;
        this.embedManager = new EmbedBuilderManager(languageManager);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("antispam")) {
            String discordServerId = event.getGuild().getId();
            String language = serverSettings.getLanguage(discordServerId);

            if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                event.replyEmbeds(embedManager.createEmbedWithColor(
                        "commands.antispam.error.title",
                        "commands.antispam.error.description",
                        language,
                        Color.RED).build()).setEphemeral(true).queue();
                return;
            }

            String subcommand = event.getSubcommandName();
            if (subcommand != null) {
                switch (subcommand) {
                    case "messagelimit":
                        handleSetMessageLimit(event, discordServerId, language);
                        break;
                    case "timelimit":
                        handleSetTimeLimit(event, discordServerId, language);
                        break;
                    default:
                        handleToggleAntiSpam(event, discordServerId, language);
                }
            }
        }
    }

    private void handleSetMessageLimit(SlashCommandInteractionEvent event, String discordServerId, String language) {
        int messageLimit = event.getOption("value").getAsInt();
        serverSettings.setAntiSpamMessageLimit(discordServerId, messageLimit);
        event.replyEmbeds(embedManager.createEmbedWithColor(
                "commands.antispam.messagelimit.title",
                "commands.antispam.messagelimit.description",
                language,
                Color.GREEN,
                messageLimit).build()).queue();
    }

    private void handleSetTimeLimit(SlashCommandInteractionEvent event, String discordServerId, String language) {
        int timeLimit = event.getOption("value").getAsInt();
        serverSettings.setAntiSpamTimeLimit(discordServerId, timeLimit);
        event.replyEmbeds(embedManager.createEmbedWithColor(
                "commands.antispam.timelimit.title",
                "commands.antispam.timelimit.description",
                language,
                Color.GREEN,
                timeLimit).build()).queue();
    }

    private void handleToggleAntiSpam(SlashCommandInteractionEvent event, String discordServerId, String language) {
        boolean antiSpamEnabled = serverSettings.getAntiSpam(discordServerId);
        serverSettings.setAntiSpam(discordServerId, !antiSpamEnabled);
        String message = !antiSpamEnabled ? "commands.antispam.status.enabled" : "commands.antispam.status.disabled";
        event.replyEmbeds(embedManager.createEmbedWithColor(
                "commands.antispam.status.title",
                message,
                language,
                Color.GREEN).build()).queue();
    }

    public boolean isAntiSpamEnabled(String discordServerId) {
        return serverSettings.getAntiSpam(discordServerId);
    }
}
