/**
 * @file CalorieNutrientTrackingService.java
 * @brief Service class for managing calorie and nutrient tracking operations
 * 
 * @details The CalorieNutrientTrackingService class provides comprehensive functionality
 *          for tracking and managing nutrition data in the Diet Planner application.
 *          It handles user nutrition goals, generates nutrition reports, calculates
 *          suggested calorie intake, and manages food nutrient information.
 * 
 * @author irem
 * @version 1.0
 * @date 2024
 * @copyright Diet Planner Application
 */
package com.berkant.kagan.haluk.irem.dietapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @class CalorieNutrientTrackingService
 * @brief Main service class for calorie and nutrient tracking operations
 * 
 * @details This class implements the core functionality for nutrition tracking in the
 *          Diet Planner application. It provides methods for managing nutrition goals,
 *          generating reports, calculating recommended calorie intake, and handling
 *          food nutrient data. The class works in conjunction with the MealPlanningService
 *          to provide comprehensive nutrition tracking capabilities.
 */
public class CalorieNutrientTrackingService {
    /** @brief Service for accessing food logs and meal planning data */
    private MealPlanningService mealPlanningService;
    
    /**
     * @brief Constructor for CalorieNutrientTrackingService
     * @details Initializes the service with a reference to the meal planning service
     *          which is required for accessing food logs and meal data.
     * 
     * @param mealPlanningService Service for accessing food logs and meal planning data
     */
    public CalorieNutrientTrackingService(MealPlanningService mealPlanningService) {
        this.mealPlanningService = mealPlanningService;
    }
    
    /**
     * @brief Sets or updates nutrition goals for a user
     * @details Validates input parameters and either creates new nutrition goals
     *          or updates existing ones in the database. Goals include daily targets
     *          for calories and macronutrients.
     * 
     * @param username The username of the user
     * @param calorieGoal The daily calorie goal in calories
     * @param proteinGoal The daily protein goal in grams
     * @param carbGoal The daily carbohydrate goal in grams
     * @param fatGoal The daily fat goal in grams
     * @return true if goals were successfully set or updated, false otherwise
     * @throws SQLException if there is an error accessing the database
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
     * @brief Retrieves the user ID associated with a username
     * @details Helper method that queries the database to find the user ID
     *          corresponding to the given username.
     * 
     * @param conn Active database connection
     * @param username The username to look up
     * @return The user ID if found, -1 if not found
     * @throws SQLException if there is an error accessing the database
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
     * @brief Retrieves nutrition goals for a user
     * @details Fetches the user's nutrition goals from the database. If no goals
     *          are set, returns default values. Default goals are 2000 calories,
     *          50g protein, 250g carbs, and 70g fat.
     * 
     * @param username The username of the user
     * @return NutritionGoal object containing the user's goals or default values
     * @throws SQLException if there is an error accessing the database
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
     * @brief Generates a nutrition report for a specific date
     * @details Calculates total nutrition intake for the specified date by
     *          aggregating data from all food entries. Includes calories,
     *          macronutrients, and other nutritional information.
     * 
     * @param username The username of the user
     * @param date The date in YYYY-MM-DD format
     * @return NutritionReport object containing the nutrition summary
     * @throws SQLException if there is an error accessing the database
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
     * @brief Generates weekly nutrition reports
     * @details Creates nutrition reports for each date in the provided array,
     *          typically representing a week of data.
     * 
     * @param username The username of the user
     * @param dates Array of dates in YYYY-MM-DD format
     * @return List of NutritionReport objects, one for each date
     * @throws SQLException if there is an error accessing the database
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
     * @brief Calculates suggested daily calorie intake
     * @details Uses the Harris-Benedict equation to calculate recommended daily
     *          calorie intake based on user's personal information and activity level.
     * 
     * @param gender User's gender ('M' or 'F')
     * @param age User's age in years
     * @param heightCm User's height in centimeters
     * @param weightKg User's weight in kilograms
     * @param activityLevel Activity level (1-5, where 1 is sedentary and 5 is very active)
     * @return Suggested daily calorie intake in calories
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
     * @brief Retrieves a list of common foods with their nutritional information
     * @details Returns an array of FoodNutrient objects containing detailed
     *          nutritional information for common foods in the database.
     * 
     * @return Array of FoodNutrient objects
     * @throws SQLException if there is an error accessing the database
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
     * @class NutritionGoal
     * @brief Class representing a user's nutrition goals
     * 
     * @details Stores daily targets for calories and macronutrients.
     *          Used for tracking progress and generating reports.
     */
    public class NutritionGoal {
        /** @brief Daily calorie goal in calories */
        private int calorieGoal;
        /** @brief Daily protein goal in grams */
        private double proteinGoal;
        /** @brief Daily carbohydrate goal in grams */
        private double carbGoal;
        /** @brief Daily fat goal in grams */
        private double fatGoal;
        
