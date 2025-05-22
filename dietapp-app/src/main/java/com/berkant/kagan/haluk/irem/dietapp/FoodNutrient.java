/**
 * @file FoodNutrient.java
 * @brief Extended food item representation with detailed nutrient information
 * 
 * @details The FoodNutrient class extends the base Food class to provide comprehensive
 *          nutritional information tracking. It adds detailed macronutrient and
 *          micronutrient tracking capabilities, including proteins, carbohydrates,
 *          fats, fiber, sugar, and sodium content.
 * 
 * @author irem
 * @version 1.0
 * @date 2024
 * @copyright Diet Planner Application
 */
package com.berkant.kagan.haluk.irem.dietapp;

/**
 * @class FoodNutrient
 * @brief Extended food item class with detailed nutrient tracking
 * 
 * @details This class extends the base Food class to provide comprehensive
 *          nutritional information tracking. It adds support for detailed
 *          macronutrient and micronutrient tracking, including:
 *          - Proteins (g)
 *          - Carbohydrates (g)
 *          - Fats (g)
 *          - Fiber (g)
 *          - Sugar (g)
 *          - Sodium (mg)
 * 
 *          The class includes built-in validation to ensure nutrient values
 *          are within reasonable ranges and maintains data integrity by
 *          preventing negative values.
 */
public class FoodNutrient extends Food {
    /** @brief The protein content in grams */
    private double protein;
    /** @brief The carbohydrate content in grams */
    private double carbs;
    /** @brief The fat content in grams */
    private double fat;
    /** @brief The fiber content in grams */
    private double fiber;
    /** @brief The sugar content in grams */
    private double sugar;
    /** @brief The sodium content in milligrams */
    private double sodium;
    
    /**
     * @brief Default constructor
     * @details Initializes a new FoodNutrient object with all nutrient values
     *          set to zero. Inherits base Food class initialization.
     */
    public FoodNutrient() {
        super();
        // Initialize all nutrient values to zero
        this.protein = 0.0;
        this.carbs = 0.0;
        this.fat = 0.0;
        this.fiber = 0.0;
        this.sugar = 0.0;
        this.sodium = 0.0;
    }
    
    /**
     * @brief Constructor with basic food information
     * @details Creates a new FoodNutrient object with specified basic food
     *          information and initializes all nutrient values to zero.
     * 
     * @param name The name of the food item
     * @param grams The weight of the food in grams
     * @param calories The calorie content of the food
     */
    public FoodNutrient(String name, double grams, int calories) {
        super(name, grams, calories);
        // Initialize all nutrient values to zero
        this.protein = 0.0;
        this.carbs = 0.0;
        this.fat = 0.0;
        this.fiber = 0.0;
        this.sugar = 0.0;
        this.sodium = 0.0;
    }
    
    /**
     * @brief Constructor with complete nutrient information
     * @details Creates a new FoodNutrient object with all nutritional information
     *          specified. Ensures all nutrient values are non-negative by
     *          converting negative values to zero.
     * 
     * @param name The name of the food item
     * @param grams The weight of the food in grams
     * @param calories The calorie content of the food
     * @param protein The protein content in grams
     * @param carbs The carbohydrate content in grams
     * @param fat The fat content in grams
     * @param fiber The fiber content in grams
     * @param sugar The sugar content in grams
     * @param sodium The sodium content in milligrams
     */
    public FoodNutrient(String name, double grams, int calories, double protein, double carbs, 
                       double fat, double fiber, double sugar, double sodium) {
        super(name, grams, calories);
        // Ensure all nutrient values are non-negative
        this.protein = Math.max(0.0, protein);
        this.carbs = Math.max(0.0, carbs);
        this.fat = Math.max(0.0, fat);
        this.fiber = Math.max(0.0, fiber);
        this.sugar = Math.max(0.0, sugar);
        this.sodium = Math.max(0.0, sodium);
    }
    
    /**
     * @brief Retrieves the protein content
     * @details Returns the protein content of the food item in grams.
     * 
     * @return The protein content in grams
     */
    public double getProtein() {
        return protein;
    }
    
    /**
     * @brief Sets the protein content
     * @details Updates the protein content of the food item in grams.
     *          Converts negative values to zero to maintain data integrity.
     * 
     * @param protein The new protein content in grams
     */
    public void setProtein(double protein) {
        this.protein = Math.max(0.0, protein);
    }
    
    /**
     * @brief Retrieves the carbohydrate content
     * @details Returns the carbohydrate content of the food item in grams.
     * 
     * @return The carbohydrate content in grams
     */
    public double getCarbs() {
        return carbs;
    }
    
    /**
     * @brief Sets the carbohydrate content
     * @details Updates the carbohydrate content of the food item in grams.
     *          Converts negative values to zero to maintain data integrity.
     * 
     * @param carbs The new carbohydrate content in grams
     */
    public void setCarbs(double carbs) {
        this.carbs = Math.max(0.0, carbs);
    }
    
