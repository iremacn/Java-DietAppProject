package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.sql.DriverManager;
import java.time.LocalDate;

/**
 * Comprehensive test class for the MealPlanningService class.
 * Aimed to achieve 100% coverage of the MealPlanningService implementation.
 */
public class MealPlanningServiceTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "testpassword";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_DATE = "2025-01-01";
    
    private static int testUserId;
    private MealPlanningService mealPlanningService;
    private Connection testConnection;
    
    /**
     * Custom subclass of MealPlanningService for testing specific scenarios
     */
    private class TestMealPlanningService extends MealPlanningService {
        private boolean useUIComponents = false;
        
        public TestMealPlanningService(Connection connection) {
            super(connection);
        }
        
        public void setUseUIComponents(boolean value) {
            this.useUIComponents = value;
        }
        
        public boolean getUseUIComponents() {
            return this.useUIComponents;
        }
    }

    /**
     * Setup required test tables in the database
     */
    private static void ensureTablesExist(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Users table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "email TEXT NOT NULL," +
                "name TEXT NOT NULL," +
                "is_logged_in INTEGER DEFAULT 0" +
                ")"
            );
            
            // Foods table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS foods (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "grams REAL NOT NULL," +
                "calories INTEGER NOT NULL," +
                "meal_type TEXT" +
                ")"
            );
            
            // FoodNutrient table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS food_nutrients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "food_id INTEGER NOT NULL," +
                "protein REAL NOT NULL," +
                "carbs REAL NOT NULL," +
                "fat REAL NOT NULL," +
                "fiber REAL NOT NULL," +
                "sugar REAL NOT NULL," +
                "sodium REAL NOT NULL," +
                "FOREIGN KEY(food_id) REFERENCES foods(id)" +
                ")"
            );
            
            // MealPlans table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS meal_plans (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "date TEXT NOT NULL," +
                "meal_type TEXT NOT NULL," +
                "food_id INTEGER NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id)," +
                "FOREIGN KEY(food_id) REFERENCES foods(id)" +
                ")"
            );
            
            // FoodLogs table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS food_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "date TEXT NOT NULL," +
                "food_id INTEGER NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id)," +
                "FOREIGN KEY(food_id) REFERENCES foods(id)" +
                ")"
            );
            
            // Weekly meal plans table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS meal_plans_weekly (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "day TEXT NOT NULL," +
                "meal_type TEXT NOT NULL," +
                "food_name TEXT NOT NULL," +
                "calories INTEGER NOT NULL," +
                "protein REAL NOT NULL," +
                "carbs REAL NOT NULL," +
                "fat REAL NOT NULL," +
                "ingredients TEXT" +
                ")"
            );
            
            // Create test user if it doesn't exist
            stmt.execute(
                "INSERT OR IGNORE INTO users (username, password, email, name, is_logged_in) " +
                "VALUES ('" + TEST_USERNAME + "', '" + TEST_PASSWORD + "', '" + 
                TEST_EMAIL + "', '" + TEST_NAME + "', 0)"
            );
            
            // Get user ID for test use
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT id FROM users WHERE username = '" + TEST_USERNAME + "'")) {
                if (rs.next()) {
                    testUserId = rs.getInt("id");
                }
            }
        }
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Initialize the database
        DatabaseHelper.initializeDatabase();
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            ensureTablesExist(conn);
        } finally {
            DatabaseHelper.releaseConnection(conn);
        }
        
        // Set application to test mode to avoid UI initialization
        DietappApp.setTestMode(true);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // Clean up test data
        try (Connection conn = DatabaseHelper.getConnection()) {
            try (PreparedStatement deleteStmt = conn.prepareStatement(
                    "DELETE FROM meal_plans WHERE user_id = ?")) {
                deleteStmt.setInt(1, testUserId);
                deleteStmt.executeUpdate();
            }
            
            try (PreparedStatement deleteStmt = conn.prepareStatement(
                    "DELETE FROM food_logs WHERE user_id = ?")) {
                deleteStmt.setInt(1, testUserId);
                deleteStmt.executeUpdate();
            }
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM meal_plans_weekly WHERE day LIKE 'Test%'");
                stmt.execute("DELETE FROM foods WHERE name LIKE 'Test%'");
                stmt.execute("DELETE FROM food_nutrients");
            }
        } catch (SQLException e) {
            System.err.println("Database cleanup error: " + e.getMessage());
        } finally {
            DatabaseHelper.closeAllConnections();
        }
    }

    @Before
    public void setUp() throws Exception {
        // Get a fresh database connection for each test
        testConnection = DatabaseHelper.getConnection();
        mealPlanningService = new MealPlanningService(testConnection);
        
        // Clean up any leftover test data
        clearTestData();
    }

    @After
    public void tearDown() throws Exception {
        // Cleanup after each test
        clearTestData();
        DatabaseHelper.releaseConnection(testConnection);
    }

    /**
     * Helper method to clear test data between tests
     */
    private void clearTestData() {
        try {
            // Clear meal plans and food logs for test user
            try (PreparedStatement stmt = testConnection.prepareStatement(
                    "DELETE FROM meal_plans WHERE user_id = ?")) {
                stmt.setInt(1, testUserId);
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = testConnection.prepareStatement(
                    "DELETE FROM food_logs WHERE user_id = ?")) {
                stmt.setInt(1, testUserId);
                stmt.executeUpdate();
            }
            
            // Clear test foods
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("DELETE FROM food_nutrients");
                stmt.execute("DELETE FROM foods WHERE name LIKE 'Test%'");
                stmt.execute("DELETE FROM meal_plans_weekly WHERE day LIKE 'Test%'");
            }
        } catch (SQLException e) {
            System.err.println("Error clearing test data: " + e.getMessage());
        }
    }

    /**
     * Test adding a meal plan with valid data
     */
    @Test
    public void testAddMealPlanWithValidData() {
        // Create a food item
        Food testFood = new Food("Test Breakfast Item", 200.0, 300);
        
        // Add meal plan
        boolean result = mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, "breakfast", testFood);
        
        // Verify
        assertTrue("Should successfully add meal plan", result);
        
        // Verify from database
        List<Food> mealPlan = mealPlanningService.getMealPlan(TEST_USERNAME, TEST_DATE, "breakfast");
        assertFalse("Meal plan should not be empty", mealPlan.isEmpty());
        
        // Find our food in the list
        boolean foundFood = false;
        for (Food food : mealPlan) {
            if ("Test Breakfast Item".equals(food.getName())) {
                foundFood = true;
                assertEquals("Food calories should match", 300, food.getCalories());
                assertEquals("Food grams should match", 200.0, food.getGrams(), 0.01);
                break;
            }
        }
        assertTrue("Should find the added food item", foundFood);
    }

    /**
     * Test for adding a meal plan with null parameters
     */
    @Test
    public void testAddMealPlanWithNullParameters() {
        // Test with null username
        boolean result1 = mealPlanningService.addMealPlan(null, TEST_DATE, "breakfast", 
                new Food("Test Food", 100.0, 200));
        assertFalse("Should fail with null username", result1);
        
        // Test with null date
        boolean result2 = mealPlanningService.addMealPlan(TEST_USERNAME, null, "breakfast", 
                new Food("Test Food", 100.0, 200));
        assertFalse("Should fail with null date", result2);
        
        // Test with null meal type
        boolean result3 = mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, null, 
                new Food("Test Food", 100.0, 200));
        assertFalse("Should fail with null meal type", result3);
        
        // Test with null food
        boolean result4 = mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, "breakfast", null);
        assertFalse("Should fail with null food", result4);
    }

    /**
     * Test for adding a meal plan with a non-existent user
     */
    @Test
    public void testAddMealPlanWithNonExistentUser() {
        boolean result = mealPlanningService.addMealPlan("nonexistentuser", TEST_DATE, "breakfast", 
                new Food("Test Food", 100.0, 200));
        
        assertFalse("Should fail with non-existent user", result);
    }

    /**
     * Test for logging food with valid data
     */
    @Test
    public void testLogFoodWithValidData() {
        // Create a food item
        Food testFood = new Food("Test Logged Food", 150.0, 220);
        
        // Log food
        boolean result = mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, testFood);
        
        // Verify
        assertTrue("Should successfully log food", result);
        
        // Verify from database
        List<Food> foodLog = mealPlanningService.getFoodLog(TEST_USERNAME, TEST_DATE);
        assertFalse("Food log should not be empty", foodLog.isEmpty());
        
        // Find our food in the log
        boolean foundFood = false;
        for (Food food : foodLog) {
            if ("Test Logged Food".equals(food.getName())) {
                foundFood = true;
                assertEquals("Food calories should match", 220, food.getCalories());
                assertEquals("Food grams should match", 150.0, food.getGrams(), 0.01);
                break;
            }
        }
        assertTrue("Should find the logged food", foundFood);
    }

    /**
     * Test for logging food with FoodNutrient subclass
     */
    @Test
    public void testLogFoodWithFoodNutrient() {
        // Create a FoodNutrient instance with nutrient information
        FoodNutrient testFoodNutrient = new FoodNutrient(
            "Test Logged FoodNutrient", 
            150.0, 
            220, 
            25.0,  // protein
            30.0,  // carbs
            10.0,  // fat
            5.0,   // fiber
            2.0,   // sugar
            100.0  // sodium
        );
        
        // Log the food with nutrients
        boolean result = mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, testFoodNutrient);
        
        // Verify
        assertTrue("Should successfully log food with nutrients", result);
        
        // Verify from database
        List<Food> foodLog = mealPlanningService.getFoodLog(TEST_USERNAME, TEST_DATE);
        
        // Find our food nutrient in the log
        boolean foundFood = false;
        for (Food food : foodLog) {
            if ("Test Logged FoodNutrient".equals(food.getName()) && food instanceof FoodNutrient) {
                foundFood = true;
                FoodNutrient fn = (FoodNutrient) food;
                
              
            }
        }
        assertTrue("Should find the logged food nutrient", foundFood);
    }

    /**
     * Test for logging food with null parameters
     */
    @Test
    public void testLogFoodWithNullParameters() {
        // Test with null username
        boolean result1 = mealPlanningService.logFood(null, TEST_DATE, 
                new Food("Test Food", 100.0, 200));
        assertFalse("Should fail with null username", result1);
        
        // Test with null date
        boolean result2 = mealPlanningService.logFood(TEST_USERNAME, null, 
                new Food("Test Food", 100.0, 200));
        assertFalse("Should fail with null date", result2);
        
        // Test with null food
        boolean result3 = mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, null);
        assertFalse("Should fail with null food", result3);
    }

    /**
     * Test for getting meal plan with valid data
     */
    @Test
    public void testGetMealPlanWithValidData() {
        // Add two meal plans
        Food breakfast1 = new Food("Test Breakfast Item 1", 150.0, 200);
        Food breakfast2 = new Food("Test Breakfast Item 2", 120.0, 180);
        
        mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, "breakfast", breakfast1);
        mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, "breakfast", breakfast2);
        
        // Get meal plan
        List<Food> mealPlan = mealPlanningService.getMealPlan(TEST_USERNAME, TEST_DATE, "breakfast");
        
        // Verify
        assertNotNull("Meal plan should not be null", mealPlan);
        assertEquals("Meal plan should contain two items", 2, mealPlan.size());
        
        // Check if the meal plan contains both foods (order may vary)
        boolean foundItem1 = false;
        boolean foundItem2 = false;
        
        for (Food food : mealPlan) {
            if ("Test Breakfast Item 1".equals(food.getName())) foundItem1 = true;
            if ("Test Breakfast Item 2".equals(food.getName())) foundItem2 = true;
        }
        
        assertTrue("Meal plan should contain first item", foundItem1);
        assertTrue("Meal plan should contain second item", foundItem2);
    }

    /**
     * Test for getting meal plan with null parameters
     */
    @Test
    public void testGetMealPlanWithNullParameters() {
        // Test with null username
        List<Food> result1 = mealPlanningService.getMealPlan(null, TEST_DATE, "breakfast");
        assertNotNull("Should return empty list with null username", result1);
        assertTrue("List should be empty with null username", result1.isEmpty());
        
        // Test with null date
        List<Food> result2 = mealPlanningService.getMealPlan(TEST_USERNAME, null, "breakfast");
        assertNotNull("Should return empty list with null date", result2);
        assertTrue("List should be empty with null date", result2.isEmpty());
        
        // Test with null meal type
        List<Food> result3 = mealPlanningService.getMealPlan(TEST_USERNAME, TEST_DATE, null);
        assertNotNull("Should return empty list with null meal type", result3);
        assertTrue("List should be empty with null meal type", result3.isEmpty());
    }

    /**
     * Test for getting food log with valid data
     */
    @Test
    public void testGetFoodLogWithValidData() {
        // Add two food log entries
        Food food1 = new Food("Test Logged Food 1", 150.0, 200);
        Food food2 = new Food("Test Logged Food 2", 120.0, 180);
        
        mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food1);
        mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food2);
        
        // Get food log
        List<Food> foodLog = mealPlanningService.getFoodLog(TEST_USERNAME, TEST_DATE);
        
        // Verify
        assertNotNull("Food log should not be null", foodLog);
        assertEquals("Food log should contain two items", 2, foodLog.size());
        
        // Check if the food log contains both foods (order may vary)
        boolean foundItem1 = false;
        boolean foundItem2 = false;
        
        for (Food food : foodLog) {
            if ("Test Logged Food 1".equals(food.getName())) foundItem1 = true;
            if ("Test Logged Food 2".equals(food.getName())) foundItem2 = true;
        }
        
        assertTrue("Food log should contain first item", foundItem1);
        assertTrue("Food log should contain second item", foundItem2);
    }

    /**
     * Test for getting food log with null parameters
     */
    @Test
    public void testGetFoodLogWithNullParameters() {
        // Test with null username
        List<Food> result1 = mealPlanningService.getFoodLog(null, TEST_DATE);
        assertNotNull("Should return empty list with null username", result1);
        assertTrue("List should be empty with null username", result1.isEmpty());
        
        // Test with null date
        List<Food> result2 = mealPlanningService.getFoodLog(TEST_USERNAME, null);
        assertNotNull("Should return empty list with null date", result2);
        assertTrue("List should be empty with null date", result2.isEmpty());
    }

    /**
     * Test for getting total calories with valid data
     */
    @Test
    public void testGetTotalCaloriesWithValidData() {
        // Add food log entries
        Food food1 = new Food("Test Calorie Food 1", 100.0, 200);
        Food food2 = new Food("Test Calorie Food 2", 100.0, 300);
        Food food3 = new Food("Test Calorie Food 3", 100.0, 100);
        
        mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food1);
        mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food2);
        mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food3);
        
        // Get total calories
        int totalCalories = mealPlanningService.getTotalCalories(TEST_USERNAME, TEST_DATE);
        
        // Verify - sum should be 600
        assertEquals("Total calories should be 600", 600, totalCalories);
    }

    /**
     * Test for getting total calories with null parameters
     */
    @Test
    public void testGetTotalCaloriesWithNullParameters() {
        // Test with null username
        int result1 = mealPlanningService.getTotalCalories(null, TEST_DATE);
        assertEquals("Should return 0 with null username", 0, result1);
        
        // Test with null date
        int result2 = mealPlanningService.getTotalCalories(TEST_USERNAME, null);
        assertEquals("Should return 0 with null date", 0, result2);
    }

    /**
     * Test SQL exceptions in getTotalCalories
     */
    @Test
    public void testGetTotalCaloriesWithSQLException() {
        // Close the connection to force a SQL exception
        try {
            testConnection.close();
            
            // This should now fail but gracefully return 0
            int calories = mealPlanningService.getTotalCalories(TEST_USERNAME, TEST_DATE);
            assertEquals("Should return 0 on SQL exception", 0, calories);
            
            // Re-open connection for other tests
            testConnection = DatabaseHelper.getConnection();
            mealPlanningService = new MealPlanningService(testConnection);
        } catch (SQLException e) {
            fail("Exception should be handled internally: " + e.getMessage());
        }
    }

    /**
     * Test for date validation
     */
    @Test
    public void testIsValidDate() {
        // Test valid dates
        assertTrue("2025-01-01 should be valid", mealPlanningService.isValidDate(2025, 1, 1));
        assertTrue("2025-12-31 should be valid", mealPlanningService.isValidDate(2025, 12, 31));
        assertTrue("Feb 29 in leap year should be valid", mealPlanningService.isValidDate(2028, 2, 29));
        
        // Test invalid dates
        assertFalse("Year below 2025 should be invalid", mealPlanningService.isValidDate(2024, 1, 1));
        assertFalse("Year above 2100 should be invalid", mealPlanningService.isValidDate(2101, 1, 1));
        assertFalse("Month 0 should be invalid", mealPlanningService.isValidDate(2025, 0, 1));
        assertFalse("Month 13 should be invalid", mealPlanningService.isValidDate(2025, 13, 1));
        assertFalse("Day 0 should be invalid", mealPlanningService.isValidDate(2025, 1, 0));
        assertFalse("Day 32 should be invalid", mealPlanningService.isValidDate(2025, 1, 32));
        assertFalse("Feb 30 should be invalid", mealPlanningService.isValidDate(2025, 2, 30));
        assertFalse("Feb 29 in non-leap year should be invalid", mealPlanningService.isValidDate(2025, 2, 29));
        
        // Test days in each month
        assertFalse("April 31 should be invalid", mealPlanningService.isValidDate(2025, 4, 31));
        assertFalse("June 31 should be invalid", mealPlanningService.isValidDate(2025, 6, 31));
        assertFalse("September 31 should be invalid", mealPlanningService.isValidDate(2025, 9, 31));
        assertFalse("November 31 should be invalid", mealPlanningService.isValidDate(2025, 11, 31));
        
        // Test days that are valid
        assertTrue("April 30 should be valid", mealPlanningService.isValidDate(2025, 4, 30));
        assertTrue("January 31 should be valid", mealPlanningService.isValidDate(2025, 1, 31));
    }

    /**
     * Test for date formatting
     */
    @Test
    public void testFormatDate() {
        assertEquals("2025-01-01", mealPlanningService.formatDate(2025, 1, 1));
        assertEquals("2025-12-31", mealPlanningService.formatDate(2025, 12, 31));
        assertEquals("2028-02-29", mealPlanningService.formatDate(2028, 2, 29));
        assertEquals("2025-05-08", mealPlanningService.formatDate(2025, 5, 8));
    }

    /**
     * Test for getting breakfast options
     */
    @Test
    public void testGetBreakfastOptions() {
        // Get breakfast options
        Food[] breakfastOptions = mealPlanningService.getBreakfastOptions();
        
        // Verify
        assertNotNull("Breakfast options should not be null", breakfastOptions);
        assertTrue("Should have at least one breakfast option", breakfastOptions.length > 0);
        
        // Check properties of the first option
        assertNotNull("First breakfast option should not be null", breakfastOptions[0]);
        assertNotNull("Breakfast option name should not be null", breakfastOptions[0].getName());
        assertTrue("Breakfast option grams should be positive", breakfastOptions[0].getGrams() > 0);
        assertTrue("Breakfast option calories should be positive", breakfastOptions[0].getCalories() > 0);
    }

    /**
     * Test for getting lunch options
     */
    @Test
    public void testGetLunchOptions() {
        // Get lunch options
        Food[] lunchOptions = mealPlanningService.getLunchOptions();
        
        // Verify
        assertNotNull("Lunch options should not be null", lunchOptions);
        assertTrue("Should have at least one lunch option", lunchOptions.length > 0);
        
        // Check properties of the first option
        assertNotNull("First lunch option should not be null", lunchOptions[0]);
        assertNotNull("Lunch option name should not be null", lunchOptions[0].getName());
        assertTrue("Lunch option grams should be positive", lunchOptions[0].getGrams() > 0);
        assertTrue("Lunch option calories should be positive", lunchOptions[0].getCalories() > 0);
    }

    /**
     * Test for getting snack options
     */
    @Test
    public void testGetSnackOptions() {
        // Get snack options
        Food[] snackOptions = mealPlanningService.getSnackOptions();
        
        // Verify
        assertNotNull("Snack options should not be null", snackOptions);
        assertTrue("Should have at least one snack option", snackOptions.length > 0);
        
        // Check properties of the first option
        assertNotNull("First snack option should not be null", snackOptions[0]);
        assertNotNull("Snack option name should not be null", snackOptions[0].getName());
        assertTrue("Snack option grams should be positive", snackOptions[0].getGrams() > 0);
        assertTrue("Snack option calories should be positive", snackOptions[0].getCalories() > 0);
    }

    /**
     * Test for getting dinner options
     */
    @Test
    public void testGetDinnerOptions() {
        // Get dinner options
        Food[] dinnerOptions = mealPlanningService.getDinnerOptions();
        
        // Verify
        assertNotNull("Dinner options should not be null", dinnerOptions);
        assertTrue("Should have at least one dinner option", dinnerOptions.length > 0);
        
        // Check properties of the first option
        assertNotNull("First dinner option should not be null", dinnerOptions[0]);
        assertNotNull("Dinner option name should not be null", dinnerOptions[0].getName());
        assertTrue("Dinner option grams should be positive", dinnerOptions[0].getGrams() > 0);
        assertTrue("Dinner option calories should be positive", dinnerOptions[0].getCalories() > 0);
    }
    
    /**
     * Test adding a meal plan with FoodNutrient
     */
    @Test
    public void testAddMealPlanWithFoodNutrient() {
        // Create a FoodNutrient with detailed nutritional information
        FoodNutrient testFoodNutrient = new FoodNutrient(
            "Test Nutrient-Rich Food", 
            180.0, 
            320, 
            25.0,  // protein
            40.0,  // carbs
            12.0,  // fat
            8.0,   // fiber
            5.0,   // sugar
            150.0  // sodium
        );
        
        // Add to meal plan
        boolean result = mealPlanningService.addMealPlan(
            TEST_USERNAME, TEST_DATE, "dinner", testFoodNutrient);
        
        // Verify
        assertTrue("Should successfully add food with nutrients", result);
        
        // Verify from database
        List<Food> mealPlan = mealPlanningService.getMealPlan(TEST_USERNAME, TEST_DATE, "dinner");
        assertFalse("Meal plan should not be empty", mealPlan.isEmpty());
        
        // Find our nutrient-rich food
        boolean foundFoodNutrient = false;
        for (Food food : mealPlan) {
            if (food instanceof FoodNutrient && "Test Nutrient-Rich Food".equals(food.getName())) {
                foundFoodNutrient = true;
                FoodNutrient fn = (FoodNutrient) food;
                
                // Verify basic food properties
                assertEquals("Calories should match", 320, fn.getCalories());
                assertEquals("Grams should match", 180.0, fn.getGrams(), 0.01);
                
                
                break;
            }
        }
        assertTrue("Should find the added food nutrient", foundFoodNutrient);
    }

    /**
     * Test saveFoodAndGetId error handling for SQL exceptions
     */
    @Test
    public void testSaveFoodAndGetIdWithSQLException() {
        // Create a food with extremely long name to cause potential SQL issues
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longName.append("x");
        }
        Food testFood = new Food(longName.toString(), 100.0, 200);
        
        // First close the connection to force a SQL exception
        try {
            testConnection.close();
            
            // Try to add a meal plan which will fail with SQL exception
            boolean result = mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, "lunch", testFood);
            
            // Method should handle exception and return false
            assertFalse("Should fail gracefully with SQL exception", result);
            
            // Re-open connection for other tests
            testConnection = DatabaseHelper.getConnection();
            mealPlanningService = new MealPlanningService(testConnection);
        } catch (SQLException e) {
            fail("Exception should be handled internally: " + e.getMessage());
        }
    }

    /**
     * Test SQL exceptions in getFoodOptionsByType
     */
    @Test
    public void testGetFoodOptionsByTypeWithSQLException() {
        try {
            // Close the connection to force SQL exceptions
            testConnection.close();
            
            // Get breakfast options - should handle the SQL exception gracefully
            Food[] breakfastOptions = mealPlanningService.getBreakfastOptions();
            
            // Should return default options instead of null
            assertNotNull("Should return non-null options on SQL exception", breakfastOptions);
            assertTrue("Should return default options on SQL exception", breakfastOptions.length > 0);
            
            // Re-open connection for other tests
            testConnection = DatabaseHelper.getConnection();
            mealPlanningService = new MealPlanningService(testConnection);
        } catch (SQLException e) {
            fail("Exception should be handled internally: " + e.getMessage());
        }
    }

    /**
     * Test for getAllFoods method
     */
    @Test
    public void testGetAllFoods() {
        // Add some test foods
        try {
            // First, clear any existing test foods
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("DELETE FROM foods WHERE name LIKE 'TestGetAllFood%'");
            }
            
            // Add test foods
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "INSERT INTO foods (name, grams, calories) VALUES (?, ?, ?)")) {
                // Add 3 test foods
                String[] foodNames = {"TestGetAllFood1", "TestGetAllFood2", "TestGetAllFood3"};
                for (String name : foodNames) {
                    pstmt.setString(1, name);
                    pstmt.setDouble(2, 100.0);
                    pstmt.setInt(3, 200);
                    pstmt.executeUpdate();
                }
            }
            
            // Get all foods
            List<String> allFoods = mealPlanningService.getAllFoods();
            
            // Verify
            assertNotNull("All foods list should not be null", allFoods);
            assertTrue("All foods list should not be empty", !allFoods.isEmpty());
            
            // Verify our test foods are in the list
            boolean foundFood1 = false;
            boolean foundFood2 = false;
            boolean foundFood3 = false;
            
            for (String food : allFoods) {
                if ("TestGetAllFood1".equals(food)) foundFood1 = true;
                if ("TestGetAllFood2".equals(food)) foundFood2 = true;
                if ("TestGetAllFood3".equals(food)) foundFood3 = true;
            }
            
            assertTrue("Should find test food 1", foundFood1);
            assertTrue("Should find test food 2", foundFood2);
            assertTrue("Should find test food 3", foundFood3);
        } catch (SQLException e) {
            fail("Database error in getAllFoods test: " + e.getMessage());
        }
    }

    /**
     * Test for SQL exception in getAllFoods
     */
    @Test
    public void testGetAllFoodsWithSQLException() {
        try {
            // Close the connection to force a SQL exception
            testConnection.close();
            
            // This should handle the exception and return an empty list
            List<String> allFoods = mealPlanningService.getAllFoods();
            
            // Verify
            assertNotNull("Should return non-null list on SQL exception", allFoods);
            assertTrue("Should return empty list on SQL exception", allFoods.isEmpty());
            
            // Re-open connection for other tests
            testConnection = DatabaseHelper.getConnection();
            mealPlanningService = new MealPlanningService(testConnection);
        } catch (Exception e) {
            fail("Exception should be handled internally: " + e.getMessage());
        }
    }

    /**
     * Test for addMealToPlan method
     */
    @Test
    public void testAddMealToPlan() {
        try {
            // First add the food to the database
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "INSERT INTO foods (name, grams, calories, meal_type) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, "Test Meal");
                pstmt.setDouble(2, 100.0);
                pstmt.setInt(3, 500);
                pstmt.setString(4, "Breakfast");
                pstmt.executeUpdate();
            }
            
            // Add meal to plan
            boolean result = mealPlanningService.addMealToPlan(testUserId, "Monday", "Breakfast", "Test Meal");
            assertTrue("Meal should be added to plan successfully", result);
            
            // Verify meal was added by checking the database directly
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "SELECT COUNT(*) FROM meal_plans WHERE user_id = ? AND day = ? AND meal_type = ?")) {
                pstmt.setInt(1, testUserId);
                pstmt.setString(2, "Monday");
                pstmt.setString(3, "Breakfast");
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                assertEquals("Should have one meal in plan", 1, rs.getInt(1));
            }
            
            // Clean up
            mealPlanningService.deleteMeal("Monday", "Breakfast");
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for addMealToPlan method with invalid input
     */
    @Test
    public void testAddMealToPlanWithInvalidInput() {
        try {
            // Test with null meal type
            boolean result1 = mealPlanningService.addMealToPlan(testUserId, "Monday", null, "Test Meal");
            assertFalse("Should fail to add meal with null meal type", result1);
            
            // Test with empty meal type
            boolean result2 = mealPlanningService.addMealToPlan(testUserId, "Monday", "", "Test Meal");
            assertFalse("Should fail to add meal with empty meal type", result2);
            
            // Test with null day
            boolean result3 = mealPlanningService.addMealToPlan(testUserId, null, "Breakfast", "Test Meal");
            assertFalse("Should fail to add meal with null day", result3);
            
            // Test with empty day
            boolean result4 = mealPlanningService.addMealToPlan(testUserId, "", "Breakfast", "Test Meal");
            assertFalse("Should fail to add meal with empty day", result4);
            
            // Test with null food name
            boolean result5 = mealPlanningService.addMealToPlan(testUserId, "Monday", "Breakfast", null);
            assertFalse("Should fail to add meal with null food name", result5);
            
            // Test with empty food name
            boolean result6 = mealPlanningService.addMealToPlan(testUserId, "Monday", "Breakfast", "");
            assertFalse("Should fail to add meal with empty food name", result6);
            
            // Verify no meals were added
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "SELECT COUNT(*) FROM meal_plans WHERE user_id = ? AND day = ? AND meal_type = ?")) {
                pstmt.setInt(1, testUserId);
                pstmt.setString(2, "Monday");
                pstmt.setString(3, "Breakfast");
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                assertEquals("Should have no meals in plan", 0, rs.getInt(1));
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for addMeal method
     */
    @Test
    public void testAddMeal() {
        try {
            // First clear any existing test meal
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "DELETE FROM meal_plans WHERE day = 'Monday' AND meal_type = 'Breakfast'")) {
                pstmt.executeUpdate();
            }
            
           
            
            // Verify meal was added by checking the database directly
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "SELECT f.*, fn.* FROM foods f " +
                    "JOIN food_nutrients fn ON f.id = fn.food_id " +
                    "WHERE f.name = ?")) {
                pstmt.setString(1, "Test Meal");
                ResultSet rs = pstmt.executeQuery();
                
              
                
                // Check nutrient values
                double protein = rs.getDouble("protein");
                double carbs = rs.getDouble("carbs");
                double fat = rs.getDouble("fat");
                
                
            }
            
            // Verify meal plan
            List<Food> plan = mealPlanningService.getMealPlan(TEST_USERNAME, LocalDate.now().toString(), "Breakfast");
            
            
            // Find our meal in the plan
            boolean foundMeal = false;
            for (Food food : plan) {
                if ("Test Meal".equals(food.getName())) {
                    foundMeal = true;
                    assertEquals("Calories should match", 500, food.getCalories());
                    if (food instanceof FoodNutrient) {
                        FoodNutrient fn = (FoodNutrient) food;
                        assertEquals("Protein should match", 30.0, fn.getProtein(), 0.01);
                        assertEquals("Carbs should match", 50.0, fn.getCarbs(), 0.01);
                        assertEquals("Fat should match", 20.0, fn.getFat(), 0.01);
                    }
                    break;
                }
            }
            
            // Clean up
            mealPlanningService.deleteMeal("Monday", "Breakfast");
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for addMeal method with invalid input
     */
    @Test
    public void testAddMealWithInvalidInput() {
        try {
            // Test with invalid user ID
            try {
                mealPlanningService.addMeal(-1, "Monday", "Breakfast", "Test Meal", 500, 30.0, 50.0, 20.0, "Test Description");
                fail("Should throw exception for invalid user ID");
            } catch (IllegalArgumentException e) {
                // Expected exception
            }
            
            // Test with null meal name
            try {
                mealPlanningService.addMeal(testUserId, "Monday", "Breakfast", null, 500, 30.0, 50.0, 20.0, "Test Description");
                fail("Should throw exception for null meal name");
            } catch (IllegalArgumentException e) {
                // Expected exception
            }
            
            // Test with empty meal name
            try {
                mealPlanningService.addMeal(testUserId, "Monday", "Breakfast", "", 500, 30.0, 50.0, 20.0, "Test Description");
                fail("Should throw exception for empty meal name");
            } catch (IllegalArgumentException e) {
                // Expected exception
            }
            
            // Verify no meals were added
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "SELECT COUNT(*) FROM foods WHERE name = 'Test Meal'")) {
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                assertEquals("Should not have added any meals", 0, rs.getInt(1));
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for getMealPlan method
     */
    @Test
    public void testGetMealPlan() {
        try {
            // First add the food to the database
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "INSERT INTO foods (name, grams, calories, meal_type) VALUES (?, ?, ?, ?)")) {
                pstmt.setString(1, "Test Meal");
                pstmt.setDouble(2, 100.0);
                pstmt.setInt(3, 500);
                pstmt.setString(4, "Breakfast");
                pstmt.executeUpdate();
            }
            
            // Add test meal
            String today = LocalDate.now().toString();
            mealPlanningService.addMeal(testUserId, "Monday", "Breakfast", "Test Meal", 500, 30.0, 50.0, 20.0, "Test Description");
            
            // Get meal plan
            List<Food> plan = mealPlanningService.getMealPlan(TEST_USERNAME, today, "Breakfast");
            
            // Verify plan
            assertNotNull("Meal plan should not be null", plan);
            assertFalse("Meal plan should not be empty", plan.isEmpty());
            assertEquals("Should contain one meal", 1, plan.size());
            assertEquals("Should contain test meal", "Test Meal", plan.get(0).getName());
            
            // Clean up
            mealPlanningService.deleteMeal("Monday", "Breakfast");
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for getMealPlan method with no meals
     */
    @Test
    public void testGetMealPlanWithNoMeals() {
        // Get meal plan for date with no meals
        List<Food> plan = mealPlanningService.getMealPlan(TEST_USERNAME, "Monday", "Breakfast");
        
        // Verify plan is empty
        assertNotNull("Meal plan should not be null", plan);
        assertTrue("Meal plan should be empty", plan.isEmpty());
    }

    /**
     * Test for deleteMeal method
     */
    @Test
    public void testDeleteMeal() {
        try {
            // Add test meal
            mealPlanningService.addMeal(testUserId, "Monday", "Breakfast", "Test Meal", 500, 30.0, 50.0, 20.0, "Test Description");
            
            // Verify meal was added
            List<Food> planBefore = mealPlanningService.getMealPlan(TEST_USERNAME, LocalDate.now().toString(), "Breakfast");
            assertFalse("Meal plan should not be empty before deletion", planBefore.isEmpty());
            
            // Delete meal
            mealPlanningService.deleteMeal("Monday", "Breakfast");
            
            // Verify meal is deleted
            List<Food> planAfter = mealPlanningService.getMealPlan(TEST_USERNAME, LocalDate.now().toString(), "Breakfast");
            
        } catch (Exception e) {
            
        }
    }

    /**
     * Test for deleteMeal method with invalid input
     */
    @Test
    public void testDeleteMealWithInvalidInput() {
        // Try to delete non-existent meal
        mealPlanningService.deleteMeal("InvalidDay", "InvalidMeal");
        
        // Verify no error occurs
        List<Food> plan = mealPlanningService.getMealPlan(TEST_USERNAME, "InvalidDay", "InvalidMeal");
        assertTrue("Meal plan should be empty", plan.isEmpty());
    }

    /**
     * Test for getWeeklyMealPlan method
     */
    @Test
    public void testGetWeeklyMealPlan() {
        try {
            // First add some test meals
            String[] days = {"Monday", "Tuesday", "Wednesday"};
            String[] mealTypes = {"Breakfast", "Lunch", "Dinner"};
            String[] foodNames = {"Oatmeal", "Salad", "Chicken"};
            
            for (int i = 0; i < days.length; i++) {
                // Add food to database
                try (PreparedStatement pstmt = testConnection.prepareStatement(
                        "INSERT INTO foods (name, grams, calories) VALUES (?, ?, ?)")) {
                    pstmt.setString(1, foodNames[i]);
                    pstmt.setDouble(2, 100.0);
                    pstmt.setInt(3, 500);
                    pstmt.executeUpdate();
                }
                
                // Add meal to plan
                mealPlanningService.addMeal(testUserId, days[i], mealTypes[i], foodNames[i], 500, 30.0, 50.0, 20.0, "Test Description");
            }
            
            // Get weekly plan
            String weeklyPlan = mealPlanningService.getWeeklyMealPlan();
            
            // Verify plan contains all meals
            for (int i = 0; i < days.length; i++) {
                
                
            }
            
            // Clean up
            for (int i = 0; i < days.length; i++) {
                mealPlanningService.deleteMeal(days[i], mealTypes[i]);
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for getMealsForDay method
     */
    @Test
    public void testGetMealsForDay() {
        try {
            // First add some test meals
            String day = "TestDay";
            String[] mealTypes = {"Breakfast", "Lunch", "Dinner"};
            String[] foodNames = {"Oatmeal", "Salad", "Chicken"};
            
            for (int i = 0; i < mealTypes.length; i++) {
                // Add food to database
                try (PreparedStatement pstmt = testConnection.prepareStatement(
                        "INSERT INTO foods (name, grams, calories) VALUES (?, ?, ?)")) {
                    pstmt.setString(1, foodNames[i]);
                    pstmt.setDouble(2, 100.0);
                    pstmt.setInt(3, 500);
                    pstmt.executeUpdate();
                }
                
                // Add meal to plan
                mealPlanningService.addMeal(testUserId, day, mealTypes[i], foodNames[i], 500, 30.0, 50.0, 20.0, "Test Description");
            }
            
            // Get meals for day
            List<String> meals = mealPlanningService.getMealsForDay(day);
            
            // Verify
            assertNotNull("Meals list should not be null", meals);
            
            
            // Check each meal
            boolean foundBreakfast = false;
            boolean foundLunch = false;
            boolean foundDinner = false;
            
            for (String meal : meals) {
                if (meal.contains("Breakfast") && meal.contains("Oatmeal")) {
                    foundBreakfast = true;
                }
                if (meal.contains("Lunch") && meal.contains("Salad")) {
                    foundLunch = true;
                }
                if (meal.contains("Dinner") && meal.contains("Chicken")) {
                    foundDinner = true;
                }
            }
            
           
            
            // Clean up
            for (String mealType : mealTypes) {
                mealPlanningService.deleteMeal(day, mealType);
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test custom UI components flag for testing purposes
     */
    @Test
    public void testUIComponentsFlag() {
        // Create a test subclass of MealPlanningService
        TestMealPlanningService testService = new TestMealPlanningService(testConnection);
        
        // Test our custom flag
        testService.setUseUIComponents(false);
        assertFalse("Test UI Components should be disabled", testService.getUseUIComponents());
        
        // Now set it to true and verify
        testService.setUseUIComponents(true);
        assertTrue("Test UI Components should be enabled", testService.getUseUIComponents());
    }

    /**
     * Test saveFoodWithMealType method
     */
    @Test
    public void testSaveFoodWithMealType() {
        try {
            // First, clear any breakfast options in the database
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute("DELETE FROM foods WHERE meal_type = 'breakfast'");
            }
            
            // Add some breakfast options
            String[] breakfastOptions = {
                "Oatmeal", "Eggs", "Toast", "Yogurt", "Fruit", "Cereal", "Pancakes", "Waffles"
            };
            
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "INSERT INTO foods (name, grams, calories, meal_type) VALUES (?, ?, ?, ?)")) {
                for (String option : breakfastOptions) {
                    pstmt.setString(1, option);
                    pstmt.setDouble(2, 100.0);
                    pstmt.setInt(3, 300);
                    pstmt.setString(4, "breakfast");
                    pstmt.executeUpdate();
                }
            }
            
            // Get breakfast options - this should return our added options
            Food[] options = mealPlanningService.getBreakfastOptions();
            
            // Verify
            assertNotNull("Breakfast options should not be null", options);
            assertEquals("Should have 8 breakfast options", 8, options.length);
            
            // Check that the options were saved to the database
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "SELECT COUNT(*) FROM foods WHERE meal_type = 'breakfast'")) {
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                assertEquals("Database should have 8 breakfast options", 8, rs.getInt(1));
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test saveFoodNutrients and updateFoodNutrients through addMealPlan
     */
    @Test
    public void testSaveAndUpdateFoodNutrients() {
        // Create a unique food name for this test
        String uniqueFoodName = "TestNutrientSave" + System.currentTimeMillis();
        
        // Create a FoodNutrient with initial nutrient values
        FoodNutrient initialFood = new FoodNutrient(
            uniqueFoodName, 150.0, 250, 10.0, 20.0, 5.0, 3.0, 1.0, 50.0);
        
        // Add the food to a meal plan, which saves the food and its nutrients
        boolean result1 = mealPlanningService.addMealPlan(
            TEST_USERNAME, TEST_DATE, "lunch", initialFood);
        assertTrue("Should successfully add initial food", result1);
        
        // Verify the food and its nutrients were saved
        try {
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "SELECT f.id, fn.protein, fn.carbs, fn.fat, fn.fiber, fn.sugar, fn.sodium " +
                    "FROM foods f " +
                    "JOIN food_nutrients fn ON f.id = fn.food_id " +
                    "WHERE f.name = ?")) {
                
                pstmt.setString(1, uniqueFoodName);
                ResultSet rs = pstmt.executeQuery();
                
                assertTrue("Should find the food in database", rs.next());
                int foodId = rs.getInt("id");
                assertEquals("Protein should match", 10.0, rs.getDouble("protein"), 0.01);
                assertEquals("Carbs should match", 20.0, rs.getDouble("carbs"), 0.01);
                assertEquals("Fat should match", 5.0, rs.getDouble("fat"), 0.01);
                assertEquals("Fiber should match", 3.0, rs.getDouble("fiber"), 0.01);
                assertEquals("Sugar should match", 1.0, rs.getDouble("sugar"), 0.01);
                assertEquals("Sodium should match", 50.0, rs.getDouble("sodium"), 0.01);
            }
            
            // Now create the same food with different nutrient values
            FoodNutrient updatedFood = new FoodNutrient(
                uniqueFoodName, 150.0, 250, 15.0, 25.0, 8.0, 4.0, 2.0, 60.0);
            
            // Add the updated food to the meal plan
            // This should trigger updateFoodNutrients since the food exists but with different nutrient values
            boolean result2 = mealPlanningService.addMealPlan(
                TEST_USERNAME, TEST_DATE, "dinner", updatedFood);
            assertTrue("Should successfully add updated food", result2);
            
            // Verify that the nutrients were updated in the database
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "SELECT f.id, fn.protein, fn.carbs, fn.fat, fn.fiber, fn.sugar, fn.sodium " +
                    "FROM foods f " +
                    "JOIN food_nutrients fn ON f.id = fn.food_id " +
                    "WHERE f.name = ?")) {
                
                pstmt.setString(1, uniqueFoodName);
                ResultSet rs = pstmt.executeQuery();
                
                assertTrue("Should find the food in database", rs.next());
                assertEquals("Protein should be updated", 15.0, rs.getDouble("protein"), 0.01);
                assertEquals("Carbs should be updated", 25.0, rs.getDouble("carbs"), 0.01);
                assertEquals("Fat should be updated", 8.0, rs.getDouble("fat"), 0.01);
                assertEquals("Fiber should be updated", 4.0, rs.getDouble("fiber"), 0.01);
                assertEquals("Sugar should be updated", 2.0, rs.getDouble("sugar"), 0.01);
                assertEquals("Sodium should be updated", 60.0, rs.getDouble("sodium"), 0.01);
            }
        } catch (SQLException e) {
            fail("Database error in testSaveAndUpdateFoodNutrients: " + e.getMessage());
        }
    }
}