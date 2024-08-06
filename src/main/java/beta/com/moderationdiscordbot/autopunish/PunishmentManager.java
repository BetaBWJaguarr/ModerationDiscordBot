package beta.com.moderationdiscordbot.autopunish;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.MuteLog;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.utils.FormatDuration;
import beta.com.moderationdiscordbot.utils.ModLogEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

public class PunishmentManager {

    private static final long DEFAULT_MUTE_DURATION = 10800;

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final ModLogEmbed modLogEmbed;
    private final MuteLog muteLog;

    public PunishmentManager(ServerSettings serverSettings, LanguageManager languageManager, MuteLog muteLog) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.modLogEmbed = new ModLogEmbed(languageManager, serverSettings);
        this.muteLog = muteLog;
    }

    public void warn(Member targetMember, String reason, String serverId) {
        String language = serverSettings.getLanguage(serverId);
        String dmDescription = MessageFormat.format(languageManager.getMessage("commands.warn.dm_description", language), reason);

        MessageEmbed dmEmbed = new EmbedBuilder()
                .setTitle(languageManager.getMessage("commands.warn.dm_title", language))
                .setDescription(dmDescription)
                .setColor(Color.ORANGE)
                .build();

        targetMember.getUser().openPrivateChannel().queue(
                privateChannel -> privateChannel.sendMessageEmbeds(dmEmbed).queue(
                        success -> System.out.println("Warn DM sent to " + targetMember.getUser().getAsTag()),
                        error -> System.err.println("Failed to send warn DM: " + error.getMessage())
                ),
                error -> System.err.println("Failed to open private channel: " + error.getMessage())
        );

        modLogEmbed.sendLog(serverId, null, "commands.warn.log.title", "commands.warn.log.user", "commands.warn.log.reason", targetMember.getUser().getName(), reason);
    }

    public void mute(Member targetMember, Long durationInSeconds, String reason, String serverId) {
        durationInSeconds = durationInSeconds != null ? durationInSeconds : DEFAULT_MUTE_DURATION;

        List<Role> muteRoles = targetMember.getGuild().getRolesByName("Muted", true);
        if (muteRoles.isEmpty()) {
            System.err.println("Mute role not found.");
            return;
        }

        Role muteRole = muteRoles.get(0);
        Long finalDurationInSeconds = durationInSeconds;
        targetMember.getGuild().addRoleToMember(targetMember, muteRole).queue(
                success -> {
                    sendMuteNotification(targetMember, reason, finalDurationInSeconds, serverId);
                    muteLog.addMuteLog(serverId, targetMember.getUser().getId(), reason, finalDurationInSeconds == -2 ? null : new Date(System.currentTimeMillis() + finalDurationInSeconds * 1000L));
                },
                error -> System.err.println("Failed to mute " + targetMember.getUser().getAsTag() + ": " + error.getMessage())
        );
    }

    private void sendMuteNotification(Member mutedMember, String reason, long durationInSeconds, String serverId) {
        String durationFormatted = FormatDuration.formatDuration(durationInSeconds);
        String language = serverSettings.getLanguage(serverId);

        MessageEmbed embed = embedBuilderManager.createEmbed("commands.mute.dm_notification", null, language)
                .setColor(Color.RED)
                .setDescription(String.format(languageManager.getMessage("commands.mute.notification.description", language), mutedMember.getGuild().getName()))
                .addField(languageManager.getMessage("commands.mute.notification.muted_by", language), "System", false)
                .addField(languageManager.getMessage("commands.mute.notification.reason", language), reason, false)
                .addField(languageManager.getMessage("commands.mute.notification.duration", language), durationFormatted, false)
                .setTimestamp(new Date().toInstant())
                .build();

        mutedMember.getUser().openPrivateChannel().queue(
                privateChannel -> privateChannel.sendMessageEmbeds(embed).queue(),
                error -> System.err.println("Failed to send mute notification DM: " + error.getMessage())
        );
    }
}
