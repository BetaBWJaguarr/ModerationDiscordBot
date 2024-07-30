package beta.com.moderationdiscordbot.autopunish.antiswear;

import beta.com.moderationdiscordbot.autopunish.PunishmentManager;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.MuteLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public class AntiSwear {

    private final ServerSettings serverSettings;
    private final EmbedBuilderManager embedBuilderManager;
    private final LanguageManager languageManager;
    private final PunishmentManager punishmentManager;
    private final MuteLog muteLog;

    public AntiSwear(ServerSettings settings, LanguageManager languageManager, MuteLog muteLog) {
        this.serverSettings = settings;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.languageManager = languageManager;
        this.muteLog = muteLog;
        this.punishmentManager = new PunishmentManager(serverSettings, languageManager, muteLog);
    }

    public boolean containsProfanity(String messageContent, String guildId) {
        if (!serverSettings.getAntiSwearEnabled(guildId)) {
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

    public MessageEmbed handleProfanity(String guildId, Member author, String reason) {
        boolean autoPunishEnabled = serverSettings.isAutoPunishEnabled(guildId);
        boolean antiSwearEnabled = serverSettings.getAntiSwearEnabled(guildId);

        if (autoPunishEnabled && antiSwearEnabled) {
            String punishmentType = serverSettings.getAntiSwearPunishmentType(guildId);

            if ("mute".equalsIgnoreCase(punishmentType)) {
                punishmentManager.mute(author, null, reason, guildId);
            } else {
                punishmentManager.warn(author, reason, guildId);
            }

            EmbedBuilder embedBuilder = embedBuilderManager.createEmbed("events.antiswear", null, serverSettings.getLanguage(guildId));
            embedBuilder.setDescription(author.getAsMention());
            return embedBuilder.build();
        }
        return null;
    }

    public boolean isAutoPunishEnabled(String guildId) {
        return serverSettings.isAutoPunishEnabled(guildId);
    }
}
