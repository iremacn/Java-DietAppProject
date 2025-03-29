package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

/**
 * Test class for the DietApp class
 */
public class DietappTest {
    
    private Dietapp dietapp;
    private TestAuthenticationService testAuthService;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    
    /**
     * Custom AuthenticationService for testing that allows us to control its behavior
     */
    private class TestAuthenticationService extends AuthenticationService {
        private boolean registerResult = true;
        private boolean loginResult = true;
        private boolean isLoggedIn = false;
        private User mockUser = null;
        
        public void setRegisterResult(boolean result) {
            this.registerResult = result;
        }
        
        public void setLoginResult(boolean result) {
            this.loginResult = result;
        }
        
        public void setLoggedIn(boolean loggedIn) {
            this.isLoggedIn = loggedIn;
        }
        
        public void setMockUser(User user) {
            this.mockUser = user;
        }
        
        @Override
        public boolean register(String username, String password, String email, String name) {
            return registerResult;
        }
        
        @Override
        public boolean login(String username, String password) {
            if (loginResult) {
                isLoggedIn = true;
                if (mockUser == null) {
                    mockUser = new User(username, password, "test@example.com", "Test User");
                    mockUser.setLoggedIn(true);
                }
            }
            return loginResult;
        }
        
        @Override
        public void logout() {
            isLoggedIn = false;
            mockUser = null;
        }
        
        @Override
        public void enableGuestMode() {
            isLoggedIn = true;
            mockUser = new User("guest", "", "", "Guest User");
            mockUser.setLoggedIn(true);
        }
        
        @Override
        public User getCurrentUser() {
            return mockUser;
        }
        
        @Override
        public boolean isUserLoggedIn() {
            return isLoggedIn;
        }
    }
    
    /**
     * Setup method that runs before each test
     * Initializes the DietApp and replaces its AuthenticationService with our test version
     */
    @Before
    public void setUp() throws Exception {
        // Redirect System.out for testing console output
        System.setOut(new PrintStream(outContent));
        
        // Create the DietApp
        dietapp = new Dietapp();
        
        // Create our test authentication service
        testAuthService = new TestAuthenticationService();
        
        // Replace the authService in DietApp with our test version using reflection
        Field authServiceField = Dietapp.class.getDeclaredField("authService");
        authServiceField.setAccessible(true);
        authServiceField.set(dietapp, testAuthService);
    }
    
