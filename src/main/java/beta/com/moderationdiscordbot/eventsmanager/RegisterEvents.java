package beta.com.moderationdiscordbot.eventsmanager;

import beta.com.moderationdiscordbot.advertisemanager.AdvertiseChecking;
import beta.com.moderationdiscordbot.autopunish.antispam.AntiSpamEvent;
import beta.com.moderationdiscordbot.autopunish.antiswear.AntiSwear;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.databasemanager.VerifySystem.VerifyMongo;
import beta.com.moderationdiscordbot.eventsmanager.events.*;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.autopunish.antispam.commands.AntiSpamCommand;
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
    private final VerifyMongo verifyMongo;
    private final VoiceManager voiceManager;

    public RegisterEvents(JDA jda, Information info, LanguageManager langManager, ServerSettings serverSettings,
                          AntiSpamCommand spamCommand, AntiVirusCommand virusCommand, AntiSwear swear, VerifyMongo verifyMongo, VoiceManager voiceManager) {
        this.jda = jda;
        this.information = info;
        this.languageManager = langManager;
        this.serverSettings = serverSettings;
        this.antiSpamCommand = spamCommand;
        this.antiVirusCommand = virusCommand;
        this.antiSwear = swear;
        this.verifyMongo = verifyMongo;
        this.voiceManager = voiceManager;
    }

    public void registerAll() {
        addEvent(new UserJoinLeaveEvents(languageManager, serverSettings,verifyMongo));
        addEvent(new AntiSpamEvent(antiSpamCommand, languageManager, serverSettings));
        addEvent(new BotJoinServer(serverSettings));
        addEvent(new AdvertiseChecking(languageManager, serverSettings));
        addEvent(new AntiVirusEvent(antiVirusCommand, languageManager, serverSettings));
        addEvent(new AutoPunishEvent(antiSwear,languageManager));
        addEvent(new AutoRoleEvent(serverSettings, languageManager));
        addEvent(voiceManager);
    }

    private void addEvent(ListenerAdapter event) {
        jda.addEventListener(event);
        information.incrementEvents();
    }
}
