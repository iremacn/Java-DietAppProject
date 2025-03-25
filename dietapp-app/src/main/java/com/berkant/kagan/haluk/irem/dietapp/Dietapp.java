package com.berkant.kagan.haluk.irem.dietapp;

/**
 * This class represents a DietApp that performs diet planning operations.
 * @details The DietApp class provides methods to perform diet planning operations such as
 * meal planning, calorie tracking, and generating recommendations.
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
     * Validates input parameters before attempting registration.
     *
     * @param username The username for registration
     * @param password The password for registration
     * @param email The email for registration
     * @param name The name for registration
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(String username, String password, String email, String name) {
        // Validate input parameters
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Invalid username. Username cannot be empty.");
            return false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            System.out.println("Invalid password. Password cannot be empty.");
            return false;
        }
        
        if (email == null || email.trim().isEmpty() || !isValidEmail(email)) {
            System.out.println("Invalid email address.");
            return false;
        }
        
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Invalid name. Name cannot be empty.");
            return false;
        }
        
        return authService.register(username, password, email, name);
    }

    /**
     * Checks if an email address is valid.
     * Simple validation that checks for @ symbol and a period after it.
     *
     * @param email The email address to validate
     * @return true if email is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        email = email.trim();
        
        // Basic email validation: contains @ and at least one . after @
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return false;
        }
        
        int dotIndex = email.indexOf('.', atIndex);
        if (dotIndex <= atIndex + 1 || dotIndex == email.length() - 1) {
            return false;
        }
        
        return true;
    }

    /**
     * Attempts to login a user with the provided credentials.
     * Validates input parameters before attempting login.
     *
     * @param username The username for login
     * @param password The password for login
     * @return true if login successful, false otherwise
     */
    public boolean loginUser(String username, String password) {
        // Validate input parameters
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Invalid username. Username cannot be empty.");
            return false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            System.out.println("Invalid password. Password cannot be empty.");
            return false;
        }
        
        return authService.login(username, password);
    }

    /**
     * Logs out the current user.
     * Checks if a user is logged in before attempting logout.
     */
    public void logoutUser() {
        if (!isUserLoggedIn()) {
            System.out.println("Cannot log out. No user is logged in.");
            return;
        }
        
        authService.logout();
        System.out.println("Successfully logged out.");
    }

    /**
     * Enables guest mode for the application.
     * Checks if a user is already logged in before enabling guest mode.
     */
    public void enableGuestMode() {
        if (isUserLoggedIn()) {
            System.out.println("Cannot enable guest mode. Please log out the current user first.");
            return;
        }
        
        authService.enableGuestMode();
        System.out.println("Guest mode enabled.");
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