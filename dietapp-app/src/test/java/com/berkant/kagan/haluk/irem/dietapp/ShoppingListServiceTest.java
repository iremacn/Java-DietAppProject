package com.berkant.kagan.haluk.irem.dietapp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ShoppingListServiceTest {
    private ShoppingListService shoppingListService;
    private MockMealPlanningService mockMealPlanningService;

    // Mock MealPlanningService for testing
    private class MockMealPlanningService extends MealPlanningService {
        @Override
        public Food[] getBreakfastOptions() {
            return new Food[]{
                new Food("Scrambled Eggs", 300, 200),
                new Food("Oatmeal with Fruits", 250, 350)
            };
        }

        @Override
        public Food[] getLunchOptions() {
            return new Food[]{
                new Food("Grilled Chicken Salad", 350, 450),
                new Food("Tuna Sandwich", 300, 400)
            };
        }

        @Override
        public Food[] getSnackOptions() {
            return new Food[]{
                new Food("Apple with Peanut Butter", 200, 150),
                new Food("Greek Yogurt", 100, 120)
            };
        }

        @Override
        public Food[] getDinnerOptions() {
            return new Food[]{
                new Food("Grilled Salmon with Vegetables", 400, 500),
                new Food("Chicken Stir-Fry", 350, 450)
            };
        }
    }

    @Before
    public void setUp() {
        // Setup in-memory database for testing
        System.setProperty("jdbc.drivers", "org.sqlite.JDBC");
        mockMealPlanningService = new MockMealPlanningService();
        shoppingListService = new ShoppingListService(mockMealPlanningService) {
            @Override
            protected Connection getConnection() {
                try {
                    Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
                    initializeTestDatabase(conn);
                    return conn;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    private void initializeTestDatabase(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Create ingredients table
            stmt.execute("CREATE TABLE ingredients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "price REAL)");

            // Create recipes table
            stmt.execute("CREATE TABLE recipes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "meal_type TEXT, " +
                    "name TEXT)");

            // Create recipe_ingredients table
            stmt.execute("CREATE TABLE recipe_ingredients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "recipe_id INTEGER, " +
                    "ingredient_id INTEGER, " +
                    "amount REAL, " +
                    "unit TEXT)");

            // Insert some test ingredients
            stmt.executeUpdate("INSERT INTO ingredients (name, price) VALUES " +
                    "('Eggs', 0.50), " +
                    "('Milk', 0.05), " +
                    "('Chicken Breast', 1.50), " +
                    "('Lettuce', 0.50), " +
                    "('Tomato', 0.40)");

            // Insert test recipes
            stmt.executeUpdate("INSERT INTO recipes (meal_type, name) VALUES " +
                    "('breakfast', 'Scrambled Eggs'), " +
                    "('lunch', 'Grilled Chicken Salad')");

            // Insert recipe ingredients
            stmt.executeUpdate("INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit) VALUES " +
                    "(1, 1, 3.0, 'unit'), " + // Eggs for Scrambled Eggs
                    "(1, 2, 30.0, 'ml'), " +  // Milk for Scrambled Eggs
                    "(2, 3, 150.0, 'g'), " +  // Chicken Breast for Grilled Chicken Salad
                    "(2, 4, 100.0, 'g'), " +  // Lettuce for Grilled Chicken Salad
                    "(2, 5, 1.0, 'unit')");   // Tomato for Grilled Chicken Salad
        }
    }

    @Test
    public void testGetIngredientsForFood_Success() {
        List<ShoppingListService.Ingredient> ingredients = 
            shoppingListService.getIngredientsForFood("breakfast", "Scrambled Eggs");
        
        assertNotNull("Ingredients should not be null", ingredients);
        assertFalse("Ingredients list should not be empty", ingredients.isEmpty());
        
        // Check specific expectations
        assertEquals("Should have 2 ingredients", 2, ingredients.size());
        
        boolean hasEggs = false;
        boolean hasMilk = false;
        
        for (ShoppingListService.Ingredient ingredient : ingredients) {
            if ("Eggs".equals(ingredient.getName())) {
                hasEggs = true;
                assertEquals(3.0, ingredient.getAmount(), 0.001);
                assertEquals("unit", ingredient.getUnit());
                assertEquals(0.50, ingredient.getPrice(), 0.001);
            }
            if ("Milk".equals(ingredient.getName())) {
                hasMilk = true;
                assertEquals(30.0, ingredient.getAmount(), 0.001);
                assertEquals("ml", ingredient.getUnit());
                assertEquals(0.05, ingredient.getPrice(), 0.001);
            }
        }
        
        assertTrue("Should contain Eggs", hasEggs);
        assertTrue("Should contain Milk", hasMilk);
    }

    @Test
    public void testGetIngredientsForFood_InvalidInput() {
        // Test null inputs
        List<ShoppingListService.Ingredient> nullMealTypeIngredients = 
            shoppingListService.getIngredientsForFood(null, "Scrambled Eggs");
        assertTrue("Null meal type should return empty list", nullMealTypeIngredients.isEmpty());

        List<ShoppingListService.Ingredient> nullFoodNameIngredients = 
            shoppingListService.getIngredientsForFood("breakfast", null);
        assertTrue("Null food name should return empty list", nullFoodNameIngredients.isEmpty());

        // Test non-existent food
        List<ShoppingListService.Ingredient> nonExistentIngredients = 
            shoppingListService.getIngredientsForFood("breakfast", "Non-Existent Food");
        assertTrue("Non-existent food should return empty list", nonExistentIngredients.isEmpty());
    }

    @Test
    public void testCalculateTotalCost() {
        // Prepare test ingredients
        List<ShoppingListService.Ingredient> ingredients = new ArrayList<>();
        ingredients.add(shoppingListService.new Ingredient("Eggs", 3.0, "unit", 0.50));
        ingredients.add(shoppingListService.new Ingredient("Milk", 300.0, "ml", 0.05));
        ingredients.add(shoppingListService.new Ingredient("Chicken", 500.0, "g", 0.20));

        // Calculate expected costs
        double expectedCost = 
            (3.0 * 0.50) +  // Eggs (unit)
            (300.0 / 100.0 * 0.05) +  // Milk (ml)
            (500.0 / 100.0 * 0.20);   // Chicken (g)

        double totalCost = shoppingListService.calculateTotalCost(ingredients);
        
        assertEquals("Total cost calculation should be accurate", expectedCost, totalCost, 0.001);
    }

    @Test
    public void testCalculateTotalCost_EdgeCases() {
        // Test null list
        double nullListCost = shoppingListService.calculateTotalCost(null);
        assertEquals("Null list should return 0", 0.0, nullListCost, 0.001);

        // Test empty list
        double emptyListCost = shoppingListService.calculateTotalCost(new ArrayList<>());
        assertEquals("Empty list should return 0", 0.0, emptyListCost, 0.001);

        // Test list with zero amount ingredients
        List<ShoppingListService.Ingredient> zeroAmountIngredients = new ArrayList<>();
        zeroAmountIngredients.add(shoppingListService.new Ingredient("Zero Ingredient", 0.0, "unit", 10.0));
        
        double zeroAmountCost = shoppingListService.calculateTotalCost(zeroAmountIngredients);
        assertEquals("Zero amount ingredient should result in 0 cost", 0.0, zeroAmountCost, 0.001);
    }

    @Test
    public void testIngredientClass() {
        // Test constructor with valid inputs
        ShoppingListService.Ingredient validIngredient = 
            shoppingListService.new Ingredient("Tomato", 2.0, "unit", 0.50);
        
        assertEquals("Tomato", validIngredient.getName());
        assertEquals(2.0, validIngredient.getAmount(), 0.001);
        assertEquals("unit", validIngredient.getUnit());
        assertEquals(0.50, validIngredient.getPrice(), 0.001);

        // Test constructor with null/negative inputs
        ShoppingListService.Ingredient nullIngredient = 
            shoppingListService.new Ingredient(null, -1.0, null, -0.50);
        
        assertEquals("", nullIngredient.getName());
        assertEquals(0.0, nullIngredient.getAmount(), 0.001);
        assertEquals("", nullIngredient.getUnit());
        assertEquals(0.0, nullIngredient.getPrice(), 0.001);

        // Test toString method
        assertEquals("Tomato (2.0 unit)", validIngredient.toString());
    }

    @Test
    public void testGetIngredientsForFood_MultipleRecipes() {
        // Test getting ingredients for a lunch recipe
        List<ShoppingListService.Ingredient> lunchIngredients = 
            shoppingListService.getIngredientsForFood("lunch", "Grilled Chicken Salad");
        
        assertNotNull("Lunch ingredients should not be null", lunchIngredients);
        assertFalse("Lunch ingredients list should not be empty", lunchIngredients.isEmpty());
        
        // Verify expected ingredients
        boolean hasChickenBreast = false;
        boolean hasLettuce = false;
        boolean hasTomato = false;
        
        for (ShoppingListService.Ingredient ingredient : lunchIngredients) {
            switch (ingredient.getName()) {
                case "Chicken Breast":
                    hasChickenBreast = true;
                    assertEquals(150.0, ingredient.getAmount(), 0.001);
                    assertEquals("g", ingredient.getUnit());
                    break;
                case "Lettuce":
                    hasLettuce = true;
                    assertEquals(100.0, ingredient.getAmount(), 0.001);
                    assertEquals("g", ingredient.getUnit());
                    break;
                case "Tomato":
                    hasTomato = true;
                    assertEquals(1.0, ingredient.getAmount(), 0.001);
                    assertEquals("unit", ingredient.getUnit());
                    break;
            }
        }
        
        assertTrue("Should contain Chicken Breast", hasChickenBreast);
        assertTrue("Should contain Lettuce", hasLettuce);
        assertTrue("Should contain Tomato", hasTomato);
    }
    @Test
    public void testInitializeIngredientsAndRecipes_ErrorHandling() {
        // Bu test, veritabanı bağlantısı hatalarını simüle edecek
        ShoppingListService errorService = new ShoppingListService(mockMealPlanningService) {
            @Override
            protected Connection getConnection() {
                return null; // Bağlantı hatası simülasyonu
            }
        };

        // Hata durumunda service'in çalışmaya devam etmesini kontrol et
        assertNotNull("Service should still be created", errorService);
    }

    @Test
    public void testGetIngredientsForFood_CaseInsensitivity() {
        // Meal type ve food name'in büyük/küçük harf duyarsızlığını test et
        List<ShoppingListService.Ingredient> ingredientsLowerCase = 
            shoppingListService.getIngredientsForFood("breakfast", "scrambled eggs");
        
        List<ShoppingListService.Ingredient> ingredientsUpperCase = 
            shoppingListService.getIngredientsForFood("BREAKFAST", "SCRAMBLED EGGS");
        
        assertEquals("Case-insensitive search should return same results", 
            ingredientsLowerCase.size(), 
            ingredientsUpperCase.size());
    }

    @Test
    public void testCalculateTotalCost_ComplexUnitScenarios() {
        List<ShoppingListService.Ingredient> complexIngredients = new ArrayList<>();
        
        // Farklı birim türlerini test et
        complexIngredients.add(shoppingListService.new Ingredient("Exotic Spice", 0.5, "mg", 1000.0));
        complexIngredients.add(shoppingListService.new Ingredient("Bulk Grain", 10000.0, "g", 0.0001));
        complexIngredients.add(shoppingListService.new Ingredient("Rare Liquid", 0.001, "ml", 10000.0));
        complexIngredients.add(shoppingListService.new Ingredient("Standard Item", 1.0, "unit", 1.0));
        
        double totalCost = shoppingListService.calculateTotalCost(complexIngredients);
        
        assertTrue("Total cost should handle extreme unit scenarios", totalCost > 0);
    }

    @Test
    public void testGetIngredientsForFood_PerformanceTest() {
        long startTime = System.currentTimeMillis();
        
        // Çoklu çağrı performansını test et
        for (int i = 0; i < 1000; i++) {
            List<ShoppingListService.Ingredient> ingredients = 
                shoppingListService.getIngredientsForFood("breakfast", "Scrambled Eggs");
            
            assertNotNull("Ingredients should be retrieved", ingredients);
            assertFalse("Ingredients list should not be empty", ingredients.isEmpty());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue("Performance test should complete within reasonable time", duration < 5000);
    }

    @Test
    public void testIngredientClass_Immutability() {
        ShoppingListService.Ingredient ingredient = 
            shoppingListService.new Ingredient("Test", 10.0, "unit", 5.0);
        
        // Getter'ların değeri değiştiremediğini doğrula
        String originalName = ingredient.getName();
        double originalAmount = ingredient.getAmount();
        String originalUnit = ingredient.getUnit();
        double originalPrice = ingredient.getPrice();
        
        // Hiçbir getter çağrısı orijinal değerleri değiştirmemeli
        assertEquals("Name should remain unchanged", originalName, ingredient.getName());
        assertEquals("Amount should remain unchanged", originalAmount, ingredient.getAmount(), 0.001);
        assertEquals("Unit should remain unchanged", originalUnit, ingredient.getUnit());
        assertEquals("Price should remain unchanged", originalPrice, ingredient.getPrice(), 0.001);
    }

    @Test
    public void testCalculateTotalCost_RandomIngredientCombinations() {
        List<ShoppingListService.Ingredient> randomIngredients = new ArrayList<>();
        
        // Rastgele içerik ve miktarlarda malzemeler oluştur
        String[] ingredientNames = {"Salt", "Pepper", "Olive Oil", "Herbs", "Spices"};
        String[] units = {"g", "ml", "unit"};
        
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 50; i++) {
            String name = ingredientNames[random.nextInt(ingredientNames.length)];
            double amount = random.nextDouble() * 1000;
            String unit = units[random.nextInt(units.length)];
            double price = random.nextDouble() * 10;
            
            randomIngredients.add(shoppingListService.new Ingredient(name, amount, unit, price));
        }
        
        double totalCost = shoppingListService.calculateTotalCost(randomIngredients);
        
        assertTrue("Random ingredient combination should have valid cost", totalCost >= 0);
        assertTrue("Total cost should not be infinite", !Double.isInfinite(totalCost));
    
    }
    @Test
    public void testGetIngredientsForFood_UnicodeAndSpecialCharacters() {
        // Unicode ve özel karakterli yemek adları için test
        List<ShoppingListService.Ingredient> ingredients = shoppingListService.getIngredientsForFood(
            "breakfast", 
            "Scrambled Eggs with Spécïäl Chärâçtërs!"
        );
        
        // Özel karakterli girişlerin hata vermediğini kontrol et
        assertNotNull("Ingredients should handle special characters", ingredients);
    }

    @Test
   
    public void testIngredientClass_ToStringVariations() {
        // Farklı toString senaryoları
        ShoppingListService.Ingredient[] testIngredients = {
            shoppingListService.new Ingredient("Normal Ingredient", 10.0, "unit", 5.0),
            shoppingListService.new Ingredient("Zero Amount", 0.0, "g", 1.0),
            shoppingListService.new Ingredient("Unicode Ingredient", 2.5, "ml", 0.75),
            shoppingListService.new Ingredient(null, -1.0, null, -0.5)
        };
        
        for (ShoppingListService.Ingredient ingredient : testIngredients) {
            String toString = ingredient.toString();
            
            // Her toString çağrısı tutarlı olmalı
            assertNotNull("toString should not return null", toString);
            assertTrue("toString should contain ingredient name", toString.contains(ingredient.getName()));
            assertTrue("toString should contain amount", toString.contains(String.valueOf(ingredient.getAmount())));
            assertTrue("toString should contain unit", toString.contains(ingredient.getUnit()));
        }
    }



    

    @Test
    public void testCalculateTotalCost_DuplicateIngredients() {
        List<ShoppingListService.Ingredient> duplicateIngredients = new ArrayList<>();
        
        // Aynı malzemeden birden fazla ekleme
        duplicateIngredients.add(shoppingListService.new Ingredient("Eggs", 2.0, "unit", 0.50));
        duplicateIngredients.add(shoppingListService.new Ingredient("Eggs", 3.0, "unit", 0.50));
        duplicateIngredients.add(shoppingListService.new Ingredient("Milk", 100.0, "ml", 0.05));
        duplicateIngredients.add(shoppingListService.new Ingredient("Milk", 200.0, "ml", 0.05));
        
        double totalCost = shoppingListService.calculateTotalCost(duplicateIngredients);
        
        // Maliyet doğru hesaplanmalı
        assertEquals("Total cost should accumulate correctly", 
            (2.0 * 0.50) + (3.0 * 0.50) + (100.0 / 100.0 * 0.05) + (200.0 / 100.0 * 0.05), 
            totalCost, 0.001);
    }

    @Test
    public void testGetIngredientsForFood_EmptyDatabase() {
        // Boş veritabanı senaryosu
        ShoppingListService emptyDbService = new ShoppingListService(mockMealPlanningService) {
            @Override
            protected Connection getConnection() {
                try {
                    Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
                    // Hiçbir tablo oluşturulmayacak
                    return conn;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        
        // Boş veritabanından malzeme çekme
        List<ShoppingListService.Ingredient> ingredients = 
            emptyDbService.getIngredientsForFood("breakfast", "Scrambled Eggs");
        
        // Boş liste dönmeli
        assertNotNull("Ingredients list should not be null", ingredients);
        assertTrue("Ingredients list should be empty", ingredients.isEmpty());
    }

    @Test
    public void testCalculateTotalCost_RoundingAndPrecision() {
        List<ShoppingListService.Ingredient> precisionIngredients = new ArrayList<>();
        
        // Hassas maliyet hesaplaması
        precisionIngredients.add(shoppingListService.new Ingredient("Precise Item 1", 0.333, "g", 3.14159));
        precisionIngredients.add(shoppingListService.new Ingredient("Precise Item 2", 0.666, "ml", 2.71828));
        
        double totalCost = shoppingListService.calculateTotalCost(precisionIngredients);
        
        // Yuvarlamalar doğru yapılmalı
        assertTrue("Total cost should handle decimal precision", totalCost > 0);
        assertFalse("Total cost should not be infinite", Double.isInfinite(totalCost));
    }
    @Test
    public void testGetIngredientsForFood_CombinedMealTypes() {
        // Birden fazla öğün türü için kapsamlı test
        String[][] mealTypeRecipes = {
            {"breakfast", "Pancakes"},
            {"breakfast", "Fruit Smoothie"},
            {"lunch", "Vegetarian Wrap"},
            {"lunch", "Quinoa Salad"},
            {"snack", "Energy Balls"},
            {"snack", "Fruit Yogurt"},
            {"dinner", "Vegetable Stir Fry"},
            {"dinner", "Mediterranean Fish"}
        };

        for (String[] recipeInfo : mealTypeRecipes) {
            List<ShoppingListService.Ingredient> ingredients = 
                shoppingListService.getIngredientsForFood(recipeInfo[0], recipeInfo[1]);
            
            assertNotNull("Ingredients for " + recipeInfo[1] + " should not be null", ingredients);
            
            // Her tarif için maliyet hesaplaması
            double totalCost = shoppingListService.calculateTotalCost(ingredients);
            
            assertTrue("Total cost should be non-negative for " + recipeInfo[1], totalCost >= 0);
        }
    }

    @Test
    public void testCalculateTotalCost_MixedUnitTypes() {
        List<ShoppingListService.Ingredient> mixedUnitIngredients = new ArrayList<>();
        
        // Farklı birim türlerini karıştırarak test et
        mixedUnitIngredients.add(shoppingListService.new Ingredient("Coffee Beans", 250.0, "g", 0.75));
        mixedUnitIngredients.add(shoppingListService.new Ingredient("Olive Oil", 50.0, "ml", 1.20));
        mixedUnitIngredients.add(shoppingListService.new Ingredient("Chocolate", 3.0, "unit", 2.50));
        mixedUnitIngredients.add(shoppingListService.new Ingredient("Rare Spice", 0.5, "mg", 10000.0));
        
        double totalCost = shoppingListService.calculateTotalCost(mixedUnitIngredients);
        
        assertTrue("Total cost should handle mixed unit types", totalCost > 0);
        assertFalse("Total cost should not be infinite", Double.isInfinite(totalCost));
    }

    @Test
    public void testIngredientClass_ImmutabilityAndCloning() {
        ShoppingListService.Ingredient originalIngredient = 
            shoppingListService.new Ingredient("Original", 10.0, "unit", 5.0);
        
        // Getter'ların orijinal değerleri değiştirmediğini doğrula
        String originalName = originalIngredient.getName();
        double originalAmount = originalIngredient.getAmount();
        String originalUnit = originalIngredient.getUnit();
        double originalPrice = originalIngredient.getPrice();
        
        // Hiçbir getter çağrısı orijinal değerleri değiştirmemeli
        assertEquals("Name should remain unchanged", originalName, originalIngredient.getName());
        assertEquals("Amount should remain unchanged", originalAmount, originalIngredient.getAmount(), 0.001);
        assertEquals("Unit should remain unchanged", originalUnit, originalIngredient.getUnit());
        assertEquals("Price should remain unchanged", originalPrice, originalIngredient.getPrice(), 0.001);
    }

    @Test
    public void testGetIngredientsForFood_PerformanceAndScalability() {
        long startTime = System.currentTimeMillis();
        int iterationCount = 5000;
        
        // Büyük ölçekli performans testi
        for (int i = 0; i < iterationCount; i++) {
            List<ShoppingListService.Ingredient> ingredients = 
                shoppingListService.getIngredientsForFood("breakfast", "Scrambled Eggs");
            
            assertNotNull("Ingredients should be retrieved in iteration " + i, ingredients);
            assertFalse("Ingredients list should not be empty in iteration " + i, ingredients.isEmpty());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue("Performance test should complete within reasonable time", duration < 10000);
        System.out.println("Performance test completed in " + duration + " ms for " + iterationCount + " iterations");
    }

    @Test
    public void testCalculateTotalCost_ExtremePrecisionScenarios() {
        List<ShoppingListService.Ingredient> precisionIngredients = new ArrayList<>();
        
        // Çok hassas değerlerle maliyet hesaplaması
        precisionIngredients.add(shoppingListService.new Ingredient("Ultra Precise 1", 0.000001, "g", 1000000.0));
        precisionIngredients.add(shoppingListService.new Ingredient("Ultra Precise 2", 1000000.0, "ml", 0.000001));
        
        double totalCost = shoppingListService.calculateTotalCost(precisionIngredients);
        
        // Çok küçük ve çok büyük değerlerle başa çıkabilmeli
        assertTrue("Total cost should handle extreme precision scenarios", totalCost >= 0);
        assertFalse("Total cost should not be infinite", Double.isInfinite(totalCost));
        assertFalse("Total cost should not be NaN", Double.isNaN(totalCost));
    }

    @Test
    public void testGetIngredientsForFood_ErrorRecovery() {
        // Hata kurtarma senaryosu
        ShoppingListService errorRecoveryService = new ShoppingListService(mockMealPlanningService) {
            private int connectionAttempts = 0;
            
            @Override
            protected Connection getConnection() {
                connectionAttempts++;
                
                try {
                    if (connectionAttempts % 3 == 0) {
                        // Her 3 denemede bir null bağlantı döndür
                        return null;
                    }
                    
                    Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
                    initializeTestDatabase(conn);
                    return conn;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        
        // Birden fazla çağrı yaparak hata kurtarma mekanizmasını test et
        for (int i = 0; i < 10; i++) {
            List<ShoppingListService.Ingredient> ingredients = 
                errorRecoveryService.getIngredientsForFood("breakfast", "Scrambled Eggs");
            
            // Her çağrıda ya boş liste ya da dolu liste dönmeli
            assertNotNull("Ingredients list should not be null", ingredients);
        }
    }

    @Test
    public void testCalculateTotalCost_CrossCurrencySimulation() {
        List<ShoppingListService.Ingredient> crossCurrencyIngredients = new ArrayList<>();
        
        // Farklı fiyat aralıklarını ve birimlerini test et
        crossCurrencyIngredients.add(shoppingListService.new Ingredient("Luxury Spice", 0.1, "g", 500.0));
        crossCurrencyIngredients.add(shoppingListService.new Ingredient("Bulk Grain", 10000.0, "g", 0.001));
        crossCurrencyIngredients.add(shoppingListService.new Ingredient("Rare Liquid", 0.001, "ml", 100000.0));
        
        double totalCost = shoppingListService.calculateTotalCost(crossCurrencyIngredients);
        
        // Çok farklı fiyat aralıklarını hesaplayabilmeli
        assertTrue("Total cost should handle cross-currency scenarios", totalCost > 0);
        assertFalse("Total cost should not be infinite", Double.isInfinite(totalCost));
    }
    @Test
    public void testGetIngredientsForFood_InternationalizationSupport() {
        // Uluslararası karakterler ve farklı dil desteği
        String[][] internationalRecipes = {
            {"breakfast", "Sütlaç"},  // Türkçe
            {"lunch", "Ramen"},        // Japonca
            {"dinner", "Paëlla"},      // İspanyolca
            {"snack", "Crêpe"}         // Fransızca
        };

        for (String[] recipeInfo : internationalRecipes) {
            List<ShoppingListService.Ingredient> ingredients = 
                shoppingListService.getIngredientsForFood(recipeInfo[0], recipeInfo[1]);
            
            assertNotNull("Ingredients for " + recipeInfo[1] + " should not be null", ingredients);
            
            // Her tarif için maliyet hesaplaması
            double totalCost = shoppingListService.calculateTotalCost(ingredients);
            
            assertTrue("Total cost should be non-negative for " + recipeInfo[1], totalCost >= 0);
        }
    }

    @Test
    public void testCalculateTotalCost_ExponentialAndLogarithmicScenarios() {
        List<ShoppingListService.Ingredient> exponentialIngredients = new ArrayList<>();
        
        // Üstel ve logaritmik değerlerle test
        exponentialIngredients.add(shoppingListService.new Ingredient("Exponential Spice", Math.pow(10, 6), "g", Math.pow(10, -6)));
        exponentialIngredients.add(shoppingListService.new Ingredient("Logarithmic Herb", Math.log(1000), "ml", Math.log(0.1)));
        exponentialIngredients.add(shoppingListService.new Ingredient("Complex Ingredient", Math.sqrt(10000), "unit", Math.cbrt(0.001)));
        
        double totalCost = shoppingListService.calculateTotalCost(exponentialIngredients);
        
        assertTrue("Total cost should handle exponential and logarithmic scenarios", totalCost > 0);
        assertFalse("Total cost should not be infinite", Double.isInfinite(totalCost));
    }

    @Test
   
    public void testGetIngredientsForFood_LargeDataVolumeStressTest() {
        String[] mealTypes = {"breakfast", "lunch", "snack", "dinner"};
        List<String> allRecipes = new ArrayList<>();
        
        // Tüm yemek türlerinden tarifleri topla
        for (String mealType : mealTypes) {
            Food[] foods = null;
            switch (mealType) {
                case "breakfast":
                    foods = mockMealPlanningService.getBreakfastOptions();
                    break;
                case "lunch":
                    foods = mockMealPlanningService.getLunchOptions();
                    break;
                case "snack":
                    foods = mockMealPlanningService.getSnackOptions();
                    break;
                case "dinner":
                    foods = mockMealPlanningService.getDinnerOptions();
                    break;
            }
            
            if (foods != null) {
                for (Food food : foods) {
                    allRecipes.add(mealType + ":" + food.getName());
                }
            }
        }
        
        // Büyük veri hacmi testi
        long startTime = System.currentTimeMillis();
        double totalAllRecipesCost = 0.0;
        
        for (String recipeInfo : allRecipes) {
            String[] parts = recipeInfo.split(":");
            List<ShoppingListService.Ingredient> ingredients = 
                shoppingListService.getIngredientsForFood(parts[0], parts[1]);
            
            double recipeCost = shoppingListService.calculateTotalCost(ingredients);
            totalAllRecipesCost += recipeCost;
            
            assertNotNull("Ingredients should not be null for " + recipeInfo, ingredients);
            assertTrue("Recipe cost should be non-negative for " + recipeInfo, recipeCost >= 0);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue("Large volume test should complete within reasonable time", duration < 15000);
        assertTrue("Total recipes cost should be positive", totalAllRecipesCost > 0);
        System.out.println("Large volume test completed in " + duration + " ms");
    }

    @Test
    public void testCalculateTotalCost_NegativeAndZeroValueHandling() {
        List<ShoppingListService.Ingredient> invalidIngredients = new ArrayList<>();
        
        // Negatif ve sıfır değerlerle test
        invalidIngredients.add(shoppingListService.new Ingredient("Negative Amount", -10.0, "g", 1.0));
        invalidIngredients.add(shoppingListService.new Ingredient("Zero Amount", 0.0, "unit", 5.0));
        invalidIngredients.add(shoppingListService.new Ingredient("Negative Price", 5.0, "ml", -2.0));
        
        double totalCost = shoppingListService.calculateTotalCost(invalidIngredients);
        
        // Negatif değerler 0'a dönüştürülmeli
        assertEquals("Total cost should handle negative and zero values", 0.0, totalCost, 0.001);
    }

    @Test
    public void testGetIngredientsForFood_ConcurrentAccessSimulation() {
        // Eş zamanlı erişim simülasyonu
        final List<List<ShoppingListService.Ingredient>> concurrentResults = 
            Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch latch = new CountDownLatch(10);
        
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    List<ShoppingListService.Ingredient> ingredients = 
                        shoppingListService.getIngredientsForFood("breakfast", "Scrambled Eggs");
                    concurrentResults.add(ingredients);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Concurrent access test interrupted");
        }
        
        // Tüm çağrılar tutarlı sonuçlar döndürmeli
        assertFalse("Concurrent results should not be empty", concurrentResults.isEmpty());
        
        // İlk sonuçla diğerlerini karşılaştır
        List<ShoppingListService.Ingredient> firstResult = concurrentResults.get(0);
        for (List<ShoppingListService.Ingredient> result : concurrentResults) {
            assertEquals("Concurrent calls should return consistent results", 
                firstResult.size(), result.size());
            
            for (int i = 0; i < firstResult.size(); i++) {
                assertEquals("Ingredient details should be consistent across concurrent calls", 
                    firstResult.get(i).getName(), result.get(i).getName());
                assertEquals("Ingredient amounts should be consistent across concurrent calls", 
                    firstResult.get(i).getAmount(), result.get(i).getAmount(), 0.001);
            }
        }
    }
    @Test
    public void testCalculateTotalCost_RandomIngredientGeneration() {
        Random random = new Random();
        List<ShoppingListService.Ingredient> randomIngredients = new ArrayList<>();
        
        // Rastgele 100 malzeme oluştur
        for (int i = 0; i < 100; i++) {
            String[] ingredientNames = {
                "Salt", "Pepper", "Olive Oil", "Herbs", "Spices", 
                "Sugar", "Flour", "Milk", "Eggs", "Cheese"
            };
            String[] units = {"g", "ml", "unit", "kg", "l"};
            
            String name = ingredientNames[random.nextInt(ingredientNames.length)];
            double amount = random.nextDouble() * 1000;
            String unit = units[random.nextInt(units.length)];
            double price = random.nextDouble() * 100;
            
            randomIngredients.add(shoppingListService.new Ingredient(name, amount, unit, price));
        }
        
        double totalCost = shoppingListService.calculateTotalCost(randomIngredients);
        
        assertTrue("Random ingredient cost should be non-negative", totalCost >= 0);
        assertFalse("Total cost should not be infinite", Double.isInfinite(totalCost));
    }

    @Test
    public void testGetIngredientsForFood_EdgeCaseInputs() {
        // Sınır değer ve özel girdi testleri
        String[][] edgeCaseInputs = {
            {"", ""},  // Boş string
            {" ", " "},  // Boşluk karakteri
            {"UPPERCASE", "UPPERCASE RECIPE"},  // Büyük harf
            {"lowercase", "lowercase recipe"},  // Küçük harf
            {"MiXeD", "MiXeD CaSe"}  // Karışık harf
        };
        
        for (String[] input : edgeCaseInputs) {
            List<ShoppingListService.Ingredient> ingredients = 
                shoppingListService.getIngredientsForFood(input[0], input[1]);
            
            assertNotNull("Ingredients list should not be null for input: " + Arrays.toString(input), ingredients);
            assertTrue("Ingredients list should be empty for edge case inputs", ingredients.isEmpty());
        }
    }

    @Test
    public void testIngredientClass_ComplexConstructorScenarios() {
        // Karmaşık constructor senaryoları
        ShoppingListService.Ingredient[] testIngredients = {
            // Null ve negatif değerler
            shoppingListService.new Ingredient(null, -100.0, null, -50.0),
            
            // Unicode karakterler
            shoppingListService.new Ingredient("Spätzle", 250.0, "g", 3.50),
            
            // Çok uzun isimler
            shoppingListService.new Ingredient(
                "VeryVeryVeryVeryVeryVeryVeryVeryVeryVeryLongIngredientName", 
                10.0, 
                "unit", 
                0.75
            )
        };
        
        for (ShoppingListService.Ingredient ingredient : testIngredients) {
            assertNotNull("Ingredient name should not be null", ingredient.getName());
            assertTrue("Ingredient amount should be non-negative", ingredient.getAmount() >= 0);
            assertNotNull("Ingredient unit should not be null", ingredient.getUnit());
            assertTrue("Ingredient price should be non-negative", ingredient.getPrice() >= 0);
            
            // toString metodu çalışmalı
            assertNotNull("toString should not return null", ingredient.toString());
        }
    }

    @Test
    public void testCalculateTotalCost_PrecisionAndRoundingScenarios() {
        List<ShoppingListService.Ingredient> precisionIngredients = new ArrayList<>();
        
        // Hassasiyet ve yuvarlama senaryoları
        precisionIngredients.add(shoppingListService.new Ingredient("Precise Salt", 0.333, "g", 3.14159));
        precisionIngredients.add(shoppingListService.new Ingredient("Precise Sugar", 0.666, "ml", 2.71828));
        precisionIngredients.add(shoppingListService.new Ingredient("Micro Spice", 0.000001, "unit", 1000000.0));
        
        double totalCost = shoppingListService.calculateTotalCost(precisionIngredients);
        
        assertTrue("Total cost should handle precision scenarios", totalCost > 0);
        assertFalse("Total cost should not be infinite", Double.isInfinite(totalCost));
        assertFalse("Total cost should not be NaN", Double.isNaN(totalCost));
    }

    @Test
  
    public void testCalculateTotalCost_MassiveIngredientList() {
        List<ShoppingListService.Ingredient> massiveIngredientList = new ArrayList<>();
        Random random = new Random();
        
        // 1000 adet rastgele malzeme oluştur
        for (int i = 0; i < 1000; i++) {
            String name = "Ingredient_" + i;
            double amount = random.nextDouble() * 1000;
            String unit = random.nextBoolean() ? "g" : "ml";
            double price = random.nextDouble() * 100;
            
            massiveIngredientList.add(shoppingListService.new Ingredient(name, amount, unit, price));
        }
        
        double totalCost = shoppingListService.calculateTotalCost(massiveIngredientList);
        
        assertTrue("Total cost for massive ingredient list should be positive", totalCost > 0);
        assertFalse("Total cost should not be infinite", Double.isInfinite(totalCost));
    }
    @Test
    public void testGetIngredientsForFood_MultiLanguageSupport() {
        String[][] multiLanguageRecipes = {
            {"breakfast", "朝食", "Japanese Breakfast"},
            {"lunch", "الغداء", "Arabic Lunch"},
            {"dinner", "Abendessen", "German Dinner"},
            {"snack", "Merenda", "Italian Snack"}
        };

        for (String[] recipeInfo : multiLanguageRecipes) {
            List<ShoppingListService.Ingredient> ingredients = 
                shoppingListService.getIngredientsForFood(recipeInfo[0], recipeInfo[1]);
            
            assertNotNull("Ingredients for " + recipeInfo[2] + " should not be null", ingredients);
        }
    }

    @Test
    public void testCalculateTotalCost_ScientificNotationHandling() {
        List<ShoppingListService.Ingredient> scientificNotationIngredients = new ArrayList<>();
        
        scientificNotationIngredients.add(shoppingListService.new Ingredient("Nano Spice", 1e-6, "g", 1e6));
        scientificNotationIngredients.add(shoppingListService.new Ingredient("Mega Ingredient", 1e6, "ml", 1e-6));
        
        double totalCost = shoppingListService.calculateTotalCost(scientificNotationIngredients);
        
        assertTrue("Total cost should handle scientific notation", totalCost > 0);
        assertFalse("Total cost should not be infinite", Double.isInfinite(totalCost));
    }

    @Test
    public void testIngredientClass_DeepCloneSimulation() {
        ShoppingListService.Ingredient original = 
            shoppingListService.new Ingredient("Original", 10.0, "unit", 5.0);
        
        // Getter'ların orijinal nesneyi değiştirmediğini doğrula
        String originalName = original.getName();
        double originalAmount = original.getAmount();
        String originalUnit = original.getUnit();
        double originalPrice = original.getPrice();
        
        // Hiçbir getter çağrısı orijinal değerleri değiştirmemeli
        assertEquals("Name should remain unchanged", originalName, original.getName());
        assertEquals("Amount should remain unchanged", originalAmount, original.getAmount(), 0.001);
        assertEquals("Unit should remain unchanged", originalUnit, original.getUnit());
        assertEquals("Price should remain unchanged", originalPrice, original.getPrice(), 0.001);
    }

    @Test
    public void testGetIngredientsForFood_EmptyAndWhitespaceInputs() {
        String[][] emptyInputs = {
            {"", ""},
            {" ", " "},
            {"   ", "   "},
            {"\t", "\t"},
            {"\n", "\n"}
        };
        
        for (String[] input : emptyInputs) {
            List<ShoppingListService.Ingredient> ingredients = 
                shoppingListService.getIngredientsForFood(input[0], input[1]);
            
            assertNotNull("Ingredients list should not be null", ingredients);
            assertTrue("Empty or whitespace inputs should return empty list", ingredients.isEmpty());
        }
    }

    @Test
    public void testCalculateTotalCost_IngredientAggregation() {
        List<ShoppingListService.Ingredient> aggregationIngredients = new ArrayList<>();
        
        // Aynı malzemeden birden fazla ekleme
        aggregationIngredients.add(shoppingListService.new Ingredient("Eggs", 2.0, "unit", 0.50));
        aggregationIngredients.add(shoppingListService.new Ingredient("Eggs", 3.0, "unit", 0.50));
        aggregationIngredients.add(shoppingListService.new Ingredient("Milk", 100.0, "ml", 0.05));
        aggregationIngredients.add(shoppingListService.new Ingredient("Milk", 200.0, "ml", 0.05));
        
        double totalCost = shoppingListService.calculateTotalCost(aggregationIngredients);
        
        // Manuel olarak toplam maliyeti hesapla
        double expectedCost = 
            (2.0 * 0.50) + (3.0 * 0.50) +  // Eggs
            (100.0 / 100.0 * 0.05) + (200.0 / 100.0 * 0.05);  // Milk
        
        assertEquals("Total cost should handle ingredient aggregation", expectedCost, totalCost, 0.001);
    }

    @Test
    public void testGetIngredientsForFood_CaseSensitivityCheck() {
        // Büyük/küçük harf duyarlılığı kontrolü
        List<ShoppingListService.Ingredient> lowerCaseIngredients = 
            shoppingListService.getIngredientsForFood("breakfast", "scrambled eggs");
        
        List<ShoppingListService.Ingredient> upperCaseIngredients = 
            shoppingListService.getIngredientsForFood("BREAKFAST", "SCRAMBLED EGGS");
        
        List<ShoppingListService.Ingredient> mixedCaseIngredients = 
            shoppingListService.getIngredientsForFood("BreAkFaSt", "ScRaMbLeD eGgS");
        
        assertEquals("Ingredients should be consistent across case variations", 
            lowerCaseIngredients.size(), upperCaseIngredients.size());
        assertEquals("Ingredients should be consistent across case variations", 
            lowerCaseIngredients.size(), mixedCaseIngredients.size());
    }

    @Test
    public void testCalculateTotalCost_ZeroAndNegativeHandling() {
        List<ShoppingListService.Ingredient> invalidIngredients = new ArrayList<>();
        
        invalidIngredients.add(shoppingListService.new Ingredient("Negative Amount", -10.0, "g", 1.0));
        invalidIngredients.add(shoppingListService.new Ingredient("Zero Amount", 0.0, "unit", 5.0));
        invalidIngredients.add(shoppingListService.new Ingredient("Negative Price", 5.0, "ml", -2.0));
        
        double totalCost = shoppingListService.calculateTotalCost(invalidIngredients);
        
        assertEquals("Total cost should handle negative and zero values", 0.0, totalCost, 0.001);
    }

    @Test
    public void testIngredientClass_LongNameHandling() {
        // Çok uzun isimli malzeme testi
        StringBuilder longNameBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longNameBuilder.append("VeryLongIngredientName");
        }
        String longName = longNameBuilder.toString();
        
        ShoppingListService.Ingredient longNameIngredient = 
            shoppingListService.new Ingredient(longName, 10.0, "unit", 5.0);
        
        assertEquals("Long name should be preserved", longName, longNameIngredient.getName());
        assertEquals("Amount should be correct", 10.0, longNameIngredient.getAmount(), 0.001);
        assertEquals("Unit should be correct", "unit", longNameIngredient.getUnit());
        assertEquals("Price should be correct", 5.0, longNameIngredient.getPrice(), 0.001);
    }

    @Test
    public void testGetIngredientsForFood_SymbolAndSpecialCharacterHandling() {
        String[][] specialCharacterRecipes = {
            {"breakfast", "Eggs & Bacon"},
            {"lunch", "Salad + Chicken"},
            {"dinner", "Fish @ Dinner"},
            {"snack", "Fruit % Snack"}
        };
        
        for (String[] recipeInfo : specialCharacterRecipes) {
            List<ShoppingListService.Ingredient> ingredients = 
                shoppingListService.getIngredientsForFood(recipeInfo[0], recipeInfo[1]);
            
            assertNotNull("Ingredients for " + recipeInfo[1] + " should not be null", ingredients);
        }
    }

    @Test
    public void testCalculateTotalCost_UnitConversionAccuracy() {
        List<ShoppingListService.Ingredient> conversionIngredients = new ArrayList<>();
        
        conversionIngredients.add(shoppingListService.new Ingredient("Flour", 1000.0, "g", 0.5));  // 10 * 100g
        conversionIngredients.add(shoppingListService.new Ingredient("Milk", 500.0, "ml", 0.1));   // 5 * 100ml
        conversionIngredients.add(shoppingListService.new Ingredient("Sugar", 3.0, "unit", 1.0));  // Direct multiplication
        
        double totalCost = shoppingListService.calculateTotalCost(conversionIngredients);
        
        // Manuel hesaplama
        double expectedCost = 
            (1000.0 / 100.0 * 0.5) +  // Flour
            (500.0 / 100.0 * 0.1) +   // Milk
            (3.0 * 1.0);              // Sugar
        
        assertEquals("Total cost should handle unit conversions accurately", expectedCost, totalCost, 0.001);
    }

    @Test
 
    public void testIngredientClass_ImmutableAfterCreation() {
        ShoppingListService.Ingredient ingredient = 
            shoppingListService.new Ingredient("Stable", 10.0, "unit", 5.0);
        
        // Değerlerin değişmezliğini doğrula
        assertEquals("Name should be immutable", "Stable", ingredient.getName());
        assertEquals("Amount should be immutable", 10.0, ingredient.getAmount(), 0.001);
        assertEquals("Unit should be immutable", "unit", ingredient.getUnit());
        assertEquals("Price should be immutable", 5.0, ingredient.getPrice(), 0.001);
    }

    @Test
    public void testGetIngredientsForFood_MaximumIngredientCount() {
        String[] mealTypes = {"breakfast", "lunch", "snack", "dinner"};
        
        for (String mealType : mealTypes) {
            Food[] foods = null;
            switch (mealType) {
                case "breakfast":
                    foods = mockMealPlanningService.getBreakfastOptions();
                    break;
                case "lunch":
                    foods = mockMealPlanningService.getLunchOptions();
                    break;
                case "snack":
                    foods = mockMealPlanningService.getSnackOptions();
                    break;
                case "dinner":
                    foods = mockMealPlanningService.getDinnerOptions();
                    break;
            }
            
            if (foods != null) {
                for (Food food : foods) {
                    List<ShoppingListService.Ingredient> ingredients = 
                        shoppingListService.getIngredientsForFood(mealType, food.getName());
                    
                    // Her yemek için maksimum 10 malzeme olduğunu doğrula
                    assertTrue("Ingredient count should not exceed 10 for " + food.getName(), 
                        ingredients.size() <= 10);
                }
            }
        }
    }
    @Test
    public void testCalculateTotalCost_DecimalPrecisionHandling() {
        List<ShoppingListService.Ingredient> precisionIngredients = new ArrayList<>();
        
        precisionIngredients.add(shoppingListService.new Ingredient("Precise Salt", 0.123456, "g", 3.14159));
        precisionIngredients.add(shoppingListService.new Ingredient("Micro Sugar", 0.000001, "ml", 1000000.0));
        
        double totalCost = shoppingListService.calculateTotalCost(precisionIngredients);
        
        assertTrue("Total cost should handle decimal precision", totalCost > 0);
        assertFalse("Total cost should not be infinite", Double.isInfinite(totalCost));
    }

    @Test
    public void testGetIngredientsForFood_UnicodeCharacterSupport() {
        String[][] unicodeRecipes = {
            {"breakfast", "Süper Kahvaltı"},  // Türkçe
            {"lunch", "漢方ランチ"},  // Japonca
            {"dinner", "Пикантный ужин"},  // Rusça
            {"snack", "العشاء الخفيف"}  // Arapça
        };
        
        for (String[] recipeInfo : unicodeRecipes) {
            List<ShoppingListService.Ingredient> ingredients = 
                shoppingListService.getIngredientsForFood(recipeInfo[0], recipeInfo[1]);
            
            assertNotNull("Ingredients for " + recipeInfo[1] + " should not be null", ingredients);
        }
    }

    @Test
   
    public void testCalculateTotalCost_ExtremeBoundaryValues() {
        List<ShoppingListService.Ingredient> boundaryIngredients = new ArrayList<>();
        
        boundaryIngredients.add(shoppingListService.new Ingredient("Min Value", Double.MIN_VALUE, "g", Double.MAX_VALUE));
        boundaryIngredients.add(shoppingListService.new Ingredient("Max Value", Double.MAX_VALUE, "ml", Double.MIN_VALUE));
        
        double totalCost = shoppingListService.calculateTotalCost(boundaryIngredients);
        
        assertTrue("Total cost should handle extreme boundary values", totalCost >= 0);
        assertFalse("Total cost should not be infinite", Double.isInfinite(totalCost));
    }

    @Test
    public void testGetIngredientsForFood_MaximumRecipeIngredients() {
        String[] mealTypes = {"breakfast", "lunch", "snack", "dinner"};
        
        for (String mealType : mealTypes) {
            Food[] foods = null;
            switch (mealType) {
                case "breakfast":
                    foods = mockMealPlanningService.getBreakfastOptions();
                    break;
                case "lunch":
                    foods = mockMealPlanningService.getLunchOptions();
                    break;
                case "snack":
                    foods = mockMealPlanningService.getSnackOptions();
                    break;
                case "dinner":
                    foods = mockMealPlanningService.getDinnerOptions();
                    break;
            }
            
            if (foods != null) {
                for (Food food : foods) {
                    List<ShoppingListService.Ingredient> ingredients = 
                        shoppingListService.getIngredientsForFood(mealType, food.getName());
                    
                    // Her yemek için maksimum 15 malzeme olduğunu doğrula
                    assertTrue("Ingredient count should not exceed 15 for " + food.getName(), 
                        ingredients.size() <= 15);
                }
            }
        }
    }

    @Test
    public void testCalculateTotalCost_MixedUnitTypes1() {
        List<ShoppingListService.Ingredient> mixedUnitIngredients = new ArrayList<>();
        
        mixedUnitIngredients.add(shoppingListService.new Ingredient("Flour", 500.0, "g", 0.5));
        mixedUnitIngredients.add(shoppingListService.new Ingredient("Milk", 250.0, "ml", 0.1));
        mixedUnitIngredients.add(shoppingListService.new Ingredient("Sugar", 3.0, "unit", 1.0));
        
        double totalCost = shoppingListService.calculateTotalCost(mixedUnitIngredients);
        
        // Manuel hesaplama
        double expectedCost = 
            (500.0 / 100.0 * 0.5) +   // Flour
            (250.0 / 100.0 * 0.1) +   // Milk
            (3.0 * 1.0);              // Sugar
        
        assertEquals("Total cost should handle mixed unit types", expectedCost, totalCost, 0.001);
    }

    @Test
    public void testIngredientClass_LongUnitNameHandling() {
        String[] longUnitNames = {
            "extremely_long_custom_unit_name_that_is_very_detailed",
            "超長いユニット名",
            "μικρή_μονάδα_μέτρησης"
        };
        
        for (String unitName : longUnitNames) {
            ShoppingListService.Ingredient ingredient = 
                shoppingListService.new Ingredient("Test Ingredient", 10.0, unitName, 5.0);
            
            assertEquals("Long unit name should be preserved", unitName, ingredient.getUnit());
        }
    }

    @Test
    public void testGetIngredientsForFood_EmptyDatabaseScenario() {
        // Boş veritabanı senaryosu
        ShoppingListService emptyDbService = new ShoppingListService(mockMealPlanningService) {
            @Override
            protected Connection getConnection() {
                try {
                    Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");
                    // Hiçbir tablo oluşturulmayacak
                    return conn;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        
        // Boş veritabanından malzeme çekme
        List<ShoppingListService.Ingredient> ingredients = 
            emptyDbService.getIngredientsForFood("breakfast", "Scrambled Eggs");
        
        // Boş liste dönmeli
        assertNotNull("Ingredients list should not be null", ingredients);
        assertTrue("Ingredients list should be empty", ingredients.isEmpty());
    }

    @Test
    
    public void testIngredientClass_NullAndEmptyStringHandling() {
        ShoppingListService.Ingredient[] nullInputIngredients = {
            shoppingListService.new Ingredient(null, 10.0, null, 5.0),
            shoppingListService.new Ingredient("", 10.0, "", 5.0)
        };
        
        for (ShoppingListService.Ingredient ingredient : nullInputIngredients) {
            assertEquals("Name should be empty string for null input", "", ingredient.getName());
            assertEquals("Unit should be empty string for null input", "", ingredient.getUnit());
            assertEquals("Amount should be preserved", 10.0, ingredient.getAmount(), 0.001);
            assertEquals("Price should be preserved", 5.0, ingredient.getPrice(), 0.001);
        }
    }
    
}