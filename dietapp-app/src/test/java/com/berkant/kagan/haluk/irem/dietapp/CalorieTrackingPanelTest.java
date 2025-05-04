package com.berkant.kagan.haluk.irem.dietapp;

import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CalorieTrackingPanelTest {

    private CalorieTrackingPanel panel;
    private MockCalorieNutrientTrackingService mockService;

    @Before
    public void setUp() {
        mockService = new MockCalorieNutrientTrackingService();
        panel = new CalorieTrackingPanel(mockService);
    }

    @Test
    public void testAddButtonSuccess() {
        setText(panel, "foodNameField", "Apple");
        setText(panel, "caloriesField", "95");
        setText(panel, "proteinField", "0.5");
        setText(panel, "carbsField", "25");
        setText(panel, "fatField", "0.3");

        clickButton(panel, "addButton");

        assertTrue(mockService.addFoodEntryCalled);
        assertEquals("Apple", mockService.foodName);
        assertEquals(95, mockService.calories);
        assertEquals(0.5, mockService.protein, 0.001);
        assertEquals(25, mockService.carbs, 0.001);
        assertEquals(0.3, mockService.fat, 0.001);

        // Fields should be cleared
        assertEquals("", getText(panel, "foodNameField"));
        assertEquals("", getText(panel, "caloriesField"));
        assertEquals("", getText(panel, "proteinField"));
        assertEquals("", getText(panel, "carbsField"));
        assertEquals("", getText(panel, "fatField"));
    }

    @Test
    public void testAddButtonInvalidNumber() {
        setText(panel, "foodNameField", "Banana");
        setText(panel, "caloriesField", "notanumber");
        setText(panel, "proteinField", "1.1");
        setText(panel, "carbsField", "27");
        setText(panel, "fatField", "0.3");

        clickButton(panel, "addButton");

        // Should not call service
        assertFalse(mockService.addFoodEntryCalled);
    }

    @Test
    public void testAddButtonThrowsException() {
        setText(panel, "foodNameField", "Orange");
        setText(panel, "caloriesField", "62");
        setText(panel, "proteinField", "1.2");
        setText(panel, "carbsField", "15");
        setText(panel, "fatField", "0.2");

        mockService.throwOnAdd = true;

        clickButton(panel, "addButton");

        // Should have tried to call service
        assertTrue(mockService.addFoodEntryCalled);
    }

    @Test
    public void testViewButtonSuccess() {
        mockService.entries = Arrays.asList(
                "Apple - Kalori: 95, Protein: 0.5g, Karbonhidrat: 25.0g, Yağ: 0.3g",
                "Banana - Kalori: 105, Protein: 1.1g, Karbonhidrat: 27.0g, Yağ: 0.3g"
        );

        clickButton(panel, "viewButton");

        JTextArea resultArea = (JTextArea) getField(panel, "resultArea");
        String text = resultArea.getText();
        assertTrue(text.contains("Apple"));
        assertTrue(text.contains("Banana"));
    }

    @Test
    public void testViewButtonThrowsException() {
        mockService.throwOnView = true;
        clickButton(panel, "viewButton");
        // Should not throw, should show dialog (not testable here, but no crash)
    }

    @Test
    public void testDeleteButtonSuccess() {
        setText(panel, "foodNameField", "Apple");
        clickButton(panel, "deleteButton");
        assertTrue(mockService.deleteFoodEntryCalled);
        assertEquals("Apple", mockService.foodName);

        // Field should be cleared
        assertEquals("", getText(panel, "foodNameField"));
    }

    @Test
    public void testDeleteButtonThrowsException() {
        setText(panel, "foodNameField", "Banana");
        mockService.throwOnDelete = true;
        clickButton(panel, "deleteButton");
        assertTrue(mockService.deleteFoodEntryCalled);
    }

    // --- Utility methods for reflection access ---

    private void setText(CalorieTrackingPanel panel, String fieldName, String value) {
        try {
            JTextField field = (JTextField) getField(panel, fieldName);
            field.setText(value);
        } catch (Exception e) {
            fail("Failed to set text: " + e.getMessage());
        }
    }

    private String getText(CalorieTrackingPanel panel, String fieldName) {
        try {
            JTextField field = (JTextField) getField(panel, fieldName);
            return field.getText();
        } catch (Exception e) {
            fail("Failed to get text: " + e.getMessage());
            return null;
        }
    }

    private void clickButton(CalorieTrackingPanel panel, String buttonName) {
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

    private static class MockCalorieNutrientTrackingService extends CalorieNutrientTrackingService {
        boolean addFoodEntryCalled = false;
        boolean deleteFoodEntryCalled = false;
        boolean throwOnAdd = false;
        boolean throwOnView = false;
        boolean throwOnDelete = false;
        String foodName;
        int calories;
        double protein, carbs, fat;
        List<String> entries = Arrays.asList();

        MockCalorieNutrientTrackingService() {
            super(null);
        }

        @Override
        public void addFoodEntry(String foodName, int calories, double protein, double carbs, double fat) throws SQLException {
            addFoodEntryCalled = true;
            this.foodName = foodName;
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
            if (throwOnAdd) throw new SQLException("Test exception");
        }

        @Override
        public List<String> viewFoodEntries() throws SQLException {
            if (throwOnView) throw new SQLException("Test exception");
            return entries;
        }

        @Override
        public void deleteFoodEntry(String foodName) throws SQLException {
            deleteFoodEntryCalled = true;
            this.foodName = foodName;
            if (throwOnDelete) throw new SQLException("Test exception");
        }
    }
}