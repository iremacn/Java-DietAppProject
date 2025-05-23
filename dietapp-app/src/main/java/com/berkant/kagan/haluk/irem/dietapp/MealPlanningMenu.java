/**
 * @file MealPlanningMenu.java
 * @brief Meal planning menu interface for the Diet Planner application
 * 
 * @details The MealPlanningMenu class provides both console-based and graphical user interface
 *          for managing meal planning operations. It supports:
 *          - Planning meals for different times of day
 *          - Logging food consumption
 *          - Viewing meal history
 *          - Managing meal plans through an intuitive interface
 * 
 * @author berkant
 * @version 1.0
 * @date 2024
 * @copyright Diet Planner Application
 */
package com.berkant.kagan.haluk.irem.dietapp;

import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @class MealPlanningMenu
 * @brief Handles meal planning menu operations and user interactions
 * 
 * @details This class provides a comprehensive interface for meal planning operations,
 *          supporting both console-based and GUI interactions. It manages:
 *          - Meal planning for different times of day (breakfast, lunch, snack, dinner)
 *          - Food logging and tracking
 *          - Meal history viewing and management
 *          - User interface components for both console and GUI modes
 */
public class MealPlanningMenu {
    /** @brief Service for handling meal planning operations */
    private MealPlanningService mealPlanningService;
    /** @brief Service for managing user authentication */
    private AuthenticationService authService;
    /** @brief Scanner for reading console user input */
    private Scanner scanner;
    
    /** @brief Flag indicating whether to use GUI components */
    protected boolean useUIComponents = true;
    
    /** @brief Main application window for GUI mode */
    private JFrame frame;
    /** @brief Main panel containing all GUI components */
    private JPanel mainPanel;
    
    /**
     * @brief Constructs a new MealPlanningMenu instance
     * @details Initializes the menu with required services and input handling:
     *          - Sets up meal planning service
     *          - Configures authentication service
     *          - Initializes input scanner
     *          - Creates GUI components if enabled
     * 
     * @param mealPlanningService Service for meal planning operations
     * @param authService Service for user authentication
     * @param scanner Scanner for reading user input
     */
    public MealPlanningMenu(MealPlanningService mealPlanningService, AuthenticationService authService, Scanner scanner) {
        this.mealPlanningService = mealPlanningService;
        this.authService = authService;
        this.scanner = scanner;
        
        if (useUIComponents) {
            initializeUI();
        }
    }
    
    /**
     * @brief Initializes the graphical user interface
     * @details Sets up the main window and components:
     *          - Creates the main frame
     *          - Configures layout and components
     *          - Sets up action listeners
     *          - Arranges navigation buttons
     */
    private void initializeUI() {
        frame = new JFrame("Meal Planning and Logging");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);  
        
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        JButton planMealsButton = new JButton("Plan Meals");
        JButton logFoodsButton = new JButton("Log Foods");
        JButton viewHistoryButton = new JButton("View Meal History");
        JButton returnButton = new JButton("Return to Main Menu");
        
        planMealsButton.addActionListener(e -> handlePlanMeals());
        logFoodsButton.addActionListener(e -> handleLogFoods());
        viewHistoryButton.addActionListener(e -> handleViewMealHistory());
        returnButton.addActionListener(e -> frame.dispose());
        
