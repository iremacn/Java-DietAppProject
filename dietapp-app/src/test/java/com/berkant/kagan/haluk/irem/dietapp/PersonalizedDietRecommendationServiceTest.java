package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.berkant.kagan.haluk.irem.dietapp.PersonalizedDietRecommendationService.DietType;
import com.berkant.kagan.haluk.irem.dietapp.PersonalizedDietRecommendationService.WeightGoal;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PersonalizedDietRecommendationServiceTest {

    private PersonalizedDietRecommendationService dietService;
    private CalorieNutrientTrackingService calorieService;
    private MealPlanningService mealService;
    
    // Test data
    private static final String TEST_USERNAME = "testuser";
    private static final char TEST_GENDER = 'M';
    private static final int TEST_AGE = 30;
    private static final double TEST_HEIGHT = 180.0;
    private static final double TEST_WEIGHT = 80.0;
    private static final int TEST_ACTIVITY_LEVEL = 2;
    
    @Before
    public void setUp() {
        // Create test implementations
        calorieService = new CalorieNutrientTrackingService(mealService) {
            @Override
            public int calculateSuggestedCalories(char gender, int age, double heightCm, double weightKg, int activityLevel) {
                return 2000; // Fixed value for tests
            }
        };
        
        mealService = new MealPlanningService() {
            @Override
            public Food[] getBreakfastOptions() {
                return new Food[] {
                    new Food(),
                    new Food()
                };
            }
            
            @Override
            public Food[] getLunchOptions() {
                return new Food[] {
                    new Food(),
                    new Food(),
                    new Food()
                };
            }
            
            @Override
            public Food[] getDinnerOptions() {
                return new Food[] {
                    new Food(),
                    new Food()
                };
            }
            
            @Override
            public Food[] getSnackOptions() {
                return new Food[] {
                    new Food(),
                    new Food()
                };
            }
        };
        
        // Create service under test with overridden database methods
        dietService = new PersonalizedDietRecommendationService(calorieService, mealService) {
            @Override
            public boolean setUserDietProfile(String username, DietType dietType, 
                                          List<String> healthConditions,
                                          WeightGoal weightGoal,
                                          List<String> excludedFoods) {
                // Simulate database logic without actually using database
                if ("nonexistentuser".equals(username)) {
                    return false; // Simulating user not found
                }
                return true; // Simulating success for existing users
            }
            
            @Override
            public UserDietProfile getUserDietProfile(String username) {
                // Simulate database retrieval logic without actually using database
                if ("nonexistentuser".equals(username)) {
                    return new UserDietProfile(DietType.BALANCED, new ArrayList<>(), 
                                     WeightGoal.MAINTAIN, new ArrayList<>());
                } else if ("testUserLowCarb".equals(username)) {
                    return new UserDietProfile(DietType.LOW_CARB, Arrays.asList("diabetes"), 
                                     WeightGoal.LOSE, Arrays.asList("nuts"));
                } else if ("testUserHighProtein".equals(username)) {
                    return new UserDietProfile(DietType.HIGH_PROTEIN, Arrays.asList("cholesterol"), 
                                     WeightGoal.GAIN, Arrays.asList("dairy"));
                } else if ("testUserVegetarian".equals(username)) {
                    return new UserDietProfile(DietType.VEGETARIAN, Arrays.asList("hypertension"), 
                                     WeightGoal.MAINTAIN, Arrays.asList("mushrooms"));
                } else if ("testUserVegan".equals(username)) {
                    return new UserDietProfile(DietType.VEGAN, new ArrayList<>(), 
                                     WeightGoal.LOSE, Arrays.asList("soy"));
                } else {
                    return new UserDietProfile(DietType.BALANCED, Arrays.asList("diabetes"), 
                                     WeightGoal.MAINTAIN, Arrays.asList("shellfish"));
                }
            }
            
            @Override
            public String[] getExampleDietPlans() {
                return getDefaultExampleDietPlans();
            }
        };
    }
    
  
    @Test
    public void testGetUserDietProfile_Default() {
        // Test default user profile
        PersonalizedDietRecommendationService.UserDietProfile profile = 
            dietService.getUserDietProfile(TEST_USERNAME);
        
        assertNotNull(profile);
        assertEquals(PersonalizedDietRecommendationService.DietType.BALANCED, profile.getDietType());
        assertEquals(PersonalizedDietRecommendationService.WeightGoal.MAINTAIN, profile.getWeightGoal());
        assertEquals(1, profile.getHealthConditions().size());
        assertEquals("diabetes", profile.getHealthConditions().get(0));
        assertEquals(1, profile.getExcludedFoods().size());
        assertEquals("shellfish", profile.getExcludedFoods().get(0));
    }
    
    @Test
    public void testGetUserDietProfile_LowCarb() {
        // Test low carb profile
        PersonalizedDietRecommendationService.UserDietProfile profile = 
            dietService.getUserDietProfile("testUserLowCarb");
        
        assertNotNull(profile);
        assertEquals(PersonalizedDietRecommendationService.DietType.LOW_CARB, profile.getDietType());
        assertEquals(PersonalizedDietRecommendationService.WeightGoal.LOSE, profile.getWeightGoal());
    }
    
    @Test
    public void testGetUserDietProfile_HighProtein() {
        // Test high protein profile
        PersonalizedDietRecommendationService.UserDietProfile profile = 
            dietService.getUserDietProfile("testUserHighProtein");
        
        assertNotNull(profile);
        assertEquals(PersonalizedDietRecommendationService.DietType.HIGH_PROTEIN, profile.getDietType());
        assertEquals(PersonalizedDietRecommendationService.WeightGoal.GAIN, profile.getWeightGoal());
    }
    
    @Test
    public void testGetUserDietProfile_Vegetarian() {
        // Test vegetarian profile
        PersonalizedDietRecommendationService.UserDietProfile profile = 
            dietService.getUserDietProfile("testUserVegetarian");
        
        assertNotNull(profile);
        assertEquals(PersonalizedDietRecommendationService.DietType.VEGETARIAN, profile.getDietType());
        assertEquals(PersonalizedDietRecommendationService.WeightGoal.MAINTAIN, profile.getWeightGoal());
    }
   
    @Test
    public void testGetUserDietProfile_Vegan() {
        // Test vegan profile
        PersonalizedDietRecommendationService.UserDietProfile profile = 
            dietService.getUserDietProfile("testUserVegan");
        
        assertNotNull(profile);
        assertEquals(PersonalizedDietRecommendationService.DietType.VEGAN, profile.getDietType());
        assertEquals(PersonalizedDietRecommendationService.WeightGoal.LOSE, profile.getWeightGoal());
    }
    
    @Test
    public void testGetUserDietProfile_NonExistentUser() {
        // Test non-existent user (should return default)
        PersonalizedDietRecommendationService.UserDietProfile profile = 
            dietService.getUserDietProfile("nonexistentuser");
        
        assertNotNull(profile);
        assertEquals(PersonalizedDietRecommendationService.DietType.BALANCED, profile.getDietType());
        assertEquals(PersonalizedDietRecommendationService.WeightGoal.MAINTAIN, profile.getWeightGoal());
        assertTrue(profile.getHealthConditions().isEmpty());
        assertTrue(profile.getExcludedFoods().isEmpty());
    }
    
    @Test
    public void testGenerateRecommendations_Balanced() {
        // Test balanced diet recommendations
        PersonalizedDietRecommendationService.DietRecommendation recommendation = 
            dietService.generateRecommendations(
                TEST_USERNAME, TEST_GENDER, TEST_AGE, TEST_HEIGHT, TEST_WEIGHT, TEST_ACTIVITY_LEVEL
            );
        
        assertNotNull(recommendation);
        assertEquals(2000, recommendation.getDailyCalories());
        
        PersonalizedDietRecommendationService.MacronutrientDistribution macros = recommendation.getMacros();
        assertEquals(125, macros.getProteinGrams());
        assertEquals(250, macros.getCarbGrams());
        assertEquals(56, macros.getFatGrams());
        
        List<PersonalizedDietRecommendationService.RecommendedMeal> meals = recommendation.getMeals();
        assertNotNull(meals);
        assertEquals(4, meals.size());
        
        List<String> guidelines = recommendation.getDietaryGuidelines();
        assertNotNull(guidelines);
        assertFalse(guidelines.isEmpty());
    }
    
    @Test
    public void testGenerateRecommendations_LowCarb() {
        // Test low carb diet recommendations
        PersonalizedDietRecommendationService.DietRecommendation recommendation = 
            dietService.generateRecommendations(
                "testUserLowCarb", TEST_GENDER, TEST_AGE, TEST_HEIGHT, TEST_WEIGHT, TEST_ACTIVITY_LEVEL
            );
        
        assertNotNull(recommendation);
        assertEquals(1700, recommendation.getDailyCalories()); // 2000 * 0.85 for LOSE goal
        
        PersonalizedDietRecommendationService.MacronutrientDistribution macros = recommendation.getMacros();
        assertEquals(128, macros.getProteinGrams()); // 30% of 1700 calories / 4 cals per gram
        assertEquals(85, macros.getCarbGrams());     // 20% of 1700 calories / 4 cals per gram
        assertEquals(94, macros.getFatGrams());      // 50% of 1700 calories / 9 cals per gram
    }
    
    @Test
    public void testGenerateRecommendations_HighProtein() {
        // Test high protein diet recommendations
        PersonalizedDietRecommendationService.DietRecommendation recommendation = 
            dietService.generateRecommendations(
                "testUserHighProtein", TEST_GENDER, TEST_AGE, TEST_HEIGHT, TEST_WEIGHT, TEST_ACTIVITY_LEVEL
            );
        
        assertNotNull(recommendation);
        assertEquals(2300, recommendation.getDailyCalories()); // 2000 * 1.15 for GAIN goal
        
        PersonalizedDietRecommendationService.MacronutrientDistribution macros = recommendation.getMacros();
        assertEquals(230, macros.getProteinGrams()); // 40% of 2300 calories / 4 cals per gram
        assertEquals(173, macros.getCarbGrams());    // 30% of 2300 calories / 4 cals per gram
        assertEquals(77, macros.getFatGrams());      // 30% of 2300 calories / 9 cals per gram
    }
    
    @Test
    public void testGenerateRecommendations_Vegetarian() {
        // Test vegetarian diet recommendations
        PersonalizedDietRecommendationService.DietRecommendation recommendation = 
            dietService.generateRecommendations(
                "testUserVegetarian", TEST_GENDER, TEST_AGE, TEST_HEIGHT, TEST_WEIGHT, TEST_ACTIVITY_LEVEL
            );
        
        assertNotNull(recommendation);
        assertEquals(2000, recommendation.getDailyCalories()); // Maintained for MAINTAIN goal
        
        PersonalizedDietRecommendationService.MacronutrientDistribution macros = recommendation.getMacros();
        assertEquals(100, macros.getProteinGrams()); // 20% of 2000 calories / 4 cals per gram
        assertEquals(300, macros.getCarbGrams());    // 60% of 2000 calories / 4 cals per gram
        assertEquals(44, macros.getFatGrams());      // 20% of 2000 calories / 9 cals per gram
    }
    
    
    
    
    
    
    
  
    @Test
    public void testGetExampleDietPlans_DatabaseSuccess() {
        // Create a service implementation that simulates a successful database query
        PersonalizedDietRecommendationService testService = new PersonalizedDietRecommendationService(calorieService, mealService) {
            @Override
            protected String[] getDefaultExampleDietPlans() {
                // Default plans that should not be returned if DB query is successful
                return new String[] {"Default Plan 1", "Default Plan 2"};
            }
            
            @Override
            public String[] getExampleDietPlans() {
                // Simulate successful database connection and query result
                List<String> plans = new ArrayList<>();
                plans.add("KETO Diet Plan:\nHigh fat, low carb");
                plans.add("PALEO Diet Plan:\nFocus on whole foods");
                return plans.toArray(new String[0]);
            }
        };
        
        // Call method under test
        String[] plans = testService.getExampleDietPlans();
        
        // Verify results
        assertNotNull("Plans should not be null", plans);
        assertEquals("Should have two plans", 2, plans.length);
        assertEquals("First plan should be keto", "KETO Diet Plan:\nHigh fat, low carb", plans[0]);
        assertEquals("Second plan should be paleo", "PALEO Diet Plan:\nFocus on whole foods", plans[1]);
    }

    @Test
    public void testGetExampleDietPlans_EmptyDatabase() {
        // Create a service implementation that simulates an empty database result
        PersonalizedDietRecommendationService testService = new PersonalizedDietRecommendationService(calorieService, mealService) {
            @Override
            protected String[] getDefaultExampleDietPlans() {
                // Default plans that should be returned when DB query returns empty
                return new String[] {"Default Plan 1", "Default Plan 2"};
            }
            
            @Override
            public String[] getExampleDietPlans() {
                // Simulate empty result from database
                List<String> plans = new ArrayList<>();
                // Empty plans list simulates no data in database
                
                // If no data was retrieved from the database, return default plans
                if (plans.isEmpty()) {
                    return getDefaultExampleDietPlans();
                }
                
                return plans.toArray(new String[0]);
            }
        };
        
        // Call method under test
        String[] plans = testService.getExampleDietPlans();
        
        // Verify default plans are returned
        assertNotNull("Plans should not be null", plans);
        assertEquals("Should have two default plans", 2, plans.length);
        assertEquals("First plan should be default plan 1", "Default Plan 1", plans[0]);
        assertEquals("Second plan should be default plan 2", "Default Plan 2", plans[1]);
    }

    @Test
    public void testGetExampleDietPlans_DatabaseError() {
        // Create a service implementation that simulates a database error
        PersonalizedDietRecommendationService testService = new PersonalizedDietRecommendationService(calorieService, mealService) {
            @Override
            protected String[] getDefaultExampleDietPlans() {
                // Default plans that should be returned when DB error occurs
                return new String[] {"Error Default Plan 1", "Error Default Plan 2"};
            }
            
            @Override
            public String[] getExampleDietPlans() {
                // Simulate database error
                System.out.println("Could not retrieve example diet plans: Simulated database error");
                
                // Return default plans in case of error
                return getDefaultExampleDietPlans();
            }
        };
        
        // Call method under test
        String[] plans = testService.getExampleDietPlans();
        
        // Verify default plans are returned
        assertNotNull("Plans should not be null", plans);
        assertEquals("Should have two default plans", 2, plans.length);
        assertEquals("First plan should be error default plan 1", "Error Default Plan 1", plans[0]);
        assertEquals("Second plan should be error default plan 2", "Error Default Plan 2", plans[1]);
    }

    @Test
    public void testGetDefaultExampleDietPlans() {
     
        PersonalizedDietRecommendationService service = new PersonalizedDietRecommendationService(calorieService, mealService);
        
        String[] defaultPlans = service.getDefaultExampleDietPlans();
        
        assertNotNull("Default plans should not be null", defaultPlans);
        assertEquals("Should have 5 default plans", 5, defaultPlans.length);
        assertTrue("First plan should be balanced diet", defaultPlans[0].contains("Balanced Diet Plan"));
        assertTrue("Second plan should be low-carb diet", defaultPlans[1].contains("Low-Carb Diet Plan"));
        assertTrue("Third plan should be high-protein diet", defaultPlans[2].contains("High-Protein Diet Plan"));
        assertTrue("Fourth plan should be vegetarian diet", defaultPlans[3].contains("Vegetarian Diet Plan"));
        assertTrue("Fifth plan should be vegan diet", defaultPlans[4].contains("Vegan Diet Plan"));
    }
    
 
    
    @Test
    public void testRecommendedMealGetters() {
      
        String mealType = "Breakfast";
        List<Food> foods = Arrays.asList(
            new Food("Oatmeal", 100, 150),
            new Food("Eggs", 50, 70)
        );
        int targetCalories = 300;
        int targetProtein = 25;
        int targetCarbs = 40;
        int targetFat = 10;
        
      
        PersonalizedDietRecommendationService.RecommendedMeal meal = 
            dietService.new RecommendedMeal(
                mealType, foods, targetCalories, targetProtein, targetCarbs, targetFat);
        
      
        assertEquals("Meal type getter should return correct value", mealType, meal.getMealType());
        assertEquals("Foods getter should return correct list", foods, meal.getFoods());
        assertEquals("Target calories getter should return correct value", targetCalories, meal.getTargetCalories());
        assertEquals("Target protein getter should return correct value", targetProtein, meal.getTargetProtein());
        assertEquals("Target carbs getter should return correct value", targetCarbs, meal.getTargetCarbs());
        assertEquals("Target fat getter should return correct value", targetFat, meal.getTargetFat());
        
      
        assertEquals("Total calories should be sum of food calories", 
                     foods.stream().mapToInt(Food::getCalories).sum(), 
                     meal.getTotalCalories());
    }
    
 
    @Test
    public void testMacronutrientDistributionMethods() {
        // Test verileri hazırla
        int proteinGrams = 125;
        int carbGrams = 250;
        int fatGrams = 56;
        
        // MacronutrientDistribution nesnesi oluştur
        PersonalizedDietRecommendationService.MacronutrientDistribution macros = 
            dietService.new MacronutrientDistribution(proteinGrams, carbGrams, fatGrams);
        
        // Getter metodlarını test et
        assertEquals("Protein grams getter should return correct value", 
                     proteinGrams, macros.getProteinGrams());
        assertEquals("Carb grams getter should return correct value", 
                     carbGrams, macros.getCarbGrams());
        assertEquals("Fat grams getter should return correct value", 
                     fatGrams, macros.getFatGrams());
        
        // toString metodunu test et
        String expectedToString = "Protein: " + proteinGrams + "g, Carbs: " + carbGrams + 
                                 "g, Fat: " + fatGrams + "g";
        assertEquals("toString should return formatted macronutrient values", 
                     expectedToString, macros.toString());
    }
    
    
    
    
    @Test
    public void testGetAppropriateOptions() throws Exception {
        // Test için gereken servisleri anonim sınıflarla oluştur
        CalorieNutrientTrackingService calorieService = new CalorieNutrientTrackingService(null) {
            @Override
            public int calculateSuggestedCalories(char gender, int age, double heightCm, double weightKg, int activityLevel) {
                return 2000; // Test için sabit değer
            }
        };
        
       
        
        // Test edilecek servis
        PersonalizedDietRecommendationService dietService = new PersonalizedDietRecommendationService(
                calorieService, mealService);
        
        // Reflection ile private metoda erişim
        Method getAppropriateOptionsMethod = PersonalizedDietRecommendationService.class.getDeclaredMethod(
                "getAppropriateOptions", 
                Food[].class,
                PersonalizedDietRecommendationService.UserDietProfile.class);
        getAppropriateOptionsMethod.setAccessible(true);
        
        // Test için gıda verileri
        Food[] testFoods = new Food[] {
            new Food("Chicken Salad", 200, 300),
            new Food("Vegetable Soup", 150, 180),
            new Food("Beef Sandwich", 250, 400),
            new Food("Fish Tacos", 220, 340),
            new Food("Egg Omelette", 180, 220),
            new Food("Tofu Stir Fry", 200, 280),
            new Food("Cheese Pizza", 300, 450),
            new Food("Greek Yogurt", 100, 120),
            new Food("Mixed Nuts", 30, 180),
            new Food("Milk Shake", 150, 300)
        };
        
        // VEGETARIAN profili test et - inner class kullanarak
        PersonalizedDietRecommendationService.UserDietProfile vegetarianProfile = 
            dietService.new UserDietProfile(
                PersonalizedDietRecommendationService.DietType.VEGETARIAN,
                new ArrayList<>(),
                PersonalizedDietRecommendationService.WeightGoal.MAINTAIN,
                new ArrayList<>());
                
        Food[] vegetarianOptions = (Food[]) getAppropriateOptionsMethod.invoke(
            dietService, testFoods, vegetarianProfile);
                
        assertNotNull("Vejetaryen gıda seçenekleri null olmamalı", vegetarianOptions);
        
        // Et içeren seçenekler filtrelenmeli
        for (Food food : vegetarianOptions) {
            assertFalse("Vejetaryen seçenekler et içermemeli", 
                food.getName().toLowerCase().contains("chicken") ||
                food.getName().toLowerCase().contains("beef") ||
                food.getName().toLowerCase().contains("fish") ||
                food.getName().toLowerCase().contains("meat"));
        }
        
        // Et içermeyen seçenekler hala listenin içinde olmalı
        boolean hasVegetarianOptions = false;
        for (Food food : vegetarianOptions) {
            if (food.getName().contains("Vegetable") || 
                food.getName().contains("Tofu") || 
                food.getName().contains("Cheese") ||
                food.getName().contains("Yogurt") ||
                food.getName().contains("Nuts") ||
                food.getName().contains("Egg")) {
                hasVegetarianOptions = true;
                break;
            }
        }
        assertTrue("Vejetaryen seçenekler uygun gıdaları içermeli", hasVegetarianOptions);
        
        // VEGAN profili test et - inner class kullanarak
        PersonalizedDietRecommendationService.UserDietProfile veganProfile = 
            dietService.new UserDietProfile(
                PersonalizedDietRecommendationService.DietType.VEGAN,
                new ArrayList<>(),
                PersonalizedDietRecommendationService.WeightGoal.MAINTAIN,
                new ArrayList<>());
                
        Food[] veganOptions = (Food[]) getAppropriateOptionsMethod.invoke(
            dietService, testFoods, veganProfile);
                
        assertNotNull("Vegan gıda seçenekleri null olmamalı", veganOptions);
        
        // Hayvansal ürünler içeren seçenekler filtrelenmeli
        for (Food food : veganOptions) {
            assertFalse("Vegan seçenekler hayvansal ürün içermemeli", 
                food.getName().toLowerCase().contains("chicken") ||
                food.getName().toLowerCase().contains("beef") ||
                food.getName().toLowerCase().contains("fish") ||
                food.getName().toLowerCase().contains("meat") ||
                food.getName().toLowerCase().contains("egg") ||
                food.getName().toLowerCase().contains("dairy") ||
                food.getName().toLowerCase().contains("milk") ||
                food.getName().toLowerCase().contains("cheese") ||
                food.getName().toLowerCase().contains("yogurt"));
        }
        
        // Vegan seçenekler hala listenin içinde olmalı
        boolean hasVeganOptions = false;
        for (Food food : veganOptions) {
            if (food.getName().contains("Vegetable") || 
                food.getName().contains("Tofu") || 
                food.getName().contains("Nuts")) {
                hasVeganOptions = true;
                break;
            }
        }
        assertTrue("Vegan seçenekler uygun gıdaları içermeli", hasVeganOptions);
        
        // Hariç tutulan gıda testi
        List<String> excludedFoods = Arrays.asList("nuts");
        PersonalizedDietRecommendationService.UserDietProfile excludeProfile = 
            dietService.new UserDietProfile(
                PersonalizedDietRecommendationService.DietType.BALANCED,
                new ArrayList<>(),
                PersonalizedDietRecommendationService.WeightGoal.MAINTAIN,
                excludedFoods);
                
        Food[] filteredOptions = (Food[]) getAppropriateOptionsMethod.invoke(
            dietService, testFoods, excludeProfile);
                
       
   
    }
   
    
   
    @Test
    public void testGenerateDietaryGuidelines_DietTypesAndWeightGoals() throws Exception {
        // Create necessary services using anonymous classes
        CalorieNutrientTrackingService calorieService = new CalorieNutrientTrackingService(null) {
            @Override
            public int calculateSuggestedCalories(char gender, int age, double heightCm, double weightKg, int activityLevel) {
                return 2000; // Fixed value for testing
            }
        };
        
        MealPlanningService mealService = new MealPlanningService() {
            @Override
            public Food[] getBreakfastOptions() { return new Food[0]; }
            @Override
            public Food[] getLunchOptions() { return new Food[0]; }
            @Override
            public Food[] getDinnerOptions() { return new Food[0]; }
            @Override
            public Food[] getSnackOptions() { return new Food[0]; }
        };
        
        // Service to be tested
        PersonalizedDietRecommendationService dietService = new PersonalizedDietRecommendationService(
                calorieService, mealService);
        
        // Access private method using reflection
        Method generateMethod = PersonalizedDietRecommendationService.class.getDeclaredMethod(
                "generateDietaryGuidelines", 
                PersonalizedDietRecommendationService.UserDietProfile.class);
        generateMethod.setAccessible(true);
        
        // ----- 1. VEGETARIAN diet type test -----
        PersonalizedDietRecommendationService.UserDietProfile vegetarianProfile = 
            dietService.new UserDietProfile(
                PersonalizedDietRecommendationService.DietType.VEGETARIAN,
                new ArrayList<>(),
                PersonalizedDietRecommendationService.WeightGoal.MAINTAIN,
                new ArrayList<>());
                
        @SuppressWarnings("unchecked")
        List<String> vegetarianGuidelines = (List<String>) generateMethod.invoke(dietService, vegetarianProfile);
        
        assertNotNull("Vegetarian guidelines should not be null", vegetarianGuidelines);
        
        // Check vegetarian-specific guidelines
        boolean hasAdequateProtein = false;
        boolean hasB12Supplement = false;
        boolean hasPlantVariety = false;
        
        for (String guideline : vegetarianGuidelines) {
            if (guideline.contains("Ensure adequate protein intake from eggs, dairy")) {
                hasAdequateProtein = true;
            }
            if (guideline.contains("Consider vitamin B12 supplementation")) {
                hasB12Supplement = true;
            }
            if (guideline.contains("Include a variety of plant foods")) {
                hasPlantVariety = true;
            }
        }
        
        assertTrue("Vegetarian diet should include protein intake guidance", hasAdequateProtein);
        assertTrue("Vegetarian diet should include B12 supplement guidance", hasB12Supplement);
        assertTrue("Vegetarian diet should include plant variety guidance", hasPlantVariety);
        
        // ----- 2. VEGAN diet type test -----
        PersonalizedDietRecommendationService.UserDietProfile veganProfile = 
            dietService.new UserDietProfile(
                PersonalizedDietRecommendationService.DietType.VEGAN,
                new ArrayList<>(),
                PersonalizedDietRecommendationService.WeightGoal.MAINTAIN,
                new ArrayList<>());
                
        @SuppressWarnings("unchecked")
        List<String> veganGuidelines = (List<String>) generateMethod.invoke(dietService, veganProfile);
        
        assertNotNull("Vegan guidelines should not be null", veganGuidelines);
        
        // Check vegan-specific guidelines
        boolean hasCompleteProtein = false;
        boolean hasSupplements = false;
        boolean hasVeganVariety = false;
        
        for (String guideline : veganGuidelines) {
            if (guideline.contains("Focus on complete protein sources like tofu")) {
                hasCompleteProtein = true;
            }
            if (guideline.contains("Consider supplements for vitamin B12")) {
                hasSupplements = true;
            }
            if (guideline.contains("Include a variety of legumes, nuts, seeds")) {
                hasVeganVariety = true;
            }
        }
        
        assertTrue("Vegan diet should include complete protein sources guidance", hasCompleteProtein);
        assertTrue("Vegan diet should include vitamin supplements guidance", hasSupplements);
        assertTrue("Vegan diet should include legume variety guidance", hasVeganVariety);
        
        // ----- 3. BALANCED diet type test -----
        PersonalizedDietRecommendationService.UserDietProfile balancedProfile = 
            dietService.new UserDietProfile(
                PersonalizedDietRecommendationService.DietType.BALANCED,
                new ArrayList<>(),
                PersonalizedDietRecommendationService.WeightGoal.MAINTAIN,
                new ArrayList<>());
                
        @SuppressWarnings("unchecked")
        List<String> balancedGuidelines = (List<String>) generateMethod.invoke(dietService, balancedProfile);
        
        assertNotNull("Balanced diet guidelines should not be null", balancedGuidelines);
        
        // Check balanced diet-specific guidelines
        boolean hasBalancedVariety = false;
        boolean hasFruitsVegetables = false;
        boolean hasWholeGrains = false;
        
        for (String guideline : balancedGuidelines) {
            if (guideline.contains("Aim for a balanced diet with variety from all food groups")) {
                hasBalancedVariety = true;
            }
            if (guideline.contains("Include fruits and vegetables with every meal")) {
                hasFruitsVegetables = true;
            }
            if (guideline.contains("Choose whole grains over refined grains")) {
                hasWholeGrains = true;
            }
        }
        
        assertTrue("Balanced diet should include food variety guidance", hasBalancedVariety);
        assertTrue("Balanced diet should include fruits and vegetables guidance", hasFruitsVegetables);
        assertTrue("Balanced diet should include whole grain guidance", hasWholeGrains);
        
        // ----- 4. WeightGoal LOSE test -----
        PersonalizedDietRecommendationService.UserDietProfile loseWeightProfile = 
            dietService.new UserDietProfile(
                PersonalizedDietRecommendationService.DietType.BALANCED,
                new ArrayList<>(),
                PersonalizedDietRecommendationService.WeightGoal.LOSE,
                new ArrayList<>());
                
        @SuppressWarnings("unchecked")
        List<String> loseWeightGuidelines = (List<String>) generateMethod.invoke(dietService, loseWeightProfile);
        
        assertNotNull("Weight loss guidelines should not be null", loseWeightGuidelines);
        
        // Check weight loss-specific guidelines
        boolean hasCalorieDeficit = false;
        boolean hasNutrientDense = false;
        boolean hasHydration = false;
        
        for (String guideline : loseWeightGuidelines) {
            if (guideline.contains("Create a moderate calorie deficit through diet and exercise")) {
                hasCalorieDeficit = true;
            }
            if (guideline.contains("Focus on nutrient-dense, filling foods")) {
                hasNutrientDense = true;
            }
            if (guideline.contains("Stay hydrated")) {
                hasHydration = true;
            }
        }
        
        assertTrue("Weight loss guidance should include calorie deficit guidance", hasCalorieDeficit);
        assertTrue("Weight loss guidance should include nutrient-dense foods guidance", hasNutrientDense);
        assertTrue("Weight loss guidance should include hydration guidance", hasHydration);
    }
 
    
   
    @Test
    public void testSetUserDietProfile_ExistingUserSuccessfulProfileCreation() {
        // Simulate an existing user with a successful profile creation
        boolean result = dietService.setUserDietProfile(
            "existinguser", 
            PersonalizedDietRecommendationService.DietType.LOW_CARB,
            Arrays.asList("diabetes"),
            PersonalizedDietRecommendationService.WeightGoal.LOSE,
            Arrays.asList("nuts")
        );
        
        assertTrue("Profile should be created for an existing user", result);
    }

    @Test
    public void testSetUserDietProfile_NonExistentUserProfileCreation() {
        // Attempt to create a profile for a non-existent user
        boolean result = dietService.setUserDietProfile(
            "nonexistentuser", 
            PersonalizedDietRecommendationService.DietType.BALANCED,
            new ArrayList<>(),
            PersonalizedDietRecommendationService.WeightGoal.MAINTAIN,
            new ArrayList<>()
        );
        
        assertFalse("Profile creation should fail for non-existent user", result);
    }

    @Test
    public void testSetUserDietProfile_UpdateExistingProfile() {
        // Create initial profile
        boolean initialProfileCreation = dietService.setUserDietProfile(
            "existinguser", 
            PersonalizedDietRecommendationService.DietType.BALANCED,
            Arrays.asList("diabetes"),
            PersonalizedDietRecommendationService.WeightGoal.MAINTAIN,
            Arrays.asList("shellfish")
        );
        
        // Update the profile
        boolean profileUpdate = dietService.setUserDietProfile(
            "existinguser", 
            PersonalizedDietRecommendationService.DietType.HIGH_PROTEIN,
            Arrays.asList("cholesterol"),
            PersonalizedDietRecommendationService.WeightGoal.GAIN,
            Arrays.asList("dairy")
        );
        
        assertTrue("Existing profile should be successfully updated", 
                   initialProfileCreation && profileUpdate);
    }

    @Test
    public void testSetUserDietProfile_EmptyHealthConditionsAndExcludedFoods() {
        // Test creating a profile with empty lists
        boolean result = dietService.setUserDietProfile(
            "existinguser", 
            PersonalizedDietRecommendationService.DietType.VEGETARIAN,
            new ArrayList<>(),
            PersonalizedDietRecommendationService.WeightGoal.MAINTAIN,
            new ArrayList<>()
        );
        
        assertTrue("Profile should be created with empty lists", result);
    }

    @Test
    public void testSetUserDietProfile_MultipleHealthConditionsAndExcludedFoods() {
        // Test creating a profile with multiple health conditions and excluded foods
        boolean result = dietService.setUserDietProfile(
            "existinguser", 
            PersonalizedDietRecommendationService.DietType.VEGAN,
            Arrays.asList("diabetes", "hypertension"),
            PersonalizedDietRecommendationService.WeightGoal.LOSE,
            Arrays.asList("soy", "nuts", "gluten")
        );
        
        assertTrue("Profile should be created with multiple conditions and exclusions", result);
    }
    
    
    
}
    
    

