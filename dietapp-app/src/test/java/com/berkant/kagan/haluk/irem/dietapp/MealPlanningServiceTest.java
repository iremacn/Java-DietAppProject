package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @class MealPlanningServiceTest
 * @brief Test class for the MealPlanningService class.
 */
public class MealPlanningServiceTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "testpassword";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_DATE = "2025-01-01";
    
    private MealPlanningService mealPlanningService;
    private static int testUserId;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Initialize database
        DatabaseHelper.initializeDatabase();
        
        // Create a test user in the database
        try (Connection conn = DatabaseHelper.getConnection()) {
            // Check if the test user already exists
            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT id FROM users WHERE username = ?")) {
                checkStmt.setString(1, TEST_USERNAME);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    // User exists, get the ID
                    testUserId = rs.getInt("id");
                } else {
                    // User doesn't exist, create a new one
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO users (username, password, email, name) VALUES (?, ?, ?, ?)",
                            PreparedStatement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setString(1, TEST_USERNAME);
                        insertStmt.setString(2, TEST_PASSWORD);
                        insertStmt.setString(3, TEST_EMAIL);
                        insertStmt.setString(4, TEST_NAME);
                        
                        insertStmt.executeUpdate();
                        
                        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            testUserId = generatedKeys.getInt(1);
                        } else {
                            fail("Failed to create test user");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // Clean up test data from the database
        try (Connection conn = DatabaseHelper.getConnection()) {
            // Delete meal plans for test user
            try (PreparedStatement deleteMealPlans = conn.prepareStatement(
                    "DELETE FROM meal_plans WHERE user_id = ?")) {
                deleteMealPlans.setInt(1, testUserId);
                deleteMealPlans.executeUpdate();
            }
            
            // Delete food logs for test user
            try (PreparedStatement deleteFoodLogs = conn.prepareStatement(
                    "DELETE FROM food_logs WHERE user_id = ?")) {
                deleteFoodLogs.setInt(1, testUserId);
                deleteFoodLogs.executeUpdate();
            }
            
            // Note: We don't delete the test user to maintain referential integrity
            // across multiple test runs
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Database cleanup failed: " + e.getMessage());
        }
        
        // Close all database connections
        DatabaseHelper.closeAllConnections();
    }

    @Before
    public void setUp() throws Exception {
        mealPlanningService = new MealPlanningService();
        
        // Clean up any data that might have been left from previous tests
        cleanupTestData();
    }

    @After
    public void tearDown() throws Exception {
        // Additional cleanup if needed
    }

    /**
     * Helper method to clean up test data between tests
     */
    private void cleanupTestData() {
        try (Connection conn = DatabaseHelper.getConnection()) {
            // Delete meal plans for test user for the test date
            try (PreparedStatement deleteMealPlans = conn.prepareStatement(
                    "DELETE FROM meal_plans WHERE user_id = ? AND date = ?")) {
                deleteMealPlans.setInt(1, testUserId);
                deleteMealPlans.setString(2, TEST_DATE);
                deleteMealPlans.executeUpdate();
            }
            
            // Delete food logs for test user for the test date
            try (PreparedStatement deleteFoodLogs = conn.prepareStatement(
                    "DELETE FROM food_logs WHERE user_id = ? AND date = ?")) {
                deleteFoodLogs.setInt(1, testUserId);
                deleteFoodLogs.setString(2, TEST_DATE);
                deleteFoodLogs.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Test data cleanup failed: " + e.getMessage());
        }
    }

    /**
     * Helper method to get the user ID for a username
     */
    private int getUserIdForTest(String username) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT id FROM users WHERE username = ?")) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Test
    public void testAddMealPlanWithValidData() {
        // Create a food item
        Food testFood = new Food("Test Breakfast", 200.0, 300);
        
        // Add meal plan
        boolean result = mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, "breakfast", testFood);
        
        // Verify
        assertTrue("Should successfully add meal plan", result);
        
        // Verify from database
        List<Food> mealPlan = mealPlanningService.getMealPlan(TEST_USERNAME, TEST_DATE, "breakfast");
        assertFalse("Meal plan should not be empty", mealPlan.isEmpty());
        assertEquals("Should have one food item", 1, mealPlan.size());
        assertEquals("Food name should match", "Test Breakfast", mealPlan.get(0).getName());
        assertEquals("Food calories should match", 300, mealPlan.get(0).getCalories());
    }

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

    @Test
    public void testAddMealPlanWithNonExistentUser() {
        boolean result = mealPlanningService.addMealPlan("nonexistentuser", TEST_DATE, "breakfast", 
                new Food("Test Food", 100.0, 200));
        
        assertFalse("Should fail with non-existent user", result);
    }

    @Test
    public void testLogFoodWithValidData() {
        // Create a food item
        Food testFood = new Food("Test Logged Food", 150.0, 250);
        
        // Log food
        boolean result = mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, testFood);
        
        // Verify
        assertTrue("Should successfully log food", result);
        
        // Verify from database
        List<Food> foodLog = mealPlanningService.getFoodLog(TEST_USERNAME, TEST_DATE);
        assertFalse("Food log should not be empty", foodLog.isEmpty());
        assertEquals("Should have one food item", 1, foodLog.size());
        assertEquals("Food name should match", "Test Logged Food", foodLog.get(0).getName());
        assertEquals("Food calories should match", 250, foodLog.get(0).getCalories());
    }

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

    @Test
    public void testGetMealPlanWithValidData() {
        // Add two meal plans
        Food breakfast1 = new Food("Breakfast Item 1", 150.0, 200);
        Food breakfast2 = new Food("Breakfast Item 2", 120.0, 180);
        
        mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, "breakfast", breakfast1);
        mealPlanningService.addMealPlan(TEST_USERNAME, TEST_DATE, "breakfast", breakfast2);
        
        // Get meal plan
        List<Food> mealPlan = mealPlanningService.getMealPlan(TEST_USERNAME, TEST_DATE, "breakfast");
        
        // Verify
        assertNotNull("Meal plan should not be null", mealPlan);
        assertEquals("Meal plan should have two items", 2, mealPlan.size());
        
        // Check if the meal plan contains both foods (order may vary)
        boolean foundItem1 = false;
        boolean foundItem2 = false;
        
        for (Food food : mealPlan) {
            if ("Breakfast Item 1".equals(food.getName())) foundItem1 = true;
            if ("Breakfast Item 2".equals(food.getName())) foundItem2 = true;
        }
        
        assertTrue("Meal plan should contain first item", foundItem1);
        assertTrue("Meal plan should contain second item", foundItem2);
    }

    @Test
    public void testGetMealPlanWithNonExistentData() {
        // Get meal plan for non-existent data
        List<Food> mealPlan = mealPlanningService.getMealPlan("nonexistentuser", TEST_DATE, "breakfast");
        
        // Verify
        assertNotNull("Meal plan should not be null even for non-existent data", mealPlan);
        assertTrue("Meal plan should be empty for non-existent data", mealPlan.isEmpty());
    }

    @Test
    public void testGetFoodLogWithValidData() {
        // Add two food log entries
        Food food1 = new Food("Logged Food 1", 150.0, 200);
        Food food2 = new Food("Logged Food 2", 120.0, 180);
        
        mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food1);
        mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food2);
        
        // Get food log
        List<Food> foodLog = mealPlanningService.getFoodLog(TEST_USERNAME, TEST_DATE);
        
        // Verify
        assertNotNull("Food log should not be null", foodLog);
        assertEquals("Food log should have two items", 2, foodLog.size());
        
        // Check if the food log contains both foods (order may vary)
        boolean foundItem1 = false;
        boolean foundItem2 = false;
        
        for (Food food : foodLog) {
            if ("Logged Food 1".equals(food.getName())) foundItem1 = true;
            if ("Logged Food 2".equals(food.getName())) foundItem2 = true;
        }
        
        assertTrue("Food log should contain first item", foundItem1);
        assertTrue("Food log should contain second item", foundItem2);
    }

    @Test
    public void testGetFoodLogWithNonExistentData() {
        // Get food log for non-existent data
        List<Food> foodLog = mealPlanningService.getFoodLog("nonexistentuser", TEST_DATE);
        
        // Verify
        assertNotNull("Food log should not be null even for non-existent data", foodLog);
        assertTrue("Food log should be empty for non-existent data", foodLog.isEmpty());
    }

    @Test
    public void testGetTotalCaloriesWithValidData() {
        // Add food log entries
        Food food1 = new Food("Calorie Food 1", 100.0, 200);
        Food food2 = new Food("Calorie Food 2", 100.0, 300);
        Food food3 = new Food("Calorie Food 3", 100.0, 100);
        
        mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food1);
        mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food2);
        mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, food3);
        
        // Get total calories
        int totalCalories = mealPlanningService.getTotalCalories(TEST_USERNAME, TEST_DATE);
        
        // Verify
        assertEquals("Total calories should be sum of all foods", 600, totalCalories);
    }

    @Test
    public void testGetTotalCaloriesWithNonExistentData() {
        // Get total calories for non-existent data
        int totalCalories = mealPlanningService.getTotalCalories("nonexistentuser", TEST_DATE);
        
        // Verify
        assertEquals("Total calories should be 0 for non-existent data", 0, totalCalories);
    }

    @Test
    public void testIsValidDateWithValidDates() {
        // Test various valid dates
        assertTrue("2025-01-01 should be valid", mealPlanningService.isValidDate(2025, 1, 1));
        assertTrue("2025-12-31 should be valid", mealPlanningService.isValidDate(2025, 12, 31));
        
        // Test leap year (2028 is a leap year)
        assertTrue("Feb 29 in leap year should be valid", mealPlanningService.isValidDate(2028, 2, 29));
    }

    @Test
    public void testIsValidDateWithInvalidDates() {
        // Test various invalid dates
        assertFalse("Year below 2025 should be invalid", mealPlanningService.isValidDate(2024, 1, 1));
        assertFalse("Year above 2100 should be invalid", mealPlanningService.isValidDate(2101, 1, 1));
        assertFalse("Month 0 should be invalid", mealPlanningService.isValidDate(2025, 0, 1));
        assertFalse("Month 13 should be invalid", mealPlanningService.isValidDate(2025, 13, 1));
        assertFalse("Day 0 should be invalid", mealPlanningService.isValidDate(2025, 1, 0));
        assertFalse("Day 32 should be invalid", mealPlanningService.isValidDate(2025, 1, 32));
        
        // Test specific edge cases
        assertFalse("Feb 30 should always be invalid", mealPlanningService.isValidDate(2025, 2, 30));
        assertFalse("Feb 29 in non-leap year should be invalid", mealPlanningService.isValidDate(2025, 2, 29));
        assertFalse("Apr 31 should be invalid", mealPlanningService.isValidDate(2025, 4, 31));
        assertFalse("Jun 31 should be invalid", mealPlanningService.isValidDate(2025, 6, 31));
        assertFalse("Sep 31 should be invalid", mealPlanningService.isValidDate(2025, 9, 31));
        assertFalse("Nov 31 should be invalid", mealPlanningService.isValidDate(2025, 11, 31));
    }

    @Test
    public void testFormatDate() {
        // Test date formatting
        assertEquals("2025-01-01", mealPlanningService.formatDate(2025, 1, 1));
        assertEquals("2025-12-31", mealPlanningService.formatDate(2025, 12, 31));
        
        // Test zero padding
        assertEquals("2025-01-05", mealPlanningService.formatDate(2025, 1, 5));
        assertEquals("2025-05-01", mealPlanningService.formatDate(2025, 5, 1));
    }

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

    @Test
    public void testLogFoodWithFoodNutrient() {
        // Create a FoodNutrient item (subclass of Food with nutrient information)
        FoodNutrient testFoodNutrient = new FoodNutrient(
            "Nutrient Test Food", 200.0, 300, 20.0, 30.0, 10.0, 5.0, 2.0, 100.0);
        
        // Log food
        boolean result = mealPlanningService.logFood(TEST_USERNAME, TEST_DATE, testFoodNutrient);
        
        // Verify
        assertTrue("Should successfully log food with nutrients", result);
        
        // Verify from database
        List<Food> foodLog = mealPlanningService.getFoodLog(TEST_USERNAME, TEST_DATE);
        assertFalse("Food log should not be empty", foodLog.isEmpty());
        
        // The returned food should be a FoodNutrient
        boolean foundNutrientFood = false;
        for (Food food : foodLog) {
            if (food instanceof FoodNutrient) {
                FoodNutrient fn = (FoodNutrient) food;
                if ("Nutrient Test Food".equals(fn.getName())) {
                    foundNutrientFood = true;
                    
                    // Verify nutrient values
                    assertEquals("Protein should match", 20.0, fn.getProtein(), 0.01);
                    assertEquals("Carbs should match", 30.0, fn.getCarbs(), 0.01);
                    assertEquals("Fat should match", 10.0, fn.getFat(), 0.01);
                    assertEquals("Fiber should match", 5.0, fn.getFiber(), 0.01);
                    assertEquals("Sugar should match", 2.0, fn.getSugar(), 0.01);
                    assertEquals("Sodium should match", 100.0, fn.getSodium(), 0.01);
                }
            }
        }
        
        assertTrue("Should find the logged food with nutrients", foundNutrientFood);
    }

    @Test
    public void testAddMealPlanWithFoodNutrient() {
        // Create a FoodNutrient item
        FoodNutrient testFoodNutrient = new FoodNutrient(
            "Meal Plan Nutrient Food", 200.0, 300, 25.0, 35.0, 15.0, 7.0, 3.0, 120.0);
        
        // Add meal plan
        boolean result = mealPlanningService.addMealPlan(
            TEST_USERNAME, TEST_DATE, "dinner", testFoodNutrient);
        
        // Verify
        assertTrue("Should successfully add meal plan with nutrients", result);
        
        // Verify from database
        List<Food> mealPlan = mealPlanningService.getMealPlan(TEST_USERNAME, TEST_DATE, "dinner");
        assertFalse("Meal plan should not be empty", mealPlan.isEmpty());
        
        // The returned food should be a FoodNutrient
        boolean foundNutrientFood = false;
        for (Food food : mealPlan) {
            if (food instanceof FoodNutrient) {
                FoodNutrient fn = (FoodNutrient) food;
                if ("Meal Plan Nutrient Food".equals(fn.getName())) {
                    foundNutrientFood = true;
                    
                    // Verify nutrient values
                    assertEquals("Protein should match", 25.0, fn.getProtein(), 0.01);
                    assertEquals("Carbs should match", 35.0, fn.getCarbs(), 0.01);
                    assertEquals("Fat should match", 15.0, fn.getFat(), 0.01);
                    assertEquals("Fiber should match", 7.0, fn.getFiber(), 0.01);
                    assertEquals("Sugar should match", 3.0, fn.getSugar(), 0.01);
                    assertEquals("Sodium should match", 120.0, fn.getSodium(), 0.01);
                }
            }
        }
        
        assertTrue("Should find the meal plan food with nutrients", foundNutrientFood);
    }
}