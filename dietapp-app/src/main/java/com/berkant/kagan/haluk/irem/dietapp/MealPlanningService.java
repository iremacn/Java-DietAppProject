package com.berkant.kagan.haluk.irem.dietapp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles meal planning and logging operations for the Diet Planner application.
 * @details The MealPlanningService class provides methods for planning meals,
 *          logging food intake, and viewing meal history.
 * @author berkant
 */
public class MealPlanningService {
    
    /**
     * Constructor for MealPlanningService class.
     * Uses database for data storage.
     */
    public MealPlanningService() {
        // No initialization needed, using database for storage
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
        if (username == null || date == null || mealType == null || food == null) {
            System.out.println("Cannot add meal plan with null parameters");
            return false;
        }
        
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return false;
            }
            
            conn.setAutoCommit(false); // Start transaction
            
            // Get user ID
            int userId = getUserId(conn, username);
            if (userId == -1) {
                System.out.println("User not found: " + username);
                return false; // User not found
            }
            
            // Save food and get its ID
            int foodId = saveFoodAndGetId(conn, food);
            if (foodId == -1) {
                return false; // Food couldn't be saved
            }
            
            // Add to meal plan
            try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO meal_plans (user_id, date, meal_type, food_id) VALUES (?, ?, ?, ?)")) {
                    
                pstmt.setInt(1, userId);
                pstmt.setString(2, date);
                pstmt.setString(3, mealType);
                pstmt.setInt(4, foodId);
                
                int rowsAffected = pstmt.executeUpdate();
                
