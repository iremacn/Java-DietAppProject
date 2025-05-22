/**
 * @file AuthenticationService.java
 * @brief Service class for handling user authentication and management operations
 * 
 * @details The AuthenticationService class provides comprehensive functionality for user management
 *          in the Diet Planner application. It handles user registration, authentication,
 *          session management, and user data retrieval. The class maintains the state of
 *          the currently logged-in user and provides methods to interact with the user database.
 * 
 * @author ugur.coruh
 * @version 1.0
 * @date 2024
 * @copyright Diet Planner Application
 */
package com.berkant.kagan.haluk.irem.dietapp;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @class AuthenticationService
 * @brief Main service class for user authentication and management
 * 
 * @details This class implements the core authentication functionality for the Diet Planner application.
 *          It provides methods for user registration, login, logout, and user management operations.
 *          The class maintains the state of the currently logged-in user and handles all database
 *          operations related to user authentication.
 */
public class AuthenticationService {
    /** @brief Reference to the currently logged in user */
    private User currentUser;
    
    /**
     * @brief Default constructor for AuthenticationService
     * @details Initializes the AuthenticationService with no logged-in user
     */
    public AuthenticationService() {
        this.currentUser = null;
    }
    
    /**
     * @brief Registers a new user in the system
     * @details This method validates the input parameters and creates a new user account
     *          if the username is not already taken. The user's information is stored
     *          in the database.
     * 
     * @param username The username for the new user (must not be null or empty)
     * @param password The password for the new user (must not be null or empty)
     * @param email    The email address for the new user (must not be null or empty)
     * @param name     The full name of the new user (must not be null or empty)
     * @return true if registration is successful, false if username already exists or input is invalid
     * @throws SQLException if there is an error accessing the database
     */
    public boolean register(String username, String password, String email, String name) {
        // Validate input parameters
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            name == null || name.trim().isEmpty()) {
            return false;
        }
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT username FROM users WHERE username = ?")) {
            
            // Check if username already exists
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                return false; // Username already exists
            }
            
            // Username is available, create new user
            try (PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO users (username, password, email, name, is_logged_in) VALUES (?, ?, ?, ?, 0)")) {
                
                insertStmt.setString(1, username);
                insertStmt.setString(2, password); // In a real app, you should hash passwords
                insertStmt.setString(3, email);
                insertStmt.setString(4, name);
                
                int affectedRows = insertStmt.executeUpdate();
                return affectedRows > 0;
            }
            
        } catch (SQLException e) {
            System.out.println("User could not be registered: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * @brief Authenticates a user and creates a session
     * @details This method verifies the user's credentials and creates a new session
     *          if the credentials are valid. It updates the user's login status in
     *          the database and maintains the current user state.
     * 
     * @param username The username for login (must not be null)
     * @param password The password for login (must not be null)
     * @return true if login is successful, false if credentials are invalid
     * @throws SQLException if there is an error accessing the database
     */
    public boolean login(String username, String password) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT * FROM users WHERE username = ? AND password = ?")) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Create user object from database
                User user = new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("name")
                );
                user.setLoggedIn(true);
                this.currentUser = user;
                
                // Update login status in database
                try (PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE users SET is_logged_in = 1 WHERE username = ?")) {
                    
                    updateStmt.setString(1, username);
                    updateStmt.executeUpdate();
                }
                
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * @brief Terminates the current user session
     * @details This method logs out the currently logged-in user by updating their
     *          login status in the database and clearing the current user state.
     *          If no user is logged in, this method does nothing.
     * 
     * @throws SQLException if there is an error accessing the database
     */
    public void logout() {
        if (currentUser != null) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE users SET is_logged_in = 0 WHERE username = ?")) {
                
                pstmt.setString(1, currentUser.getUsername());
                pstmt.executeUpdate();
                
            } catch (SQLException e) {
                System.out.println("An error occurred while logging out: " + e.getMessage());
            }
            
            currentUser.setLoggedIn(false);
            currentUser = null;
        }
    }
    
    /**
     * @brief Enables limited access mode for unregistered users
     * @details This method creates a temporary guest user session that allows
     *          limited access to the application without requiring registration.
     *          The guest user is not stored in the database.
     */
    public void enableGuestMode() {
        // Create a temporary guest user (not stored in database)
        this.currentUser = new User("guest", "", "", "Guest User");
        this.currentUser.setLoggedIn(true);
    }
    
    /**
     * @brief Retrieves the currently logged-in user
     * @details Returns the User object representing the currently logged-in user,
     *          or null if no user is logged in.
     * 
     * @return The current User object, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * @brief Checks if there is an active user session
     * @details Verifies whether a user is currently logged in and their session
     *          is active.
     * 
     * @return true if a user is logged in and their session is active, false otherwise
     */
    public boolean isUserLoggedIn() {
        return currentUser != null && currentUser.isLoggedIn();
    }
    
    /**
     * @brief Retrieves all registered users from the database
     * @details This method fetches all user records from the database and returns
     *          them as a list of User objects. Each User object contains the user's
     *          complete profile information and login status.
     * 
     * @return List<User> containing all registered users
     * @throws SQLException if there is an error accessing the database
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            
            while (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("name")
                );
                
                user.setLoggedIn(rs.getInt("is_logged_in") == 1);
                users.add(user);
            }
            
        } catch (SQLException e) {
            System.out.println("Users could not be retrieved: " + e.getMessage());
        }
       
        return users;
    }
}