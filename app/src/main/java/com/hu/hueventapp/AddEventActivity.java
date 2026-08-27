package com.hu.hueventapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.graphics.Typeface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hu.hueventapp.data.AppDatabase;
import com.hu.hueventapp.data.EventDao;
import com.hu.hueventapp.model.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {
    private static final String TAG = "AddEventActivity";
    
    private TextInputEditText titleInput;
    private TextInputEditText descriptionInput;
    private TextInputEditText locationInput;
    private TextInputEditText organizerInput;
    private MaterialButton dateButton;
    private MaterialButton timeButton;
    private FloatingActionButton submitButton;  // Change from MaterialButton to FloatingActionButton
    private ChipGroup categoryChipGroup;
    private Spinner categorySpinner;
    
    private EventDao eventDao;
    private boolean isEditMode = false;
    private Event event;
    
    private int selectedYear, selectedMonth, selectedDay;
    private int selectedHour, selectedMinute;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_event);
            
            // Setup toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            
            // Initialize database
            try {
                AppDatabase database = AppDatabase.getInstance(this);
                if (database == null) {
                    Log.e(TAG, "Database instance is null");
                    Toast.makeText(this, "Error initializing database", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                eventDao = database.eventDao();
                if (eventDao == null) {
                    Log.e(TAG, "EventDao is null");
                    Toast.makeText(this, "Error accessing database", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                Log.d(TAG, "Database initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Database initialization error", e);
                Toast.makeText(this, "Error initializing database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Initialize views
            titleInput = findViewById(R.id.eventTitleInput);
            descriptionInput = findViewById(R.id.eventDescriptionInput);
            locationInput = findViewById(R.id.eventLocationInput);
            organizerInput = findViewById(R.id.eventOrganizerInput);
            dateButton = findViewById(R.id.dateButton);
            timeButton = findViewById(R.id.timeButton);
            submitButton = findViewById(R.id.fabSaveEvent);
            categoryChipGroup = findViewById(R.id.categoryChipGroup);
            
            // Initialize date and time formats
            dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            
            // Initialize calendar with current date and time
            calendar = Calendar.getInstance();
            selectedYear = calendar.get(Calendar.YEAR);
            selectedMonth = calendar.get(Calendar.MONTH);
            selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
            selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
            selectedMinute = calendar.get(Calendar.MINUTE);
            
            // Setup date button
            dateButton.setOnClickListener(v -> showDatePickerDialog());
            
            // Setup time button
            timeButton.setOnClickListener(v -> showTimePickerDialog());
            
            // Setup submit button
            submitButton.setOnClickListener(v -> saveEvent());
            
            // Check if we're in edit mode
            long eventId = getIntent().getLongExtra("event_id", -1);
            isEditMode = getIntent().getBooleanExtra("is_edit", false);
            
            if (isEditMode && eventId != -1) {
                getSupportActionBar().setTitle("Edit Event");
                loadEvent(eventId);
            } else {
                getSupportActionBar().setTitle("Add Event");
                updateDateButtonText();
                updateTimeButtonText();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void loadEvent(long eventId) {
        try {
            eventDao.getEventById(eventId).observe(this, loadedEvent -> {
                if (loadedEvent != null) {
                    event = loadedEvent;
                    
                    // Populate fields with event data
                    titleInput.setText(event.getTitle());
                    descriptionInput.setText(event.getDescription());
                    locationInput.setText(event.getLocation());
                    organizerInput.setText(event.getOrganizer());
                    
                    // Set date and time
                    calendar.setTime(event.getEventDate());
                    selectedYear = calendar.get(Calendar.YEAR);
                    selectedMonth = calendar.get(Calendar.MONTH);
                    selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
                    selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
                    selectedMinute = calendar.get(Calendar.MINUTE);
                    
                    updateDateButtonText();
                    updateTimeButtonText();
                    
                    // Set category
                    String category = event.getCategory();
                    if (category != null) {
                        for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                            Chip chip = (Chip) categoryChipGroup.getChildAt(i);
                            if (chip.getText().toString().equals(category)) {
                                chip.setChecked(true);
                                break;
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading event", e);
            Toast.makeText(this, "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedYear = year;
                selectedMonth = month;
                selectedDay = dayOfMonth;
                updateDateButtonText();
            },
            selectedYear,
            selectedMonth,
            selectedDay
        );
        datePickerDialog.show();
    }
    
    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            R.style.CustomTimePickerDialog,
            (view, hourOfDay, minute) -> {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                updateTimeButtonText();
            },
            selectedHour,
            selectedMinute,
            false
        );
        
        // Force white background and remove any dark overlay
        if (timePickerDialog.getWindow() != null) {
            timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
            // Remove any dim behind the dialog
            timePickerDialog.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        
        timePickerDialog.show();
    }
    
    private void updateDateButtonText() {
        calendar.set(selectedYear, selectedMonth, selectedDay);
        dateButton.setText(dateFormat.format(calendar.getTime()));
    }
    
    private void updateTimeButtonText() {
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);
        timeButton.setText(timeFormat.format(calendar.getTime()));
    }
    
    private void saveEvent() {
        try {
            String title = titleInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            String organizer = organizerInput.getText().toString().trim();
            
            // Validate inputs
            if (title.isEmpty()) {
                titleInput.setError("Title is required");
                return;
            }
            
            if (description.isEmpty()) {
                descriptionInput.setError("Description is required");
                return;
            }
            
            if (location.isEmpty()) {
                locationInput.setError("Location is required");
                return;
            }
            
            if (organizer.isEmpty()) {
                organizerInput.setError("Organizer is required");
                return;
            }
            
            // Get selected category
            int selectedChipId = categoryChipGroup.getCheckedChipId();
            if (selectedChipId == View.NO_ID) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Chip selectedChip = findViewById(selectedChipId);
            String category = selectedChip.getText().toString();
            
            // Set date and time
            calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
            Date eventDate = calendar.getTime();
            
            // Create or update event
            if (isEditMode && event != null) {
                // Update existing event
                event.setTitle(title);
                event.setDescription(description);
                event.setLocation(location);
                event.setOrganizer(organizer);
                event.setCategory(category);
                event.setEventDate(eventDate);
                
                AppDatabase.getDatabaseExecutor().execute(() -> {
                    try {
                        eventDao.update(event);
                        runOnUiThread(() -> {
                            Toast.makeText(AddEventActivity.this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating event", e);
                        runOnUiThread(() -> {
                            Toast.makeText(AddEventActivity.this, "Error updating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } else {
                // Create new event
                Event newEvent = new Event(title, description, location, category, eventDate, organizer);
                
                AppDatabase.getDatabaseExecutor().execute(() -> {
                    try {
                        long id = eventDao.insert(newEvent);
                        runOnUiThread(() -> {
                            Toast.makeText(AddEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating event", e);
                        runOnUiThread(() -> {
                            Toast.makeText(AddEventActivity.this, "Error creating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving event", e);
            Toast.makeText(this, "Error saving event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}