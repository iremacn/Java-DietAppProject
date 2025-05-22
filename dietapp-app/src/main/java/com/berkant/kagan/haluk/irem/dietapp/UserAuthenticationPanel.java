/**
 * @file UserAuthenticationPanel.java
 * @brief A Swing panel that handles user authentication (login and registration)
 * @author Berkant Kagan Haluk Irem
 * @date 2024
 */

package com.berkant.kagan.haluk.irem.dietapp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * @class UserAuthenticationPanel
 * @brief A JPanel implementation that provides user authentication functionality
 * 
 * This class creates a graphical user interface for user authentication,
 * including login and registration features. It displays a logo, username
 * and password fields, and login/register buttons with a modern design.
 */
public class UserAuthenticationPanel extends JPanel {
    /** @brief Service responsible for handling authentication operations */
    private final AuthenticationService authService;
    
    /** @brief Text field for username input */
    private final JTextField usernameField;
    
    /** @brief Password field for secure password input */
    private final JPasswordField passwordField;
    
    /** @brief Callback function to be executed after successful login */
    private Runnable loginSuccessCallback;

    /**
     * @brief Constructs a new UserAuthenticationPanel
     * @param authService The authentication service to be used for login/register operations
     * 
     * Initializes the panel with a logo, title, input fields, and buttons.
     * Sets up the layout and styling of all components.
     */
    public UserAuthenticationPanel(AuthenticationService authService) {
        this.authService = authService;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Load logo
        try {
            // Load image from resources folder
            URL imageUrl = getClass().getResource("/images/logo.png");
            if (imageUrl != null) {
                Image image = ImageIO.read(imageUrl);
                // Resize logo
                Image scaledImage = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
                
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 2;
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.insets = new Insets(20, 5, 20, 5);
                add(logoLabel, gbc);
            }
        } catch (Exception e) {
            System.err.println("Logo could not be loaded: " + e.getMessage());
            e.printStackTrace();
        }

        // Title
        JLabel titleLabel = new JLabel("Diet Planner");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 5, 30, 5);
        add(titleLabel, gbc);

        // Custom background color for panel
        setBackground(new Color(236, 240, 241));

        // Username field
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(new Color(44, 62, 80));
        add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        usernameField = new JTextField(20);
        usernameField.setPreferredSize(new Dimension(200, 30));
        add(usernameField, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passLabel.setForeground(new Color(44, 62, 80));
        add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(200, 30));
        add(passwordField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        // Login button
        JButton loginButton = createStyledButton("Login", new Color(52, 152, 219));
        buttonPanel.add(loginButton);

        // Register button
        JButton registerButton = createStyledButton("Register", new Color(46, 204, 113));
        buttonPanel.add(registerButton);

        // Buton panelini ekle
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 5, 5, 5);
        add(buttonPanel, gbc);

        // Login butonu işlevi
        loginButton.addActionListener(e -> handleLogin());

        // Register butonu işlevi
        registerButton.addActionListener(e -> handleRegister());
    }

    /**
     * @brief Creates a styled button with hover effects
     * @param text The text to display on the button
     * @param backgroundColor The background color of the button
     * @return A styled JButton instance
     */
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 35));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Hover efekti
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }

    /**
     * @brief Handles the login button click event
     * 
     * Validates the input fields and attempts to authenticate the user.
     * Shows appropriate error messages if validation fails or authentication
     * is unsuccessful.
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Username and password cannot be empty!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (authService.login(username, password)) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                if (loginSuccessCallback != null) {
                    loginSuccessCallback.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!", 
                    "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error during login: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @brief Handles the register button click event
     * 
     * Validates the input fields and attempts to register a new user.
     * Shows appropriate error messages if validation fails or registration
     * is unsuccessful.
     */
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Username and password cannot be empty!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (authService.register(username, password, username + "@example.com", username)) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now login.");
                usernameField.setText("");
                passwordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "This username is already taken!",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error during registration: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @brief Sets the callback function to be executed after successful login
     * @param callback The Runnable to be executed after successful login
     */
    public void setLoginSuccessCallback(Runnable callback) {
        this.loginSuccessCallback = callback;
    }
}