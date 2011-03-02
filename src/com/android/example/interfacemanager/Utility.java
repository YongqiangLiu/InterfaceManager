package com.android.example.interfacemanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

public class Utility {
	public static String TAG = "Utility";
	
	public static final int WIFI_NETWORK = 0;
	public static final int G3_NETWORK = 1;
	
	public static final int MESSAGE_TRAFFIC_INFO = 0;
	public static final int MESSAGE_UPDATE_ICON = 1;
	public static final int MESSAGE_UPDATE_INTERFACE_STATE = 2;
	
	public static final int INTERFACE_ACTIVE = 0;
	public static final int INTERFACE_INACTIVE = 1;
	public static final int INTERFACE_DISABLE = 2;
	
	public static final int POLICLY_FOLLOW_SYSTEM = 0;
	public static final int POLICLY_FOLLOW_ME = 1;
	
	public static final String wifiName = "eth0";
	public static final String g3Name = "rmnet0";
	
	
	
	public static ArrayList<String> readLinesFromFile(String filename) {
		ArrayList<String> lines = new ArrayList<String>();
		File file = new File(filename);
		if (!file.canRead()) {
			Log.d(TAG, filename + "can not be read");
			return lines;
		}
		BufferedReader buffer = null;
        try {
			buffer = new BufferedReader(new FileReader(file), 8192);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
		}
		try {
			String line = null;
			while ((line = buffer.readLine()) != null) {
				lines.add(line.trim());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
		}
		finally {
			try {
				buffer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getMessage());
			}
		}
		return lines;
	}
	
	public static long[] readTraffic(String device) {
		long[] traffic = {0, 0};
		for (String line : Utility.readLinesFromFile("/proc/net/dev")) {
			if (!line.startsWith(device)) continue;
			String[] elems = line.split(" +");
			traffic[0] = Long.parseLong(elems[1]);
			traffic[1] = Long.parseLong(elems[9]);
		}
		return traffic;
	}
	
	public static int getInfacePhysicalState(int intface) {
		int state = Utility.INTERFACE_DISABLE;
		final String gwNetmask = "00000000"; //indicate this route direct to default gw
		String device = (intface == Utility.WIFI_NETWORK)? Utility.wifiName : Utility.g3Name;
		for (String line : Utility.readLinesFromFile("/proc/net/route")) {
				String[] items = line.split("\t| +");
				if (items[0].equals(device)) {
					state = Utility.INTERFACE_INACTIVE;
					if (items[1].equals(gwNetmask)) state = Utility.INTERFACE_ACTIVE;
				}
		}
		return state;
	}
	
}