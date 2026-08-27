package com.hu.hueventapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hu.hueventapp.data.AppDatabase;
import com.hu.hueventapp.data.EventDao;
import com.hu.hueventapp.model.Event;
import com.hu.hueventapp.receiver.EventReminderReceiver;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.widget.Toast;
public class EventDetailActivity extends BaseActivity {
    private EventDao eventDao;
    private Event event;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Set back button
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

            // Initialize database
            try {
                AppDatabase database = AppDatabase.getInstance(this);
                if (database == null) {
                    showCustomToast("Error initializing database", false);
                    finish();
                    return;
                }
                eventDao = database.eventDao();
                if (eventDao == null) {
                    showCustomToast("Error accessing database", false);
                    finish();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showCustomToast("Error initializing database: " + e.getMessage(), false);
                finish();
                return;
            }

            // Initialize AlarmManager
            try {
                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager == null) {
                    showCustomToast("Error initializing alarm manager", false);
                    finish();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showCustomToast("Error initializing alarm manager: " + e.getMessage(), false);
                finish();
                return;
            }

            // Get event ID from intent
            long eventId = getIntent().getLongExtra("event_id", -1);
            if (eventId == -1) {
                showCustomToast("Invalid event ID", false);
                finish();
                return;
            }

            // Load event details
            try {
                eventDao.getEventById(eventId).observe(this, event -> {
                    if (event == null) {
                        showCustomToast("Event not found", false);
                        finish();
                        return;
                    }
                    displayEventDetails(event);
                });
            } catch (Exception e) {
                e.printStackTrace();
                showCustomToast("Error loading event details: " + e.getMessage(), false);
                finish();
                return;
            }

            // Setup reminder FAB
            FloatingActionButton fabSetReminder = findViewById(R.id.fabSetReminder);
            if (fabSetReminder != null) {
                fabSetReminder.setOnClickListener(v -> {
                    if (checkAlarmPermission()) {
                        showReminderDialog();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            showCustomToast("Error loading event details", false);
            finish();
        }
    }

    private boolean checkAlarmPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                    new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("This app needs permission to set exact alarms for event reminders. Please grant the permission in settings.")
                        .setPositiveButton("Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            showCustomToast("Error checking alarm permission", false);
            return false;
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_event_detail;
    }
    
    private void displayEventDetails(Event event) {
        if (event == null) {
            showCustomToast("Event not found", false);
            finish();
            return;
        }

        this.event = event;

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(event.getTitle());
        }

        // Update TextViews
        TextView titleText = findViewById(R.id.eventTitle);
        TextView descriptionText = findViewById(R.id.eventDescription);
        TextView locationText = findViewById(R.id.eventLocation);
        TextView organizerText = findViewById(R.id.eventOrganizer);
        TextView dateText = findViewById(R.id.eventDate);
        TextView categoryText = findViewById(R.id.eventCategory);
        TextView statusText = findViewById(R.id.eventStatus);

        if (titleText != null) titleText.setText(event.getTitle());
        if (descriptionText != null) descriptionText.setText(event.getDescription());
        if (locationText != null) locationText.setText(event.getLocation());
        if (organizerText != null) organizerText.setText(event.getOrganizer());
        if (dateText != null) dateText.setText(dateFormat.format(event.getEventDate()));
        if (categoryText != null) categoryText.setText(event.getCategory());
        
        // Set event status (Active/Expired)
        if (statusText != null) {
            boolean isActive = isEventActive(event);
            statusText.setText(isActive ? "Active" : "Expired");
            statusText.setTextColor(getResources().getColor(
                isActive ? R.color.colorActive : R.color.colorExpired));
            statusText.setVisibility(View.VISIBLE);
        }
    }

    private void showReminderDialog() {
        try {
            if (event == null) {
                showCustomToast("Event information not available", false);
                return;
            }
            
            if (event.getEventDate() == null) {
                Toast.makeText(this, "Event date not available", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check if event is in the future
            if (!isEventActive(event)) {
                Toast.makeText(this, "Cannot set reminder for expired events", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(event.getEventDate());
            calendar.add(Calendar.HOUR_OF_DAY, -1); // Default to 1 hour before

            try {
                // Create TimePickerDialog with our custom style to ensure pure white background
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    R.style.CustomTimePickerDialog,
                    (view, hourOfDay, minute) -> {
                        try {
                            Calendar reminderTime = Calendar.getInstance();
                            reminderTime.setTime(event.getEventDate());
                            reminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            reminderTime.set(Calendar.MINUTE, minute);

                            if (reminderTime.before(Calendar.getInstance())) {
                                Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            setReminder(reminderTime.getTime());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error processing selected time: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                );
                
                // Force white background and remove any dark overlay
                if (timePickerDialog.getWindow() != null) {
                    timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
                    // Remove any dim behind the dialog
                    timePickerDialog.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                }

                timePickerDialog.setTitle("Set Reminder Time");
                timePickerDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating time picker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error showing reminder dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setReminder(Date reminderTime) {
        try {
            if (event == null || alarmManager == null) return;

            // Create intent for the alarm
            Intent intent = new Intent(this, EventReminderReceiver.class);
            intent.putExtra("event_id", event.getId());
            intent.putExtra("event_title", event.getTitle());
            intent.putExtra("event_time", event.getEventDate().getTime());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) event.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Schedule the alarm
            boolean alarmSet = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime.getTime(),
                        pendingIntent
                    );
                    alarmSet = true;
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime.getTime(),
                    pendingIntent
                );
                alarmSet = true;
            }

            if (alarmSet) {
                // Update event in database with reminder information
                // Use a background thread for database operations
                new Thread(() -> {
                    try {
                        // Make sure we're using the latest event data
                        Event currentEvent = eventDao.getEventByIdSync(event.getId());
                        if (currentEvent != null) {
                            // Update only if we have the current event
                            eventDao.update(event);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Reminder set successfully", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Error: Event no longer exists", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Error updating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            } else {
                Toast.makeText(this, "Cannot set exact alarms. Please check permissions.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error setting reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    
    private boolean isEventActive(Event event) {
        if (event == null || event.getEventDate() == null) return false;
        return event.getEventDate().after(new Date());
    }
}