package com.example.hotelbooking;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hotelbooking.adapter.HotelAdapter;
import com.example.hotelbooking.database.DatabaseHelper;
import com.example.hotelbooking.fragment.HotelFormFragment;
import com.example.hotelbooking.model.Hotel;
import com.example.hotelbooking.model.Room;
import com.example.hotelbooking.network.ApiConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Activity3 extends AppCompatActivity implements HotelAdapter.OnHotelClickListener {

    // UI Components
    private RecyclerView recyclerView;
    private Button addButton;
    private FrameLayout fragmentContainer;

    // Data and Helpers
    private HotelAdapter hotelAdapter;
    private DatabaseHelper dbHelper;
    private List<Hotel> hotelList;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);

        // Initialize database and networking
        dbHelper = new DatabaseHelper(this);
        requestQueue = Volley.newRequestQueue(this);

        // Setup UI
        setupToolbar();
        initializeViews();
        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load or refresh data from server every time the activity is shown
        fetchHotelsFromServer();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hotel Database (Synced)");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        addButton = findViewById(R.id.addButton);
        fragmentContainer = findViewById(R.id.fragmentContainer);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        hotelList = new ArrayList<>();
        // FIXED: Passing only the 2 arguments required by your Adapter
        hotelAdapter = new HotelAdapter(hotelList, this);
        recyclerView.setAdapter(hotelAdapter);
    }

    private void setupListeners() {
        addButton.setOnClickListener(v -> showAddHotelFragment());
    }

    /**
     * Fetches all hotels from the server, then syncs the local database.
     */
    private void fetchHotelsFromServer() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, ApiConfig.HOTELS_ENDPOINT, null,
                response -> {
                    try {
                        List<Hotel> networkHotels = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject hotelJson = response.getJSONObject(i);
                            Hotel hotel = new Hotel(
                                    hotelJson.optInt("id", 0),
                                    hotelJson.getString("name"),
                                    hotelJson.getString("location"),
                                    hotelJson.optInt("rating", 3),
                                    hotelJson.getDouble("price"),
                                    hotelJson.optString("check_in_date"),
                                    hotelJson.optBoolean("available", true),
                                    hotelJson.optString("room_type", "Standard")
                            );

                            // --- FINAL, ROBUST IMAGE PARSING LOGIC ---
                            String imageUrl = hotelJson.optString("image_url");
                            String imageData = hotelJson.optString("image");

                            if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
                                hotel.setImageUrl(imageUrl);
                            } else if (imageData != null && !imageData.isEmpty()) {
                                try {
                                    // Handle Base64 strings, removing the data URI prefix if present
                                    if (imageData.contains(",")) {
                                        imageData = imageData.substring(imageData.indexOf(",") + 1);
                                    }
                                    byte[] decodedString = Base64.decode(imageData, Base64.DEFAULT);
                                    hotel.setImage(decodedString);
                                } catch (IllegalArgumentException e) {
                                    Log.e("Activity3", "Invalid Base64 string from server for image.", e);
                                }
                            }
                            networkHotels.add(hotel);
                        }
                        synchronizeLocalDatabase(networkHotels);
                    } catch (JSONException e) {
                        Log.e("Activity3", "JSON Parsing Error", e);
                        Toast.makeText(this, "Error parsing server data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Activity3", "Network Error on fetch", error);
                    Toast.makeText(this, "Server unreachable. Showing local data.", Toast.LENGTH_SHORT).show();
                    loadHotelsFromLocalDatabase(); // Fallback to local data on error
                }
        );
        requestQueue.add(request);
    }

    /**
     * Wipes the local DB and refills it with fresh data from the server.
     */
    private void synchronizeLocalDatabase(List<Hotel> networkHotels) {
        new Thread(() -> {
            dbHelper.deleteAllHotels();
            for (Hotel hotel : networkHotels) {
                dbHelper.addHotel(hotel);
            }
            runOnUiThread(this::loadHotelsFromLocalDatabase);
        }).start();
    }

    /**
     * Loads all hotels from the local SQLite DB and updates the RecyclerView.
     */
    private void loadHotelsFromLocalDatabase() {
        hotelList.clear();
        hotelList.addAll(dbHelper.getAllHotels());
        hotelAdapter.updateList(hotelList);
        if (hotelList.isEmpty()) {
            Toast.makeText(this, "No hotels found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddHotelFragment() {
        HotelFormFragment fragment = HotelFormFragment.newInstance(null);
        fragment.setOnHotelSavedListener(() -> onHotelSaved(null));
        showFormFragment(fragment);
    }

    private void showEditHotelFragment(Hotel hotel) {
        HotelFormFragment fragment = HotelFormFragment.newInstance(hotel);
        fragment.setOnHotelSavedListener(() -> onHotelSaved(hotel));
        showFormFragment(fragment);
    }

    private void showFormFragment(HotelFormFragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        toggleFragmentView(true);
    }

    /**
     * Called after a hotel is saved locally in the form fragment.
     * It then triggers the appropriate network request to sync with the server.
     * @param originalHotel The hotel before editing, or null if creating a new one.
     */
    private void onHotelSaved(Hotel originalHotel) {
        // First, close the fragment and show the updated local list
        getSupportFragmentManager().popBackStackImmediate();
        toggleFragmentView(false);
        loadHotelsFromLocalDatabase();

        // Now, perform the network operation in the background
        if (originalHotel == null) {
            // This was a new hotel
            Hotel newHotel = dbHelper.getLastAddedHotel();
            if (newHotel != null) {
                createHotelOnServer(newHotel);
            }
        } else {
            // This was an existing hotel being updated
            Hotel updatedHotel = dbHelper.getHotel(originalHotel.getId());
            if (updatedHotel != null) {
                updateHotelOnServer(updatedHotel);
            }
        }
    }

    /**
     * Sends a new hotel's data (including image) to the server.
     */
    private void createHotelOnServer(Hotel hotel) {
        try {
            JSONObject jsonBody = hotel.toJSONObject(); // Assuming a method to convert Hotel to JSONObject
            // Re-sync from server on success to get the final version of the data
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiConfig.HOTELS_ENDPOINT, jsonBody,
                    response -> {
                        Toast.makeText(this, "New hotel synced to server!", Toast.LENGTH_SHORT).show();
                        fetchHotelsFromServer();
                    },
                    error -> {
                        Log.e("Activity3", "Failed to create on server", error);
                        Toast.makeText(this, "Failed to sync creation to server", Toast.LENGTH_SHORT).show();
                    }
            );
            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e("Activity3", "JSON Error while creating hotel for server", e);
        }
    }

    /**
     * Sends an updated hotel's data (including image) to the server.
     */
    private void updateHotelOnServer(Hotel hotel) {
        try {
            JSONObject jsonBody = hotel.toJSONObject();
            String url = ApiConfig.getHotelUrl(hotel.getId());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonBody,
                    response -> {
                        Toast.makeText(this, "Synced update to server!", Toast.LENGTH_SHORT).show();
                        // No need to fetch again, local data is the source of truth for this action.
                        // The next onResume() will perform a full sync.
                    },
                    error -> {
                        Log.e("Activity3", "Failed to update on server", error);
                        String message = "Update Failed.";
                        if (error.networkResponse != null) {
                            message += " Server responded with status: " + error.networkResponse.statusCode;
                        }
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
            );
            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e("Activity3", "JSON Error while updating hotel for server", e);
        }
    }

    @Override
    public void onEditClick(Hotel hotel) {
        showEditHotelFragment(hotel);
    }

    @Override
    public void onDeleteClick(Hotel hotel) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hotel")
                .setMessage("Are you sure you want to delete '" + hotel.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteHotel(hotel.getId());
                    loadHotelsFromLocalDatabase(); // Immediately update UI from local DB
                    deleteHotelOnServer(hotel); // Sync deletion with server
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteHotelOnServer(Hotel hotel) {
        String url = ApiConfig.getHotelUrl(hotel.getId());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> Toast.makeText(this, "Deletion synced to server.", Toast.LENGTH_SHORT).show(),
                error -> {
                    Log.e("Activity3", "Failed to delete on server", error);
                    Toast.makeText(this, "Failed to sync deletion to server.", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(request);
    }

    @Override
    public void onItemClick(Hotel hotel) {
        List<Room> rooms = dbHelper.getRoomsByHotelId(hotel.getId());
        Toast.makeText(this, hotel.getName() + " has " + rooms.size() + " rooms in DB", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (fragmentContainer.getVisibility() == View.VISIBLE) {
            getSupportFragmentManager().popBackStack();
            toggleFragmentView(false);
        } else {
            super.onBackPressed();
        }
    }

    private void toggleFragmentView(boolean showFragment) {
        fragmentContainer.setVisibility(showFragment ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(showFragment ? View.GONE : View.VISIBLE);
        addButton.setVisibility(showFragment ? View.GONE : View.VISIBLE);
    }
}
