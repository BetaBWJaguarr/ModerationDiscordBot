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
import java.time.OffsetDateTime;

public class UserJoinLeaveEvents extends ListenerAdapter {

    private final LanguageManager languageManager;
    private final ServerSettings serverSettings;

    public UserJoinLeaveEvents(LanguageManager languageManager, ServerSettings serverSettings) {
        this.languageManager = languageManager;
        this.serverSettings = serverSettings;
    }

    private void sendMemberEventMessage(TextChannel channel, String action, OffsetDateTime eventTime, String avatarUrl,
                                        int mutualGuilds, int roleCount, boolean isBot, boolean isVerified, String discordServerId) {
        String language = serverSettings.getLanguage(discordServerId);
        String reliability = calculateReliability(mutualGuilds, roleCount, isBot, isVerified, language);

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(action.equals("Join") ? languageManager.getMessage("events.joinquit.titles.join_title", language) : languageManager.getMessage("events.joinquit.titles.leave_title", language))
                .setColor(action.equals("Join") ? Color.GREEN : Color.RED)
                .setThumbnail(avatarUrl)
                .addField(languageManager.getMessage("events.joinquit.titles.mutual_guilds_title", language), String.valueOf(mutualGuilds), true)
                .addField(languageManager.getMessage("events.joinquit.titles.role_count_title", language), String.valueOf(roleCount), true)
                .addField(languageManager.getMessage("events.joinquit.titles.is_bot_title", language), String.valueOf(isBot), true)
                .addField(languageManager.getMessage("events.joinquit.titles.is_verified_title", language), String.valueOf(isVerified), true)
                .addField(languageManager.getMessage("events.joinquit.titles.event_time_title", language), eventTime.toString(), true)
                .addField(languageManager.getMessage("events.joinquit.titles.reliability_title", language), reliability, true);

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    private String calculateReliability(int mutualGuilds, int roleCount, boolean isBot, boolean isVerified, String language) {
        if (mutualGuilds > 5 && !isBot && isVerified && roleCount > 2) {
            return languageManager.getMessage("events.joinquit.reliability.high", language);
        } else if (mutualGuilds > 3 && !isBot && roleCount > 1) {
            return languageManager.getMessage("events.joinquit.reliability.medium", language);
        } else {
            return languageManager.getMessage("events.joinquit.reliability.low", language);
        }
    }

    private void handleMemberEvent(User user, TextChannel channel, String action, OffsetDateTime eventTime, String discordServerId) {
        if (channel != null) {
            int mutualGuilds = user.getJDA().getMutualGuilds(user).size();
            boolean isBot = user.isBot();
            boolean isVerified = user.getFlags().contains(User.UserFlag.VERIFIED_BOT);
            int roleCount = action.equals("Join") ? user.getMutualGuilds().stream()
                    .filter(g -> g.getId().equals(discordServerId))
                    .findFirst()
                    .map(g -> g.getMember(user))
                    .map(m -> m.getRoles().size())
                    .orElse(0) : 0;

            sendMemberEventMessage(channel, action, eventTime, user.getAvatarUrl(), mutualGuilds, roleCount, isBot, isVerified, discordServerId);
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        handleMemberEvent(event.getMember().getUser(), (TextChannel) event.getGuild().getDefaultChannel(), "Join", event.getMember().getTimeJoined(), event.getGuild().getId());
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        handleMemberEvent(event.getUser(), (TextChannel) event.getGuild().getDefaultChannel(), "Leave", OffsetDateTime.now(), event.getGuild().getId());
    }
}
