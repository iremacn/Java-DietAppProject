/**
 * @file DietappApp.java
 * @brief Main application class for the Diet Planner system
 * 
 * @details The DietappApp class serves as the main application entry point and controller
 *          for the Diet Planner system. It manages the application lifecycle, user interface,
 *          and coordinates between different services and menus. The class supports both
 *          GUI and console-based interfaces.
 * 
 * @author ugur.coruh
 * @version 1.0
 * @date 2024
 * @copyright Diet Planner Application
 */
package com.berkant.kagan.haluk.irem.dietapp;

import javax.swing.SwingUtilities;

/**
 * @class DietappApp
 * @brief Main application controller and coordinator
 * 
 * @details This class manages the Diet Planner application's lifecycle and coordinates
 *          between different services and user interfaces. It handles user authentication,
 *          menu navigation, and service initialization. The class supports both GUI and
 *          console-based interfaces with automatic detection of the appropriate mode.
 */
public class DietappApp {
    /** @brief Main application instance for core functionality */
    private Dietapp dietApp;
    /** @brief Scanner instance for reading user input in console mode */
    private java.util.Scanner scanner;
    /** @brief Service for managing meal planning operations */
    private MealPlanningService mealPlanningService;
    /** @brief Menu interface for meal planning functionality */
    private MealPlanningMenu mealPlanningMenu;
    /** @brief Service for tracking calories and nutrients */
    private CalorieNutrientTrackingService calorieNutrientService;
    /** @brief Menu interface for calorie and nutrient tracking */
    private CalorieNutrientTrackingMenu calorieNutrientMenu;
    /** @brief Service for managing shopping lists */
    private ShoppingListService shoppingListService;
    /** @brief Menu interface for shopping list operations */
    private ShoppingListMenu shoppingListMenu;
    /** @brief Service for generating personalized diet recommendations */
    private PersonalizedDietRecommendationService personalizedDietService;
    /** @brief Menu interface for personalized diet recommendations */
    private PersonalizedDietRecommendationMenu personalizedDietMenu;
  
    /** @brief Flag indicating whether the application is running in test mode */
    private static boolean testMode = false;
    
    /**
     * @brief Sets the test mode flag
     * @details Enables or disables test mode, which affects application behavior
     *          and interface initialization.
     * 
     * @param mode true to enable test mode, false to disable
     */
    public static void setTestMode(boolean mode) {
        testMode = mode;
    }
    
    /**
     * @brief Checks if the application is running in test mode
     * @details Returns the current state of the test mode flag.
     * 
     * @return true if test mode is enabled, false otherwise
     */
    public static boolean isTestMode() {
        return testMode;
    }
    
    /**
     * @brief Constructor for DietappApp class
     * @details Initializes all necessary services and menus for the application.
     *          Creates instances of Dietapp, Scanner, and all required services
     *          and their corresponding menu interfaces.
     */
    public DietappApp() {
        this.dietApp = new Dietapp();
        this.scanner = new java.util.Scanner(System.in);
        this.mealPlanningService = new MealPlanningService(null);
        this.mealPlanningMenu = new MealPlanningMenu(mealPlanningService, dietApp.getAuthService(), scanner);
        
        // Add new services
        this.calorieNutrientService = new CalorieNutrientTrackingService(mealPlanningService);
        this.calorieNutrientMenu = new CalorieNutrientTrackingMenu(calorieNutrientService, mealPlanningService, dietApp.getAuthService(), scanner);
        
        // Add shopping list services
        this.shoppingListService = new ShoppingListService(mealPlanningService);
        this.shoppingListMenu = new ShoppingListMenu(
            shoppingListService, mealPlanningService, dietApp.getAuthService(), scanner);
        
        // Add Personalized Diet Recommendations services
        this.personalizedDietService = new PersonalizedDietRecommendationService(
            calorieNutrientService, mealPlanningService);
        this.personalizedDietMenu = new PersonalizedDietRecommendationMenu(
            personalizedDietService);
    }
   
    /**
     * @brief Main entry point of the application
     * @details Initializes the database, determines the appropriate interface mode
     *          (GUI or console), and starts the application. Handles database
     *          connection cleanup in the finally block.
     * 
     * @param args Command-line arguments passed to the application
     */
    public static void main(String[] args) {
        DatabaseHelper.initializeDatabase();
        try {
            // Headless kontrolü ekle
            if (!testMode && !java.awt.GraphicsEnvironment.isHeadless()) {
                SwingUtilities.invokeLater(() -> {
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                });
            } else if (java.awt.GraphicsEnvironment.isHeadless()) {
                System.out.println("Headless ortam: GUI başlatılmayacak, sadece konsol işlemleri yapılacak.");
            }
        } finally {
            DatabaseHelper.closeConnection();
        }
    }
    
