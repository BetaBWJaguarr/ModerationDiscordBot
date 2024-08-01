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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

public class VerifyCommands extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;
    private final VerifyMongo verifyMongo;
    private final ModLogEmbed modLogEmbed;
    private MemberVerifySystem memberVerify;
    private String dcserverid;
    private User adminuser;


    private final Map<String, Message> verificationMessages;

    public VerifyCommands(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorManager, RateLimit rateLimit, VerifyMongo verifyMongo) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.errorManager = errorManager;
        this.rateLimit = rateLimit;
        this.verifyMongo = verifyMongo;
        this.modLogEmbed = new ModLogEmbed(languageManager, serverSettings);
        this.verificationMessages = new HashMap<>();
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

            dcserverid = event.getGuild().getId();
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
            adminuser = event.getUser();

            if (matcher.find()) {
                String userToVerifyId = matcher.group(1);
                event.getGuild().retrieveMemberById(userToVerifyId).queue(userToVerify -> {
                    String userId = userToVerify.getUser().getId();
                    String username = userToVerify.getUser().getName();

                    String verifiedRoleId = serverSettings.getVerifiedRole(dcserverid);
                    Role verifiedRole = event.getGuild().getRoleById(verifiedRoleId);

                    if (verifiedRole != null && userToVerify.getRoles().contains(verifiedRole)) {
                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.already_verified", null, serverSettings.getLanguage(dcserverid)).build())
                                .setEphemeral(true)
                                .queue();
                        return;
                    }

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

                    sendVerificationDM(event.getUser().getJDA().getUserById(userToVerifyId), username, event);

                }, failure -> {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.user_not_found", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                });
            } else {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.invalid_mention", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
            }
        }
    }

    private void sendVerificationDM(User user, String username, SlashCommandInteractionEvent event) {
        String dcserverid = event.getGuild().getId();
        Emoji verificationEmoji = Emoji.fromUnicode("✅");
        long verificationTimeout = 1;

        EmbedBuilder dmEmbed = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setTitle(languageManager.getMessage("commands.verify.dm_title", serverSettings.getLanguage(dcserverid)))
                .setDescription(languageManager.getMessage("commands.verify.dm_description", serverSettings.getLanguage(dcserverid)))
                .addField(languageManager.getMessage("commands.verify.dm_field_username", serverSettings.getLanguage(dcserverid)), username, false)
                .addField(languageManager.getMessage("commands.verify.dm_field_instructions", serverSettings.getLanguage(dcserverid)),
                        languageManager.getMessage("commands.verify.dm_field_instructions_text", serverSettings.getLanguage(dcserverid)), false)
                .setFooter(languageManager.getMessage("commands.verify.dm_footer", serverSettings.getLanguage(dcserverid)));

        user.openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessageEmbeds(dmEmbed.build()).queue(message -> {
                message.addReaction(verificationEmoji).queue();

                verificationMessages.put(user.getId(), message);

                String verificationSentMessage = languageManager.getMessage("commands.verify.sent_message", serverSettings.getLanguage(dcserverid))
                        .replace("{username}", username);

                adminuser.openPrivateChannel().queue(channel -> {
                    EmbedBuilder confirmationEmbed = new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setTitle(languageManager.getMessage("commands.verify.sent_title", serverSettings.getLanguage(dcserverid)))
                            .setDescription(verificationSentMessage);
                    channel.sendMessageEmbeds(confirmationEmbed.build()).queue();
                });

                message.delete().queueAfter(verificationTimeout, TimeUnit.HOURS, aVoid -> {
                    if (verificationMessages.containsKey(user.getId())) {
                        verificationMessages.remove(user.getId());
                        handleVerificationTimeout(user, event);
                    }
                });
            }, failure -> {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.dm_failed", null, serverSettings.getLanguage(dcserverid)).build())
                        .setEphemeral(true)
                        .queue();
            });
        }, failure -> {
            event.replyEmbeds(embedBuilderManager.createEmbed("commands.verify.dm_failed", null, serverSettings.getLanguage(dcserverid)).build())
                    .setEphemeral(true)
                    .queue();
        });
    }


    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        String emoji = event.getReaction().getEmoji().getAsReactionCode();
        User user = event.getUser();

        if (user != null && verificationMessages.containsKey(user.getId())) {
            Message message = verificationMessages.get(user.getId());

            if (message.getId().equals(event.getMessageId())) {
                Guild guild = event.getJDA().getGuildById(dcserverid);

                Role verifiedRole = guild.getRoleById(serverSettings.getVerifiedRole(dcserverid));

                if (emoji.equals("✅")) {
                    guild.addRoleToMember(UserSnowflake.fromId(user.getId()), verifiedRole).queue(success -> {
                        verificationMessages.remove(user.getId());
                        message.delete().queue();

                        String verificationSuccessMessage = languageManager.getMessage("commands.verify.success_message", serverSettings.getLanguage(dcserverid));

                        adminuser.openPrivateChannel().queue(channel -> {
                            EmbedBuilder confirmationEmbed = new EmbedBuilder()
                                    .setColor(Color.GREEN)
                                    .setTitle(languageManager.getMessage("commands.verify.success_title", serverSettings.getLanguage(dcserverid)))
                                    .setDescription(verificationSuccessMessage);
                            channel.sendMessageEmbeds(confirmationEmbed.build()).queue();
                        });

                        verifyMongo.upsertMemberVerifySystem(memberVerify, dcserverid);
                    }, failure -> {
                        EmbedBuilder errorEmbed = embedBuilderManager.createEmbed("commands.verify.role_add_failed", null, serverSettings.getLanguage(dcserverid));
                        event.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
                    });
                } else {
                    EmbedBuilder errorEmbed = embedBuilderManager.createEmbed("commands.verify.invalid_reaction", null, serverSettings.getLanguage(dcserverid));
                    event.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
                }
            }
        }
    }


    private void handleVerificationTimeout(User user, SlashCommandInteractionEvent event) {
        String dcserverid = event.getGuild().getId();

        EmbedBuilder timeoutEmbed = embedBuilderManager.createEmbed("commands.verify.timeout", null, serverSettings.getLanguage(dcserverid));
        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(timeoutEmbed.build()).queue());

        MemberVerifySystem memberVerify = verifyMongo.findMemberVerifySystem(user.getName(), dcserverid);
        if (memberVerify != null) {
            memberVerify.setStatus(Status.REJECTED);
            verifyMongo.upsertMemberVerifySystem(memberVerify, dcserverid);
        }
    }
}