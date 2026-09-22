package com.yawmiyati.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import androidx.core.app.NotificationCompat;

public class PrayerAlarmReceiver extends BroadcastReceiver {
    static final String CHANNEL="prayer_times";
    @Override public void onReceive(Context context, Intent intent){
        String name=intent.getStringExtra("name");
        if(name==null) name="الصلاة";
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(android.os.Build.VERSION.SDK_INT >= 26){
            Uri sound=Uri.parse("android.resource://"+context.getPackageName()+"/raw/notification");
            AudioAttributes aa=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            NotificationChannel ch=new NotificationChannel(CHANNEL,"تنبيهات الصلاة",NotificationManager.IMPORTANCE_HIGH);
            ch.setSound(sound,aa); ch.enableVibration(true); nm.createNotificationChannel(ch);
        }
        Intent open=new Intent(context,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(context,1001,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b=new NotificationCompat.Builder(context,CHANNEL)
            .setSmallIcon(com.yawmiyati.app.R.mipmap.ic_launcher)
            .setContentTitle("يومي — حان وقت "+name)
            .setContentText("حان الآن وقت صلاة "+name+".")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi);
        nm.notify((name+System.currentTimeMillis()).hashCode(),b.build());
    }
}
