package com.berkant.kagan.haluk.irem.dietapp;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Unit tests for the FoodNutrient class.
 * @author irem
 */
public class FoodNutrientTest {
    
    private FoodNutrient foodNutrient;
    
    @Before
    public void setUp() {
        // Create a basic food nutrient object for testing
        foodNutrient = new FoodNutrient("Apple", 100.0, 52);
    }
    
    @Test
    public void testDefaultConstructor() {
        FoodNutrient defaultFood = new FoodNutrient();
        assertEquals("", defaultFood.getName());
        assertEquals(0.0, defaultFood.getGrams(), 0.001);
        assertEquals(0, defaultFood.getCalories());
        assertEquals(0.0, defaultFood.getProtein(), 0.001);
        assertEquals(0.0, defaultFood.getCarbs(), 0.001);
        assertEquals(0.0, defaultFood.getFat(), 0.001);
        assertEquals(0.0, defaultFood.getFiber(), 0.001);
        assertEquals(0.0, defaultFood.getSugar(), 0.001);
        assertEquals(0.0, defaultFood.getSodium(), 0.001);
    }
    
    @Test
    public void testBasicConstructor() {
        assertEquals("Apple", foodNutrient.getName());
        assertEquals(100.0, foodNutrient.getGrams(), 0.001);
        assertEquals(52, foodNutrient.getCalories());
        assertEquals(0.0, foodNutrient.getProtein(), 0.001);
        assertEquals(0.0, foodNutrient.getCarbs(), 0.001);
        assertEquals(0.0, foodNutrient.getFat(), 0.001);
        assertEquals(0.0, foodNutrient.getFiber(), 0.001);
        assertEquals(0.0, foodNutrient.getSugar(), 0.001);
        assertEquals(0.0, foodNutrient.getSodium(), 0.001);
    }
    
    @Test
    public void testCompleteConstructor() {
        FoodNutrient completeFood = new FoodNutrient(
            "Banana", 100.0, 89, 1.1, 22.8, 0.3, 2.6, 12.2, 1.0);
            
        assertEquals("Banana", completeFood.getName());
        assertEquals(100.0, completeFood.getGrams(), 0.001);
        assertEquals(89, completeFood.getCalories());
        assertEquals(1.1, completeFood.getProtein(), 0.001);
        assertEquals(22.8, completeFood.getCarbs(), 0.001);
        assertEquals(0.3, completeFood.getFat(), 0.001);
        assertEquals(2.6, completeFood.getFiber(), 0.001);
        assertEquals(12.2, completeFood.getSugar(), 0.001);
        assertEquals(1.0, completeFood.getSodium(), 0.001);
    }
    
    @Test
    public void testConstructorWithNegativeValues() {
        // Constructor should convert negative values to zero
        FoodNutrient negativeFood = new FoodNutrient(
            "Test", 100.0, 50, -1.0, -2.0, -3.0, -4.0, -5.0, -6.0);
            
        assertEquals(0.0, negativeFood.getProtein(), 0.001);
        assertEquals(0.0, negativeFood.getCarbs(), 0.001);
        assertEquals(0.0, negativeFood.getFat(), 0.001);
        assertEquals(0.0, negativeFood.getFiber(), 0.001);
        assertEquals(0.0, negativeFood.getSugar(), 0.001);
        assertEquals(0.0, negativeFood.getSodium(), 0.001);
    }
    
    @Test
    public void testSetProtein() {
        foodNutrient.setProtein(5.5);
        assertEquals(5.5, foodNutrient.getProtein(), 0.001);
        
        // Test with negative value
        foodNutrient.setProtein(-1.0);
        assertEquals(0.0, foodNutrient.getProtein(), 0.001);
    }
    
    @Test
    public void testSetCarbs() {
        foodNutrient.setCarbs(25.5);
        assertEquals(25.5, foodNutrient.getCarbs(), 0.001);
        
        // Test with negative value
        foodNutrient.setCarbs(-1.0);
        assertEquals(0.0, foodNutrient.getCarbs(), 0.001);
    }
    
    @Test
    public void testSetFat() {
        foodNutrient.setFat(3.5);
        assertEquals(3.5, foodNutrient.getFat(), 0.001);
        
        // Test with negative value
        foodNutrient.setFat(-1.0);
        assertEquals(0.0, foodNutrient.getFat(), 0.001);
    }
    
