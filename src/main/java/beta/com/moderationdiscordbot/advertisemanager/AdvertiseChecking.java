package beta.com.moderationdiscordbot.advertisemanager;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class implements a Discord bot feature to detect and manage advertisements
 * in text messages sent by members. It monitors messages for specific keywords,
 * URLs, and duplicate content, taking actions such as deleting messages and kicking
 * members who violate the server's advertising rules. Exempt members with the role
 * "AdvertiseExempt" are excluded from these checks.
 *
 * Features:
 * - Detection of advertisement keywords ("buy now", "discount", etc.) and non-whitelisted URLs.
 * - Prevention of duplicate messages exceeding a defined limit per user.
 * - Utilizes LanguageManager for localized messages based on server settings, ensuring
 *   consistent communication with users in their preferred language.
 * - Integrates ServerSettings for accessing server-specific configurations and language preferences.
 *
 * Dependencies:
 * - LanguageManager: Manages localized messages for different server languages.
 * - ServerSettings: Provides access to server-specific settings and configurations.
 *
 * Usage:
 * Initialize AdvertiseChecking with an instance of LanguageManager and ServerSettings
 * to enable proactive monitoring and enforcement of advertising policies within Discord guilds.
 * This class extends ListenerAdapter to intercept and process MessageReceivedEvent, allowing
 * real-time analysis of messages and enforcement actions as defined by server policies.
 */

public class AdvertiseChecking extends ListenerAdapter {

    private static final List<String> ADVERTISEMENT_KEYWORDS = List.of(
            "buy now", "discount", "free", "visit", "click here", "cheap", "offer", "promo", "sale"
    );

    private static final List<String> WHITELISTED_URLS = List.of(
            "emlaktv24.com", "tunarasimocak.com","tunacraft.com"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(http|https)://(\\w+\\.)?(\\w+\\.\\w+)(/\\w+)?"
    );

    private static final int MIN_MESSAGE_LENGTH = 10;
    private static final int MAX_MESSAGE_DUPLICATES = 3;
    private static final String ADVERTISEMENT_ROLE_EXEMPT = "AdvertiseExempt";

    private static final Logger LOGGER = Logger.getLogger(AdvertiseChecking.class.getName());
    private final ConcurrentHashMap<String, Integer> messageCounts = new ConcurrentHashMap<>();
    private final Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();

    private final LanguageManager languageManager;
    private final ServerSettings serverSettings;

    public AdvertiseChecking(LanguageManager languageManager, ServerSettings serverSettings) {
        this.languageManager = languageManager;
        this.serverSettings = serverSettings;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        Member member = event.getMember();

        if (member == null || member.getUser().isBot()) {
            return;
        }


        if (message.isFromType(ChannelType.TEXT) && message.getType() == MessageType.GUILD_MEMBER_JOIN) {
            return;
        }

        messageQueue.add(message);
        processMessageQueue();


    }

    public void processMessageQueue() {
        Message message;
        while ((message = messageQueue.poll()) != null) {
            LOGGER.info("Processing message from queue: " + message.getContentRaw());
            Member member = message.getMember();
            if (member == null) continue;

            if (hasExemptRole(member)) {
                continue;
            }

            String messageContent = message.getContentRaw();
            String userName = member.getEffectiveName();
            String userId = member.getId();

            if (isAdvertisement(userName, true) || isAdvertisement(messageContent, false)) {
                deleteMessage(message);
                message.getGuild().kick(member, languageManager.getMessage("events.advertising.not_allowed", serverSettings.getLanguage(message.getGuildId()))).queue();
                message.getChannel().sendMessage(member.getUser().getAsTag() + ", " + languageManager.getMessage("events.advertising.not_allowed", serverSettings.getLanguage(message.getGuildId()))).queue();
                return;
            }

            if (isDuplicateMessage(userId, messageContent)) {
                deleteMessage(message);
                message.getGuild().kick(member, languageManager.getMessage("events.advertising.repeated_not_allowed",serverSettings.getLanguage(message.getGuildId()) )).queue();
                message.getChannel().sendMessage(member.getUser().getAsTag() + ", " + languageManager.getMessage("events.advertising.repeated_not_allowed", serverSettings.getLanguage(message.getGuildId()))).queue();
            }
        }

        LOGGER.info("Queue processing complete.");
    }


    private void deleteMessage(Message message) {
        message.delete().queue(
                success -> LOGGER.info("Deleted message: " + message.getContentRaw()),
                error -> LOGGER.warning("Failed to delete message: " + message.getContentRaw())
        );
    }


    private boolean isAdvertisement(String content, boolean isUsername) {
        if (content == null || content.isEmpty()) {
            return false;
        }

        String lowerCaseContent = content.toLowerCase();

        if (!isUsername && lowerCaseContent.length() < MIN_MESSAGE_LENGTH) {
            return true;
        }

        if (ADVERTISEMENT_KEYWORDS.stream().anyMatch(keyword -> lowerCaseContent.contains(keyword))) {
            return true;
        }

        if (URL_PATTERN.matcher(lowerCaseContent).find()) {
            return !WHITELISTED_URLS.stream().anyMatch(whitelistUrl -> lowerCaseContent.contains(whitelistUrl));
        }

        return false;
    }

    private boolean hasExemptRole(Member member) {
        for (Role role : member.getRoles()) {
            if (role.getName().equalsIgnoreCase(ADVERTISEMENT_ROLE_EXEMPT)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDuplicateMessage(String userId, String messageContent) {
        String key = userId + messageContent;
        messageCounts.compute(key, (k, v) -> v == null ? 1 : v + 1);

        int messageCount = messageCounts.get(key);
        if (messageCount > MAX_MESSAGE_DUPLICATES) {
            return true;
        }

        messageCounts.computeIfPresent(userId, (k, v) -> v > 1 ? v - 1 : null);
        messageCounts.computeIfPresent(key, (k, v) -> v > 1 ? v - 1 : null);

        return false;
    }
}
