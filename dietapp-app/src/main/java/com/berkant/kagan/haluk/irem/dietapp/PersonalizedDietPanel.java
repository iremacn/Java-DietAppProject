package com.berkant.kagan.haluk.irem.dietapp;

import javax.swing.*;
import javax.swing.text.Caret;

import java.awt.*;
import java.util.List;

public class PersonalizedDietPanel extends JPanel {
    private PersonalizedDietRecommendationService dietService;
    
    private JTextField ageField;
    private JTextField weightField;
    private JTextField heightField;
    private JComboBox<String> genderComboBox;
    private JComboBox<String> activityLevelComboBox;
    private JTextArea recommendationsArea;
    private JButton generateButton;
    
    public PersonalizedDietPanel(PersonalizedDietRecommendationService dietService) {
        this.dietService = dietService;
        initializeComponents();
        setupLayout();
        addListeners();
    }
    
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
    
    private void addListeners() {
        generateButton.addActionListener(e -> {
            try {
                int age = Integer.parseInt(ageField.getText().trim());
                double weight = Double.parseDouble(weightField.getText().trim());
                double height = Double.parseDouble(heightField.getText().trim());
                String gender = (String) genderComboBox.getSelectedItem();
                String activityLevel = (String) activityLevelComboBox.getSelectedItem();
                
                List<String> recommendations = dietService.generateRecommendations(age, weight, height, gender, activityLevel);
                recommendationsArea.setCaret((Caret) recommendations);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for age, weight, and height");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
    }
}