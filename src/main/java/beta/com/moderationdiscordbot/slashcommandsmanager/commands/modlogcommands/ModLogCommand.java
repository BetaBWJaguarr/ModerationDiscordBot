package beta.com.moderationdiscordbot.slashcommandsmanager.commands.modlogcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ModLogCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final HandleErrors errorHandle;

    public ModLogCommand(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorHandle) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.errorHandle = errorHandle;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (event.getName().equals("modlog")) {
                String dcserverid = event.getGuild().getId();
                PermissionsManager permissionsManager = new PermissionsManager();

                if (!permissionsManager.checkPermissionAndOption(event, PermType.MESSAGE_MANAGE, embedBuilderManager, serverSettings, "commands.modlog.no_permissions")) {
                    return;
                }

                String channelId = event.getOption("channel").getAsString();
                serverSettings.setModLogChannel(dcserverid, channelId);

                event.replyEmbeds(embedBuilderManager.createEmbed("commands.modlog.success", null, serverSettings.getLanguage(dcserverid)).build()).queue();
            }
        } catch (Exception e) {
            errorHandle.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }
}
