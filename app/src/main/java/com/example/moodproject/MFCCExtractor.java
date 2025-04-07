package com.example.moodproject;

import android.content.Context;
import android.util.Log;

import com.jlibrosa.audio.JLibrosa;
import com.jlibrosa.audio.exception.FileFormatNotSupportedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MFCCExtractor {
    private static final int SAMPLE_RATE = 44100;
    private static final int NUM_MFCC = 13;
    private static final int N_FFT = 2048;
    private static final int N_MELS = 128;
    private static final int HOP_LENGTH = 512;

    private Context context;

    public MFCCExtractor(Context context) {
        this.context = context;
    }

    public float[][][] extractMFCCFeatures(byte[] audioData) throws Exception {
        // Create a temporary file for audio data
        File tempAudioFile = createTempAudioFile(audioData);

        try {
            // Initialize JLibrosa
            JLibrosa jLibrosa = new JLibrosa();

            // Load audio file
            float[] audioFloatArray = jLibrosa.loadAndRead(
                    tempAudioFile.getAbsolutePath(),
                    SAMPLE_RATE,  // Sample rate
                    -1             // Full audio length
            );

            // Generate MFCC features with more detailed parameters
            float[][] mfccFeatures = jLibrosa.generateMFCCFeatures(
                    audioFloatArray,     // Audio data
                    SAMPLE_RATE,         // Sample rate
                    NUM_MFCC,            // Number of MFCC coefficients
                    N_FFT,               // FFT window size
                    N_MELS,              // Number of mel bands
                    HOP_LENGTH           // Hop length between frames
            );

            // Reshape to match TensorFlow Lite model input
            float[][][] formattedMFCCs = new float[1][mfccFeatures.length][mfccFeatures[0].length];
            for (int i = 0; i < mfccFeatures.length; i++) {
                formattedMFCCs[0][i] = mfccFeatures[i];
            }

            return formattedMFCCs;

        } catch (Exception e) {
            Log.e("MFCCExtractor", "Error extracting MFCC features", e);
            throw new Exception("MFCC extraction failed", e);
        } finally {
            // Clean up temporary file
            if (tempAudioFile != null) {
                tempAudioFile.delete();
            }
        }
    }

    private File createTempAudioFile(byte[] audioData) throws IOException {
        File tempFile = File.createTempFile("audio_chunk", ".wav", context.getCacheDir());

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            writeWavHeader(fos, audioData.length);
            fos.write(audioData);
        }

        return tempFile;
    }

    private void writeWavHeader(FileOutputStream fos, int dataLength) throws IOException {
        // WAV header writing logic (similar to previous implementation)
        byte[] header = new byte[44];

        // RIFF chunk
        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';

        // Overall file size
        writeInt(header, 4, dataLength + 36);

        // WAVE header
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';

        // fmt chunk
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';

        // Subchunk1Size
        writeInt(header, 16, 16);

        // Audio format (1 = PCM)
        writeShort(header, 20, (short)1);

        // Number of channels
        writeShort(header, 22, (short)1);

        // Sample rate
        writeInt(header, 24, SAMPLE_RATE);

        // Byte rate
        writeInt(header, 28, SAMPLE_RATE * 2);

        // Block align
        writeShort(header, 32, (short)2);

        // Bits per sample
        writeShort(header, 34, (short)16);

        // Data chunk
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';

        // Data size
        writeInt(header, 40, dataLength);

        fos.write(header);
    }

    private void writeInt(byte[] buffer, int offset, int value) {
        buffer[offset] = (byte)(value & 0xFF);
        buffer[offset + 1] = (byte)((value >> 8) & 0xFF);
        buffer[offset + 2] = (byte)((value >> 16) & 0xFF);
        buffer[offset + 3] = (byte)((value >> 24) & 0xFF);
    }

    private void writeShort(byte[] buffer, int offset, short value) {
        buffer[offset] = (byte)(value & 0xFF);
        buffer[offset + 1] = (byte)((value >> 8) & 0xFF);
    }

    // Utility methods for processing
    public String getMaxEmotion(Map<String, Float> emotions) {
        String maxEmotion = "";
        float maxValue = 0;

        for (Map.Entry<String, Float> entry : emotions.entrySet()) {
            if (entry.getValue() > maxValue) {
                maxValue = entry.getValue();
                maxEmotion = entry.getKey();
            }
        }

        return maxEmotion;
    }

    public Map<String, Float> aggregateEmotionResults(List<Map<String, Float>> secondResults) {
        Map<String, Float> aggregated = new HashMap<>();

        // Get all emotion labels from the first second
        Set<String> emotionLabels = secondResults.get(0).keySet();

        // Initialize aggregated values
        for (String emotion : emotionLabels) {
            aggregated.put(emotion, 0.0f);
        }

        // Sum up confidence values across seconds
        for (Map<String, Float> secondResult : secondResults) {
            for (String emotion : emotionLabels) {
                aggregated.put(
                        emotion,
                        aggregated.get(emotion) + secondResult.getOrDefault(emotion, 0.0f)
                );
            }
        }

        // Normalize by dividing by the number of seconds
        for (String emotion : emotionLabels) {
            aggregated.put(
                    emotion,
                    aggregated.get(emotion) / secondResults.size()
            );
        }

        return aggregated;
    }

    public Map<String, Float> processModelOutputForSecond(float[] output, int secondIndex) {
        Map<String, Float> emotions = new HashMap<>();

        // Emotion labels (should match your model's output order)
        String[] emotionLabels = {
                "angry", "calm", "disgust", "fearful", "happy", "neutral", "sad", "surprised"
        };

        // Store emotion confidences
        for (int i = 0; i < output.length && i < emotionLabels.length; i++) {
            emotions.put(emotionLabels[i], output[i]);
        }

        // Log the detected emotion for this second
        String maxEmotion = getMaxEmotion(emotions);
        Log.d("MFCCExtractor", "Second " + secondIndex + ": " +
                maxEmotion + " (" + emotions.get(maxEmotion) + ")");

        return emotions;
    }
}