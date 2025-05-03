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
    private Scanner scanner;
    private final PersonalizedDietRecommendationService dietService;
    private final AuthenticationService authService;
    private final List<DietRecommendation> recommendations;

    public PersonalizedDietRecommendationMenu(PersonalizedDietRecommendationService dietService) {
        this.scanner = new Scanner(System.in);
        this.dietService = dietService;
        this.authService = null;
        this.recommendations = new ArrayList<>();
    }
    
    public PersonalizedDietRecommendationMenu(PersonalizedDietRecommendationService dietService, 
                                             AuthenticationService authService, 
                                             Scanner scanner) {
        this.scanner = scanner;
        this.dietService = dietService;
        this.authService = authService;
        this.recommendations = new ArrayList<>();
    }

    public void displayMenu() {
        while (true) {
            System.out.println("\n=== Personalized Diet Recommendation Menu ===");
            System.out.println("1. Generate New Diet Recommendation");
            System.out.println("2. View Previous Recommendations");
            System.out.println("3. View Personalized Diet Recommendation");
            System.out.println("4. View Example Diet Plans");
            System.out.println("5. Exit");
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
                        handleViewRecommendations();
                        break;
                    case 4:
                        handleViewExampleDietPlans();
                        break;
                    case 5:
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
    
    // Helper method to get user's choice, added for testing purposes
    private int getUserChoice() {
        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return -1;
            }
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    // Helper method for setting user diet preferences
    private void handleSetDietPreferences() {
        try {
            // Get username from auth service
            String username = authService != null && authService.getCurrentUser() != null ? 
                              authService.getCurrentUser().getUsername() : "guest";
            
            System.out.println("\nSelect diet type:");
            System.out.println("1. Balanced");
            System.out.println("2. Low-Carb");
            int dietTypeChoice = getUserChoice();
            DietType dietType = DietType.BALANCED; // Default
            
            switch (dietTypeChoice) {
                case 1:
                    dietType = DietType.BALANCED;
                    break;
                case 2:
                    dietType = DietType.LOW_CARB;
                    break;
                default:
                    System.out.println("Invalid choice. Using Balanced diet type.");
            }
            
            System.out.println("\nSelect weight goal:");
            System.out.println("1. Lose weight");
            System.out.println("2. Maintain weight");
            WeightGoal weightGoal = WeightGoal.MAINTAIN; // Default
            int weightGoalChoice = getUserChoice();
            
            switch (weightGoalChoice) {
                case 1:
                    weightGoal = WeightGoal.LOSE;
                    break;
                case 2:
                    weightGoal = WeightGoal.MAINTAIN;
                    break;
                default:
                    System.out.println("Invalid choice. Using Maintain weight goal.");
            }
            
            System.out.println("\nDo you have any health conditions? (Y/N)");
            String hasConditions = scanner.nextLine().trim().toUpperCase();
            List<String> healthConditions = new ArrayList<>();
            
            if ("Y".equals(hasConditions)) {
                System.out.println("Enter health conditions (comma separated):");
                String[] conditions = scanner.nextLine().split(",");
                for (String condition : conditions) {
                    healthConditions.add(condition.trim());
                }
            }
            
            System.out.println("\nDo you want to exclude any foods? (Y/N)");
            String hasExclusions = scanner.nextLine().trim().toUpperCase();
            List<String> excludedFoods = new ArrayList<>();
            
            if ("Y".equals(hasExclusions)) {
                System.out.println("Enter foods to exclude (comma separated):");
                String[] foods = scanner.nextLine().split(",");
                for (String food : foods) {
                    excludedFoods.add(food.trim());
                }
            }
           
            // Call service to set user diet profile
            boolean success = dietService.setUserDietProfile(username, dietType, healthConditions, weightGoal, excludedFoods);
            
            if (success) {
                System.out.println("Diet preferences updated successfully!");
            } else {
                System.out.println("Failed to update diet preferences. Please try again later.");
            }
        } catch (Exception e) {
            System.out.println("Error setting diet preferences: " + e.getMessage());
        }
    }

    // Helper method for viewing recommendations
    private void handleViewRecommendations() {
        try {
            // Get username from auth service
            String username = authService != null && authService.getCurrentUser() != null ? 
                              authService.getCurrentUser().getUsername() : "guest";
            
            // Get recommendations
            PersonalizedDietRecommendationService.DietRecommendation recommendation = 
                dietService.generateRecommendations(username, 'M', 30, 170, 70, 2);
            
            if (recommendation == null) {
                System.out.println("No recommendations found. Please generate recommendations first.");
                return;
            }
            
            // Display recommendation details
            System.out.println("\n===== Your Personalized Diet Recommendation =====");
            System.out.println("Daily Calories: " + recommendation.getDailyCalories() + " kcal");
            
            // Display macronutrient distribution
            System.out.println("\nMacronutrient Distribution:");
            System.out.println(recommendation.getMacros().toString());
            
            // Display meals
            System.out.println("\nRecommended Meals:");
            for (PersonalizedDietRecommendationService.RecommendedMeal meal : recommendation.getMeals()) {
                System.out.println("\n" + meal.getMealType() + " (" + meal.getTargetCalories() + " kcal):");
                for (Food food : meal.getFoods()) {
                    System.out.println("- " + food.getName() + " (" + food.getCalories() + " kcal)");
                }
            }
            
            // Display dietary guidelines
            System.out.println("\nDietary Guidelines:");
            for (String guideline : recommendation.getDietaryGuidelines()) {
                System.out.println("- " + guideline);
            }
            
        } catch (Exception e) {
            System.out.println("Error viewing recommendations: " + e.getMessage());
        }
    }
    
    // Helper method for viewing example diet plans
    private void handleViewExampleDietPlans() {
        try {
            String[] examplePlans = dietService.getDefaultExampleDietPlans();
            
            if (examplePlans == null || examplePlans.length == 0) {
                System.out.println("No example diet plans available.");
                return;
            }
            
            System.out.println("\n===== Example Diet Plans =====");
            for (String plan : examplePlans) {
                System.out.println("\n" + plan);
                System.out.println("-------------------");
            }
            
        } catch (Exception e) {
            System.out.println("Error viewing example diet plans: " + e.getMessage());
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
    
    // Enum for diet types needed by tests
    public enum DietType {
        BALANCED,
        LOW_CARB,
        HIGH_PROTEIN,
        VEGETARIAN,
        VEGAN
    }
    
    // Enum for weight goals needed by tests
    public enum WeightGoal {
        LOSE,
        MAINTAIN,
        GAIN
    }
}

