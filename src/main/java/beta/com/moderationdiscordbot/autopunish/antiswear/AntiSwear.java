package beta.com.moderationdiscordbot.autopunish.antiswear;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public class AntiSwear {

    private final ServerSettings serverSettings;
    private final EmbedBuilderManager embedBuilderManager;
    private final LanguageManager languageManager;

    public AntiSwear(ServerSettings settings, LanguageManager languageManager) {
        this.serverSettings = settings;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.languageManager = languageManager;
    }

    public boolean containsProfanity(String messageContent, String guildId) {
        boolean enabled = serverSettings.getAntiSwearEnabled(guildId);
        if (!enabled) {
            return false;
        }

        List<String> profanities = serverSettings.getAntiSwearWordsList(guildId);
        for (String profanity : profanities) {
            if (messageContent.toLowerCase().contains(profanity.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public MessageEmbed handleProfanity(String guildId, String authorMention) {
        boolean autoPunishEnabled = serverSettings.isAutoPunishEnabled(guildId);
        boolean antiSwearEnabled = serverSettings.getAntiSwearEnabled(guildId);
        EmbedBuilder message = null;

        if (autoPunishEnabled && antiSwearEnabled) {
            EmbedBuilder embedBuilder = embedBuilderManager.createEmbed("events.antiswear", null, serverSettings.getLanguage(guildId));
            embedBuilder.setDescription(authorMention);
            return message.build();
        }
        return null;
    }

    public boolean isAutoPunishEnabled(String guildId) {
        return serverSettings.isAutoPunishEnabled(guildId);
    }
}
