package com.berkant.kagan.haluk.irem.dietapp;

public class Main {
    private static boolean testMode = false;
    
    public static void setTestMode(boolean mode) {
        testMode = mode;
    }
    
    public static boolean isTestMode() {
        return testMode;
    }
    
    public static void main(String[] args) {
        // Create services
        PersonalizedDietRecommendationService dietService = new PersonalizedDietRecommendationService(null, null);
        
        // Create menu class
        PersonalizedDietRecommendationMenu menu = new PersonalizedDietRecommendationMenu(dietService);
        
        // Start menu
        if (!testMode) {
            menu.displayMenu();
        }
    }
} 