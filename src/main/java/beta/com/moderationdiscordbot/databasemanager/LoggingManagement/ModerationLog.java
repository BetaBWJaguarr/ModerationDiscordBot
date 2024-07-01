package beta.com.moderationdiscordbot.databasemanager.LoggingManagement;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.Date;
import java.util.List;


/**
 * This class manages moderation logs in a MongoDB collection for a Discord bot.
 * It provides methods to add, retrieve, and remove moderation logs for specific server and user combinations.
 *
 * Dependencies:
 * - MongoCollection<Document>: Represents a MongoDB collection where moderation logs are stored.
 * - Document: Represents a BSON document used for MongoDB operations.
 * - Filters: Provides static factory methods for creating filters used in MongoDB queries.
 * - Updates: Provides static factory methods for creating update operations used in MongoDB updates.
 * - UpdateOptions: Options to control the behavior of update operations.
 *
 * Usage:
 * Initialize ModerationLog with a MongoCollection<Document> instance representing the collection
 * where moderation logs are stored. Use methods like addLog(), getLogs(), and removeLog()
 * to interact with moderation logs in the MongoDB database..
 *
 * addLog(String serverId, String userId, String reason, Date duration, String logType, String logKey):
 * This method is used to add a new moderation log to the MongoDB collection. It takes the server ID, user ID, reason
 * for the moderation action, duration of the action, type of the log, and a key for the log as parameters. It creates a
 * filter to find the document for the specific server and an update operation to add the new log. If the document for the server
 * does not exist, it will be created due to the upsert(true) option.
 *
 * removeLog(String serverId, String userId, String logType, String logKey):
 * This method is used to remove a specific moderation log from the MongoDB
 * collection. It takes the server ID, user ID, type of the log, and a key for the log as parameters. It creates a filter to find
 * the document for the specific server and the specific log and an update operation to remove the log. If the document for the
 * server or the log does not exist, no action will be taken.
 *
 */

public class ModerationLog {
    private final MongoCollection<Document> collection;

    public ModerationLog(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public void addLog(String serverId, String userId, String reason, Date duration, String logType, String logKey) {
        addLog(serverId, userId, reason, duration, logType, logKey, null);
    }

    public void addLog(String serverId, String userId, String reason, Date duration, String logType, String logKey, String channelId) {
        try {
            var filter = Filters.eq("serverId", serverId);
            var logDocument = new Document(logKey, userId)
                    .append("reason", reason)
                    .append("duration", duration);
            if (channelId != null && !channelId.isEmpty()) {
                logDocument.append("channelId", channelId);
            }
            var update = Updates.push(logType, logDocument);
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
        removeLog(serverId, userId, logType, logKey, null);
    }

    public void removeLog(String serverId, String userId, String logType, String logKey, String channelId) {
        try {
            var filter = Filters.and(Filters.eq("serverId", serverId), Filters.eq(logType + "." + logKey, userId));
            if (channelId != null && !channelId.isEmpty()) {
                filter = Filters.and(filter, Filters.eq(logType + ".channelId", channelId));
            }
            var update = Updates.pull(logType, new Document(logKey, userId));
            collection.updateOne(filter, update);
        } catch (MongoException e) {
            System.err.println("Error removing document from MongoDB: " + e.getMessage());
        }
    }
}