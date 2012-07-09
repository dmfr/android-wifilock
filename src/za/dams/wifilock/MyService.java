package za.dams.wifilock;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class MyService extends Service {
	
	private boolean chkHighPerf ;
	private boolean chkWifiLock ;
	private boolean chkWifiPingKA ;
	
	private boolean isPlugged = false ;
	private boolean isWirelessOn = false ;
	private int wirelessGatewayIp ;
	
	private boolean currentStateWifilock = false ;
	private boolean currentStateWifilockHighperf = false ;
	
	private Thread pingThread ;
	
	private WifiManager.WifiLock mWifiLock ;
	private WifiManager.WifiLock mWifiLockHighperf ;
	
	private static final String TAG = "WifiLock/Service";

	public static boolean isRunning(Context context){
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("za.dams.wifilock.MyService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
	}
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Log.w("BatteryService", "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		//new UploadTask().execute() ;
		
		return START_STICKY;
	}
	
	public void onCreate(){
		super.onCreate();
		registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		registerReceiver(this.mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		
		// Get some preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		chkHighPerf = prefs.getBoolean("chkHighPerf", true);
		chkWifiLock = prefs.getBoolean("chkWifiLock", true);
		chkWifiPingKA = prefs.getBoolean("chkWifiPingKA", false);
	}
	public void onDestroy(){
		if( mWifiLock != null && mWifiLock.isHeld() ) {
			mWifiLock.release() ;
		}
		if( mWifiLockHighperf != null && mWifiLockHighperf.isHeld() ) {
			mWifiLockHighperf.release() ;
		}
		if( pingThread != null ) {
			pingThreadStop() ;
		}
		
		this.unregisterReceiver(this.batteryInfoReceiver);
		this.unregisterReceiver(this.mConnReceiver);
		
		super.onDestroy();
	}
	
	
	private boolean isWifiConnected() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mWifi.isConnected()) {
			return true ;
		}
		else{
			return false ;
		}
	}
	private int getWifiGateway() {
		DhcpInfo dhcpInfo = ((WifiManager)getSystemService(Context.WIFI_SERVICE)).getDhcpInfo() ;
		if( dhcpInfo == null ) {
			return 0 ;
		}
		return dhcpInfo.gateway ;
	}
	
	private synchronized void updateWifilockStateWifi() {
		boolean newState = ( isWirelessOn ) ;
		if( newState == currentStateWifilock ) {
			return ;
		}
		
		currentStateWifilock = newState ;
		
		if( mWifiLock == null ) {
			mWifiLock = ((WifiManager)getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL,"WifiLock") ;
		}
		
		if( currentStateWifilock ) {
			if( chkWifiLock ) {
				if( mWifiLock != null && !mWifiLock.isHeld() ) {
					mWifiLock.acquire() ;
				}
				if( chkWifiPingKA ) {
					pingThreadStart() ;
				}
			}
		}
		if( !currentStateWifilock ) {
			if( chkWifiLock ) {
				if( mWifiLock != null && mWifiLock.isHeld() ) {
					mWifiLock.release() ;
				}
				if( chkWifiPingKA ) {
					pingThreadStop() ;
				}
			}
		}
	}
	private synchronized void updateWifilockStateHighperf() {
		boolean newState = ( isPlugged && isWirelessOn ) ;
		if( newState == currentStateWifilockHighperf ) {
			return ;
		}
		
		currentStateWifilockHighperf = newState ;
		
		if( mWifiLockHighperf == null ) {
			mWifiLockHighperf = ((WifiManager)getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,"WifiLockHighPerf") ;
		}
		
		if( currentStateWifilockHighperf && chkHighPerf ) {
			if( mWifiLockHighperf != null && !mWifiLockHighperf.isHeld() ) {
				mWifiLockHighperf.acquire() ;
			}
		}
		if( !currentStateWifilockHighperf && chkHighPerf ) {
			if( mWifiLockHighperf != null && mWifiLockHighperf.isHeld() ) {
				mWifiLockHighperf.release() ;
			}
		}
	}
	
	
	private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {

	        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
	        isPlugged = ( plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB );
	        
	        
        	updateWifilockStateWifi() ;
        	updateWifilockStateHighperf() ;
		}

	};
	
	
	
	private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	isWirelessOn = isWifiConnected() ;
        	wirelessGatewayIp = getWifiGateway() ;
        	
        	
        	updateWifilockStateWifi() ;
        	updateWifilockStateHighperf() ;
        }
    };
    
    
	private void pingThreadStart() {
		if( pingThread != null && pingThread.isAlive() ) {
			return ;
		}
		
		pingThread = new Thread(){
	        public void run() {
	        	if( wirelessGatewayIp == 0 ) {
	        		return ;
	        	}
	        	
	        	InetAddress address ;
	        	try {
					address = InetAddress.getByName(intToIp(wirelessGatewayIp));
					// Log.w(TAG,address.getCanonicalHostName()) ;
				} catch (UnknownHostException e1) {
					Log.w(TAG,"Error !") ;
					return ;
				}
	        	
	        	while(true) {
	        		if( currentThread().isInterrupted() ){
	        			break ;
	        		}
	        		
	        		try {
						address.isReachable(1000) ;
					} catch (IOException e1) {
						return ;
					}
	        		
	        		try{
	        			Thread.sleep(60*1000) ;
	        		}
	        		catch( InterruptedException e ){
	        			break ;
	        		}
	        	}
	        };
		};
		pingThread.start() ;
	}
	private void pingThreadStop() {
		if( pingThread != null && pingThread.isAlive() ) {
			pingThread.interrupt() ;
			pingThread = null ;
		}
	}

	public String intToIp(int i) {

		return ( i & 0xFF ) + "." +
				((i >> 8 ) & 0xFF) + "." +
				((i >> 16 ) & 0xFF) + "." +
				( (i >> 24 ) & 0xFF) ;
	}
}
