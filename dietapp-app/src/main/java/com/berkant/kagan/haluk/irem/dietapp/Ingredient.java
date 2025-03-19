package com.berkant.kagan.haluk.irem.dietapp;

/**
 * This class represents an ingredient for the Shopping List Generator.
 * @details The Ingredient class stores information about an ingredient including
 * its name, amount, unit, and category.
 * @author [your_name]
 */
public class Ingredient {
    // Private fields for encapsulation
    private String name;
    private double amount;
    private String unit;
    private String category;
    
    /**
     * Default constructor for Ingredient class.
     */
    public Ingredient() {
    }
    
    /**
     * Parameterized constructor for Ingredient class.
     *
     * @param name The name of the ingredient
     * @param amount The amount of the ingredient
     * @param unit The unit of measurement for the ingredient
     * @param category The food category of the ingredient
     */
    public Ingredient(String name, double amount, String unit, String category) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.category = category;
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
     * Sets the name of the ingredient.
     *
     * @param name The ingredient name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the amount of the ingredient.
     *
     * @return The ingredient amount
     */
    public double getAmount() {
        return amount;
    }
    
    /**
     * Sets the amount of the ingredient.
     *
     * @param amount The ingredient amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    /**
     * Gets the unit of the ingredient.
     *
     * @return The ingredient unit
     */
    public String getUnit() {
        return unit;
    }
    
    /**
     * Sets the unit of the ingredient.
     *
     * @param unit The ingredient unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    /**
     * Gets the category of the ingredient.
     *
     * @return The ingredient category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Sets the category of the ingredient.
     *
     * @param category The ingredient category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Returns a string representation of the Ingredient object.
     *
     * @return A string containing ingredient information
     */
    @Override
    public String toString() {
        return name + " - " + amount + " " + unit;
    }
}