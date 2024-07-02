package beta.com.moderationdiscordbot.eventsmanager.events;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AutoRoleEvent extends ListenerAdapter {

    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final EmbedBuilderManager embedBuilderManager;

    public AutoRoleEvent(ServerSettings serverSettings, LanguageManager languageManager) {
        this.serverSettings = serverSettings;
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        String guildId = guild.getId();

        String autoRoleId = serverSettings.getAutoRole(guildId);

        if (autoRoleId == null) {
            return;
        }

        Role autoRole = guild.getRoleById(autoRoleId);
        if (autoRole == null) {
            System.err.println("Auto role not found with ID: " + autoRoleId);
            return;
        }

        guild.addRoleToMember(event.getMember(), autoRole).queue(
                success -> System.out.println("Auto role assigned successfully to " + event.getMember().getUser().getName() + " in guild " + guild.getName()),
                error -> System.err.println("Failed to assign auto role to " + event.getMember().getUser().getName() + " in guild " + guild.getName() + ": " + error.getMessage())
        );
    }
}
