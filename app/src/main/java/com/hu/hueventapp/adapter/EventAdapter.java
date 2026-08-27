package com.hu.hueventapp.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.hu.hueventapp.R;
import com.hu.hueventapp.model.Event;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private static final String TAG = "EventAdapter";
    private List<Event> events = new ArrayList<>();
    private OnEventClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

    public interface OnEventClickListener {
        void onEventClick(Event event);
        void onEditClick(Event event);
        void onDeleteClick(Event event);
    }

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
                
            // Let the XML styling handle the background
            // Keeping elevation for shadow effect
            view.setElevation(8f);
            
            return new EventViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Error creating ViewHolder", e);
            throw new RuntimeException("Error creating ViewHolder: " + e.getMessage());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        try {
            if (position < 0 || position >= events.size()) {
                Log.e(TAG, "Invalid position: " + position);
                return;
            }
            Event event = events.get(position);
            if (event == null) {
                Log.e(TAG, "Event is null at position: " + position);
                return;
            }
            holder.bind(event);
            
            // Apply animation to each item
            applyAnimation(holder.itemView, position);
        } catch (Exception e) {
            Log.e(TAG, "Error binding ViewHolder", e);
        }
    }
    
    private void applyAnimation(View view, int position) {
        try {
            // Only animate items when they first appear
            // Use a staggered animation based on position
            android.view.animation.Animation animation = android.view.animation.AnimationUtils
                .loadAnimation(view.getContext(), R.anim.item_animation_rise_up);
            animation.setStartOffset(position * 50); // Stagger the animations
            view.startAnimation(animation);
        } catch (Exception e) {
            Log.e(TAG, "Error applying animation", e);
        }
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public void setEvents(List<Event> events) {
        try {
            if (events == null) {
                Log.w(TAG, "Setting null events list");
                this.events = new ArrayList<>();
            } else {
                this.events = events;
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Error setting events", e);
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "N/A";
        return dateFormat.format(date);
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView dateText;
        private TextView locationText;
        private TextView categoryText;
        private TextView statusText;
        private MaterialButton editButton;
        private MaterialButton deleteButton;
        private View eventActions;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                titleText = itemView.findViewById(R.id.eventTitle);
                dateText = itemView.findViewById(R.id.eventDate);
                locationText = itemView.findViewById(R.id.eventLocation);
                categoryText = itemView.findViewById(R.id.eventCategory);
                statusText = itemView.findViewById(R.id.eventStatus);
                editButton = itemView.findViewById(R.id.btnEditEvent);
                deleteButton = itemView.findViewById(R.id.btnDeleteEvent);
                eventActions = itemView.findViewById(R.id.eventActions);
                
                // Apply ripple effects to buttons
                if (editButton != null) {
                    applyRippleEffect(editButton);
                }
                
                if (deleteButton != null) {
                    applyRippleEffect(deleteButton);
                }
                
                // Apply ripple effect to the entire item for better touch feedback
                applyRippleEffectToItem(itemView);

                itemView.setOnClickListener(v -> {
                    try {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && listener != null) {
                            Event event = events.get(position);
                            if (event != null) {
                                listener.onEventClick(event);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling click", e);
                    }
                });
                
                if (editButton != null) {
                    editButton.setOnClickListener(v -> {
                        try {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION && listener != null) {
                                Event event = events.get(position);
                                if (event != null) {
                                    listener.onEditClick(event);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error handling edit click", e);
                        }
                    });
                }
                
                if (deleteButton != null) {
                    deleteButton.setOnClickListener(v -> {
                        try {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION && listener != null) {
                                Event event = events.get(position);
                                if (event != null) {
                                    listener.onDeleteClick(event);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error handling delete click", e);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initializing ViewHolder", e);
            }
        }

        public void bind(Event event) {
            try {
                if (event == null) {
                    Log.e(TAG, "Cannot bind null event");
                    return;
                }

                if (titleText != null) titleText.setText(event.getTitle());
                if (dateText != null) dateText.setText(formatDate(event.getEventDate()));
                if (locationText != null) locationText.setText(event.getLocation());
                if (categoryText != null) categoryText.setText(event.getCategory());
                
                // Make sure status text is visible and properly set
                if (statusText != null) {
                    boolean isActive = isEventActive(event);
                    statusText.setText(isActive ? "Active" : "Expired");
                    statusText.setTextColor(itemView.getContext().getResources().getColor(
                        isActive ? R.color.colorActive : R.color.colorExpired));
                    statusText.setVisibility(View.VISIBLE); // Ensure visibility
                }
                
                // Show/hide action buttons based on admin status
                if (eventActions != null) {
                    boolean isAdmin = itemView.getContext()
                        .getSharedPreferences("HUEventApp", itemView.getContext().MODE_PRIVATE)
                        .getBoolean("is_admin", false);
                    
                    Log.d(TAG, "Setting event actions visibility, isAdmin: " + isAdmin);
                    eventActions.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                    
                    // Make sure edit button is properly initialized
                    if (editButton != null) {
                        editButton.setOnClickListener(v -> {
                            try {
                                int position = getAdapterPosition();
                                if (position != RecyclerView.NO_POSITION && listener != null) {
                                    Event clickedEvent = events.get(position);
                                    if (clickedEvent != null) {
                                        listener.onEditClick(clickedEvent);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error handling edit click", e);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error binding event data", e);
            }
        }
    }
    
    private boolean isEventActive(Event event) {
        if (event == null || event.getEventDate() == null) return false;
        return event.getEventDate().after(new Date());
    }
    
    /**
     * Applies a ripple effect to a button for better touch feedback
     */
    private void applyRippleEffect(View view) {
        try {
            if (view != null) {
                // Only set clickable and focusable properties
                // Don't override the foreground to preserve the button styling
                view.setClickable(true);
                view.setFocusable(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying ripple effect", e);
        }
    }
    
    /**
     * Applies a ripple effect to the entire item view
     */
    private void applyRippleEffectToItem(View view) {
        try {
            if (view != null) {
                // Don't set foreground to allow XML styling to take effect
                // Just ensure the view is clickable and focusable
                view.setClickable(true);
                view.setFocusable(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying ripple effect to item", e);
        }
    }
}