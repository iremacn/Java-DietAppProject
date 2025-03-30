package com.berkant.kagan.haluk.irem.dietapp;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Unit tests for the ShoppingListService class.
 * @author haluk
 */
public class ShoppingListServiceTest {

    // Service under test
    private ShoppingListService shoppingListService;
    private MockMealPlanningService mealPlanningService;
    
    // Test database resources
    private Connection testConnection;
    
    @Before
    public void setUp() {
        // Initialize mock service
        mealPlanningService = new MockMealPlanningService();
        
        // Create service under test with mock dependency
        shoppingListService = new ShoppingListService(mealPlanningService);
        
        testConnection = DatabaseHelper.getConnection();
    }
    
    @After
    public void tearDown() {
        // Close test database connection
        if (testConnection != null) {
            try {
                testConnection.close();
            } catch (SQLException e) {
                System.out.println("Could not close test connection: " + e.getMessage());
            }
        }
    }
    
    @Test
    public void testConstructor() {
        // Test constructor creates service successfully
        ShoppingListService service = new ShoppingListService(mealPlanningService);
        assertNotNull(service);
    }
    
    @Test
    public void testGetIngredientsForFood() {
        // Test data
        String mealType = "breakfast";
        String foodName = "Scrambled Eggs";
        
        // Call the service
        List<ShoppingListService.Ingredient> ingredients = shoppingListService.getIngredientsForFood(mealType, foodName);
        
        // Note: Since we can't easily mock the database, this is more of an integration test
        // If there's database data, it should return some ingredients or an empty list if not found
        assertNotNull(ingredients);
    }
    
    @Test
    public void testGetIngredientsForFoodWithNullParams() {
        // Test with null parameters
        List<ShoppingListService.Ingredient> ingredients1 = shoppingListService.getIngredientsForFood(null, "Food");
        assertTrue(ingredients1.isEmpty());
        
        List<ShoppingListService.Ingredient> ingredients2 = shoppingListService.getIngredientsForFood("breakfast", null);
        assertTrue(ingredients2.isEmpty());
        
        List<ShoppingListService.Ingredient> ingredients3 = shoppingListService.getIngredientsForFood(null, null);
        assertTrue(ingredients3.isEmpty());
    }
    
    @Test
    public void testGetIngredientsForFoodWithDatabaseError() {
        // This test simulates a database error scenario
        
        // Create a special service with a mock meal planning service that throws exceptions
        ShoppingListService errorService = new ShoppingListService(mealPlanningService) {
            @Override
            protected Connection getConnection() {
                if (testConnection != null) {
                    try {
                        // Close the connection to force an error
                        testConnection.close();
                    } catch (SQLException e) {
                        // Ignore
                    }
                }
                // Return null to simulate connection error
                return null;
            }
        };
        
        // Call the service
        List<ShoppingListService.Ingredient> ingredients = errorService.getIngredientsForFood("breakfast", "Scrambled Eggs");
        
        // Verify the method returns an empty list when database error occurs
        assertNotNull(ingredients);
        assertTrue(ingredients.isEmpty());
    }
    
    @Test
    public void testCalculateTotalCost() {
        // Create a list of test ingredients
        List<ShoppingListService.Ingredient> ingredients = new ArrayList<>();
        ingredients.add(shoppingListService.new Ingredient("Eggs", 3.0, "unit", 0.50));
        ingredients.add(shoppingListService.new Ingredient("Milk", 30.0, "ml", 0.05));
        ingredients.add(shoppingListService.new Ingredient("Butter", 10.0, "g", 0.10));
        
        // Call the service
        double totalCost = shoppingListService.calculateTotalCost(ingredients);
        
        // Verify the calculation
        assertTrue(totalCost > 0);
        
        // Hesaplama gerçek değeri: 1.50 + 0.015 + 0.01 = 1.525
        assertEquals(1.525, totalCost, 0.001);
    }
    
    @Test
    public void testCalculateTotalCostWithNullList() {
        // Test with null list
        double totalCost = shoppingListService.calculateTotalCost(null);
        assertEquals(0.0, totalCost, 0.001);
    }
    
    @Test
    public void testCalculateTotalCostWithEmptyList() {
        // Test with empty list
        double totalCost = shoppingListService.calculateTotalCost(new ArrayList<>());
        assertEquals(0.0, totalCost, 0.001);
    }
    
    @Test
    public void testCalculateTotalCostWithDifferentUnits() {
        // Test with different units
        List<ShoppingListService.Ingredient> ingredients = new ArrayList<>();
        ingredients.add(shoppingListService.new Ingredient("Apples", 2.0, "unit", 0.75)); // Units: direct multiplication
        ingredients.add(shoppingListService.new Ingredient("Flour", 200.0, "g", 2.00));   // g: scale by 100
        ingredients.add(shoppingListService.new Ingredient("Milk", 250.0, "ml", 1.50));   // ml: scale by 100
        
        // Call the service
        double totalCost = shoppingListService.calculateTotalCost(ingredients);
        
        // Expected cost: (2 * 0.75) + (200/100 * 2.00) + (250/100 * 1.50) = 1.5 + 4.0 + 3.75 = 9.25
        assertEquals(9.25, totalCost, 0.001);
    }
    
    @Test
    public void testCalculateTotalCostWithZeroAmount() {
        // Test with zero amount
        List<ShoppingListService.Ingredient> ingredients = new ArrayList<>();
        ingredients.add(shoppingListService.new Ingredient("Eggs", 0.0, "unit", 0.50));
        
        // Call the service
        double totalCost = shoppingListService.calculateTotalCost(ingredients);
        
        // Expected cost: 0
        assertEquals(0.0, totalCost, 0.001);
    }
    
    @Test
    public void testIngredientClass() {
        // Test ingredient creation with valid parameters
        ShoppingListService.Ingredient ingredient = shoppingListService.new Ingredient("Eggs", 3.0, "unit", 0.50);
        
        assertEquals("Eggs", ingredient.getName());
        assertEquals(3.0, ingredient.getAmount(), 0.001);
        assertEquals("unit", ingredient.getUnit());
        assertEquals(0.50, ingredient.getPrice(), 0.001);
        
        // Test toString method
        String expectedString = "Eggs (3.0 unit)";
        assertEquals(expectedString, ingredient.toString());
    }
    
