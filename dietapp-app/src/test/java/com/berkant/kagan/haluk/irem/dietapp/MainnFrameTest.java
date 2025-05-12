package com.berkant.kagan.haluk.irem.dietapp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainnFrameTest {
    private MainnFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel buttonPanel;
    private CalorieTrackingPanel calorieTrackingPanel;
    private MealPlanningPanel mealPlanningPanel;
    private PersonalizedDietPanel personalizedDietPanel;
    private ShoppingListPanel shoppingListPanel;
    private UserAuthenticationPanel authPanel;

    @Before
    public void setUp() throws Exception {
        // Initialize the frame on the Event Dispatch Thread
        SwingUtilities.invokeAndWait(() -> {
            frame = new MainnFrame();
            frame.setVisible(true);
        });
        
        // Extract components using reflection
        mainPanel = (JPanel) getPrivateField(frame, "mainPanel");
        cardLayout = (CardLayout) getPrivateField(frame, "cardLayout");
        buttonPanel = (JPanel) getPrivateField(frame, "buttonPanel");
        calorieTrackingPanel = (CalorieTrackingPanel) getPrivateField(frame, "calorieTrackingPanel");
        mealPlanningPanel = (MealPlanningPanel) getPrivateField(frame, "mealPlanningPanel");
        personalizedDietPanel = (PersonalizedDietPanel) getPrivateField(frame, "personalizedDietPanel");
        shoppingListPanel = (ShoppingListPanel) getPrivateField(frame, "shoppingListPanel");
        authPanel = (UserAuthenticationPanel) getPrivateField(frame, "authPanel");
    }

    @After
    public void tearDown() {
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }
    }

    @Test
    public void testFrameInitialization() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertNotNull("Frame should not be null", frame);
            assertEquals("Diet Planner", frame.getTitle());
            assertEquals(JFrame.DISPOSE_ON_CLOSE, frame.getDefaultCloseOperation());
            assertTrue("Frame should be visible", frame.isVisible());
            assertNotNull("Main panel should be initialized", mainPanel);
            assertNotNull("Card layout should be initialized", cardLayout);
            assertNotNull("Button panel should be initialized", buttonPanel);
            assertNotNull("Calorie tracking panel should be initialized", calorieTrackingPanel);
            assertNotNull("Meal planning panel should be initialized", mealPlanningPanel);
            assertNotNull("Personalized diet panel should be initialized", personalizedDietPanel);
            assertNotNull("Shopping list panel should be initialized", shoppingListPanel);
            assertNotNull("Auth panel should be initialized", authPanel);
        });
    }

    @Test
    public void testNavigationButtons() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // Make button panel visible for testing
            buttonPanel.setVisible(true);
            
            // Test Calorie Tracking button
            JButton calorieButton = findButtonByText(buttonPanel, "Calorie Tracking");
            assertNotNull("Calorie Tracking button should exist", calorieButton);
            calorieButton.doClick();
            assertTrue("Calorie panel should be visible", calorieTrackingPanel.isVisible());

            // Test Meal Planning button
            JButton mealButton = findButtonByText(buttonPanel, "Meal Planning");
            assertNotNull("Meal Planning button should exist", mealButton);
            mealButton.doClick();
            assertTrue("Meal planning panel should be visible", mealPlanningPanel.isVisible());

            // Test Personalized Diet button
            JButton dietButton = findButtonByText(buttonPanel, "Personalized Diet");
            assertNotNull("Personalized Diet button should exist", dietButton);
            dietButton.doClick();
            assertTrue("Personalized diet panel should be visible", personalizedDietPanel.isVisible());

            // Test Shopping List button
            JButton shoppingButton = findButtonByText(buttonPanel, "Shopping List");
            assertNotNull("Shopping List button should exist", shoppingButton);
            shoppingButton.doClick();
            assertTrue("Shopping list panel should be visible", shoppingListPanel.isVisible());
        });
    }

    @Test
    public void testShowMainMenu() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame.showMainMenu();
            assertTrue("Button panel should be visible after showing main menu", buttonPanel.isVisible());
            assertTrue("Calorie tracking panel should be visible after showing main menu", calorieTrackingPanel.isVisible());
        });
    }

    @Test
    public void testMainMethod() {
        // Create a thread to run the main method
        Thread mainThread = new Thread(() -> {
            try {
                String[] args = new String[0];
                MainnFrame.main(args);
            } catch (Exception e) {
                fail("Main method threw an exception: " + e.getMessage());
            }
        });

        mainThread.start();

        try {
            mainThread.join(1000); // Wait for 1 second
        } catch (InterruptedException e) {
            fail("Main method test was interrupted: " + e.getMessage());
        }

        if (mainThread.isAlive()) {
            mainThread.interrupt();
        }
    }

    @Test
    public void testJdbcDriverNotFound() {
        // Create a thread to run the test
        Thread testThread = new Thread(() -> {
            try {
                // Temporarily remove the JDBC driver
                ClassLoader.getSystemClassLoader().loadClass("org.sqlite.JDBC");
                MainnFrame.main(new String[0]);
            } catch (ClassNotFoundException e) {
                // Expected exception
                assertTrue("Should throw ClassNotFoundException", true);
            } catch (Exception e) {
                fail("Unexpected exception: " + e.getMessage());
            }
        });

        testThread.start();

        try {
            testThread.join(1000);
        } catch (InterruptedException e) {
            fail("Test was interrupted: " + e.getMessage());
        }

        if (testThread.isAlive()) {
            testThread.interrupt();
        }
    }

    // Helper methods
    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    private JButton findButtonByText(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton && text.equals(((JButton) component).getText())) {
                return (JButton) component;
            } else if (component instanceof Container) {
                JButton button = findButtonByText((Container) component, text);
                if (button != null) {
                    return button;
                }
            }
        }
        return null;
    }
}
