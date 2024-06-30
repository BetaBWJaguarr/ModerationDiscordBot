package beta.com.moderationdiscordbot.slashcommandsmanager.commands.warncommands;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.WarnLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WarnListCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final WarnLog warnLog;
    private final HandleErrors errorHandle;
    private final RateLimit rateLimit;

    public WarnListCommand(ServerSettings serverSettings, LanguageManager languageManager, WarnLog warnLog, HandleErrors errorHandle, RateLimit rateLimit) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.warnLog = warnLog;
        this.errorHandle = errorHandle;
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (event.getName().equals("warnlist")) {
                String dcserverid = event.getGuild().getId();

                if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                    return;
                }

                String mention = event.getOption("username").getAsString();
                String userToCheckId = extractUserIdFromMention(mention,event);

                if (userToCheckId == null) {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.warnlist.invalid_mention", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                    return;
                }

                List<String> warnIds = warnLog.getWarnIds(dcserverid, userToCheckId);

                if (warnIds == null || warnIds.isEmpty()) {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.warnlist.no_warns", null, serverSettings.getLanguage(dcserverid)).build()).queue();
                } else {
                    int numWarns = warnIds.size();
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle(languageManager.getMessage("commands.warnlist.success_title", serverSettings.getLanguage(dcserverid)));
                    String description = MessageFormat.format(languageManager.getMessage("commands.warnlist.success_description", serverSettings.getLanguage(dcserverid)), numWarns);
                    embedBuilder.setDescription(description);
                    embedBuilder.setColor(Color.YELLOW);
                    embedBuilder.setTimestamp(Instant.now());

                    event.replyEmbeds(embedBuilder.build()).queue();
                }
            }
        } catch (Exception e) {
            errorHandle.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }

    private String extractUserIdFromMention(String mention,SlashCommandInteractionEvent event) {
        try {
            Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");
            Matcher matcher = mentionPattern.matcher(mention);

            if (matcher.find()) {
                return matcher.group(1);
            } else {
                return null;
            }
        } catch (Exception e) {
            errorHandle.sendErrorMessage(e,event.getChannel().asTextChannel());
            return null;
        }
    }
}
