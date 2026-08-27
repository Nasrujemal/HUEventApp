package com.hu.hueventapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hu.hueventapp.data.AppDatabase;
import com.hu.hueventapp.data.UserDao;
import com.hu.hueventapp.model.User;
import android.widget.Toast;
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Don't show the menu on login screen
        return false;
    }
    private UserDao userDao;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            // Initialize database
            try {
                AppDatabase database = AppDatabase.getInstance(this);
                userDao = database.userDao();
                if (userDao == null) {
                    Log.e(TAG, "Failed to initialize UserDao");
                    Toast.makeText(this, "Error initializing database", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            } catch (Exception e) {
                Log.e(TAG, "Database initialization error", e);
                Toast.makeText(this, "Error initializing database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize SharedPreferences
            prefs = getSharedPreferences("HUEventApp", MODE_PRIVATE);

            // Check if user is already logged in
            if (isUserLoggedIn()) {
                startMainActivity();
                return;
            }

            // Initialize views
            setupViews();

            // Create default admin user if none exists
            createDefaultAdminIfNeeded();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        try {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Login");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar", e);
        }
    }

    private void setupViews() {
        try {
            usernameInput = findViewById(R.id.usernameInput);
            passwordInput = findViewById(R.id.passwordInput);

            MaterialButton loginButton = findViewById(R.id.loginButton);
            loginButton.setOnClickListener(v -> attemptLogin());
            // Apply ripple effect for better touch feedback
            applyRippleEffect(loginButton, true);

            MaterialButton registerButton = findViewById(R.id.registerButton);
            registerButton.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
            // Apply ripple effect for better touch feedback
            applyRippleEffect(registerButton, true);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up views", e);
        }
    }

    private void createDefaultAdminIfNeeded() {
        try {
            userDao.getAllAdmins().observe(this, admins -> {
                if (admins == null || admins.isEmpty()) {
                    AppDatabase.getDatabaseExecutor().execute(() -> {
                        try {
                            User admin = new User(
                                "admin",
                                "admin123",
                                true,
                                "admin@hu.edu",
                                "System Administrator"
                            );
                            userDao.insert(admin);
                        } catch (Exception e) {
                            Log.e(TAG, "Error creating admin user", e);
                            runOnUiThread(() -> {
                                showCustomToast("Error creating admin user: " + e.getMessage(), false);
                            });
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error checking admin users", e);
            showCustomToast("Error checking admin users: " + e.getMessage(), false);
        }
    }

    private void attemptLogin() {
        try {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty()) {
                usernameInput.setError("Username is required");
                return;
            }

            if (password.isEmpty()) {
                passwordInput.setError("Password is required");
                return;
            }

            userDao.login(username, password).observe(this, user -> {
                if (user != null) {
                    saveLoginState(user);
                    startMainActivity();
                } else {
                    showCustomToast("Invalid username or password", false);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error during login", e);
            showCustomToast("Error during login: " + e.getMessage(), false);
        }
    }

    private void saveLoginState(User user) {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("user_id", user.getId());
            editor.putString("username", user.getUsername());
            editor.putBoolean("is_admin", user.isAdmin());
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving login state", e);
            showCustomToast("Error saving login state: " + e.getMessage(), false);
        }
    }

    private boolean isUserLoggedIn() {
        try {
            return prefs.getLong("user_id", -1) != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error checking login state", e);
            return false;
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_login;
    }
    
    private void startMainActivity() {
        try {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error starting MainActivity", e);
            showCustomToast("Error starting app: " + e.getMessage(), false);
        }
    }
}