package com.berkant.kagan.haluk.irem.dietapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles shopping list generation operations for the Diet Planner application.
 * @details The ShoppingListService class provides methods for generating shopping lists
 *          based on meal plans and optimizing ingredient lists.
 * @author [your_name]
 */
public class ShoppingListService {
    // Service dependency
    private MealPlanningService mealPlanningService;
    
    // Maps to store ingredient mappings
    private Map<String, String> foodToIngredientMap;
    private Map<String, String> ingredientCategories;
    
    /**
     * Constructor for ShoppingListService class.
     * 
     * @param mealPlanningService The meal planning service for accessing meal plans
     */
    public ShoppingListService(MealPlanningService mealPlanningService) {
        this.mealPlanningService = mealPlanningService;
        initFoodToIngredientMap();
        initIngredientCategories();
    }
    
    /**
     * Initializes the mapping between foods and their ingredients.
     * This is a simplified example. In a real app, this could be more complex
     * or loaded from a database.
     */
    private void initFoodToIngredientMap() {
        foodToIngredientMap = new HashMap<>();
        
        // Example mappings - in a real system, this would be more comprehensive
        foodToIngredientMap.put("Scrambled Eggs", "Eggs:2:unit:Dairy;Milk:2:tbsp:Dairy;Salt:1:pinch:Spices");
        foodToIngredientMap.put("Oatmeal with Fruits", "Oats:50:g:Grains;Banana:1:unit:Fruits;Milk:100:ml:Dairy;Honey:1:tsp:Sweeteners");
        foodToIngredientMap.put("Grilled Chicken Salad", "Chicken Breast:150:g:Meat;Lettuce:100:g:Vegetables;Tomato:1:unit:Vegetables;Cucumber:0.5:unit:Vegetables;Olive Oil:1:tbsp:Oils");
        // Add more mappings as needed
    }
    
    /**
     * Initializes the categories for various ingredients.
     */
    private void initIngredientCategories() {
        ingredientCategories = new HashMap<>();
        
        // Example categories
        ingredientCategories.put("Eggs", "Dairy");
        ingredientCategories.put("Milk", "Dairy");
        ingredientCategories.put("Chicken Breast", "Meat");
        ingredientCategories.put("Beef", "Meat");
        ingredientCategories.put("Apple", "Fruits");
        ingredientCategories.put("Banana", "Fruits");
        ingredientCategories.put("Lettuce", "Vegetables");
        ingredientCategories.put("Tomato", "Vegetables");
        // Add more categories as needed
    }
    
    /**
     * Generates a shopping list for the specified date range.
     * 
     * @param username The username of the user
     * @param startDate The start date in format YYYY-MM-DD
     * @param endDate The end date in format YYYY-MM-DD
     * @return A list of ingredients needed for the meal plans
     */
    public List<Ingredient> generateShoppingList(String username, String startDate, String endDate) {
        // Get all dates between start and end date
        List<String> dateRange = generateDateRange(startDate, endDate);
        
        // Get all planned meals for the date range
        List<Food> allPlannedFoods = new ArrayList<>();
        String[] mealTypes = {"breakfast", "lunch", "snack", "dinner"};
        
        for (String date : dateRange) {
            for (String mealType : mealTypes) {
                List<Food> mealPlan = mealPlanningService.getMealPlan(username, date, mealType);
                allPlannedFoods.addAll(mealPlan);
            }
        }
        
        // Convert foods to ingredients
        List<Ingredient> ingredients = extractIngredientsFromFoods(allPlannedFoods);
        
        // Combine and optimize ingredients
        return combineIngredients(ingredients);
    }
    
    /**
     * Extracts ingredients from a list of foods.
     * 
     * @param foods The list of foods
     * @return A list of ingredients
     */
    private List<Ingredient> extractIngredientsFromFoods(List<Food> foods) {
        List<Ingredient> ingredients = new ArrayList<>();
        
        for (Food food : foods) {
            String foodName = food.getName();
            
            // Check if we have ingredient mapping for this food
            if (foodToIngredientMap.containsKey(foodName)) {
                String ingredientInfo = foodToIngredientMap.get(foodName);
                String[] ingredientArray = ingredientInfo.split(";");
                
                for (String ingredientData : ingredientArray) {
                    String[] parts = ingredientData.split(":");
                    if (parts.length == 4) {
                        String name = parts[0];
                        double amount = Double.parseDouble(parts[1]);
                        String unit = parts[2];
                        String category = parts[3];
                        
                        ingredients.add(new Ingredient(name, amount, unit, category));
                    }
                }
            } else {
                // For foods without ingredient mapping, add the food itself as an ingredient
                ingredients.add(new Ingredient(foodName, 1, "serving", 
                                              getIngredientCategory(foodName)));
            }
        }
        
        return ingredients;
    }
    
    /**
     * Gets the category for an ingredient.
     * 
     * @param ingredientName The name of the ingredient
     * @return The category of the ingredient or "Other" if not found
     */
    private String getIngredientCategory(String ingredientName) {
        return ingredientCategories.getOrDefault(ingredientName, "Other");
    }
    
    /**
     * Combines ingredients with the same name and unit.
     * 
     * @param ingredients The list of ingredients to combine
     * @return A combined list of ingredients
     */
    public List<Ingredient> combineIngredients(List<Ingredient> ingredients) {
        Map<String, Ingredient> combinedMap = new HashMap<>();
        
        for (Ingredient ingredient : ingredients) {
            // Create a key using name and unit
            String key = ingredient.getName() + ":" + ingredient.getUnit();
            
            if (combinedMap.containsKey(key)) {
                // If already in map, add to amount
                Ingredient existing = combinedMap.get(key);
                existing.setAmount(existing.getAmount() + ingredient.getAmount());
            } else {
                // Otherwise, add to map
                combinedMap.put(key, new Ingredient(
                    ingredient.getName(), 
                    ingredient.getAmount(), 
                    ingredient.getUnit(), 
                    ingredient.getCategory()
                ));
            }
        }
        
        return new ArrayList<>(combinedMap.values());
    }
    
    /**
     * Categorizes a list of ingredients by their category.
     * 
     * @param ingredients The list of ingredients to categorize
     * @return A map of categories to lists of ingredients
     */
    public Map<String, List<Ingredient>> categorizeIngredients(List<Ingredient> ingredients) {
        Map<String, List<Ingredient>> categorized = new HashMap<>();
        
        for (Ingredient ingredient : ingredients) {
            String category = ingredient.getCategory();
            
            if (!categorized.containsKey(category)) {
                categorized.put(category, new ArrayList<>());
            }
            
            categorized.get(category).add(ingredient);
        }
        
        return categorized;
    }
    
    /**
     * Generates a list of dates between start and end date (inclusive).
     * This is a simplified implementation that doesn't handle all edge cases.
     * 
     * @param startDate The start date in format YYYY-MM-DD
     * @param endDate The end date in format YYYY-MM-DD
     * @return A list of dates in the range
     */
    private List<String> generateDateRange(String startDate, String endDate) {
        List<String> dates = new ArrayList<>();
        
        // For simplicity, just add the start and end dates
        // In a real application, you would generate all dates in between
        dates.add(startDate);
        if (!startDate.equals(endDate)) {
            dates.add(endDate);
            // Here you would add all dates in between
        }
        
        return dates;
    }
    
    /**
     * Gets a list of predefined ingredient categories.
     * 
     * @return Array of ingredient categories
     */
    public String[] getIngredientCategories() {
        return new String[] {
            "Fruits", "Vegetables", "Meat", "Dairy", "Grains", 
            "Spices", "Oils", "Sweeteners", "Other"
        };
    }
}