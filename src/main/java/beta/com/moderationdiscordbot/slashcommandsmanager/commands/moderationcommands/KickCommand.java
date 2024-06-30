package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KickCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final RateLimit rateLimit;

    public KickCommand(ServerSettings serverSettings, LanguageManager languageManager, RateLimit rateLimit) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("kick")) {


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

                    event.getGuild().kick(UserSnowflake.fromId(userToKickId), reason).queue();

                    String modLogChannelId = serverSettings.getModLogChannel(dcserverid);
                    if (modLogChannelId != null) {
                        TextChannel modLogChannel = event.getJDA().getTextChannelById(modLogChannelId);
                        if (modLogChannel != null) {
                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setTitle(languageManager.getMessage("commands.kick.log.title", serverSettings.getLanguage(dcserverid)));
                            embedBuilder.addField(languageManager.getMessage("commands.kick.log.user", serverSettings.getLanguage(dcserverid)), username, false);
                            embedBuilder.addField((languageManager.getMessage("commands.kick.log.reason", serverSettings.getLanguage(dcserverid))), reason, false);
                            embedBuilder.setColor(Color.RED);
                            embedBuilder.setTimestamp(Instant.now());

                            modLogChannel.sendMessageEmbeds(embedBuilder.build()).queue();
                        }
                    }

                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.kick.success", null, serverSettings.getLanguage(dcserverid), username, reason).build()).queue();

                }, error -> {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.kick.user_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                });
            }
        }
    }
}