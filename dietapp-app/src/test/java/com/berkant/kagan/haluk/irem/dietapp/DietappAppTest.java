package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

/**
 * Test class for DietappApp
 * Simple unit tests that focus on testing components individually
 */
public class DietappAppTest {
    
    private DietappApp app;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;
    private TestDietapp testDietApp;
    
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
        
        public void setLoggedIn(boolean loggedIn) {
            this.isLoggedIn = loggedIn;
        }
        
        public void setMockUser(User user) {
            this.mockUser = user;
        }
        
        @Override
        public boolean registerUser(String username, String password, String email, String name) {
            return registerResult;
        }
        
        @Override
        public boolean loginUser(String username, String password) {
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
        public void logoutUser() {
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
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        
        // Create app instance 
        app = new DietappApp();
        
        // Create test Dietapp 
        testDietApp = new TestDietapp();
        
        // Replace real Dietapp with test version
        try {
            Field dietAppField = DietappApp.class.getDeclaredField("dietApp");
            dietAppField.setAccessible(true);
            dietAppField.set(app, testDietApp);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup after each test
     */
    @After
    public void tearDown() {
        System.setOut(originalOut);
    }
    
    /**
     * Simple test for dietApp initialization
     */
    @Test
    public void testDietAppIsInitialized() {
        try {
            Field dietAppField = DietappApp.class.getDeclaredField("dietApp");
            dietAppField.setAccessible(true);
            Object dietApp = dietAppField.get(app);
            
            assertNotNull("dietApp should be initialized", dietApp);
            assertTrue("dietApp should be an instance of TestDietapp", dietApp instanceof TestDietapp);
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for basic scanner initialization
     */
    @Test
    public void testScannerIsInitialized() {
        try {
            Field scannerField = DietappApp.class.getDeclaredField("scanner");
            scannerField.setAccessible(true);
            Object scanner = scannerField.get(app);
            
            assertNotNull("scanner should be initialized", scanner);
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for service fields initialization
     */
    @Test
    public void testServicesAreInitialized() {
        try {
            // Test a sample of services
            checkFieldInitialized("mealPlanningService");
            checkFieldInitialized("calorieNutrientService");
            checkFieldInitialized("shoppingListService");
            checkFieldInitialized("personalizedDietService");
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for menu fields initialization
     */
    @Test
    public void testMenusAreInitialized() {
        try {
            // Test a sample of menus
            checkFieldInitialized("mealPlanningMenu");
            checkFieldInitialized("calorieNutrientMenu");
            checkFieldInitialized("shoppingListMenu");
            checkFieldInitialized("personalizedDietMenu");
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to check if a field is initialized
     */
    private void checkFieldInitialized(String fieldName) throws Exception {
        Field field = DietappApp.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(app);
        assertNotNull(fieldName + " should be initialized", value);
    }
    
    /**
     * Test for email validation with valid email
     */
    @Test
    public void testIsValidEmailWithValidEmail() {
        try {
            // Access the private method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("isValidEmail", String.class);
            method.setAccessible(true);
            
            // Test with valid email
            Boolean result = (Boolean) method.invoke(app, "test@example.com");
            assertTrue("Valid email should be accepted", result);
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for email validation with null email
     */
    @Test
    public void testIsValidEmailWithNullEmail() {
        try {
            // Access the private method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("isValidEmail", String.class);
            method.setAccessible(true);
            
            // Test with null
            Boolean result = (Boolean) method.invoke(app, (Object) null);
            assertFalse("Null email should be rejected", result);
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for email validation with empty email
     */
    @Test
    public void testIsValidEmailWithEmptyEmail() {
        try {
            // Access the private method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("isValidEmail", String.class);
            method.setAccessible(true);
            
            // Test with empty string
            Boolean result = (Boolean) method.invoke(app, "");
            assertFalse("Empty email should be rejected", result);
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for email validation with invalid email (no @)
     */
    @Test
    public void testIsValidEmailWithoutAtSymbol() {
        try {
            // Access the private method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("isValidEmail", String.class);
            method.setAccessible(true);
            
            // Test with invalid email (no @)
            Boolean result = (Boolean) method.invoke(app, "invalid");
            assertFalse("Email without @ should be rejected", result);
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for email validation with invalid email (no domain)
     */
    @Test
    public void testIsValidEmailWithoutDomain() {
        try {
            // Access the private method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("isValidEmail", String.class);
            method.setAccessible(true);
            
            // Test with invalid email (no domain)
            Boolean result = (Boolean) method.invoke(app, "invalid@");
            assertFalse("Email without domain should be rejected", result);
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test for handling guest mode
     */
    @Test
    public void testHandleGuestMode() {
        try {
            // Access the private method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleGuestMode");
            method.setAccessible(true);
            
            // Call the method
            method.invoke(app);
            
            // Verify guest mode was enabled
            assertTrue("User should be logged in after enabling guest mode", testDietApp.isUserLoggedIn());
            assertNotNull("Current user should not be null", testDietApp.getCurrentUser());
            assertEquals("Current user should be guest", "guest", testDietApp.getCurrentUser().getUsername());
            
            // Check output
            String output = outContent.toString();
            assertTrue("Output should mention guest mode", 
                       output.contains("guest") || output.contains("Guest"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test that handleAuthMenu with exit option returns false
     */
    @Test
    public void testHandleAuthMenuExit() {
        try {
            // Access the private method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleAuthMenu", int.class);
            method.setAccessible(true);
            
            // Call with exit option (0)
            Boolean result = (Boolean) method.invoke(app, 0);
            
            // Should return false (exit the app)
            assertFalse("handleAuthMenu should return false for exit option", result);
            
            // Check output
            String output = outContent.toString();
            assertTrue("Output should contain goodbye message", output.contains("Goodbye"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test that handleAuthMenu with invalid option returns true
     */
    @Test
    public void testHandleAuthMenuInvalidOption() {
        try {
            // Access the private method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleAuthMenu", int.class);
            method.setAccessible(true);
            
            // Call with invalid option (99)
            Boolean result = (Boolean) method.invoke(app, 99);
            
            // Should return true (continue the app)
            assertTrue("handleAuthMenu should return true for invalid option", result);
            
            // Check output
            String output = outContent.toString();
            assertTrue("Output should contain invalid selection message", 
                       output.contains("Invalid selection"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test that handleUserMainMenu with exit option returns false
     */
    @Test
    public void testHandleUserMainMenuExit() {
        try {
            // Access the private method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleUserMainMenu", int.class);
            method.setAccessible(true);
            
            // Call with exit option (0)
            Boolean result = (Boolean) method.invoke(app, 0);
            
            // Should return false (exit the app)
            assertFalse("handleUserMainMenu should return false for exit option", result);
            
            // Check output
            String output = outContent.toString();
            assertTrue("Output should contain goodbye message", output.contains("Goodbye"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test that handleUserMainMenu with logout option returns true
     */
    @Test
    public void testHandleUserMainMenuLogout() {
        try {
            // Set up test conditions
            testDietApp.setLoggedIn(true);
            testDietApp.setMockUser(new User("testuser", "password", "test@example.com", "Test User"));
            
            // Access the private method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleUserMainMenu", int.class);
            method.setAccessible(true);
            
            // Call with logout option (5)
            Boolean result = (Boolean) method.invoke(app, 5);
            
            // Should return true (continue the app)
            assertTrue("handleUserMainMenu should return true for logout option", result);
            
            // Check if user was logged out
            assertFalse("User should be logged out", testDietApp.isUserLoggedIn());
            
            // Check output
            String output = outContent.toString();
            assertTrue("Output should contain logged out message", 
                       output.contains("logged out"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test that handleUserMainMenu with invalid option returns true
     */
    @Test
    public void testHandleUserMainMenuInvalidOption() {
        try {
            // Access the private method
            java.lang.reflect.Method method = DietappApp.class.getDeclaredMethod("handleUserMainMenu", int.class);
            method.setAccessible(true);
            
            // Call with invalid option (99)
            Boolean result = (Boolean) method.invoke(app, 99);
            
            // Should return true (continue the app)
            assertTrue("handleUserMainMenu should return true for invalid option", result);
            
            // Check output
            String output = outContent.toString();
            assertTrue("Output should contain invalid selection message", 
                       output.contains("Invalid selection"));
        } catch (Exception e) {
            fail("Test failed: " + e.getMessage());
        }
    }
}