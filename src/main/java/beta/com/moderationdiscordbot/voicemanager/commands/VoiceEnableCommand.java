package beta.com.moderationdiscordbot.voicemanager.commands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class VoiceEnableCommand extends ListenerAdapter {

    private final ServerSettings serverSettings;
    private final EmbedBuilderManager embedManager;
    private final RateLimit rateLimit;

    public VoiceEnableCommand(ServerSettings serverSettings, LanguageManager languageManager, RateLimit rateLimit) {
        this.serverSettings = serverSettings;
        this.embedManager = new EmbedBuilderManager(languageManager);
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("voiceaction")) {
            String discordServerId = event.getGuild().getId();
            String language = serverSettings.getLanguage(discordServerId);

            String subcommand = event.getSubcommandName();
            if (subcommand != null) {
                switch (subcommand) {
                    case "enable":
                        handleVoiceActionToggle(event, discordServerId, language, true);
                        break;
                    case "disable":
                        handleVoiceActionToggle(event, discordServerId, language, false);
                        break;
                }
            }
        }
    }

    private void handleVoiceActionToggle(SlashCommandInteractionEvent event, String discordServerId, String language, boolean enable) {
        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            sendErrorEmbed(event, "commands.voiceaction.error.title", "commands.voiceaction.error.description", language);
            return;
        }

        if (rateLimit.isRateLimited(event, embedManager, serverSettings)) {
            return;
        }

        boolean success = setVoiceActionEnabled(discordServerId, enable);
        if (success) {
            String titleKey = enable ? "commands.voiceaction.enable.title" : "commands.voiceaction.disable.title";
            String descKey = enable ? "commands.voiceaction.enable.description" : "commands.voiceaction.disable.description";
            Color color = enable ? Color.GREEN : Color.RED;
            sendSuccessEmbed(event, titleKey, descKey, language, color);
        } else {
            sendErrorEmbed(event, "commands.voiceaction.toggle.error.title", "commands.voiceaction.toggle.error.description", language);
        }
    }

    private boolean setVoiceActionEnabled(String discordServerId, boolean enable) {
        try {
            serverSettings.setVoiceAction(discordServerId, enable);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void sendErrorEmbed(SlashCommandInteractionEvent event, String titleKey, String descKey, String language) {
        event.replyEmbeds(embedManager.createEmbedWithColor(titleKey, descKey, language, Color.RED).build())
                .setEphemeral(true)
                .queue();
    }

    private void sendSuccessEmbed(SlashCommandInteractionEvent event, String titleKey, String descKey, String language, Color color) {
        event.replyEmbeds(embedManager.createEmbedWithColor(titleKey, descKey, language, color).build())
                .queue();
    }
}
