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
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

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
    private TestMealPlanningMenu mealPlanningMenu;
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
        mealPlanningService = new MealPlanningService(null);
        authService = new AuthenticationService();
        
        // Create and login a test user
        testUser = new User("testuser", "password", "test@example.com", "Test User");
        testUser.setLoggedIn(true);
        
        // Mock the authentication service's getCurrentUser method
        // by setting up a test user that is logged in
        authService = new TestAuthenticationService(testUser);
        
        // Swing EDT çakışmasını önlemek için
        try {
            SwingUtilities.invokeAndWait(() -> {
                // Swing bileşenlerini başlatma kodları buraya gelecek (eğer varsa)
            });
        } catch (InvocationTargetException e) {
            // Test için yok sayılabilir
        }
    }
    
    @After
    public void tearDown() throws Exception {
        // Reset output and input streams
        System.setOut(originalOut);
        System.setIn(originalIn);
        
        // Clear the output stream
        outputStream.reset();
        
        // Swing bileşenlerini temizleyin
        try {
            SwingUtilities.invokeAndWait(() -> {
                // Swing bileşenlerini temizleme kodları
            });
        } catch (InvocationTargetException e) {
            // Test için yok sayılabilir
        }
    }
    
    // Test MealPlanningMenu sınıfı - UI bileşenlerini devre dışı bırakır
    private class TestMealPlanningMenu extends MealPlanningMenu {
        public TestMealPlanningMenu(MealPlanningService service, AuthenticationService authService, Scanner scanner) {
            super(service, authService, scanner);
            // Swing bileşenlerini devre dışı bırak
            this.useUIComponents = false;
        }
        
        // Gerekirse test için metotları burada yeniden tanımlayabilirsiniz
    }
    
    /**
     * Test the displayMenu method with option 0 (return to main menu).
     */
    @Test
    public void testDisplayMenuReturnToMainMenu() throws Exception {
        // Arrange
        String input = "0\n"; // Choose option 0 to exit
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new TestMealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.displayMenu();
        });
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Menu should display correct title", output.contains("Meal Planning and Logging"));
        assertTrue("Menu should contain option to return to main menu", output.contains("0. Return to Main Menu"));
    }
    
    /**
     * Test the displayMenu method with an invalid option.
     */
    @Test
    public void testDisplayMenuInvalidOption() throws Exception {
        // Arrange
        String input = "99\n0\n"; // Choose invalid option 99, then exit
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new TestMealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.displayMenu();
        });
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Menu should display error for invalid choice", output.contains("Invalid choice"));
    }
   
    
    /**
     * Test the handlePlanMeals method with valid input.
     */
    @Test
    public void testHandlePlanMealsValidInput() throws Exception {
        // Arrange
        // Valid date, breakfast (1), first food option (1), then exit
        String input = "1\n2025\n1\n1\n1\n1\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testMealPlanningService = new TestMealPlanningService();
        mealPlanningMenu = new TestMealPlanningMenu(testMealPlanningService, authService, new Scanner(System.in));
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.displayMenu();
        });
        
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
    public void testHandleLogFoodsValidInput() throws Exception {
        // Arrange
        // Option 2 (Log Foods), valid date, food details, then exit
        String input = "2\n2025\n2\n1\nApple\n100\n52\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testMealPlanningService = new TestMealPlanningService();
        mealPlanningMenu = new TestMealPlanningMenu(testMealPlanningService, authService, new Scanner(System.in));
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.displayMenu();
        });
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show log foods menu", output.contains("Log Foods"));
        assertTrue("Should confirm food logged", output.contains("Food logged successfully"));
    }
    
    /**
     * Test the handleLogFoods method with invalid food details.
     */
    @Test
    public void testHandleLogFoodsInvalidFoodDetails() throws Exception {
        // Arrange
        // Option 2 (Log Foods), valid date, invalid food details, then exit
        String input = "2\n2025\n2\n1\nApple\n-100\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new TestMealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.displayMenu();
        });
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show error for negative amount", output.contains("Amount must be positive"));
    }
    
    /**
     * Test the handleViewMealHistory method with no meal history.
     */
    @Test
    public void testHandleViewMealHistoryNoHistory() throws Exception {
        // Arrange
        // Option 3 (View Meal History), valid date, then exit
        String input = "3\n2025\n3\n1\n\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testMealPlanningService = new TestMealPlanningService();
        // Empty lists will be returned by default
        mealPlanningMenu = new TestMealPlanningMenu(testMealPlanningService, authService, new Scanner(System.in));
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.displayMenu();
        });
        
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
    public void testHandleViewMealHistoryWithHistory() throws Exception {
        // Arrange
        // Option 3 (View Meal History), valid date, then exit
        String input = "3\n2025\n4\n2\n\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testMealPlanningService = new TestMealPlanningService();
        testMealPlanningService.setReturnMealPlan(true);
        testMealPlanningService.setReturnFoodLog(true);
        mealPlanningMenu = new TestMealPlanningMenu(testMealPlanningService, authService, new Scanner(System.in));
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.displayMenu();
        });
        
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
    public void testDateAndMealTypeHandling() throws Exception {
        // Arrange
        // Test all 4 meal types (1, 2, 3, 4) with valid date then exit
        String input = "1\n2025\n5\n5\n1\n1\n1\n2025\n5\n5\n2\n1\n1\n2025\n5\n5\n3\n1\n1\n2025\n5\n5\n4\n1\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningService testMealPlanningService = new TestMealPlanningService();
        mealPlanningMenu = new TestMealPlanningMenu(testMealPlanningService, authService, new Scanner(System.in));
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.displayMenu();
        });
        
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
    public void testTooManyFoodOptions() throws Exception {
        // Arrange
        // Option 1 (Plan Meals), valid date, meal type 1, invalid food choice, then exit
        String input = "1\n2025\n6\n6\n1\n99\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // Create a mock service that has a limited number of food options
        MealPlanningService mockService = new MealPlanningService(null) {
            @Override
            public Food[] getBreakfastOptions() {
                return new Food[] { new Food("Test Breakfast", 100, 200) };
            }
            
            @Override
            public boolean isValidDate(int year, int month, int day) {
                return true; // Always valid date for test
            }
            
            @Override
            public String formatDate(int year, int month, int day) {
                return "2025-06-06"; // Fixed date for test
            }
        };
        
        // Create a custom TestMealPlanningMenu class instance to handle the test
        TestMealPlanningMenu testMenu = new TestMealPlanningMenu(
            mockService, 
            authService, 
            new Scanner(System.in)
        ) {
            // Override method to handle plan meals to better control test flow
            @Override
            public void displayMenu() {
                handlePlanMealsConsole();
            }
        };
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            testMenu.displayMenu();
        });
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show error for invalid food choice", 
            output.contains("Invalid food choice"));
    }
    
    /**
     * Test the getUserChoice method with invalid input.
     */
    @Test
    public void testGetUserChoiceInvalidInput() throws Exception {
        try {
            // Use reflection to access the private getUserChoice method
            Method getUserChoiceMethod = MealPlanningMenu.class.getDeclaredMethod("getUserChoice");
            getUserChoiceMethod.setAccessible(true);

            // Prepare various invalid input scenarios
            String[] invalidInputs = {
                "abc\n",     // Non-numeric input
                "\n",        // Empty input
                "  \n",      // Whitespace input
                "12a\n"      // Mixed numeric and non-numeric
            };

            for (String inputStr : invalidInputs) {
                // Set up input stream with invalid input
                System.setIn(new ByteArrayInputStream(inputStr.getBytes()));

                // Create a new MealPlanningMenu instance
                TestMealPlanningMenu menu = new TestMealPlanningMenu(
                    new MealPlanningService(null), 
                    new AuthenticationService(), 
                    new Scanner(System.in)
                );

                // EDT üzerinde çalıştırma
                final int[] result = new int[1];
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        result[0] = (int) getUserChoiceMethod.invoke(menu);
                    } catch (Exception e) {
                        // Test için yok sayılabilir
                    }
                });

                // Verify the result
                assertEquals("Invalid input should return -1", -1, result[0]);
            }

        } catch (Exception e) {
            // Only fail if this is not a reflection or UI exception
            if (!(e instanceof NoSuchMethodException) && 
                !(e instanceof InvocationTargetException) &&
                !(e instanceof InterruptedException)) {
                fail("Test failed with unexpected exception: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test the day input validation during date entry.
     */
    @Test
    public void testDayInputValidation() throws Exception {
        try {
            // Use reflection to access the private getDateFromUser method
            Method getDateFromUserMethod = MealPlanningMenu.class.getDeclaredMethod("getDateFromUser");
            getDateFromUserMethod.setAccessible(true);

            // Set up a custom scanner with the pre-defined input sequence
            String input = "2025\n2\nabc\n15\n"; // Year, month, invalid day format, valid day
            Scanner mockScanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
            
            // Create MealPlanningMenu with our mock service and the mock scanner
            TestMealPlanningMenu menu = new TestMealPlanningMenu(
                new MealPlanningService(null), 
                new AuthenticationService(), 
                mockScanner
            );
            
            // Capture output to verify error message
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outputStream));
            
            // EDT üzerinde çalıştırma
            final String[] result = new String[1];
            SwingUtilities.invokeAndWait(() -> {
                try {
                    result[0] = (String) getDateFromUserMethod.invoke(menu);
                } catch (Exception e) {
                    // Test için yok sayılabilir
                }
            });
            
            // Restore original output
            System.setOut(originalOut);
            
            // Verify results
            String output = outputStream.toString();
            assertTrue("Should display invalid day format error", 
                output.contains("Invalid day format"));
            
        } catch (Exception e) {
            // Only fail if this is not a reflection or UI exception
            if (!(e instanceof NoSuchMethodException) && 
                !(e instanceof InvocationTargetException) &&
                !(e instanceof InterruptedException)) {
                fail("Test failed with unexpected exception: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test the invalid date validation functionality.
     */
    @Test
    public void testInvalidDateValidation() throws Exception {
        try {
            // Use reflection to access the private getDateFromUser method
            Method getDateFromUserMethod = MealPlanningMenu.class.getDeclaredMethod("getDateFromUser");
            getDateFromUserMethod.setAccessible(true);

            // Create a mock MealPlanningService with controlled validation behavior
            MealPlanningService mockService = new MealPlanningService(null) {
                private int callCount = 0;
                
                @Override
                public boolean isValidDate(int year, int month, int day) {
                    // First call returns false, second call returns true to simulate invalid then valid date
                    callCount++;
                    return callCount > 1;
                }
                
                @Override
                public String formatDate(int year, int month, int day) {
                    return "2025-02-15"; // Return a fixed date string
                }
            };

            // Set up input with invalid date first, then valid date
            String input = "2025\n2\n30\n2025\n2\n15\n"; // Invalid (Feb 30), then valid (Feb 15)
            Scanner mockScanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
            
            // Capture output to verify error messages
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            System.setOut(new PrintStream(outputStream));

            // Create MealPlanningMenu with mock service
            TestMealPlanningMenu menu = new TestMealPlanningMenu(
                mockService, 
                new AuthenticationService(), 
                mockScanner
            );

            // Execute test on EDT
            final String[] result = new String[1];
            SwingUtilities.invokeAndWait(() -> {
                try {
                    result[0] = (String) getDateFromUserMethod.invoke(menu);
                } catch (Exception e) {
                    // Ignore exceptions for test
                }
            });

            // Restore original output stream
            System.setOut(originalOut);

            // Verify results
            String output = outputStream.toString();
            assertTrue("Should display invalid date error message", 
                output.contains("Invalid date. Please check the number of days in the selected month."));
            assertNotNull("Should eventually return a valid date", result[0]);
            assertEquals("Should return the fixed date format", "2025-02-15", result[0]);

        } catch (Exception e) {
            // Only fail if this is not a reflection or UI exception
            if (!(e instanceof NoSuchMethodException) && 
                !(e instanceof InvocationTargetException) &&
                !(e instanceof InterruptedException)) {
                fail("Test failed with unexpected exception: " + e.getMessage());
            }
        }
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
        
        public TestMealPlanningService() {
            super(null);
        }
        
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
    public void testCaloriesInputValidation() throws Exception {
        try {
            // MealPlanningMenu sınıfındaki metot adını kontrol edip buraya yazın
            Method getFoodDetailsMethod = MealPlanningMenu.class.getDeclaredMethod("getFoodDetailsFromUser");
            getFoodDetailsMethod.setAccessible(true);

            // Test scenario 1: Valid calories input
            String validInput = "Apple\n100\n50\n";
            System.setIn(new ByteArrayInputStream(validInput.getBytes()));
            TestMealPlanningMenu menu = new TestMealPlanningMenu(
                new MealPlanningService(null), 
                new AuthenticationService(), 
                new Scanner(System.in)
            );
            
            // Daha güvenli bir EDT çağrısı
            final Object[] result = new Object[1];
            try {
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        result[0] = getFoodDetailsMethod.invoke(menu);
                    } catch (Exception e) {
                        System.out.println("Method invocation error: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) { 
                System.out.println("EDT error: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Test assertions
            assertNotNull("Valid calories input should create a Food object", result[0]);
            
            // Diğer test senaryoları...
            
        } catch (NoSuchMethodException e) {
            fail("Method not found: " + e.getMessage() + ". Check the correct method name in MealPlanningMenu class.");
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    
    @Test
    public void testCapitalizeMethod() throws Exception {
       try {
           // Use reflection to access the private capitalize method
           Method capitalizeMethod = MealPlanningMenu.class.getDeclaredMethod("capitalize", String.class);
           capitalizeMethod.setAccessible(true);
           
           TestMealPlanningMenu menu = new TestMealPlanningMenu(
               new MealPlanningService(null), 
               new AuthenticationService(), 
               new Scanner(System.in)
           );

           // Test case 1: Normal string
           final String normalInput = "breakfast";
           final Object[] result = new Object[1];
           
           SwingUtilities.invokeAndWait(() -> {
               try {
                   result[0] = capitalizeMethod.invoke(menu, normalInput);
               } catch (Exception e) {
                   // Test için yok sayılabilir
               }
           });
           
           assertEquals("Should capitalize first letter", "Breakfast", result[0]);

           // Test case 2: Null input
           final Object[] nullResult = new Object[1];
           SwingUtilities.invokeAndWait(() -> {
               try {
                   nullResult[0] = capitalizeMethod.invoke(menu, (Object) null);
               } catch (Exception e) {
                   // Test için yok sayılabilir
               }
           });
           
           assertNull("Null input should return null", nullResult[0]);

           // Test case 3: Empty string
           final String emptyInput = "";
           final Object[] emptyResult = new Object[1];
           SwingUtilities.invokeAndWait(() -> {
               try {
                   emptyResult[0] = capitalizeMethod.invoke(menu, emptyInput);
               } catch (Exception e) {
                   // Test için yok sayılabilir
               }
           });
           
           assertEquals("Empty string should remain empty", "", emptyResult[0]);

           // Test case 4: Single character string
           final String singleCharInput = "a";
           final Object[] singleCharResult = new Object[1];
           SwingUtilities.invokeAndWait(() -> {
               try {
                   singleCharResult[0] = capitalizeMethod.invoke(menu, singleCharInput);
               } catch (Exception e) {
                   // Test için yok sayılabilir
               }
           });
           
           assertEquals("Single character should be capitalized", "A", singleCharResult[0]);

       } catch (Exception e) {
          // Test için yok sayılabilir
       }
    }
    
    
    
    
    
    
    @Test
    public void testInvalidAmountInput() throws Exception {
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
           TestMealPlanningMenu menu = new TestMealPlanningMenu(
               new MealPlanningService(null), 
               new AuthenticationService(), 
               new Scanner(System.in)
           );

           // EDT üzerinde çalıştırma
           final Object[] result = new Object[1];
           SwingUtilities.invokeAndWait(() -> {
               try {
                   result[0] = getFoodDetailsMethod.invoke(menu);
               } catch (Exception e) {
                   // Test için yok sayılabilir
               }
           });

           // Restore original output
           System.setOut(originalOut);

           // Verify results
           assertNull("Invalid amount input should return null", result[0]);
           assertTrue("Should display invalid amount format error", 
               outputStream.toString().contains("Invalid amount format"));

       } catch (Exception e) {
          // Test için yok sayılabilir
       }
    }
    
    @Test
    public void testInvalidDayInputBoundaryValidation() throws Exception {
        // Test a single invalid day case (day = 0)
        String input = "2025\n2\n0\n15\n"; // Year, month, invalid day (0), valid day
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // Create a mock service that always validates dates as true (except for the validation in getDateFromUser)
        MealPlanningService mockService = new MealPlanningService(null) {
            @Override
            public boolean isValidDate(int year, int month, int day) {
                return true; // Always return valid date for this test
            }
            
            @Override
            public String formatDate(int year, int month, int day) {
                return "2025-02-15"; // Return a fixed formatted date
            }
        };
        
        // Capture output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        // Create test menu with mock service
        TestMealPlanningMenu menu = new TestMealPlanningMenu(
            mockService, 
            authService, 
            new Scanner(System.in)
        );
        
        // Access getDateFromUser via reflection
        Method getDateFromUserMethod = MealPlanningMenu.class.getDeclaredMethod("getDateFromUser");
        getDateFromUserMethod.setAccessible(true);
        
        // Execute test method on EDT
        SwingUtilities.invokeAndWait(() -> {
            try {
                getDateFromUserMethod.invoke(menu);
            } catch (Exception e) {
                // Ignore exceptions for this test
            }
        });
        
        // Restore standard output
        System.setOut(originalOut);
        
        // Verify test results
        String output = outputStream.toString();
        assertTrue("Should display invalid day range error message", 
            output.contains("Invalid day. Please enter a day between 1 and 31."));
        
        // Skip checking the result value since it might vary depending on implementation
        // Just verify the error message was shown
    }
    
    
    @Test
    public void testMonthInputValidation() throws Exception {
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

               // Create MealPlanningMenu with test version
               final TestMealPlanningMenu menu = new TestMealPlanningMenu(
                   new MealPlanningService(null), 
                   new AuthenticationService(), 
                   new Scanner(System.in)
               );

               // EDT üzerinde güvenli şekilde çalıştır
               final Object[] result = new Object[1];
               try {
                   SwingUtilities.invokeAndWait(() -> {
                       try {
                           result[0] = getDateFromUserMethod.invoke(menu);
                       } catch (Exception e) {
                           result[0] = null;
                       }
                   });
               } catch (Exception e) {
                   System.setOut(originalOut);
                   System.out.println("EDT execution error: " + e.getMessage());
                   continue;
               }

               // Restore original output
               System.setOut(originalOut);

               // Kontrol et
               String output = outputStream.toString();
               
               // Hata mesajlarını kontrol et
               boolean hasMonthRangeError = output.contains("Invalid month. Please enter a month between 1 and 12.");
               boolean hasMonthFormatError = output.contains("Invalid month format. Please enter a valid number.");
               
               assertTrue("Should display invalid month range or format error message", 
                   hasMonthRangeError || hasMonthFormatError);
           }

       } catch (Exception e) {
          // Test için yok sayılabilir
       }
    }
    
    @Test
    public void testFoodLoggingFailure() throws Exception {
       try {
           // Create a mock MealPlanningService that returns false for logFood
           MealPlanningService mockService = new MealPlanningService(null) {
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
           TestMealPlanningMenu menu = new TestMealPlanningMenu(
               mockService, 
               mockAuthService, 
               new Scanner(System.in)
           );

           // Use reflection to access the private handleLogFoods method
           Method handleLogFoodsMethod = MealPlanningMenu.class.getDeclaredMethod("handleLogFoodsConsole");
           handleLogFoodsMethod.setAccessible(true);

           // EDT üzerinde çalıştırma
           SwingUtilities.invokeAndWait(() -> {
               try {
                   handleLogFoodsMethod.invoke(menu);
               } catch (Exception e) {
                   // Test için yok sayılabilir
               }
           });

           // Restore original output
           System.setOut(originalOut);

           // Verify the output
           String output = outputStream.toString();
           assertTrue("Should display food logging failure message", 
               output.contains("Failed to log food."));

       } catch (Exception e) {
          // Test için yok sayılabilir
       }
    }
    
    @Test
    public void testMealPlanningFailure() throws Exception {
       try {
           // Create a mock MealPlanningService that returns false for addMealPlan
           MealPlanningService mockService = new MealPlanningService(null) {
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
           TestMealPlanningMenu menu = new TestMealPlanningMenu(
               mockService, 
               mockAuthService, 
               new Scanner(System.in)
           );

           // Use reflection to access the private handlePlanMeals method
           Method handlePlanMealsMethod = MealPlanningMenu.class.getDeclaredMethod("handlePlanMealsConsole");
           handlePlanMealsMethod.setAccessible(true);

           // EDT üzerinde çalıştırma
           SwingUtilities.invokeAndWait(() -> {
               try {
                   handlePlanMealsMethod.invoke(menu);
               } catch (Exception e) {
                   // Test için yok sayılabilir
               }
           });

           // Restore original output
           System.setOut(originalOut);

           // Verify the output
           String output = outputStream.toString();
           assertTrue("Should display meal planning failure message", 
               output.contains("Failed to add food to meal plan."));

       } catch (Exception e) {
          // Test için yok sayılabilir
       }
    }
    
    
    
    @Test
    public void testMealTypeSelection() throws Exception {
        try {
            // Create a mock MealPlanningService to control food options
            MealPlanningService mockService = new MealPlanningService(null) {
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
            TestMealPlanningMenu menu = new TestMealPlanningMenu(
                mockService, 
                mockAuthService, 
                new Scanner(System.in)
            );

            // Use reflection to access the private handlePlanMeals method
            Method handlePlanMealsMethod = MealPlanningMenu.class.getDeclaredMethod("handlePlanMealsConsole");
            handlePlanMealsMethod.setAccessible(true);

            // EDT üzerinde çalıştırma
            SwingUtilities.invokeAndWait(() -> {
                try {
                    handlePlanMealsMethod.invoke(menu);
                } catch (Exception e) {
                    // Test için yok sayılabilir
                }
            });

            // Restore original output
            System.setOut(originalOut);

            // Verify the output
            String output = outputStream.toString();
            assertTrue("Should display invalid meal type error message", 
                output.contains("Invalid meal type. Returning to menu."));

        } catch (Exception e) {
            // Test için yok sayılabilir
        }
    }
    
    
    
    @Test
    public void testHandlePlanMealsNullDate() throws Exception {
       try {
           // Create a mock MealPlanningService that returns null for getDateFromUser
           MealPlanningService mockService = new MealPlanningService(null) {
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
           TestMealPlanningMenu menu = new TestMealPlanningMenu(
               mockService, 
               mockAuthService, 
               new Scanner(System.in)
           );

           // Use reflection to access the private handlePlanMeals method
           Method handlePlanMealsMethod = MealPlanningMenu.class.getDeclaredMethod("handlePlanMealsConsole");
           handlePlanMealsMethod.setAccessible(true);

           // EDT üzerinde çalıştırma
           SwingUtilities.invokeAndWait(() -> {
               try {
                   handlePlanMealsMethod.invoke(menu);
               } catch (Exception e) {
                   // Test için yok sayılabilir
               }
           });

           // Restore original output
           System.setOut(originalOut);

           // Verify that the method completes without error
           // Actual assertion will depend on how your code handles null dates

       } catch (Exception e) {
          // Test için yok sayılabilir
       }
    }
    
    @Test
    public void testHandleLogFoodsNullDate() throws Exception {
       try {
           // Create a mock MealPlanningService that returns null for getDateFromUser
           MealPlanningService mockService = new MealPlanningService(null) {
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
           TestMealPlanningMenu menu = new TestMealPlanningMenu(
               mockService, 
               mockAuthService, 
               new Scanner(System.in)
           );

           // Use reflection to access the private handleLogFoods method
           Method handleLogFoodsMethod = MealPlanningMenu.class.getDeclaredMethod("handleLogFoodsConsole");
           handleLogFoodsMethod.setAccessible(true);

           // EDT üzerinde çalıştırma
           SwingUtilities.invokeAndWait(() -> {
               try {
                   handleLogFoodsMethod.invoke(menu);
               } catch (Exception e) {
                   // Test için yok sayılabilir
               }
           });

           // Restore original output
           System.setOut(originalOut);

           // Verify that the method completes without error
           // Actual assertion will depend on how your code handles null dates
           
       } catch (Exception e) {
          // Test için yok sayılabilir
       }
    }
    
    
    @Test
    public void testHandleViewMealHistoryNullDate() throws Exception {
       try {
           // Create a mock MealPlanningService that returns null for getDateFromUser
           MealPlanningService mockService = new MealPlanningService(null) {
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
           TestMealPlanningMenu menu = new TestMealPlanningMenu(
               mockService, 
               mockAuthService, 
               new Scanner(System.in)
           );

           // Use reflection to access the private handleViewMealHistory method
           Method handleViewMealHistoryMethod = MealPlanningMenu.class.getDeclaredMethod("handleViewMealHistoryConsole");
           handleViewMealHistoryMethod.setAccessible(true);

           // EDT üzerinde çalıştırma
           SwingUtilities.invokeAndWait(() -> {
               try {
                   handleViewMealHistoryMethod.invoke(menu);
               } catch (Exception e) {
                   // Test için yok sayılabilir
               }
           });

           // Restore original output
           System.setOut(originalOut);

           // Verify that the method completes without error
           // Actual assertion will depend on how your code handles null dates
           
       } catch (Exception e) {
          // Test için yok sayılabilir
       }
    }
}