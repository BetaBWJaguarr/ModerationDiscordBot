package beta.com.moderationdiscordbot.voicemanager.auidomanager;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.User;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AudioReceiver implements AudioReceiveHandler {
    private final AudioFormat audioFormat;
    private final Map<User, ByteArrayOutputStream> userAudioStreams;

    public AudioReceiver() {
        this.audioFormat = new AudioFormat(48000.0f, 16, 2, true, true);
        this.userAudioStreams = new HashMap<>();
    }

    @Override
    public boolean canReceiveCombined() {
        return true;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        byte[] audioData = combinedAudio.getAudioData(1.0f);
        for (User user : combinedAudio.getUsers()) {
            ByteArrayOutputStream byteArrayOutputStream = userAudioStreams.computeIfAbsent(user, k -> new ByteArrayOutputStream());
            try {
                byteArrayOutputStream.write(audioData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveAudioToFile(String userDir, User user) {
        ByteArrayOutputStream byteArrayOutputStream = userAudioStreams.get(user);
        if (byteArrayOutputStream != null && byteArrayOutputStream.size() > 0) {
            try {
                byte[] audioBytes = byteArrayOutputStream.toByteArray();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioBytes);
                AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, audioBytes.length / audioFormat.getFrameSize());

                File outputFile = new File(userDir + "/output.wav");
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);
                System.out.println("Audio data saved to file for user: " + user.getName());

                userAudioStreams.remove(user);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No audio data available for user: " + user.getName());
        }
    }

    public void closeAudioStreams(User user) {
        ByteArrayOutputStream stream = userAudioStreams.remove(user);
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
