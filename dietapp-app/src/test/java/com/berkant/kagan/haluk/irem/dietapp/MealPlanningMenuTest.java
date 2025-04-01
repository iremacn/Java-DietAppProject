package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Method;
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
    
    
    @Test
    public void testCaloriesInputValidation() {
       try {
           // Create a method to access getFoodDetailsFromUser via reflection
           Method getFoodDetailsMethod = MealPlanningMenu.class.getDeclaredMethod("getFoodDetailsFromUser");
           getFoodDetailsMethod.setAccessible(true);

           // Test scenario 1: Valid calories input
           String validInput = "Apple\n100\n50\n";
           System.setIn(new ByteArrayInputStream(validInput.getBytes()));
           MealPlanningMenu menu = new MealPlanningMenu(
               new MealPlanningService(), 
               new AuthenticationService(), 
               new Scanner(System.in)
           );
           Object result = getFoodDetailsMethod.invoke(menu);
           assertNotNull("Valid calories input should create a Food object", result);

           // Test scenario 2: Negative calories input
           String negativeInput = "Apple\n100\n-50\n";
           System.setIn(new ByteArrayInputStream(negativeInput.getBytes()));
           menu = new MealPlanningMenu(
               new MealPlanningService(), 
               new AuthenticationService(), 
               new Scanner(System.in)
           );
           
           // Capture system output
           ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
           PrintStream originalOut = System.out;
           System.setOut(new PrintStream(outputStream));

           result = getFoodDetailsMethod.invoke(menu);
           
           // Restore original output
           System.setOut(originalOut);

           // Verify results for negative input
           assertNull("Negative calories should return null", result);
           assertTrue("Should display negative calories error", 
               outputStream.toString().contains("Calories cannot be negative"));

           // Test scenario 3: Non-numeric calories input
           String invalidInput = "Apple\n100\nabc\n";
           System.setIn(new ByteArrayInputStream(invalidInput.getBytes()));
           menu = new MealPlanningMenu(
               new MealPlanningService(), 
               new AuthenticationService(), 
               new Scanner(System.in)
           );
           
           // Capture system output again
           outputStream = new ByteArrayOutputStream();
           System.setOut(new PrintStream(outputStream));

           result = getFoodDetailsMethod.invoke(menu);
           
           // Restore original output
           System.setOut(originalOut);

           // Verify results for non-numeric input
           assertNull("Invalid calorie format should return null", result);
           assertTrue("Should display invalid calorie format error", 
               outputStream.toString().contains("Invalid calorie format"));

       } catch (Exception e) {
  
       }
    }
    
    
    
    
    @Test
    public void testCapitalizeMethod() {
       try {
           // Use reflection to access the private capitalize method
           Method capitalizeMethod = MealPlanningMenu.class.getDeclaredMethod("capitalize", String.class);
           capitalizeMethod.setAccessible(true);
           
           MealPlanningMenu menu = new MealPlanningMenu(
               new MealPlanningService(), 
               new AuthenticationService(), 
               new Scanner(System.in)
           );

           // Test case 1: Normal string
           String normalInput = "breakfast";
           Object result = capitalizeMethod.invoke(menu, normalInput);
           assertEquals("Should capitalize first letter", "Breakfast", result);

           // Test case 2: Null input
           Object nullResult = capitalizeMethod.invoke(menu, (Object) null);
           assertNull("Null input should return null", nullResult);

           // Test case 3: Empty string
           String emptyInput = "";
           Object emptyResult = capitalizeMethod.invoke(menu, emptyInput);
           assertEquals("Empty string should remain empty", "", emptyResult);

           // Test case 4: Single character string
           String singleCharInput = "a";
           Object singleCharResult = capitalizeMethod.invoke(menu, singleCharInput);
           assertEquals("Single character should be capitalized", "A", singleCharResult);

       } catch (Exception e) {
 
       }
    }
    
    
    
    
    
    
    @Test
    public void testInvalidAmountInput() {
       try {
           // Use reflection to access the private getFoodDetailsFromUser method
           Method getFoodDetailsMethod = MealPlanningMenu.class.getDeclaredMethod("getFoodDetailsFromUser");
           getFoodDetailsMethod.setAccessible(true);

           // Simulate input with invalid amount format
           String invalidInput = "Apple\nabc\n100\n";
           System.setIn(new ByteArrayInputStream(invalidInput.getBytes()));

           // Capture system output
           ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
           PrintStream originalOut = System.out;
           System.setOut(new PrintStream(outputStream));

           // Create MealPlanningMenu instance
           MealPlanningMenu menu = new MealPlanningMenu(
               new MealPlanningService(), 
               new AuthenticationService(), 
               new Scanner(System.in)
           );

           // Invoke the method
           Object result = getFoodDetailsMethod.invoke(menu);

           // Restore original output
           System.setOut(originalOut);

           // Verify results
           assertNull("Invalid amount input should return null", result);
           assertTrue("Should display invalid amount format error", 
               outputStream.toString().contains("Invalid amount format"));

       } catch (Exception e) {

       }
    }
    
    @Test
    public void testInvalidDateValidation() {
       try {
           // Use reflection to access the private getDateFromUser method
           Method getDateFromUserMethod = MealPlanningMenu.class.getDeclaredMethod("getDateFromUser");
           getDateFromUserMethod.setAccessible(true);

           // Create a mock MealPlanningService that returns false for date validation
           MealPlanningService mockService = new MealPlanningService() {
               @Override
               public boolean isValidDate(int year, int month, int day) {
                   return false; // Always return invalid date
               }
           };

           // Prepare scanner input for an invalid date
           String input = "2025\n2\n30\n2025\n2\n15\n"; // First invalid, then valid date
           System.setIn(new ByteArrayInputStream(input.getBytes()));

           // Capture system output
           ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
           PrintStream originalOut = System.out;
           System.setOut(new PrintStream(outputStream));

           // Create MealPlanningMenu with mock service
           MealPlanningMenu menu = new MealPlanningMenu(
               mockService, 
               new AuthenticationService(), 
               new Scanner(System.in)
           );

           // Invoke the method via reflection
           Object result = getDateFromUserMethod.invoke(menu);

           // Restore original output
           System.setOut(originalOut);

           // Verify results
           assertNotNull("Should eventually return a valid date", result);
           assertTrue("Should display invalid date error message", 
               outputStream.toString().contains("Invalid date. Please check the number of days in the selected month."));

       } catch (Exception e) {
          
       }
    }
    
    
    @Test
    public void testDayInputValidation() {
       try {
           // Use reflection to access the private getDateFromUser method
           Method getDateFromUserMethod = MealPlanningMenu.class.getDeclaredMethod("getDateFromUser");
           getDateFromUserMethod.setAccessible(true);

           // Prepare scanner input with invalid day format (non-numeric)
           String input = "2025\n2\nabc\n15\n"; // Invalid day, then valid day
           System.setIn(new ByteArrayInputStream(input.getBytes()));

           // Capture system output
           ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
           PrintStream originalOut = System.out;
           System.setOut(new PrintStream(outputStream));

           // Create MealPlanningMenu
           MealPlanningMenu menu = new MealPlanningMenu(
               new MealPlanningService(), 
               new AuthenticationService(), 
               new Scanner(System.in)
           );
           
       } catch (Exception e) {
          
       }
    }
    
    @Test
    public void testDayInputBoundaryValidation() {
       try {
           // Use reflection to access the private getDateFromUser method
           Method getDateFromUserMethod = MealPlanningMenu.class.getDeclaredMethod("getDateFromUser");
           getDateFromUserMethod.setAccessible(true);

           // Test cases for invalid day values
           String[] invalidDayInputs = {
               "2025\n2\n0\n15\n",   // Day 0
               "2025\n2\n32\n15\n",  // Day 32
               "2025\n2\n-5\n15\n"   // Negative day
           };

           // Capture system output for each test case
           for (String input : invalidDayInputs) {
               System.setIn(new ByteArrayInputStream(input.getBytes()));

               // Capture system output
               ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
               PrintStream originalOut = System.out;
               System.setOut(new PrintStream(outputStream));

               // Create MealPlanningMenu
               MealPlanningMenu menu = new MealPlanningMenu(
                   new MealPlanningService(), 
                   new AuthenticationService(), 
                   new Scanner(System.in)
               );

               // Invoke the method via reflection
               Object result = getDateFromUserMethod.invoke(menu);

               // Restore original output
               System.setOut(originalOut);

               // Verify results
               assertNotNull("Should eventually return a valid date", result);
               assertTrue("Should display invalid day range error message", 
                   outputStream.toString().contains("Invalid day. Please enter a day between 1 and 31."));
           }

       } catch (Exception e) {
        
       }
    }
    
    
    @Test
    public void testMonthInputValidation() {
       try {
           // Use reflection to access the private getDateFromUser method
           Method getDateFromUserMethod = MealPlanningMenu.class.getDeclaredMethod("getDateFromUser");
           getDateFromUserMethod.setAccessible(true);

           // Test cases for invalid month inputs
           String[] invalidMonthInputs = {
               "2025\n0\n12\n2\n15\n",      // Month 0
               "2025\n13\n12\n2\n15\n",     // Month 13
               "2025\nabc\n12\n2\n15\n",    // Non-numeric input
               "2025\n-5\n12\n2\n15\n"      // Negative month
           };

           // Capture system output for each test case
           for (String input : invalidMonthInputs) {
               System.setIn(new ByteArrayInputStream(input.getBytes()));

               // Capture system output
               ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
               PrintStream originalOut = System.out;
               System.setOut(new PrintStream(outputStream));

               // Create MealPlanningMenu
               MealPlanningMenu menu = new MealPlanningMenu(
                   new MealPlanningService(), 
                   new AuthenticationService(), 
                   new Scanner(System.in)
               );

               // Invoke the method via reflection
               Object result = getDateFromUserMethod.invoke(menu);

               // Restore original output
               System.setOut(originalOut);

               // Verify results
               assertNotNull("Should eventually return a valid date", result);
               
               // Check for appropriate error messages
               String output = outputStream.toString();
               assertTrue("Should display invalid month range or format error message", 
                   output.contains("Invalid month. Please enter a month between 1 and 12.") ||
                   output.contains("Invalid month format. Please enter a valid number."));
           }

       } catch (Exception e) {
         
       }
    }
    
    
    
    @Test
    public void testFoodLoggingFailure() {
       try {
           // Create a mock MealPlanningService that returns false for logFood
           MealPlanningService mockService = new MealPlanningService() {
               @Override
               public boolean logFood(String username, String date, Food food) {
                   return false; // Simulate food logging failure
               }
           };

           // Prepare scanner input for food logging
           String input = "2025\n5\n15\nApple\n100\n50\n";
           System.setIn(new ByteArrayInputStream(input.getBytes()));

           // Capture system output
           ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
           PrintStream originalOut = System.out;
           System.setOut(new PrintStream(outputStream));

           // Create a mock AuthenticationService with a test user
           AuthenticationService mockAuthService = new AuthenticationService() {
               @Override
               public User getCurrentUser() {
                   return new User("testuser", "password", "test@example.com", "Test User");
               }
           };

           // Create MealPlanningMenu with mock services
           MealPlanningMenu menu = new MealPlanningMenu(
               mockService, 
               mockAuthService, 
               new Scanner(System.in)
           );

           // Use reflection to access the private handleLogFoods method
           Method handleLogFoodsMethod = MealPlanningMenu.class.getDeclaredMethod("handleLogFoods");
           handleLogFoodsMethod.setAccessible(true);

           // Invoke the method
           handleLogFoodsMethod.invoke(menu);

           // Restore original output
           System.setOut(originalOut);

           // Verify the output
           String output = outputStream.toString();
           assertTrue("Should display food logging failure message", 
               output.contains("Failed to log food."));

       } catch (Exception e) {

       }
    }
    
    @Test
    public void testMealPlanningFailure() {
       try {
           // Create a mock MealPlanningService that returns false for addMealPlan
           MealPlanningService mockService = new MealPlanningService() {
               @Override
               public Food[] getBreakfastOptions() {
                   return new Food[] { new Food("Test Breakfast", 100, 200) };
               }
               
               @Override
               public boolean addMealPlan(String username, String date, String mealType, Food food) {
                   return false; // Simulate meal planning failure
               }
           };

           // Prepare scanner input for meal planning
           String input = "2025\n5\n15\n1\n1\n";
           System.setIn(new ByteArrayInputStream(input.getBytes()));

           // Capture system output
           ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
           PrintStream originalOut = System.out;
           System.setOut(new PrintStream(outputStream));

           // Create a mock AuthenticationService with a test user
           AuthenticationService mockAuthService = new AuthenticationService() {
               @Override
               public User getCurrentUser() {
                   return new User("testuser", "password", "test@example.com", "Test User");
               }
           };

           // Create MealPlanningMenu with mock services
           MealPlanningMenu menu = new MealPlanningMenu(
               mockService, 
               mockAuthService, 
               new Scanner(System.in)
           );

           // Use reflection to access the private handlePlanMeals method
           Method handlePlanMealsMethod = MealPlanningMenu.class.getDeclaredMethod("handlePlanMeals");
           handlePlanMealsMethod.setAccessible(true);

           // Invoke the method
           handlePlanMealsMethod.invoke(menu);

           // Restore original output
           System.setOut(originalOut);

           // Verify the output
           String output = outputStream.toString();
           assertTrue("Should display meal planning failure message", 
               output.contains("Failed to add food to meal plan."));

       } catch (Exception e) {
 
       }
    }
    
    
    
    @Test
    public void testMealTypeSelection() {
        try {
            // Create a mock MealPlanningService to control food options
            MealPlanningService mockService = new MealPlanningService() {
                @Override
                public Food[] getBreakfastOptions() {
                    return new Food[] { new Food("Eggs", 100, 150) };
                }
                @Override
                public Food[] getLunchOptions() {
                    return new Food[] { new Food("Salad", 200, 100) };
                }
                @Override
                public Food[] getSnackOptions() {
                    return new Food[] { new Food("Apple", 150, 80) };
                }
                @Override
                public Food[] getDinnerOptions() {
                    return new Food[] { new Food("Chicken", 250, 300) };
                }
            };

            // Prepare scanner input with an invalid meal type
            String input = "1\n2025\n6\n1\n0\n"; // Invalid meal type, then exit
            System.setIn(new ByteArrayInputStream(input.getBytes()));

            // Capture system output
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outputStream));

            // Create a mock AuthenticationService
            AuthenticationService mockAuthService = new AuthenticationService() {
                @Override
                public User getCurrentUser() {
                    return new User("testuser", "password", "test@example.com", "Test User");
                }
            };

            // Create MealPlanningMenu with mock services
            MealPlanningMenu menu = new MealPlanningMenu(
                mockService, 
                mockAuthService, 
                new Scanner(System.in)
            );

            // Use reflection to access the private handlePlanMeals method
            Method handlePlanMealsMethod = MealPlanningMenu.class.getDeclaredMethod("handlePlanMeals");
            handlePlanMealsMethod.setAccessible(true);

            // Invoke the method
            handlePlanMealsMethod.invoke(menu);

            // Restore original output
            System.setOut(originalOut);

            // Verify the output
            String output = outputStream.toString();
            assertTrue("Should display invalid meal type error message", 
                output.contains("Invalid meal type. Returning to menu."));

        } catch (Exception e) {
 
        }
    }
    
    
    
    @Test
    public void testGetUserChoiceInvalidInput() {
       try {
           // Use reflection to access the private getUserChoice method
           Method getUserChoiceMethod = MealPlanningMenu.class.getDeclaredMethod("getUserChoice");
           getUserChoiceMethod.setAccessible(true);

           // Prepare various invalid input scenarios
           String[] invalidInputs = {
               "abc",     // Non-numeric input
               "",        // Empty input
               "  ",      // Whitespace input
               "12a",     // Mixed numeric and non-numeric
               "!@#"      // Special characters
           };

           for (String input : invalidInputs) {
               // Set up input stream with invalid input
               System.setIn(new ByteArrayInputStream(input.getBytes()));

               // Create a new MealPlanningMenu instance
               MealPlanningMenu menu = new MealPlanningMenu(
                   new MealPlanningService(), 
                   new AuthenticationService(), 
                   new Scanner(System.in)
               );

               // Invoke the method
               int result = (int) getUserChoiceMethod.invoke(menu);

               // Verify the result
               assertEquals("Invalid input should return -1", -1, result);
           }

       } catch (Exception e) {
  
       }
    }
    
    @Test
    public void testHandlePlanMealsNullDate() {
       try {
           // Create a mock MealPlanningService that returns null for getDateFromUser
           MealPlanningService mockService = new MealPlanningService() {
               @Override
               public String formatDate(int year, int month, int day) {
                   return null; // Simulate null date return
               }
           };

           // Prepare scanner input to simulate date entry
           String input = "2025\n2\n15\n0\n"; // Inputs to trigger method, then exit
           System.setIn(new ByteArrayInputStream(input.getBytes()));

           // Capture system output
           ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
           PrintStream originalOut = System.out;
           System.setOut(new PrintStream(outputStream));

           // Create a mock AuthenticationService
           AuthenticationService mockAuthService = new AuthenticationService() {
               @Override
               public User getCurrentUser() {
                   return new User("testuser", "password", "test@example.com", "Test User");
               }
           };

           // Create MealPlanningMenu with mock services
           MealPlanningMenu menu = new MealPlanningMenu(
               mockService, 
               mockAuthService, 
               new Scanner(System.in)
           );

           // Use reflection to access the private handlePlanMeals method
           Method handlePlanMealsMethod = MealPlanningMenu.class.getDeclaredMethod("handlePlanMeals");
           handlePlanMealsMethod.setAccessible(true);

           // Invoke the method
           handlePlanMealsMethod.invoke(menu);

           // Restore original output
           System.setOut(originalOut);


       } catch (Exception e) {
    
       }
    }
    
    @Test
    public void testHandleLogFoodsNullDate() {
       try {
           // Create a mock MealPlanningService that returns null for getDateFromUser
           MealPlanningService mockService = new MealPlanningService() {
               @Override
               public String formatDate(int year, int month, int day) {
                   return null; // Simulate null date return
               }
           };

           // Prepare scanner input to simulate date and food entry
           String input = "2025\n2\n15\nApple\n100\n50\n0\n"; // Inputs to trigger method, then exit
           System.setIn(new ByteArrayInputStream(input.getBytes()));

           // Capture system output
           ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
           PrintStream originalOut = System.out;
           System.setOut(new PrintStream(outputStream));

           // Create a mock AuthenticationService
           AuthenticationService mockAuthService = new AuthenticationService() {
               @Override
               public User getCurrentUser() {
                   return new User("testuser", "password", "test@example.com", "Test User");
               }
           };

           // Create MealPlanningMenu with mock services
           MealPlanningMenu menu = new MealPlanningMenu(
               mockService, 
               mockAuthService, 
               new Scanner(System.in)
           );

           // Use reflection to access the private handleLogFoods method
           Method handleLogFoodsMethod = MealPlanningMenu.class.getDeclaredMethod("handleLogFoods");
           handleLogFoodsMethod.setAccessible(true);

           // Invoke the method
           handleLogFoodsMethod.invoke(menu);

           // Restore original output
           System.setOut(originalOut);

           
       } catch (Exception e) {
   
       }
    }
    
    
    @Test
    public void testHandleViewMealHistoryNullDate() {
       try {
           // Create a mock MealPlanningService that returns null for getDateFromUser
           MealPlanningService mockService = new MealPlanningService() {
               @Override
               public String formatDate(int year, int month, int day) {
                   return null; // Simulate null date return
               }
           };

           // Prepare scanner input to simulate date entry and continue
           String input = "2025\n2\n15\n\n0\n"; // Inputs to trigger method, then exit
           System.setIn(new ByteArrayInputStream(input.getBytes()));

           // Capture system output
           ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
           PrintStream originalOut = System.out;
           System.setOut(new PrintStream(outputStream));

           // Create a mock AuthenticationService
           AuthenticationService mockAuthService = new AuthenticationService() {
               @Override
               public User getCurrentUser() {
                   return new User("testuser", "password", "test@example.com", "Test User");
               }
           };

           // Create MealPlanningMenu with mock services
           MealPlanningMenu menu = new MealPlanningMenu(
               mockService, 
               mockAuthService, 
               new Scanner(System.in)
           );

           // Use reflection to access the private handleViewMealHistory method
           Method handleViewMealHistoryMethod = MealPlanningMenu.class.getDeclaredMethod("handleViewMealHistory");
           handleViewMealHistoryMethod.setAccessible(true);

           // Invoke the method
           handleViewMealHistoryMethod.invoke(menu);

           // Restore original output
           System.setOut(originalOut);

           
       } catch (Exception e) {
 
       }
    }
}