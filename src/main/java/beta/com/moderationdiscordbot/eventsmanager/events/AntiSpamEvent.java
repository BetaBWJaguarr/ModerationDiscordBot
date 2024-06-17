package beta.com.moderationdiscordbot.eventsmanager.events;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.AntiSpamCommand;
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
    private static final int TIME_LIMIT = 5; // seconds
    private static final String MUTED_ROLE_NAME = "Muted";

    private final AntiSpamCommand antiSpamCommand;
    private LanguageManager languageManager;
    private ServerSettings serverSettings;

    public AntiSpamEvent(AntiSpamCommand antiSpamCommand, LanguageManager languageManager, ServerSettings serverSettings) {
        this.antiSpamCommand = antiSpamCommand;
        this.languageManager = languageManager;
        this.serverSettings = serverSettings;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (!event.isFromGuild()) {
            return;
        }

        String discorserverid = event.getGuild().getId();
        if (!antiSpamCommand.isAntiSpamEnabled(discorserverid)) {
            return;
        }
        User user = event.getAuthor();
        if (!messageCache.containsKey(user)) {
            messageCache.put(user, new MessageDetails(1, System.currentTimeMillis()));
        } else {
            MessageDetails details = messageCache.get(user);
            details.incrementCount();
            if (details.getCount() >= MESSAGE_LIMIT && (System.currentTimeMillis() - details.getTime()) / 1000 <= TIME_LIMIT) {
                boolean muteSuccessful = muteUser(event, user,discorserverid);
                if (muteSuccessful) {
                    event.getChannel().sendMessageEmbeds(createEmbed(user,discorserverid)).queue();
                }
                details.resetCount();
            }
            details.setTime(System.currentTimeMillis());
        }
    }

    private MessageEmbed createEmbed(User user,String serverid) {
        EmbedBuilder embed = new EmbedBuilder();
        //embed.setTitle("Anti Spam Warning");
        embed.setTitle(languageManager.getMessage("events.antispam.warning_title",serverSettings.getLanguage(serverid)));
        embed.setColor(Color.RED);
        embed.setDescription(
                MessageFormat.format(
                        languageManager.getMessage("events.antispam.warning_description", serverSettings.getLanguage(serverid)),
                        user.getName()
                )
        );
        return embed.build();
    }

    private boolean muteUser(MessageReceivedEvent event, User user,String dcserverid) {
        List<Role> roles = event.getGuild().getRolesByName(MUTED_ROLE_NAME, true);
        if (roles.isEmpty()) {
            List<Member> admins = event.getGuild().getMembers().stream()
                    .filter(member -> member.hasPermission(Permission.ADMINISTRATOR))
                    .collect(Collectors.toList());
            for (Member admin : admins) {
                if (!admin.getUser().isBot()) {
                    admin.getUser().openPrivateChannel().queue(channel ->
                            //channel.sendMessage("The 'Muted' role does not exist. Please create it.").queue());
                            channel.sendMessage(languageManager.getMessage("events.antispam.muted_role_not_exist", serverSettings.getLanguage(dcserverid))).queue());
                }
            }
            return false;
        }
        Role mutedRole = roles.get(0);
        event.getGuild().addRoleToMember(UserSnowflake.fromId(user.getIdLong()), mutedRole).queue(success ->
                event.getGuild().removeRoleFromMember(UserSnowflake.fromId(user.getIdLong()), mutedRole).queueAfter(15, TimeUnit.SECONDS));
        return true;
    }
}