package com.berkant.kagan.haluk.irem.dietapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles shopping list generation operations for the Diet Planner application.
 * @details The ShoppingListService class provides methods for generating shopping
 *          lists based on meal plans and calculating costs.
 * @author haluk
 */
public class ShoppingListService {
    /** Service for accessing meal planning data and food options */
    private MealPlanningService mealPlanningService;
    
    /**
     * Constructor for ShoppingListService class.
     * 
     * @param mealPlanningService The meal planning service
     */
    public ShoppingListService(MealPlanningService mealPlanningService) {
        this.mealPlanningService = mealPlanningService;
        
        // Create tables and sample data in the database if they exist
        try {
            initializeIngredientsAndRecipes();
        } catch (SQLException e) {
            System.out.println("Could not initialize ingredients and recipes database: " + e.getMessage());
        }
    }
    
    /**
     * Gets the database connection used by the service.
     * Protected for testing purposes in subclasses.
     * 
     * @return The database connection
     */
    protected Connection getConnection() {
        return DatabaseHelper.getConnection();
    }
    
    /**
     * Initializes ingredients and recipes in the database if they don't exist.
     * 
     * @throws SQLException If a database error occurs
     */
    private void initializeIngredientsAndRecipes() throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection(); // Protected metodu kullan
            if (conn == null) {
                System.out.println("Failed to obtain database connection for ingredients initialization");
                return;
            }
            
            // Check if ingredients already exist
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ingredients")) {
                
                rs.next();
                int count = rs.getInt(1);
                
