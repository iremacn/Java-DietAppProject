
package com.berkant.kagan.haluk.irem.dietapp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles database operations for the Diet Planner application.
 * @details The DatabaseHelper class provides methods for initializing and managing the SQLite database.
 * @author Claude
 */
public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:dietplanner.db";
    private static final int MAX_CONNECTIONS = 10;
    private static List<Connection> connectionPool = new ArrayList<>();
    
    static {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found: " + e.getMessage());
        }
    }
    
    /**
     * Initializes the database connection and creates tables if they don't exist.
     */
    public static void initializeDatabase() {
        try {
            // Create database connection and initialize tables
            Connection conn = getConnection();
            if (conn != null) {
                createTables(conn);
                releaseConnection(conn);
                System.out.println("Database connection successful");
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }
    
    /**
     * Gets a database connection from the pool or creates a new one.
     * 
     * @return The Connection object
     */
    public static synchronized Connection getConnection() {
        try {
            if (connectionPool.isEmpty()) {
                return createConnection();
            } else {
                Connection conn = connectionPool.remove(connectionPool.size() - 1);
                if (conn.isClosed()) {
                    return createConnection();
                }
                return conn;
            }
        } catch (SQLException e) {
            System.out.println("Could not get database connection: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Creates a new database connection with optimized settings.
     * 
     * @return A new Connection object
     * @throws SQLException If connection creation fails
     */
    private static Connection createConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(
            DB_URL + "?journal_mode=WAL&synchronous=NORMAL&cache_size=1000");
        return connection;
    }
    
    /**
     * Releases a connection back to the connection pool.
     * 
     * @param conn The Connection to release
     */
    public static synchronized void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed() && connectionPool.size() < MAX_CONNECTIONS) {
                    connectionPool.add(conn);
                } else {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        System.out.println("Could not close connection: " + e.getMessage());
                    }
                }
            } catch (SQLException e) {
                System.out.println("Error checking connection status: " + e.getMessage());
            }
        }
    }
    
    /**
     * Closes all connections in the pool.
     */
    public static void closeAllConnections() {
        for (Connection conn : connectionPool) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Could not close connection: " + e.getMessage());
            }
        }
        connectionPool.clear();
    }
    
    /**
     * Closes the database connection.
     */
    public static void closeConnection() {
        closeAllConnections();
        System.out.println("Database connections closed");
    }
    
    /**
     * Creates all necessary tables in the database.
     * 
     * @param conn The database connection
     */
    private static void createTables(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            // Users table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "email TEXT NOT NULL," +
                "name TEXT NOT NULL," +
                "is_logged_in INTEGER DEFAULT 0" +
                ");"
            );
            
            // Foods table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS foods (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "grams REAL NOT NULL," +
                "calories INTEGER NOT NULL," +
                "meal_type TEXT" +
                ");"
            );
            
            // FoodNutrient table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS food_nutrients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "food_id INTEGER NOT NULL," +
                "protein REAL NOT NULL," +
                "carbs REAL NOT NULL," +
                "fat REAL NOT NULL," +
                "fiber REAL NOT NULL," +
                "sugar REAL NOT NULL," +
                "sodium REAL NOT NULL," +
                "FOREIGN KEY(food_id) REFERENCES foods(id)" +
                ");"
            );
            
            // NutritionGoals table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS nutrition_goals (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "calorie_goal INTEGER NOT NULL," +
                "protein_goal REAL NOT NULL," +
                "carb_goal REAL NOT NULL," +
                "fat_goal REAL NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id)" +
                ");"
            );
            
            // MealPlans table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS meal_plans (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "date TEXT NOT NULL," +
                "meal_type TEXT NOT NULL," +
                "food_id INTEGER NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id)," +
                "FOREIGN KEY(food_id) REFERENCES foods(id)" +
                ");"
            );
            
            // FoodLogs table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS food_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "date TEXT NOT NULL," +
                "food_id INTEGER NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id)," +
                "FOREIGN KEY(food_id) REFERENCES foods(id)" +
                ");"
            );
            
            // DietProfiles table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS diet_profiles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "diet_type TEXT NOT NULL," +
                "weight_goal TEXT NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id)" +
                ");"
            );
            
            // HealthConditions table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS health_conditions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "profile_id INTEGER NOT NULL," +
                "condition_name TEXT NOT NULL," +
                "FOREIGN KEY(profile_id) REFERENCES diet_profiles(id)" +
                ");"
            );
            
            // ExcludedFoods table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS excluded_foods (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "profile_id INTEGER NOT NULL," +
                "food_name TEXT NOT NULL," +
                "FOREIGN KEY(profile_id) REFERENCES diet_profiles(id)" +
                ");"
            );
            
            // Ingredients table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS ingredients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT UNIQUE NOT NULL," +
                "price REAL NOT NULL" +
                ");"
            );

            // Recipes table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS recipes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "meal_type TEXT NOT NULL," +
                "food_id INTEGER," +
                "name TEXT NOT NULL," +
                "FOREIGN KEY(food_id) REFERENCES foods(id)" +
                ");"
            );

            // Recipe Ingredients table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS recipe_ingredients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "recipe_id INTEGER NOT NULL," +
                "ingredient_id INTEGER NOT NULL," +
                "amount REAL NOT NULL," +
                "unit TEXT NOT NULL," +
                "FOREIGN KEY(recipe_id) REFERENCES recipes(id)," +
                "FOREIGN KEY(ingredient_id) REFERENCES ingredients(id)" +
                ");"
            );
            
            // Insert sample data (optional)
            insertSampleData(statement);
            
            System.out.println("Database tables created successfully");
        }
    }
    
    /**
     * Helper method to insert sample data for testing purposes.
     * 
     * @param statement The SQL statement
     * @throws SQLException If there is an error executing SQL
     */
    private static void insertSampleData(Statement statement) throws SQLException {
        // Add sample user (for testing purposes only, in empty database)
        try {
            statement.execute(
                "INSERT INTO users (username, password, email, name) " +
                "SELECT 'admin', 'admin123', 'admin@example.com', 'Admin User' " +
                "WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');"
            );
        } catch (SQLException e) {
            // Data may already exist, we can ignore this error
        }
    }
   
    /**
     * Helper method to get a user's ID by username.
     * 
     * @param username The username to look up
     * @return The user ID or -1 if not found
     */
    public static int getUserId(String username) {
        Connection conn = null;
        try {
            conn = getConnection();
            int userId = -1;
            
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    userId = rs.getInt("id");
                }
            }
            
            return userId;
        } catch (SQLException e) {
            System.out.println("Could not get user ID: " + e.getMessage());
            return -1;
        } finally {
            releaseConnection(conn);
        }
    }
   
    /**
     * Helper method to save a Food object to the database and return its ID.
     * 
     * @param food The Food object to save
     * @return The food ID in the database or -1 if there was an error
     */
    public static int saveFoodAndGetId(Food food) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) {
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
                    // Food already exists, return its ID
                    int foodId = rs.getInt("id");
                    
                    // If this is a FoodNutrient, update the nutrients
                    if (food instanceof FoodNutrient) {
                        FoodNutrient fn = (FoodNutrient) food;
                        updateFoodNutrients(conn, foodId, fn);
                    }
                    
                    return foodId;
                }
            }
            
            // Insert new food
            String insertSql = "INSERT INTO foods (name, grams, calories) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, food.getName());
                insertStmt.setDouble(2, food.getGrams());
                insertStmt.setInt(3, food.getCalories());
                
                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    // Get the generated ID
                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int foodId = generatedKeys.getInt(1);
                        
                        // If this is a FoodNutrient, save the nutrients
                        if (food instanceof FoodNutrient) {
                            FoodNutrient fn = (FoodNutrient) food;
                            saveFoodNutrients(conn, foodId, fn);
                        }
                        
                        return foodId;
                    }
                }
            }
            
            return -1; // Error
        } finally {
            releaseConnection(conn);
        }
    }

    /**
     * Helper method to update food nutrients in the database.
     * 
     * @param conn The database connection
     * @param foodId The ID of the food in the database
     * @param foodNutrient The FoodNutrient object containing the nutrients
     * @return true if successful, false otherwise
     */
    private static boolean updateFoodNutrients(Connection conn, int foodId, FoodNutrient foodNutrient) {
        try {
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT id FROM food_nutrients WHERE food_id = ?")) {
                checkStmt.setInt(1, foodId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    // Update existing record
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE food_nutrients SET protein = ?, carbs = ?, fat = ?, " +
                            "fiber = ?, sugar = ?, sodium = ? WHERE food_id = ?")) {
                        
                        updateStmt.setDouble(1, foodNutrient.getProtein());
                        updateStmt.setDouble(2, foodNutrient.getCarbs());
                        updateStmt.setDouble(3, foodNutrient.getFat());
                        updateStmt.setDouble(4, foodNutrient.getFiber());
                        updateStmt.setDouble(5, foodNutrient.getSugar());
                        updateStmt.setDouble(6, foodNutrient.getSodium());
                        updateStmt.setInt(7, foodId);
                        
                        return updateStmt.executeUpdate() > 0;
                    }
                } else {
                    // Insert new record
                    return saveFoodNutrients(conn, foodId, foodNutrient);
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not update nutrient values: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to save food nutrients to the database.
     * 
     * @param conn The database connection
     * @param foodId The ID of the food in the database
     * @param foodNutrient The FoodNutrient object containing the nutrients
     * @return true if successful, false otherwise
     */
    private static boolean saveFoodNutrients(Connection conn, int foodId, FoodNutrient foodNutrient) {
        try {
            try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO food_nutrients (food_id, protein, carbs, fat, fiber, sugar, sodium) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                
                pstmt.setInt(1, foodId);
                pstmt.setDouble(2, foodNutrient.getProtein());
                pstmt.setDouble(3, foodNutrient.getCarbs());
                pstmt.setDouble(4, foodNutrient.getFat());
                pstmt.setDouble(5, foodNutrient.getFiber());
                pstmt.setDouble(6, foodNutrient.getSugar());
                pstmt.setDouble(7, foodNutrient.getSodium());
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Could not save nutrient values: " + e.getMessage());
            return false;
        }
    }
}