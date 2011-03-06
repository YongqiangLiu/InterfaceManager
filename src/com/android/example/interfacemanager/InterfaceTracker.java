package com.android.example.interfacemanager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class InterfaceTracker {
	final static String TAG = "InterfaceControl";
	
	private Activity context = null;
	private Thread interfaceMonitorThread = null;
	
	
	public InterfaceTracker(Activity context, Handler msgHandler) {
		this.context = context;
		this.interfaceMonitorThread = new Thread(new InterfaceMonitor(msgHandler));
		this.interfaceMonitorThread.start();
	}
	
    public void enableInterface(int intface, boolean enable) {
    	
    	if (intface == Utility.WIFI_NETWORK) {
    		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    		if (wifiManager.isWifiEnabled() != enable) {
    			wifiManager.setWifiEnabled(enable);
    		}	
    	} else {
    		if (getMobileDataEnabled() != enable) {
    			setMobileDataEnabled(enable);
    		}
    	}
    }
    
    private Method getMethodFromClass(Object obj, String methodName) {
    	final String TAG = "getMethodFromClass";
        Class<?> whichClass = null;
        try {
            whichClass = Class.forName(obj.getClass().getName());
        } catch (ClassNotFoundException e2) {
            // TODO Auto-generated catch block
            Log.d(TAG, "class not found");
        }
        
        Method method = null;
        try {
            //method = whichClass.getDeclaredMethod(methodName);
            Method[] methods = whichClass.getDeclaredMethods();
        	for (Method m : methods) {
        		//Log.d(TAG, "method: " + m.getName());
        		if (m.getName().contains(methodName)) {
        			method = m;
        		}
        	}
        } catch (SecurityException e2) {
            // TODO Auto-generated catch block
        	Log.d(TAG, "SecurityException for " + methodName);
        } 
        return method;
    }
    
    private Object runMethodofClass(Object obj, Method method, Object... argv) {
    	Object result = null;
    	if (method == null) return result;
    	method.setAccessible(true);
        try {
			result = method.invoke(obj, argv);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "IllegalArgumentException for " + method.getName());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "IllegalAccessException for " + method.getName());
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "InvocationTargetException for " + method.getName() 
					+ "; Reason: " + e.getLocalizedMessage());
		}
		return result;
    }
    
    private boolean getMobileDataEnabled() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        Method m = getMethodFromClass(cm, "getMobileDataEnabled");
        Object enabled = runMethodofClass(cm, m);
		return (Boolean) enabled;
    }
    
    private void setMobileDataEnabled(boolean enable) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        Method m = getMethodFromClass(cm, "setMobileDataEnabled");
        runMethodofClass(cm, m, enable);
    }	

}