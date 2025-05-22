/**
 * @file MealPlanningService.java
 * @brief Service class for managing meal planning and food logging operations
 * 
 * @details The MealPlanningService class provides comprehensive functionality for:
 *          - Planning and managing meals
 *          - Logging food consumption
 *          - Tracking nutritional information
 *          - Managing meal history
 *          - Handling food options for different meal types
 * 
 * @author berkant
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
import java.time.LocalDate;

/**
 * @class MealPlanningService
 * @brief Service class for meal planning and food logging operations
 * 
 * @details This class provides a comprehensive set of methods for managing meal planning
 *          and food logging operations in the Diet Planner application. It handles:
 *          - Meal planning for different times of day
 *          - Food logging and tracking
 *          - Nutritional information management
 *          - Database operations for meal and food data
 *          - Food options management for different meal types
 */
public class MealPlanningService {
    
    /** @brief Database connection for performing operations */
    private Connection connection;
    
    /**
     * @brief Constructs a new MealPlanningService instance
     * @details Initializes the service with a database connection for data storage
     * 
     * @param connection Database connection to use for operations
     */
    public MealPlanningService(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * @brief Adds a meal plan for a specific date
     * @details Creates a meal plan entry in the database:
     *          - Validates input parameters
     *          - Gets user ID from username
     *          - Saves food information
     *          - Creates meal plan entry
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @param mealType The type of meal (breakfast, lunch, snack, dinner)
     * @param food The food to add to the meal plan
     * @return true if meal plan was added successfully, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean addMealPlan(String username, String date, String mealType, Food food) {
        if (username == null || date == null || mealType == null || food == null) {
            System.out.println("Cannot add meal plan with null parameters");
            return false;
        }
        
        try {
            // Get user ID
            int userId = getUserId(connection, username);
            if (userId == -1) {
                System.out.println("User not found: " + username);
                return false; // User not found
            }
            
            // Save food and get its ID
            int foodId = saveFoodAndGetId(connection, food);
            if (foodId == -1) {
                return false; // Food couldn't be saved
            }
            
            // Add to meal plan
            try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO meal_plans (user_id, date, meal_type, food_id) VALUES (?, ?, ?, ?)")) {
                    
                pstmt.setInt(1, userId);
                pstmt.setString(2, date);
                pstmt.setString(3, mealType);
                pstmt.setInt(4, foodId);
                
                int rowsAffected = pstmt.executeUpdate();
                
                return rowsAffected > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace(); // For error details
            return false;
        }
    }
    
    /**
     * @brief Gets user ID from username
     * @details Retrieves the user ID from the database:
     *          - Validates username
     *          - Queries users table
     *          - Returns user ID or -1 if not found
     * 
     * @param conn Database connection to use
     * @param username Username to look up
     * @return User ID if found, -1 otherwise
     * @throws SQLException If a database error occurs
     */
    private int getUserId(Connection conn, String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return -1;
        }
        
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
     * @brief Saves food information and returns its ID
     * @details Handles food data persistence:
     *          - Checks if food already exists
     *          - Updates or inserts food data
     *          - Handles nutrient information if available
     *          - Returns food ID or -1 if error occurs
     * 
     * @param conn Database connection to use
     * @param food Food object to save
     * @return Food ID if successful, -1 otherwise
     * @throws SQLException If a database error occurs
     */
    private int saveFoodAndGetId(Connection conn, Food food) throws SQLException {
        if (food == null) {
            return -1;
        }

        // Check if this food already exists
        String checkSql = "SELECT id FROM foods WHERE name = ? AND grams = ? AND calories = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, food.getName());
            checkStmt.setDouble(2, food.getGrams());
            checkStmt.setInt(3, food.getCalories());

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                int foodId = rs.getInt("id");

                // Eğer bu bir FoodNutrient nesnesiyse, besin değerlerini güncelle
                if (food instanceof FoodNutrient) {
                    FoodNutrient fn = (FoodNutrient) food;
                    updateFoodNutrients(conn, foodId, fn);
                }

                return foodId;
            }
        }

        // Added new food
        String insertSql = "INSERT INTO foods (name, grams, calories) VALUES (?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, food.getName());
            insertStmt.setDouble(2, food.getGrams());
            insertStmt.setInt(3, food.getCalories());

