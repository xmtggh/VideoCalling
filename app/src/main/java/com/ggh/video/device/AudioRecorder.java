package com.ggh.video.device;


import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.util.Log;

import com.ggh.video.encode.AudioEncoder;
import com.gyz.voipdemo_speex.util.Speex;

public class AudioRecorder implements Runnable {

    String LOG = "Recorder";

    //是否正在录制
    private boolean isRecording = false;
    //音频录制对象
    private AudioRecord audioRecord;

    private int audioBufSize = 0;
    //回声消除
    private AcousticEchoCanceler canceler;

    public void startRecording() {
        Log.d("ggh", "开启录音");
        //计算缓存大小
        audioBufSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLERATE,
                AudioConfig.PLAYER_CHANNEL_CONFIG2, AudioConfig.AUDIO_FORMAT);
        //实例化录制对象
        if (null == audioRecord && audioBufSize != AudioRecord.ERROR_BAD_VALUE) {
            audioRecord = new AudioRecord(AudioConfig.AUDIO_RESOURCE,
                    AudioConfig.SAMPLERATE,
                    AudioConfig.PLAYER_CHANNEL_CONFIG2,
                    AudioConfig.AUDIO_FORMAT, audioBufSize);
        }

        //消回音处理
        initAEC(audioRecord.getAudioSessionId());
        new Thread(this).start();
    }

    public boolean initAEC(int audioSession) {
        if (canceler != null) {
            return false;
        }
        if (!AcousticEchoCanceler.isAvailable()){
            return false;
        }
        canceler = AcousticEchoCanceler.create(audioSession);
        canceler.setEnabled(true);
        return canceler.getEnabled();
    }


    public void stopRecording() {
        this.isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public void run() {
        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e("ggh", "unInit");
            return;
        }
        // start encoder before recording
        AudioEncoder encoder = AudioEncoder.getInstance();
        encoder.startEncoding();
        audioRecord.startRecording();
        this.isRecording = true;
        int size = Speex.getInstance().getFrameSize();
        short[] samples = new short[size];
        while (isRecording) {
            int bufferRead = audioRecord.read(samples, 0, size);
            if (bufferRead > 0) {
                encoder.addData(samples,bufferRead);
            }
        }
        audioRecord.stop();
        encoder.stopEncoding();
    }
}
