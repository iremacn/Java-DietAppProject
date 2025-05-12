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
        try {
            // Add some test foods
            String[] testFoods = {"Test Food 1", "Test Food 2", "Test Food 3"};
            for (String foodName : testFoods) {
                try (PreparedStatement pstmt = testConnection.prepareStatement(
                        "INSERT INTO foods (name, grams, calories) VALUES (?, ?, ?)")) {
                    pstmt.setString(1, foodName);
                    pstmt.setDouble(2, 100.0);
                    pstmt.setInt(3, 500);
                    pstmt.executeUpdate();
                }
            }

            // Get all foods
            List<String> foods = mealPlanningService.getAllFoods();

            // Verify
            assertNotNull("Foods list should not be null", foods);
            assertTrue("Should contain test foods", foods.size() >= testFoods.length);

            // Check if all test foods are in the list
            for (String testFood : testFoods) {
                assertTrue("Should contain " + testFood, foods.contains(testFood));
            }

            // Clean up
            for (String foodName : testFoods) {
                try (PreparedStatement pstmt = testConnection.prepareStatement(
                        "DELETE FROM foods WHERE name = ?")) {
                    pstmt.setString(1, foodName);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for getTotalCalories method (düzeltilmiş)
     */
    @Test
    public void testGetTotalCalories() {
        try {
            // Test için önce temizlik
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "DELETE FROM food_logs WHERE user_id = ? AND date = ?")) {
                pstmt.setInt(1, testUserId);
                pstmt.setString(2, TEST_DATE);
                pstmt.executeUpdate();
            }
            // Test foods ekle
            Food food1 = new Food("Test Food 1", 100.0, 300);
            Food food2 = new Food("Test Food 2", 150.0, 450);
            mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food1);
            mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food2);

            // Toplam kalori
            int totalCalories = mealPlanningService.getTotalCalories(TEST_USERNAME, TEST_DATE);
            assertEquals("Total calories should match sum of food calories", 750, totalCalories);

            // Var olmayan kullanıcı
            int caloriesForNonExistentUser = mealPlanningService.getTotalCalories("NonExistentUser", TEST_DATE);
            assertEquals("Should return 0 for non-existent user", 0, caloriesForNonExistentUser);

            // Var olmayan tarih (temizlenmiş olmalı)
            int caloriesForNonExistentDate = mealPlanningService.getTotalCalories(TEST_USERNAME, "2099-12-31");
            assertEquals("Should return 0 for non-existent date", 0, caloriesForNonExistentDate);

            // Temizlik
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "DELETE FROM food_logs WHERE user_id = ? AND date = ?")) {
                pstmt.setInt(1, testUserId);
                pstmt.setString(2, TEST_DATE);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for logFood method
     */
    @Test
    public void testLogFood() {
        try {
            // Test with valid food
            Food testFood = new Food("Test Log Food", 200.0, 400);
            boolean result = mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, testFood);
            assertTrue("Should successfully log food", result);
            
            // Verify food was logged
            List<Food> foodLog = mealPlanningService.getFoodLog(TEST_USERNAME, TEST_DATE);
            assertFalse("Food log should not be empty", foodLog.isEmpty());
            
            boolean foundFood = false;
            for (Food food : foodLog) {
                if ("Test Log Food".equals(food.getName())) {
                    foundFood = true;
                    assertEquals("Food calories should match", 400, food.getCalories());
                    assertEquals("Food grams should match", 200.0, food.getGrams(), 0.01);
                    break;
                }
            }
            assertTrue("Should find the logged food", foundFood);
            
            // Test with null parameters
            assertFalse("Should not log food with null username", 
                mealPlanningService.logFood(null, TEST_DATE, testFood));
            assertFalse("Should not log food with null date", 
                mealPlanningService.logFood(TEST_USERNAME, null, testFood));
            assertFalse("Should not log food with null food", 
                mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, null));
            
            // Clean up
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "DELETE FROM food_logs WHERE user_id = ? AND date = ?")) {
                pstmt.setInt(1, testUserId);
                pstmt.setString(2, TEST_DATE);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for getFoodLog method
     */
    @Test
    public void testGetFoodLog() {
        try {
            // Add test foods to log
            Food food1 = new Food("Test Log Food 1", 100.0, 300);
            Food food2 = new Food("Test Log Food 2", 150.0, 450);
            
            mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food1);
            mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food2);
            
            // Get food log
            List<Food> foodLog = mealPlanningService.getFoodLog(TEST_USERNAME, TEST_DATE);
            
            // Verify
            assertNotNull("Food log should not be null", foodLog);
            assertEquals("Should have two foods in log", 2, foodLog.size());
            
            // Check if both foods are in the log
            boolean foundFood1 = false;
            boolean foundFood2 = false;
            
            for (Food food : foodLog) {
                if ("Test Log Food 1".equals(food.getName())) {
                    foundFood1 = true;
                    assertEquals("Food 1 calories should match", 300, food.getCalories());
                    assertEquals("Food 1 grams should match", 100.0, food.getGrams(), 0.01);
                }
                if ("Test Log Food 2".equals(food.getName())) {
                    foundFood2 = true;
                    assertEquals("Food 2 calories should match", 450, food.getCalories());
                    assertEquals("Food 2 grams should match", 150.0, food.getGrams(), 0.01);
                }
            }
            
            assertTrue("Should find first logged food", foundFood1);
            assertTrue("Should find second logged food", foundFood2);
            
            // Test with null parameters
            List<Food> nullUsernameLog = mealPlanningService.getFoodLog(null, TEST_DATE);
            assertNotNull("Should return empty list with null username", nullUsernameLog);
            assertTrue("List should be empty with null username", nullUsernameLog.isEmpty());
            
            List<Food> nullDateLog = mealPlanningService.getFoodLog(TEST_USERNAME, null);
            assertNotNull("Should return empty list with null date", nullDateLog);
            assertTrue("List should be empty with null date", nullDateLog.isEmpty());
            
            // Clean up
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "DELETE FROM food_logs WHERE user_id = ? AND date = ?")) {
                pstmt.setInt(1, testUserId);
                pstmt.setString(2, TEST_DATE);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for addMealToPlan method
     */
    @Test
    public void testAddMealToPlan() {
        try {
            // Test with valid data
            boolean result = mealPlanningService.addMealToPlan(testUserId, "Monday", "Breakfast", "Test Breakfast");
            assertTrue("Should successfully add meal to plan", result);

            // Verify meal was added
            String weeklyPlan = mealPlanningService.getWeeklyMealPlan();
            assertTrue("Weekly plan should contain added meal", weeklyPlan.contains("Test Breakfast"));

            // Test with invalid user ID
            try {
                mealPlanningService.addMealToPlan(-1, "Monday", "Breakfast", "Test Food");
                fail("Should throw IllegalArgumentException for invalid user ID");
            } catch (IllegalArgumentException e) {
                // Expected exception
            }

            // Test with null parameters
            assertFalse("Should not add meal with null day", 
                mealPlanningService.addMealToPlan(testUserId, null, "Breakfast", "Test Food"));
            assertFalse("Should not add meal with null meal type", 
                mealPlanningService.addMealToPlan(testUserId, "Monday", null, "Test Food"));
            assertFalse("Should not add meal with null food name", 
                mealPlanningService.addMealToPlan(testUserId, "Monday", "Breakfast", null));

            // Clean up
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "DELETE FROM meal_plans WHERE user_id = ?")) {
                pstmt.setInt(1, testUserId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for addMeal method (düzeltilmiş)
     */
    @Test
    public void testAddMeal() {
        try {
            // Test with valid data (TEST_DATE kullanılacak şekilde ekleme yapılacak)
            // addMealToPlan ile aynı işlevi test edeceğiz çünkü addMeal LocalDate.now() kullanıyor
            boolean result = mealPlanningService.addMealToPlan(testUserId, "Monday", "Lunch", "Test Lunch");
            assertTrue("Should successfully add meal to plan", result);

            // Haftalık planı kontrol et
            String weeklyPlan = mealPlanningService.getWeeklyMealPlan();
            assertTrue("Weekly plan should contain added meal", weeklyPlan.contains("Test Lunch"));

            // Hatalı userId
            try {
                mealPlanningService.addMeal(-1, "Monday", "Lunch", "Test Food", 500, 30.0, 40.0, 20.0, "Test ingredients");
                fail("Should throw IllegalArgumentException for invalid user ID");
            } catch (IllegalArgumentException e) {
                // Beklenen
            }

            // Null parametreler
            try {
                mealPlanningService.addMeal(testUserId, null, "Lunch", "Test Food", 500, 30.0, 40.0, 20.0, "Test ingredients");
                fail("Should throw IllegalArgumentException for null day");
            } catch (IllegalArgumentException e) {
                // Beklenen
            }

            // Temizlik
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "DELETE FROM meal_plans WHERE user_id = ?")) {
                pstmt.setInt(1, testUserId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for deleteMeal method
     */
    @Test
    public void testDeleteMeal() {
        try {
            // First add a meal
            mealPlanningService.addMeal(testUserId, "Monday", "Dinner", "Test Dinner", 600, 40.0, 50.0, 25.0, "Test ingredients");

            // Delete the meal
            mealPlanningService.deleteMeal("Monday", "Dinner");

            // Verify meal was deleted
            String weeklyPlan = mealPlanningService.getWeeklyMealPlan();
            assertFalse("Weekly plan should not contain deleted meal", weeklyPlan.contains("Test Dinner"));

            // Test with non-existent meal
            mealPlanningService.deleteMeal("Tuesday", "Breakfast"); // Should not throw exception

            // Clean up
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "DELETE FROM meal_plans WHERE user_id = ?")) {
                pstmt.setInt(1, testUserId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for getWeeklyMealPlan method (düzeltilmiş)
     */
    @Test
    public void testGetWeeklyMealPlan() {
        try {
            // addMealToPlan ile farklı günler için yemek ekle
            mealPlanningService.addMealToPlan(testUserId, "Monday", "Breakfast", "Monday Breakfast");
            mealPlanningService.addMealToPlan(testUserId, "Wednesday", "Lunch", "Wednesday Lunch");
            mealPlanningService.addMealToPlan(testUserId, "Friday", "Dinner", "Friday Dinner");

            // Haftalık planı al
            String weeklyPlan = mealPlanningService.getWeeklyMealPlan();

            // Doğrulama
            assertNotNull("Weekly plan should not be null", weeklyPlan);
            assertTrue("Should contain Monday's breakfast", weeklyPlan.contains("Monday Breakfast"));
            assertTrue("Should contain Wednesday's lunch", weeklyPlan.contains("Wednesday Lunch"));
            assertTrue("Should contain Friday's dinner", weeklyPlan.contains("Friday Dinner"));

            // Temizlik
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "DELETE FROM meal_plans WHERE user_id = ?")) {
                pstmt.setInt(1, testUserId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test for getMealsForDay method (düzeltilmiş)
     */
    @Test
    public void testGetMealsForDay() {
        try {
            // addMealToPlan ile TEST_DATE ve Monday için yemek ekle
            mealPlanningService.addMealToPlan(testUserId, "Monday", "Breakfast", "Monday Breakfast");
            mealPlanningService.addMealToPlan(testUserId, "Monday", "Lunch", "Monday Lunch");
            mealPlanningService.addMealToPlan(testUserId, "Monday", "Dinner", "Monday Dinner");

            // Monday için yemekleri al
            List<String> mondayMeals = mealPlanningService.getMealsForDay("Monday");

            // Doğrulama
            assertNotNull("Meals list should not be null", mondayMeals);
            assertEquals("Should have three meals for Monday", 3, mondayMeals.size());
            assertTrue("Should contain breakfast", mondayMeals.stream().anyMatch(meal -> meal.contains("Monday Breakfast")));
            assertTrue("Should contain lunch", mondayMeals.stream().anyMatch(meal -> meal.contains("Monday Lunch")));
            assertTrue("Should contain dinner", mondayMeals.stream().anyMatch(meal -> meal.contains("Monday Dinner")));

            // Var olmayan gün
            List<String> nonExistentDayMeals = mealPlanningService.getMealsForDay("NonExistentDay");
            assertNotNull("Should return empty list for non-existent day", nonExistentDayMeals);
            assertTrue("List should be empty for non-existent day", nonExistentDayMeals.isEmpty());

            // Null gün
            List<String> nullDayMeals = mealPlanningService.getMealsForDay(null);
            assertNotNull("Should return empty list for null day", nullDayMeals);
            assertTrue("List should be empty for null day", nullDayMeals.isEmpty());

            // Temizlik
            try (PreparedStatement pstmt = testConnection.prepareStatement(
                    "DELETE FROM meal_plans WHERE user_id = ?")) {
                pstmt.setInt(1, testUserId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            fail("Test failed with SQLException: " + e.getMessage());
        }
    }

    /**
     * Test addMealPlan: success, nulls, non-existent user, SQL exception
     */
    @Test
    public void testAddMealPlan_AllBranches() throws Exception {
        // Success
        Food food = new Food("BranchTestFood", 100, 200);
        assertTrue(mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, "breakfast", food));
        // Null parameters
        assertFalse(mealPlanningService.addMealPlan(null, TEST_DATE, "breakfast", food));
        assertFalse(mealPlanningService.addMealPlan(TEST_USERNAME, null, "breakfast", food));
        assertFalse(mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, null, food));
        assertFalse(mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, "breakfast", null));
        // Non-existent user
        assertFalse(mealPlanningService.addMealPlan("no_such_user", TEST_DATE, "breakfast", food));
        // SQL Exception
        testConnection.close();
        assertFalse(mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, "breakfast", food));
        // Reopen for next tests
        testConnection = DatabaseHelper.getConnection();
        mealPlanningService = new MealPlanningService(testConnection);
    }

    /**
     * Test logFood: success, nulls, non-existent user, SQL exception
     */
    @Test
    public void testLogFood_AllBranches() throws Exception {
        Food food = new Food("BranchLogFood", 100, 200);
        assertTrue(mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food));
        assertFalse(mealPlanningService.logFood(null, TEST_DATE, food));
        assertFalse(mealPlanningService.logFood(TEST_USERNAME, null, food));
        assertFalse(mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, null));
        assertFalse(mealPlanningService.logFood("no_such_user", TEST_DATE, food));
        testConnection.close();
        assertFalse(mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food));
        testConnection = DatabaseHelper.getConnection();
        mealPlanningService = new MealPlanningService(testConnection);
    }

    /**
     * Test getMealPlan: success, nulls, non-existent user, SQL exception, empty
     */
    @Test
    public void testGetMealPlan_AllBranches() throws Exception {
        Food food = new Food("BranchGetMealPlanFood", 100, 200);
        mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, "lunch", food);
        List<Food> result = mealPlanningService.getMealPlan(TEST_USERNAME, TEST_DATE, "lunch");
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(f -> f.getName().equals("BranchGetMealPlanFood")));
        // Nulls
        assertTrue(mealPlanningService.getMealPlan(null, TEST_DATE, "lunch").isEmpty());
        assertTrue(mealPlanningService.getMealPlan(TEST_USERNAME, null, "lunch").isEmpty());
        assertTrue(mealPlanningService.getMealPlan(TEST_USERNAME, TEST_DATE, null).isEmpty());
        // Non-existent user
        assertTrue(mealPlanningService.getMealPlan("no_such_user", TEST_DATE, "lunch").isEmpty());
        // SQL Exception
        testConnection.close();
        assertTrue(mealPlanningService.getMealPlan(TEST_USERNAME, TEST_DATE, "lunch").isEmpty());
        testConnection = DatabaseHelper.getConnection();
        mealPlanningService = new MealPlanningService(testConnection);
    }

    /**
     * Test getFoodLog: success, nulls, non-existent user, SQL exception, empty
     */
    @Test
    public void testGetFoodLog_AllBranches() throws Exception {
        Food food = new Food("BranchGetFoodLogFood", 100, 200);
        mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food);
        List<Food> result = mealPlanningService.getFoodLog(TEST_USERNAME, TEST_DATE);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(f -> f.getName().equals("BranchGetFoodLogFood")));
        // Nulls
        assertTrue(mealPlanningService.getFoodLog(null, TEST_DATE).isEmpty());
        assertTrue(mealPlanningService.getFoodLog(TEST_USERNAME, null).isEmpty());
        // Non-existent user
        assertTrue(mealPlanningService.getFoodLog("no_such_user", TEST_DATE).isEmpty());
        // SQL Exception
        testConnection.close();
        assertTrue(mealPlanningService.getFoodLog(TEST_USERNAME, TEST_DATE).isEmpty());
        testConnection = DatabaseHelper.getConnection();
        mealPlanningService = new MealPlanningService(testConnection);
    }

    /**
     * Test getTotalCalories: success, nulls, non-existent user, SQL exception, empty
     */
    @Test
    public void testGetTotalCalories_AllBranches() throws Exception {
        Food food = new Food("BranchCaloriesFood", 100, 123);
        mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food);
        assertEquals(123, mealPlanningService.getTotalCalories(TEST_USERNAME, TEST_DATE));
        assertEquals(0, mealPlanningService.getTotalCalories(null, TEST_DATE));
        assertEquals(0, mealPlanningService.getTotalCalories(TEST_USERNAME, null));
        assertEquals(0, mealPlanningService.getTotalCalories("no_such_user", TEST_DATE));
        testConnection.close();
        assertEquals(0, mealPlanningService.getTotalCalories(TEST_USERNAME, TEST_DATE));
        testConnection = DatabaseHelper.getConnection();
        mealPlanningService = new MealPlanningService(testConnection);
    }

    /**
     * Test isValidDate: all branches
     */
    @Test
    public void testIsValidDate_AllBranches() {
        assertTrue(mealPlanningService.isValidDate(2025, 1, 1)); // valid
        assertFalse(mealPlanningService.isValidDate(2024, 1, 1)); // year < 2025
        assertFalse(mealPlanningService.isValidDate(2101, 1, 1)); // year > 2100
        assertFalse(mealPlanningService.isValidDate(2025, 0, 1)); // month < 1
        assertFalse(mealPlanningService.isValidDate(2025, 13, 1)); // month > 12
        assertFalse(mealPlanningService.isValidDate(2025, 1, 0)); // day < 1
        assertFalse(mealPlanningService.isValidDate(2025, 1, 32)); // day > 31
        assertFalse(mealPlanningService.isValidDate(2025, 2, 30)); // Feb 30
        assertTrue(mealPlanningService.isValidDate(2028, 2, 29)); // Leap year
        assertFalse(mealPlanningService.isValidDate(2025, 2, 29)); // Not leap year
        assertFalse(mealPlanningService.isValidDate(2025, 4, 31)); // April 31
        assertTrue(mealPlanningService.isValidDate(2025, 4, 30)); // April 30
    }

    /**
     * Test formatDate: all branches
     */
    @Test
    public void testFormatDate_AllBranches() {
        assertEquals("2025-01-01", mealPlanningService.formatDate(2025, 1, 1));
        assertEquals("2025-12-31", mealPlanningService.formatDate(2025, 12, 31));
        assertEquals("2028-02-29", mealPlanningService.formatDate(2028, 2, 29));
        assertEquals("2025-05-08", mealPlanningService.formatDate(2025, 5, 8));
    }

    /**
     * Test getBreakfastOptions, getLunchOptions, getSnackOptions, getDinnerOptions: DB and default
     */
    @Test
    public void testMealOptions_AllBranches() throws Exception {
        // Remove all foods for meal type to force default
        try (PreparedStatement pstmt = testConnection.prepareStatement("DELETE FROM foods WHERE meal_type = ?")) {
            pstmt.setString(1, "breakfast"); pstmt.executeUpdate();
            pstmt.setString(1, "lunch"); pstmt.executeUpdate();
            pstmt.setString(1, "snack"); pstmt.executeUpdate();
            pstmt.setString(1, "dinner"); pstmt.executeUpdate();
        }
        assertTrue(mealPlanningService.getBreakfastOptions().length > 0);
        assertTrue(mealPlanningService.getLunchOptions().length > 0);
        assertTrue(mealPlanningService.getSnackOptions().length > 0);
        assertTrue(mealPlanningService.getDinnerOptions().length > 0);
        // Add a food for each type and test DB branch
        try (PreparedStatement pstmt = testConnection.prepareStatement("INSERT INTO foods (name, grams, calories, meal_type) VALUES (?, ?, ?, ?)")) {
            pstmt.setString(1, "DBBreakfast"); pstmt.setDouble(2, 100); pstmt.setInt(3, 100); pstmt.setString(4, "breakfast"); pstmt.executeUpdate();
            pstmt.setString(1, "DBLunch"); pstmt.setDouble(2, 100); pstmt.setInt(3, 100); pstmt.setString(4, "lunch"); pstmt.executeUpdate();
            pstmt.setString(1, "DBSnack"); pstmt.setDouble(2, 100); pstmt.setInt(3, 100); pstmt.setString(4, "snack"); pstmt.executeUpdate();
            pstmt.setString(1, "DBDinner"); pstmt.setDouble(2, 100); pstmt.setInt(3, 100); pstmt.setString(4, "dinner"); pstmt.executeUpdate();
        }
        assertTrue(arrayContains(mealPlanningService.getBreakfastOptions(), "DBBreakfast"));
        assertTrue(arrayContains(mealPlanningService.getLunchOptions(), "DBLunch"));
        assertTrue(arrayContains(mealPlanningService.getSnackOptions(), "DBSnack"));
        assertTrue(arrayContains(mealPlanningService.getDinnerOptions(), "DBDinner"));
    }
    private boolean arrayContains(Food[] arr, String name) {
        for (Food f : arr) if (f.getName().equals(name)) return true;
        return false;
    }

    /**
     * Test addMealToPlan: all branches
     */
    @Test
    public void testAddMealToPlan_AllBranches() throws Exception {
        assertTrue(mealPlanningService.addMealToPlan(testUserId, "Monday", "Breakfast", "PlanFood"));
        assertFalse(mealPlanningService.addMealToPlan(testUserId, null, "Breakfast", "PlanFood"));
        assertFalse(mealPlanningService.addMealToPlan(testUserId, "Monday", null, "PlanFood"));
        assertFalse(mealPlanningService.addMealToPlan(testUserId, "Monday", "Breakfast", null));
        try { mealPlanningService.addMealToPlan(-1, "Monday", "Breakfast", "PlanFood"); fail(); } catch (IllegalArgumentException e) {}
        testConnection.close();
        assertFalse(mealPlanningService.addMealToPlan(testUserId, "Monday", "Breakfast", "PlanFood"));
        testConnection = DatabaseHelper.getConnection();
        mealPlanningService = new MealPlanningService(testConnection);
    }

    /**
     * Test addMeal: all branches (invalid params, exception)
     */
    @Test
    public void testAddMeal_AllBranches() throws Exception {
        // Only invalid param/exception branches, as date is not controllable
        try { mealPlanningService.addMeal(-1, "Monday", "Lunch", "Food", 100, 1, 1, 1, ""); fail(); } catch (IllegalArgumentException e) {}
        try { mealPlanningService.addMeal(testUserId, null, "Lunch", "Food", 100, 1, 1, 1, ""); fail(); } catch (IllegalArgumentException e) {}
        try { mealPlanningService.addMeal(testUserId, "Monday", null, "Food", 100, 1, 1, 1, ""); fail(); } catch (IllegalArgumentException e) {}
        try { mealPlanningService.addMeal(testUserId, "Monday", "Lunch", null, 100, 1, 1, 1, ""); fail(); } catch (IllegalArgumentException e) {}
        testConnection.close();
        try { mealPlanningService.addMeal(testUserId, "Monday", "Lunch", "Food", 100, 1, 1, 1, ""); fail(); } catch (RuntimeException e) {}
        testConnection = DatabaseHelper.getConnection();
        mealPlanningService = new MealPlanningService(testConnection);
    }

    /**
     * Test deleteMeal: success, SQL exception
     */
    @Test
    public void testDeleteMeal_AllBranches() throws Exception {
        mealPlanningService.addMealToPlan(testUserId, "Tuesday", "Dinner", "DeleteMe");
        mealPlanningService.deleteMeal("Tuesday", "Dinner"); // should not throw
        testConnection.close();
        try { mealPlanningService.deleteMeal("Tuesday", "Dinner"); fail(); } catch (RuntimeException e) {}
        testConnection = DatabaseHelper.getConnection();
        mealPlanningService = new MealPlanningService(testConnection);
    }

    /**
     * Test getWeeklyMealPlan: empty, with data, SQL exception
     */
    @Test
    public void testGetWeeklyMealPlan_AllBranches() throws Exception {
        // Empty
        String plan = mealPlanningService.getWeeklyMealPlan();
        assertNotNull(plan);
        // With data
        mealPlanningService.addMealToPlan(testUserId, "Wednesday", "Lunch", "WeeklyFood");
        String plan2 = mealPlanningService.getWeeklyMealPlan();
        assertTrue(plan2.contains("WeeklyFood"));
        // SQL Exception
        testConnection.close();
        try { mealPlanningService.getWeeklyMealPlan(); fail(); } catch (RuntimeException e) {}
        testConnection = DatabaseHelper.getConnection();
        mealPlanningService = new MealPlanningService(testConnection);
    }

    /**
     * Test getMealsForDay: empty, with data, null, SQL exception
     */
    @Test
    public void testGetMealsForDay_AllBranches() throws Exception {
        // Empty
        List<String> meals = mealPlanningService.getMealsForDay("NoDay");
        assertNotNull(meals);
        assertTrue(meals.isEmpty());
        // With data
        mealPlanningService.addMealToPlan(testUserId, "Thursday", "Breakfast", "DayFood");
        List<String> meals2 = mealPlanningService.getMealsForDay("Thursday");
        assertFalse(meals2.isEmpty());
        assertTrue(meals2.stream().anyMatch(s -> s.contains("DayFood")));
        // Null
        assertTrue(mealPlanningService.getMealsForDay(null).isEmpty());
        // SQL Exception
        testConnection.close();
        assertTrue(mealPlanningService.getMealsForDay("Thursday").isEmpty());
        testConnection = DatabaseHelper.getConnection();
        mealPlanningService = new MealPlanningService(testConnection);
    }

    /**
     * Test getAllFoods: empty, with data, SQL exception
     */
    @Test
    public void testGetAllFoods_AllBranches() throws Exception {
        // Empty
        try (PreparedStatement pstmt = testConnection.prepareStatement("DELETE FROM foods")) { pstmt.executeUpdate(); }
        List<String> foods = mealPlanningService.getAllFoods();
        assertNotNull(foods);
        // With data
        try (PreparedStatement pstmt = testConnection.prepareStatement("INSERT INTO foods (name, grams, calories) VALUES (?, ?, ?);")) {
            pstmt.setString(1, "AllFoodsTest"); pstmt.setDouble(2, 100); pstmt.setInt(3, 100); pstmt.executeUpdate();
        }
        List<String> foods2 = mealPlanningService.getAllFoods();
        assertTrue(foods2.contains("AllFoodsTest"));
        // SQL Exception
        testConnection.close();
        assertTrue(mealPlanningService.getAllFoods().isEmpty());
        testConnection = DatabaseHelper.getConnection();
        mealPlanningService = new MealPlanningService(testConnection);
    }

    @Test
    public void testSaveFoodAndGetId_NullFood() throws Exception {
        int result = invokeSaveFoodAndGetId(null);
        assertEquals(-1, result);
    }

    @Test
    public void testSaveFoodAndGetId_ExistingFoodWithNutrients() throws Exception {
        FoodNutrient fn = new FoodNutrient("TestFood", 100, 200, 10, 20, 5, 2, 1, 0.5);
        int id1 = invokeSaveFoodAndGetId(fn);
        int id2 = invokeSaveFoodAndGetId(fn);
        assertTrue(id1 > 0 && id2 > 0);
        assertEquals(id1, id2);
    }

    @Test
    public void testUpdateFoodNutrients_NewInsert() throws Exception {
        // Insert a food
        FoodNutrient fn = new FoodNutrient("NutrientFood", 100, 200, 10, 20, 5, 2, 1, 0.5);
        int foodId = invokeSaveFoodAndGetId(fn);
        // Remove nutrients if exist
        try (PreparedStatement ps = testConnection.prepareStatement("DELETE FROM food_nutrients WHERE food_id = ?")) {
            ps.setInt(1, foodId);
            ps.executeUpdate();
        }
        // Now update nutrients (should insert)
        invokeUpdateFoodNutrients(foodId, fn);
        // Check if inserted
        try (PreparedStatement ps = testConnection.prepareStatement("SELECT * FROM food_nutrients WHERE food_id = ?")) {
            ps.setInt(1, foodId);
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
        }
    }

    @Test
    public void testGetUserId_NullAndEmpty() throws Exception {
        int nullResult = invokeGetUserId(null);
        int emptyResult = invokeGetUserId("");
        assertEquals(-1, nullResult);
        assertEquals(-1, emptyResult);
    }

    @Test
    public void testGetUserId_NonExistent() throws Exception {
        int result = invokeGetUserId("nonexistentuser123");
        assertEquals(-1, result);
    }

    @Test
    public void testSaveFoodWithMealType_NullFood() throws Exception {
        int result = invokeSaveFoodWithMealType(null, "breakfast");
        assertEquals(-1, result);
    }

    @Test
    public void testGetBreakfastOptions_EmptyTable() throws Exception {
        clearFoodsTable();
        Food[] options = mealPlanningService.getBreakfastOptions();
        assertNotNull(options);
    }

    @Test
    public void testGetFoodOptionsByType_InvalidType() throws Exception {
        List<Food> options = invokeGetFoodOptionsByType("invalidtype");
        assertNotNull(options);
        assertTrue(options.isEmpty());
    }

    @Test
    public void testSaveFoodWithMealType_NullMealType() throws Exception {
        Food food = new Food("NullMealTypeFood", 100, 100);
        int result = invokeSaveFoodWithMealType(food, null);
        assertEquals(-1, result);
    }

    @Test
    public void testSaveFoodWithMealType_SQLException() throws Exception {
        Food food = new Food("SQLExceptionFood", 100, 100);
        testConnection.close();
        try {
            int result = invokeSaveFoodWithMealType(food, "breakfast");
            assertEquals(-1, result);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof SQLException);
        } finally {
            testConnection = DatabaseHelper.getConnection();
            mealPlanningService = new MealPlanningService(testConnection);
        }
    }

    @Test
    public void testGetBreakfastOptions_SQLException() throws Exception {
        testConnection.close();
        try {
            Food[] options = mealPlanningService.getBreakfastOptions();
            assertNotNull(options);
            assertTrue(options.length > 0); // Should return default options
        } finally {
            testConnection = DatabaseHelper.getConnection();
            mealPlanningService = new MealPlanningService(testConnection);
        }
    }

    @Test
    public void testGetFoodOptionsByType_NullType() throws Exception {
        List<Food> options = invokeGetFoodOptionsByType(null);
        assertNotNull(options);
        assertTrue(options.isEmpty());
    }

    @Test
    public void testGetFoodOptionsByType_SQLException() throws Exception {
        testConnection.close();
        try {
            List<Food> options = invokeGetFoodOptionsByType("breakfast");
            assertNotNull(options);
            assertTrue(options.isEmpty());
        } finally {
            testConnection = DatabaseHelper.getConnection();
            mealPlanningService = new MealPlanningService(testConnection);
        }
    }

    @Test
    public void testAddMealToPlan_WhitespaceParams() {
        boolean result = mealPlanningService.addMealToPlan(testUserId, "   ", "breakfast", "Eggs");
        assertFalse(result);
        result = mealPlanningService.addMealToPlan(testUserId, "Monday", "   ", "Eggs");
        assertFalse(result);
        result = mealPlanningService.addMealToPlan(testUserId, "Monday", "breakfast", "   ");
        assertFalse(result);
    }

    @Test
    public void testAddMealToPlan_SQLException() throws Exception {
        testConnection.close();
        try {
            boolean result = mealPlanningService.addMealToPlan(testUserId, "Monday", "breakfast", "Eggs");
            assertFalse(result);
        } finally {
            testConnection = DatabaseHelper.getConnection();
            mealPlanningService = new MealPlanningService(testConnection);
        }
    }

    @Test
    public void testAddMeal_SQLException() throws Exception {
        testConnection.close();
        try {
            mealPlanningService.addMeal(testUserId, "Monday", "breakfast", "Eggs", 100, 10, 10, 5, "");
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Error adding meal to plan"));
        } finally {
            testConnection = DatabaseHelper.getConnection();
            mealPlanningService = new MealPlanningService(testConnection);
        }
    }

    @Test
    public void testSaveFoodNutrients_SQLException() throws Exception {
        // Simulate SQLException by passing a closed connection
        FoodNutrient fn = new FoodNutrient("SQLNutrient", 100, 100, 10, 10, 10, 2, 2, 2);
        testConnection.close();
        try {
            invokeSaveFoodNutrients(testConnection, 1, fn);
            fail("Should throw SQLException");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof SQLException);
        } finally {
            testConnection = DatabaseHelper.getConnection();
            mealPlanningService = new MealPlanningService(testConnection);
        }
    }

    @Test
    public void testUpdateFoodNutrients_UpdateAndInsertBranches() throws Exception {
        // Insert a food and nutrients
        FoodNutrient fn = new FoodNutrient("BranchNutrient", 100, 100, 10, 10, 10, 2, 2, 2);
        int foodId = invokeSaveFoodAndGetId(fn);
        // Update branch
        fn.setProtein(20);
        invokeUpdateFoodNutrients(foodId, fn);
        // Remove nutrients and test insert branch
        try (PreparedStatement ps = testConnection.prepareStatement("DELETE FROM food_nutrients WHERE food_id = ?")) {
            ps.setInt(1, foodId);
            ps.executeUpdate();
        }
        invokeUpdateFoodNutrients(foodId, fn);
        // Check if inserted
        try (PreparedStatement ps = testConnection.prepareStatement("SELECT * FROM food_nutrients WHERE food_id = ?")) {
            ps.setInt(1, foodId);
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
        }
    }

    @Test
    public void testFoodNutrient_InvalidValues() {
        FoodNutrient fn = new FoodNutrient("Invalid", 100, 100, 200, 200, 200, 50, 50, 50); // excessive macros
        assertFalse(fn.isValid());
        fn = new FoodNutrient("Negative", 100, 100, -10, -10, -10, -5, -5, -5);
        assertTrue(fn.getProtein() == 0 && fn.getCarbs() == 0 && fn.getFat() == 0 && fn.getFiber() == 0 && fn.getSugar() == 0 && fn.getSodium() == 0);
    }

    @Test
    public void testDuplicateFoodInsertion() throws Exception {
        Food food = new Food("DuplicateFood", 100, 100);
        int id1 = invokeSaveFoodAndGetId(food);
        int id2 = invokeSaveFoodAndGetId(food);
        assertEquals(id1, id2);
    }

    @Test
    public void testPartialNutrientInfo() throws Exception {
        FoodNutrient fn = new FoodNutrient("PartialNutrient", 100, 100);
        fn.setProtein(10);
        int id = invokeSaveFoodAndGetId(fn);
        assertTrue(id > 0);
    }

    // Helper for saveFoodNutrients via reflection
    private void invokeSaveFoodNutrients(Connection conn, int foodId, FoodNutrient fn) throws Exception {
        java.lang.reflect.Method m = MealPlanningService.class.getDeclaredMethod("saveFoodNutrients", Connection.class, int.class, FoodNutrient.class);
        m.setAccessible(true);
        m.invoke(mealPlanningService, conn, foodId, fn);
    }

    // --- Helper methods for reflection ---
    private int invokeSaveFoodAndGetId(Food food) throws Exception {
        java.lang.reflect.Method m = MealPlanningService.class.getDeclaredMethod("saveFoodAndGetId", Connection.class, Food.class);
        m.setAccessible(true);
        return (int) m.invoke(mealPlanningService, testConnection, food);
    }
    private void invokeUpdateFoodNutrients(int foodId, FoodNutrient fn) throws Exception {
        java.lang.reflect.Method m = MealPlanningService.class.getDeclaredMethod("updateFoodNutrients", Connection.class, int.class, FoodNutrient.class);
        m.setAccessible(true);
        m.invoke(mealPlanningService, testConnection, foodId, fn);
    }
    private int invokeGetUserId(String username) throws Exception {
        java.lang.reflect.Method m = MealPlanningService.class.getDeclaredMethod("getUserId", Connection.class, String.class);
        m.setAccessible(true);
        return (int) m.invoke(mealPlanningService, testConnection, username);
    }
    private int invokeSaveFoodWithMealType(Food food, String mealType) throws Exception {
        java.lang.reflect.Method m = MealPlanningService.class.getDeclaredMethod("saveFoodWithMealType", Connection.class, Food.class, String.class);
        m.setAccessible(true);
        return (int) m.invoke(mealPlanningService, testConnection, food, mealType);
    }
    private List<Food> invokeGetFoodOptionsByType(String mealType) throws Exception {
        java.lang.reflect.Method m = MealPlanningService.class.getDeclaredMethod("getFoodOptionsByType", String.class);
        m.setAccessible(true);
        return (List<Food>) m.invoke(mealPlanningService, mealType);
    }
    private void clearFoodsTable() throws Exception {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM foods");
        }
    }
}