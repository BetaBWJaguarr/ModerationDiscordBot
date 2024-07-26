package beta.com.moderationdiscordbot.slashcommandsmanager.commands.verifycommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.databasemanager.VerifySystem.VerifyMongo;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.permissionsmanager.PermType;
import beta.com.moderationdiscordbot.permissionsmanager.PermissionsManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.memberverifysystem.MemberVerifySystem;
import beta.com.moderationdiscordbot.memberverifysystem.Status;
import beta.com.moderationdiscordbot.utils.ModLogEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyCommands extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;
    private final VerifyMongo verifyMongo;
    private final ModLogEmbed modLogEmbed;

    public VerifyCommands(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorManager, RateLimit rateLimit, VerifyMongo verifyMongo) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.errorManager = errorManager;
        this.rateLimit = rateLimit;
        this.verifyMongo = verifyMongo;
        this.modLogEmbed = new ModLogEmbed(languageManager, serverSettings);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("verify") && event.getSubcommandName().equals("user")) {
            PermissionsManager permissionsManager = new PermissionsManager();

            if (!permissionsManager.checkPermissionAndOption(event, PermType.MANAGE_CHANNEL, embedBuilderManager, serverSettings, "commands.verify.no_permissions")) {
                return;
            }

            if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                return;
            }

            String dcserverid = event.getGuild().getId();
            boolean isVerifySystemEnabled = serverSettings.getVerifySystem(dcserverid);
            if (!isVerifySystemEnabled) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.system_disabled", null, serverSettings.getLanguage(dcserverid)).build())
                        .setEphemeral(true)
                        .queue();
                return;
            }

            String mention = event.getOption("username").getAsString();
            Pattern mentionPattern = Pattern.compile("<@!?(\\d+)>");
            Matcher matcher = mentionPattern.matcher(mention);

            if (matcher.find()) {
                String userToVerifyId = matcher.group(1);
                event.getGuild().retrieveMemberById(userToVerifyId).queue(userToVerify -> {
                    String userId = userToVerify.getUser().getId();
                    String username = userToVerify.getUser().getName();

                    Integer level;
                    String levelStr = event.getOption("level") != null ? event.getOption("level").getAsString() : null;

                    if (levelStr == null) {
                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.missing_level", null, serverSettings.getLanguage(dcserverid)).build())
                                .setEphemeral(true)
                                .queue();
                        return;
                    }

                    try {
                        level = Integer.parseInt(levelStr);
                    } catch (NumberFormatException e) {
                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.invalid_level", null, serverSettings.getLanguage(dcserverid)).build())
                                .setEphemeral(true)
                                .queue();
                        return;
                    }

                    Status status;
                    String statusStr = event.getOption("status") != null ? event.getOption("status").getAsString().toUpperCase() : null;

                    if (statusStr == null) {
                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.missing_status", null, serverSettings.getLanguage(dcserverid)).build())
                                .setEphemeral(true)
                                .queue();
                        return;
                    }

                    try {
                        status = Status.valueOf(statusStr);
                    } catch (IllegalArgumentException e) {
                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.invalid_status", null, serverSettings.getLanguage(dcserverid)).build())
                                .setEphemeral(true)
                                .queue();
                        return;
                    }

                    MemberVerifySystem existingRecord = verifyMongo.findMemberVerifySystem(username, dcserverid);

                    MemberVerifySystem memberVerify;
                    if (existingRecord != null) {
                        memberVerify = new MemberVerifySystem(
                                UUID.fromString(existingRecord.getId().toString()),
                                username,
                                level,
                                status
                        );
                    } else {
                        memberVerify = new MemberVerifySystem(
                                UUID.randomUUID(),
                                username,
                                level,
                                status
                        );
                    }

                    verifyMongo.upsertMemberVerifySystem(memberVerify, dcserverid);

                    String statusMessageKey = status == Status.VERIFIED ? "commands.verify.success_accepted" : "commands.verify.success_rejected";
                    event.replyEmbeds(embedBuilderManager.createEmbed(statusMessageKey, null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();

                    String titleKey = "commands.verify.modlog.title";
                    String userKey = "commands.verify.modlog.user";
                    String reasonKey = "commands.verify.modlog.status";
                    String reasonMessageKey = status == Status.VERIFIED ? "commands.verify.modlog.success" : "commands.verify.modlog.failure";
                    String reason = languageManager.getMessage(reasonMessageKey, serverSettings.getLanguage(dcserverid));

                    modLogEmbed.sendLog(
                            dcserverid,
                            event,
                            titleKey,
                            userKey,
                            reasonKey,
                            username,
                            reason
                    );

                }, failure -> {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.user_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                });
            } else {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.invalid_mention", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
            }
        }
    }
}
