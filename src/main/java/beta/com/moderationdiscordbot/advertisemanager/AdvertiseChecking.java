package beta.com.moderationdiscordbot.advertisemanager;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;

public class AdvertiseChecking extends ListenerAdapter {

    private static final List<String> ADVERTISEMENT_KEYWORDS = List.of(
            "buy now", "discount", "free", "visit", "click here", "cheap", "offer", "promo", "sale"
    );

    private static final List<String> WHITELISTED_URLS = List.of(
            "trustedwebsite.com", "ourpartner.com"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(http|https)://(\\w+\\.)?(\\w+\\.\\w+)(/\\w+)?"
    );

    private static final int MIN_MESSAGE_LENGTH = 20;
    private static final int MAX_MESSAGE_DUPLICATES = 3;
    private static final String ADVERTISEMENT_ROLE_EXEMPT = "AdvertiseExempt";

    private static final Logger LOGGER = Logger.getLogger(AdvertiseChecking.class.getName());
    private final ConcurrentHashMap<String, Integer> messageCounts = new ConcurrentHashMap<>();
    private final Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        Member member = event.getMember();

        if (member == null || member.getUser().isBot()) {
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

            if (isAdvertisement(messageContent) || isAdvertisement(userName)) {
                deleteMessage(message);
                message.getGuild().kick(member, "Advertising is not allowed").queue();
                message.getChannel().sendMessage(member.getUser().getAsTag() + ", advertising is not allowed!").queue();
                return;
            }

            if (isDuplicateMessage(userId, messageContent)) {
                deleteMessage(message);
                message.getGuild().kick(member, "Repeated advertising is not allowed").queue();
                message.getChannel().sendMessage(member.getUser().getAsTag() + ", repeated advertising is not allowed!").queue();
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


    private boolean isAdvertisement(String content) {
        if (content.length() < MIN_MESSAGE_LENGTH) {
            return true;
        }

        for (String keyword : ADVERTISEMENT_KEYWORDS) {
            if (content.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }

        if (URL_PATTERN.matcher(content).find()) {
            for (String whitelistUrl : WHITELISTED_URLS) {
                if (content.toLowerCase().contains(whitelistUrl.toLowerCase())) {
                    return false;
                }
            }
            return true;
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
        messageCounts.merge(userId, 1, Integer::sum);
        messageCounts.put(userId + messageContent, messageCounts.getOrDefault(userId + messageContent, 0) + 1);

        int messageCount = messageCounts.get(userId + messageContent);
        if (messageCount > MAX_MESSAGE_DUPLICATES) {
            return true;
        }

        messageCounts.computeIfPresent(userId, (key, count) -> count > 1 ? count - 1 : null);
        messageCounts.computeIfPresent(userId + messageContent, (key, count) -> count > 1 ? count - 1 : null);

        return false;
    }
}
