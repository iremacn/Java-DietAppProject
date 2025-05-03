package com.berkant.kagan.haluk.irem.dietapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.berkant.kagan.haluk.irem.dietapp.PersonalizedDietRecommendationService.DietType;
import com.berkant.kagan.haluk.irem.dietapp.PersonalizedDietRecommendationService.WeightGoal;

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
    
    /**
     * Diet types for personalized recommendations
     */
    public enum DietType {
        BALANCED, LOW_CARB, HIGH_PROTEIN, VEGETARIAN, VEGAN
    }
    
    /**
     * Weight goals for personalized recommendations
     */
    public enum WeightGoal {
        LOSE, MAINTAIN, GAIN
    }
    
    /**
     * User diet profile class to store user preferences
     */
    public class UserDietProfile {
        private DietType dietType;
        private List<String> healthConditions;
        private WeightGoal weightGoal;
        private List<String> excludedFoods;
        
        public UserDietProfile(DietType dietType, List<String> healthConditions, 
                               WeightGoal weightGoal, List<String> excludedFoods) {
            this.dietType = dietType;
            this.healthConditions = healthConditions;
            this.weightGoal = weightGoal;
            this.excludedFoods = excludedFoods;
        }
        
        public DietType getDietType() {
            return dietType;
        }
        
        public List<String> getHealthConditions() {
            return healthConditions;
        }
        
        public WeightGoal getWeightGoal() {
            return weightGoal;
        }
        
        public List<String> getExcludedFoods() {
            return excludedFoods;
        }
    }

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
    
    /**
     * Macronutrient distribution class
     */
    public class MacronutrientDistribution {
        private int proteinGrams;
        private int carbGrams;
        private int fatGrams;
        
        public MacronutrientDistribution(int proteinGrams, int carbGrams, int fatGrams) {
            this.proteinGrams = proteinGrams;
            this.carbGrams = carbGrams;
            this.fatGrams = fatGrams;
        }
        
        public int getProteinGrams() {
            return proteinGrams;
        }
        
        public int getCarbGrams() {
            return carbGrams;
        }
        
        public int getFatGrams() {
            return fatGrams;
        }
        
        @Override
        public String toString() {
            return "Protein: " + proteinGrams + "g, Carbs: " + carbGrams + "g, Fat: " + fatGrams + "g";
        }
    }
    
    /**
     * Recommended meal class
     */
    public class RecommendedMeal {
        private String mealType;
        private List<Food> foods;
        private int targetCalories;
        private int targetProtein;
        private int targetCarbs;
        private int targetFat;
        
        public RecommendedMeal(String mealType, List<Food> foods, int targetCalories, 
                             int targetProtein, int targetCarbs, int targetFat) {
            this.mealType = mealType;
            this.foods = foods;
            this.targetCalories = targetCalories;
            this.targetProtein = targetProtein;
            this.targetCarbs = targetCarbs;
            this.targetFat = targetFat;
        }
        
        public String getMealType() {
            return mealType;
        }
        
        public List<Food> getFoods() {
            return foods;
        }
        
        public int getTargetCalories() {
            return targetCalories;
        }
        
        public int getTargetProtein() {
            return targetProtein;
        }
        
        public int getTargetCarbs() {
            return targetCarbs;
        }
        
        public int getTargetFat() {
            return targetFat;
        }
        
        public int getTotalCalories() {
            int total = 0;
            for (Food food : foods) {
                total += food.getCalories();
            }
            return total;
        }
    }
    
    /**
     * Diet recommendation class
     */
    public class DietRecommendation {
        private int dailyCalories;
        private MacronutrientDistribution macros;
        private List<RecommendedMeal> meals;
        private List<String> dietaryGuidelines;
        
        public DietRecommendation(int dailyCalories, MacronutrientDistribution macros, 
                                List<RecommendedMeal> meals, List<String> dietaryGuidelines) {
            this.dailyCalories = dailyCalories;
            this.macros = macros;
            this.meals = meals;
            this.dietaryGuidelines = dietaryGuidelines;
        }
        
        public int getDailyCalories() {
            return dailyCalories;
        }
        
        public MacronutrientDistribution getMacros() {
            return macros;
        }
        
        public List<RecommendedMeal> getMeals() {
            return meals;
        }
        
        public List<String> getDietaryGuidelines() {
            return dietaryGuidelines;
        }
    }
    
    /**
     * Set user diet profile preferences
     * 
     * @param username User's username
     * @param dietType Type of diet
     * @param healthConditions List of health conditions
     * @param weightGoal Weight goal
     * @param excludedFoods List of excluded foods
     * @return true if successfully set
     */
    public boolean setUserDietProfile(String username, DietType dietType, 
                                   List<String> healthConditions,
                                   WeightGoal weightGoal,
                                   List<String> excludedFoods) {
        try {
            // Implement database logic to store user diet profile
            // This is a stub implementation
            return true;
        } catch (Exception e) {
            System.out.println("Error occurred while updating diet profile: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get user diet profile
     * 
     * @param username User's username
     * @return UserDietProfile object or null if not found
     */
    public UserDietProfile getUserDietProfile(String username) {
        try {
            // Implement database logic to retrieve user diet profile
            // This is a stub implementation
            return new UserDietProfile(DietType.BALANCED, new ArrayList<>(), 
                                  WeightGoal.MAINTAIN, new ArrayList<>());
        } catch (Exception e) {
            System.out.println("Error occurred while retrieving diet profile: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Generate personalized diet recommendations based on user profile and physical stats
     * 
     * @param username User's username
     * @param gender User's gender
     * @param age User's age
     * @param heightCm User's height in cm
     * @param weightKg User's weight in kg
     * @param activityLevel User's activity level
     * @return DietRecommendation object
     */
    public DietRecommendation generateRecommendations(String username, char gender, int age, 
                                                  double heightCm, double weightKg, 
                                                  int activityLevel) {
        // Get user diet profile
        UserDietProfile profile = getUserDietProfile(username);
        
        if (profile == null) {
            // Create default profile if none exists
            profile = new UserDietProfile(DietType.BALANCED, new ArrayList<>(), 
                                      WeightGoal.MAINTAIN, new ArrayList<>());
        }
        
        // Calculate suggested calories using calorie service
        int baseCalories = calorieService.calculateSuggestedCalories(gender, age, heightCm, weightKg, activityLevel);
        
        // Adjust calories based on weight goal
        int adjustedCalories = adjustCaloriesForWeightGoal(baseCalories, profile.getWeightGoal());
        
        // Calculate macronutrient distribution
        MacronutrientDistribution macros = calculateMacronutrients(adjustedCalories, profile.getDietType());
        
        // Generate meal plan
        List<RecommendedMeal> meals = generateMealPlan(adjustedCalories, macros, profile);
        
        // Generate dietary guidelines
        List<String> guidelines = generateDietaryGuidelines(profile);
        
        // Create and return diet recommendation
        return new DietRecommendation(adjustedCalories, macros, meals, guidelines);
    }
    
    /**
     * Adjust calories based on weight goal
     * 
     * @param baseCalories Base calories
     * @param weightGoal Weight goal
     * @return Adjusted calories
     */
    private int adjustCaloriesForWeightGoal(int baseCalories, WeightGoal weightGoal) {
        switch (weightGoal) {
            case LOSE:
                return (int)(baseCalories * 0.85); // 15% deficit
            case GAIN:
                return (int)(baseCalories * 1.15); // 15% surplus
            case MAINTAIN:
            default:
                return baseCalories;
        }
    }
    
    /**
     * Calculate macronutrient distribution based on diet type
     * 
     * @param calories Total calories
     * @param dietType Diet type
     * @return MacronutrientDistribution object
     */
    private MacronutrientDistribution calculateMacronutrients(int calories, DietType dietType) {
        // Calculate macronutrient distribution based on diet type
        int proteinGrams, carbGrams, fatGrams;
        
        switch (dietType) {
            case LOW_CARB:
                // 30% protein, 20% carbs, 50% fat
                proteinGrams = (int)Math.round(calories * 0.30 / 4); // 4 calories per gram of protein
                carbGrams = (int)Math.round(calories * 0.20 / 4);    // 4 calories per gram of carbs
                fatGrams = (int)Math.round(calories * 0.50 / 9);     // 9 calories per gram of fat
                break;
            case HIGH_PROTEIN:
                // 40% protein, 30% carbs, 30% fat
                proteinGrams = (int)Math.round(calories * 0.40 / 4);
                carbGrams = (int)Math.round(calories * 0.30 / 4);
                fatGrams = (int)Math.round(calories * 0.30 / 9);
                break;
            case VEGETARIAN:
            case VEGAN:
                // 20% protein, 60% carbs, 20% fat
                proteinGrams = (int)Math.round(calories * 0.20 / 4);
                carbGrams = (int)Math.round(calories * 0.60 / 4);
                fatGrams = (int)Math.round(calories * 0.20 / 9);
                break;
            case BALANCED:
            default:
                // 25% protein, 50% carbs, 25% fat
                proteinGrams = (int)Math.round(calories * 0.25 / 4);
                carbGrams = (int)Math.round(calories * 0.50 / 4);
                fatGrams = (int)Math.round(calories * 0.25 / 9);
                break;
        }
        
        return new MacronutrientDistribution(proteinGrams, carbGrams, fatGrams);
    }
    
    /**
     * Generate meal plan based on calories, macros, and diet preferences
     * 
     * @param calories Total calories
     * @param macros Macronutrient distribution
     * @param profile User diet profile
     * @return List of RecommendedMeal objects
     */
    private List<RecommendedMeal> generateMealPlan(int calories, MacronutrientDistribution macros, 
                                             UserDietProfile profile) {
        List<RecommendedMeal> meals = new ArrayList<>();
        
        // Calculate calories for each meal
        int breakfastCalories = (int)(calories * 0.25); // 25% of daily calories
        int lunchCalories = (int)(calories * 0.35);     // 35% of daily calories
        int dinnerCalories = (int)(calories * 0.30);    // 30% of daily calories
        int snackCalories = (int)(calories * 0.10);     // 10% of daily calories
        
        // Calculate macros for each meal (proportional to calories)
        int breakfastProtein = (int)(macros.getProteinGrams() * 0.25);
        int breakfastCarbs = (int)(macros.getCarbGrams() * 0.25);
        int breakfastFat = (int)(macros.getFatGrams() * 0.25);
        
        int lunchProtein = (int)(macros.getProteinGrams() * 0.35);
        int lunchCarbs = (int)(macros.getCarbGrams() * 0.35);
        int lunchFat = (int)(macros.getFatGrams() * 0.35);
        
        int dinnerProtein = (int)(macros.getProteinGrams() * 0.30);
        int dinnerCarbs = (int)(macros.getCarbGrams() * 0.30);
        int dinnerFat = (int)(macros.getFatGrams() * 0.30);
        
        int snackProtein = (int)(macros.getProteinGrams() * 0.10);
        int snackCarbs = (int)(macros.getCarbGrams() * 0.10);
        int snackFat = (int)(macros.getFatGrams() * 0.10);
        
        // Get food options for each meal type
        Food[] breakfastOptions = getAppropriateOptions(mealService.getBreakfastOptions(), profile);
        Food[] lunchOptions = getAppropriateOptions(mealService.getLunchOptions(), profile);
        Food[] dinnerOptions = getAppropriateOptions(mealService.getDinnerOptions(), profile);
        Food[] snackOptions = getAppropriateOptions(mealService.getSnackOptions(), profile);
        
        // Create meals
        meals.add(createMealRecommendation("Breakfast", breakfastOptions, 
                                      breakfastCalories, breakfastProtein, breakfastCarbs, breakfastFat));
        meals.add(createMealRecommendation("Lunch", lunchOptions, 
                                      lunchCalories, lunchProtein, lunchCarbs, lunchFat));
        meals.add(createMealRecommendation("Dinner", dinnerOptions, 
                                      dinnerCalories, dinnerProtein, dinnerCarbs, dinnerFat));
        meals.add(createMealRecommendation("Snack", snackOptions, 
                                      snackCalories, snackProtein, snackCarbs, snackFat));
        
        return meals;
    }
    
    /**
     * Create a meal recommendation with appropriate foods
     * 
     * @param mealType Meal type (breakfast, lunch, etc.)
     * @param options Food options
     * @param targetCalories Target calories
     * @param targetProtein Target protein
     * @param targetCarbs Target carbs
     * @param targetFat Target fat
     * @return RecommendedMeal object
     */
    private RecommendedMeal createMealRecommendation(String mealType, Food[] options, 
                                               int targetCalories, int targetProtein, 
                                               int targetCarbs, int targetFat) {
        // Select appropriate foods to meet target calories and macros
        List<Food> selectedFoods = new ArrayList<>();
        
        // Simple food selection (just take first 1-3 items for demo)
        int totalCalories = 0;
        int count = 0;
        
        for (Food food : options) {
            if (count < 3 && totalCalories < targetCalories) {
                selectedFoods.add(food);
                totalCalories += food.getCalories();
                count++;
            }
        }
        
        return new RecommendedMeal(mealType, selectedFoods, targetCalories, 
                                targetProtein, targetCarbs, targetFat);
    }
    
    /**
     * Filter food options based on diet type and excluded foods
     * 
     * @param allOptions All food options
     * @param profile User diet profile
     * @return Filtered food options
     */
    private Food[] getAppropriateOptions(Food[] allOptions, UserDietProfile profile) {
        List<Food> filteredOptions = new ArrayList<>();
        
        for (Food food : allOptions) {
            boolean isAppropriate = true;
            String foodName = food.getName().toLowerCase();
            
            // Check for excluded foods
            for (String excludedFood : profile.getExcludedFoods()) {
                if (foodName.contains(excludedFood.toLowerCase())) {
                    isAppropriate = false;
                    break;
                }
            }
            
            // Check for diet type restrictions
            if (isAppropriate) {
                switch (profile.getDietType()) {
                    case VEGETARIAN:
                        if (foodName.contains("chicken") || foodName.contains("beef") || 
                            foodName.contains("fish") || foodName.contains("meat")) {
                            isAppropriate = false;
                        }
                        break;
                    case VEGAN:
                        if (foodName.contains("chicken") || foodName.contains("beef") || 
                            foodName.contains("fish") || foodName.contains("meat") || 
                            foodName.contains("milk") || foodName.contains("cheese") || 
                            foodName.contains("egg") || foodName.contains("yogurt")) {
                            isAppropriate = false;
                        }
                        break;
                    default:
                        // No additional restrictions for other diet types
                        break;
                }
            }
            
            if (isAppropriate) {
                filteredOptions.add(food);
            }
        }
        
        return filteredOptions.toArray(new Food[0]);
    }
    
    /**
     * Generate dietary guidelines based on user profile
     * 
     * @param profile User diet profile
     * @return List of dietary guidelines
     */
    private List<String> generateDietaryGuidelines(UserDietProfile profile) {
        List<String> guidelines = new ArrayList<>();
        
        // Add diet type specific guidelines
        switch (profile.getDietType()) {
            case BALANCED:
                guidelines.add("Focus on a balanced diet with a variety of whole foods.");
                guidelines.add("Include lean proteins, complex carbohydrates, and healthy fats in each meal.");
                guidelines.add("Aim for at least 5 servings of fruits and vegetables per day.");
                break;
            case LOW_CARB:
                guidelines.add("Limit intake of bread, pasta, rice, and other high-carb foods.");
                guidelines.add("Focus on non-starchy vegetables, proteins, and healthy fats.");
                guidelines.add("Choose low glycemic index carbohydrates when consumed.");
                break;
            case HIGH_PROTEIN:
                guidelines.add("Include a protein source with every meal.");
                guidelines.add("Focus on lean protein sources like chicken, fish, lean beef, eggs, and plant proteins.");
                guidelines.add("Space protein intake throughout the day for optimal muscle protein synthesis.");
                break;
            case VEGETARIAN:
                guidelines.add("Ensure adequate protein intake from eggs, dairy, legumes, tofu, and plant proteins.");
                guidelines.add("Include a variety of plant foods to get all essential amino acids.");
                guidelines.add("Consider supplementing with vitamin B12 if not consuming dairy or eggs regularly.");
                break;
            case VEGAN:
                guidelines.add("Focus on complete protein sources like tofu, tempeh, seitan, and complementary protein combinations.");
                guidelines.add("Include a variety of plant foods to ensure adequate nutrient intake.");
                guidelines.add("Consider supplements for vitamin B12, vitamin D, omega-3, and possibly iron.");
                break;

        }
        
        // Add weight goal specific guidelines
        switch (profile.getWeightGoal()) {
            case LOSE:
                guidelines.add("Maintain a moderate calorie deficit of about 15% below maintenance level.");
                guidelines.add("Include regular physical activity, combining cardio and strength training.");
                guidelines.add("Focus on protein intake to preserve muscle mass during weight loss.");
                break;
            case GAIN:
                guidelines.add("Maintain a calorie surplus of about 15% above maintenance level.");
                guidelines.add("Focus on strength training to promote muscle growth.");
                guidelines.add("Ensure adequate protein intake to support muscle protein synthesis.");
                break;
            case MAINTAIN:
                guidelines.add("Monitor your weight regularly and adjust calories as needed to maintain.");
                guidelines.add("Focus on overall nutrition quality rather than restriction.");
                guidelines.add("Include regular physical activity for overall health benefits.");
                break;
        }
        
        // Add health condition specific guidelines
        for (String condition : profile.getHealthConditions()) {
            if (condition.toLowerCase().contains("diabetes")) {
                guidelines.add("Monitor carbohydrate intake and focus on low glycemic index foods.");
                guidelines.add("Maintain consistent meal timing to help regulate blood sugar levels.");
                guidelines.add("Limit added sugars and highly processed foods.");
            } else if (condition.toLowerCase().contains("hypertension")) {
                guidelines.add("Limit sodium intake to less than 2,300 mg per day.");
                guidelines.add("Focus on foods rich in potassium, magnesium, and calcium.");
                guidelines.add("Include foods with heart-healthy omega-3 fatty acids like fatty fish.");
            } else if (condition.toLowerCase().contains("cholesterol")) {
                guidelines.add("Limit saturated and trans fats.");
                guidelines.add("Include foods rich in soluble fiber like oats, beans, and fruits.");
                guidelines.add("Consider plant sterols and stanols to help lower cholesterol.");
            }
        }
        
        return guidelines;
    }
    
    /**
     * Get default example diet plans
     * 
     * @return Array of example diet plans as strings
     */
    public String[] getDefaultExampleDietPlans() {
        // Create example diet plans for each diet type
        String[] plans = new String[5];
        
        plans[0] = "Balanced Diet Plan:\n" +
                 "- Focus on whole foods with a balance of all macronutrients\n" +
                 "- Sample Day: Eggs and oatmeal for breakfast, chicken salad for lunch, " +
                 "salmon with vegetables and quinoa for dinner, yogurt and fruit for snacks.";
        
        plans[1] = "Low-Carb Diet Plan:\n" +
                 "- Reduces carbohydrate intake and increases protein and fat\n" +
                 "- Sample Day: Eggs and avocado for breakfast, chicken and vegetable salad for lunch, " +
                 "steak with non-starchy vegetables for dinner, nuts and cheese for snacks.";
        
        plans[2] = "High-Protein Diet Plan:\n" +
                 "- Emphasizes protein intake with moderate carbs and fat\n" +
                 "- Sample Day: Protein smoothie for breakfast, turkey wrap for lunch, " +
                 "chicken breast with sweet potato and vegetables for dinner, Greek yogurt for snacks.";
        
        plans[3] = "Vegetarian Diet Plan:\n" +
                 "- Plant-based diet that includes dairy and eggs but no meat\n" +
                 "- Sample Day: Greek yogurt with granola for breakfast, hummus wrap for lunch, " +
                 "bean and vegetable stir-fry for dinner, cheese and crackers for snacks.";
        
        plans[4] = "Vegan Diet Plan:\n" +
                 "- Entirely plant-based diet with no animal products\n" +
                 "- Sample Day: Tofu scramble for breakfast, lentil soup for lunch, " +
                 "tempeh stir-fry with vegetables and rice for dinner, fruit and nuts for snacks.";
        
        return plans;
    }
    
    private int getUserId(Connection conn, String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return -1;
        }
        
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        
        return -1;
    }

   // @Override//
    public String[] getExampleDietPlans() {
        return getDefaultExampleDietPlans();
    }
}