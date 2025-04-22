package com.example.moodproject;

import android.content.Context;
import android.util.Log;
import com.jlibrosa.audio.JLibrosa;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

public class MFCCExtractor {

    private static final String TAG = "MFCCExtractor";

    private static final int SAMPLE_RATE = 44100;
    private static final int NUM_MFCC = 284;       // Model expects 284 MFCC coefficients
    private static final int N_FFT = 2048;
    private static final int HOP_LENGTH = 512;
    private static final int N_MELS = 284;         // Match NUM_MFCC for consistency
    private static final int EXPECTED_FRAMES = 30; // Exactly match your model input frames

    private final Context context;

    public MFCCExtractor(Context context) {
        this.context = context;
    }

    public float[][][] extractMFCCFeatures(byte[] audioData) throws Exception {
        File wavFile = createTempWavFile(audioData);

        // Set locale temporarily to avoid decimal errors
        Locale originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);

        try {
            JLibrosa jLibrosa = new JLibrosa();

            float[] audioFloatArray = jLibrosa.loadAndRead(
                    wavFile.getAbsolutePath(), SAMPLE_RATE, -1
            );

            float[][] mfcc = jLibrosa.generateMFCCFeatures(
                    audioFloatArray, SAMPLE_RATE, NUM_MFCC, N_FFT, N_MELS, HOP_LENGTH
            );

            // Log original dimensions
            Log.d(TAG, "MFCC dimensions before resize: frames=" + mfcc.length + ", coeffs=" + mfcc[0].length);

            // Resize MFCC array to exactly [30 frames x 284 features]
            mfcc = resizeMFCC(mfcc, EXPECTED_FRAMES, NUM_MFCC);

            // Wrap in [1,30,284] shape
            return new float[][][]{mfcc};

        } catch (Exception e) {
            Log.e(TAG, "Error extracting MFCC: ", e);
            throw new Exception("MFCC extraction failed: " + e.getMessage(), e);
        } finally {
            if (wavFile.exists()) wavFile.delete();
            Locale.setDefault(originalLocale); // restore original locale
        }
    }

    /** Resize MFCC array to match the expected model input shape precisely */
    private float[][] resizeMFCC(float[][] originalMFCC, int targetFrames, int targetCoeffs) {
        float[][] resizedMFCC = new float[targetFrames][targetCoeffs];

        int framesToCopy = Math.min(targetFrames, originalMFCC.length);
        int coeffsToCopy = Math.min(targetCoeffs, originalMFCC[0].length);

        for (int i = 0; i < framesToCopy; i++) {
            System.arraycopy(originalMFCC[i], 0, resizedMFCC[i], 0, coeffsToCopy);
        }

        // Remaining cells are zero-filled if original is smaller
        return resizedMFCC;
    }

    /** Create valid WAV file from PCM audio data */
    private File createTempWavFile(byte[] pcmData) throws IOException {
        File tempWav = File.createTempFile("audio_chunk", ".wav", context.getCacheDir());

        try (FileOutputStream fos = new FileOutputStream(tempWav)) {
            writeWavHeader(fos, pcmData.length);
            fos.write(pcmData);
        }

        return tempWav;
    }

    /** Proper WAV header generation for PCM data */
    private void writeWavHeader(FileOutputStream fos, int audioDataLength) throws IOException {
        int totalDataLen = audioDataLength + 36;
        int channels = 1;
        int byteRate = SAMPLE_RATE * channels * 16 / 8;

        ByteBuffer header = ByteBuffer.allocate(44);
        header.order(ByteOrder.LITTLE_ENDIAN);

        header.put("RIFF".getBytes());
        header.putInt(totalDataLen);
        header.put("WAVE".getBytes());
        header.put("fmt ".getBytes());
        header.putInt(16);                     // PCM format chunk size
        header.putShort((short) 1);            // PCM audio format
        header.putShort((short) channels);
        header.putInt(SAMPLE_RATE);
        header.putInt(byteRate);
        header.putShort((short) (channels * 16 / 8)); // Block align
        header.putShort((short) 16);           // Bits per sample
        header.put("data".getBytes());
        header.putInt(audioDataLength);

        fos.write(header.array());
    }
}