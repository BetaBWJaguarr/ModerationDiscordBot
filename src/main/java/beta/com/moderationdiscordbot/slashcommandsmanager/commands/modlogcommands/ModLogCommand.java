package beta.com.moderationdiscordbot.slashcommandsmanager.commands.modlogcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ModLogCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;

    public ModLogCommand(ServerSettings serverSettings, LanguageManager languageManager) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("modlog")) {
            String dcserverid = event.getGuild().getId();
            if (!event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.modlog.no_permissions", null, serverSettings.getLanguage(dcserverid)).build()).setEphemeral(true).queue();
                return;
            }

            String channelId = event.getOption("channel").getAsString();
            serverSettings.setModLogChannel(dcserverid, channelId);

            event.replyEmbeds(embedBuilderManager.createEmbed("commands.modlog.success", null, serverSettings.getLanguage(dcserverid)).build()).queue();
        }
    }
}