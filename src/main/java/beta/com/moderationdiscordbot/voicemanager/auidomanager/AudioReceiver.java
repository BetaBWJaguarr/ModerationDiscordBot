package beta.com.moderationdiscordbot.voicemanager.auidomanager;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;

import javax.sound.sampled.*;
import java.io.*;

public class AudioReceiver implements AudioReceiveHandler {
    private AudioFormat audioFormat;
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    public AudioReceiver() {
        audioFormat = new AudioFormat(48000.0f, 16, 2, true, true);
    }

    @Override
    public boolean canReceiveCombined() {
        return true;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        if (!combinedAudio.getUsers().isEmpty()) {
            byte[] audioData = combinedAudio.getAudioData(1.0f);
            try {
                byteArrayOutputStream.write(audioData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveAudioToFile(String filePath) {
        byte[] audioBytes = byteArrayOutputStream.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioBytes);
        AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, audioBytes.length / audioFormat.getFrameSize());

        File outputFile = new File(filePath);
        try {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
