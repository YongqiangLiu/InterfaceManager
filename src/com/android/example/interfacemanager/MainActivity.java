package com.android.example.interfacemanager;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	public static final String TAG = "MainActivity";

	public UIRefreshHandler msgHandler = null;
	public static MainActivity currentInstance = null;
	
	private int wifiState = Utility.INTERFACE_ACTIVE;
	private int g3State = Utility.INTERFACE_ACTIVE;
	private int policy = Utility.POLICLY_FOLLOW_SYSTEM;
	private Object stateLock = new Object();
	
	private TableLayout trafficTable = null;
	private RouteApplication application = null;
	private ImageView wifiImage = null;
	private ImageView g3Image = null;
	private Button wifiButton = null;
	private Button g3Button = null;
	private TextView wifiTrafficUpRate = null;
	private TextView g3TrafficUpRate = null;
	private TextView wifiTrafficDownRate = null;
	private TextView g3TrafficDownRate = null;
	private ToggleButton trafficButton = null;
	
	private Toast board = null;
	
	private InterfaceTracker IFTracker = null; 
	

	public int getInterfaceState(int type) {
		synchronized (this.stateLock) {
			return (type == Utility.WIFI_NETWORK)? this.wifiState : this.g3State;
		}
	}
	
	public int getPolicy() {
		return this.policy;
	}
	
	private void setInterfaceSate(int type, int newState) {
		synchronized (this.stateLock) {
			switch (type) {
			case Utility.WIFI_NETWORK:
				this.wifiState = newState;
				break;
			case Utility.G3_NETWORK:
				this.g3State = newState;
			}
		}
	} 

	private void setCurrentInstance(MainActivity currentInstance) {
		MainActivity.currentInstance = currentInstance;
	}
	
	private OnClickListener buttonListener = new View.OnClickListener() {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Button button = (Button) v;
			//Toast.makeText(RoutePathSelector.this, button.getText(), Toast.LENGTH_SHORT).show();
            changeToNetwork(button.getId());
		}
	};

	private OnClickListener imageViewListener = new View.OnClickListener() {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ImageView image = (ImageView) v;
			//Toast.makeText(MainActivity.this, "image"image.getId(), Toast.LENGTH_SHORT).show();
			changeInterfaceState(image.getId());
		}
	};
	
	private OnClickListener toggleListener = new View.OnClickListener() {
		public void onClick(View v) {
			ToggleButton toggleButton = (ToggleButton) v;
			if (toggleButton.isChecked()) {
				if (!trafficTable.isShown()) trafficTable.setVisibility(View.VISIBLE);
			}
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "calling onCreate()");
    	super.onCreate(savedInstanceState);
    	
    	
    	this.application = (RouteApplication)getApplication();
    	setCurrentInstance(this);
    	msgHandler = new UIRefreshHandler(this);
    	this.application.getActivityReady();
    	IFTracker = new InterfaceTracker(this, msgHandler);
    	
        setContentView(R.layout.main);
        
        trafficTable = (TableLayout) findViewById(R.id.trafficTable);
        wifiImage = (ImageView) findViewById(R.id.wifiImage);
        g3Image = (ImageView) findViewById(R.id.g3Image);
        wifiImage.setOnClickListener(imageViewListener);
        g3Image.setOnClickListener(imageViewListener);
        
        wifiTrafficUpRate = (TextView) findViewById(R.id.wifiTrafficUpRate);
        wifiTrafficDownRate = (TextView) findViewById(R.id.wifiTrafficDownRate);
        g3TrafficUpRate = (TextView) findViewById(R.id.g3TrafficUpRate);
        g3TrafficDownRate = (TextView) findViewById(R.id.g3TrafficDownRate);
        
        wifiButton = (Button) findViewById(R.id.wifiButton);
        g3Button = (Button) findViewById(R.id.g3Button);
        wifiButton.setOnClickListener(buttonListener);
        g3Button.setOnClickListener(buttonListener);
        
        trafficButton = (ToggleButton) findViewById(R.id.trafficMonitorButton);
        trafficButton.setOnClickListener(toggleListener);
        
        board = Toast.makeText(this, "start up", Toast.LENGTH_SHORT);
        trafficTable.setVisibility(View.INVISIBLE);
        
    	setInterfaceSate(Utility.WIFI_NETWORK, 
    			Utility.getInfacePhysicalState(Utility.WIFI_NETWORK));
    	setInterfaceSate(Utility.G3_NETWORK, 
    			Utility.getInfacePhysicalState(Utility.G3_NETWORK));
    	updateImage();
    	
    }
    
    private void changeToNetwork(int buttonID) {
		int toNetwork = (buttonID == R.id.wifiButton) ? Utility.WIFI_NETWORK : Utility.G3_NETWORK; 
		this.application.changeDefaultGW(toNetwork);
    }
    
    private void changeInterfaceState(int imageViewID) {
    	int intface = (imageViewID == R.id.wifiImage) ? Utility.WIFI_NETWORK:Utility.G3_NETWORK;
    	String device = (intface == Utility.WIFI_NETWORK)? "WiFi" : "3G";
    	if (getInterfaceState(intface) != Utility.INTERFACE_DISABLE) {
    		setInterfaceSate(intface, Utility.INTERFACE_DISABLE);
    		IFTracker.enableInterface(intface, false);
    		new ShowInProcess().execute("Shutting Down " + device, "1");
    	} else {
    		setInterfaceSate(intface, Utility.INTERFACE_ACTIVE);
    		IFTracker.enableInterface(intface, true);
    		new ShowInProcess().execute("Bringing UP " + device, "5");
    	}
    }
    
    private int getIconByState(int intface, int state) {
    	int icon = 0;
    	switch (state) {
    	case Utility.INTERFACE_ACTIVE:
    		icon = (intface == Utility.WIFI_NETWORK)?R.drawable.wifi_start:R.drawable.g3_start;
    		break;
    	case Utility.INTERFACE_INACTIVE:
    		icon = (intface == Utility.WIFI_NETWORK)?R.drawable.wifi_stop:R.drawable.g3_stop;
    		break;
    	case Utility.INTERFACE_DISABLE:
    		icon = (intface == Utility.WIFI_NETWORK)?R.drawable.wifi_disable:R.drawable.g3_disable;
    		break;
    	}
    	return icon;
    }

    private void updateImage() {
    	int wifiIcon = getIconByState(Utility.WIFI_NETWORK, getInterfaceState(Utility.WIFI_NETWORK));
    	int g3Icon = getIconByState(Utility.G3_NETWORK, getInterfaceState(Utility.G3_NETWORK));
    	Message msg = Message.obtain();
    	msg.what = Utility.MESSAGE_UPDATE_ICON;
    	msg.arg1 = wifiIcon;
    	msg.arg2 = g3Icon;
    	msgHandler.sendMessage(msg);
    }
    
    
    public class UIRefreshHandler extends Handler {
    	
    	static final String TAG = "UIRefreshHandler";
    	
    	private MainActivity parentActivity = null;
    	  
    	UIRefreshHandler(MainActivity parentActivity) {
    		this.parentActivity = parentActivity;
    	}
    	
    	@Override
    	public void handleMessage(Message msg) {
    		//Log.d(TAG, "receive msg :" + msg.what);
    		switch (msg.what) {
    		case Utility.MESSAGE_TRAFFIC_INFO:
    			final String noTraffic = "- - - -";
    			int ntType = msg.arg1;
    			TrafficMonitor.TrafficStatis data = (TrafficMonitor.TrafficStatis) msg.obj;
    			int state = Utility.getInfacePhysicalState(ntType);
    			int color = (state == Utility.INTERFACE_ACTIVE)? Color.WHITE : Color.GRAY;
    			TextView upRateView = (ntType == Utility.WIFI_NETWORK) ? 
    					parentActivity.wifiTrafficUpRate : parentActivity.g3TrafficUpRate;
    			TextView downRateView = (ntType == Utility.WIFI_NETWORK) ? 
    					parentActivity.wifiTrafficDownRate : parentActivity.g3TrafficDownRate;
    			String upRate = (state == Utility.INTERFACE_DISABLE)? noTraffic : "" + data.uploadRate + "Kbps";
    			String downRate = (state == Utility.INTERFACE_DISABLE)? noTraffic : "" + data.downloadRate + "Kbps";
    			upRateView.setText(upRate);
    			upRateView.setTextColor(color);
    			downRateView.setText(downRate);
    			downRateView.setTextColor(color);
    			break;
    		case Utility.MESSAGE_UPDATE_ICON:
    			parentActivity.wifiImage.setImageResource(msg.arg1);
    			parentActivity.g3Image.setImageResource(msg.arg2);
    			break;
    		case Utility.MESSAGE_UPDATE_INTERFACE_STATE:
    			MainActivity.currentInstance.setInterfaceSate(Utility.WIFI_NETWORK, msg.arg1);
    			MainActivity.currentInstance.setInterfaceSate(Utility.G3_NETWORK, msg.arg2);
    			MainActivity.currentInstance.updateImage();
    			break;	

    		}
    	}
    }
    
    
    private class ShowInProcess extends AsyncTask<String, String, Integer> {
    	protected Integer doInBackground(String... params) {
    		String text = params[0];
    		for (int i = 0; i < Integer.parseInt(params[1]); i++) {
    			text += " ..";
    			 publishProgress(text);
    			 try {
					Thread.sleep(2*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		return 0;
    	}
    	
    	protected void onProgressUpdate(String... progress) {
            board.setText(progress[0]);
            board.setDuration(Toast.LENGTH_SHORT);
            board.show();
        }

    }
    
}