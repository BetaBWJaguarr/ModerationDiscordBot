package beta.com.moderationdiscordbot.slashcommandsmanager.commands.clearcommands.utils;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.Instant;

public class ModLogMessage {

    public static void sendModLogMessage(ServerSettings serverSettings, LanguageManager languageManager, SlashCommandInteractionEvent event, TextChannel textChannel, int amount) {
        String modLogChannelId = serverSettings.getClearLogChannel(event.getGuild().getId());
        if (modLogChannelId != null) {
            TextChannel modLogChannel = event.getJDA().getTextChannelById(modLogChannelId);
            if (modLogChannel != null) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(languageManager.getMessage("commands.clear.log.title", serverSettings.getLanguage(event.getGuild().getId())));
                embedBuilder.addField(languageManager.getMessage("commands.clear.log.channel", serverSettings.getLanguage(event.getGuild().getId())), textChannel.getAsMention(), false);
                embedBuilder.addField(languageManager.getMessage("commands.clear.log.amount", serverSettings.getLanguage(event.getGuild().getId())), String.valueOf(amount), false);
                embedBuilder.setColor(Color.BLUE);
                embedBuilder.setTimestamp(Instant.now());

                modLogChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            }
        }
    }
}
