package com.berkant.kagan.haluk.irem.dietapp;

import java.util.Scanner;

/**
 * This class represents the main application class for the DietApp.
 * @details The DietAppApp class provides the entry point for the DietApp. It initializes the necessary components,
 *          performs calculations, and handles exceptions.
 * @author ugur.coruh
 */
public class DietappApp {
    // Private fields for encapsulation
    private Dietapp dietApp;
    private Scanner scanner;
    private MealPlanningService mealPlanningService;
    private MealPlanningMenu mealPlanningMenu;
    // Yeni eklenen değişkenler
    private CalorieNutrientTrackingService calorieNutrientService;
    private CalorieNutrientTrackingMenu calorieNutrientMenu;
    // Alışveriş listesi için eklenen değişkenler
    private ShoppingListService shoppingListService;
    private ShoppingListMenu shoppingListMenu;
    // PersonalizedDietRecommendations için eklenen değişkenler
    private PersonalizedDietRecommendationService personalizedDietService;
    private PersonalizedDietRecommendationMenu personalizedDietMenu;
    
    /**
     * Constructor for DietAppApp class.
     * Initializes the DietApp and Scanner objects.
     */
    public DietappApp() {
        this.dietApp = new Dietapp();
        this.scanner = new Scanner(System.in);
        this.mealPlanningService = new MealPlanningService();
        this.mealPlanningMenu = new MealPlanningMenu(mealPlanningService, dietApp.getAuthService(), scanner);
        
        // Yeni servisleri ekle
        this.calorieNutrientService = new CalorieNutrientTrackingService(mealPlanningService);
        this.calorieNutrientMenu = new CalorieNutrientTrackingMenu(
            calorieNutrientService, mealPlanningService, dietApp.getAuthService(), scanner);
        
        // Alışveriş listesi servislerini ekle
        this.shoppingListService = new ShoppingListService(mealPlanningService);
        this.shoppingListMenu = new ShoppingListMenu(
            shoppingListService, mealPlanningService, dietApp.getAuthService(), scanner);
        
        // Personalized Diet Recommendations servislerini ekle
        this.personalizedDietService = new PersonalizedDietRecommendationService(
            calorieNutrientService, mealPlanningService);
        this.personalizedDietMenu = new PersonalizedDietRecommendationMenu(
            personalizedDietService, dietApp.getAuthService(), scanner);
    }
    
    /**
     * The main entry point of the DietApp App.
     * 
     * @details The main method is the starting point of the DietApp App. It
     *          initializes the logger, performs logging, displays a greeting
     *          message, and handles user input.
     * 
     * @param args The command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        // Create an instance of the application
        DietappApp app = new DietappApp();
        
        // Run the application
        app.run();
    }
    
    /**
     * Runs the main application loop.
     */
    public void run() {
        boolean running = true;
        
        System.out.println("Welcome to Diet Planner Application!");
        
        while (running) {
            if (dietApp.isUserLoggedIn()) {
                printUserMainMenu();
            } else {
                printAuthMenu();
            }
            
            int choice = getUserChoice();
            
            if (dietApp.isUserLoggedIn()) {
                running = handleUserMainMenu(choice);
            } else {
                handleAuthMenu(choice);
            }
        }
        
        // Close the scanner
        scanner.close();
    }
    
    /**
     * Prints the authentication menu.
     */
    private void printAuthMenu() {
        System.out.println("\n===== Diet Planner Authentication =====");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Continue as Guest");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }
    
    /**
     * Prints the main user menu after authentication.
     */
    private void printUserMainMenu() {
        User currentUser = dietApp.getCurrentUser();
        System.out.println("\n===== Diet Planner Main Menu =====");
        System.out.println("Logged in as: " + currentUser.getUsername());
        System.out.println("1. Meal Planning and Logging");
        System.out.println("2. Calorie and Nutrient Tracking");
        System.out.println("3. Personalized Diet Recommendations");
        System.out.println("4. Shopping List Generator");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }
    
    /**
     * Gets the user's menu choice from the console with improved validation.
     * 
     * @return The user's choice as an integer
     */
    private int getUserChoice() {
        String input = scanner.nextLine().trim();
        
        // Check if input is empty
        if (input.isEmpty()) {
            System.out.println("Boş giriş. Lütfen bir sayı girin.");
            return -1;
        }
        
        // Check if input contains only digits
        if (!input.matches("^\\d+$")) {
            System.out.println("Geçersiz seçim. Lütfen sadece sayı girin.");
            return -1;
        }
        
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Geçersiz seçim. Lütfen geçerli bir sayı girin.");
            return -1;
        }
    }
    
