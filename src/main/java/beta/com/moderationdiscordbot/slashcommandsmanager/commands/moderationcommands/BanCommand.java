package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.BanLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.utils.ParseDuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BanCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final BanLog banLog;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;

    public BanCommand(ServerSettings serverSettings, LanguageManager languageManager, BanLog banLog, HandleErrors errorManager, RateLimit rateLimit) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.banLog = banLog;
        this.errorManager = errorManager;
        this.rateLimit = rateLimit;
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("ban")) {
            PermissionsManager permissionsManager = new PermissionsManager();

            if (!permissionsManager.checkPermissionAndOption(event, PermType.BAN_MEMBERS, embedBuilderManager, serverSettings, "commands.ban.no_permissions")) {
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
                String userToBanId = matcher.group(1);
                event.getGuild().retrieveMemberById(userToBanId).queue(userToBan -> {
                    String username = userToBan.getUser().getName();

                    long durationInSeconds;
                    if (event.getOption("duration") != null) {
                        String durationStr = event.getOption("duration").getAsString();
                        durationInSeconds = ParseDuration.parse(durationStr);
                        if (durationInSeconds == -1) {
                            event.replyEmbeds(embedBuilderManager.createEmbed("commands.ban.invalid_duration", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                            return;
                        }
                    } else {
                        durationInSeconds = -2;
                    }

                    String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : languageManager.getMessage("no_reason", serverSettings.getLanguage(dcserverid));

                    int deleteHistoryDuration = event.getOption("delete_history_message_duration") != null ? event.getOption("delete_history_message_duration").getAsInt() : 0;
                    if (deleteHistoryDuration > 7) {
                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.ban.delete_history_duration_too_long", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                        return;
                    }

                    event.getGuild().ban(UserSnowflake.fromId(userToBan.getId()), deleteHistoryDuration, TimeUnit.DAYS).queue(
                            success -> {
                                if (durationInSeconds == -2) {
                                    banLog.addBanLog(dcserverid, userToBanId, reason, null);
                                } else {
                                    banLog.addBanLog(dcserverid, userToBanId, reason, new Date(System.currentTimeMillis() + durationInSeconds * 1000L));
                                }

                                String successMessageKey = durationInSeconds > 0 ? "commands.ban.tempban" : "commands.ban.permban";

                                String modLogChannelId = serverSettings.getModLogChannel(dcserverid);
                                if (modLogChannelId != null) {
                                    TextChannel modLogChannel = event.getJDA().getTextChannelById(modLogChannelId);
                                    if (modLogChannel != null) {
                                        EmbedBuilder embedBuilder = new EmbedBuilder();
                                        embedBuilder.setTitle(languageManager.getMessage("commands.ban.log.title", serverSettings.getLanguage(dcserverid)));
                                        embedBuilder.addField(languageManager.getMessage("commands.ban.log.user", serverSettings.getLanguage(dcserverid)), username, false);
                                        embedBuilder.addField((languageManager.getMessage("commands.ban.log.reason", serverSettings.getLanguage(dcserverid))), reason, false);
                                        embedBuilder.setColor(Color.RED);
                                        embedBuilder.setTimestamp(Instant.now());

                                        modLogChannel.sendMessageEmbeds(embedBuilder.build()).queue();
                                    }
                                }

                                event.replyEmbeds(embedBuilderManager.createEmbed("commands.ban.success", successMessageKey, serverSettings.getLanguage(dcserverid), username, reason).build()).queue();
                            },
                            error -> {
                                errorManager.sendErrorMessage((Exception) error, event.getChannel().asTextChannel());
                            }
                    );

                }, error -> {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.ban.user_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                });
            } else {
            }
        }
    }
}