    @Test
    public void testSetFiber() {
        foodNutrient.setFiber(2.5);
        assertEquals(2.5, foodNutrient.getFiber(), 0.001);
        
        // Test with negative value
        foodNutrient.setFiber(-1.0);
        assertEquals(0.0, foodNutrient.getFiber(), 0.001);
    }
    
    @Test
    public void testSetSugar() {
        foodNutrient.setSugar(10.5);
        assertEquals(10.5, foodNutrient.getSugar(), 0.001);
        
        // Test with negative value
        foodNutrient.setSugar(-1.0);
        assertEquals(0.0, foodNutrient.getSugar(), 0.001);
    }
    
    @Test
    public void testSetSodium() {
        foodNutrient.setSodium(15.5);
        assertEquals(15.5, foodNutrient.getSodium(), 0.001);
        
        // Test with negative value
        foodNutrient.setSodium(-1.0);
        assertEquals(0.0, foodNutrient.getSodium(), 0.001);
    }
    
    @Test
    public void testIsValidWithValidValues() {
        FoodNutrient validFood = new FoodNutrient(
            "Chicken", 100.0, 165, 31.0, 0.0, 3.6, 0.0, 0.0, 74.0);
        assertTrue(validFood.isValid());
    }
    
    @Test
    public void testIsValidWithInvalidMacroTotal() {
        // Total macros exceed food weight
        FoodNutrient invalidFood = new FoodNutrient(
            "Test", 100.0, 500, 50.0, 50.0, 50.0, 10.0, 10.0, 100.0);
        assertFalse(invalidFood.isValid());
    }
    
    @Test
    public void testIsValidWithIndividualExcess() {
        // Protein exceeds food weight
        FoodNutrient invalidProtein = new FoodNutrient(
            "Test", 100.0, 500, 150.0, 10.0, 10.0, 5.0, 5.0, 100.0);
        assertFalse(invalidProtein.isValid());
        
        // Carbs exceeds food weight
        FoodNutrient invalidCarbs = new FoodNutrient(
            "Test", 100.0, 500, 10.0, 150.0, 10.0, 5.0, 5.0, 100.0);
        assertFalse(invalidCarbs.isValid());
        
        // Fat exceeds food weight
        FoodNutrient invalidFat = new FoodNutrient(
            "Test", 100.0, 500, 10.0, 10.0, 150.0, 5.0, 5.0, 100.0);
        assertFalse(invalidFat.isValid());
    }
    
    @Test
    public void testIsValidWithSubnutrientExcess() {
        // Sugar exceeds carbs
        FoodNutrient invalidSugar = new FoodNutrient(
            "Test", 100.0, 500, 10.0, 10.0, 10.0, 5.0, 15.0, 100.0);
        assertFalse(invalidSugar.isValid());
        
        // Fiber exceeds carbs
        FoodNutrient invalidFiber = new FoodNutrient(
            "Test", 100.0, 500, 10.0, 10.0, 10.0, 15.0, 5.0, 100.0);
        assertFalse(invalidFiber.isValid());
    }
    /*
    @Test
    public void testToString() {
        FoodNutrient food = new FoodNutrient(
            "Banana", 100.0, 89, 1.1, 22.8, 0.3, 2.6, 12.2, 1.0);
        String expected = "Banana (100.0g, 89 calories) | P:1.1g, C:22.8g, F:0.3g";
        assertEquals(expected, food.toString());
    } 
    */
    
    /*
   @Test
    public void testToDetailedString() {
        FoodNutrient food = new FoodNutrient(
            "Banana", 100.0, 89, 1.1, 22.8, 0.3, 2.6, 12.2, 1.0);
        String expected = "Banana (100.0g, 89 calories)" +
                         "\n  - Protein: 1.1g" +
                         "\n  - Carbs: 22.8g" +
                         "\n  - Fat: 0.3g" +
                         "\n  - Fiber: 2.6g" +
                         "\n  - Sugar: 12.2g" +
                         "\n  - Sodium: 1.0mg";
        assertEquals(expected, food.toDetailedString());
    }
    */
}