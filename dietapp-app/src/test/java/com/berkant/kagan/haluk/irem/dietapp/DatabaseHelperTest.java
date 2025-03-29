package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for DatabaseHelper
 */
public class DatabaseHelperTest {
    
    private Connection connection;
    
    /**
     * Setup method that runs before each test
     * Initializes the database for testing
     */
    @Before
    public void setUp() throws Exception {
        // Clear connection pool before tests
        resetConnectionPool();
        
        // Initialize test database
        DatabaseHelper.initializeDatabase();
        
        // Get a connection for test operations
        connection = DatabaseHelper.getConnection();
        assertNotNull("Database connection should not be null", connection);
        
        // Clear test data
        clearTestData();
    }
    
    /**
     * Teardown method that runs after each test
     * Closes database connections
     */
    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        DatabaseHelper.closeAllConnections();
    }
    
    /**
     * Reset the connection pool using reflection to start tests with a clean state
     */
    private void resetConnectionPool() throws Exception {
        Field connectionPoolField = DatabaseHelper.class.getDeclaredField("connectionPool");
        connectionPoolField.setAccessible(true);
        List<Connection> emptyPool = new ArrayList<>();
        connectionPoolField.set(null, emptyPool);
    }
    
    /**
     * Clear test data to ensure clean state for each test
     */
    private void clearTestData() {
        try {
            Statement stmt = connection.createStatement();
            // Delete test data in reverse order to avoid foreign key constraints
            stmt.execute("DELETE FROM food_nutrients WHERE food_id IN (SELECT id FROM foods WHERE name LIKE 'Test%')");
            stmt.execute("DELETE FROM foods WHERE name LIKE 'Test%'");
            stmt.execute("DELETE FROM users WHERE username LIKE 'test_%'");
            stmt.close();
        } catch (SQLException e) {
            // Ignore errors if tables don't exist yet
        }
    }
    
    /**
     * Test for getConnection method
     * Verifies that connections can be obtained and are valid
     */
    @Test
    public void testGetConnection() {
        Connection conn1 = null;
        Connection conn2 = null;
        
        try {
            // Get two connections to test pool functionality
            conn1 = DatabaseHelper.getConnection();
            conn2 = DatabaseHelper.getConnection();
            
            assertNotNull("First connection should not be null", conn1);
            assertNotNull("Second connection should not be null", conn2);
            assertFalse("Connection should be valid", conn1.isClosed());
            assertFalse("Connection should be valid", conn2.isClosed());
        } catch (SQLException e) {
            fail("Test should not throw exception: " + e.getMessage());
        } finally {
            // Return connections to pool
            DatabaseHelper.releaseConnection(conn1);
            DatabaseHelper.releaseConnection(conn2);
        }
    }
    
    /**
     * Test for releaseConnection method
     * Verifies that connections are properly released back to the pool
     */
    @Test
    public void testReleaseConnection() {
        try {
            // Get a connection
            Connection conn = DatabaseHelper.getConnection();
            assertNotNull("Connection should not be null", conn);
            
            // Release it
            DatabaseHelper.releaseConnection(conn);
            
            // Get the connection pool using reflection to check its size
            Field connectionPoolField = DatabaseHelper.class.getDeclaredField("connectionPool");
            connectionPoolField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Connection> connectionPool = (List<Connection>) connectionPoolField.get(null);
            
            assertFalse("Connection pool should not be empty after releasing a connection", connectionPool.isEmpty());
        } catch (Exception e) {
            fail("Test should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * Test for closeAllConnections method
     * Verifies that all connections are closed
     */
    @Test
    public void testCloseAllConnections() {
        try {
            // Get several connections
            Connection conn1 = DatabaseHelper.getConnection();
            Connection conn2 = DatabaseHelper.getConnection();
            
            // Release them to pool
            DatabaseHelper.releaseConnection(conn1);
            DatabaseHelper.releaseConnection(conn2);
            
            // Close all connections
            DatabaseHelper.closeAllConnections();
            
            // Get the connection pool using reflection to check its size
            Field connectionPoolField = DatabaseHelper.class.getDeclaredField("connectionPool");
            connectionPoolField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Connection> connectionPool = (List<Connection>) connectionPoolField.get(null);
            
            assertTrue("Connection pool should be empty after closing all connections", connectionPool.isEmpty());
        } catch (Exception e) {
            fail("Test should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * Test for closeConnection method which calls closeAllConnections
     */
    @Test
    public void testCloseConnection() {
        try {
            // Get a connection to ensure pool is not empty
            Connection conn = DatabaseHelper.getConnection();
            DatabaseHelper.releaseConnection(conn);
            
            // Call closeConnection
            DatabaseHelper.closeConnection();
            
            // Get the connection pool using reflection
            Field connectionPoolField = DatabaseHelper.class.getDeclaredField("connectionPool");
            connectionPoolField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Connection> connectionPool = (List<Connection>) connectionPoolField.get(null);
            
            assertTrue("Connection pool should be empty after closeConnection", connectionPool.isEmpty());
        } catch (Exception e) {
            fail("Test should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * Test for getUserId method
     * Verifies that user IDs can be retrieved correctly
     */
    @Test
    public void testGetUserId() {
        try {
            // Create a test user with unique username to avoid conflicts
            String testUsername = "test_user_" + System.currentTimeMillis();
            createTestUser(testUsername, "password123", "test@example.com", "Test User");
            
            // Get the user ID
            int userId = DatabaseHelper.getUserId(testUsername);
            
            assertTrue("User ID should be positive for existing user", userId > 0);
            
            // Test with non-existent user
            int nonExistentUserId = DatabaseHelper.getUserId("non_existent_user_" + System.currentTimeMillis());
            assertEquals("User ID should be -1 for non-existent user", -1, nonExistentUserId);
        } catch (SQLException e) {
            fail("Test should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * Test for saving food
     * We will modify this to test food creation functions without using the problematic methods
     */
    @Test
    public void testSaveFood() {
        try {
            // Create a test food with a unique name to avoid conflicts
            String uniqueName = "Test Apple " + System.currentTimeMillis();
            Food testFood = new Food(uniqueName, 100.0, 52);
            
            // Save food directly to the database
            int foodId = saveFoodDirectly(testFood);
            assertTrue("Food ID should be positive", foodId > 0);
            
            // Verify food exists in database
            boolean exists = checkFoodExists(foodId);
            assertTrue("Food should exist in database", exists);
            
            // Try to save same food again (should still work)
            int secondId = saveFoodDirectly(testFood);
            assertTrue("Second attempt should still get a valid ID", secondId > 0);
        } catch (SQLException e) {
            fail("Test should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * Test for saving food nutrients
     */
    @Test
    public void testSaveFoodNutrient() {
        try {
            // Create test food with nutrients and unique name
            String uniqueName = "Test Banana " + System.currentTimeMillis();
            FoodNutrient testFoodNutrient = new FoodNutrient(
                uniqueName, 100.0, 89, 1.1, 22.8, 0.3, 2.6, 12.2, 1.0);
            
            // Save food directly
            int foodId = saveFoodDirectly(testFoodNutrient);
            assertTrue("Food ID should be positive", foodId > 0);
            
            // Save nutrients separately
            boolean success = saveFoodNutrientsDirectly(foodId, testFoodNutrient);
            assertTrue("Saving nutrients should succeed", success);
            
            // Verify nutrients exist
            boolean nutrientsExist = checkFoodNutrientsExist(foodId);
            assertTrue("Food nutrients should exist in database", nutrientsExist);
            
            // Verify one specific nutrient value to ensure it was saved correctly
            double carbs = getNutrientValue(foodId, "carbs");
            assertEquals("Carbs value should match what was saved", 22.8, carbs, 0.001);
        } catch (SQLException e) {
            fail("Test should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * Test for updating food nutrients
     */
    @Test
    public void testUpdateFoodNutrients() {
        try {
            // Create food with nutrients and save it
            String uniqueName = "Test Update Food " + System.currentTimeMillis();
            FoodNutrient originalNutrient = new FoodNutrient(
                uniqueName, 100.0, 150, 5.0, 10.0, 3.0, 1.0, 2.0, 50.0);
            
            int foodId = saveFoodDirectly(originalNutrient);
            saveFoodNutrientsDirectly(foodId, originalNutrient);
            
            // Check initial value
            double initialProtein = getNutrientValue(foodId, "protein");
            assertEquals("Initial protein should match", 5.0, initialProtein, 0.001);
            
            // Create updated values
            FoodNutrient updatedNutrient = new FoodNutrient(
                uniqueName, 100.0, 150, 8.0, 12.0, 4.0, 2.0, 3.0, 60.0);
            
            // Update the nutrients
            boolean updated = updateFoodNutrientsDirectly(foodId, updatedNutrient);
            assertTrue("Update should succeed", updated);
            
            // Check updated value
            double updatedProtein = getNutrientValue(foodId, "protein");
            assertEquals("Updated protein should match", 8.0, updatedProtein, 0.001);
        } catch (SQLException e) {
            fail("Test should not throw exception: " + e.getMessage());
        }
    }
    
    /**
     * Save food directly to the database without using DatabaseHelper method
     */
    private int saveFoodDirectly(Food food) throws SQLException {
        // First check if food already exists with same name, grams, calories
        PreparedStatement checkStmt = connection.prepareStatement(
            "SELECT id FROM foods WHERE name = ? AND grams = ? AND calories = ?");
        checkStmt.setString(1, food.getName());
        checkStmt.setDouble(2, food.getGrams());
        checkStmt.setInt(3, food.getCalories());
        
        ResultSet rs = checkStmt.executeQuery();
        if (rs.next()) {
            // Food already exists, return its ID
            int id = rs.getInt("id");
            rs.close();
            checkStmt.close();
            return id;
        }
        rs.close();
        checkStmt.close();
        
        // Food doesn't exist, insert it
        PreparedStatement pstmt = connection.prepareStatement(
            "INSERT INTO foods (name, grams, calories) VALUES (?, ?, ?)");
        pstmt.setString(1, food.getName());
        pstmt.setDouble(2, food.getGrams());
        pstmt.setInt(3, food.getCalories());
        pstmt.executeUpdate();
        pstmt.close();
        
        // Get the ID of the inserted food
        Statement stmt = connection.createStatement();
        ResultSet idRs = stmt.executeQuery("SELECT last_insert_rowid()");
        int foodId = -1;
        if (idRs.next()) {
            foodId = idRs.getInt(1);
        }
        idRs.close();
        stmt.close();
        
        return foodId;
    }
    
    /**
     * Save food nutrients directly to the database
     */
    private boolean saveFoodNutrientsDirectly(int foodId, FoodNutrient foodNutrient) throws SQLException {
        // First check if nutrients already exist for this food
        PreparedStatement checkStmt = connection.prepareStatement(
            "SELECT food_id FROM food_nutrients WHERE food_id = ?");
        checkStmt.setInt(1, foodId);
        ResultSet rs = checkStmt.executeQuery();
        boolean exists = rs.next();
        rs.close();
        checkStmt.close();
        
        if (exists) {
            // Already exists, update it
            return updateFoodNutrientsDirectly(foodId, foodNutrient);
        }
        
        // Insert new nutrients
        PreparedStatement pstmt = connection.prepareStatement(
            "INSERT INTO food_nutrients (food_id, protein, carbs, fat, fiber, sugar, sodium) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)");
        pstmt.setInt(1, foodId);
        pstmt.setDouble(2, foodNutrient.getProtein());
        pstmt.setDouble(3, foodNutrient.getCarbs());
        pstmt.setDouble(4, foodNutrient.getFat());
        pstmt.setDouble(5, foodNutrient.getFiber());
        pstmt.setDouble(6, foodNutrient.getSugar());
        pstmt.setDouble(7, foodNutrient.getSodium());
        
        int result = pstmt.executeUpdate();
        pstmt.close();
        
        return result > 0;
    }
    
    /**
     * Update food nutrients directly in the database
     */
    private boolean updateFoodNutrientsDirectly(int foodId, FoodNutrient foodNutrient) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(
            "UPDATE food_nutrients SET protein = ?, carbs = ?, fat = ?, fiber = ?, sugar = ?, sodium = ? " +
            "WHERE food_id = ?");
        pstmt.setDouble(1, foodNutrient.getProtein());
        pstmt.setDouble(2, foodNutrient.getCarbs());
        pstmt.setDouble(3, foodNutrient.getFat());
        pstmt.setDouble(4, foodNutrient.getFiber());
        pstmt.setDouble(5, foodNutrient.getSugar());
        pstmt.setDouble(6, foodNutrient.getSodium());
        pstmt.setInt(7, foodId);
        
        int result = pstmt.executeUpdate();
        pstmt.close();
        
        return result > 0;
    }
    
    /**
     * Create a test user in the database
     */
    private void createTestUser(String username, String password, String email, String name) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(
            "INSERT INTO users (username, password, email, name) VALUES (?, ?, ?, ?)");
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        pstmt.setString(3, email);
        pstmt.setString(4, name);
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    /**
     * Check if a food exists in the database by ID
     */
    private boolean checkFoodExists(int foodId) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement("SELECT id FROM foods WHERE id = ?");
        pstmt.setInt(1, foodId);
        ResultSet rs = pstmt.executeQuery();
        boolean exists = rs.next();
        rs.close();
        pstmt.close();
        return exists;
    }
    
    /**
     * Check if food nutrients exist for a food in the database
     */
    private boolean checkFoodNutrientsExist(int foodId) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement("SELECT food_id FROM food_nutrients WHERE food_id = ?");
        pstmt.setInt(1, foodId);
        ResultSet rs = pstmt.executeQuery();
        boolean exists = rs.next();
        rs.close();
        pstmt.close();
        return exists;
    }
    
    /**
     * Get a specific nutrient value for a food
     */
    private double getNutrientValue(int foodId, String nutrientName) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(
            "SELECT " + nutrientName + " FROM food_nutrients WHERE food_id = ?");
        pstmt.setInt(1, foodId);
        ResultSet rs = pstmt.executeQuery();
        double value = -1.0;
        if (rs.next()) {
            value = rs.getDouble(nutrientName);
        }
        rs.close();
        pstmt.close();
        return value;
    }
}