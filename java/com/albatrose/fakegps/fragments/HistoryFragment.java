package com.albatrose.fakegps.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.albatrose.fakegps.MainActivity;
import com.albatrose.fakegps.R;
import com.albatrose.fakegps.data.AppDatabase;
import com.albatrose.fakegps.data.Location;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HistoryFragment extends Fragment {

    private LinearLayout locationContainer;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    View currentCard;
    View lastCard;
    LatLng fakelocation;

    public HistoryFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationContainer = view.findViewById(R.id.locationContainer);
        db = AppDatabase.getDatabase(getActivity());

        getLocationCards();
    }

    @Override
    public void onResume() {
        super.onResume();

        locationContainer.removeAllViews();
        getLocationCards();
        ((MainActivity) requireActivity()).configureFab(true);
        ((MainActivity) requireActivity()).setFabClickListener(() -> {
            if(currentCard != null){
                double lat = Double.parseDouble(((TextView) currentCard.findViewById(R.id.textLat)).getText().toString());
                double lng = Double.parseDouble(((TextView) currentCard.findViewById(R.id.textLng)).getText().toString());
                LatLng fakelocation = new LatLng(lat, lng);
                MainActivity.setFake(fakelocation);
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) requireActivity()).setFabClickListener(null); // FAB işlevini temizle
    }

    private void getLocationCards() {
        executorService.execute(() -> {
            final List<Location> locations = db.locationDAO().getAllLocations();
            Collections.reverse(locations);

            getActivity().runOnUiThread(() -> {
                LayoutInflater inflater = LayoutInflater.from(getActivity());

                for (final Location l : locations) {
                    View cardView = inflater.inflate(R.layout.location_card, locationContainer, false);

                    TextView textViewTitle = cardView.findViewById(R.id.textTitle);
                    TextView textViewLat = cardView.findViewById(R.id.textLat);
                    TextView textViewLng = cardView.findViewById(R.id.textLng);
                    TextView textViewTimestamp = cardView.findViewById(R.id.textTimestamp);
                    ImageButton editButton = cardView.findViewById(R.id.buttonEdit);
                    ImageButton deleteButton = cardView.findViewById(R.id.buttonDelete);
                    ImageView viewSelectedOverlay = cardView.findViewById(R.id.viewSelectedOverlay);

                    textViewTitle.setText(l.getTitle());
                    textViewLat.setText(String.valueOf(l.getLat()));
                    textViewLng.setText(String.valueOf(l.getLng()));
                    textViewTimestamp.setText(l.getTimestamp());

                    viewSelectedOverlay.setVisibility(View.GONE);

                    cardView.setOnClickListener(v -> onCardClick(cardView));
                    editButton.setOnClickListener(v-> onEditClick(textViewTitle));
                    deleteButton.setOnClickListener(v-> onDeleteClick(l, cardView));


                    locationContainer.addView(cardView);
                }
            });
        });
    }

    private void onCardClick(View card){
        currentCard = card;
        currentCard.findViewById(R.id.viewSelectedOverlay).setVisibility(View.VISIBLE);
        if(lastCard != null){
            lastCard.findViewById(R.id.viewSelectedOverlay).setVisibility(View.GONE);
        }
        lastCard = lastCard == currentCard? null : currentCard;
    }

    private void onEditClick(TextView textTitle) {
        // Mevcut başlığı alıyoruz
        String currentTitle = textTitle.getText().toString();

        // Yeni bir AlertDialog oluştur
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Başlık Düzenle");

        // EditText oluştur ve mevcut başlığı içine yaz
        final EditText input = new EditText(getActivity());
        input.setText(currentTitle);
        builder.setView(input);

        // "Kaydet" butonuna tıklanınca yapılacaklar
        builder.setPositiveButton("Kaydet", (dialog, which) -> {
            String newTitle = input.getText().toString().replaceAll("\n", "").replaceAll(" ","");
            if (!newTitle.isEmpty() && !newTitle.equals(currentTitle)) {
                // TextView'i güncelle
                textTitle.setText(newTitle);

                // Veritabanında başlığı güncelle
                executorService.execute(() -> {
                    db.locationDAO().updateTitleByLatLng(currentTitle, newTitle);

                    getActivity().runOnUiThread(() -> {
                        // UI Güncelleme işlemleri burada yapılabilir (gerekirse)
                    });
                });
            }
        });

        // "İptal" butonuna tıklanınca dialogu kapat
        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());

        // Dialogu göster
        builder.show();
    }


    private void onDeleteClick(Location l, View card){
        executorService.execute(() -> {
            db.locationDAO().deleteLocation(l);

            // UI'den kaldır
            getActivity().runOnUiThread(() -> locationContainer.removeView(card));
        });
    }
}