        /**
         * @brief Constructor for NutritionGoal
         * @details Initializes a new NutritionGoal with specified targets
         * 
         * @param calorieGoal Daily calorie goal in calories
         * @param proteinGoal Daily protein goal in grams
         * @param carbGoal Daily carbohydrate goal in grams
         * @param fatGoal Daily fat goal in grams
         */
        public NutritionGoal(int calorieGoal, double proteinGoal, 
                            double carbGoal, double fatGoal) {
            this.calorieGoal = Math.max(0, calorieGoal); // Ensure non-negative
            this.proteinGoal = Math.max(0, proteinGoal); // Ensure non-negative
            this.carbGoal = Math.max(0, carbGoal);       // Ensure non-negative
            this.fatGoal = Math.max(0, fatGoal);         // Ensure non-negative
        }
        
        /**
         * Gets the daily calorie goal.
         * @return The calorie goal in calories
         */
        public int getCalorieGoal() {
            return calorieGoal;
        }
        
        /**
         * Gets the daily protein goal.
         * @return The protein goal in grams
         */
        public double getProteinGoal() {
            return proteinGoal;
        }
        
        /**
         * Gets the daily carbohydrate goal.
         * @return The carbohydrate goal in grams
         */
        public double getCarbGoal() {
            return carbGoal;
        }
        
        /**
         * Gets the daily fat goal.
         * @return The fat goal in grams
         */
        public double getFatGoal() {
            return fatGoal;
        }
    }
    
    /**
     * @class NutritionReport
     * @brief Class representing a nutrition report for a specific date
     * 
     * @details Contains total nutrition intake for a day, including calories,
     *          macronutrients, and other nutritional information. Also includes
     *          the user's nutrition goals for comparison.
     */
    public class NutritionReport {
        /** @brief The date of the report in YYYY-MM-DD format */
        private String date;
        /** @brief Total calories consumed */
        private int totalCalories;
        /** @brief Total protein consumed in grams */
        private double totalProtein;
        /** @brief Total carbohydrates consumed in grams */
        private double totalCarbs;
        /** @brief Total fat consumed in grams */
        private double totalFat;
        /** @brief Total fiber consumed in grams */
        private double totalFiber;
        /** @brief Total sugar consumed in grams */
        private double totalSugar;
        /** @brief Total sodium consumed in milligrams */
        private double totalSodium;
        /** @brief User's nutrition goals for comparison */
        private NutritionGoal goals;
        
        /**
         * @brief Constructor for NutritionReport
         * @details Initializes a new NutritionReport with specified values
         * 
         * @param date The date of the report
         * @param totalCalories Total calories consumed
         * @param totalProtein Total protein consumed in grams
         * @param totalCarbs Total carbohydrates consumed in grams
         * @param totalFat Total fat consumed in grams
         * @param totalFiber Total fiber consumed in grams
         * @param totalSugar Total sugar consumed in grams
         * @param totalSodium Total sodium consumed in milligrams
         * @param goals User's nutrition goals
         */
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
        
        /**
         * Gets the date of the report.
         * @return The date in YYYY-MM-DD format
         */
        public String getDate() {
            return date;
        }
        
        /**
         * Gets the total calories consumed.
         * @return Total calories
         */
        public int getTotalCalories() {
            return totalCalories;
        }
        
        /**
         * Gets the total protein consumed.
         * @return Total protein in grams
         */
        public double getTotalProtein() {
            return totalProtein;
        }
        
        /**
         * Gets the total carbohydrates consumed.
         * @return Total carbohydrates in grams
         */
        public double getTotalCarbs() {
            return totalCarbs;
        }
        
        /**
         * Gets the total fat consumed.
         * @return Total fat in grams
         */
        public double getTotalFat() {
            return totalFat;
        }
        
        /**
         * Gets the total fiber consumed.
         * @return Total fiber in grams
         */
        public double getTotalFiber() {
            return totalFiber;
        }
        
