package beta.com.moderationdiscordbot.autopunish.antiswear.commands.subcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.List;

public class ListCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final RateLimit rateLimit;
    private final HandleErrors errorHandle;

    public ListCommand(ServerSettings serverSettings, LanguageManager languageManager, RateLimit rateLimit, HandleErrors errorHandle) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.rateLimit = rateLimit;
        this.errorHandle = errorHandle;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (!isCommandApplicable(event)) {
                return;
            }

            String dcserverid = event.getGuild().getId();

            if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                return;
            }

            if (!hasPermission(event)) {
                return;
            }

            List<String> swearWords = serverSettings.getAntiSwearWordsList(dcserverid);
            if (swearWords == null) {
                String errorMessage = languageManager.getMessage("commands.antiswear.list.no_words",serverSettings.getLanguage(dcserverid));
                event.reply(errorMessage).queue();
                return;
            }

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(languageManager.getMessage("commands.antiswear.list.title",serverSettings.getLanguage(dcserverid)));
            embedBuilder.setColor(Color.BLUE);

            StringBuilder wordsList = new StringBuilder();
            for (String word : swearWords) {
                wordsList.append(word).append("\n");
            }

            embedBuilder.setDescription(wordsList.toString());
            event.replyEmbeds(embedBuilder.build()).queue();

        } catch (Exception e) {
            errorHandle.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }

    private boolean isCommandApplicable(SlashCommandInteractionEvent event) {
        return event.getName().equals("antiswear") && event.getSubcommandName().equals("list");
    }

    private boolean hasPermission(SlashCommandInteractionEvent event) {
        PermissionsManager permissionsManager = new PermissionsManager();
        return permissionsManager.checkPermissionAndOption(event, PermType.MESSAGE_MANAGE, embedBuilderManager, serverSettings, "commands.antiswear.list.no_permissions");
    }
}
