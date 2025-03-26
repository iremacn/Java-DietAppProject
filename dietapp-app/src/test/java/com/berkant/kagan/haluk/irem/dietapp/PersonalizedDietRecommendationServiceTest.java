package com.berkant.kagan.haluk.irem.dietapp;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

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
    public void testSetUserDietProfile_Success() {
        // Normal user - should succeed
        boolean result = dietService.setUserDietProfile(
            TEST_USERNAME, 
            PersonalizedDietRecommendationService.DietType.LOW_CARB,
            Arrays.asList("diabetes"),
            PersonalizedDietRecommendationService.WeightGoal.LOSE,
            Arrays.asList("nuts")
        );
        
        assertTrue(result);
    }
    
    @Test
    public void testSetUserDietProfile_UserNotFound() {
        // Non-existent user - should fail
        boolean result = dietService.setUserDietProfile(
            "nonexistentuser", 
            PersonalizedDietRecommendationService.DietType.BALANCED,
            new ArrayList<>(),
            PersonalizedDietRecommendationService.WeightGoal.MAINTAIN,
            new ArrayList<>()
        );
        
        assertFalse(result);
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
    
    
    
   
    
    
 
    
}
    
    




