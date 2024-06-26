package beta.com.moderationdiscordbot.slashcommandsmanager.commands.clearcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.clearcommands.utils.ModLogMessage;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class ClearAllCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;

    public ClearAllCommand(ServerSettings serverSettings, LanguageManager languageManager) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getName().equals("clear")) {

            String discordServerId = event.getGuild().getId();

            if (event.getSubcommandName().equals("all")) {
                if (event.getOption("amount") == null || event.getOption("channel") == null) {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.clear.invalid_parameters", null, serverSettings.getLanguage(discordServerId)).build()).setEphemeral(true).queue();
                    return;
                }

                int amount = (int) event.getOption("amount").getAsLong();
                String channelId = event.getOption("channel").getAsChannel().getId();

                TextChannel textChannel = event.getGuild().getTextChannelById(channelId);
                if (textChannel == null) {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.clear.channel_not_found", null, serverSettings.getLanguage(discordServerId)).build()).setEphemeral(true).queue();
                    return;
                }

                textChannel.getIterableHistory().takeAsync(amount).thenAcceptAsync(messages -> {
                    AtomicInteger deletedMessagesCount = new AtomicInteger(0);

                    messages.forEach(message -> {
                        textChannel.deleteMessageById(message.getIdLong()).queue(
                                success -> {
                                    deletedMessagesCount.incrementAndGet();

                                    if (deletedMessagesCount.get() == messages.size()) {
                                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.clear.success", null, serverSettings.getLanguage(discordServerId), textChannel.getAsMention(), deletedMessagesCount.get()).build()).queue();
                                        ModLogMessage.sendModLogMessage(serverSettings, languageManager, event, textChannel, deletedMessagesCount.get());
                                    }
                                },
                                error -> {
                                    System.out.println("Failed to delete message: " + error.getMessage());
                                }
                        );
                    });
                }).exceptionally(e -> {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.clear.error", null, serverSettings.getLanguage(discordServerId)).build()).queue();
                    e.printStackTrace();
                    return null;
                });
            }
        }
    }
}
