package com.berkant.kagan.haluk.irem.dietapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles calorie and nutrient tracking operations for the Diet Planner application.
 * @details The CalorieNutrientTrackingService class provides methods for tracking calories,
 *          nutrients, setting goals, and viewing nutrition reports.
 * @author irem
 */
public class CalorieNutrientTrackingService {
    // Maps to store user nutrition goals
    private Map<String, NutritionGoal> userNutritionGoals;
    private MealPlanningService mealPlanningService;
    
    /**
     * Constructor for CalorieNutrientTrackingService class.
     * 
     * @param mealPlanningService The meal planning service for accessing food logs
     */
    public CalorieNutrientTrackingService(MealPlanningService mealPlanningService) {
        this.userNutritionGoals = new HashMap<>();
        this.mealPlanningService = mealPlanningService;
    }
    
    /**
     * Sets or updates nutrition goals for a user.
     * 
     * @param username The username of the user
     * @param calorieGoal The daily calorie goal
     * @param proteinGoal The daily protein goal in grams
     * @param carbGoal The daily carbohydrate goal in grams
     * @param fatGoal The daily fat goal in grams
     * @return true if goals set successfully
     */
    public boolean setNutritionGoals(String username, int calorieGoal, double proteinGoal, 
                                    double carbGoal, double fatGoal) {
        NutritionGoal goal = new NutritionGoal(calorieGoal, proteinGoal, carbGoal, fatGoal);
        userNutritionGoals.put(username, goal);
        return true;
    }
    
    /**
     * Gets the nutrition goals for a user.
     * 
     * @param username The username of the user
     * @return The user's nutrition goals or default goals if none are set
     */
    public NutritionGoal getNutritionGoals(String username) {
        // Return user's goals or default goals if none are set
        return userNutritionGoals.getOrDefault(username, 
                new NutritionGoal(2000, 50, 250, 70)); // Default values
    }
    
    /**
     * Generates a nutrition report for a specific date.
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @return A NutritionReport object containing the nutrition summary
     */
    public NutritionReport getNutritionReport(String username, String date) {
        List<Food> foods = mealPlanningService.getFoodLog(username, date);
        
        int totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;
        double totalFiber = 0;
        double totalSugar = 0;
        double totalSodium = 0;
        
        // Calculate totals for the day
        for (Food food : foods) {
            totalCalories += food.getCalories();
            
            // If the food has detailed nutrients (is a FoodNutrient)
            if (food instanceof FoodNutrient) {
                FoodNutrient foodNutrient = (FoodNutrient) food;
                totalProtein += foodNutrient.getProtein();
                totalCarbs += foodNutrient.getCarbs();
                totalFat += foodNutrient.getFat();
                totalFiber += foodNutrient.getFiber();
                totalSugar += foodNutrient.getSugar();
                totalSodium += foodNutrient.getSodium();
            }
        }
        
        // Get user's nutrition goals
        NutritionGoal goals = getNutritionGoals(username);
        
        // Create nutrition report
        return new NutritionReport(
            date,
            totalCalories,
            totalProtein,
            totalCarbs,
            totalFat,
            totalFiber,
            totalSugar,
            totalSodium,
            goals
        );
    }
    
    /**
     * Gets a weekly nutrition report.
     * 
     * @param username The username of the user
     * @param dates An array of dates to include in the report
     * @return A list of NutritionReport objects for each date
     */
    public List<NutritionReport> getWeeklyReport(String username, String[] dates) {
        List<NutritionReport> reports = new ArrayList<>();
        
        for (String date : dates) {
            reports.add(getNutritionReport(username, date));
        }
        
        return reports;
    }
    
    /**
     * Calculates a suggested daily calorie intake based on user information.
     * 
     * @param gender The user's gender (M/F)
     * @param age The user's age
     * @param heightCm The user's height in centimeters
     * @param weightKg The user's weight in kilograms
     * @param activityLevel The user's activity level (1-5)
     * @return The suggested daily calorie intake
     */
    public int calculateSuggestedCalories(char gender, int age, double heightCm, 
                                         double weightKg, int activityLevel) {
        double bmr = 0;
        
        // Calculate BMR using Mifflin-St Jeor Equation
        if (gender == 'M' || gender == 'm') {
            bmr = 10 * weightKg + 6.25 * heightCm - 5 * age + 5;
        } else {
            bmr = 10 * weightKg + 6.25 * heightCm - 5 * age - 161;
        }
        
        // Apply activity factor
        double activityFactor = 1.2; // Sedentary
        
        switch (activityLevel) {
            case 1: // Sedentary
                activityFactor = 1.2;
                break;
            case 2: // Lightly active
                activityFactor = 1.375;
                break;
            case 3: // Moderately active
                activityFactor = 1.55;
                break;
            case 4: // Very active
                activityFactor = 1.725;
                break;
            case 5: // Extra active
                activityFactor = 1.9;
                break;
        }
        
        return (int) Math.round(bmr * activityFactor);
    }
    
    /**
     * Inner class to represent a user's nutrition goals.
     */
    public class NutritionGoal {
        private int calorieGoal;
        private double proteinGoal;
        private double carbGoal;
        private double fatGoal;
        
