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

    private void processUnmute(Guild guild, Document muteLog) {
        String userId = muteLog.getString("userId");
        Date muteDuration = muteLog.getDate("duration");

        if (muteDuration != null && new Date().after(muteDuration)) {
            Role muteRole = guild.getRolesByName("Muted", true).stream().findFirst().orElse(null);
            if (muteRole != null) {
                guild.retrieveMemberById(userId).queue(member -> {
                    guild.removeRoleFromMember(member, muteRole).queue(
                            success -> this.muteLog.removeMuteLog(guild.getId(), userId),
                            error -> {

                            }
                    );
                });
            }
        }
    }

    public void checkAndUnmuteUsers(String serverId) {
        List<Document> muteLogs = muteLog.getMuteLogs(serverId);
        if (muteLogs != null && !muteLogs.isEmpty()) {
            Guild guild = jda.getGuildById(serverId);
            if (guild != null) {
                muteLogs.forEach(muteLog -> processUnmute(guild, muteLog));
            }
        }
    }

    public void checkAndUnmuteUsersInAllGuilds() {
        jda.getGuilds().forEach(guild -> checkAndUnmuteUsers(guild.getId()));
    }
}
