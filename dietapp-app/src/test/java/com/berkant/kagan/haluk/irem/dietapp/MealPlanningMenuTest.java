package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @class MealPlanningMenuTest
 * @brief Test class for the MealPlanningMenu class.
 */
public class MealPlanningMenuTest {
    
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    
    private MealPlanningService mealPlanningService;
    private AuthenticationService authService;
    private MealPlanningMenu mealPlanningMenu;
    private User testUser;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Initialize database
        DatabaseHelper.initializeDatabase();
    }
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // Close database connections
        DatabaseHelper.closeAllConnections();
    }
    
    @Before
    public void setUp() throws Exception {
        // Redirect System.out to our outputStream
        System.setOut(new PrintStream(outputStream));
        
        // Initialize services
        mealPlanningService = new MealPlanningService();
        authService = new AuthenticationService();
        
        // Create and login a test user
        testUser = new User("testuser", "password", "test@example.com", "Test User");
        testUser.setLoggedIn(true);
        
        // Mock the authentication service's getCurrentUser method
        // by setting up a test user that is logged in
        authService = new TestAuthenticationService(testUser);
    }
    
    @After
    public void tearDown() throws Exception {
        // Reset output and input streams
        System.setOut(originalOut);
        System.setIn(originalIn);
        
        // Clear the output stream
        outputStream.reset();
    }
    
    /**
     * Test the displayMenu method with option 0 (return to main menu).
     */
    @Test
    public void testDisplayMenuReturnToMainMenu() {
        // Arrange
        String input = "0\n"; // Choose option 0 to exit
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new MealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Menu should display correct title", output.contains("Meal Planning and Logging"));
        assertTrue("Menu should contain option to return to main menu", output.contains("0. Return to Main Menu"));
    }
    
    /**
     * Test the displayMenu method with an invalid option.
     */
    @Test
    public void testDisplayMenuInvalidOption() {
        // Arrange
        String input = "99\n0\n"; // Choose invalid option 99, then exit
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new MealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Menu should display error for invalid choice", output.contains("Invalid choice"));
    }
   
    
    /**
     * Test the handlePlanMeals method with valid input.
     */
    @Test
    public void testHandlePlanMealsValidInput() {
        // Arrange
        // Valid date, breakfast (1), first food option (1), then exit
        String input = "1\n2025\n1\n1\n1\n1\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testMealPlanningService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testMealPlanningService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show meal planning menu", output.contains("Plan Meals"));
        assertTrue("Should show breakfast options", output.contains("Breakfast"));
        assertTrue("Should confirm meal added", output.contains("added to breakfast successfully"));
    }
    
    /**
     * Test the handleLogFoods method with valid input.
     */
    @Test
    public void testHandleLogFoodsValidInput() {
        // Arrange
        // Option 2 (Log Foods), valid date, food details, then exit
        String input = "2\n2025\n2\n1\nApple\n100\n52\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testMealPlanningService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testMealPlanningService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show log foods menu", output.contains("Log Foods"));
        assertTrue("Should confirm food logged", output.contains("Food logged successfully"));
    }
    
    /**
     * Test the handleLogFoods method with invalid food details.
     */
    @Test
    public void testHandleLogFoodsInvalidFoodDetails() {
        // Arrange
        // Option 2 (Log Foods), valid date, invalid food details, then exit
        String input = "2\n2025\n2\n1\nApple\n-100\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new MealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show error for negative amount", output.contains("Amount must be positive"));
    }
    
    /**
     * Test the handleViewMealHistory method with no meal history.
     */
    @Test
    public void testHandleViewMealHistoryNoHistory() {
        // Arrange
        // Option 3 (View Meal History), valid date, then exit
        String input = "3\n2025\n3\n1\n\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testMealPlanningService = new TestMealPlanningService();
        // Empty lists will be returned by default
        mealPlanningMenu = new MealPlanningMenu(testMealPlanningService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show meal history menu", output.contains("View Meal History"));
        assertTrue("Should show no planned meals", output.contains("No planned meals found"));
        assertTrue("Should show no food logged", output.contains("No food logged for this date"));
    }
    
    /**
     * Test the handleViewMealHistory method with existing meal history.
     */
    @Test
    public void testHandleViewMealHistoryWithHistory() {
        // Arrange
        // Option 3 (View Meal History), valid date, then exit
        String input = "3\n2025\n4\n2\n\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testMealPlanningService = new TestMealPlanningService();
        testMealPlanningService.setReturnMealPlan(true);
        testMealPlanningService.setReturnFoodLog(true);
        mealPlanningMenu = new MealPlanningMenu(testMealPlanningService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show meal history menu", output.contains("View Meal History"));
        assertTrue("Should show breakfast meals", output.contains("Breakfast:"));
        assertTrue("Should show food log", output.contains("Food Log"));
        assertTrue("Should show total calories", output.contains("Total calories consumed: 100"));
    }
    
    /**
     * Test the formatDate method through reflected food option methods.
     */
    @Test
    public void testDateAndMealTypeHandling() {
        // Arrange
        // Test all 4 meal types (1, 2, 3, 4) with valid date then exit
        String input = "1\n2025\n5\n5\n1\n1\n1\n2025\n5\n5\n2\n1\n1\n2025\n5\n5\n3\n1\n1\n2025\n5\n5\n4\n1\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testMealPlanningService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testMealPlanningService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should handle breakfast", output.contains("Breakfast"));
        assertTrue("Should handle lunch", output.contains("Lunch"));
        assertTrue("Should handle snack", output.contains("Snack"));
        assertTrue("Should handle dinner", output.contains("Dinner"));
    }
    
    /**
     * Test error handling for too many food options.
     */
    @Test
    public void testTooManyFoodOptions() {
        // Arrange
        // Option 1 (Plan Meals), valid date, meal type 1, invalid food choice, then exit
        String input = "1\n2025\n6\n6\n1\n99\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new MealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show error for invalid food choice", output.contains("Invalid food choice"));
    }
    
    // Mock AuthenticationService class for testing
    private class TestAuthenticationService extends AuthenticationService {
        private User currentUser;
        
        public TestAuthenticationService(User user) {
            this.currentUser = user;
        }
        
        @Override
        public User getCurrentUser() {
            return currentUser;
        }
    }
    
    // Mock MealPlanningService class for testing
    private class TestMealPlanningService extends MealPlanningService {
        private boolean returnMealPlan = false;
        private boolean returnFoodLog = false;
        
        public void setReturnMealPlan(boolean value) {
            this.returnMealPlan = value;
        }
        
        public void setReturnFoodLog(boolean value) {
            this.returnFoodLog = value;
        }
        
        @Override
        public boolean addMealPlan(String username, String date, String mealType, Food food) {
            return true; // Always return success
        }
        
        @Override
        public boolean logFood(String username, String date, Food food) {
            return true; // Always return success
        }
        
        @Override
        public List<Food> getMealPlan(String username, String date, String mealType) {
            if (returnMealPlan) {
                List<Food> plan = new ArrayList<>();
                plan.add(new Food("Test Food", 100, 200));
                return plan;
            }
            return new ArrayList<>(); // Return empty list by default
        }
        
        @Override
        public List<Food> getFoodLog(String username, String date) {
            if (returnFoodLog) {
                List<Food> log = new ArrayList<>();
                log.add(new Food("Logged Food", 50, 100));
                return log;
            }
            return new ArrayList<>(); // Return empty list by default
        }
        
        @Override
        public int getTotalCalories(String username, String date) {
            return returnFoodLog ? 100 : 0;
        }
        
        @Override
        public boolean isValidDate(int year, int month, int day) {
            return true; // Always return valid
        }
        
        @Override
        public Food[] getBreakfastOptions() {
            return new Food[] { new Food("Breakfast Option", 100, 200) };
        }
        
        @Override
        public Food[] getLunchOptions() {
            return new Food[] { new Food("Lunch Option", 100, 200) };
        }
        
        @Override
        public Food[] getSnackOptions() {
            return new Food[] { new Food("Snack Option", 100, 200) };
        }
        
        @Override
        public Food[] getDinnerOptions() {
            return new Food[] { new Food("Dinner Option", 100, 200) };
        }
    }
}