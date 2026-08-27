package com.hu.hueventapp.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.hu.hueventapp.model.Event;
import com.hu.hueventapp.model.User;
import com.hu.hueventapp.util.DateConverter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Add this import at the top of the file
import android.util.Log;

@Database(entities = {Event.class, User.class}, version = 2, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "huevent_db";
    private static volatile AppDatabase instance;
    // Add this if it's missing
    private static final java.util.concurrent.ExecutorService databaseExecutor = 
        java.util.concurrent.Executors.newFixedThreadPool(4);
    
    public abstract EventDao eventDao();
    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            try {
                instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration() // This will recreate tables if schema changes
                    .build();
            } catch (Exception e) {
                Log.e("AppDatabase", "Failed to initialize database", e);
                return null;
            }
        }
        return instance;
    }

    public static java.util.concurrent.ExecutorService getDatabaseExecutor() {
        return databaseExecutor;
    }

    public static void shutdown() {
        if (databaseExecutor != null) {
            databaseExecutor.shutdown();
        }
    }
}