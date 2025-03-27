package com.berkant.kagan.haluk.irem.dietapp;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles personalized diet recommendation operations for the Diet Planner application.
 * @details The PersonalizedDietRecommendationService class provides methods for generating
 *          personalized diet recommendations based on user profile and preferences.
 * @author haluk
 */
public class PersonalizedDietRecommendationService {
    // Service dependencies
    private CalorieNutrientTrackingService calorieNutrientService;
    private MealPlanningService mealPlanningService;
   
    /**
     * Constructor for PersonalizedDietRecommendationService class.
     * 
     * @param calorieNutrientService The calorie and nutrient tracking service
     * @param mealPlanningService The meal planning service
     */
    public PersonalizedDietRecommendationService(
            CalorieNutrientTrackingService calorieNutrientService,
            MealPlanningService mealPlanningService) {
        this.calorieNutrientService = calorieNutrientService;
        this.mealPlanningService = mealPlanningService;
    }
   
    // Helper method to return default plans if they can't be retrieved from the database
    protected String[] getDefaultExampleDietPlans() {
        return new String[] {
            "Balanced Diet Plan:\n" +
            "A balanced approach focusing on whole foods, lean proteins, healthy fats, and complex carbohydrates. " +
            "This plan provides all essential nutrients in appropriate proportions.",
            
            "Low-Carb Diet Plan:\n" +
            "Reduces carbohydrate intake while increasing protein and fat. " +
            "Good for blood sugar control and may help with weight loss for some individuals.",
            
            "High-Protein Diet Plan:\n" +
            "Emphasizes increased protein intake to support muscle maintenance, growth, and satiety. " +
            "Popular for athletes and those looking to preserve muscle while losing fat.",
            
            "Vegetarian Diet Plan:\n" +
            "Excludes meat but may include dairy and eggs. " +
            "Focuses on plant proteins, whole grains, fruits, vegetables, nuts, and seeds.",
            
            "Vegan Diet Plan:\n" +
            "Excludes all animal products. " +
            "Requires careful planning to ensure adequate protein, vitamin B12, iron, zinc, and omega-3 fatty acids."
        };
    }
   
    /**
     * Creates or updates a user's diet profile.
     * 
     * @param username The username of the user
     * @param dietType The user's diet type preference
     * @param healthConditions The user's health conditions or allergies
     * @param weightGoal The user's weight goal
     * @param excludedFoods List of foods the user wants to exclude
     * @return true if profile created/updated successfully
     */
    public boolean setUserDietProfile(String username, DietType dietType, 
                                    List<String> healthConditions,
                                    WeightGoal weightGoal,
                                    List<String> excludedFoods) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            // First find the user ID
            int userId = getUserId(conn, username);
            if (userId == -1) {
                return false; // User not found
            }
           
            // Check for existing profile
            boolean hasProfile = false;
            int profileId = -1;
            
            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT id FROM diet_profiles WHERE user_id = ?")) {
                
