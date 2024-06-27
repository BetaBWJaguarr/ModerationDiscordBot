package beta.com.moderationdiscordbot.expectionmanagement;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class HandleErrors {

    private final LanguageManager languageManager;
    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;

    public HandleErrors(LanguageManager languageManager, ServerSettings serverSettings){
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
    }

    public void sendErrorMessage(Exception e, TextChannel channel) {
        channel.sendMessageEmbeds(embedBuilderManager.createEmbed("errorshandle.error.title", "errorshandle.error.descriptions",serverSettings.getLanguage(channel.getGuild().getId()),e.getMessage() ).build()).queue();
    }
}