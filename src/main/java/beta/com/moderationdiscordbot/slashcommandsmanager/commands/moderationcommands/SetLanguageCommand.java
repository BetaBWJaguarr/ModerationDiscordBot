package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class SetLanguageCommand extends ListenerAdapter {

    private final ServerSettings serverSettings;
    private final EmbedBuilderManager embedManager;
    private final List<String> validLanguages = Arrays.asList("en", "tr");
    private final HandleErrors errorHandle;
    private final RateLimit rateLimit;

    public SetLanguageCommand(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorHandle, RateLimit rateLimit) {
        this.serverSettings = serverSettings;
        this.embedManager = new EmbedBuilderManager(languageManager);
        this.errorHandle = errorHandle;
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (event.getName().equals("setlanguage")) {
                String discordServerId = event.getGuild().getId();
                String language = serverSettings.getLanguage(discordServerId);

                if (rateLimit.isRateLimited(event, embedManager, serverSettings)) {
                    return;
                }

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
        } catch (Exception e) {
            errorHandle.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }
}
