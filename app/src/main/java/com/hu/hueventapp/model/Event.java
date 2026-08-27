package com.hu.hueventapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.util.Date;

@Entity(tableName = "events")
public class Event {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "location")
    private String location;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "event_date") // Changed from "date" to "event_date"
    private Date eventDate;          // Field name should match your code
    
    @ColumnInfo(name = "organizer")
    private String organizer;

    @ColumnInfo(name = "active")
    private boolean active = true;  // Default to true for new events
    
    // Constructor
    public Event(String title, String description, String location, String category, Date eventDate, String organizer) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.category = category;
        this.eventDate = eventDate;
        this.organizer = organizer;
        this.active = true;  // Default to active
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }
    
    public String getOrganizer() {
        return organizer;
    }
    
    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // For backward compatibility with code that might still use getDateTime()
    public Date getDateTime() {
        return eventDate;
    }
}