        public NutritionGoal(int calorieGoal, double proteinGoal, 
                            double carbGoal, double fatGoal) {
            this.calorieGoal = calorieGoal;
            this.proteinGoal = proteinGoal;
            this.carbGoal = carbGoal;
            this.fatGoal = fatGoal;
        }
        
        public int getCalorieGoal() {
            return calorieGoal;
        }
        
        public double getProteinGoal() {
            return proteinGoal;
        }
        
        public double getCarbGoal() {
            return carbGoal;
        }
        
        public double getFatGoal() {
            return fatGoal;
        }
    }
    
    /**
     * Inner class to represent a nutrition report for a specific date.
     */
    public class NutritionReport {
        private String date;
        private int totalCalories;
        private double totalProtein;
        private double totalCarbs;
        private double totalFat;
        private double totalFiber;
        private double totalSugar;
        private double totalSodium;
        private NutritionGoal goals;
        
        public NutritionReport(String date, int totalCalories, double totalProtein,
                              double totalCarbs, double totalFat, double totalFiber,
                              double totalSugar, double totalSodium, NutritionGoal goals) {
            this.date = date;
            this.totalCalories = totalCalories;
            this.totalProtein = totalProtein;
            this.totalCarbs = totalCarbs;
            this.totalFat = totalFat;
            this.totalFiber = totalFiber;
            this.totalSugar = totalSugar;
            this.totalSodium = totalSodium;
            this.goals = goals;
        }
        
        public String getDate() {
            return date;
        }
        
        public int getTotalCalories() {
            return totalCalories;
        }
        
        public double getTotalProtein() {
            return totalProtein;
        }
        
        public double getTotalCarbs() {
            return totalCarbs;
        }
        
        public double getTotalFat() {
            return totalFat;
        }
        
        public double getTotalFiber() {
            return totalFiber;
        }
        
        public double getTotalSugar() {
            return totalSugar;
        }
        
        public double getTotalSodium() {
            return totalSodium;
        }
        
        public NutritionGoal getGoals() {
            return goals;
        }
        
        public double getCaloriePercentage() {
            return goals.getCalorieGoal() > 0 ? (totalCalories * 100.0 / goals.getCalorieGoal()) : 0;
        }
        
        public double getProteinPercentage() {
            return goals.getProteinGoal() > 0 ? (totalProtein * 100.0 / goals.getProteinGoal()) : 0;
        }
        
        public double getCarbPercentage() {
            return goals.getCarbGoal() > 0 ? (totalCarbs * 100.0 / goals.getCarbGoal()) : 0;
        }
        
        public double getFatPercentage() {
            return goals.getFatGoal() > 0 ? (totalFat * 100.0 / goals.getFatGoal()) : 0;
        }
    }
    
    /**
     * Gets predefined common food items with detailed nutrient information.
     * 
     * @return Array of common foods with nutrient details
     */
    public FoodNutrient[] getCommonFoodsWithNutrients() {
        return new FoodNutrient[] {
            // Name, grams, calories, protein, carbs, fat, fiber, sugar, sodium
            new FoodNutrient("Apple", 100, 52, 0.3, 14.0, 0.2, 2.4, 10.3, 1.0),
            new FoodNutrient("Banana", 100, 89, 1.1, 22.8, 0.3, 2.6, 12.2, 1.0),
            new FoodNutrient("Chicken Breast", 100, 165, 31.0, 0.0, 3.6, 0.0, 0.0, 74.0),
            new FoodNutrient("Salmon", 100, 206, 22.0, 0.0, 13.0, 0.0, 0.0, 59.0),
            new FoodNutrient("Brown Rice", 100, 112, 2.6, 23.5, 0.9, 1.8, 0.4, 5.0),
            new FoodNutrient("Egg", 50, 78, 6.3, 0.6, 5.3, 0.0, 0.6, 62.0),
            new FoodNutrient("Broccoli", 100, 34, 2.8, 6.6, 0.4, 2.6, 1.7, 33.0),
            new FoodNutrient("Greek Yogurt", 100, 59, 10.0, 3.6, 0.4, 0.0, 3.6, 36.0),
            new FoodNutrient("Almonds", 30, 173, 6.0, 6.1, 14.9, 3.5, 1.2, 0.3),
            new FoodNutrient("Sweet Potato", 100, 86, 1.6, 20.1, 0.1, 3.0, 4.2, 55.0),
            new FoodNutrient("Avocado", 100, 160, 2.0, 8.5, 14.7, 6.7, 0.7, 7.0),
            new FoodNutrient("Oatmeal", 100, 68, 2.5, 12.0, 1.4, 2.0, 0.0, 2.0),
            new FoodNutrient("Whole Wheat Bread", 30, 76, 3.6, 14.0, 1.1, 2.0, 1.5, 152.0),
            new FoodNutrient("Milk", 100, 42, 3.4, 5.0, 1.0, 0.0, 5.0, 44.0),
            new FoodNutrient("Ground Beef (Lean)", 100, 250, 26.0, 0.0, 15.0, 0.0, 0.0, 70.0)
        };
    }
}