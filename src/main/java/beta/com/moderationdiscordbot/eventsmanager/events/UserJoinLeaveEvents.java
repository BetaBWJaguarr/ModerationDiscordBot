package beta.com.moderationdiscordbot.eventsmanager.events;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.databasemanager.VerifySystem.VerifyMongo;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.memberverifysystem.MemberVerifySystem;
import beta.com.moderationdiscordbot.memberverifysystem.Status;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.UUID;

public class UserJoinLeaveEvents extends ListenerAdapter {

    private final LanguageManager languageManager;
    private final ServerSettings serverSettings;
    private final VerifyMongo verifyMongo;


    public UserJoinLeaveEvents(LanguageManager languageManager, ServerSettings serverSettings, VerifyMongo verifyMongo) {
        this.languageManager = languageManager;
        this.serverSettings = serverSettings;
        this.verifyMongo = verifyMongo;
    }

    private void sendMemberEventMessage(TextChannel channel, String action, OffsetDateTime eventTime, String avatarUrl,
                                        boolean isBot,int RoleCount,boolean GifAvatar, boolean isVerified, String discordServerId, boolean hasProfilePicture,User user) {
        String language = serverSettings.getLanguage(discordServerId);
        String reliability = calculateReliability(isBot,GifAvatar, isVerified, user.getTimeCreated(), hasProfilePicture, language);

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(action.equals("Join") ? languageManager.getMessage("events.joinquit.titles.join_title", language) : languageManager.getMessage("events.joinquit.titles.leave_title", language))
                .setColor(action.equals("Join") ? Color.GREEN : Color.RED)
                .setThumbnail(avatarUrl)
                .addField(languageManager.getMessage("events.joinquit.titles.role_count_title", language), String.valueOf(RoleCount), true)
                .addField(languageManager.getMessage("events.joinquit.titles.is_bot_title", language), String.valueOf(isBot), true)
                .addField(languageManager.getMessage("events.joinquit.titles.is_verified_title", language), String.valueOf(isVerified), true)
                .addField(languageManager.getMessage("events.joinquit.titles.event_time_title", language), eventTime.toString(), true)
                .addField(languageManager.getMessage("events.joinquit.titles.reliability_title", language), reliability, true);

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    private String calculateReliability(boolean isBot,boolean isGifAvatar, boolean isVerified, OffsetDateTime accountCreationDate, boolean hasProfilePicture, String language) {
        Period accountAge = Period.between(accountCreationDate.toLocalDate(), LocalDate.now());
        int accountAgeYears = accountAge.getYears();
        boolean isAccountOldEnough = accountAgeYears > 1;
        boolean isProfileComplete = hasProfilePicture && !isGifAvatar;

        if (!isBot && isVerified && isAccountOldEnough && isProfileComplete) {
            return languageManager.getMessage("events.joinquit.reliability.high", language);
        } else if (!isBot && isAccountOldEnough) {
            return languageManager.getMessage("events.joinquit.reliability.medium", language);
        } else {
            return languageManager.getMessage("events.joinquit.reliability.low", language);
        }
    }

    private void handleMemberEvent(User user, TextChannel channel, String action, OffsetDateTime eventTime, String discordServerId) {
        if (channel != null) {
            boolean isBot = user.isBot();
            boolean isVerified = user.getFlags().contains(User.UserFlag.VERIFIED_BOT);
            boolean hasProfilePicture = user.getAvatarUrl() != null;
            boolean hasGifAvatar = user.getAvatarUrl() != null && user.getAvatarUrl().endsWith(".gif");
            int roleCount = action.equals("Join") ? user.getMutualGuilds().stream()
                    .filter(g -> g.getId().equals(discordServerId))
                    .findFirst()
                    .map(g -> g.getMember(user))
                    .map(m -> m.getRoles().size())
                    .orElse(0) : 0;

            MemberVerifySystem memberVerify = new MemberVerifySystem(
                    UUID.randomUUID(),
                    user.getName(),
                    0,
                    Status.PENDING
            );


            verifyMongo.upsertMemberVerifySystem(memberVerify);

            sendMemberEventMessage(channel, action, eventTime, user.getAvatarUrl(), isBot,roleCount,hasGifAvatar, isVerified, discordServerId, hasProfilePicture,user);

            String modLogChannelId = serverSettings.getModLogChannel(discordServerId);
            if (modLogChannelId != null) {
                TextChannel modLogChannel = channel.getJDA().getTextChannelById(modLogChannelId);
                if (modLogChannel != null) {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle(languageManager.getMessage("events.joinquit.log.title", serverSettings.getLanguage(discordServerId)));
                    String userMessageTemplate = languageManager.getMessage("events.joinquit.log.user", serverSettings.getLanguage(discordServerId));
                    String formattedUserMessage = MessageFormat.format(userMessageTemplate, user.getName());
                    embedBuilder.addField(formattedUserMessage, "", false);

                    String actionMessageTemplate = languageManager.getMessage("events.joinquit.log.action", serverSettings.getLanguage(discordServerId));
                    String formattedActionMessage = MessageFormat.format(actionMessageTemplate, action);
                    embedBuilder.addField(formattedActionMessage, "", false);
                    embedBuilder.setColor(action.equals("Join") ? Color.GREEN : Color.RED);
                    embedBuilder.setTimestamp(OffsetDateTime.now());

                    modLogChannel.sendMessageEmbeds(embedBuilder.build()).queue();
                }
            }
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
