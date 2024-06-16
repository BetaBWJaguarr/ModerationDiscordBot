package beta.com.moderationdiscordbot.eventsmanager.events;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.text.MessageFormat;
import java.time.OffsetDateTime;

public class UserJoinLeaveEvents extends ListenerAdapter {

    private LanguageManager languageManager;
    private ServerSettings serverSettings;

    public UserJoinLeaveEvents(LanguageManager languageManager, ServerSettings serverSettings) {
        this.languageManager = languageManager;
        this.serverSettings = serverSettings;
    }


    private void sendMemberEventMessage(TextChannel channel, String action, OffsetDateTime eventTime, String username, String avatarUrl,
                                        int mutualGuilds, int roleCount, boolean isBot, boolean isVerified,String discordserverid) {
        String reliability = calculateReliability(mutualGuilds, roleCount, isBot, isVerified,discordserverid);
        String description = MessageFormat.format(languageManager.getMessage("events.joinquit.user_action",serverSettings.getLanguage(discordserverid)), username, action);
        String isBotMessage = MessageFormat.format(languageManager.getMessage("events.joinquit.is_bot",serverSettings.getLanguage(discordserverid)), isBot);
        String isVerifiedMessage = MessageFormat.format(languageManager.getMessage("events.joinquit.is_verified",serverSettings.getLanguage(discordserverid)), isVerified);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(action + " Event");
        eb.setDescription(description);
        eb.setColor(action.equals("Join") ? Color.GREEN : Color.RED);
        eb.setThumbnail(avatarUrl);
        eb.addField(languageManager.getMessage("events.joinquit.titles.mutual_guilds_title",serverSettings.getLanguage(discordserverid)), String.valueOf(mutualGuilds), true);
        eb.addField(languageManager.getMessage("events.joinquit.titles.role_count_title",serverSettings.getLanguage(discordserverid)), String.valueOf(roleCount), true);
        eb.addField(languageManager.getMessage("events.joinquit.titles.is_bot_title",serverSettings.getLanguage(discordserverid)), isBotMessage, true);
        eb.addField(languageManager.getMessage("events.joinquit.titles.is_verified_title",serverSettings.getLanguage(discordserverid)), isVerifiedMessage, true);
        eb.addField(languageManager.getMessage("events.joinquit.titles.event_time_title",serverSettings.getLanguage(discordserverid)), eventTime.toString(), true);
        eb.addField(languageManager.getMessage("events.joinquit.titles.reliability_title",serverSettings.getLanguage(discordserverid)), reliability, true);

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    private String calculateReliability(int mutualGuilds, int roleCount, boolean isBot, boolean isVerified,String discordserverid) {
        if (mutualGuilds > 5 && !isBot && isVerified && roleCount > 2) {
            return languageManager.getMessage("events.joinquit.reliability.high",serverSettings.getLanguage(discordserverid));
        } else if (mutualGuilds > 3 && !isBot && roleCount > 1) {
            return languageManager.getMessage("events.joinquit.reliability.medium",serverSettings.getLanguage(discordserverid));
        } else {
            return languageManager.getMessage("events.joinquit.reliability.low",serverSettings.getLanguage(discordserverid));
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        String discordserverid = event.getGuild().getId();
        TextChannel channel = (TextChannel) event.getGuild().getDefaultChannel();
        if (channel != null) {
            OffsetDateTime joinDate = event.getMember().getTimeJoined();
            String username = event.getMember().getUser().getName();
            String avatarUrl = event.getMember().getUser().getAvatarUrl();
            int mutualGuilds = event.getJDA().getMutualGuilds(event.getMember().getUser()).size();
            int roleCount = event.getMember().getRoles().size();
            boolean isBot = event.getMember().getUser().isBot();
            boolean isVerified = event.getMember().getUser().getFlags().contains(User.UserFlag.VERIFIED_BOT);

            sendMemberEventMessage(channel, "Join", joinDate, username, avatarUrl, mutualGuilds, roleCount, isBot, isVerified,discordserverid);
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        String discordserverid = event.getGuild().getId();
        TextChannel channel = (TextChannel) event.getGuild().getDefaultChannel();
        if (channel != null) {
            OffsetDateTime leaveDate = OffsetDateTime.now();
            String username = event.getUser().getName();
            String avatarUrl = event.getUser().getAvatarUrl();
            int mutualGuilds = event.getJDA().getMutualGuilds(event.getUser()).size();
            boolean isBot = event.getUser().isBot();
            boolean isVerified = event.getUser().getFlags().contains(User.UserFlag.VERIFIED_BOT);
            int roleCount = 0;
            sendMemberEventMessage(channel, "Leave", leaveDate, username, avatarUrl, mutualGuilds, roleCount, isBot, isVerified,discordserverid);
        }
    }
}