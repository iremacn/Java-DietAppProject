package com.berkant.kagan.haluk.irem.dietapp;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class handles the calorie and nutrient tracking menu operations for the Diet Planner application.
 * @details The CalorieNutrientTrackingMenu class provides menu interfaces for tracking calories
 *          and nutrients, setting goals, and viewing nutrition reports.
 * @author irem
 */
public class CalorieNutrientTrackingMenu {
    /** The calorie and nutrient tracking service */
    private CalorieNutrientTrackingService calorieNutrientService;
    /** The meal planning service */
    private MealPlanningService mealPlanningService;
    /** The authentication service */
    private AuthenticationService authService;
    /** Scanner for reading user input */
    private Scanner scanner;
    
    /**
     * Constructor for CalorieNutrientTrackingMenu class.
     * 
     * @param calorieNutrientService The calorie and nutrient tracking service
     * @param mealPlanningService The meal planning service
     * @param authService The authentication service
     * @param scanner The scanner for user input
     */
    public CalorieNutrientTrackingMenu(
            CalorieNutrientTrackingService calorieNutrientService,
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
            System.out.println("2. View Daily Nutrition Report");
            System.out.println("3. View Weekly Nutrition Report");
            System.out.println("4. Calculate Suggested Calories");
            System.out.println("5. Browse Common Foods with Nutrients");
            System.out.println("0. Return to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = getUserChoice();
            
            switch (choice) {
                case 1:
                    handleSetNutritionGoals();
                    break;
                case 2:
                    handleViewDailyReport();
                    break;
                case 3:
                    handleViewWeeklyReport();
                    break;
                case 4:
                    handleCalculateSuggestedCalories();
                    break;
                case 5:
                    handleBrowseCommonFoods();
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
     * @return The user's choice as an integer, returns -1 if input is invalid
     */
    private int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1; // Invalid input
        }
    }
    
    /**
     * Handles setting nutrition goals through user interaction.
     * @details Guides the user through setting daily goals for calories, protein,
     *          carbohydrates, and fat, then saves these goals to their profile.
     */
    private void handleSetNutritionGoals() {
        System.out.println("\n===== Set Nutrition Goals =====");
        
        // Get calorie goal
        int calorieGoal = 0;
        while (calorieGoal <= 0) {
            System.out.print("Enter daily calorie goal: ");
            try {
                calorieGoal = Integer.parseInt(scanner.nextLine());
                if (calorieGoal <= 0) {
                    System.out.println("Calorie goal must be positive. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Get protein goal
        double proteinGoal = 0;
        while (proteinGoal <= 0) {
            System.out.print("Enter daily protein goal (grams): ");
            try {
                proteinGoal = Double.parseDouble(scanner.nextLine());
                if (proteinGoal <= 0) {
                    System.out.println("Protein goal must be positive. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Get carb goal
        double carbGoal = 0;
        while (carbGoal <= 0) {
            System.out.print("Enter daily carbohydrate goal (grams): ");
            try {
                carbGoal = Double.parseDouble(scanner.nextLine());
                if (carbGoal <= 0) {
                    System.out.println("Carbohydrate goal must be positive. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Get fat goal
        double fatGoal = 0;
        while (fatGoal <= 0) {
            System.out.print("Enter daily fat goal (grams): ");
            try {
                fatGoal = Double.parseDouble(scanner.nextLine());
                if (fatGoal <= 0) {
                    System.out.println("Fat goal must be positive. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Save nutrition goals
        String username = authService.getCurrentUser().getUsername();
        boolean success = calorieNutrientService.setNutritionGoals(username, calorieGoal, proteinGoal, carbGoal, fatGoal);
        
        if (success) {
            System.out.println("Nutrition goals updated successfully!");
        } else {
            System.out.println("Failed to update nutrition goals.");
        }
    }
    
    /**
     * Handles viewing the daily nutrition report.
     * @details Displays a detailed report of calories and nutrients consumed for a specific date,
     *          including comparisons against daily goals and percentage achievements.
     */
    private void handleViewDailyReport() {
        System.out.println("\n===== Daily Nutrition Report =====");
        
        // Get date
        String date = getDateInput();
        if (date.isEmpty()) {
            return; // User canceled
        }
        
        // Get the nutrition report
        String username = authService.getCurrentUser().getUsername();
        CalorieNutrientTrackingService.NutritionReport report = 
            calorieNutrientService.getNutritionReport(username, date);
        
        // Display the report
        System.out.println("\nNutrition Report for " + date);
        System.out.println("----------------------------------");
        System.out.println("Calories: " + report.getTotalCalories() + " / " + 
                          report.getGoals().getCalorieGoal() + " (" + 
                          String.format("%.1f", report.getCaloriePercentage()) + "%)");
        
        System.out.println("Protein: " + String.format("%.1f", report.getTotalProtein()) + "g / " + 
                          report.getGoals().getProteinGoal() + "g (" + 
                          String.format("%.1f", report.getProteinPercentage()) + "%)");
        
        System.out.println("Carbs: " + String.format("%.1f", report.getTotalCarbs()) + "g / " + 
                          report.getGoals().getCarbGoal() + "g (" + 
                          String.format("%.1f", report.getCarbPercentage()) + "%)");
        
        System.out.println("Fat: " + String.format("%.1f", report.getTotalFat()) + "g / " + 
                          report.getGoals().getFatGoal() + "g (" + 
                          String.format("%.1f", report.getFatPercentage()) + "%)");
        
        System.out.println("Fiber: " + String.format("%.1f", report.getTotalFiber()) + "g");
        System.out.println("Sugar: " + String.format("%.1f", report.getTotalSugar()) + "g");
        System.out.println("Sodium: " + String.format("%.1f", report.getTotalSodium()) + "mg");
        
        // Pause before returning to menu
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Handles viewing the weekly nutrition report.
     * @details Shows nutrition summaries for a 7-day period, including daily totals
     *          and weekly averages for calories and macronutrients.
     */
    private void handleViewWeeklyReport() {
        System.out.println("\n===== Weekly Nutrition Report =====");
        
        // Get start date
        System.out.println("Please enter the start date of the week:");
        String startDate = getDateInput();
        if (startDate.isEmpty()) {
            return; // User canceled
        }
        
        // Generate dates for the week (7 days from start date)
        String[] dates = generateWeekDates(startDate);
        
        // Get the weekly nutrition reports
        String username = authService.getCurrentUser().getUsername();
        List<CalorieNutrientTrackingService.NutritionReport> reports = 
            calorieNutrientService.getWeeklyReport(username, dates);
        
        // Display the reports
        System.out.println("\nWeekly Nutrition Report");
        System.out.println("----------------------------------");
        
        for (CalorieNutrientTrackingService.NutritionReport report : reports) {
            System.out.println("\nDate: " + report.getDate());
            System.out.println("Calories: " + report.getTotalCalories() + " / " + 
                              report.getGoals().getCalorieGoal() + " (" + 
                              String.format("%.1f", report.getCaloriePercentage()) + "%)");
            
            System.out.println("Protein: " + String.format("%.1f", report.getTotalProtein()) + "g");
            System.out.println("Carbs: " + String.format("%.1f", report.getTotalCarbs()) + "g");
            System.out.println("Fat: " + String.format("%.1f", report.getTotalFat()) + "g");
        }
        
        // Calculate weekly averages
        int totalCalories = 0;
        double totalProtein = 0, totalCarbs = 0, totalFat = 0;
        
        for (CalorieNutrientTrackingService.NutritionReport report : reports) {
            totalCalories += report.getTotalCalories();
            totalProtein += report.getTotalProtein();
            totalCarbs += report.getTotalCarbs();
            totalFat += report.getTotalFat();
        }
        
        int days = reports.size();
        if (days > 0) {
            System.out.println("\nWeekly Averages:");
            System.out.println("----------------------------------");
            System.out.println("Average Calories: " + (totalCalories / days));
            System.out.println("Average Protein: " + String.format("%.1f", totalProtein / days) + "g");
            System.out.println("Average Carbs: " + String.format("%.1f", totalCarbs / days) + "g");
            System.out.println("Average Fat: " + String.format("%.1f", totalFat / days) + "g");
        }
        
        // Pause before returning to menu
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Handles calculating suggested calories.
     * @details Calculates recommended daily calorie intake based on user metrics
     *          and activity level, with option to set it as a new goal.
     */
    private void handleCalculateSuggestedCalories() {
        System.out.println("\n===== Calculate Suggested Calories =====");
        
        // Get gender
        char gender;
        while (true) {
            System.out.print("Enter gender (M/F): ");
            String input = scanner.nextLine().toUpperCase();
            if (input.length() > 0 && (input.charAt(0) == 'M' || input.charAt(0) == 'F')) {
                gender = input.charAt(0);
                break;
            } else {
                System.out.println("Invalid input. Please enter M for male or F for female.");
            }
        }
        
        // Get age
        int age = 0;
        while (age <= 0 || age > 120) {
            System.out.print("Enter age: ");
            try {
                age = Integer.parseInt(scanner.nextLine());
                if (age <= 0 || age > 120) {
                    System.out.println("Please enter a valid age between 1 and 120.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Get height
        double heightCm = 0;
        while (heightCm <= 0) {
            System.out.print("Enter height (cm): ");
            try {
                heightCm = Double.parseDouble(scanner.nextLine());
                if (heightCm <= 0) {
                    System.out.println("Height must be positive.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Get weight
        double weightKg = 0;
        while (weightKg <= 0) {
            System.out.print("Enter weight (kg): ");
            try {
                weightKg = Double.parseDouble(scanner.nextLine());
                if (weightKg <= 0) {
                    System.out.println("Weight must be positive.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Get activity level
        System.out.println("\nActivity Levels:");
        System.out.println("1. Sedentary (little or no exercise)");
        System.out.println("2. Lightly active (light exercise/sports 1-3 days/week)");
        System.out.println("3. Moderately active (moderate exercise/sports 3-5 days/week)");
        System.out.println("4. Very active (hard exercise/sports 6-7 days/week)");
        System.out.println("5. Extra active (very hard exercise, physical job, or training twice a day)");
        
        int activityLevel = 0;
        while (activityLevel < 1 || activityLevel > 5) {
            System.out.print("Enter activity level (1-5): ");
            try {
                activityLevel = Integer.parseInt(scanner.nextLine());
                if (activityLevel < 1 || activityLevel > 5) {
                    System.out.println("Please enter a number between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Calculate suggested calories
        int suggestedCalories = calorieNutrientService.calculateSuggestedCalories(
            gender, age, heightCm, weightKg, activityLevel);
            
        System.out.println("\nYour suggested daily calorie intake is: " + suggestedCalories + " calories");
        
        // Ask if user wants to set this as their goal
        System.out.print("\nWould you like to set this as your calorie goal? (Y/N): ");
        String setGoal = scanner.nextLine().toUpperCase();
        
        if (setGoal.startsWith("Y")) {
            // Get default macronutrient split based on calories
            double proteinGoal = suggestedCalories * 0.25 / 4; // 25% protein, 4 calories per gram
            double carbGoal = suggestedCalories * 0.5 / 4;     // 50% carbs, 4 calories per gram
            double fatGoal = suggestedCalories * 0.25 / 9;     // 25% fat, 9 calories per gram
            
            // Save nutrition goals
            String username = authService.getCurrentUser().getUsername();
            boolean success = calorieNutrientService.setNutritionGoals(
                username, suggestedCalories, proteinGoal, carbGoal, fatGoal);
                
            if (success) {
                System.out.println("Nutrition goals updated successfully!");
            } else {
                System.out.println("Failed to update nutrition goals.");
            }
        }
        
        // Pause before returning to menu
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
   
    /**
     * Handles browsing common foods with nutrients.
     * @details Displays a list of common foods with their nutritional information,
     *          including calories, macronutrients, fiber, sugar, and sodium content.
     */
    private void handleBrowseCommonFoods() {
        System.out.println("\n===== Common Foods with Nutrients =====");
        
        FoodNutrient[] commonFoods = calorieNutrientService.getCommonFoodsWithNutrients();
        
        for (int i = 0; i < commonFoods.length; i++) {
            FoodNutrient food = commonFoods[i];
            System.out.println("\n" + (i + 1) + ". " + food.getName() + " (" + food.getGrams() + "g)");
            System.out.println("   Calories: " + food.getCalories());
            System.out.println("   Protein: " + food.getProtein() + "g");
            System.out.println("   Carbs: " + food.getCarbs() + "g");
            System.out.println("   Fat: " + food.getFat() + "g");
            System.out.println("   Fiber: " + food.getFiber() + "g");
            System.out.println("   Sugar: " + food.getSugar() + "g");
            System.out.println("   Sodium: " + food.getSodium() + "mg");
        }
        
        // Pause before returning to menu
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Helper method to get a date input in YYYY-MM-DD format.
     * @details Guides the user through entering a valid year, month, and day,
     *          with validation for each component.
     * 
     * @return The date string in YYYY-MM-DD format, or empty string if canceled
     */
    private String getDateInput() {
        // Get year
        int year = 0;
        while (year < 2023 || year > 2100) {
            System.out.print("Enter year (2023-2100): ");
            try {
                year = Integer.parseInt(scanner.nextLine());
                if (year < 2023 || year > 2100) {
                    System.out.println("Please enter a valid year between 2023 and 2100.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Get month
        int month = 0;
        while (month < 1 || month > 12) {
            System.out.print("Enter month (1-12): ");
            try {
                month = Integer.parseInt(scanner.nextLine());
                if (month < 1 || month > 12) {
                    System.out.println("Please enter a valid month between 1 and 12.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Get day
        int day = 0;
        int maxDay = getMaxDaysInMonth(month, year);
        while (day < 1 || day > maxDay) {
            System.out.print("Enter day (1-" + maxDay + "): ");
            try {
                day = Integer.parseInt(scanner.nextLine());
                if (day < 1 || day > maxDay) {
                    System.out.println("Please enter a valid day between 1 and " + maxDay + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
        
        // Format date as YYYY-MM-DD
        return String.format("%04d-%02d-%02d", year, month, day);
    }
    
    /**
     * Helper method to get the maximum number of days in a month.
     * @details Accounts for different month lengths and leap years.
     * 
     * @param month The month (1-12)
     * @param year The year
     * @return The maximum number of days in the specified month
     */
    private int getMaxDaysInMonth(int month, int year) {
        switch (month) {
            case 2: // February
                return isLeapYear(year) ? 29 : 28;
            case 4: // April
            case 6: // June
            case 9: // September
            case 11: // November
                return 30;
            default:
                return 31;
        }
    }
    
    /**
     * Helper method to check if a year is a leap year.
     * @details Uses the standard leap year calculation rules:
     *          - Years divisible by 4 are leap years
     *          - Years divisible by 100 are not leap years
     *          - Years divisible by 400 are leap years
     * 
     * @param year The year to check
     * @return true if leap year, false otherwise
     */
    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
    
    /**
     * Helper method to generate an array of dates for a week starting from the given date.
     * @details Generates 7 consecutive dates starting from the provided date,
     *          handling month and year transitions correctly.
     * 
     * @param startDate The start date in format YYYY-MM-DD
     * @return Array of 7 dates (including the start date) in YYYY-MM-DD format
     */
    private String[] generateWeekDates(String startDate) {
        String[] dates = new String[7];
        dates[0] = startDate;
        
        try {
            // Parse the start date
            int year = Integer.parseInt(startDate.substring(0, 4));
            int month = Integer.parseInt(startDate.substring(5, 7));
            int day = Integer.parseInt(startDate.substring(8, 10));
            
            // Generate the next 6 dates
            for (int i = 1; i < 7; i++) {
                // Increment day
                day++;
                
                // Check if we need to advance to the next month
                int maxDays = getMaxDaysInMonth(month, year);
                if (day > maxDays) {
                    day = 1;
                    month++;
                    
                    // Check if we need to advance to the next year
                    if (month > 12) {
                        month = 1;
                        year++;
                    }
                }
                
                // Format the date
                dates[i] = String.format("%04d-%02d-%02d", year, month, day);
            }
        } catch (Exception e) {
            // In case of parsing error, just return the start date for all days
            for (int i = 1; i < 7; i++) {
                dates[i] = startDate;
            }
        }
        
        return dates;
    }
}