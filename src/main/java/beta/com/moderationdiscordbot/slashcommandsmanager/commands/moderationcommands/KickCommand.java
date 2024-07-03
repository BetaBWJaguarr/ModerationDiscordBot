package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.utils.ModLogEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KickCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final RateLimit rateLimit;
    private final HandleErrors errorHandle;
    private final ModLogEmbed modLogEmbed;

    public KickCommand(ServerSettings serverSettings, LanguageManager languageManager, RateLimit rateLimit, HandleErrors errorHandle) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.rateLimit = rateLimit;
        this.errorHandle = errorHandle;
        this.modLogEmbed = new ModLogEmbed(languageManager,serverSettings);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (!event.getName().equals("kick")) {
                return;
            }

            if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                return;
            }

            String dcserverid = event.getGuild().getId();
            PermissionsManager permissionsManager = new PermissionsManager();

            if (!permissionsManager.checkPermissionAndOption(event, PermType.KICK_MEMBERS, embedBuilderManager, serverSettings, "commands.kick.no_permissions")) {
                return;
            }

            String mention = event.getOption("username").getAsString();
            Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");
            Matcher matcher = mentionPattern.matcher(mention);

            if (matcher.find()) {
                String userToKickId = matcher.group(1);
                event.getGuild().retrieveMemberById(userToKickId).queue(userToKick -> {
                    String username = userToKick.getUser().getName();

                    String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : languageManager.getMessage("no_reason", serverSettings.getLanguage(dcserverid));

                    event.getGuild().kick(userToKick, reason).queue(success -> {
                        modLogEmbed.sendLog(dcserverid,event, "commands.kick.log.title", "commands.kick.log.user", "commands.kick.log.reason", username, reason);

                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.kick.success", null, serverSettings.getLanguage(dcserverid), username, reason).build()).queue();
                    }, error -> {
                        errorHandle.sendErrorMessage((Exception) error, event.getChannel().asTextChannel());
                    });

                }, error -> {
                    errorHandle.sendErrorMessage((Exception) error, event.getChannel().asTextChannel());
                });
            } else {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.kick.invalid_mention", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
            }
        } catch (Exception e) {
            errorHandle.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }
}
