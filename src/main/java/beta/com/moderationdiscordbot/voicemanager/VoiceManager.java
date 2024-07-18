package beta.com.moderationdiscordbot.voicemanager;

import beta.com.moderationdiscordbot.autopunish.antiswear.AntiSwear;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class VoiceManager extends ListenerAdapter {
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final AntiSwear antiSwear;
    private final Map<VoiceChannel, Map<Member, VoiceSession>> activeSessions = new HashMap<>();

    public VoiceManager(ServerSettings settings, LanguageManager languageManager, AntiSwear antiSwear) {
        this.serverSettings = settings;
        this.languageManager = languageManager;
        this.antiSwear = antiSwear;
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        String guildId = event.getGuild().getId();


        if (!serverSettings.getVoiceAction(guildId)) {
            return;
        }

        if (event.getChannelJoined() instanceof VoiceChannel) {
            handleVoiceJoin(event.getGuild(), (VoiceChannel) event.getChannelJoined(), event.getMember());
        } else if (event.getChannelLeft() instanceof VoiceChannel) {
            handleVoiceLeave(event.getGuild(), (VoiceChannel) event.getChannelLeft(), event.getMember());
        }
    }

    private void handleVoiceJoin(Guild guild, VoiceChannel channel, Member member) {
        String serverDir = "recordings/" + guild.getName();
        String channelDir = serverDir + "/" + channel.getName();
        String userDir = channelDir + "/" + member.getEffectiveName();

        new File(userDir).mkdirs();

        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(channel);


        activeSessions.putIfAbsent(channel, new HashMap<>());

        VoiceSession session = new VoiceSession(audioManager, userDir, antiSwear, member, serverSettings, languageManager);
        activeSessions.get(channel).put(member, session);
        session.startRecording();
    }

    private void handleVoiceLeave(Guild guild, VoiceChannel channel, Member member) {
        Map<Member, VoiceSession> channelSessions = activeSessions.get(channel);
        if (channelSessions != null) {
            VoiceSession session = channelSessions.remove(member);
            if (session != null) {
                session.stopRecordingAndAnalyze();
            }

            if (channel.getMembers().size() == 1 && channel.getMembers().get(0).getUser().isBot()) {
                guild.getAudioManager().closeAudioConnection();
                activeSessions.remove(channel);
            }
        }
    }
}
