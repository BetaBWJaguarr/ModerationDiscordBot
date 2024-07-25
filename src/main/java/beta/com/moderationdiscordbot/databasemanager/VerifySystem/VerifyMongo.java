package beta.com.moderationdiscordbot.databasemanager.VerifySystem;

import beta.com.moderationdiscordbot.databasemanager.MongoDB;
import beta.com.moderationdiscordbot.memberverifysystem.MemberVerifySystem;
import beta.com.moderationdiscordbot.memberverifysystem.Status;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VerifyMongo {

    private final MongoDB mongoDB;

    public VerifyMongo(MongoDB mongoDB) {
        this.mongoDB = mongoDB;
    }

    public void upsertMemberVerifySystem(MemberVerifySystem memberVerify, String guildId) {
        MongoCollection<Document> collection = mongoDB.getCollection("member_verification");

        Document filter = new Document("guildId", guildId);
        Document update = new Document("$set", new Document("verified_list." + memberVerify.getId().toString(),
                new Document("id", memberVerify.getId().toString())
                        .append("username", memberVerify.getUsername())
                        .append("level", memberVerify.getLevel())
                        .append("status", memberVerify.getStatus().toString())));

        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    public MemberVerifySystem findMemberVerifySystem(String username, String guildId) {
        MongoCollection<Document> collection = mongoDB.getCollection("member_verification");

        Document filter = new Document("guildId", guildId);
        Document guildDocument = collection.find(filter).first();

        if (guildDocument != null) {
            Document verifiedList = (Document) guildDocument.get("verified_list");
            if (verifiedList != null) {
                for (String key : verifiedList.keySet()) {
                    Document memberDoc = (Document) verifiedList.get(key);
                    if (username.equals(memberDoc.getString("username"))) {
                        return new MemberVerifySystem(
                                UUID.fromString(memberDoc.getString("id")),
                                memberDoc.getString("username"),
                                memberDoc.getInteger("level"),
                                Status.valueOf(memberDoc.getString("status"))
                        );
                    }
                }
            }
        }
        return null;
    }
}
