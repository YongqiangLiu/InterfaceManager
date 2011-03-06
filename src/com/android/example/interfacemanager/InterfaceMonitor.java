package com.android.example.interfacemanager;

import android.os.Handler;
import android.os.Message;

public class InterfaceMonitor implements Runnable {
	final String TAG = "InterfaceMonitor";
	private int INTERVAL = 1; //monitor interval in secs
	private Handler msgHandler = null;
	
	public InterfaceMonitor(Handler msgHandler) {
		this.msgHandler = msgHandler;
	}
	
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
			if (msgHandler != null) msgHandler.sendMessage(msg);
		}
	}
	
	private void changeSystem() {
		return;
	}
}