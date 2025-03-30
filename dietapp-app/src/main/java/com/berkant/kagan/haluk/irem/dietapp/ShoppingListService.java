package com.berkant.kagan.haluk.irem.dietapp;

import java.sql.*;
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
    // Service dependencies
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
     * Servis tarafından kullanılan veritabanı bağlantısını alır.
     * Alt sınıflarla test edilebilir olması için protected olarak tanımlanmıştır.
     * 
     * @return Veritabanı bağlantısı
     */
    protected Connection getConnection() {
        return DatabaseHelper.getConnection();
    }
    
    /**
     * Initializes ingredients and recipes in the database if they don't exist.
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
                    initializeRecipeIngredients(conn);
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
     * Initializes the ingredient prices database.
     * 
     * @param conn The database connection
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
     * Initializes recipe ingredients for all food items in the meal planning service.
     * 
     * @param conn The database connection
     */
    private void initializeRecipeIngredients(Connection conn) throws SQLException {
        initializeBreakfastRecipes(conn);
        initializeLunchRecipes(conn);
        initializeSnackRecipes(conn);
        initializeDinnerRecipes(conn);
        
        System.out.println("Recipes saved to database.");
    }
    
    /**
     * Initializes breakfast recipe ingredients.
     * 
     * @param conn The database connection
     */
    private void initializeBreakfastRecipes(Connection conn) throws SQLException {
        String mealType = "breakfast";
        
        // Scrambled Eggs
        int recipeId = insertRecipe(conn, mealType, "Scrambled Eggs");
        if (recipeId != -1) {
            insertRecipeIngredient(conn, recipeId, "Eggs", 3, "unit");
            insertRecipeIngredient(conn, recipeId, "Milk", 30, "ml");
            insertRecipeIngredient(conn, recipeId, "Salt", 2, "g");
            insertRecipeIngredient(conn, recipeId, "Pepper", 1, "g");
            insertRecipeIngredient(conn, recipeId, "Butter", 10, "g");
        }
        
        // Oatmeal with Fruits
        recipeId = insertRecipe(conn, mealType, "Oatmeal with Fruits");
        if (recipeId != -1) {
            insertRecipeIngredient(conn, recipeId, "Oats", 80, "g");
            insertRecipeIngredient(conn, recipeId, "Milk", 200, "ml");
            insertRecipeIngredient(conn, recipeId, "Banana", 1, "unit");
            insertRecipeIngredient(conn, recipeId, "Strawberry", 50, "g");
            insertRecipeIngredient(conn, recipeId, "Honey", 15, "ml");
        }
        
        // Greek Yogurt with Honey
        recipeId = insertRecipe(conn, mealType, "Greek Yogurt with Honey");
        if (recipeId != -1) {
            insertRecipeIngredient(conn, recipeId, "Greek Yogurt", 200, "g");
            insertRecipeIngredient(conn, recipeId, "Honey", 20, "ml");
            insertRecipeIngredient(conn, recipeId, "Blueberry", 30, "g");
        }
        
        // Similar pattern for other recipes...
    }
    
    /**
     * Initializes lunch recipe ingredients.
     * 
     * @param conn The database connection
     */
    private void initializeLunchRecipes(Connection conn) throws SQLException {
        String mealType = "lunch";
        
        // Grilled Chicken Salad
        int recipeId = insertRecipe(conn, mealType, "Grilled Chicken Salad");
        if (recipeId != -1) {
            insertRecipeIngredient(conn, recipeId, "Chicken Breast", 150, "g");
            insertRecipeIngredient(conn, recipeId, "Lettuce", 100, "g");
            insertRecipeIngredient(conn, recipeId, "Tomato", 1, "unit");
            insertRecipeIngredient(conn, recipeId, "Cucumber", 0.5, "unit");
            insertRecipeIngredient(conn, recipeId, "Olive Oil", 15, "ml");
            insertRecipeIngredient(conn, recipeId, "Lemon", 0.5, "unit");
            insertRecipeIngredient(conn, recipeId, "Salt", 2, "g");
            insertRecipeIngredient(conn, recipeId, "Pepper", 1, "g");
        }
        
        // Similar pattern for other recipes...
    }
    
    /**
     * Initializes snack recipe ingredients.
     * 
     * @param conn The database connection
     */
    private void initializeSnackRecipes(Connection conn) throws SQLException {
        String mealType = "snack";
        
        // Apple with Peanut Butter
        int recipeId = insertRecipe(conn, mealType, "Apple with Peanut Butter");
        if (recipeId != -1) {
            insertRecipeIngredient(conn, recipeId, "Apple", 1, "unit");
            insertRecipeIngredient(conn, recipeId, "Peanut Butter", 30, "g");
        }
        
        // Similar pattern for other recipes...
    }
    
    /**
     * Initializes dinner recipe ingredients.
     * 
     * @param conn The database connection
     */
    private void initializeDinnerRecipes(Connection conn) throws SQLException {
        String mealType = "dinner";
        
        // Grilled Salmon with Vegetables
        int recipeId = insertRecipe(conn, mealType, "Grilled Salmon with Vegetables");
        if (recipeId != -1) {
            insertRecipeIngredient(conn, recipeId, "Salmon", 200, "g");
            insertRecipeIngredient(conn, recipeId, "Broccoli", 100, "g");
            insertRecipeIngredient(conn, recipeId, "Carrot", 1, "unit");
            insertRecipeIngredient(conn, recipeId, "Olive Oil", 15, "ml");
            insertRecipeIngredient(conn, recipeId, "Lemon", 1, "unit");
            insertRecipeIngredient(conn, recipeId, "Garlic", 2, "clove");
            insertRecipeIngredient(conn, recipeId, "Salt", 2, "g");
            insertRecipeIngredient(conn, recipeId, "Pepper", 1, "g");
        }
        
        // Similar pattern for other recipes...
    }
    
    /**
     * Inserts a recipe into the database.
     * 
     * @param conn The database connection
     * @param mealType The type of meal
     * @param recipeName The name of the recipe
     * @return The ID of the inserted recipe or -1 if failed
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
    public class Ingredient {
        private String name;
        private double amount;
        private String unit;
        private double price;
        
        public Ingredient(String name, double amount, String unit, double price) {
            this.name = name != null ? name : "";
            this.amount = Math.max(0, amount);
            this.unit = unit != null ? unit : "";
            this.price = Math.max(0, price);
        }
        
        public String getName() {
            return name;
        }
        
        public double getAmount() {
            return amount;
        }
        
        public String getUnit() {
            return unit;
        }
        
        public double getPrice() {
            return price;
        }
        
        @Override
        public String toString() {
            return name + " (" + amount + " " + unit + ")";
        }
    }
}