package beta.com.moderationdiscordbot.databasemanager;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import beta.com.moderationdiscordbot.envmanager.Env;

public class MongoDB {
    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoDB(Env env) {
        try {
            String connectionString = env.getProperty("MONGODB_CONNECTION_STRING");
            String databaseName = env.getProperty("MONGODB_DATABASE_NAME");

            mongoClient = MongoClients.create(MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .build());

            database = mongoClient.getDatabase(databaseName);
        } catch (MongoException e) {
            System.err.println("Error connecting to MongoDB: " + e.getMessage());
        }
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    public void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (MongoException e) {
                System.err.println("Error closing MongoDB connection: " + e.getMessage());
            }
        }
    }
}