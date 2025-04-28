package com.berkant.kagan.haluk.irem.dietapp;

public class Main {
    public static void main(String[] args) {
        // Create services
        PersonalizedDietRecommendationService dietService = new PersonalizedDietRecommendationService(null, null);
        
        // Create menu class
        PersonalizedDietRecommendationMenu menu = new PersonalizedDietRecommendationMenu(dietService);
        
        // Start menu
        menu.displayMenu();
    }
} 