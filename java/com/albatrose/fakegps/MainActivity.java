package com.albatrose.fakegps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.albatrose.fakegps.utils.ApplyMockBroadcastReceiver;
import com.albatrose.fakegps.utils.MockLocationProvider;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int SCHEDULE_REQUEST_CODE = 1;
    private static boolean isMocking = false;

    public static boolean isRunning = false;
    private static LatLng fake;
    public OnFabClickListener fabClickListener;
    private FloatingActionButton fab;

    public static Intent serviceIntent;
    public static PendingIntent pendingIntent;
    public static AlarmManager alarmManager;

    public static int timeInterval = 1;
    private static MockLocationProvider mockNetwork;
    private static MockLocationProvider mockGps;

    public static Context context;

    public static LatLng getFake() {
        return fake;
    }

    public static void setFake(LatLng fake) {
        MainActivity.fake = fake;
    }

    protected static void applyLocation(){
        if(fake == null){
            Toast.makeText(context,"Fake location not set", Toast.LENGTH_SHORT).show();
            return;
        }

        double lat = fake.latitude;
        double lng = fake.longitude;

        try {
            mockNetwork = new MockLocationProvider(context, LocationManager.NETWORK_PROVIDER);
            mockGps = new MockLocationProvider(context, LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(context, "Location permission denied.", Toast.LENGTH_SHORT).show();
            stopMockingLocation();
            return;
        }

        if (!isMocking()) {
            Toast.makeText(context, "Fake location started", Toast.LENGTH_SHORT).show();
            setAlarm();
        } else {
            stopMockingLocation();
        }
    }

    public static void exec(double lat, double lng) {
        try {
            mockNetwork.pushLocation(lat, lng);
            mockGps.pushLocation(lat, lng);
        } catch (Exception e) {
            Toast.makeText(context, "Location permission denied.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
    }

    public static void stopMockingLocation() {

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(context, "Fake location stopped", Toast.LENGTH_SHORT).show();
        }

        if (mockNetwork != null)
            mockNetwork.shutdown();
        if (mockGps != null)
            mockGps.shutdown();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        EdgeToEdge.enable(this); // Sistem çubuklarıyla uyumlu bir tasarım sağlamak için
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);

            return;
        }
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 101);
            return;
        }

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            if (fabClickListener != null) {
                fabClickListener.onFabClick(); // Fragment özel işini yapar
            }

            isRunning = !isRunning;
            onFABClick(); // Default iş her zaman çalışır
        });

        // Sistem çubukları için padding ayarlamak
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // TabLayout ve ViewPager2 referansları
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);

        // Adapter ayarla
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // TabLayout ve ViewPager2'yi bağla
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Home");
                    break;
                case 1:
                    tab.setText("History");
                    break;
                case 2:
                    tab.setText("Settings");
                    break;
            }
        }).attach();
    }


    private void onFABClick() {
        if (isRunning) {
            if (!isMocking()) {
                fab.setImageResource(R.drawable.baseline_pause_24);
                Toast.makeText(this, "Fake location started", Toast.LENGTH_SHORT).show();
                applyLocation();
                setMocking(true);
            }
        } else {
            if (isMocking()) {
                fab.setImageResource(R.drawable.baseline_play_arrow_24);
                Toast.makeText(this, "Fake location stopped", Toast.LENGTH_SHORT).show();
                //stopMockingLocation();
                setMocking(false);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMockingLocation();
    }

    @SuppressLint({"ScheduleExactAlarm", "ObsoleteSdkInt"})
    public static void setAlarm() {
        serviceIntent = new Intent(context, ApplyMockBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, SCHEDULE_REQUEST_CODE, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            if (Build.VERSION.SDK_INT >= 19) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, System.currentTimeMillis() + timeInterval * 100L, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis() + timeInterval * 100L, pendingIntent);
                }
            } else {
                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + timeInterval * 100L, pendingIntent);
            }

        } catch (Exception e) {
            Log.e("Exception", "setAlert" + e.getMessage());
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void configureFab(boolean isVisible) {
        FloatingActionButton fab = findViewById(R.id.fab);
        if (isVisible) {
            fab.setVisibility(View.VISIBLE);

        } else {
            fab.setVisibility(View.GONE);
        }
    }

    public void setFabClickListener(OnFabClickListener listener) {
        this.fabClickListener = listener;
    }

    public static boolean isMocking() {
        return isMocking;
    }

    public static void setMocking(boolean mocking) {
        isMocking = mocking;
    }

    public interface OnFabClickListener {
        void onFabClick();
    }
}
