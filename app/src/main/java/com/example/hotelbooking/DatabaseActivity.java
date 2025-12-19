package com.example.hotelbooking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbooking.adapter.HotelAdapter;
import com.example.hotelbooking.database.DatabaseHelper;
import com.example.hotelbooking.fragment.HotelFormFragment;
import com.example.hotelbooking.model.Hotel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseActivity extends AppCompatActivity implements HotelAdapter.OnHotelClickListener {

    // UI Components
    private RecyclerView recyclerView;
    private Button addHotelButton;

    // Data and Helpers
    private HotelAdapter adapter;
    private List<Hotel> hotelList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        // Initialize the DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        setupToolbar();
        initializeViews();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load or refresh the data every time the activity comes into view
        loadHotelsFromDatabase();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hotel Database (Local)");
        }
        // Handle the back arrow click
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        addHotelButton = findViewById(R.id.addHotelButton);
        addHotelButton.setOnClickListener(v -> showHotelForm(null)); // null means create mode
    }

    private void setupRecyclerView() {
        hotelList = new ArrayList<>();
        adapter = new HotelAdapter(hotelList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * This is the core method for getting data. It fetches all hotels
     * from the local SQLite database and updates the RecyclerView.
     */
    private void loadHotelsFromDatabase() {
        // Clear the existing list to avoid duplicates
        hotelList.clear();
        // Get all hotels from the database helper
        List<Hotel> hotelsFromDb = dbHelper.getAllHotels();
        // Add them to our list
        hotelList.addAll(hotelsFromDb);
        // Notify the adapter that the data has changed, so it redraws the list
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "Loaded " + hotelList.size() + " hotels from local DB", Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays the HotelFormFragment, either for creating a new hotel or editing an existing one.
     * @param hotel The hotel to edit, or null to create a new one.
     */
    private void showHotelForm(Hotel hotel) {
        HotelFormFragment fragment = HotelFormFragment.newInstance(hotel);
        // Set a listener for when the save button is clicked in the fragment
        fragment.setOnHotelSavedListener(() -> {
            // After saving, reload the data to show the changes
            loadHotelsFromDatabase();
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment) // Make sure you have a FrameLayout with this ID
                .addToBackStack(null) // Allows user to press back to close the fragment
                .commit();
    }

    // --- Implementation of the OnHotelClickListener interface ---

    @Override
    public void onEditClick(Hotel hotel) {
        // When the edit button is clicked, show the form with the hotel's data
        showHotelForm(hotel);
    }

    @Override
    public void onDeleteClick(Hotel hotel) {
        // Show a confirmation dialog before deleting
        new AlertDialog.Builder(this)
                .setTitle("Delete Hotel")
                .setMessage("Are you sure you want to delete '" + hotel.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteHotel(hotel.getId()); // Delete from database
                    loadHotelsFromDatabase(); // Refresh the list
                    Toast.makeText(this, "Hotel deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onItemClick(Hotel hotel) {
        // Example action: Show a toast with hotel details
        Toast.makeText(this, "You clicked on: " + hotel.getName(), Toast.LENGTH_SHORT).show();

        // You could also navigate to a detailed view activity here
        // Intent intent = new Intent(this, HotelDetailActivity.class);
        // intent.putExtra("HOTEL_ID", hotel.getId());
        // startActivity(intent);
    }
}
