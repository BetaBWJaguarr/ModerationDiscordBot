package beta.com.moderationdiscordbot;

import beta.com.moderationdiscordbot.envmanager.Env;
import beta.com.moderationdiscordbot.eventsmanager.RegisterEvents;
import beta.com.moderationdiscordbot.eventsmanager.events.UserJoinLeaveEvents;
import beta.com.moderationdiscordbot.slashcommandsmanager.RegisterSlashCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.PingCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
    public static void main(String[] args) {
        Env env = new Env(".env");
        String token = env.getProperty("TOKEN");

        try {
            JDA jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS) // GUILD_MEMBERS niyetini etkinleştir
                    .setActivity(Activity.watching("Watching something"))
                    .addEventListeners(new PingCommand())
                    .build();

            RegisterSlashCommand registerSlashCommand = new RegisterSlashCommand(jda);
            registerSlashCommand.register("ping", "A ping command");

            RegisterEvents registerEvents = new RegisterEvents(jda);
            registerEvents.register(new UserJoinLeaveEvents());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}