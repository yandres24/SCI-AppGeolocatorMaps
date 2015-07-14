package com.sci.www.sci_appgeolocator.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sci.www.sci_appgeolocator.Classes.NotifyManager;
import com.sci.www.sci_appgeolocator.HomeActivity;
import com.sci.www.sci_appgeolocator.MainActivity;
import com.sci.www.sci_appgeolocator.R;

import java.util.Timer;
import java.util.TimerTask;

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

    private Context ctx;
    private Timer mTimer = null;
    private int countRept = 0;
    public String arrayTrama = null;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        this.mTimer = new Timer();
        this.mTimer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        ejecutarTarea();
                        countRept = countRept + 1;
                    }
                }
                , 0, 1000 * 60);
    }

    private void ejecutarTarea(){
        try {
            //TramaGps trama = new TramaGps();
            //arrayTrama = trama.Trama().toString();
            final String[] result = {null};

            LocationManager mLocMan = null;
            GpsStatus mStatus = null;
            LocationManager lLocMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocMan = lLocMan;
            lLocMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
            //lLocMan.addGpsStatusListener(this);
            //lLocMan.addNmeaListener(this);
            lLocMan.addNmeaListener(new GpsStatus.NmeaListener() {
                public void onNmeaReceived(long timestamp, String nmea) {
                    try {
                        result[0] = nmea;
                    } catch (Exception e) {
                        Log.e("TestGps", e.getMessage());
                    }
                }
            });

            Thread t = new Thread(new Runnable() {
                public void run() {
                    NotifyManager notify = new NotifyManager();
                    notify.playNotification(getApplicationContext(),
                            MainActivity.class, "Tienes una notificaci?n"
                            , "Notificaci?n " + arrayTrama, R.drawable.abc_textfield_activated_mtrl_alpha, countRept);
                }
            });
            t.start();
        }
        catch (Exception ex)
        {
            System.out.println("Error " + ex);
        }
    }
}
