package com.ggh.video.decode;

import android.util.Log;

import com.ggh.video.encode.AudioData;
import com.ggh.video.net.AudioPlayer;
import com.gyz.voipdemo_speex.util.Speex;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class AudioDecoder implements Runnable {

    String LOG = "AudioDecoder";
    private static AudioDecoder decoder;

    private static final int MAX_BUFFER_SIZE = 2048;

    private short[] decodedData;
    private boolean isDecoding = false;
    private List<AudioData> dataList = null;

    public static AudioDecoder getInstance() {
        if (decoder == null) {
            decoder = new AudioDecoder();
        }
        return decoder;
    }

    private AudioDecoder() {
        this.dataList = Collections
                .synchronizedList(new LinkedList<AudioData>());
    }


    public void addData(byte[] data, int size) {
        AudioData adata = new AudioData();
        adata.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
        adata.setReceiverdata(tempData);
        dataList.add(adata);
    }

    /**
     * start decode AMR data
     */
    public void startDecoding() {
        System.out.println(LOG + "开始解码");
        if (isDecoding) {
            return;
        }
        new Thread(this).start();
    }

    @Override
    public void run() {
        // start player first
        AudioPlayer player = AudioPlayer.getInstance();
        player.startPlaying();
        //
        this.isDecoding = true;

        Log.d(LOG, LOG + "initialized decoder");
        int decodeSize = 0;
        while (isDecoding) {
            while (dataList.size() > 0) {
                AudioData encodedData = dataList.remove(0);
                decodedData = new short[Speex.getInstance().getFrameSize()];
                byte[] data = encodedData.getReceiverdata();
                decodeSize = Speex.getInstance().decode(data, decodedData, data.length);
                Log.e(LOG, "解码一次" + data.length + " 解码后的长度 " + decodeSize);
                if (decodeSize > 0) {
                    // add decoded audio to player
                    player.addData(decodedData, decodeSize);
                }
            }
        }
        System.out.println(LOG + "stop decoder");
        // stop playback audio
        player.stopPlaying();
    }

    public void stopDecoding() {
        this.isDecoding = false;
    }
}