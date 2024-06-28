package beta.com.moderationdiscordbot.slashcommandsmanager.commands.clearcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;

public class ClearLogChannel extends ListenerAdapter {

    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final EmbedBuilderManager embedBuilderManager;
    private final HandleErrors errorManager;

    public ClearLogChannel(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorManager) {
        this.serverSettings = serverSettings;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.languageManager = languageManager;
        this.errorManager = errorManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (event.getName().equals("clearlogchannel")) {
                String discordServerId = event.getGuild().getId();

                if (!event.getMember().isOwner()) {
                    event.replyEmbeds(embedBuilderManager.createEmbed("commands.setclearlogchannel.only_owner", null, serverSettings.getLanguage(discordServerId)).build()).setEphemeral(true).queue();
                    return;
                }

                String channelId = event.getOption("channel").getAsString();

                serverSettings.setClearLogChannel(discordServerId, channelId);

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(languageManager.getMessage("commands.setclearlogchannel.success_title", serverSettings.getLanguage(discordServerId)));
                embedBuilder.setDescription(languageManager.getMessage("commands.setclearlogchannel.success_description", serverSettings.getLanguage(discordServerId)));
                embedBuilder.setColor(Color.GREEN);
                embedBuilder.setTimestamp(Instant.now());

                event.replyEmbeds(embedBuilder.build()).queue();
            }
        } catch (Exception e) {
            errorManager.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }
}
