package com.berkant.kagan.haluk.irem.dietapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles personalized diet recommendation operations for the Diet Planner application.
 * @details The PersonalizedDietRecommendationService class provides methods for generating
 *          personalized diet recommendations based on user profile and preferences.
 * @author haluk
 */
public class PersonalizedDietRecommendationService {
    private Connection connection;
    private CalorieNutrientTrackingService calorieService;
    private MealPlanningService mealService;

    public PersonalizedDietRecommendationService(CalorieNutrientTrackingService calorieService, MealPlanningService mealService) {
        this.connection = DatabaseHelper.getConnection();
        this.calorieService = calorieService;
        this.mealService = mealService;
    }

    public List<String> generateRecommendations(int age, double weight, double height, String gender, String activityLevel) throws SQLException {
        List<String> recommendations = new ArrayList<>();
        
        // BMR (Bazal Metabolik Hız) hesaplama
        double bmr;
        if (gender.equals("Erkek")) {
            bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
        } else {
            bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
        }

        // Aktivite seviyesine göre günlük kalori ihtiyacı hesaplama
        double dailyCalories;
        switch (activityLevel) {
            case "Hareketsiz":
                dailyCalories = bmr * 1.2;
                break;
            case "Az Aktif":
                dailyCalories = bmr * 1.375;
                break;
            case "Orta Aktif":
                dailyCalories = bmr * 1.55;
                break;
            case "Çok Aktif":
                dailyCalories = bmr * 1.725;
                break;
            case "Aşırı Aktif":
                dailyCalories = bmr * 1.9;
                break;
            default:
                dailyCalories = bmr * 1.2;
        }

        recommendations.add(String.format("Günlük Kalori İhtiyacı: %.0f kcal", dailyCalories));
        recommendations.add(String.format("Protein İhtiyacı: %.0f gram", weight * 1.6));
        recommendations.add(String.format("Karbonhidrat İhtiyacı: %.0f gram", (dailyCalories * 0.5) / 4));
        recommendations.add(String.format("Yağ İhtiyacı: %.0f gram", (dailyCalories * 0.3) / 9));

        // Veritabanından uygun yemek önerileri
        String sql = "SELECT * FROM meals WHERE calories <= ? ORDER BY RAND() LIMIT 5";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, dailyCalories / 3); // Öğün başına maksimum kalori
            try (ResultSet rs = stmt.executeQuery()) {
                recommendations.add("\nÖnerilen Yemekler:");
                while (rs.next()) {
                    String meal = String.format("%s - Kalori: %d, Protein: %.1fg, Karbonhidrat: %.1fg, Yağ: %.1fg",
                        rs.getString("meal_name"),
                        rs.getInt("calories"),
                        rs.getDouble("protein"),
                        rs.getDouble("carbs"),
                        rs.getDouble("fat"));
                    recommendations.add(meal);
                }
            }
        }

        return recommendations;
    }
}