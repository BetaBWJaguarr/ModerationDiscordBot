package beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.ModerationLog;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Date;
import java.util.List;

public class BanLog {
    private final ModerationLog moderationLog;

    public BanLog(MongoCollection<Document> collection) {
        this.moderationLog = new ModerationLog(collection);
    }

    public void addBanLog(String serverId, String userId, String reason, Date duration) {
        moderationLog.addLog(serverId, userId, reason, duration, "users", "userId");
    }

    public List<Document> getBanLogs(String serverId) {
        return moderationLog.getLogs(serverId, "users");
    }

    public void removeBanLog(String serverId, String userId) {
        moderationLog.removeLog(serverId, userId, "users", "userId");
    }
}