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
        // Load or refresh data every time the activity is shown
        // We now fetch from server to ensure sync
        fetchHotelsFromServer();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hotel Database (Synced)");
        }
        // Handle the back arrow click
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
        // Corrected adapter constructor: (List<Hotel>, OnHotelClickListener)
        hotelAdapter = new HotelAdapter(hotelList, this);
        recyclerView.setAdapter(hotelAdapter);
    }

    private void setupListeners() {
        addButton.setOnClickListener(v -> showAddHotelFragment());
    }

    /**
     * Fetches all hotels from the Flask server and syncs local DB.
     */
    private void fetchHotelsFromServer() {
        // Show a loading indicator if possible, or just toast
        // Toast.makeText(this, "Syncing...", Toast.LENGTH_SHORT).show();

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
                                    hotelJson.getString("checkInDate"),
                                    hotelJson.getBoolean("available"),
                                    hotelJson.optString("roomType", "Standard")
                            );
                            
                            // --- Image Parsing Logic ---
                            String imageString = hotelJson.optString("image");
                            if (imageString != null && !imageString.isEmpty()) {
                                if (imageString.startsWith("http")) {
                                    // It's a full URL
                                    hotel.setImageUrl(imageString);
                                } else if (imageString.startsWith("/")) {
                                    // It's a relative URL (e.g., /static/img.jpg)
                                    // Prepend the Server URL (http://172.31.239.130:5000)
                                    hotel.setImageUrl(ApiConfig.SERVER_URL + imageString);
                                } else {
                                    // Assume it's Base64
                                    try {
                                        String base64Data = imageString;
                                        // Handle data URI scheme if present (e.g. "data:image/jpeg;base64,...")
                                        if (base64Data.contains(",")) {
                                            base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                                        }
                                        
                                        byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
                                        hotel.setImage(decodedString);
                                    } catch (IllegalArgumentException e) {
                                        // Not a valid Base64 string
                                        Log.e("Activity3", "Invalid Base64 string for image: " + imageString);
                                    }
                                }
                            }
                            
                            networkHotels.add(hotel);
                        }

                        // Synchronize local database
                        synchronizeLocalDatabase(networkHotels);

                    } catch (JSONException e) {
                        Log.e("Activity3", "JSON Parsing Error", e);
                        Toast.makeText(this, "Error parsing server data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Activity3", "Network Error", error);
                    Toast.makeText(this, "Server unreachable. Showing local data.", Toast.LENGTH_SHORT).show();
                    // Fallback to local data if server fails
                    loadHotelsFromLocalDatabase();
                }
        );
        requestQueue.add(request);
    }

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
     * Loads all hotels from the local database and updates the UI.
     */
    private void loadHotelsFromLocalDatabase() {
        hotelList.clear();
        hotelList.addAll(dbHelper.getAllHotels());
        hotelAdapter.updateList(hotelList);

        if (hotelList.isEmpty()) {
            Toast.makeText(this, "No hotels found.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays the form fragment for adding a new hotel.
     */
    private void showAddHotelFragment() {
        HotelFormFragment fragment = HotelFormFragment.newInstance(null); // 'null' for create mode
        fragment.setOnHotelSavedListener(() -> onHotelSaved(null));
        showFormFragment(fragment);
    }

    /**
     * Displays the form fragment for editing an existing hotel.
     */
    private void showEditHotelFragment(Hotel hotel) {
        HotelFormFragment fragment = HotelFormFragment.newInstance(hotel); // Pass hotel for edit mode
        fragment.setOnHotelSavedListener(() -> onHotelSaved(hotel));
        showFormFragment(fragment);
    }

    /**
     * Generic method to show a fragment and manage UI visibility.
     */
    private void showFormFragment(HotelFormFragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        // Show the fragment container and hide the main list view
        fragmentContainer.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
    }

    /**
     * Callback method for when a hotel is saved (created or updated).
     * @param originalHotel The hotel that was edited, or null if it was a new creation.
     */
    private void onHotelSaved(Hotel originalHotel) {
        // The fragment has already saved to the local DB. Now we sync to server.
        
        if (originalHotel == null) {
            // New hotel creation
            Hotel newHotel = dbHelper.getLastAddedHotel();
            if (newHotel != null) {
                createHotelOnServer(newHotel);
            }
        } else {
            // Update existing hotel
            // Get the updated version from DB
            Hotel updatedHotel = dbHelper.getHotel(originalHotel.getId());
            if (updatedHotel != null) {
                updateHotelOnServer(updatedHotel);
            }
        }

        // Restore the main list view
        getSupportFragmentManager().popBackStack(); // Close the fragment
        fragmentContainer.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);
    }
    
    private void createHotelOnServer(Hotel hotel) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", hotel.getName());
            jsonBody.put("location", hotel.getLocation());
            jsonBody.put("rating", hotel.getRating());
            jsonBody.put("price", hotel.getPrice());
            jsonBody.put("checkInDate", hotel.getCheckInDate());
            jsonBody.put("available", hotel.isAvailable());
            jsonBody.put("roomType", hotel.getRoomType());
            
            // Send Base64 image if available locally
            if (hotel.getImage() != null && hotel.getImage().length > 0) {
                 String encodedImage = Base64.encodeToString(hotel.getImage(), Base64.DEFAULT);
                 jsonBody.put("image", encodedImage);
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ApiConfig.HOTELS_ENDPOINT, jsonBody,
                    response -> {
                        Toast.makeText(this, "Synced to server!", Toast.LENGTH_SHORT).show();
                        // Re-fetch to get the server IDs correct
                        fetchHotelsFromServer();
                    },
                    error -> Toast.makeText(this, "Failed to sync creation to server", Toast.LENGTH_SHORT).show()
            );
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateHotelOnServer(Hotel hotel) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", hotel.getName());
            jsonBody.put("location", hotel.getLocation());
            jsonBody.put("rating", hotel.getRating());
            jsonBody.put("price", hotel.getPrice());
            jsonBody.put("checkInDate", hotel.getCheckInDate());
            jsonBody.put("available", hotel.isAvailable());
            jsonBody.put("roomType", hotel.getRoomType());
            
            // Send Base64 image if available locally (and user updated it)
            if (hotel.getImage() != null && hotel.getImage().length > 0) {
                 String encodedImage = Base64.encodeToString(hotel.getImage(), Base64.DEFAULT);
                 jsonBody.put("image", encodedImage);
            }

            String url = ApiConfig.getHotelUrl(hotel.getId());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonBody,
                    response -> {
                        Toast.makeText(this, "Synced update to server!", Toast.LENGTH_SHORT).show();
                        fetchHotelsFromServer();
                    },
                    error -> Toast.makeText(this, "Failed to sync update to server", Toast.LENGTH_SHORT).show()
            );
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void deleteHotelFromServer(Hotel hotel) {
        String url = ApiConfig.getHotelUrl(hotel.getId());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    Toast.makeText(this, "Deleted from server", Toast.LENGTH_SHORT).show();
                    // Also delete locally
                    dbHelper.deleteHotel(hotel.getId());
                    loadHotelsFromLocalDatabase();
                },
                error -> Toast.makeText(this, "Failed to delete from server", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(request);
    }

    // --- OnHotelClickListener Implementation ---

    @Override
    public void onEditClick(Hotel hotel) {
        // When edit is clicked, open the fragment to edit the selected hotel
        showEditHotelFragment(hotel);
    }

    @Override
    public void onDeleteClick(Hotel hotel) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hotel")
                .setMessage("Are you sure you want to delete '" + hotel.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Try to delete from server first
                    deleteHotelFromServer(hotel);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onItemClick(Hotel hotel) {
        // This method shows the room count in a toast
        List<Room> rooms = dbHelper.getRoomsByHotelId(hotel.getId());
        Toast.makeText(
                this,
                hotel.getName() + " has " + rooms.size() + " rooms in DB",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onBackPressed() {
        // If a fragment is showing, pressing back should close it and show the list.
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            fragmentContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.VISIBLE);
            
            // Re-fetch when coming back from fragment just in case
            fetchHotelsFromServer();
        } else {
            // Otherwise, perform the default back action (exit the activity).
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close(); // Close the database connection to prevent leaks
        }
    }
}
