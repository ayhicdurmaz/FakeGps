package com.albatrose.fakegps.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.albatrose.fakegps.MainActivity;
import com.albatrose.fakegps.R;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Loglama Switch
        Switch switchLogging = view.findViewById(R.id.switch_logging);
        switchLogging.setOnCheckedChangeListener((buttonView, isChecked) -> {

        });

        // Uygulama Kilidi Switch
        Switch switchAppLock = view.findViewById(R.id.switch_app_lock);
        switchAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {

        });

        // Uygulama Tespiti Engelleme Switch
        Switch switchAppDetection = view.findViewById(R.id.switch_app_detection);
        switchAppDetection.setOnCheckedChangeListener((buttonView, isChecked) -> {

        });

        // Otomatik Başlatma Switch
        Switch switchAutostart = view.findViewById(R.id.switch_autostart);
        switchAutostart.setOnCheckedChangeListener((buttonView, isChecked) -> {

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setFabClickListener(null);
        ((MainActivity) requireActivity()).configureFab(false);
    }
}