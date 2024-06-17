package beta.com.moderationdiscordbot.databasemanager.Logging;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.Date;
import java.util.List;

public class BanLog {
    private final MongoCollection<Document> collection;

    public BanLog(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public void addBanLog(String serverId, String userId, String reason, Date duration) {
        try {
            var filter = Filters.eq("serverId", serverId);
            var update = Updates.push("users", new Document("userId", userId)
                    .append("ban", new Document("reason", reason).append("duration", duration)));
            collection.updateOne(filter, update, new UpdateOptions().upsert(true));
        } catch (MongoException e) {
            System.err.println("Error updating document in MongoDB: " + e.getMessage());
        }
    }

    public List<Document> getBanLogs(String serverId) {
        try {
            var filter = Filters.eq("serverId", serverId);
            Document document = collection.find(filter).first();

            if (document != null) {
                return (List<Document>) document.get("users");
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving document from MongoDB: " + e.getMessage());
        }

        return null;
    }

    public void removeBanLog(String serverId, String userId) {
        try {
            var filter = Filters.and(Filters.eq("serverId", serverId), Filters.eq("users.userId", userId));
            var update = Updates.pull("users", new Document("userId", userId));
            collection.updateOne(filter, update);
        } catch (MongoException e) {
            System.err.println("Error removing document from MongoDB: " + e.getMessage());
        }
    }

}