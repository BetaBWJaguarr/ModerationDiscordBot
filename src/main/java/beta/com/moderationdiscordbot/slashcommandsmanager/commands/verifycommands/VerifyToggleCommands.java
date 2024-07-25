package beta.com.moderationdiscordbot.slashcommandsmanager.commands.verifycommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class VerifyToggleCommands extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;

    public VerifyToggleCommands(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorManager, RateLimit rateLimit) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.errorManager = errorManager;
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("verify") && event.getSubcommandName().equals("toggle")) {
            PermissionsManager permissionsManager = new PermissionsManager();

            if (!permissionsManager.checkPermissionAndOption(event, PermType.MANAGE_CHANNEL, embedBuilderManager, serverSettings, "commands.verify.no_permissions")) {
                return;
            }

            if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                return;
            }

            String discordServerId = event.getGuild().getId();
            String action = event.getOption("action") != null ? event.getOption("action").getAsString().toLowerCase() : null;

            if (action == null) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.missing_action", null, serverSettings.getLanguage(discordServerId)).build())
                        .setEphemeral(true)
                        .queue();
                return;
            }

            switch (action) {
                case "enable":
                    setVerifySystem(discordServerId, true);
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.enabled", null, serverSettings.getLanguage(discordServerId)).build())
                            .setEphemeral(true)
                            .queue();
                    break;

                case "disable":
                    setVerifySystem(discordServerId, false);
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.disabled", null, serverSettings.getLanguage(discordServerId)).build())
                            .setEphemeral(true)
                            .queue();
                    break;

                default:
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.invalid_action", null, serverSettings.getLanguage(discordServerId)).build())
                            .setEphemeral(true)
                            .queue();
                    break;
            }
        }
    }

    private void setVerifySystem(String discordServerId, boolean enabled) {
        serverSettings.setVerifySystem(discordServerId, enabled);
    }
}
