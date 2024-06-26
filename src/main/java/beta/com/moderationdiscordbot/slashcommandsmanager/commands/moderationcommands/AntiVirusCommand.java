package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class AntiVirusCommand extends ListenerAdapter {

    private final ServerSettings serverSettings;
    private final EmbedBuilderManager embedManager;
    private final RateLimit rateLimit;

    public AntiVirusCommand(ServerSettings serverSettings, LanguageManager languageManager, RateLimit rateLimit) {
        this.serverSettings = serverSettings;
        this.embedManager = new EmbedBuilderManager(languageManager);
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("antivirus")) {
            String discordServerId = event.getGuild().getId();
            String language = serverSettings.getLanguage(discordServerId);

            if (rateLimit.isRateLimited(event, embedManager, serverSettings)) {
                return;
            }

            if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                event.replyEmbeds(embedManager.createEmbedWithColor(
                        "commands.antivirus.error.title",
                        "commands.antivirus.error.description",
                        language,
                        Color.RED).build()).setEphemeral(true).queue();
                return;
            }

            handleToggleAntiVirus(event, discordServerId, language);
        }
    }

    private void handleToggleAntiVirus(SlashCommandInteractionEvent event, String discordServerId, String language) {
        boolean antiVirusEnabled = serverSettings.getAntiVirus(discordServerId);
        serverSettings.setAntiVirus(discordServerId, !antiVirusEnabled);
        String message = !antiVirusEnabled ? "commands.antivirus.status.enabled" : "commands.antivirus.status.disabled";
        event.replyEmbeds(embedManager.createEmbedWithColor(
                "commands.antivirus.status.title",
                message,
                language,
                Color.GREEN).build()).queue();
    }

    public boolean isAntiVirusEnabled(String discordServerId) {
        return serverSettings.getAntiVirus(discordServerId);
    }
}
