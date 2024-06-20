package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.Logging.MuteLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.utils.ParseDuration;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MuteCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final MuteLog muteLog;

    public MuteCommand(ServerSettings serverSettings, LanguageManager languageManager, MuteLog muteLog) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.muteLog = muteLog;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("mute")) {
            String dcserverid = event.getGuild().getId();
            if (!event.getMember().hasPermission(Permission.MANAGE_ROLES)) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.mute.no_permissions", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                return;
            }

            String mention = event.getOption("username").getAsString();
            Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");
            Matcher matcher = mentionPattern.matcher(mention);

            if (matcher.find()) {
                String userToMuteId = matcher.group(1);
                event.getGuild().retrieveMemberById(userToMuteId).queue(userToMute -> {
                    String username = userToMute.getUser().getName();

                    long durationInSeconds = -2;
                    if (event.getOption("duration") != null) {
                        String durationStr = event.getOption("duration").getAsString();
                        durationInSeconds = ParseDuration.parse(durationStr);
                        if (durationInSeconds == -1) {
                            event.replyEmbeds(embedBuilderManager.createEmbed("commands.mute.invalid_duration", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                            return;
                        }
                    }

                    String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "No reason provided";

                    Role muteRole = event.getGuild().getRolesByName("Muted", true).get(0);
                    event.getGuild().addRoleToMember(userToMute, muteRole).queue();

                    if (durationInSeconds != -2) {
                        muteLog.addMuteLog(dcserverid, username, reason, new Date(System.currentTimeMillis() + durationInSeconds * 1000L));
                    }

                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.mute.success", null, serverSettings.getLanguage(dcserverid), username, reason).build()).queue();

                }, error -> {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.mute.user_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                });
            } else {
            }
        }
    }
}