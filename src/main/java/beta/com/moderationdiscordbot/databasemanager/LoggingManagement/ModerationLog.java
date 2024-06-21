package beta.com.moderationdiscordbot.databasemanager.LoggingManagement;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.Date;
import java.util.List;

public class ModerationLog {
    private final MongoCollection<Document> collection;

    public ModerationLog(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public void addLog(String serverId, String userId, String reason, Date duration, String logType, String logKey) {
        try {
            var filter = Filters.eq("serverId", serverId);
            var update = Updates.push(logType, new Document(logKey, userId)
                    .append("reason", reason).append("duration", duration));
            collection.updateOne(filter, update, new UpdateOptions().upsert(true));
        } catch (MongoException e) {
            System.err.println("Error updating document in MongoDB: " + e.getMessage());
        }
    }

    public List<Document> getLogs(String serverId, String logType) {
        try {
            var filter = Filters.eq("serverId", serverId);
            Document document = collection.find(filter).first();

            if (document != null) {
                return (List<Document>) document.get(logType);
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving document from MongoDB: " + e.getMessage());
        }

        return null;
    }

    public void removeLog(String serverId, String userId, String logType, String logKey) {
        try {
            var filter = Filters.and(Filters.eq("serverId", serverId), Filters.eq(logType + "." + logKey, userId));
            var update = Updates.pull(logType, new Document(logKey, userId));
            collection.updateOne(filter, update);
        } catch (MongoException e) {
            System.err.println("Error removing document from MongoDB: " + e.getMessage());
        }
    }
}