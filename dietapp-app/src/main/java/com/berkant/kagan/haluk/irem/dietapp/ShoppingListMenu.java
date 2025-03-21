package com.berkant.kagan.haluk.irem.dietapp;

import java.util.List;
import java.util.Scanner;

/**
 * This class handles the shopping list menu operations for the Diet Planner application.
 * @details The ShoppingListMenu class provides menu interfaces for generating shopping lists
 *          based on meal plans and viewing ingredient costs.
 * @author haluk
 */
public class ShoppingListMenu {
    // Service objects
    private ShoppingListService shoppingListService;
    private MealPlanningService mealPlanningService;
    private AuthenticationService authService;
    private Scanner scanner;
    
    /**
     * Constructor for ShoppingListMenu class.
     * 
     * @param shoppingListService The shopping list service
     * @param mealPlanningService The meal planning service
     * @param authService The authentication service
     * @param scanner The scanner for user input
     */
    public ShoppingListMenu(ShoppingListService shoppingListService, 
                          MealPlanningService mealPlanningService,
                          AuthenticationService authService,
                          Scanner scanner) {
        this.shoppingListService = shoppingListService;
        this.mealPlanningService = mealPlanningService;
        this.authService = authService;
        this.scanner = scanner;
    }
    
    /**
     * Displays the main shopping list menu and handles user selections.
     */
    public void displayMenu() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n===== Shopping List Generator =====");
            System.out.println("1. Generate Shopping List for a Meal");
            System.out.println("0. Return to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = getUserChoice();
            
            switch (choice) {
                case 1:
                    handleGenerateShoppingList();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
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
     * Handles the shopping list generation process.
     */
    private void handleGenerateShoppingList() {
        System.out.println("\n===== Generate Shopping List =====");
        
        // Display available meal types
        System.out.println("\nSelect Meal Type:");
        System.out.println("1. Breakfast");
        System.out.println("2. Lunch");
        System.out.println("3. Snack");
        System.out.println("4. Dinner");
        System.out.print("Enter your choice: ");
        
        int mealTypeChoice = getUserChoice();
        String mealType;
        Food[] foodOptions;
        
        switch (mealTypeChoice) {
            case 1:
                mealType = "breakfast";
                foodOptions = mealPlanningService.getBreakfastOptions();
                break;
            case 2:
                mealType = "lunch";
                foodOptions = mealPlanningService.getLunchOptions();
                break;
            case 3:
                mealType = "snack";
                foodOptions = mealPlanningService.getSnackOptions();
                break;
            case 4:
                mealType = "dinner";
                foodOptions = mealPlanningService.getDinnerOptions();
                break;
            default:
                System.out.println("Invalid meal type. Returning to menu.");
                return;
        }
        
        // Display food options for the selected meal type
        System.out.println("\nSelect Food for " + capitalize(mealType) + ":");
        for (int i = 0; i < foodOptions.length; i++) {
            System.out.println((i + 1) + ". " + foodOptions[i].getName());
        }
        System.out.print("Enter your choice (1-" + foodOptions.length + "): ");
        
        int foodChoice = getUserChoice();
        if (foodChoice < 1 || foodChoice > foodOptions.length) {
            System.out.println("Invalid food choice. Returning to menu.");
            return;
        }
        
        // Get selected food
        Food selectedFood = foodOptions[foodChoice - 1];
        
        // Get ingredients for the selected food
        List<ShoppingListService.Ingredient> ingredients = 
            shoppingListService.getIngredientsForFood(mealType, selectedFood.getName());
        
        if (ingredients.isEmpty()) {
            System.out.println("\nNo ingredients found for " + selectedFood.getName() + ".");
            System.out.println("This recipe may not be in our database yet.");
            return;
        }
        
        // Calculate total cost
        double totalCost = shoppingListService.calculateTotalCost(ingredients);
        
        // Display the shopping list
        System.out.println("\n===== Shopping List for " + selectedFood.getName() + " =====");
        System.out.println("\nIngredients:");
        
        for (ShoppingListService.Ingredient ingredient : ingredients) {
            System.out.println("- " + ingredient);
        }
        
        System.out.println("\nEstimated Total Cost: $" + String.format("%.2f", totalCost));
        
        // Pause before returning to menu
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Capitalizes the first letter of a string.
     * 
     * @param str The string to capitalize
     * @return The capitalized string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}