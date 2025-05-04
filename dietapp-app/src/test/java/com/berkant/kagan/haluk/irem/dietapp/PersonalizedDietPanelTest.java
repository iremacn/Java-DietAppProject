package com.berkant.kagan.haluk.irem.dietapp;

import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PersonalizedDietPanelTest {

    private PersonalizedDietPanel panel;
    private MockPersonalizedDietRecommendationService mockService;

    @Before
    public void setUp() {
        mockService = new MockPersonalizedDietRecommendationService();
        panel = new PersonalizedDietPanel(mockService);
    }

    @Test
    public void testGenerateRecommendationsSuccess() {
        setText(panel, "ageField", "30");
        setText(panel, "weightField", "70");
        setText(panel, "heightField", "175");
        setCombo(panel, "genderComboBox", "Male");
        setCombo(panel, "activityLevelComboBox", "Moderate");

        mockService.recommendations = Arrays.asList("Eat more vegetables", "Exercise daily");

        clickButton(panel, "generateButton");

        assertTrue(mockService.generateRecommendationsCalled);
        assertEquals(30, mockService.age);
        assertEquals(70.0, mockService.weight, 0.001);
        assertEquals(175.0, mockService.height, 0.001);
        assertEquals("Male", mockService.gender);
        assertEquals("Moderate", mockService.activityLevel);
    }

    @Test
    public void testGenerateRecommendationsInvalidNumber() {
        setText(panel, "ageField", "notanumber");
        setText(panel, "weightField", "70");
        setText(panel, "heightField", "175");
        setCombo(panel, "genderComboBox", "Female");
        setCombo(panel, "activityLevelComboBox", "Active");

        clickButton(panel, "generateButton");

        assertFalse(mockService.generateRecommendationsCalled);
    }

    @Test
    public void testGenerateRecommendationsThrowsException() {
        setText(panel, "ageField", "25");
        setText(panel, "weightField", "60");
        setText(panel, "heightField", "165");
        setCombo(panel, "genderComboBox", "Female");
        setCombo(panel, "activityLevelComboBox", "Light");

        mockService.throwOnGenerate = true;

        clickButton(panel, "generateButton");

        assertTrue(mockService.generateRecommendationsCalled);
    }

    // --- Utility methods for reflection access ---

    private void setText(PersonalizedDietPanel panel, String fieldName, String value) {
        try {
            JTextField field = (JTextField) getField(panel, fieldName);
            field.setText(value);
        } catch (Exception e) {
            fail("Failed to set text: " + e.getMessage());
        }
    }

    private void setCombo(PersonalizedDietPanel panel, String fieldName, String value) {
        try {
            JComboBox combo = (JComboBox) getField(panel, fieldName);
            combo.setSelectedItem(value);
        } catch (Exception e) {
            fail("Failed to set combo: " + e.getMessage());
        }
    }

    private void clickButton(PersonalizedDietPanel panel, String buttonName) {
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

    private static class MockPersonalizedDietRecommendationService extends PersonalizedDietRecommendationService {
        boolean generateRecommendationsCalled = false;
        boolean throwOnGenerate = false;
        int age;
        double weight, height;
        String gender, activityLevel;
        List<String> recommendations = Arrays.asList();

        MockPersonalizedDietRecommendationService() {
            super(null, null); // Use the required constructor with nulls
        }

        @Override
        public List<String> generateRecommendations(int age, double weight, double height, String gender, String activityLevel) {
            generateRecommendationsCalled = true;
            this.age = age;
            this.weight = weight;
            this.height = height;
            this.gender = gender;
            this.activityLevel = activityLevel;
            if (throwOnGenerate) throw new RuntimeException("Test exception");
            return recommendations;
        }
    }
}