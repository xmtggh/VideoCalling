package com.ggh.video.event;

import android.os.Handler;
import android.os.Looper;

import org.greenrobot.eventbus.EventBus;


public class MainThreadBus extends EventBus {
	public final Handler handler = new Handler(Looper.getMainLooper());

	MainThreadBus() {}
	
	@Override
	public void post(final Object event) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			super.post(event);
		} else {
			handler.post(new Runnable() {
				@Override
				public void run() {
					MainThreadBus.super.post(event);
				}
			});
		}
	}
	
	public void postDelayed(final Object event, int ms) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				MainThreadBus.super.post(event);
			}
		}, ms);
	}
	
	public void postRunnable(Runnable runnable) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			runnable.run();
		} else {
			handler.post(runnable);
		}
	}
	
	public void postRunnableDelayed(Runnable runnable, int ms) {
		handler.postDelayed(runnable, ms);
	}
}
