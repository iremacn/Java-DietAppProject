package com.berkant.kagan.haluk.irem.dietapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class CalorieTrackingPanel extends JPanel {
    private CalorieNutrientTrackingService trackingService;
    private JTextField foodNameField;
    private JTextField caloriesField;
    private JTextField proteinField;
    private JTextField carbsField;
    private JTextField fatField;
    private JTextArea resultArea;
    private JButton addButton;
    private JButton viewButton;
    private JButton deleteButton;

    public CalorieTrackingPanel(CalorieNutrientTrackingService trackingService) {
        this.trackingService = trackingService;
        initializeComponents();
        setupLayout();
        setupListeners();
    }

    private void initializeComponents() {
        foodNameField = new JTextField(20);
        caloriesField = new JTextField(10);
        proteinField = new JTextField(10);
        carbsField = new JTextField(10);
        fatField = new JTextField(10);
        resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false);
        addButton = new JButton("Ekle");
        viewButton = new JButton("Görüntüle");
        deleteButton = new JButton("Sil");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Yemek Adı:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(foodNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Kalori:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(caloriesField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Protein (g):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(proteinField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Karbonhidrat (g):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(carbsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(new JLabel("Yağ (g):"), gbc);
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
                    JOptionPane.showMessageDialog(CalorieTrackingPanel.this, "Yemek başarıyla eklendi!");
                    clearFields();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(CalorieTrackingPanel.this, "Lütfen geçerli sayısal değerler girin!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CalorieTrackingPanel.this, "Hata oluştu: " + ex.getMessage());
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
                    JOptionPane.showMessageDialog(CalorieTrackingPanel.this, "Hata oluştu: " + ex.getMessage());
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String foodName = foodNameField.getText();
                    trackingService.deleteFoodEntry(foodName);
                    JOptionPane.showMessageDialog(CalorieTrackingPanel.this, "Yemek başarıyla silindi!");
                    clearFields();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CalorieTrackingPanel.this, "Hata oluştu: " + ex.getMessage());
                }
            }
        });
    }

    private void clearFields() {
        foodNameField.setText("");
        caloriesField.setText("");
        proteinField.setText("");
        carbsField.setText("");
        fatField.setText("");
    }
}