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
    
    /**
     * Constructor for DietAppApp class.
     * Initializes the DietApp and Scanner objects.
     */
    public DietappApp() {
        this.dietApp = new Dietapp();
        this.scanner = new Scanner(System.in);
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
            printMainMenu();
            int choice = getUserChoice();
            
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
                    running = false;
                    System.out.println("Thank you for using Diet Planner. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        
        // Close the scanner
        scanner.close();
    }
    
    /**
     * Prints the main authentication menu.
     */
    private void printMainMenu() {
        System.out.println("\n===== Diet Planner Authentication =====");
        if (dietApp.isUserLoggedIn()) {
            User currentUser = dietApp.getCurrentUser();
            System.out.println("Logged in as: " + currentUser.getUsername());
            System.out.println("1. Log out");
            System.out.println("0. Exit");
        } else {
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Continue as Guest");
            System.out.println("0. Exit");
        }
        System.out.print("Enter your choice: ");
    }
    
    /**
     * Gets the user's menu choice from the console.
     * 
     * @return The user's choice as an integer
     */
    private int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1; // Invalid input
        }
    }
    
    /**
     * Handles the user login process.
     */
    private void handleLogin() {
        if (dietApp.isUserLoggedIn()) {
            // Log out if already logged in
            dietApp.logoutUser();
            System.out.println("You have been logged out.");
            return;
        }
        
        System.out.println("\n===== Login =====");
        
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        boolean success = dietApp.loginUser(username, password);
        
        if (success) {
            System.out.println("Login successful! Welcome, " + dietApp.getCurrentUser().getName() + "!");
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }
    }
    
    /**
     * Handles the user registration process.
     */
    private void handleRegistration() {
        System.out.println("\n===== Registration =====");
        
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        
        boolean success = dietApp.registerUser(username, password, email, name);
        
        if (success) {
            System.out.println("Registration successful! You can now log in.");
        } else {
            System.out.println("Registration failed. Username already exists.");
        }
    }
    
    /**
     * Handles guest mode login.
     */
    private void handleGuestMode() {
        dietApp.enableGuestMode();
        System.out.println("You are now using the application as a guest. Some features may be limited.");
    }
}