package com.berkant.kagan.haluk.irem.dietapp;

import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.junit.Assert.*;

public class MealPlanningPanelTest {

    private MealPlanningPanel panel;
    private MockMealPlanningService mockService;

    @Before
    public void setUp() {
        mockService = new MockMealPlanningService();
        panel = new MealPlanningPanel(mockService);
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
        assertEquals("Monday", mockService.day);
        assertEquals("Lunch", mockService.mealType);
        assertEquals("Chicken Salad", mockService.name);
        assertEquals(350, mockService.calories);
        assertEquals(30.0, mockService.protein, 0.001);
        assertEquals(10.0, mockService.carbs, 0.001);
        assertEquals(15.0, mockService.fat, 0.001);
        assertEquals("Chicken, Lettuce, Olive Oil", mockService.ingredients);

        // Fields should be cleared
        assertEquals("", getText(panel, "nameField"));
        assertEquals("", getText(panel, "caloriesField"));
        assertEquals("", getText(panel, "proteinField"));
        assertEquals("", getText(panel, "carbsField"));
        assertEquals("", getText(panel, "fatField"));
        assertEquals("", getTextArea(panel, "ingredientsArea"));
    }

    @Test
    public void testAddMealInvalidNumber() {
        setCombo(panel, "dayComboBox", "Tuesday");
        setCombo(panel, "mealTypeComboBox", "Dinner");
        setText(panel, "nameField", "Steak");
        setText(panel, "caloriesField", "notanumber");
        setText(panel, "proteinField", "25");
        setText(panel, "carbsField", "0");
        setText(panel, "fatField", "20");
        setTextArea(panel, "ingredientsArea", "Beef, Salt, Pepper");

        clickButton(panel, "addButton");

        assertFalse(mockService.addMealCalled);
    }

    @Test
    public void testAddMealEmptyFields() {
        setCombo(panel, "dayComboBox", "Wednesday");
        setCombo(panel, "mealTypeComboBox", "Breakfast");
        setText(panel, "nameField", "");
        setText(panel, "caloriesField", "200");
        setText(panel, "proteinField", "10");
        setText(panel, "carbsField", "30");
        setText(panel, "fatField", "5");
        setTextArea(panel, "ingredientsArea", "");

        clickButton(panel, "addButton");

        assertFalse(mockService.addMealCalled);
    }

    @Test
    public void testAddMealThrowsException() {
        setCombo(panel, "dayComboBox", "Thursday");
        setCombo(panel, "mealTypeComboBox", "Snack");
        setText(panel, "nameField", "Yogurt");
        setText(panel, "caloriesField", "100");
        setText(panel, "proteinField", "5");
        setText(panel, "carbsField", "12");
        setText(panel, "fatField", "2");
        setTextArea(panel, "ingredientsArea", "Yogurt, Honey");

        mockService.throwOnAdd = true;

        clickButton(panel, "addButton");

        assertTrue(mockService.addMealCalled);
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
    public void testViewWeeklyPlanThrowsException() {
        mockService.throwOnGetPlan = true;
        clickButton(panel, "viewButton");
        // Should not throw, should show dialog (not testable here, but no crash)
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
    public void testDeleteMealThrowsException() {
        setCombo(panel, "dayComboBox", "Saturday");
        setCombo(panel, "mealTypeComboBox", "Breakfast");
        mockService.throwOnDelete = true;
        clickButton(panel, "deleteButton");
        assertTrue(mockService.deleteMealCalled);
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
        int calories;
        double protein, carbs, fat;
        String weeklyPlan = "";

        MockMealPlanningService() {
            super(null);
        }

        @Override
        public void addMeal(String day, String mealType, String name, int calories, double protein, double carbs, double fat, String ingredients) {
            addMealCalled = true;
            this.day = day;
            this.mealType = mealType;
            this.name = name;
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