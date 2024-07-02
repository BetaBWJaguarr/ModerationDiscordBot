package beta.com.moderationdiscordbot.autopunish.antiswear.commands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class AntiSwearCommand extends ListenerAdapter {

    private final ServerSettings serverSettings;
    private final EmbedBuilderManager embedManager;
    private final RateLimit rateLimit;

    public AntiSwearCommand(ServerSettings serverSettings, LanguageManager languageManager, RateLimit rateLimit) {
        this.serverSettings = serverSettings;
        this.embedManager = new EmbedBuilderManager(languageManager);
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("antiswear")) {
            String discordServerId = event.getGuild().getId();
            String language = serverSettings.getLanguage(discordServerId);

            String subcommand = event.getSubcommandName();
            if (subcommand != null) {
                switch (subcommand) {
                    case "enable":
                        handleToggleAntiSwear(event, discordServerId, language, true);
                        break;
                    case "disable":
                        handleToggleAntiSwear(event, discordServerId, language, false);
                        break;
                }
            }
        }
    }


    private void handleToggleAntiSwear(SlashCommandInteractionEvent event, String discordServerId, String language, boolean enable) {
        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.replyEmbeds(embedManager.createEmbedWithColor(
                    "commands.antiswear.error.title",
                    "commands.antiswear.error.description",
                    language,
                    Color.RED).build()).setEphemeral(true).queue();
            return;
        }

        if (rateLimit.isRateLimited(event, embedManager, serverSettings)) {
            return;
        }

        if (enable) {
            handleEnableAntiSwear(event, discordServerId, language);
        } else {
            handleDisableAntiSwear(event, discordServerId, language);
        }
    }

    private void handleEnableAntiSwear(SlashCommandInteractionEvent event, String discordServerId, String language) {
        serverSettings.setAntiSwearEnabled(discordServerId, true);
        event.replyEmbeds(embedManager.createEmbedWithColor(
                "commands.antiswear.enable.title",
                "commands.antiswear.enable.description",
                language,
                Color.GREEN).build()).queue();
    }

    private void handleDisableAntiSwear(SlashCommandInteractionEvent event, String discordServerId, String language) {
        serverSettings.setAntiSwearEnabled(discordServerId, false);
        event.replyEmbeds(embedManager.createEmbedWithColor(
                "commands.antiswear.disable.title",
                "commands.antiswear.disable.description",
                language,
                Color.GREEN).build()).queue();
    }
}