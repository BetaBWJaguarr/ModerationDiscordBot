package beta.com.moderationdiscordbot;

import beta.com.moderationdiscordbot.autopunish.antispam.commands.AntiSpamCommand;
import beta.com.moderationdiscordbot.autopunish.antiswear.AntiSwear;
import beta.com.moderationdiscordbot.databasemanager.VerifySystem.VerifyMongo;
import beta.com.moderationdiscordbot.managers.CommandManager;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.BanLog;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.MuteLog;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.WarnLog;
import beta.com.moderationdiscordbot.databasemanager.MongoDB;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.envmanager.Env;
import beta.com.moderationdiscordbot.eventsmanager.RegisterEvents;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.managers.SchedulerManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.slashcommandsmanager.RegisterSlashCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.*;
import beta.com.moderationdiscordbot.startup.Information;
import beta.com.moderationdiscordbot.voicemanager.VoiceManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws IOException {
        DebugManager.logDebug("Loading environment variables...");
        Env env = new Env(".env");
        String token = env.getProperty("TOKEN");

        DebugManager.logDebug("Initializing LanguageManager...");
        LanguageManager languageManager = new LanguageManager();

        DebugManager.logDebug("Connecting to MongoDB...");
        MongoDB db = new MongoDB(env);
        VerifyMongo verifyMongo = new VerifyMongo(db);
        ServerSettings serverSettings = new ServerSettings(db.getCollection("ServerSettings"));
        BanLog banLog = new BanLog(db.getCollection("BanLog"));
        MuteLog muteLog = new MuteLog(db.getCollection("MuteLog"));
        WarnLog warnLog = new WarnLog(db.getCollection("WarnLog"));

        DebugManager.logDebug("Setting up error handling...");
        HandleErrors handleErrors = new HandleErrors(languageManager, serverSettings);

        DebugManager.logDebug("Setting up command rate limits and utils...");
        RateLimit rateLimit = new RateLimit(2, TimeUnit.SECONDS);
        AntiSpamCommand antiSpamCommand = new AntiSpamCommand(serverSettings, languageManager, rateLimit, handleErrors);
        AntiVirusCommand antiVirusCommand = new AntiVirusCommand(serverSettings, languageManager, rateLimit, handleErrors);
        AntiSwear antiSwear = new AntiSwear(serverSettings, languageManager,muteLog);
        VoiceManager voiceManager = new VoiceManager(serverSettings, languageManager, antiSwear);
        CommandManager commandManager = new CommandManager(serverSettings, languageManager, handleErrors, banLog, muteLog, warnLog,verifyMongo,voiceManager);

        try {
            DebugManager.logDebug("Building JDA...");
            JDABuilder jdaBuilder = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setActivity(Activity.watching("Watching something"))
                    .addEventListeners(antiSpamCommand)
                    .addEventListeners(antiVirusCommand);

            commandManager.addCommandsToJDABuilder(jdaBuilder);

            DebugManager.logDebug("Logging into Discord...");
            JDA jda = jdaBuilder.build();

            final var botInfo = getInformation(jda);

            DebugManager.logDebug("Starting scheduler...");
            SchedulerManager schedulerManager = new SchedulerManager(banLog, muteLog, jda);
            schedulerManager.startSchedulers();

            DebugManager.logDebug("Registering slash commands...");
            new RegisterSlashCommand(jda, botInfo).registerCommands();

            DebugManager.logDebug("Initializing AntiSwear...");

            DebugManager.logDebug("Registering events...");
            new RegisterEvents(jda, botInfo, languageManager, serverSettings, antiSpamCommand, antiVirusCommand, antiSwear,verifyMongo,voiceManager,muteLog).registerAll();

            DebugManager.logDebug("Printing bot information...");
            botInfo.printInformation();

            DebugManager.logDebug("Bot started successfully!");
            DebugManager.logDebug("Bot name: " + Information.getBotName());
            DebugManager.logDebug("Bot version: " + Information.getBotVersion());
            DebugManager.logDebug("Developer: " + Information.getDeveloperName());
            DebugManager.logDebug("Bot ID: " + Information.getBotId());
            DebugManager.logDebug("Server count: " + Information.getServerCount());
            DebugManager.logDebug("User count: " + Information.getUserCount());
            DebugManager.logDebug("Command list: " + Information.getCommandList());
            DebugManager.logDebug("Event list: " + Information.getEventList());

        } catch (Exception e) {
            DebugManager.logError("An error occurred during bot startup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Information getInformation(JDA jda) {
        DebugManager.logDebug("Gathering bot information...");
        Information botInfo = new Information("ModerationBot", "1.0", "Beta_BWJaguarr", jda.getSelfUser().getId());
        int serverCount = jda.getGuilds().size();
        int userCount = jda.getGuilds().stream().mapToInt(guild -> guild.getMembers().size()).sum();
        botInfo.setServerCount(serverCount);
        botInfo.setUserCount(userCount);
        botInfo.setCommandList(Arrays.asList("ping", "mute", "setlanguage", "antispam", "ban", "modlog", "antivirus", "unban", "unmute", "clear", "warn", "unwarn", "kick", "warnlist", "antiswear", "autopunish", "channel", "setwarnkick", "autorole", "voiceaction","verify","punishmentsearch"));
        botInfo.setEventList(Arrays.asList("UserJoinLeaveEvents", "AntiSpamEvent", "BotJoinServer", "AdvertiseChecking", "AntiVirusEvent", "HighWarnKickEvent", "AutoPunishEvent", "VoiceManager"));
        DebugManager.logDebug("Bot information gathered.");
        return botInfo;
    }
}
