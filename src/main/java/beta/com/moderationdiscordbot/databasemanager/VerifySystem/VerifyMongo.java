package beta.com.moderationdiscordbot.databasemanager.VerifySystem;


import beta.com.moderationdiscordbot.databasemanager.MongoDB;
import beta.com.moderationdiscordbot.memberverifysystem.MemberVerifySystem;
import beta.com.moderationdiscordbot.memberverifysystem.Status;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.UUID;

public class VerifyMongo {

    private final MongoDB mongoDB;

    public VerifyMongo(MongoDB mongoDB) {
        this.mongoDB = mongoDB;
    }

    public void upsertMemberVerifySystem(MemberVerifySystem memberVerify) {
        MongoCollection<Document> collection = mongoDB.getCollection("member_verification");
        Document filter = new Document("id", memberVerify.getId().toString());
        Document update = new Document("$set", new Document("username", memberVerify.getUsername())
                .append("level", memberVerify.getLevel())
                .append("status", memberVerify.getStatus().toString()));
        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    public MemberVerifySystem findMemberVerifySystem(UUID id) {
        MongoCollection<Document> collection = mongoDB.getCollection("member_verification");
        Document filter = new Document("id", id.toString());
        Document result = collection.find(filter).first();
        if (result != null) {
            return new MemberVerifySystem(UUID.fromString(result.getString("id")),
                    result.getString("username"),
                    result.getInteger("level"),
                    Status.valueOf(result.getString("status")));
        }
        return null;
    }

}
