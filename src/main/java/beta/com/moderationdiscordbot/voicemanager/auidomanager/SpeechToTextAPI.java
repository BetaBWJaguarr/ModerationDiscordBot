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

/**
 * The {@code SpeechToTextAPI} class provides functionality for converting speech in an audio file to text using the Vosk Speech-to-Text library.
 * It utilizes the Vosk {@link Recognizer} to process audio data and return transcriptions of the spoken content.
 * <p>
 * This class requires an audio file in WAV format and a trained Vosk model for accurate speech recognition.
 * The audio file must have a sample rate of 16000 Hz and be mono-channel.
 *
 * <p><b>Methods:</b></p>
 * <ul>
 * <li>{@code static String convertSpeechToText(String fileName, String modelPath)}: Converts the speech in the specified audio file to text using the Vosk model at the specified path. Returns the transcription as a {@code String}.</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * {@code
 * String transcription = SpeechToTextAPI.convertSpeechToText("path/to/audiofile.wav", "path/to/vosk-model");
 * }
 * </pre>
 *
 * @param fileName The path to the WAV file containing the speech to be converted.
 * @param modelPath The path to the Vosk model directory.
 * @return A {@code String} containing the transcribed text from the audio file.
 * @throws IOException If an I/O error occurs while reading the audio file or model.
 * @throws UnsupportedAudioFileException If the audio file format is not supported.
 */

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