/**
 * @file PersonalizedDietPanel.java
 * @brief GUI panel for personalized diet recommendations
 * 
 * @details The PersonalizedDietPanel class provides a graphical user interface for:
 *          - Collecting user information (age, weight, height, gender, activity level)
 *          - Generating personalized diet recommendations
 *          - Displaying diet recommendations in a scrollable text area
 * 
 * @author berkant
 * @version 1.0
 * @date 2024
 * @copyright Diet Planner Application
 */
package com.berkant.kagan.haluk.irem.dietapp;

import javax.swing.*;
import javax.swing.text.Caret;

import java.awt.*;
import java.util.List;

/**
 * @class PersonalizedDietPanel
 * @brief GUI panel for collecting user information and displaying diet recommendations
 * 
 * @details This class extends JPanel to create a user interface for the diet recommendation system.
 *          It provides input fields for user characteristics and displays personalized
 *          diet recommendations based on the input data.
 */
public class PersonalizedDietPanel extends JPanel {
    
    /** @brief Service for generating diet recommendations */
    private PersonalizedDietRecommendationService dietService;
    
    /** @brief Text field for entering age */
    private JTextField ageField;
    
    /** @brief Text field for entering weight in kilograms */
    private JTextField weightField;
    
    /** @brief Text field for entering height in centimeters */
    private JTextField heightField;
    
    /** @brief Combo box for selecting gender (Male/Female) */
    private JComboBox<String> genderComboBox;
    
    /** @brief Combo box for selecting activity level */
    private JComboBox<String> activityLevelComboBox;
    
    /** @brief Text area for displaying diet recommendations */
    private JTextArea recommendationsArea;
    
    /** @brief Button for generating recommendations */
    private JButton generateButton;
    
    /**
     * @brief Constructs a new PersonalizedDietPanel
     * @details Initializes the panel with the diet recommendation service and sets up the UI components
     * 
     * @param dietService Service for generating diet recommendations
     */
    public PersonalizedDietPanel(PersonalizedDietRecommendationService dietService) {
        this.dietService = dietService;
        initializeComponents();
        setupLayout();
        addListeners();
    }
    
    /**
     * @brief Initializes UI components
     * @details Creates and configures all UI elements:
     *          - Text fields for numeric input
     *          - Combo boxes for selection
     *          - Text area for recommendations
     *          - Generate button
     */
    private void initializeComponents() {
        ageField = new JTextField(10);
        weightField = new JTextField(10);
        heightField = new JTextField(10);
        
        String[] genders = {"Male", "Female"};
        genderComboBox = new JComboBox<>(genders);
        
        String[] activityLevels = {"Sedentary", "Light", "Moderate", "Active", "Very Active"};
        activityLevelComboBox = new JComboBox<>(activityLevels);
        
        recommendationsArea = new JTextArea(20, 40);
        recommendationsArea.setEditable(false);
        
        generateButton = new JButton("Generate Recommendations");
    }
    
    /**
     * @brief Sets up the panel layout
     * @details Arranges UI components in a structured layout:
     *          - Input panel with form fields
     *          - Button panel with generate button
     *          - Recommendations panel with scrollable text area
     *          - Uses BorderLayout for main panel organization
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
        inputPanel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(ageField, gbc);
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Weight (kg):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(weightField, gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Height (cm):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(heightField, gbc);
        
        // Row 3
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(genderComboBox, gbc);
        
        // Row 4
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Activity Level:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(activityLevelComboBox, gbc);
        
        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(generateButton);
        
        // Recommendations Panel
        JPanel recommendationsPanel = new JPanel(new BorderLayout());
        recommendationsPanel.setBorder(BorderFactory.createTitledBorder("Diet Recommendations"));
        recommendationsPanel.add(new JScrollPane(recommendationsArea), BorderLayout.CENTER);
        
        // Add all panels to main panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(inputPanel, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(leftPanel, BorderLayout.WEST);
        add(recommendationsPanel, BorderLayout.CENTER);
    }
    
    /**
     * @brief Adds event listeners to UI components
     * @details Sets up action listeners for the generate button:
     *          - Validates input data
     *          - Generates recommendations
     *          - Displays results or error messages
     *          - Handles exceptions appropriately
     */
    private void addListeners() {
        generateButton.addActionListener(e -> {
            try {
                int age = Integer.parseInt(ageField.getText().trim());
                double weight = Double.parseDouble(weightField.getText().trim());
                double height = Double.parseDouble(heightField.getText().trim());
                String gender = (String) genderComboBox.getSelectedItem();
                String activityLevel = (String) activityLevelComboBox.getSelectedItem();
                
                List<String> recommendations = dietService.generateRecommendations(age, weight, height, gender, activityLevel);
                StringBuilder sb = new StringBuilder();
                for (String rec : recommendations) {
                    sb.append(rec).append("\n");
                }
                recommendationsArea.setText(sb.toString());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for age, weight, and height");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
    }
}