package com.example.hotelbooking;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView checkInDateText;
    private TextView checkOutDateText;
    private TextView nightsCount;
    private Button decreaseNights;
    private Button increaseNights;
    private Button searchButton;
    private SearchView searchLocation;
    private View hotelsTonightLayout;
    private View cityCard;
    private View calendarIcon;

    private Calendar checkInCalendar;
    private Calendar checkOutCalendar;
    private int nights = 1;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views first
        initializeViews();

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a reference to the ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        dateFormat = new SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault());
        checkInCalendar = Calendar.getInstance();
        checkOutCalendar = Calendar.getInstance();
        checkOutCalendar.add(Calendar.DAY_OF_MONTH, nights);

        setupListeners();
        updateDates();
    }

    private void initializeViews() {
        checkInDateText = findViewById(R.id.checkInDate);
        checkOutDateText = findViewById(R.id.checkOutDate);
        nightsCount = findViewById(R.id.nightsCount);
        decreaseNights = findViewById(R.id.decreaseNights);
        increaseNights = findViewById(R.id.increaseNights);
        searchButton = findViewById(R.id.searchButton);
        searchLocation = findViewById(R.id.searchLocation);
        hotelsTonightLayout = findViewById(R.id.tonightHotelsCard);
        cityCard = findViewById(R.id.cityCard);
        calendarIcon = findViewById(R.id.calendarIcon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Toast.makeText(this, "Menu clicked!", Toast.LENGTH_SHORT).show();
            return true;
        }

        int id = item.getItemId();

        if (id == R.id.menu_list) {
            Intent intent = new Intent(this, SimpleListActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_database) {
            Intent intent = new Intent(this, Activity3.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_settings) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupListeners() {
        // Check-in date picker
        if (calendarIcon != null) {
            calendarIcon.setOnClickListener(v -> showDatePicker());
        }

        if (checkInDateText != null) {
            checkInDateText.setOnClickListener(v -> showDatePicker());
        }

        // Night counter buttons
        if (decreaseNights != null) {
            decreaseNights.setOnClickListener(v -> {
                if (nights > 1) {
                    nights--;
                    updateNightsAndCheckout();
                }
            });
        }

        if (increaseNights != null) {
            increaseNights.setOnClickListener(v -> {
                nights++;
                updateNightsAndCheckout();
            });
        }

        // Search button
        if (searchButton != null) {
            searchButton.setOnClickListener(v -> {
                String query = searchLocation != null ? searchLocation.getQuery().toString() : "";
                Intent intent = new Intent(MainActivity.this, Activity3.class);
                intent.putExtra("searchQuery", query);
                startActivity(intent);
            });
        }

        // Hotels tonight - launches SimpleListActivity
        if (hotelsTonightLayout != null) {
            hotelsTonightLayout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SimpleListActivity.class);
                startActivity(intent);
            });
        }

        // City card click
        if (cityCard != null) {
            cityCard.setOnClickListener(v -> {
                Toast.makeText(this, "Opening Singapore hotels", Toast.LENGTH_SHORT).show();
            });
        }

        // Search view query listener
        if (searchLocation != null) {
            searchLocation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Toast.makeText(MainActivity.this, "Searching: " + query, Toast.LENGTH_SHORT).show();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    checkInCalendar.set(year, month, dayOfMonth);
                    updateDates();
                },
                checkInCalendar.get(Calendar.YEAR),
                checkInCalendar.get(Calendar.MONTH),
                checkInCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateNightsAndCheckout() {
        if (nightsCount != null) {
            nightsCount.setText(String.valueOf(nights));
            checkOutCalendar.setTimeInMillis(checkInCalendar.getTimeInMillis());
            checkOutCalendar.add(Calendar.DAY_OF_MONTH, nights);
            updateDates();
        }
    }

    private void updateDates() {
        if (checkInDateText != null) {
            checkInDateText.setText(dateFormat.format(checkInCalendar.getTime()));
        }
        if (checkOutDateText != null) {
            String checkOutText = "Check-out Date: " + dateFormat.format(checkOutCalendar.getTime());
            checkOutDateText.setText(checkOutText);
        }
    }
}