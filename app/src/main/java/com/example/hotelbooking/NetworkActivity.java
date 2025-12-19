package com.example.hotelbooking;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.example.hotelbooking.adapter.HotelAdapter;
import com.example.hotelbooking.database.DatabaseHelper;
import com.example.hotelbooking.fragment.HotelFormFragment;
import com.example.hotelbooking.model.Hotel;
import com.example.hotelbooking.network.ApiConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NetworkActivity extends AppCompatActivity implements HotelAdapter.OnHotelClickListener {

    // UI Components
    private RecyclerView recyclerView;
    private HotelAdapter adapter; // Use the local adapter
    private Button addButton;
    private Button refreshButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private View fragmentContainer;

    // Data and Networking
    private List<Hotel> hotelList; // This will hold data from the local DB
    private RequestQueue requestQueue;
    private DatabaseHelper dbHelper; // Instance of the local database helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        requestQueue = Volley.newRequestQueue(this);
        dbHelper = new DatabaseHelper(this); // Initialize DatabaseHelper

        setupToolbar();
        initializeViews();
        setupRecyclerView();
        setupListeners();

        // Fetch data from server, which will then update the local DB and UI
        fetchHotelsFromServer();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hotels (Synced with Web)");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        addButton = findViewById(R.id.addButton);
        refreshButton = findViewById(R.id.refreshButton);
        progressBar = findViewById(R.id.progressBar);
        statusText = findViewById(R.id.statusText);
        fragmentContainer = findViewById(R.id.fragmentContainer);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        hotelList = new ArrayList<>();

        // Remove the first 'this' (Context) to match the constructor signature
        adapter = new HotelAdapter(hotelList, this);

        recyclerView.setAdapter(adapter);
    }


    private void setupListeners() {
        addButton.setOnClickListener(v -> showHotelForm(null));
        refreshButton.setOnClickListener(v -> fetchHotelsFromServer());
    }

    /**
     * Step 1: Fetches all hotels from the Flask server.
     */
    private void fetchHotelsFromServer() {
        showLoading(true);
        updateStatus("Syncing with server...");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ApiConfig.GET_ALL_HOTELS, null,
                response -> {
                    try {
                        // Assuming the JSON response is {"success": true, "records": [...]}
                        JSONArray recordsArray = response.getJSONArray("records");
                        List<Hotel> networkHotels = new ArrayList<>();
                        for (int i = 0; i < recordsArray.length(); i++) {
                            JSONObject hotelJson = recordsArray.getJSONObject(i);
                            // Create a local Hotel object from the JSON
                            Hotel hotel = new Hotel(
                                    hotelJson.getInt("id"),
                                    hotelJson.getString("name"),
                                    hotelJson.getString("location"),
                                    hotelJson.getInt("rating"),
                                    hotelJson.getDouble("price"),
                                    null, // checkInDate - not provided by your simplified network model
                                    true, // available - assuming true from network
                                    null  // roomType - not provided by your simplified network model
                            );
                            // In a real app, you would also parse the image URL and save it
                            // hotel.setImageUrl(hotelJson.optString("image_url"));
                            networkHotels.add(hotel);
                        }

                        // Step 2: Synchronize the local database with this new data
                        synchronizeLocalDatabase(networkHotels);

                    } catch (JSONException e) {
                        handleError(new VolleyError("Failed to parse server data.", e));
                    }
                },
                this::handleError // Pass the error directly to our handler
        );
        requestQueue.add(request);
    }

    /**
     * Step 2: Wipes the local database and refills it with fresh data from the server.
     */
    private void synchronizeLocalDatabase(List<Hotel> networkHotels) {
        // Run database operations on a background thread to avoid blocking the UI
        new Thread(() -> {
            dbHelper.deleteAllHotels(); // Clear the local database first
            for (Hotel hotel : networkHotels) {
                dbHelper.addHotel(hotel); // Add each hotel from the server to the local DB
            }
            // After syncing, load data from the local DB to update the UI (on the main thread)
            runOnUiThread(this::loadHotelsFromLocalDatabase);
        }).start();
    }

    /**
     * Step 3: Loads all hotels from the local SQLite database and updates the RecyclerView.
     */
    private void loadHotelsFromLocalDatabase() {
        hotelList.clear();
        hotelList.addAll(dbHelper.getAllHotels());
        adapter.notifyDataSetChanged(); // Refresh the RecyclerView
        showLoading(false);
        updateStatus("Synced " + hotelList.size() + " hotels.");
        Toast.makeText(this, "Data synced with server!", Toast.LENGTH_SHORT).show();
    }

    private void showHotelForm(Hotel hotel) {
        HotelFormFragment fragment = HotelFormFragment.newInstance(hotel);
        fragment.setOnHotelSavedListener(() -> {
            // After any save (create or update), always re-sync with the server
            fetchHotelsFromServer();
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        recyclerView.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
        refreshButton.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onEditClick(Hotel hotel) {
        // Show the form fragment, passing the selected hotel data for editing
        showHotelForm(hotel);
    }

    @Override
    public void onDeleteClick(Hotel hotel) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hotel")
                .setMessage("Are you sure you want to delete '" + hotel.getName() + "'? This will also delete it from the web server.")
                .setPositiveButton("Delete", (dialog, which) -> deleteHotelFromServer(hotel))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteHotelFromServer(Hotel hotel) {
        showLoading(true);
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("id", hotel.getId());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiConfig.DELETE_HOTEL, jsonBody,
                    response -> {
                        Toast.makeText(this, "Hotel deleted from server.", Toast.LENGTH_SHORT).show();
                        fetchHotelsFromServer(); // Re-sync the local DB from the server
                    },
                    this::handleError
            );
            requestQueue.add(request);
        } catch (JSONException e) {
            handleError(new VolleyError("Error creating delete request.", e));
        }
    }

    @Override
    public void onItemClick(Hotel hotel) {
        Toast.makeText(this, "Clicked on local hotel: " + hotel.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            recyclerView.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.VISIBLE);
            refreshButton.setVisibility(View.VISIBLE);
            fragmentContainer.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    // --- Helper Methods ---

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setAlpha(isLoading ? 0.5f : 1.0f);
    }

    private void updateStatus(String message) {
        statusText.setText(message);
    }

    private void handleError(VolleyError error) {
        showLoading(false);
        String errorMessage = "An error occurred";
        if (error.networkResponse != null) {
            errorMessage = "Server Error: " + error.networkResponse.statusCode;
        } else if (error.getMessage() != null) {
            errorMessage = "Request Error: " + error.getMessage();
        } else {
            errorMessage = "An unknown network error occurred.";
        }
        updateStatus(errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e("NetworkActivity", "Volley Error: " + errorMessage, error);
    }
}
