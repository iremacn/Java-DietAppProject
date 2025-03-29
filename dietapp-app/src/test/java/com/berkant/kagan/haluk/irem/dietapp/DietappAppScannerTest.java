package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Scanner;

/**
 * Test class for DietappApp's methods that use Scanner
 * Focuses specifically on login and registration functionality
 */
public class DietappAppScannerTest {
    
    private DietappApp app;
    private TestDietapp testDietApp;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    
    /**
     * A test double for Dietapp to avoid database operations
     */
    private class TestDietapp extends Dietapp {
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
        
        @Override
        public boolean registerUser(String username, String password, String email, String name) {
            // Simple implementation that doesn't hit the database
            return registerResult;
        }
        
        @Override
        public boolean loginUser(String username, String password) {
            // If login would succeed, set up the user
            if (loginResult) {
                isLoggedIn = true;
                mockUser = new User(username, password, "test@example.com", "Test User");
                mockUser.setLoggedIn(true);
            }
            return loginResult;
        }
        
        @Override
        public boolean isUserLoggedIn() {
            return isLoggedIn;
        }
        
        @Override
        public User getCurrentUser() {
            return mockUser;
        }
    }
    
    /**
     * Setup method that runs before each test
     */
    @Before
    public void setUp() {
        // Redirect System.out
        System.setOut(new PrintStream(outContent));
        
        // Create the app
        app = new DietappApp();
        
        // Create and inject our test Dietapp
        testDietApp = new TestDietapp();
        injectTestDietapp();
    }
    
    /**
     * Helper to inject our test Dietapp
     */
    private void injectTestDietapp() {
        try {
            // Replace the real Dietapp with our test version
            Field dietAppField = DietappApp.class.getDeclaredField("dietApp");
            dietAppField.setAccessible(true);
            dietAppField.set(app, testDietApp);
        } catch (Exception e) {
            fail("Could not inject test Dietapp: " + e.getMessage());
        }
    }
    
    /**
     * Injects a custom scanner that will return predefined inputs
     */
    private void injectTestScanner(String simulatedInput) {
        try {
            // Create a new scanner with our simulated input
            ByteArrayInputStream testIn = new ByteArrayInputStream(simulatedInput.getBytes());
            System.setIn(testIn);
            Scanner testScanner = new Scanner(System.in);
            
            // Replace the scanner in the app
            Field scannerField = DietappApp.class.getDeclaredField("scanner");
            scannerField.setAccessible(true);
            
            // Close the old scanner to prevent resource leaks
            Scanner oldScanner = (Scanner) scannerField.get(app);
            if (oldScanner != null) {
                oldScanner.close();
            }
            
            // Set our test scanner
            scannerField.set(app, testScanner);
        } catch (Exception e) {
            fail("Could not inject test scanner: " + e.getMessage());
        }
    }
    
    /**
     * Teardown method to restore standard I/O
     */
    @After
    public void tearDown() {
        // Reset System.out and System.in
        System.setOut(originalOut);
        System.setIn(originalIn);
        
        // Close any open scanners
        try {
            Field scannerField = DietappApp.class.getDeclaredField("scanner");
            scannerField.setAccessible(true);
            Scanner scanner = (Scanner) scannerField.get(app);
            if (scanner != null) {
                scanner.close();
            }
        } catch (Exception e) {
            // Ignore errors in teardown
        }
    }
    
