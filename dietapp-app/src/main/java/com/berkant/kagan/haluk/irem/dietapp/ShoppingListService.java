package com.berkant.kagan.haluk.irem.dietapp;

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
    
    // Maps to store ingredient data
    private Map<String, Map<String, List<Ingredient>>> recipeIngredients;
    private Map<String, Double> ingredientPrices;
    
    /**
     * Constructor for ShoppingListService class.
     * 
     * @param mealPlanningService The meal planning service
     */
    public ShoppingListService(MealPlanningService mealPlanningService) {
        this.mealPlanningService = mealPlanningService;
        this.recipeIngredients = new HashMap<>();
        this.ingredientPrices = new HashMap<>();
        initializeIngredientPrices();
        initializeRecipeIngredients();
    }
    
    /**
     * Initializes the ingredient prices database.
     */
    private void initializeIngredientPrices() {
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
        ingredientPrices.put("Eggs", 3.25); // dozen
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
        
        // Nuts and Seeds
        ingredientPrices.put("Almonds", 5.95);
        ingredientPrices.put("Walnuts", 6.50);
        ingredientPrices.put("Peanuts", 3.95);
        ingredientPrices.put("Chia Seeds", 4.75);
        ingredientPrices.put("Flax Seeds", 3.95);
        ingredientPrices.put("Peanut Butter", 4.25);
        ingredientPrices.put("Almond Butter", 6.95);
        
        // Condiments and Oils
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
    }
    
    /**
     * Initializes recipe ingredients for all food items in the meal planning service.
     */
    private void initializeRecipeIngredients() {
        // Create maps for each meal type
        Map<String, List<Ingredient>> breakfastIngredients = new HashMap<>();
        Map<String, List<Ingredient>> lunchIngredients = new HashMap<>();
        Map<String, List<Ingredient>> snackIngredients = new HashMap<>();
        Map<String, List<Ingredient>> dinnerIngredients = new HashMap<>();
        
        // Add to the main map
        recipeIngredients.put("breakfast", breakfastIngredients);
        recipeIngredients.put("lunch", lunchIngredients);
        recipeIngredients.put("snack", snackIngredients);
        recipeIngredients.put("dinner", dinnerIngredients);
        
        // Initialize breakfast ingredients
        initializeBreakfastRecipes(breakfastIngredients);
        
        // Initialize lunch ingredients
        initializeLunchRecipes(lunchIngredients);
        
        // Initialize snack ingredients
        initializeSnackRecipes(snackIngredients);
        
        // Initialize dinner ingredients
        initializeDinnerRecipes(dinnerIngredients);
    }
    
    /**
     * Initializes breakfast recipe ingredients.
     * 
     * @param breakfastIngredients Map to store breakfast recipe ingredients
     */
    private void initializeBreakfastRecipes(Map<String, List<Ingredient>> breakfastIngredients) {
        // Scrambled Eggs
        List<Ingredient> scrambledEggsIngredients = new ArrayList<>();
        scrambledEggsIngredients.add(new Ingredient("Eggs", 3, "unit"));
        scrambledEggsIngredients.add(new Ingredient("Milk", 30, "ml"));
        scrambledEggsIngredients.add(new Ingredient("Salt", 2, "g"));
        scrambledEggsIngredients.add(new Ingredient("Pepper", 1, "g"));
        scrambledEggsIngredients.add(new Ingredient("Butter", 10, "g"));
        breakfastIngredients.put("Scrambled Eggs", scrambledEggsIngredients);
        
        // Oatmeal with Fruits
        List<Ingredient> oatmealIngredients = new ArrayList<>();
        oatmealIngredients.add(new Ingredient("Oats", 80, "g"));
        oatmealIngredients.add(new Ingredient("Milk", 200, "ml"));
        oatmealIngredients.add(new Ingredient("Banana", 1, "unit"));
        oatmealIngredients.add(new Ingredient("Strawberry", 50, "g"));
        oatmealIngredients.add(new Ingredient("Honey", 15, "ml"));
        breakfastIngredients.put("Oatmeal with Fruits", oatmealIngredients);
        
        // Greek Yogurt with Honey
        List<Ingredient> greekYogurtIngredients = new ArrayList<>();
        greekYogurtIngredients.add(new Ingredient("Greek Yogurt", 200, "g"));
        greekYogurtIngredients.add(new Ingredient("Honey", 20, "ml"));
        greekYogurtIngredients.add(new Ingredient("Blueberry", 30, "g"));
        breakfastIngredients.put("Greek Yogurt with Honey", greekYogurtIngredients);
        
        // Whole Grain Toast with Avocado
        List<Ingredient> avocadoToastIngredients = new ArrayList<>();
        avocadoToastIngredients.add(new Ingredient("Whole Wheat Bread", 2, "slice"));
        avocadoToastIngredients.add(new Ingredient("Avocado", 1, "unit"));
        avocadoToastIngredients.add(new Ingredient("Lemon", 0.5, "unit"));
        avocadoToastIngredients.add(new Ingredient("Salt", 1, "g"));
        avocadoToastIngredients.add(new Ingredient("Pepper", 1, "g"));
        breakfastIngredients.put("Whole Grain Toast with Avocado", avocadoToastIngredients);
        
        // Smoothie Bowl
        List<Ingredient> smoothieBowlIngredients = new ArrayList<>();
        smoothieBowlIngredients.add(new Ingredient("Banana", 1, "unit"));
        smoothieBowlIngredients.add(new Ingredient("Strawberry", 100, "g"));
        smoothieBowlIngredients.add(new Ingredient("Blueberry", 50, "g"));
        smoothieBowlIngredients.add(new Ingredient("Greek Yogurt", 100, "g"));
        smoothieBowlIngredients.add(new Ingredient("Almond Milk", 100, "ml"));
        smoothieBowlIngredients.add(new Ingredient("Honey", 10, "ml"));
        breakfastIngredients.put("Smoothie Bowl", smoothieBowlIngredients);
        
        // Pancakes with Maple Syrup
        List<Ingredient> pancakesIngredients = new ArrayList<>();
        pancakesIngredients.add(new Ingredient("Flour", 150, "g"));
        pancakesIngredients.add(new Ingredient("Eggs", 2, "unit"));
        pancakesIngredients.add(new Ingredient("Milk", 200, "ml"));
        pancakesIngredients.add(new Ingredient("Butter", 30, "g"));
        pancakesIngredients.add(new Ingredient("Maple Syrup", 50, "ml"));
        breakfastIngredients.put("Pancakes with Maple Syrup", pancakesIngredients);
        
        // Breakfast Burrito
        List<Ingredient> breakfastBurritoIngredients = new ArrayList<>();
        breakfastBurritoIngredients.add(new Ingredient("Eggs", 2, "unit"));
        breakfastBurritoIngredients.add(new Ingredient("Tortilla", 1, "unit"));
        breakfastBurritoIngredients.add(new Ingredient("Bell Pepper", 0.5, "unit"));
        breakfastBurritoIngredients.add(new Ingredient("Onion", 0.5, "unit"));
        breakfastBurritoIngredients.add(new Ingredient("Cheese", 30, "g"));
        breakfastBurritoIngredients.add(new Ingredient("Salt", 1, "g"));
        breakfastBurritoIngredients.add(new Ingredient("Pepper", 1, "g"));
        breakfastIngredients.put("Breakfast Burrito", breakfastBurritoIngredients);
        
        // Fruit and Nut Granola
        List<Ingredient> granolaIngredients = new ArrayList<>();
        granolaIngredients.add(new Ingredient("Oats", 100, "g"));
        granolaIngredients.add(new Ingredient("Almonds", 30, "g"));
        granolaIngredients.add(new Ingredient("Walnuts", 20, "g"));
        granolaIngredients.add(new Ingredient("Honey", 30, "ml"));
        granolaIngredients.add(new Ingredient("Dried Cranberries", 20, "g"));
        granolaIngredients.add(new Ingredient("Coconut Oil", 15, "ml"));
        breakfastIngredients.put("Fruit and Nut Granola", granolaIngredients);
    }
    
    /**
     * Initializes lunch recipe ingredients.
     * 
     * @param lunchIngredients Map to store lunch recipe ingredients
     */
    private void initializeLunchRecipes(Map<String, List<Ingredient>> lunchIngredients) {
        // Grilled Chicken Salad
        List<Ingredient> chickenSaladIngredients = new ArrayList<>();
        chickenSaladIngredients.add(new Ingredient("Chicken Breast", 150, "g"));
        chickenSaladIngredients.add(new Ingredient("Lettuce", 100, "g"));
        chickenSaladIngredients.add(new Ingredient("Tomato", 1, "unit"));
        chickenSaladIngredients.add(new Ingredient("Cucumber", 0.5, "unit"));
        chickenSaladIngredients.add(new Ingredient("Olive Oil", 15, "ml"));
        chickenSaladIngredients.add(new Ingredient("Lemon", 0.5, "unit"));
        chickenSaladIngredients.add(new Ingredient("Salt", 2, "g"));
        chickenSaladIngredients.add(new Ingredient("Pepper", 1, "g"));
        lunchIngredients.put("Grilled Chicken Salad", chickenSaladIngredients);
        
        // Quinoa Bowl with Vegetables
        List<Ingredient> quinoaBowlIngredients = new ArrayList<>();
        quinoaBowlIngredients.add(new Ingredient("Quinoa", 80, "g"));
        quinoaBowlIngredients.add(new Ingredient("Bell Pepper", 0.5, "unit"));
        quinoaBowlIngredients.add(new Ingredient("Cucumber", 0.5, "unit"));
        quinoaBowlIngredients.add(new Ingredient("Tomato", 1, "unit"));
        quinoaBowlIngredients.add(new Ingredient("Avocado", 0.5, "unit"));
        quinoaBowlIngredients.add(new Ingredient("Olive Oil", 10, "ml"));
        quinoaBowlIngredients.add(new Ingredient("Lemon", 0.5, "unit"));
        lunchIngredients.put("Quinoa Bowl with Vegetables", quinoaBowlIngredients);
        
        // Turkey and Avocado Sandwich
        List<Ingredient> turkeySandwichIngredients = new ArrayList<>();
        turkeySandwichIngredients.add(new Ingredient("Whole Wheat Bread", 2, "slice"));
        turkeySandwichIngredients.add(new Ingredient("Turkey", 100, "g"));
        turkeySandwichIngredients.add(new Ingredient("Avocado", 0.5, "unit"));
        turkeySandwichIngredients.add(new Ingredient("Lettuce", 30, "g"));
        turkeySandwichIngredients.add(new Ingredient("Tomato", 0.5, "unit"));
        turkeySandwichIngredients.add(new Ingredient("Mayonnaise", 15, "g"));
        lunchIngredients.put("Turkey and Avocado Sandwich", turkeySandwichIngredients);
        
        // Vegetable Soup with Bread
        List<Ingredient> vegetableSoupIngredients = new ArrayList<>();
        vegetableSoupIngredients.add(new Ingredient("Carrot", 2, "unit"));
        vegetableSoupIngredients.add(new Ingredient("Potato", 1, "unit"));
        vegetableSoupIngredients.add(new Ingredient("Onion", 1, "unit"));
        vegetableSoupIngredients.add(new Ingredient("Garlic", 2, "clove"));
        vegetableSoupIngredients.add(new Ingredient("Tomato", 2, "unit"));
        vegetableSoupIngredients.add(new Ingredient("Broccoli", 100, "g"));
        vegetableSoupIngredients.add(new Ingredient("Olive Oil", 15, "ml"));
        vegetableSoupIngredients.add(new Ingredient("Salt", 3, "g"));
        vegetableSoupIngredients.add(new Ingredient("Bread", 2, "slice"));
        lunchIngredients.put("Vegetable Soup with Bread", vegetableSoupIngredients);
        
        // Tuna Salad Wrap
        List<Ingredient> tunaWrapIngredients = new ArrayList<>();
        tunaWrapIngredients.add(new Ingredient("Tuna", 100, "g"));
        tunaWrapIngredients.add(new Ingredient("Tortilla", 1, "unit"));
        tunaWrapIngredients.add(new Ingredient("Lettuce", 30, "g"));
        tunaWrapIngredients.add(new Ingredient("Tomato", 0.5, "unit"));
        tunaWrapIngredients.add(new Ingredient("Onion", 0.25, "unit"));
        tunaWrapIngredients.add(new Ingredient("Mayonnaise", 20, "g"));
        tunaWrapIngredients.add(new Ingredient("Lemon", 0.5, "unit"));
        lunchIngredients.put("Tuna Salad Wrap", tunaWrapIngredients);
        
        // Falafel with Hummus
        List<Ingredient> falafelIngredients = new ArrayList<>();
        falafelIngredients.add(new Ingredient("Chickpeas", 150, "g"));
        falafelIngredients.add(new Ingredient("Parsley", 20, "g"));
        falafelIngredients.add(new Ingredient("Garlic", 2, "clove"));
        falafelIngredients.add(new Ingredient("Onion", 0.5, "unit"));
        falafelIngredients.add(new Ingredient("Flour", 30, "g"));
        falafelIngredients.add(new Ingredient("Olive Oil", 30, "ml"));
        falafelIngredients.add(new Ingredient("Hummus", 100, "g"));
        falafelIngredients.add(new Ingredient("Pita Bread", 1, "unit"));
        lunchIngredients.put("Falafel with Hummus", falafelIngredients);
        
        // Caesar Salad with Grilled Chicken
        List<Ingredient> caesarSaladIngredients = new ArrayList<>();
        caesarSaladIngredients.add(new Ingredient("Chicken Breast", 150, "g"));
        caesarSaladIngredients.add(new Ingredient("Lettuce", 200, "g"));
        caesarSaladIngredients.add(new Ingredient("Parmesan Cheese", 30, "g"));
        caesarSaladIngredients.add(new Ingredient("Bread", 1, "slice"));
        caesarSaladIngredients.add(new Ingredient("Olive Oil", 15, "ml"));
        caesarSaladIngredients.add(new Ingredient("Garlic", 1, "clove"));
        caesarSaladIngredients.add(new Ingredient("Lemon", 0.5, "unit"));
        lunchIngredients.put("Caesar Salad with Grilled Chicken", caesarSaladIngredients);
        
        // Mediterranean Pasta Salad
        List<Ingredient> pastaSaladIngredients = new ArrayList<>();
        pastaSaladIngredients.add(new Ingredient("Pasta", 100, "g"));
        pastaSaladIngredients.add(new Ingredient("Cucumber", 0.5, "unit"));
        pastaSaladIngredients.add(new Ingredient("Tomato", 1, "unit"));
        pastaSaladIngredients.add(new Ingredient("Bell Pepper", 0.5, "unit"));
        pastaSaladIngredients.add(new Ingredient("Olive Oil", 20, "ml"));
        pastaSaladIngredients.add(new Ingredient("Feta Cheese", 50, "g"));
        pastaSaladIngredients.add(new Ingredient("Olives", 30, "g"));
        pastaSaladIngredients.add(new Ingredient("Lemon", 0.5, "unit"));
        lunchIngredients.put("Mediterranean Pasta Salad", pastaSaladIngredients);
    }
    
    /**
     * Initializes snack recipe ingredients.
     * 
     * @param snackIngredients Map to store snack recipe ingredients
     */
    private void initializeSnackRecipes(Map<String, List<Ingredient>> snackIngredients) {
        // Apple with Peanut Butter
        List<Ingredient> applePBIngredients = new ArrayList<>();
        applePBIngredients.add(new Ingredient("Apple", 1, "unit"));
        applePBIngredients.add(new Ingredient("Peanut Butter", 30, "g"));
        snackIngredients.put("Apple with Peanut Butter", applePBIngredients);
        
        // Greek Yogurt with Berries
        List<Ingredient> yogurtBerriesIngredients = new ArrayList<>();
        yogurtBerriesIngredients.add(new Ingredient("Greek Yogurt", 150, "g"));
        yogurtBerriesIngredients.add(new Ingredient("Strawberry", 50, "g"));
        yogurtBerriesIngredients.add(new Ingredient("Blueberry", 50, "g"));
        yogurtBerriesIngredients.add(new Ingredient("Honey", 10, "ml"));
        snackIngredients.put("Greek Yogurt with Berries", yogurtBerriesIngredients);
        
        // Mixed Nuts
        List<Ingredient> mixedNutsIngredients = new ArrayList<>();
        mixedNutsIngredients.add(new Ingredient("Almonds", 15, "g"));
        mixedNutsIngredients.add(new Ingredient("Walnuts", 15, "g"));
        mixedNutsIngredients.add(new Ingredient("Peanuts", 20, "g"));
        snackIngredients.put("Mixed Nuts", mixedNutsIngredients);
        
        // Hummus with Carrot Sticks
        List<Ingredient> hummusCarrotsIngredients = new ArrayList<>();
        hummusCarrotsIngredients.add(new Ingredient("Hummus", 100, "g"));
        hummusCarrotsIngredients.add(new Ingredient("Carrot", 2, "unit"));
        snackIngredients.put("Hummus with Carrot Sticks", hummusCarrotsIngredients);
        
        // Protein Bar
        List<Ingredient> proteinBarIngredients = new ArrayList<>();
        proteinBarIngredients.add(new Ingredient("Protein Bar", 1, "unit"));
        snackIngredients.put("Protein Bar", proteinBarIngredients);
        
        // Fruit Smoothie
        List<Ingredient> fruitSmoothieIngredients = new ArrayList<>();
        fruitSmoothieIngredients.add(new Ingredient("Banana", 1, "unit"));
        fruitSmoothieIngredients.add(new Ingredient("Strawberry", 100, "g"));
        fruitSmoothieIngredients.add(new Ingredient("Yogurt", 100, "g"));
        fruitSmoothieIngredients.add(new Ingredient("Milk", 100, "ml"));
        fruitSmoothieIngredients.add(new Ingredient("Honey", 10, "ml"));
        snackIngredients.put("Fruit Smoothie", fruitSmoothieIngredients);
        
        // Dark Chocolate Square
        List<Ingredient> darkChocolateIngredients = new ArrayList<>();
        darkChocolateIngredients.add(new Ingredient("Dark Chocolate", 30, "g"));
        snackIngredients.put("Dark Chocolate Square", darkChocolateIngredients);
        
        // Cheese and Crackers
        List<Ingredient> cheeseAndCrackersIngredients = new ArrayList<>();
        cheeseAndCrackersIngredients.add(new Ingredient("Cheese", 50, "g"));
        cheeseAndCrackersIngredients.add(new Ingredient("Crackers", 30, "g"));
        snackIngredients.put("Cheese and Crackers", cheeseAndCrackersIngredients);
    }
    
    /**
     * Initializes dinner recipe ingredients.
     * 
     * @param dinnerIngredients Map to store dinner recipe ingredients
     */
    private void initializeDinnerRecipes(Map<String, List<Ingredient>> dinnerIngredients) {
        // Grilled Salmon with Vegetables
        List<Ingredient> salmonIngredients = new ArrayList<>();
        salmonIngredients.add(new Ingredient("Salmon", 200, "g"));
        salmonIngredients.add(new Ingredient("Broccoli", 100, "g"));
        salmonIngredients.add(new Ingredient("Carrot", 1, "unit"));
        salmonIngredients.add(new Ingredient("Olive Oil", 15, "ml"));
        salmonIngredients.add(new Ingredient("Lemon", 1, "unit"));
        salmonIngredients.add(new Ingredient("Garlic", 2, "clove"));
        salmonIngredients.add(new Ingredient("Salt", 2, "g"));
        salmonIngredients.add(new Ingredient("Pepper", 1, "g"));
        dinnerIngredients.put("Grilled Salmon with Vegetables", salmonIngredients);
        
        // Beef Stir Fry with Rice
        List<Ingredient> beefStirFryIngredients = new ArrayList<>();
        beefStirFryIngredients.add(new Ingredient("Ground Beef", 200, "g"));
        beefStirFryIngredients.add(new Ingredient("White Rice", 100, "g"));
        beefStirFryIngredients.add(new Ingredient("Bell Pepper", 1, "unit"));
        beefStirFryIngredients.add(new Ingredient("Onion", 1, "unit"));
        beefStirFryIngredients.add(new Ingredient("Garlic", 2, "clove"));
        beefStirFryIngredients.add(new Ingredient("Soy Sauce", 30, "ml"));
        beefStirFryIngredients.add(new Ingredient("Olive Oil", 15, "ml"));
        dinnerIngredients.put("Beef Stir Fry with Rice", beefStirFryIngredients);
        
        // Vegetable Curry with Tofu
        List<Ingredient> vegCurryIngredients = new ArrayList<>();
        vegCurryIngredients.add(new Ingredient("Tofu", 200, "g"));
        vegCurryIngredients.add(new Ingredient("Carrot", 1, "unit"));
        vegCurryIngredients.add(new Ingredient("Bell Pepper", 1, "unit"));
        vegCurryIngredients.add(new Ingredient("Onion", 1, "unit"));
        vegCurryIngredients.add(new Ingredient("Garlic", 2, "clove"));
        vegCurryIngredients.add(new Ingredient("Coconut Milk", 200, "ml"));
        vegCurryIngredients.add(new Ingredient("Curry Powder", 10, "g"));
        vegCurryIngredients.add(new Ingredient("Brown Rice", 100, "g"));
        dinnerIngredients.put("Vegetable Curry with Tofu", vegCurryIngredients);
        
        // Spaghetti with Tomato Sauce
        List<Ingredient> spaghettiIngredients = new ArrayList<>();
        spaghettiIngredients.add(new Ingredient("Pasta", 150, "g"));
        spaghettiIngredients.add(new Ingredient("Tomato Sauce", 200, "g"));
        spaghettiIngredients.add(new Ingredient("Onion", 1, "unit"));
        spaghettiIngredients.add(new Ingredient("Garlic", 2, "clove"));
        spaghettiIngredients.add(new Ingredient("Olive Oil", 15, "ml"));
        spaghettiIngredients.add(new Ingredient("Parmesan Cheese", 20, "g"));
        spaghettiIngredients.add(new Ingredient("Salt", 2, "g"));
        spaghettiIngredients.add(new Ingredient("Pepper", 1, "g"));
        dinnerIngredients.put("Spaghetti with Tomato Sauce", spaghettiIngredients);
        
        // Baked Chicken with Sweet Potato
        List<Ingredient> bakedChickenIngredients = new ArrayList<>();
        bakedChickenIngredients.add(new Ingredient("Chicken Breast", 200, "g"));
        bakedChickenIngredients.add(new Ingredient("Sweet Potato", 200, "g"));
        bakedChickenIngredients.add(new Ingredient("Olive Oil", 15, "ml"));
        bakedChickenIngredients.add(new Ingredient("Garlic", 2, "clove"));
        bakedChickenIngredients.add(new Ingredient("Rosemary", 5, "g"));
        bakedChickenIngredients.add(new Ingredient("Salt", 2, "g"));
        bakedChickenIngredients.add(new Ingredient("Pepper", 1, "g"));
        dinnerIngredients.put("Baked Chicken with Sweet Potato", bakedChickenIngredients);
        
        // Lentil Soup with Bread
        List<Ingredient> lentilSoupIngredients = new ArrayList<>();
        lentilSoupIngredients.add(new Ingredient("Lentils", 150, "g"));
        lentilSoupIngredients.add(new Ingredient("Carrot", 2, "unit"));
        lentilSoupIngredients.add(new Ingredient("Onion", 1, "unit"));
        lentilSoupIngredients.add(new Ingredient("Garlic", 2, "clove"));
        lentilSoupIngredients.add(new Ingredient("Tomato", 2, "unit"));
        lentilSoupIngredients.add(new Ingredient("Olive Oil", 15, "ml"));
        lentilSoupIngredients.add(new Ingredient("Salt", 2, "g"));
        lentilSoupIngredients.add(new Ingredient("Bread", 2, "slice"));
        dinnerIngredients.put("Lentil Soup with Bread", lentilSoupIngredients);
        
        // Grilled Steak with Mashed Potatoes
        List<Ingredient> steakIngredients = new ArrayList<>();
        steakIngredients.add(new Ingredient("Beef Steak", 250, "g"));
        steakIngredients.add(new Ingredient("Potato", 300, "g"));
        steakIngredients.add(new Ingredient("Butter", 30, "g"));
        steakIngredients.add(new Ingredient("Milk", 50, "ml"));
        steakIngredients.add(new Ingredient("Garlic", 2, "clove"));
        steakIngredients.add(new Ingredient("Salt", 3, "g"));
        steakIngredients.add(new Ingredient("Pepper", 2, "g"));
        steakIngredients.add(new Ingredient("Olive Oil", 15, "ml"));
        dinnerIngredients.put("Grilled Steak with Mashed Potatoes", steakIngredients);
        
        // Fish Tacos with Slaw
        List<Ingredient> fishTacosIngredients = new ArrayList<>();
        fishTacosIngredients.add(new Ingredient("White Fish", 200, "g"));
        fishTacosIngredients.add(new Ingredient("Tortilla", 3, "unit"));
        fishTacosIngredients.add(new Ingredient("Cabbage", 100, "g"));
        fishTacosIngredients.add(new Ingredient("Carrot", 1, "unit"));
        fishTacosIngredients.add(new Ingredient("Lime", 1, "unit"));
        fishTacosIngredients.add(new Ingredient("Yogurt", 50, "g"));
        fishTacosIngredients.add(new Ingredient("Garlic", 1, "clove"));
        fishTacosIngredients.add(new Ingredient("Olive Oil", 15, "ml"));
        dinnerIngredients.put("Fish Tacos with Slaw", fishTacosIngredients);
    }
    
    /**
     * Gets the ingredients for a specific food item.
     * 
     * @param mealType The type of meal (breakfast, lunch, snack, dinner)
     * @param foodName The name of the food
     * @return List of ingredients for the food item or empty list if not found
     */
    public List<Ingredient> getIngredientsForFood(String mealType, String foodName) {
        Map<String, List<Ingredient>> mealIngredients = recipeIngredients.getOrDefault(mealType.toLowerCase(), new HashMap<>());
        return mealIngredients.getOrDefault(foodName, new ArrayList<>());
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
            Double price = ingredientPrices.getOrDefault(ingredient.getName(), 0.0);
            
            // Scale price based on amount (simplified approach)
            // For simplicity, we assume the price is for 100g or 1 unit
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
     * Inner class to represent an ingredient with amount and unit.
     */
    public class Ingredient {
        private String name;
        private double amount;
        private String unit;
        
        public Ingredient(String name, double amount, String unit) {
            this.name = name;
            this.amount = amount;
            this.unit = unit;
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
        
        @Override
        public String toString() {
            return name + " (" + amount + " " + unit + ")";
        }
    }
}