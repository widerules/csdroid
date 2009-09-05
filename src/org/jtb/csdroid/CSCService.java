package org.jtb.csdroid;

import java.util.Timer;
import java.util.TimerTask;

import org.jtb.csc.CSCManager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class CSCService extends Service {	
	private CSCService mThis;
	private Timer mTimer = new Timer();
	
	public CSCService() {
		this.mThis = this;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(getClass().getSimpleName(), "service started");
		mTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Message m = Message.obtain(TabWidgetActivity.mStaticHandler, TabWidgetActivity.SERVICE_START_WHAT);
				ClosestActivity.mStaticHandler.sendMessage(m);
			}
			// delay refresh interval, because activity already initialized
			// service data for its use
			// schedule service a minute after the refresh interval, to avoid race conditions
		}, CSCManager.REFRESH_INTERVAL, CSCManager.REFRESH_INTERVAL + 10 * 1000);
	}
	
	@Override
	public void onDestroy() {
		Log.d(getClass().getSimpleName(), "service stopped");		
	}

}
