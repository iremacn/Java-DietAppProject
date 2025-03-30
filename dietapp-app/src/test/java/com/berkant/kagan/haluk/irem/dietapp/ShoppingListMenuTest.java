package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
        // Test verisi hazırla - Kahvaltı (1) seç, ilk yemek seçeneğini (1) seç
        String input = "1" + LINE_SEPARATOR + "1" + LINE_SEPARATOR + "1" + LINE_SEPARATOR + "0" + LINE_SEPARATOR;
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
        assertTrue("Alışveriş listesi oluşturma başlığı gösterilmelidir", output.contains("Generate Shopping List"));
        assertTrue("Öğün tipi seçimi gösterilmelidir", output.contains("Select Meal Type"));
        assertTrue("Kahvaltı seçeneği gösterilmelidir", output.contains("1. Breakfast"));
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
            super(null); // ShoppingListService'in constructor'ına null geçiyoruz
        }
        
        @Override
        public List<Ingredient> getIngredientsForFood(String mealType, String foodName) {
            List<Ingredient> ingredients = new ArrayList<>();
            if (foodName.equals("Apple with Peanut Butter")) {
                // Ingredient nesneleri oluşturamıyoruz çünkü bu iç sınıf
                // Bu durumda manuel olarak oluşturmak yerine boş liste dönüyoruz
            }
            return ingredients;
        }
        
        @Override
        public double calculateTotalCost(List<Ingredient> ingredients) {
            return 5.99;
        }
    }
}