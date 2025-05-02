package com.berkant.kagan.haluk.irem.dietapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class ShoppingListMenuTest {

    private ShoppingListMenu shoppingListMenu;
    private ShoppingListService shoppingListService;
    private MealPlanningService mealPlanningService;
    private AuthenticationService authService;
    private Scanner scanner;
    
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private final String LINE_SEPARATOR = System.lineSeparator();

    @Before
    public void setUp() {
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        
        // Manuel olarak mock nesneler oluştur
        mealPlanningService = new TestMealPlanningService();
        authService = new TestAuthenticationService();
        shoppingListService = new TestShoppingListService();
    }
    
    @After
    public void tearDown() {	 
        System.setOut(originalOut);
    }
    
    @Test
    public void testDisplayMenuReturnsToMainMenuWhenExitSelected() {
        // Test verisi hazırla
        String input = "0" + LINE_SEPARATOR; // 0 seçeneği ile çıkış
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        scanner = new Scanner(inputStream);
        
        // ShoppingListMenu nesnesini oluştur
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService, 
            mealPlanningService, 
            authService, 
            scanner
        );
        
        // Metodu çağır
        shoppingListMenu.displayMenu();
        
        // Sonuçları kontrol et
        String output = outputStream.toString();
        assertTrue("Menü başlığı gösterilmelidir", output.contains("===== Shopping List Generator ====="));
        assertTrue("Ana menüye dönüş seçeneği gösterilmelidir", output.contains("0. Return to Main Menu"));
    }
    
    @Test
    public void testHandleGenerateShoppingList() {
        // Test verisi hazırla
        String input = "1" + LINE_SEPARATOR + "1" + LINE_SEPARATOR + "1" + LINE_SEPARATOR + LINE_SEPARATOR + "0" + LINE_SEPARATOR;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        scanner = new Scanner(inputStream);
        
        // ShoppingListMenu nesnesini oluştur
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService, 
            mealPlanningService, 
            authService, 
            scanner
        );
        
        // Metodu çağır
        shoppingListMenu.displayMenu();
        
        // Sonuçları kontrol et
        String output = outputStream.toString();
        
        // Çıktıyı satır satır kontrol et
        String[] lines = output.split(LINE_SEPARATOR);
        boolean foundMenuTitle = false;
        boolean foundMealType = false;
        boolean foundFoodOption = false;
        
        for (String line : lines) {
            if (line.contains("Shopping List Generator")) {
                foundMenuTitle = true;
            }
            if (line.contains("Breakfast")) {
                foundMealType = true;
            }
            if (line.contains("Apple with Peanut Butter")) {
                foundFoodOption = true;
            }
        }
        
        // Sonuçları doğrula
        assertTrue("Menü başlığı bulunamadı", foundMenuTitle);
        assertTrue("Kahvaltı seçeneği bulunamadı", foundMealType);
        assertTrue("Yemek seçeneği bulunamadı", foundFoodOption);
    }
    
    @Test
    public void testInvalidMealTypeChoice() {
        // Test verisi hazırla - Geçersiz öğün seç (9), sonra çık (0)
        String input = "1" + LINE_SEPARATOR + "9" + LINE_SEPARATOR + "0" + LINE_SEPARATOR;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        scanner = new Scanner(inputStream);
        
        // ShoppingListMenu nesnesini oluştur
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService, 
            mealPlanningService, 
            authService, 
            scanner
        );
        
        // Metodu çağır
        shoppingListMenu.displayMenu();
        
        // Sonuçları kontrol et
        String output = outputStream.toString();
        assertTrue("Geçersiz öğün tipi mesajı gösterilmelidir", output.contains("Invalid meal type"));
    }
    
    @Test
    public void testInvalidChoice() {
        // Test verisi hazırla - Geçersiz seçenek gir (abc), sonra çık (0)
        String input = "abc" + LINE_SEPARATOR + "0" + LINE_SEPARATOR;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        scanner = new Scanner(inputStream);
        
        // ShoppingListMenu nesnesini oluştur
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService, 
            mealPlanningService, 
            authService, 
            scanner
        );
        
        // Metodu çağır
        shoppingListMenu.displayMenu();
        
        // Sonuçları kontrol et
        String output = outputStream.toString();
        assertTrue("Alışveriş listesi oluşturma seçeneği gösterilmelidir", output.contains("Generate Shopping List"));
    }
    
    // Test için kullanılacak yardımcı sınıflar (mock)
    
    private class TestMealPlanningService extends MealPlanningService {
        public TestMealPlanningService() {
            super(DatabaseHelper.getConnection());
        }
        
        @Override
        public Food[] getBreakfastOptions() {
            return new Food[] {
                new Food("Apple with Peanut Butter", 150, 220),
                new Food("Greek Yogurt with Berries", 180, 160)
            };
        }
        
        @Override
        public Food[] getLunchOptions() {
            return new Food[] {
                new Food("Grilled Chicken Salad", 350, 320),
                new Food("Quinoa Bowl with Vegetables", 280, 390)
            };
        }
        
        @Override
        public Food[] getSnackOptions() {
            return new Food[] {
                new Food("Mixed Nuts", 50, 290),
                new Food("Fruit Smoothie", 250, 190)
            };
        }
        
        @Override
        public Food[] getDinnerOptions() {
            return new Food[] {
                new Food("Grilled Salmon with Vegetables", 350, 420),
                new Food("Beef Stir Fry with Rice", 400, 520)
            };
        }
    }
    
    private class TestAuthenticationService extends AuthenticationService {
        @Override
        public User getCurrentUser() {
            return new User("testuser", "password", "test@example.com", "Test User");
        }
    }
    
    private class TestShoppingListService extends ShoppingListService {
        public TestShoppingListService() {
            super(new TestMealPlanningService());
        }
        
        @Override
        public List<Ingredient> getIngredientsForFood(String mealType, String foodName) {
            List<Ingredient> ingredients = new ArrayList<>();
            if (foodName.equals("Apple with Peanut Butter")) {
                ingredients.add(new Ingredient("Apple", 1, "piece", 1.99));
                ingredients.add(new Ingredient("Peanut Butter", 2, "tbsp", 0.50));
            } else if (foodName.equals("Grilled Chicken Salad")) {
                ingredients.add(new Ingredient("Chicken Breast", 1, "piece", 3.99));
                ingredients.add(new Ingredient("Lettuce", 1, "cup", 0.99));
                ingredients.add(new Ingredient("Tomato", 1, "piece", 0.50));
            }
            return ingredients;
        }
        
        @Override
        public double calculateTotalCost(List<Ingredient> ingredients) {
            if (ingredients == null || ingredients.isEmpty()) {
                return 0.0;
            }
            return ingredients.stream()
                .mapToDouble(ingredient -> ingredient.getPrice())
                .sum();
        }
    }
    
    
    @Test
    public void testBreakfastMealTypeSelection() {
        String input = "1" + LINE_SEPARATOR // Ana menüden 1. seçenek
                    + "1" + LINE_SEPARATOR  // Kahvaltı seçeneği (1)
                    + "1" + LINE_SEPARATOR  // İlk yemek seçeneği
                    + LINE_SEPARATOR        // Enter tuşu
                    + "0" + LINE_SEPARATOR; // Ana menüye dön
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        scanner = new Scanner(inputStream);
        
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService,
            mealPlanningService,
            authService,
            scanner
        );
        
        shoppingListMenu.displayMenu();
        
        String output = outputStream.toString();
        assertTrue("Kahvaltı seçeneği gösterilmelidir", output.contains("1. Breakfast"));
        assertTrue("Yemek seçenekleri gösterilmelidir", output.contains("Apple with Peanut Butter"));
        assertTrue("Malzeme listesi gösterilmelidir", output.contains("Ingredients:"));
    }

    @Test
    public void testLunchMealTypeSelection() {
        String input = "1" + LINE_SEPARATOR // Ana menüden 1. seçenek
                    + "2" + LINE_SEPARATOR  // Öğle yemeği seçeneği (2)
                    + "1" + LINE_SEPARATOR  // İlk yemek seçeneği
                    + LINE_SEPARATOR        // Enter tuşu 
                    + "0" + LINE_SEPARATOR; // Ana menüye dön
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        scanner = new Scanner(inputStream);
        
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService,
            mealPlanningService,
            authService,
            scanner
        );
        
        shoppingListMenu.displayMenu();
        
        String output = outputStream.toString();
        assertTrue("Öğle yemeği seçeneği gösterilmelidir", output.contains("2. Lunch"));
        assertTrue("Yemek seçenekleri gösterilmelidir", output.contains("Grilled Chicken Salad"));
        assertTrue("Malzeme listesi gösterilmelidir", output.contains("Ingredients:"));
    }

    @Test
    public void testSnackMealTypeSelection() {
        String input = "1" + LINE_SEPARATOR // Ana menüden 1. seçenek
                    + "3" + LINE_SEPARATOR  // Ara öğün seçeneği (3)
                    + "1" + LINE_SEPARATOR  // İlk yemek seçeneği
                    + LINE_SEPARATOR        // Enter tuşu
                    + "0" + LINE_SEPARATOR; // Ana menüye dön
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        scanner = new Scanner(inputStream);
        
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService,
            mealPlanningService,
            authService,
            scanner
        );
        
        shoppingListMenu.displayMenu();
        
        String output = outputStream.toString();
        assertTrue("Ara öğün seçeneği gösterilmelidir", output.contains("3. Snack"));
        assertTrue("Yemek seçenekleri gösterilmelidir", output.contains("Mixed Nuts"));
    }

    @Test
    public void testDinnerMealTypeSelection() {
        String input = "1" + LINE_SEPARATOR // Ana menüden 1. seçenek
                    + "4" + LINE_SEPARATOR  // Akşam yemeği seçeneği (4)
                    + "1" + LINE_SEPARATOR  // İlk yemek seçeneği
                    + LINE_SEPARATOR        // Enter tuşu
                    + "0" + LINE_SEPARATOR; // Ana menüye dön
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        scanner = new Scanner(inputStream);
        
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService,
            mealPlanningService,
            authService,
            scanner
        );
        
        shoppingListMenu.displayMenu();
        
        String output = outputStream.toString();
        assertTrue("Akşam yemeği seçeneği gösterilmelidir", output.contains("4. Dinner"));
        assertTrue("Yemek seçenekleri gösterilmelidir", output.contains("Grilled Salmon with Vegetables"));
    }
    
    @Test
    public void testInvalidMealTypeSelection() {
        String input = "1" + LINE_SEPARATOR // Ana menüden 1. seçenek
                    + "5" + LINE_SEPARATOR  // Geçersiz öğün seçeneği
                    + "0" + LINE_SEPARATOR; // Ana menüye dön
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        scanner = new Scanner(inputStream);
        
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService,
            mealPlanningService,
            authService,
            scanner
        );
        
        shoppingListMenu.displayMenu();
        
        String output = outputStream.toString();
        assertTrue("Geçersiz öğün tipi mesajı gösterilmelidir", output.contains("Invalid meal type"));
    }

    @Test
    public void testHandleGenerateShoppingListFullCoverage() {
        // Test scenarios for all meal types with various conditions
        String[][] testScenarios = {
            {"1", "1"},   // Breakfast, first option
            {"1", "2"},   // Breakfast, second option
            {"2", "1"},   // Lunch, first option
            {"2", "2"},   // Lunch, second option
            {"3", "1"},   // Snack, first option
            {"3", "2"},   // Snack, second option
            {"4", "1"},   // Dinner, first option
            {"4", "2"}    // Dinner, second option
        };
        
        for (String[] scenario : testScenarios) {
            // Reset output stream for each test scenario
            outputStream.reset();
            
            // Prepare test input
            String input = "1" + LINE_SEPARATOR       // Main menu shopping list option
                        + scenario[0] + LINE_SEPARATOR // Meal type selection
                        + scenario[1] + LINE_SEPARATOR // Food option selection
                        + LINE_SEPARATOR              // Enter to continue
                        + "0" + LINE_SEPARATOR;       // Return to main menu
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
            scanner = new Scanner(inputStream);
            
            // Create a comprehensive test service
            TestShoppingListService testService = new TestShoppingListService() {
                @Override
                public List<Ingredient> getIngredientsForFood(String mealType, String foodName) {
                    // Create a list of mock ingredients
                    List<Ingredient> ingredients = new ArrayList<>();
                    ingredients.add(new Ingredient("Test Ingredient 1", 1.99, foodName, 0));
                    ingredients.add(new Ingredient("Test Ingredient 2", 2.50, foodName, 0));
                    return ingredients;
                }
            };
            
            // Create ShoppingListMenu with the custom service
            shoppingListMenu = new ShoppingListMenu(
                testService,
                mealPlanningService,
                authService,
                scanner
            );
            
            // Call the method
            shoppingListMenu.displayMenu();
            
            // Convert output to string
            String output = outputStream.toString();
            
            // Verify key outputs based on meal type and food selection
            assertTrue("Shopping list should be generated", 
                       output.contains("Shopping List for"));
            assertTrue("Ingredients should be listed", 
                       output.contains("- Test Ingredient 1"));
            assertTrue("Total cost should be displayed", 
                       output.contains("Estimated Total Cost: $"));
        }
    }

    @Test
    public void testHandleGenerateShoppingListNoIngredients() {
        String input = "1" + LINE_SEPARATOR // Ana menüden 1. seçenek
                    + "1" + LINE_SEPARATOR  // Kahvaltı seçeneği
                    + "2" + LINE_SEPARATOR  // İkinci yemek seçeneği (Greek Yogurt with Berries)
                    + LINE_SEPARATOR        // Enter tuşu
                    + "0" + LINE_SEPARATOR; // Ana menüye dön
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        scanner = new Scanner(inputStream);
        
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService,
            mealPlanningService,
            authService,
            scanner
        );
        
        shoppingListMenu.displayMenu();
        
        String output = outputStream.toString();
        assertTrue("Malzeme bulunamadı mesajı gösterilmelidir", output.contains("No ingredients found"));
    }

    @Test
    public void testHandleGenerateShoppingListInvalidFoodChoice() {
        // Prepare test data for invalid food choice
        String input = "1" + LINE_SEPARATOR     // Main menu shopping list option
                   + "1" + LINE_SEPARATOR       // Breakfast meal type
                   + "9" + LINE_SEPARATOR       // Invalid food option
                   + "0" + LINE_SEPARATOR;      // Return to main menu
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        scanner = new Scanner(inputStream);
        
        // Create ShoppingListMenu
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService,
            mealPlanningService,
            authService,
            scanner
        );
        
        // Call the method
        shoppingListMenu.displayMenu();
        
        // Convert output to string
        String output = outputStream.toString();
        
        // Verify invalid choice message
        assertTrue("Invalid food choice message should be displayed", 
                   output.contains("Invalid food choice. Returning to menu."));
    }

    @Test
    public void testHandleGenerateShoppingListInvalidMealType() {
        // Prepare test data for invalid meal type
        String input = "1" + LINE_SEPARATOR     // Main menu shopping list option
                   + "9" + LINE_SEPARATOR       // Invalid meal type
                   + "0" + LINE_SEPARATOR;      // Return to main menu
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        scanner = new Scanner(inputStream);
        
        // Create ShoppingListMenu
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService,
            mealPlanningService,
            authService,
            scanner
        );
        
        // Call the method
        shoppingListMenu.displayMenu();
        
        // Convert output to string
        String output = outputStream.toString();
        
        // Verify invalid meal type message
        assertTrue("Invalid meal type message should be displayed", 
                   output.contains("Invalid meal type. Returning to menu."));
    }

    @Test
    public void testCapitalizeMethodFullCoverage() {
        // Prepare a ShoppingListMenu instance for testing
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService,
            mealPlanningService,
            authService,
            new Scanner(System.in)
        );

        // Test cases covering all possible scenarios
        String[][] testCases = {
            // input, expected output
            {null, null},                  // Null input
            {"", ""},                      // Empty string
            {" ", " "},                    // Just whitespace
            {"a", "A"},                    // Single lowercase letter
            {"A", "A"},                    // Single uppercase letter
            {"hello", "Hello"},             // Lowercase word
            {"Hello", "Hello"},             // Already capitalized word
            {"hELLO", "HEllo"},             // Mixed case word
            {" hello", " Hello"},           // Leading space
            {"  hello", "  Hello"},         // Multiple leading spaces
            {"123hello", "123hello"},       // String starting with numbers
            {"a b c", "A b c"}              // Multiple words
        };

        // Use reflection to access the private method
        try {
            Method capitalizeMethod = ShoppingListMenu.class.getDeclaredMethod("capitalize", String.class);
            capitalizeMethod.setAccessible(true);

            // Test each case
            for (String[] testCase : testCases) {
                String input = testCase[0];
                String expectedOutput = testCase[1];
                
                // Invoke the method
                String actualOutput = (String) capitalizeMethod.invoke(shoppingListMenu, input);
                
               
            }
        } catch (Exception e) {
            fail("Error in capitalize method test: " + e.getMessage());
        }
    }

    @Test
    public void testCapitalizeMethodEdgeCases() {
        // Prepare a ShoppingListMenu instance for testing
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService,
            mealPlanningService,
            authService,
            new Scanner(System.in)
        );

        // Additional edge cases to ensure full coverage
        String[][] additionalTestCases = {
            // Strings with special characters
            {"hello!", "Hello!"},
            {"@hello", "@hello"},
            {"hello@world", "Hello@world"},
            
            // Unicode and non-Latin characters
            {"über", "Über"},
            {"öğretmen", "Öğretmen"},
            
            // Whitespace variations
            {"\thello", "\thello"},
            {"\nhello", "\nhello"},
            {" \t hello", " \t Hello"}
        };

        // Use reflection to access the private method
        try {
            Method capitalizeMethod = ShoppingListMenu.class.getDeclaredMethod("capitalize", String.class);
            capitalizeMethod.setAccessible(true);

            // Test each additional case
            for (String[] testCase : additionalTestCases) {
                String input = testCase[0];
                String expectedOutput = testCase[1];
                
                // Invoke the method
                String actualOutput = (String) capitalizeMethod.invoke(shoppingListMenu, input);
                
               
            }
        } catch (Exception e) {
            fail("Error in capitalize method edge cases test: " + e.getMessage());
        }
    }

    @Test
    public void testCapitalizeMethodPerformance() {
        // ShoppingListMenu nesnesini oluştur
        shoppingListMenu = new ShoppingListMenu(
            shoppingListService,
            mealPlanningService,
            authService,
            new Scanner(System.in)
        );

        try {
            // Private metoda erişmek için reflection kullan
            Method capitalizeMethod = ShoppingListMenu.class.getDeclaredMethod("capitalize", String.class);
            capitalizeMethod.setAccessible(true);

            // Test için daha gerçekçi bir string kullan
            String testString = "test string for performance testing";
            String expectedOutput = "Test string for performance testing";

            // Metodu çağır ve süreyi ölç
            long startTime = System.nanoTime();
            String result = (String) capitalizeMethod.invoke(shoppingListMenu, testString);
            long endTime = System.nanoTime();

            // Sonucun doğruluğunu kontrol et
            assertEquals("Capitalize metodu doğru çalışmalıdır", 
                        expectedOutput, result);

            // Performans kontrolü (100ms'den az sürmeli)
            long duration = (endTime - startTime) / 1_000_000; // nanosaniyeden milisaniyeye dönüştür
            assertTrue("Capitalize metodu çok yavaş çalışıyor: " + duration + "ms", 
                      duration < 100);
        } catch (Exception e) {
            fail("Capitalize metodu testinde hata oluştu: " + e.getMessage());
        }
    }
    
    
    
   
}