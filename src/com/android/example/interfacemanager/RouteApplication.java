package com.android.example.interfacemanager;

import java.util.Date;

import android.app.Application;
import android.os.Message;
import android.util.Log;

public class RouteApplication extends Application {
	public static final String TAG = "RouteApplication";
	
	private Thread trafficMonitorThread = null;
	
	@Override
	public void onCreate() {
		Log.d(TAG, "Calling onCreate()");

	}
	
	public void getActivityReady() {
		trafficMonitorThread = new Thread(new TrafficMonitor(MainActivity.currentInstance.msgHandler));
		trafficMonitorThread.start();
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
}