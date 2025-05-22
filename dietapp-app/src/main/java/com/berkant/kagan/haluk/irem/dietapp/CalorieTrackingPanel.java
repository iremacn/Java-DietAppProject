/**
 * @file CalorieTrackingPanel.java
 * @brief GUI panel for managing calorie and nutrient tracking
 * 
 * @details The CalorieTrackingPanel class provides a graphical user interface for
 *          managing food entries and their nutritional information. It allows users
 *          to add, view, and delete food entries with their associated calorie and
 *          macronutrient values.
 * 
 * @author irem
 * @version 1.0
 * @date 2024
 * @copyright Diet Planner Application
 */
package com.berkant.kagan.haluk.irem.dietapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @class CalorieTrackingPanel
 * @brief GUI panel for calorie and nutrient tracking operations
 * 
 * @details This class implements a Swing-based GUI panel that provides functionality
 *          for managing food entries and their nutritional information. The panel
 *          includes input fields for food name, calories, and macronutrients, along
 *          with buttons for adding, viewing, and deleting entries.
 */
public class CalorieTrackingPanel extends JPanel {
    /** @brief Service for handling calorie and nutrient tracking operations */
    private CalorieNutrientTrackingService trackingService;
    /** @brief Text field for entering food name */
    private JTextField foodNameField;
    /** @brief Text field for entering calorie value */
    private JTextField caloriesField;
    /** @brief Text field for entering protein value */
    private JTextField proteinField;
    /** @brief Text field for entering carbohydrate value */
    private JTextField carbsField;
    /** @brief Text field for entering fat value */
    private JTextField fatField;
    /** @brief Text area for displaying food entries */
    private JTextArea resultArea;
    /** @brief Button for adding new food entries */
    private JButton addButton;
    /** @brief Button for viewing existing food entries */
    private JButton viewButton;
    /** @brief Button for deleting food entries */
    private JButton deleteButton;

    /**
     * @brief Constructor for CalorieTrackingPanel
     * @details Initializes the panel with the required tracking service and
     *          sets up the GUI components and event listeners.
     * 
     * @param trackingService Service for handling calorie and nutrient tracking operations
     */
    public CalorieTrackingPanel(CalorieNutrientTrackingService trackingService) {
        this.trackingService = trackingService;
        initializeComponents();
        setupLayout();
        setupListeners();
    }

    /**
     * @brief Initializes the GUI components
     * @details Creates and configures all the necessary Swing components
     *          including text fields, buttons, and the result area.
     */
    private void initializeComponents() {
        foodNameField = new JTextField(20);
        caloriesField = new JTextField(10);
        proteinField = new JTextField(10);
        carbsField = new JTextField(10);
        fatField = new JTextField(10);
        resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false);
        addButton = new JButton("Add");
        viewButton = new JButton("View");
        deleteButton = new JButton("Delete");
    }

    /**
     * @brief Sets up the panel layout
     * @details Arranges the GUI components using BorderLayout and GridBagLayout.
     *          Creates input fields for food information and organizes buttons
     *          in a separate panel.
     */
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Food Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(foodNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Calories:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(caloriesField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Protein (g):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(proteinField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Carbs (g):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(carbsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(new JLabel("Fat (g):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(fatField, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(deleteButton);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * @brief Sets up event listeners for the buttons
     * @details Configures action listeners for add, view, and delete buttons.
     *          Handles user input validation and error cases.
     * 
     * @throws NumberFormatException if numeric input fields contain invalid values
     * @throws SQLException if there is an error accessing the database
     */
    private void setupListeners() {
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String foodName = foodNameField.getText();
                    int calories = Integer.parseInt(caloriesField.getText());
                    double protein = Double.parseDouble(proteinField.getText());
                    double carbs = Double.parseDouble(carbsField.getText());
                    double fat = Double.parseDouble(fatField.getText());

                    trackingService.addFoodEntry(foodName, calories, protein, carbs, fat);
                    JOptionPane.showMessageDialog(CalorieTrackingPanel.this, "Food added successfully!");
                    clearFields();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(CalorieTrackingPanel.this, "Please enter valid numeric values!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CalorieTrackingPanel.this, "Error occurred: " + ex.getMessage());
                }
            }
        });

        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    List<String> entries = trackingService.viewFoodEntries();
                    StringBuilder sb = new StringBuilder();
                    for (String entry : entries) {
                        sb.append(entry).append("\n");
                    }
                    resultArea.setText(sb.toString());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CalorieTrackingPanel.this, "Error occurred: " + ex.getMessage());
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String foodName = foodNameField.getText();
                    trackingService.deleteFoodEntry(foodName);
                    JOptionPane.showMessageDialog(CalorieTrackingPanel.this, "Food deleted successfully!");
                    clearFields();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CalorieTrackingPanel.this, "Error occurred: " + ex.getMessage());
                }
            }
        });
    }

    /**
     * @brief Clears all input fields
     * @details Resets all text fields to empty strings after successful
     *          operations or when needed.
     */
    private void clearFields() {
        foodNameField.setText("");
        caloriesField.setText("");
        proteinField.setText("");
        carbsField.setText("");
        fatField.setText("");
    }
}