                checkStmt.setInt(1, userId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    hasProfile = true;
                    profileId = rs.getInt("id");
                }
            }
            
            if (hasProfile) {
                // Update existing profile
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE diet_profiles SET diet_type = ?, weight_goal = ? WHERE id = ?")) {
                    
                    updateStmt.setString(1, dietType.name());
                    updateStmt.setString(2, weightGoal.name());
                    updateStmt.setInt(3, profileId);
                    
                    updateStmt.executeUpdate();
                }
                
                // Delete existing health conditions and excluded foods
                try (PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM health_conditions WHERE profile_id = ?")) {
                    deleteStmt.setInt(1, profileId);
                    deleteStmt.executeUpdate();
                }
                
                try (PreparedStatement deleteStmt = conn.prepareStatement(
                        "DELETE FROM excluded_foods WHERE profile_id = ?")) {
                    deleteStmt.setInt(1, profileId);
                    deleteStmt.executeUpdate();
                }
            } else {
                // Create new profile
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO diet_profiles (user_id, diet_type, weight_goal) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    
                    insertStmt.setInt(1, userId);
                    insertStmt.setString(2, dietType.name());
                    insertStmt.setString(3, weightGoal.name());
                    
                    insertStmt.executeUpdate();
                    
                    try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            profileId = generatedKeys.getInt(1);
                        } else {
                            return false;
                        }
                    }
                }
            }
            
            // Add health conditions
            if (!healthConditions.isEmpty()) {
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO health_conditions (profile_id, condition_name) VALUES (?, ?)")) {
                    
                    for (String condition : healthConditions) {
                        insertStmt.setInt(1, profileId);
                        insertStmt.setString(2, condition);
                        insertStmt.executeUpdate();
                    }
                }
            }
            
            // Add excluded foods
            if (!excludedFoods.isEmpty()) {
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO excluded_foods (profile_id, food_name) VALUES (?, ?)")) {
                    
                    for (String food : excludedFoods) {
                        insertStmt.setInt(1, profileId);
                        insertStmt.setString(2, food);
                        insertStmt.executeUpdate();
                    }
                }
            }
            
            return true;
            
        } catch (SQLException e) {
            System.out.println("Error occurred while updating diet profile: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper method to get user ID by username
     * 
     * @param conn Database connection
     * @param username Username to look up
     * @return User ID or -1 if not found
     * @throws SQLException If database error occurs
     */
    private int getUserId(Connection conn, String username) throws SQLException {
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
    
    /**
     * Gets a user's diet profile.
     * 
     * @param username The username of the user
     * @return The user's diet profile or a default profile if none exists
     */
    public UserDietProfile getUserDietProfile(String username) {
        try (Connection conn = DatabaseHelper.getConnection()) {
            int userId = getUserId(conn, username);
            if (userId == -1) {
                return new UserDietProfile(DietType.BALANCED, new ArrayList<>(), 
                         WeightGoal.MAINTAIN, new ArrayList<>());
            }
            
            DietType dietType = DietType.BALANCED;
            WeightGoal weightGoal = WeightGoal.MAINTAIN;
            List<String> healthConditions = new ArrayList<>();
            List<String> excludedFoods = new ArrayList<>();
            
            // Get diet profile
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT id, diet_type, weight_goal FROM diet_profiles WHERE user_id = ?")) {
                
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    int profileId = rs.getInt("id");
                    dietType = DietType.valueOf(rs.getString("diet_type"));
                    weightGoal = WeightGoal.valueOf(rs.getString("weight_goal"));
                    
                    // Get health conditions
                    try (PreparedStatement condStmt = conn.prepareStatement(
                            "SELECT condition_name FROM health_conditions WHERE profile_id = ?")) {
                        
                        condStmt.setInt(1, profileId);
                        ResultSet condRs = condStmt.executeQuery();
                        
                        while (condRs.next()) {
                            healthConditions.add(condRs.getString("condition_name"));
                        }
                    }
                    
                    // Get excluded foods
                    try (PreparedStatement foodStmt = conn.prepareStatement(
                            "SELECT food_name FROM excluded_foods WHERE profile_id = ?")) {
                        
                        foodStmt.setInt(1, profileId);
                        ResultSet foodRs = foodStmt.executeQuery();
                        
                        while (foodRs.next()) {
                            excludedFoods.add(foodRs.getString("food_name"));
                        }
                    }
                }
            }
            
            return new UserDietProfile(dietType, healthConditions, weightGoal, excludedFoods);
            
        } catch (SQLException e) {
            System.out.println("Error occurred while retrieving diet profile: " + e.getMessage());
            return new UserDietProfile(DietType.BALANCED, new ArrayList<>(), 
                     WeightGoal.MAINTAIN, new ArrayList<>());
        }
    }
    
    /**
     * Generates personalized diet recommendations based on user profile and preferences.
     * 
     * @param username The username of the user
     * @param gender The user's gender (M/F)
     * @param age The user's age
     * @param heightCm The user's height in centimeters
     * @param weightKg The user's weight in kilograms
     * @param activityLevel The user's activity level (1-5)
     * @return A DietRecommendation object containing the personalized recommendations
     */
    public DietRecommendation generateRecommendations(String username, char gender, int age,
                                                    double heightCm, double weightKg, 
                                                    int activityLevel) {
        // Get user's diet profile
        UserDietProfile profile = getUserDietProfile(username);
        
        // Calculate suggested calories based on user metrics and weight goal
        int baseCalories = calorieNutrientService.calculateSuggestedCalories(
            gender, age, heightCm, weightKg, activityLevel);
        
        // Adjust calories based on weight goal
        int adjustedCalories = adjustCaloriesForWeightGoal(baseCalories, profile.getWeightGoal());
        
        // Calculate macronutrient distribution based on diet type
        MacronutrientDistribution macros = calculateMacronutrients(
            adjustedCalories, profile.getDietType());
        
        // Generate meal plan based on calories, macros, and diet restrictions
        List<RecommendedMeal> recommendedMeals = generateMealPlan(
            adjustedCalories, macros, profile);
        
        // Generate dietary guidelines based on profile
        List<String> guidelines = generateDietaryGuidelines(profile);
        
        // Create and return diet recommendation
        DietRecommendation recommendation = new DietRecommendation(
            adjustedCalories, macros, recommendedMeals, guidelines);
        
        // You can save recommendations to the database (optional)
        saveRecommendation(username, recommendation);
        
        return recommendation;
    }
    
    /**
     * Saves a diet recommendation to the database (optional).
     * 
     * @param username The username of the user
     * @param recommendation The diet recommendation to save
     */
    private void saveRecommendation(String username, DietRecommendation recommendation) {
        // You can implement this method in the future if needed
        // You can add code here to store recommendations in the database
    }
    
    /**
     * Adjusts calorie intake based on weight goal.
     * 
     * @param baseCalories The base calorie needs
     * @param weightGoal The weight goal (lose, maintain, gain)
     * @return The adjusted calorie intake
     */
    private int adjustCaloriesForWeightGoal(int baseCalories, WeightGoal weightGoal) {
        switch (weightGoal) {
            case LOSE:
                return (int) (baseCalories * 0.85); // 15% deficit for weight loss
            case GAIN:
                return (int) (baseCalories * 1.15); // 15% surplus for weight gain
            case MAINTAIN:
            default:
                return baseCalories; // Maintain current weight
        }
    }
    
    /**
     * Calculates macronutrient distribution based on diet type.
     * 
     * @param calories The total daily calories
     * @param dietType The diet type preference
     * @return A MacronutrientDistribution object
     */
    private MacronutrientDistribution calculateMacronutrients(int calories, DietType dietType) {
        double proteinPercentage, carbPercentage, fatPercentage;
        
        switch (dietType) {
            case LOW_CARB:
                proteinPercentage = 0.30; // 30% protein
                carbPercentage = 0.20;    // 20% carbs
                fatPercentage = 0.50;     // 50% fat
                break;
            case HIGH_PROTEIN:
                proteinPercentage = 0.40; // 40% protein
                carbPercentage = 0.30;    // 30% carbs
                fatPercentage = 0.30;     // 30% fat
                break;
            case VEGETARIAN:
            case VEGAN:
                proteinPercentage = 0.20; // 20% protein
                carbPercentage = 0.60;    // 60% carbs
                fatPercentage = 0.20;     // 20% fat
                break;
            case BALANCED:
            default:
                proteinPercentage = 0.25; // 25% protein
                carbPercentage = 0.50;    // 50% carbs
                fatPercentage = 0.25;     // 25% fat
                break;
        }
        
        // Convert percentages to grams (4 calories per gram of protein/carb, 9 for fat)
        double proteinGrams = (calories * proteinPercentage) / 4.0;
        double carbGrams = (calories * carbPercentage) / 4.0;
        double fatGrams = (calories * fatPercentage) / 9.0;
        
        return new MacronutrientDistribution(
            (int) Math.round(proteinGrams),
            (int) Math.round(carbGrams),
            (int) Math.round(fatGrams)
        );
    }
    
   
    
    /**
     * Generates a meal plan based on nutritional requirements and preferences.
     * 
     * @param calories The total daily calories
     * @param macros The macronutrient distribution
     * @param profile The user's diet profile
     * @return A list of recommended meals
     */
    private List<RecommendedMeal> generateMealPlan(int calories, 
                                                 MacronutrientDistribution macros,
                                                 UserDietProfile profile) {
        List<RecommendedMeal> meals = new ArrayList<>();
        
        // Meal distribution (percentage of total calories)
        double breakfastPct = 0.25; // 25% of calories for breakfast
        double lunchPct = 0.35;     // 35% of calories for lunch
        double dinnerPct = 0.30;    // 30% of calories for dinner
        double snackPct = 0.10;     // 10% of calories for snack
        
        // Get appropriate food options based on diet type
        Food[] breakfastOptions = getAppropriateOptions(
            mealPlanningService.getBreakfastOptions(), profile);
        Food[] lunchOptions = getAppropriateOptions(
            mealPlanningService.getLunchOptions(), profile);
        Food[] dinnerOptions = getAppropriateOptions(
            mealPlanningService.getDinnerOptions(), profile);
        Food[] snackOptions = getAppropriateOptions(
            mealPlanningService.getSnackOptions(), profile);
        
        // Create meal recommendations
        if (breakfastOptions.length > 0) {
            meals.add(createMealRecommendation("Breakfast", breakfastOptions,
                      (int)(calories * breakfastPct),
                      (int)(macros.getProteinGrams() * breakfastPct),
                      (int)(macros.getCarbGrams() * breakfastPct),
                      (int)(macros.getFatGrams() * breakfastPct)));
        }
        
        if (lunchOptions.length > 0) {
            meals.add(createMealRecommendation("Lunch", lunchOptions,
                      (int)(calories * lunchPct),
                      (int)(macros.getProteinGrams() * lunchPct),
                      (int)(macros.getCarbGrams() * lunchPct),
                      (int)(macros.getFatGrams() * lunchPct)));
        }
        
        if (dinnerOptions.length > 0) {
            meals.add(createMealRecommendation("Dinner", dinnerOptions,
                      (int)(calories * dinnerPct),
                      (int)(macros.getProteinGrams() * dinnerPct),
                      (int)(macros.getCarbGrams() * dinnerPct),
                      (int)(macros.getFatGrams() * dinnerPct)));
        }
        
        if (snackOptions.length > 0) {
            meals.add(createMealRecommendation("Snack", snackOptions,
                      (int)(calories * snackPct),
                      (int)(macros.getProteinGrams() * snackPct),
                      (int)(macros.getCarbGrams() * snackPct),
                      (int)(macros.getFatGrams() * snackPct)));
        }
        
        return meals;
    }
    
    /**
     * Creates a meal recommendation with appropriate foods.
     * 
     * @param mealType The type of meal (breakfast, lunch, etc.)
     * @param options The food options for this meal
     * @param calories The target calories for this meal
     * @param protein The target protein for this meal
     * @param carbs The target carbs for this meal
     * @param fat The target fat for this meal
     * @return A RecommendedMeal object
     */
    private RecommendedMeal createMealRecommendation(String mealType, Food[] options,
                                                   int calories, int protein,
                                                   int carbs, int fat) {
        // Select 1-3 foods that best match the calorie and macro targets
        List<Food> selectedFoods = new ArrayList<>();
        
        // Simple food selection - in a real app this would be more sophisticated
        // to better match the nutritional targets
        int remainingCalories = calories;
        int count = 0;
        
        // Add foods until we reach close to the calorie target or max 3 foods
        while (remainingCalories > 0 && count < 3 && options.length > 0) {
            // Simple selection - just pick foods in order
            // In a real app, this would use an algorithm to optimize for nutrition
            Food selected = options[count % options.length];
            
            if (selected.getCalories() <= remainingCalories) {
                selectedFoods.add(selected);
                remainingCalories -= selected.getCalories();
            }
            
            count++;
        }
        
        return new RecommendedMeal(mealType, selectedFoods, calories, protein, carbs, fat);
    }
    
    /**
     * Filters food options based on user diet profile.
     * 
     * @param allOptions All available food options
     * @param profile The user's diet profile
     * @return Array of appropriate food options
     */
    private Food[] getAppropriateOptions(Food[] allOptions, UserDietProfile profile) {
        List<Food> appropriateOptions = new ArrayList<>();
        
        for (Food food : allOptions) {
            // Skip excluded foods
            if (profile.getExcludedFoods().contains(food.getName().toLowerCase())) {
                continue;
            }
            
            // Filter based on diet type
            boolean isAppropriate = true;
            switch (profile.getDietType()) {
                case VEGETARIAN:
                    // Filter out non-vegetarian options (simplified example)
                    if (food.getName().toLowerCase().contains("beef") ||
                        food.getName().toLowerCase().contains("chicken") ||
                        food.getName().toLowerCase().contains("fish") ||
                        food.getName().toLowerCase().contains("meat")) {
                        isAppropriate = false;
                    }
                    break;
                case VEGAN:
                    // Filter out non-vegan options (simplified example)
                    if (food.getName().toLowerCase().contains("egg") ||
                        food.getName().toLowerCase().contains("dairy") ||
                        food.getName().toLowerCase().contains("milk") ||
                        food.getName().toLowerCase().contains("cheese") ||
                        food.getName().toLowerCase().contains("yogurt") ||
                        food.getName().toLowerCase().contains("beef") ||
                        food.getName().toLowerCase().contains("chicken") ||
                        food.getName().toLowerCase().contains("fish") ||
                        food.getName().toLowerCase().contains("meat")) {
                        isAppropriate = false;
                    }
                    break;
                default:
                    // No special filtering
                    break;
            }
            
            if (isAppropriate) {
                appropriateOptions.add(food);
            }
        }
        
        return appropriateOptions.toArray(new Food[0]);
    }
    
    /**
     * Generates dietary guidelines based on the user's profile.
     * 
     * @param profile The user's diet profile
     * @return List of dietary guidelines
     */
    private List<String> generateDietaryGuidelines(UserDietProfile profile) {
        List<String> guidelines = new ArrayList<>();
        
        // Add basic guidelines based on diet type
        switch (profile.getDietType()) {
            case LOW_CARB:
                guidelines.add("Limit intake of bread, pasta, rice, and starchy vegetables.");
                guidelines.add("Focus on non-starchy vegetables, proteins, and healthy fats.");
                guidelines.add("Choose whole, unprocessed foods over processed alternatives.");
                break;
            case HIGH_PROTEIN:
                guidelines.add("Include a protein source with every meal.");
                guidelines.add("Consider protein-rich snacks like Greek yogurt, eggs, or nuts.");
                guidelines.add("Distribute protein intake evenly throughout the day.");
                break;
            case VEGETARIAN:
                guidelines.add("Ensure adequate protein intake from eggs, dairy, legumes, and plant proteins.");
                guidelines.add("Consider vitamin B12 supplementation if limiting dairy and eggs.");
                guidelines.add("Include a variety of plant foods to ensure complete protein profiles.");
                break;
            case VEGAN:
                guidelines.add("Focus on complete protein sources like tofu, tempeh, and quinoa.");
                guidelines.add("Consider supplements for vitamin B12, vitamin D, and omega-3 fatty acids.");
                guidelines.add("Include a variety of legumes, nuts, seeds, and whole grains.");
                break;
            case BALANCED:
            default:
                guidelines.add("Aim for a balanced diet with variety from all food groups.");
                guidelines.add("Include fruits and vegetables with every meal.");
                guidelines.add("Choose whole grains over refined grains when possible.");
                break;
        }
        
        // Add guidelines based on weight goal
        switch (profile.getWeightGoal()) {
            case LOSE:
                guidelines.add("Create a moderate calorie deficit through diet and exercise.");
                guidelines.add("Focus on nutrient-dense, filling foods to manage hunger.");
                guidelines.add("Stay hydrated - sometimes thirst is mistaken for hunger.");
                break;
            case GAIN:
                guidelines.add("Eat frequent, calorie-dense meals to reach your calorie surplus.");
                guidelines.add("Prioritize nutrient-rich foods rather than empty calories.");
                guidelines.add("Consider strength training to support muscle growth with your calorie surplus.");
                break;
            case MAINTAIN:
            default:
                guidelines.add("Monitor your weight regularly to ensure you're maintaining.");
                guidelines.add("Adjust calorie intake if you notice unintended weight changes.");
                guidelines.add("Find a sustainable eating pattern that works with your lifestyle.");
                break;
        }
        
        // Add health condition specific guidelines
        for (String condition : profile.getHealthConditions()) {
            if (condition.equalsIgnoreCase("diabetes")) {
                guidelines.add("Monitor carbohydrate intake and choose complex carbs over simple sugars.");
                guidelines.add("Eat regular, well-balanced meals to maintain stable blood sugar.");
            } else if (condition.equalsIgnoreCase("hypertension")) {
                guidelines.add("Limit sodium intake and focus on potassium-rich foods.");
                guidelines.add("Consider the DASH diet approach (Dietary Approaches to Stop Hypertension).");
            } else if (condition.equalsIgnoreCase("high cholesterol")) {
                guidelines.add("Choose healthy fats (mono and polyunsaturated) over saturated fats.");
                guidelines.add("Increase soluble fiber intake from foods like oats, beans, and fruits.");
            }
        }
        
        return guidelines;
    }
    
    /**
     * Enum for different diet types.
     */
    public enum DietType {
        BALANCED,
        LOW_CARB,
        HIGH_PROTEIN,
        VEGETARIAN,
        VEGAN
    }
    
    /**
     * Enum for weight goals.
     */
    public enum WeightGoal {
        LOSE,
        MAINTAIN,
        GAIN
    }
    
    /**
     * Inner class to represent a user's diet profile.
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
    
    /**
     * Inner class to represent macronutrient distribution.
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
            return "Protein: " + proteinGrams + "g, Carbs: " + carbGrams + 
                   "g, Fat: " + fatGrams + "g";
        }
    }
    
    /**
     * Inner class to represent a recommended meal.
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
     * Inner class to represent a diet recommendation.
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
     * Gets example diet recommendations for various diet types.
     * 
     * @return Array of example diet recommendations
     */
    public String[] getExampleDietPlans() {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT diet_type, description FROM example_diet_plans")) {
            
            List<String> plans = new ArrayList<>();
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String dietType = rs.getString("diet_type");
                String description = rs.getString("description");
                plans.add(dietType + " Diet Plan:\n" + description);
            }
           
            // If no data was retrieved from the database, return default plans
            if (plans.isEmpty()) {
                return getDefaultExampleDietPlans();
            }
            
            return plans.toArray(new String[0]);
        } catch (SQLException e) {
            System.out.println("Could not retrieve example diet plans: " + e.getMessage());
            // Return default plans in case of error
            return getDefaultExampleDietPlans();
        }
    }
    }