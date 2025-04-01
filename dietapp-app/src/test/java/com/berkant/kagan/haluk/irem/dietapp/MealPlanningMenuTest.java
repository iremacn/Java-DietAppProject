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
    /**
     * Test handling of invalid month values in getDateFromUser method.
     */
    @Test
   
    
    public void testInvalidFoodAmountFormat() {
        // Arrange
        // Option 2 (Log Foods), valid date, invalid format for amount, then exit
        String input = "2\n2025\n1\n1\nApple\nabc\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new MealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show error for invalid amount format", output.contains("Invalid amount format"));
    }

    /**
     * Test handling of invalid format for food calories in getFoodDetailsFromUser method.
     */
    @Test
    public void testInvalidFoodCaloriesFormat() {
        // Arrange
        // Option 2 (Log Foods), valid date, invalid format for calories, then exit
        String input = "2\n2025\n1\n1\nApple\n100\nabc\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new MealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show error for invalid calorie format", output.contains("Invalid calorie format"));
    }

    /**
     * Test handling of negative food calories in getFoodDetailsFromUser method.
     */
    @Test
    public void testNegativeFoodCalories() {
        // Arrange
        // Option 2 (Log Foods), valid date, negative calories, then exit
        String input = "2\n2025\n1\n1\nApple\n100\n-50\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new MealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show error for negative calories", output.contains("Calories cannot be negative"));
    }

    /**
     * Test addMealPlan failure handling.
     */
    @Test
    public void testAddMealPlanFailure() {
        // Arrange
        // Option 1 (Plan Meals), valid date, first meal and food option, then exit
        String input = "1\n2025\n1\n1\n1\n1\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // Create a test service that returns failure on add meal plan
        TestMealPlanningService testService = new TestMealPlanningService() {
            @Override
            public boolean addMealPlan(String username, String date, String mealType, Food food) {
                return false; // Simulate failure
            }
        };
        
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show failure message", output.contains("Failed to add food to meal plan"));
    }

    /**
     * Test logFood failure handling.
     */
    @Test
    public void testLogFoodFailure() {
        // Arrange
        // Option 2 (Log Foods), valid date, food details, then exit
        String input = "2\n2025\n1\n1\nApple\n100\n52\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // Create a test service that returns failure on log food
        TestMealPlanningService testService = new TestMealPlanningService() {
            @Override
            public boolean logFood(String username, String date, Food food) {
                return false; // Simulate failure
            }
        };
        
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show failure message", output.contains("Failed to log food"));
    }

    /**
     * Test for non-numeric input in getUserChoice method.
     */
    @Test
    public void testNonNumericUserChoice() {
        // Arrange
        // Non-numeric input, then valid exit
        String input = "abc\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new MealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show invalid choice message", output.contains("Invalid choice"));
    }

    /**
     * Test capitalize method with empty string.
     */
    @Test
    public void testCapitalizeWithEmptyString() {
        // Arrange
        // We need to access the private capitalize method, so let's test it through view meal history
        // Option 3 (View Meal History), valid date, then exit
        String input = "3\n2025\n1\n1\n\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // Create a test service that returns empty meal types to test empty string capitalize
        TestMealPlanningService testService = new TestMealPlanningService() {
            @Override
            public List<Food> getMealPlan(String username, String date, String mealType) {
                if (mealType.equals("")) {
                    List<Food> plan = new ArrayList<>();
                    plan.add(new Food("Empty Test", 10, 20));
                    return plan;
                }
                return new ArrayList<>();
            }
        };
        
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert - We can't directly test the capitalize method, but we ensure it doesn't cause any errors
        String output = outputStream.toString();
        assertTrue("Should display meal history menu", output.contains("View Meal History"));
    }
    /**
     * Test handling of zero input for food amount.
     */
    @Test
    public void testZeroFoodAmount() {
        // Arrange
        // Option 2 (Log Foods), valid date, zero amount for food
        String input = "2\n2025\n1\n1\nBanana\n0\n100\n20\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show error for zero amount", output.contains("Amount must be positive"));
    }

    /**
     * Test handling of zero input for food calories.
     */
    @Test
    public void testZeroCalories() {
        // Arrange
        // Option 2 (Log Foods), valid date, zero calories for food
        String input = "2\n2025\n1\n1\nCarrot\n50\n0\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        // This may or may not be valid in your implementation, adjust assertion as needed
        assertTrue("Should process zero calorie food", output.contains("Food logged successfully"));
    }

    /**
     * Test handling of very large calorie input.
     */
    @Test
    public void testVeryLargeCalories() {
        // Arrange
        // Option 2 (Log Foods), valid date, very large calorie value
        String input = "2\n2025\n1\n1\nCake\n100\n9999999\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        // This tests if large calorie values are properly handled
        assertTrue("Should process high calorie food", output.contains("Food logged successfully"));
    }

    /**
     * Test multiple meal types in a single meal plan session.
     */
    @Test
    public void testMultipleMealTypesPlan() {
        // Arrange
        // Option 1 (Plan Meals), plan breakfast, then lunch, then exit
        String input = "1\n2025\n1\n1\n1\n1\n1\n2025\n1\n1\n2\n1\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should handle breakfast planning", output.contains("breakfast successfully"));
        assertTrue("Should handle lunch planning", output.contains("lunch successfully"));
    }

    /**
     * Test handling of invalid meal type input with non-numeric data.
     */
    @Test
    public void testInvalidMealTypeNonNumeric() {
        // Arrange
        // Option 1 (Plan Meals), valid date, non-numeric meal type, then exit
        String input = "1\n2025\n1\n1\nabc\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show error for invalid meal type", output.contains("Invalid meal type"));
    }

    /**
     * Test handling of invalid food choice with non-numeric data.
     */
    @Test
    public void testInvalidFoodChoiceNonNumeric() {
        // Arrange
        // Option 1 (Plan Meals), valid date, valid meal type, non-numeric food choice, then exit
        String input = "1\n2025\n1\n1\n1\nabc\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show error for invalid food choice", output.contains("Invalid food choice"));
    }

    /**
     * Test handling of meal plan with empty food array.
     */
    @Test
   
    public void testVeryLongFoodName() {
        // Arrange
        // Option 2 (Log Foods), valid date, very long food name
        String longName = "This is an extremely long food name that tests the system's ability to handle lengthy input strings and ensure that there are no unexpected behaviors when processing such input";
        String input = "2\n2025\n1\n1\n" + longName + "\n100\n200\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should process long food name", output.contains("Food logged successfully"));
    }

    /**
     * Test handling of view meal history for future date.
     */
    @Test
    public void testViewMealHistoryFutureDate() {
        // Arrange
        // Option 3 (View Meal History), future date, then exit
        String input = "3\n2099\n12\n31\n\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should handle future date", output.contains("View Meal History"));
    }

    /**
     * Test handling of decimal input for food amount.
     */
    @Test
    public void testDecimalFoodAmount() {
        // Arrange
        // Option 2 (Log Foods), valid date, decimal food amount
        String input = "2\n2025\n1\n1\nApple\n123.45\n100\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should process decimal food amount", output.contains("Food logged successfully"));
    }

    /**
     * Test displaying all four meal types in view meal history.
     */
    @Test
    public void testViewAllMealTypes() {
        // Arrange
        // Option 3 (View Meal History), valid date with all meal types, then exit
        String input = "3\n2025\n1\n1\n\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // Create test service that returns meal plan data for all types
        TestMealPlanningService testService = new TestMealPlanningService() {
            @Override
            public List<Food> getMealPlan(String username, String date, String mealType) {
                List<Food> plan = new ArrayList<>();
                // Add different foods for different meal types to ensure we can verify each one
                if (mealType.equals("breakfast")) {
                    plan.add(new Food("Breakfast Item", 150, 300));
                } else if (mealType.equals("lunch")) {
                    plan.add(new Food("Lunch Item", 200, 500));
                } else if (mealType.equals("snack")) {
                    plan.add(new Food("Snack Item", 50, 100));
                } else if (mealType.equals("dinner")) {
                    plan.add(new Food("Dinner Item", 250, 600));
                }
                return plan;
            }
        };
        
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show breakfast in meal plan", output.contains("Breakfast:"));
        assertTrue("Should show lunch in meal plan", output.contains("Lunch:"));
        assertTrue("Should show snack in meal plan", output.contains("Snack:"));
        assertTrue("Should show dinner in meal plan", output.contains("Dinner:"));
    }
    /**
     * Test consecutive invalid inputs in date selection.
     */
    @Test
   
    public void testSnackOptions() {
        // Arrange
        // Plan meals, select snack option
        String input = "1\n2025\n1\n1\n3\n1\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // Create test service with specific snack options
        TestMealPlanningService testService = new TestMealPlanningService() {
            @Override
            public Food[] getSnackOptions() {
                return new Food[] { 
                    new Food("Special Snack 1", 50, 100),
                    new Food("Special Snack 2", 60, 120)
                };
            }
        };
        
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show snack options", output.contains("Select Food for Snack"));
        assertTrue("Should add snack to meal plan", output.contains("added to snack successfully"));
    }

    /**
     * Test handling of dinner options.
     */
    @Test
    public void testDinnerOptions() {
        // Arrange
        // Plan meals, select dinner option
        String input = "1\n2025\n1\n1\n4\n1\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // Create test service with specific dinner options
        TestMealPlanningService testService = new TestMealPlanningService() {
            @Override
            public Food[] getDinnerOptions() {
                return new Food[] { 
                    new Food("Special Dinner 1", 300, 600),
                    new Food("Special Dinner 2", 350, 700)
                };
            }
        };
        
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show dinner options", output.contains("Select Food for Dinner"));
        assertTrue("Should add dinner to meal plan", output.contains("added to dinner successfully"));
    }

    /**
     * Test meal planning with maximum date values.
     */
    @Test
    public void testMaxDateValues() {
        // Arrange
        // Plan meals with maximum allowed date values
        String input = "1\n2100\n12\n31\n1\n1\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should handle maximum date values", output.contains("Select Meal Type"));
        assertTrue("Should successfully plan meal", output.contains("added to breakfast successfully"));
    }

    /**
     * Test meal planning with minimum date values.
     */
    @Test
    public void testMinDateValues() {
        // Arrange
        // Plan meals with minimum allowed date values
        String input = "1\n2025\n1\n1\n1\n1\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should handle minimum date values", output.contains("Select Meal Type"));
        assertTrue("Should successfully plan meal", output.contains("added to breakfast successfully"));
    }

    /**
     * Test for food with maximum possible calorie value.
     */
    @Test
    public void testMaxCalorieValue() {
        // Arrange
        // Log food with maximum possible integer value for calories
        String input = "2\n2025\n1\n1\nMaxCalorieFood\n100\n" + Integer.MAX_VALUE + "\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should handle maximum calorie value", output.contains("Food logged successfully"));
    }

    /**
     * Test handling of different capitalizations in meal type.
     */
    @Test
    public void testMealTypeCapitalization() {
        // Arrange
        // View meal history with specific meal plan data
        String input = "3\n2025\n1\n1\n\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // Create test service that tests capitalization handling
        TestMealPlanningService testService = new TestMealPlanningService() {
            @Override
            public List<Food> getMealPlan(String username, String date, String mealType) {
                // This tests if the service correctly handles different capitalizations
                // Your original code should convert all to lowercase before comparison
                List<Food> plan = new ArrayList<>();
                if (mealType.equals("breakfast")) {
                    plan.add(new Food("Test Breakfast", 150, 300));
                }
                return plan;
            }
        };
        
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should handle meal type capitalization", output.contains("Breakfast:"));
    }

    /**
     * Test multiple consecutive menu operations.
     */
    @Test
    public void testMultipleConsecutiveOperations() {
        // Arrange
        // Plan meal -> Log food -> View history -> Exit
        String input = "1\n2025\n1\n1\n1\n1\n2\n2025\n1\n1\nApple\n100\n50\n3\n2025\n1\n1\n\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService() {
            @Override
            public List<Food> getMealPlan(String username, String date, String mealType) {
                List<Food> plan = new ArrayList<>();
                if (mealType.equals("breakfast")) {
                    plan.add(new Food("Breakfast Option", 100, 200));
                }
                return plan;
            }
            
            @Override
            public List<Food> getFoodLog(String username, String date) {
                List<Food> log = new ArrayList<>();
                log.add(new Food("Apple", 100, 50));
                return log;
            }
            
            @Override
            public int getTotalCalories(String username, String date) {
                return 50;
            }
        };
        
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should handle plan meal operation", output.contains("added to breakfast successfully"));
        assertTrue("Should handle log food operation", output.contains("Food logged successfully"));
        assertTrue("Should handle view history operation", output.contains("Planned Meals for"));
        assertTrue("Should show breakfast in meal plan", output.contains("Breakfast:"));
        assertTrue("Should show logged food", output.contains("Apple"));
    }

    /**
     * Test handling of special characters in food name.
     */
    @Test
    public void testSpecialCharactersInFoodName() {
        // Arrange
        // Log food with special characters in name
        String specialName = "Special!@#$%^&*()_+{}[]|\\:;\"'<>,.?/Food";
        String input = "2\n2025\n1\n1\n" + specialName + "\n100\n200\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should handle special characters in food name", output.contains("Food logged successfully"));
    }

    /**
     * Test meal history with a date having both meal plan and food log but zero calories.
     */
    @Test
    public void testMealHistoryWithZeroCalories() {
        // Arrange
        // View meal history for a date with zero calories
        String input = "3\n2025\n1\n1\n\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // Create test service that returns meal plan and food log with zero calories
        TestMealPlanningService testService = new TestMealPlanningService() {
            @Override
            public List<Food> getMealPlan(String username, String date, String mealType) {
                List<Food> plan = new ArrayList<>();
                if (mealType.equals("breakfast")) {
                    plan.add(new Food("Zero Calorie Breakfast", 100, 0));
                }
                return plan;
            }
            
            @Override
            public List<Food> getFoodLog(String username, String date) {
                List<Food> log = new ArrayList<>();
                log.add(new Food("Zero Calorie Food", 100, 0));
                return log;
            }
            
            @Override
            public int getTotalCalories(String username, String date) {
                return 0; // Zero calories
            }
        };
        
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show breakfast with zero calories", output.contains("Zero Calorie Breakfast"));
        assertTrue("Should show food log with zero calories", output.contains("Zero Calorie Food"));
        assertTrue("Should show zero total calories", output.contains("Total calories consumed: 0"));
    }
    /**
     * Test handling of maximum number of food options when selecting food.
     */
    @Test
    public void testFoodOptionsMaxLength() {
        // Arrange
        // Plan meals, breakfast, with many food options
        String input = "1\n2025\n1\n1\n1\n8\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // Create service that returns many food options
        TestMealPlanningService testService = new TestMealPlanningService() {
            @Override
            public Food[] getBreakfastOptions() {
                // Create an array with exact maximum size mentioned in prompt (8)
                Food[] options = new Food[8];
                for (int i = 0; i < 8; i++) {
                    options[i] = new Food("Breakfast Option " + (i+1), 100, 200);
                }
                return options;
            }
        };
        
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should display all food options", output.contains("8. Breakfast Option 8"));
        assertTrue("Should handle selection of last option", output.contains("added to breakfast successfully"));
    }

    /**
     * Test invalid year format with empty string.
     */
    @Test
    public void testEmptyYearInput() {
        // Arrange
        // Empty year input followed by valid inputs
        String input = "1\n\n2025\n1\n1\n1\n1\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should handle empty year input", output.contains("Invalid year format"));
    }

    /**
     * Test critical error handling in MealPlanningService operations.
     */
    @Test
   
    
    
    
    public void testEmptyUsernameHandling() {
        // Arrange
        // Try to plan meal with user that has empty username
        String input = "1\n2025\n1\n1\n1\n1\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // Create user with empty username
        User emptyUser = new User("", "password", "email@example.com", "Empty User");
        emptyUser.setLoggedIn(true);
        
        // Auth service that returns empty username user
        AuthenticationService emptyUserAuth = new TestAuthenticationService(emptyUser);
        
        mealPlanningMenu = new MealPlanningMenu(mealPlanningService, emptyUserAuth, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert - mainly checking that no exceptions occur
        String output = outputStream.toString();
        assertTrue("Should handle empty username", output.contains("Plan Meals"));
    }

    /**
     * Test extreme condition: very large number of foods in history.
     */
    @Test
    public void testLargeFoodHistoryList() {
        // Arrange
        // View meal history with many items
        String input = "3\n2025\n1\n1\n\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // Create service that returns many food items
        TestMealPlanningService testService = new TestMealPlanningService() {
            @Override
            public List<Food> getFoodLog(String username, String date) {
                List<Food> log = new ArrayList<>();
                // Add 100 food items
                for (int i = 0; i < 100; i++) {
                    log.add(new Food("Food Item " + i, 100, 200));
                }
                return log;
            }
            
            @Override
            public int getTotalCalories(String username, String date) {
                return 20000; // 100 items * 200 calories
            }
        };
        
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should handle large food list", output.contains("Food Item 0"));
        assertTrue("Should show correct calorie total", output.contains("Total calories consumed: 20000"));
    }

    /**
     * Test input that causes integer overflow in calories.
     */
    @Test
    public void testCalorieOverflow() {
        // Arrange
        // Log food with value near Integer.MAX_VALUE to test potential overflow
        String calorieValue = Integer.toString(Integer.MAX_VALUE - 10);
        String input = "2\n2025\n1\n1\nHighCalorieFood\n100\n" + calorieValue + "\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should handle near-max calorie value", output.contains("Food logged successfully"));
    }

    /**
     * Test extreme whitespace handling in food name.
     */
    @Test
    public void testWhitespaceInFoodName() {
        // Arrange
        // Log food with extreme whitespace in name
        String input = "2\n2025\n1\n1\n   Spaced   Food   Name   \n100\n200\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testService = new TestMealPlanningService();
        mealPlanningMenu = new MealPlanningMenu(testService, authService, new Scanner(System.in));
        
        // Act
        mealPlanningMenu.displayMenu();
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should handle whitespace in food name", output.contains("Food logged successfully"));
    }

   
    
}
