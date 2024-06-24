package beta.com.moderationdiscordbot.utils;

import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.text.MessageFormat;

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
}
