/**
 * @file ShoppingListPanel.java
 * @brief A Swing panel that displays and manages shopping lists for meal plans
 * @author Berkant Kagan Haluk Irem
 * @date 2024
 */

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

/**
 * @class ShoppingListPanel
 * @brief A JPanel implementation that provides shopping list management functionality
 * 
 * This class creates a graphical user interface for managing shopping lists,
 * including generating new shopping lists and clearing existing ones.
 * It displays a scrollable text area for the shopping list items and
 * provides buttons for generating and clearing the list.
 */
public class ShoppingListPanel extends JPanel {
    /** @brief Service responsible for generating shopping lists */
    private final ShoppingListService shoppingService;
    
    /** @brief Text area component for displaying the shopping list */
    private JTextArea shoppingListArea;
    
    /** @brief Button for generating a new shopping list */
    private JButton generateButton;
    
    /** @brief Button for clearing the current shopping list */
    private JButton clearButton;

    /**
     * @brief Constructs a new ShoppingListPanel
     * @param shoppingService The shopping list service to be used for generating lists
     * 
     * Initializes the panel components, sets up the layout, and configures
     * event listeners for the buttons.
     */
    public ShoppingListPanel(ShoppingListService shoppingService) {
        this.shoppingService = shoppingService;
        initializeComponents();
        setupLayout();
        setupListeners();
    }

    /**
     * @brief Initializes the panel's components
     * 
     * Creates and configures the text area and buttons with appropriate
     * properties and styling.
     */
    private void initializeComponents() {
        shoppingListArea = new JTextArea(15, 40);
        shoppingListArea.setEditable(false);
        shoppingListArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        generateButton = new JButton("Generate Shopping List");
        clearButton = new JButton("Clear");
    }

    /**
     * @brief Sets up the panel's layout
     * 
     * Configures the BorderLayout, adds borders, and arranges the components
     * including the scrollable text area and button panel.
     */
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

    /**
     * @brief Sets up event listeners for the panel's buttons
     * 
     * Configures action listeners for the generate and clear buttons.
     * The generate button creates a new shopping list using the shopping service,
     * while the clear button empties the current shopping list display.
     */
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