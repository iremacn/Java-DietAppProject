package com.berkant.kagan.haluk.irem.dietapp;

import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ShoppingListPanelTest {

    private ShoppingListPanel panel;
    private MockShoppingListService mockService;

    @Before
    public void setUp() {
        mockService = new MockShoppingListService();
        panel = new ShoppingListPanel(mockService);
    }

    @Test
    public void testGenerateShoppingListSuccess() {
        mockService.shoppingList = Arrays.asList("Apples", "Bananas", "Chicken Breast");

        clickButton(panel, "generateButton");

        JTextArea area = (JTextArea) getField(panel, "shoppingListArea");
        String text = area.getText();
        assertTrue(text.contains("Apples"));
        assertTrue(text.contains("Bananas"));
        assertTrue(text.contains("Chicken Breast"));
        assertTrue(mockService.generateShoppingListCalled);
    }

    @Test
    public void testGenerateShoppingListThrowsException() {
        mockService.throwOnGenerate = true;
        try {
            clickButton(panel, "generateButton");
        } catch (java.awt.HeadlessException e) {
            // Ignore, expected in headless test environments
        }
        assertTrue(mockService.generateShoppingListCalled);
    }

    @Test
    public void testClearButton() {
        JTextArea area = (JTextArea) getField(panel, "shoppingListArea");
        area.setText("Some text");
        clickButton(panel, "clearButton");
        assertEquals("", area.getText());
    }

    // --- Utility methods for reflection access ---

    private void clickButton(ShoppingListPanel panel, String buttonName) {
        JButton button = null;
        try {
            button = (JButton) getField(panel, buttonName);
        } catch (Exception e) {
            fail("Failed to get button: " + buttonName + " - " + e.getMessage());
        }
        if (button == null) {
            StringBuilder sb = new StringBuilder("Button '").append(buttonName).append("' not found. Available fields: ");
            for (java.lang.reflect.Field f : panel.getClass().getDeclaredFields()) {
                sb.append(f.getName()).append(" ");
            }
            fail(sb.toString());
        }
        for (ActionListener al : button.getActionListeners()) {
            al.actionPerformed(new ActionEvent(button, ActionEvent.ACTION_PERFORMED, ""));
        }
    }

    private Object getField(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException nsfe) {
            // Try superclass (for inherited fields)
            Class<?> superClass = obj.getClass().getSuperclass();
            if (superClass != null) {
                try {
                    java.lang.reflect.Field field = superClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.get(obj);
                } catch (Exception e) {
                    // fall through
                }
            }
            StringBuilder sb = new StringBuilder("Field '").append(fieldName).append("' not found. Available fields: ");
            for (java.lang.reflect.Field f : obj.getClass().getDeclaredFields()) {
                sb.append(f.getName()).append(" ");
            }
            throw new RuntimeException(sb.toString(), nsfe);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access field: " + fieldName, e);
        }
    }

    // --- Mock Service ---

    private static class MockShoppingListService extends ShoppingListService {
        boolean generateShoppingListCalled = false;
        boolean throwOnGenerate = false;
        List<String> shoppingList = Arrays.asList();

        MockShoppingListService() {
            super(null);
        }

        @Override
        public List<String> generateShoppingList() {
            generateShoppingListCalled = true;
            if (throwOnGenerate) throw new RuntimeException("Test exception");
            return shoppingList;
        }
    }
}