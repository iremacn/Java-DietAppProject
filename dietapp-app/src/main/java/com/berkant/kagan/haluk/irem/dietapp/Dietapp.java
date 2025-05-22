/**
 * @file Dietapp.java
 * @brief Main application class for the Diet Planner system
 * 
 * @details The Dietapp class serves as the main entry point for the Diet Planner application,
 *          providing core functionality for user authentication, registration, and session
 *          management. It acts as a facade for the authentication service and implements
 *          input validation for user operations.
 * 
 * @author ugur.coruh
 * @version 1.0
 * @date 2024
 * @copyright Diet Planner Application
 */
package com.berkant.kagan.haluk.irem.dietapp;

/**
 * @class Dietapp
 * @brief Main application class for diet planning operations
 * 
 * @details This class provides the core functionality for the Diet Planner application,
 *          including user authentication, registration, and session management. It
 *          implements input validation and acts as a facade for the authentication service.
 */
public class Dietapp {
    /** @brief Service for handling user authentication operations */
    private AuthenticationService authService;

    /**
     * @brief Constructor for Dietapp class
     * @details Initializes the authentication service for user management.
     *          Creates a new instance of AuthenticationService.
     */
    public Dietapp() {
        this.authService = new AuthenticationService();
    }

    /**
     * @brief Retrieves the authentication service instance
     * @details Returns the AuthenticationService instance used for user management.
     * 
     * @return The AuthenticationService instance
     */
    public AuthenticationService getAuthService() {
        return authService;
    }

    /**
     * @brief Registers a new user in the system
     * @details Validates all input parameters and attempts to register a new user.
     *          Performs validation for username, password, email, and name fields.
     * 
     * @param username The username for the new user account
     * @param password The password for the new user account
     * @param email The email address for the new user account
     * @param name The full name of the new user
     * @return true if registration is successful, false otherwise
     * @throws IllegalArgumentException if any of the input parameters are invalid
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
     * @brief Validates an email address format
     * @details Performs basic email validation checking for:
     *          - Non-null and non-empty string
     *          - Presence of @ symbol
     *          - Presence of domain with at least one character
     *          - Presence of TLD with at least one character
     * 
     * @param email The email address to validate
     * @return true if the email format is valid, false otherwise
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
     * @brief Authenticates a user with provided credentials
     * @details Validates input parameters and attempts to log in a user.
     *          Performs validation for username and password fields.
     * 
     * @param username The username for authentication
     * @param password The password for authentication
     * @return true if login is successful, false otherwise
     * @throws IllegalArgumentException if username or password is invalid
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
     * @brief Logs out the current user
     * @details Checks if a user is currently logged in before attempting logout.
     *          Provides feedback if no user is logged in.
     * 
     * @throws IllegalStateException if no user is currently logged in
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
     * @brief Enables guest mode for the application
     * @details Checks if a user is currently logged in before enabling guest mode.
     *          Provides feedback if a user is already logged in.
     * 
     * @throws IllegalStateException if a user is currently logged in
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
     * @brief Checks if a user is currently logged in
     * @details Queries the authentication service to determine if there is
     *          an active user session.
     * 
     * @return true if a user is currently logged in, false otherwise
     */
    public boolean isUserLoggedIn() {
        return authService.isUserLoggedIn();
    }

    /**
     * @brief Retrieves the currently logged in user
     * @details Returns the User object representing the currently logged in user.
     * 
     * @return The current User object, or null if no user is logged in
     */
    public User getCurrentUser() {
        return authService.getCurrentUser();
    }
}