package com.sci.www.sci_appgeolocator.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sci.www.sci_appgeolocator.HomeActivity;
import com.sci.www.sci_appgeolocator.R;

public class Notifications extends Service {
    // Notification ID to allow for future updates
    private static final int MY_NOTIFICATION_ID = 1;

    // Notification Count
    private int mNotificationCount;

    // Notification Text Elements
    private final CharSequence tickerText = "Prueba!";
    private final CharSequence contentTitle = "Prueba Notificacion Geolocalizador";
    private final CharSequence contentText = "Prueba Usted esta siendo notificado.!";

    // Notification Action Elements
    private Intent mNotificationIntent;
    private PendingIntent mContentIntent;

    // Notification Sound and Vibration on Arrival
    private Uri soundURI = Uri
            .parse("android.resource://course.examples.notification.statusbar/"
                    + R.raw.alarm_rooster);
    private long[] mVibratePattern = { 0, 200, 200, 300 };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(getClass().getSimpleName(), "Creating service");
    }


    public int onStartCommand(Intent intent, int flags, int startId)  {
        try {
            super.onStartCommand(intent, flags, startId);
            SendNotification();
            return super.onStartCommand(intent,flags,startId);
        }
        catch(Exception ex)
        {
            return super.onStartCommand(intent,flags,startId);
        }
    }

    public void SendNotification()
    {
        try {
            //Class<HomeActivity> activity = HomeActivity.class;
            //mNotificationIntent = new Intent(getApplicationContext(),
                    //activity);
            //mContentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                    //mNotificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

            Notification.Builder notificationBuilder = new Notification.Builder(
                    getApplicationContext())
                    .setTicker(tickerText)
                    .setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(
                            contentText + " (" + ++mNotificationCount + ")")
                    .setContentIntent(mContentIntent).setSound(soundURI)
                    .setVibrate(mVibratePattern);

            // Pass the Notification to the NotificationManager:
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(MY_NOTIFICATION_ID,
                    notificationBuilder.build());
        }
        catch(Exception ex){
            Log.i(getClass().getSimpleName(), "Creating service");
        }
    }
}
