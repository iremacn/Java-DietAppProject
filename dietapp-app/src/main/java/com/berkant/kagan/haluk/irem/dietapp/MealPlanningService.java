package com.berkant.kagan.haluk.irem.dietapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles meal planning and logging operations for the Diet Planner application.
 * @details The MealPlanningService class provides methods for planning meals,
 *          logging food intake, and viewing meal history.
 * @author berkant
 */
public class MealPlanningService {
    // Maps to store meal plans and food logs by date and user
    private Map<String, Map<String, List<Food>>> userMealPlans;
    private Map<String, Map<String, List<Food>>> userFoodLogs;
    
    /**
     * Constructor for MealPlanningService class.
     * Initializes the meal plans and food logs maps.
     */
    public MealPlanningService() {
        this.userMealPlans = new HashMap<>();
        this.userFoodLogs = new HashMap<>();
    }
    
    /**
     * Adds a meal plan for a specific date.
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @param mealType The type of meal (breakfast, lunch, snack, dinner)
     * @param food The food to add to the meal plan
     * @return true if added successfully
     */
    public boolean addMealPlan(String username, String date, String mealType, Food food) {
        // Get or create user's meal plans
        Map<String, List<Food>> userPlans = userMealPlans.getOrDefault(username, new HashMap<>());
        userMealPlans.put(username, userPlans);
        
        // Get or create meal plan for specific date
        List<Food> mealPlan = userPlans.getOrDefault(date + "_" + mealType, new ArrayList<>());
        userPlans.put(date + "_" + mealType, mealPlan);
        
        // Add food to meal plan
        mealPlan.add(food);
        return true;
    }
    
    /**
     * Logs food consumed by the user.
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @param food The food that was consumed
     * @return true if logged successfully
     */
    public boolean logFood(String username, String date, Food food) {
        // Get or create user's food logs
        Map<String, List<Food>> userLogs = userFoodLogs.getOrDefault(username, new HashMap<>());
        userFoodLogs.put(username, userLogs);
        
        // Get or create food log for specific date
        List<Food> foodLog = userLogs.getOrDefault(date, new ArrayList<>());
        userLogs.put(date, foodLog);
        
        // Add food to food log
        foodLog.add(food);
        return true;
    }
    
    /**
     * Gets the meal plan for a specific date and meal type.
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @param mealType The type of meal (breakfast, lunch, snack, dinner)
     * @return List of foods planned for the specified meal
     */
    public List<Food> getMealPlan(String username, String date, String mealType) {
        Map<String, List<Food>> userPlans = userMealPlans.getOrDefault(username, new HashMap<>());
        return userPlans.getOrDefault(date + "_" + mealType, new ArrayList<>());
    }
    
    /**
     * Gets all food logged for a specific date.
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @return List of foods logged for the specified date
     */
    public List<Food> getFoodLog(String username, String date) {
        Map<String, List<Food>> userLogs = userFoodLogs.getOrDefault(username, new HashMap<>());
        return userLogs.getOrDefault(date, new ArrayList<>());
    }
    
    /**
     * Calculates the total calories consumed on a specific date.
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @return The total calories consumed
     */
    public int getTotalCalories(String username, String date) {
        List<Food> foods = getFoodLog(username, date);
        int totalCalories = 0;
        
        for (Food food : foods) {
            totalCalories += food.getCalories();
        }
        
        return totalCalories;
    }
    
    /**
     * Validates a date in the format YYYY-MM-DD.
     * 
     * @param year The year (between 2025 and 2100)
     * @param month The month (between 1 and 12)
     * @param day The day (between 1 and 31)
     * @return true if the date is valid, false otherwise
     */
    public boolean isValidDate(int year, int month, int day) {
        // Check year is within range
        if (year < 2025 || year > 2100) {
            return false;
        }
        
        // Check month is within range
        if (month < 1 || month > 12) {
            return false;
        }
        
        // Check day is within range
        if (day < 1 || day > 31) {
            return false;
        }
        
        // Check days in month (simplified version)
        if (month == 2) { // February
            // Leap year check (simplified)
            boolean isLeapYear = (year % 4 == 0);
            if (isLeapYear && day > 29) {
                return false;
            } else if (!isLeapYear && day > 28) {
                return false;
            }
        } else if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
            // April, June, September, November have 30 days
            return false;
        }
        
        return true;
    }
    
    /**
     * Formats date components into a string in YYYY-MM-DD format.
     * 
     * @param year The year
     * @param month The month
     * @param day The day
     * @return The formatted date string
     */
    public String formatDate(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month, day);
    }
    
    /**
     * Gets predefined breakfast food options.
     * 
     * @return Array of breakfast food options
     */
    public Food[] getBreakfastOptions() {
        return new Food[] {
            new Food("Scrambled Eggs", 150, 220),
            new Food("Oatmeal with Fruits", 250, 350),
            new Food("Greek Yogurt with Honey", 200, 180),
            new Food("Whole Grain Toast with Avocado", 120, 240),
            new Food("Smoothie Bowl", 300, 280),
            new Food("Pancakes with Maple Syrup", 180, 450),
            new Food("Breakfast Burrito", 220, 380),
            new Food("Fruit and Nut Granola", 100, 410)
        };
    }
    
    /**
     * Gets predefined lunch food options.
     * 
     * @return Array of lunch food options
     */
    public Food[] getLunchOptions() {
        return new Food[] {
            new Food("Grilled Chicken Salad", 350, 320),
            new Food("Quinoa Bowl with Vegetables", 280, 390),
            new Food("Turkey and Avocado Sandwich", 230, 450),
            new Food("Vegetable Soup with Bread", 400, 280),
            new Food("Tuna Salad Wrap", 250, 330),
            new Food("Falafel with Hummus", 300, 480),
            new Food("Caesar Salad with Grilled Chicken", 320, 370),
            new Food("Mediterranean Pasta Salad", 280, 410)
        };
    }
    
    /**
     * Gets predefined snack food options.
     * 
     * @return Array of snack food options
     */
    public Food[] getSnackOptions() {
        return new Food[] {
            new Food("Apple with Peanut Butter", 150, 220),
            new Food("Greek Yogurt with Berries", 180, 160),
            new Food("Mixed Nuts", 50, 290),
            new Food("Hummus with Carrot Sticks", 150, 180),
            new Food("Protein Bar", 60, 200),
            new Food("Fruit Smoothie", 250, 190),
            new Food("Dark Chocolate Square", 30, 170),
            new Food("Cheese and Crackers", 100, 230)
        };
    }
    
    /**
     * Gets predefined dinner food options.
     * 
     * @return Array of dinner food options
     */
    public Food[] getDinnerOptions() {
        return new Food[] {
            new Food("Grilled Salmon with Vegetables", 350, 420),
            new Food("Beef Stir Fry with Rice", 400, 520),
            new Food("Vegetable Curry with Tofu", 350, 380),
            new Food("Spaghetti with Tomato Sauce", 320, 450),
            new Food("Baked Chicken with Sweet Potato", 380, 390),
            new Food("Lentil Soup with Bread", 400, 350),
            new Food("Grilled Steak with Mashed Potatoes", 350, 550),
            new Food("Fish Tacos with Slaw", 300, 410)
        };
    }
}