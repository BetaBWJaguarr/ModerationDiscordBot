package beta.com.moderationdiscordbot.autopunish.antispam.commands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class AntiSpamCommand extends ListenerAdapter {

    private final ServerSettings serverSettings;
    private final EmbedBuilderManager embedManager;
    private final RateLimit rateLimit;
    private final HandleErrors errorManager;

    public AntiSpamCommand(ServerSettings serverSettings, LanguageManager languageManager, RateLimit rateLimit, HandleErrors errorManager) {
        this.serverSettings = serverSettings;
        this.embedManager = new EmbedBuilderManager(languageManager);
        this.rateLimit = rateLimit;
        this.errorManager = errorManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (!event.getName().equals("antispam")) {
                return;
            }

            String discordServerId = event.getGuild().getId();
            String language = serverSettings.getLanguage(discordServerId);

            String subcommand = event.getSubcommandName();
            if (subcommand != null) {
                switch (subcommand) {
                    case "messagelimit":
                        handleSetMessageLimit(event, discordServerId, language);
                        break;
                    case "timelimit":
                        handleSetTimeLimit(event, discordServerId, language);
                        break;
                    case "enable":
                        handleToggleAntiSpam(event, discordServerId, language);
                        break;
                    case "disable":
                        handleToggleAntiSpam(event, discordServerId, language);
                        break;
                }
            }
        } catch (Exception e) {
            errorManager.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }

    private void handleSetMessageLimit(SlashCommandInteractionEvent event, String discordServerId, String language) {

        if (!checkPermissionsAndRateLimit(event, language)) {
            return;
        }
        try {
            int messageLimit = event.getOption("value").getAsInt();
            serverSettings.setAntiSpamMessageLimit(discordServerId, messageLimit);
            event.replyEmbeds(embedManager.createEmbedWithColor(
                    "commands.antispam.messagelimit.title",
                    "commands.antispam.messagelimit.description",
                    language,
                    Color.GREEN,
                    messageLimit).build()).queue();
        } catch (Exception e) {
            errorManager.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }

    private void handleSetTimeLimit(SlashCommandInteractionEvent event, String discordServerId, String language) {
        if (!checkPermissionsAndRateLimit(event, language)) {
            return;
        }

        try {
            int timeLimit = event.getOption("value").getAsInt();
            serverSettings.setAntiSpamTimeLimit(discordServerId, timeLimit);
            event.replyEmbeds(embedManager.createEmbedWithColor(
                    "commands.antispam.timelimit.title",
                    "commands.antispam.timelimit.description",
                    language,
                    Color.GREEN,
                    timeLimit).build()).queue();
        } catch (Exception e) {
            errorManager.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }

    private void handleToggleAntiSpam(SlashCommandInteractionEvent event, String discordServerId, String language) {

        if (!checkPermissionsAndRateLimit(event, language)) {
            return;
        }

        try {
            boolean antiSpamEnabled = serverSettings.getAntiSpam(discordServerId);
            serverSettings.setAntiSpam(discordServerId, !antiSpamEnabled);
            String message = !antiSpamEnabled ? "commands.antispam.status.enabled" : "commands.antispam.status.disabled";
            event.replyEmbeds(embedManager.createEmbedWithColor(
                    "commands.antispam.status.title",
                    message,
                    language,
                    Color.GREEN).build()).queue();
        } catch (Exception e) {
            errorManager.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }


    private boolean checkPermissionsAndRateLimit(SlashCommandInteractionEvent event, String language) {
        if (rateLimit.isRateLimited(event, embedManager, serverSettings)) {
            return false;
        }

        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.replyEmbeds(embedManager.createEmbedWithColor(
                    "commands.antispam.error.title",
                    "commands.antispam.error.description",
                    language,
                    Color.RED).build()).setEphemeral(true).queue();
            return false;
        }

        return true;
    }

    public boolean isAntiSpamEnabled(String discordServerId) {
        return serverSettings.getAntiSpam(discordServerId);
    }
}
