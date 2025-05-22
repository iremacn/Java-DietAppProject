/**
 * @file MainnFrame.java
 * @brief Alternative main graphical user interface frame for the Diet Planner application
 * 
 * @details The MainnFrame class serves as an alternative main window implementation
 *          for the Diet Planner application. It provides a more structured layout
 *          with improved component organization and event handling.
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
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * @class MainnFrame
 * @brief Alternative main window implementation for the Diet Planner
 * 
 * @details This class extends JFrame to create an alternative main application window
 *          with enhanced layout management and event handling. It provides:
 *          - Structured content pane with proper borders
 *          - Improved component organization
 *          - Enhanced event handling for navigation
 *          - Better visual styling for UI elements
 * 
 *          The class manages:
 *          - User authentication
 *          - Calorie tracking
 *          - Meal planning
 *          - Personalized diet recommendations
 *          - Shopping list management
 */
public class MainnFrame extends JFrame {

	/** @brief Serial version UID for serialization */
	private static final long serialVersionUID = 1L;
	/** @brief Main content pane with border */
	private JPanel contentPane;
	/** @brief Panel containing all functional panels */
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
	/** @brief Button for accessing calorie tracking */
	private JButton calorieButton;
	/** @brief Button for accessing meal planning */
	private JButton mealButton;
	/** @brief Button for accessing personalized diet */
	private JButton dietButton;
	/** @brief Button for accessing shopping list */
	private JButton shoppingButton;

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
			
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						MainnFrame frame = new MainnFrame();
						frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(null, 
				"SQLite JDBC driver not found: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * @brief Constructs the main application window
	 * @details Initializes the main window with all necessary components:
	 *          - Sets up the window properties and bounds
	 *          - Initializes the database
	 *          - Creates and configures the content pane
	 *          - Sets up the main panel with CardLayout
	 *          - Creates service objects
	 *          - Initializes all functional panels
	 *          - Creates and configures navigation buttons
	 *          - Sets up the initial view
	 */
	public MainnFrame() {
		setTitle("Diet Planner");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		setLocationRelativeTo(null);
		
		// Initialize database
		DatabaseHelper.initializeDatabase();
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		// Main panel with CardLayout
		mainPanel = new JPanel();
		cardLayout = new CardLayout();
		mainPanel.setLayout(cardLayout);
		contentPane.add(mainPanel, BorderLayout.CENTER);
		
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
		buttonPanel = new JPanel();
		buttonPanel.setBackground(new Color(236, 240, 241));
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
		contentPane.add(buttonPanel, BorderLayout.NORTH);
		
		// Initialize and configure navigation buttons
		initializeNavigationButtons();
		
		// Add panels to CardLayout
		mainPanel.add(authPanel, "login");
		mainPanel.add(calorieTrackingPanel, "calorie");
		mainPanel.add(mealPlanningPanel, "meal");
		mainPanel.add(personalizedDietPanel, "diet");
		mainPanel.add(shoppingListPanel, "shopping");
		
		// Show login screen initially
		cardLayout.show(mainPanel, "login");
		buttonPanel.setVisible(false);
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
	 * @brief Initializes and configures navigation buttons
	 * @details Creates and sets up all navigation buttons with:
	 *          - Consistent styling and dimensions
	 *          - Custom colors and fonts
	 *          - Action listeners for panel switching
	 *          - Proper layout and positioning
	 */
	private void initializeNavigationButtons() {
		// Meal Planning button
		mealButton = new JButton("Meal Planning");
		configureButton(mealButton, "meal");
		
		// Calorie Tracking button
		calorieButton = new JButton("Calorie Tracking");
		configureButton(calorieButton, "calorie");
		
		// Personalized Diet button
		dietButton = new JButton("Personalized Diet");
		configureButton(dietButton, "diet");
		
		// Shopping List button
		shoppingButton = new JButton("Shopping List");
		configureButton(shoppingButton, "shopping");
		
		// Add buttons to panel
		buttonPanel.add(calorieButton);
		buttonPanel.add(mealButton);
		buttonPanel.add(dietButton);
		buttonPanel.add(shoppingButton);
	}
	
	/**
	 * @brief Configures a navigation button with standard styling
	 * @details Applies consistent styling to a navigation button:
	 *          - Sets font and colors
	 *          - Configures dimensions
	 *          - Adds action listener
	 * 
	 * @param button The button to configure
	 * @param panelName The name of the panel to show when clicked
	 */
	private void configureButton(JButton button, String panelName) {
		button.setFont(new Font("Arial", Font.BOLD, 14));
		button.setForeground(Color.WHITE);
		button.setBackground(new Color(52, 152, 219));
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setPreferredSize(new Dimension(150, 40));
		button.addActionListener(e -> cardLayout.show(mainPanel, panelName));
	}
}
