package beta.com.moderationdiscordbot.databasemanager.ServerSettings;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class ServerSettings {
    private final MongoCollection<Document> collection;

    private static final int TIME_LIMIT_DEFAULT = 5;
    private static final int MESSAGE_LIMIT_DEFAULT = 5;
    private static final String DEFAULT_LANGUAGE = "en";

    public ServerSettings(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    // Main method to set the server settings
    public void setServerSettings(String discordServerId, boolean antiSpamEnabled, boolean antiVirusEnabled) {
        var filter = Filters.eq("_id", discordServerId);
        var update = Updates.combine(
                Updates.set("settings.antispam", antiSpamEnabled),
                Updates.set("settings.antivirus", antiVirusEnabled),
                Updates.set("settings.antiSpamTimeLimit", TIME_LIMIT_DEFAULT),
                Updates.set("settings.antiSpamMessageLimit", MESSAGE_LIMIT_DEFAULT),
                Updates.set("settings.language", DEFAULT_LANGUAGE),
                Updates.set("settings.antiswearfeatures.enabled", false),
                Updates.set("settings.autopunish", false),
                Updates.set("settings.voiceaction.enabled", false),
                Updates.set("settings.verifysystem.enabled", false)
        );
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    // AntiSpam Feature
    public boolean getAntiSpam(String discordServerId) {
        return ServerSettingsHelper.getBooleanSetting(collection, discordServerId, "antispam", false);
    }

    public void setAntiSpam(String discordServerId, boolean antiSpamEnabled) {
        ServerSettingsHelper.setBooleanSetting(collection, discordServerId, "antispam", antiSpamEnabled);
    }

    public void setAntiSpamMessageLimit(String discordServerId, int messageLimit) {
        ServerSettingsHelper.setIntegerSetting(collection, discordServerId, "antiSpamMessageLimit", messageLimit);
    }

    public void setAntiSpamTimeLimit(String discordServerId, int timeLimit) {
        ServerSettingsHelper.setIntegerSetting(collection, discordServerId, "antiSpamTimeLimit", timeLimit);
    }

    // Language Feature
    public String getLanguage(String discordServerId) {
        return ServerSettingsHelper.getStringSetting(collection, discordServerId, "language", DEFAULT_LANGUAGE);
    }

    public boolean setLanguage(String discordServerId, String language) {
        return ServerSettingsHelper.setStringSetting(collection, discordServerId, "language", language);
    }

    // Mod Log Feature
    public void setModLogChannel(String discordServerId, String channelId) {
        ServerSettingsHelper.setStringSetting(collection, discordServerId, "modLogChannel", channelId);
    }

    public String getModLogChannel(String discordServerId) {
        return ServerSettingsHelper.getStringSetting(collection, discordServerId, "modLogChannel", null);
    }

    // Clear Log Feature
    public void setClearLogChannel(String discordServerId, String channelId) {
        ServerSettingsHelper.setStringSetting(collection, discordServerId, "clearLogChannel", channelId);
    }

    public String getClearLogChannel(String discordServerId) {
        return ServerSettingsHelper.getStringSetting(collection, discordServerId, "clearLogChannel", null);
    }

    // Anti Virus Feature
    public void setAntiVirus(String discordServerId, boolean antiVirusEnabled) {
        ServerSettingsHelper.setBooleanSetting(collection, discordServerId, "antivirus", antiVirusEnabled);
    }

    public boolean getAntiVirus(String discordServerId) {
        return ServerSettingsHelper.getBooleanSetting(collection, discordServerId, "antivirus", false);
    }

    // Auto Punish Feature
    public void setAutoPunishEnabled(String discordServerId, boolean enabled) {
        ServerSettingsHelper.setBooleanSetting(collection, discordServerId, "autopunish", enabled);
    }

    public boolean isAutoPunishEnabled(String discordServerId) {
        return ServerSettingsHelper.getBooleanSetting(collection, discordServerId, "autopunish", false);
    }

    // AntiSwear Feature
    public void setAntiSwearEnabled(String discordServerId, boolean enabled) {
        ServerSettingsHelper.setBooleanSetting(collection, discordServerId, "antiswearfeatures.enabled", enabled);
    }

    public boolean getAntiSwearEnabled(String discordServerId) {
        try {
            var filter = Filters.eq("_id", discordServerId);
            Document document = collection.find(filter).first();
            if (document != null) {
                Document settings = (Document) document.get("settings");
                if (settings != null && settings.containsKey("antiswearfeatures")) {
                    Document antiswearFeatures = (Document) settings.get("antiswearfeatures");
                    return antiswearFeatures.getBoolean("enabled");
                }
            }
        } catch (MongoException e) {
        }
        return false;
    }


    public void addAntiSwearWord(String discordServerId, String word) {
        ServerSettingsHelper.updateArraySetting(collection, discordServerId, "antiswearfeatures.words-list", word, true);
    }

    public void removeAntiSwearWord(String discordServerId, String word) {
        ServerSettingsHelper.updateArraySetting(collection, discordServerId, "antiswearfeatures.words-list", word, false);
    }

    @SuppressWarnings("unchecked")
    public List<String> getAntiSwearWordsList(String discordServerId) {
        try {
            var filter = Filters.eq("_id", discordServerId);
            Document document = collection.find(filter).first();
            if (document != null) {
                Document settings = (Document) document.get("settings");
                if (settings != null) {
                    Document antiswearfeatures = (Document) settings.get("antiswearfeatures");
                    if (antiswearfeatures != null) {
                        return (List<String>) antiswearfeatures.get("words-list");
                    }
                }
            }
        } catch (MongoException e) {
            System.err.println("Error retrieving document from MongoDB: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    // Auto Role Feature
    public void setAutoRole(String discordServerId, String roleId) {
        ServerSettingsHelper.setStringSetting(collection, discordServerId, "autoRole", roleId);
    }

    public String getAutoRole(String discordServerId) {
        return ServerSettingsHelper.getStringSetting(collection, discordServerId, "autoRole", null);
    }

    // Voice Action Feature
    public void setVoiceAction(String discordServerId, boolean enabled) {
        ServerSettingsHelper.setBooleanSetting(collection, discordServerId, "voiceaction.enabled", enabled);
    }

    public boolean getVoiceAction(String discordServerId) {
        try {
            var filter = Filters.eq("_id", discordServerId);
            Document document = collection.find(filter).first();
            if (document != null) {
                Document settings = (Document) document.get("settings");
                if (settings != null && settings.containsKey("voiceaction")) {
                    Document voiceAction = (Document) settings.get("voiceaction");
                    return voiceAction.getBoolean("enabled");
                }
            }
        } catch (MongoException e) {
            // Handle MongoException
        }
        return false;
    }

    //Verify System Feature
    public void setVerifySystem(String discordServerId, boolean enabled) {
        ServerSettingsHelper.setBooleanSetting(collection, discordServerId, "verifysystem.enabled", enabled);
    }

    public boolean getVerifySystem(String discordServerId) {
        try {
            var filter = Filters.eq("_id", discordServerId);
            Document document = collection.find(filter).first();
            if (document != null) {
                Document settings = (Document) document.get("settings");
                if (settings != null && settings.containsKey("verifysystem")) {
                    Document verifySystem = (Document) settings.get("verifysystem");
                    return verifySystem.getBoolean("enabled");
                }
            }
        } catch (MongoException e) {
            // Handle MongoException
        }
        return false;
    }
}
