package beta.com.moderationdiscordbot.eventsmanager.events;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BotJoinServer extends ListenerAdapter {

    private final ServerSettings serverSettings;

    public BotJoinServer(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        String discordServerId = event.getGuild().getId();
        serverSettings.setServerSettings(discordServerId, true,true);
    }
}