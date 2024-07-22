package beta.com.moderationdiscordbot.voicemanager.auidomanager;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.entities.User;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code AudioReceiver} class implements {@link AudioReceiveHandler} to handle audio data received from users in a voice channel.
 * It collects and stores audio data for each user and provides methods to save the audio data to a file and close audio streams.
 * <p>
 * This class uses {@link AudioFormat} to define the audio format and {@link ByteArrayOutputStream} to store the audio data.
 * It is designed to handle audio data for multiple users simultaneously.
 *
 * <p><b>Fields:</b></p>
 * <ul>
 * <li>{@code audioFormat}: The {@link AudioFormat} instance specifying the format of the audio data (e.g., sample rate, bit depth).</li>
 * <li>{@code userAudioStreams}: A {@link Map} that associates each {@link User} with their {@link ByteArrayOutputStream} containing audio data.</li>
 * </ul>
 *
 * <p><b>Constructor:</b></p>
 * <ul>
 * <li>{@code AudioReceiver()}: Constructs an {@code AudioReceiver} instance with the default audio format.</li>
 * </ul>
 *
 * <p><b>Methods:</b></p>
 * <ul>
 * <li>{@code boolean canReceiveCombined()}: Returns {@code true} indicating that this handler can process combined audio data.</li>
 * <li>{@code void handleCombinedAudio(CombinedAudio combinedAudio)}: Handles the combined audio data received from all users. Writes the audio data to the appropriate {@link ByteArrayOutputStream} for each user.</li>
 * <li>{@code void saveAudioToFile(String userDir, User user)}: Saves the audio data of the specified {@link User} to a WAV file in the specified directory. If no audio data is available, it prints a message indicating so.</li>
 * <li>{@code void closeAudioStreams(User user)}: Closes and removes the {@link ByteArrayOutputStream} associated with the specified {@link User}. If no stream is found, it prints a message indicating so.</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * {@code
 * AudioReceiver audioReceiver = new AudioReceiver();
 * // Register audioReceiver with JDA's audio system
 * audioReceiver.saveAudioToFile("path/to/userDir", user);
 * audioReceiver.closeAudioStreams(user);
 * }
 * </pre>
 */

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
