package com.example.hotelbooking.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hotelbooking.R;
import com.example.hotelbooking.network.ApiConfig;
import com.example.hotelbooking.network.NetworkHotel;

import org.json.JSONException;

/**
 * Fragment for adding/editing hotels via network
 * Makes POST requests to web server using Volley
 */
public class NetworkHotelFormFragment extends Fragment {

    private EditText hotelNameInput;
    private EditText hotelLocationInput;
    private Spinner ratingSpinner;
    private EditText hotelPriceInput;
    private CheckBox availableCheckbox;
    private RadioGroup roomTypeRadioGroup;
    private Button saveButton;
    private Button cancelButton;
    private TextView formTitle;

    private NetworkHotel editingHotel;
    private OnHotelSavedListener listener;
    private RequestQueue requestQueue;

    /**
     * Callback interface for when hotel is saved
     */
    public interface OnHotelSavedListener {
        void onHotelSaved(NetworkHotel hotel);
    }

    /**
     * Create new instance of fragment
     * @param hotel Hotel to edit (null for new hotel)
     */
    public static NetworkHotelFormFragment newInstance(NetworkHotel hotel) {
        NetworkHotelFormFragment fragment = new NetworkHotelFormFragment();

        if (hotel != null) {
            Bundle args = new Bundle();
            args.putInt("hotelId", hotel.getId());
            args.putString("hotelName", hotel.getName());
            args.putString("hotelLocation", hotel.getLocation());
            args.putInt("hotelRating", hotel.getRating());
            args.putDouble("hotelPrice", hotel.getPrice());
            args.putBoolean("available", hotel.isAvailable());
            args.putString("roomType", hotel.getRoomType());
            fragment.setArguments(args);
        }

        return fragment;
    }

    public void setOnHotelSavedListener(OnHotelSavedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_network_hotel_form, container, false);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(getContext());

        initializeViews(view);
        setupRatingSpinner();
        setupListeners();
        loadHotelData();

        return view;
    }

    private void initializeViews(View view) {
        formTitle = view.findViewById(R.id.formTitle);
        hotelNameInput = view.findViewById(R.id.hotelNameInput);
        hotelLocationInput = view.findViewById(R.id.hotelLocationInput);
        ratingSpinner = view.findViewById(R.id.ratingSpinner);
        hotelPriceInput = view.findViewById(R.id.hotelPriceInput);
        availableCheckbox = view.findViewById(R.id.availableCheckbox);
        roomTypeRadioGroup = view.findViewById(R.id.roomTypeRadioGroup);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);
    }

    private void setupRatingSpinner() {
        String[] ratings = {"1 Star", "2 Stars", "3 Stars", "4 Stars", "5 Stars"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                ratings
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ratingSpinner.setAdapter(adapter);
        ratingSpinner.setSelection(4); // Default 5 stars
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveHotelToServer());
        cancelButton.setOnClickListener(v -> closeFragment());
    }

    private void loadHotelData() {
        if (getArguments() != null) {
            int hotelId = getArguments().getInt("hotelId", -1);

            if (hotelId != -1) {
                formTitle.setText("Edit Hotel");

                // Create hotel object with existing data
                editingHotel = new NetworkHotel();
                editingHotel.setId(hotelId);
                editingHotel.setName(getArguments().getString("hotelName"));
                editingHotel.setLocation(getArguments().getString("hotelLocation"));
                editingHotel.setRating(getArguments().getInt("hotelRating"));
                editingHotel.setPrice(getArguments().getDouble("hotelPrice"));
                editingHotel.setAvailable(getArguments().getBoolean("available"));
                editingHotel.setRoomType(getArguments().getString("roomType"));

                // Fill form with data
                hotelNameInput.setText(editingHotel.getName());
                hotelLocationInput.setText(editingHotel.getLocation());
                ratingSpinner.setSelection(editingHotel.getRating() - 1);
                hotelPriceInput.setText(String.valueOf(editingHotel.getPrice()));
                availableCheckbox.setChecked(editingHotel.isAvailable());

                // Set room type radio button
                String roomType = editingHotel.getRoomType();
                if ("Single Room".equals(roomType)) {
                    roomTypeRadioGroup.check(R.id.radioSingle);
                } else if ("Double Room".equals(roomType)) {
                    roomTypeRadioGroup.check(R.id.radioDouble);
                } else if ("Suite".equals(roomType)) {
                    roomTypeRadioGroup.check(R.id.radioSuite);
                }
            }
        }
    }

    /**
     * Save hotel to server using Volley POST request
     * This demonstrates how to send JSON data to PHP backend
     */
    private void saveHotelToServer() {
        // Get form data
        String name = hotelNameInput.getText().toString().trim();
        String location = hotelLocationInput.getText().toString().trim();
        String priceStr = hotelPriceInput.getText().toString().trim();

        // Validate input
        if (name.isEmpty() || location.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Get other form values
        int rating = ratingSpinner.getSelectedItemPosition() + 1;
        double price = Double.parseDouble(priceStr);
        boolean available = availableCheckbox.isChecked();

        int selectedRadioId = roomTypeRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadio = getView().findViewById(selectedRadioId);
        String roomType = selectedRadio.getText().toString();

        // Create NetworkHotel object
        NetworkHotel hotel = new NetworkHotel();
        if (editingHotel != null) {
            hotel.setId(editingHotel.getId());
        }
        hotel.setName(name);
        hotel.setLocation(location);
        hotel.setRating(rating);
        hotel.setPrice(price);
        hotel.setAvailable(available);
        hotel.setRoomType(roomType);
        hotel.setCheckInDate(""); // Not used in this form

        // Disable button during request
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        try {
            // Convert hotel to JSON
            org.json.JSONObject jsonBody = hotel.toJSON();

            // Determine URL (create or update)
            String url = editingHotel != null ? ApiConfig.UPDATE_HOTEL : ApiConfig.CREATE_HOTEL;

            // Create JSON request
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        // Success callback
                        Toast.makeText(getContext(),
                                "Hotel saved to server successfully!",
                                Toast.LENGTH_SHORT).show();

                        // Notify listener
                        if (listener != null) {
                            listener.onHotelSaved(hotel);
                        }

                        closeFragment();
                    },
                    error -> {
                        // Error callback
                        String errorMessage = "Failed to save hotel: ";

                        if (error.networkResponse != null) {
                            errorMessage += "Server error " + error.networkResponse.statusCode;
                        } else if (error.getMessage() != null) {
                            errorMessage += error.getMessage();
                        } else {
                            errorMessage += "Network error";
                        }

                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();

                        // Re-enable button
                        saveButton.setEnabled(true);
                        saveButton.setText("Save to Server");
                    }
            );

            // Add request to queue
            requestQueue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating JSON", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            saveButton.setText("Save to Server");
        }
    }

    private void closeFragment() {
        if (getFragmentManager() != null) {
            getFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cancel any pending requests
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}