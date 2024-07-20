package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.undocommands;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.BanLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.utils.ModLogEmbed;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelUnBanCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final BanLog banLog;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;
    private final ModLogEmbed modLogEmbed;

    public ChannelUnBanCommand(ServerSettings serverSettings, LanguageManager languageManager, BanLog banLog, HandleErrors errorManager, RateLimit rateLimit) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.banLog = banLog;
        this.errorManager = errorManager;
        this.rateLimit = rateLimit;
        this.modLogEmbed = new ModLogEmbed(languageManager, serverSettings);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("channels")) {

            if (event.getSubcommandName().equals("unban")) {

                PermissionsManager permissionsManager = new PermissionsManager();

                if (!permissionsManager.checkPermissionAndOption(event, PermType.BAN_MEMBERS, embedBuilderManager, serverSettings, "commands.channel-unban.no_permissions")) {
                    return;
                }

                if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                    return;
                }

                String dcserverid = event.getGuild().getId();

                String mention = event.getOption("username").getAsString();
                Channel channel = event.getOption("channel").getAsChannel();
                String channelId = channel.getId();

                Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");
                Matcher userMatcher = mentionPattern.matcher(mention);

                if (userMatcher.find()) {
                    String userToUnbanId = userMatcher.group(1);

                    event.getGuild().retrieveMemberById(userToUnbanId).queue(userToUnban -> {
                        String username = userToUnban.getUser().getName();

                        TextChannel textChannel = event.getGuild().getTextChannelById(channelId);
                        if (textChannel != null) {
                            String channelName = textChannel.getName();
                            if (textChannel.getPermissionOverride(userToUnban) == null || !textChannel.getPermissionOverride(userToUnban).getDenied().contains(Permission.MESSAGE_SEND)) {
                                event.replyEmbeds(embedBuilderManager.createEmbed("commands.channel-unban.user_not_banned", null, serverSettings.getLanguage(dcserverid), username, channelName).build()).queue();
                            } else {
                                textChannel.getPermissionOverride(userToUnban).getManager()
                                        .clear(Permission.MESSAGE_SEND)
                                        .queue(success -> {
                                            banLog.removeBanLog(dcserverid, userToUnbanId, channelId);
                                            event.replyEmbeds(embedBuilderManager.createEmbed("commands.channel-unban.success", null, serverSettings.getLanguage(dcserverid), username, channelName).build()).queue();
                                            modLogEmbed.sendLog(dcserverid, event, "commands.channel-unban.log_title", "commands.channel-unban.log_user", null, username, null);
                                        }, error -> {
                                            errorManager.sendErrorMessage((Exception) error, event.getChannel().asTextChannel());
                                        });
                            }
                        } else {
                            event.replyEmbeds(embedBuilderManager.createEmbed("commands.channel-unban.channel_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                        }

                    }, error -> {
                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.channel-unban.user_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                    });
                } else {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.channel-unban.invalid_user_or_channel", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                }
            }
        }
    }
}