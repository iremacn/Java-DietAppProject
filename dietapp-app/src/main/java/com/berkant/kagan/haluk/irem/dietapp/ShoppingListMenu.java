package com.berkant.kagan.haluk.irem.dietapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * This class handles the shopping list menu operations for the Diet Planner application.
 * @details The ShoppingListMenu class provides menu interfaces for generating,
 *          viewing, and managing shopping lists.
 * @author [your_name]
 */
public class ShoppingListMenu {
    // Service objects
    private ShoppingListService shoppingListService;
    private MealPlanningService mealPlanningService;
    private AuthenticationService authService;
    private Scanner scanner;
    
    // Son oluşturulan alışveriş listesini saklamak için
    private List<Ingredient> lastGeneratedList;
    
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
        this.lastGeneratedList = new ArrayList<>();
    }
    
    /**
     * Displays the main shopping list menu and handles user selections.
     */
    public void displayMenu() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n===== Shopping List Generator =====");
            System.out.println("1. Generate Shopping List");
            System.out.println("2. View Last Generated List");
            System.out.println("3. Filter Shopping List by Category");
            System.out.println("0. Return to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = getUserChoice();
            
            switch (choice) {
                case 1:
                    handleGenerateShoppingList();
                    break;
                case 2:
                    handleViewLastGeneratedList();
                    break;
                case 3:
                    handleFilterByCategory();
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
     * Handles generating a shopping list.
     */
    private void handleGenerateShoppingList() {
        System.out.println("\n===== Generate Shopping List =====");
        
        // Get start date
        String startDate = getDateFromUser("start");
        if (startDate == null) {
            return; // Invalid date entered
        }
        
        // Get end date
        String endDate = getDateFromUser("end");
        if (endDate == null) {
            return; // Invalid date entered
        }
        
        // Generate the shopping list
        String username = authService.getCurrentUser().getUsername();
        List<Ingredient> shoppingList = shoppingListService.generateShoppingList(username, startDate, endDate);
        
        // Save the list for later viewing
        this.lastGeneratedList = shoppingList;
        
        // Display the list
        displayShoppingList(shoppingList);
    }
    
    /**
     * Handles viewing the last generated shopping list.
     */
    private void handleViewLastGeneratedList() {
        System.out.println("\n===== Last Generated Shopping List =====");
        
        if (lastGeneratedList.isEmpty()) {
            System.out.println("No shopping list has been generated yet.");
            System.out.println("Please use 'Generate Shopping List' option first.");
        } else {
            displayShoppingList(lastGeneratedList);
        }
    }
    
    /**
     * Handles filtering the shopping list by category.
     */
    private void handleFilterByCategory() {
        System.out.println("\n===== Filter Shopping List by Category =====");
        
        if (lastGeneratedList.isEmpty()) {
            System.out.println("No shopping list has been generated yet.");
            System.out.println("Please use 'Generate Shopping List' option first.");
            
            // Pause before returning to menu
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Get all available categories from the last generated list
        Map<String, List<Ingredient>> categorizedList = 
            shoppingListService.categorizeIngredients(lastGeneratedList);
        
        // Show available categories
        System.out.println("\nAvailable Categories:");
        String[] categories = categorizedList.keySet().toArray(new String[0]);
        
        for (int i = 0; i < categories.length; i++) {
            System.out.println((i + 1) + ". " + categories[i]);
        }
        System.out.println((categories.length + 1) + ". All Categories");
        System.out.print("\nSelect a category to filter (1-" + (categories.length + 1) + "): ");
        
        int categoryChoice = getUserChoice();
        
        if (categoryChoice < 1 || categoryChoice > categories.length + 1) {
            System.out.println("Invalid category selection. Returning to menu.");
            return;
        }
        
        if (categoryChoice == categories.length + 1) {
            // Show all categories (unfiltered)
            System.out.println("\n===== All Categories =====");
            displayShoppingList(lastGeneratedList);
        } else {
            // Show selected category
            String selectedCategory = categories[categoryChoice - 1];
            System.out.println("\n===== Category: " + selectedCategory + " =====");
            
            List<Ingredient> filteredList = categorizedList.get(selectedCategory);
            if (filteredList != null && !filteredList.isEmpty()) {
                for (Ingredient ingredient : filteredList) {
                    System.out.println("- " + ingredient);
                }
                System.out.println("\nTotal Items in " + selectedCategory + ": " + filteredList.size());
            } else {
                System.out.println("No items found in this category.");
            }
            
            // Pause before returning to menu
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    /**
     * Displays a shopping list.
     * 
     * @param ingredients The list of ingredients to display
     */
    private void displayShoppingList(List<Ingredient> ingredients) {
        if (ingredients.isEmpty()) {
            System.out.println("\nNo ingredients found for the selected date range.");
            return;
        }
        
        System.out.println("\n===== Your Shopping List =====");
        
        // Get categorized ingredients
        Map<String, List<Ingredient>> categorized = 
            shoppingListService.categorizeIngredients(ingredients);
        
        // Display by category
        for (String category : categorized.keySet()) {
            System.out.println("\n" + category + ":");
            
            List<Ingredient> categoryIngredients = categorized.get(category);
            for (Ingredient ingredient : categoryIngredients) {
                System.out.println("- " + ingredient);
            }
        }
        
        // Display total count
        System.out.println("\nTotal Items: " + ingredients.size());
        
        // Pause before returning to menu
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Gets date information from the user.
     * 
     * @param dateType The type of date (start or end)
     * @return The formatted date string or null if invalid
     */
    private String getDateFromUser(String dateType) {
        int year = 0;
        int month = 0;
        int day = 0;
        boolean validDate = false;
        
        while (!validDate) {
            System.out.println("\nEnter " + dateType + " date:");
            
            // Get year and validate range
            System.out.print("Year (2025-2100): ");
            try {
                year = Integer.parseInt(scanner.nextLine());
                if (year < 2025 || year > 2100) {
                    System.out.println("Invalid year. Please enter a year between 2025 and 2100.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid year format. Please enter a valid number.");
                continue;
            }
            
            // Get month and validate range
            System.out.print("Month (1-12): ");
            try {
                month = Integer.parseInt(scanner.nextLine());
                if (month < 1 || month > 12) {
                    System.out.println("Invalid month. Please enter a month between 1 and 12.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid month format. Please enter a valid number.");
                continue;
            }
            
            // Get day and validate range
            System.out.print("Day (1-31): ");
            try {
                day = Integer.parseInt(scanner.nextLine());
                if (day < 1 || day > 31) {
                    System.out.println("Invalid day. Please enter a day between 1 and 31.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid day format. Please enter a valid number.");
                continue;
            }
            
            // Additional date validation (days in month)
            if (!mealPlanningService.isValidDate(year, month, day)) {
                System.out.println("Invalid date. Please check the number of days in the selected month.");
                continue;
            }
            
            validDate = true;
        }
        
        return mealPlanningService.formatDate(year, month, day);
    }
}