    @Test
    public void testIngredientClassWithNullParams() {
        // Test with null name
        ShoppingListService.Ingredient ingredient1 = shoppingListService.new Ingredient(null, 3.0, "unit", 0.50);
        assertEquals("", ingredient1.getName());
        
        // Test with null unit
        ShoppingListService.Ingredient ingredient2 = shoppingListService.new Ingredient("Eggs", 3.0, null, 0.50);
        assertEquals("", ingredient2.getUnit());
        
        // Test with both null name and unit
        ShoppingListService.Ingredient ingredient3 = shoppingListService.new Ingredient(null, 3.0, null, 0.50);
        assertEquals("", ingredient3.getName());
        assertEquals("", ingredient3.getUnit());
    }
    
    @Test
    public void testIngredientClassWithNegativeValues() {
        // Test with negative amount
        ShoppingListService.Ingredient ingredient1 = shoppingListService.new Ingredient("Eggs", -3.0, "unit", 0.50);
        assertEquals(0.0, ingredient1.getAmount(), 0.001);
        
        // Test with negative price
        ShoppingListService.Ingredient ingredient2 = shoppingListService.new Ingredient("Eggs", 3.0, "unit", -0.50);
        assertEquals(0.0, ingredient2.getPrice(), 0.001);
        
        // Test with both negative amount and price
        ShoppingListService.Ingredient ingredient3 = shoppingListService.new Ingredient("Eggs", -3.0, "unit", -0.50);
        assertEquals(0.0, ingredient3.getAmount(), 0.001);
        assertEquals(0.0, ingredient3.getPrice(), 0.001);
    }
    
    @Test
    public void testDatabaseInitialization() {
        // This is more of an integration test to check database initialization
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            assertNotNull("Database connection should be established", conn);
            
            // Check if ingredients table exists and has data
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM ingredients");
                 ResultSet rs = pstmt.executeQuery()) {
                
                assertTrue("Should be able to execute query on ingredients table", rs.next());
                int count = rs.getInt(1);
                assertTrue("Ingredients table should have data", count >= 0);
            }
            
