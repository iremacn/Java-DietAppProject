package com.berkant.kagan.haluk.irem.dietapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.berkant.kagan.haluk.irem.dietapp.PersonalizedDietRecommendationService.DietRecommendation;
import com.berkant.kagan.haluk.irem.dietapp.PersonalizedDietRecommendationService.DietType;
import com.berkant.kagan.haluk.irem.dietapp.PersonalizedDietRecommendationService.MacronutrientDistribution;
import com.berkant.kagan.haluk.irem.dietapp.PersonalizedDietRecommendationService.RecommendedMeal;
import com.berkant.kagan.haluk.irem.dietapp.PersonalizedDietRecommendationService.WeightGoal;

/**
 * This class handles the personalized diet recommendation menu operations for the Diet Planner application.
 * @details The PersonalizedDietRecommendationMenu class provides menu interfaces for setting diet preferences,
 *          generating and viewing personalized diet recommendations.
 * @author haluk
 */
public class PersonalizedDietRecommendationMenu {
    // Service objects
    private PersonalizedDietRecommendationService personalizedDietService;
    private AuthenticationService authService;
    private Scanner scanner;
    
    // Store the last generated recommendation for viewing
    private DietRecommendation lastRecommendation;
    
    /**
     * Constructor for PersonalizedDietRecommendationMenu class.
     * 
     * @param personalizedDietService The personalized diet recommendation service
     * @param authService The authentication service
     * @param scanner The scanner for user input
     */
    public PersonalizedDietRecommendationMenu(
            PersonalizedDietRecommendationService personalizedDietService,
            AuthenticationService authService,
            Scanner scanner) {
        this.personalizedDietService = personalizedDietService;
        this.authService = authService;
        this.scanner = scanner;
        this.lastRecommendation = null;
    }
    
