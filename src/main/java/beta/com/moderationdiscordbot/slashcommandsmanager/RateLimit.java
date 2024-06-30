package beta.com.moderationdiscordbot.slashcommandsmanager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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
