package beta.com.moderationdiscordbot.eventsmanager.events;

import beta.com.moderationdiscordbot.autopunish.antiswear.AntiSwear;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoPunishEvent extends ListenerAdapter {

    private final ScheduledExecutorService executorService;
    private final AntiSwear antiSwear;
    private final LanguageManager languageManager;

    public AutoPunishEvent(AntiSwear antiSwear, LanguageManager languageManager) {
        this.antiSwear = antiSwear;
        this.languageManager = languageManager;
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


        executorService.schedule(() -> {
            if (antiSwear.containsProfanity(event.getMessage().getContentRaw().toLowerCase(), guildId)) {
                MessageEmbed embed = antiSwear.handleProfanity(guildId, event.getMember(), languageManager.getMessage("events.antiswear-event.description", guildId));
                if (embed != null) {
                    event.getMessage().delete().queue();
                    event.getChannel().sendMessageEmbeds(embed).queue();
                }
            }
        }, 0, TimeUnit.SECONDS);
    }
}