    /**
     * @brief Runs the main application loop
     * @details Manages the application's main execution flow, including:
     *          - User authentication
     *          - Menu navigation
     *          - Service coordination
     *          - Resource cleanup
     * 
     * @throws RuntimeException if critical services fail to initialize
     */
    public void run() {
        boolean running = true;
        
        System.out.println("Welcome to Diet Planner Application!");
        
        // dietApp null kontrolü
        if (dietApp == null) {
            System.out.println("Error: dietApp is null");
            return;
        }
        
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
                running = handleAuthMenu(choice);
            }
        }
        
        // Close the scanner
        if (scanner != null) {
            scanner.close();
        }
        
        // Close database connection
        try {
            DatabaseHelper.closeAllConnections();
        } catch (Exception e) {
            System.out.println("Error closing database connections: " + e.getMessage());
        }
    }

    /**
     * @brief Handles authentication menu choices
     * @details Processes user selections from the authentication menu,
     *          including login, registration, and guest mode options.
     *          Manages the application flow based on user choices.
     * 
     * @param choice The user's menu selection
     * @return true to continue running, false to exit
     */
    private boolean handleAuthMenu(int choice) {
        switch (choice) {
            case 1:
                handleLogin();
                return true;
            case 2:
                handleRegistration();
                return true;
            case 3:
                handleGuestMode();
                return true;
            case 0:
                System.out.println("Thank you for using Diet Planner. Goodbye!");
                return false;
            default:
                System.out.println("Invalid selection. Please try again.");
                return true;
        }
    }
    
    /**
     * @brief Displays the authentication menu
     * @details Prints the available authentication options to the console,
     *          including login, registration, guest mode, and exit options.
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
     * @brief Displays the main user menu
     * @details Shows the main menu options available to authenticated users,
     *          including their current login status and available features.
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
     * @brief Gets and validates user menu choice
     * @details Reads and validates user input for menu selections,
     *          ensuring it is a valid numeric choice.
     * 
     * @return The validated user choice as an integer, or -1 for invalid input
     */
    private int getUserChoice() {
        String input = scanner.nextLine().trim();
        
        // Check if input is empty
        if (input.isEmpty()) {
            System.out.println("Empty input. Please enter a number.");
            return -1;
        }
        
        // Check if input contains only digits
        if (!input.matches("^\\d+$")) {
            System.out.println("Invalid selection. Please enter only numbers.");
            return -1;
        }
        
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection. Please enter a valid number.");
            return -1;
        }
    }
    
    /**
     * @brief Handles main menu choices
     * @details Processes user selections from the main menu and directs
     *          to appropriate feature menus or actions.
     * 
     * @param choice The user's menu selection
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
                System.out.println("Invalid selection. Please try again.");
                return true;
        }
    }
    
    /**
     * @brief Handles user login process
     * @details Manages the user login flow, including:
     *          - Input validation
     *          - Authentication attempt
     *          - Error handling
     *          - User feedback
     */
    private void handleLogin() {
        System.out.println("\n===== Login =====");
        
        String username = "";
        while (username.trim().isEmpty()) {
            System.out.print("Enter username: ");
            username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty. Please try again.");
            }
        }
        
        String password = "";
        while (password.trim().isEmpty()) {
            System.out.print("Enter password: ");
            password = scanner.nextLine().trim();
            if (password.isEmpty()) {
                System.out.println("Password cannot be empty. Please try again.");
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
     * @brief Handles user registration process
     * @details Manages the user registration flow, including:
     *          - Input validation
     *          - Email format verification
     *          - Registration attempt
     *          - Error handling
     *          - User feedback
     */
    void handleRegistration() {
        System.out.println("\n===== Registration =====");
        
        // Get and validate username
        String username = "";
        while (username.trim().isEmpty()) {
            System.out.print("Enter username: ");
            username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty. Please try again.");
            }
        }
        
        // Get and validate password
        String password = "";
        while (password.trim().isEmpty()) {
            System.out.print("Enter password: ");
            password = scanner.nextLine().trim();
            if (password.isEmpty()) {
                System.out.println("Password cannot be empty. Please try again.");
            }
        }
        
        // Get and validate email
        String email = "";
        while (!isValidEmail(email)) {
            System.out.print("Enter email: ");
            email = scanner.nextLine().trim();
            if (!isValidEmail(email)) {
                System.out.println("Invalid email format. Please enter a valid email address.");
            }
        }
        
        // Get and validate name
        String name = "";
        while (name.trim().isEmpty()) {
            System.out.print("Enter your name: ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Name cannot be empty. Please try again.");
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
     * @brief Validates email address format
     * @details Performs comprehensive email validation checking for:
     *          - Non-null and non-empty string
     *          - Valid email format
     *          - Domain and TLD presence
     * 
     * @param email The email address to validate
     * @return true if the email format is valid, false otherwise
     */
    boolean isValidEmail(String email) {
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
     * @brief Handles guest mode activation
     * @details Manages the process of enabling guest mode, including:
     *          - State validation
     *          - Mode activation
     *          - User feedback
     */
    private void handleGuestMode() {
        dietApp.enableGuestMode();
        System.out.println("You are now using the application as a guest. Some features may be limited.");
    
    }
}