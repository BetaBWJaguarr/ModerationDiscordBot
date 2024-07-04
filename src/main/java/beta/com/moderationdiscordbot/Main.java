package beta.com.moderationdiscordbot;

import beta.com.moderationdiscordbot.advertisemanager.AdvertiseChecking;
import beta.com.moderationdiscordbot.autopunish.antiswear.AntiSwear;
import beta.com.moderationdiscordbot.managers.CommandManager;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.BanLog;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.MuteLog;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.WarnLog;
import beta.com.moderationdiscordbot.databasemanager.MongoDB;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.envmanager.Env;
import beta.com.moderationdiscordbot.eventsmanager.RegisterEvents;
import beta.com.moderationdiscordbot.eventsmanager.events.*;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.managers.SchedulerManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.slashcommandsmanager.RegisterSlashCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.*;
import beta.com.moderationdiscordbot.startup.Information;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        Env env = new Env(".env");
        String token = env.getProperty("TOKEN");

        LanguageManager languageManager = new LanguageManager();

        MongoDB db = new MongoDB(env);
        ServerSettings serverSettings = new ServerSettings(db.getCollection("ServerSettings"));
        BanLog banLog = new BanLog(db.getCollection("BanLog"));
        MuteLog muteLog = new MuteLog(db.getCollection("MuteLog"));
        WarnLog warnLog = new WarnLog(db.getCollection("WarnLog"));



        //HandleExpections
        HandleErrors handleErrors = new HandleErrors(languageManager,serverSettings);
        //HandleExpections

        //Commands
        RateLimit rateLimit = new RateLimit(2, TimeUnit.SECONDS);
        AntiSpamCommand antiSpamCommand = new AntiSpamCommand(serverSettings, languageManager, rateLimit,handleErrors);
        AntiVirusCommand antiVirusCommand = new AntiVirusCommand(serverSettings, languageManager, rateLimit,handleErrors);
        CommandManager commandManager = new CommandManager(serverSettings, languageManager, handleErrors, banLog, muteLog, warnLog);
        //Commands

        try {
            JDABuilder jdaBuilder = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setActivity(Activity.watching("Watching something"))
                            .addEventListeners(antiSpamCommand)
                            .addEventListeners(antiVirusCommand);

                    commandManager.addCommandsToJDABuilder(jdaBuilder);

            JDA jda = jdaBuilder.build();


            final var botInfo = getInformation(jda);


            //Scheduler;
            SchedulerManager schedulerManager = new SchedulerManager(banLog,muteLog,jda);
            schedulerManager.startSchedulers();
            //Scheduler;

            new RegisterSlashCommand(jda, botInfo).registerCommands();


            AntiSwear antiSwear = new AntiSwear(serverSettings,languageManager);

             new RegisterEvents(jda,botInfo)
                     .register(new UserJoinLeaveEvents(languageManager,serverSettings))
                     .register(new AntiSpamEvent(antiSpamCommand,languageManager,serverSettings))
                     .register(new BotJoinServer(serverSettings))
                     .register(new AdvertiseChecking(languageManager,serverSettings))
                     .register(new AntiVirusEvent(antiVirusCommand,languageManager,serverSettings))
                     .register(new AutoPunishEvent(antiSwear))
                     .register(new AutoRoleEvent(serverSettings,languageManager));

            botInfo.printInformation();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Information getInformation(JDA jda) {
        Information botInfo = new Information("ModerationBot", "1.0", "Beta_BWJaguarr", jda.getSelfUser().getId());
        int serverCount = jda.getGuilds().size();
        int userCount = jda.getGuilds().stream().mapToInt(guild -> guild.getMembers().size()).sum();
        botInfo.setServerCount(serverCount);
        botInfo.setUserCount(userCount);
        botInfo.setCommandList(Arrays.asList("ping", "mute", "setlanguage", "antispam", "ban", "modlog", "antivirus", "unban", "unmute","clear","warn","unwarn","kick","warnlist","antiswear","autopunish","channel","setwarnkick","autorole"));
        botInfo.setEventList(Arrays.asList("UserJoinLeaveEvents", "AntiSpamEvent", "BotJoinServer", "AdvertiseChecking", "AntiVirusEvent","HighWarnKickEvent","AutoPunishEvent"));
        return botInfo;
    }
}