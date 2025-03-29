package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for the AuthenticationService
 */
public class AuthenticationServiceTest {
    
    private AuthenticationService authService;
    private Connection connection;
    
    // Test user data
    private String testUsername;
    private final String TEST_PASSWORD = "test123";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_NAME = "Test User";
    
    /**
     * Setup method that runs before each test
     */
    @Before
    public void setUp() throws Exception {
        // Initialize database
        DatabaseHelper.initializeDatabase();
        
        // Create a unique test username for each test run to avoid conflicts
        testUsername = "testuser_" + System.currentTimeMillis();
        
        // Get a connection for test operations
        connection = DatabaseHelper.getConnection();
        assertNotNull("Database connection should not be null", connection);
        
        // Clear test data to ensure clean state
        clearTestUsers();
        
        // Create the authentication service
        authService = new AuthenticationService();
    }
    
    /**
     * Teardown method that runs after each test
     */
    @After
    public void tearDown() throws Exception {
        // Clean up test data
        clearTestUsers();
        
        // Close the connection
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        
        // Ensure the auth service is logged out
        if (authService.isUserLoggedIn()) {
            authService.logout();
        }
    }
    
    /**
     * Helper method to clear test users from the database
     */
    private void clearTestUsers() {
        try {
            Statement stmt = connection.createStatement();
            stmt.execute("DELETE FROM users WHERE username LIKE 'testuser_%' OR username = 'newuser'");
            stmt.close();
        } catch (SQLException e) {
            // Ignore if tables don't exist yet
        }
    }
    
