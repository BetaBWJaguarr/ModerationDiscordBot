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
    private final MongoCollection<Document> collection;

    public VerifyMongo(MongoDB mongoDB) {
        this.mongoDB = mongoDB;
        this.collection = mongoDB.getCollection("member_verification");
    }

    public void upsertMemberVerifySystem(MemberVerifySystem memberVerify, String guildId) {
        Document filter = new Document("guildId", guildId);
        Document update = new Document("$set", new Document("verified_list." + memberVerify.getId(),
                new Document("id", memberVerify.getId().toString())
                        .append("username", memberVerify.getUsername())
                        .append("level", memberVerify.getLevel())
                        .append("status", memberVerify.getStatus().toString())));

        collection.updateOne(filter, update, new UpdateOptions().upsert(true));
    }

    public void removeMemberVerifySystem(UUID memberId, String guildId) {
        Document filter = new Document("guildId", guildId);
        Document update = new Document("$unset", new Document("verified_list." + memberId.toString(), ""));
        collection.updateOne(filter, update);
    }

    public MemberVerifySystem findMemberVerifySystem(String username, String guildId) {
        Document filter = new Document("guildId", guildId);
        Document guildDocument = collection.find(filter).first();

        if (guildDocument != null) {
            Document verifiedList = (Document) guildDocument.get("verified_list");
            if (verifiedList != null) {
                return verifiedList.values().stream()
                        .filter(doc -> doc instanceof Document)
                        .map(doc -> (Document) doc)
                        .filter(memberDoc -> username.equals(memberDoc.getString("username")))
                        .findFirst()
                        .map(memberDoc -> new MemberVerifySystem(
                                UUID.fromString(memberDoc.getString("id")),
                                memberDoc.getString("username"),
                                memberDoc.getInteger("level"),
                                Status.valueOf(memberDoc.getString("status"))
                        ))
                        .orElse(null);
            }
        }
        return null;
    }
}
