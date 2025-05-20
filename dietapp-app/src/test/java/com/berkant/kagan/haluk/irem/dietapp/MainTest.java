package com.berkant.kagan.haluk.irem.dietapp;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the MainFrame class.
 * These tests verify the functionality of the main application frame,
 * including initialization, navigation, and error handling.
 */
public class MainTest {

    private MainFrame mainFrame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel buttonPanel;
    private CalorieTrackingPanel calorieTrackingPanel;
    private MealPlanningPanel mealPlanningPanel;
    private PersonalizedDietPanel personalizedDietPanel;
    private ShoppingListPanel shoppingListPanel;
    private UserAuthenticationPanel authPanel;

    @Before
    public void setUp() {
        // Initialize DatabaseHelper for tests
        DatabaseHelper.initializeDatabase();
        
        // Create the frame
        mainFrame = new MainFrame();
        
        // Extract components using reflection
        try {
            mainPanel = (JPanel) getPrivateField(mainFrame, "mainPanel");
            cardLayout = (CardLayout) getPrivateField(mainFrame, "cardLayout");
            buttonPanel = (JPanel) getPrivateField(mainFrame, "buttonPanel");
            calorieTrackingPanel = (CalorieTrackingPanel) getPrivateField(mainFrame, "calorieTrackingPanel");
            mealPlanningPanel = (MealPlanningPanel) getPrivateField(mainFrame, "mealPlanningPanel");
            personalizedDietPanel = (PersonalizedDietPanel) getPrivateField(mainFrame, "personalizedDietPanel");
            shoppingListPanel = (ShoppingListPanel) getPrivateField(mainFrame, "shoppingListPanel");
            authPanel = (UserAuthenticationPanel) getPrivateField(mainFrame, "authPanel");
        } catch (Exception e) {
            fail("Failed to extract components: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        if (mainFrame != null) {
            mainFrame.dispose();
        }
        DatabaseHelper.closeConnection();
    }

    @Test
    public void testFrameInitialization() {
        assertNotNull("MainFrame should be initialized", mainFrame);
        
        assertEquals("Frame title should be 'Diet Planner'", "Diet Planner", mainFrame.getTitle());
        assertEquals("Frame width should be 800", 800, mainFrame.getWidth());
        assertEquals("Frame height should be 600", 600, mainFrame.getHeight());
    }

    @Test
    public void testComponentInitialization() {
        assertNotNull("Main panel should be initialized", mainPanel);
        assertNotNull("Card layout should be initialized", cardLayout);
        assertNotNull("Button panel should be initialized", buttonPanel);
        assertNotNull("Calorie tracking panel should be initialized", calorieTrackingPanel);
        assertNotNull("Meal planning panel should be initialized", mealPlanningPanel);
        assertNotNull("Personalized diet panel should be initialized", personalizedDietPanel);
        assertNotNull("Shopping list panel should be initialized", shoppingListPanel);
        assertNotNull("Auth panel should be initialized", authPanel);
    }

    @Test
    public void testInitialPanelVisibility() {
        assertFalse("Button panel should be initially hidden", buttonPanel.isVisible());
        // Verify login panel is shown initially
        Component visibleComponent = null;
        for (Component comp : mainPanel.getComponents()) {
            if (comp.isVisible()) {
                visibleComponent = comp;
                break;
            }
        }
        assertTrue("Login panel should be visible initially", visibleComponent instanceof UserAuthenticationPanel);
    }

    @Test
    public void testNavigationButtons() {
        // Find navigation buttons
        JButton calorieButton = findButtonByText(buttonPanel, "Calorie Tracking");
        JButton mealButton = findButtonByText(buttonPanel, "Meal Planning");
        JButton dietButton = findButtonByText(buttonPanel, "Personalized Diet");
        JButton shoppingButton = findButtonByText(buttonPanel, "Shopping List");

        assertNotNull("Calorie tracking button should exist", calorieButton);
        assertNotNull("Meal planning button should exist", mealButton);
        assertNotNull("Diet button should exist", dietButton);
        assertNotNull("Shopping list button should exist", shoppingButton);

        // Test navigation
        calorieButton.doClick();
        assertTrue("Calorie tracking panel should be visible", calorieTrackingPanel.isVisible());

        mealButton.doClick();
        assertTrue("Meal planning panel should be visible", mealPlanningPanel.isVisible());

        dietButton.doClick();
        assertTrue("Personalized diet panel should be visible", personalizedDietPanel.isVisible());

        shoppingButton.doClick();
        assertTrue("Shopping list panel should be visible", shoppingListPanel.isVisible());
    }

    @Test
    public void testShowMainMenu() {
        mainFrame.showMainMenu();
        assertTrue("Button panel should be visible after showing main menu", buttonPanel.isVisible());
        assertTrue("Calorie tracking panel should be visible after showing main menu", calorieTrackingPanel.isVisible());
    }

    @Test
    public void testSetAndGetTestMode() {
        // Test başlangıçta test modunun false olduğunu kontrol et
        assertFalse(Main.isTestMode());
        
        // Test modunu true olarak ayarla
        Main.setTestMode(true);
        assertTrue(Main.isTestMode());
        
        // Test modunu false olarak ayarla
        Main.setTestMode(false);
        assertFalse(Main.isTestMode());
    }
    
    @Test
    public void testMainMethod() {
        // Test modunu aktif et
        Main.setTestMode(true);
        
        // main metodunu çağır
        Main.main(new String[]{});
        
        // Test modunu kapat
        Main.setTestMode(false);
    }

    private Object getPrivateField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            fail("Failed to access field " + fieldName + ": " + e.getMessage());
            return null;
        }
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
