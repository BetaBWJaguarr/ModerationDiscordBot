package beta.com.moderationdiscordbot.voicemanager;

import beta.com.moderationdiscordbot.autopunish.antiswear.AntiSwear;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.voicemanager.auidomanager.AudioReceiver;
import beta.com.moderationdiscordbot.voicemanager.auidomanager.SpeechToTextAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class VoiceSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceSession.class);

    private final AudioManager audioManager;
    private final String userDir;
    private final AntiSwear antiSwear;
    private final Member member;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final AudioReceiver audioReceiver;
    private final EmbedBuilderManager embedBuilderManager;
    private static final String VOSK_MODEL_PATH = "C:\\Users\\tuna\\Downloads\\vosk-model-en-us-0.22";

    public VoiceSession(AudioManager audioManager, String userDir, AntiSwear antiSwear, Member member, ServerSettings serverSettings, LanguageManager languageManager, AudioReceiver audioReceiver) {
        this.audioManager = audioManager;
        this.userDir = userDir;
        this.antiSwear = antiSwear;
        this.member = member;
        this.serverSettings = serverSettings;
        this.languageManager = languageManager;
        this.audioReceiver = audioReceiver;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
    }

    public void startRecording() {
        LOGGER.info("Recording started for user: {}", member.getEffectiveName());
    }

    public void stopRecordingAndAnalyze() {
        CompletableFuture.runAsync(() -> {
            audioReceiver.saveAudioToFile(userDir, member.getUser());
            File audioFile = new File(userDir + "/output.wav");

            if (audioFile.exists()) {
                try {
                    String text = SpeechToTextAPI.convertSpeechToText(audioFile.getAbsolutePath(), VOSK_MODEL_PATH);
                    if (antiSwear.containsProfanity(text, member.getGuild().getId())) {
                        LOGGER.warn("Profanity detected for user: {}", member.getEffectiveName());
                        sendWarningDM();
                    }
                } catch (IOException | UnsupportedAudioFileException e) {
                    LOGGER.error("Error during speech-to-text conversion or profanity detection", e);
                } finally {
                    if (!audioFile.delete()) {
                        LOGGER.error("Failed to delete audio file: {}", audioFile.getAbsolutePath());
                    }
                }
            } else {
                LOGGER.info("Audio file not found for user: {}", member.getEffectiveName());
            }

            audioReceiver.closeAudioStreams(member.getUser());
        });
    }

    private void sendWarningDM() {
        String language = serverSettings.getLanguage(member.getGuild().getId());
        EmbedBuilder embed = embedBuilderManager.createEmbedWithColor(
                "events.voice.warning.title",
                "events.voice.warning.description",
                language,
                Color.RED
        );
        member.getUser().openPrivateChannel().queue(channel ->
                channel.sendMessageEmbeds(embed.build()).queue()
        );
    }
}