                // If no ingredients exist, initialize the data
                if (count == 0) {
                    conn.setAutoCommit(false);
                    initializeIngredientPrices(conn);
                    conn.commit();
                }
            }
        } catch (SQLException e) {
            // Table might not exist yet, this is handled in DatabaseHelper
            System.out.println("Error during ingredient database check: " + e.getMessage());
            if (conn != null) { 
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Failed to rollback: " + ex.getMessage());
                }
            }
            throw e; // Re-throw for caller to handle
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.out.println("Failed to reset auto-commit: " + e.getMessage());
                }
                DatabaseHelper.releaseConnection(conn);
            }
        }
    }
    
    /**
     * Initializes the ingredient prices database with default values.
     * 
     * @param conn The database connection
     * @throws SQLException If a database error occurs during insertion
     */
    private void initializeIngredientPrices(Connection conn) throws SQLException {
        // Prepare statement for ingredient insertion
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO ingredients (name, price) VALUES (?, ?)")) {
            
            // Define ingredient prices
            Map<String, Double> ingredientPrices = new HashMap<>();
            
            // Vegetables
            ingredientPrices.put("Tomato", 1.20);
            ingredientPrices.put("Cucumber", 0.90);
            // ...other foods...
            
            // Insert each ingredient into the database
            for (Map.Entry<String, Double> entry : ingredientPrices.entrySet()) {
                pstmt.setString(1, entry.getKey());
                pstmt.setDouble(2, entry.getValue());
                pstmt.executeUpdate();
            }
            
            System.out.println("Ingredient prices saved to database.");
        }
    }
    


    
    /**
     * Inserts a recipe into the database.
     * 
     * @param conn The database connection
     * @param mealType The type of meal
     * @param recipeName The name of the recipe
     * @return The ID of the inserted recipe or -1 if failed
     * @throws SQLException If a database error occurs
     */
    private int insertRecipe(Connection conn, String mealType, String recipeName) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
            "INSERT INTO recipes (meal_type, name) VALUES (?, ?)",
            Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, mealType);
            pstmt.setString(2, recipeName);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return -1;
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    return -1;
                }
            }
        }
    }
    
    /**
     * Inserts a recipe ingredient into the database.
     * 
     * @param conn The database connection
     * @param recipeId The ID of the recipe
     * @param ingredientName The name of the ingredient
     * @param amount The amount of the ingredient
     * @param unit The unit of measurement
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    private boolean insertRecipeIngredient(Connection conn, int recipeId, String ingredientName, double amount, String unit) throws SQLException {
        // First, get the ingredient ID
        int ingredientId = getIngredientId(conn, ingredientName);
        if (ingredientId == -1) {
            return false;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(
             "INSERT INTO recipe_ingredients (recipe_id, ingredient_id, amount, unit) VALUES (?, ?, ?, ?)")) {
            
            pstmt.setInt(1, recipeId);
            pstmt.setInt(2, ingredientId);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, unit);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Gets the ID of an ingredient by name.
     * 
     * @param conn The database connection
     * @param ingredientName The name of the ingredient
     * @return The ID of the ingredient or -1 if not found
     * @throws SQLException If a database error occurs
     */
    private int getIngredientId(Connection conn, String ingredientName) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
             "SELECT id FROM ingredients WHERE name = ?")) {
            
            pstmt.setString(1, ingredientName);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                return -1;
            }
        }
    }
    
    /**
     * Gets the ingredients for a specific food item.
     * 
     * @param mealType The type of meal (breakfast, lunch, snack, dinner)
     * @param foodName The name of the food
     * @return List of ingredients for the food item or empty list if not found
     */
    public List<Ingredient> getIngredientsForFood(String mealType, String foodName) {
        List<Ingredient> ingredients = new ArrayList<>();
        
        if (mealType == null || foodName == null) {
            return ingredients;
        }
        
        Connection conn = null;
        try {
            conn = getConnection(); // Protected metodu kullan
            if (conn == null) {
                return ingredients;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT i.name, ri.amount, ri.unit, i.price " +
                 "FROM recipe_ingredients ri " +
                 "JOIN ingredients i ON ri.ingredient_id = i.id " +
                 "JOIN recipes r ON ri.recipe_id = r.id " +
                 "WHERE r.meal_type = ? AND r.name = ?")) {
                
                pstmt.setString(1, mealType.toLowerCase());
                pstmt.setString(2, foodName);
                
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    String name = rs.getString("name");
                    double amount = rs.getDouble("amount");
                    String unit = rs.getString("unit");
                    double price = rs.getDouble("price");
                    
                    ingredients.add(new Ingredient(name, amount, unit, price));
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Could not get ingredient list: " + e.getMessage());
        } finally {
            DatabaseHelper.releaseConnection(conn);
        }
        
        return ingredients;
    }
    
    /**
     * Calculates the total cost of ingredients for a food item.
     * 
     * @param ingredients List of ingredients
     * @return Total cost of all ingredients
     */
    public double calculateTotalCost(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return 0.0;
        }
        
        double totalCost = 0.0;
        
        for (Ingredient ingredient : ingredients) {
            String unit = ingredient.getUnit();
            double amount = ingredient.getAmount();
            double price = ingredient.getPrice();
            
            if ("unit".equals(unit)) {
                // Direct price per unit (pieces)
                totalCost += amount * price;
            } 
            else if ("g".equals(unit) || "ml".equals(unit)) {
                // Price per 100 grams or milliliters
                totalCost += (amount / 100.0) * price;
            }
            else {
                // Default calculation for other units
                totalCost += amount * price;
            }
        }
        
        return totalCost;
    }

    public List<String> generateShoppingList() {
        List<String> shoppingList = new ArrayList<>();
        Map<String, Double> ingredientAmounts = new HashMap<>();
        Connection conn = null;
        
        try {
            conn = getConnection();
            if (conn == null) {
                throw new SQLException("Failed to obtain database connection");
            }
            
            // Collect ingredients from weekly meal plan
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT ri.ingredient_id, i.name, SUM(ri.amount) as total_amount, ri.unit " +
                    "FROM recipe_ingredients ri " +
                    "JOIN ingredients i ON ri.ingredient_id = i.id " +
                    "JOIN recipes r ON ri.recipe_id = r.id " +
                    "JOIN meal_plans mp ON mp.food_id = (SELECT f.id FROM foods f WHERE f.name = r.name) " +
                    "GROUP BY ri.ingredient_id, i.name, ri.unit")) {
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String ingredientName = rs.getString("name");
                        Double amount = rs.getDouble("total_amount");
                        String unit = rs.getString("unit");
                        
                        if (ingredientName != null && amount != null && unit != null) {
                            // Combine ingredient amounts
                            if (ingredientAmounts.containsKey(ingredientName)) {
                                amount += ingredientAmounts.get(ingredientName);
                            }
                            ingredientAmounts.put(ingredientName, amount);
                            
                            // Add to shopping list
                            shoppingList.add(String.format("%s - %.2f %s", 
                                ingredientName, amount, unit));
                        }
                    }
                }
            }
            
            // Calculate total cost
            double totalCost = 0.0;
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT i.name, i.price, SUM(ri.amount) as total_amount " +
                    "FROM recipe_ingredients ri " +
                    "JOIN ingredients i ON ri.ingredient_id = i.id " +
                    "JOIN recipes r ON ri.recipe_id = r.id " +
                    "JOIN meal_plans mp ON mp.food_id = (SELECT f.id FROM foods f WHERE f.name = r.name) " +
                    "GROUP BY i.name, i.price")) {
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Double price = rs.getDouble("price");
                        Double amount = rs.getDouble("total_amount");
                        
                        if (price != null && amount != null) {
                            totalCost += price * amount;
                        }
                    }
                }
            }
            
            // Add total cost to list
            shoppingList.add(String.format("\nToplam Tahmini Maliyet: %.2f TL", totalCost));
            
        } catch (SQLException e) {
            System.err.println("Error generating shopping list: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate shopping list", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
        
        return shoppingList;
    }

    /**
     * Inner class to represent an ingredient with its amount, unit, and price.
     * @details Contains information about a specific ingredient including its
     *          name, quantity, measurement unit, and price per unit.
     */
    public class Ingredient {
        /** The name of the ingredient */
        private String name;
        /** The quantity of the ingredient needed */
        private double amount;
        /** The unit of measurement (g, ml, unit, etc.) */
        private String unit;
        /** The price per standard unit */
        private double price;
        
        /**
         * Constructor for Ingredient class.
         * 
         * @param name The name of the ingredient
         * @param amount The amount of the ingredient
         * @param unit The unit of measurement (g, ml, unit, etc.)
         * @param price The price per standard unit
         */
        public Ingredient(String name, double amount, String unit, double price) {
            this.name = name != null ? name : "";
            this.amount = Math.max(0, amount);
            this.unit = unit != null ? unit : "";
            this.price = Math.max(0, price);
        }
        
        /**
         * Gets the name of the ingredient.
         * 
         * @return The ingredient name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Gets the amount of the ingredient.
         * 
         * @return The amount
         */
        public double getAmount() {
            return amount;
        }
        
        /**
         * Gets the unit of measurement for the ingredient.
         * 
         * @return The unit (g, ml, unit, etc.)
         */
        public String getUnit() {
            return unit;
        }
        
        /**
         * Gets the price of the ingredient per standard unit.
         * 
         * @return The price
         */
        public double getPrice() {
            return price;
        }
        
        /**
         * Returns a string representation of the Ingredient object.
         * 
         * @return A string containing ingredient information
         */
        @Override
        public String toString() {
            return name + " (" + amount + " " + unit + ")";
        }
    }
}