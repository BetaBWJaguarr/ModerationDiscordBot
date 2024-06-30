package beta.com.moderationdiscordbot.eventsmanager.events;

import beta.com.moderationdiscordbot.autopunish.antiswear.AntiSwear;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoPunishEvent extends ListenerAdapter {

    private final ScheduledExecutorService executorService;
    private final AntiSwear antiSwear;

    public AutoPunishEvent(AntiSwear antiSwear) {
        this.antiSwear = antiSwear;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String guildId = event.getGuild().getId();
        if (!antiSwear.isAutoPunishEnabled(guildId)) {
            return;
        }

        List<TextChannel> allChannels = event.getGuild().getTextChannels();

        allChannels.forEach(channel -> {
            executorService.schedule(() -> {
                channel.getIterableHistory().forEachAsync(message -> {
                    if (antiSwear.containsProfanity(message.getContentRaw().toLowerCase(), guildId)) {
                        MessageEmbed embed = antiSwear.handleProfanity(guildId, message.getAuthor().getAsMention());
                        if (embed != null) {
                            channel.sendMessageEmbeds(embed).queue();
                        }
                    }
                    return true;
                });
            }, 0, TimeUnit.SECONDS);
        });
    }
}
