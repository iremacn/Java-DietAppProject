package com.berkant.kagan.haluk.irem.dietapp;

import java.util.List;
import java.util.Scanner;

/**
 * This class handles the calorie and nutrient tracking menu operations for the Diet Planner application.
 * @details The CalorieNutrientTrackingMenu class provides menu interfaces for setting nutrition goals,
 *          tracking nutrients, and viewing nutrition reports.
 * @author irem
 */
public class CalorieNutrientTrackingMenu {
    // Service objects
    private CalorieNutrientTrackingService calorieNutrientService;
    private MealPlanningService mealPlanningService;
    private AuthenticationService authService;
    private Scanner scanner;
    
    /**
     * Constructor for CalorieNutrientTrackingMenu class.
     * 
     * @param calorieNutrientService The calorie and nutrient tracking service
     * @param mealPlanningService The meal planning service
     * @param authService The authentication service
     * @param scanner The scanner for user input
     */
    public CalorieNutrientTrackingMenu(CalorieNutrientTrackingService calorieNutrientService,
                                      MealPlanningService mealPlanningService,
                                      AuthenticationService authService,
                                      Scanner scanner) {
        this.calorieNutrientService = calorieNutrientService;
        this.mealPlanningService = mealPlanningService;
        this.authService = authService;
        this.scanner = scanner;
    }
    
