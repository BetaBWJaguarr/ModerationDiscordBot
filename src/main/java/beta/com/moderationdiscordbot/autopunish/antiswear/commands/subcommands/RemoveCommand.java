package beta.com.moderationdiscordbot.autopunish.antiswear.commands.subcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RemoveCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final RateLimit rateLimit;
    private final HandleErrors errorHandle;

    public RemoveCommand(ServerSettings serverSettings, LanguageManager languageManager, RateLimit rateLimit, HandleErrors errorHandle) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.rateLimit = rateLimit;
        this.errorHandle = errorHandle;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (!event.getName().equals("antiswear")) {
                return;
            }

            String dcserverid = event.getGuild().getId();

            if (!event.getSubcommandName().equals("remove")) {
                return;
            }

            PermissionsManager permissionsManager = new PermissionsManager();
            if (!permissionsManager.checkPermissionAndOption(event, PermType.MESSAGE_MANAGE, embedBuilderManager, serverSettings, "commands.antiswear.remove.no_permissions")) {
                return;
            }

            String wordToRemove = event.getOption("word").getAsString();

            if (!serverSettings.getAntiSwearWordsList(dcserverid).contains(wordToRemove.toLowerCase())) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.antiswear.remove.not_found", null, serverSettings.getLanguage(dcserverid), wordToRemove).build()).queue();
                return;
            }

            serverSettings.removeAntiSwearWord(dcserverid, wordToRemove);

            event.replyEmbeds(embedBuilderManager.createEmbed("commands.antiswear.remove.success", null, serverSettings.getLanguage(dcserverid), wordToRemove).build()).queue();
        } catch (Exception e) {
            errorHandle.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }
}
