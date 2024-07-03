package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.MuteLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.utils.ModLogEmbed;
import beta.com.moderationdiscordbot.utils.ParseDuration;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MuteCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final MuteLog muteLog;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;
    private final ModLogEmbed modLogEmbed;

    public MuteCommand(ServerSettings serverSettings, LanguageManager languageManager, MuteLog muteLog, HandleErrors errorManager, RateLimit rateLimit) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.muteLog = muteLog;
        this.errorManager = errorManager;
        this.rateLimit = rateLimit;
        this.modLogEmbed = new ModLogEmbed(languageManager,serverSettings);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("mute")) {
            PermissionsManager permissionsManager = new PermissionsManager();

            if (!permissionsManager.checkPermissionAndOption(event, PermType.MANAGE_CHANNEL, embedBuilderManager, serverSettings, "commands.mute.no_permissions")) {
                return;
            }

            if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                return;
            }

            String dcserverid = event.getGuild().getId();

            String mention = event.getOption("username").getAsString();
            Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");
            Matcher matcher = mentionPattern.matcher(mention);

            if (matcher.find()) {
                String userToMuteId = matcher.group(1);
                event.getGuild().retrieveMemberById(userToMuteId).queue(userToMute -> {
                    String userid = userToMute.getUser().getId();
                    String username = userToMute.getUser().getName();

                    long durationInSeconds;
                    if (event.getOption("duration") != null) {
                        String durationStr = event.getOption("duration").getAsString();
                        durationInSeconds = ParseDuration.parse(durationStr);
                        if (durationInSeconds == -1) {
                            event.replyEmbeds(embedBuilderManager.createEmbed("commands.mute.invalid_duration", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                            return;
                        }
                    } else {
                        durationInSeconds = -2;
                    }

                    String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : languageManager.getMessage("no_reason", serverSettings.getLanguage(dcserverid));

                    List<Role> muteRoles = event.getGuild().getRolesByName("Muted", true);
                    if (muteRoles.isEmpty()) {
                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.mute.no_mute_role", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                        return;
                    }

                    Role muteRole = muteRoles.get(0);
                    event.getGuild().addRoleToMember(userToMute, muteRole).queue(
                            success -> {
                                if (durationInSeconds != -2) {
                                    muteLog.addMuteLog(dcserverid, userid, reason, new Date(System.currentTimeMillis() + durationInSeconds * 1000L));
                                } else {
                                    muteLog.addMuteLog(dcserverid, userid, reason, null);
                                }

                                sendMuteNotification(userToMute, username, reason, durationInSeconds, dcserverid);

                                modLogEmbed.sendLog(dcserverid, event, "commands.mute.log.title", "commands.mute.log.user", "commands.mute.log.reason", username, reason);


                                event.replyEmbeds(embedBuilderManager.createEmbed("commands.mute.success", null, serverSettings.getLanguage(dcserverid), username, reason).build()).queue();
                            },
                            error -> {
                                errorManager.sendErrorMessage((Exception) error, event.getChannel().asTextChannel());
                            }
                    );

                }, error -> {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.mute.user_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                });
            } else {
                // Handle if no user mention found
            }
        }
    }

    private void sendMuteNotification(Member mutedMember, String muterUsername, String reason, long durationInSeconds, String serverId) {
        mutedMember.getUser().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessageEmbeds(embedBuilderManager.createEmbed("commands.mute.dm_notification", null, serverSettings.getLanguage(serverId))
                    .setColor(Color.RED)
                    .setDescription(String.format(languageManager.getMessage("commands.mute.notification.description", serverSettings.getLanguage(serverId)), mutedMember.getGuild().getName()))
                    .addField(languageManager.getMessage("commands.mute.notification.muted_by", serverSettings.getLanguage(serverId)), muterUsername, false)
                    .addField(languageManager.getMessage("commands.mute.notification.reason", serverSettings.getLanguage(serverId)), reason, false)
                    .addField(languageManager.getMessage("commands.mute.notification.duration", serverSettings.getLanguage(serverId)), (durationInSeconds == -2 ? languageManager.getMessage("commands.mute.notification.permanent", serverSettings.getLanguage(serverId)) : String.valueOf(ParseDuration.parse(String.valueOf(durationInSeconds)))), false)
                    .setTimestamp(new Date().toInstant())
                    .build()).queue();
        }, error -> {
            // Handle error if unable to send DM
            System.err.println("Failed to send mute notification DM to " + mutedMember.getUser().getAsTag() + ": " + error.getMessage());
        });
    }
}