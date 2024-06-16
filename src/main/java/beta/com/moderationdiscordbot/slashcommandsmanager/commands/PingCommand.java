package beta.com.moderationdiscordbot.slashcommandsmanager.commands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.text.MessageFormat;

public class PingCommand extends ListenerAdapter {

    private LanguageManager languageManager;
    private ServerSettings serverSettings;

    public PingCommand(ServerSettings serverSettings,LanguageManager languageManager) {
        this.languageManager = languageManager;
        this.serverSettings = serverSettings;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("ping")) {
            long time = System.currentTimeMillis();
            String discordserverid = event.getGuild().getId();
            String message = languageManager.getMessage("commands.ping.pinging", serverSettings.getLanguage(discordserverid));
            event.reply(message).setEphemeral(true).queue(interactionHook -> {
                interactionHook.deleteOriginal().queue();
                long pingTime = System.currentTimeMillis() - time;
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(languageManager.getMessage("commands.ping.pong", serverSettings.getLanguage(discordserverid)));
                String pingTimeMessageTemplate = languageManager.getMessage("commands.ping.time", serverSettings.getLanguage(discordserverid));
                String pingTimeMessage = MessageFormat.format(pingTimeMessageTemplate, pingTime);
                embed.setDescription(pingTimeMessage);
                embed.setColor(Color.CYAN);
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            });
        }
    }
}