                // Commit transaction if successful
                if (rowsAffected > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback transaction on error
                }
            } catch (SQLException ex) {
            }
            e.printStackTrace(); // For error details
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                } catch (SQLException e) {
                }
                DatabaseHelper.releaseConnection(conn);
            }
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
     * Helper method to save food and get its ID
     * Uses a transaction to ensure data consistency
     * 
     * @param conn Database connection
     * @param food Food to save
     * @return Food ID or -1 if error occurs
     * @throws SQLException If database error occurs
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
                     ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        int foodId = rs.getInt(1);

                        
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
     * Helper method to update food nutrients
     * 
     * @param conn Database connection
     * @param foodId Food ID
     * @param fn FoodNutrient object
     * @throws SQLException If database error occurs
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
     * Helper method to save food nutrients
     * 
     * @param conn Database connection
     * @param foodId Food ID
     * @param fn FoodNutrient object
     * @throws SQLException If database error occurs
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
     * Logs food consumed by the user.
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @param food The food that was consumed
     * @return true if logged successfully
     */
    public boolean logFood(String username, String date, Food food) {
        if (username == null || date == null || food == null) {
            System.out.println("Cannot log food with null parameters");
            return false;
        }
        
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return false;
            }
            
            conn.setAutoCommit(false); // Start transaction
            
            // Get user ID
            int userId = getUserId(conn, username);
            if (userId == -1) {
                System.out.println("User not found: " + username);
                conn.rollback();
                return false;
            }
            
            // Save food and get its ID
            int foodId = saveFoodAndGetId(conn, food);
            if (foodId == -1) {
                conn.rollback();
                return false;
            }
            
            // Add to food log
            try (PreparedStatement logStmt = conn.prepareStatement(
                "INSERT INTO food_logs (user_id, date, food_id) VALUES (?, ?, ?)")) {
                
                logStmt.setInt(1, userId);
                logStmt.setString(2, date);
                logStmt.setInt(3, foodId);
                
                int affectedRows = logStmt.executeUpdate();
                
                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("Failed to rollback transaction: " + ex.getMessage());
            }
            System.out.println("Could not log food: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                }
                DatabaseHelper.releaseConnection(conn);
            }
        }
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
        List<Food> mealPlan = new ArrayList<>();
        
        if (username == null || date == null || mealType == null) {
            return mealPlan; // Return empty list for null parameters
        }
        
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return mealPlan; // Return empty list if connection fails
            }
            
            // Get user ID
            int userId = getUserId(conn, username);
            if (userId == -1) {
                return mealPlan; // Empty list if user not found
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(
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
        } finally {
            DatabaseHelper.releaseConnection(conn);
        }
        
        return mealPlan;
    }
    
    /**
     * Gets all food logged for a specific date.
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @return List of foods logged for the specified date
     */
    public List<Food> getFoodLog(String username, String date) {
        List<Food> foodLog = new ArrayList<>();
        
        if (username == null || date == null) {
            return foodLog; // Return empty list for null parameters
        }
        
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return foodLog; // Return empty list if connection fails
            }
            
            // Get user ID
            int userId = getUserId(conn, username);
            if (userId == -1) {
                return foodLog; // Empty list if user not found
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(
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

        } finally {
            DatabaseHelper.releaseConnection(conn);
        }
        
        return foodLog;
    }
    
    /**
     * Calculates the total calories consumed on a specific date.
     * 
     * @param username The username of the user
     * @param date The date in format YYYY-MM-DD
     * @return The total calories consumed
     */
    public int getTotalCalories(String username, String date) {
        if (username == null || date == null) {
            return 0; // Return 0 for null parameters
        }
        
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return 0; // Return 0 if connection fails
            }
            
            // Get user ID
            int userId = getUserId(conn, username);
            if (userId == -1) {
                return 0; // Return 0 if user not found
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(
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

        } finally {
            DatabaseHelper.releaseConnection(conn);
        }
        
        return 0;
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
            Connection conn = null;
            try {
                conn = DatabaseHelper.getConnection();
                if (conn != null) {
                    conn.setAutoCommit(false);
                    
                    for (Food food : defaultOptions) {
                        saveFoodWithMealType(conn, food, "breakfast");
                    }
                    
                    conn.commit();
                }
            } catch (SQLException e) {
                System.out.println("Could not save default breakfast options: " + e.getMessage());
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                    }
                }
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                    } catch (SQLException e) {
                    }
                    DatabaseHelper.releaseConnection(conn);
                }
            }
            
            return defaultOptions;
        }
        
        return options.toArray(new Food[0]);
    }
    /**
     * Gets predefined lunch food options.
     * 
     * @return Array of lunch food options
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
            Connection conn = null;
            try {
                conn = DatabaseHelper.getConnection();
                if (conn != null) {
                    conn.setAutoCommit(false);
                    
                    for (Food food : defaultOptions) {
                        saveFoodWithMealType(conn, food, "lunch");
                    }
                    
                    conn.commit();
                }
            } catch (SQLException e) {
                System.out.println("Could not save default lunch options: " + e.getMessage());
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                    }
                }
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                    } catch (SQLException e) {
                    }
                    DatabaseHelper.releaseConnection(conn);
                }
            }
            
            return defaultOptions;
        }
        
        return options.toArray(new Food[0]);
    }
    
    /**
     * Gets predefined snack food options.
     * 
     * @return Array of snack food options
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
            Connection conn = null;
            try {
                conn = DatabaseHelper.getConnection();
                if (conn != null) {
                    conn.setAutoCommit(false);
                    
                    for (Food food : defaultOptions) {
                        saveFoodWithMealType(conn, food, "snack");
                    }
                    
                    conn.commit();
                }
            } catch (SQLException e) {
                System.out.println("Could not save default snack options: " + e.getMessage());
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {

                    }
                }
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                    } catch (SQLException e) {

                    }
                    DatabaseHelper.releaseConnection(conn);
                }
            }
            
            return defaultOptions;
        }
        
        return options.toArray(new Food[0]);
    }
    
    /**
     * Gets predefined dinner food options.
     * 
     * @return Array of dinner food options
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
            Connection conn = null;
            try {
                conn = DatabaseHelper.getConnection();
                if (conn != null) {
                    conn.setAutoCommit(false);
                    
                    for (Food food : defaultOptions) {
                        saveFoodWithMealType(conn, food, "dinner");
                    }
                    
                    conn.commit();
                }
            } catch (SQLException e) {
                System.out.println("Could not save default dinner options: " + e.getMessage());
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {

                    }
                }
            } finally {
                if (conn != null) {
                	try {
                        conn.setAutoCommit(true);
                    } catch (SQLException e) {

                    }
                    DatabaseHelper.releaseConnection(conn);
                }
            }
            
            return defaultOptions;
        }
        
        return options.toArray(new Food[0]);
    }
    
    /**
     * Gets food options by meal type from the database.
     * 
     * @param mealType The type of meal (breakfast, lunch, snack, dinner)
     * @return List of food options for the meal type
     */
    private List<Food> getFoodOptionsByType(String mealType) {
        List<Food> options = new ArrayList<>();
        
        if (mealType == null) {
            return options;
        }
        
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            if (conn == null) {
                return options;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(
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
        } finally {
            DatabaseHelper.releaseConnection(conn);
        }
        
        return options;
    }
    
    /**
     * Saves a food with a meal type to the database.
     * 
     * @param conn The database connection
     * @param food The food to save
     * @param mealType The type of meal
     * @return The ID of the saved food
     * @throws SQLException If an error occurs
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
}