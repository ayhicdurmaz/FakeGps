package com.albatrose.fakegps.utils;

import static com.albatrose.fakegps.MainActivity.getFake;
import static com.albatrose.fakegps.MainActivity.isMocking;
import static com.albatrose.fakegps.MainActivity.setMocking;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.provider.ProviderProperties;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.albatrose.fakegps.R;
import com.google.android.gms.maps.model.LatLng;

public class GpsMockService extends Service implements LocationListener {

    public static final String MOCK_PROVIDER_NAME = "GpsMockProvider";
    private LocationManager locationManager;
    Thread mock;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if(isMocking()) {
                setMocking(false);
                mock.join();
                locationManager.removeTestProvider(MOCK_PROVIDER_NAME);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final String CHANNEL_ID = "ForegroundServiceChannel";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentText("Fake GPS is on.")
                .setContentTitle("Fake GPS")
                .setSmallIcon(R.drawable.baseline_location_on_24);
        startForeground(1001, notification.build());

        LatLng latLng = getFake();

        try {
            // Mock provider eklenmesi
            locationManager.addTestProvider(MOCK_PROVIDER_NAME, true, true, false, false,
                    true, false, true, ProviderProperties.POWER_USAGE_MEDIUM, ProviderProperties.ACCURACY_FINE);

            locationManager.setTestProviderEnabled(MOCK_PROVIDER_NAME, true);
            locationManager.requestLocationUpdates(MOCK_PROVIDER_NAME, 0L, (float) 0, (LocationListener) this);

            // Konum güncelleme işlemi
            mockLocationUpdates(latLng.latitude, latLng.longitude);

        } catch (SecurityException | IllegalArgumentException e) {
            Log.e("GpsMockManager", "Mock provider eklenemedi: " + e.getMessage());
        }



        return super.onStartCommand(intent, flags, startId);
    }

    private void mockLocationUpdates(double latitude, double longitude) {
        double altitude = 5.0;

        setMocking(true);

        mock = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isMocking()) {
                    Location mockLocation = new Location(MOCK_PROVIDER_NAME);
                    mockLocation.setLatitude(latitude);
                    mockLocation.setLongitude(longitude);
                    mockLocation.setAltitude(altitude);
                    mockLocation.setAccuracy(5.0f); // Set accuracy as a float
                    mockLocation.setTime(System.currentTimeMillis()); // Set timestamp
                    mockLocation.setElapsedRealtimeNanos(System.nanoTime());
                    mockLocation.setBearing(0F);

                    try {
                        locationManager.setTestProviderLocation(MOCK_PROVIDER_NAME, mockLocation);
                        Thread.sleep(200); // Update every 2 seconds
                    } catch (InterruptedException e) {
                        break;
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mock.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.e("Mock", "Mock konum değişti: " + location.getLatitude() + ", " + location.getLongitude());
    }
}
