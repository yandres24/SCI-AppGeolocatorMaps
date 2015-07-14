package com.sci.www.sci_appgeolocator.Classes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.sci.www.sci_appgeolocator.R;

/**
 * Created by Administrador on 14/07/15.
 */
public class NotifyManager {
    public void playNotification(Context context, Class<?> cls, String textNotification, String titleNotification, int drawable, int repeticiones) {

        /*NOTIFICATION VARS*/
        NotificationManager mNotificationManager;
        int SIMPLE_NOTIFICATION_ID = 1;
        Notification notifyDetails;
        Uri soundURI = Uri
                .parse("android.resource://course.examples.notification.statusbar/"
                        + R.raw.alarm_rooster);
        /*NOTIFICATION INICIO*/
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyDetails = new Notification(drawable, titleNotification, System.currentTimeMillis());
        long[] vibrate = {0, 200, 200, 300};
        int mNotificationCount = 0;
        notifyDetails.vibrate = vibrate;
        notifyDetails.defaults = Notification.DEFAULT_ALL;
        notifyDetails.flags |= Notification.FLAG_AUTO_CANCEL;
        //notifyDetails.sound = soundURI;
        mNotificationCount = repeticiones;
        /*NOTIFICATION FIN*/
        CharSequence contentTitle = titleNotification;
        CharSequence contentText = textNotification + " (" + mNotificationCount + ")" ;

        Intent notifyIntent = new Intent(context, cls);

        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent intent = PendingIntent.getActivity(context, 0, notifyIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);

        notifyDetails.tickerText = textNotification;
        notifyDetails.setLatestEventInfo(context, contentTitle, contentText, intent);

        try {
            mNotificationManager.notify(SIMPLE_NOTIFICATION_ID, notifyDetails);
        } catch (Exception e) {

        }
    }
}
