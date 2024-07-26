package beta.com.moderationdiscordbot.slashcommandsmanager.commands.clearcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.clearcommands.utils.ModLogMessage;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.atomic.AtomicInteger;

public class ClearContentCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;

    public ClearContentCommand(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorManager, RateLimit rateLimit) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.errorManager = errorManager;
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("clear") || !event.getSubcommandName().equals("content")) {
            return;
        }

        if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
            return;
        }

        String content = event.getOption("content").getAsString();
        int amount = event.getOption("amount").getAsInt();
        String channelId = event.getOption("channel").getAsChannel().getId();

        TextChannel textChannel = event.getGuild().getTextChannelById(channelId);
        if (textChannel == null) {
            sendErrorResponse(event, "commands.clear.channel_not_found");
            return;
        }

        AtomicInteger deletedMessagesCount = new AtomicInteger(0);

        textChannel.getIterableHistory().takeAsync(amount).thenAcceptAsync(messages -> {
            messages.stream()
                    .filter(message -> message.getContentRaw().contains(content))
                    .forEach(message -> message.delete().queue(
                            success -> {
                                deletedMessagesCount.incrementAndGet();
                                if (deletedMessagesCount.get() == messages.size()) {
                                    sendSuccessResponse(event, textChannel, deletedMessagesCount.get());
                                    ModLogMessage.sendModLogMessage(serverSettings, languageManager, event, textChannel, deletedMessagesCount.get());
                                }
                            },
                            error -> errorManager.sendErrorMessage((Exception) error, event.getChannel().asTextChannel())
                    ));
        }).exceptionally(e -> {
            sendErrorResponse(event, "commands.clear.error");
            errorManager.sendErrorMessage((Exception) e, event.getChannel().asTextChannel());
            return null;
        });
    }

    private void sendSuccessResponse(SlashCommandInteractionEvent event, TextChannel textChannel, int deletedMessagesCount) {
        event.replyEmbeds(
                embedBuilderManager.createEmbed("commands.clear.success", null, serverSettings.getLanguage(event.getGuild().getId()), textChannel.getAsMention(), deletedMessagesCount)
                        .build()
        ).queue();
    }

    private void sendErrorResponse(SlashCommandInteractionEvent event, String messageKey) {
        event.replyEmbeds(
                embedBuilderManager.createEmbed(messageKey, null, serverSettings.getLanguage(event.getGuild().getId()))
                        .build()
        ).setEphemeral(true).queue();
    }
}
