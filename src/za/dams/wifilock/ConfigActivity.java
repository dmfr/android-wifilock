package za.dams.wifilock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class ConfigActivity extends PreferenceActivity 
	implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private static final String TAG = "ConfigActivity";
	
	private ConfigController mCfgCtrl ;

    
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PreferenceManager.setDefaultValues(this, R.xml.config, false);
        addPreferencesFromResource(R.xml.config);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(this) ;
        
        mCfgCtrl = new ConfigController(this) ;
        
        // Launch service if not running
        if( !MyService.isRunning(getApplication()) ) {
        	startService(new Intent(this, MyService.class)) ;
        }
    }

    
    public void onSharedPreferenceChanged(SharedPreferences pref, String label) {
    	if( MyService.isRunning(getApplication()) ) {
    		stopService(new Intent(this, MyService.class)) ;
    	}
    	startService(new Intent(this, MyService.class)) ;
    }
    
    
    private class ConfigController implements Preference.OnPreferenceClickListener {
    	
    	private CheckBoxPreference chkHighPerf ;
    	private CheckBoxPreference chkWifiLock ;
    	private CheckBoxPreference chkWifiPingKA ;
    	
    	@SuppressWarnings("deprecation")
		public ConfigController( ConfigActivity configActivity ) {
    		chkHighPerf = (CheckBoxPreference) configActivity.findPreference("chkHighPerf") ;
    		chkHighPerf.setOnPreferenceClickListener(this) ;
    		chkWifiLock = (CheckBoxPreference) configActivity.findPreference("chkWifiLock") ;
    		chkWifiLock.setOnPreferenceClickListener(this) ;
    		chkWifiPingKA = (CheckBoxPreference) configActivity.findPreference("chkWifiPingKA") ;
    		chkWifiPingKA.setOnPreferenceClickListener(this) ;
    		
    		setDisableStates() ;
    	}

		public boolean onPreferenceClick(Preference pref) {
			if( pref.getKey().equals("chkWifiLock") ) {
				setDisableStates() ;
			}
			return false;
		}
		
		
		private void setDisableStates() {
			if( chkWifiLock != null && chkWifiPingKA != null ) {
				if( chkWifiLock.isChecked() ) {
					chkWifiPingKA.setEnabled(true) ;
 				}
				else{
					chkWifiPingKA.setEnabled(false) ;
				}
				
			}
 		}
    	
     }

}
