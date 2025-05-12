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

public class MainnFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel mainPanel;
	private CardLayout cardLayout;
	private CalorieTrackingPanel calorieTrackingPanel;
	private MealPlanningPanel mealPlanningPanel;
	private PersonalizedDietPanel personalizedDietPanel;
	private ShoppingListPanel shoppingListPanel;
	private UserAuthenticationPanel authPanel;
	private JPanel buttonPanel;
	private JButton calorieButton;
	private JButton mealButton;
	private JButton dietButton;
	private JButton shoppingButton;

	/**
	 * Launch the application.
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
	 * Create the frame.
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
		
		mealButton = new JButton("Meal Planning");
		mealButton.setFont(new Font("Arial", Font.BOLD, 14));
		mealButton.setForeground(Color.WHITE);
		mealButton.setBackground(new Color(52, 152, 219));
		mealButton.setFocusPainted(false);
		mealButton.setBorderPainted(false);
		mealButton.setPreferredSize(new Dimension(150, 40));
		mealButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cardLayout.show(mainPanel, "meal");
			}
		});
		
		calorieButton = new JButton("Calorie Tracking");
		buttonPanel.add(calorieButton);
		calorieButton.setFont(new Font("Arial", Font.BOLD, 14));
		calorieButton.setForeground(Color.WHITE);
		calorieButton.setBackground(new Color(52, 152, 219));
		calorieButton.setFocusPainted(false);
		calorieButton.setBorderPainted(false);
		calorieButton.setPreferredSize(new Dimension(150, 40));
		calorieButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cardLayout.show(mainPanel, "calorie");
			}
		});
		buttonPanel.add(mealButton);
		
		dietButton = new JButton("Personalized Diet");
		dietButton.setFont(new Font("Arial", Font.BOLD, 14));
		dietButton.setForeground(Color.WHITE);
		dietButton.setBackground(new Color(52, 152, 219));
		dietButton.setFocusPainted(false);
		dietButton.setBorderPainted(false);
		dietButton.setPreferredSize(new Dimension(150, 40));
		dietButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cardLayout.show(mainPanel, "diet");
			}
		});
		buttonPanel.add(dietButton);
		
		shoppingButton = new JButton("Shopping List");
		shoppingButton.setFont(new Font("Arial", Font.BOLD, 14));
		shoppingButton.setForeground(Color.WHITE);
		shoppingButton.setBackground(new Color(52, 152, 219));
		shoppingButton.setFocusPainted(false);
		shoppingButton.setBorderPainted(false);
		shoppingButton.setPreferredSize(new Dimension(150, 40));
		shoppingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cardLayout.show(mainPanel, "shopping");
			}
		});
		buttonPanel.add(shoppingButton);
		
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
	
	public void showMainMenu() {
		cardLayout.show(mainPanel, "calorie");
		buttonPanel.setVisible(true);
	}
}
