package com.berkant.kagan.haluk.irem.dietapp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class DataHelperTest {
    private static final String TEST_DB_URL = "jdbc:sqlite:test_dietplanner.db";

    @Before
    public void setUp() throws Exception {
        // Load SQLite JDBC driver
        Class.forName("org.sqlite.JDBC");
        
        // Reset connection pool and initialize database
        DatabaseHelper.closeAllConnections();
        DatabaseHelper.initializeDatabase();
        clearTestData();
    }

    @After
    public void tearDown() throws Exception {
        // Close all connections and delete test database
        DatabaseHelper.closeAllConnections();
        deleteTestDatabase();
        clearTestData();
    }

    private void deleteTestDatabase() {
        try {
            java.io.File dbFile = new java.io.File("test_dietplanner.db");
            if (dbFile.exists()) {
                dbFile.delete();
            }
        } catch (Exception e) {
            // Ignore if deletion fails
        }
    }

    private void clearTestData() {
        try {
            Connection connection = DatabaseHelper.getConnection();
            PreparedStatement stmt1 = connection.prepareStatement("DELETE FROM food_nutrients");
            stmt1.executeUpdate();
            stmt1.close();
            PreparedStatement stmt2 = connection.prepareStatement("DELETE FROM foods");
            stmt2.executeUpdate();
            stmt2.close();
        } catch (SQLException e) {
            // ignore
        }
    }

    @Test
    public void testInitializeDatabase() {
        // Verify database initialization
        Connection conn = DatabaseHelper.getConnection();
        assertNotNull("Database connection should be established", conn);
        
        try {
            // Check if tables are created
            Statement stmt = conn.createStatement();
            
            // Test users table
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='users'");
            assertTrue("Users table should exist", rs.next());
            
            // Test foods table
            rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='foods'");
            assertTrue("Foods table should exist", rs.next());
            
            // Test food_nutrients table
            rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='food_nutrients'");
            assertTrue("Food_nutrients table should exist", rs.next());
        } catch (SQLException e) {
            fail("Database initialization failed: " + e.getMessage());
        } finally {
            DatabaseHelper.releaseConnection(conn);
        }
    }

    @Test
    public void testGetConnection() {
        Connection conn1 = DatabaseHelper.getConnection();
        assertNotNull("First connection should be established", conn1);
        DatabaseHelper.releaseConnection(conn1);

        Connection conn2 = DatabaseHelper.getConnection();
        assertNotNull("Second connection should be established", conn2);
        DatabaseHelper.releaseConnection(conn2);
    }

    @Test
    public void testReleaseConnection() {
        Connection conn = DatabaseHelper.getConnection();
        assertNotNull("Connection should be established", conn);
        
        DatabaseHelper.releaseConnection(conn);
        
        // Verify connection pool works after release
        Connection newConn = DatabaseHelper.getConnection();
        assertNotNull("Connection should be reusable after release", newConn);
        DatabaseHelper.releaseConnection(newConn);
    }

    @Test
    public void testGetUserId() {
        // Test getting user ID for sample admin user
        int adminId = DatabaseHelper.getUserId("admin");
        assertTrue("Admin user ID should be valid", adminId > 0);

        // Test non-existent user
        int invalidUserId = DatabaseHelper.getUserId("non_existent_user");
        assertEquals("Non-existent user should return -1", -1, invalidUserId);
    }

    @Test
    public void testSaveFoodAndGetId() throws SQLException {
        // Create a test food
        Food testFood = new Food("Apple", 100.0, 52);
        
        // Save food and get its ID
        int foodId = DatabaseHelper.saveFoodAndGetId(testFood);
        assertTrue("Food ID should be valid", foodId > 0);

        // Try saving the same food again (should return existing ID)
        int duplicateFoodId = DatabaseHelper.saveFoodAndGetId(testFood);
        assertEquals("Duplicate food should return same ID", foodId, duplicateFoodId);

        // Test with FoodNutrient
        FoodNutrient fnFood = new FoodNutrient("Banana", 118.0, 105, 1.3, 27.0, 0.3, 0.4, 1.0, 422.0);
        int fnFoodId = DatabaseHelper.saveFoodAndGetId(fnFood);
        assertTrue("FoodNutrient ID should be valid", fnFoodId > 0);
    }

    @Test
    public void testCloseConnections() {
        // Get some connections
        Connection conn1 = DatabaseHelper.getConnection();
        Connection conn2 = DatabaseHelper.getConnection();
        
        assertNotNull("First connection should be established", conn1);
        assertNotNull("Second connection should be established", conn2);
        
        // Close all connections
        DatabaseHelper.closeAllConnections();
        
        // Verify connections are closed
        Connection newConn = DatabaseHelper.getConnection();
        assertNotNull("New connection should be established after closing all", newConn);
        DatabaseHelper.releaseConnection(newConn);
    }

    @Test
    public void testSaveFoodNutrients() throws SQLException {
        // Create a test FoodNutrient
        FoodNutrient testFoodNutrient = new FoodNutrient(
            "Chicken Breast", 100.0, 165, 31.0, 0.0, 3.6, 0.0, 0.1, 74.0
        );

        // Save food and get its ID
        int foodId = DatabaseHelper.saveFoodAndGetId(testFoodNutrient);
        assertTrue("Food ID should be valid", foodId > 0);

        // Validate nutrient details were saved
        Connection conn = DatabaseHelper.getConnection();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT * FROM food_nutrients WHERE food_id = " + foodId
            );

            assertTrue("Nutrient record should exist", rs.next());
            assertEquals("Protein should match", 31.0, rs.getDouble("protein"), 0.01);
            assertEquals("Carbs should match", 0.0, rs.getDouble("carbs"), 0.01);
            assertEquals("Fat should match", 3.6, rs.getDouble("fat"), 0.01);
        } catch (SQLException e) {
            fail("Error checking saved nutrients: " + e.getMessage());
        } finally {
            DatabaseHelper.releaseConnection(conn);
        }
    }

    @Test
    public void testUpdateFoodNutrients() throws SQLException {
        // Create and save initial food
        FoodNutrient initialFood = new FoodNutrient(
            "Salmon", 100.0, 208, 22.0, 0.0, 0.0, 0.0, 0.0, 59.0
        );
        int foodId = DatabaseHelper.saveFoodAndGetId(initialFood);
        assertTrue("Initial food ID should be valid", foodId > 0);

        // Update with new nutrient values
        FoodNutrient updatedFood = new FoodNutrient(
            "Salmon", 100.0, 208, 25.0, 1.0, 4.0, 0.5, 0.2, 75.0
        );
        int updatedFoodId = DatabaseHelper.saveFoodAndGetId(updatedFood);
        assertEquals("Food ID should remain the same", foodId, updatedFoodId);

        // Validate updated nutrient details
        Connection conn = DatabaseHelper.getConnection();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT * FROM food_nutrients WHERE food_id = " + foodId
            );

            assertTrue("Updated nutrient record should exist", rs.next());
            assertEquals("Updated protein should match", 25.0, rs.getDouble("protein"), 0.01);
            assertEquals("Updated carbs should match", 1.0, rs.getDouble("carbs"), 0.01);
        } catch (SQLException e) {
            fail("Error checking updated nutrients: " + e.getMessage());
        } finally {
            DatabaseHelper.releaseConnection(conn);
        }
    }
}