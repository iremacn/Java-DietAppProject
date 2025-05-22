/**
 * @file MainFrame.java
 * @brief Main graphical user interface frame for the Diet Planner application
 * 
 * @details The MainFrame class serves as the primary window for the Diet Planner application.
 *          It manages the application's main interface, including navigation between
 *          different functional panels and user authentication.
 * 
 * @author berkant
 * @version 1.0
 * @date 2024
 * @copyright Diet Planner Application
 */
package com.berkant.kagan.haluk.irem.dietapp;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @class MainFrame
 * @brief Main window class for the Diet Planner application
 * 
 * @details This class extends JFrame to create the main application window.
 *          It manages multiple functional panels using CardLayout for navigation
 *          and provides a consistent user interface across the application.
 *          The class handles:
 *          - User authentication
 *          - Calorie tracking
 *          - Meal planning
 *          - Personalized diet recommendations
 *          - Shopping list management
 */
public class MainFrame extends JFrame {
    /** @brief Main panel containing all functional panels */
    private JPanel mainPanel;
    /** @brief Layout manager for switching between panels */
    private CardLayout cardLayout;
    /** @brief Panel for tracking calories and nutrients */
    private CalorieTrackingPanel calorieTrackingPanel;
    /** @brief Panel for meal planning functionality */
    private MealPlanningPanel mealPlanningPanel;
    /** @brief Panel for personalized diet recommendations */
    private PersonalizedDietPanel personalizedDietPanel;
    /** @brief Panel for shopping list management */
    private ShoppingListPanel shoppingListPanel;
    /** @brief Panel for user authentication */
    private UserAuthenticationPanel authPanel;
    /** @brief Panel containing navigation buttons */
    private JPanel buttonPanel;

    /**
     * @brief Constructs the main application window
     * @details Initializes the main window with all necessary components:
     *          - Sets up the window properties
     *          - Initializes the database
     *          - Creates service objects
     *          - Initializes all functional panels
     *          - Sets up navigation buttons
     *          - Configures the layout
     */
    public MainFrame() {
        setTitle("Diet Planner");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
 
        // Initialize database
        DatabaseHelper.initializeDatabase();

        // Main panel with CardLayout
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);

        // Create service objects
        AuthenticationService authService = new AuthenticationService();
        MealPlanningService mealService = new MealPlanningService(DatabaseHelper.getConnection());
        CalorieNutrientTrackingService calorieService = new CalorieNutrientTrackingService(mealService);
        PersonalizedDietRecommendationService dietService = new PersonalizedDietRecommendationService(calorieService, mealService);
        ShoppingListService shoppingService = new ShoppingListService(mealService);
	
        // Create panels
        authPanel = new UserAuthenticationPanel(authService);
        authPanel.setLoginSuccessCallback(this::showMainMenu);

        calorieTrackingPanel = new CalorieTrackingPanel(calorieService);
        mealPlanningPanel = new MealPlanningPanel(mealService);
        personalizedDietPanel = new PersonalizedDietPanel(dietService);
        shoppingListPanel = new ShoppingListPanel(shoppingService);

        // Create navigation buttons
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(236, 240, 241));

        JButton calorieButton = createNavButton("Calorie Tracking");
        JButton mealButton = createNavButton("Meal Planning");
        JButton dietButton = createNavButton("Personalized Diet");
        JButton shoppingButton = createNavButton("Shopping List");

        buttonPanel.add(calorieButton);
        buttonPanel.add(mealButton);
        buttonPanel.add(dietButton);
        buttonPanel.add(shoppingButton);

        // Add panels to CardLayout
        mainPanel.add(authPanel, "login");
        mainPanel.add(calorieTrackingPanel, "calorie");
        mainPanel.add(mealPlanningPanel, "meal");
        mainPanel.add(personalizedDietPanel, "diet");
        mainPanel.add(shoppingListPanel, "shopping");

        // Add button panel to frame
        add(buttonPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Show login screen initially
        cardLayout.show(mainPanel, "login");
        buttonPanel.setVisible(false);
    }

    /**
     * @brief Creates a navigation button with consistent styling
     * @details Creates and configures a JButton with:
     *          - Standard size and styling
     *          - Custom colors and font
     *          - Action listener for panel switching
     * 
     * @param text The text to display on the button
     * @return A configured JButton instance
     */
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 40));
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));

        button.addActionListener(e -> {
            switch (text) {
                case "Calorie Tracking":
                    cardLayout.show(mainPanel, "calorie");
                    break;
                case "Meal Planning":
                    cardLayout.show(mainPanel, "meal");
                    break;
                case "Personalized Diet":
                    cardLayout.show(mainPanel, "diet");
                    break;
                case "Shopping List":
                    cardLayout.show(mainPanel, "shopping");
                    break;
            }
        });

        return button;
    }

    /**
     * @brief Shows the main application menu
     * @details Displays the main application interface after successful login:
     *          - Shows the calorie tracking panel
     *          - Makes the navigation buttons visible
     */
    public void showMainMenu() {
        cardLayout.show(mainPanel, "calorie");
        buttonPanel.setVisible(true);
    }

    /**
     * @brief Main entry point for the application
     * @details Initializes and displays the main application window:
     *          - Loads the SQLite JDBC driver
     *          - Creates and shows the main frame
     *          - Handles any initialization errors
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            SwingUtilities.invokeLater(() -> {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            });
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, 
                "SQLite JDBC driver not found: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
