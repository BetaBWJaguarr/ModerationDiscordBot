package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.undocommands;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.BanLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Unban extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final BanLog banLog;
    private final HandleErrors errorHandle;

    public Unban(ServerSettings serverSettings, LanguageManager languageManager, BanLog banLog, HandleErrors errorHandle) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.banLog = banLog;
        this.errorHandle = errorHandle;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (event.getName().equals("unban")) {
                String dcserverid = event.getGuild().getId();
                PermissionsManager permissionsManager = new PermissionsManager();

                if (!permissionsManager.hasPermission(event.getMember(), PermType.BAN_MEMBERS)) {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.unban.no_permissions", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                    return;
                }

                String mention = event.getOption("username").getAsString();
                String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : languageManager.getMessage("no_reason", serverSettings.getLanguage(dcserverid));

                Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");
                Matcher matcher = mentionPattern.matcher(mention);

                if (matcher.find()) {
                    String userToUnbanId = matcher.group(1);
                    event.getGuild().retrieveBanList().queue(banList -> {
                        if (banList.stream().noneMatch(ban -> ban.getUser().getId().equals(userToUnbanId))) {
                            event.replyEmbeds(embedBuilderManager.createEmbed("commands.unban.user_not_banned", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                            return;
                        }
                        event.getGuild().unban(UserSnowflake.fromId(userToUnbanId)).queue(
                                success -> {
                                    banLog.removeBanLog(dcserverid, userToUnbanId);
                                    if (reason != null) {
                                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.unban.success", "commands.unban.user_unbanned_reason", serverSettings.getLanguage(dcserverid), reason).build()).queue();
                                    } else {
                                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.unban.success", "commands.unban.user_unbanned", serverSettings.getLanguage(dcserverid)).build()).queue();
                                    }
                                },
                                error -> errorHandle.sendErrorMessage((Exception) error, event.getChannel().asTextChannel())
                        );
                    });
                }
            }
        } catch (Exception e) {
            errorHandle.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }
}
