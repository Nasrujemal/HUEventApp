package com.hu.hueventapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog; // Add this import
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hu.hueventapp.adapter.EventAdapter;
import com.hu.hueventapp.data.AppDatabase;
import com.hu.hueventapp.data.EventDao;
import com.hu.hueventapp.model.Event;
import com.hu.hueventapp.sync.SyncManager;
import java.util.Date;
import java.util.List;
import android.widget.Toast;
public class MainActivity extends BaseActivity implements EventAdapter.OnEventClickListener {
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private EventDao eventDao;
    private TextInputEditText searchInput;
    private ChipGroup filterChipGroup;
    private LiveData<List<Event>> currentEventList;
    private SharedPreferences prefs;
    private boolean isAdmin;
    private SyncManager syncManager;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "onCreate started");

            // Initialize SharedPreferences
            prefs = getSharedPreferences("HUEventApp", MODE_PRIVATE);
            isAdmin = prefs.getBoolean("is_admin", false);
            Log.d(TAG, "SharedPreferences initialized, isAdmin: " + isAdmin);

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

            // Initialize SyncManager
            try {
                syncManager = SyncManager.getInstance(this);
                Log.d(TAG, "SyncManager initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "SyncManager initialization error", e);
                Toast.makeText(this, "Error initializing sync manager", Toast.LENGTH_SHORT).show();
            }

            // Setup RecyclerView
            setupRecyclerView();

            // Setup search
            setupSearch();

            // Setup filter chips
            setupFilterChips();

            // Setup FAB
            setupFAB();

            // Observe sync status
            if (syncManager != null) {
                syncManager.getSyncStatus().observe(this, status -> {
                    if (status != null && !status.equals("Ready")) {
                        showSnackbar(status);
                    }
                });
            }

            // Load initial events
            updateEventList();
            Log.d(TAG, "onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void setupToolbar() {
        try {
            super.setupToolbar();
            // We don't need to set the title here as it's already in the layout
            // This was causing duplicate headings
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar", e);
        }
    }

    private void setupRecyclerView() {
        try {
            recyclerView = findViewById(R.id.eventsRecyclerView);
            if (recyclerView == null) {
                Log.e(TAG, "RecyclerView not found in layout");
                return;
            }
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new EventAdapter(this);
            recyclerView.setAdapter(adapter);

            // Apply glassmorphism effect to RecyclerView
            recyclerView.setBackgroundResource(R.drawable.bg_gradient);
            recyclerView.setElevation(4f);
            
            // Enhanced layout animation with staggered effect
            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(
                this, R.anim.layout_animation_fall_down);
            recyclerView.setLayoutAnimation(animation);
            
            // Add item decoration for better spacing and visual appeal
            int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
            recyclerView.addItemDecoration(new androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    outRect.left = spacing;
                    outRect.right = spacing;
                    outRect.bottom = spacing;
                    // Add top margin only for the first item
                    if (parent.getChildAdapterPosition(view) == 0) {
                        outRect.top = spacing;
                    }
                }
            });
            
            Log.d(TAG, "Enhanced RecyclerView setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void setupSearch() {
        try {
            searchInput = findViewById(R.id.searchInput);
            if (searchInput == null) {
                Log.e(TAG, "SearchInput not found in layout");
                return;
            }
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateEventList();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            Log.d(TAG, "Search setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up search", e);
        }
    }

    private void setupFilterChips() {
        try {
            filterChipGroup = findViewById(R.id.filterChipGroup);
            if (filterChipGroup == null) {
                Log.e(TAG, "FilterChipGroup not found in layout");
                return;
            }
            filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (group != null) {
                    group.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
                    updateEventList();
                }
            });
            Log.d(TAG, "Filter chips setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up filter chips", e);
        }
    }

    private void setupFAB() {
        try {
            FloatingActionButton fab = findViewById(R.id.fabAddEvent);
            if (fab != null) {
                // Only show the FAB if the user is an admin
                boolean isAdmin = getSharedPreferences("HUEventApp", MODE_PRIVATE)
                    .getBoolean("is_admin", false);
                
                fab.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                
                // Apply glassmorphism effect and animations to FAB
                if (isAdmin) {
                    // Apply elevation for shadow effect
                    fab.setElevation(12f);
                    
                    // Apply entrance animation
                    fab.setAlpha(0f);
                    fab.setScaleX(0f);
                    fab.setScaleY(0f);
                    fab.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(500)
                        .setInterpolator(new android.view.animation.OvershootInterpolator())
                        .start();
                    
                    // Add ripple effect for better touch feedback
                    fab.setCompatElevation(16f);
                }
                
                fab.setOnClickListener(v -> {
                    try {
                        // Add click animation
                        v.animate()
                            .scaleX(0.9f)
                            .scaleY(0.9f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(100)
                                    .start();
                                    
                                // Start activity with transition animation
                                Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
                                android.os.Bundle options = android.app.ActivityOptions
                                    .makeSceneTransitionAnimation(MainActivity.this).toBundle();
                                startActivity(intent, options);
                            })
                            .start();
                    } catch (Exception e) {
                        Log.e(TAG, "Error launching AddEventActivity", e);
                        Toast.makeText(MainActivity.this, "Error opening add event screen", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            Log.d(TAG, "Enhanced FAB setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up FAB", e);
        }
    }

    private void updateEventList() {
        try {
            if (eventDao == null) {
                Log.e(TAG, "EventDao is null");
                Toast.makeText(this, "Database not initialized", Toast.LENGTH_SHORT).show();
                return;
            }

            final String searchQuery = searchInput != null ? searchInput.getText().toString() : "";
            final int checkedChipId = filterChipGroup != null ? filterChipGroup.getCheckedChipId() : View.NO_ID;
            final Chip selectedChip = checkedChipId != View.NO_ID ? findViewById(checkedChipId) : null;
            final String chipText = selectedChip != null ? selectedChip.getText().toString() : "";

            // Remove previous observers to prevent memory leaks
            if (currentEventList != null) {
                currentEventList.removeObservers(this);
            }

            // Use a safer approach with try-catch blocks
            try {
                AppDatabase.getDatabaseExecutor().execute(() -> {
                    try {
                        LiveData<List<Event>> newEventList;
                        if (!searchQuery.isEmpty()) {
                            if (selectedChip != null && !chipText.equals("All")) {
                                // Search by both title and category
                                newEventList = eventDao.searchEventsByCategory(searchQuery, chipText);
                            } else {
                                // Search by title only across all categories
                                newEventList = eventDao.searchEvents(searchQuery);
                            }
                        } else if (selectedChip != null && !chipText.equals("All")) {
                            // Filter by category only
                            newEventList = eventDao.getEventsByCategory(chipText);
                        } else {
                            // No filters, show all upcoming events
                            newEventList = eventDao.getUpcomingEvents(new Date());
                        }

                        if (newEventList != null) {
                            runOnUiThread(() -> {
                                try {
                                    currentEventList = newEventList;
                                    currentEventList.observe(this, events -> {
                                        try {
                                            if (adapter != null) {
                                                adapter.setEvents(events);
                                                if (recyclerView != null) {
                                                    // Apply smooth animation when updating the list
                                                    recyclerView.setAlpha(0.4f);
                                                    recyclerView.animate()
                                                        .alpha(1f)
                                                        .setDuration(300)
                                                        .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                                                        .withEndAction(() -> {
                                                            // Schedule the layout animation after fade-in
                                                            recyclerView.scheduleLayoutAnimation();
                                                        })
                                                        .start();
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error updating adapter with events", e);
                                            Toast.makeText(this, "Error displaying events", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.e(TAG, "Error setting up LiveData observer", e);
                                    Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error querying database", e);
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Error loading events: " + e.getMessage(), 
                                          Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error executing database query", e);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in updateEventList", e);
            Toast.makeText(this, "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEventClick(Event event) {
        try {
            if (event != null) {
                // Create intent with event data
                Intent intent = new Intent(MainActivity.this, EventDetailActivity.class);
                intent.putExtra("event_id", event.getId());
                
                // Create transition animation bundle
                android.os.Bundle options = android.app.ActivityOptions
                    .makeSceneTransitionAnimation(MainActivity.this).toBundle();
                
                // Start activity with transition animation
                startActivity(intent, options);
                
                // Apply activity transition animation
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onEventClick", e);
            Toast.makeText(this, "Error opening event details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditClick(Event event) {
        try {
            if (event != null) {
                // Create intent with event data
                Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
                intent.putExtra("event_id", event.getId());
                intent.putExtra("is_edit", true);
                
                // Create transition animation bundle
                android.os.Bundle options = android.app.ActivityOptions
                    .makeSceneTransitionAnimation(MainActivity.this).toBundle();
                
                // Start activity with transition animation
                startActivity(intent, options);
                
                // Apply activity transition animation
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onEditClick", e);
            Toast.makeText(this, "Error opening edit screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteClick(Event event) {
        try {
            if (event != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        AppDatabase.getDatabaseExecutor().execute(() -> {
                            try {
                                // Change this line from deleteEvent(event) to delete(event)
                                eventDao.delete(event);
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                                    updateEventList();
                                });
                            } catch (Exception e) {
                                Log.e(TAG, "Error deleting event", e);
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null);
                
                AlertDialog dialog = builder.create();
                
                // Force white background and remove any dark overlay
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
                    // Remove any dim behind the dialog
                    dialog.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                }
                
                dialog.show();
        }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDeleteClick", e);
            Toast.makeText(this, "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            if (syncManager != null) {
                syncManager.shutdown();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int itemId = item.getItemId();
            if (itemId == R.id.action_profile) {
                // Navigate to profile
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            } else if (itemId == R.id.action_sync) {
                // Trigger sync
                if (syncManager != null) {
                    syncManager.syncEvents();
                    showSnackbar("Syncing events...");
                } else {
                    showSnackbar("Sync manager not available");
                }
                return true;
            } else if (itemId == R.id.action_logout) {
                // Handle logout
                try {
                    // Clear user session data
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove("user_id");
                    editor.remove("username");
                    editor.remove("is_admin");
                    editor.apply();
                    
                    // Navigate to login
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                    finish(); // Close the current activity
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Error during logout", e);
                    showSnackbar("Error during logout");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling menu item selection", e);
        }
        return super.onOptionsItemSelected(item);
    }
}