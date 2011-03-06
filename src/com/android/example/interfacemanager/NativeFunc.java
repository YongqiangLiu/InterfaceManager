package com.android.example.interfacemanager;

import android.util.Log;

public class NativeFunc {
	public static String TAG = "NativeFunc";
	static {
		try {
		    Log.i(TAG, "Trying to load libnative-func.so");    
			System.loadLibrary("native-func");
		} catch (UnsatisfiedLinkError ule) {
            Log.i(TAG, "Could not load libnative-func.so");
        }
      
	}
	public static native String getTest();
	public static native int delDefaultRoute(int type);
	public static native int addDefaultRoute(int type);
}
