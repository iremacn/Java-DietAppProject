package com.berkant.kagan.haluk.irem.dietapp;

/**
 * This class represents a food item in the Diet Planner application.
 * @details The Food class stores information about a food item including
 *          its name, amount in grams, and calorie content.
 * @author berkant
 */
public class Food {
    // Private fields for encapsulation
    private String name;
    private double grams;
    private int calories;
    
    /**
     * Default constructor for Food class.
     */
    public Food() {
    }
    
    /**
     * Parameterized constructor for Food class.
     * 
     * @param name     The name of the food
     * @param grams    The amount of food in grams
     * @param calories The calorie content of the food
     */
    public Food(String name, double grams, int calories) {
        this.name = name;
        this.grams = grams;
        this.calories = calories;
    }
    
    /**
     * Gets the name of the food.
     * 
     * @return The food name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the food.
     * 
     * @param name The food name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the amount of the food in grams.
     * 
     * @return The amount in grams
     */
    public double getGrams() {
        return grams;
    }
    
    /**
     * Sets the amount of the food in grams.
     * 
     * @param grams The amount in grams to set
     */
    public void setGrams(double grams) {
        this.grams = grams;
    }
    
    /**
     * Gets the calorie content of the food.
     * 
     * @return The calories
     */
    public int getCalories() {
        return calories;
    }
    
    /**
     * Sets the calorie content of the food.
     * 
     * @param calories The calories to set
     */
    public void setCalories(int calories) {
        this.calories = calories;
    }
    
    /**
     * Returns a string representation of the Food object.
     * 
     * @return A string containing food information
     */
    @Override
    public String toString() {
        return name + " (" + grams + "g, " + calories + " calories)";
    }
}