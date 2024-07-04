package beta.com.moderationdiscordbot.slashcommandsmanager.commands.warncommands;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.WarnLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SetWarnKickTimesCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final WarnLog warnLog;
    private final HandleErrors errorHandle;
    private final RateLimit rateLimit;

    public SetWarnKickTimesCommand(ServerSettings serverSettings, LanguageManager languageManager, WarnLog warnLog, HandleErrors errorHandle, RateLimit rateLimit) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.warnLog = warnLog;
        this.errorHandle = errorHandle;
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (event.getName().equals("setwarnkick")) {
                String dcserverid = event.getGuild().getId();

                if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                    return;
                }

                PermissionsManager permissionsManager = new PermissionsManager();

                if (!permissionsManager.checkPermissionAndOption(event, PermType.ADMINISTRATOR, embedBuilderManager, serverSettings, "commands.setwarnkick.no_permissions")) {
                    return;
                }

                int kickTimes = event.getOption("times").getAsInt();
                warnLog.setWarnKickTimes(dcserverid, kickTimes);

                event.replyEmbeds(embedBuilderManager.createEmbed("commands.setwarnkick.success", null, serverSettings.getLanguage(dcserverid), kickTimes).build()).queue();
            }
        } catch (Exception e) {
            errorHandle.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }
}
