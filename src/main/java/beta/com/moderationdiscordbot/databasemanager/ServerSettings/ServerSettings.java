package beta.com.moderationdiscordbot.databasemanager.ServerSettings;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

public class ServerSettings {
    private final MongoCollection<Document> collection;

    private static final int TIME_LIMIT_DEFAULT = 5;

    private static final int MESSAGE_LIMIT_DEFAULT = 5;


    public ServerSettings(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    //Main method to set the server settings

    public void setServerSettings(String discordServerId, boolean antiSpamEnabled) {
        var filter = Filters.eq("_id", discordServerId);
        var update = Updates.combine(
                Updates.set("settings.antispam", antiSpamEnabled),
                Updates.set("settings.antiSpamTimeLimit", TIME_LIMIT_DEFAULT),
                Updates.set("settings.antiSpamMessageLimit", MESSAGE_LIMIT_DEFAULT)
        );
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    //Main method to get the server settings


    //AntiSpam Feauture

    public boolean getAntiSpam(String discordServerId) {
        try {
            var filter = Filters.eq("_id", discordServerId);
            Document document = collection.find(filter).first();

            if (document != null) {
                Document settings = (Document) document.get("settings");
                return settings.getBoolean("antispam");
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving document from MongoDB: " + e.getMessage());
        }

        return false;
    }

    public void setAntiSpam(String discordServerId, boolean antiSpamEnabled) {
        var filter = Filters.eq("_id", discordServerId);
        var update = Updates.set("settings.antispam", antiSpamEnabled);
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }


    public void setAntiSpamMessageLimit(String discordServerId, int messageLimit) {
        var filter = Filters.eq("_id", discordServerId);
        var update = Updates.set("settings.antiSpamMessageLimit", messageLimit);
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    public void setAntiSpamTimeLimit(String discordServerId, int timeLimit) {
        var filter = Filters.eq("_id", discordServerId);
        var update = Updates.set("settings.antiSpamTimeLimit", timeLimit);
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    //AntiSpam Feauture
}