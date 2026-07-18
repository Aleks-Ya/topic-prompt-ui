package topicpromptui.ui.model.question.sound;

import topicpromptui.core.storagefilesystem.AnswerType;
import jakarta.inject.Singleton;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

@Singleton
class SoundServiceImpl implements SoundService {
    private static final float SAMPLE_RATE = 44_100f;
    private static final int DURATION_MS = 150;
    private static final int FADE_MS = 8;
    private static final double AMPLITUDE = 0.1 * Short.MAX_VALUE;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

    private final Map<AnswerType, byte[]> tones = new EnumMap<>(AnswerType.class);

    public SoundServiceImpl() {
        tones.put(AnswerType.GRAMMAR, tone(440));
        tones.put(AnswerType.OPEN_AI, tone(550));
        tones.put(AnswerType.CLAUDE, tone(660));
        tones.put(AnswerType.GCP, tone(880));
    }

    private static byte[] tone(double frequencyHz) {
        int sampleCount = (int) (SAMPLE_RATE * DURATION_MS / 1000);
        int fadeSamples = (int) (SAMPLE_RATE * FADE_MS / 1000);
        byte[] samples = new byte[sampleCount * 2];
        for (int i = 0; i < sampleCount; i++) {
            double envelope = 1.0;
            if (i < fadeSamples) {
                envelope = (double) i / fadeSamples;
            } else if (i > sampleCount - fadeSamples) {
                envelope = (double) (sampleCount - i) / fadeSamples;
            }
            short sample = (short) (AMPLITUDE * envelope * Math.sin(2 * Math.PI * frequencyHz * i / SAMPLE_RATE));
            samples[2 * i] = (byte) (sample & 0xFF);
            samples[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return samples;
    }

    @Override
    public synchronized void beenOnAnswer(AnswerType answerType) {
        byte[] samples = tones.get(answerType);
        try {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.open(new AudioInputStream(new ByteArrayInputStream(samples), FORMAT, samples.length / 2));
            clip.start();
        } catch (LineUnavailableException | IOException e) {
            throw new IllegalStateException("Failed to play beep for answer type " + answerType, e);
        }
    }
}
