/**
 * @file MealPlanningPanel.java
 * @brief GUI panel for meal planning functionality
 * @author Berkant Kagan Haluk Irem
 * @date 2024
 */

package com.berkant.kagan.haluk.irem.dietapp;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * @class MealPlanningPanel
 * @brief A Swing panel that provides a user interface for meal planning
 * 
 * This class extends JPanel to create a graphical interface for managing meal plans.
 * It allows users to add, view, and delete meals, and displays a weekly meal plan.
 */
public class MealPlanningPanel extends JPanel {
    /** @brief Service for handling meal planning operations */
    private MealPlanningService planningService;
    
    /** @brief Text field for entering meal name */
    private JTextField nameField;
    
    /** @brief Text field for entering calorie count */
    private JTextField caloriesField;
    
    /** @brief Text field for entering protein content */
    private JTextField proteinField;
    
    /** @brief Text field for entering carbohydrate content */
    private JTextField carbsField;
    
    /** @brief Text field for entering fat content */
    private JTextField fatField;
    
    /** @brief Text area for entering meal ingredients */
    private JTextArea ingredientsArea;
    
    /** @brief Combo box for selecting the day of the week */
    private JComboBox<String> dayComboBox;
    
    /** @brief Combo box for selecting the meal type */
    private JComboBox<String> mealTypeComboBox;
    
    /** @brief Text area for displaying the weekly meal plan */
    private JTextArea weeklyPlanArea;
    
    /** @brief Button for adding a new meal */
    private JButton addButton;
    
    /** @brief Button for viewing the weekly plan */
    private JButton viewButton;
    
    /** @brief Button for deleting a meal */
    private JButton deleteButton;
    
    /**
     * @brief Constructs a new MealPlanningPanel
     * @param planningService The service to handle meal planning operations
     */
    public MealPlanningPanel(MealPlanningService planningService) {
        this.planningService = planningService;
        initializeComponents();
        setupLayout();
        addListeners();
    }
    
    /**
     * @brief Initializes all UI components
     * 
     * Creates and configures all text fields, combo boxes, text areas,
     * and buttons used in the panel.
     */
    private void initializeComponents() {
        nameField = new JTextField(20);
        caloriesField = new JTextField(10);
        proteinField = new JTextField(10);
        carbsField = new JTextField(10);
        fatField = new JTextField(10);
        ingredientsArea = new JTextArea(3, 20);
        ingredientsArea.setLineWrap(true);
        ingredientsArea.setWrapStyleWord(true);
        
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        dayComboBox = new JComboBox<>(days);
        
        String[] mealTypes = {"Breakfast", "Lunch", "Snack", "Dinner"};
        mealTypeComboBox = new JComboBox<>(mealTypes);
        
        weeklyPlanArea = new JTextArea(20, 40);
        weeklyPlanArea.setEditable(false);
        
        addButton = new JButton("Add Meal");
        viewButton = new JButton("View Weekly Plan");
        deleteButton = new JButton("Delete Meal");
    }
    
    /**
     * @brief Sets up the layout of the panel
     * 
     * Arranges all components in a grid layout with proper spacing and alignment.
     * Creates separate panels for input fields, buttons, and the weekly plan display.
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Row 0
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Day:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(dayComboBox, gbc);
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Meal Type:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(mealTypeComboBox, gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Food Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(nameField, gbc);
        
        // Row 3
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Calories:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(caloriesField, gbc);
        
        // Row 4
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Protein (g):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(proteinField, gbc);
        
        // Row 5
        gbc.gridx = 0; gbc.gridy = 5;
        inputPanel.add(new JLabel("Carbs (g):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(carbsField, gbc);
        
        // Row 6
        gbc.gridx = 0; gbc.gridy = 6;
        inputPanel.add(new JLabel("Fat (g):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(fatField, gbc);
        
        // Row 7
        gbc.gridx = 0; gbc.gridy = 7;
        inputPanel.add(new JLabel("Ingredients:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(new JScrollPane(ingredientsArea), gbc);
        
        // Buttons Panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(addButton);
        buttonsPanel.add(viewButton);
        buttonsPanel.add(deleteButton);
        
        // Weekly Plan Panel
        JPanel weeklyPlanPanel = new JPanel(new BorderLayout());
        weeklyPlanPanel.setBorder(BorderFactory.createTitledBorder("Weekly Meal Plan"));
        weeklyPlanPanel.add(new JScrollPane(weeklyPlanArea), BorderLayout.CENTER);
        
        // Add all panels to main panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(inputPanel, BorderLayout.CENTER);
        leftPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        add(leftPanel, BorderLayout.WEST);
        add(weeklyPlanPanel, BorderLayout.CENTER);
    }
    
    /**
     * @brief Adds action listeners to all buttons
     * 
     * Configures the behavior of the Add, View, and Delete buttons.
     * Handles user input validation and error cases.
     */
    private void addListeners() {
        // Add Meal Button
        addButton.addActionListener(e -> {
            try {
                String day = (String) dayComboBox.getSelectedItem();
                String mealType = (String) mealTypeComboBox.getSelectedItem();
                String name = nameField.getText().trim();
                int calories = Integer.parseInt(caloriesField.getText().trim());
                double protein = Double.parseDouble(proteinField.getText().trim());
                double carbs = Double.parseDouble(carbsField.getText().trim());
                double fat = Double.parseDouble(fatField.getText().trim());
                String ingredients = ingredientsArea.getText().trim();
                
                if (name.isEmpty() || ingredients.isEmpty()) {
                    throw new IllegalArgumentException("Please fill in all fields");
                }
                
                int userId = 1; // TODO: Aktif kullanıcıdan alınabilir
                planningService.addMeal(userId, day, mealType, name, calories, protein, carbs, fat, ingredients);
                clearFields();
                updateWeeklyPlan();
                JOptionPane.showMessageDialog(this, "Meal added successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for calories and nutrients");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        
        // View Weekly Plan Button
        viewButton.addActionListener(e -> {
            updateWeeklyPlan();
        });
        
        // Delete Meal Button
        deleteButton.addActionListener(e -> {
            try {
                String day = (String) dayComboBox.getSelectedItem();
                String mealType = (String) mealTypeComboBox.getSelectedItem();
                
                planningService.deleteMeal(day, mealType);
                updateWeeklyPlan();
                JOptionPane.showMessageDialog(this, "Meal deleted successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
    }
    
    /**
     * @brief Clears all input fields
     * 
     * Resets the text in all text fields and text areas to empty strings.
     */
    private void clearFields() {
        nameField.setText("");
        caloriesField.setText("");
        proteinField.setText("");
        carbsField.setText("");
        fatField.setText("");
        ingredientsArea.setText("");
    }
    
    /**
     * @brief Updates the weekly meal plan display
     * 
     * Retrieves the current weekly meal plan from the service and
     * updates the display area. Handles any errors that occur during
     * the update process.
     */
    private void updateWeeklyPlan() {
        try {
            String weeklyPlan = planningService.getWeeklyMealPlan();
            weeklyPlanArea.setText(weeklyPlan);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error retrieving weekly plan: " + e.getMessage());
        }
    }
}