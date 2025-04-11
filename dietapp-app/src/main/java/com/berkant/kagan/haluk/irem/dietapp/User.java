package com.berkant.kagan.haluk.irem.dietapp;

/**
 * This class represents a user in the Diet Planner application.
 * @details The User class stores information about a user including 
 *          username, password, personal details and dietary preferences.
 * @author ugur.coruh
 */
public class User {
    /** The unique username for the user account */
    private String username;
    /** The user's account password */
    private String password;
    /** The user's email address for communication */
    private String email;
    /** The user's full name */
    private String name;
    /** The current login status of the user */
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
     * @details Returns the password associated with the user account.
     * @return The user's password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the password of the user.
     * @details Updates the user's account password with the provided value.
     * @param password The new password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Gets the email of the user.
     * @details Returns the email address associated with the user account.
     * @return The user's email address
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Sets the email of the user.
     * @details Updates the user's email address with the provided value.
     * @param email The new email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Gets the name of the user.
     * @details Returns the full name of the user.
     * @return The user's full name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the user.
     * @details Updates the user's full name with the provided value.
     * @param name The new name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Checks if the user is logged in.
     * @details Returns the current login status of the user.
     * @return true if user is currently logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    /**
     * Sets the login status of the user.
     * @details Updates the user's login status with the provided value.
     * @param isLoggedIn The new login status to set
     */
    public void setLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }
    
    /**
     * Returns a string representation of the User object.
     * @details Creates a string containing all user information except the password.
     * @return A string containing user information (username, email, name, login status)
     */
    @Override
    public String toString() {
        return "User [username=" + username + ", email=" + email + ", name=" + name + ", isLoggedIn=" + isLoggedIn + "]";
    }
}