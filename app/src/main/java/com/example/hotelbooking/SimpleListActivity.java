package com.example.hotelbooking;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Arrays;

public class SimpleListActivity extends AppCompatActivity {

    private ListView simpleListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize ListView
        simpleListView = findViewById(R.id.simpleListView);

        // Sample hotel data
        ArrayList<String> hotelList = new ArrayList<>(Arrays.asList(
                "Marina Bay Sands - $350/night",
                "Raffles Hotel - $450/night",
                "Pan Pacific Singapore - $280/night",
                "Mandarin Oriental - $320/night",
                "The Fullerton Hotel - $310/night",
                "Shangri-La Hotel - $290/night",
                "Conrad Centennial Singapore - $265/night",
                "Swissôtel The Stamford - $240/night",
                "Grand Park City Hall - $220/night",
                "InterContinental Singapore - $295/night",
                "Four Seasons Hotel - $380/night",
                "The Ritz-Carlton - $420/night",
                "Fairmont Singapore - $275/night",
                "Parkroyal Collection - $255/night",
                "Holiday Inn Singapore - $180/night"
        ));

        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                hotelList
        );

        // Set adapter to ListView
        simpleListView.setAdapter(adapter);

        // Set item click listener
        simpleListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedHotel = hotelList.get(position);
            Toast.makeText(SimpleListActivity.this,
                    "Selected: " + selectedHotel,
                    Toast.LENGTH_SHORT).show();
        });

        // Setup back button
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}