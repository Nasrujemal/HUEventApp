package com.hu.hueventapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.snackbar.Snackbar;
import com.hu.hueventapp.sync.SyncManager;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private static final String PREFS_NAME = "HUEventAppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Apply activity transition animation
            getWindow().setEnterTransition(android.transition.TransitionInflater.from(this)
                .inflateTransition(android.R.transition.fade));
            getWindow().setExitTransition(android.transition.TransitionInflater.from(this)
                .inflateTransition(android.R.transition.fade));
            
            // Apply glassmorphism effect to window
            getWindow().setBackgroundDrawableResource(R.drawable.bg_gradient);
            
            // Set content view
            setContentView(getLayoutResourceId());
            
            // Setup toolbar with animation
            setupToolbar();
            
            // Apply subtle entrance animation to the root view
            View rootView = findViewById(android.R.id.content);
            if (rootView != null) {
                rootView.setAlpha(0f);
                rootView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }
    
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        // Apply transition animation with fade and scale effect for a modern look
        overridePendingTransition(R.anim.fade_transition, R.anim.fade_out);
    }
    
    @Override
    public void finish() {
        super.finish();
        // Apply transition animation for back navigation
        overridePendingTransition(R.anim.fade_in, R.anim.fade_transition);
    }

    protected abstract int getLayoutResourceId();

//    @Override
    protected void setupToolbar() {
        try {
//            super.setupToolbar();
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
            }
            
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            int itemId = item.getItemId();
            if (itemId == R.id.action_profile) {
                // Navigate to profile
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            } else if (itemId == R.id.action_sync) {
                // Trigger sync
                SyncManager syncManager = SyncManager.getInstance(this);
                syncManager.syncEvents();
                showSnackbar("Syncing events...");
                return true;
            } else if (itemId == R.id.action_logout) {
                // Handle logout
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("user_id");
                editor.remove("username");
                editor.apply();
                
                // Navigate to login
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling menu item selection", e);
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showSnackbar(String message) {
        try {
            if (message != null && !message.isEmpty()) {
                View rootView = findViewById(android.R.id.content);
                if (rootView != null) {
                    // Create a styled Snackbar with glassmorphism effect
                    Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
                    
                    // Get the Snackbar view
                    View snackbarView = snackbar.getView();
                    
                    // Apply glassmorphism styling
                    snackbarView.setBackgroundResource(R.drawable.bg_gradient);
                    snackbarView.setElevation(8f);
                    
                    // Style the text
                    TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                    if (textView != null) {
                        textView.setTextColor(getResources().getColor(R.color.colorOnPrimary));
                        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    }
                    
                    // Add animation
                    snackbarView.setAlpha(0f);
                    snackbarView.animate()
                        .alpha(1f)
                        .setDuration(250)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
                    
                    snackbar.show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing snackbar", e);
        }
    }
    
    /**
     * Shows a custom toast message with the app's glassmorphism design
     * 
     * @param message The message to display
     * @param isSuccess Whether this is a success message (green icon) or info message (blue icon)
     */
    protected void showCustomToast(String message, boolean isSuccess) {
        try {
            if (message != null && !message.isEmpty()) {
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.custom_toast, null);
                
                TextView text = layout.findViewById(R.id.toast_text);
                text.setText(message);
                
                ImageView icon = layout.findViewById(R.id.toast_icon);
                if (isSuccess) {
                    icon.setImageResource(android.R.drawable.ic_dialog_info);
                    icon.setColorFilter(getResources().getColor(R.color.colorSuccess));
                } else {
                    icon.setImageResource(android.R.drawable.ic_dialog_info);
                    icon.setColorFilter(getResources().getColor(R.color.colorInfo));
                }
                
                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 40);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                toast.show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing custom toast", e);
            // Fallback to regular toast if custom toast fails
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Applies a ripple effect to any view for better touch feedback
     *
     * @param view The view to apply the ripple effect to
     * @param isRounded Whether to use rounded corners for the ripple
     */
    protected void applyRippleEffect(View view, boolean isRounded) {
        try {
            if (view != null) {
                // Create ripple drawable programmatically
//                int rippleColor = getResources().getColor(R.color.colorPrimaryLight);
////                int mask = isRounded ? R.drawable.glass_ripple_effect : R.drawable.button_ripple_effect;
//
//                // Apply ripple effect
//                view.setForeground(getDrawable(mask));
                view.setClickable(true);
                view.setFocusable(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying ripple effect", e);
        }
    }
    
    /**
     * Applies animation to RecyclerView for a more engaging UI experience
     * 
     * @param recyclerView The RecyclerView to animate
     * @param useRiseUp Whether to use rise-up animation (true) or fall-down animation (false)
     */
    protected void applyRecyclerViewAnimation(androidx.recyclerview.widget.RecyclerView recyclerView, boolean useRiseUp) {
        try {
            if (recyclerView != null) {
                int animResId = useRiseUp ? 
                    R.anim.layout_animation_rise_up : 
                    R.anim.layout_animation_fall_down;
                    
                android.view.animation.LayoutAnimationController animation = 
                    android.view.animation.AnimationUtils.loadLayoutAnimation(this, animResId);
                recyclerView.setLayoutAnimation(animation);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying RecyclerView animation", e);
        }
    }
}