package com.berkant.kagan.haluk.irem.dietapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class handles the personalized diet recommendation menu operations for the Diet Planner application.
 * @details The PersonalizedDietRecommendationMenu class provides menu interfaces for setting diet preferences,
 *          generating and viewing personalized diet recommendations.
 * @author haluk
 */
public class PersonalizedDietRecommendationMenu {
    private final Scanner scanner;
    private final PersonalizedDietRecommendationService dietService;
    private final List<DietRecommendation> recommendations;

    public PersonalizedDietRecommendationMenu(PersonalizedDietRecommendationService dietService) {
        this.scanner = new Scanner(System.in);
        this.dietService = dietService;
        this.recommendations = new ArrayList<>();
    }

    public void displayMenu() {
        while (true) {
            System.out.println("\n=== Personalized Diet Recommendation Menu ===");
            System.out.println("1. Generate New Diet Recommendation");
            System.out.println("2. View Previous Recommendations");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1:
                        generateNewRecommendation();
                        break;
                    case 2:
                        viewPreviousRecommendations();
                        break;
                    case 3:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private void generateNewRecommendation() {
        try {
            System.out.print("Enter age: ");
            int age = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter weight (kg): ");
            double weight = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Enter height (cm): ");
            double height = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Enter gender (Male/Female): ");
            String gender = scanner.nextLine().trim();

            System.out.print("Enter activity level (Sedentary/Light/Moderate/Active/Very Active): ");
            String activityLevel = scanner.nextLine().trim();

            // generateRecommendations function can return a String or List<String>. Here we take the first element.
            String recommendationResult = "";
            Object result = dietService.generateRecommendations(age, weight, height, gender, activityLevel);
            if (result instanceof String) {
                recommendationResult = (String) result;
            } else if (result instanceof List) {
                List<?> list = (List<?>) result;
                if (!list.isEmpty() && list.get(0) instanceof String) {
                    recommendationResult = (String) list.get(0);
                }
            }
            recommendations.add(new DietRecommendation(age, weight, height, gender, activityLevel, recommendationResult));
            System.out.println("\nDiet Recommendation Generated Successfully!");
            System.out.println(recommendationResult);
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid numbers for age, weight, and height.");
        } catch (Exception e) {
            System.out.println("Error generating recommendation: " + e.getMessage());
        }
    }

    private void viewPreviousRecommendations() {
        if (recommendations.isEmpty()) {
            System.out.println("No previous recommendations found.");
            return;
        }

        System.out.println("\nPrevious Diet Recommendations:");
        for (int i = 0; i < recommendations.size(); i++) {
            DietRecommendation rec = recommendations.get(i);
            System.out.println("\nRecommendation #" + (i + 1));
            System.out.println("Age: " + rec.getAge());
            System.out.println("Weight: " + rec.getWeight() + " kg");
            System.out.println("Height: " + rec.getHeight() + " cm");
            System.out.println("Gender: " + rec.getGender());
            System.out.println("Activity Level: " + rec.getActivityLevel());
            System.out.println("Recommendation: " + rec.getRecommendation());
            System.out.println("-------------------");
        }
    }

    // record yerine klasik iç sınıf
    public static class DietRecommendation {
        private final int age;
        private final double weight;
        private final double height;
        private final String gender;
        private final String activityLevel;
        private final String recommendation;

        public DietRecommendation(int age, double weight, double height, String gender, String activityLevel, String recommendation) {
            this.age = age;
            this.weight = weight;
            this.height = height;
            this.gender = gender;
            this.activityLevel = activityLevel;
            this.recommendation = recommendation;
        }

        public int getAge() { return age; }
        public double getWeight() { return weight; }
        public double getHeight() { return height; }
        public String getGender() { return gender; }
        public String getActivityLevel() { return activityLevel; }
        public String getRecommendation() { return recommendation; }
    }
}