    /**
     * Displays the main personalized diet recommendation menu and handles user selections.
     */
    public void displayMenu() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n===== Personalized Diet Recommendations =====");
            System.out.println("1. Set Diet Preferences");
            System.out.println("2. Generate Diet Recommendations");
            System.out.println("3. View Recommendations");
            System.out.println("4. View Example Diet Plans");
            System.out.println("0. Return to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = getUserChoice();
            
            switch (choice) {
                case 1:
                    handleSetDietPreferences();
                    break;
                case 2:
                    handleGenerateRecommendations();
                    break;
                case 3:
                    handleViewRecommendations();
                    break;
                case 4:
                    handleViewExampleDietPlans();
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
     * Handles setting diet preferences.
     */
    private void handleSetDietPreferences() {
        System.out.println("\n===== Set Diet Preferences =====");
        
        // Get diet type preference
        System.out.println("\nSelect Diet Type:");
        System.out.println("1. Balanced");
        System.out.println("2. Low Carb");
        System.out.println("3. High Protein");
        System.out.println("4. Vegetarian");
        System.out.println("5. Vegan");
        System.out.print("Enter your choice (1-5): ");
        
        int dietTypeChoice = getUserChoice();
        DietType dietType;
        
        switch (dietTypeChoice) {
            case 1:
                dietType = DietType.BALANCED;
                break;
            case 2:
                dietType = DietType.LOW_CARB;
                break;
            case 3:
                dietType = DietType.HIGH_PROTEIN;
                break;
            case 4:
                dietType = DietType.VEGETARIAN;
                break;
            case 5:
                dietType = DietType.VEGAN;
                break;
            default:
                System.out.println("Invalid choice. Using Balanced diet type as default.");
                dietType = DietType.BALANCED;
                break;
        }
        
        // Get weight goal
        System.out.println("\nSelect Weight Goal:");
        System.out.println("1. Lose Weight");
        System.out.println("2. Maintain Weight");
        System.out.println("3. Gain Weight");
        System.out.print("Enter your choice (1-3): ");
        
        int weightGoalChoice = getUserChoice();
        WeightGoal weightGoal;
        
        switch (weightGoalChoice) {
            case 1:
                weightGoal = WeightGoal.LOSE;
                break;
            case 2:
                weightGoal = WeightGoal.MAINTAIN;
                break;
            case 3:
                weightGoal = WeightGoal.GAIN;
                break;
            default:
                System.out.println("Invalid choice. Using Maintain Weight as default.");
                weightGoal = WeightGoal.MAINTAIN;
                break;
        }
        
        // Get health conditions/allergies
        System.out.println("\nDo you have any health conditions or allergies? (Y/N): ");
        String hasConditions = scanner.nextLine().toUpperCase();
        
        List<String> healthConditions = new ArrayList<>();
        if (hasConditions.equals("Y")) {
            System.out.println("Enter your health conditions/allergies (comma separated): ");
            String conditionsInput = scanner.nextLine();
            
            if (!conditionsInput.isEmpty()) {
                String[] conditions = conditionsInput.split(",");
                for (String condition : conditions) {
                    healthConditions.add(condition.trim());
                }
            }
        }
        
        // Get excluded foods
        System.out.println("\nDo you want to exclude any specific foods? (Y/N): ");
        String hasExclusions = scanner.nextLine().toUpperCase();
        
        List<String> excludedFoods = new ArrayList<>();
        if (hasExclusions.equals("Y")) {
            System.out.println("Enter foods to exclude (comma separated): ");
            String exclusionsInput = scanner.nextLine();
            
            if (!exclusionsInput.isEmpty()) {
                String[] exclusions = exclusionsInput.split(",");
                for (String exclusion : exclusions) {
                    excludedFoods.add(exclusion.trim().toLowerCase());
                }
            }
        }
        
        // Save user diet profile
        String username = authService.getCurrentUser().getUsername();
        boolean success = personalizedDietService.setUserDietProfile(
            username, dietType, healthConditions, weightGoal, excludedFoods);
        
        if (success) {
            System.out.println("Diet preferences updated successfully!");
        } else {
            System.out.println("Failed to update diet preferences.");
        }
    }
    
    /**
     * Handles generating personalized diet recommendations.
     */
    private void handleGenerateRecommendations() {
        System.out.println("\n===== Generate Diet Recommendations =====");
        
        // Get user information
        char gender;
        while (true) {
            System.out.print("Enter gender (M/F): ");
            String input = scanner.nextLine().toUpperCase();
            if (input.equals("M") || input.equals("F")) {
                gender = input.charAt(0);
                break;
            } else {
                System.out.println("Please enter 'M' for male or 'F' for female.");
            }
        }
        
        int age;
        while (true) {
            System.out.print("Enter age: ");
            try {
                age = Integer.parseInt(scanner.nextLine());
                if (age > 0 && age < 120) {
                    break;
                } else {
                    System.out.println("Please enter a valid age between 1 and 120.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        double heightCm;
        while (true) {
            System.out.print("Enter height (cm): ");
            try {
                heightCm = Double.parseDouble(scanner.nextLine());
                if (heightCm > 0) {
                    break;
                } else {
                    System.out.println("Height must be positive.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        double weightKg;
        while (true) {
            System.out.print("Enter weight (kg): ");
            try {
                weightKg = Double.parseDouble(scanner.nextLine());
                if (weightKg > 0) {
                    break;
                } else {
                    System.out.println("Weight must be positive.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        System.out.println("\nSelect your activity level:");
        System.out.println("1. Sedentary (little or no exercise)");
        System.out.println("2. Lightly active (light exercise/sports 1-3 days/week)");
        System.out.println("3. Moderately active (moderate exercise/sports 3-5 days/week)");
        System.out.println("4. Very active (hard exercise/sports 6-7 days/week)");
        System.out.println("5. Extra active (very hard exercise & physical job or training twice a day)");
        System.out.print("Enter your choice (1-5): ");
        
        int activityLevel;
        while (true) {
            try {
                activityLevel = Integer.parseInt(scanner.nextLine());
                if (activityLevel >= 1 && activityLevel <= 5) {
                    break;
                } else {
                    System.out.println("Please enter a number between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Generate diet recommendations
        String username = authService.getCurrentUser().getUsername();
        
        System.out.println("\nGenerating personalized diet recommendations...");
        this.lastRecommendation = personalizedDietService.generateRecommendations(
            username, gender, age, heightCm, weightKg, activityLevel);
        
        System.out.println("Diet recommendations generated successfully!");
        System.out.println("\nPress Enter to view your diet recommendations...");
        scanner.nextLine();
        
        handleViewRecommendations();
    }
    
    /**
     * Handles viewing personalized diet recommendations.
     */
    private void handleViewRecommendations() {
        System.out.println("\n===== View Diet Recommendations =====");
        
        if (lastRecommendation == null) {
            System.out.println("No diet recommendations have been generated yet.");
            System.out.println("Please use 'Generate Diet Recommendations' option first.");
            
            // Pause before returning to menu
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Display diet recommendation summary
        System.out.println("\n--- Personalized Diet Recommendation Summary ---");
        System.out.println("Daily Calorie Target: " + lastRecommendation.getDailyCalories() + " calories");
        System.out.println("Macronutrient Distribution: " + lastRecommendation.getMacros().toString());
        
        // Display meal plan
        System.out.println("\n--- Daily Meal Plan ---");
        for (RecommendedMeal meal : lastRecommendation.getMeals()) {
            System.out.println("\n" + meal.getMealType() + ":");
            System.out.println("Target: " + meal.getTargetCalories() + " calories, " +
                              meal.getTargetProtein() + "g protein, " +
                              meal.getTargetCarbs() + "g carbs, " +
                              meal.getTargetFat() + "g fat");
            
            List<Food> foods = meal.getFoods();
            if (foods.isEmpty()) {
                System.out.println("No specific foods recommended.");
            } else {
                System.out.println("Recommended foods:");
                for (Food food : foods) {
                    System.out.println("- " + food.toString());
                }
                System.out.println("Total calories: " + meal.getTotalCalories());
            }
        }
        
        // Display dietary guidelines
        System.out.println("\n--- Dietary Guidelines ---");
        List<String> guidelines = lastRecommendation.getDietaryGuidelines();
        for (int i = 0; i < guidelines.size(); i++) {
            System.out.println((i + 1) + ". " + guidelines.get(i));
        }
        
        // Pause before returning to menu
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Handles viewing example diet plans.
     */
    private void handleViewExampleDietPlans() {
        System.out.println("\n===== Example Diet Plans =====");
        
        String[] examplePlans = personalizedDietService.getExampleDietPlans();
        
        for (int i = 0; i < examplePlans.length; i++) {
            System.out.println("\n" + examplePlans[i]);
        }
        
        // Pause before returning to menu
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}