        /**
         * Gets the total sugar consumed.
         * @return Total sugar in grams
         */
        public double getTotalSugar() {
            return totalSugar;
        }
        
        /**
         * Gets the total sodium consumed.
         * @return Total sodium in milligrams
         */
        public double getTotalSodium() {
            return totalSodium;
        }
        
        /**
         * Gets the nutrition goals.
         * @return The NutritionGoal object
         */
        public NutritionGoal getGoals() {
            return goals;
        }
        
        /**
         * Calculates the percentage of calorie goal achieved.
         * @return Percentage of calorie goal (0-100+)
         */
        public double getCaloriePercentage() {
            return goals.getCalorieGoal() > 0 ? (totalCalories * 100.0 / goals.getCalorieGoal()) : 0;
        }
        
        /**
         * Calculates the percentage of protein goal achieved.
         * @return Percentage of protein goal (0-100+)
         */
        public double getProteinPercentage() {
            return goals.getProteinGoal() > 0 ? (totalProtein * 100.0 / goals.getProteinGoal()) : 0;
        }
        
        /**
         * Calculates the percentage of carbohydrate goal achieved.
         * @return Percentage of carbohydrate goal (0-100+)
         */
        public double getCarbPercentage() {
            return goals.getCarbGoal() > 0 ? (totalCarbs * 100.0 / goals.getCarbGoal()) : 0;
        }
        
        /**
         * Calculates the percentage of fat goal achieved.
         * @return Percentage of fat goal (0-100+)
         */
        public double getFatPercentage() {
            return goals.getFatGoal() > 0 ? (totalFat * 100.0 / goals.getFatGoal()) : 0;
        }
    }

    public List<String> getAllFoods() {
        List<String> foods = new ArrayList<>();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM foods")) {
            
            while (rs.next()) {
                foods.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Foods could not be retrieved: " + e.getMessage());
        }
        return foods;
    }

    public boolean addFoodConsumption(String foodName, double quantity) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO food_consumption (food_name, quantity, date) VALUES (?, ?, CURRENT_DATE)")) {
            
            pstmt.setString(1, foodName);
            pstmt.setDouble(2, quantity);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Food consumption could not be added: " + e.getMessage());
            return false;
        }
    }

    public String getDailyConsumptionLog() {
        StringBuilder log = new StringBuilder();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT f.name, fc.quantity, f.calories " +
                 "FROM food_consumption fc " +
                 "JOIN foods f ON fc.food_name = f.name " +
                 "WHERE fc.date = CURRENT_DATE")) {
            
            while (rs.next()) {
                log.append(rs.getString("name"))
                   .append(" - ")
                   .append(rs.getDouble("quantity"))
                   .append("g (")
                   .append(rs.getInt("calories"))
                   .append(" kalori)\n");
            }
        } catch (SQLException e) {
            System.out.println("Daily consumption log could not be retrieved: " + e.getMessage());
        }
        return log.toString();
    }

    public void addFoodEntry(String foodName, int calories, double protein, double carbs, double fat) throws SQLException {
        String sql = "INSERT INTO food_entries (food_name, calories, protein, carbs, fat) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseHelper.getConnection().prepareStatement(sql)) {
            stmt.setString(1, foodName);
            stmt.setInt(2, calories);
            stmt.setDouble(3, protein);
            stmt.setDouble(4, carbs);
            stmt.setDouble(5, fat);
            stmt.executeUpdate();
        }
    }

    public List<String> viewFoodEntries() throws SQLException {
        List<String> entries = new ArrayList<>();
        String sql = "SELECT * FROM food_entries";
        try (Statement stmt = DatabaseHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String entry = String.format("%s - Kalori: %d, Protein: %.1fg, Karbonhidrat: %.1fg, Yağ: %.1fg",
                    rs.getString("food_name"),
                    rs.getInt("calories"),
                    rs.getDouble("protein"),
                    rs.getDouble("carbs"),
                    rs.getDouble("fat"));
                entries.add(entry);
            }
        }
        return entries;
    }

    public void deleteFoodEntry(String foodName) throws SQLException {
        String sql = "DELETE FROM food_entries WHERE food_name = ?";
        try (PreparedStatement stmt = DatabaseHelper.getConnection().prepareStatement(sql)) {
            stmt.setString(1, foodName);
            stmt.executeUpdate();
        }
    }
}