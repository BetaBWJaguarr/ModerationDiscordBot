package beta.com.moderationdiscordbot.utils;

import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.text.MessageFormat;


/**
 * This class simplifies the creation of EmbedBuilder objects with localized titles and descriptions.
 * It uses LanguageManager to retrieve messages in the specified language and supports dynamic formatting of arguments.
 * EmbedBuilderManager facilitates the creation of embeds with or without specified colors, enhancing visual
 * distinction and presentation of information within Discord messages.
 *
 * Dependencies:
 * - LanguageManager: Provides access to localized messages for embedding titles and descriptions based on server settings.
 *
 * Usage:
 * Initialize EmbedBuilderManager with a LanguageManager instance to begin creating EmbedBuilder objects
 * with localized content tailored to the server's language preferences. Use methods like createEmbed() to generate
 * embeds with customizable titles, descriptions, and optional colors for visual differentiation in Discord channels.
 */

public class EmbedBuilderManager {

    private final LanguageManager languageManager;

    public EmbedBuilderManager(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    public EmbedBuilder createEmbed(String titleKey, String descriptionKey, String language, Object... formatArgs) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String title = null;
        String description = null;

        if (titleKey != null) {
            title = languageManager.getMessage(titleKey, language);
        }

        if (descriptionKey != null) {
            description = languageManager.getMessage(descriptionKey, language);
        }

        if (title != null && formatArgs.length > 0) {
            title = MessageFormat.format(title, formatArgs);
        }

        if (description != null && formatArgs.length > 0) {
            description = MessageFormat.format(description, formatArgs);
        }

        if (title != null) {
            embedBuilder.setTitle(title);
        }

        if (description != null) {
            embedBuilder.setDescription(description);
        }

        return embedBuilder;
    }

    public EmbedBuilder createEmbedWithColor(String titleKey, String descriptionKey, String language, Color color, Object... formatArgs) {
        EmbedBuilder embedBuilder = createEmbed(titleKey, descriptionKey, language, formatArgs);
        embedBuilder.setColor(color);
        return embedBuilder;
    }


    public void sendDM(SlashCommandInteractionEvent event, String userId, MessageEmbed embed) {
        event.getJDA().openPrivateChannelById(userId).queue(privateChannel -> {
            privateChannel.sendMessageEmbeds(embed).queue();
        });
    }
}