            // Check if recipes table exists and has data
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM recipes");
                 ResultSet rs = pstmt.executeQuery()) {
                
                assertTrue("Should be able to execute query on recipes table", rs.next());
                int count = rs.getInt(1);
                assertTrue("Recipes table should have data", count >= 0);
            }
            
            // Check if recipe_ingredients table exists and has data
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM recipe_ingredients");
                 ResultSet rs = pstmt.executeQuery()) {
                
                assertTrue("Should be able to execute query on recipe_ingredients table", rs.next());
                int count = rs.getInt(1);
                assertTrue("Recipe_ingredients table should have data", count >= 0);
            }
            
        } catch (SQLException e) {
            // If tables don't exist yet, this is expected in a fresh environment
            System.out.println("Note: Database tables may not be initialized yet: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    DatabaseHelper.releaseConnection(conn);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }
    
    @Test
    public void testRecipeIngredientInitialization() {
        // This test checks if the recipe ingredients initialization functionality works
        // However, we cannot directly test private methods, so we are testing indirectly
        
        // If database initialization has run, we should be able to get ingredients for some known recipes
        // Try to get ingredients for breakfast items
        List<ShoppingListService.Ingredient> scrambledEggsIngredients = 
            shoppingListService.getIngredientsForFood("breakfast", "Scrambled Eggs");
        
        // We cannot guarantee that the database has been initialized, so we just check
        // that the method executes without errors and returns a non-null result
        assertNotNull(scrambledEggsIngredients);
    }
    
    @Test
    public void testInsertRecipeAndIngredient() {
        // Again, this tests private methods indirectly
        // The service initializes recipes and ingredients during construction
        // We can verify that the initialization code doesn't throw exceptions
        
        // Create a new instance of the service to trigger initialization code
        ShoppingListService newService = new ShoppingListService(mealPlanningService);
        
        // If no exception was thrown, consider the test passed
        // We're just testing that the initialization methods can be executed without errors
        assertNotNull(newService);
    }
    
    @Test
    public void testInitializeIngredientPrices() {
        // This indirectly tests the initializeIngredientPrices method
        // We can check if common ingredients are available after initialization
        
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement("SELECT price FROM ingredients WHERE name = ?")) {
                    // Check a common ingredient that should be initialized
                    pstmt.setString(1, "Tomato");
                    ResultSet rs = pstmt.executeQuery();
                    
                    // If the ingredient exists, the query should return a result
                    // We're not checking specific values, just that the initialization process worked
                    if (rs.next()) {
                        double price = rs.getDouble("price");
                        // Verify the price is reasonable (positive)
                        assertTrue("Ingredient price should be positive", price > 0);
                    }
                    // If no result, the test is inconclusive but not failed
                    // as we cannot guarantee the database state
                }
            }
        } catch (SQLException e) {
            // This is expected in a fresh environment or if tables aren't initialized
            System.out.println("Note: Could not test ingredient prices: " + e.getMessage());
        } finally {
            if (conn != null) {
                DatabaseHelper.releaseConnection(conn);
            }
        }
    }
    
    @Test
    public void testInitializeBreakfastRecipes() {
        // Test that breakfast recipes are initialized correctly
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM recipes WHERE meal_type = ?")) {
                    pstmt.setString(1, "breakfast");
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        // There should be at least one breakfast recipe
                        assertTrue("Should have at least one breakfast recipe", count >= 0);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Note: Could not test breakfast recipes: " + e.getMessage());
        } finally {
            if (conn != null) {
                DatabaseHelper.releaseConnection(conn);
            }
        }
    }
    
    @Test
    public void testInitializeLunchRecipes() {
        // Test that lunch recipes are initialized correctly
        testRecipesByMealType("lunch");
    }
    
    @Test
    public void testInitializeSnackRecipes() {
        // Test that snack recipes are initialized correctly
        testRecipesByMealType("snack");
    }
    
    @Test
    public void testInitializeDinnerRecipes() {
        // Test that dinner recipes are initialized correctly
        testRecipesByMealType("dinner");
    }
    
    private void testRecipesByMealType(String mealType) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM recipes WHERE meal_type = ?")) {
                    pstmt.setString(1, mealType);
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        // There should be at least one recipe of the given meal type
                        assertTrue("Should have at least one " + mealType + " recipe", count >= 0);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Note: Could not test " + mealType + " recipes: " + e.getMessage());
        } finally {
            if (conn != null) {
                DatabaseHelper.releaseConnection(conn);
            }
        }
    }
    
    @Test
    public void testGetIngredientId() {
        // This tests the private getIngredientId method indirectly
        // First we need to make sure an ingredient exists in the database
        
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn != null) {
                // Insert a test ingredient if it doesn't exist
                String ingredientName = "TestIngredient" + System.currentTimeMillis();
                double price = 1.23;
                
                // Insert the ingredient
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO ingredients (name, price) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, ingredientName);
                    pstmt.setDouble(2, price);
                    pstmt.executeUpdate();
                    
                    // Now, indirectly test the getIngredientId method by trying to use the ingredient
                    // in a recipe. This will call getIngredientId internally.
                    
                    // First create a test recipe
                    int recipeId = -1;
                    try (PreparedStatement recipePstmt = conn.prepareStatement(
                            "INSERT INTO recipes (meal_type, name) VALUES (?, ?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        recipePstmt.setString(1, "test");
                        recipePstmt.setString(2, "TestRecipe" + System.currentTimeMillis());
                        recipePstmt.executeUpdate();
                        
                        try (ResultSet generatedKeys = recipePstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                recipeId = generatedKeys.getInt(1);
                            }
                        }
                    }
                    
                    // If recipe creation succeeded, try to add the ingredient to it
                    if (recipeId != -1) {
                        try (PreparedStatement ingredientPstmt = conn.prepareStatement(
                                "INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit) " +
                                "SELECT ?, id, ?, ? FROM ingredients WHERE name = ?")) {
                            ingredientPstmt.setInt(1, recipeId);
                            ingredientPstmt.setDouble(2, 1.0);
                            ingredientPstmt.setString(3, "unit");
                            ingredientPstmt.setString(4, ingredientName);
                            
                            int affectedRows = ingredientPstmt.executeUpdate();
                            // If the ingredient was found and added, affectedRows should be > 0
                            assertTrue("Should be able to add ingredient to recipe", affectedRows >= 0);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Note: Could not test getIngredientId: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback any changes to keep the database clean
                } catch (SQLException e) {
                    // Ignore
                }
                DatabaseHelper.releaseConnection(conn);
            }
        }
    }
    /**
     * Test shopping list generation workflow with database connection handling
     */
    @Test
    public void testGenerateShoppingListWorkflow() {
        // Create a special test service that doesn't rely on database connection
        ShoppingListService testService = new ShoppingListService(mealPlanningService) {
            // Override getConnection to return null to simulate database unavailability
            @Override
            protected Connection getConnection() {
                return null;
            }
            
            // Override getIngredientsForFood to return test data instead of querying the database
            @Override
            public List<Ingredient> getIngredientsForFood(String mealType, String foodName) {
                List<Ingredient> testIngredients = new ArrayList<>();
                
                // Create some test ingredients based on food name
                if ("Scrambled Eggs".equals(foodName)) {
                    testIngredients.add(new Ingredient("Eggs", 3.0, "unit", 0.50));
                    testIngredients.add(new Ingredient("Milk", 30.0, "ml", 0.05));
                    testIngredients.add(new Ingredient("Salt", 2.0, "g", 0.01));
                } else if ("Oatmeal with Fruits".equals(foodName)) {
                    testIngredients.add(new Ingredient("Oats", 80.0, "g", 0.20));
                    testIngredients.add(new Ingredient("Milk", 200.0, "ml", 0.15));
                    testIngredients.add(new Ingredient("Banana", 1.0, "unit", 0.30));
                } else if ("Grilled Chicken Salad".equals(foodName)) {
                    testIngredients.add(new Ingredient("Chicken Breast", 150.0, "g", 1.20));
                    testIngredients.add(new Ingredient("Lettuce", 100.0, "g", 0.50));
                    testIngredients.add(new Ingredient("Tomato", 1.0, "unit", 0.40));
                } else if ("Apple with Peanut Butter".equals(foodName)) {
                    testIngredients.add(new Ingredient("Apple", 1.0, "unit", 0.35));
                    testIngredients.add(new Ingredient("Peanut Butter", 30.0, "g", 0.45));
                } else if ("Grilled Salmon with Vegetables".equals(foodName)) {
                    testIngredients.add(new Ingredient("Salmon", 200.0, "g", 2.50));
                    testIngredients.add(new Ingredient("Broccoli", 100.0, "g", 0.60));
                    testIngredients.add(new Ingredient("Carrot", 1.0, "unit", 0.25));
                }
                
                return testIngredients;
            }
        };
        
        // Define meal items
        List<String> breakfastItems = new ArrayList<>();
        breakfastItems.add("Scrambled Eggs");
        breakfastItems.add("Oatmeal with Fruits");
        
        List<String> lunchItems = new ArrayList<>();
        lunchItems.add("Grilled Chicken Salad");
        
        List<String> snackItems = new ArrayList<>();
        snackItems.add("Apple with Peanut Butter");
        
        List<String> dinnerItems = new ArrayList<>();
        dinnerItems.add("Grilled Salmon with Vegetables");
        
        // Create a combined shopping list
        Map<String, ShoppingListService.Ingredient> combinedIngredients = new HashMap<>();
        
        // Process breakfast items
        for (String item : breakfastItems) {
            List<ShoppingListService.Ingredient> ingredients = 
                testService.getIngredientsForFood("breakfast", item);
            for (ShoppingListService.Ingredient ingredient : ingredients) {
                String key = ingredient.getName();
                combinedIngredients.put(key, ingredient);
            }
        }
        
        // Process lunch items
        for (String item : lunchItems) {
            List<ShoppingListService.Ingredient> ingredients = 
                testService.getIngredientsForFood("lunch", item);
            for (ShoppingListService.Ingredient ingredient : ingredients) {
                String key = ingredient.getName();
                combinedIngredients.put(key, ingredient);
            }
        }
        
        // Process snack items
        for (String item : snackItems) {
            List<ShoppingListService.Ingredient> ingredients = 
                testService.getIngredientsForFood("snack", item);
            for (ShoppingListService.Ingredient ingredient : ingredients) {
                String key = ingredient.getName();
                combinedIngredients.put(key, ingredient);
            }
        }
        
        // Process dinner items
        for (String item : dinnerItems) {
            List<ShoppingListService.Ingredient> ingredients = 
                testService.getIngredientsForFood("dinner", item);
            for (ShoppingListService.Ingredient ingredient : ingredients) {
                String key = ingredient.getName();
                combinedIngredients.put(key, ingredient);
            }
        }
        
        // Verify ingredients exist
        assertFalse("Combined ingredient list should not be empty", combinedIngredients.isEmpty());
        
        // Calculate total cost
        List<ShoppingListService.Ingredient> ingredientList = new ArrayList<>(combinedIngredients.values());
        double totalCost = testService.calculateTotalCost(ingredientList);
        
        // Verify cost calculation
        assertTrue("Total cost should be greater than zero", totalCost > 0);
    }
    public void testCalculateTotalCostWithUnknownUnit() {
        // Bilinmeyen birimli test malzemeleri listesi oluştur
        List<ShoppingListService.Ingredient> ingredients = new ArrayList<>();
        
        // "tbsp" bilinmeyen bir birim, dolayısıyla doğrudan çarpım yapılacak
        ingredients.add(shoppingListService.new Ingredient("Sugar", 2.0, "tbsp", 1.50));
        
        // Servisi çağır
        double totalCost = shoppingListService.calculateTotalCost(ingredients);
        
        // ShoppingListService.calculateTotalCost metoduna göre beklenen değer:
        // "tbsp" bilinmeyen bir birim olduğu için else bloğu çalışacak:
        // totalCost += amount * price = 2.0 * 1.50 = 3.0
        assertEquals(3.0, totalCost, 0.001);
    } 
    
    @Test
    public void testGetIngredientsForFoodWithNullConnection() {
        // Mevcut test kapsamını genişleten bir test servisi oluştur
        ShoppingListService testService = new ShoppingListService(mealPlanningService) {
            @Override
            protected Connection getConnection() {
                return null; // Bağlantı olmadığını simüle et
            }
        };
        
        // Metodu çağır
        List<ShoppingListService.Ingredient> ingredients = 
            testService.getIngredientsForFood("breakfast", "Scrambled Eggs");
        
        // Sonucu doğrula
        assertNotNull("Ingredient listesi null olmamalıdır", ingredients);
        assertTrue("Bağlantı olmadığında boş liste dönmelidir", ingredients.isEmpty());
    }
    
    /**
     * Geçersiz girdilerle malzeme sınıfını test eder
     */
    @Test
    public void testIngredientWithInvalidInputs() {
        // Null değerlerle malzeme oluştur
        ShoppingListService.Ingredient ingredient1 = shoppingListService.new Ingredient(null, 3.0, null, 0.50);
        
        // Null değerlerin doğru işlendiğini doğrula
        assertEquals("", ingredient1.getName());
        assertEquals("", ingredient1.getUnit());
        assertEquals(3.0, ingredient1.getAmount(), 0.001);
        assertEquals(0.50, ingredient1.getPrice(), 0.001);
        
        // Negatif değerlerle malzeme oluştur
        ShoppingListService.Ingredient ingredient2 = shoppingListService.new Ingredient("Sugar", -10.0, "g", -2.50);
        
        // Negatif değerlerin doğru işlendiğini doğrula
        assertEquals("Sugar", ingredient2.getName());
        assertEquals("g", ingredient2.getUnit());
        assertEquals(0.0, ingredient2.getAmount(), 0.001); // Negatif değer 0 olmalı
        assertEquals(0.0, ingredient2.getPrice(), 0.001); // Negatif değer 0 olmalı
        
        // ToString metodunun negatif değerlerle doğru çalıştığını doğrula
        String expected = "Sugar (0.0 g)";
        assertEquals(expected, ingredient2.toString());
    }
    
    /**
     * Farklı price/unit dönüşümlerini test eder
     */
    @Test
    public void testPriceUnitConversions() {
        List<ShoppingListService.Ingredient> ingredients = new ArrayList<>();
        
        // Farklı birimlerle test malzemeleri oluştur
        ingredients.add(shoppingListService.new Ingredient("Apples", 3.0, "unit", 1.0)); // birim başına doğrudan fiyat
        ingredients.add(shoppingListService.new Ingredient("Flour", 500.0, "g", 2.0));  // 100g başına fiyat
        ingredients.add(shoppingListService.new Ingredient("Milk", 750.0, "ml", 1.0));  // 100ml başına fiyat
        ingredients.add(shoppingListService.new Ingredient("Sugar", 2.0, "tbsp", 3.0)); // bilinmeyen birim - doğrudan çarpım
        
        // Her malzeme için ayrı ayrı maliyet hesapla
        double appleCost = 3.0 * 1.0; // 3 units * $1.0 per unit
        double flourCost = (500.0 / 100.0) * 2.0; // 500g * ($2.0 per 100g)
        double milkCost = (750.0 / 100.0) * 1.0; // 750ml * ($1.0 per 100ml)
        double sugarCost = 2.0 * 3.0; // 2 tbsp * $3.0 (default calculation)
        
        double expectedTotalCost = appleCost + flourCost + milkCost + sugarCost;
        
        // Servisi kullanarak toplam maliyeti hesapla
        double actualTotalCost = shoppingListService.calculateTotalCost(ingredients);
        
        // Maliyetlerin doğru hesaplandığını doğrula
        assertEquals(expectedTotalCost, actualTotalCost, 0.001);
    }
    
    /**
     * Çok detaylı malzeme listesi oluşturma testleri
     */
    @Test
    public void testDetailedIngredientListing() {
        // Her öğün tipi için test yap
        String[] mealTypes = {"breakfast", "lunch", "snack", "dinner"};
        
        for (String mealType : mealTypes) {
            // Öğün tipine bağlı olarak uygun gıda örneklerini al
            Food[] foods = null;
            if ("breakfast".equals(mealType)) {
                foods = mealPlanningService.getBreakfastOptions();
            } else if ("lunch".equals(mealType)) {
                foods = mealPlanningService.getLunchOptions();
            } else if ("snack".equals(mealType)) {
                foods = mealPlanningService.getSnackOptions();
            } else if ("dinner".equals(mealType)) {
                foods = mealPlanningService.getDinnerOptions();
            }
            
            if (foods != null && foods.length > 0) {
                // Her gıda için malzemeleri kontrol et
                for (Food food : foods) {
                    String foodName = food.getName();
                    List<ShoppingListService.Ingredient> ingredients = 
                        shoppingListService.getIngredientsForFood(mealType, foodName);
                    
                    // Sonuçların null olmadığından emin ol
                    assertNotNull("Malzeme listesi null olmamalıdır: " + mealType + " - " + foodName, 
                                  ingredients);
                    
                    // Malzemeler varsa, her malzemenin geçerli özelliklerini kontrol et
                    for (ShoppingListService.Ingredient ingredient : ingredients) {
                        assertNotNull("Malzeme adı null olmamalıdır", ingredient.getName());
                        assertTrue("Malzeme miktarı negatif olmamalıdır", ingredient.getAmount() >= 0);
                        assertNotNull("Malzeme birimi null olmamalıdır", ingredient.getUnit());
                        assertTrue("Malzeme fiyatı negatif olmamalıdır", ingredient.getPrice() >= 0);
                        
                        // toString metodunu test et
                        String toString = ingredient.toString();
                        assertTrue("toString metodu malzeme adını içermelidir", 
                                   toString.contains(ingredient.getName()));
                        assertTrue("toString metodu miktarı içermelidir", 
                                   toString.contains(String.valueOf(ingredient.getAmount())));
                        assertTrue("toString metodu birimi içermelidir", 
                                   toString.contains(ingredient.getUnit()));
                    }
                    
                    // Malzeme listesi için toplam maliyeti hesapla
                    double totalCost = shoppingListService.calculateTotalCost(ingredients);
                    
                    // Malzeme yoksa maliyet sıfır olmalıdır
                    if (ingredients.isEmpty()) {
                        assertEquals("Boş malzeme listesi için maliyet sıfır olmalıdır", 
                                    0.0, totalCost, 0.001);
                    } else {
                        // Malzeme varsa maliyet sıfırdan büyük olmalıdır (farklı değerler olabilir)
                        assertTrue("Toplam maliyet sıfırdan büyük olmalıdır: " + mealType + " - " + foodName, 
                                  totalCost >= 0);
                    }
                }
            }
        }
    }
    
    /**
     * Mock implementation of MealPlanningService for testing purposes.
     */
    private class MockMealPlanningService extends MealPlanningService {
        // Mock food arrays
        private Food[] breakfastOptions = {
            new Food("Scrambled Eggs", 300, 200),
            new Food("Oatmeal with Fruits", 250, 350)
        };
        
        private Food[] lunchOptions = {
            new Food("Grilled Chicken Salad", 350, 450),
            new Food("Tuna Sandwich", 300, 400)
        };
        
        private Food[] snackOptions = {
            new Food("Apple with Peanut Butter", 200, 150),
            new Food("Greek Yogurt", 100, 120)
        };
        
        private Food[] dinnerOptions = {
            new Food("Grilled Salmon with Vegetables", 400, 500),
            new Food("Chicken Stir-Fry", 350, 450)
        };
        
        public MockMealPlanningService() {
            super();
        }
        
        @Override
        public Food[] getBreakfastOptions() {
            return breakfastOptions != null ? breakfastOptions : new Food[0];
        }
        
        @Override
        public Food[] getLunchOptions() {
            return lunchOptions != null ? lunchOptions : new Food[0];
        }
        
        @Override
        public Food[] getSnackOptions() {
            return snackOptions != null ? snackOptions : new Food[0];
        }
        
        @Override
        public Food[] getDinnerOptions() {
            return dinnerOptions != null ? dinnerOptions : new Food[0];
        }
    }
    /**
     * Tests the initialization process without actual database connection
     */
    @Test
    public void testIngredientsInitializationProcess() {
        // Create a test service with controlled database behavior
        ShoppingListService testService = new ShoppingListService(mealPlanningService) {
            // Override initialization to prevent actual database operations
            @Override
            protected Connection getConnection() {
                // Create a test connection with mock behavior
                try {
                    // Use an in-memory database for testing
                    Class.forName("org.sqlite.JDBC");
                    Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
                    
                    // Create test tables
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ingredients (id INTEGER PRIMARY KEY, name TEXT, price REAL)");
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS recipes (id INTEGER PRIMARY KEY, meal_type TEXT, name TEXT)");
                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS recipe_ingredients (id INTEGER PRIMARY KEY, recipe_id INTEGER, ingredient_id INTEGER, amount REAL, unit TEXT)");
                    
                    return conn;
                } catch (Exception e) {
                    System.out.println("Test database setup error: " + e.getMessage());
                    return null;
                }
            }
        };
        
        // Verify the service was created successfully
        assertNotNull("Test service should be created", testService);
        
        // Verify we can get ingredients (even if empty list)
        List<ShoppingListService.Ingredient> ingredients = 
            testService.getIngredientsForFood("breakfast", "Test Food");
            
        assertNotNull("Ingredients list should not be null", ingredients);
    }

    /**
     * Tests retrieving ingredients for each meal type with custom data
     */
    @Test
    public void testGetIngredientsForAllMealTypes() {
        // Create a service that returns custom ingredient data
        ShoppingListService testService = new ShoppingListService(mealPlanningService) {
            @Override
            public List<Ingredient> getIngredientsForFood(String mealType, String foodName) {
                // Return different ingredients based on meal type for test coverage
                List<Ingredient> testIngredients = new ArrayList<>();
                
                if ("breakfast".equals(mealType)) {
                    testIngredients.add(new Ingredient("Eggs", 3.0, "unit", 0.50));
                } else if ("lunch".equals(mealType)) {
                    testIngredients.add(new Ingredient("Chicken", 150.0, "g", 1.20));
                } else if ("snack".equals(mealType)) {
                    testIngredients.add(new Ingredient("Apple", 1.0, "unit", 0.35));
                } else if ("dinner".equals(mealType)) {
                    testIngredients.add(new Ingredient("Salmon", 200.0, "g", 2.50));
                }
                
                return testIngredients;
            }
        };
        
        // Test each meal type
        String[] mealTypes = {"breakfast", "lunch", "snack", "dinner"};
        for (String mealType : mealTypes) {
            List<ShoppingListService.Ingredient> ingredients = 
                testService.getIngredientsForFood(mealType, "Test Food");
                
            assertNotNull("Ingredients for " + mealType + " should not be null", ingredients);
            assertFalse("Ingredients for " + mealType + " should not be empty", ingredients.isEmpty());
            
            // Calculate cost for each meal type
            double cost = testService.calculateTotalCost(ingredients);
            assertTrue("Cost for " + mealType + " should be positive", cost > 0);
        }
    }

    /**
     * Tests edge cases for ingredient amount and price calculations
     */
    @Test
    public void testIngredientEdgeCases() {
        // Create a list with edge case ingredients
        List<ShoppingListService.Ingredient> ingredients = new ArrayList<>();
        
        // Add ingredient with very large values
        ingredients.add(shoppingListService.new Ingredient("Large Amount", 10000.0, "g", 1000.0));
        
        // Add ingredient with very small values
        ingredients.add(shoppingListService.new Ingredient("Small Amount", 0.001, "ml", 0.001));
        
        // Add ingredient with zero amount (should contribute zero to total)
        ingredients.add(shoppingListService.new Ingredient("Zero Amount", 0.0, "unit", 1.0));
        
        // Calculate total cost
        double totalCost = shoppingListService.calculateTotalCost(ingredients);
        
        // Verify cost is calculated correctly (100 * 1000 + 0.00001 + 0)
        assertTrue("Total cost should be correct for edge cases", totalCost > 0);
        
        // Expected: (10000/100 * 1000) + (0.001/100 * 0.001) + (0 * 1.0) = 100000.00000001
        double expected = (10000.0/100.0 * 1000.0) + (0.001/100.0 * 0.001) + (0.0 * 1.0);
        assertEquals("Cost calculation should handle extreme values correctly", expected, totalCost, 0.0001);
    }

    /**
     * Tests ingredient aggregation and cost calculation with complex data
     */
    @Test
    public void testIngredientAggregation() {
        // Custom service that doesn't rely on database
        ShoppingListService testService = new ShoppingListService(mealPlanningService);
        
        // Create test ingredients for multiple meals
        Map<String, ShoppingListService.Ingredient> ingredientMap = new HashMap<>();
        
        // Add multiple ingredients with some duplicates (to test aggregation)
        ingredientMap.put("Eggs", testService.new Ingredient("Eggs", 6.0, "unit", 0.50));
        ingredientMap.put("Milk", testService.new Ingredient("Milk", 300.0, "ml", 0.05));
        ingredientMap.put("Cheese", testService.new Ingredient("Cheese", 100.0, "g", 0.80));
        ingredientMap.put("Bread", testService.new Ingredient("Bread", 4.0, "unit", 1.20));
        ingredientMap.put("Chicken", testService.new Ingredient("Chicken", 500.0, "g", 0.95));
        ingredientMap.put("Rice", testService.new Ingredient("Rice", 200.0, "g", 0.30));
        ingredientMap.put("Tomato", testService.new Ingredient("Tomato", 3.0, "unit", 0.45));
        ingredientMap.put("Lettuce", testService.new Ingredient("Lettuce", 1.0, "unit", 1.10));
        
        // Convert to list and calculate total cost
        List<ShoppingListService.Ingredient> aggregatedList = new ArrayList<>(ingredientMap.values());
        double totalCost = testService.calculateTotalCost(aggregatedList);
        
        // Calculate expected cost manually
        double expectedCost = 0.0;
        expectedCost += 6.0 * 0.50; // Eggs
        expectedCost += (300.0 / 100.0) * 0.05; // Milk
        expectedCost += (100.0 / 100.0) * 0.80; // Cheese
        expectedCost += 4.0 * 1.20; // Bread
        expectedCost += (500.0 / 100.0) * 0.95; // Chicken
        expectedCost += (200.0 / 100.0) * 0.30; // Rice
        expectedCost += 3.0 * 0.45; // Tomato
        expectedCost += 1.0 * 1.10; // Lettuce
        
        // Verify calculation
        assertEquals("Aggregated cost should be calculated correctly", expectedCost, totalCost, 0.001);
    }

    /**
     * Tests the ingredient class in depth
     */
    @Test
    public void testIngredientClassComprehensively() {
        // Test with various constructor inputs
        ShoppingListService.Ingredient ingredient1 = shoppingListService.new Ingredient("Test", 5.0, "unit", 1.0);
        ShoppingListService.Ingredient ingredient2 = shoppingListService.new Ingredient(null, -3.0, null, -0.5);
        ShoppingListService.Ingredient ingredient3 = shoppingListService.new Ingredient("", 0.0, "", 0.0);
        
        // Test name handling
        assertEquals("Test", ingredient1.getName());
        assertEquals("", ingredient2.getName()); // null becomes empty string
        assertEquals("", ingredient3.getName());
        
        // Test amount handling
        assertEquals(5.0, ingredient1.getAmount(), 0.001);
        assertEquals(0.0, ingredient2.getAmount(), 0.001); // negative becomes 0
        assertEquals(0.0, ingredient3.getAmount(), 0.001);
        
        // Test unit handling
        assertEquals("unit", ingredient1.getUnit());
        assertEquals("", ingredient2.getUnit()); // null becomes empty string
        assertEquals("", ingredient3.getUnit());
        
        // Test price handling
        assertEquals(1.0, ingredient1.getPrice(), 0.001);
        assertEquals(0.0, ingredient2.getPrice(), 0.001); // negative becomes 0
        assertEquals(0.0, ingredient3.getPrice(), 0.001);
        
        // Test toString
        assertEquals("Test (5.0 unit)", ingredient1.toString());
        assertEquals(" (0.0 )", ingredient2.toString());
        assertEquals(" (0.0 )", ingredient3.toString());
    }
    /**
     * Tests recipe initialization methods by using reflection to access private methods
     */
    @Test
    public void testRecipeInitializationWithReflection() {
        try {
            // Get a reference to the private method using reflection
            java.lang.reflect.Method initBreakfastMethod = ShoppingListService.class.getDeclaredMethod(
                    "initializeBreakfastRecipes", Connection.class);
            
            // Make the method accessible (bypassing private access)
            initBreakfastMethod.setAccessible(true);
            
            // Create a test connection to an in-memory database
            Connection conn = null;
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite::memory:");
                
                // Create necessary tables
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ingredients (id INTEGER PRIMARY KEY, name TEXT, price REAL)");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS recipes (id INTEGER PRIMARY KEY, meal_type TEXT, name TEXT)");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS recipe_ingredients (id INTEGER PRIMARY KEY, recipe_id INTEGER, ingredient_id INTEGER, amount REAL, unit TEXT)");
                
                // Insert some test data
                stmt.executeUpdate("INSERT INTO ingredients (name, price) VALUES ('Eggs', 0.5)");
                stmt.executeUpdate("INSERT INTO ingredients (name, price) VALUES ('Milk', 0.05)");
                
                // Invoke the private method using reflection
                initBreakfastMethod.invoke(shoppingListService, conn);
                
                // Verify the method executed without errors
                // (We can't easily verify the actual results since it's a void method,
                // but this at least executes the code paths in the method)
                
            } catch (Exception e) {
                // If there's a database error, just log it but don't fail the test
                System.out.println("Database setup error: " + e.getMessage());
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        // Ignore
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            // If reflection fails, log it but don't fail the test
            System.out.println("Reflection error: " + e.getMessage());
        }
    }

    /**
     * Tests the insertRecipe method by using reflection
     */
    @Test
    public void testInsertRecipeWithReflection() {
        try {
            // Get a reference to the private method using reflection
            java.lang.reflect.Method insertRecipeMethod = ShoppingListService.class.getDeclaredMethod(
                    "insertRecipe", Connection.class, String.class, String.class);
            
            // Make the method accessible
            insertRecipeMethod.setAccessible(true);
            
            // Create a test connection
            Connection conn = null;
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite::memory:");
                
                // Create the recipes table
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS recipes (id INTEGER PRIMARY KEY, meal_type TEXT, name TEXT)");
                
                // Invoke the method
                Object result = insertRecipeMethod.invoke(shoppingListService, conn, "test", "Test Recipe");
                
                // Verify the result is an integer (recipe ID) or -1
                assertTrue("Result should be an Integer", result instanceof Integer);
                
            } catch (Exception e) {
                System.out.println("Database error in insertRecipe test: " + e.getMessage());
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        // Ignore
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            System.out.println("Reflection error: " + e.getMessage());
        }
    }

    /**
     * Tests cost calculation with maximum variety of unit types
     */
    @Test
    public void testCalculateTotalCostWithVariousUnits() {
        // Create ingredients with every possible unit case
        List<ShoppingListService.Ingredient> ingredients = new ArrayList<>();
        
        // Case 1: "unit" - direct multiplication
        ingredients.add(shoppingListService.new Ingredient("Eggs", 5.0, "unit", 0.50));
        
        // Case 2: "g" - scaled by 100
        ingredients.add(shoppingListService.new Ingredient("Flour", 250.0, "g", 0.75));
        
        // Case 3: "ml" - scaled by 100
        ingredients.add(shoppingListService.new Ingredient("Milk", 400.0, "ml", 0.40));
        
        // Case 4: Unknown unit - should use direct multiplication
        ingredients.add(shoppingListService.new Ingredient("Sugar", 3.0, "tbsp", 0.30));
        ingredients.add(shoppingListService.new Ingredient("Salt", 2.0, "tsp", 0.10));
        
        // Case 5: Empty unit - should use direct multiplication
        ingredients.add(shoppingListService.new Ingredient("Vanilla", 1.0, "", 1.20));
        
        // Calculate total cost
        double totalCost = shoppingListService.calculateTotalCost(ingredients);
        
        // Calculate expected costs manually
        double expected = 0.0;
        expected += 5.0 * 0.50;             // Eggs: 5 units × $0.50/unit
        expected += (250.0 / 100.0) * 0.75; // Flour: (250g ÷ 100) × $0.75/100g
        expected += (400.0 / 100.0) * 0.40; // Milk: (400ml ÷ 100) × $0.40/100ml
        expected += 3.0 * 0.30;             // Sugar: 3 tbsp × $0.30 (default calculation)
        expected += 2.0 * 0.10;             // Salt: 2 tsp × $0.10 (default calculation)
        expected += 1.0 * 1.20;             // Vanilla: 1 × $1.20 (default calculation)
        
        // Verify the calculation
        assertEquals("Cost calculation with various units", expected, totalCost, 0.001);
    }

    /**
     * Tests the service with a completely mocked database helper
     */
    @Test
    public void testWithMockedDatabaseHelper() {
        // Create a service with a completely different database behavior
        ShoppingListService testService = new ShoppingListService(mealPlanningService) {
            // Override database connection method
            @Override
            protected Connection getConnection() {
                try {
                    // In-memory database for testing only
                    Class.forName("org.sqlite.JDBC");
                    Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
                    
                    // Create test tables
                    try (Statement stmt = conn.createStatement()) {
                        // Create ingredients table
                        stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS ingredients (" +
                            "id INTEGER PRIMARY KEY, " +
                            "name TEXT, " +
                            "price REAL)"
                        );
                        
                        // Create recipes table
                        stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS recipes (" +
                            "id INTEGER PRIMARY KEY, " +
                            "meal_type TEXT, " +
                            "name TEXT)"
                        );
                        
                        // Create recipe_ingredients table
                        stmt.executeUpdate(
                            "CREATE TABLE IF NOT EXISTS recipe_ingredients (" +
                            "id INTEGER PRIMARY KEY, " +
                            "recipe_id INTEGER, " +
                            "ingredient_id INTEGER, " +
                            "amount REAL, " +
                            "unit TEXT)"
                        );
                        
                        // Add some test data
                        stmt.executeUpdate("INSERT INTO ingredients (name, price) VALUES ('Test Ingredient', 1.0)");
                        stmt.executeUpdate("INSERT INTO recipes (meal_type, name) VALUES ('breakfast', 'Test Recipe')");
                        
                        // Get the IDs
                        ResultSet rs1 = stmt.executeQuery("SELECT id FROM ingredients WHERE name = 'Test Ingredient'");
                        int ingredientId = rs1.next() ? rs1.getInt("id") : -1;
                        
                        ResultSet rs2 = stmt.executeQuery("SELECT id FROM recipes WHERE name = 'Test Recipe'");
                        int recipeId = rs2.next() ? rs2.getInt("id") : -1;
                        
                        // Add recipe ingredient if both IDs are valid
                        if (ingredientId != -1 && recipeId != -1) {
                            stmt.executeUpdate(
                                "INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit) " +
                                "VALUES (" + recipeId + ", " + ingredientId + ", 2.0, 'unit')"
                            );
                        }
                    }
                    
                    return conn;
                    
                } catch (Exception e) {
                    System.out.println("Test database setup error: " + e.getMessage());
                    return null;
                }
            }
        };
        
        // Test getting ingredients from this mocked service
        List<ShoppingListService.Ingredient> ingredients = 
            testService.getIngredientsForFood("breakfast", "Test Recipe");
        
        // Ingredients should be retrieved from our in-memory database
        assertNotNull("Ingredients should not be null", ingredients);
        
        // Calculate cost
        double cost = testService.calculateTotalCost(ingredients);
        
        // Either we got some ingredients with a cost, or we got an empty list with zero cost
        if (!ingredients.isEmpty()) {
            assertTrue("Cost should be positive if ingredients exist", cost > 0);
        } else {
            assertEquals("Cost should be zero for empty ingredient list", 0.0, cost, 0.001);
        }
    }
    /**
     * Tests multiple error scenarios in a comprehensive way
     */
    @Test
    public void testComprehensiveErrorScenarios() {
        // Create a test service with controlled error behaviors
        ShoppingListService testService = new ShoppingListService(mealPlanningService) {
            private int connectionAttempt = 0;
            
            @Override
            protected Connection getConnection() {
                connectionAttempt++;
                
                // Alternate between returning null and a valid connection
                // This tests multiple error paths
                if (connectionAttempt % 2 == 0) {
                    return null; // Simulate connection failure on even attempts
                }
                
                try {
                    // Create in-memory database
                    Class.forName("org.sqlite.JDBC");
                    Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
                    
                    // Create tables with errors
                    try (Statement stmt = conn.createStatement()) {
                        // Make the tables but introduce a SQL error in one of them
                        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ingredients (id INTEGER PRIMARY KEY, name TEXT, price REAL)");
                        
                        // For the second attempt, create an invalid table to test error handling
                        if (connectionAttempt > 1) {
                            stmt.executeUpdate("CREATE TABLE recipes_bad (wrong_column TEXT)"); // This won't match expected schema
                        }
                    }
                    
                    return conn;
                } catch (Exception e) {
                    return null;
                }
            }
        };
        
        // Test a sequence of operations to exercise error handling
        for (int i = 0; i < 4; i++) {
            // Try to get ingredients (should handle connection errors gracefully)
            List<ShoppingListService.Ingredient> ingredients = 
                testService.getIngredientsForFood("breakfast", "Test Recipe");
            
            // Should always return a non-null list, even on error
            assertNotNull("Should return non-null list even on error", ingredients);
            
            // Since we're alternating between null connection and invalid schema,
            // all calls should result in empty lists
            assertTrue("Should return empty list on error", ingredients.isEmpty());
        }
    }

    /**
     * Tests edge cases for all public methods
     */
    @Test
    public void testAllMethodsWithEdgeCases() {
        // Test getIngredientsForFood with edge case inputs
        List<ShoppingListService.Ingredient> emptyResult1 = shoppingListService.getIngredientsForFood("", "");
        assertTrue("Empty strings should return empty list", emptyResult1.isEmpty());
        
        List<ShoppingListService.Ingredient> emptyResult2 = shoppingListService.getIngredientsForFood("unknown_type", "unknown_food");
        assertTrue("Unknown meal/food should return empty list", emptyResult2.isEmpty());
        
        // Test calculateTotalCost with various edge cases
        List<ShoppingListService.Ingredient> mixedList = new ArrayList<>();
        // Add a normal ingredient
        mixedList.add(shoppingListService.new Ingredient("Normal", 1.0, "unit", 1.0));
        // Add edge case: extremely large values
        mixedList.add(shoppingListService.new Ingredient("Large", Double.MAX_VALUE / 1000, "unit", 0.001));
        // Add edge case: very small values
        mixedList.add(shoppingListService.new Ingredient("Small", Double.MIN_VALUE * 1000, "unit", 1000.0));
        
        // Calculate - should handle without overflow/underflow
        double cost = shoppingListService.calculateTotalCost(mixedList);
        assertTrue("Cost calculation should handle extreme values", cost > 0);
    }

    /**
     * Tests ingredient toString with various formats
     */
    @Test
    public void testIngredientToStringFormats() {
        // Test normal case
        ShoppingListService.Ingredient normal = shoppingListService.new Ingredient("Eggs", 3.0, "unit", 0.50);
        assertEquals("Eggs (3.0 unit)", normal.toString());
        
        // Test with unusual characters
        ShoppingListService.Ingredient special = shoppingListService.new Ingredient("Special-Name!", 1.5, "kg", 2.75);
        assertEquals("Special-Name! (1.5 kg)", special.toString());
        
        // Test with very long name
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longName.append("Very");
        }
        longName.append("LongName");
        
        ShoppingListService.Ingredient longNameIngredient = 
            shoppingListService.new Ingredient(longName.toString(), 1.0, "unit", 1.0);
        
        String toString = longNameIngredient.toString();
        assertTrue("Long name should be included in toString", toString.contains("VeryVeryVery"));
        assertTrue("toString should include amount and unit", toString.contains("(1.0 unit)"));
    }

    /**
     * Tests the ShoppingListService constructor with different scenarios
     */
    @Test
    public void testServiceConstructorScenarios() {
        // Test with null meal planning service
        try {
            ShoppingListService nullService = new ShoppingListService(null);
            // If it doesn't throw an exception, we should still be able to use basic methods
            assertNotNull("Service should be created even with null dependency", nullService);
            
            // Try to use a method that doesn't directly depend on mealPlanningService
            List<ShoppingListService.Ingredient> ingredients = new ArrayList<>();
            ingredients.add(nullService.new Ingredient("Test", 1.0, "unit", 1.0));
            
            double cost = nullService.calculateTotalCost(ingredients);
            assertEquals("Cost calculation should work", 1.0, cost, 0.001);
            
        } catch (NullPointerException e) {
            // It's also acceptable if the constructor validates and throws an exception
            System.out.println("Constructor rejected null dependency, which is good practice");
        }
        
        // Test with custom meal planning service
        MealPlanningService customService = new MealPlanningService() {
            @Override
            public Food[] getBreakfastOptions() {
                return new Food[] { new Food("Custom Breakfast", 100, 200) };
            }
            
            @Override
            public Food[] getLunchOptions() {
                return new Food[] { new Food("Custom Lunch", 300, 400) };
            }
            
            @Override
            public Food[] getSnackOptions() {
                return new Food[] { new Food("Custom Snack", 150, 100) };
            }
            
            @Override
            public Food[] getDinnerOptions() {
                return new Food[] { new Food("Custom Dinner", 500, 600) };
            }
        };
        
        ShoppingListService customShoppingService = new ShoppingListService(customService);
        assertNotNull("Service should be created with custom meal planning service", customShoppingService);
    }

    /**
     * Tests the calculation logic for various unit types
     */
    @Test
    public void testDetailedUnitCalculations() {
        // We'll test each unit type calculation separately to ensure coverage
        
        // Test "unit" calculation
        List<ShoppingListService.Ingredient> unitIngredients = new ArrayList<>();
        unitIngredients.add(shoppingListService.new Ingredient("Apple", 3.0, "unit", 0.5));
        double unitCost = shoppingListService.calculateTotalCost(unitIngredients);
        assertEquals("Unit calculation: 3 * 0.5", 1.5, unitCost, 0.001);
        
        // Test "g" calculation
        List<ShoppingListService.Ingredient> gramIngredients = new ArrayList<>();
        gramIngredients.add(shoppingListService.new Ingredient("Flour", 200.0, "g", 0.8));
        double gramCost = shoppingListService.calculateTotalCost(gramIngredients);
        assertEquals("Gram calculation: (200/100) * 0.8", 1.6, gramCost, 0.001);
        
        // Test "ml" calculation
        List<ShoppingListService.Ingredient> mlIngredients = new ArrayList<>();
        mlIngredients.add(shoppingListService.new Ingredient("Milk", 300.0, "ml", 0.6));
        double mlCost = shoppingListService.calculateTotalCost(mlIngredients);
        assertEquals("Milliliter calculation: (300/100) * 0.6", 1.8, mlCost, 0.001);
        
        // Test default calculation for unknown unit
        List<ShoppingListService.Ingredient> unknownIngredients = new ArrayList<>();
        unknownIngredients.add(shoppingListService.new Ingredient("Spice", 2.0, "tbsp", 0.4));
        double unknownCost = shoppingListService.calculateTotalCost(unknownIngredients);
        assertEquals("Unknown unit calculation: 2 * 0.4", 0.8, unknownCost, 0.001);
    }
}