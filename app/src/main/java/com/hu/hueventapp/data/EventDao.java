package com.hu.hueventapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.hu.hueventapp.model.Event;
import java.util.Date;
import java.util.List;

@Dao
public interface EventDao {
    @Query("SELECT * FROM events WHERE id = :id")
    LiveData<Event> getEventById(long id);
    
    @Query("SELECT * FROM events WHERE id = :id")
    Event getEventByIdSync(long id);

    @Query("SELECT * FROM events WHERE event_date >= :currentDate ORDER BY event_date ASC")
    LiveData<List<Event>> getUpcomingEvents(Date currentDate);

    @Query("SELECT * FROM events WHERE category = :category ORDER BY event_date ASC")
    LiveData<List<Event>> getEventsByCategory(String category);

    @Query("SELECT * FROM events WHERE title LIKE '%' || :query || '%' ORDER BY event_date ASC")
    LiveData<List<Event>> searchEvents(String query);
    
    @Query("SELECT * FROM events WHERE category = :category AND title LIKE '%' || :query || '%' ORDER BY event_date ASC")
    LiveData<List<Event>> searchEventsByCategory(String query, String category);

    @Query("SELECT * FROM events ORDER BY event_date ASC")
    LiveData<List<Event>> getAllEvents();

    @Insert
    long insert(Event event);

    @Update
    void update(Event event);

    @Delete
    void delete(Event event);
}