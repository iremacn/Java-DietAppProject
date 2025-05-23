package com.berkant.kagan.haluk.irem.dietapp;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class MealPlanningPanelTest {

    private MealPlanningPanel panel;
    private MockMealPlanningService mockService;

    @Before
    public void setUp() throws Exception {
        mockService = new MockMealPlanningService();
        panel = new MealPlanningPanel(mockService);
        clearTestData();
    }

    @After
    public void tearDown() throws Exception {
        clearTestData();
    }

    private void clearTestData() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            PreparedStatement stmt1 = conn.prepareStatement("DELETE FROM food_nutrients");
            stmt1.executeUpdate();
            stmt1.close();
            PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM foods");
            stmt2.executeUpdate();
            stmt2.close();
            DatabaseHelper.releaseConnection(conn);
        } catch (SQLException e) {
            // ignore
        }
    }

    
    @Test
    public void testAddMealSuccess() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "Chicken Salad");
        setText(panel, "caloriesField", "350");
        setText(panel, "proteinField", "30");
        setText(panel, "carbsField", "10");
        setText(panel, "fatField", "15");
        setTextArea(panel, "ingredientsArea", "Chicken, Lettuce, Olive Oil");
        
        clickButton(panel, "addButton");
        
        assertTrue(mockService.addMealCalled);
        assertEquals(1, mockService.userId); // Default user ID
        assertEquals("Monday", mockService.day);
        assertEquals("Lunch", mockService.mealType);
        assertEquals("Chicken Salad", mockService.name);
        assertEquals(350, mockService.calories);
        assertEquals(30.0, mockService.protein, 0.001);
        assertEquals(10.0, mockService.carbs, 0.001);
        assertEquals(15.0, mockService.fat, 0.001);
        assertEquals("Chicken, Lettuce, Olive Oil", mockService.ingredients);

        // Fields should be cleared after successful add
        assertEquals("", getText(panel, "nameField"));
        assertEquals("", getText(panel, "caloriesField"));
        assertEquals("", getText(panel, "proteinField"));
        assertEquals("", getText(panel, "carbsField"));
        assertEquals("", getText(panel, "fatField"));
        assertEquals("", getTextArea(panel, "ingredientsArea"));
    }

    @Test
    public void testAddMealWithInvalidCalories() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "Chicken Salad");
        setText(panel, "caloriesField", "abc");
        setText(panel, "proteinField", "30");
        setText(panel, "carbsField", "10");
        setText(panel, "fatField", "15");
        setTextArea(panel, "ingredientsArea", "Chicken, Lettuce, Olive Oil");
        
        clickButton(panel, "addButton");
        
        assertFalse(mockService.addMealCalled);
        // Fields should not be cleared when validation fails
        assertEquals("Chicken Salad", getText(panel, "nameField"));
        assertEquals("abc", getText(panel, "caloriesField"));
    }

    @Test
    public void testAddMealWithInvalidProtein() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "Chicken Salad");
        setText(panel, "caloriesField", "350");
        setText(panel, "proteinField", "invalid");
        setText(panel, "carbsField", "10");
        setText(panel, "fatField", "15");
        setTextArea(panel, "ingredientsArea", "Chicken, Lettuce, Olive Oil");
        
        clickButton(panel, "addButton");
        
        assertFalse(mockService.addMealCalled);
    }

    @Test
    public void testAddMealWithInvalidCarbs() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "Chicken Salad");
        setText(panel, "caloriesField", "350");
        setText(panel, "proteinField", "30");
        setText(panel, "carbsField", "invalid");
        setText(panel, "fatField", "15");
        setTextArea(panel, "ingredientsArea", "Chicken, Lettuce, Olive Oil");
        
        clickButton(panel, "addButton");
        
        assertFalse(mockService.addMealCalled);
    }

    @Test
    public void testAddMealWithInvalidFat() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "Chicken Salad");
        setText(panel, "caloriesField", "350");
        setText(panel, "proteinField", "30");
        setText(panel, "carbsField", "10");
        setText(panel, "fatField", "invalid");
        setTextArea(panel, "ingredientsArea", "Chicken, Lettuce, Olive Oil");
        
        clickButton(panel, "addButton");
        
        assertFalse(mockService.addMealCalled);
    }

    @Test
    public void testAddMealWithEmptyName() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "");
        setText(panel, "caloriesField", "350");
        setText(panel, "proteinField", "30");
        setText(panel, "carbsField", "10");
        setText(panel, "fatField", "15");
        setTextArea(panel, "ingredientsArea", "Chicken, Lettuce, Olive Oil");
        
        clickButton(panel, "addButton");
        
        assertFalse(mockService.addMealCalled);
    }

    @Test
    public void testAddMealWithEmptyIngredients() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "Chicken Salad");
        setText(panel, "caloriesField", "350");
        setText(panel, "proteinField", "30");
        setText(panel, "carbsField", "10");
        setText(panel, "fatField", "15");
        setTextArea(panel, "ingredientsArea", "");
        
        clickButton(panel, "addButton");
        
        assertFalse(mockService.addMealCalled);
    }

    @Test
    public void testAddMealWithNegativeValues() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "Chicken Salad");
        setText(panel, "caloriesField", "-350");
        setText(panel, "proteinField", "-30");
        setText(panel, "carbsField", "-10");
        setText(panel, "fatField", "-15");
        setTextArea(panel, "ingredientsArea", "Chicken, Lettuce, Olive Oil");
        
        clickButton(panel, "addButton");
        
       
    }

    @Test
    public void testAddMealWithZeroValues() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "Chicken Salad");
        setText(panel, "caloriesField", "0");
        setText(panel, "proteinField", "0");
        setText(panel, "carbsField", "0");
        setText(panel, "fatField", "0");
        setTextArea(panel, "ingredientsArea", "Chicken, Lettuce, Olive Oil");
        
        clickButton(panel, "addButton");
        
        assertTrue(mockService.addMealCalled);
        assertEquals(0, mockService.calories);
        assertEquals(0.0, mockService.protein, 0.001);
        assertEquals(0.0, mockService.carbs, 0.001);
        assertEquals(0.0, mockService.fat, 0.001);
    }

    @Test
    public void testAddMealWithDecimalValues() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "Chicken Salad");
        setText(panel, "caloriesField", "350");
        setText(panel, "proteinField", "30.5");
        setText(panel, "carbsField", "10.75");
        setText(panel, "fatField", "15.25");
        setTextArea(panel, "ingredientsArea", "Chicken, Lettuce, Olive Oil");
        
        clickButton(panel, "addButton");
        
        assertTrue(mockService.addMealCalled);
        assertEquals(350, mockService.calories);
        assertEquals(30.5, mockService.protein, 0.001);
        assertEquals(10.75, mockService.carbs, 0.001);
        assertEquals(15.25, mockService.fat, 0.001);
    }

    @Test
    public void testAddMealWithException() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "Chicken Salad");
        setText(panel, "caloriesField", "350");
        setText(panel, "proteinField", "30");
        setText(panel, "carbsField", "10");
        setText(panel, "fatField", "15");
        setTextArea(panel, "ingredientsArea", "Chicken, Lettuce, Olive Oil");
        
        mockService.throwOnAdd = true;
        clickButton(panel, "addButton");
        
        assertTrue(mockService.addMealCalled);
        // Fields should not be cleared when exception occurs
        assertEquals("Chicken Salad", getText(panel, "nameField"));
        assertEquals("350", getText(panel, "caloriesField"));
    }

    @Test
    public void testViewWeeklyPlanSuccess() {
        mockService.weeklyPlan = "Monday: Chicken Salad\nTuesday: Steak";
        clickButton(panel, "viewButton");
        JTextArea weeklyPlanArea = (JTextArea) getField(panel, "weeklyPlanArea");
        String text = weeklyPlanArea.getText();
        assertTrue(text.contains("Chicken Salad"));
        assertTrue(text.contains("Steak"));
    }

    @Test
    public void testViewWeeklyPlanWithException() {
        mockService.throwOnGetPlan = true;
        clickButton(panel, "viewButton");
        JTextArea weeklyPlanArea = (JTextArea) getField(panel, "weeklyPlanArea");
        String text = weeklyPlanArea.getText();
        
    }

    @Test
    public void testDeleteMealSuccess() {
        setCombo(panel, "dayComboBox", "Friday");
        setCombo(panel, "mealTypeComboBox", "Dinner");
        clickButton(panel, "deleteButton");
        assertTrue(mockService.deleteMealCalled);
        assertEquals("Friday", mockService.day);
        assertEquals("Dinner", mockService.mealType);
    }

    @Test
    public void testDeleteMealWithException() {
        setCombo(panel, "dayComboBox", "Saturday");
        setCombo(panel, "mealTypeComboBox", "Breakfast");
        mockService.throwOnDelete = true;
        clickButton(panel, "deleteButton");
        assertTrue(mockService.deleteMealCalled);
    }

    @Test
    public void testDeleteMealWithEmptySelection() {
        setCombo(panel, "dayComboBox", "");
        setCombo(panel, "mealTypeComboBox", "");
        clickButton(panel, "deleteButton");
        
    }

    @Test
    public void testInitializeComponents() {
        assertNotNull(getField(panel, "nameField"));
        assertNotNull(getField(panel, "caloriesField"));
        assertNotNull(getField(panel, "proteinField"));
        assertNotNull(getField(panel, "carbsField"));
        assertNotNull(getField(panel, "fatField"));
        assertNotNull(getField(panel, "ingredientsArea"));
        assertNotNull(getField(panel, "dayComboBox"));
        assertNotNull(getField(panel, "mealTypeComboBox"));
        assertNotNull(getField(panel, "weeklyPlanArea"));
        assertNotNull(getField(panel, "addButton"));
        assertNotNull(getField(panel, "viewButton"));
        assertNotNull(getField(panel, "deleteButton"));
    }

    @Test
    public void testComboBoxSelections() {
        JComboBox<String> dayCombo = (JComboBox<String>) getField(panel, "dayComboBox");
        JComboBox<String> mealTypeCombo = (JComboBox<String>) getField(panel, "mealTypeComboBox");

        // Test day combo box
        assertEquals(7, dayCombo.getItemCount());
        assertEquals("Monday", dayCombo.getItemAt(0));
        assertEquals("Sunday", dayCombo.getItemAt(6));

        // Test meal type combo box
        assertEquals(4, mealTypeCombo.getItemCount());
        assertEquals("Breakfast", mealTypeCombo.getItemAt(0));
        assertEquals("Lunch", mealTypeCombo.getItemAt(1));
        assertEquals("Snack", mealTypeCombo.getItemAt(2));
        assertEquals("Dinner", mealTypeCombo.getItemAt(3));
    }

    @Test
    public void testAddMealWithAllFieldsEmpty() {
        setCombo(panel, "dayComboBox", "");
        setCombo(panel, "mealTypeComboBox", "");
        setText(panel, "nameField", "");
        setText(panel, "caloriesField", "");
        setText(panel, "proteinField", "");
        setText(panel, "carbsField", "");
        setText(panel, "fatField", "");
        setTextArea(panel, "ingredientsArea", "");
        
        clickButton(panel, "addButton");
        
        assertFalse(mockService.addMealCalled);
    }

    @Test
    public void testAddMealWithSpacesOnly() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "   ");
        setText(panel, "caloriesField", "350");
        setText(panel, "proteinField", "30");
        setText(panel, "carbsField", "10");
        setText(panel, "fatField", "15");
        setTextArea(panel, "ingredientsArea", "   ");
        
        clickButton(panel, "addButton");
        
        assertFalse(mockService.addMealCalled);
    }

    @Test
    public void testAddMealWithVeryLargeNumbers() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "Chicken Salad");
        setText(panel, "caloriesField", "999999999");
        setText(panel, "proteinField", "999999999");
        setText(panel, "carbsField", "999999999");
        setText(panel, "fatField", "999999999");
        setTextArea(panel, "ingredientsArea", "Chicken, Lettuce, Olive Oil");
        
        clickButton(panel, "addButton");
        
        assertTrue(mockService.addMealCalled);
        assertEquals(999999999, mockService.calories);
        assertEquals(999999999.0, mockService.protein, 0.001);
        assertEquals(999999999.0, mockService.carbs, 0.001);
        assertEquals(999999999.0, mockService.fat, 0.001);
    }

    @Test
    public void testAddMealWithSpecialCharacters() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "Chicken & Rice");
        setText(panel, "caloriesField", "350");
        setText(panel, "proteinField", "30");
        setText(panel, "carbsField", "10");
        setText(panel, "fatField", "15");
        setTextArea(panel, "ingredientsArea", "Chicken, Rice, Salt & Pepper");
        
        clickButton(panel, "addButton");
        
        assertTrue(mockService.addMealCalled);
        assertEquals("Chicken & Rice", mockService.name);
        assertEquals("Chicken, Rice, Salt & Pepper", mockService.ingredients);
    }

    @Test
    public void testAddMealWithLongText() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        setText(panel, "nameField", "Chicken Salad with Extra Long Name That Should Still Work");
        setText(panel, "caloriesField", "350");
        setText(panel, "proteinField", "30");
        setText(panel, "carbsField", "10");
        setText(panel, "fatField", "15");
        setTextArea(panel, "ingredientsArea", "Chicken, Lettuce, Olive Oil, Tomatoes, Cucumbers, Red Onions, Bell Peppers, Feta Cheese, Kalamata Olives, Fresh Herbs, Lemon Juice, Extra Virgin Olive Oil, Salt, Pepper, and a variety of other ingredients to make this a very long list");
        
        clickButton(panel, "addButton");
        
        assertTrue(mockService.addMealCalled);
        assertEquals("Chicken Salad with Extra Long Name That Should Still Work", mockService.name);
        assertTrue(mockService.ingredients.length() > 100);
    }

    @Test
    public void testViewWeeklyPlanEmpty() {
        mockService.weeklyPlan = "";
        clickButton(panel, "viewButton");
        JTextArea weeklyPlanArea = (JTextArea) getField(panel, "weeklyPlanArea");
        String text = weeklyPlanArea.getText();
        assertEquals("", text);
    }

    @Test
    public void testDeleteMealWithInvalidDay() {
        setCombo(panel, "dayComboBox", "InvalidDay");
        setCombo(panel, "mealTypeComboBox", "Lunch");
        clickButton(panel, "deleteButton");
        assertTrue(mockService.deleteMealCalled);
        
    }

    @Test
    public void testDeleteMealWithInvalidMealType() {
        setCombo(panel, "dayComboBox", "Monday");
        setCombo(panel, "mealTypeComboBox", "InvalidMealType");
        clickButton(panel, "deleteButton");
        assertTrue(mockService.deleteMealCalled);
    }

    @Test
    public void testAddMealWithDifferentMealTypes() {
        String[] mealTypes = {"Breakfast", "Lunch", "Snack", "Dinner"};
        for (String mealType : mealTypes) {
            setCombo(panel, "dayComboBox", "Monday");
            setCombo(panel, "mealTypeComboBox", mealType);
            setText(panel, "nameField", "Test Meal");
            setText(panel, "caloriesField", "350");
            setText(panel, "proteinField", "30");
            setText(panel, "carbsField", "10");
            setText(panel, "fatField", "15");
            setTextArea(panel, "ingredientsArea", "Test Ingredients");
            
            clickButton(panel, "addButton");
            
            assertTrue(mockService.addMealCalled);
            assertEquals(mealType, mockService.mealType);
            mockService.addMealCalled = false; // Reset for next iteration
        }
    }

    @Test
    public void testAddMealWithDifferentDays() {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : days) {
            setCombo(panel, "dayComboBox", day);
            setCombo(panel, "mealTypeComboBox", "Lunch");
            setText(panel, "nameField", "Test Meal");
            setText(panel, "caloriesField", "350");
            setText(panel, "proteinField", "30");
            setText(panel, "carbsField", "10");
            setText(panel, "fatField", "15");
            setTextArea(panel, "ingredientsArea", "Test Ingredients");
            
            clickButton(panel, "addButton");
            
            assertTrue(mockService.addMealCalled);
            assertEquals(day, mockService.day);
            mockService.addMealCalled = false; // Reset for next iteration
        }
    }

    // --- Utility methods for reflection access ---

    private void setText(MealPlanningPanel panel, String fieldName, String value) {
        try {
            JTextField field = (JTextField) getField(panel, fieldName);
            field.setText(value);
        } catch (Exception e) {
            fail("Failed to set text: " + e.getMessage());
        }
    }

    private String getText(MealPlanningPanel panel, String fieldName) {
        try {
            JTextField field = (JTextField) getField(panel, fieldName);
            return field.getText();
        } catch (Exception e) {
            fail("Failed to get text: " + e.getMessage());
            return null;
        }
    }

    private void setTextArea(MealPlanningPanel panel, String fieldName, String value) {
        try {
            JTextArea area = (JTextArea) getField(panel, fieldName);
            area.setText(value);
        } catch (Exception e) {
            fail("Failed to set text area: " + e.getMessage());
        }
    }

    private String getTextArea(MealPlanningPanel panel, String fieldName) {
        try {
            JTextArea area = (JTextArea) getField(panel, fieldName);
            return area.getText();
        } catch (Exception e) {
            fail("Failed to get text area: " + e.getMessage());
            return null;
        }
    }

    private void setCombo(MealPlanningPanel panel, String fieldName, String value) {
        try {
            JComboBox combo = (JComboBox) getField(panel, fieldName);
            combo.setSelectedItem(value);
        } catch (Exception e) {
            fail("Failed to set combo: " + e.getMessage());
        }
    }

    private void clickButton(MealPlanningPanel panel, String buttonName) {
        try {
            JButton button = (JButton) getField(panel, buttonName);
            for (ActionListener al : button.getActionListeners()) {
                al.actionPerformed(new ActionEvent(button, ActionEvent.ACTION_PERFORMED, ""));
            }
        } catch (Exception e) {
            fail("Failed to click button: " + e.getMessage());
        }
    }

    private Object getField(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access field: " + fieldName, e);
        }
    }

    // --- Mock Service ---

    private static class MockMealPlanningService extends MealPlanningService {
        boolean addMealCalled = false;
        boolean deleteMealCalled = false;
        boolean throwOnAdd = false;
        boolean throwOnDelete = false;
        boolean throwOnGetPlan = false;
        String day, mealType, name, ingredients;
        int calories, userId;
        double protein, carbs, fat;
        String weeklyPlan = "";

        MockMealPlanningService() {
            super(null);
        }

        @Override
        public void addMeal(int userId, String day, String mealType, String foodName, int calories, 
                           double protein, double carbs, double fat, String ingredients) {
            addMealCalled = true;
            this.userId = userId;
            this.day = day;
            this.mealType = mealType;
            this.name = foodName;
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
            this.ingredients = ingredients;
            if (throwOnAdd) throw new RuntimeException("Test exception");
        }

        @Override
        public void deleteMeal(String day, String mealType) {
            deleteMealCalled = true;
            this.day = day;
            this.mealType = mealType;
            if (throwOnDelete) throw new RuntimeException("Test exception");
        }

        @Override
        public String getWeeklyMealPlan() {
            if (throwOnGetPlan) throw new RuntimeException("Test exception");
            return weeklyPlan;
        }
    }
}