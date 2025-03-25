package com.berkant.kagan.haluk.irem.dietapp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles calorie and nutrient tracking operations for the Diet Planner application.
 * @details The CalorieNutrientTrackingService class provides methods for tracking calories,
 *          nutrients, setting goals, and viewing nutrition reports.
 * @author irem
 */
public class CalorieNutrientTrackingService {
    // Service dependencies
    private MealPlanningService mealPlanningService;
    
    /**
     * Constructor for CalorieNutrientTrackingService class.
     * 
     * @param mealPlanningService The meal planning service for accessing food logs
     */
    public CalorieNutrientTrackingService(MealPlanningService mealPlanningService) {
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
        // Validate input parameters
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        if (calorieGoal < 0 || proteinGoal < 0 || carbGoal < 0 || fatGoal < 0) {
            return false;
        }
        
        try (Connection conn = DatabaseHelper.getConnection()) {
            // Get user id
            int userId = getUserId(conn, username);
            if (userId == -1) {
                return false; // User not found
            }
            
            // Check if user already has nutrition goals
            boolean hasGoals = false;
            int goalId = -1;
            
            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT id FROM nutrition_goals WHERE user_id = ?")) {
                
                checkStmt.setInt(1, userId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    hasGoals = true;
                    goalId = rs.getInt("id");
                }
            }
            
            // Insert or update goals
            if (hasGoals) {
                // Update existing goals
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE nutrition_goals SET calorie_goal = ?, protein_goal = ?, carb_goal = ?, fat_goal = ? " +
                        "WHERE id = ?")) {
                    
                    updateStmt.setInt(1, calorieGoal);
                    updateStmt.setDouble(2, proteinGoal);
                    updateStmt.setDouble(3, carbGoal);
                    updateStmt.setDouble(4, fatGoal);
                    updateStmt.setInt(5, goalId);
                    
                    int affectedRows = updateStmt.executeUpdate();
                    return affectedRows > 0;
                }
            } else {
                // Insert new goals
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO nutrition_goals (user_id, calorie_goal, protein_goal, carb_goal, fat_goal) " +
                        "VALUES (?, ?, ?, ?, ?)")) {
                    
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, calorieGoal);
                    insertStmt.setDouble(3, proteinGoal);
                    insertStmt.setDouble(4, carbGoal);
                    insertStmt.setDouble(5, fatGoal);
                    
                    int affectedRows = insertStmt.executeUpdate();
                    return affectedRows > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Nutrition goals could not be saved: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper method to get user ID by username
     * 
     * @param conn Database connection
     * @param username Username to look up
     * @return User ID or -1 if not found
     * @throws SQLException If database error occurs
     */
    private int getUserId(Connection conn, String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        
        return -1;
    }
    
    /**
     * Gets the nutrition goals for a user.
     * 
     * @param username The username of the user
     * @return The user's nutrition goals or default goals if none are set
     */
    public NutritionGoal getNutritionGoals(String username) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            // Get user id
            int userId = getUserId(conn, username);
            if (userId == -1) {
                return new NutritionGoal(2000, 50, 250, 70); // Default values for invalid user
            }
            
            // Get nutrition goals
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM nutrition_goals WHERE user_id = ?")) {
                
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return new NutritionGoal(
                        rs.getInt("calorie_goal"),
                        rs.getDouble("protein_goal"),
                        rs.getDouble("carb_goal"),
                        rs.getDouble("fat_goal")
                    );
                }
            }
            
            // Return default goals if none are set
            return new NutritionGoal(2000, 50, 250, 70);
            
        } catch (SQLException e) {
            System.out.println("Nutrition goals could not be retrieved: " + e.getMessage());
            return new NutritionGoal(2000, 50, 250, 70); // Default values on error
        }
    }
    
    /**
     * Generates a nutrition report for a specific date.
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @return A NutritionReport object containing the nutrition summary
     */
    public NutritionReport getNutritionReport(String username, String date) {
        // Validate input parameters
        if (username == null || username.trim().isEmpty() || date == null || date.trim().isEmpty()) {
            // Return an empty report with default goals
            return new NutritionReport(
                date != null ? date : "",
                0, 0, 0, 0, 0, 0, 0,
                getNutritionGoals(username)
            );
        }
        
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
        
        // Validate input parameters
        if (username == null || username.trim().isEmpty() || dates == null) {
            return reports; // Return empty list
        }
        
        for (String date : dates) {
            if (date != null && !date.trim().isEmpty()) {
                reports.add(getNutritionReport(username, date));
            }
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
        // Validate input parameters
        if (age <= 0 || heightCm <= 0 || weightKg <= 0 || 
            activityLevel < 1 || activityLevel > 5) {
            return 0; // Invalid input
        }
        
        if (gender != 'M' && gender != 'm' && gender != 'F' && gender != 'f') {
            return 0; // Invalid gender
        }
        
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
            default:
                activityFactor = 1.2; // Default to sedentary
                break;
        }
        
        return (int) Math.round(bmr * activityFactor);
    }
    
    /**
     * Gets predefined common food items with detailed nutrient information.
     * 
     * @return Array of common foods with nutrient details
     */
    public FoodNutrient[] getCommonFoodsWithNutrients() {
        // Try to get from database first
        List<FoodNutrient> commonFoods = new ArrayList<>();
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT f.id, f.name, f.grams, f.calories, fn.protein, fn.carbs, fn.fat, " +
                 "fn.fiber, fn.sugar, fn.sodium FROM foods f " +
                 "JOIN food_nutrients fn ON f.id = fn.food_id " +
                 "LIMIT 15")) {
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                commonFoods.add(new FoodNutrient(
                    rs.getString("name"),
                    rs.getDouble("grams"),
                    rs.getInt("calories"),
                    rs.getDouble("protein"),
                    rs.getDouble("carbs"),
                    rs.getDouble("fat"),
                    rs.getDouble("fiber"),
                    rs.getDouble("sugar"),
                    rs.getDouble("sodium")
                ));
            }
            
        } catch (SQLException e) {
            System.out.println("Common foods could not be retrieved: " + e.getMessage());
        }
        
        // If we found foods in the database, return those
        if (!commonFoods.isEmpty()) {
            return commonFoods.toArray(new FoodNutrient[0]);
        }
        
        // Otherwise, return the default hard-coded array
        // and add these to the database for future use
        FoodNutrient[] defaultFoods = {
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
        
        // Save these to the database for future use
        try (Connection conn = DatabaseHelper.getConnection()) {
            for (FoodNutrient food : defaultFoods) {
                DatabaseHelper.saveFoodAndGetId(food);
            }
        } catch (SQLException e) {
            System.out.println("Common foods could not be saved to database: " + e.getMessage());
        }
        
        return defaultFoods;
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
            this.calorieGoal = Math.max(0, calorieGoal); // Ensure non-negative
            this.proteinGoal = Math.max(0, proteinGoal); // Ensure non-negative
            this.carbGoal = Math.max(0, carbGoal);       // Ensure non-negative
            this.fatGoal = Math.max(0, fatGoal);         // Ensure non-negative
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
            this.date = date != null ? date : "";
            this.totalCalories = Math.max(0, totalCalories);       // Ensure non-negative
            this.totalProtein = Math.max(0, totalProtein);         // Ensure non-negative
            this.totalCarbs = Math.max(0, totalCarbs);             // Ensure non-negative
            this.totalFat = Math.max(0, totalFat);                 // Ensure non-negative
            this.totalFiber = Math.max(0, totalFiber);             // Ensure non-negative
            this.totalSugar = Math.max(0, totalSugar);             // Ensure non-negative
            this.totalSodium = Math.max(0, totalSodium);           // Ensure non-negative
            this.goals = goals != null ? goals : new NutritionGoal(2000, 50, 250, 70);
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
}