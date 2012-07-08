package za.dams.wifilock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class ConfigActivity extends Activity {
	
	private static final String TAG = "ConfigActivity";

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        
        // Log.w(TAG,"Start activity");
        /*
        Intent intent = getApplication().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean isPlugged = ( plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB );
        
        String str ;
        
        if( isPlugged ) {
        	str = "Is plugged" ;
        }
        else {
        	str = "Not Plugged" ;
        }
        
        Toast.makeText(getApplication(), str, Toast.LENGTH_LONG).show();*/
        
        if( !MyService.isRunning(getApplication()) ) {
        	Log.w(TAG,"Lauching service") ;
        	startService(new Intent(this, MyService.class)) ;
        }
        else {
        	Log.w(TAG,"Service already running...") ;
        }
        	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_config, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
