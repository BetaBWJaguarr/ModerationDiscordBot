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
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class VoiceSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceSession.class);

    private final AudioReceiver audioReceiver;
    private final Member member;
    private final AntiSwear antiSwear;
    private final EmbedBuilderManager embedBuilderManager;
    private final String userDir;
    private final String voskModelPath = "C:\\Users\\tuna\\Downloads\\vosk-model-en-us-0.22";

    public VoiceSession(String userDir, AntiSwear antiSwear, Member member,
                         LanguageManager languageManager, AudioReceiver audioReceiver) {
        this.audioReceiver = audioReceiver;
        this.member = member;
        this.antiSwear = antiSwear;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.userDir = userDir;
    }

    public void startRecording() {
        LOGGER.info("Recording started for user: {}", member.getEffectiveName());
    }

    public void stopRecordingAndAnalyze() {
        CompletableFuture.runAsync(() -> {
            File audioFile = new File(Paths.get(userDir, "output.wav").toString());

            try {
                audioReceiver.saveAudioToFile(userDir, member.getUser());

                if (audioFile.exists()) {
                    String text = SpeechToTextAPI.convertSpeechToText(audioFile.getAbsolutePath(), voskModelPath);
                    if (antiSwear.containsProfanity(text, member.getGuild().getId())) {
                        LOGGER.warn("Profanity detected for user: {}", member.getEffectiveName());
                        sendWarningDM();
                    }
                } else {
                    LOGGER.info("Audio file not found for user: {}", member.getEffectiveName());
                }
            } catch (IOException | UnsupportedAudioFileException e) {
                LOGGER.error("Error during speech-to-text conversion or profanity detection for user: {}", member.getEffectiveName(), e);
            } finally {
                if (!audioFile.delete()) {
                    LOGGER.error("Failed to delete audio file: {}", audioFile.getAbsolutePath());
                }
                audioReceiver.closeAudioStreams(member.getUser());
            }
        });
    }

    private void sendWarningDM() {
        EmbedBuilder embed = embedBuilderManager.createEmbedWithColor(
                "events.voice.warning.title",
                "events.voice.warning.description",
                member.getGuild().getId(),
                Color.RED
        );
        member.getUser().openPrivateChannel().queue(channel ->
                channel.sendMessageEmbeds(embed.build()).queue()
        );
    }
}