    /**
     * @brief Retrieves the fat content
     * @details Returns the fat content of the food item in grams.
     * 
     * @return The fat content in grams
     */
    public double getFat() {
        return fat;
    }
    
    /**
     * @brief Sets the fat content
     * @details Updates the fat content of the food item in grams.
     *          Converts negative values to zero to maintain data integrity.
     * 
     * @param fat The new fat content in grams
     */
    public void setFat(double fat) {
        this.fat = Math.max(0.0, fat);
    }
    
    /**
     * @brief Retrieves the fiber content
     * @details Returns the fiber content of the food item in grams.
     * 
     * @return The fiber content in grams
     */
    public double getFiber() {
        return fiber;
    }
    
    /**
     * @brief Sets the fiber content
     * @details Updates the fiber content of the food item in grams.
     *          Converts negative values to zero to maintain data integrity.
     * 
     * @param fiber The new fiber content in grams
     */
    public void setFiber(double fiber) {
        this.fiber = Math.max(0.0, fiber);
    }
    
    /**
     * @brief Retrieves the sugar content
     * @details Returns the sugar content of the food item in grams.
     * 
     * @return The sugar content in grams
     */
    public double getSugar() {
        return sugar;
    }
    
    /**
     * @brief Sets the sugar content
     * @details Updates the sugar content of the food item in grams.
     *          Converts negative values to zero to maintain data integrity.
     * 
     * @param sugar The new sugar content in grams
     */
    public void setSugar(double sugar) {
        this.sugar = Math.max(0.0, sugar);
    }
    
    /**
     * @brief Retrieves the sodium content
     * @details Returns the sodium content of the food item in milligrams.
     * 
     * @return The sodium content in milligrams
     */
    public double getSodium() {
        return sodium;
    }
    
    /**
     * @brief Sets the sodium content
     * @details Updates the sodium content of the food item in milligrams.
     *          Converts negative values to zero to maintain data integrity.
     * 
     * @param sodium The new sodium content in milligrams
     */
    public void setSodium(double sodium) {
        this.sodium = Math.max(0.0, sodium);
    }
    
    /**
     * @brief Validates the food item's nutrient values
     * @details Performs comprehensive validation of all nutrient values:
     *          - Validates base food properties using super.isValid()
     *          - Ensures total macronutrients don't exceed food weight
     *          - Verifies individual macronutrients don't exceed food weight
     *          - Confirms sugar and fiber don't exceed total carbohydrates
     * 
     * @return true if all nutrient values are valid, false otherwise
     */
    @Override
    public boolean isValid() {
        // Check if base food properties are valid
        if (!super.isValid()) {
            return false;
        }
        
        // Macronutrients should not exceed reasonable limits
        double totalMacros = protein + carbs + fat;
        
        // Total macronutrients (in grams) should not exceed the food's weight
        if (totalMacros > getGrams()) {
            return false;
        }
        
        // Check if individual nutrients are reasonable
        // Protein should not exceed 100% of food weight
        if (protein > getGrams()) {
            return false;
        }
        
        // Carbs should not exceed 100% of food weight
        if (carbs > getGrams()) {
            return false;
        }
        
        // Fat should not exceed 100% of food weight
        if (fat > getGrams()) {
            return false;
        }
        
        // Sugar is a type of carbohydrate, so it shouldn't exceed carbs
        if (sugar > carbs) {
            return false;
        }
        
        // Fiber is a type of carbohydrate, so it shouldn't exceed carbs
        if (fiber > carbs) {
            return false;
        }
        
        return true;
    }
    
    /**
     * @brief Generates a detailed string representation
     * @details Creates a formatted string containing the food item's
     *          basic information and all nutrient values.
     * 
     * @return A string containing comprehensive food and nutrient information
     */
    @Override
    public String toString() {
        return super.toString() + " | P:" + String.format("%.1fg", protein) + 
               ", C:" + String.format("%.1fg", carbs) + 
               ", F:" + String.format("%.1fg", fat);
    }
    
    /**
     * @brief Generates an extended string representation
     * @details Creates a more detailed formatted string containing the food item's
     *          basic information and all nutrient values, with additional
     *          nutritional context.
     * 
     * @return A detailed string containing comprehensive food and nutrient information
     */
    public String toDetailedString() {
        return getName() + " (" + getGrams() + "g, " + getCalories() + " calories)" +
               "\n  - Protein: " + String.format("%.1fg", protein) +
               "\n  - Carbs: " + String.format("%.1fg", carbs) +
               "\n  - Fat: " + String.format("%.1fg", fat) +
               "\n  - Fiber: " + String.format("%.1fg", fiber) +
               "\n  - Sugar: " + String.format("%.1fg", sugar) +
               "\n  - Sodium: " + String.format("%.1fmg", sodium);
    }
}