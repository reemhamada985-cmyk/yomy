package com.yawmiyati.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    static final int REQ_NOTIFICATIONS = 7001;
    WebView webView;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebViewClient(new WebViewClient(){
            @Override public void onPageFinished(WebView v, String url){
                v.postDelayed(() -> requestNotificationPermission(), 1200);
            }
        });
        webView.addJavascriptInterface(new NativeBridge(this), "Android");
        webView.loadUrl("file:///android_asset/index.html");
        setContentView(webView);
    }

    void requestNotificationPermission(){
        if(Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATIONS);
        }
    }

    public static void scheduleOne(Context ctx, String name, String iso, int reminderMinutes, boolean enabled){
        if(!enabled) return;
        try {
            long at = java.time.Instant.parse(iso).toEpochMilli();
            if(reminderMinutes > 0) at -= reminderMinutes * 60_000L;
            if(at <= System.currentTimeMillis()) return;
            AlarmManager am=(AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
            Intent i=new Intent(ctx, PrayerAlarmReceiver.class).putExtra("name",name).putExtra("adhan", reminderMinutes==0);
            int id=(name+iso).hashCode();
            PendingIntent pi=PendingIntent.getBroadcast(ctx,id,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            if(Build.VERSION.SDK_INT >= 23){
                try{ am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi); }
                catch(SecurityException e){ am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi); }
            }else am.setExact(AlarmManager.RTC_WAKEUP,at,pi);
        }catch(Exception e){ }
    }

    public static class NativeBridge {
        final MainActivity a;
        NativeBridge(MainActivity a){this.a=a;}
        @JavascriptInterface public void requestNotificationPermission(){a.runOnUiThread(a::requestNotificationPermission);}
        @JavascriptInterface public void requestExactAlarmAccess(){
            if(Build.VERSION.SDK_INT >= 31){
                try { Intent i=new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:"+a.getPackageName())); a.startActivity(i); }
                catch(Exception e){ Toast.makeText(a,"افتح إعدادات المنبهات للتطبيق",Toast.LENGTH_LONG).show(); }
            }
        }
        @JavascriptInterface public void schedulePrayerAlarm(String name,String iso,String json){
            try{ JSONObject o=new JSONObject(json); boolean enabled=o.optBoolean("enabled",true); int rem=o.optInt("reminderMinutes",0); scheduleOne(a,name,iso,rem,enabled); }
            catch(Exception ignored){}
        }
        @JavascriptInterface public void schedulePrayerAlarms(String json){
            try{
                JSONArray arr=new JSONArray(json);
                for(int i=0;i<arr.length();i++){
                    JSONObject o=arr.getJSONObject(i);
                    String name=o.optString("name");
                    int minutes=o.optInt("minutes",0);
                    // Web-side scheduler remains the source of exact date/time. This method is a safety hook.
                }
            }catch(Exception ignored){}
        }
        @JavascriptInterface public void setCity(String city){ /* local web state remains authoritative */ }
    }
}
