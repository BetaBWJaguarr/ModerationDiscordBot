package beta.com.moderationdiscordbot.managers;

import beta.com.moderationdiscordbot.autopunish.AutoPunishEnableCommands;
import beta.com.moderationdiscordbot.autopunish.antiswear.commands.AntiSwearCommand;
import beta.com.moderationdiscordbot.autopunish.antiswear.commands.subcommands.AddCommand;
import beta.com.moderationdiscordbot.autopunish.antiswear.commands.subcommands.AntiSwearPunishmentTypeCommand;
import beta.com.moderationdiscordbot.autopunish.antiswear.commands.subcommands.ListCommand;
import beta.com.moderationdiscordbot.autopunish.antiswear.commands.subcommands.RemoveCommand;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.BanLog;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.MuteLog;
import beta.com.moderationdiscordbot.databasemanager.LoggingManagement.logs.WarnLog;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.databasemanager.VerifySystem.VerifyMongo;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.PingCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.clearcommands.*;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.*;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.undocommands.ChannelUnBanCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.undocommands.Unban;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.undocommands.Unmute;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.modlogcommands.ModLogCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.punishmentsearchcommands.PunishmentSearchCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.verifycommands.VerifyCommands;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.verifycommands.VerifySetRole;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.verifycommands.VerifyToggleCommands;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.warncommands.SetWarnKickTimesCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.warncommands.UnWarnCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.warncommands.WarnCommand;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.warncommands.WarnListCommand;
import beta.com.moderationdiscordbot.voicemanager.VoiceManager;
import beta.com.moderationdiscordbot.voicemanager.commands.VoiceEnableCommand;
import beta.com.moderationdiscordbot.voicemanager.commands.VoiceRequest;
import beta.com.moderationdiscordbot.voicemanager.commands.VoiceRequestEnd;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandManager {
    private final List<ListenerAdapter> commands = new ArrayList<>();
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final HandleErrors handleErrors;
    private final BanLog banLog;
    private final MuteLog muteLog;
    private final WarnLog warnLog;
    private final VerifyMongo verifyMongo;
    private final VoiceManager voiceManager;

    public CommandManager(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors handleErrors, BanLog banLog, MuteLog muteLog, WarnLog warnLog, VerifyMongo verifyMongo, VoiceManager voiceManager) {
        this.serverSettings = serverSettings;
        this.languageManager = languageManager;
        this.handleErrors = handleErrors;
        this.banLog = banLog;
        this.muteLog = muteLog;
        this.warnLog = warnLog;
        this.verifyMongo = verifyMongo;
        this.voiceManager = voiceManager;
        initializeCommands();
    }

    private void initializeCommands() {
        RateLimit rateLimit = new RateLimit(2, TimeUnit.SECONDS);

        commands.add(new AutoRoleCommand(serverSettings, languageManager, handleErrors, rateLimit));
        commands.add(new PingCommand(serverSettings, languageManager, rateLimit));
        commands.add(new SetLanguageCommand(serverSettings, languageManager, handleErrors, rateLimit));
        commands.add(new BanCommand(serverSettings, languageManager, banLog, handleErrors, rateLimit));
        commands.add(new ChannelBanCommand(serverSettings, languageManager, banLog, handleErrors, rateLimit));
        commands.add(new ChannelUnBanCommand(serverSettings, languageManager, banLog, handleErrors, rateLimit));
        commands.add(new ModLogCommand(serverSettings, languageManager, handleErrors, rateLimit));
        commands.add(new MuteCommand(serverSettings, languageManager, muteLog, handleErrors, rateLimit));
        commands.add(new AntiSwearCommand(serverSettings, languageManager, rateLimit));
        commands.add(new AddCommand(serverSettings, languageManager, rateLimit, handleErrors));
        commands.add(new RemoveCommand(serverSettings, languageManager, rateLimit, handleErrors));
        commands.add(new ListCommand(serverSettings, languageManager, rateLimit, handleErrors));
        commands.add(new AutoPunishEnableCommands(serverSettings, languageManager, rateLimit));
        commands.add(new WarnCommand(serverSettings, languageManager, warnLog, handleErrors, rateLimit));
        commands.add(new WarnListCommand(serverSettings, languageManager, warnLog, handleErrors, rateLimit));
        commands.add(new KickCommand(serverSettings, languageManager, rateLimit, handleErrors));
        commands.add(new UnWarnCommand(serverSettings, languageManager, warnLog, handleErrors, rateLimit));
        commands.add(new Unban(serverSettings, languageManager, banLog, handleErrors, rateLimit));
        commands.add(new Unmute(serverSettings, languageManager, muteLog, handleErrors, rateLimit));
        commands.add(new ClearAllCommand(serverSettings, languageManager, handleErrors, rateLimit));
        commands.add(new ClearFileCommand(serverSettings, languageManager, handleErrors, rateLimit));
        commands.add(new ClearLogChannel(serverSettings, languageManager, handleErrors, rateLimit));
        commands.add(new ClearBotsCommand(serverSettings, languageManager, handleErrors, rateLimit));
        commands.add(new SetWarnKickTimesCommand(serverSettings, languageManager, warnLog, handleErrors, rateLimit));
        commands.add(new ClearEmbedCommand(serverSettings, languageManager, handleErrors, rateLimit));
        commands.add(new ClearContentCommand(serverSettings, languageManager, handleErrors, rateLimit));
        commands.add(new VoiceEnableCommand(serverSettings, languageManager, rateLimit,languageManager));
        commands.add(new VerifyCommands(serverSettings, languageManager, handleErrors, rateLimit,verifyMongo));
        commands.add(new VoiceRequest(voiceManager,languageManager,serverSettings,rateLimit));
        commands.add(new VerifyToggleCommands(serverSettings, languageManager, handleErrors, rateLimit));
        commands.add(new VoiceRequestEnd(voiceManager,languageManager,serverSettings,rateLimit));
        commands.add(new VerifySetRole(serverSettings, languageManager, handleErrors, rateLimit));
        commands.add(new AntiSpamPunishmentTypeCommand(serverSettings, languageManager, rateLimit, handleErrors));
        commands.add(new AntiSwearPunishmentTypeCommand(serverSettings, languageManager, rateLimit, handleErrors));
        commands.add(new PunishmentSearchCommand(serverSettings, languageManager, handleErrors, rateLimit));
    }

    public void addCommandsToJDABuilder(JDABuilder builder) {
        builder.addEventListeners(commands.toArray(new ListenerAdapter[0]));
    }
}