    /**
     * Displays the main calorie and nutrient tracking menu and handles user selections.
     */
    public void displayMenu() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n===== Calorie and Nutrient Tracking =====");
            System.out.println("1. Set Nutrition Goals");
            System.out.println("2. Log Food with Nutrients");
            System.out.println("3. View Nutrition Report");
            System.out.println("4. Calculate Suggested Calories");
            System.out.println("0. Return to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = getUserChoice();
            
            switch (choice) {
                case 1:
                    handleSetNutritionGoals();
                    break;
                case 2:
                    handleLogFoodWithNutrients();
                    break;
                case 3:
                    handleViewNutritionReport();
                    break;
                case 4:
                    handleCalculateSuggestedCalories();
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
     * Handles setting nutrition goals.
     */
    private void handleSetNutritionGoals() {
        System.out.println("\n===== Set Nutrition Goals =====");
        String username = authService.getCurrentUser().getUsername();
        
        // Get current goals if any
        CalorieNutrientTrackingService.NutritionGoal currentGoals = calorieNutrientService.getNutritionGoals(username);
        
        System.out.println("Current Goals:");
        System.out.println("- Calories: " + currentGoals.getCalorieGoal() + " cal");
        System.out.println("- Protein: " + currentGoals.getProteinGoal() + " g");
        System.out.println("- Carbohydrates: " + currentGoals.getCarbGoal() + " g");
        System.out.println("- Fat: " + currentGoals.getFatGoal() + " g");
        
        System.out.println("\nEnter new goals (leave blank to keep current):");
        
        int calorieGoal;
        double proteinGoal, carbGoal, fatGoal;
        
        System.out.print("Daily Calorie Goal (calories): ");
        String calorieInput = scanner.nextLine();
        calorieGoal = calorieInput.isEmpty() ? currentGoals.getCalorieGoal() : 
                        Integer.parseInt(calorieInput);
        
        System.out.print("Daily Protein Goal (grams): ");
        String proteinInput = scanner.nextLine();
        proteinGoal = proteinInput.isEmpty() ? currentGoals.getProteinGoal() : 
                        Double.parseDouble(proteinInput);
        
        System.out.print("Daily Carbohydrate Goal (grams): ");
        String carbInput = scanner.nextLine();
        carbGoal = carbInput.isEmpty() ? currentGoals.getCarbGoal() : 
                    Double.parseDouble(carbInput);
        
        System.out.print("Daily Fat Goal (grams): ");
        String fatInput = scanner.nextLine();
        fatGoal = fatInput.isEmpty() ? currentGoals.getFatGoal() : 
                    Double.parseDouble(fatInput);
        
        // Set the new goals
        boolean success = calorieNutrientService.setNutritionGoals(username, calorieGoal, 
                                                                  proteinGoal, carbGoal, fatGoal);
        
        if (success) {
            System.out.println("Nutrition goals updated successfully!");
        } else {
            System.out.println("Failed to update nutrition goals.");
        }
    }
    
    /**
     * Handles logging food with detailed nutrient information.
     */
    private void handleLogFoodWithNutrients() {
        System.out.println("\n===== Log Food with Nutrients =====");
        
        // Get date information
        String date = getDateFromUser();
        if (date == null) {
            return; // Invalid date entered
        }
        
        System.out.println("\nSelect an option:");
        System.out.println("1. Choose from common foods");
        System.out.println("2. Enter custom food with nutrients");
        System.out.print("Enter your choice: ");
        
        int choice = getUserChoice();
        
        FoodNutrient foodNutrient = null;
        
        switch (choice) {
            case 1:
                foodNutrient = selectCommonFood();
                break;
            case 2:
                foodNutrient = enterCustomFoodWithNutrients();
                break;
            default:
                System.out.println("Invalid choice. Returning to menu.");
                return;
        }
        
        if (foodNutrient != null) {
            String username = authService.getCurrentUser().getUsername();
            boolean success = mealPlanningService.logFood(username, date, foodNutrient);
            
            if (success) {
                System.out.println("Food with nutrients logged successfully!");
            } else {
                System.out.println("Failed to log food.");
            }
        }
    }
    
    /**
     * Allows user to select from a list of common foods with nutrient information.
     * 
     * @return The selected FoodNutrient object or null if cancelled
     */
    private FoodNutrient selectCommonFood() {
        FoodNutrient[] commonFoods = calorieNutrientService.getCommonFoodsWithNutrients();
        
        System.out.println("\nSelect a food from the list:");
        for (int i = 0; i < commonFoods.length; i++) {
            System.out.println((i + 1) + ". " + commonFoods[i].getName());
        }
        System.out.print("Enter your choice (1-" + commonFoods.length + "): ");
        
        int choice = getUserChoice();
        if (choice < 1 || choice > commonFoods.length) {
            System.out.println("Invalid choice.");
            return null;
        }
        
        FoodNutrient selectedFood = commonFoods[choice - 1];
        
        // Allow customizing the portion size
        System.out.println("\nSelected: " + selectedFood.toDetailedString());
        System.out.print("Enter portion size in grams (or press Enter for default " + 
                          selectedFood.getGrams() + "g): ");
        
        String portionInput = scanner.nextLine();
        if (!portionInput.isEmpty()) {
            try {
                double newGrams = Double.parseDouble(portionInput);
                if (newGrams <= 0) {
                    System.out.println("Portion size must be positive. Using default.");
                } else {
                    // Scale nutrients based on new portion size
                    double ratio = newGrams / selectedFood.getGrams();
                    
                    FoodNutrient scaledFood = new FoodNutrient(
                        selectedFood.getName(),
                        newGrams,
                        (int) Math.round(selectedFood.getCalories() * ratio),
                        selectedFood.getProtein() * ratio,
                        selectedFood.getCarbs() * ratio,
                        selectedFood.getFat() * ratio,
                        selectedFood.getFiber() * ratio,
                        selectedFood.getSugar() * ratio,
                        selectedFood.getSodium() * ratio
                    );
                    
                    return scaledFood;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Using default portion size.");
            }
        }
        
        return selectedFood;
    }
    
    /**
     * Allows user to enter custom food with detailed nutrient information.
     * 
     * @return The custom FoodNutrient object or null if cancelled
     */
    private FoodNutrient enterCustomFoodWithNutrients() {
        System.out.println("\nEnter Custom Food with Nutrients:");
        
        System.out.print("Enter food name: ");
        String name = scanner.nextLine();
        if (name.isEmpty()) {
            System.out.println("Food name is required.");
            return null;
        }
        
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
        
        // Get nutrient information
        System.out.println("\nEnter nutrient information (press Enter to skip):");
        
        double protein = getDoubleInputOrDefault("Protein (g): ", 0);
        double carbs = getDoubleInputOrDefault("Carbohydrates (g): ", 0);
        double fat = getDoubleInputOrDefault("Fat (g): ", 0);
        double fiber = getDoubleInputOrDefault("Fiber (g): ", 0);
        double sugar = getDoubleInputOrDefault("Sugar (g): ", 0);
        double sodium = getDoubleInputOrDefault("Sodium (mg): ", 0);
                
                return new FoodNutrient(name, grams, calories, protein, carbs, fat, fiber, sugar, sodium);
            }
            
            /**
             * Gets a double input from the user or returns a default value if input is empty.
             * 
             * @param prompt The prompt to display to the user
             * @param defaultValue The default value to return if input is empty
             * @return The user input as a double or the default value
             */
            private double getDoubleInputOrDefault(String prompt, double defaultValue) {
                System.out.print(prompt);
                String input = scanner.nextLine();
                
                if (input.isEmpty()) {
                    return defaultValue;
                }
                
                try {
                    double value = Double.parseDouble(input);
                    return value < 0 ? defaultValue : value;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Using default value.");
                    return defaultValue;
                }
            }
            
            /**
             * Handles viewing the nutrition report.
             */
            private void handleViewNutritionReport() {
                System.out.println("\n===== View Nutrition Report =====");
                
                // Get date information
                String date = getDateFromUser();
                if (date == null) {
                    return; // Invalid date entered
                }
                
                String username = authService.getCurrentUser().getUsername();
                
                // Get the nutrition report for the date
                CalorieNutrientTrackingService.NutritionReport report = 
                    calorieNutrientService.getNutritionReport(username, date);
                
                System.out.println("\n--- Nutrition Report for " + date + " ---");
                
                // Display nutrition report
                List<Food> foods = mealPlanningService.getFoodLog(username, date);
                if (foods.isEmpty()) {
                    System.out.println("No food logged for this date.");
                } else {
                    System.out.println("Foods consumed:");
                    for (Food food : foods) {
                        System.out.println("- " + food);
                    }
                    
                    System.out.println("\nNutrition Summary:");
                    System.out.println("- Calories: " + report.getTotalCalories() + " / " + 
                                      report.getGoals().getCalorieGoal() + " cal (" + 
                                      String.format("%.1f", report.getCaloriePercentage()) + "%)");
                    
                    System.out.println("- Protein: " + String.format("%.1f", report.getTotalProtein()) + " / " + 
                                      report.getGoals().getProteinGoal() + " g (" + 
                                      String.format("%.1f", report.getProteinPercentage()) + "%)");
                    
                    System.out.println("- Carbohydrates: " + String.format("%.1f", report.getTotalCarbs()) + " / " + 
                                      report.getGoals().getCarbGoal() + " g (" + 
                                      String.format("%.1f", report.getCarbPercentage()) + "%)");
                    
                    System.out.println("- Fat: " + String.format("%.1f", report.getTotalFat()) + " / " + 
                                      report.getGoals().getFatGoal() + " g (" + 
                                      String.format("%.1f", report.getFatPercentage()) + "%)");
                    
                    if (report.getTotalFiber() > 0) {
                        System.out.println("- Fiber: " + String.format("%.1f", report.getTotalFiber()) + " g");
                    }
                    
                    if (report.getTotalSugar() > 0) {
                        System.out.println("- Sugar: " + String.format("%.1f", report.getTotalSugar()) + " g");
                    }
                    
                    if (report.getTotalSodium() > 0) {
                        System.out.println("- Sodium: " + String.format("%.1f", report.getTotalSodium()) + " mg");
                    }
                    
                    // Provide basic analysis
                    analyzeNutritionReport(report);
                }
                
                // Pause before returning to menu
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
            
            /**
             * Analyzes a nutrition report and provides feedback.
             * 
             * @param report The nutrition report to analyze
             */
            private void analyzeNutritionReport(CalorieNutrientTrackingService.NutritionReport report) {
                System.out.println("\nNutrition Analysis:");
                
                // Check if calorie intake is within a reasonable range
                if (report.getCaloriePercentage() < 80) {
                    System.out.println("- Your calorie intake is below 80% of your goal. " +
                                       "Consider eating more to meet your energy needs.");
                } else if (report.getCaloriePercentage() > 120) {
                    System.out.println("- Your calorie intake is above 120% of your goal. " +
                                       "Consider moderating your intake.");
                } else {
                    System.out.println("- Your calorie intake is within a good range of your goal.");
                }
                
                // Check macronutrient balance
                if (report.getProteinPercentage() < 80) {
                    System.out.println("- Your protein intake is below target. Protein is important for " +
                                       "muscle maintenance and recovery.");
                }
                
                if (report.getCarbPercentage() < 70 || report.getCarbPercentage() > 130) {
                    System.out.println("- Your carbohydrate intake is " + 
                                      (report.getCarbPercentage() < 70 ? "below" : "above") + 
                                      " the recommended range. Carbs provide energy for daily activities.");
                }
                
                if (report.getFatPercentage() < 70) {
                    System.out.println("- Your fat intake is low. Healthy fats are essential for hormone " +
                                       "production and vitamin absorption.");
                } else if (report.getFatPercentage() > 130) {
                    System.out.println("- Your fat intake is high. Consider focusing on healthy fat sources " +
                                       "like avocados, nuts, and olive oil.");
                }
            }
            
            /**
             * Handles the calculation of suggested calories.
             */
            private void handleCalculateSuggestedCalories() {
                System.out.println("\n===== Calculate Suggested Calories =====");
                
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
                
                // Calculate suggested calories
                int suggestedCalories = calorieNutrientService.calculateSuggestedCalories(
                    gender, age, heightCm, weightKg, activityLevel);
                
                System.out.println("\nBased on your information:");
                System.out.println("- Your suggested daily calorie intake is: " + suggestedCalories + " calories");
                
                // Suggest macronutrient distribution
                System.out.println("\nRecommended macronutrient distribution:");
                
                // Protein: 15-25% of calories (4 calories per gram)
                double proteinCalories = suggestedCalories * 0.20; // 20% of calories
                double proteinGrams = proteinCalories / 4.0;
                
                // Carbs: 45-65% of calories (4 calories per gram)
                double carbCalories = suggestedCalories * 0.55; // 55% of calories
                double carbGrams = carbCalories / 4.0;
                
                // Fat: 20-35% of calories (9 calories per gram)
                double fatCalories = suggestedCalories * 0.25; // 25% of calories
                double fatGrams = fatCalories / 9.0;
                
                System.out.println("- Protein: " + String.format("%.0f", proteinGrams) + " g (" + 
                                   String.format("%.0f", proteinCalories) + " calories, 20%)");
                System.out.println("- Carbohydrates: " + String.format("%.0f", carbGrams) + " g (" + 
                                   String.format("%.0f", carbCalories) + " calories, 55%)");
                System.out.println("- Fat: " + String.format("%.0f", fatGrams) + " g (" + 
                                   String.format("%.0f", fatCalories) + " calories, 25%)");
                
                // Ask if user wants to set these as goals
                System.out.print("\nWould you like to set these as your nutrition goals? (Y/N): ");
                String choice = scanner.nextLine().toUpperCase();
                
                if (choice.equals("Y")) {
                    String username = authService.getCurrentUser().getUsername();
                    boolean success = calorieNutrientService.setNutritionGoals(
                        username, suggestedCalories, proteinGrams, carbGrams, fatGrams);
                    
                    if (success) {
                        System.out.println("Nutrition goals updated successfully!");
                    } else {
                        System.out.println("Failed to update nutrition goals.");
                    }
                }
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
        }