package beta.com.moderationdiscordbot.slashcommandsmanager.commands.warncommands;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.WarnLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnWarnCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final WarnLog warnLog;

    public UnWarnCommand(ServerSettings serverSettings, LanguageManager languageManager, WarnLog warnLog) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.warnLog = warnLog;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("unwarn")) {
            String dcserverid = event.getGuild().getId();
            PermissionsManager permissionsManager = new PermissionsManager();

            if (!permissionsManager.hasPermission(event.getMember(), PermType.MESSAGE_MANAGE)) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.unwarn.no_permissions", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                return;
            }

            String mention = event.getOption("username").getAsString();
            Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");
            Matcher matcher = mentionPattern.matcher(mention);

            if (matcher.find()) {
                String userToUnwarnId = matcher.group(1);
                String warningId = event.getOption("warningid").getAsString();
                String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : languageManager.getMessage("no_reason", serverSettings.getLanguage(dcserverid));

                event.getGuild().retrieveMemberById(userToUnwarnId).queue(userToUnwarn -> {
                    String username = userToUnwarn.getUser().getName();

                    boolean isWarnRemoved = warnLog.removeWarnLog(dcserverid, warningId);

                    if (!isWarnRemoved) {
                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.unwarn.warn_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                        return;
                    }

                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.unwarn.success", null, serverSettings.getLanguage(dcserverid), username, reason).build()).queue();
                }, error -> {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.unwarn.user_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                });
            } else {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.unwarn.invalid_mention", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
            }
        }
    }
}
