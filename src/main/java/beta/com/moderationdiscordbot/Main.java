package beta.com.moderationdiscordbot;

import beta.com.moderationdiscordbot.advertisemanager.AdvertiseChecking;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.BanLog;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.MuteLog;
import beta.com.moderationdiscordbot.databasemanager.MongoDB;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.envmanager.Env;
import beta.com.moderationdiscordbot.eventsmanager.RegisterEvents;
import beta.com.moderationdiscordbot.eventsmanager.events.AntiSpamEvent;
import beta.com.moderationdiscordbot.eventsmanager.events.AntiVirusEvent;
import beta.com.moderationdiscordbot.eventsmanager.events.BotJoinServer;
import beta.com.moderationdiscordbot.eventsmanager.events.UserJoinLeaveEvents;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.scheduler.UnbanScheduler;
import beta.com.moderationdiscordbot.scheduler.UnmuteScheduler;
import beta.com.moderationdiscordbot.slashcommandsmanager.RegisterSlashCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.*;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.PingCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.undocommands.Unban;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.undocommands.Unmute;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.modlogcommands.ModLogCommand;
import beta.com.moderationdiscordbot.startup.Information;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.swing.text.html.Option;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        Env env = new Env(".env");
        String token = env.getProperty("TOKEN");

        Information information = new Information();
        LanguageManager languageManager = new LanguageManager();

        MongoDB db = new MongoDB(env);
        ServerSettings serverSettings = new ServerSettings(db.getCollection("ServerSettings"));
        BanLog banLog = new BanLog(db.getCollection("BanLog"));
        MuteLog muteLog = new MuteLog(db.getCollection("MuteLog"));



        //Commands
        AntiSpamCommand antiSpamCommand = new AntiSpamCommand(serverSettings,languageManager);
        PingCommand pingCommand = new PingCommand(serverSettings,languageManager);
        SetLanguageCommand setLanguageCommand = new SetLanguageCommand(serverSettings,languageManager);
        BanCommand banCommand = new BanCommand(serverSettings,languageManager,banLog);
        ModLogCommand modLogCommand = new ModLogCommand(serverSettings,languageManager);
        MuteCommand muteCommand = new MuteCommand(serverSettings,languageManager,muteLog);
        AntiVirusCommand antiVirusCommand = new AntiVirusCommand(serverSettings,languageManager);

        Unban unbanCommand = new Unban(serverSettings,languageManager,banLog);
        Unmute unmuteCommand = new Unmute(serverSettings,languageManager,muteLog);
        //Commands

        try {
            JDA jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .setActivity(Activity.watching("Watching something"))
                    .addEventListeners(pingCommand)
                    .addEventListeners(antiSpamCommand)
                    .addEventListeners(setLanguageCommand)
                    .addEventListeners(banCommand)
                    .addEventListeners(modLogCommand)
                    .addEventListeners(muteCommand)
                    .addEventListeners(antiVirusCommand)
                    .addEventListeners(unbanCommand)
                    .addEventListeners(unmuteCommand)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .build();

            UnbanScheduler unbanScheduler = new UnbanScheduler(banLog, jda);
            UnmuteScheduler unmuteScheduler = new UnmuteScheduler(muteLog,jda);

            //Scheduler
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                unbanScheduler.checkAndUnbanUsersInAllGuilds();
            }, 0, 3, TimeUnit.SECONDS);

            ScheduledExecutorService unmutescheduler = Executors.newScheduledThreadPool(1);
            unmutescheduler.scheduleAtFixedRate(() -> {
                unmuteScheduler.checkAndUnmuteUsersInAllGuilds();
            }, 0, 3, TimeUnit.SECONDS);

            //Scheduler;

            new RegisterSlashCommand(jda, information)
                    .register("ping", "A ping command")
                    .register("mute", "Mute a user from the server",
                            new OptionData(OptionType.STRING, "username", "The username (mentionable) of the user to mute", true),
                            new OptionData(OptionType.STRING, "duration", "The mute duration (e.g., 7d, 12h)", false),
                            new OptionData(OptionType.STRING, "reason", "The reason for muting", false))
                    .register("setlanguage", "Set the language of the bot",
                            new OptionData(OptionType.STRING, "language", "The language to set", true))
                    .register("antispam", "AntiSpam Command",
                            new SubcommandData("messagelimit", "Set the anti-spam message limit")
                                    .addOption(OptionType.INTEGER, "value", "The new message limit", true),
                            new SubcommandData("timelimit", "Set the anti-spam time limit")
                                    .addOption(OptionType.INTEGER, "value", "The new time limit", true),
                            new SubcommandData("enable", "Set the anti-spam enable or false")
                    )
                    .register("ban", "Ban a user from the server",
                            new OptionData(OptionType.STRING, "username", "The username (mentionable) of the user to ban", true),
                            new OptionData(OptionType.STRING, "duration", "The ban duration (e.g., 7d, 12h)", false),
                            new OptionData(OptionType.STRING, "reason", "The reason for banning", false),
                            new OptionData(OptionType.INTEGER, "delete_history_message_duration", "The duration of deleting the message history", false)
                    )
                    .register("modlog", "Set the modlog channel",
                            new OptionData(OptionType.CHANNEL, "channel", "The channel to set as the modlog channel", true)
                    )
                    .register("antivirus", "AntiVirus Command"
                    )
                    .register("unban", "Unban a user from the server",
                            new OptionData(OptionType.STRING, "username", "The username (mentionable) of the user to unban", true),
                            new OptionData(OptionType.STRING, "reason", "the reason for unbanning", false)
                    )
                    .register("unmute", "Unmute a user from the server",
                            new OptionData(OptionType.STRING, "username", "The username (mentionable) of the user to unmute", true),
                            new OptionData(OptionType.STRING, "reason", "The reason for unmuting", false)
                    );


             new RegisterEvents(jda,information)
                     .register(new UserJoinLeaveEvents(languageManager,serverSettings))
                     .register(new AntiSpamEvent(antiSpamCommand,languageManager,serverSettings))
                     .register(new BotJoinServer(serverSettings))
                     .register(new AdvertiseChecking(languageManager,serverSettings))
                     .register(new AntiVirusEvent(antiVirusCommand,languageManager,serverSettings));

             information.printInformation();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}