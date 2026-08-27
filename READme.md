# HUEventSchedulerApp Presentation

Group members
Nasradin Jemal...........1334/14
Abdulmalik Battery.......1960/14
Amanue Behailu...........2128/14
Alemu Ayelu..............2109/14

## Introduction

- **Project Overview**: HUEventApp is a mobile application designed for university students to discover, track, and engage with campus events.
- **Target Audience**: Students, faculty, and staff at the university.
- **Technology Stack**: Android native application with Material Design components and custom UI elements.

## Problem Statement

- Students often miss important campus events due to lack of centralized information.
- Traditional event promotion methods (posters, emails) have limited reach and engagement.
- Difficulty in filtering events based on personal interests and availability.
- No streamlined way to register for events or receive updates about changes.

## Objectives

- Create a user-friendly mobile platform for event discovery and management.
- Implement a modern, visually appealing UI with glassmorphism design elements.
- Provide robust search and filtering capabilities for events by category (Academic, Cultural, Sports, Entertainment).
- Enable user authentication for personalized experiences.
- Incorporate university branding elements (colors, logos) for consistent identity.

## Key Features

### User Interface

- **Glassmorphism Design**: Translucent UI elements with blur effects for a modern look.
- **University Branding**: Consistent use of university colors (blue #2196F3 and yellow #FFC107).
- **Responsive Layouts**: Adaptive designs for various screen sizes.
  **Material Components**: Leveraging Google's Material Design library for consistent UI elements.
- **Custom Animations**: Implemented fade, slide, and scale animations for smooth transitions.
- **Responsive Layouts**: Using ConstraintLayout and CoordinatorLayout for adaptive UI.

### Functionality

- **Event Browsing**: Card-based event listings with essential information.
- **Search & Filtering**: Advanced search capabilities with category filtering.
- **Event Details**: Comprehensive view of event information including location, time, and description.
  Events have status (Active & Expired) it will become expired after it took place losing its active status and users cannot set reminder notification on an expired events.
- **User Authentication**: Secure login and registration system and both users can login and logout.
- **Modules**: There are two modules. 1. Admin Module (Haramaya University) 2. Users Module (Students, faculty, and staff at the university.).
- **Admin module**: Only admin can create, update and delete events. admin can update their profile (username, password)  
  current credintials: username: admin password:admin123 email:admin@hu.edu Fullname: System Administrater.
- **Users module**: users can browse through the available events and they can put a reminder notification on for the events they're interested in when those events will take place.
- Users should be registered to use the app and once users are registered they can update their profiles (username, password)

## Limitations

- Currently available only on Android platform.
- Requires internet connection for real-time updates.
- Limited offline functionality.
- Manual event creation process (no integration with university calendar systems yet).
- No Vast database system is implemented in the current version.

## Future Enhancements

- iOS version development.
- Integration with university calendar and scheduling systems.
- Social sharing capabilities.
- Event attendance tracking.
- Analytics dashboard for event organizers.

## Conclusion

- HUEventApp successfully addresses the need for a centralized, user-friendly platform for university event management.
- The application's modern design and intuitive interface enhance the student experience.
- The implementation of university branding creates a sense of familiarity and trust.
- Future enhancements will further improve functionality and user engagement.
