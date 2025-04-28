package com.berkant.kagan.haluk.irem.dietapp;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ShoppingListPanel extends JPanel {
    private final ShoppingListService shoppingService;
    private JTextArea shoppingListArea;
    private JButton generateButton;
    private JButton clearButton;

    public ShoppingListPanel(ShoppingListService shoppingService) {
        this.shoppingService = shoppingService;
        initializeComponents();
        setupLayout();
        setupListeners();
    }

    private void initializeComponents() {
        shoppingListArea = new JTextArea(15, 40);
        shoppingListArea.setEditable(false);
        shoppingListArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        generateButton = new JButton("Generate Shopping List");
        clearButton = new JButton("Clear");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(generateButton);
        buttonPanel.add(clearButton);

        JScrollPane scrollPane = new JScrollPane(shoppingListArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Shopping List"));

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        generateButton.addActionListener(e -> {
            try {
                List<String> shoppingList = shoppingService.generateShoppingList();
                StringBuilder sb = new StringBuilder();
                shoppingList.forEach(item -> sb.append(item).append("\n"));
                shoppingListArea.setText(sb.toString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error generating shopping list: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        clearButton.addActionListener(e -> shoppingListArea.setText(""));
    }
}