    /**
     * Handles the authentication menu choices.
     * 
     * @param choice The user's menu choice
     */
    private void handleAuthMenu(int choice) {
        switch (choice) {
            case 1:
                handleLogin();
                break;
            case 2:
                handleRegistration();
                break;
            case 3:
                handleGuestMode();
                break;
            case 0:
                System.out.println("Thank you for using Diet Planner. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Geçersiz seçim. Lütfen tekrar deneyin.");
        }
    }
    
    /**
     * Handles the main user menu choices.
     * 
     * @param choice The user's menu choice
     * @return true to continue running, false to exit
     */
    private boolean handleUserMainMenu(int choice) {
        switch (choice) {
            case 1:
                // Handle Meal Planning and Logging
                mealPlanningMenu.displayMenu();
                return true;
            case 2:
                // Handle Calorie and Nutrient Tracking
                calorieNutrientMenu.displayMenu();
                return true;
            case 3:
                // Handle Personalized Diet Recommendations
                personalizedDietMenu.displayMenu();
                return true;
            case 4:
                // Handle Shopping List Generator
                shoppingListMenu.displayMenu();
                return true;
            case 5:
                // Log out
                dietApp.logoutUser();
                System.out.println("You have been logged out.");
                return true;
            case 0:
                System.out.println("Thank you for using Diet Planner. Goodbye!");
                return false;
            default:
                System.out.println("Geçersiz seçim. Lütfen tekrar deneyin.");
                return true;
        }
    }
    
    /**
     * Handles the user login process.
     */
    private void handleLogin() {
        System.out.println("\n===== Login =====");
        
        String username = "";
        while (username.trim().isEmpty()) {
            System.out.print("Enter username: ");
            username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Kullanıcı adı boş olamaz. Lütfen tekrar deneyin.");
            }
        }
        
        String password = "";
        while (password.trim().isEmpty()) {
            System.out.print("Enter password: ");
            password = scanner.nextLine().trim();
            if (password.isEmpty()) {
                System.out.println("Şifre boş olamaz. Lütfen tekrar deneyin.");
            }
        }
        
        boolean success = dietApp.loginUser(username, password);
        
        if (success) {
            System.out.println("Login successful! Welcome, " + dietApp.getCurrentUser().getName() + "!");
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }
    }
    
    /**
     * Handles the user registration process with improved validation.
     */
    private void handleRegistration() {
        System.out.println("\n===== Registration =====");
        
        // Get and validate username
        String username = "";
        while (username.trim().isEmpty()) {
            System.out.print("Enter username: ");
            username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Kullanıcı adı boş olamaz. Lütfen tekrar deneyin.");
            }
        }
        
        // Get and validate password
        String password = "";
        while (password.trim().isEmpty()) {
            System.out.print("Enter password: ");
            password = scanner.nextLine().trim();
            if (password.isEmpty()) {
                System.out.println("Şifre boş olamaz. Lütfen tekrar deneyin.");
            }
        }
        
        // Get and validate email
        String email = "";
        while (!isValidEmail(email)) {
            System.out.print("Enter email: ");
            email = scanner.nextLine().trim();
            if (!isValidEmail(email)) {
                System.out.println("Geçersiz e-posta formatı. Lütfen geçerli bir e-posta adresi girin.");
            }
        }
        
        // Get and validate name
        String name = "";
        while (name.trim().isEmpty()) {
            System.out.print("Enter your name: ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("İsim boş olamaz. Lütfen tekrar deneyin.");
            }
        }
        
        boolean success = dietApp.registerUser(username, password, email, name);
        
        if (success) {
            System.out.println("Registration successful! You can now log in.");
        } else {
            System.out.println("Registration failed. Username already exists.");
        }
    }
    
    /**
     * Checks if an email address is valid.
     * Simple validation that checks for @ symbol and a period after it.
     *
     * @param email The email address to validate
     * @return true if email is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        email = email.trim();
        
        // Basic email validation: contains @ and at least one . after @
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return false;
        }
        
        int dotIndex = email.indexOf('.', atIndex);
        if (dotIndex <= atIndex + 1 || dotIndex == email.length() - 1) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Handles guest mode login.
     */
    private void handleGuestMode() {
        dietApp.enableGuestMode();
        System.out.println("You are now using the application as a guest. Some features may be limited.");
    }
}