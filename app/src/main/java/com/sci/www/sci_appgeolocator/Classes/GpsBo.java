package com.sci.www.sci_appgeolocator.Classes;

import android.location.GpsStatus;
import android.util.Log;

/**
 * Created by Administrador on 03/07/15.
 */
public class GpsBo {
    //Status del Gps
    public int onGpsStatus(int event) {
        Log.e("onGpsStatusChanged", event + "");
        switch (event) {
            case (GpsStatus.GPS_EVENT_SATELLITE_STATUS):
                System.out.println(GpsStatus.GPS_EVENT_SATELLITE_STATUS);
            case GpsStatus.GPS_EVENT_STARTED:
                Log.e("onGpsStatusChanged", "GPS_EVENT_STARTED");
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                Log.e("onGpsStatusChanged", "GPS_EVENT_STOPPED");
                break;
        }
        return event;
    }
}
