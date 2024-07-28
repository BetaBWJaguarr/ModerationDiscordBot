package beta.com.moderationdiscordbot.databasemanager.ServerSettings;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import com.mongodb.client.result.UpdateResult;


public class ServerSettingsHelper {

    public static boolean getBooleanSetting(MongoCollection<Document> collection, String discordServerId, String settingKey, boolean defaultValue) {
        try {
            var filter = Filters.eq("_id", discordServerId);
            Document document = collection.find(filter).first();
            if (document != null) {
                Document settings = (Document) document.get("settings");
                if (settings != null && settings.containsKey(settingKey)) {
                    return settings.getBoolean(settingKey, defaultValue);
                }
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving document from MongoDB: " + e.getMessage());
        }
        return defaultValue;
    }

    public static void setBooleanSetting(MongoCollection<Document> collection, String discordServerId, String settingKey, boolean value) {
        var filter = Filters.eq("_id", discordServerId);
        var update = Updates.set("settings." + settingKey, value);
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    public static String getStringSetting(MongoCollection<Document> collection, String discordServerId, String settingKey, String defaultValue) {
        try {
            var filter = Filters.eq("_id", discordServerId);
            Document document = collection.find(filter).first();
            if (document != null) {
                Document settings = (Document) document.get("settings");
                if (settings != null) {
                    String[] keys = settingKey.split("\\.");
                    Document currentDocument = settings;
                    for (int i = 0; i < keys.length - 1; i++) {
                        currentDocument = (Document) currentDocument.get(keys[i]);
                        if (currentDocument == null) {
                            return defaultValue;
                        }
                    }
                    return currentDocument.getString(keys[keys.length - 1]);
                }
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving document from MongoDB: " + e.getMessage());
        }
        return defaultValue;
    }

    public static boolean setStringSetting(MongoCollection<Document> collection, String discordServerId, String settingKey, String value) {
        try {
            var filter = Filters.eq("_id", discordServerId);
            var update = Updates.set("settings." + settingKey, value);
            UpdateResult result = collection.updateOne(filter, update, new UpdateOptions().upsert(true));
            return result.getModifiedCount() > 0;
        } catch (MongoException e) {
            System.err.println("Error updating document in MongoDB: " + e.getMessage());
            return false;
        }
    }

    public static void setIntegerSetting(MongoCollection<Document> collection, String discordServerId, String settingKey, int value) {
        var filter = Filters.eq("_id", discordServerId);
        var update = Updates.set("settings." + settingKey, value);
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    public static void updateArraySetting(MongoCollection<Document> collection, String discordServerId, String settingKey, String value, boolean add) {
        var filter = Filters.eq("_id", discordServerId);
        var update = add ? Updates.addToSet("settings." + settingKey, value) : Updates.pull("settings." + settingKey, value);
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }
}
