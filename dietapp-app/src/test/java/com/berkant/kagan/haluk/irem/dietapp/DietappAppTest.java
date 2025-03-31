package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Unit tests for the DietappApp class.
 * @author Claude
 */
public class DietappAppTest {

    private DietappApp app;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private InputStream originalIn;

    @Before
    public void setUp() {
        // Initialize DatabaseHelper for tests
        DatabaseHelper.initializeDatabase();
        
        // Save original System.out and System.in
        originalOut = System.out;
        originalIn = System.in;
        
        // Setup output capture
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        
        // Create the app instance
        app = new DietappApp();
    }
    
    @After
    public void tearDown() {
        // Restore original System.out and System.in
        System.setOut(originalOut);
        System.setIn(originalIn);
        
        // Close database connections
        DatabaseHelper.closeConnection();
    }
    
   
    
    @Test
    public void testMain() {
        // Test main method with direct call
        String[] args = new String[0];
        try {
            // Redirect standard input to provide input for the app
            String input = "0\n"; // Just exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            
            // Call main method directly
            DietappApp.main(args);
            
            // Verify welcome message was displayed
            String output = outputStream.toString();
            assertTrue("Welcome message should be displayed", 
                       output.contains("Welcome to Diet Planner Application!"));
        } catch (Exception e) {
            fail("Main method threw an exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testAuthMenuDisplay() {
        // Simulate user selecting exit option (0)
        String input = "0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify auth menu was displayed
        String output = outputStream.toString();
        assertTrue("Auth menu should be displayed", output.contains("===== Diet Planner Authentication ====="));
        assertTrue("Login option should be displayed", output.contains("1. Login"));
        assertTrue("Register option should be displayed", output.contains("2. Register"));
        assertTrue("Guest mode option should be displayed", output.contains("3. Continue as Guest"));
        assertTrue("Exit option should be displayed", output.contains("0. Exit"));
        assertTrue("Exit message should be displayed", output.contains("Thank you for using Diet Planner. Goodbye!"));
    }
    
 
    
    @Test
    public void testNonNumericAuthMenuOption() {
        // Simulate user entering non-numeric option then exit
        String input = "abc\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify error message was displayed
        String output = outputStream.toString();
        assertTrue("Invalid selection message should be displayed", 
                  output.contains("Invalid selection. Please enter only numbers."));
    }
    
    @Test
    public void testEmptyAuthMenuOption() {
        // Simulate user entering empty input then exit
        String input = "\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify error message was displayed
        String output = outputStream.toString();
        assertTrue("Invalid selection message should be displayed", 
                  output.contains("Empty input. Please enter a number."));
    }
    
    @Test
    public void testRegistrationWithValidInput() {
        // Generate a unique test username to avoid conflicts
        String testUsername = "testuser_" + System.currentTimeMillis();
        
        // Simulate user registering with valid inputs then exiting
        String input = "2\n" + // Register option
                       testUsername + "\n" + // Username
                       "test123\n" + // Password
                       "test@example.com\n" + // Email
                       "Test User\n" + // Name
                       "0\n"; // Exit
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify registration success message was displayed
        String output = outputStream.toString();
        assertTrue("Registration success message should be displayed", 
                  output.contains("Registration successful! You can now log in."));
    }
    
    
    
    @Test
    public void testRegistrationWithInvalidUsername() {
        // Simulate user registering with invalid username (empty) then valid details
        String testUsername = "testuser_" + System.currentTimeMillis();
        String input = "2\n" + // Register option
                       "\n" + // Empty username
                       testUsername + "\n" + // Valid username
                       "test123\n" + // Password
                       "test@example.com\n" + // Email
                       "Test User\n" + // Name
                       "0\n"; // Exit
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify error message was displayed
        String output = outputStream.toString();
        assertTrue("Invalid username message should be displayed", 
                  output.contains("Username cannot be empty. Please try again."));
    }
    
    @Test
    public void testLoginWithValidCredentials() {
        // First register a test user
        String testUsername = "testuser_login_" + System.currentTimeMillis();
        String testPassword = "test123";
        
        // Create app instance
        DietappApp registerApp = new DietappApp();
        
        // Get the dietApp field using reflection
        Dietapp dietApp = null;
        try {
            Field dietAppField = DietappApp.class.getDeclaredField("dietApp");
            dietAppField.setAccessible(true);
            dietApp = (Dietapp) dietAppField.get(registerApp);
        } catch (Exception e) {
            fail("Could not access dietApp field: " + e.getMessage());
        }
        
        // Register a test user directly
        boolean registrationResult = dietApp.registerUser(testUsername, testPassword, "test@example.com", "Test User");
        assertTrue("Test user registration should succeed", registrationResult);
        
        // Now test login with those credentials
        String input = "1\n" + // Login option
                       testUsername + "\n" + // Username
                       testPassword + "\n" + // Password
                       "5\n" + // Logout option
                       "0\n"; // Exit
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Reset output stream for login test
        outputStream.reset();
        
        // Create a new instance with our controlled input
        DietappApp loginApp = new DietappApp();
        loginApp.run();
        
        // Verify login success and main menu was displayed
        String output = outputStream.toString();
        assertTrue("Login success message should be displayed", 
                  output.contains("Login successful! Welcome, Test User!"));
        assertTrue("Main menu should be displayed after login", 
                  output.contains("===== Diet Planner Main Menu ====="));
    }
    
   
    
    @Test
    public void testGuestMode() {
        // Simulate guest mode selection then logout
        String input = "3\n" + // Guest mode option
                       "5\n" + // Logout option
                       "0\n"; // Exit
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify guest mode message and main menu was displayed
        String output = outputStream.toString();
        assertTrue("Guest mode message should be displayed", 
                  output.contains("You are now using the application as a guest. Some features may be limited."));
        assertTrue("Main menu should be displayed after enabling guest mode", 
                  output.contains("===== Diet Planner Main Menu ====="));
        assertTrue("Should show logged in as guest", 
                  output.contains("Logged in as: guest"));
    }
    
    
    
   
    
    @Test
    public void testMealPlanningMenuNavigation() {
        // Test navigation to Meal Planning Menu and interaction
        String input = "3\n" + // Guest mode
                       "1\n" + // Meal Planning Menu
                       "1\n" + // Plan Meals
                       "2025\n" + // Year
                       "6\n" + // Month
                       "15\n" + // Day
                       "1\n" + // Breakfast
                       "1\n" + // First option
                       "0\n" + // Return to Main Menu
                       "5\n" + // Logout
                       "0\n"; // Exit
                       
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify Meal Planning menu interactions
        String output = outputStream.toString();
        assertTrue("Plan Meals option should be displayed", 
                  output.contains("===== Plan Meals ====="));
        assertTrue("Date entry should be processed", 
                  output.contains("Enter Date:"));
    }
    
    @Test
    public void testCalorieTrackingMenuNavigation() {
        // Test navigation to Calorie Tracking Menu and interaction
        String input = "3\n" + // Guest mode
                       "2\n" + // Calorie Tracking Menu
                       "5\n" + // Browse Common Foods
                       "\n" + // Press Enter to continue
                       "0\n" + // Return to Main Menu
                       "5\n" + // Logout
                       "0\n"; // Exit
                       
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify Calorie Tracking menu interactions
        String output = outputStream.toString();
        assertTrue("Common Foods option should be displayed and processed", 
                  output.contains("===== Common Foods with Nutrients ====="));
    }
    
   
    
    @Test
    public void testSetNutritionGoals() {
        // Test Calorie Tracking's set nutrition goals feature
        String input = "3\n" + // Guest mode
                       "2\n" + // Calorie Tracking Menu
                       "1\n" + // Set Nutrition Goals
                       "2000\n" + // Calories
                       "60\n" + // Protein
                       "250\n" + // Carbs
                       "70\n" + // Fat
                       "0\n" + // Return to Main Menu
                       "5\n" + // Logout
                       "0\n"; // Exit
                       
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify nutrition goals setting
        String output = outputStream.toString();
        assertTrue("Nutrition goals should be set", 
                  output.contains("===== Set Nutrition Goals ====="));
    }
    
    @Test
    public void testNutritionReport() {
        // Test Calorie Tracking's daily nutrition report feature
        String input = "3\n" + // Guest mode
                       "2\n" + // Calorie Tracking Menu
                       "2\n" + // View Daily Nutrition Report
                       "2025\n" + // Year
                       "5\n" + // Month
                       "15\n" + // Day
                       "\n" + // Press Enter to continue
                       "0\n" + // Return to Main Menu
                       "5\n" + // Logout
                       "0\n"; // Exit
                       
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify nutrition report generation
        String output = outputStream.toString();
        assertTrue("Daily nutrition report should be displayed", 
                  output.contains("===== Daily Nutrition Report ====="));
    }
    
    @Test
    public void testWeeklyNutritionReport() {
        // Test Calorie Tracking's weekly nutrition report feature
        String input = "3\n" + // Guest mode
                       "2\n" + // Calorie Tracking Menu
                       "3\n" + // View Weekly Nutrition Report
                       "2025\n" + // Year
                       "5\n" + // Month
                       "15\n" + // Day
                       "\n" + // Press Enter to continue
                       "0\n" + // Return to Main Menu
                       "5\n" + // Logout
                       "0\n"; // Exit
                       
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify weekly nutrition report generation
        String output = outputStream.toString();
        assertTrue("Weekly nutrition report should be displayed", 
                  output.contains("===== Weekly Nutrition Report ====="));
    }
    
    
    
    @Test
    public void testSetDietPreferences() {
        // Test Diet Recommendations' set diet preferences feature
        String input = "3\n" + // Guest mode
                       "3\n" + // Diet Recommendations Menu
                       "1\n" + // Set Diet Preferences
                       "1\n" + // Balanced diet
                       "2\n" + // Maintain weight
                       "N\n" + // No health conditions
                       "N\n" + // No foods to exclude
                       "0\n" + // Return to Main Menu
                       "5\n" + // Logout
                       "0\n"; // Exit
                       
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify diet preferences setting
        String output = outputStream.toString();
        assertTrue("Diet preferences should be set", 
                  output.contains("===== Set Diet Preferences ====="));
    }
    
    @Test
    public void testDietRecommendationsGeneration() {
        // Test Diet Recommendations' generate recommendations feature
        String input = "3\n" + // Guest mode
                       "3\n" + // Diet Recommendations Menu
                       "2\n" + // Generate Diet Recommendations
                       "M\n" + // Gender
                       "30\n" + // Age
                       "180\n" + // Height
                       "75\n" + // Weight
                       "2\n" + // Activity level
                       "\n" + // Press Enter to view recommendations
                       "\n" + // Press Enter to continue
                       "0\n" + // Return to Main Menu
                       "5\n" + // Logout
                       "0\n"; // Exit
                       
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify diet recommendations generation
        String output = outputStream.toString();
        assertTrue("Diet recommendations should be generated", 
                  output.contains("===== Generate Diet Recommendations ====="));
    }
    
    @Test
    public void testViewDietRecommendations() {
        // Test Diet Recommendations' view recommendations feature
        String input = "3\n" + // Guest mode
                       "3\n" + // Diet Recommendations Menu
                       "3\n" + // View Recommendations
                       "\n" + // Press Enter to continue
                       "0\n" + // Return to Main Menu
                       "5\n" + // Logout
                       "0\n"; // Exit
                       
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify view diet recommendations
        String output = outputStream.toString();
        assertTrue("Diet recommendations view should be displayed", 
                  output.contains("===== View Diet Recommendations ====="));
    }
    
    @Test
    public void testShoppingListMenuNavigation() {
        // Test navigation to Shopping List Menu and interaction
        String input = "3\n" + // Guest mode
                       "4\n" + // Shopping List Menu
                       "1\n" + // Generate Shopping List
                       "1\n" + // Breakfast
                       "1\n" + // First option
                       "\n" + // Press Enter to continue
                       "0\n" + // Return to Main Menu
                       "5\n" + // Logout
                       "0\n"; // Exit
                       
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify Shopping List menu interactions
        String output = outputStream.toString();
        assertTrue("Generate Shopping List option should be displayed", 
                  output.contains("===== Generate Shopping List ====="));
    }
    
    
    
    @Test
    public void testNonNumericMainMenuOption() {
        // First login as guest, then try non-numeric option
        String input = "3\n" + // Guest mode
                       "abc\n" + // Non-numeric option
                       "5\n" + // Logout
                       "0\n"; // Exit
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify error message was displayed
        String output = outputStream.toString();
        assertTrue("Invalid selection message should be displayed", 
                  output.contains("Invalid selection. Please enter only numbers."));
    }
    
    @Test
    public void testEmptyMainMenuOption() {
        // First login as guest, then try empty input
        String input = "3\n" + // Guest mode
                       "\n" + // Empty input
                       "5\n" + // Logout
                       "0\n"; // Exit
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify error message was displayed
        String output = outputStream.toString();
        assertTrue("Empty input message should be displayed", 
                  output.contains("Empty input. Please enter a number."));
    }

    
   

@Test
    public void testLoginWithEmptyInputs() {
        // Simulate login with empty inputs then exit
        String input = "1\n" + // Login option
                       "\n" + // Empty username
                       "testuser\n" + // Valid username
                       "\n" + // Empty password
                       "test123\n" + // Valid password
                       "0\n"; // Exit
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify error messages were displayed
        String output = outputStream.toString();
        assertTrue("Empty username message should be displayed", 
                  output.contains("Username cannot be empty. Please try again."));
        assertTrue("Empty password message should be displayed", 
                  output.contains("Password cannot be empty. Please try again."));
    }
    
   
    
      
    @Test
    public void testHandleGuestMode() {
        try {
            // Access handleGuestMode method using reflection
            Method handleGuestModeMethod = DietappApp.class.getDeclaredMethod("handleGuestMode");
            handleGuestModeMethod.setAccessible(true);
            
            // Call handleGuestMode method
            handleGuestModeMethod.invoke(app);
            
            // Verify output
            String output = outputStream.toString();
            assertTrue("Guest mode message should be displayed", 
                      output.contains("You are now using the application as a guest"));
        } catch (Exception e) {
            fail("Could not test handleGuestMode method: " + e.getMessage());
        }
    }
    
   
    
    @Test
    public void testHandleUserMainMenu() {
        try {
            // Access handleUserMainMenu method using reflection
            Method handleUserMainMenuMethod = DietappApp.class.getDeclaredMethod("handleUserMainMenu", int.class);
            handleUserMainMenuMethod.setAccessible(true);
            
            // Enable guest mode to set up the test
            Dietapp dietApp = new Dietapp();
            dietApp.enableGuestMode();
            
            // Set the dietApp field in DietappApp
            Field dietAppField = DietappApp.class.getDeclaredField("dietApp");
            dietAppField.setAccessible(true);
            dietAppField.set(app, dietApp);
            
            // Test with exit option (0)
            boolean result = (boolean) handleUserMainMenuMethod.invoke(app, 0);
            
            // Verify the result and output
            assertFalse("handleUserMainMenu should return false for exit option", result);
            String output = outputStream.toString();
            assertTrue("Exit message should be displayed", 
                      output.contains("Thank you for using Diet Planner"));
            
            // Reset output stream
            outputStream.reset();
            
            // Test with invalid option
            result = (boolean) handleUserMainMenuMethod.invoke(app, 99);
            
            // Verify the result and output
            assertTrue("handleUserMainMenu should return true for invalid option", result);
            output = outputStream.toString();
            assertTrue("Invalid option message should be displayed", 
                      output.contains("Invalid selection"));
        } catch (Exception e) {
            fail("Could not test handleUserMainMenu method: " + e.getMessage());
        }
    }
    
    @Test
    public void testPrintAuthMenu() {
        try {
            // Access printAuthMenu method using reflection
            Method printAuthMenuMethod = DietappApp.class.getDeclaredMethod("printAuthMenu");
            printAuthMenuMethod.setAccessible(true);
            
            // Call printAuthMenu method
            printAuthMenuMethod.invoke(app);
            
            // Verify output
            String output = outputStream.toString();
            assertTrue("Auth menu should be displayed", 
                      output.contains("===== Diet Planner Authentication ====="));
            assertTrue("Login option should be displayed", 
                      output.contains("1. Login"));
            assertTrue("Register option should be displayed", 
                      output.contains("2. Register"));
            assertTrue("Guest mode option should be displayed", 
                      output.contains("3. Continue as Guest"));
            assertTrue("Exit option should be displayed", 
                      output.contains("0. Exit"));
        } catch (Exception e) {
            fail("Could not test printAuthMenu method: " + e.getMessage());
        }
    }
    
    @Test
    public void testPrintUserMainMenu() {
        try {
            // Access printUserMainMenu method using reflection
            Method printUserMainMenuMethod = DietappApp.class.getDeclaredMethod("printUserMainMenu");
            printUserMainMenuMethod.setAccessible(true);
            
            // Enable guest mode to set up the test
            Dietapp dietApp = new Dietapp();
            dietApp.enableGuestMode();
            
            // Set the dietApp field in DietappApp
            Field dietAppField = DietappApp.class.getDeclaredField("dietApp");
            dietAppField.setAccessible(true);
            dietAppField.set(app, dietApp);
            
            // Call printUserMainMenu method
            printUserMainMenuMethod.invoke(app);
            
            // Verify output
            String output = outputStream.toString();
            assertTrue("Main menu should be displayed", 
                      output.contains("===== Diet Planner Main Menu ====="));
            assertTrue("Should show logged in as guest", 
                      output.contains("Logged in as: guest"));
            assertTrue("Meal Planning option should be displayed", 
                      output.contains("1. Meal Planning and Logging"));
            assertTrue("Calorie Tracking option should be displayed", 
                      output.contains("2. Calorie and Nutrient Tracking"));
            assertTrue("Diet Recommendations option should be displayed", 
                      output.contains("3. Personalized Diet Recommendations"));
            assertTrue("Shopping List option should be displayed", 
                      output.contains("4. Shopping List Generator"));
            assertTrue("Logout option should be displayed", 
                      output.contains("5. Log out"));
            assertTrue("Exit option should be displayed", 
                      output.contains("0. Exit"));
        } catch (Exception e) {
            fail("Could not test printUserMainMenu method: " + e.getMessage());
        }
    }
    
   
    
  
    
    @Test
    public void testGetUserChoice() {
        try {
            // Get access to the private getUserChoice method using reflection
            Method getUserChoiceMethod = DietappApp.class.getDeclaredMethod("getUserChoice");
            getUserChoiceMethod.setAccessible(true);
            
            // Test with empty input
            System.setIn(new ByteArrayInputStream("\n".getBytes()));
            app = new DietappApp(); // Recreate with new input
            assertEquals("Empty input should return -1", -1, getUserChoiceMethod.invoke(app));
            
            // Test with non-numeric input
            System.setIn(new ByteArrayInputStream("abc\n".getBytes()));
            app = new DietappApp(); // Recreate with new input
            assertEquals("Non-numeric input should return -1", -1, getUserChoiceMethod.invoke(app));
            
            // Test with valid numeric input
            System.setIn(new ByteArrayInputStream("5\n".getBytes()));
            app = new DietappApp(); // Recreate with new input
            assertEquals("Valid numeric input should return that number", 5, getUserChoiceMethod.invoke(app));
            
        } catch (Exception e) {
            fail("Could not test getUserChoice method: " + e.getMessage());
        }
    }
    
    @Test
    public void testGetUserWithSpecialInputs() {
        try {
            // Get access to the private getUserChoice method using reflection
            Method getUserChoiceMethod = DietappApp.class.getDeclaredMethod("getUserChoice");
            getUserChoiceMethod.setAccessible(true);
            
            // Test with multiple digits
            System.setIn(new ByteArrayInputStream("123\n".getBytes()));
            app = new DietappApp(); // Recreate with new input
            assertEquals("Multiple digits should be parsed correctly", 123, getUserChoiceMethod.invoke(app));
            
            // Test with zero
            System.setIn(new ByteArrayInputStream("0\n".getBytes()));
            app = new DietappApp(); // Recreate with new input
            assertEquals("Zero should be parsed correctly", 0, getUserChoiceMethod.invoke(app));
            
            // Test with negative number (should be parsed but will be handled by menu methods)
            System.setIn(new ByteArrayInputStream("-1\n".getBytes()));
            app = new DietappApp(); // Recreate with new input
            assertEquals("Negative number should be parsed correctly", -1, getUserChoiceMethod.invoke(app));
            
            // Test with whitespace around number
            System.setIn(new ByteArrayInputStream("  42  \n".getBytes()));
            app = new DietappApp(); // Recreate with new input
            assertEquals("Number with whitespace should be trimmed and parsed", 42, getUserChoiceMethod.invoke(app));
            
        } catch (Exception e) {
            fail("Could not test getUserChoice method with special inputs: " + e.getMessage());
        }
    }
    
    @Test
    public void testRunMultipleOperations() {
        // Test a comprehensive sequence of operations
        String testUsername = "testuser_multi_" + System.currentTimeMillis();
        
        // Complex input sequence that tests multiple features
        String input = "2\n" + // Register option
                       testUsername + "\n" + // Username
                       "test123\n" + // Password
                       "test@example.com\n" + // Email
                       "Test User\n" + // Name
                       "1\n" + // Login option
                       testUsername + "\n" + // Username
                       "test123\n" + // Password
                       "1\n" + // Meal Planning Menu
                       "0\n" + // Return to Main Menu
                       "2\n" + // Calorie Tracking Menu
                       "0\n" + // Return to Main Menu
                       "5\n" + // Logout
                       "3\n" + // Guest mode
                       "5\n" + // Logout
                       "0\n"; // Exit
                       
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify multiple operations
        String output = outputStream.toString();
        assertTrue("Registration should be successful", 
                  output.contains("Registration successful!"));
        assertTrue("Login should be successful", 
                  output.contains("Login successful!"));
        assertTrue("Main menu should be displayed", 
                  output.contains("===== Diet Planner Main Menu ====="));
        assertTrue("Meal Planning menu should be displayed", 
                  output.contains("===== Meal Planning and Logging ====="));
        assertTrue("Calorie Tracking menu should be displayed", 
                  output.contains("===== Calorie and Nutrient Tracking ====="));
        assertTrue("Logout message should be displayed", 
                  output.contains("You have been logged out."));
        assertTrue("Guest mode should be enabled", 
                  output.contains("You are now using the application as a guest"));
        assertTrue("Exit message should be displayed", 
                  output.contains("Thank you for using Diet Planner. Goodbye!"));
    }
    
    
    
    @Test
    public void testErrorRecovery() {
        // Test that the app can recover from errors and continue running
        String input = "abc\n" + // Invalid auth menu input
                       "3\n" + // Guest mode
                       "xyz\n" + // Invalid main menu input
                       "5\n" + // Logout
                       "0\n"; // Exit
                       
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // Verify error recovery
        String output = outputStream.toString();
        assertTrue("Should handle invalid auth menu input", 
                  output.contains("Invalid selection"));
        assertTrue("Should transition to guest mode", 
                  output.contains("You are now using the application as a guest"));
        assertTrue("Should handle invalid main menu input", 
                  output.contains("Invalid selection"));
        assertTrue("Should logout successfully", 
                  output.contains("You have been logged out."));
        assertTrue("Should exit gracefully", 
                  output.contains("Thank you for using Diet Planner. Goodbye!"));
    }
   
    
    
   

   

   

   

   
    
    
    
    
    @Test
    public void testExceptionalInputs() {
        // Test that the app can handle very long inputs
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("a");
        }
        
        // Create input with very long username that should be rejected
        String input = "2\n" + // Register option
                       longInput.toString() + "\n" + // Very long username
                       "testuser\n" + // Valid username
                       "test123\n" + // Password
                       "test@example.com\n" + // Email
                       "Test User\n" + // Name
                       "0\n"; // Exit
                       
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // Create a new instance with our controlled input
        DietappApp testApp = new DietappApp();
        testApp.run();
        
        // App should continue running without crashing
        String output = outputStream.toString();
        assertTrue("App should continue running after handling very long input", 
                  output.contains("Thank you for using Diet Planner. Goodbye!"));
    }
}