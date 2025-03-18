package com.berkant.kagan.haluk.irem.dietapp;

/**
 * This class represents a DietApp that performs diet planning operations.
 * @details The DietApp class provides methods to perform diet planning operations such as
 *          meal planning, calorie tracking, and generating recommendations.
 * @author ugur.coruh
 */
public class Dietapp {
    // Private fields for encapsulation
    private AuthenticationService authService;
    
    /**
     * Default constructor for DietApp class.
     * Initializes the authentication service.
     */
    public Dietapp() {
        this.authService = new AuthenticationService();
    }
    
    /**
     * Gets the authentication service.
     * 
     * @return The AuthenticationService instance
     */
    public AuthenticationService getAuthService() {
        return authService;
    }
    
    /**
     * Registers a new user in the system.
     * 
     * @param username The username for registration
     * @param password The password for registration
     * @param email    The email for registration
     * @param name     The name for registration
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(String username, String password, String email, String name) {
        return authService.register(username, password, email, name);
    }
    
    /**
     * Attempts to login a user with the provided credentials.
     * 
     * @param username The username for login
     * @param password The password for login
     * @return true if login successful, false otherwise
     */
    public boolean loginUser(String username, String password) {
        return authService.login(username, password);
    }
    
    /**
     * Logs out the current user.
     */
    public void logoutUser() {
        authService.logout();
    }
    
    /**
     * Enables guest mode for the application.
     */
    public void enableGuestMode() {
        authService.enableGuestMode();
    }
    
    /**
     * Checks if a user is currently logged in.
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isUserLoggedIn() {
        return authService.isUserLoggedIn();
    }
    
    /**
     * Gets the currently logged in user.
     * 
     * @return The current user or null if no user is logged in
     */
    public User getCurrentUser() {
        return authService.getCurrentUser();
    }
}