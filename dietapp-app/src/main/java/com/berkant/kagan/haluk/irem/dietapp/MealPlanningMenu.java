package com.berkant.kagan.haluk.irem.dietapp;

import java.util.List;
import java.util.Scanner;

/**
 * This class handles the meal planning menu operations for the Diet Planner application.
 * @details The MealPlanningMenu class provides menu interfaces for meal planning,
 *          food logging, and viewing meal history.
 * @author berkant
 */
public class MealPlanningMenu {
    // Service objects
    private MealPlanningService mealPlanningService;
    private AuthenticationService authService;
    private Scanner scanner;
    
    /**
     * Constructor for MealPlanningMenu class.
     * 
     * @param mealPlanningService The meal planning service
     * @param authService The authentication service
     * @param scanner The scanner for user input
     */
    public MealPlanningMenu(MealPlanningService mealPlanningService, AuthenticationService authService, Scanner scanner) {
        this.mealPlanningService = mealPlanningService;
        this.authService = authService;
        this.scanner = scanner;
    }
    
    /**
     * Displays the main meal planning menu and handles user selections.
     */
    public void displayMenu() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n===== Meal Planning and Logging =====");
            System.out.println("1. Plan Meals");
            System.out.println("2. Log Foods");
            System.out.println("3. View Meal History");
            System.out.println("0. Return to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = getUserChoice();
            
            switch (choice) {
                case 1:
                    handlePlanMeals();
                    break;
                case 2:
                    handleLogFoods();
                    break;
                case 3:
                    handleViewMealHistory();
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
     * Handles the meal planning process.
     */
    private void handlePlanMeals() {
        System.out.println("\n===== Plan Meals =====");
        
        // Get date information
        String date = getDateFromUser();
        if (date == null) {
            return; // Invalid date entered
        }
        
        // Get meal type
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
            System.out.println((i + 1) + ". " + foodOptions[i]);
        }
        System.out.print("Enter your choice (1-8): ");
        
        int foodChoice = getUserChoice();
        if (foodChoice < 1 || foodChoice > foodOptions.length) {
            System.out.println("Invalid food choice. Returning to menu.");
            return;
        }
        
        // Get selected food
        Food selectedFood = foodOptions[foodChoice - 1];
        
        String username = authService.getCurrentUser().getUsername();
        boolean success = mealPlanningService.addMealPlan(username, date, mealType, selectedFood);
        
        if (success) {
            System.out.println(selectedFood.getName() + " added to " + mealType + " successfully!");
        } else {
            System.out.println("Failed to add food to meal plan.");
        }
    }
    
    /**
     * Handles the food logging process.
     */
    private void handleLogFoods() {
        System.out.println("\n===== Log Foods =====");
        
        // Get date information
        String date = getDateFromUser();
        if (date == null) {
            return; // Invalid date entered
        }
        
        // Add food to the food log
        System.out.println("\nLog Food Consumed:");
        Food food = getFoodDetailsFromUser();
        
        if (food != null) {
            String username = authService.getCurrentUser().getUsername();
            boolean success = mealPlanningService.logFood(username, date, food);
            
            if (success) {
                System.out.println("Food logged successfully!");
            } else {
                System.out.println("Failed to log food.");
            }
        }
    }
    
    /**
     * Handles viewing the meal history.
     */
    private void handleViewMealHistory() {
        System.out.println("\n===== View Meal History =====");
        
        // Get date information
        String date = getDateFromUser();
        if (date == null) {
            return; // Invalid date entered
        }
        
        String username = authService.getCurrentUser().getUsername();
        boolean hasContent = false;
        
        // Check for planned meals for this date
        boolean hasPlannedMeals = false;
        System.out.println("\n--- Planned Meals for " + date + " ---");
        
        String[] mealTypes = {"breakfast", "lunch", "snack", "dinner"};
        for (String mealType : mealTypes) {
            List<Food> mealPlan = mealPlanningService.getMealPlan(username, date, mealType);
            if (!mealPlan.isEmpty()) {
                hasPlannedMeals = true;
                hasContent = true;
                System.out.println("\n" + capitalize(mealType) + ":");
                for (Food food : mealPlan) {
                    System.out.println("- " + food);
                }
            }
        }
        
        if (!hasPlannedMeals) {
            System.out.println("No planned meals found for this date.");
        }
        
        // Check for food logs for this date
        List<Food> foodLog = mealPlanningService.getFoodLog(username, date);
        System.out.println("\n--- Food Log for " + date + " ---");
        
        if (foodLog.isEmpty()) {
            System.out.println("No food logged for this date.");
        } else {
            hasContent = true;
            for (Food food : foodLog) {
                System.out.println("- " + food);
            }
            
            int totalCalories = mealPlanningService.getTotalCalories(username, date);
            System.out.println("\nTotal calories consumed: " + totalCalories);
        }
        
        if (!hasContent) {
            System.out.println("\nNo meal plans or food logs found for " + date);
        }
        
        // Pause before returning to menu
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Gets date information from the user.
     * 
     * @return The formatted date string or null if invalid
     */
    private String getDateFromUser() {
        int year = 0;
        int month = 0;
        int day = 0;
        boolean validDate = false;
        
        while (!validDate) {
            System.out.println("\nEnter Date:");
            
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
    
    /**
     * Gets food details from the user.
     * 
     * @return A Food object with the entered details or null if invalid
     */
    private Food getFoodDetailsFromUser() {
        System.out.print("Enter food name: ");
        String name = scanner.nextLine();
        
        double grams;
        System.out.print("Enter amount (grams): ");
        try {
            grams = Double.parseDouble(scanner.nextLine());
            if (grams <= 0) {
                System.out.println("Amount must be positive.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format.");
            return null;
        }
        
        int calories;
        System.out.print("Enter calories: ");
        try {
            calories = Integer.parseInt(scanner.nextLine());
            if (calories < 0) {
                System.out.println("Calories cannot be negative.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid calorie format.");
            return null;
        }
        
        return new Food(name, grams, calories);
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