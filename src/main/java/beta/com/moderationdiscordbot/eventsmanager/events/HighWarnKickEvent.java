package beta.com.moderationdiscordbot.eventsmanager.events;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.WarnLog;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Guild;
import org.bson.Document;

import java.util.List;

public class HighWarnKickEvent {
    private final WarnLog warnLog;

    public HighWarnKickEvent(WarnLog warnLog) {
        this.warnLog = warnLog;
    }

    public void checkAndKickUser(String serverId, Member member) {
        List<Document> warnLogs = warnLog.getWarnLogs(serverId);
        if (warnLogs == null) return;

        int userWarnCount = 0;
        for (Document log : warnLogs) {
            if (log.getString("userId").equals(member.getId())) {
                userWarnCount++;
            }
        }

        int warnKickTimes = warnLog.getWarnKickTimes(serverId);

        if (userWarnCount >= warnKickTimes) {
            Guild guild = member.getGuild();
            guild.kick(member).queue(
                    success -> {
                        for (Document log : warnLogs) {
                            if (log.getString("userId").equals(member.getId())) {
                                warnLog.removeWarnLog(serverId, log.getString("warningId"));
                            }
                        }
                    },
                    error -> {
                        System.err.println("Error kicking user: " + error.getMessage());
                    }
            );
        }
    }
}
