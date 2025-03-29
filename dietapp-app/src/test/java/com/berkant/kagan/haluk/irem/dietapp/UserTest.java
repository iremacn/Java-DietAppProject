package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for User
 */
public class UserTest {
    
    private User user;
    private final String TEST_USERNAME = "testuser";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_NAME = "Test User";
    
    /**
     * Setup method that runs before each test
     * Creates a test user for testing
     */
    @Before
    public void setUp() {
        user = new User(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL, TEST_NAME);
    }
    
    /**
     * Test for default constructor
     * Verifies that default constructor initializes fields correctly
     */
    @Test
    public void testDefaultConstructor() {
        User defaultUser = new User();
        
        assertNull("Username should be null by default", defaultUser.getUsername());
        assertNull("Password should be null by default", defaultUser.getPassword());
        assertNull("Email should be null by default", defaultUser.getEmail());
        assertNull("Name should be null by default", defaultUser.getName());
        assertFalse("isLoggedIn should be false by default", defaultUser.isLoggedIn());
    }
    
    /**
     * Test for parameterized constructor
     * Verifies that parameterized constructor initializes fields correctly
     */
    @Test
    public void testParameterizedConstructor() {
        assertEquals("Username should match constructor parameter", TEST_USERNAME, user.getUsername());
        assertEquals("Password should match constructor parameter", TEST_PASSWORD, user.getPassword());
        assertEquals("Email should match constructor parameter", TEST_EMAIL, user.getEmail());
        assertEquals("Name should match constructor parameter", TEST_NAME, user.getName());
        assertFalse("isLoggedIn should be false initially", user.isLoggedIn());
    }
    
    /**
     * Test for getUsername and setUsername methods
     * Verifies that username can be get and set correctly
     */
    @Test
    public void testGetSetUsername() {
        // Test initial value
        assertEquals("Initial username should match", TEST_USERNAME, user.getUsername());
        
        // Set new value
        String newUsername = "newusername";
        user.setUsername(newUsername);
        
        // Test new value
        assertEquals("Username should be updated", newUsername, user.getUsername());
    }
    
    /**
     * Test for getPassword and setPassword methods
     * Verifies that password can be get and set correctly
     */
    @Test
    public void testGetSetPassword() {
        // Test initial value
        assertEquals("Initial password should match", TEST_PASSWORD, user.getPassword());
        
        // Set new value
        String newPassword = "newpassword123";
        user.setPassword(newPassword);
        
        // Test new value
        assertEquals("Password should be updated", newPassword, user.getPassword());
    }
    
    /**
     * Test for getEmail and setEmail methods
     * Verifies that email can be get and set correctly
     */
    @Test
    public void testGetSetEmail() {
        // Test initial value
        assertEquals("Initial email should match", TEST_EMAIL, user.getEmail());
        
        // Set new value
        String newEmail = "new@example.com";
        user.setEmail(newEmail);
        
        // Test new value
        assertEquals("Email should be updated", newEmail, user.getEmail());
    }
    
    /**
     * Test for getName and setName methods
     * Verifies that name can be get and set correctly
     */
    @Test
    public void testGetSetName() {
        // Test initial value
        assertEquals("Initial name should match", TEST_NAME, user.getName());
        
        // Set new value
        String newName = "New Test User";
        user.setName(newName);
        
        // Test new value
        assertEquals("Name should be updated", newName, user.getName());
    }
    
    /**
     * Test for isLoggedIn and setLoggedIn methods
     * Verifies that login status can be get and set correctly
     */
    @Test
    public void testIsSetLoggedIn() {
        // Test initial value
        assertFalse("Initial login status should be false", user.isLoggedIn());
        
        // Set to true
        user.setLoggedIn(true);
        assertTrue("Login status should be true after setting", user.isLoggedIn());
        
        // Set back to false
        user.setLoggedIn(false);
        assertFalse("Login status should be false after setting back", user.isLoggedIn());
    }
    
    /**
     * Test for toString method
     * Verifies that toString returns the expected string representation
     */
    @Test
    public void testToString() {
        String expected = "User [username=" + TEST_USERNAME + 
                         ", email=" + TEST_EMAIL + 
                         ", name=" + TEST_NAME + 
                         ", isLoggedIn=" + false + "]";
        
        assertEquals("ToString should return correct representation", expected, user.toString());
        
        // Test with logged in state
        user.setLoggedIn(true);
        expected = "User [username=" + TEST_USERNAME + 
                  ", email=" + TEST_EMAIL + 
                  ", name=" + TEST_NAME + 
                  ", isLoggedIn=" + true + "]";
        
        assertEquals("ToString should reflect login status change", expected, user.toString());
    }
    
    /**
     * Test edge cases with null values
     * Verifies that the class handles null values appropriately
     */
    @Test
    public void testNullValues() {
        User nullUser = new User(null, null, null, null);
        
        assertNull("Username should handle null", nullUser.getUsername());
        assertNull("Password should handle null", nullUser.getPassword());
        assertNull("Email should handle null", nullUser.getEmail());
        assertNull("Name should handle null", nullUser.getName());
        
        // Test setting null values
        user.setUsername(null);
        user.setPassword(null);
        user.setEmail(null);
        user.setName(null);
        
        assertNull("setUsername should accept null", user.getUsername());
        assertNull("setPassword should accept null", user.getPassword());
        assertNull("setEmail should accept null", user.getEmail());
        assertNull("setName should accept null", user.getName());
    }
}