package beta.com.moderationdiscordbot.voicemanager;

import beta.com.moderationdiscordbot.autopunish.antiswear.AntiSwear;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.voicemanager.auidomanager.AudioReceiver;
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
    private final AudioReceiver audioReceiver;

    public VoiceManager(ServerSettings settings, LanguageManager languageManager, AntiSwear antiSwear) {
        this.serverSettings = settings;
        this.languageManager = languageManager;
        this.antiSwear = antiSwear;
        this.audioReceiver = new AudioReceiver();
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
        try {
            String userDir = createUserDirectory(guild, channel, member);
            AudioManager audioManager = guild.getAudioManager();
            audioManager.openAudioConnection(channel);

            if (audioManager.getReceivingHandler() == null) {
                audioManager.setReceivingHandler(audioReceiver);
            }

            activeSessions.computeIfAbsent(channel, k -> new HashMap<>());

            VoiceSession session = new VoiceSession(audioManager, userDir, antiSwear, member, serverSettings, languageManager, audioReceiver);
            activeSessions.get(channel).put(member, session);
            session.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleVoiceLeave(Guild guild, VoiceChannel channel, Member member) {
        Map<Member, VoiceSession> channelSessions = activeSessions.get(channel);
        if (channelSessions != null) {
            VoiceSession session = channelSessions.remove(member);
            if (session != null) {
                session.stopRecordingAndAnalyze();
            }

            if (shouldCloseConnection(channel)) {
                guild.getAudioManager().closeAudioConnection();
                activeSessions.remove(channel);
            }
        }
    }

    private String createUserDirectory(Guild guild, VoiceChannel channel, Member member) {
        String serverDir = "recordings/" + guild.getName();
        String channelDir = serverDir + "/" + channel.getName();
        String userDir = channelDir + "/" + member.getEffectiveName();

        File dir = new File(userDir);
        if (!dir.exists()) {
            boolean dirsCreated = dir.mkdirs();
            if (!dirsCreated) {
                System.err.println("Failed to create directories: " + userDir);
            }
        }
        return userDir;
    }

    private boolean shouldCloseConnection(VoiceChannel channel) {
        return channel.getMembers().isEmpty() ||
                (channel.getMembers().size() == 1 && channel.getMembers().get(0).getUser().isBot());
    }
}
