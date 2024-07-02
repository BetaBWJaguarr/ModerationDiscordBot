package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class AutoRoleCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;

    public AutoRoleCommand(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorManager, RateLimit rateLimit) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.errorManager = errorManager;
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("autorole")) return;

        PermissionsManager permissionsManager = new PermissionsManager();

        if (!permissionsManager.checkPermissionAndOption(event, PermType.MANAGE_ROLES, embedBuilderManager, serverSettings, "commands.autorole.no_permissions")) {
            return;
        }

        if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
            return;
        }

        String dcserverid = event.getGuild().getId();

        Role role = event.getOption("role").getAsRole();


        String roleId = role.getId();
        serverSettings.setAutoRole(dcserverid, roleId);


        event.replyEmbeds(embedBuilderManager.createEmbed("commands.autorole.success", null, serverSettings.getLanguage(dcserverid))
                .setColor(Color.GREEN)
                .addField(languageManager.getMessage("commands.autorole.role_set", serverSettings.getLanguage(dcserverid)), role.getName(), false)
                .build()).queue();
    }
}
