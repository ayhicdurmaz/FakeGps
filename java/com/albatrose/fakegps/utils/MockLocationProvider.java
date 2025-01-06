package com.albatrose.fakegps.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.provider.ProviderProperties;
import android.os.Build;
import android.os.SystemClock;

public class MockLocationProvider {

    String providerName;
    Context ctx;

    public MockLocationProvider(Context ctx, String providerName) {
        this.ctx = ctx;
        this.providerName = providerName;

        int powerUsage = 0;
        int accuracy = 5;

        if(Build.VERSION.SDK_INT >= 30) {
            powerUsage = 1;
            accuracy = 2;
        }

        LocationManager lm = (LocationManager)  ctx.getSystemService(Context.LOCATION_SERVICE);
        startup(lm, powerUsage, accuracy, 3, 0);
    }

    private void startup(LocationManager lm, int powerUsage, int accuracy, int maxRetryCount, int currentRetryCount) {
        if(currentRetryCount < maxRetryCount){
            try{
                shutdown();
                lm.addTestProvider(providerName,false,false,false,false,false,true,true,powerUsage,accuracy);
                lm.setTestProviderEnabled(providerName,true);
            }
            catch (Exception e){
                startup(lm, powerUsage,accuracy , maxRetryCount, currentRetryCount);
            }
        }else{
            throw  new SecurityException("Not allowed to perform MOCK_LOCATION");
        }
    }

    public void shutdown(){
        try{
            LocationManager lm = (LocationManager)  ctx.getSystemService(Context.LOCATION_SERVICE);
            lm.removeTestProvider(providerName);
        }
        catch (Exception e){}
    }

    public void pushLocation(double lat, double lng){
        LocationManager lm = (LocationManager)  ctx.getSystemService(Context.LOCATION_SERVICE);

        Location mockLocation = new Location(providerName);
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lng);
        mockLocation.setAltitude(3F);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setSpeed(0.01F);
        mockLocation.setBearing(1F);
        mockLocation.setAccuracy(3F);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            mockLocation.setBearingAccuracyDegrees(0.1F);
            mockLocation.setVerticalAccuracyMeters(0.1F);
            mockLocation.setSpeedAccuracyMetersPerSecond(0.01F);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        lm.setTestProviderLocation(providerName, mockLocation);
    }
}
