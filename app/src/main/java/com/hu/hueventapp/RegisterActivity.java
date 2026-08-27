package com.hu.hueventapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hu.hueventapp.data.AppDatabase;
import com.hu.hueventapp.data.UserDao;
import com.hu.hueventapp.model.User;

public class RegisterActivity extends BaseActivity {
    private UserDao userDao;
    private TextInputEditText fullNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;

    private static final String TAG = "RegisterActivity";
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Don't show the menu on register screen
        return false;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Initialize database
            try {
                AppDatabase database = AppDatabase.getInstance(this);
                if (database == null) {
                    showCustomToast("Error initializing database", false);
                    finish();
                    return;
                }
                userDao = database.userDao();
                if (userDao == null) {
                    showCustomToast("Error accessing database", false);
                    finish();
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Database initialization error", e);
                showCustomToast("Error initializing database: " + e.getMessage(), false);
                finish();
                return;
            }
            
            // Initialize views
            setupViews();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            showCustomToast("Error initializing registration: " + e.getMessage(), false);
            finish();
        }
    }

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Register");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar", e);
        }
    }
    
    private void setupViews() {
        try {
            // Initialize input fields
            fullNameInput = findViewById(R.id.fullNameInput);
            emailInput = findViewById(R.id.emailInput);
            usernameInput = findViewById(R.id.usernameInput);
            passwordInput = findViewById(R.id.passwordInput);
            confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

            // Setup register button with ripple effect
            MaterialButton registerButton = findViewById(R.id.registerButton);
            registerButton.setOnClickListener(v -> attemptRegistration());
            applyRippleEffect(registerButton, true);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up views", e);
        }
    }
    
    private void attemptRegistration() {
        try {
            // Get input values
            String fullName = fullNameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
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

            if (username.isEmpty()) {
                usernameInput.setError("Username is required");
                return;
            }

            if (password.isEmpty()) {
                passwordInput.setError("Password is required");
                return;
            }

            if (password.length() < 6) {
                passwordInput.setError("Password must be at least 6 characters");
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordInput.setError("Passwords do not match");
                return;
            }

            // Check if username already exists
            userDao.getUserByUsername(username).observe(this, existingUser -> {
                if (existingUser != null) {
                    usernameInput.setError("Username already exists");
                } else {
                    // Create new user
                    User newUser = new User(
                        username,
                        password,
                        false, // Regular user, not admin
                        email,
                        fullName
                    );

                    // Save user in background
                    AppDatabase.getDatabaseExecutor().execute(() -> {
                        try {
                            userDao.insert(newUser);
                            runOnUiThread(() -> {
                                showCustomToast("Registration successful", true);
                                finish();
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error creating user", e);
                            runOnUiThread(() -> {
                                showCustomToast("Error creating user: " + e.getMessage(), false);
                            });
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error during registration", e);
            showCustomToast("Error during registration: " + e.getMessage(), false);
        }
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_register;
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