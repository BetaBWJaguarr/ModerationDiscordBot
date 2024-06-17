package beta.com.moderationdiscordbot.slashcommandsmanager.commands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class PingCommand extends ListenerAdapter {

    private final ServerSettings serverSettings;
    private final EmbedBuilderManager embedManager;
    private final LanguageManager languageManager;

    public PingCommand(ServerSettings serverSettings, LanguageManager languageManager) {
        this.serverSettings = serverSettings;
        this.embedManager = new EmbedBuilderManager(languageManager);
        this.languageManager = languageManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("ping")) {
            long time = System.currentTimeMillis();
            String discordserverid = event.getGuild().getId();
            String language = serverSettings.getLanguage(discordserverid);
            String message = languageManager.getMessage("commands.ping.pinging", serverSettings.getLanguage(discordserverid));
            event.reply(message).setEphemeral(true).queue(interactionHook -> {
                interactionHook.deleteOriginal().queue();
                long pingTime = System.currentTimeMillis() - time;
                event.getHook().sendMessageEmbeds(embedManager.createEmbedWithColor(
                        "commands.ping.pong",
                        "commands.ping.time",
                        language,
                        Color.CYAN,
                        pingTime).build()).queue();
            });
        }
    }
}