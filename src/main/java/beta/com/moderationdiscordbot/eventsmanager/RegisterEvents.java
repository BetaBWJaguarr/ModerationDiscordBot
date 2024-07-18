package beta.com.moderationdiscordbot.eventsmanager;

import beta.com.moderationdiscordbot.advertisemanager.AdvertiseChecking;
import beta.com.moderationdiscordbot.autopunish.antiswear.AntiSwear;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.eventsmanager.events.*;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.AntiSpamCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.AntiVirusCommand;
import beta.com.moderationdiscordbot.startup.Information;
import beta.com.moderationdiscordbot.voicemanager.VoiceManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RegisterEvents extends ListenerAdapter {
    private final JDA jda;
    private final Information information;
    private final LanguageManager languageManager;
    private final ServerSettings serverSettings;
    private final AntiSpamCommand antiSpamCommand;
    private final AntiVirusCommand antiVirusCommand;
    private final AntiSwear antiSwear;

    public RegisterEvents(JDA jda, Information information, LanguageManager languageManager, ServerSettings serverSettings,
                          AntiSpamCommand antiSpamCommand, AntiVirusCommand antiVirusCommand, AntiSwear antiSwear) {
        this.jda = jda;
        this.information = information;
        this.languageManager = languageManager;
        this.serverSettings = serverSettings;
        this.antiSpamCommand = antiSpamCommand;
        this.antiVirusCommand = antiVirusCommand;
        this.antiSwear = antiSwear;
    }

    public void registerAll() {
        jda.addEventListener(new UserJoinLeaveEvents(languageManager, serverSettings));
        information.incrementEvents();

        jda.addEventListener(new AntiSpamEvent(antiSpamCommand, languageManager, serverSettings));
        information.incrementEvents();

        jda.addEventListener(new BotJoinServer(serverSettings));
        information.incrementEvents();

        jda.addEventListener(new AdvertiseChecking(languageManager, serverSettings));
        information.incrementEvents();

        jda.addEventListener(new AntiVirusEvent(antiVirusCommand, languageManager, serverSettings));
        information.incrementEvents();

        jda.addEventListener(new AutoPunishEvent(antiSwear));
        information.incrementEvents();

        jda.addEventListener(new AutoRoleEvent(serverSettings, languageManager));
        information.incrementEvents();

        jda.addEventListener(new VoiceManager(serverSettings, languageManager,antiSwear));
        information.incrementEvents();
    }
}