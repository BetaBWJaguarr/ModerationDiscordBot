package beta.com.moderationdiscordbot;

import beta.com.moderationdiscordbot.envmanager.Env;
import beta.com.moderationdiscordbot.eventsmanager.RegisterEvents;
import beta.com.moderationdiscordbot.eventsmanager.events.AntiSpamEvent;
import beta.com.moderationdiscordbot.eventsmanager.events.UserJoinLeaveEvents;
import beta.com.moderationdiscordbot.slashcommandsmanager.RegisterSlashCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.AntiSpamCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.PingCommand;
import beta.com.moderationdiscordbot.startup.Information;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {

    public static void main(String[] args) {
        Env env = new Env(".env");
        String token = env.getProperty("TOKEN");

        Information information = new Information();

        //Commands
        AntiSpamCommand antiSpamCommand = new AntiSpamCommand();
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
                    .register("antispam", "Enable or disable anti-spam protection");

             new RegisterEvents(jda,information)
                     .register(new UserJoinLeaveEvents())
                     .register(new AntiSpamEvent(antiSpamCommand));

             information.printInformation();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}