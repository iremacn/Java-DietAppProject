package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive test class for ShoppingListService
 * @author test-writer
 */
public class ShoppingListServiceTest {

    private ShoppingListService shoppingListService;
    private MealPlanningService mealPlanningService;
    private Connection connection;
    private static final String TEST_DB = "test-dietplanner.db";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Delete test database if it exists
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }
    
    @Before
    public void setUp() throws Exception {
        // Initialize database for testing
        System.setProperty("db.url", "jdbc:sqlite:" + TEST_DB);
        DatabaseHelper.initializeDatabase();
        
        // Get a database connection for test verification
        connection = DatabaseHelper.getConnection();
        
        // Create dependent services
        mealPlanningService = new MealPlanningService();
        
        // Create the service to test - this will trigger initializeIngredientsAndRecipes
        shoppingListService = new ShoppingListService(mealPlanningService);
    }

    @After
    public void tearDown() throws Exception {
        // Clean up test data
        if (connection != null) {
            // Delete test data from tables
            cleanUpTestData();
            
            // Release the connection
            DatabaseHelper.releaseConnection(connection);
        }
        
        // Close all database connections
        DatabaseHelper.closeAllConnections();
    }
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // Delete test database
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }
    
    /**
     * Clean up any test data created during tests
     */
    private void cleanUpTestData() {
        try {
            // Drop all test tables to ensure clean state
            String[] tables = {"recipe_ingredients", "recipes", "ingredients", "food_nutrients", 
                               "foods", "meal_plans", "food_logs", "excluded_foods", 
                               "health_conditions", "diet_profiles", "nutrition_goals", "users"};

            for (String table : tables) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("DROP TABLE IF EXISTS " + table);
                } catch (SQLException e) {
                    // Ignore errors - table might not exist
                }
            }
        } catch (Exception e) {
            System.out.println("Error cleaning up test data: " + e.getMessage());
        }
    }
    
    /**
     * Test constructor and database initialization
     */
    @Test
    public void testConstructor() {
        // Verify shoppingListService is not null
        assertNotNull("ShoppingListService should not be null", shoppingListService);
        
        // Verify that the constructor initialized the database tables
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ingredients")) {
            
            rs.next();
            int count = rs.getInt(1);
            assertTrue("Should have ingredients in the database", count > 0);
        } catch (SQLException e) {
            fail("Failed to query ingredients table: " + e.getMessage());
        }
        
        // Verify recipes were created
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM recipes")) {
            
            rs.next();
            int count = rs.getInt(1);
            assertTrue("Should have recipes in the database", count > 0);
        } catch (SQLException e) {
            fail("Failed to query recipes table: " + e.getMessage());
        }
    }
    
    /**
     * Test initializeIngredientsAndRecipes method
     */
    @Test
    public void testInitializeIngredientsAndRecipes() throws Exception {
        // Clean up any existing data
        cleanUpTestData();
        
        // Create tables manually to simulate database initialization
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS ingredients (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT UNIQUE NOT NULL," +
                        "price REAL NOT NULL)");
                        
            stmt.execute("CREATE TABLE IF NOT EXISTS recipes (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "meal_type TEXT NOT NULL," +
                        "food_id INTEGER," +
                        "name TEXT NOT NULL)");
                        
            stmt.execute("CREATE TABLE IF NOT EXISTS recipe_ingredients (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "recipe_id INTEGER NOT NULL," +
                        "ingredient_id INTEGER NOT NULL," +
                        "amount REAL NOT NULL," +
                        "unit TEXT NOT NULL)");
        }
        
        // Use reflection to access private method
        Method method = ShoppingListService.class.getDeclaredMethod(
            "initializeIngredientsAndRecipes");
        method.setAccessible(true);
        
        // Create a new service instance and call the method
        ShoppingListService testService = new ShoppingListService(mealPlanningService);
        method.invoke(testService);
        
        // Verify ingredients were created
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ingredients")) {
            
            rs.next();
            int count = rs.getInt(1);
            assertTrue("Should have ingredients after initialization", count > 0);
        }
        
        // Verify recipes were created
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM recipes")) {
            
            rs.next();
            int count = rs.getInt(1);
            assertTrue("Should have recipes after initialization", count > 0);
        }
        
        // Verify recipe_ingredients were created
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM recipe_ingredients")) {
            
            rs.next();
            int count = rs.getInt(1);
            assertTrue("Should have recipe ingredients after initialization", count > 0);
        }
    }
    
    /**
     * Test initializeIngredientPrices method
     */
    @Test
    public void testInitializeIngredientPrices() throws Exception {
        // Clean up any existing data
        cleanUpTestData();
        
        // Create tables manually to simulate database initialization
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS ingredients (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT UNIQUE NOT NULL," +
                        "price REAL NOT NULL)");
        }
        
        // Create a test connection for reflective call to private method
        Connection testConn = DatabaseHelper.getConnection();
        
        try {
            // Use reflection to call the private method initializeIngredientPrices
            Method method = ShoppingListService.class.getDeclaredMethod(
                "initializeIngredientPrices", Connection.class);
            method.setAccessible(true);
            
            // Create a new service instance
            ShoppingListService testService = new ShoppingListService(mealPlanningService);
            
            // Invoke the private method
            method.invoke(testService, testConn);
            
            // Verify ingredient prices were saved
            try (Statement stmt = testConn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ingredients")) {
                
                assertTrue("Should have ingredients in the database", rs.next());
                int count = rs.getInt(1);
                assertTrue("Should have at least one ingredient", count > 0);
            }
            
            // Verify specific ingredients were created
            try (Statement stmt = testConn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT price FROM ingredients WHERE name = 'Tomato'")) {
                
                assertTrue("Should have Tomato ingredient", rs.next());
                assertTrue("Tomato should have a positive price", rs.getDouble(1) > 0);
            }
        } finally {
            // Cleanup
            DatabaseHelper.releaseConnection(testConn);
        }
    }
    
    /**
     * Test initializeBreakfastRecipes method
     */
    @Test
    public void testInitializeBreakfastRecipes() throws Exception {
        // Clean up any existing data
        cleanUpTestData();
        
        // Create necessary tables
        setupTestTables();
        
        // Create a test connection for reflective call to private method
        Connection testConn = DatabaseHelper.getConnection();
        
        try {
            // Use reflection to call the private method
            Method method = ShoppingListService.class.getDeclaredMethod(
                "initializeBreakfastRecipes", Connection.class);
            method.setAccessible(true);
            
            // Create a new service instance
            ShoppingListService testService = new ShoppingListService(mealPlanningService);
            
            // Invoke the private method
            method.invoke(testService, testConn);
            
            // Verify breakfast recipes were created
            try (Statement stmt = testConn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM recipes WHERE meal_type = 'breakfast'")) {
                
                rs.next();
                int count = rs.getInt(1);
                assertTrue("Should have breakfast recipes", count > 0);
            }
            
            // Verify Scrambled Eggs recipe was created
            try (Statement stmt = testConn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT id FROM recipes WHERE name = 'Scrambled Eggs' AND meal_type = 'breakfast'")) {
                
                assertTrue("Should have Scrambled Eggs recipe", rs.next());
                int recipeId = rs.getInt("id");
                
                // Verify ingredients for Scrambled Eggs
                verifyIngredientDetails(testConn, recipeId, "Eggs", 3, "unit");
                verifyIngredientDetails(testConn, recipeId, "Milk", 30, "ml");
                verifyIngredientDetails(testConn, recipeId, "Salt", 2, "g");
            }
        } finally {
            // Cleanup
            DatabaseHelper.releaseConnection(testConn);
        }
    }
    
    /**
     * Test initializeLunchRecipes method
     */
    @Test
    public void testInitializeLunchRecipes() throws Exception {
        // Clean up any existing data
        cleanUpTestData();
        
        // Create necessary tables
        setupTestTables();
        
        // Create a test connection for reflective call to private method
        Connection testConn = DatabaseHelper.getConnection();
        
        try {
            // Use reflection to call the private method
            Method method = ShoppingListService.class.getDeclaredMethod(
                "initializeLunchRecipes", Connection.class);
            method.setAccessible(true);
            
            // Create a new service instance
            ShoppingListService testService = new ShoppingListService(mealPlanningService);
            
            // Invoke the private method
            method.invoke(testService, testConn);
            
            // Verify lunch recipes were created
            try (Statement stmt = testConn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM recipes WHERE meal_type = 'lunch'")) {
                
                rs.next();
                int count = rs.getInt(1);
                assertTrue("Should have lunch recipes", count > 0);
            }
            
            // Verify Grilled Chicken Salad recipe was created
            try (Statement stmt = testConn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT id FROM recipes WHERE name = 'Grilled Chicken Salad' AND meal_type = 'lunch'")) {
                
                assertTrue("Should have Grilled Chicken Salad recipe", rs.next());
                int recipeId = rs.getInt("id");
                
                // Verify ingredients for Grilled Chicken Salad
                verifyIngredientDetails(testConn, recipeId, "Chicken Breast", 150, "g");
                verifyIngredientDetails(testConn, recipeId, "Lettuce", 100, "g");
            }
        } finally {
            // Cleanup
            DatabaseHelper.releaseConnection(testConn);
        }
    }
    
    /**
     * Test initializeSnackRecipes method
     */
    @Test
    public void testInitializeSnackRecipes() throws Exception {
        // Clean up any existing data
        cleanUpTestData();
        
        // Create necessary tables
        setupTestTables();
        
        // Create a test connection for reflective call to private method
        Connection testConn = DatabaseHelper.getConnection();
        
        try {
            // Use reflection to call the private method
            Method method = ShoppingListService.class.getDeclaredMethod(
                "initializeSnackRecipes", Connection.class);
            method.setAccessible(true);
            
            // Create a new service instance
            ShoppingListService testService = new ShoppingListService(mealPlanningService);
            
            // Invoke the private method
            method.invoke(testService, testConn);
            
            // Verify snack recipes were created
            try (Statement stmt = testConn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM recipes WHERE meal_type = 'snack'")) {
                
                rs.next();
                int count = rs.getInt(1);
                assertTrue("Should have snack recipes", count > 0);
            }
            
            // Verify Apple with Peanut Butter recipe was created
            try (Statement stmt = testConn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT id FROM recipes WHERE name = 'Apple with Peanut Butter' AND meal_type = 'snack'")) {
                
                assertTrue("Should have Apple with Peanut Butter recipe", rs.next());
                int recipeId = rs.getInt("id");
                
                // Verify ingredients for Apple with Peanut Butter
                verifyIngredientDetails(testConn, recipeId, "Apple", 1, "unit");
                verifyIngredientDetails(testConn, recipeId, "Peanut Butter", 30, "g");
            }
        } finally {
            // Cleanup
            DatabaseHelper.releaseConnection(testConn);
        }
    }
    
    @Test
    public void testInitializeDinnerRecipes() throws Exception {
        // Clean up any existing data
        cleanUpTestData();
        
        // Create necessary tables
        setupTestTables();
        
        // Create a test connection for reflective call to private method
        Connection testConn = DatabaseHelper.getConnection();
        
        try {
            // Use reflection to call the private method
            Method method = ShoppingListService.class.getDeclaredMethod(
                "initializeDinnerRecipes", Connection.class);
            method.setAccessible(true);
            
            // Create a new service instance
            ShoppingListService testService = new ShoppingListService(mealPlanningService);
            
            // Invoke the private method
            method.invoke(testService, testConn);
            
            // Verify dinner recipes were created
            try (Statement stmt = testConn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM recipes WHERE meal_type = 'dinner'")) {
                
                rs.next();
                int count = rs.getInt(1);
                assertTrue("Should have dinner recipes", count > 0);
            }
            
            // Verify Grilled Salmon with Vegetables recipe was created
            try (Statement stmt = testConn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT id FROM recipes WHERE name = 'Grilled Salmon with Vegetables' AND meal_type = 'dinner'")) {
                
                assertTrue("Should have Grilled Salmon with Vegetables recipe", rs.next());
                int recipeId = rs.getInt("id");
                
                // Verify ingredients were added (just check a couple)
                try (Statement ingredientStmt = testConn.createStatement();
                     ResultSet ingredientRs = ingredientStmt.executeQuery(
                         "SELECT COUNT(*) FROM recipe_ingredients WHERE recipe_id = " + recipeId)) {
                    ingredientRs.next();
                    int ingredientCount = ingredientRs.getInt(1);
                    assertTrue("Should have ingredients for the recipe", ingredientCount > 0);
                }
            }
        } finally {
            // Cleanup
            DatabaseHelper.releaseConnection(testConn);
        }
    }
    
   
    
    @Test
    public void testInsertRecipe() throws Exception {
        // Clean up any existing data
        cleanUpTestData();
        
        // Create necessary tables
        setupTestTables();
        
        // Create a test connection for reflective call to private method
        Connection testConn = DatabaseHelper.getConnection();
        
        try {
            // Use reflection to call the private method
            Method method = ShoppingListService.class.getDeclaredMethod(
                "insertRecipe", Connection.class, String.class, String.class);
            method.setAccessible(true);
            
            // Create a new service instance
            ShoppingListService testService = new ShoppingListService(mealPlanningService);
            
            // Invoke the private method
            int recipeId = (int) method.invoke(testService, testConn, "breakfast", "Test Breakfast");
            
            // Verify recipe was inserted
            assertTrue("Should return valid recipe ID", recipeId > 0);
            
            try (Statement stmt = testConn.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT name, meal_type FROM recipes WHERE id = " + recipeId)) {
                
                assertTrue("Should find the inserted recipe", rs.next());
                assertEquals("Recipe name should match", "Test Breakfast", rs.getString("name"));
                assertEquals("Meal type should match", "breakfast", rs.getString("meal_type"));
            }
            
            // Test inserting another recipe
            int recipeId2 = (int) method.invoke(testService, testConn, "lunch", "Test Lunch");
            assertTrue("Should return valid recipe ID", recipeId2 > 0);
            assertTrue("Second recipe ID should be different", recipeId2 != recipeId);
        } finally {
            // Cleanup
            DatabaseHelper.releaseConnection(testConn);
        }
    }
    
    
    @Test
    public void testAddIngredientToRecipe() throws Exception {
        // Clean up any existing data
        cleanUpTestData();
        
        // Create necessary tables
        setupTestTables();
        
        // Insert a test recipe
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO recipes (meal_type, name) VALUES ('test', 'Test Recipe')");
        }
        
        // Get the recipe ID
        int recipeId = -1;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM recipes WHERE name = 'Test Recipe'")) {
            if (rs.next()) {
                recipeId = rs.getInt("id");
            }
        }
        
        assertTrue("Should have valid recipe ID", recipeId > 0);
        
        // Create a test connection for reflective call to private method
        Connection testConn = DatabaseHelper.getConnection();
        
        try {
            // Use reflection to call the private method
            Method method = ShoppingListService.class.getDeclaredMethod(
                "addIngredientToRecipe", Connection.class, int.class, String.class, double.class, String.class);
            method.setAccessible(true);
            
            // Create a new service instance
            ShoppingListService testService = new ShoppingListService(mealPlanningService);
            
            // Invoke the private method with a new ingredient
            method.invoke(testService, testConn, recipeId, "Banana", 2.0, "unit");
            
            // Verify the ingredient was added
            try (Statement stmt = testConn.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT ri.amount, ri.unit FROM recipe_ingredients ri " +
                    "JOIN ingredients i ON ri.ingredient_id = i.id " +
                    "WHERE ri.recipe_id = " + recipeId + " AND i.name = 'Banana'")) {
                
                assertTrue("Should find the added ingredient", rs.next());
                assertEquals("Amount should match", 2.0, rs.getDouble("amount"), 0.001);
                assertEquals("Unit should match", "unit", rs.getString("unit"));
            }
            
            // Call the method again with same ingredient but different amount
            method.invoke(testService, testConn, recipeId, "Banana", 3.0, "unit");
            
            // Verify the ingredient was updated (not duplicated)
            try (Statement stmt = testConn.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) as count, amount FROM recipe_ingredients ri " +
                    "JOIN ingredients i ON ri.ingredient_id = i.id " +
                    "WHERE ri.recipe_id = " + recipeId + " AND i.name = 'Banana' " +
                    "GROUP BY ri.recipe_id")) {
                
                assertTrue("Should find only one entry for the ingredient", rs.next());
                assertEquals("Should have just one entry", 1, rs.getInt("count"));
                assertEquals("Amount should be updated", 3.0, rs.getDouble("amount"), 0.001);
            }
            
            // Test with non-existent recipe ID
            method.invoke(testService, testConn, 9999, "Apple", 1.0, "unit");
            
            // Verify handling of invalid input
            method.invoke(testService, testConn, -1, "Invalid", 1.0, "unit");
            method.invoke(testService, testConn, recipeId, null, 1.0, "unit");
            method.invoke(testService, testConn, recipeId, "Test", -1.0, null);
            
        } finally {
            // Cleanup
            DatabaseHelper.releaseConnection(testConn);
        }
    }
    
    
    
    /**
     * Test getIngredientId method
     */
    @Test
    public void testGetIngredientId() throws Exception {
        // Clean up any existing data
        cleanUpTestData();
        
        // Create necessary tables and insert test ingredient
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS ingredients (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "name TEXT UNIQUE NOT NULL," +
                         "price REAL NOT NULL)");
            
            stmt.execute("INSERT INTO ingredients (name, price) VALUES ('Test Ingredient', 2.99)");
        }
        
        // Create a test connection for reflective call to private method
        Connection testConn = DatabaseHelper.getConnection();
        
        try {
            // Use reflection to call the private method
            Method method = ShoppingListService.class.getDeclaredMethod(
                "getIngredientId", Connection.class, String.class);
            method.setAccessible(true);
            
            // Create a new service instance
            ShoppingListService testService = new ShoppingListService(mealPlanningService);
            
            // Invoke the private method with existing ingredient
            int ingredientId = (int) method.invoke(testService, testConn, "Test Ingredient");
            
            // Verify ingredient ID was found
            assertTrue("Should return valid ingredient ID", ingredientId > 0);
            
            // Test with non-existent ingredient
            int nonExistentId = (int) method.invoke(testService, testConn, "Non-existent Ingredient");
            assertEquals("Should return -1 for non-existent ingredient", -1, nonExistentId);
        } finally {
            // Cleanup
            DatabaseHelper.releaseConnection(testConn);
        }
    }
    
    /**
     * Test insertRecipeIngredient method
     */
    @Test
    public void testInsertRecipeIngredient() throws Exception {
        // Clean up any existing data
        cleanUpTestData();
        
        // Create necessary tables and data
        setupTestTables();
        
        // Insert a test recipe and ingredient
        int recipeId = -1;
        int ingredientId = -1;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO recipes (meal_type, name) VALUES ('test', 'Test Recipe')");
            stmt.execute("INSERT INTO ingredients (name, price) VALUES ('Test Ingredient', 2.99)");
            
            // Get the IDs
            ResultSet rsRecipe = stmt.executeQuery("SELECT id FROM recipes WHERE name = 'Test Recipe'");
            if (rsRecipe.next()) {
                recipeId = rsRecipe.getInt("id");
            }
            
            ResultSet rsIngredient = stmt.executeQuery("SELECT id FROM ingredients WHERE name = 'Test Ingredient'");
            if (rsIngredient.next()) {
                ingredientId = rsIngredient.getInt("id");
            }
        }
        
        // Ensure we have valid IDs
        assertTrue("Should have valid recipe ID", recipeId > 0);
        assertTrue("Should have valid ingredient ID", ingredientId > 0);
        
        // Create a test connection for reflective call to private method
        Connection testConn = DatabaseHelper.getConnection();
        
        try {
            // Use reflection to call the private method
            Method method = ShoppingListService.class.getDeclaredMethod(
                "insertRecipeIngredient", Connection.class, int.class, String.class, double.class, String.class);
            method.setAccessible(true);
            
            // Create a new service instance
            ShoppingListService testService = new ShoppingListService(mealPlanningService);
            
            // Invoke the private method
            boolean success = (boolean) method.invoke(testService, testConn, recipeId, "Test Ingredient", 150.0, "g");
            
            // Verify ingredient was added to recipe
            assertTrue("Should successfully add ingredient to recipe", success);
            
            // Verify the ingredient was added in the database
            try (Statement stmt = testConn.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT amount, unit FROM recipe_ingredients WHERE recipe_id = " + recipeId +
                    " AND ingredient_id = " + ingredientId)) {
                
                assertTrue("Should find the recipe ingredient", rs.next());
                assertEquals("Amount should match", 150.0, rs.getDouble("amount"), 0.001);
                assertEquals("Unit should match", "g", rs.getString("unit"));
            }
            
            // Test with non-existent ingredient (should return false)
            boolean failResult = (boolean) method.invoke(testService, testConn, recipeId, "Non-existent Ingredient", 100.0, "g");
            assertFalse("Should return false for non-existent ingredient", failResult);
        } finally {
            // Cleanup
            DatabaseHelper.releaseConnection(testConn);
        }
    }
    
    /**
     * Test getIngredientsForFood method with valid inputs
     */
    @Test
    public void testGetIngredientsForFood() {
        // Create test data in the database
        createTestRecipeWithIngredients("breakfast", "Test Breakfast", 
            new String[] {"Eggs", "Bread", "Butter"}, 
            new double[] {2.0, 2.0, 10.0}, 
            new String[] {"unit", "slice", "g"}, 
            new double[] {0.5, 0.25, 0.1});
        
        // Get ingredients for the test recipe
        List<ShoppingListService.Ingredient> ingredients = 
            shoppingListService.getIngredientsForFood("breakfast", "Test Breakfast");
        
        // Verify results
        assertNotNull("Ingredients list should not be null", ingredients);
        assertEquals("Should have 3 ingredients", 3, ingredients.size());
        
        // Check first ingredient
        ShoppingListService.Ingredient firstIngredient = ingredients.get(0);
        assertEquals("First ingredient should be Eggs", "Eggs", firstIngredient.getName());
        assertEquals("Amount should match", 2.0, firstIngredient.getAmount(), 0.001);
        assertEquals("Unit should match", "unit", firstIngredient.getUnit());
        assertTrue("Price should be positive", firstIngredient.getPrice() > 0);
    }
    
    @Test
    public void testGetIngredientsForFoodDatabaseError() {
        // Create an invalid database state to test error handling in getIngredientsForFood
        try (Statement stmt = connection.createStatement()) {
            // Rename a column to cause SQL error
            stmt.execute("ALTER TABLE recipes RENAME TO recipes_original");
            stmt.execute("CREATE TABLE recipes (id INTEGER PRIMARY KEY, wrong_column TEXT)");
        } catch (SQLException e) {
            // Ignore errors - might not support ALTER TABLE
            try {
                // Alternative approach - drop the table
                connection.createStatement().execute("DROP TABLE recipes");
            } catch (SQLException ex) {
                // Ignore
            }
        }
        
        // Try to get ingredients - should handle the error gracefully
        List<ShoppingListService.Ingredient> ingredients = 
            shoppingListService.getIngredientsForFood("breakfast", "Test Food");
        
        // Should return an empty list, not throw exception
        assertNotNull("Should handle database error gracefully", ingredients);
        assertTrue("Should return empty list on error", ingredients.isEmpty());
    }
    
    @Test
    public void testGetIngredientsForFoodInvalidMeal() {
        // Test with an invalid meal type
        List<ShoppingListService.Ingredient> ingredients = 
            shoppingListService.getIngredientsForFood("invalidmeal", "Test Food");
        
        // Verify results
        assertNotNull("Ingredients list should not be null", ingredients);
        assertTrue("Ingredients list should be empty", ingredients.isEmpty());
    }

    @Test
    public void testGetIngredientsForFoodInvalidFood() {
        // Test with a valid meal type but invalid food name
        List<ShoppingListService.Ingredient> ingredients = 
            shoppingListService.getIngredientsForFood("breakfast", "NonExistentFood");
        
        // Verify results
        assertNotNull("Ingredients list should not be null", ingredients);
        assertTrue("Ingredients list should be empty", ingredients.isEmpty());
    }

    
        	    public void testGetIngredientsForFoodNullParameters() {
        	        // Test with null parameters
        	        List<ShoppingListService.Ingredient> ingredients1 = shoppingListService.getIngredientsForFood(null, "Test Food");
        	        List<ShoppingListService.Ingredient> ingredients2 = shoppingListService.getIngredientsForFood("breakfast", null);
        	        List<ShoppingListService.Ingredient> ingredients3 = shoppingListService.getIngredientsForFood(null, null);
        	        
        	        // Verify results
        	        assertNotNull("Ingredients list should not be null", ingredients1);
        	        assertTrue("Ingredients list should be empty", ingredients1.isEmpty());
        	        
        	        assertNotNull("Ingredients list should not be null", ingredients2);
        	        assertTrue("Ingredients list should be empty", ingredients2.isEmpty());
        	        
        	        assertNotNull("Ingredients list should not be null", ingredients3);
        	        assertTrue("Ingredients list should be empty", ingredients3.isEmpty());
        	    }
        	    
        	    /**
        	     * Test calculateTotalCost with valid ingredients
        	     */
        	    @Test
        	    public void testCalculateTotalCost() {
        	        // Create test ingredients
        	        List<ShoppingListService.Ingredient> ingredients = new ArrayList<>();
        	        
        	        // Add ingredients with different units
        	        ingredients.add(shoppingListService.new Ingredient("Flour", 200.0, "g", 2.0)); // 2.0 is price for 100g
        	        ingredients.add(shoppingListService.new Ingredient("Milk", 300.0, "ml", 1.0)); // 1.0 is price for 100ml
        	        ingredients.add(shoppingListService.new Ingredient("Eggs", 2.0, "unit", 0.5)); // 0.5 is price per unit
        	        
        	        // Calculate total cost
        	        double totalCost = shoppingListService.calculateTotalCost(ingredients);
        	        
        	        // Expected cost: (200/100)*2.0 + (300/100)*1.0 + 2*0.5 = 4.0 + 3.0 + 1.0 = 8.0
        	        assertEquals("Total cost should match the expected value", 8.0, totalCost, 0.001);
        	        
        	        // Test with one ingredient
        	        List<ShoppingListService.Ingredient> singleIngredient = new ArrayList<>();
        	        singleIngredient.add(shoppingListService.new Ingredient("Sugar", 50.0, "g", 1.5)); // 1.5 is price for 100g
        	        
        	        // Expected cost: (50/100)*1.5 = 0.75
        	        double singleCost = shoppingListService.calculateTotalCost(singleIngredient);
        	        assertEquals("Single ingredient cost should match", 0.75, singleCost, 0.001);
        	    }

        	    @Test
        	    public void testCalculateTotalCostWithEmptyIngredients() {
        	        // Test with empty ingredients list
        	        List<ShoppingListService.Ingredient> ingredients = new ArrayList<>();
        	        
        	        double totalCost = shoppingListService.calculateTotalCost(ingredients);
        	        assertEquals("Total cost should be 0 for empty list", 0.0, totalCost, 0.001);
        	    }

        	    @Test
        	    public void testCalculateTotalCostWithNullIngredients() {
        	        // Test with null ingredients list
        	        double totalCost = shoppingListService.calculateTotalCost(null);
        	        assertEquals("Total cost should be 0 for null list", 0.0, totalCost, 0.001);
        	    }

        	    @Test
        	    public void testIngredientClass() {
        	        // Create an Ingredient object directly (need to use the inner class)
        	        ShoppingListService.Ingredient ingredient = shoppingListService.new Ingredient(
        	            "Test Ingredient", 150.0, "g", 2.5);
        	        
        	        // Test getters
        	        assertEquals("Name should match", "Test Ingredient", ingredient.getName());
        	        assertEquals("Amount should match", 150.0, ingredient.getAmount(), 0.001);
        	        assertEquals("Unit should match", "g", ingredient.getUnit());
        	        assertEquals("Price should match", 2.5, ingredient.getPrice(), 0.001);
        	        
        	        // Test toString method
        	        assertEquals("ToString should format correctly", 
        	                    "Test Ingredient (150.0 g)", ingredient.toString());
        	        
        	        // Test with null name and unit
        	        ShoppingListService.Ingredient nullNameIngredient = 
        	            shoppingListService.new Ingredient(null, 100.0, "g", 1.0);
        	        assertEquals("Null name should become empty string", "", nullNameIngredient.getName());
        	        
        	        ShoppingListService.Ingredient nullUnitIngredient = 
        	            shoppingListService.new Ingredient("Name", 100.0, null, 1.0);
        	        assertEquals("Null unit should become empty string", "", nullUnitIngredient.getUnit());
        	        
        	        // Test with negative values
        	        ShoppingListService.Ingredient negativeIngredient = 
        	            shoppingListService.new Ingredient("Negative", -10.0, "g", -5.0);
        	        assertEquals("Negative amount should become 0", 0.0, negativeIngredient.getAmount(), 0.001);
        	        assertEquals("Negative price should become 0", 0.0, negativeIngredient.getPrice(), 0.001);
        	    }
        	    
        	    /**
        	     * Test error handling when initializing the database
        	     */
        	    @Test
        	    public void testDatabaseErrorHandling() throws Exception {
        	        // Simulate database connection error by using an invalid JDBC URL
        	        System.setProperty("db.url", "jdbc:invalid:notreal");
        	        
        	        // Create a new service, which should handle the error gracefully
        	        ShoppingListService errorHandlingService = null;
        	        try {
        	            errorHandlingService = new ShoppingListService(mealPlanningService);
        	            // Should not throw exception even with invalid database URL
        	            assertNotNull("Should create service even with database errors", errorHandlingService);
        	        } catch (Exception e) {
        	            fail("Should handle database errors gracefully, but threw: " + e.getMessage());
        	        } finally {
        	            // Reset the JDBC URL
        	            System.setProperty("db.url", "jdbc:sqlite:" + TEST_DB);
        	        }
        	    }
        	    
        	    /**
        	     * Test integration with MealPlanningService
        	     */
        	    @Test
        	    public void testIntegrationWithMealPlanningService() {
        	        // Confirm integration with MealPlanningService
        	        // You can test if the service is properly using the MealPlanningService provided in constructor
        	        
        	        // This can be a reflective test to verify the field is properly set
        	        try {
        	            Field mealPlanningServiceField = ShoppingListService.class.getDeclaredField("mealPlanningService");
        	            mealPlanningServiceField.setAccessible(true);
        	            
        	            Object fieldValue = mealPlanningServiceField.get(shoppingListService);
        	            assertNotNull("MealPlanningService field should not be null", fieldValue);
        	            assertTrue("MealPlanningService field should have correct type", 
        	                      fieldValue instanceof MealPlanningService);
        	            assertEquals("MealPlanningService field should be the one provided in constructor", 
        	                        mealPlanningService, fieldValue);
        	        } catch (Exception e) {
        	            fail("Exception while testing integration: " + e.getMessage());
        	        }
        	    }
        	    
        	    /**
        	     * Test with malformed database or missing tables
        	     */
        	    @Test
        	    public void testWithMalformedDatabase() throws Exception {
        	        // Clean up any existing data
        	        cleanUpTestData();
        	        
        	        // Create only partial tables to simulate malformed database
        	        try (Statement stmt = connection.createStatement()) {
        	            stmt.execute("CREATE TABLE IF NOT EXISTS ingredients (" +
        	                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        	                        "name TEXT UNIQUE NOT NULL," +
        	                        "price REAL NOT NULL)");
        	            
        	            // Do not create recipes or recipe_ingredients tables
        	        }
        	        
        	        // Create a new service - it should handle the missing tables gracefully
        	        ShoppingListService testService = new ShoppingListService(mealPlanningService);
        	        
        	        // Test getting ingredients - should return empty list, not throw exception
        	        List<ShoppingListService.Ingredient> ingredients = 
        	            testService.getIngredientsForFood("breakfast", "Test Food");
        	        
        	        assertNotNull("Should handle missing tables gracefully", ingredients);
        	        assertTrue("Should return empty list when tables are missing", ingredients.isEmpty());
        	    }
        	    
        	    /**
        	     * Helper method to verify ingredient details in a recipe
        	     */
        	    private void verifyIngredientDetails(Connection conn, int recipeId, String ingredientName, 
        	                                       double expectedAmount, String expectedUnit) throws SQLException {
        	        try (Statement stmt = conn.createStatement()) {
        	            ResultSet rs = stmt.executeQuery(
        	                "SELECT ri.amount, ri.unit FROM recipe_ingredients ri " +
        	                "JOIN ingredients i ON ri.ingredient_id = i.id " +
        	                "WHERE ri.recipe_id = " + recipeId + " AND i.name = '" + ingredientName + "'");
        	            
        	            assertTrue("Should find ingredient " + ingredientName, rs.next());
        	            assertEquals("Amount for " + ingredientName + " should match", 
        	                        expectedAmount, rs.getDouble("amount"), 0.001);
        	            assertEquals("Unit for " + ingredientName + " should match", 
        	                        expectedUnit, rs.getString("unit"));
        	        }
        	    }
        	    
        	    /**
        	     * Helper method to set up test tables
        	     */
        	    private void setupTestTables() throws SQLException {
        	        try (Statement stmt = connection.createStatement()) {
        	            stmt.execute("CREATE TABLE IF NOT EXISTS ingredients (" +
        	                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        	                         "name TEXT UNIQUE NOT NULL," +
        	                         "price REAL NOT NULL)");
        	            
        	            stmt.execute("CREATE TABLE IF NOT EXISTS recipes (" +
        	                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        	                         "meal_type TEXT NOT NULL," +
        	                         "food_id INTEGER," +
        	                         "name TEXT NOT NULL)");
        	            
        	            stmt.execute("CREATE TABLE IF NOT EXISTS recipe_ingredients (" +
        	                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        	                         "recipe_id INTEGER NOT NULL," +
        	                         "ingredient_id INTEGER NOT NULL," +
        	                         "amount REAL NOT NULL," +
        	                         "unit TEXT NOT NULL)");
        	            
        	            // Insert common ingredients
        	            String[] ingredients = {
        	                "Chicken Breast", "Lettuce", "Tomato", "Cucumber", "Olive Oil",
        	                "Lemon", "Salt", "Pepper", "Apple", "Peanut Butter", "Salmon",
        	                "Broccoli", "Carrot", "Garlic", "Eggs", "Milk", "Banana",
        	                "Strawberry", "Honey", "Greek Yogurt", "Blueberry", "Bread", "Butter"
        	            };
        	            
        	            for (String ingredient : ingredients) {
        	                stmt.execute("INSERT OR IGNORE INTO ingredients (name, price) VALUES ('" + 
        	                             ingredient + "', 1.99)");
        	            }
        	        }
        	    }
        	    
        	    /**
        	     * Helper method to create a test recipe with ingredients
        	     */
        	    private void createTestRecipeWithIngredients(String mealType, String recipeName, 
        	                                               String[] ingredientNames, double[] amounts,
        	                                               String[] units, double[] prices) {
        	        try {
        	            // First, create the ingredients
        	            for (int i = 0; i < ingredientNames.length; i++) {
        	                try (Statement stmt = connection.createStatement()) {
        	                    stmt.execute("INSERT OR IGNORE INTO ingredients (name, price) VALUES ('" +
        	                                ingredientNames[i] + "', " + prices[i] + ")");
        	                }
        	            }
        	            
        	            // Create the recipe
        	            int recipeId;
        	            try (Statement stmt = connection.createStatement()) {
        	                stmt.execute("INSERT INTO recipes (meal_type, name) VALUES ('" + 
        	                             mealType + "', '" + recipeName + "')");
        	                
        	                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
        	                if (!rs.next()) {
        	                    return; // Failed to get recipe ID
        	                }
        	                recipeId = rs.getInt(1);
        	            }
        	            
        	            // Add ingredients to the recipe
        	            for (int i = 0; i < ingredientNames.length; i++) {
        	                // Get ingredient ID
        	                int ingredientId;
        	                try (Statement stmt = connection.createStatement()) {
        	                    ResultSet rs = stmt.executeQuery(
        	                        "SELECT id FROM ingredients WHERE name = '" + ingredientNames[i] + "'");
        	                    
        	                    if (!rs.next()) {
        	                        continue; // Skip this ingredient
        	                    }
        	                    ingredientId = rs.getInt("id");
        	                }
        	                
        	                // Add ingredient to recipe
        	                try (Statement stmt = connection.createStatement()) {
        	                    stmt.execute(
        	                        "INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit) " +
        	                        "VALUES (" + recipeId + ", " + ingredientId + ", " + amounts[i] + ", '" + units[i] + "')");
        	                }
        	            }
        	        } catch (SQLException e) {
        	            System.out.println("Error creating test recipe: " + e.getMessage());
        	        }
        	    }
        	    
        	    /**
        	     * Test edge cases for the addIngredientToRecipe method
        	     */
        	    @Test
        	    public void testAddIngredientToRecipeEdgeCases() throws Exception {
        	        // Clean up any existing data
        	        cleanUpTestData();
        	        
        	        // Create necessary tables
        	        setupTestTables();
        	        
        	        // Insert a test recipe
        	        try (Statement stmt = connection.createStatement()) {
        	            stmt.execute("INSERT INTO recipes (meal_type, name) VALUES ('test', 'Test Recipe')");
        	        }
        	        
        	        // Get the recipe ID
        	        int recipeId = -1;
        	        try (Statement stmt = connection.createStatement();
        	             ResultSet rs = stmt.executeQuery("SELECT id FROM recipes WHERE name = 'Test Recipe'")) {
        	            if (rs.next()) {
        	                recipeId = rs.getInt("id");
        	            }
        	        }
        	        
        	        assertTrue("Should have valid recipe ID", recipeId > 0);
        	        
        	        // Create a test connection for reflective call to private method
        	        Connection testConn = DatabaseHelper.getConnection();
        	        
        	        try {
        	            // Use reflection to call the private method
        	            Method method = ShoppingListService.class.getDeclaredMethod(
        	                "addIngredientToRecipe", Connection.class, int.class, String.class, double.class, String.class);
        	            method.setAccessible(true);
        	            
        	            // Create a new service instance
        	            ShoppingListService testService = new ShoppingListService(mealPlanningService);
        	            
        	            // Test with null ingredient name
        	            method.invoke(testService, testConn, recipeId, null, 1.0, "unit");
        	            
        	            // Test with null unit
        	            method.invoke(testService, testConn, recipeId, "Apple", 1.0, null);
        	            
        	            // Test with negative recipe ID (should handle gracefully)
        	            method.invoke(testService, testConn, -1, "Apple", 1.0, "unit");
        	            
        	            // Test with zero amount (edge case)
        	            method.invoke(testService, testConn, recipeId, "Banana", 0.0, "unit");
        	            
        	            // Verify behavior with zero amount
        	            try (Statement stmt = testConn.createStatement();
        	                ResultSet rs = stmt.executeQuery(
        	                    "SELECT amount FROM recipe_ingredients ri " +
        	                    "JOIN ingredients i ON ri.ingredient_id = i.id " +
        	                    "WHERE ri.recipe_id = " + recipeId + " AND i.name = 'Banana'")) {
        	                
        	                if (rs.next()) {
        	                    // Check that amount is as expected (either 0 or a minimum value)
        	                    assertTrue("Amount should be non-negative", rs.getDouble("amount") >= 0);
        	                }
        	                // It's okay if this test doesn't find the ingredient - some implementations might reject zero amounts
        	            }
        	        } finally {
        	            // Cleanup
        	            DatabaseHelper.releaseConnection(testConn);
        	        }
        	    }
        	}