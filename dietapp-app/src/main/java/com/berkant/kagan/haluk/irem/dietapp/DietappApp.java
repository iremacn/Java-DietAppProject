package com.berkant.kagan.haluk.irem.dietapp;

import javax.swing.SwingUtilities;

/**
 * This class represents the main application class for the DietApp.
 * @details The DietAppApp class provides the entry point for the DietApp. It initializes the necessary components,
 *          performs calculations, and handles exceptions.
 * @author ugur.coruh
 */
public class DietappApp {
    /** The main DietApp instance */
    private Dietapp dietApp;
    /** Scanner for reading user input */
    private java.util.Scanner scanner;
    /** Service for meal planning operations */
    private MealPlanningService mealPlanningService;
    /** Menu interface for meal planning */
    private MealPlanningMenu mealPlanningMenu;
    /** Service for calorie and nutrient tracking */
    private CalorieNutrientTrackingService calorieNutrientService;
    /** Menu interface for calorie and nutrient tracking */
    private CalorieNutrientTrackingMenu calorieNutrientMenu;
    /** Service for shopping list operations */
    private ShoppingListService shoppingListService;
    /** Menu interface for shopping list */
    private ShoppingListMenu shoppingListMenu;
    /** Service for personalized diet recommendations */
    private PersonalizedDietRecommendationService personalizedDietService;
    /** Menu interface for personalized diet recommendations */
    private PersonalizedDietRecommendationMenu personalizedDietMenu;
  
    /**
     * Constructor for DietAppApp class.
     * Initializes the DietApp and Scanner objects.
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
     * The main entry point of the DietApp App.
     * 
     * @details The main method is the starting point of the DietApp App. It
     *          initializes the logger, performs logging, displays a greeting
     *          message, and handles user input.
     * 
     * @param args The command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        // Veritabanı bağlantısını başlat
        DatabaseHelper.initializeDatabase();
        
        try {
            // Swing UI'ı başlat
            SwingUtilities.invokeLater(() -> {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            });
        } finally {
            // Uygulama kapandığında veritabanı bağlantısını kapat
            DatabaseHelper.closeConnection();
        }
    }
    
    /**
     * Runs the main application loop.
     * @details Handles the main application flow, including user authentication
     *          and menu navigation. Continues running until the user chooses to exit.
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
                running = handleAuthMenu(choice);
            }
        }
        
        // Close the scanner
        scanner.close();
        
        // Close database connection
        DatabaseHelper.closeAllConnections();
    }

    /**
     * Handles the authentication menu choices.
     * @details Processes user selections from the authentication menu,
     *          including login, registration, and guest mode options.
     * 
     * @param choice The user's menu choice
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
     * Prints the authentication menu.
     * @details Displays the available authentication options to the user,
     *          including login, registration, and guest mode.
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
     * @details Displays the main menu options available to authenticated users,
     *          including meal planning, tracking, and recommendations.
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
     * @details Validates user input to ensure it is a valid numeric choice.
     *          Handles empty input and non-numeric characters.
     * 
     * @return The user's choice as an integer, returns -1 for invalid input
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
     * Handles the main user menu choices.
     * @details Processes user selections from the main menu, directing to appropriate
     *          sub-menus for different features of the application.
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
                System.out.println("Invalid selection. Please try again.");
                return true;
        }
    }
    
    /**
     * Handles the user login process.
     * @details Guides the user through the login process, validating input
     *          and providing feedback on success or failure.
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
     * Handles the user registration process with improved validation.
     * @details Guides the user through registration, validating all inputs
     *          including username, password, email, and name.
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
     * Checks if an email address is valid.
     * @details Performs basic email validation by checking for proper format:
     *          - Must contain @ symbol
     *          - Must have at least one period after @
     *          - Cannot start or end with @ or period
     *
     * @param email The email address to validate
     * @return true if email is valid, false otherwise
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
     * Handles guest mode login.
     * @details Enables guest mode access to the application with limited
     *          functionality and informs the user of restrictions.
     */
    private void handleGuestMode() {
        dietApp.enableGuestMode();
        System.out.println("You are now using the application as a guest. Some features may be limited.");
    }
}