package com.yawmiyati.app;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class BootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c, Intent i){
   // WebView state remains the single source of truth. When the app is next opened,
   // the JS scheduler recreates prayer alarms for the current day.
 }
}