    /**
     * Helper method to create a test user directly in the database
     */
    private void createTestUserInDB(String username, String password, String email, String name) {
        try {
            PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO users (username, password, email, name, is_logged_in) VALUES (?, ?, ?, ?, 0)");
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, name);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            fail("Failed to create test user: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to check if a user exists in the database
     */
    private boolean userExistsInDB(String username) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT username FROM users WHERE username = ?");
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            boolean exists = rs.next();
            rs.close();
            pstmt.close();
            return exists;
        } catch (SQLException e) {
            fail("Database error while checking user existence: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper method to check if a user is logged in in the database
     */
    private boolean isUserLoggedInDB(String username) {
        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT is_logged_in FROM users WHERE username = ?");
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                boolean isLoggedIn = rs.getInt("is_logged_in") == 1;
                rs.close();
                pstmt.close();
                return isLoggedIn;
            }
            
            rs.close();
            pstmt.close();
            return false;
        } catch (SQLException e) {
            fail("Database error while checking login status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Test for constructor
     * Verifies that the constructor initializes currentUser to null
     */
    @Test
    public void testConstructor() {
        assertNull("Current user should be null after construction", authService.getCurrentUser());
        assertFalse("User should not be logged in after construction", authService.isUserLoggedIn());
    }
    
    /**
     * Test for register method with valid inputs
     * Verifies that a new user can be registered correctly
     */
    @Test
    public void testRegisterValidUser() {
        boolean result = authService.register(testUsername, TEST_PASSWORD, TEST_EMAIL, TEST_NAME);
        
        assertTrue("Registration should succeed with valid inputs", result);
        assertTrue("User should exist in database after registration", userExistsInDB(testUsername));
        assertFalse("New user should not be automatically logged in", authService.isUserLoggedIn());
    }
    
    /**
     * Test for register method with duplicate username
     * Verifies that registration fails when username already exists
     */
    @Test
    public void testRegisterDuplicateUsername() {
        // First register a user
        boolean result1 = authService.register(testUsername, TEST_PASSWORD, TEST_EMAIL, TEST_NAME);
        assertTrue("First registration should succeed", result1);
        
        // Then try to register with the same username
        boolean result2 = authService.register(testUsername, "different", "different@example.com", "Different User");
        assertFalse("Registration should fail with duplicate username", result2);
    }
    
    /**
     * Test for register method with invalid inputs
     * Verifies that registration fails with null or empty input parameters
     */
    @Test
    public void testRegisterInvalidInputs() {
        // Test with null username
        boolean result1 = authService.register(null, TEST_PASSWORD, TEST_EMAIL, TEST_NAME);
        assertFalse("Registration should fail with null username", result1);
        
        // Test with empty username
        boolean result2 = authService.register("", TEST_PASSWORD, TEST_EMAIL, TEST_NAME);
        assertFalse("Registration should fail with empty username", result2);
        
        // Test with null password
        boolean result3 = authService.register(testUsername, null, TEST_EMAIL, TEST_NAME);
        assertFalse("Registration should fail with null password", result3);
        
        // Test with empty password
        boolean result4 = authService.register(testUsername, "", TEST_EMAIL, TEST_NAME);
        assertFalse("Registration should fail with empty password", result4);
        
        // Test with null email
        boolean result5 = authService.register(testUsername, TEST_PASSWORD, null, TEST_NAME);
        assertFalse("Registration should fail with null email", result5);
        
        // Test with empty email
        boolean result6 = authService.register(testUsername, TEST_PASSWORD, "", TEST_NAME);
        assertFalse("Registration should fail with empty email", result6);
        
        // Test with null name
        boolean result7 = authService.register(testUsername, TEST_PASSWORD, TEST_EMAIL, null);
        assertFalse("Registration should fail with null name", result7);
        
        // Test with empty name
        boolean result8 = authService.register(testUsername, TEST_PASSWORD, TEST_EMAIL, "");
        assertFalse("Registration should fail with empty name", result8);
    }
    
    /**
     * Test for login method with valid credentials
     * Verifies that a user can login with correct credentials
     */
    @Test
    public void testLoginValidCredentials() {
        // First register a user
        authService.register(testUsername, TEST_PASSWORD, TEST_EMAIL, TEST_NAME);
        
        // Then try to login
        boolean loginResult = authService.login(testUsername, TEST_PASSWORD);
        
        assertTrue("Login should succeed with valid credentials", loginResult);
        assertTrue("User should be logged in after successful login", authService.isUserLoggedIn());
        assertTrue("User should be marked as logged in in database", isUserLoggedInDB(testUsername));
        
        // Check current user properties
        User currentUser = authService.getCurrentUser();
        assertNotNull("Current user should not be null after login", currentUser);
        assertEquals("Current user username should match", testUsername, currentUser.getUsername());
        assertEquals("Current user email should match", TEST_EMAIL, currentUser.getEmail());
        assertEquals("Current user name should match", TEST_NAME, currentUser.getName());
        assertTrue("Current user should be marked as logged in", currentUser.isLoggedIn());
    }
    
    /**
     * Test for login method with invalid credentials
     * Verifies that login fails with incorrect credentials
     */
    @Test
    public void testLoginInvalidCredentials() {
        // First register a user
        authService.register(testUsername, TEST_PASSWORD, TEST_EMAIL, TEST_NAME);
        
        // Try to login with wrong password
        boolean result1 = authService.login(testUsername, "wrongpassword");
        assertFalse("Login should fail with wrong password", result1);
        assertFalse("User should not be logged in after failed login", authService.isUserLoggedIn());
        
        // Try to login with non-existent username
        boolean result2 = authService.login("nonexistentuser", TEST_PASSWORD);
        assertFalse("Login should fail with non-existent username", result2);
        assertFalse("User should not be logged in after failed login", authService.isUserLoggedIn());
    }
    
    /**
     * Test for logout method
     * Verifies that a user can logout correctly
     */
    @Test
    public void testLogout() {
        // First register and login a user
        authService.register(testUsername, TEST_PASSWORD, TEST_EMAIL, TEST_NAME);
        authService.login(testUsername, TEST_PASSWORD);
        
        // Verify login was successful
        assertTrue("User should be logged in before logout test", authService.isUserLoggedIn());
        
        // Test logout
        authService.logout();
        
        // Verify logout effects
        assertFalse("User should not be logged in after logout", authService.isUserLoggedIn());
        assertNull("Current user should be null after logout", authService.getCurrentUser());
        assertFalse("User should be marked as logged out in database", isUserLoggedInDB(testUsername));
    }
    
    /**
     * Test for logout method when no user is logged in
     * Verifies that logout works safely when no user is logged in
     */
    @Test
    public void testLogoutWhenNotLoggedIn() {
        // Ensure no user is logged in
        assertFalse("No user should be logged in initially", authService.isUserLoggedIn());
        
        // Test logout without a logged in user
        authService.logout();
        
        // Should still have no user logged in
        assertFalse("No user should be logged in after logout", authService.isUserLoggedIn());
        assertNull("Current user should still be null", authService.getCurrentUser());
    }
    
    /**
     * Test for enableGuestMode method
     * Verifies that guest mode can be enabled correctly
     */
    @Test
    public void testEnableGuestMode() {
        // Enable guest mode
        authService.enableGuestMode();
        
        // Verify guest mode effects
        assertTrue("User should be logged in after enabling guest mode", authService.isUserLoggedIn());
        
        User guestUser = authService.getCurrentUser();
        assertNotNull("Current user should not be null in guest mode", guestUser);
        assertEquals("Guest username should be 'guest'", "guest", guestUser.getUsername());
        assertEquals("Guest name should be 'Guest User'", "Guest User", guestUser.getName());
        assertTrue("Guest user should be marked as logged in", guestUser.isLoggedIn());
    }
    
    /**
     * Test for getCurrentUser method
     * Verifies that getCurrentUser returns the correct user when logged in and null when not logged in
     */
    @Test
    public void testGetCurrentUser() {
        // Initially no user should be logged in
        assertNull("Current user should be null initially", authService.getCurrentUser());
        
        // Register and login a user
        authService.register(testUsername, TEST_PASSWORD, TEST_EMAIL, TEST_NAME);
        authService.login(testUsername, TEST_PASSWORD);
        
        // Check current user after login
        User currentUser = authService.getCurrentUser();
        assertNotNull("Current user should not be null after login", currentUser);
        assertEquals("Current user username should match", testUsername, currentUser.getUsername());
        
        // Logout and check current user is null
        authService.logout();
        assertNull("Current user should be null after logout", authService.getCurrentUser());
    }
    
    /**
     * Test for isUserLoggedIn method
     * Verifies that isUserLoggedIn returns true when a user is logged in and false otherwise
     */
    @Test
    public void testIsUserLoggedIn() {
        // Initially no user should be logged in
        assertFalse("No user should be logged in initially", authService.isUserLoggedIn());
        
        // Register and login a user
        authService.register(testUsername, TEST_PASSWORD, TEST_EMAIL, TEST_NAME);
        authService.login(testUsername, TEST_PASSWORD);
        
        // Check logged in status after login
        assertTrue("User should be logged in after login", authService.isUserLoggedIn());
        
        // Logout and check logged in status
        authService.logout();
        assertFalse("No user should be logged in after logout", authService.isUserLoggedIn());
        
        // Enable guest mode and check logged in status
        authService.enableGuestMode();
        assertTrue("User should be logged in after enabling guest mode", authService.isUserLoggedIn());
    }
    
    /**
     * Test exception handling in register method
     * Verifies that the method handles SQLException gracefully
     */
    @Test
    public void testRegisterSQLException() {
        // We'll use a helper class to simulate database connection issues
        try {
            // Force database to be temporarily unavailable by closing and reopening
            DatabaseHelper.closeAllConnections();
            
            // Clear the connection pool to force a new connection attempt
            resetConnectionPool();
            
            // Try to register a user - should handle the exception gracefully
            boolean result = authService.register("newuser", "password", "email@test.com", "Test Name");
            
            // Should return false but not throw exception
            assertFalse("Register should return false on database error", result);
            
            // Reinitialize database for other tests
            DatabaseHelper.initializeDatabase();
        } catch (Exception e) {
            fail("Method should handle SQLException gracefully: " + e.getMessage());
        }
    }
    
    /**
     * Test exception handling in login method
     * Verifies that the method handles SQLException gracefully
     */
    @Test
    public void testLoginSQLException() {
        try {
            // Force database to be temporarily unavailable by closing and reopening
            DatabaseHelper.closeAllConnections();
            
            // Clear the connection pool to force a new connection attempt
            resetConnectionPool();
            
            // Try to login - should handle the exception gracefully
            boolean result = authService.login("someuser", "somepassword");
            
            // Should return false but not throw exception
            assertFalse("Login should return false on database error", result);
            
            // Reinitialize database for other tests
            DatabaseHelper.initializeDatabase();
        } catch (Exception e) {
            fail("Method should handle SQLException gracefully: " + e.getMessage());
        }
    }
    
    /**
     * Test exception handling in logout method
     * Verifies that the method handles SQLException gracefully
     */
    @Test
    public void testLogoutSQLException() {
        try {
            // First register and login a user so we have a current user
            authService.register(testUsername, TEST_PASSWORD, TEST_EMAIL, TEST_NAME);
            authService.login(testUsername, TEST_PASSWORD);
            
            // Verify user is logged in
            assertTrue("User should be logged in before test", authService.isUserLoggedIn());
            
            // Force database issues
            DatabaseHelper.closeAllConnections();
            resetConnectionPool();
            
            // Try to logout - should handle exception gracefully
            authService.logout();
            
            // User should still be logged out in memory even if DB update failed
            assertNull("Current user should be null after logout despite DB error", authService.getCurrentUser());
            assertFalse("User should not be logged in after logout despite DB error", authService.isUserLoggedIn());
            
            // Reinitialize database for other tests
            DatabaseHelper.initializeDatabase();
        } catch (Exception e) {
            fail("Method should handle SQLException gracefully: " + e.getMessage());
        }
    }
    
    /**
     * Test exception handling in getAllUsers method
     * Verifies that the method handles SQLException gracefully
     */
    @Test
    public void testGetAllUsersSQLException() {
        try {
            // Force database issues
            DatabaseHelper.closeAllConnections();
            resetConnectionPool();
            
            // Try to get all users - should handle exception gracefully
            List<User> users = authService.getAllUsers();
            
            // Should return an empty list but not throw exception
            assertNotNull("getAllUsers should return an empty list on database error", users);
            assertTrue("getAllUsers should return an empty list on database error", users.isEmpty());
            
            // Reinitialize database for other tests
            DatabaseHelper.initializeDatabase();
        } catch (Exception e) {
            fail("Method should handle SQLException gracefully: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to reset the connection pool for testing database errors
     */
    private void resetConnectionPool() throws Exception {
        java.lang.reflect.Field connectionPoolField = DatabaseHelper.class.getDeclaredField("connectionPool");
        connectionPoolField.setAccessible(true);
        connectionPoolField.set(null, new ArrayList<>());
    }
}