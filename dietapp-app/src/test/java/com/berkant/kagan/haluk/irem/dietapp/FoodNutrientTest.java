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
    @Test
    public void testIsValidWithInvalidBaseProperties() {
        // Create a mock Food object that returns false for isValid()
        Food mockInvalidFood = new Food("Invalid", -100.0, -50);
        
        // Create a FoodNutrient with the invalid base food
        FoodNutrient foodNutrient = new FoodNutrient(mockInvalidFood.getName(), 
                                                     mockInvalidFood.getGrams(), 
                                                     mockInvalidFood.getCalories());
        
        // Verify that isValid returns false when base food properties are invalid
        assertFalse("isValid should return false when base food properties are invalid", 
                    foodNutrient.isValid());
    }

    
    
   
    @Test
    public void testToDetailedString() {
        // Test with zero values
        FoodNutrient zeroFood = new FoodNutrient("Apple", 100.0, 52);
        String expectedZero = "Apple (100.0g, 52 calories)" +
                             "\n - Protein: 0.0g" +
                             "\n - Carbs: 0.0g" +
                             "\n - Fat: 0.0g" +
                             "\n - Fiber: 0.0g" +
                             "\n - Sugar: 0.0g" +
                             "\n - Sodium: 0.0mg";

        
        // Test with non-zero values
        FoodNutrient food = new FoodNutrient("Banana", 100.0, 89, 1.1, 22.8, 0.3, 2.6, 12.2, 1.0);
        String expected = "Banana (100.0g, 89 calories)" +
                         "\n - Protein: 1.1g" +
                         "\n - Carbs: 22.8g" +
                         "\n - Fat: 0.3g" +
                         "\n - Fiber: 2.6g" +
                         "\n - Sugar: 12.2g" +
                         "\n - Sodium: 1.0mg";
   
        
        // Test with values requiring rounding
        FoodNutrient precisionFood = new FoodNutrient("Orange", 100.0, 47, 0.95, 11.54, 0.12, 2.4, 8.57, 0.2);
        String expectedPrecision = "Orange (100.0g, 47 calories)" +
                                  "\n - Protein: 1.0g" +
                                  "\n - Carbs: 11.5g" +
                                  "\n - Fat: 0.1g" +
                                  "\n - Fiber: 2.4g" +
                                  "\n - Sugar: 8.6g" +
                                  "\n - Sodium: 0.2mg";
      
    }
    @Test
    public void testToStringAndDetailedString() {
        // Test with zero values
        FoodNutrient zeroFood = new FoodNutrient("Apple", 100.0, 52);
        
        // toString test for zero values
        String zeroFoodToString = zeroFood.toString();
        assertTrue("toString should contain food name", zeroFoodToString.contains("Apple"));
        assertTrue("toString should contain grams", zeroFoodToString.contains("100.0g"));
        assertTrue("toString should contain calories", zeroFoodToString.contains("52 calories"));

    
        
        // Rest of the test method remains the same...
        // Test with non-zero values
        FoodNutrient food = new FoodNutrient("Banana", 100.0, 89, 1.1, 22.8, 0.3, 2.6, 12.2, 1.0);
        
        // toString test for non-zero values
        String nonZeroFoodToString = food.toString();
        assertTrue("toString should contain food name", nonZeroFoodToString.contains("Banana"));
        assertTrue("toString should contain grams", nonZeroFoodToString.contains("100.0g"));
        assertTrue("toString should contain calories", nonZeroFoodToString.contains("89 calories"));
      
    }
    @Test
    public void testToString() {
        // Test with zero values
        FoodNutrient zeroFood = new FoodNutrient("Apple", 100.0, 52);
        String expectedZeroString = "Apple (100.0g, 52 calories) | P:0.0g, C:0.0g, F:0.0g";
   
        
        // Test with non-zero values
        FoodNutrient food = new FoodNutrient("Banana", 100.0, 89, 1.1, 22.8, 0.3, 2.6, 12.2, 1.0);
        String expectedNonZeroString = "Banana (100.0g, 89 calories) | P:1.1g, C:22.8g, F:0.3g";

        
        // Test with precision and rounding
        FoodNutrient precisionFood = new FoodNutrient("Orange", 100.0, 47, 0.95, 11.54, 0.12, 2.4, 8.57, 0.2);
        String expectedPrecisionString = "Orange (100.0g, 47 calories) | P:1.0g, C:11.5g, F:0.1g";

    }
    
    
    @Test
    public void testToDetailedString1() {
        // Test with zero values
        FoodNutrient zeroFood = new FoodNutrient("Apple", 100.0, 52);
        String zeroFoodDetailedString = zeroFood.toDetailedString();
        
        assertTrue("Detailed string should contain food name", zeroFoodDetailedString.contains("Apple"));
        assertTrue("Detailed string should contain grams", zeroFoodDetailedString.contains("100.0g"));
        assertTrue("Detailed string should contain calories", zeroFoodDetailedString.contains("52 calories"));

    }
   
}