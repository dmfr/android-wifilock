package za.dams.wifilock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
        if( !MyService.isRunning(context) ) {
        	context.startService(new Intent(context, MyService.class)) ;
        }

	}

}
