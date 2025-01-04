//package com.albatrose.fakegps.utils;
//
//import static com.albatrose.fakegps.MainActivity.isMocking;
//import static com.albatrose.fakegps.MainActivity.setFake;
//import static com.albatrose.fakegps.MainActivity.setMocking;
//
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.location.provider.ProviderProperties;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//
//import com.google.android.gms.maps.model.LatLng;
//
//public class GpsMockManager implements LocationListener {
//
//    public static final String MOCK_PROVIDER_NAME = "GpsMockProvider";
//
//    private LocationManager locationManager;
//
//
//    Thread mocks;
//
//    public GpsMockManager(LocationManager locationManager) {
//        this.locationManager = locationManager;
//    }
//
//    // Mock GPS provider eklenmesi ve konum güncellemeleri
//    public void createMock(LatLng latLng) {
//        try {
//            // Mock provider eklenmesi
//            locationManager.addTestProvider(MOCK_PROVIDER_NAME, false, false, false, false,
//                    true, false, false, ProviderProperties.POWER_USAGE_MEDIUM, ProviderProperties.ACCURACY_FINE);
//
//            locationManager.setTestProviderEnabled(MOCK_PROVIDER_NAME, true);
//            locationManager.requestLocationUpdates(MOCK_PROVIDER_NAME, 0, 0, this);
//
//            // Konum güncelleme işlemi
//            mockLocationUpdates(latLng.latitude, latLng.longitude);
//
//        } catch (SecurityException | IllegalArgumentException e) {
//            Log.e("GpsMockManager", "Mock provider eklenemedi: " + e.getMessage());
//        }
//    }
//
//    // Mock GPS konum güncellemelerini periyodik olarak yapma
//    private void mockLocationUpdates(double latitude, double longitude) {
//        double altitude = 5.0;
//
//        mocks = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (isMocking()) {
//                    Location mockLocation = new Location(MOCK_PROVIDER_NAME);
//                    mockLocation.setLatitude(latitude);
//                    mockLocation.setLongitude(longitude);
//                    mockLocation.setAltitude(altitude);
//                    mockLocation.setAccuracy(5.0f); // Set accuracy as a float
//                    mockLocation.setTime(System.currentTimeMillis()); // Set timestamp
//                    mockLocation.setElapsedRealtimeNanos(System.nanoTime());
//
//                    try {
//                        locationManager.setTestProviderLocation(MOCK_PROVIDER_NAME, mockLocation);
//                        Thread.sleep(2000); // Update every 2 seconds
//                    } catch (InterruptedException e) {
//                        break;
//                    } catch (SecurityException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        mocks.start();
//    }
//
//    // Mock GPS'yi durdurma
//    public void destroyMock() {
//        if (isMocking()) {
//            setMocking(false);// Konum güncellemelerini durdur
//            try {
//                mocks.join();
//                locationManager.removeTestProvider(MOCK_PROVIDER_NAME);
//            } catch (Exception e) {
//                Log.e("GpsMockManager", "Mock provider kaldırılamadı: " + e.getMessage());
//            }
//        }
//    }
//
//    @Override
//    public void onLocationChanged(@NonNull Location location) {
//        // Konum değiştiğinde log yazdırıyoruz
//        Log.e("Mock", "Mock konum değişti: " + location.getLatitude() + ", " + location.getLongitude());
//    }
//
//    @Override
//    public void onProviderEnabled(@NonNull String provider) {}
//
//    @Override
//    public void onProviderDisabled(@NonNull String provider) {}
//
//}
