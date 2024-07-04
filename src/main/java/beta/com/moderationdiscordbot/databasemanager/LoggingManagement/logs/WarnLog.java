package beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WarnLog {
    private final MongoCollection<Document> collection;

    public WarnLog(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public void addWarnLog(String serverId, String userId, String reason, String moderatorId) {
        try {
            String warningId = UUID.randomUUID().toString();
            Document warnLog = new Document()
                    .append("warningId", warningId)
                    .append("userId", userId)
                    .append("reason", reason)
                    .append("moderator", moderatorId);

            var filter = Filters.eq("serverId", serverId);
            var update = Updates.combine(
                    Updates.push("warns", warnLog),
                    Updates.setOnInsert("warn-kick-times", 3)
            );
            collection.updateOne(filter, update, new UpdateOptions().upsert(true));
        } catch (MongoException e) {
            System.err.println("Error updating document in MongoDB: " + e.getMessage());
        }
    }


    public List<Document> getWarnIds(String serverId, String userId) {
        try {
            var filter = Filters.and(
                    Filters.eq("serverId", serverId),
                    Filters.eq("warns.userId", userId)
            );

            Document document = collection.find(filter).first();

            if (document != null) {
                return (List<Document>) document.get("warns");
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving document from MongoDB: " + e.getMessage());
        }

        return null;
    }


    public List<Document> getWarnLogs(String serverId) {
        try {
            var filter = Filters.eq("serverId", serverId);
            Document document = collection.find(filter).first();

            if (document != null) {
                return (List<Document>) document.get("warns");
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving document from MongoDB: " + e.getMessage());
        }

        return null;
    }

    public boolean removeWarnLog(String serverId, String warningId) {
        try {
            var filter = Filters.eq("serverId", serverId);
            var update = Updates.pull("warns", new Document("warningId", warningId));
            var result = collection.updateOne(filter, update);
            return result.getModifiedCount() > 0;
        } catch (MongoException e) {
            System.err.println("Error removing document from MongoDB: " + e.getMessage());
        }
        return false;
    }

    public void setWarnKickTimes(String serverId, int warnKickTimes) {
        try {
            var filter = Filters.eq("serverId", serverId);
            var update = Updates.set("warn-kick-times", warnKickTimes);
            collection.updateOne(filter, update, new UpdateOptions().upsert(true));
        } catch (MongoException e) {
            System.err.println("Error setting warn-kick-times in MongoDB: " + e.getMessage());
        }
    }

    public int getWarnKickTimes(String serverId) {
        try {
            var filter = Filters.eq("serverId", serverId);
            Document document = collection.find(filter).first();

            if (document != null && document.containsKey("warn-kick-times")) {
                return document.getInteger("warn-kick-times");
            } else {
                return 3;
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving warn-kick-times from MongoDB: " + e.getMessage());
        }

        return 3;
    }
}
