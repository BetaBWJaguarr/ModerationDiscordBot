package beta.com.moderationdiscordbot.autopunish;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.List;

public class AntiSwear extends ListenerAdapter {

    private static boolean enabled = false;

    private static final List<String> profanities = Arrays.asList("swear1", "swear2", "swear3");

    public static void setEnabled(boolean enabled) {
        AntiSwear.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean containsProfanity(String message) {
        if (!enabled) {
            return false;
        }

        for (String profanity : profanities) {
            if (message.contains(profanity)) {
                return true;
            }
        }
        return false;
    }

    public static void handleProfanity(Guild guild, Message message) {
        TextChannel textChannel = message.getChannel().asTextChannel();

        message.delete().queue();
        textChannel.sendMessage(message.getAuthor().getAsMention() + ", Please do not swear!!").queue();
    }
}
