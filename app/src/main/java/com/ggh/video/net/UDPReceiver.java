package com.ggh.video.net;

import android.util.Log;

import java.net.DatagramPacket;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by ZQZN on 2017/12/12.
 */

public class UDPReceiver extends Receiver {
    private boolean isReceiver = false;
    private DatagramPacket mPacket;
    private byte[] packetBuf = new byte[65536];
    private int packetSize = 65536;
    public UDPReceiver() {
        mPacket = new DatagramPacket(packetBuf,packetSize);
        initRxReceiver();
    }

    private void initRxReceiver() {
        Observable.create(new ObservableOnSubscribe<Frame>() {
            @Override
            public void subscribe(ObservableEmitter<Frame> e) throws Exception {
                while (isReceiver){
                    UDPProvider.getInstance().getmSocket().receive(mPacket);
                    e.onNext(new Frame(mPacket.getData(),mPacket.getLength()));
                    continue;
                }
            }
        }).subscribe(new Observer<Frame>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Frame frame) {
                Log.d("ggh","送去解码");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }



    @Override
    public void startRecivice() {
        isReceiver = true;
    }

    @Override
    public void stopRecivice() {
        isReceiver = false;
    }
}