    /**
     * Cleanup after tests
     */
    @org.junit.After
    public void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);
    }
    
    /**
     * Test for the constructor
     * Verifies that the constructor initializes the authentication service
     */
    @Test
    public void testConstructor() {
        // Create a new DietApp without our test authentication service
        Dietapp newDietapp = new Dietapp();
        
        // Verify the authentication service was initialized
        assertNotNull("Authentication service should be initialized", newDietapp.getAuthService());
    }
    
    /**
     * Test for getAuthService method
     * Verifies that the method returns the authentication service
     */
    @Test
    public void testGetAuthService() {
        assertSame("getAuthService should return the authentication service", testAuthService, dietapp.getAuthService());
    }
    
    /**
     * Test for registerUser method with valid inputs
     * Verifies that registration works with valid inputs
     */
    @Test
    public void testRegisterUserValidInputs() {
        // Set up test conditions
        testAuthService.setRegisterResult(true);
        
        // Call the method with valid inputs
        boolean result = dietapp.registerUser("testuser", "password", "test@example.com", "Test User");
        
        // Verify the result
        assertTrue("Registration should succeed with valid inputs", result);
    }
    
    /**
     * Test for registerUser method with null or empty username
     * Verifies that registration fails with invalid username
     */
    @Test
    public void testRegisterUserInvalidUsername() {
        // Call with null username
        boolean result1 = dietapp.registerUser(null, "password", "test@example.com", "Test User");
        assertFalse("Registration should fail with null username", result1);
        assertTrue("Console output should contain username error", outContent.toString().contains("Invalid username"));
        
        // Reset output capture
        outContent.reset();
        
        // Call with empty username
        boolean result2 = dietapp.registerUser("", "password", "test@example.com", "Test User");
        assertFalse("Registration should fail with empty username", result2);
        assertTrue("Console output should contain username error", outContent.toString().contains("Invalid username"));
    }
    
    /**
     * Test for registerUser method with null or empty password
     * Verifies that registration fails with invalid password
     */
    @Test
    public void testRegisterUserInvalidPassword() {
        // Call with null password
        boolean result1 = dietapp.registerUser("testuser", null, "test@example.com", "Test User");
        assertFalse("Registration should fail with null password", result1);
        assertTrue("Console output should contain password error", outContent.toString().contains("Invalid password"));
        
        // Reset output capture
        outContent.reset();
        
        // Call with empty password
        boolean result2 = dietapp.registerUser("testuser", "", "test@example.com", "Test User");
        assertFalse("Registration should fail with empty password", result2);
        assertTrue("Console output should contain password error", outContent.toString().contains("Invalid password"));
    }
    
    /**
     * Test for registerUser method with null, empty, or invalid email
     * Verifies that registration fails with invalid email
     */
    @Test
    public void testRegisterUserInvalidEmail() {
        // Call with null email
        boolean result1 = dietapp.registerUser("testuser", "password", null, "Test User");
        assertFalse("Registration should fail with null email", result1);
        assertTrue("Console output should contain email error", outContent.toString().contains("Invalid email"));
        
        // Reset output capture
        outContent.reset();
        
        // Call with empty email
        boolean result2 = dietapp.registerUser("testuser", "password", "", "Test User");
        assertFalse("Registration should fail with empty email", result2);
        assertTrue("Console output should contain email error", outContent.toString().contains("Invalid email"));
        
        // Reset output capture
        outContent.reset();
        
        // Call with invalid email (no @)
        boolean result3 = dietapp.registerUser("testuser", "password", "invalidemail", "Test User");
        assertFalse("Registration should fail with invalid email", result3);
        assertTrue("Console output should contain email error", outContent.toString().contains("Invalid email"));
        
        // Reset output capture
        outContent.reset();
        
        // Call with invalid email (no . after @)
        boolean result4 = dietapp.registerUser("testuser", "password", "invalid@email", "Test User");
        assertFalse("Registration should fail with invalid email", result4);
        assertTrue("Console output should contain email error", outContent.toString().contains("Invalid email"));
    }
    
    /**
     * Test for registerUser method with null or empty name
     * Verifies that registration fails with invalid name
     */
    @Test
    public void testRegisterUserInvalidName() {
        // Call with null name
        boolean result1 = dietapp.registerUser("testuser", "password", "test@example.com", null);
        assertFalse("Registration should fail with null name", result1);
        assertTrue("Console output should contain name error", outContent.toString().contains("Invalid name"));
        
        // Reset output capture
        outContent.reset();
        
        // Call with empty name
        boolean result2 = dietapp.registerUser("testuser", "password", "test@example.com", "");
        assertFalse("Registration should fail with empty name", result2);
        assertTrue("Console output should contain name error", outContent.toString().contains("Invalid name"));
    }
    
    /**
     * Test for registerUser method when registration fails
     * Verifies that the method returns false when the authentication service fails
     */
    @Test
    public void testRegisterUserServiceFailure() {
        // Set up test conditions
        testAuthService.setRegisterResult(false);
        
        // Call the method with valid inputs
        boolean result = dietapp.registerUser("testuser", "password", "test@example.com", "Test User");
        
        // Verify the result
        assertFalse("Registration should fail when service fails", result);
    }
    
    /**
     * Test for loginUser method with valid inputs
     * Verifies that login works with valid inputs
     */
    @Test
    public void testLoginUserValidInputs() {
        // Set up test conditions
        testAuthService.setLoginResult(true);
        
        // Call the method with valid inputs
        boolean result = dietapp.loginUser("testuser", "password");
        
        // Verify the result
        assertTrue("Login should succeed with valid inputs", result);
        assertTrue("User should be logged in after successful login", dietapp.isUserLoggedIn());
    }
    
    /**
     * Test for loginUser method with null or empty username
     * Verifies that login fails with invalid username
     */
    @Test
    public void testLoginUserInvalidUsername() {
        // Call with null username
        boolean result1 = dietapp.loginUser(null, "password");
        assertFalse("Login should fail with null username", result1);
        assertTrue("Console output should contain username error", outContent.toString().contains("Invalid username"));
        
        // Reset output capture
        outContent.reset();
        
        // Call with empty username
        boolean result2 = dietapp.loginUser("", "password");
        assertFalse("Login should fail with empty username", result2);
        assertTrue("Console output should contain username error", outContent.toString().contains("Invalid username"));
    }
    
    /**
     * Test for loginUser method with null or empty password
     * Verifies that login fails with invalid password
     */
    @Test
    public void testLoginUserInvalidPassword() {
        // Call with null password
        boolean result1 = dietapp.loginUser("testuser", null);
        assertFalse("Login should fail with null password", result1);
        assertTrue("Console output should contain password error", outContent.toString().contains("Invalid password"));
        
        // Reset output capture
        outContent.reset();
        
        // Call with empty password
        boolean result2 = dietapp.loginUser("testuser", "");
        assertFalse("Login should fail with empty password", result2);
        assertTrue("Console output should contain password error", outContent.toString().contains("Invalid password"));
    }
    
    /**
     * Test for loginUser method when login fails
     * Verifies that the method returns false when the authentication service fails
     */
    @Test
    public void testLoginUserServiceFailure() {
        // Set up test conditions
        testAuthService.setLoginResult(false);
        
        // Call the method with valid inputs
        boolean result = dietapp.loginUser("testuser", "password");
        
        // Verify the result
        assertFalse("Login should fail when service fails", result);
        assertFalse("User should not be logged in after failed login", dietapp.isUserLoggedIn());
    }
    
    /**
     * Test for logoutUser method when a user is logged in
     * Verifies that logout works when a user is logged in
     */
    @Test
    public void testLogoutUserLoggedIn() {
        // Set up test conditions
        testAuthService.setLoggedIn(true);
        testAuthService.setMockUser(new User("testuser", "password", "test@example.com", "Test User"));
        
        // Call the method
        dietapp.logoutUser();
        
        // Verify the result
        assertFalse("User should not be logged in after logout", dietapp.isUserLoggedIn());
        assertTrue("Console output should indicate successful logout", outContent.toString().contains("Successfully logged out"));
    }
    
    /**
     * Test for logoutUser method when no user is logged in
     * Verifies that logout handles the case when no user is logged in
     */
    @Test
    public void testLogoutUserNotLoggedIn() {
        // Set up test conditions
        testAuthService.setLoggedIn(false);
        
        // Call the method
        dietapp.logoutUser();
        
        // Verify the result
        assertTrue("Console output should indicate error", outContent.toString().contains("Cannot log out"));
    }
    
    /**
     * Test for enableGuestMode method when no user is logged in
     * Verifies that guest mode can be enabled when no user is logged in
     */
    @Test
    public void testEnableGuestModeNotLoggedIn() {
        // Set up test conditions
        testAuthService.setLoggedIn(false);
        
        // Call the method
        dietapp.enableGuestMode();
        
        // Verify the result
        assertTrue("User should be logged in after enabling guest mode", dietapp.isUserLoggedIn());
        User currentUser = dietapp.getCurrentUser();
        assertNotNull("Current user should not be null after enabling guest mode", currentUser);
        assertEquals("Current user should be guest", "guest", currentUser.getUsername());
        assertTrue("Console output should indicate guest mode enabled", outContent.toString().contains("Guest mode enabled"));
    }
    
    /**
     * Test for enableGuestMode method when a user is already logged in
     * Verifies that guest mode cannot be enabled when a user is already logged in
     */
    @Test
    public void testEnableGuestModeAlreadyLoggedIn() {
        // Set up test conditions
        testAuthService.setLoggedIn(true);
        testAuthService.setMockUser(new User("testuser", "password", "test@example.com", "Test User"));
        
        // Call the method
        dietapp.enableGuestMode();
        
        // Verify the result
        assertTrue("User should still be logged in", dietapp.isUserLoggedIn());
        User currentUser = dietapp.getCurrentUser();
        assertNotNull("Current user should not be null", currentUser);
        assertEquals("Current user should still be the original user", "testuser", currentUser.getUsername());
        assertTrue("Console output should indicate error", outContent.toString().contains("Cannot enable guest mode"));
    }
    
    /**
     * Test for isUserLoggedIn method
     * Verifies that the method correctly reports login status
     */
    @Test
    public void testIsUserLoggedIn() {
        // Test when not logged in
        testAuthService.setLoggedIn(false);
        assertFalse("User should not be logged in", dietapp.isUserLoggedIn());
        
        // Test when logged in
        testAuthService.setLoggedIn(true);
        assertTrue("User should be logged in", dietapp.isUserLoggedIn());
    }
    
    /**
     * Test for getCurrentUser method
     * Verifies that the method returns the current user
     */
    @Test
    public void testGetCurrentUser() {
        // Test when no user is logged in
        testAuthService.setLoggedIn(false);
        testAuthService.setMockUser(null);
        assertNull("Current user should be null when not logged in", dietapp.getCurrentUser());
        
        // Test when a user is logged in
        User mockUser = new User("testuser", "password", "test@example.com", "Test User");
        testAuthService.setLoggedIn(true);
        testAuthService.setMockUser(mockUser);
        assertSame("getCurrentUser should return the current user", mockUser, dietapp.getCurrentUser());
    }
    
    /**
     * Test for the private isValidEmail method using reflection
     * Verifies that the method correctly validates email addresses
     */
    @Test
    public void testIsValidEmail() throws Exception {
        // Get access to the private method using reflection
        java.lang.reflect.Method isValidEmailMethod = Dietapp.class.getDeclaredMethod("isValidEmail", String.class);
        isValidEmailMethod.setAccessible(true);
        
        // Test with valid email
        boolean result1 = (boolean) isValidEmailMethod.invoke(dietapp, "test@example.com");
        assertTrue("Valid email should be accepted", result1);
       
        // Test with empty email
        boolean result3 = (boolean) isValidEmailMethod.invoke(dietapp, "");
        assertFalse("Empty email should be rejected", result3);
        
        // Test with whitespace email
        boolean result4 = (boolean) isValidEmailMethod.invoke(dietapp, "   ");
        assertFalse("Whitespace email should be rejected", result4);
        
        // Test with email missing @
        boolean result5 = (boolean) isValidEmailMethod.invoke(dietapp, "invalidemail.com");
        assertFalse("Email without @ should be rejected", result5);
        
        // Test with email having @ at beginning
        boolean result6 = (boolean) isValidEmailMethod.invoke(dietapp, "@example.com");
        assertFalse("Email with @ at beginning should be rejected", result6);
        
        // Test with email missing . after @
        boolean result7 = (boolean) isValidEmailMethod.invoke(dietapp, "test@examplecom");
        assertFalse("Email without . after @ should be rejected", result7);
        
        // Test with email having . immediately after @
        boolean result8 = (boolean) isValidEmailMethod.invoke(dietapp, "test@.example.com");
        assertFalse("Email with . immediately after @ should be rejected", result8);
        
        // Test with email having . at end
        boolean result9 = (boolean) isValidEmailMethod.invoke(dietapp, "test@example.");
        assertFalse("Email with . at end should be rejected", result9);
    }
}