package com.example.hotelbooking;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.adapter.HotelAdapter;
import com.example.hotelbooking.database.DatabaseHelper;
import com.example.hotelbooking.fragment.HotelFormFragment;
import com.example.hotelbooking.model.Hotel;
import com.example.hotelbooking.model.Room;

import java.util.ArrayList;
import java.util.List;

import android.widget.FrameLayout;

public class Activity3 extends AppCompatActivity implements HotelAdapter.OnHotelClickListener {

    private RecyclerView recyclerView;
    private HotelAdapter hotelAdapter;
    private DatabaseHelper dbHelper;
    private List<Hotel> hotelList;
    private Button addButton;
    private FrameLayout fragmentContainer;

    // Show how many rooms are linked to this hotel
    private void showRoomsForHotel(Hotel hotel) {
        List<Room> rooms = dbHelper.getRoomsByHotelId(hotel.getId());

        Toast.makeText(
                this,
                hotel.getName() + " has " + rooms.size() + " rooms in DB",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup back button
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        addButton = findViewById(R.id.addButton);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        hotelList = new ArrayList<>();
        hotelAdapter = new HotelAdapter(hotelList, this);
        recyclerView.setAdapter(hotelAdapter);

        // Load data
        loadHotels();

        // Add button click listener
        addButton.setOnClickListener(v -> showAddHotelFragment());
    }

    private void loadHotels() {
    hotelList = dbHelper.getAllHotels();
    hotelAdapter.updateList(hotelList);

    Toast.makeText(this,
            "Loaded " + hotelList.size() + " hotels from DB",
            Toast.LENGTH_SHORT).show();

    if (hotelList.isEmpty()) {
        Toast.makeText(this, "No hotels found. Add a new hotel!", Toast.LENGTH_SHORT).show();
    }
}

    private void showAddHotelFragment() {
        HotelFormFragment fragment = HotelFormFragment.newInstance(null);
        fragment.setOnHotelSavedListener(this::onHotelSaved);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        // Hide RecyclerView and Add button when fragment is shown

        fragmentContainer.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
    }

    private void showEditHotelFragment(Hotel hotel) {
        HotelFormFragment fragment = HotelFormFragment.newInstance(hotel);
        fragment.setOnHotelSavedListener(this::onHotelSaved);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        // Hide RecyclerView and Add button when fragment is shown
        recyclerView.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
    }

  private void onHotelSaved() {
    // Reload hotels
    loadHotels();

    // Show RecyclerView and Add button again
    fragmentContainer.setVisibility(View.GONE);   // <--
    recyclerView.setVisibility(View.VISIBLE);
    addButton.setVisibility(View.VISIBLE);
}

    @Override
    public void onEditClick(Hotel hotel) {
        // Show number of rooms, then open edit form
        showRoomsForHotel(hotel);
        showEditHotelFragment(hotel);
    }

    @Override
    public void onDeleteClick(Hotel hotel) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hotel")
                .setMessage("Are you sure you want to delete " + hotel.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteHotel(hotel.getId());
                    Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
                    loadHotels();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
public void onBackPressed() {
    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
        getSupportFragmentManager().popBackStack();
        fragmentContainer.setVisibility(View.GONE);   // <--
        recyclerView.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);
    } else {
        super.onBackPressed();
    }
}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}