        mainPanel.add(planMealsButton);
        mainPanel.add(logFoodsButton);
        mainPanel.add(viewHistoryButton);
        mainPanel.add(returnButton);
        
        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    /**
     * @brief Displays the main meal planning menu
     * @details Shows either GUI or console interface based on useUIComponents flag:
     *          - GUI mode: Displays interactive buttons
     *          - Console mode: Shows text-based menu
     *          Handles user input and navigation between different features
     */
    public void displayMenu() {
        if (useUIComponents) {
            if (frame == null || !frame.isVisible()) {
                initializeUI();
            }
        } else {
            boolean running = true;
            while (running) {
                System.out.println("\n===== Meal Planning and Logging =====");
                System.out.println("1. Plan Meals");
                System.out.println("2. Log Foods");
                System.out.println("3. View Meal History");
                System.out.println("0. Return to Main Menu");
                System.out.print("Enter your choice: ");
                
                int choice = getUserChoice();
                
                switch (choice) {
                    case 1:
                        handlePlanMeals();
                        break;
                    case 2:
                        handleLogFoods();
                        break;
                    case 3:
                        handleViewMealHistory();
                        break;
                    case 0:
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        }
    }
    
    /**
     * @brief Gets user input from console
     * @details Reads and validates user input:
     *          - Attempts to parse integer input
     *          - Returns -1 for invalid input
     * 
     * @return The user's choice as an integer, or -1 for invalid input
     */
    private int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * @brief Handles meal planning process
     * @details Routes to appropriate handler based on interface mode:
     *          - GUI mode: Uses handlePlanMealsUI()
     *          - Console mode: Uses handlePlanMealsConsole()
     */
    private void handlePlanMeals() {
        if (useUIComponents) {
            handlePlanMealsUI();
        } else {
            handlePlanMealsConsole();
        }
    }
    
    /**
     * @brief Handles meal planning in console mode
     * @details Guides user through meal planning process:
     *          - Gets date information
     *          - Selects meal type
     *          - Chooses food options
     *          - Adds meal to plan
     */
    protected void handlePlanMealsConsole() {
        System.out.println("\n===== Plan Meals =====");
        
        String date = getDateFromUser();
        if (date == null) {
            return;
        }
       
        System.out.println("\nSelect Meal Type:");
        System.out.println("1. Breakfast");
        System.out.println("2. Lunch");
        System.out.println("3. Snack");
        System.out.println("4. Dinner");
        System.out.print("Enter your choice: ");
        
        int mealTypeChoice = getUserChoice();
        String mealType;
        Food[] foodOptions;
        
        switch (mealTypeChoice) {
            case 1:
                mealType = "breakfast";
                foodOptions = mealPlanningService.getBreakfastOptions();
                break;
            case 2:
                mealType = "lunch";
                foodOptions = mealPlanningService.getLunchOptions();
                break;
            case 3:
                mealType = "snack";
                foodOptions = mealPlanningService.getSnackOptions();
                break;
            case 4:
                mealType = "dinner";
                foodOptions = mealPlanningService.getDinnerOptions();
                break;
            default:
                System.out.println("Invalid meal type. Returning to menu.");
                return;
        }
        
        System.out.println("\nSelect Food for " + capitalize(mealType) + ":");
        for (int i = 0; i < foodOptions.length; i++) {
            System.out.println((i + 1) + ". " + foodOptions[i]);
        }
        System.out.print("Enter your choice (1-" + foodOptions.length + "): ");
        
        int foodChoice = getUserChoice();
        if (foodChoice < 1 || foodChoice > foodOptions.length) {
            System.out.println("Invalid food choice. Returning to menu.");
            return;
        }
        
        Food selectedFood = foodOptions[foodChoice - 1];
        String username = authService.getCurrentUser().getUsername();
        boolean success = mealPlanningService.addMealPlan(username, date, mealType, selectedFood);
        
        if (success) {
            System.out.println(selectedFood.getName() + " added to " + mealType + " successfully!");
        } else {
            System.out.println("Failed to add food to meal plan.");
        }
    }
    
    /**
     * @brief Handles meal planning in GUI mode
     * @details Creates a dialog for meal planning with:
     *          - Date selection
     *          - Meal type selection
     *          - Food selection interface
     *          - Confirmation dialog
     */
    private void handlePlanMealsUI() {
        JFrame planFrame = new JFrame("Plan Meals");
        planFrame.setSize(500, 400);
        planFrame.setLayout(new BorderLayout());
        
        JPanel datePanel = new JPanel();
        datePanel.setLayout(new GridLayout(4, 2));
        
        JLabel yearLabel = new JLabel("Year (2025-2100):");
        JTextField yearField = new JTextField();
        JLabel monthLabel = new JLabel("Month (1-12):");
        JTextField monthField = new JTextField();
        JLabel dayLabel = new JLabel("Day (1-31):");
        JTextField dayField = new JTextField();
        
        datePanel.add(yearLabel);
        datePanel.add(yearField);
        datePanel.add(monthLabel);
        datePanel.add(monthField);
        datePanel.add(dayLabel);
        datePanel.add(dayField);
        
        JPanel mealTypePanel = new JPanel();
        String[] mealTypes = {"Breakfast", "Lunch", "Snack", "Dinner"};
        JComboBox<String> mealTypeCombo = new JComboBox<>(mealTypes);
        mealTypePanel.add(new JLabel("Meal Type:"));
        mealTypePanel.add(mealTypeCombo);
        
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new BorderLayout());
        selectionPanel.add(datePanel, BorderLayout.NORTH);
        selectionPanel.add(mealTypePanel, BorderLayout.CENTER);
        
        JButton continueButton = new JButton("Continue");
        JButton cancelButton = new JButton("Cancel");
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(continueButton);
        buttonPanel.add(cancelButton);
        
        planFrame.add(selectionPanel, BorderLayout.CENTER);
        planFrame.add(buttonPanel, BorderLayout.SOUTH);
        
        cancelButton.addActionListener(e -> planFrame.dispose());
        
        continueButton.addActionListener(e -> {
            try {
                int year = Integer.parseInt(yearField.getText());
                int month = Integer.parseInt(monthField.getText());
                int day = Integer.parseInt(dayField.getText());
                
                if (year < 2025 || year > 2100 || month < 1 || month > 12 || day < 1 || day > 31) {
                    JOptionPane.showMessageDialog(planFrame, 
                        "Invalid date values. Please check your input.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!mealPlanningService.isValidDate(year, month, day)) {
                    JOptionPane.showMessageDialog(planFrame, 
                        "Invalid date. Please check the number of days in the selected month.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String date = mealPlanningService.formatDate(year, month, day);
                String mealTypeStr = mealTypeCombo.getSelectedItem().toString().toLowerCase();
                
                planFrame.dispose();
                showFoodSelectionUI(date, mealTypeStr);
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(planFrame, 
                    "Please enter valid numbers for date fields.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        planFrame.setLocationRelativeTo(null);
        planFrame.setVisible(true);
    }
    
    /**
     * @brief Shows food selection interface in GUI mode
     * @details Creates a dialog for selecting food items:
     *          - Displays available food options
     *          - Handles food selection
     *          - Confirms meal addition
     * 
     * @param date The selected date for the meal
     * @param mealType The type of meal being planned
     */
    private void showFoodSelectionUI(String date, String mealType) {
        
        JFrame foodFrame = new JFrame("Select Food for " + capitalize(mealType));
        foodFrame.setSize(500, 400);
        foodFrame.setLayout(new BorderLayout());
        
        
        Food[] foodOptions;
        switch (mealType) {
            case "breakfast":
                foodOptions = mealPlanningService.getBreakfastOptions();
                break;
            case "lunch":
                foodOptions = mealPlanningService.getLunchOptions();
                break;
            case "snack":
                foodOptions = mealPlanningService.getSnackOptions();
                break;
            case "dinner":
                foodOptions = mealPlanningService.getDinnerOptions();
                break;
            default:
                JOptionPane.showMessageDialog(foodFrame, "Invalid meal type", "Error", JOptionPane.ERROR_MESSAGE);
                foodFrame.dispose();
                return;
        }
        
        
        DefaultListModel<Food> listModel = new DefaultListModel<>();
        for (Food food : foodOptions) {
            listModel.addElement(food);
        }
        
        JList<Food> foodList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(foodList);
        
        
        JButton selectButton = new JButton("Select");
        JButton cancelButton = new JButton("Cancel");
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);
        
        foodFrame.add(scrollPane, BorderLayout.CENTER);
        foodFrame.add(buttonPanel, BorderLayout.SOUTH);
        
        cancelButton.addActionListener(e -> foodFrame.dispose());
        
        selectButton.addActionListener(e -> {
            Food selectedFood = foodList.getSelectedValue();
            if (selectedFood == null) {
                JOptionPane.showMessageDialog(foodFrame, "Please select a food item", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String username = authService.getCurrentUser().getUsername();
            boolean success = mealPlanningService.addMealPlan(username, date, mealType, selectedFood);
            
            if (success) {
                JOptionPane.showMessageDialog(foodFrame, selectedFood.getName() + " added to " + mealType + " successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(foodFrame, "Failed to add food to meal plan.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            
            foodFrame.dispose();
        });
        
        foodFrame.setLocationRelativeTo(null);
        foodFrame.setVisible(true);
    }
    
    /**
     * @brief Handles food logging process
     * @details Routes to appropriate handler based on interface mode:
     *          - GUI mode: Uses handleLogFoodsUI()
     *          - Console mode: Uses handleLogFoodsConsole()
     */
    private void handleLogFoods() {
        if (useUIComponents) {
            handleLogFoodsUI();
        } else {
            handleLogFoodsConsole();
        }
    }
    
    /**
     * @brief Handles food logging in console mode
     * @details Guides user through food logging process:
     *          - Gets date information
     *          - Collects food details
     *          - Logs the food entry
     */
    protected void handleLogFoodsConsole() {
        System.out.println("\n===== Log Foods =====");
        
        String date = getDateFromUser();
        if (date == null) {
            return;
        }
        
        System.out.println("\nLog Food Consumed:");
        Food food = getFoodDetailsFromUser();
        
        if (food != null) {
            String username = authService.getCurrentUser().getUsername();
            boolean success = mealPlanningService.logFood(username, date, food);
            
            if (success) {
                System.out.println("Food logged successfully!");
            } else {
                System.out.println("Failed to log food.");
            }
        }
    }
    
    /**
     * @brief Handles food logging in GUI mode
     * @details Creates a dialog for food logging with:
     *          - Date selection
     *          - Food details input
     *          - Confirmation dialog
     */
    private void handleLogFoodsUI() {
        JFrame logFrame = new JFrame("Log Foods");
        logFrame.setSize(500, 400);
        logFrame.setLayout(new BorderLayout());

        JPanel datePanel = new JPanel();
        datePanel.setLayout(new GridLayout(4, 2));

        JLabel yearLabel = new JLabel("Year (2025-2100):");
        JTextField yearField = new JTextField();
        JLabel monthLabel = new JLabel("Month (1-12):");
        JTextField monthField = new JTextField();
        JLabel dayLabel = new JLabel("Day (1-31):");
        JTextField dayField = new JTextField();

        datePanel.add(yearLabel);
        datePanel.add(yearField);
        datePanel.add(monthLabel);
        datePanel.add(monthField);
        datePanel.add(dayLabel);
        datePanel.add(dayField);

        JPanel foodPanel = new JPanel();
        foodPanel.setLayout(new GridLayout(3, 2));

        JLabel nameLabel = new JLabel("Food Name:");
        JTextField nameField = new JTextField();
        JLabel amountLabel = new JLabel("Amount (grams):");
        JTextField amountField = new JTextField();
        JLabel caloriesLabel = new JLabel("Calories:");
        JTextField caloriesField = new JTextField();

        foodPanel.add(nameLabel);
        foodPanel.add(nameField);
        foodPanel.add(amountLabel);
        foodPanel.add(amountField);
        foodPanel.add(caloriesLabel);
        foodPanel.add(caloriesField);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(datePanel, BorderLayout.NORTH);
        inputPanel.add(foodPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        logFrame.add(inputPanel, BorderLayout.CENTER);
        logFrame.add(buttonPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> logFrame.dispose());

        saveButton.addActionListener(e -> {
            try {
                // Validate date fields
                int year, month, day;
                try {
                    year = Integer.parseInt(yearField.getText().trim());
                    month = Integer.parseInt(monthField.getText().trim());
                    day = Integer.parseInt(dayField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(logFrame, 
                        "Please enter valid numbers for date fields.", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate date ranges
                if (year < 2025 || year > 2100) {
                    JOptionPane.showMessageDialog(logFrame, 
                        "Year must be between 2025 and 2100.", 
                        "Invalid Year", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (month < 1 || month > 12) {
                    JOptionPane.showMessageDialog(logFrame, 
                        "Month must be between 1 and 12.", 
                        "Invalid Month", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (day < 1 || day > 31) {
                    JOptionPane.showMessageDialog(logFrame, 
                        "Day must be between 1 and 31.", 
                        "Invalid Day", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate date validity
                if (!mealPlanningService.isValidDate(year, month, day)) {
                    JOptionPane.showMessageDialog(logFrame, 
                        "Invalid date. Please check the number of days in the selected month.", 
                        "Invalid Date", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate food name
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(logFrame, 
                        "Food name cannot be empty.", 
                        "Invalid Food Name", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate amount
                double amount;
                try {
                    amount = Double.parseDouble(amountField.getText().trim());
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(logFrame, 
                            "Amount must be greater than 0.", 
                            "Invalid Amount", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(logFrame, 
                        "Please enter a valid number for amount.", 
                        "Invalid Amount Format", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate calories
                int calories;
                try {
                    calories = Integer.parseInt(caloriesField.getText().trim());
                    if (calories < 0) {
                        JOptionPane.showMessageDialog(logFrame, 
                            "Calories cannot be negative.", 
                            "Invalid Calories", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(logFrame, 
                        "Please enter a valid number for calories.", 
                        "Invalid Calories Format", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Create food entry
                String date = mealPlanningService.formatDate(year, month, day);
                Food food = new Food(name, amount, calories);

                // Log the food
                String username = authService.getCurrentUser().getUsername();
                boolean success = mealPlanningService.logFood(username, date, food);

                if (success) {
                    JOptionPane.showMessageDialog(logFrame, 
                        "Food logged successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    logFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(logFrame, 
                        "Failed to log food. Please try again.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(logFrame, 
                    "An unexpected error occurred: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        logFrame.setLocationRelativeTo(null);
        logFrame.setVisible(true);
    }
    
    /**
     * @brief Handles meal history viewing process
     * @details Routes to appropriate handler based on interface mode:
     *          - GUI mode: Uses handleViewMealHistoryUI()
     *          - Console mode: Uses handleViewMealHistoryConsole()
     */
    private void handleViewMealHistory() {
        if (useUIComponents) {
            handleViewMealHistoryUI();
        } else {
            handleViewMealHistoryConsole();
        }
    }
    
    /**
     * @brief Handles meal history viewing in console mode
     * @details Guides user through viewing meal history:
     *          - Gets date information
     *          - Displays meal history
     *          - Shows meal details
     */
    protected void handleViewMealHistoryConsole() {
        System.out.println("\n===== View Meal History =====");
        
        String date = getDateFromUser();
        if (date == null) {
            return;
        }
        
        String username = authService.getCurrentUser().getUsername();
        boolean hasContent = false;
        
        System.out.println("\n--- Planned Meals for " + date + " ---");
        boolean hasPlannedMeals = false;
        
        String[] mealTypes = {"breakfast", "lunch", "snack", "dinner"};
        for (String mealType : mealTypes) {
            List<Food> mealPlan = mealPlanningService.getMealPlan(username, date, mealType);
            if (!mealPlan.isEmpty()) {
                hasPlannedMeals = true;
                hasContent = true;
                System.out.println("\n" + capitalize(mealType) + ":");
                for (Food food : mealPlan) {
                    System.out.println("- " + food);
                }
            }
        }
        
        if (!hasPlannedMeals) {
            System.out.println("No planned meals found for this date.");
        }
        
        System.out.println("\n--- Food Log for " + date + " ---");
        List<Food> foodLog = mealPlanningService.getFoodLog(username, date);
        
        if (foodLog.isEmpty()) {
            System.out.println("No food logged for this date.");
        } else {
            hasContent = true;
            for (Food food : foodLog) {
                System.out.println("- " + food);
            }
            
            int totalCalories = mealPlanningService.getTotalCalories(username, date);
            System.out.println("\nTotal calories consumed: " + totalCalories);
        }
        
        if (!hasContent) {
            System.out.println("\nNo meal plans or food logs found for " + date);
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * @brief Handles meal history viewing in GUI mode
     * @details Creates a dialog for viewing meal history with:
     *          - Date selection
     *          - Meal history display
     *          - Detailed meal information
     */
    private void handleViewMealHistoryUI() {
        JFrame dateFrame = new JFrame("Select Date for Meal History");
        dateFrame.setSize(450, 200);
        dateFrame.setLayout(new BorderLayout());

        JPanel datePanel = new JPanel();
        datePanel.setLayout(new GridLayout(3, 2));

        JLabel yearLabel = new JLabel("Year (2025-2100):");
        JTextField yearField = new JTextField();
        JLabel monthLabel = new JLabel("Month (1-12):");
        JTextField monthField = new JTextField();
        JLabel dayLabel = new JLabel("Day (1-31):");
        JTextField dayField = new JTextField();

        datePanel.add(yearLabel);
        datePanel.add(yearField);
        datePanel.add(monthLabel);
        datePanel.add(monthField);
        datePanel.add(dayLabel);
        datePanel.add(dayField);

        JButton viewButton = new JButton("View History");
        JButton cancelButton = new JButton("Cancel");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(viewButton);
        buttonPanel.add(cancelButton);

        dateFrame.add(datePanel, BorderLayout.CENTER);
        dateFrame.add(buttonPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> dateFrame.dispose());

        viewButton.addActionListener(e -> {
            try {
                int year = Integer.parseInt(yearField.getText());
                int month = Integer.parseInt(monthField.getText());
                int day = Integer.parseInt(dayField.getText());

                if (year < 2025 || year > 2100 || month < 1 || month > 12 || day < 1 || day > 31) {
                    JOptionPane.showMessageDialog(dateFrame, 
                        "Invalid date values. Please check your input.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!mealPlanningService.isValidDate(year, month, day)) {
                    JOptionPane.showMessageDialog(dateFrame, 
                        "Invalid date. Please check the number of days in the selected month.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String date = mealPlanningService.formatDate(year, month, day);
                dateFrame.dispose();
                displayMealHistoryUI(date);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dateFrame, 
                    "Please enter valid numbers for date fields.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        dateFrame.setLocationRelativeTo(null);
        dateFrame.setVisible(true);
    }
    
    /**
     * @brief Displays meal history in GUI mode
     * @details Creates a dialog showing meal history for a specific date:
     *          - Shows meal plan
     *          - Displays logged foods
     *          - Calculates and shows totals
     * 
     * @param date The date for which to display meal history
     */
    private void displayMealHistoryUI(String date) {
        JFrame historyFrame = new JFrame("Meal History for " + date);
        historyFrame.setSize(600, 500);
        historyFrame.setLayout(new BorderLayout());
        
        String username = authService.getCurrentUser().getUsername();
        boolean hasContent = false;
        
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        
        JPanel plannedPanel = new JPanel();
        plannedPanel.setLayout(new BoxLayout(plannedPanel, BoxLayout.Y_AXIS));
        plannedPanel.setBorder(BorderFactory.createTitledBorder("Planned Meals"));
        
        boolean hasPlannedMeals = false;
        String[] mealTypes = {"breakfast", "lunch", "snack", "dinner"};
        
        for (String mealType : mealTypes) {
            List<Food> mealPlan = mealPlanningService.getMealPlan(username, date, mealType);
            if (!mealPlan.isEmpty()) {
                hasPlannedMeals = true;
                hasContent = true;
                
                JPanel mealPanel = new JPanel();
                mealPanel.setLayout(new BoxLayout(mealPanel, BoxLayout.Y_AXIS));
                JLabel mealLabel = new JLabel(capitalize(mealType) + ":");
                mealLabel.setFont(new Font("Arial", Font.BOLD, 14));
                mealPanel.add(mealLabel);
                
                for (Food food : mealPlan) {
                    mealPanel.add(new JLabel("- " + food));
                }
                
                plannedPanel.add(mealPanel);
                plannedPanel.add(Box.createVerticalStrut(10)); 
            }
        }
        
        if (!hasPlannedMeals) {
            plannedPanel.add(new JLabel("No planned meals found for this date."));
        }
        
        contentPanel.add(plannedPanel);
        
        
        JPanel logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.Y_AXIS));
        logPanel.setBorder(BorderFactory.createTitledBorder("Food Log"));
        
        List<Food> foodLog = mealPlanningService.getFoodLog(username, date);
        
        if (foodLog.isEmpty()) {
            logPanel.add(new JLabel("No food logged for this date."));
        } else {
            hasContent = true;
            
            for (Food food : foodLog) {
                logPanel.add(new JLabel("- " + food));
            }
            
            int totalCalories = mealPlanningService.getTotalCalories(username, date);
            JLabel caloriesLabel = new JLabel("Total calories consumed: " + totalCalories);
            caloriesLabel.setFont(new Font("Arial", Font.BOLD, 14));
            logPanel.add(Box.createVerticalStrut(10));
            logPanel.add(caloriesLabel);
        }
        
        contentPanel.add(logPanel);
        
        if (!hasContent) {
            JLabel noContentLabel = new JLabel("No meal plans or food logs found for " + date);
            noContentLabel.setFont(new Font("Arial", Font.BOLD, 14));
            contentPanel.add(noContentLabel);
        }
        
        // Scroll panel
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        
        
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> historyFrame.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        
        historyFrame.add(scrollPane, BorderLayout.CENTER);
        historyFrame.add(buttonPanel, BorderLayout.SOUTH);
        
        historyFrame.setLocationRelativeTo(null);
        historyFrame.setVisible(true);
    }
    
    /**
     * @brief Gets date input from user
     * @details Prompts user for date input and validates format:
     *          - Accepts date in YYYY-MM-DD format
     *          - Validates date format
     *          - Returns null for invalid input
     * 
     * @return The entered date as a string, or null if invalid
     */
    private String getDateFromUser() {
        int year = 0;
        int month = 0;
        int day = 0;
        boolean validDate = false;
        
        while (!validDate) {
            System.out.println("\nEnter Date:");
            
            // Get year and validate range
            System.out.print("Year (2025-2100): ");
            try {
                year = Integer.parseInt(scanner.nextLine());
                if (year < 2025 || year > 2100) {
                    System.out.println("Invalid year. Please enter a year between 2025 and 2100.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid year format. Please enter a valid number.");
                continue;
            }
            
            // get month and validate range
            System.out.print("Month (1-12): ");
            try {
                month = Integer.parseInt(scanner.nextLine());
                if (month < 1 || month > 12) {
                    System.out.println("Invalid month. Please enter a month between 1 and 12.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid month format. Please enter a valid number.");
                continue;
            }
            
            // get day and validate range
            System.out.print("Day (1-31): ");
            try {
                day = Integer.parseInt(scanner.nextLine());
                if (day < 1 || day > 31) {
                    System.out.println("Invalid day. Please enter a day between 1 and 31.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid day format. Please enter a valid number.");
                continue;
            }
            
            // Additional date validation (days in month)
            if (!mealPlanningService.isValidDate(year, month, day)) {
                System.out.println("Invalid date. Please check the number of days in the selected month.");
                continue;
            }
            
            validDate = true;
        }
        
        return mealPlanningService.formatDate(year, month, day);
    }
    
    /**
     * @brief Gets food details from user
     * @details Prompts user for food information:
     *          - Food name
     *          - Calorie content
     *          - Macronutrient values
     *          - Creates Food object
     * 
     * @return A new Food object with user-provided details
     */
    private Food getFoodDetailsFromUser() {
        System.out.print("Enter food name: ");
        String name = scanner.nextLine();
        
        double grams;
        System.out.print("Enter amount (grams): ");
        try {
            grams = Double.parseDouble(scanner.nextLine());
            if (grams <= 0) {
                System.out.println("Amount must be positive.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount format.");
            return null;
        }
        
        int calories;
        System.out.print("Enter calories: ");
        try {
            calories = Integer.parseInt(scanner.nextLine());
            if (calories < 0) {
                System.out.println("Calories cannot be negative.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid calorie format.");
            return null;
        }
        
        return new Food(name, grams, calories);
    }
    
    /**
     * @brief Capitalizes the first letter of a string
     * @details Converts the first character to uppercase:
     *          - Handles empty strings
     *          - Preserves rest of string
     * 
     * @param str The string to capitalize
     * @return The string with first letter capitalized
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}