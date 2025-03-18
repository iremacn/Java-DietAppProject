package com.berkant.kagan.haluk.irem.dietapp;

/**
 * This class extends the Food class to include detailed nutrient information.
 * @details The FoodNutrient class adds nutrient tracking capabilities to the Food class
 *          by adding macronutrient information (proteins, carbs, fats).
 * @author irem
 */
public class FoodNutrient extends Food {
    // Private fields for nutrients
    private double protein;  // in grams
    private double carbs;    // in grams
    private double fat;      // in grams
    private double fiber;    // in grams
    private double sugar;    // in grams
    private double sodium;   // in milligrams
    
    /**
     * Default constructor for FoodNutrient class.
     */
    public FoodNutrient() {
        super();
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
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.fiber = fiber;
        this.sugar = sugar;
        this.sodium = sodium;
    }
    
    // Getter and setter methods for nutrients
    
    public double getProtein() {
        return protein;
    }
    
    public void setProtein(double protein) {
        this.protein = protein;
    }
    
    public double getCarbs() {
        return carbs;
    }
    
    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }
    
    public double getFat() {
        return fat;
    }
    
    public void setFat(double fat) {
        this.fat = fat;
    }
    
    public double getFiber() {
        return fiber;
    }
    
    public void setFiber(double fiber) {
        this.fiber = fiber;
    }
    
    public double getSugar() {
        return sugar;
    }
    
    public void setSugar(double sugar) {
        this.sugar = sugar;
    }
    
    public double getSodium() {
        return sodium;
    }
    
    public void setSodium(double sodium) {
        this.sodium = sodium;
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