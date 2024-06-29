package beta.com.moderationdiscordbot.eventsmanager.events;



import beta.com.moderationdiscordbot.autopunish.AntiSwear;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoPunishEvent extends ListenerAdapter {

    private final ScheduledExecutorService executorService;

    public AutoPunishEvent() {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        List<TextChannel> allChannels = event.getGuild().getTextChannels();

        allChannels.forEach(channel -> {
            executorService.schedule(() -> {
                channel.getIterableHistory().forEachAsync(message -> {
                    if (AntiSwear.containsProfanity(message.getContentRaw().toLowerCase())) {
                        AntiSwear.handleProfanity(event.getGuild(), message);
                    }
                    return true;
                });
            }, 0, TimeUnit.SECONDS);
        });
    }
}
