package com.hu.hueventapp.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.hu.hueventapp.data.AppDatabase;
import com.hu.hueventapp.data.EventDao;
import com.hu.hueventapp.model.Event;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private static SyncManager instance;
    private final AppDatabase database;
    private final EventDao eventDao;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final MutableLiveData<Boolean> isSyncing;
    private final MutableLiveData<String> syncStatus;

    private SyncManager(Context context) {
        database = AppDatabase.getInstance(context);
        eventDao = database.eventDao();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        isSyncing = new MutableLiveData<>(false);
        syncStatus = new MutableLiveData<>("Ready");
    }

    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context.getApplicationContext());
        }
        return instance;
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    public LiveData<String> getSyncStatus() {
        return syncStatus;
    }

    public void syncEvents() {
        if (Boolean.TRUE.equals(isSyncing.getValue())) {
            return;
        }

        isSyncing.postValue(true);
        syncStatus.postValue("Syncing events...");

        executor.execute(() -> {
            try {
                // Simulate network delay
                Thread.sleep(1000);

                // Get local events - fixed threading approach
                List<Event> localEvents = null;
                
                if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                    // We're already on the main thread
                    localEvents = eventDao.getAllEvents().getValue();
                } else {
                    // We need to get to the main thread
                    final CountDownLatch latch = new CountDownLatch(1);
                    final List<Event>[] eventsHolder = new List[1];
                    
                    mainHandler.post(() -> {
                        try {
                            eventsHolder[0] = eventDao.getAllEvents().getValue();
                        } catch (Exception e) {
                            Log.e(TAG, "Error getting events on main thread", e);
                        } finally {
                            latch.countDown();
                        }
                    });
                    
                    try {
                        latch.await();
                        localEvents = eventsHolder[0];
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Thread interrupted while waiting for events", e);
                    }
                }
                
                if (localEvents != null) {
                    // In a real app, this would sync with a remote server
                    // For now, we'll just update the sync status
                    mainHandler.post(() -> {
                        syncStatus.setValue("Sync completed");
                        isSyncing.setValue(false);
                    });
                } else {
                    mainHandler.post(() -> {
                        syncStatus.setValue("Sync failed: No events to sync");
                        isSyncing.setValue(false);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during sync", e);
                mainHandler.post(() -> {
                    syncStatus.setValue("Sync failed: " + e.getMessage());
                    isSyncing.setValue(false);
                });
            }
        });
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) 
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void shutdown() {
        executor.shutdown();
    }
}