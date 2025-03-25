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
     * Initializes ingredients and recipes in the database if they don't exist.
     */
    private void initializeIngredientsAndRecipes() throws SQLException {
        // Check if ingredients already exist
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ingredients")) {
            
            rs.next();
            int count = rs.getInt(1);
            
            // If no ingredients exist, initialize the data
            if (count == 0) {
                initializeIngredientPrices();
                initializeRecipeIngredients();
            }
        } catch (SQLException e) {
            // Table might not exist yet, this is handled in DatabaseHelper
            System.out.println("Error during ingredient database check: " + e.getMessage());
        }
    }
    
    /**
     * Initializes the ingredient prices database.
     */
    private void initializeIngredientPrices() throws SQLException {
        try (Connection conn = DatabaseHelper.getConnection()) {
            // Prepare statement for ingredient insertion
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO ingredients (name, price) VALUES (?, ?)")) {
                
                // Define ingredient prices
                Map<String, Double> ingredientPrices = new HashMap<>();
                
                // Vegetables
                ingredientPrices.put("Tomato", 1.20);
                ingredientPrices.put("Cucumber", 0.90);
                ingredientPrices.put("Lettuce", 1.50);
                ingredientPrices.put("Carrot", 0.75);
                ingredientPrices.put("Onion", 0.60);
                ingredientPrices.put("Garlic", 0.85);
                ingredientPrices.put("Potato", 0.95);
                ingredientPrices.put("Sweet Potato", 1.25);
                ingredientPrices.put("Broccoli", 2.10);
                ingredientPrices.put("Cauliflower", 2.30);
                ingredientPrices.put("Spinach", 1.85);
                ingredientPrices.put("Kale", 2.15);
                ingredientPrices.put("Bell Pepper", 1.35);
                ingredientPrices.put("Zucchini", 1.15);
                ingredientPrices.put("Eggplant", 1.40);
                
                // Fruits
                ingredientPrices.put("Apple", 0.85);
                ingredientPrices.put("Banana", 0.60);
                ingredientPrices.put("Orange", 0.95);
                ingredientPrices.put("Strawberry", 3.50);
                ingredientPrices.put("Blueberry", 4.20);
                ingredientPrices.put("Raspberry", 4.10);
                ingredientPrices.put("Avocado", 2.15);
                ingredientPrices.put("Lemon", 0.75);
                ingredientPrices.put("Lime", 0.80);
                
                // Proteins
                ingredientPrices.put("Chicken Breast", 4.50);
                ingredientPrices.put("Ground Beef", 5.75);
                ingredientPrices.put("Salmon", 7.95);
                ingredientPrices.put("Tuna", 6.50);
                ingredientPrices.put("Eggs", 3.25);
                ingredientPrices.put("Tofu", 2.75);
                ingredientPrices.put("Tempeh", 3.25);
                ingredientPrices.put("Turkey", 5.25);
                ingredientPrices.put("Shrimp", 8.50);
                
                // Dairy and Alternatives
                ingredientPrices.put("Milk", 1.95);
                ingredientPrices.put("Almond Milk", 2.75);
                ingredientPrices.put("Soy Milk", 2.50);
                ingredientPrices.put("Yogurt", 3.50);
                ingredientPrices.put("Greek Yogurt", 4.25);
                ingredientPrices.put("Cheese", 4.50);
                ingredientPrices.put("Feta Cheese", 5.25);
                ingredientPrices.put("Cottage Cheese", 3.95);
                
                // Grains
                ingredientPrices.put("White Rice", 2.25);
                ingredientPrices.put("Brown Rice", 2.75);
                ingredientPrices.put("Quinoa", 4.50);
                ingredientPrices.put("Oats", 2.95);
                ingredientPrices.put("Bread", 2.50);
                ingredientPrices.put("Whole Wheat Bread", 3.25);
                ingredientPrices.put("Pasta", 1.80);
                ingredientPrices.put("Whole Wheat Pasta", 2.50);
                
                // Additional items
                ingredientPrices.put("Almonds", 5.95);
                ingredientPrices.put("Walnuts", 6.50);
                ingredientPrices.put("Peanuts", 3.95);
                ingredientPrices.put("Chia Seeds", 4.75);
                ingredientPrices.put("Flax Seeds", 3.95);
                ingredientPrices.put("Peanut Butter", 4.25);
                ingredientPrices.put("Almond Butter", 6.95);
                ingredientPrices.put("Olive Oil", 7.95);
                ingredientPrices.put("Coconut Oil", 8.50);
                ingredientPrices.put("Soy Sauce", 3.25);
                ingredientPrices.put("Honey", 4.95);
                ingredientPrices.put("Maple Syrup", 6.75);
                ingredientPrices.put("Salt", 1.25);
                ingredientPrices.put("Pepper", 1.95);
                ingredientPrices.put("Tomato Sauce", 2.25);
                ingredientPrices.put("Hot Sauce", 3.50);
                ingredientPrices.put("Vinegar", 2.75);
                ingredientPrices.put("Hummus", 3.95);
                ingredientPrices.put("Butter", 4.25);
                ingredientPrices.put("Tortilla", 2.50);
                ingredientPrices.put("Flour", 1.50);
                ingredientPrices.put("Protein Bar", 2.50);
                ingredientPrices.put("Dark Chocolate", 3.75);
                ingredientPrices.put("Crackers", 2.20);
                ingredientPrices.put("Chickpeas", 1.85);
                ingredientPrices.put("Parsley", 1.30);
                ingredientPrices.put("Pita Bread", 2.75);
                ingredientPrices.put("Parmesan Cheese", 6.50);
                ingredientPrices.put("Olives", 3.75);
                ingredientPrices.put("Coconut Milk", 3.25);
                ingredientPrices.put("Curry Powder", 3.50);
                ingredientPrices.put("Rosemary", 2.25);
                ingredientPrices.put("Lentils", 2.45);
                ingredientPrices.put("Beef Steak", 12.50);
                ingredientPrices.put("White Fish", 9.25);
                ingredientPrices.put("Cabbage", 1.50);
                ingredientPrices.put("Mayonnaise", 3.25);
                ingredientPrices.put("Dried Cranberries", 4.85);
                
                // Insert each ingredient into the database
                for (Map.Entry<String, Double> entry : ingredientPrices.entrySet()) {
                    pstmt.setString(1, entry.getKey());
                    pstmt.setDouble(2, entry.getValue());
                    pstmt.executeUpdate();
                }
                
                System.out.println("Ingredient prices saved to database.");
            }
        }
    }
    
    /**
     * Initializes recipe ingredients for all food items in the meal planning service.
     */
    private void initializeRecipeIngredients() throws SQLException {
        initializeBreakfastRecipes();
        initializeLunchRecipes();
        initializeSnackRecipes();
        initializeDinnerRecipes();
        
        System.out.println("Recipes saved to database.");
    }
    
    /**
     * Initializes breakfast recipe ingredients.
     */
    private void initializeBreakfastRecipes() throws SQLException {
        String mealType = "breakfast";
        
        // Scrambled Eggs
        int recipeId = insertRecipe(mealType, "Scrambled Eggs");
        if (recipeId != -1) {
            insertRecipeIngredient(recipeId, "Eggs", 3, "unit");
            insertRecipeIngredient(recipeId, "Milk", 30, "ml");
            insertRecipeIngredient(recipeId, "Salt", 2, "g");
            insertRecipeIngredient(recipeId, "Pepper", 1, "g");
            insertRecipeIngredient(recipeId, "Butter", 10, "g");
        }
        
        // Oatmeal with Fruits
        recipeId = insertRecipe(mealType, "Oatmeal with Fruits");
        if (recipeId != -1) {
            insertRecipeIngredient(recipeId, "Oats", 80, "g");
            insertRecipeIngredient(recipeId, "Milk", 200, "ml");
            insertRecipeIngredient(recipeId, "Banana", 1, "unit");
            insertRecipeIngredient(recipeId, "Strawberry", 50, "g");
            insertRecipeIngredient(recipeId, "Honey", 15, "ml");
        }
        
        // Greek Yogurt with Honey
        recipeId = insertRecipe(mealType, "Greek Yogurt with Honey");
        if (recipeId != -1) {
            insertRecipeIngredient(recipeId, "Greek Yogurt", 200, "g");
            insertRecipeIngredient(recipeId, "Honey", 20, "ml");
            insertRecipeIngredient(recipeId, "Blueberry", 30, "g");
        }
        
        // Whole Grain Toast with Avocado
        recipeId = insertRecipe(mealType, "Whole Grain Toast with Avocado");
        if (recipeId != -1) {
            insertRecipeIngredient(recipeId, "Whole Wheat Bread", 2, "slice");
            insertRecipeIngredient(recipeId, "Avocado", 1, "unit");
            insertRecipeIngredient(recipeId, "Lemon", 0.5, "unit");
            insertRecipeIngredient(recipeId, "Salt", 1, "g");
            insertRecipeIngredient(recipeId, "Pepper", 1, "g");
        }
        
        // Smoothie Bowl
        recipeId = insertRecipe(mealType, "Smoothie Bowl");
        if (recipeId != -1) {
            insertRecipeIngredient(recipeId, "Banana", 1, "unit");
            insertRecipeIngredient(recipeId, "Strawberry", 100, "g");
            insertRecipeIngredient(recipeId, "Blueberry", 50, "g");
            insertRecipeIngredient(recipeId, "Greek Yogurt", 100, "g");
            insertRecipeIngredient(recipeId, "Almond Milk", 100, "ml");
            insertRecipeIngredient(recipeId, "Honey", 10, "ml");
        }
        
        // Pancakes with Maple Syrup
        recipeId = insertRecipe(mealType, "Pancakes with Maple Syrup");
        if (recipeId != -1) {
            insertRecipeIngredient(recipeId, "Flour", 150, "g");
            insertRecipeIngredient(recipeId, "Eggs", 2, "unit");
            insertRecipeIngredient(recipeId, "Milk", 200, "ml");
            insertRecipeIngredient(recipeId, "Butter", 30, "g");
            insertRecipeIngredient(recipeId, "Maple Syrup", 50, "ml");
        }
        
        // Breakfast Burrito
        recipeId = insertRecipe(mealType, "Breakfast Burrito");
        if (recipeId != -1) {
            insertRecipeIngredient(recipeId, "Eggs", 2, "unit");
            insertRecipeIngredient(recipeId, "Tortilla", 1, "unit");
            insertRecipeIngredient(recipeId, "Bell Pepper", 0.5, "unit");
            insertRecipeIngredient(recipeId, "Onion", 0.5, "unit");
            insertRecipeIngredient(recipeId, "Cheese", 30, "g");
            insertRecipeIngredient(recipeId, "Salt", 1, "g");
            insertRecipeIngredient(recipeId, "Pepper", 1, "g");
        }
        
        // Fruit and Nut Granola
        recipeId = insertRecipe(mealType, "Fruit and Nut Granola");
        if (recipeId != -1) {
            insertRecipeIngredient(recipeId, "Oats", 100, "g");
            insertRecipeIngredient(recipeId, "Almonds", 30, "g");
            insertRecipeIngredient(recipeId, "Walnuts", 20, "g");
            insertRecipeIngredient(recipeId, "Honey", 30, "ml");
            insertRecipeIngredient(recipeId, "Dried Cranberries", 20, "g");
            insertRecipeIngredient(recipeId, "Coconut Oil", 15, "ml");
        }
    }
    
    /**
     * Initializes lunch recipe ingredients.
     */
    private void initializeLunchRecipes() throws SQLException {
        String mealType = "lunch";
        
        // Grilled Chicken Salad
        int recipeId = insertRecipe(mealType, "Grilled Chicken Salad");
        if (recipeId != -1) {
            insertRecipeIngredient(recipeId, "Chicken Breast", 150, "g");
            insertRecipeIngredient(recipeId, "Lettuce", 100, "g");
            insertRecipeIngredient(recipeId, "Tomato", 1, "unit");
            insertRecipeIngredient(recipeId, "Cucumber", 0.5, "unit");
            insertRecipeIngredient(recipeId, "Olive Oil", 15, "ml");
            insertRecipeIngredient(recipeId, "Lemon", 0.5, "unit");
            insertRecipeIngredient(recipeId, "Salt", 2, "g");
            insertRecipeIngredient(recipeId, "Pepper", 1, "g");
        }
        
        // Quinoa Bowl with Vegetables
        recipeId = insertRecipe(mealType, "Quinoa Bowl with Vegetables");
        if (recipeId != -1) {
            insertRecipeIngredient(recipeId, "Quinoa", 80, "g");
            insertRecipeIngredient(recipeId, "Bell Pepper", 0.5, "unit");
            insertRecipeIngredient(recipeId, "Cucumber", 0.5, "unit");
            insertRecipeIngredient(recipeId, "Tomato", 1, "unit");
            insertRecipeIngredient(recipeId, "Avocado", 0.5, "unit");
            insertRecipeIngredient(recipeId, "Olive Oil", 10, "ml");
            insertRecipeIngredient(recipeId, "Lemon", 0.5, "unit");
        }
        
        // Add other lunch recipes in a similar way...
    }
    
    /**
     * Initializes snack recipe ingredients.
     */
    private void initializeSnackRecipes() throws SQLException {
        String mealType = "snack";
        
        // Apple with Peanut Butter
        int recipeId = insertRecipe(mealType, "Apple with Peanut Butter");
        if (recipeId != -1) {
            insertRecipeIngredient(recipeId, "Apple", 1, "unit");
            insertRecipeIngredient(recipeId, "Peanut Butter", 30, "g");
        }
        
        // Greek Yogurt with Berries
        recipeId = insertRecipe(mealType, "Greek Yogurt with Berries");
        if (recipeId != -1) {
            insertRecipeIngredient(recipeId, "Greek Yogurt", 150, "g");
            insertRecipeIngredient(recipeId, "Strawberry", 50, "g");
            insertRecipeIngredient(recipeId, "Blueberry", 50, "g");
            insertRecipeIngredient(recipeId, "Honey", 10, "ml");
        }
        
        // Add other snack recipes in a similar way...
    }
    
    /**
     * Initializes dinner recipe ingredients.
     */
    private void initializeDinnerRecipes() throws SQLException {
        String mealType = "dinner";
        
        // Grilled Salmon with Vegetables
        int recipeId = insertRecipe(mealType, "Grilled Salmon with Vegetables");
        if (recipeId != -1) {
            insertRecipeIngredient(recipeId, "Salmon", 200, "g");
            insertRecipeIngredient(recipeId, "Broccoli", 100, "g");
            insertRecipeIngredient(recipeId, "Carrot", 1, "unit");
            insertRecipeIngredient(recipeId, "Olive Oil", 15, "ml");
            insertRecipeIngredient(recipeId, "Lemon", 1, "unit");
            insertRecipeIngredient(recipeId, "Garlic", 2, "clove");
            insertRecipeIngredient(recipeId, "Salt", 2, "g");
            insertRecipeIngredient(recipeId, "Pepper", 1, "g");
        }
        
        // Add other dinner recipes in a similar way...
    }
    
    /**
     * Inserts a recipe into the database.
     * 
     * @param mealType The type of meal
     * @param recipeName The name of the recipe
     * @return The ID of the inserted recipe or -1 if failed
     */
    private int insertRecipe(String mealType, String recipeName) throws SQLException {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
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
     * @param recipeId The ID of the recipe
     * @param ingredientName The name of the ingredient
     * @param amount The amount of the ingredient
     * @param unit The unit of measurement
     * @return true if successful, false otherwise
     */
    private boolean insertRecipeIngredient(int recipeId, String ingredientName, double amount, String unit) throws SQLException {
        // First, get the ingredient ID
        int ingredientId = getIngredientId(ingredientName);
        if (ingredientId == -1) {
            return false;
        }
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
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
     * @param ingredientName The name of the ingredient
     * @return The ID of the ingredient or -1 if not found
     */
    private int getIngredientId(String ingredientName) throws SQLException {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
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
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
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
            
        } catch (SQLException e) {
            System.out.println("Could not get ingredient list: " + e.getMessage());
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
        double totalCost = 0.0;
        
        for (Ingredient ingredient : ingredients) {
            double price = ingredient.getPrice();
            
            // Scale price based on amount (simplified approach)
            if (ingredient.getUnit().equals("g")) {
                totalCost += (price * ingredient.getAmount() / 100);
            } else if (ingredient.getUnit().equals("ml")) {
                totalCost += (price * ingredient.getAmount() / 100);
            } else {
                // For units, multiply directly
                totalCost += (price * ingredient.getAmount());
            }
        }
        
        return totalCost;
    }
    
    /**
     * Inner class to represent an ingredient with amount, unit, and price.
     */
    public class Ingredient {
        private String name;
        private double amount;
        private String unit;
        private double price;
        
        public Ingredient(String name, double amount, String unit, double price) {
            this.name = name;
            this.amount = amount;
            this.unit = unit;
            this.price = price;
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