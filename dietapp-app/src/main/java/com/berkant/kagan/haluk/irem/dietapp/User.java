package com.berkant.kagan.haluk.irem.dietapp;

/**
 * This class represents a user in the Diet Planner application.
 * @details The User class stores information about a user including 
 *          username, password, personal details and dietary preferences.
 * @author ugur.coruh
 */
public class User {
    // Private fields for encapsulation
    private String username;
    private String password;
    private String email;
    private String name;
    private boolean isLoggedIn;
    
    /**
     * Default constructor for User class.
     */
    public User() {
        this.isLoggedIn = false;
    }
    
    /**
     * Parameterized constructor for User class.
     * 
     * @param username The username for the user account
     * @param password The password for the user account
     * @param email    The email address of the user
     * @param name     The full name of the user
     */
    public User(String username, String password, String email, String name) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.isLoggedIn = false;
    }
    
    /**
     * Gets the username of the user.
     * 
     * @return The username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Sets the username of the user.
     * 
     * @param username The username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Gets the password of the user.
     * 
     * @return The password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the password of the user.
     * 
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Gets the email of the user.
     * 
     * @return The email
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Sets the email of the user.
     * 
     * @param email The email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Gets the name of the user.
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the user.
     * 
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Checks if the user is logged in.
     * 
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    /**
     * Sets the login status of the user.
     * 
     * @param isLoggedIn The login status to set
     */
    public void setLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }
    
    /**
     * Returns a string representation of the User object.
     * 
     * @return A string containing user information
     */
    @Override
    public String toString() {
        return "User [username=" + username + ", email=" + email + ", name=" + name + ", isLoggedIn=" + isLoggedIn + "]";
    }
}