package com.berkant.kagan.haluk.irem.dietapp;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the authentication operations for the Diet Planner application.
 * @details The AuthenticationService class provides methods for user registration,
 *          login, logout, and user management.
 * @author ugur.coruh
 */
public class AuthenticationService {
	 // Reference to the currently logged in user
    private User currentUser;
    
    /**
     * Constructor for AuthenticationService class.
     */
    public AuthenticationService() {
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
            System.out.println("Kullanıcı kaydedilemedi: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Attempts to login a user with the provided credentials.
     * 
     * @param username The username for login
     * @param password The password for login
     * @return true if login successful, false otherwise
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
            System.out.println("Giriş yapılamadı: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Logs out the currently logged in user.
     */
    public void logout() {
        if (currentUser != null) {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE users SET is_logged_in = 0 WHERE username = ?")) {
                
                pstmt.setString(1, currentUser.getUsername());
                pstmt.executeUpdate();
                
            } catch (SQLException e) {
                System.out.println("Çıkış yapılırken hata oluştu: " + e.getMessage());
            }
            
            currentUser.setLoggedIn(false);
            currentUser = null;
        }
    }
    
    /**
     * Enables guest mode which allows limited access without registration.
     */
    public void enableGuestMode() {
        // Create a temporary guest user (not stored in database)
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
            System.out.println("Kullanıcılar alınamadı: " + e.getMessage());
        }
        
        return users;
    }
}