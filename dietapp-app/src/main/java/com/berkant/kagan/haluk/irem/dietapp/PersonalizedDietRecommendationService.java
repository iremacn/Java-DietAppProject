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
        
        // Calculate BMR (Basal Metabolic Rate)
        double bmr;
        if (gender.equals("Male")) {
            bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
        } else {
            bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
        }

        // Calculate daily calorie needs based on activity level
        double dailyCalories;
        switch (activityLevel) {
            case "Sedentary":
                dailyCalories = bmr * 1.2;
                break;
            case "Light":
                dailyCalories = bmr * 1.375;
                break;
            case "Moderate":
                dailyCalories = bmr * 1.55;
                break;
            case "Active":
                dailyCalories = bmr * 1.725;
                break;
            case "Very Active":
                dailyCalories = bmr * 1.9;
                break;
            default:
                dailyCalories = bmr * 1.2;
        }

        recommendations.add(String.format("Daily Calorie Need: %.0f kcal", dailyCalories));
        recommendations.add(String.format("Protein Need: %.0f grams", weight * 1.6));
        recommendations.add(String.format("Carbohydrate Need: %.0f grams", (dailyCalories * 0.5) / 4));
        recommendations.add(String.format("Fat Need: %.0f grams", (dailyCalories * 0.3) / 9));

        // Get suitable meal recommendations from database
        String sql = "SELECT * FROM meals WHERE calories <= ? ORDER BY RAND() LIMIT 5";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, dailyCalories / 3); // Maximum calories per meal
            try (ResultSet rs = stmt.executeQuery()) {
                recommendations.add("\nRecommended Meals:");
                while (rs.next()) {
                    String meal = String.format("%s - Calories: %d, Protein: %.1fg, Carbs: %.1fg, Fat: %.1fg",
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