package beta.com.moderationdiscordbot.slashcommandsmanager.commands.clearcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.clearcommands.utils.ModLogMessage;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClearEmbedCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;

    public ClearEmbedCommand(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorManager, RateLimit rateLimit) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.errorManager = errorManager;
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (!event.getName().equals("clear")) return;

            if (!event.getSubcommandName().equals("embeds")) return;

            if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                return;
            }

            int amount = (int) event.getOption("amount").getAsLong();
            String channelId = event.getOption("channel").getAsChannel().getId();

            TextChannel textChannel = event.getGuild().getTextChannelById(channelId);
            if (textChannel == null) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.clear.channel_not_found", null, serverSettings.getLanguage(event.getGuild().getId())).build()).setEphemeral(true).queue();
                return;
            }

            AtomicInteger deletedEmbedsCount = new AtomicInteger(0);

            textChannel.getIterableHistory().takeAsync(amount).thenAcceptAsync(messages -> {
                messages.forEach(message -> {
                    List<MessageEmbed> embeds = message.getEmbeds();
                    embeds.forEach(embed -> {
                        message.delete().queue(
                                success -> {
                                    deletedEmbedsCount.incrementAndGet();
                                    if (deletedEmbedsCount.get() == messages.size()) {
                                        event.replyEmbeds(embedBuilderManager.createEmbed("commands.clear.success", null, serverSettings.getLanguage(event.getGuild().getId()), textChannel.getAsMention(), deletedEmbedsCount.get()).build()).queue();
                                        ModLogMessage.sendModLogMessage(serverSettings, languageManager, event, textChannel, deletedEmbedsCount.get());
                                    }
                                },
                                error -> errorManager.sendErrorMessage((Exception) error, event.getChannel().asTextChannel())
                        );
                    });
                });
            }).exceptionally(e -> {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.clear.error", null, serverSettings.getLanguage(event.getGuild().getId())).build()).queue();
                errorManager.sendErrorMessage((Exception) e, event.getChannel().asTextChannel());
                return null;
            });
        } catch (Exception e) {
            errorManager.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }
}
