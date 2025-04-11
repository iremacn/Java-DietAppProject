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
    * @brief Default constructor for User class.
    *
    * @details Initializes a new User instance with default settings.
    * Sets the initial login status to false, indicating the user is not logged in.
    *
    * @note Creates an empty User object with no specific credentials
    *
    * @post isLoggedIn is set to false
    */
    public User() {
        this.isLoggedIn = false;
    }
    
    /**
    * @brief Parameterized constructor for User class.
    *
    * @details Initializes a new User instance with provided user account details.
    * Sets all user-specific information and initializes login status to false.
    *
    * @param username The unique username for the user account
    * @param password The user's account password
    * @param email The user's email address for communication
    * @param name The user's full name
    *
    * @note Newly created user is automatically set to a logged-out state
    *
    * @post User object is created with specified credentials
    * @post isLoggedIn is set to false
    */
    public User(String username, String password, String email, String name) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.isLoggedIn = false;
    }
    
    /**
    * @brief Retrieves the user's username.
    *
    * @details Returns the unique identifier for the user account.
    *
    * @return String representing the user's username
    * @retval String The unique username of the user
    *
    * @note Provides read-only access to the username attribute
    *
    * @see setUsername()
    */
    public String getUsername() {
        return username;
    }
    
    /**
    * @brief Sets the username for the user account.
    *
    * @details Updates the user's unique identifier with the provided username.
    *
    * @param username The new username to be assigned to the user account
    *
    * @note Allows modification of the user's username
    *
    * @warning Ensure the new username follows any application-specific naming rules
    *
    * @see getUsername()
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