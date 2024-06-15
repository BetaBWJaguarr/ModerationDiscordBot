package beta.com.moderationdiscordbot;

import beta.com.moderationdiscordbot.databasemanager.MongoDB;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.envmanager.Env;
import beta.com.moderationdiscordbot.eventsmanager.RegisterEvents;
import beta.com.moderationdiscordbot.eventsmanager.events.AntiSpamEvent;
import beta.com.moderationdiscordbot.eventsmanager.events.BotJoinServer;
import beta.com.moderationdiscordbot.eventsmanager.events.UserJoinLeaveEvents;
import beta.com.moderationdiscordbot.slashcommandsmanager.RegisterSlashCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.AntiSpamCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.PingCommand;
import beta.com.moderationdiscordbot.startup.Information;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {

    public static void main(String[] args) {
        Env env = new Env(".env");
        String token = env.getProperty("TOKEN");

        Information information = new Information();

        MongoDB db = new MongoDB(env);
        ServerSettings serverSettings = new ServerSettings(db.getCollection());

        //Commands
        AntiSpamCommand antiSpamCommand = new AntiSpamCommand(serverSettings);
        PingCommand pingCommand = new PingCommand();
        //Commands

        try {
            JDA jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setActivity(Activity.watching("Watching something"))
                    .addEventListeners(pingCommand)
                    .addEventListeners(antiSpamCommand)
                    .build();

            new RegisterSlashCommand(jda,information)
                    .register("ping", "A ping command")
                    .register("antispam", "AntiSpam Command",
                            new SubcommandData("messagelimit", "Set the anti-spam message limit")
                                    .addOption(OptionType.INTEGER, "value", "The new message limit", true),
                            new SubcommandData("timelimit", "Set the anti-spam time limit")
                                    .addOption(OptionType.INTEGER, "value", "The new time limit", true),
                            new SubcommandData("enable", "Set the anti-spam enable or false")
                                    .addOption(OptionType.BOOLEAN, "value", "New value", true)
                    );

             new RegisterEvents(jda,information)
                     .register(new UserJoinLeaveEvents())
                     .register(new AntiSpamEvent(antiSpamCommand))
                     .register(new BotJoinServer(serverSettings));

             information.printInformation();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}