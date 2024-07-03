package beta.com.moderationdiscordbot.utils;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.Instant;

public class ModLogEmbed {

    private final LanguageManager languageManager;
    private final ServerSettings serverSettings;

    public ModLogEmbed(LanguageManager languageManager, ServerSettings serverSettings) {
        this.languageManager = languageManager;
        this.serverSettings = serverSettings;
    }

    public void sendLog(String dcserverid, SlashCommandInteractionEvent event, String titleKey, String userKey, String reasonKey, String username, String reason) {
        String modLogChannelId = serverSettings.getModLogChannel(dcserverid);

        if (modLogChannelId != null) {
            TextChannel modLogChannel = event.getJDA().getTextChannelById(modLogChannelId);
            if (modLogChannel != null) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(languageManager.getMessage(titleKey, serverSettings.getLanguage(dcserverid)));

                if (username != null) {
                    embedBuilder.addField(languageManager.getMessage(userKey, serverSettings.getLanguage(dcserverid)),username,false);
                } else {
                    embedBuilder.setDescription(languageManager.getMessage(userKey, serverSettings.getLanguage(dcserverid)));
                }

                if (reason != null && reasonKey != null) {
                    embedBuilder.addField(languageManager.getMessage(reasonKey, serverSettings.getLanguage(dcserverid)), reason, false);
                }

                embedBuilder.setColor(Color.RED);
                embedBuilder.setTimestamp(Instant.now());

                modLogChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            }
        }
    }
}
