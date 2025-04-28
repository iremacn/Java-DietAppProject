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

public class UserAuthenticationPanel extends JPanel {
    private final AuthenticationService authService;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private Runnable loginSuccessCallback;

    public UserAuthenticationPanel(AuthenticationService authService) {
        this.authService = authService;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Logo yükleme
        try {
            // Görseli resources klasöründen yükle
            URL imageUrl = getClass().getResource("/images/logo.png");
            if (imageUrl != null) {
                Image image = ImageIO.read(imageUrl);
                // Logoyu yeniden boyutlandır
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
            System.err.println("Logo yüklenemedi: " + e.getMessage());
            e.printStackTrace();
        }

        // Başlık
        JLabel titleLabel = new JLabel("Diet Planner");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(41, 128, 185));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 5, 30, 5);
        add(titleLabel, gbc);

        // Panel için özel arka plan rengi
        setBackground(new Color(236, 240, 241));

        // Username alanı
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        JLabel userLabel = new JLabel("Kullanıcı Adı:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(new Color(44, 62, 80));
        add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        usernameField = new JTextField(20);
        usernameField.setPreferredSize(new Dimension(200, 30));
        add(usernameField, gbc);

        // Password alanı
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel passLabel = new JLabel("Şifre:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passLabel.setForeground(new Color(44, 62, 80));
        add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(200, 30));
        add(passwordField, gbc);

        // Butonlar için panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        // Login butonu
        JButton loginButton = createStyledButton("Giriş Yap", new Color(52, 152, 219));
        buttonPanel.add(loginButton);

        // Register butonu
        JButton registerButton = createStyledButton("Kayıt Ol", new Color(46, 204, 113));
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

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Kullanıcı adı ve şifre boş olamaz!", 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (authService.login(username, password)) {
                JOptionPane.showMessageDialog(this, "Giriş başarılı!");
                if (loginSuccessCallback != null) {
                    loginSuccessCallback.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Hatalı kullanıcı adı veya şifre!", 
                    "Giriş Hatası", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Giriş sırasında hata: " + e.getMessage(),
                "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Kullanıcı adı ve şifre boş olamaz!", 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (authService.register(username, password, username + "@example.com", username)) {
                JOptionPane.showMessageDialog(this, "Kayıt başarılı! Şimdi giriş yapabilirsiniz.");
                usernameField.setText("");
                passwordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Bu kullanıcı adı zaten kullanılıyor!",
                    "Kayıt Hatası", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Kayıt sırasında hata: " + e.getMessage(),
                "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setLoginSuccessCallback(Runnable callback) {
        this.loginSuccessCallback = callback;
    }
}