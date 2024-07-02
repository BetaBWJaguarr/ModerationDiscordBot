package beta.com.moderationdiscordbot.autopunish;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class AutoPunishEnableCommands extends ListenerAdapter {

    private final ServerSettings serverSettings;
    private final EmbedBuilderManager embedManager;
    private final RateLimit rateLimit;

    public AutoPunishEnableCommands(ServerSettings serverSettings, LanguageManager languageManager, RateLimit rateLimit) {
        this.serverSettings = serverSettings;
        this.embedManager = new EmbedBuilderManager(languageManager);
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("autopunish")) {
            String discordServerId = event.getGuild().getId();
            String language = serverSettings.getLanguage(discordServerId);

            if (rateLimit.isRateLimited(event, embedManager, serverSettings)) {
                return;
            }

            if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.replyEmbeds(embedManager.createEmbedWithColor(
                        "commands.autopunish.error.title",
                        "commands.autopunish.error.description",
                        language,
                        Color.RED).build()).setEphemeral(true).queue();
                return;
            }

            String subcommand = event.getSubcommandName();
            if (subcommand != null) {
                switch (subcommand) {
                    case "enable":
                        handleEnableAutoPunish(event, discordServerId, language);
                        break;
                    case "disable":
                        handleDisableAutoPunish(event, discordServerId, language);
                        break;
                }
            }
        }
    }

    private void handleEnableAutoPunish(SlashCommandInteractionEvent event, String discordServerId, String language) {
        serverSettings.setAutoPunishEnabled(discordServerId, true);
        event.replyEmbeds(embedManager.createEmbedWithColor(
                "commands.autopunish.enable.title",
                "commands.autopunish.enable.description",
                language,
                Color.GREEN).build()).queue();
    }

    private void handleDisableAutoPunish(SlashCommandInteractionEvent event, String discordServerId, String language) {
        serverSettings.setAutoPunishEnabled(discordServerId, false);
        event.replyEmbeds(embedManager.createEmbedWithColor(
                "commands.autopunish.disable.title",
                "commands.autopunish.disable.description",
                language,
                Color.GREEN).build()).queue();
    }
}