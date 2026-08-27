package com.hu.hueventapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hu.hueventapp.data.AppDatabase;
import com.hu.hueventapp.data.UserDao;
import com.hu.hueventapp.model.User;

public class ProfileActivity extends BaseActivity {
    private static final String TAG = "ProfileActivity";
    private UserDao userDao;
    private SharedPreferences prefs;
    private TextInputEditText fullNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText currentPasswordInput;
    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmPasswordInput;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set back button and title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }

        // Initialize database and preferences
        userDao = AppDatabase.getInstance(this).userDao();
        prefs = getSharedPreferences("HUEventApp", MODE_PRIVATE);

        // Initialize views
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        currentPasswordInput = findViewById(R.id.currentPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

        // Load user data
        loadUserData();

        // Setup save button
        MaterialButton saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void loadUserData() {
        long userId = prefs.getLong("user_id", -1);
        if (userId != -1) {
            userDao.getUserById(userId).observe(this, user -> {
                if (user != null) {
                    currentUser = user;
                    fullNameInput.setText(user.getFullName());
                    emailInput.setText(user.getEmail());
                }
            });
        }
    }

    private void saveChanges() {
        if (currentUser == null) {
            return;
        }

        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String currentPassword = currentPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate inputs
        if (fullName.isEmpty()) {
            fullNameInput.setError("Full name is required");
            return;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            return;
        }

        if (!isValidEmail(email)) {
            emailInput.setError("Invalid email format");
            return;
        }

        // Check if password change is requested
        if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            if (currentPassword.isEmpty()) {
                currentPasswordInput.setError("Current password is required");
                return;
            }

            if (!currentPassword.equals(currentUser.getPassword())) {
                currentPasswordInput.setError("Current password is incorrect");
                return;
            }

            if (newPassword.isEmpty()) {
                newPasswordInput.setError("New password is required");
                return;
            }

            if (newPassword.length() < 6) {
                newPasswordInput.setError("Password must be at least 6 characters");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                confirmPasswordInput.setError("Passwords do not match");
                return;
            }

            // Update password
            currentUser.setPassword(newPassword);
        }

        // Update user information
        currentUser.setFullName(fullName);
        currentUser.setEmail(email);

        // Replace new Thread with database executor
        AppDatabase.getDatabaseExecutor().execute(() -> {
            try {
                userDao.update(currentUser);
                runOnUiThread(() -> {
                    showCustomToast("Profile updated successfully", true);
                    finish();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error saving profile", e);
                runOnUiThread(() -> 
                    showCustomToast("Save failed", false));
            }
        });
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_profile;
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