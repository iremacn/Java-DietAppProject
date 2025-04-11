package com.berkant.kagan.haluk.irem.dietapp;

/**
 * This class extends the Food class to include detailed nutrient information.
 * @details The FoodNutrient class adds nutrient tracking capabilities to the Food class
 *          by adding macronutrient information (proteins, carbs, fats).
 * @author irem
 */
public class FoodNutrient extends Food {
    /** The protein content in grams */
    private double protein;
    /** The carbohydrate content in grams */
    private double carbs;
    /** The fat content in grams */
    private double fat;
    /** The fiber content in grams */
    private double fiber;
    /** The sugar content in grams */
    private double sugar;
    /** The sodium content in milligrams */
    private double sodium;
    
    /**
     * Default constructor for FoodNutrient class.
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
     * Constructor with basic food information.
     * 
     * @param name The name of the food
     * @param grams The amount of food in grams
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
     * Constructor with complete nutrient information.
     * 
     * @param name The name of the food
     * @param grams The amount of food in grams
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
    
    // Getter and setter methods for nutrients
    
    /**
     * Gets the protein content in grams.
     * 
     * @return The protein content
     */
    public double getProtein() {
        return protein;
    }
    
    /**
     * Sets the protein content in grams.
     * Negative values will be converted to zero.
     * 
     * @param protein The protein content to set
     */
    public void setProtein(double protein) {
        this.protein = Math.max(0.0, protein);
    }
    
    /**
     * Gets the carbohydrate content in grams.
     * 
     * @return The carbohydrate content
     */
    public double getCarbs() {
        return carbs;
    }
    
    /**
     * Sets the carbohydrate content in grams.
     * Negative values will be converted to zero.
     * 
     * @param carbs The carbohydrate content to set
     */
    public void setCarbs(double carbs) {
        this.carbs = Math.max(0.0, carbs);
    }
    
    /**
     * Gets the fat content in grams.
     * 
     * @return The fat content
     */
    public double getFat() {
        return fat;
    }
    
    /**
     * Sets the fat content in grams.
     * Negative values will be converted to zero.
     * 
     * @param fat The fat content to set
     */
    public void setFat(double fat) {
        this.fat = Math.max(0.0, fat);
    }
    
    /**
     * Gets the fiber content in grams.
     * 
     * @return The fiber content
     */
    public double getFiber() {
        return fiber;
    }
    
    /**
     * Sets the fiber content in grams.
     * Negative values will be converted to zero.
     * 
     * @param fiber The fiber content to set
     */
    public void setFiber(double fiber) {
        this.fiber = Math.max(0.0, fiber);
    }
    
    /**
     * Gets the sugar content in grams.
     * 
     * @return The sugar content
     */
    public double getSugar() {
        return sugar;
    }
    
    /**
     * Sets the sugar content in grams.
     * Negative values will be converted to zero.
     * 
     * @param sugar The sugar content to set
     */
    public void setSugar(double sugar) {
        this.sugar = Math.max(0.0, sugar);
    }
    
    /**
     * Gets the sodium content in milligrams.
     * 
     * @return The sodium content
     */
    public double getSodium() {
        return sodium;
    }
    
    /**
     * Sets the sodium content in milligrams.
     * Negative values will be converted to zero.
     * 
     * @param sodium The sodium content to set
     */
    public void setSodium(double sodium) {
        this.sodium = Math.max(0.0, sodium);
    }
    
    /**
     * Validates if all nutrient values are within acceptable ranges.
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
     * Returns a string representation of the FoodNutrient object including nutrient details.
     * 
     * @return A string containing food and nutrient information
     */
    @Override
    public String toString() {
        return super.toString() + " | P:" + String.format("%.1fg", protein) + 
               ", C:" + String.format("%.1fg", carbs) + 
               ", F:" + String.format("%.1fg", fat);
    }
    
    /**
     * Returns a detailed string representation of all nutrient information.
     * 
     * @return A detailed string with all nutrient information
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