            int rowsAffected = insertStmt.executeUpdate();
            if (rowsAffected > 0) {
                
                try (Statement stmt = conn.createStatement();
                     ResultSet rs2 = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs2.next()) {
                        int foodId = rs2.getInt(1);

                        
                        if (food instanceof FoodNutrient) {
                            FoodNutrient fn = (FoodNutrient) food;
                            saveFoodNutrients(conn, foodId, fn);
                        }

                        return foodId;
                    }
                }
            }
        }

        return -1; 
    }

    
    /**
     * @brief Updates nutrient information for existing food
     * @details Modifies nutrient data in the database:
     *          - Checks if nutrient data exists
     *          - Updates existing data or creates new entry
     *          - Handles all nutrient fields
     * 
     * @param conn Database connection to use
     * @param foodId ID of the food to update
     * @param fn FoodNutrient object containing new values
     * @throws SQLException If a database error occurs
     */
    private void updateFoodNutrients(Connection conn, int foodId, FoodNutrient fn) throws SQLException {
        try (PreparedStatement checkStmt = conn.prepareStatement("SELECT id FROM food_nutrients WHERE food_id = ?")) {
            checkStmt.setInt(1, foodId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Update existing nutrients
                try (PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE food_nutrients SET protein = ?, carbs = ?, fat = ?, " +
                    "fiber = ?, sugar = ?, sodium = ? WHERE food_id = ?")) {
                    
                    updateStmt.setDouble(1, fn.getProtein());
                    updateStmt.setDouble(2, fn.getCarbs());
                    updateStmt.setDouble(3, fn.getFat());
                    updateStmt.setDouble(4, fn.getFiber());
                    updateStmt.setDouble(5, fn.getSugar());
                    updateStmt.setDouble(6, fn.getSodium());
                    updateStmt.setInt(7, foodId);
                    
                    updateStmt.executeUpdate();
                }
            } else {
                // Insert new nutrients
                saveFoodNutrients(conn, foodId, fn);
            }
        }
    }
    
    /**
     * @brief Saves nutrient information for new food
     * @details Creates new nutrient data entry:
     *          - Inserts all nutrient values
     *          - Links to food ID
     *          - Handles all nutrient fields
     * 
     * @param conn Database connection to use
     * @param foodId ID of the food to save nutrients for
     * @param fn FoodNutrient object containing values
     * @throws SQLException If a database error occurs
     */
    private void saveFoodNutrients(Connection conn, int foodId, FoodNutrient fn) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
            "INSERT INTO food_nutrients (food_id, protein, carbs, fat, fiber, sugar, sodium) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            
            pstmt.setInt(1, foodId);
            pstmt.setDouble(2, fn.getProtein());
            pstmt.setDouble(3, fn.getCarbs());
            pstmt.setDouble(4, fn.getFat());
            pstmt.setDouble(5, fn.getFiber());
            pstmt.setDouble(6, fn.getSugar());
            pstmt.setDouble(7, fn.getSodium());
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * @brief Logs food consumption for a user
     * @details Records food intake in the database:
     *          - Validates input parameters
     *          - Gets user ID
     *          - Saves food information
     *          - Creates food log entry
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @param food The food that was consumed
     * @return true if food was logged successfully, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean logFood(String username, String date, Food food) {
        if (username == null || date == null || food == null) {
            System.out.println("Cannot log food with null parameters");
            return false;
        }
        
        try {
            // Get user ID
            int userId = getUserId(connection, username);
            if (userId == -1) {
                System.out.println("User not found: " + username);
                return false;
            }
            
            // Save food and get its ID
            int foodId = saveFoodAndGetId(connection, food);
            if (foodId == -1) {
                return false;
            }
            
            // Add to food log
            try (PreparedStatement logStmt = connection.prepareStatement(
                "INSERT INTO food_logs (user_id, date, food_id) VALUES (?, ?, ?)")) {
                
                logStmt.setInt(1, userId);
                logStmt.setString(2, date);
                logStmt.setInt(3, foodId);
                
                int affectedRows = logStmt.executeUpdate();
                
                return affectedRows > 0;
            }
            
        } catch (SQLException e) {
            System.out.println("Could not log food: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * @brief Gets meal plan for a specific date and meal type
     * @details Retrieves planned meals from database:
     *          - Gets user ID
     *          - Queries meal plans table
     *          - Returns list of planned foods
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @param mealType The type of meal (breakfast, lunch, snack, dinner)
     * @return List of Food objects in the meal plan
     * @throws SQLException If a database error occurs
     */
    public List<Food> getMealPlan(String username, String date, String mealType) {
        List<Food> mealPlan = new ArrayList<>();
        
        if (username == null || date == null || mealType == null) {
            return mealPlan; // Return empty list for null parameters
        }
        
        try {
            // Get user ID
            int userId = getUserId(connection, username);
            if (userId == -1) {
                return mealPlan; // Empty list if user not found
            }
            
            try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT f.*, fn.* FROM meal_plans mp " +
                "JOIN foods f ON mp.food_id = f.id " +
                "LEFT JOIN food_nutrients fn ON f.id = fn.food_id " +
                "WHERE mp.user_id = ? AND mp.date = ? AND mp.meal_type = ?")) {
                
                pstmt.setInt(1, userId);
                pstmt.setString(2, date);
                pstmt.setString(3, mealType);
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    if (rs.getObject("protein") != null) {
                        // This food has nutrition data
                        mealPlan.add(new FoodNutrient(
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
                    } else {
                        // Basic food without nutrition data
                        mealPlan.add(new Food(
                            rs.getString("name"),
                            rs.getDouble("grams"),
                            rs.getInt("calories")
                        ));
                    }
                }
            }
            
        } catch (SQLException e) {
        }
        
        return mealPlan;
    }
    
    /**
     * @brief Gets food log for a specific date
     * @details Retrieves consumed foods from database:
     *          - Gets user ID
     *          - Queries food logs table
     *          - Returns list of consumed foods
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @return List of Food objects consumed
     * @throws SQLException If a database error occurs
     */
    public List<Food> getFoodLog(String username, String date) {
        List<Food> foodLog = new ArrayList<>();
        
        if (username == null || date == null) {
            return foodLog; // Return empty list for null parameters
        }
        
        try {
            // Get user ID
            int userId = getUserId(connection, username);
            if (userId == -1) {
                return foodLog; // Empty list if user not found
            }
            
            try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT f.*, fn.* FROM food_logs fl " +
                "JOIN foods f ON fl.food_id = f.id " +
                "LEFT JOIN food_nutrients fn ON f.id = fn.food_id " +
                "WHERE fl.user_id = ? AND fl.date = ?")) {
                
                pstmt.setInt(1, userId);
                pstmt.setString(2, date);
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    if (rs.getObject("protein") != null) {
                        // This food has nutrition data
                        foodLog.add(new FoodNutrient(
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
                    } else {
                        // Basic food without nutrition data
                        foodLog.add(new Food(
                            rs.getString("name"),
                            rs.getDouble("grams"),
                            rs.getInt("calories")
                        ));
                    }
                }
            }
            
        } catch (SQLException e) {

        }
        
        return foodLog;
    }
    
    /**
     * @brief Calculates total calories for a specific date
     * @details Sums up calories from all consumed foods:
     *          - Gets user ID
     *          - Queries food logs table
     *          - Calculates total calories
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @return Total calories consumed
     * @throws SQLException If a database error occurs
     */
    public int getTotalCalories(String username, String date) {
        if (username == null || date == null) {
            return 0; // Return 0 for null parameters
        }
        
        try {
            // Get user ID
            int userId = getUserId(connection, username);
            if (userId == -1) {
                return 0; // Return 0 if user not found
            }
            
            try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT SUM(f.calories) as total_calories FROM food_logs fl " +
                "JOIN foods f ON fl.food_id = f.id " +
                "WHERE fl.user_id = ? AND fl.date = ?")) {
                
                pstmt.setInt(1, userId);
                pstmt.setString(2, date);
                
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt("total_calories");
                }
            }
            
        } catch (SQLException e) {

        }
        
        return 0;
    }
    
    /**
     * @brief Validates date components
     * @details Checks if date components form a valid date:
     *          - Validates year range
     *          - Validates month range
     *          - Validates day range
     *          - Checks for leap years
     * 
     * @param year Year to validate
     * @param month Month to validate
     * @param day Day to validate
     * @return true if date is valid, false otherwise
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
     * @brief Formats date components into string
     * @details Creates date string in YYYY-MM-DD format:
     *          - Pads month and day with zeros
     *          - Combines components with hyphens
     * 
     * @param year Year component
     * @param month Month component
     * @param day Day component
     * @return Formatted date string
     */
    public String formatDate(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month, day);
    }
    
    /**
     * @brief Gets breakfast food options
     * @details Returns predefined breakfast food options:
     *          - Includes common breakfast items
     *          - Provides nutritional information
     * 
     * @return Array of Food objects for breakfast
     */
    public Food[] getBreakfastOptions() {
        // Try to get options from database first
        List<Food> options = getFoodOptionsByType("breakfast");
        
        // If no options are found in the database, return default options
        if (options.isEmpty()) {
            Food[] defaultOptions = {
                new Food("Scrambled Eggs", 150, 220),
                new Food("Oatmeal with Fruits", 250, 350),
                new Food("Greek Yogurt with Honey", 200, 180),
                new Food("Whole Grain Toast with Avocado", 120, 240),
                new Food("Smoothie Bowl", 300, 280),
                new Food("Pancakes with Maple Syrup", 180, 450),
                new Food("Breakfast Burrito", 220, 380),
                new Food("Fruit and Nut Granola", 100, 410)
            };
            
            // Save default options to database for future use
            try {
                connection.setAutoCommit(false);
                
                for (Food food : defaultOptions) {
                    saveFoodWithMealType(connection, food, "breakfast");
                }
                
                connection.commit();
            } catch (SQLException e) {
                System.out.println("Could not save default breakfast options: " + e.getMessage());
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                    }
                }
            }
            
            return defaultOptions;
        }
        
        return options.toArray(new Food[0]);
    }
    /**
     * @brief Gets lunch food options
     * @details Returns predefined lunch food options:
     *          - Includes common lunch items
     *          - Provides nutritional information
     * 
     * @return Array of Food objects for lunch
     */
    public Food[] getLunchOptions() {
        // Try to get options from database first
        List<Food> options = getFoodOptionsByType("lunch");
        
        // If no options are found in the database, return default options
        if (options.isEmpty()) {
            Food[] defaultOptions = {
                new Food("Grilled Chicken Salad", 350, 320),
                new Food("Quinoa Bowl with Vegetables", 280, 390),
                new Food("Turkey and Avocado Sandwich", 230, 450),
                new Food("Vegetable Soup with Bread", 400, 280),
                new Food("Tuna Salad Wrap", 250, 330),
                new Food("Falafel with Hummus", 300, 480),
                new Food("Caesar Salad with Grilled Chicken", 320, 370),
                new Food("Mediterranean Pasta Salad", 280, 410)
            };
            
            // Save default options to database for future use
            try {
                connection.setAutoCommit(false);
                
                for (Food food : defaultOptions) {
                    saveFoodWithMealType(connection, food, "lunch");
                }
                
                connection.commit();
            } catch (SQLException e) {
                System.out.println("Could not save default lunch options: " + e.getMessage());
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {
                    }
                }
            }
            
            return defaultOptions;
        }
        
        return options.toArray(new Food[0]);
    }
    
    /**
     * @brief Gets snack food options
     * @details Returns predefined snack food options:
     *          - Includes common snack items
     *          - Provides nutritional information
     * 
     * @return Array of Food objects for snacks
     */
    public Food[] getSnackOptions() {
        // Try to get options from database first
        List<Food> options = getFoodOptionsByType("snack");
        
        // If no options are found in the database, return default options
        if (options.isEmpty()) {
            Food[] defaultOptions = {
                new Food("Apple with Peanut Butter", 150, 220),
                new Food("Greek Yogurt with Berries", 180, 160),
                new Food("Mixed Nuts", 50, 290),
                new Food("Hummus with Carrot Sticks", 150, 180),
                new Food("Protein Bar", 60, 200),
                new Food("Fruit Smoothie", 250, 190),
                new Food("Dark Chocolate Square", 30, 170),
                new Food("Cheese and Crackers", 100, 230)
            };
            
            // Save default options to database for future use
            try {
                connection.setAutoCommit(false);
                
                for (Food food : defaultOptions) {
                    saveFoodWithMealType(connection, food, "snack");
                }
                
                connection.commit();
            } catch (SQLException e) {
                System.out.println("Could not save default snack options: " + e.getMessage());
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {

                    }
                }
            }
            
            return defaultOptions;
        }
        
        return options.toArray(new Food[0]);
    }
    
    /**
     * @brief Gets dinner food options
     * @details Returns predefined dinner food options:
     *          - Includes common dinner items
     *          - Provides nutritional information
     * 
     * @return Array of Food objects for dinner
     */
    public Food[] getDinnerOptions() {
        // Try to get options from database first
        List<Food> options = getFoodOptionsByType("dinner");
        
        // If no options are found in the database, return default options
        if (options.isEmpty()) {
            Food[] defaultOptions = {
                new Food("Grilled Salmon with Vegetables", 350, 420),
                new Food("Beef Stir Fry with Rice", 400, 520),
                new Food("Vegetable Curry with Tofu", 350, 380),
                new Food("Spaghetti with Tomato Sauce", 320, 450),
                new Food("Baked Chicken with Sweet Potato", 380, 390),
                new Food("Lentil Soup with Bread", 400, 350),
                new Food("Grilled Steak with Mashed Potatoes", 350, 550),
                new Food("Fish Tacos with Slaw", 300, 410)
            };
            
            // Save default options to database for future use
            try {
                connection.setAutoCommit(false);
                
                for (Food food : defaultOptions) {
                    saveFoodWithMealType(connection, food, "dinner");
                }
                
                connection.commit();
            } catch (SQLException e) {
                System.out.println("Could not save default dinner options: " + e.getMessage());
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException ex) {

                    }
                }
            }
            
            return defaultOptions;
        }
        
        return options.toArray(new Food[0]);
    }
    
    /**
     * @brief Gets food options by meal type
     * @details Retrieves food options from database:
     *          - Queries foods table
     *          - Filters by meal type
     *          - Returns list of foods
     * 
     * @param mealType Type of meal to get options for
     * @return List of Food objects for the meal type
     * @throws SQLException If a database error occurs
     */
    private List<Food> getFoodOptionsByType(String mealType) {
        List<Food> options = new ArrayList<>();
        
        if (mealType == null) {
            return options;
        }
        
        try {
            try (PreparedStatement pstmt = connection.prepareStatement(
                 "SELECT f.*, fn.* FROM foods f " +
                 "LEFT JOIN food_nutrients fn ON f.id = fn.food_id " +
                 "WHERE f.meal_type = ? " +
                 "LIMIT 8")) {
                
                pstmt.setString(1, mealType);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    if (rs.getObject("protein") != null) {
                        // This food has nutrition data
                        options.add(new FoodNutrient(
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
                    } else {
                        // Basic food without nutrition data
                        options.add(new Food(
                            rs.getString("name"),
                            rs.getDouble("grams"),
                            rs.getInt("calories")
                        ));
                    }
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Could not get food options: " + e.getMessage());
        }
        
        return options;
    }
    
    /**
     * @brief Saves food with meal type
     * @details Stores food information with meal type:
     *          - Checks for existing food
     *          - Updates or inserts food data
     *          - Links to meal type
     * 
     * @param conn Database connection to use
     * @param food Food object to save
     * @param mealType Type of meal
     * @return Food ID if successful, -1 otherwise
     * @throws SQLException If a database error occurs
     */
    private int saveFoodWithMealType(Connection conn, Food food, String mealType) throws SQLException {
        if (food == null || mealType == null) {
            return -1;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(
             "INSERT INTO foods (name, grams, calories, meal_type) VALUES (?, ?, ?, ?)",
             Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, food.getName());
            pstmt.setDouble(2, food.getGrams());
            pstmt.setInt(3, food.getCalories());
            pstmt.setString(4, mealType);
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int foodId = rs.getInt(1);
                
                // If this is a FoodNutrient, save the nutrients
                if (food instanceof FoodNutrient) {
                    FoodNutrient fn = (FoodNutrient) food;
                    saveFoodNutrients(conn, foodId, fn);
                }
                
                return foodId;
            }
        }
        
        return -1;
    }

    public List<String> getAllFoods() {
        List<String> foods = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT name FROM foods")) {
            
            while (rs.next()) {
                foods.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Foods could not be retrieved: " + e.getMessage());
        }
        return foods;
    }

    /**
     * @brief Adds meal to plan
     * @details Creates new meal plan entry:
     *          - Validates user ID
     *          - Checks food existence
     *          - Creates meal plan entry
     * 
     * @param userId ID of the user
     * @param day Day for the meal plan
     * @param mealType Type of meal
     * @param foodName Name of the food
     * @return true if meal was added successfully, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean addMealToPlan(int userId, String day, String mealType, String foodName) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (day == null || day.trim().isEmpty() || mealType == null || mealType.trim().isEmpty() || 
            foodName == null || foodName.trim().isEmpty()) {
            return false;
        }

        try {
            // First check if the food exists
            int foodId = -1;
            String foodQuery = "SELECT id FROM foods WHERE name = ?";
            try (PreparedStatement foodStmt = connection.prepareStatement(foodQuery)) {
                foodStmt.setString(1, foodName);
                ResultSet rs = foodStmt.executeQuery();
                if (rs.next()) {
                    foodId = rs.getInt("id");
                } else {
                    // If food doesn't exist, create it
                    String insertFood = "INSERT INTO foods (name, grams, calories) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertFood)) {
                        insertStmt.setString(1, foodName);
                        insertStmt.setDouble(2, 0);
                        insertStmt.setInt(3, 500);
                        insertStmt.executeUpdate();
                        try (Statement stmt = connection.createStatement();
                             ResultSet rs2 = stmt.executeQuery("SELECT last_insert_rowid()")) {
                            if (rs2.next()) {
                                foodId = rs2.getInt(1);
                            }
                        }
                    }
                }
            }

            if (foodId == -1) {
                return false;
            }

            // Now add the meal to the plan
            String date = LocalDate.now().toString();
            String sql = "INSERT INTO meal_plans (user_id, date, meal_type, food_id, day) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, date);
                pstmt.setString(3, mealType);
                pstmt.setInt(4, foodId);
                pstmt.setString(5, day);
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            System.out.println("Meal could not be added to plan: " + e.getMessage());
            return false;
        }
    }

    /**
     * @brief Adds meal with detailed information
     * @details Creates new meal with nutritional data:
     *          - Validates input parameters
     *          - Saves food information
     *          - Creates meal plan entry
     *          - Stores nutritional data
     * 
     * @param userId ID of the user
     * @param day Day for the meal
     * @param mealType Type of meal
     * @param foodName Name of the food
     * @param calories Calorie content
     * @param protein Protein content
     * @param carbs Carbohydrate content
     * @param fat Fat content
     * @param ingredients List of ingredients
     * @throws SQLException If a database error occurs
     */
    public void addMeal(int userId, String day, String mealType, String foodName, int calories, 
                       double protein, double carbs, double fat, String ingredients) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (day == null || day.trim().isEmpty() || mealType == null || mealType.trim().isEmpty() || 
            foodName == null || foodName.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid meal parameters");
        }

        try {
            int foodId = -1;
            String foodQuery = "SELECT id FROM foods WHERE name = ?";
            try (PreparedStatement foodStmt = connection.prepareStatement(foodQuery)) {
                foodStmt.setString(1, foodName);
                ResultSet rs = foodStmt.executeQuery();
                if (rs.next()) {
                    foodId = rs.getInt("id");
                } else {
                    String insertFood = "INSERT INTO foods (name, grams, calories) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertFood)) {
                        insertStmt.setString(1, foodName);
                        insertStmt.setDouble(2, 0);
                        insertStmt.setInt(3, calories);
                        insertStmt.executeUpdate();
                        try (Statement stmt = connection.createStatement();
                             ResultSet rs2 = stmt.executeQuery("SELECT last_insert_rowid()")) {
                            if (rs2.next()) {
                                foodId = rs2.getInt(1);
                            }
                        }
                    }
                }
            }

            if (foodId == -1) {
                throw new RuntimeException("Failed to create or find food");
            }

            // Save nutrients
            String nutrientSql = "INSERT INTO food_nutrients (food_id, protein, carbs, fat, fiber, sugar, sodium) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement nutrientStmt = connection.prepareStatement(nutrientSql)) {
                nutrientStmt.setInt(1, foodId);
                nutrientStmt.setDouble(2, protein);
                nutrientStmt.setDouble(3, carbs);
                nutrientStmt.setDouble(4, fat);
                nutrientStmt.setDouble(5, 0); // fiber
                nutrientStmt.setDouble(6, 0); // sugar
                nutrientStmt.setDouble(7, 0); // sodium
                nutrientStmt.executeUpdate();
            }

            String date = LocalDate.now().toString();
            String sql = "INSERT INTO meal_plans (user_id, date, meal_type, food_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, date);
                stmt.setString(3, mealType);
                stmt.setInt(4, foodId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding meal to plan: " + e.getMessage());
        }
    }

    /**
     * @brief Deletes meal from plan
     * @details Removes meal plan entry:
     *          - Validates input parameters
     *          - Deletes meal plan entry
     * 
     * @param day Day of the meal
     * @param mealType Type of meal
     * @throws SQLException If a database error occurs
     */
    public void deleteMeal(String day, String mealType) {
        String sql = "DELETE FROM meal_plans WHERE day = ? AND meal_type = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, day);
            stmt.setString(2, mealType);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting meal: " + e.getMessage());
        }
    }

    /**
     * @brief Gets weekly meal plan
     * @details Retrieves meal plan for the week:
     *          - Gets current date
     *          - Queries meal plans for week
     *          - Formats plan as string
     * 
     * @return Formatted weekly meal plan string
     * @throws SQLException If a database error occurs
     */
    public String getWeeklyMealPlan() {
        StringBuilder plan = new StringBuilder();
        String sql = "SELECT mp.day, mp.meal_type, f.name AS food_name, f.calories, " +
                    "COALESCE(fn.protein, 0) as protein, " +
                    "COALESCE(fn.carbs, 0) as carbs, " +
                    "COALESCE(fn.fat, 0) as fat " +
                    "FROM meal_plans mp " +
                    "JOIN foods f ON mp.food_id = f.id " +
                    "LEFT JOIN food_nutrients fn ON f.id = fn.food_id " +
                    "WHERE mp.day IS NOT NULL " +
                    "ORDER BY CASE mp.day " +
                    "WHEN 'Monday' THEN 1 " +
                    "WHEN 'Tuesday' THEN 2 " +
                    "WHEN 'Wednesday' THEN 3 " +
                    "WHEN 'Thursday' THEN 4 " +
                    "WHEN 'Friday' THEN 5 " +
                    "WHEN 'Saturday' THEN 6 " +
                    "WHEN 'Sunday' THEN 7 END, " +
                    "CASE mp.meal_type " +
                    "WHEN 'Breakfast' THEN 1 " +
                    "WHEN 'Lunch' THEN 2 " +
                    "WHEN 'Snack' THEN 3 " +
                    "WHEN 'Dinner' THEN 4 END";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            String currentDay = null;
            while (rs.next()) {
                String day = rs.getString("day");
                if (day != null && (currentDay == null || !day.equals(currentDay))) {
                    plan.append("\n").append(day).append(":\n");
                    currentDay = day;
                }
                plan.append("  ").append(rs.getString("meal_type")).append(": ")
                    .append(rs.getString("food_name"))
                    .append(" (Calories: ").append(rs.getInt("calories"))
                    .append(", Protein: ").append(rs.getDouble("protein")).append("g")
                    .append(", Carbs: ").append(rs.getDouble("carbs")).append("g")
                    .append(", Fat: ").append(rs.getDouble("fat")).append("g")
                    .append(")\n");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving weekly meal plan: " + e.getMessage());
        }
        return plan.toString();
    }

    /**
     * @brief Gets meals for specific day
     * @details Retrieves all meals for a day:
     *          - Queries meal plans table
     *          - Returns list of meals
     * 
     * @param day Day to get meals for
     * @return List of meal strings
     * @throws SQLException If a database error occurs
     */
    public List<String> getMealsForDay(String day) {
        List<String> meals = new ArrayList<>();
        if (day == null || day.trim().isEmpty()) {
            return meals;
        }
        
        try {
            String sql = "SELECT mp.meal_type, f.name AS food_name, f.calories, " +
                        "COALESCE(fn.protein, 0) as protein, " +
                        "COALESCE(fn.carbs, 0) as carbs, " +
                        "COALESCE(fn.fat, 0) as fat " +
                        "FROM meal_plans mp " +
                        "JOIN foods f ON mp.food_id = f.id " +
                        "LEFT JOIN food_nutrients fn ON f.id = fn.food_id " +
                        "WHERE mp.day = ? " +
                        "ORDER BY CASE mp.meal_type " +
                        "WHEN 'Breakfast' THEN 1 " +
                        "WHEN 'Lunch' THEN 2 " +
                        "WHEN 'Snack' THEN 3 " +
                        "WHEN 'Dinner' THEN 4 END";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, day);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String mealInfo = String.format("%s: %s (Calories: %d, Protein: %.1fg, Carbs: %.1fg, Fat: %.1fg)",
                            rs.getString("meal_type"),
                            rs.getString("food_name"),
                            rs.getInt("calories"),
                            rs.getDouble("protein"),
                            rs.getDouble("carbs"),
                            rs.getDouble("fat"));
                        meals.add(mealInfo);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return meals;
    }
}