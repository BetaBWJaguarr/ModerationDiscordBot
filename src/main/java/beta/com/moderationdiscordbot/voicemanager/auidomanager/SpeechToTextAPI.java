package beta.com.moderationdiscordbot.voicemanager.auidomanager;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.LibVosk;
import org.vosk.LogLevel;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class SpeechToTextAPI {

    public static String convertSpeechToText(String fileName, String modelPath) throws IOException, UnsupportedAudioFileException {

        LibVosk.setLogLevel(LogLevel.INFO);


        Model model = new Model(modelPath);


        AudioInputStream ais = AudioSystem.getAudioInputStream(new File(fileName));
        AudioFormat audioFormat = ais.getFormat();


        if (audioFormat.getSampleRate() != 16000 || audioFormat.getChannels() != 1) {
            audioFormat = new AudioFormat(16000, 16, 1, true, false);
            ais = AudioSystem.getAudioInputStream(audioFormat, ais);
        }


        Recognizer recognizer = new Recognizer(model, 16000);


        int nBytesRead;
        byte[] buffer = new byte[4096];
        StringBuilder transcription = new StringBuilder();

        while ((nBytesRead = ais.read(buffer)) != -1) {
            if (recognizer.acceptWaveForm(buffer, nBytesRead)) {
                transcription.append(recognizer.getResult()).append("\n");
            } else {
                transcription.append(recognizer.getPartialResult()).append("\n");
            }
        }
        transcription.append(recognizer.getFinalResult()).append("\n");


        recognizer.close();
        ais.close();
        model.close();

        return transcription.toString();
    }
}