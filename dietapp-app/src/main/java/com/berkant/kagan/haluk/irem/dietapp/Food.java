/**
 * @file Food.java
 * @brief Food item representation for the Diet Planner application
 * 
 * @details The Food class provides a data model for representing food items in the
 *          Diet Planner application. It encapsulates essential nutritional information
 *          including name, weight, and calorie content, with built-in validation
 *          and data integrity checks.
 * 
 * @author berkant
 * @version 1.0
 * @date 2024
 * @copyright Diet Planner Application
 */
package com.berkant.kagan.haluk.irem.dietapp;

/**
 * @class Food
 * @brief Data model for food items in the Diet Planner
 * 
 * @details This class represents a food item with its basic nutritional information.
 *          It provides methods for managing food properties with built-in validation
 *          to ensure data integrity. The class handles null values and negative
 *          numbers appropriately, converting them to valid defaults.
 */
public class Food {
    /** @brief The name of the food item */
    private String name;
    /** @brief The amount of food in grams */
    private double grams;
    /** @brief The calorie content of the food per serving */
    private int calories;

    /**
     * @brief Default constructor
     * @details Initializes a new Food object with empty name and zero values
     *          for grams and calories.
     */
    public Food() {
        this.name = "";
        this.grams = 0.0;
        this.calories = 0;
    }

    /**
     * @brief Parameterized constructor
     * @details Creates a new Food object with specified properties.
     *          Handles null values and negative numbers by converting them
     *          to appropriate defaults.
     * 
     * @param name The name of the food item
     * @param grams The weight of the food in grams
     * @param calories The calorie content of the food
     */
    public Food(String name, double grams, int calories) {
        this.name = name != null ? name : "";
        this.grams = Math.max(0.0, grams);
        this.calories = Math.max(0, calories);
    }

    /**
     * @brief Retrieves the food item's name
     * @details Returns the name of the food item. If the name was previously
     *          set to null, returns an empty string.
     * 
     * @return The name of the food item
     */
    public String getName() {
        return name;
    }

    /**
     * @brief Sets the food item's name
     * @details Updates the name of the food item. If the provided name is null,
     *          it will be converted to an empty string.
     * 
     * @param name The new name for the food item
     */
    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    /**
     * @brief Retrieves the food item's weight
     * @details Returns the weight of the food item in grams.
     * 
     * @return The weight in grams
     */
    public double getGrams() {
        return grams;
    }

    /**
     * @brief Sets the food item's weight
     * @details Updates the weight of the food item in grams. If a negative
     *          value is provided, it will be converted to zero.
     * 
     * @param grams The new weight in grams
     */
    public void setGrams(double grams) {
        this.grams = Math.max(0.0, grams);
    }

    /**
     * @brief Retrieves the food item's calorie content
     * @details Returns the calorie content of the food item.
     * 
     * @return The calorie content
     */
    public int getCalories() {
        return calories;
    }

    /**
     * @brief Sets the food item's calorie content
     * @details Updates the calorie content of the food item. If a negative
     *          value is provided, it will be converted to zero.
     * 
     * @param calories The new calorie content
     */
    public void setCalories(int calories) {
        this.calories = Math.max(0, calories);
    }

    /**
     * @brief Validates the food item's properties
     * @details Performs comprehensive validation of all food properties:
     *          - Name must not be null or empty
     *          - Weight must be positive
     *          - Calories must not be negative
     * 
     * @return true if all properties are valid, false otherwise
     */
    public boolean isValid() {
        // Check if name is not empty
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Check if grams is positive
        if (grams <= 0) {
            return false;
        }
        
        // Calories can be zero (e.g., water) but not negative
        if (calories < 0) {
            return false;
        }
        
        return true;
    }

    /**
     * @brief Generates a string representation of the food item
     * @details Creates a formatted string containing the food item's
     *          name, weight, and calorie content.
     * 
     * @return A string in the format "name (weightg, calories calories)"
     */
    @Override
    public String toString() {
        return name + " (" + grams + "g, " + calories + " calories)";
    }
}