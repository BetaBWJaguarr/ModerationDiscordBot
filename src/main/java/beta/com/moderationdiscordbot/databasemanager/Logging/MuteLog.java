package beta.com.moderationdiscordbot.databasemanager.Logging;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.Date;
import java.util.List;

public class MuteLog {
    private final MongoCollection<Document> collection;

    public MuteLog(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public void addMuteLog(String serverId, String username, String reason, Date duration) {
        try {
            var filter = Filters.eq("_id", serverId);
            var update = Updates.push("mutes", new Document("username", username)
                    .append("reason", reason).append("duration", duration));
            collection.updateOne(filter, update, new UpdateOptions().upsert(true));
        } catch (MongoException e) {
            System.err.println("Error updating document in MongoDB: " + e.getMessage());
        }
    }

    public List<Document> getMuteLogs(String serverId) {
        try {
            var filter = Filters.eq("_id", serverId);
            Document document = collection.find(filter).first();

            if (document != null) {
                return (List<Document>) document.get("mutes");
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving document from MongoDB: " + e.getMessage());
        }

        return null;
    }

    public void removeMuteLog(String serverId, String username) {
        try {
            var filter = Filters.and(Filters.eq("_id", serverId), Filters.eq("mutes.username", username));
            var update = Updates.pull("mutes", new Document("username", username));
            collection.updateOne(filter, update);
        } catch (MongoException e) {
            System.err.println("Error removing document from MongoDB: " + e.getMessage());
        }
    }
}