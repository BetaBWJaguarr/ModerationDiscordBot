package beta.com.moderationdiscordbot.slashcommandsmanager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * The {@code RateLimit} class is responsible for managing rate limits for users issuing commands in a Discord bot.
 * It ensures that users can only issue commands at a specified rate, preventing spam and potential abuse.
 * The rate limit is specified in a given time unit (e.g., seconds, minutes).
 * If a user exceeds the rate limit, they will receive a message indicating how much time remains until they can issue another command.
 * <p>
 * This class utilizes a {@code ConcurrentHashMap} to store user timestamps, ensuring thread-safe operations.
 * The {@code cooldown} period is stored in milliseconds and is initialized through the constructor.
 * </p>
 * <p>
 * The class provides methods to check if a user is allowed to issue a command, to get the remaining cooldown time
 * for a user, and to handle rate-limited responses in a {@code SlashCommandInteractionEvent}.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * RateLimit rateLimit = new RateLimit(30, TimeUnit.SECONDS);
 * if (!rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
 *     // Handle command execution
 * }
 * }
 * </pre>
 * </p>
 */

public class RateLimit {

    private final ConcurrentHashMap<String, Long> userTimestamps = new ConcurrentHashMap<>();
    private final long cooldown;

    public RateLimit(long cooldown, TimeUnit unit) {
        this.cooldown = unit.toMillis(cooldown);
    }

    public boolean isAllowed(String userId) {
        long currentTime = System.currentTimeMillis();
        Long lastTime = userTimestamps.get(userId);

        if (lastTime == null || (currentTime - lastTime) >= cooldown) {
            userTimestamps.put(userId, currentTime);
            return true;
        }

        return false;
    }

    public long getRemainingTime(String userId) {
        long currentTime = System.currentTimeMillis();
        Long lastTime = userTimestamps.get(userId);

        if (lastTime == null) {
            return 0;
        }

        return cooldown - (currentTime - lastTime);
    }

    public boolean isRateLimited(SlashCommandInteractionEvent event, EmbedBuilderManager embedBuilderManager, ServerSettings serverSettings) {
        String userId = event.getUser().getId();
        String dcserverid = event.getGuild().getId();

        if (!isAllowed(userId)) {
            long remainingTime = getRemainingTime(userId);
            event.replyEmbeds(embedBuilderManager.createEmbed("commands.rate_limit", null, serverSettings.getLanguage(dcserverid), String.valueOf(remainingTime / 1000)).build())
                    .setEphemeral(true)
                    .queue();
            return true;
        }

        return false;
    }
}
