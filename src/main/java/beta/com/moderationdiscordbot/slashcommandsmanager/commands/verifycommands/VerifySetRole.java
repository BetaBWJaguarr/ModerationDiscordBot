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
    private final ModLogEmbed modLogEmbed; // Added ModLogEmbed

    public VerifySetRole(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorManager, RateLimit rateLimit) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.errorManager = errorManager;
        this.rateLimit = rateLimit;
        this.modLogEmbed = new ModLogEmbed(languageManager, serverSettings); // Initialize ModLogEmbed
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("verify") && event.getSubcommandName().equals("setrole")) {

            PermissionsManager permissionsManager = new PermissionsManager();

            if (!permissionsManager.checkPermissionAndOption(event, PermType.MANAGE_ROLES, embedBuilderManager, serverSettings, "commands.verify.no_permissions")) {
                return;
            }

            if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                return;
            }

            String dcserverid = event.getGuild().getId();
            boolean isVerifySystemEnabled = serverSettings.getVerifySystem(dcserverid);
            if (!isVerifySystemEnabled) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.system_disabled", null, serverSettings.getLanguage(dcserverid)).build())
                        .setEphemeral(true)
                        .queue();
                return;
            }

            Role role = event.getOption("role").getAsRole();
            String roleId = role.getId();

            serverSettings.setVerifiedRole(dcserverid, roleId);

            event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.setrole.success", null, serverSettings.getLanguage(dcserverid))
                    .setColor(Color.GREEN)
                    .addField(languageManager.getMessage("commands.verify.setrole.role_set", serverSettings.getLanguage(dcserverid)), role.getName(), false)
                    .build()).queue();

            sendRoleSetLog(event, dcserverid, role.getName());
        }
    }

    private void sendRoleSetLog(SlashCommandInteractionEvent event, String serverId, String roleName) {
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
