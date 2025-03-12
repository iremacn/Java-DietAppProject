package com.berkant.kagan.haluk.irem.dietapp;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the authentication operations for the Diet Planner application.
 * @details The AuthenticationService class provides methods for user registration,
 *          login, logout, and user management.
 * @author ugur.coruh
 */
public class AuthenticationService {
    // List to store registered users
    private List<User> users;
    // Reference to the currently logged in user
    private User currentUser;
    
    /**
     * Constructor for AuthenticationService class.
     * Initializes the users list.
     */
    public AuthenticationService() {
        this.users = new ArrayList<>();
        this.currentUser = null;
    }
    
    /**
     * Registers a new user with the provided information.
     * 
     * @param username The username for the new user
     * @param password The password for the new user
     * @param email    The email for the new user
     * @param name     The name for the new user
     * @return true if registration successful, false if username already exists
     */
    public boolean register(String username, String password, String email, String name) {
        // Check if username already exists
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return false; // Username already exists
            }
        }
        
        // Create new user and add to the list
        User newUser = new User(username, password, email, name);
        users.add(newUser);
        return true;
    }
    
    /**
     * Attempts to login a user with the provided credentials.
     * 
     * @param username The username for login
     * @param password The password for login
     * @return true if login successful, false otherwise
     */
    public boolean login(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                user.setLoggedIn(true);
                this.currentUser = user;
                return true;
            }
        }
        return false; // Login failed
    }
    
    /**
     * Logs out the currently logged in user.
     */
    public void logout() {
        if (currentUser != null) {
            currentUser.setLoggedIn(false);
            currentUser = null;
        }
    }
    
    /**
     * Enables guest mode which allows limited access without registration.
     */
    public void enableGuestMode() {
        // Create a temporary guest user
        this.currentUser = new User("guest", "", "", "Guest User");
        this.currentUser.setLoggedIn(true);
    }
    
    /**
     * Gets the currently logged in user.
     * 
     * @return The current user or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Checks if a user is currently logged in.
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isUserLoggedIn() {
        return currentUser != null && currentUser.isLoggedIn();
    }
    
    /**
     * Gets all registered users.
     * 
     * @return List of all registered users
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users); // Return a copy to preserve encapsulation
    }
}