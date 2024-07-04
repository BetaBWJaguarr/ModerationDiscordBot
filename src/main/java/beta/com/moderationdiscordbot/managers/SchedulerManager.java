package beta.com.moderationdiscordbot.managers;

import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.BanLog;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.MuteLog;
import beta.com.moderationdiscordbot.scheduler.UnbanScheduler;
import beta.com.moderationdiscordbot.scheduler.UnmuteScheduler;
import net.dv8tion.jda.api.JDA;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerManager {

    private final UnbanScheduler unbanScheduler;
    private final UnmuteScheduler unmuteScheduler;
    private final BanLog banLog;
    private final MuteLog muteLog;
    private final JDA jda;

    public SchedulerManager(BanLog banLog, MuteLog muteLog,JDA jda) {
        this.banLog = banLog;
        this.muteLog = muteLog;
        this.jda = jda;
        this.unbanScheduler = new UnbanScheduler(banLog,jda);
        this.unmuteScheduler = new UnmuteScheduler(muteLog,jda);
    }

    public void startSchedulers() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            unbanScheduler.checkAndUnbanUsersInAllGuilds();
        }, 0, 3, TimeUnit.SECONDS);

        ScheduledExecutorService unmutescheduler = Executors.newScheduledThreadPool(1);
        unmutescheduler.scheduleAtFixedRate(() -> {
            unmuteScheduler.checkAndUnmuteUsersInAllGuilds();
        }, 0, 3, TimeUnit.SECONDS);
    }

}
