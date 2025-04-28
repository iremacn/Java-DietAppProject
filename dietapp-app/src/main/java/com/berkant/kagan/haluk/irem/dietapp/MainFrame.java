package com.berkant.kagan.haluk.irem.dietapp;

import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private CalorieTrackingPanel calorieTrackingPanel;
    private MealPlanningPanel mealPlanningPanel;
    private PersonalizedDietPanel personalizedDietPanel;
    private ShoppingListPanel shoppingListPanel;
    private UserAuthenticationPanel authPanel;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    public MainFrame() {
        setTitle("Diet Planner");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Veritabanını başlat
        DatabaseHelper.initializeDatabase();

        // Card Layout için ana panel
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);

        // Servis nesnelerini oluştur
        AuthenticationService authService = new AuthenticationService();
        MealPlanningService mealService = new MealPlanningService(null);
        CalorieNutrientTrackingService calorieService = new CalorieNutrientTrackingService(mealService);
        PersonalizedDietRecommendationService dietService = new PersonalizedDietRecommendationService(calorieService, mealService);
        ShoppingListService shoppingService = new ShoppingListService(mealService);

        // Login panelini oluştur
        authPanel = new UserAuthenticationPanel(authService);
        authPanel.setLoginSuccessCallback(this::showMainMenu);

        // Panel nesnelerini oluştur
        calorieTrackingPanel = new CalorieTrackingPanel(calorieService);
        mealPlanningPanel = new MealPlanningPanel(mealService);
        personalizedDietPanel = new PersonalizedDietPanel(dietService);
        shoppingListPanel = new ShoppingListPanel(shoppingService);

        // TabbedPane oluştur ve panelleri ekle
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Kalori Takibi", calorieTrackingPanel);
        tabbedPane.addTab("Yemek Planlama", mealPlanningPanel);
        tabbedPane.addTab("Kişisel Diyet", personalizedDietPanel);
        tabbedPane.addTab("Alışveriş Listesi", shoppingListPanel);

        // Card Layout'a panelleri ekle
        mainPanel.add(authPanel, "login");
        mainPanel.add(tabbedPane, "main");

        // Ana paneli frame'e ekle
        add(mainPanel);

        // Başlangıçta login ekranını göster
        cardLayout.show(mainPanel, "login");
    }

    public void showMainMenu() {
        cardLayout.show(mainPanel, "main");
    }

    public static void main(String[] args) {
        try {
            // SQLite JDBC sürücüsünü yükle
            Class.forName("org.sqlite.JDBC");
            
            SwingUtilities.invokeLater(() -> {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            });
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, 
                "SQLite JDBC sürücüsü bulunamadı: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
