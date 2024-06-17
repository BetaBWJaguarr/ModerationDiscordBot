package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class SetLanguageCommand extends ListenerAdapter {

    private final ServerSettings serverSettings;
    private final EmbedBuilderManager embedManager;
    private final List<String> validLanguages = Arrays.asList("en", "tr");

    public SetLanguageCommand(ServerSettings serverSettings, LanguageManager languageManager) {
        this.serverSettings = serverSettings;
        this.embedManager = new EmbedBuilderManager(languageManager);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("setlanguage")) {
            String discordServerId = event.getGuild().getId();
            String language = serverSettings.getLanguage(discordServerId);

            if (!event.getMember().isOwner()) {
                event.replyEmbeds(embedManager.createEmbedWithColor(
                        "commands.setlanguage.title",
                        "commands.setlanguage.no_permissions",
                        language,
                        Color.RED).build()).setEphemeral(true).queue();
                return;
            }

            String newLanguage = event.getOption("language").getAsString();
            if (validLanguages.contains(newLanguage) && serverSettings.setLanguage(discordServerId, newLanguage)) {
                event.replyEmbeds(embedManager.createEmbedWithColor(
                        "commands.setlanguage.title",
                        "commands.setlanguage.success",
                        language,
                        Color.GREEN).build()).setEphemeral(true).queue();
            } else {
                event.replyEmbeds(embedManager.createEmbedWithColor(
                        "commands.setlanguage.title",
                        "commands.setlanguage.error",
                        language,
                        Color.RED).build()).setEphemeral(true).queue();
            }
        }
    }
}
