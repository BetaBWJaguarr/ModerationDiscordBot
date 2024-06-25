package beta.com.moderationdiscordbot.slashcommandsmanager.commands.clearcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class ClearFileCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;

    public ClearFileCommand(ServerSettings serverSettings, LanguageManager languageManager) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("clear")) {
            if (event.getSubcommandName().equals("files")) {
                if (event.getOption("amount") == null || event.getOption("channel") == null) {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.clear.invalid_parameters", null, serverSettings.getLanguage(event.getGuild().getId())).build()).setEphemeral(true).queue();
                    return;
                }

                int amount = (int) event.getOption("amount").getAsLong();
                String channelId = event.getOption("channel").getAsChannel().getId();

                TextChannel textChannel = event.getGuild().getTextChannelById(channelId);
                if (textChannel == null) {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.clear.channel_not_found", null, serverSettings.getLanguage(event.getGuild().getId())).build()).setEphemeral(true).queue();
                    return;
                }

                textChannel.getIterableHistory().takeAsync(amount).thenAcceptAsync(messages -> {
                    messages.forEach(message -> {
                        List<Message.Attachment> attachments = message.getAttachments();
                        attachments.forEach(attachment -> {
                            message.delete().queue(
                                    success -> System.out.println("Attachment message deleted successfully"),
                                    error -> System.out.println("Failed to delete attachment message: " + error.getMessage())
                            );
                        });
                    });
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.clear.files_success", null, serverSettings.getLanguage(event.getGuild().getId()), textChannel.getAsMention(),amount).build()).queue();
                }).exceptionally(e -> {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.clear.error", null, serverSettings.getLanguage(event.getGuild().getId())).build()).queue();
                    return null;
                });
            }
        }
    }
}
