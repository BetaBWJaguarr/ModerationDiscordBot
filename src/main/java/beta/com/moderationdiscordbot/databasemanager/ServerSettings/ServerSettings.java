package beta.com.moderationdiscordbot.databasemanager.ServerSettings;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

public class ServerSettings {
    private final MongoCollection<Document> collection;

    private static final int TIME_LIMIT_DEFAULT = 5;

    private static final int MESSAGE_LIMIT_DEFAULT = 5;

    private static final String DEFAULT_LANGUAGE = "en";


    public ServerSettings(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    //Main method to set the server settings

    public void setServerSettings(String discordServerId, boolean antiSpamEnabled,boolean antiVirusEnabled) {
        var filter = Filters.eq("_id", discordServerId);
        var update = Updates.combine(
                Updates.set("settings.antispam", antiSpamEnabled),
                Updates.set("settings.antivirus", antiVirusEnabled),
                Updates.set("settings.antiSpamTimeLimit", TIME_LIMIT_DEFAULT),
                Updates.set("settings.antiSpamMessageLimit", MESSAGE_LIMIT_DEFAULT),
                Updates.set("settings.language", DEFAULT_LANGUAGE)
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

    // Language Feature

    public String getLanguage(String discordServerId) {
        try {
            var filter = Filters.eq("_id", discordServerId);
            Document document = collection.find(filter).first();

            if (document != null) {
                Document settings = (Document) document.get("settings");
                return settings.getString("language");
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving document from MongoDB: " + e.getMessage());
        }

        return DEFAULT_LANGUAGE; // Return default language if not set
    }

    public boolean setLanguage(String discordServerId, String language) {
        try {
            var filter = Filters.eq("_id", discordServerId);
            var update = Updates.set("settings.language", language);
            UpdateResult result = collection.updateOne(filter, update, new UpdateOptions().upsert(true));
            return result.getModifiedCount() > 0;
        } catch (MongoException e) {
            System.err.println("Error updating document in MongoDB: " + e.getMessage());
            return false;
        }
    }

    // Language Feature


    //Mod Log Feature
    public void setModLogChannel(String discordServerId, String channelId) {
        try {
            var filter = Filters.eq("_id", discordServerId);
            var update = Updates.set("settings.modLogChannel", channelId);
            UpdateResult result = collection.updateOne(filter, update, new UpdateOptions().upsert(true));
        } catch (MongoException e) {
            System.err.println("Error updating document in MongoDB: " + e.getMessage());
        }
    }

    public String getModLogChannel(String discordServerId) {
        try {
            var filter = Filters.eq("_id", discordServerId);
            Document document = collection.find(filter).first();
            if (document != null) {
                Document settings = (Document) document.get("settings");
                if (settings != null) {
                    return settings.getString("modLogChannel");
                }
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving document from MongoDB: " + e.getMessage());
        }
        return null;
    }

    //Mod Log Feature


    //Anti Virus Feature

    public void setAntiVirus(String discordServerId, boolean antiVirusEnabled) {
        var filter = Filters.eq("_id", discordServerId);
        var update = Updates.set("settings.antivirus", antiVirusEnabled);
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    public boolean getAntiVirus(String discordServerId) {
        var filter = Filters.eq("_id", discordServerId);
        var document = collection.find(filter).first();
        return document != null && document.getBoolean("settings.antivirus", false);
    }

    //Anti Virus Feature

}