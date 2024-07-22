package beta.com.moderationdiscordbot.utils;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.Instant;


/**
 * The {@code ModLogEmbed} class is responsible for sending moderation log messages to a specified moderation log channel.
 * It constructs and sends an embedded message with details about the moderation action, including the user involved and the reason for the action.
 * <p>
 * This class utilizes {@link LanguageManager} for localization and {@link ServerSettings} for retrieving server-specific settings.
 *
 * <p><b>Fields:</b></p>
 * <ul>
 * <li>{@code languageManager}: The {@link LanguageManager} instance used for retrieving localized messages.</li>
 * <li>{@code serverSettings}: The {@link ServerSettings} instance used for retrieving server-specific settings.</li>
 * </ul>
 *
 * <p><b>Constructor:</b></p>
 * <ul>
 * <li>{@code ModLogEmbed(LanguageManager languageManager, ServerSettings serverSettings)}: Constructs a {@code ModLogEmbed} instance with the specified {@code LanguageManager} and {@code ServerSettings}.</li>
 * </ul>
 *
 * <p><b>Methods:</b></p>
 * <ul>
 * <li>{@code void sendLog(String dcserverid, SlashCommandInteractionEvent event, String titleKey, String userKey, String reasonKey, String username, String reason)}: Sends a moderation log message to the specified moderation log channel.</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * {@code
 * ModLogEmbed modLogEmbed = new ModLogEmbed(languageManager, serverSettings);
 * modLogEmbed.sendLog(dcserverid, event, "modlog.title", "modlog.user", "modlog.reason", username, reason);
 * }
 * </pre>
 */

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