    /**
     * Test successful registration
     */
    @Test
    public void testHandleRegistrationSuccess() {
        // Set up test conditions
        testDietApp.setRegisterResult(true);
        
        // Simulated user input: username, password, valid email, name
        String input = "testuser\npassword123\nvalid@example.com\nTest User\n";
        injectTestScanner(input);
        
        try {
            // Invoke the method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleRegistration");
            method.setAccessible(true);
            method.invoke(app);
            
            // Check output
            String output = outContent.toString();
            assertTrue("Should show registration success message", 
                      output.contains("Registration successful"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test failed registration
     */
    @Test
    public void testHandleRegistrationFailure() {
        // Set up test conditions - registration will fail
        testDietApp.setRegisterResult(false);
        
        // Simulated user input: username, password, valid email, name
        String input = "testuser\npassword123\nvalid@example.com\nTest User\n";
        injectTestScanner(input);
        
        try {
            // Invoke the method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleRegistration");
            method.setAccessible(true);
            method.invoke(app);
            
            // Check output
            String output = outContent.toString();
            assertTrue("Should show registration failure message", 
                      output.contains("Registration failed"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for invalid inputs during registration
     */
    @Test
    public void testHandleRegistrationInvalidInputs() {
        // Simulate a user first entering empty inputs then valid ones
        String input = "\n\nvalid\ninvalidemail\nvalid@example.com\n\nTest User\n";
        injectTestScanner(input);
        
        try {
            // Invoke the method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleRegistration");
            method.setAccessible(true);
            method.invoke(app);
            
            // Check output
            String output = outContent.toString();
            assertTrue("Should prompt for username again when empty", 
                      output.contains("Username cannot be empty"));
            assertTrue("Should prompt for password again when empty", 
                      output.contains("Password cannot be empty"));
            assertTrue("Should prompt for valid email", 
                      output.contains("Invalid email format"));
            assertTrue("Should prompt for name again when empty", 
                      output.contains("Name cannot be empty"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test successful login
     */
    @Test
    public void testHandleLoginSuccess() {
        // Set up test conditions
        testDietApp.setLoginResult(true);
        
        // Simulated user input: username and password
        String input = "testuser\npassword123\n";
        injectTestScanner(input);
        
        try {
            // Create a mock user for the welcome message
            User mockUser = new User("testuser", "password123", "test@example.com", "Test User");
            mockUser.setLoggedIn(true);
            testDietApp.mockUser = mockUser;
            
            // Invoke the method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleLogin");
            method.setAccessible(true);
            method.invoke(app);
            
            // Check output
            String output = outContent.toString();
            assertTrue("Should show login success message", 
                      output.contains("Login successful"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test failed login
     */
    @Test
    public void testHandleLoginFailure() {
        // Set up test conditions - login will fail
        testDietApp.setLoginResult(false);
        
        // Simulated user input: username and password
        String input = "testuser\npassword123\n";
        injectTestScanner(input);
        
        try {
            // Invoke the method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleLogin");
            method.setAccessible(true);
            method.invoke(app);
            
            // Check output
            String output = outContent.toString();
            assertTrue("Should show login failure message", 
                      output.contains("Login failed"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for invalid inputs during login
     */
    @Test
    public void testHandleLoginInvalidInputs() {
        // Simulate a user first entering empty inputs then valid ones
        String input = "\ntestuser\n\npassword123\n";
        injectTestScanner(input);
        
        try {
            // Invoke the method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleLogin");
            method.setAccessible(true);
            method.invoke(app);
            
            // Check output
            String output = outContent.toString();
            assertTrue("Should prompt for username again when empty", 
                      output.contains("Username cannot be empty"));
            assertTrue("Should prompt for password again when empty", 
                      output.contains("Password cannot be empty"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for the handleUserMainMenu method
     */
    @Test
    public void testHandleUserMainMenuLogout() {
        try {
            // Invoke the method with logout option (5)
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleUserMainMenu", int.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(app, 5);
            
            // Should return true to continue running
            assertTrue("Should return true for logout option", result);
            
            // Check output
            String output = outContent.toString();
            assertTrue("Should show logged out message", 
                      output.contains("logged out"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for the handleUserMainMenu method with exit option
     */
    @Test
    public void testHandleUserMainMenuExit() {
        try {
            // Invoke the method with exit option (0)
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleUserMainMenu", int.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(app, 0);
            
            // Should return false to exit
            assertFalse("Should return false for exit option", result);
            
            // Check output
            String output = outContent.toString();
            assertTrue("Should show goodbye message", 
                      output.contains("Goodbye"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
}