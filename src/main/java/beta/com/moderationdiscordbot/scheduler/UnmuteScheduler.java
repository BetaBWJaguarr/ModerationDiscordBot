package beta.com.moderationdiscordbot.scheduler;

import java.util.Date;
import java.util.List;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.MuteLog;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.bson.Document;

public class UnmuteScheduler {
    private final MuteLog muteLog;
    private final JDA jda;

    public UnmuteScheduler(MuteLog muteLog, JDA jda) {
        this.muteLog = muteLog;
        this.jda = jda;
    }

    public void checkAndUnmuteUsers(String serverId) {
        List<Document> muteLogs = muteLog.getMuteLogs(serverId);
        if (muteLogs == null || muteLogs.isEmpty()) {
            return;
        }

        Guild guild = jda.getGuildById(serverId);
        if (guild == null) {
            return;
        }

        Role muteRole = guild.getRolesByName("Muted", true).get(0);

        for (Document muteLog : muteLogs) {
            Date muteDuration = muteLog.getDate("duration");
            if (muteDuration != null && new Date().after(muteDuration)) {
                String userId = muteLog.getString("userId");
                guild.retrieveMemberById(userId).queue(member -> {
                    guild.removeRoleFromMember(member, muteRole).queue();
                    this.muteLog.removeMuteLog(serverId, userId);
                });
            }
        }
    }


    public void checkAndUnmuteUsersInAllGuilds() {
        List<Guild> guilds = jda.getGuilds();
        for (Guild guild : guilds) {
            String serverId = guild.getId();
            checkAndUnmuteUsers(serverId);
        }
    }
}