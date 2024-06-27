package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.undocommands;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.MuteLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
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

    public Unmute(ServerSettings serverSettings, LanguageManager languageManager, MuteLog muteLog, HandleErrors errorManager) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.muteLog = muteLog;
        this.errorManager = errorManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("unmute")) {
            String dcserverid = event.getGuild().getId();
            PermissionsManager permissionsManager = new PermissionsManager();

            if (!permissionsManager.hasPermission(event.getMember(), PermType.MESSAGE_MANAGE)) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.unmute.no_permissions", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                return;
            }

            String mention = event.getOption("username").getAsString();
            String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : languageManager.getMessage("no_reason", serverSettings.getLanguage(dcserverid));

            Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");
            Matcher matcher = mentionPattern.matcher(mention);

            if (matcher.find()) {
                String userToUnmuteId = matcher.group(1);
                event.getGuild().retrieveMemberById(userToUnmuteId).queue(userToUnmute -> {
                    Role muteRole = event.getGuild().getRolesByName("Muted", true).stream().findFirst().orElse(null);
                    if (muteRole != null && userToUnmute.getRoles().contains(muteRole)) {
                        event.getGuild().removeRoleFromMember(userToUnmute, muteRole).queue(
                                success -> {
                                    muteLog.removeMuteLog(dcserverid, userToUnmuteId);
                                    if (reason != null) {
                                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.unmute.success", "commands.unmute.user_unmuted_reason", serverSettings.getLanguage(dcserverid), reason).build()).queue();
                                    } else {
                                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.unmute.success", "commands.unmute.user_unmuted", serverSettings.getLanguage(dcserverid)).build()).queue();
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
