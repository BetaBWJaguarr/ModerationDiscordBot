package beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.ModerationLog;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Date;
import java.util.List;

public class MuteLog {
    private final ModerationLog moderationLog;

    public MuteLog(MongoCollection<Document> collection) {
        this.moderationLog = new ModerationLog(collection);
    }

    public void addMuteLog(String serverId, String userid, String reason, Date duration) {
        moderationLog.addLog(serverId, userid, reason, duration, "mutes", "userId");
    }

    public List<Document> getMuteLogs(String serverId) {
        return moderationLog.getLogs(serverId, "mutes");
    }

    public void removeMuteLog(String serverId, String userId) {
        moderationLog.removeLog(serverId, userId, "mutes", "userId");
    }
}