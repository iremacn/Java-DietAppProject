package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * @class FoodTest
 * @brief Test class for the Food class.
 */
public class FoodTest {

    private Food food;

    @Before
    public void setUp() {
        // Create a default food object for testing
        food = new Food("Apple", 150.0, 80);
    }

    @Test
    public void testDefaultConstructor() {
        // Test the default constructor
        Food emptyFood = new Food();
        
        // Verify default values
        assertEquals("Default name should be empty string", "", emptyFood.getName());
        assertEquals("Default grams should be 0.0", 0.0, emptyFood.getGrams(), 0.001);
        assertEquals("Default calories should be 0", 0, emptyFood.getCalories());
    }

    @Test
    public void testParameterizedConstructor() {
        // Test the parameterized constructor with valid values
        Food testFood = new Food("Banana", 118.0, 105);
        
        // Verify values are set correctly
        assertEquals("Name should be 'Banana'", "Banana", testFood.getName());
        assertEquals("Grams should be 118.0", 118.0, testFood.getGrams(), 0.001);
        assertEquals("Calories should be 105", 105, testFood.getCalories());
    }

    @Test
    public void testConstructorWithNullName() {
        // Test constructor with null name
        Food testFood = new Food(null, 100.0, 50);
        
        // Verify name is set to empty string instead of null
        assertEquals("Name should be empty string for null input", "", testFood.getName());
        assertEquals("Grams should still be set correctly", 100.0, testFood.getGrams(), 0.001);
        assertEquals("Calories should still be set correctly", 50, testFood.getCalories());
    }

    @Test
    public void testConstructorWithNegativeValues() {
        // Test constructor with negative grams and calories
        Food testFood = new Food("Test", -10.0, -20);
        
        // Verify negative values are converted to zero
        assertEquals("Name should be set correctly", "Test", testFood.getName());
        assertEquals("Negative grams should be converted to 0.0", 0.0, testFood.getGrams(), 0.001);
        assertEquals("Negative calories should be converted to 0", 0, testFood.getCalories());
    }

    @Test
    public void testSetName() {
        // Test setting a valid name
        food.setName("Orange");
        assertEquals("Name should be updated to 'Orange'", "Orange", food.getName());
        
        // Test setting null name
        food.setName(null);
        assertEquals("Name should be empty string for null input", "", food.getName());
        
        // Test setting empty name
        food.setName("");
        assertEquals("Name should allow empty string", "", food.getName());
    }

    @Test
    public void testSetGrams() {
        // Test setting a valid value for grams
        food.setGrams(200.0);
        assertEquals("Grams should be updated to 200.0", 200.0, food.getGrams(), 0.001);
        
        // Test setting zero value for grams
        food.setGrams(0.0);
        assertEquals("Grams should allow zero", 0.0, food.getGrams(), 0.001);
        
        // Test setting negative value for grams
        food.setGrams(-50.0);
        assertEquals("Negative grams should be converted to 0.0", 0.0, food.getGrams(), 0.001);
    }

    @Test
    public void testSetCalories() {
        // Test setting a valid value for calories
        food.setCalories(120);
        assertEquals("Calories should be updated to 120", 120, food.getCalories());
        
        // Test setting zero value for calories
        food.setCalories(0);
        assertEquals("Calories should allow zero", 0, food.getCalories());
        
        // Test setting negative value for calories
        food.setCalories(-30);
        assertEquals("Negative calories should be converted to 0", 0, food.getCalories());
    }

    @Test
    public void testIsValid() {
        // Test with all valid properties
        Food validFood = new Food("Banana", 118.0, 105);
        assertTrue("Food with valid properties should be valid", validFood.isValid());
        
        // Test with empty name
        Food emptyNameFood = new Food("", 100.0, 50);
        assertFalse("Food with empty name should be invalid", emptyNameFood.isValid());
        
        // Test with null name
        Food nullNameFood = new Food(null, 100.0, 50);
        assertFalse("Food with null name should be invalid", nullNameFood.isValid());
        
        // Test with zero grams
        Food zeroGramsFood = new Food("Apple", 0.0, 80);
        assertFalse("Food with zero grams should be invalid", zeroGramsFood.isValid());
        
        // Test with negative grams (should be converted to 0 and be invalid)
        Food negativeGramsFood = new Food("Orange", -10.0, 47);
        assertFalse("Food with negative grams (converted to 0) should be invalid", negativeGramsFood.isValid());
        
        // Test with zero calories (should be valid)
        Food zeroCaloriesFood = new Food("Water", 200.0, 0);
        assertTrue("Food with zero calories should be valid", zeroCaloriesFood.isValid());
        
        // Test with negative calories (should be converted to 0 and be valid)
        Food negativeCaloriesFood = new Food("Cucumber", 100.0, -5);
        assertTrue("Food with negative calories (converted to 0) should be valid", negativeCaloriesFood.isValid());
    }

    @Test
    public void testToString() {
        // Test the toString method
        Food testFood = new Food("Banana", 118.0, 105);
        String expected = "Banana (118.0g, 105 calories)";
        assertEquals("toString should return the correct format", expected, testFood.toString());
        
        // Test with zero values
        Food zeroFood = new Food("Water", 200.0, 0);
        String expectedZero = "Water (200.0g, 0 calories)";
        assertEquals("toString should handle zero calories correctly", expectedZero, zeroFood.toString());
    }


    
    
    
    
    
}