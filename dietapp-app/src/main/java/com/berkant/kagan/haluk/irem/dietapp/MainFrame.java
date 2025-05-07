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

public class MainFrame extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private CalorieTrackingPanel calorieTrackingPanel;
    private MealPlanningPanel mealPlanningPanel;
    private PersonalizedDietPanel personalizedDietPanel;
    private ShoppingListPanel shoppingListPanel;
    private UserAuthenticationPanel authPanel;
    private JPanel buttonPanel;

    public MainFrame() {
        setTitle("Diet Planner");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
 
        // Initialize database
        DatabaseHelper.initializeDatabase();
//
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
//
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

    public void showMainMenu() {
        cardLayout.show(mainPanel, "calorie");
        buttonPanel.setVisible(true);
    }

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
