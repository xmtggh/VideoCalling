package com.ggh.video.device;

import android.media.AudioFormat;
import android.media.MediaRecorder;

public class AudioConfig {

    /**
     * Recorder Configure
     * 8KHZ
     */
    public static final int SAMPLERATE = 8000;
    //    public static final int SAMPLERATE = 44100;
    public static final int PLAYER_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    // public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_8BIT;

    /**
     * Recorder Configure
     */
    public static final int AUDIO_RESOURCE = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    public static final int RECORDER_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    /**
     *
     */
    public static final int PLAYER_CHANNEL_CONFIG2 = AudioFormat.CHANNEL_CONFIGURATION_MONO;
}
