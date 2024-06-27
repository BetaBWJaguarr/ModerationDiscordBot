package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KickCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;

    public KickCommand(ServerSettings serverSettings, LanguageManager languageManager) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("kick")) {
            String dcserverid = event.getGuild().getId();
            PermissionsManager permissionsManager = new PermissionsManager();

            if (!permissionsManager.hasPermission(event.getMember(), PermType.KICK_MEMBERS)) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.kick.no_permissions", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
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

                    event.getGuild().kick(UserSnowflake.fromId(userToKickId), reason).queue();

                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.kick.success", null, serverSettings.getLanguage(dcserverid), username, reason).build()).queue();

                }, error -> {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.kick.user_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                });
            }
        }
    }
}