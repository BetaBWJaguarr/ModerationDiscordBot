package beta.com.moderationdiscordbot.slashcommandsmanager.commands.verifycommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.utils.ModLogEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class VerifySetRole extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;
    private final ModLogEmbed modLogEmbed;

    public VerifySetRole(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorManager, RateLimit rateLimit) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.errorManager = errorManager;
        this.rateLimit = rateLimit;
        this.modLogEmbed = new ModLogEmbed(languageManager, serverSettings);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (isVerifySetRoleCommand(event)) {
            if (!hasManageRolesPermission(event)) {
                return;
            }

            if (isRateLimited(event)) {
                return;
            }

            if (!isVerifySystemEnabled(event)) {
                sendSystemDisabledMessage(event);
                return;
            }

            Role role = getRoleFromEvent(event);
            setVerifiedRole(event, role);
            sendSuccessMessage(event, role);
            sendRoleSetLog(event, role.getName());
        }
    }

    private boolean isVerifySetRoleCommand(SlashCommandInteractionEvent event) {
        return event.getName().equals("verify") && event.getSubcommandName().equals("setrole");
    }

    private boolean hasManageRolesPermission(SlashCommandInteractionEvent event) {
        PermissionsManager permissionsManager = new PermissionsManager();
        return permissionsManager.checkPermissionAndOption(event, PermType.MANAGE_ROLES, embedBuilderManager, serverSettings, "commands.verify.no_permissions");
    }

    private boolean isRateLimited(SlashCommandInteractionEvent event) {
        return rateLimit.isRateLimited(event, embedBuilderManager, serverSettings);
    }

    private boolean isVerifySystemEnabled(SlashCommandInteractionEvent event) {
        String dcserverid = event.getGuild().getId();
        return serverSettings.getVerifySystem(dcserverid);
    }

    private void sendSystemDisabledMessage(SlashCommandInteractionEvent event) {
        String dcserverid = event.getGuild().getId();
        event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.system_disabled", null, serverSettings.getLanguage(dcserverid))
                        .build())
                .setEphemeral(true)
                .queue();
    }

    private Role getRoleFromEvent(SlashCommandInteractionEvent event) {
        return event.getOption("role").getAsRole();
    }

    private void setVerifiedRole(SlashCommandInteractionEvent event, Role role) {
        String dcserverid = event.getGuild().getId();
        String roleId = role.getId();
        serverSettings.setVerifiedRole(dcserverid, roleId);
    }

    private void sendSuccessMessage(SlashCommandInteractionEvent event, Role role) {
        String dcserverid = event.getGuild().getId();
        event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.setrole.success", null, serverSettings.getLanguage(dcserverid))
                        .setColor(Color.GREEN)
                        .addField(languageManager.getMessage("commands.verify.setrole.role_set", serverSettings.getLanguage(dcserverid)), role.getName(), false)
                        .build())
                .queue();
    }

    private void sendRoleSetLog(SlashCommandInteractionEvent event, String roleName) {
        String serverId = event.getGuild().getId();
        String titleKey = "commands.verify.modlog.setrole.title";
        String userKey = "commands.verify.modlog.setrole.user";
        String roleSetKey = "commands.verify.modlog.setrole.role_set";

        modLogEmbed.sendLog(
                serverId,
                event,
                titleKey,
                userKey,
                roleSetKey,
                event.getUser().getName(),
                roleName
        );
    }
}
