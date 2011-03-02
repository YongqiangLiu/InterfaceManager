package com.android.example.interfacemanager;

import java.util.Date;

import android.app.Application;
import android.os.Message;
import android.util.Log;

public class RouteApplication extends Application {
	public static final String TAG = "RouteApplication";

	public static final int ClASS_INTERFACE_MONITOR = 0;
	public static final int ClASS_TRAFFIC_MONITOR = 1;
	
	private Thread trafficMonitorThread = null;
	private Thread interfaceMonitorThread = null;
	
	@Override
	public void onCreate() {
		Log.d(TAG, "Calling onCreate()");

	}
	
	public void getActivityReady() {
		setThreadEnable(this.interfaceMonitorThread, ClASS_INTERFACE_MONITOR, true);
		setThreadEnable(this.trafficMonitorThread, ClASS_TRAFFIC_MONITOR, true);
	}
	
	public void changeDefaultGW(int type) {
		int activeNetwork = type;
		if (Utility.getInfacePhysicalState(activeNetwork) ==  Utility.INTERFACE_DISABLE) return;
		int oldNetwork = (type == Utility.WIFI_NETWORK) ? Utility.G3_NETWORK : Utility.WIFI_NETWORK;
		NativeFunc.delDefaultRoute(oldNetwork);
		NativeFunc.addDefaultRoute(activeNetwork);
		Message msg = Message.obtain();
		msg.what = Utility.MESSAGE_UPDATE_INTERFACE_STATE;
		msg.arg1 = (activeNetwork == Utility.WIFI_NETWORK)? Utility.INTERFACE_ACTIVE : Utility.INTERFACE_INACTIVE;
		msg.arg2 = (activeNetwork == Utility.G3_NETWORK)? Utility.INTERFACE_ACTIVE : Utility.INTERFACE_INACTIVE;
		MainActivity.currentInstance.msgHandler.sendMessage(msg);
	}
	
	private void setThreadEnable(Thread thread, int className, boolean enable) {
		if (enable) {
			if (thread == null || thread.isInterrupted()) {
				//it is ugly, can be better
				switch (className) { 
				case ClASS_INTERFACE_MONITOR:
					thread = new Thread(new InterfaceMonitor());
					break;
				case ClASS_TRAFFIC_MONITOR:
					thread = new Thread(new TrafficMonitor());
				}
				thread.start();
			}
		} else {
			if (thread != null) {
				thread.interrupt();
			}
		}
	}
	
	public class TrafficMonitor implements Runnable {
		final static int INTERVAL = 2; //sample rate in seconds
		final static String TAG = "TrafficMonitor";
		TrafficStatis wifiPrevData = new TrafficStatis();
		TrafficStatis g3PrevData = new TrafficStatis();
		long lastTime = 0;
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
			if (MainActivity.currentInstance != null) MainActivity.currentInstance.msgHandler.sendMessage(msg);
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
	

	public class InterfaceMonitor implements Runnable {
		final String TAG = "InterfaceMonitor";
		private int INTERVAL = 1; //monitor interval in secs
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				int wifiState = Utility.getInfacePhysicalState(Utility.WIFI_NETWORK);
				int g3State = Utility.getInfacePhysicalState(Utility.G3_NETWORK);
				if (MainActivity.currentInstance.getPolicy() == Utility.POLICLY_FOLLOW_SYSTEM) {
					changeMe(wifiState, g3State);
				} else {
					changeSystem();
				}

				try {
					Thread.sleep(INTERVAL*1000);
				} catch (InterruptedException e){
					Thread.currentThread().interrupt();
				}
			}
		}
		
		private void changeMe(int sysWifiState, int sysG3State) {
			if (sysWifiState != MainActivity.currentInstance.getInterfaceState(Utility.WIFI_NETWORK) ||
					sysG3State != MainActivity.currentInstance.getInterfaceState(Utility.G3_NETWORK)) {
				//Log.d(TAG, "wifi state is " + sysWifiState + "; 3G state is " + sysG3State);
				Message msg = Message.obtain();
				msg.what = Utility.MESSAGE_UPDATE_INTERFACE_STATE;
				msg.arg1 = sysWifiState;
				msg.arg2 = sysG3State;
				MainActivity.currentInstance.msgHandler.sendMessage(msg);
			}
		}
		
		private void changeSystem() {
			return;
		}
	}
 
}