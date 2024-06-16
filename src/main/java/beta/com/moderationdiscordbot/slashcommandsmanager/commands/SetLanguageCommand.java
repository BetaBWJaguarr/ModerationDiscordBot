package beta.com.moderationdiscordbot.slashcommandsmanager.commands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class SetLanguageCommand extends ListenerAdapter {

    private ServerSettings serverSettings;
    private LanguageManager languageManager;
    private List<String> validLanguages = Arrays.asList("en", "tr");

    public SetLanguageCommand(ServerSettings serverSettings, LanguageManager languageManager) {
        this.serverSettings = serverSettings;
        this.languageManager = languageManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("setlanguage")) {
            String discordserverid = event.getGuild().getId();
            if (!event.getMember().isOwner()) {
                event.reply(languageManager.getMessage("commands.setlanguage.no_permissions",serverSettings.getLanguage(discordserverid))).setEphemeral(true).queue();
                return;
            }
            String newLanguage = event.getOption("language").getAsString();
            if (validLanguages.contains(newLanguage) && serverSettings.setLanguage(discordserverid, newLanguage)) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(languageManager.getMessage("commands.setlanguage.title", serverSettings.getLanguage(discordserverid)));
                embed.setDescription(languageManager.getMessage("commands.setlanguage.success", serverSettings.getLanguage(discordserverid)));
                embed.setColor(Color.GREEN);
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            } else {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(languageManager.getMessage("commands.setlanguage.title", serverSettings.getLanguage(discordserverid)));
                embed.setDescription(languageManager.getMessage("commands.setlanguage.error", serverSettings.getLanguage(discordserverid)));
                embed.setColor(Color.RED);
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            }
        }
    }
}