package beta.com.moderationdiscordbot.scheduler;

import java.util.Date;
import java.util.List;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.BanLog;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
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

        for (Document banLog : banLogs) {
            Date banDuration = banLog.getDate("duration");
            String channelId = banLog.getString("channelId");
            if (banDuration != null && new Date().after(banDuration)) {
                String userId = banLog.getString("userId");
                TextChannel textChannel = guild.getTextChannelById(channelId);
                if (textChannel != null) {
                    Member member = guild.getMemberById(userId);
                    if (member != null) {
                        try {
                            textChannel.upsertPermissionOverride(member)
                                    .clear(Permission.MESSAGE_SEND)
                                    .queue(success -> {
                                        this.banLog.removeBanLog(serverId, userId,channelId);
                                    }, error -> {
                                    });
                        } catch (PermissionException e) {
                        }
                    }
                } else {
                    guild.unban(UserSnowflake.fromId(userId)).queue(success -> {
                        this.banLog.removeBanLog(serverId, userId);
                    });
                }
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
