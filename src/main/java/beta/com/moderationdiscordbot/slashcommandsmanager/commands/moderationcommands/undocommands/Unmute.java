package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.undocommands;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.MuteLog;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Unmute extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final MuteLog muteLog;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;
    private final ModLogEmbed modLogEmbed;

    public Unmute(ServerSettings serverSettings, LanguageManager languageManager, MuteLog muteLog, HandleErrors errorManager, RateLimit rateLimit) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.muteLog = muteLog;
        this.errorManager = errorManager;
        this.rateLimit = rateLimit;
        this.modLogEmbed = new ModLogEmbed(languageManager, serverSettings);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("unmute")) {
            String dcserverid = event.getGuild().getId();
            PermissionsManager permissionsManager = new PermissionsManager();

            if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                return;
            }

            if (!permissionsManager.checkPermissionAndOption(event, PermType.MESSAGE_MANAGE, embedBuilderManager, serverSettings, "commands.unmute.no_permissions")) {
                return;
            }

            String mention = event.getOption("username").getAsString();
            String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : null;

            Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");
            Matcher matcher = mentionPattern.matcher(mention);

            if (matcher.find()) {
                String userToUnmuteId = matcher.group(1);
                event.getGuild().retrieveMemberById(userToUnmuteId).queue(userToUnmute -> {
                    String username = userToUnmute.getUser().getName();
                    Role muteRole = event.getGuild().getRolesByName("Muted", true).stream().findFirst().orElse(null);
                    if (muteRole != null && userToUnmute.getRoles().contains(muteRole)) {
                        event.getGuild().removeRoleFromMember(userToUnmute, muteRole).queue(
                                success -> {
                                    muteLog.removeMuteLog(dcserverid, userToUnmuteId);

                                    if (reason != null) {
                                        modLogEmbed.sendLog(dcserverid, event, "commands.unmute.log.title", "commands.unmute.log.user", "commands.unmute.log.reason", username, reason);
                                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.unmute.success", "commands.unmute.user_unmuted_reason", serverSettings.getLanguage(dcserverid), username, reason).build()).queue();
                                    } else {
                                        modLogEmbed.sendLog(dcserverid, event, "commands.unmute.log.title", "commands.unmute.log.user", null, username, null);
                                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.unmute.success", "commands.unmute.user_unmuted", serverSettings.getLanguage(dcserverid), username).build()).queue();
                                    }
                                },
                                error -> {
                                    errorManager.sendErrorMessage((Exception) error, event.getChannel().asTextChannel());
                                }
                        );
                    } else {
                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.unmute.user_not_muted", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                    }
                }, error -> {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.unmute.user_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                });
            }
        }
    }
}
