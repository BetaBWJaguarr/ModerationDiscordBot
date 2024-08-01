package beta.com.moderationdiscordbot.autopunish.antispam;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.MuteLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.autopunish.PunishmentManager;
import beta.com.moderationdiscordbot.autopunish.antispam.commands.AntiSpamCommand;
import beta.com.moderationdiscordbot.utils.MessageDetails;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AntiSpamEvent extends ListenerAdapter {
    private final Map<User, MessageDetails> messageCache = new HashMap<>();
    private static final int MESSAGE_LIMIT = 5;
    private static final int TIME_LIMIT = 5;
    private static final int MUTE_DURATION = 15;
    private static final String MUTED_ROLE_NAME = "Muted";

    private final AntiSpamCommand antiSpamCommand;
    private final LanguageManager languageManager;
    private final ServerSettings serverSettings;
    private final PunishmentManager punishmentManager;
    private final MuteLog muteLog;

    public AntiSpamEvent(AntiSpamCommand antiSpamCommand, LanguageManager languageManager, ServerSettings serverSettings, MuteLog muteLog) {
        this.antiSpamCommand = antiSpamCommand;
        this.languageManager = languageManager;
        this.serverSettings = serverSettings;
        this.muteLog = muteLog;
        this.punishmentManager = new PunishmentManager(serverSettings, languageManager,muteLog);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!event.isFromGuild()) {
            return;
        }

        if (event.getAuthor().isBot()) {
            return;
        }

        if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        String discordServerId = event.getGuild().getId();

        if (!serverSettings.isAutoPunishEnabled(discordServerId)) {
            return;
        }

        if (!antiSpamCommand.isAntiSpamEnabled(discordServerId)) {
            return;
        }

        User user = event.getAuthor();
        long currentTime = System.currentTimeMillis();

        messageCache.computeIfAbsent(user, k -> new MessageDetails(1, currentTime));

        MessageDetails details = messageCache.get(user);
        details.incrementCount();

        String messageContent = event.getMessage().getContentRaw();

        long upperCaseLetters = messageContent.chars().filter(Character::isUpperCase).count();
        if ((double) upperCaseLetters / messageContent.length() >= 0.55) {
            event.getMessage().delete().queue();
        }

        if (messageContent.contains("http://") || messageContent.contains("https://")) {
            event.getMessage().delete().queue();
        }

        if (details.getCount() >= MESSAGE_LIMIT && (currentTime - details.getTime()) / 1000 <= TIME_LIMIT) {
            handlePunishment(event, user, discordServerId);
            details.resetCount();
        }

        details.setTime(currentTime);
    }

    private void handlePunishment(MessageReceivedEvent event, User user, String discordServerId) {
        String punishmentType = serverSettings.getAntiSpamPunishmentType(discordServerId);

        if ("mute".equalsIgnoreCase(punishmentType)) {
            boolean muteSuccessful = muteUser(event, user, discordServerId);
            if (muteSuccessful) {
                event.getChannel().sendMessageEmbeds(createEmbed(user, discordServerId)).queue();
            }
        } else if ("warn".equalsIgnoreCase(punishmentType)) {
            punishmentManager.warn(event.getMember(), languageManager.getMessage("events.antispam.warn_reason", serverSettings.getLanguage(discordServerId)), discordServerId);
            event.getChannel().sendMessageEmbeds(createEmbed(user, discordServerId)).queue();
        }
    }

    private MessageEmbed createEmbed(User user, String serverId) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(languageManager.getMessage("events.antispam.warning_title", serverSettings.getLanguage(serverId)));
        embed.setColor(Color.RED);
        embed.setDescription(
                MessageFormat.format(
                        languageManager.getMessage("events.antispam.warning_description", serverSettings.getLanguage(serverId)),
                        user.getName()
                )
        );
        return embed.build();
    }

    private boolean muteUser(MessageReceivedEvent event, User user, String discordServerId) {
        List<Role> roles = event.getGuild().getRolesByName(MUTED_ROLE_NAME, true);

        if (roles.isEmpty()) {
            List<Member> admins = event.getGuild().getMembers().stream()
                    .filter(member -> member.hasPermission(Permission.ADMINISTRATOR))
                    .collect(Collectors.toList());
            for (Member admin : admins) {
                if (!admin.getUser().isBot()) {
                    admin.getUser().openPrivateChannel().queue(channel ->
                            channel.sendMessage(languageManager.getMessage("events.antispam.muted_role_not_exist", serverSettings.getLanguage(discordServerId))).queue());
                }
            }
            return false;
        }

        Role mutedRole = roles.get(0);
        event.getGuild().addRoleToMember(UserSnowflake.fromId(user.getIdLong()), mutedRole).queue(success ->
                event.getGuild().removeRoleFromMember(UserSnowflake.fromId(user.getIdLong()), mutedRole).queueAfter(MUTE_DURATION, TimeUnit.SECONDS));
        return true;
    }
}
