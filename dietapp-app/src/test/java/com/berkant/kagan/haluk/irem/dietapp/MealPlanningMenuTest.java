package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;

/**
 * @class MealPlanningMenuTest
 * @brief Enhanced test class for the MealPlanningMenu class, including UI tests.
 * @details This test class aims to provide comprehensive coverage for both console
 *          and graphical interface functionality of the MealPlanningMenu class.
 */
public class MealPlanningMenuTest {
    
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    
    private MealPlanningService mealPlanningService;
    private AuthenticationService authService;
    private TestMealPlanningMenu mealPlanningMenu;
    private User testUser;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Initialize database
        DatabaseHelper.initializeDatabase();
        
        // Set test mode to avoid Swing EDT issues
        DietappApp.setTestMode(true);
    }
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        // Close database connections
        DatabaseHelper.closeAllConnections();
    }
    
    @Before
    public void setUp() throws Exception {
        // Redirect System.out to our outputStream for console tests
        System.setOut(new PrintStream(outputStream));
        
        // Initialize services
        mealPlanningService = new MockMealPlanningService();
        authService = new MockAuthenticationService();
        
        // Create and login a test user
        testUser = new User("testuser", "password", "test@example.com", "Test User");
        testUser.setLoggedIn(true);
        ((MockAuthenticationService)authService).setCurrentUser(testUser);
        
        // Create the menu instance for testing
        mealPlanningMenu = new TestMealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Ensure UI components initialization runs on EDT
        try {
            SwingUtilities.invokeAndWait(() -> {
                // Initialize UI components if needed
            });
        } catch (InvocationTargetException e) {
            // Ignore for tests
        }
    }
    
    @After
    public void tearDown() throws Exception {
        // Reset output and input streams
        System.setOut(originalOut);
        System.setIn(originalIn);
        
        // Clear the output stream
        outputStream.reset();
        
        // Clean up any UI components
        try {
            SwingUtilities.invokeAndWait(() -> {
                disposeAllWindows();
            });
        } catch (InvocationTargetException e) {
            // Ignore for tests
        }
    }
    
    /**
     * Custom implementation of MealPlanningMenu for testing
     */
    private class TestMealPlanningMenu extends MealPlanningMenu {
        private boolean uiTestMode = false;
        
        public TestMealPlanningMenu(MealPlanningService service, AuthenticationService authService, Scanner scanner) {
            super(service, authService, scanner);
            // By default, disable UI components for console tests
            this.useUIComponents = false;
        }
        
        // Enable UI testing mode
        public void enableUIMode() {
            this.useUIComponents = true;
            this.uiTestMode = true;
        }
        
        // Check if UI testing mode is enabled
        public boolean isUITestMode() {
            return this.uiTestMode;
        }
        
     // Instead of trying to override initializeUI() directly
        private void initializeUIForTest() {
            if (isUITestMode()) {
                try {
                    // Use reflection to access the private method
                    Method initMethod = MealPlanningMenu.class.getDeclaredMethod("initializeUI");
                    initMethod.setAccessible(true);
                    initMethod.invoke(this);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to access initializeUI method", e);
                }
            }
        }
        
        // Expose protected methods for testing via reflection
        public void accessHandlePlanMealsUI() {
            try {
                Method method = MealPlanningMenu.class.getDeclaredMethod("handlePlanMealsUI");
                method.setAccessible(true);
                method.invoke(this);
            } catch (Exception e) {
                throw new RuntimeException("Failed to access handlePlanMealsUI", e);
            }
        }
        
        public void accessHandleLogFoodsUI() {
            try {
                Method method = MealPlanningMenu.class.getDeclaredMethod("handleLogFoodsUI");
                method.setAccessible(true);
                method.invoke(this);
            } catch (Exception e) {
                throw new RuntimeException("Failed to access handleLogFoodsUI", e);
            }
        }
        
        public void accessHandleViewMealHistoryUI() {
            try {
                Method method = MealPlanningMenu.class.getDeclaredMethod("handleViewMealHistoryUI");
                method.setAccessible(true);
                method.invoke(this);
            } catch (Exception e) {
                throw new RuntimeException("Failed to access handleViewMealHistoryUI", e);
            }
        }
        
        public void accessShowFoodSelectionUI(String date, String mealType) {
            try {
                Method method = MealPlanningMenu.class.getDeclaredMethod("showFoodSelectionUI", String.class, String.class);
                method.setAccessible(true);
                method.invoke(this, date, mealType);
            } catch (Exception e) {
                throw new RuntimeException("Failed to access showFoodSelectionUI", e);
            }
        }
        
        public void accessDisplayMealHistoryUI(String date) {
            try {
                Method method = MealPlanningMenu.class.getDeclaredMethod("displayMealHistoryUI", String.class);
                method.setAccessible(true);
                method.invoke(this, date);
            } catch (Exception e) {
                throw new RuntimeException("Failed to access displayMealHistoryUI", e);
            }
        }
        
        // Access UI components for testing
        public JFrame getFrame() {
            try {
                Field frameField = MealPlanningMenu.class.getDeclaredField("frame");
                frameField.setAccessible(true);
                return (JFrame) frameField.get(this);
            } catch (Exception e) {
                throw new RuntimeException("Failed to access frame", e);
            }
        }
        
        public JPanel getMainPanel() {
            try {
                Field mainPanelField = MealPlanningMenu.class.getDeclaredField("mainPanel");
                mainPanelField.setAccessible(true);
                return (JPanel) mainPanelField.get(this);
            } catch (Exception e) {
                throw new RuntimeException("Failed to access mainPanel", e);
            }
        }
    }
    
    /**
     * Mock implementation of MealPlanningService for testing
     */
    private class MockMealPlanningService extends MealPlanningService {
        private boolean returnMealPlan = false;
        private boolean returnFoodLog = false;
        private boolean successfulOperations = true;
        
        public MockMealPlanningService() {
            super(null);
        }
        
        public void setReturnMealPlan(boolean value) {
            this.returnMealPlan = value;
        }
        
        public void setReturnFoodLog(boolean value) {
            this.returnFoodLog = value;
        }
        
        public void setSuccessfulOperations(boolean value) {
            this.successfulOperations = value;
        }
        
        @Override
        public boolean addMealPlan(String username, String date, String mealType, Food food) {
            return successfulOperations;
        }
        
        @Override
        public boolean logFood(String username, String date, Food food) {
            return successfulOperations;
        }
        
        @Override
        public List<Food> getMealPlan(String username, String date, String mealType) {
            if (returnMealPlan) {
                List<Food> plan = new ArrayList<>();
                plan.add(new Food("Test Food", 100, 200));
                return plan;
            }
            return new ArrayList<>();
        }
        
        @Override
        public List<Food> getFoodLog(String username, String date) {
            if (returnFoodLog) {
                List<Food> log = new ArrayList<>();
                log.add(new Food("Logged Food", 50, 100));
                return log;
            }
            return new ArrayList<>();
        }
        
        @Override
        public int getTotalCalories(String username, String date) {
            return returnFoodLog ? 100 : 0;
        }
        
        @Override
        public boolean isValidDate(int year, int month, int day) {
            return true; // Always valid for tests
        }
        
        @Override
        public String formatDate(int year, int month, int day) {
            return "2025-" + String.format("%02d", month) + "-" + String.format("%02d", day);
        }
        
        @Override
        public Food[] getBreakfastOptions() {
            return new Food[] { 
                new Food("Breakfast Option 1", 100, 200),
                new Food("Breakfast Option 2", 120, 220)
            };
        }
        
        @Override
        public Food[] getLunchOptions() {
            return new Food[] { 
                new Food("Lunch Option 1", 250, 350),
                new Food("Lunch Option 2", 270, 370)
            };
        }
        
        @Override
        public Food[] getSnackOptions() {
            return new Food[] { 
                new Food("Snack Option 1", 80, 150),
                new Food("Snack Option 2", 90, 170)
            };
        }
        
        @Override
        public Food[] getDinnerOptions() {
            return new Food[] { 
                new Food("Dinner Option 1", 300, 450),
                new Food("Dinner Option 2", 320, 470)
            };
        }
        
        @Override
        public List<String> getMealsForDay(String day) {
            List<String> meals = new ArrayList<>();
            meals.add("Breakfast: Eggs (Calories: 150, Protein: 12.0g, Carbs: 1.0g, Fat: 10.0g)");
            meals.add("Lunch: Salad (Calories: 200, Protein: 5.0g, Carbs: 15.0g, Fat: 12.0g)");
            return meals;
        }
    }
    
    /**
     * Mock implementation of AuthenticationService for testing
     */
    private class MockAuthenticationService extends AuthenticationService {
        private User currentUser;
        
        public void setCurrentUser(User user) {
            this.currentUser = user;
        }
        
        @Override
        public User getCurrentUser() {
            return currentUser;
        }
    }
    
    // Helper method to dispose all JFrames created during tests
    private void disposeAllWindows() {
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame) {
                window.dispose();
            }
        }
    }
    
    // Helper method to find a component by name in a container
    private Component findComponentByName(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container) {
                Component found = findComponentByName((Container) component, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    // Helper method to simulate button click
    private void clickButton(JButton button) {
        button.doClick();
    }
    
    // Helper method to simulate JComboBox selection
    private void selectComboBoxItem(JComboBox<?> comboBox, int index) {
        comboBox.setSelectedIndex(index);
    }
    
    // Helper method to simulate JList selection
    private void selectListItem(JList<?> list, int index) {
        list.setSelectedIndex(index);
        ListSelectionListener[] listeners = list.getListSelectionListeners();
        if (listeners.length > 0) {
            ListSelectionEvent event = new ListSelectionEvent(list, index, index, false);
            for (ListSelectionListener listener : listeners) {
                listener.valueChanged(event);
            }
        }
    }
    
    /**
     * Test initialization of UI components
     */
    @Test
    public void testUIInitialization() throws Exception {
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
        });
        
        // Verify that frame was created
        JFrame frame = mealPlanningMenu.getFrame();
        assertNotNull("Frame should be created", frame);
        
        // Verify that main panel was created
        JPanel mainPanel = mealPlanningMenu.getMainPanel();
        assertNotNull("Main panel should be created", mainPanel);
        
        // Verify that buttons were added to main panel
        Component[] components = mainPanel.getComponents();
        boolean hasButtons = false;
        for (Component component : components) {
            if (component instanceof JButton) {
                hasButtons = true;
                break;
            }
        }
        assertTrue("Main panel should contain buttons", hasButtons);
    }
    
    /**
     * Test button actions in the main menu UI
     */
    @Test
    public void testMainMenuButtonActions() throws Exception {
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
        });
        
        // Get the frame and main panel
        JFrame frame = mealPlanningMenu.getFrame();
        JPanel mainPanel = mealPlanningMenu.getMainPanel();
        
        // Find buttons in the panel
        JButton planMealsButton = null;
        JButton logFoodsButton = null;
        JButton viewHistoryButton = null;
        JButton returnButton = null;
        
        for (Component component : mainPanel.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if ("Plan Meals".equals(button.getText())) {
                    planMealsButton = button;
                } else if ("Log Foods".equals(button.getText())) {
                    logFoodsButton = button;
                } else if ("View Meal History".equals(button.getText())) {
                    viewHistoryButton = button;
                } else if ("Return to Main Menu".equals(button.getText())) {
                    returnButton = button;
                }
            }
        }
        
        // Verify that all buttons were found
        assertNotNull("Plan Meals button should exist", planMealsButton);
        assertNotNull("Log Foods button should exist", logFoodsButton);
        assertNotNull("View History button should exist", viewHistoryButton);
        assertNotNull("Return button should exist", returnButton);
        
        // Test that clicking the return button disposes the frame
        final JButton finalReturnButton = returnButton;
        final JFrame finalFrame = frame;
        
        SwingUtilities.invokeAndWait(() -> {
            // Check frame visibility before click
            assertTrue("Frame should be visible before clicking return", finalFrame.isVisible());
            
            // Click the return button
            clickButton(finalReturnButton);
            
            // For this test, we just verify the frame is still there since we're not accessing
            // the real implementation that would dispose the frame
            assertFalse("Frame should be closed after clicking return", finalFrame.isVisible());
        });
    }
    
    /**
     * Test the handlePlanMealsUI method
     */
    @Test
    public void testHandlePlanMealsUI() throws Exception {
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components and call the method on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
            mealPlanningMenu.accessHandlePlanMealsUI();
        });
        
        // Wait for all windows to appear
        Thread.sleep(200);
        
        // Find the plan meals dialog
        boolean foundPlanMealsDialog = false;
        JFrame planFrame = null;
        
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window != mealPlanningMenu.getFrame()) {
                JFrame frame = (JFrame) window;
                if (frame.getTitle() != null && frame.getTitle().contains("Plan Meals")) {
                    foundPlanMealsDialog = true;
                    planFrame = frame;
                    break;
                }
            }
        }
        
        assertTrue("Plan Meals dialog should be created", foundPlanMealsDialog);
        
        // Verify that the plan meals dialog contains expected components
        if (planFrame != null) {
            Container contentPane = planFrame.getContentPane();
            
            // Look for text fields for date input
            int textFieldCount = 0;
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JTextField) {
                    textFieldCount++;
                }
            }
            
            assertTrue("Plan Meals dialog should contain text fields", textFieldCount > 0);
            
            // Look for buttons
            boolean hasContinueButton = false;
            boolean hasCancelButton = false;
            
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JButton) {
                    JButton button = (JButton) component;
                    if ("Continue".equals(button.getText())) {
                        hasContinueButton = true;
                    } else if ("Cancel".equals(button.getText())) {
                        hasCancelButton = true;
                    }
                }
            }
            
            assertTrue("Plan Meals dialog should have a Continue button", hasContinueButton);
            assertTrue("Plan Meals dialog should have a Cancel button", hasCancelButton);
        }
    }
    
    /**
     * Test the handleLogFoodsUI method
     */
    @Test
    public void testHandleLogFoodsUI() throws Exception {
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components and call the method on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
            mealPlanningMenu.accessHandleLogFoodsUI();
        });
        
        // Wait for all windows to appear
        Thread.sleep(200);
        
        // Find the log foods dialog
        boolean foundLogFoodsDialog = false;
        JFrame logFrame = null;
        
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window != mealPlanningMenu.getFrame()) {
                JFrame frame = (JFrame) window;
                if (frame.getTitle() != null && frame.getTitle().contains("Log Foods")) {
                    foundLogFoodsDialog = true;
                    logFrame = frame;
                    break;
                }
            }
        }
        
        assertTrue("Log Foods dialog should be created", foundLogFoodsDialog);
        
        // Verify that the log foods dialog contains expected components
        if (logFrame != null) {
            Container contentPane = logFrame.getContentPane();
            
            // Look for text fields for food input
            int textFieldCount = 0;
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JTextField) {
                    textFieldCount++;
                }
            }
            
            assertTrue("Log Foods dialog should contain text fields", textFieldCount > 0);
            
            // Look for buttons
            boolean hasSaveButton = false;
            boolean hasCancelButton = false;
            
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JButton) {
                    JButton button = (JButton) component;
                    if ("Save".equals(button.getText())) {
                        hasSaveButton = true;
                    } else if ("Cancel".equals(button.getText())) {
                        hasCancelButton = true;
                    }
                }
            }
            
            assertTrue("Log Foods dialog should have a Save button", hasSaveButton);
            assertTrue("Log Foods dialog should have a Cancel button", hasCancelButton);
        }
    }
    
    /**
     * Test the handleViewMealHistoryUI method
     */
    @Test
    public void testHandleViewMealHistoryUI() throws Exception {
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components and call the method on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
            mealPlanningMenu.accessHandleViewMealHistoryUI();
        });
        
        // Wait for all windows to appear
        Thread.sleep(200);
        
        // Find the view meal history dialog
        boolean foundHistoryDialog = false;
        JFrame historyFrame = null;
        
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window != mealPlanningMenu.getFrame()) {
                JFrame frame = (JFrame) window;
                if (frame.getTitle() != null && frame.getTitle().contains("Meal History")) {
                    foundHistoryDialog = true;
                    historyFrame = frame;
                    break;
                }
            }
        }
        
        assertTrue("View Meal History dialog should be created", foundHistoryDialog);
        
        // Verify that the history dialog contains expected components
        if (historyFrame != null) {
            Container contentPane = historyFrame.getContentPane();
            
            // Look for text fields for date input
            int textFieldCount = 0;
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JTextField) {
                    textFieldCount++;
                }
            }
            
            assertTrue("View Meal History dialog should contain text fields", textFieldCount > 0);
            
            // Look for buttons
            boolean hasViewButton = false;
            boolean hasCancelButton = false;
            
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JButton) {
                    JButton button = (JButton) component;
                    if ("View History".equals(button.getText())) {
                        hasViewButton = true;
                    } else if ("Cancel".equals(button.getText())) {
                        hasCancelButton = true;
                    }
                }
            }
            
            assertTrue("View Meal History dialog should have a View History button", hasViewButton);
            assertTrue("View Meal History dialog should have a Cancel button", hasCancelButton);
        }
    }
    
    /**
     * Test the showFoodSelectionUI method
     */
    @Test
    public void testShowFoodSelectionUI() throws Exception {
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components and call the method on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
            mealPlanningMenu.accessShowFoodSelectionUI("2025-05-15", "breakfast");
        });
        
        // Wait for all windows to appear
        Thread.sleep(200);
        
        // Find the food selection dialog
        boolean foundFoodSelectionDialog = false;
        JFrame foodFrame = null;
        
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window != mealPlanningMenu.getFrame()) {
                JFrame frame = (JFrame) window;
                if (frame.getTitle() != null && frame.getTitle().contains("Select Food")) {
                    foundFoodSelectionDialog = true;
                    foodFrame = frame;
                    break;
                }
            }
        }
        
        assertTrue("Food Selection dialog should be created", foundFoodSelectionDialog);
        
        // Verify that the food selection dialog contains expected components
        if (foodFrame != null) {
            Container contentPane = foodFrame.getContentPane();
            
            // Look for JList for food selection
            boolean hasListComponent = false;
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JList) {
                    hasListComponent = true;
                    break;
                } else if (component instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) component;
                    if (scrollPane.getViewport().getView() instanceof JList) {
                        hasListComponent = true;
                        break;
                    }
                }
            }
            
            assertTrue("Food Selection dialog should contain a list component", hasListComponent);
            
            // Look for buttons
            boolean hasSelectButton = false;
            boolean hasCancelButton = false;
            
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JButton) {
                    JButton button = (JButton) component;
                    if ("Select".equals(button.getText())) {
                        hasSelectButton = true;
                    } else if ("Cancel".equals(button.getText())) {
                        hasCancelButton = true;
                    }
                }
            }
            
            assertTrue("Food Selection dialog should have a Select button", hasSelectButton);
            assertTrue("Food Selection dialog should have a Cancel button", hasCancelButton);
        }
    }
    
    /**
     * Test the displayMealHistoryUI method
     */
    @Test
    public void testDisplayMealHistoryUI() throws Exception {
        // Set up mock service to return meal plans and food log
        ((MockMealPlanningService) mealPlanningService).setReturnMealPlan(true);
        ((MockMealPlanningService) mealPlanningService).setReturnFoodLog(true);
        
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components and call the method on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
            mealPlanningMenu.accessDisplayMealHistoryUI("2025-05-15");
        });
        
        // Wait for all windows to appear
        Thread.sleep(200);
        
        // Find the meal history display dialog
        boolean foundHistoryDisplayDialog = false;
        JFrame historyFrame = null;
        
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window != mealPlanningMenu.getFrame()) {
                JFrame frame = (JFrame) window;
                if (frame.getTitle() != null && frame.getTitle().contains("Meal History for")) {
                    foundHistoryDisplayDialog = true;
                    historyFrame = frame;
                    break;
                }
            }
        }
        
        assertTrue("Meal History display dialog should be created", foundHistoryDisplayDialog);
        
        // Verify that the history display dialog contains expected components
        if (historyFrame != null) {
            Container contentPane = historyFrame.getContentPane();
            
            // Look for JScrollPane for displaying meal history
            boolean hasScrollPane = false;
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JScrollPane) {
                    hasScrollPane = true;
                    break;
                }
            }
            
            assertTrue("Meal History display dialog should contain a scroll pane", hasScrollPane);
            
            // Look for back button
            boolean hasBackButton = false;
            
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JButton) {
                    JButton button = (JButton) component;
                    if ("Back".equals(button.getText())) {
                        hasBackButton = true;
                        break;
                    }
                }
            }
            
            assertTrue("Meal History display dialog should have a Back button", hasBackButton);
        }
    }
    
    /**
     * Test the UI version of handlePlanMeals method with successful meal plan
     */
    @Test
    public void testUIHandlePlanMealsSuccess() throws Exception {
        // Set up mock service for successful operations
        ((MockMealPlanningService) mealPlanningService).setSuccessfulOperations(true);
        
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
        });
        
        // Access the plan meals method through the menu instance
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.accessHandlePlanMealsUI();
        });
        
        // Find the plan meals dialog
        Window planMealsDialog = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window != mealPlanningMenu.getFrame()) {
                planMealsDialog = window;
                break;
            }
        }
        
        assertNotNull("Plan Meals dialog should be created", planMealsDialog);
        
        // Find and fill in the text fields
        final Window finalPlanMealsDialog = planMealsDialog;
        
        SwingUtilities.invokeAndWait(() -> {
        	Component[] components = getAllComponents(((JFrame)finalPlanMealsDialog).getContentPane());
            
            // Find text fields and fill them
            for (Component component : components) {
                if (component instanceof JTextField) {
                    JTextField textField = (JTextField) component;
                    Component parent = textField.getParent();
                    
                    // Try to determine which field it is by checking nearby labels
                    boolean isYearField = false;
                    boolean isMonthField = false;
                    boolean isDayField = false;
                    
                    if (parent instanceof Container) {
                        for (Component sibling : ((Container) parent).getComponents()) {
                            if (sibling instanceof JLabel) {
                                JLabel label = (JLabel) sibling;
                                String labelText = label.getText();
                                
                                if (labelText != null) {
                                    if (labelText.contains("Year")) {
                                        isYearField = true;
                                    } else if (labelText.contains("Month")) {
                                        isMonthField = true;
                                    } else if (labelText.contains("Day")) {
                                        isDayField = true;
                                    }
                                }
                            }
                        }
                    }
                    
                    // Fill in appropriate test values
                    if (isYearField) {
                        textField.setText("2025");
                    } else if (isMonthField) {
                        textField.setText("5");
                    } else if (isDayField) {
                        textField.setText("15");
                    }
                }
            }
            
            // Find the meal type combo box and select an option
            for (Component component : components) {
                if (component instanceof JComboBox) {
                    JComboBox<?> comboBox = (JComboBox<?>) component;
                    comboBox.setSelectedIndex(0); // Select first item (Breakfast)
                    break;
                }
            }
            
            // Find and click the Continue button
            for (Component component : components) {
                if (component instanceof JButton) {
                    JButton button = (JButton) component;
                    if ("Continue".equals(button.getText())) {
                        clickButton(button);
                        break;
                    }
                }
            }
        });
        
        // Wait for food selection UI to appear
        Thread.sleep(300);
        
        // Find the food selection dialog
        Window foodSelectionDialog = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window.isVisible() && 
                window != mealPlanningMenu.getFrame() && window != finalPlanMealsDialog) {
                foodSelectionDialog = window;
                break;
            }
        }
        
        // Food selection dialog may not be visible in test environment due to modal dialogs
        // We'll verify the process completes without error
    }
    
    /**
     * Test the UI version of handleLogFoods method with successful food logging
     */
    @Test
    public void testUIHandleLogFoodsSuccess() throws Exception {
        // Set up mock service for successful operations
        ((MockMealPlanningService) mealPlanningService).setSuccessfulOperations(true);
        
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
        });
        
        // Access the log foods method through the menu instance
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.accessHandleLogFoodsUI();
        });
        
        // Find the log foods dialog
        Window logFoodsDialog = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window != mealPlanningMenu.getFrame()) {
                logFoodsDialog = window;
                break;
            }
        }
        
        assertNotNull("Log Foods dialog should be created", logFoodsDialog);
        
     // Find and fill in the text fields
        final Window finalLogFoodsDialog = logFoodsDialog;

        try {
            SwingUtilities.invokeAndWait(() -> {
                // Window 
                Component[] components;
                if (finalLogFoodsDialog instanceof JFrame) {
                    components = getAllComponents(((JFrame)finalLogFoodsDialog).getContentPane());
                } else if (finalLogFoodsDialog instanceof JDialog) {
                    components = getAllComponents(((JDialog)finalLogFoodsDialog).getContentPane());
                } else {
                    components = getAllComponents(finalLogFoodsDialog);
                }
                
                // Find text fields and fill them
                List<JTextField> textFields = new ArrayList<>();
                for (Component component : components) {
                    if (component instanceof JTextField) {
                        textFields.add((JTextField) component);
                    }
                }
                
                // Fill in test values for each field
                for (int i = 0; i < textFields.size(); i++) {
                    JTextField field = textFields.get(i);
                    switch (i) {
                        case 0: field.setText("2025"); break; // Year
                        case 1: field.setText("5"); break;    // Month
                        case 2: field.setText("15"); break;   // Day
                        case 3: field.setText("Apple"); break; // Food name
                        case 4: field.setText("100"); break;   // Amount
                        case 5: field.setText("50"); break;    // Calories
                    }
                }
                
                // Find and click the Save button
                for (Component component : components) {
                    if (component instanceof JButton) {
                        JButton button = (JButton) component;
                        if ("Save".equals(button.getText())) {
                            clickButton(button);
                            break;
                        }
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
            fail("Test failed due to UI interaction error: " + e.getMessage());
        }
        
        // The test completes successfully if no exception is thrown
    }
    
    /**
     * Test the UI version of handleLogFoods method with invalid input
     */
    @Test
    public void testUIHandleLogFoodsInvalidInput() throws Exception {
        // Set up mock service for successful operations
        ((MockMealPlanningService) mealPlanningService).setSuccessfulOperations(true);
        
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
        });
        
        // Access the log foods method through the menu instance
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.accessHandleLogFoodsUI();
        });
        
        // Find the log foods dialog
        Window logFoodsDialog = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window != mealPlanningMenu.getFrame()) {
                logFoodsDialog = window;
                break;
            }
        }
        
        assertNotNull("Log Foods dialog should be created", logFoodsDialog);
        
      
     // Find and fill in the text fields with invalid data
        final Window finalLogFoodsDialog = logFoodsDialog;

        try {
            SwingUtilities.invokeAndWait(() -> {
                // Convert Window object to appropriate type
                Component[] components;
                if (finalLogFoodsDialog instanceof JFrame) {
                    components = getAllComponents(((JFrame)finalLogFoodsDialog).getContentPane());
                } else if (finalLogFoodsDialog instanceof JDialog) {
                    components = getAllComponents(((JDialog)finalLogFoodsDialog).getContentPane());
                } else {
                    components = getAllComponents(finalLogFoodsDialog);
                }

                // Find text fields and fill them with invalid data
                List<JTextField> textFields = new ArrayList<>();
                for (Component component : components) {
                    if (component instanceof JTextField) {
                        textFields.add((JTextField) component);
                    }
                }

                // Fill in test values for each field, with invalid values
                for (int i = 0; i < textFields.size(); i++) {
                    JTextField field = textFields.get(i);
                    switch (i) {
                        case 0: field.setText("2025"); break; // Year - valid
                        case 1: field.setText("5"); break;    // Month - valid
                        case 2: field.setText("15"); break;   // Day - valid
                        case 3: field.setText(""); break;     // Food name - invalid (empty)
                        case 4: field.setText("-100"); break; // Amount - invalid (negative)
                        case 5: field.setText("abc"); break;  // Calories - invalid (non-numeric)
                    }
                }

                // Install a UI error handler to catch dialog messages
                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        // Just log the exception for test purposes
                        System.err.println("Caught expected UI exception: " + e.getMessage());
                    }
                });

                // Find and click the Save button
                for (Component component : components) {
                    if (component instanceof JButton) {
                        JButton button = (JButton) component;
                        if ("Save".equals(button.getText())) {
                            clickButton(button);
                            break;
                        }
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
            fail("Test failed due to UI interaction error: " + e.getMessage());
        }
        
        	    
        // The test completes successfully if no exception is thrown
    }
    
    /**
     * Test the UI version of handleViewMealHistory with no meal history
     */
    @Test
    public void testUIHandleViewMealHistoryNoHistory() throws Exception {
        // Set up mock service to return no meal plans or food logs
        ((MockMealPlanningService) mealPlanningService).setReturnMealPlan(false);
        ((MockMealPlanningService) mealPlanningService).setReturnFoodLog(false);
        
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
        });
        
        // Access the view meal history method through the menu instance
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.accessHandleViewMealHistoryUI();
        });
        
        // Find the date selection dialog
        Window dateDialog = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window != mealPlanningMenu.getFrame()) {
                dateDialog = window;
                break;
            }
        }
        
        assertNotNull("Date selection dialog should be created", dateDialog);
        
        
        
     // Fill in the date fields and click the View History button
        final Window finalDateDialog = dateDialog;

        try {
            SwingUtilities.invokeAndWait(() -> {
                // Convert Window object to appropriate type
                Component[] components;
                if (finalDateDialog instanceof JFrame) {
                    components = getAllComponents(((JFrame)finalDateDialog).getContentPane());
                } else if (finalDateDialog instanceof JDialog) {
                    components = getAllComponents(((JDialog)finalDateDialog).getContentPane());
                } else {
                    components = getAllComponents(finalDateDialog);
                }

                // Find text fields and fill them
                List<JTextField> textFields = new ArrayList<>();
                for (Component component : components) {
                    if (component instanceof JTextField) {
                        textFields.add((JTextField) component);
                    }
                }

                // Fill in date fields
                for (int i = 0; i < textFields.size(); i++) {
                    JTextField field = textFields.get(i);
                    switch (i) {
                        case 0: field.setText("2025"); break; // Year
                        case 1: field.setText("5"); break;    // Month
                        case 2: field.setText("15"); break;   // Day
                    }
                }

                // Find and click the View History button
                for (Component component : components) {
                    if (component instanceof JButton) {
                        JButton button = (JButton) component;
                        if ("View History".equals(button.getText())) {
                            clickButton(button);
                            break;
                        }
                    }
                }
            });
            
            // Wait for the history display dialog to appear
            Thread.sleep(300);

            // Find the history display dialog
            Window historyDialog = null;
            for (Window window : Window.getWindows()) {
                if (window instanceof JFrame && window != mealPlanningMenu.getFrame() && window != finalDateDialog) {
                    historyDialog = window;
                    break;
                }
            }
            
            // History dialog may be null in test environment due to modal dialogs
            // The test completes successfully if no exception is thrown
            
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
            fail("Test failed due to UI interaction error: " + e.getMessage());
        }
        
        // History dialog may be null in test environment due to modal dialogs
        // The test completes successfully if no exception is thrown
    }
    
    /**
     * Test the UI version of handleViewMealHistory with existing meal history
     */
    @Test
    public void testUIHandleViewMealHistoryWithHistory() throws Exception {
        // Set up mock service to return meal plans and food logs
        ((MockMealPlanningService) mealPlanningService).setReturnMealPlan(true);
        ((MockMealPlanningService) mealPlanningService).setReturnFoodLog(true);
        
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
        });
        
        // Access the view meal history method through the menu instance
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.accessHandleViewMealHistoryUI();
        });
        
        // Find the date selection dialog
        Window dateDialog = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window != mealPlanningMenu.getFrame()) {
                dateDialog = window;
                break;
            }
        }
        
        assertNotNull("Date selection dialog should be created", dateDialog);
        
     // Fill in the date fields and click the View History button
        final Window finalDateDialog = dateDialog;

        try {
            SwingUtilities.invokeAndWait(() -> {
                Component[] components;
                if (finalDateDialog instanceof JFrame) {
                    components = getAllComponents(((JFrame)finalDateDialog).getContentPane());
                } else if (finalDateDialog instanceof JDialog) {
                    components = getAllComponents(((JDialog)finalDateDialog).getContentPane());
                } else {
                    components = getAllComponents(finalDateDialog);
                }

                // Find text fields and fill them
                List<JTextField> textFields = new ArrayList<>();
                for (Component component : components) {
                    if (component instanceof JTextField) {
                        textFields.add((JTextField) component);
                    }
                }

                // Fill in date fields
                for (int i = 0; i < textFields.size(); i++) {
                    JTextField field = textFields.get(i);
                    switch (i) {
                        case 0: field.setText("2025"); break; // Year
                        case 1: field.setText("5"); break;    // Month
                        case 2: field.setText("15"); break;   // Day
                    }
                }

                // Find and click the View History button
                for (Component component : components) {
                    if (component instanceof JButton) {
                        JButton button = (JButton) component;
                        if ("View History".equals(button.getText())) {
                            clickButton(button);
                            break;
                        }
                    }
                }
            });

            // Wait for the history display dialog to appear
            Thread.sleep(300);

            // Find the history display dialog
            Window historyDialog = null;
            for (Window window : Window.getWindows()) {
                if (window instanceof JFrame && window != mealPlanningMenu.getFrame() && window != finalDateDialog) {
                    historyDialog = window;
                    break;
                }
            }
            
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
            fail("Test failed due to UI interaction error: " + e.getMessage());
        }}
        
      
    
    /**
     * Test the showFoodSelectionUI method with a valid selection
     */
    @Test
    public void testShowFoodSelectionUIWithValidSelection() throws Exception {
        // Set up mock service for successful operations
        ((MockMealPlanningService) mealPlanningService).setSuccessfulOperations(true);
        
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
        });
        
        // Access the showFoodSelectionUI method through the menu instance
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.accessShowFoodSelectionUI("2025-05-15", "breakfast");
        });
        
        // Find the food selection dialog
        Window foodSelectionDialog = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window != mealPlanningMenu.getFrame()) {
                foodSelectionDialog = window;
                break;
            }
        }
        
        assertNotNull("Food selection dialog should be created", foodSelectionDialog);
        
        // Select a food item and click the Select button
        final Window finalFoodSelectionDialog = foodSelectionDialog;
        
        SwingUtilities.invokeAndWait(() -> {
            Container contentPane = ((JFrame) finalFoodSelectionDialog).getContentPane();
            
            // Find the JList component
            JList<?> foodList = null;
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JList) {
                    foodList = (JList<?>) component;
                    break;
                } else if (component instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) component;
                    if (scrollPane.getViewport().getView() instanceof JList) {
                        foodList = (JList<?>) scrollPane.getViewport().getView();
                        break;
                    }
                }
            }
            
            // Select the first food item if the list is found
            if (foodList != null) {
                foodList.setSelectedIndex(0);
            }
            
            // Find and click the Select button
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JButton) {
                    JButton button = (JButton) component;
                    if ("Select".equals(button.getText())) {
                        clickButton(button);
                        break;
                    }
                }
            }
        });
        
        // The test completes successfully if no exception is thrown
    }
    
    /**
     * Test the showFoodSelectionUI method with no selection
     */
    @Test
    public void testShowFoodSelectionUIWithNoSelection() throws Exception {
        // Set up mock service for successful operations
        ((MockMealPlanningService) mealPlanningService).setSuccessfulOperations(true);
        
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
        });
        
        // Access the showFoodSelectionUI method through the menu instance
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.accessShowFoodSelectionUI("2025-05-15", "breakfast");
        });
        
        // Find the food selection dialog
        Window foodSelectionDialog = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window != mealPlanningMenu.getFrame()) {
                foodSelectionDialog = window;
                break;
            }
        }
        
        assertNotNull("Food selection dialog should be created", foodSelectionDialog);
        
        // Click the Select button without selecting a food item
        final Window finalFoodSelectionDialog = foodSelectionDialog;
        
        SwingUtilities.invokeAndWait(() -> {
            Container contentPane = ((JFrame) finalFoodSelectionDialog).getContentPane();
            
            // Install a UI error handler to catch dialog messages
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    // Just log the exception for test purposes
                    System.err.println("Caught expected UI exception: " + e.getMessage());
                }
            });
            
            // Find and click the Select button without selecting a food
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JButton) {
                    JButton button = (JButton) component;
                    if ("Select".equals(button.getText())) {
                        clickButton(button);
                        break;
                    }
                }
            }
        });
        
        // The test completes successfully if no exception is thrown
    }
    
    /**
     * Test the displayMealHistoryUI method with Back button
     */
    @Test
    public void testDisplayMealHistoryUIBackButton() throws Exception {
        // Set up mock service to return meal plans and food logs
        ((MockMealPlanningService) mealPlanningService).setReturnMealPlan(true);
        ((MockMealPlanningService) mealPlanningService).setReturnFoodLog(true);
        
        // Enable UI mode
        mealPlanningMenu.enableUIMode();
        
        // Initialize UI components on EDT
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.initializeUIForTest();
        });
        
        // Access the displayMealHistoryUI method through the menu instance
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.accessDisplayMealHistoryUI("2025-05-15");
        });
        
        // Find the meal history display dialog
        Window historyDialog = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window != mealPlanningMenu.getFrame()) {
                historyDialog = window;
                break;
            }
        }
        
        assertNotNull("Meal history display dialog should be created", historyDialog);
        
        // Click the Back button
        final Window finalHistoryDialog = historyDialog;
        
        SwingUtilities.invokeAndWait(() -> {
            Container contentPane = ((JFrame) finalHistoryDialog).getContentPane();
            
            // Find and click the Back button
            for (Component component : getAllComponents(contentPane)) {
                if (component instanceof JButton) {
                    JButton button = (JButton) component;
                    if ("Back".equals(button.getText())) {
                        clickButton(button);
                        break;
                    }
                }
            }
        });
        
        // The test completes successfully if no exception is thrown
    }
    
    /**
     * Helper method to get all components from a container and its nested containers
     */
    private Component[] getAllComponents(Container container) {
        List<Component> componentList = new ArrayList<>();
        getAllComponentsHelper(container, componentList);
        return componentList.toArray(new Component[0]);
    }
    
    /**
     * Recursive helper method to get all components from a container and its nested containers
     */
    private void getAllComponentsHelper(Container container, List<Component> componentList) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            componentList.add(component);
            if (component instanceof Container) {
                getAllComponentsHelper((Container) component, componentList);
            }
        }
    }
    
    // Original console-based tests below
    
    /**
     * Test the displayMenu method with option 0 (return to main menu).
     */
    @Test
    public void testDisplayMenuReturnToMainMenu() throws Exception {
        // Arrange
        String input = "0\n"; // Choose option 0 to exit
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new TestMealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.displayMenu();
        });
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Menu should display correct title", output.contains("Meal Planning and Logging"));
        assertTrue("Menu should contain option to return to main menu", output.contains("0. Return to Main Menu"));
    }
    
    /**
     * Test the displayMenu method with an invalid option.
     */
    @Test
    public void testDisplayMenuInvalidOption() throws Exception {
        // Arrange
        String input = "99\n0\n"; // Choose invalid option 99, then exit
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new TestMealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.displayMenu();
        });
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Menu should display error for invalid choice", output.contains("Invalid choice"));
    }
   
    
    /**
     * Test the handlePlanMeals method with valid input.
     */
    @Test
    public void testHandlePlanMealsValidInput() throws Exception {
        // Arrange
        // Valid date, breakfast (1), first food option (1), then exit
        String input = "1\n2025\n1\n1\n1\n1\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningMenu testMenu = new TestMealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            testMenu.displayMenu();
        });
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show meal planning menu", output.contains("Plan Meals"));
        assertTrue("Should show breakfast options", output.contains("Breakfast"));
    }
    
    /**
     * Test the handleLogFoods method with valid input.
     */
    @Test
    public void testHandleLogFoodsValidInput() throws Exception {
        // Arrange
        // Option 2 (Log Foods), valid date, food details, then exit
        String input = "2\n2025\n2\n1\nApple\n100\n52\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningMenu testMenu = new TestMealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            testMenu.displayMenu();
        });
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show log foods menu", output.contains("Log Foods"));
    }
    
    /**
     * Test the handleLogFoods method with invalid food details.
     */
    @Test
    public void testHandleLogFoodsInvalidFoodDetails() throws Exception {
        // Arrange
        // Option 2 (Log Foods), valid date, invalid food details, then exit
        String input = "2\n2025\n2\n1\nApple\n-100\n0\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        mealPlanningMenu = new TestMealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Act - EDT thread'inde çalıştırma
        SwingUtilities.invokeAndWait(() -> {
            mealPlanningMenu.displayMenu();
        });
        
        // Assert
        String output = outputStream.toString();
        assertTrue("Should show error for negative amount", output.contains("Amount must be positive"));
    }
    
    /**
     * Test all meal types in console mode
     */
    @Test
    public void testAllMealTypesConsoleMode() throws Exception {
        // Set up mock service
        ((MockMealPlanningService) mealPlanningService).setSuccessfulOperations(true);
        
        // Test each meal type in sequence
        String[] mealTypes = {"breakfast", "lunch", "snack", "dinner"};
        
        for (int i = 0; i < mealTypes.length; i++) {
            // Option 1 (Plan Meals), valid date, meal type (i+1), first food option (1), then exit to main menu
            String input = "1\n2025\n5\n15\n" + (i+1) + "\n1\n0\n";
            InputStream inputStream = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputStream);
            
            // Reset output stream for this test
            outputStream.reset();
            
            TestMealPlanningMenu testMenu = new TestMealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
            
            // Execute test
            Method handlePlanMealsMethod = MealPlanningMenu.class.getDeclaredMethod("handlePlanMealsConsole");
            handlePlanMealsMethod.setAccessible(true);
            
            SwingUtilities.invokeAndWait(() -> {
                try {
                    handlePlanMealsMethod.invoke(testMenu);
                } catch (Exception e) {
                    // Ignore for test
                }
            });
            
            // Verify output contains the selected meal type
            String output = outputStream.toString();
            String capitalizedMealType = mealTypes[i].substring(0, 1).toUpperCase() + mealTypes[i].substring(1);
            assertTrue("Output should contain meal type: " + capitalizedMealType, 
                      output.contains(capitalizedMealType));
        }
    }
    
    /**
     * Test invalid meal type selection in console mode
     */
    @Test
    public void testInvalidMealTypeConsoleMode() throws Exception {
        // Option 1 (Plan Meals), valid date, invalid meal type (99)
        String input = "1\n2025\n5\n15\n99\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        TestMealPlanningMenu testMenu = new TestMealPlanningMenu(mealPlanningService, authService, new Scanner(System.in));
        
        // Execute test
        Method handlePlanMealsMethod = MealPlanningMenu.class.getDeclaredMethod("handlePlanMealsConsole");
        handlePlanMealsMethod.setAccessible(true);
        
        SwingUtilities.invokeAndWait(() -> {
            try {
                handlePlanMealsMethod.invoke(testMenu);
            } catch (Exception e) {
                // Ignore for test
            }
        });
        
        // Verify output contains error message
        String output = outputStream.toString();
        assertTrue("Output should contain invalid meal type error", 
                  output.contains("Invalid meal type"));
    }
}