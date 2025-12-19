package com.example.hotelbooking.fragment;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.hotelbooking.R;
import com.example.hotelbooking.database.DatabaseHelper;
import com.example.hotelbooking.model.Hotel;
import com.example.hotelbooking.model.Room;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HotelFormFragment extends Fragment {

    private EditText hotelNameInput;
    private EditText hotelLocationInput;
    private Spinner ratingSpinner;
    private EditText hotelPriceInput;
    private TextView checkInDateDisplay;
    private CheckBox availableCheckbox;
    private RadioGroup roomTypeRadioGroup;
    private Button saveButton;
    private Button cancelButton;
    private TextView formTitle;
    private ImageView hotelImageView;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private byte[] selectedImageBytes;

    private DatabaseHelper dbHelper;
    private Calendar checkInCalendar;
    private SimpleDateFormat dateFormat;
    private Hotel editingHotel;
    private OnHotelSavedListener listener;

    public interface OnHotelSavedListener {
        void onHotelSaved();
    }

    public static HotelFormFragment newInstance(Hotel hotel) {
        HotelFormFragment fragment = new HotelFormFragment();
        if (hotel != null) {
            Bundle args = new Bundle();
            args.putInt("hotelId", hotel.getId());
            args.putString("hotelName", hotel.getName());
            args.putString("hotelLocation", hotel.getLocation());
            args.putInt("hotelRating", hotel.getRating());
            args.putDouble("hotelPrice", hotel.getPrice());
            args.putString("checkInDate", hotel.getCheckInDate());
            args.putBoolean("available", hotel.isAvailable());
            args.putString("roomType", hotel.getRoomType());
            args.putByteArray("imageBytes", hotel.getImage());
            fragment.setArguments(args);
        }
        return fragment;
    }

    public void setOnHotelSavedListener(OnHotelSavedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageBytes = getImageBytesFromUri(uri);
                        if (hotelImageView != null && selectedImageBytes != null) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(
                                    selectedImageBytes, 0, selectedImageBytes.length);
                            hotelImageView.setImageBitmap(bitmap);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hotel_form, container, false);

        dbHelper = new DatabaseHelper(getContext());
        dateFormat = new SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault());
        checkInCalendar = Calendar.getInstance();

        initializeViews(view);
        setupRatingSpinner();
        setupListeners(view);
        loadHotelData();

        return view;
    }

    private void initializeViews(View view) {
        formTitle = view.findViewById(R.id.formTitle);
        hotelNameInput = view.findViewById(R.id.hotelNameInput);
        hotelLocationInput = view.findViewById(R.id.hotelLocationInput);
        ratingSpinner = view.findViewById(R.id.ratingSpinner);
        hotelPriceInput = view.findViewById(R.id.hotelPriceInput);
        checkInDateDisplay = view.findViewById(R.id.checkInDateDisplay);
        availableCheckbox = view.findViewById(R.id.availableCheckbox);
        roomTypeRadioGroup = view.findViewById(R.id.roomTypeRadioGroup);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        hotelImageView = view.findViewById(R.id.hotelImageView);
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
        ratingSpinner.setSelection(4);
    }

    private void setupListeners(View view) {
        checkInDateDisplay.setOnClickListener(v -> showDatePicker());
        View calendarButton = view.findViewById(R.id.checkInDateButton);
        if (calendarButton != null) {
            calendarButton.setOnClickListener(v -> showDatePicker());
        }

        saveButton.setOnClickListener(v -> saveHotel());
        cancelButton.setOnClickListener(v -> closeFragment());

        hotelImageView.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void loadHotelData() {
        if (getArguments() != null) {
            int hotelId = getArguments().getInt("hotelId", -1);
            if (hotelId != -1) {
                formTitle.setText(R.string.edit_hotel);
                editingHotel = new Hotel();
                editingHotel.setId(hotelId);
                editingHotel.setName(getArguments().getString("hotelName"));
                editingHotel.setLocation(getArguments().getString("hotelLocation"));
                editingHotel.setRating(getArguments().getInt("hotelRating"));
                editingHotel.setPrice(getArguments().getDouble("hotelPrice"));
                editingHotel.setCheckInDate(getArguments().getString("checkInDate"));
                editingHotel.setAvailable(getArguments().getBoolean("available"));
                editingHotel.setRoomType(getArguments().getString("roomType"));

                hotelNameInput.setText(editingHotel.getName());
                hotelLocationInput.setText(editingHotel.getLocation());
                ratingSpinner.setSelection(editingHotel.getRating() - 1);
                hotelPriceInput.setText(String.valueOf(editingHotel.getPrice()));
                checkInDateDisplay.setText(editingHotel.getCheckInDate());
                availableCheckbox.setChecked(editingHotel.isAvailable());

                // Load the image if it exists
                byte[] imageBytes = getArguments().getByteArray("imageBytes");
                if (imageBytes != null && imageBytes.length > 0) {
                    selectedImageBytes = imageBytes;
                    editingHotel.setImage(imageBytes);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    hotelImageView.setImageBitmap(bitmap);
                }

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

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    checkInCalendar.set(year, month, dayOfMonth);
                    checkInDateDisplay.setText(dateFormat.format(checkInCalendar.getTime()));
                },
                checkInCalendar.get(Calendar.YEAR),
                checkInCalendar.get(Calendar.MONTH),
                checkInCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveHotel() {
        String name = hotelNameInput.getText().toString().trim();
        String location = hotelLocationInput.getText().toString().trim();
        String priceStr = hotelPriceInput.getText().toString().trim();
        String checkInDate = checkInDateDisplay.getText().toString();

        if (name.isEmpty() || location.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        int rating = ratingSpinner.getSelectedItemPosition() + 1;
        double price = Double.parseDouble(priceStr);
        boolean available = availableCheckbox.isChecked();

        int selectedRadioId = roomTypeRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadio = getView().findViewById(selectedRadioId);
        String roomType = selectedRadio.getText().toString();

        if (editingHotel != null) {
            // Update existing hotel
            editingHotel.setName(name);
            editingHotel.setLocation(location);
            editingHotel.setRating(rating);
            editingHotel.setPrice(price);
            editingHotel.setCheckInDate(checkInDate);
            editingHotel.setAvailable(available);
            editingHotel.setRoomType(roomType);

            if (selectedImageBytes != null) {
                editingHotel.setImage(selectedImageBytes);
            }

            int rowsAffected = dbHelper.updateHotel(editingHotel);
            if (rowsAffected > 0) {
                Toast.makeText(getContext(), "Hotel updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error updating hotel", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Create new hotel
            Hotel hotel = new Hotel(name, location, rating, price, checkInDate, available, roomType);

            if (selectedImageBytes != null) {
                hotel.setImage(selectedImageBytes);
            }

            long hotelId = dbHelper.addHotel(hotel);

            if (hotelId == -1) {
                Toast.makeText(getContext(), "Error: hotel NOT saved to database", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a default room for this hotel
            Room room = new Room(
                    (int) hotelId,
                    "101",
                    roomType,
                    2,
                    true,
                    0.0
            );

            dbHelper.addRoom(room);
            Toast.makeText(getContext(), R.string.save_success, Toast.LENGTH_SHORT).show();
        }

        if (listener != null) {
            listener.onHotelSaved();
        }

        closeFragment();
    }

    private byte[] getImageBytesFromUri(Uri imageUri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            inputStream.close();
            return byteBuffer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            return null;
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
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}