package com.berkant.kagan.haluk.irem.dietapp;

public class Main {
    public static void main(String[] args) {
        // Servisleri oluştur
        PersonalizedDietRecommendationService dietService = new PersonalizedDietRecommendationService(null, null);
        
        // Menu sınıfını oluştur
        PersonalizedDietRecommendationMenu menu = new PersonalizedDietRecommendationMenu(dietService);
        
        // Menüyü başlat
        menu.displayMenu();
    }
} 