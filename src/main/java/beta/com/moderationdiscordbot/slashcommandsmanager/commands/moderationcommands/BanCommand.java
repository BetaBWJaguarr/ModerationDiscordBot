package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.Logging.BanLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
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

    public BanCommand(ServerSettings serverSettings, LanguageManager languageManager, BanLog banLog) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.banLog = banLog;
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("ban")) {
            String dcserverid = event.getGuild().getId();
            if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.ban.no_permissions", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                return;
            }

            String mention = event.getOption("username").getAsString();
            Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");
            Matcher matcher = mentionPattern.matcher(mention);

            if (matcher.find()) {
                String userToBanId = matcher.group(1);
                event.getGuild().retrieveMemberById(userToBanId).queue(userToBan -> {
                    String username = userToBan.getUser().getName();

                    long durationInSeconds = -2;
                    if (event.getOption("duration") != null) {
                        String durationStr = event.getOption("duration").getAsString();
                        durationInSeconds = parseDuration(durationStr);
                        if (durationInSeconds == -1) {
                            event.replyEmbeds(embedBuilderManager.createEmbed("commands.ban.invalid_duration", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                            return;
                        }
                    }

                    String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "No reason provided";

                    int deleteHistoryDuration = event.getOption("delete_history_message_duration") != null ? event.getOption("delete_history_message_duration").getAsInt() : 0;
                    if (deleteHistoryDuration > 7) {
                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.ban.delete_history_duration_too_long", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                        return;
                    }

                    event.getGuild().ban(UserSnowflake.fromId(userToBan.getId()),deleteHistoryDuration, TimeUnit.DAYS).queue();


                    if (durationInSeconds == -2 ) {
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

                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.ban.success", successMessageKey,serverSettings.getLanguage(dcserverid), username, reason).build()).queue();

                }, error -> {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.ban.user_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                });
            } else {
            }
        }
    }



    private long parseDuration(String durationStr) {
        int durationInSeconds = 0;
        String number = "";
        for (int i = 0; i < durationStr.length(); i++) {
            char c = durationStr.charAt(i);
            if (Character.isDigit(c)) {
                number += c;
            } else if (Character.isLetter(c)) {
                switch (c) {
                    case 'd':
                        durationInSeconds += Integer.parseInt(number) * 60 * 60 * 24;
                        break;
                    case 'h':
                        durationInSeconds += Integer.parseInt(number) * 60 * 60;
                        break;
                    case 'm':
                        durationInSeconds += Integer.parseInt(number) * 60;
                        break;
                    case 's':
                        durationInSeconds += Integer.parseInt(number);
                        break;
                }
                number = "";
            }
        }
        return durationInSeconds;
    }

}