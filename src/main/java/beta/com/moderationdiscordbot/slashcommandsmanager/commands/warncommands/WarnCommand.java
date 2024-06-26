package beta.com.moderationdiscordbot.slashcommandsmanager.commands.warncommands;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.WarnLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.eventsmanager.events.HighWarnKickEvent;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WarnCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final WarnLog warnLog;

    public WarnCommand(ServerSettings serverSettings, LanguageManager languageManager, WarnLog warnLog) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.warnLog = warnLog;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("warn")) {
            String dcserverid = event.getGuild().getId();
            PermissionsManager permissionsManager = new PermissionsManager();

            if (!permissionsManager.hasPermission(event.getMember(), PermType.MESSAGE_MANAGE)) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.warn.no_permissions", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                return;
            }

            String mention = event.getOption("username").getAsString();
            Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");
            Matcher matcher = mentionPattern.matcher(mention);

            if (matcher.find()) {
                String userToWarnId = matcher.group(1);
                event.getGuild().retrieveMemberById(userToWarnId).queue(userToWarn -> {
                    String username = userToWarn.getUser().getName();

                    String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : languageManager.getMessage("no_reason", serverSettings.getLanguage(dcserverid));
                    String moderatorId = event.getUser().getId();

                    warnLog.addWarnLog(dcserverid, userToWarnId, reason, moderatorId);

                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.warn.success", null, serverSettings.getLanguage(dcserverid), username, reason).build()).queue();

                    HighWarnKickEvent highWarnKickEvent = new HighWarnKickEvent(warnLog);
                    highWarnKickEvent.checkAndKickUser(dcserverid, userToWarn);
                }, error -> {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.warn.user_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                });
            } else {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.warn.invalid_mention", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
            }
        }
    }
}
