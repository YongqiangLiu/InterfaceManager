package com.android.example.interfacemanager;

import java.util.Date;

import android.os.Handler;
import android.os.Message;


public class TrafficMonitor implements Runnable {
	public final static int INTERVAL = 2; //sample rate in seconds
	public final static String TAG = "TrafficMonitor";
	private TrafficStatis wifiPrevData = new TrafficStatis();
	private TrafficStatis g3PrevData = new TrafficStatis();
	private long lastTime = 0;
	private Handler msgHandler = null;
	
	public TrafficMonitor(Handler msgHandler) {
		this.msgHandler = msgHandler;
	}
	
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			//Log.d(TAG, "get traffic ");
			wifiPrevData = reportTraffic(Utility.wifiName, wifiPrevData, Utility.WIFI_NETWORK);
			g3PrevData = reportTraffic(Utility.g3Name, g3PrevData, Utility.G3_NETWORK);
			lastTime = new Date().getTime();
			try {
				Thread.sleep(INTERVAL*1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} 
		}
	}
	
	private TrafficStatis reportTraffic(String device, TrafficStatis prevTrafficData, int ntType) {
		TrafficStatis trafficData = new TrafficStatis();
		long currentTime = new Date().getTime();
		long[] traffic = Utility.readTraffic(device);
		trafficData.downloadBytes = traffic[0];
		trafficData.uploadBytes = traffic[1];
		float elapsedSecs = (currentTime - this.lastTime)/1000;
		trafficData.downloadRate = Math.round((trafficData.downloadBytes - prevTrafficData.downloadBytes)*8/(1024*elapsedSecs));
		trafficData.uploadRate = Math.round((trafficData.uploadBytes - prevTrafficData.uploadBytes)*8/(1024*elapsedSecs));
		Message msg = Message.obtain();
		msg.what = Utility.MESSAGE_TRAFFIC_INFO;
		msg.arg1 = ntType;
		msg.obj = trafficData;
		if (msgHandler != null) msgHandler.sendMessage(msg);
		return trafficData;
	}
	
	public class TrafficStatis {
		public TrafficStatis() {
			downloadBytes = 0;
			uploadBytes = 0;
		}
		public long downloadBytes;
		public long uploadBytes;
		public int downloadRate;
		public int uploadRate;
	}
}
