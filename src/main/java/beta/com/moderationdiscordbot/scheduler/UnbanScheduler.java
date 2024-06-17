package beta.com.moderationdiscordbot.scheduler;

import java.util.Date;
import java.util.List;

import beta.com.moderationdiscordbot.databasemanager.Logging.BanLog;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.bson.Document;

public class UnbanScheduler {
    private final BanLog banLog;
    private final JDA jda;

    public UnbanScheduler(BanLog banLog, JDA jda) {
        this.banLog = banLog;
        this.jda = jda;
    }

    public void checkAndUnbanUsers(String serverId) {
        List<Document> banLogs = banLog.getBanLogs(serverId);
        if (banLogs == null || banLogs.isEmpty()) {
            return;
        }

        Guild guild = jda.getGuildById(serverId);
        if (guild == null) {
            return;
        }

        for (Document user : banLogs) {
            Document ban = (Document) user.get("ban");
            Date banDuration = ban.getDate("duration");
            if (banDuration != null && new Date().after(banDuration)) {
                String userId = user.getString("userId");
                guild.unban(UserSnowflake.fromId(userId)).queue();
                banLog.removeBanLog(serverId, userId);
            }
        }
    }


    public void checkAndUnbanUsersInAllGuilds() {
        List<Guild> guilds = jda.getGuilds();
        for (Guild guild : guilds) {
            String serverId = guild.getId();
            checkAndUnbanUsers(serverId);
        }
    }
}
