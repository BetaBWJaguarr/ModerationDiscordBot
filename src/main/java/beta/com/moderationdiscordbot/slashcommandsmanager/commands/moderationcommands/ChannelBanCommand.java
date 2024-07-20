package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.BanLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.utils.ModLogEmbed;
import beta.com.moderationdiscordbot.utils.ParseDuration;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelBanCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final BanLog banLog;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;
    private final ModLogEmbed modLogEmbed;

    public ChannelBanCommand(ServerSettings serverSettings, LanguageManager languageManager, BanLog banLog, HandleErrors errorManager, RateLimit rateLimit) {
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
        try {
            if (!event.getName().equals("channels") || !event.getSubcommandName().equals("ban")) {
                return;
            }

            PermissionsManager permissionsManager = new PermissionsManager();

            if (!permissionsManager.checkPermissionAndOption(event, PermType.BAN_MEMBERS, embedBuilderManager, serverSettings, "commands.channel-ban.no_permissions")) {
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
                String userToBanId = userMatcher.group(1);

                event.getGuild().retrieveMemberById(userToBanId).queue(userToBan -> {
                    String username = userToBan.getUser().getName();

                    long durationInSeconds;
                    if (event.getOption("duration") != null) {
                        String durationStr = event.getOption("duration").getAsString();
                        durationInSeconds = ParseDuration.parse(durationStr);
                        if (durationInSeconds == -1) {
                            event.replyEmbeds(embedBuilderManager.createEmbed("commands.channel-ban.invalid_duration", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                            return;
                        }
                    } else {
                        durationInSeconds = -2;
                    }

                    String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : languageManager.getMessage("no_reason", serverSettings.getLanguage(dcserverid));

                    TextChannel textChannel = event.getGuild().getTextChannelById(channelId);
                    if (textChannel != null) {
                        String channelName = textChannel.getName();

                        if (textChannel.getPermissionOverride(userToBan) != null && textChannel.getPermissionOverride(userToBan).getDenied().contains(Permission.MESSAGE_SEND)) {
                            event.replyEmbeds(embedBuilderManager.createEmbed("commands.channel-ban.already_banned", null, serverSettings.getLanguage(dcserverid), username, channelName).build()).setEphemeral(true).queue();
                            return;
                        }

                        textChannel.upsertPermissionOverride(userToBan)
                                .deny(Permission.MESSAGE_SEND)
                                .queue(success -> {
                                    if (durationInSeconds == -2) {
                                        banLog.addBanLog(dcserverid, userToBanId, reason, null, channelId);
                                    } else {
                                        banLog.addBanLog(dcserverid, userToBanId, reason, new Date(System.currentTimeMillis() + durationInSeconds * 1000L), channelId);
                                    }

                                    String successMessageKey = durationInSeconds > 0 ? "commands.channel-ban.tempban" : "commands.channel-ban.permban";

                                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.channel-ban.success", successMessageKey, serverSettings.getLanguage(dcserverid), username, channelName, reason).build()).queue();
                                    modLogEmbed.sendLog(dcserverid, event, "commands.channel-ban.log_title", "commands.channel-ban.log_user", "commands.channel-ban.log_reason", username, reason);
                                }, error -> {
                                    errorManager.sendErrorMessage((Exception) error, event.getChannel().asTextChannel());
                                });
                    } else {
                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.channel-ban.channel_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                    }

                }, error -> {
                    errorManager.sendErrorMessage((Exception) error, event.getChannel().asTextChannel());
                });
            } else {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.channel-ban.invalid_user_or_channel", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
            }
        } catch (Exception e) {
            errorManager.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }
}
