package com.albatrose.fakegps.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.albatrose.fakegps.MainActivity;
import com.albatrose.fakegps.data.AppDatabase;
import com.albatrose.fakegps.data.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import com.albatrose.fakegps.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private AppDatabase db;
    private GoogleMap mMap;
    private LatLng fakeLocation;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getDatabase(this.requireContext());

        ((MainActivity) requireActivity()).configureFab(true);

        mapsInit();
        autoComplete();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).configureFab(true);
        ((MainActivity) requireActivity()).setFabClickListener(() -> {
            if(!MainActivity.isMocking()){
                MainActivity.setFake(fakeLocation);
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) requireActivity()).setFabClickListener(null); // FAB işlevini temizle
    }

    private void saveLocationToDatabase(){
        if(fakeLocation != null){
            executorService.execute(() -> {
                Location newLoc = new Location(fakeLocation.latitude,fakeLocation.longitude, getAddressFromLatLng(fakeLocation.latitude, fakeLocation.longitude), getCurrentTimestamp());
                db.locationDAO().addLocation(newLoc);
                Log.d("Room", getCurrentTimestamp() + " - Location added to database");
            });
        }
    }

    @NonNull
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd - HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void mapsInit() {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyBh5_wklnqpUo3LMWZ52DnaCm6ex2vPoMM");
        }

        // Harita fragmentini yükle
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void autoComplete() {
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    // Kullanıcı bir yer seçtiğinde
                    LatLng selectedLocation = place.getLatLng();
                    fakeLocation = selectedLocation;
                    if (selectedLocation != null) {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(selectedLocation).title(place.getName()));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 12));
                    }
                }

                @Override
                public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                    // Hata durumunu işleyin
                    System.out.println("An error occurred: " + status);
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Varsayılan bir konum ayarla (örneğin, İstanbul)
        LatLng defaultLocation = new LatLng(41.0082, 28.9784);
        mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Default Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));

        mMap.setOnMapLongClickListener(this::longClickAction);
    }

    private void longClickAction(LatLng latLng) {
        fakeLocation = latLng;
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = (Address) ((List<?>) addresses).get(0);
                return address.getAddressLine(0); // Adresin tam satırını döndür
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Adres bulunamadı";
    }
}