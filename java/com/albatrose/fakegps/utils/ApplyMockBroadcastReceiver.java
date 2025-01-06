package com.albatrose.fakegps.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.albatrose.fakegps.MainActivity;
import com.google.android.gms.maps.model.LatLng;

public class ApplyMockBroadcastReceiver extends BroadcastReceiver {

    Intent serviceIntent;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;

    public ApplyMockBroadcastReceiver() {
        alarmManager = MainActivity.alarmManager;
        serviceIntent = MainActivity.serviceIntent;
        pendingIntent = MainActivity.pendingIntent;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            LatLng latLng = MainActivity.getFake();
            double lat = latLng.latitude;
            double lng = latLng.longitude;

            MainActivity.exec(lat, lng);

            if (MainActivity.isMocking()) {
                MainActivity.setAlarm();
            } else {
                MainActivity.stopMockingLocation();
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
