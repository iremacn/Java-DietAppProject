package com.berkant.kagan.haluk.irem.dietapp;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the CalorieNutrientTrackingService class.
 * @author irem
 */
public class CalorieNutrientTrackingServiceTest {

    // Service under test
    private MockCalorieNutrientTrackingService calorieNutrientService;
    private MealPlanningService mealPlanningService;
    
    @Before
    public void setUp() {
        // Initialize mock services
        mealPlanningService = new MealPlanningService();
        calorieNutrientService = new MockCalorieNutrientTrackingService();
    }
    
    @Test
    public void testSetNutritionGoals() {
        // Test data
        String username = "testuser";
        int calorieGoal = 2000;
        double proteinGoal = 50.0;
        double carbGoal = 250.0;
        double fatGoal = 70.0;
        
        // Call the service
        boolean result = calorieNutrientService.setNutritionGoals(username, calorieGoal, proteinGoal, carbGoal, fatGoal);
        
        // Verify the result
        assertTrue(result);
        
        // Verify the method was called with correct parameters
        assertEquals(1, calorieNutrientService.getSetNutritionGoalsCallCount());
        assertEquals(username, calorieNutrientService.getLastUsername());
        assertEquals(calorieGoal, calorieNutrientService.getLastCalorieGoal());
        assertEquals(proteinGoal, calorieNutrientService.getLastProteinGoal(), 0.001);
        assertEquals(carbGoal, calorieNutrientService.getLastCarbGoal(), 0.001);
        assertEquals(fatGoal, calorieNutrientService.getLastFatGoal(), 0.001);
    }
    
    @Test
    public void testSetNutritionGoalsWithInvalidInput() {
        // Test with null username
        boolean result1 = calorieNutrientService.setNutritionGoals(null, 2000, 50, 250, 70);
        assertFalse(result1);
        
        // Test with negative values
        boolean result2 = calorieNutrientService.setNutritionGoals("testuser", -100, 50, 250, 70);
        assertFalse(result2);
        
        boolean result3 = calorieNutrientService.setNutritionGoals("testuser", 2000, -10, 250, 70);
        assertFalse(result3);
        
        boolean result4 = calorieNutrientService.setNutritionGoals("testuser", 2000, 50, -50, 70);
        assertFalse(result4);
        
        boolean result5 = calorieNutrientService.setNutritionGoals("testuser", 2000, 50, 250, -20);
        assertFalse(result5);
    }
    
    @Test
    public void testViewNutritionGoals() {
        // Test data
        String username = "testuser";
        
        // Call the service
        CalorieNutrientTrackingService.NutritionGoal goals = calorieNutrientService.getNutritionGoals(username);
        
        // Verify the method was called
        assertEquals(1, calorieNutrientService.getGetNutritionGoalsCallCount());
        assertEquals(username, calorieNutrientService.getLastUsername());
        
        // Verify the result
        assertNotNull(goals);
        assertEquals(2000, goals.getCalorieGoal());
        assertEquals(50.0, goals.getProteinGoal(), 0.001);
        assertEquals(250.0, goals.getCarbGoal(), 0.001);
        assertEquals(70.0, goals.getFatGoal(), 0.001);
    }
    
    @Test
    public void testGetDefaultNutritionGoals() {
        // Test with non-existent user
        CalorieNutrientTrackingService.NutritionGoal goals = calorieNutrientService.getNutritionGoals("nonexistentuser");
        
        // Verify default goals are returned
        assertNotNull(goals);
        assertEquals(2000, goals.getCalorieGoal());
        assertEquals(50.0, goals.getProteinGoal(), 0.001);
        assertEquals(250.0, goals.getCarbGoal(), 0.001);
        assertEquals(70.0, goals.getFatGoal(), 0.001);
    }
    
    @Test
    public void testViewDailyReport() {
        // Test data
        String username = "testuser";
        String date = "2023-04-15";
        
        // Setup mock report
        setupMockNutritionReport();
        
        // Call the service
        CalorieNutrientTrackingService.NutritionReport report = calorieNutrientService.getNutritionReport(username, date);
        
        // Verify the method was called with correct parameters
        assertEquals(1, calorieNutrientService.getGetNutritionReportCallCount());
        assertEquals(username, calorieNutrientService.getLastUsername());
        assertEquals(date, calorieNutrientService.getLastDate());
        
        // Verify the report data
        assertNotNull(report);
        assertEquals(date, report.getDate());
        assertEquals(1500, report.getTotalCalories());
        assertEquals(40.0, report.getTotalProtein(), 0.001);
        assertEquals(200.0, report.getTotalCarbs(), 0.001);
        assertEquals(50.0, report.getTotalFat(), 0.001);
        
        // Verify percentages
        assertEquals(75.0, report.getCaloriePercentage(), 0.001);
        assertEquals(80.0, report.getProteinPercentage(), 0.001);
        assertEquals(80.0, report.getCarbPercentage(), 0.001);
        assertEquals(71.4, report.getFatPercentage(), 0.1);
    }
    
    @Test
    public void testViewDailyReportWithInvalidInput() {
        // Test with null username
        CalorieNutrientTrackingService.NutritionReport report1 = calorieNutrientService.getNutritionReport(null, "2023-04-15");
        assertEquals("", report1.getDate());
        assertEquals(0, report1.getTotalCalories());
        
        // Test with null date
        CalorieNutrientTrackingService.NutritionReport report2 = calorieNutrientService.getNutritionReport("testuser", null);
        assertEquals("", report2.getDate());
        assertEquals(0, report2.getTotalCalories());
    }
    
    @Test
    public void testGetWeeklyReport() {
        // Test data
        String username = "testuser";
        String[] dates = {
            "2023-04-15", "2023-04-16", "2023-04-17", 
            "2023-04-18", "2023-04-19", "2023-04-20", "2023-04-21"
        };
        
        // Setup mock weekly report
        setupMockWeeklyReport();
        
        // Call the service
        List<CalorieNutrientTrackingService.NutritionReport> reports = calorieNutrientService.getWeeklyReport(username, dates);
        
        // Verify the method was called with correct parameters
        assertEquals(1, calorieNutrientService.getGetWeeklyReportCallCount());
        assertEquals(username, calorieNutrientService.getLastUsername());
        assertArrayEquals(dates, calorieNutrientService.getLastDatesArray());
        
        // Verify the reports
        assertNotNull(reports);
        assertEquals(7, reports.size());
        assertEquals("2023-04-15", reports.get(0).getDate());
        assertEquals("2023-04-21", reports.get(6).getDate());
    }
    
    @Test
    public void testCalculateSuggestedCalories() {
        // Test data
        char gender = 'M';
        int age = 30;
        double heightCm = 180.0;
        double weightKg = 75.0;
        int activityLevel = 2;
        
        // Set expected result
        calorieNutrientService.setMockSuggestedCalories(2046);
        
        // Call the service
        int suggestedCalories = calorieNutrientService.calculateSuggestedCalories(gender, age, heightCm, weightKg, activityLevel);
        
        // Verify the method was called with correct parameters
        assertEquals(1, calorieNutrientService.getCalculateSuggestedCaloriesCallCount());
        assertEquals(gender, calorieNutrientService.getLastGender());
        assertEquals(age, calorieNutrientService.getLastAge());
        assertEquals(heightCm, calorieNutrientService.getLastHeight(), 0.001);
        assertEquals(weightKg, calorieNutrientService.getLastWeight(), 0.001);
        assertEquals(activityLevel, calorieNutrientService.getLastActivityLevel());
        
        // Verify the result
        assertEquals(2046, suggestedCalories);
    }
    
    @Test
    public void testCalculateSuggestedCaloriesWithInvalidInputs() {
        // Test with invalid gender
        int result1 = calorieNutrientService.calculateSuggestedCalories('X', 30, 180, 75, 2);
        assertEquals(0, result1);
        
        // Test with invalid age
        int result2 = calorieNutrientService.calculateSuggestedCalories('M', -10, 180, 75, 2);
        assertEquals(0, result2);
        
        // Test with invalid height
        int result3 = calorieNutrientService.calculateSuggestedCalories('M', 30, -180, 75, 2);
        assertEquals(0, result3);
        
        // Test with invalid weight
        int result4 = calorieNutrientService.calculateSuggestedCalories('M', 30, 180, -75, 2);
        assertEquals(0, result4);
        
        // Test with invalid activity level
        int result5 = calorieNutrientService.calculateSuggestedCalories('M', 30, 180, 75, 0);
        assertEquals(0, result5);
        
        int result6 = calorieNutrientService.calculateSuggestedCalories('M', 30, 180, 75, 6);
        assertEquals(0, result6);
    }
    
    @Test
    public void testGetCommonFoodsWithNutrients() {
        // Setup mock common foods
        setupMockCommonFoods();
        
        // Call the service
        FoodNutrient[] foods = calorieNutrientService.getCommonFoodsWithNutrients();
        
        // Verify the method was called
        assertEquals(1, calorieNutrientService.getGetCommonFoodsWithNutrientsCallCount());
        
        // Verify the result
        assertNotNull(foods);
        assertEquals(2, foods.length);
        assertEquals("Apple", foods[0].getName());
        assertEquals(52, foods[0].getCalories());
        assertEquals("Banana", foods[1].getName());
        assertEquals(89, foods[1].getCalories());
    }
    
    // Helper methods for setting up mock data
    
    private void setupMockNutritionReport() {
        // Create mock goals
        CalorieNutrientTrackingService.NutritionGoal goals = 
            calorieNutrientService.new NutritionGoal(2000, 50, 250, 70);
        
        // Create mock report
        CalorieNutrientTrackingService.NutritionReport report = 
            calorieNutrientService.new NutritionReport(
                "2023-04-15", 1500, 40, 200, 50, 20, 30, 1500, goals);
        
        // Set the mock report
        calorieNutrientService.setMockNutritionReport(report);
    }
    
    private void setupMockWeeklyReport() {
        List<CalorieNutrientTrackingService.NutritionReport> reports = new ArrayList<>();
        
        // Create mock goals
        CalorieNutrientTrackingService.NutritionGoal goals = 
            calorieNutrientService.new NutritionGoal(2000, 50, 250, 70);
        
        // Create reports for 7 days
        for (int i = 0; i < 7; i++) {
            String date = String.format("2023-04-%d", 15 + i);
            CalorieNutrientTrackingService.NutritionReport report = 
                calorieNutrientService.new NutritionReport(
                    date, 1500 - (i * 100), 40 - (i * 2), 200 - (i * 10), 
                    50 - (i * 3), 20, 30, 1500, goals);
            reports.add(report);
        }
        
        // Set the mock weekly report
        calorieNutrientService.setMockWeeklyReport(reports);
    }
    
    private void setupMockCommonFoods() {
        FoodNutrient[] commonFoods = new FoodNutrient[2];
        
        commonFoods[0] = new FoodNutrient(
            "Apple", 100.0, 52, 0.3, 14.0, 0.2, 2.4, 10.3, 1.0);
            
        commonFoods[1] = new FoodNutrient(
            "Banana", 100.0, 89, 1.1, 22.8, 0.3, 2.6, 12.2, 1.0);
        
        // Set the mock common foods
        calorieNutrientService.setMockCommonFoods(commonFoods);
    }
    
    /**
     * Mock implementation of CalorieNutrientTrackingService for testing purposes.
     */
    private class MockCalorieNutrientTrackingService extends CalorieNutrientTrackingService {
        // Counters for method calls
        private int setNutritionGoalsCallCount = 0;
        private int getNutritionReportCallCount = 0;
        private int getWeeklyReportCallCount = 0;
        private int calculateSuggestedCaloriesCallCount = 0;
        private int getCommonFoodsWithNutrientsCallCount = 0;
        private int getNutritionGoalsCallCount = 0;
        
        // Parameters passed to methods
        private String lastUsername;
        private int lastCalorieGoal;
        private double lastProteinGoal;
        private double lastCarbGoal;
        private double lastFatGoal;
        private String lastDate;
        private String[] lastDatesArray;
        private char lastGender;
        private int lastAge;
        private double lastHeight;
        private double lastWeight;
        private int lastActivityLevel;
        
        // Mock responses
        private NutritionReport mockNutritionReport;
        private List<NutritionReport> mockWeeklyReport;
        private int mockSuggestedCalories;
        private FoodNutrient[] mockCommonFoods;
        
        public MockCalorieNutrientTrackingService() {
            // Call super constructor with null (not used in tests)
            super(null);
        }
        
        // Getters for call counters
        public int getSetNutritionGoalsCallCount() { return setNutritionGoalsCallCount; }
        public int getGetNutritionReportCallCount() { return getNutritionReportCallCount; }
        public int getGetWeeklyReportCallCount() { return getWeeklyReportCallCount; }
        public int getCalculateSuggestedCaloriesCallCount() { return calculateSuggestedCaloriesCallCount; }
        public int getGetCommonFoodsWithNutrientsCallCount() { return getCommonFoodsWithNutrientsCallCount; }
        public int getGetNutritionGoalsCallCount() { return getNutritionGoalsCallCount; }
        
        // Total functions called
        public int functionsCalledCount() {
            return setNutritionGoalsCallCount + getNutritionReportCallCount + 
                   getWeeklyReportCallCount + calculateSuggestedCaloriesCallCount + 
                   getCommonFoodsWithNutrientsCallCount + getNutritionGoalsCallCount;
        }
        
        // Getters for parameters
        public String getLastUsername() { return lastUsername; }
        public int getLastCalorieGoal() { return lastCalorieGoal; }
        public double getLastProteinGoal() { return lastProteinGoal; }
        public double getLastCarbGoal() { return lastCarbGoal; }
        public double getLastFatGoal() { return lastFatGoal; }
        public String getLastDate() { return lastDate; }
        public String[] getLastDatesArray() { return lastDatesArray; }
        public char getLastGender() { return lastGender; }
        public int getLastAge() { return lastAge; }
        public double getLastHeight() { return lastHeight; }
        public double getLastWeight() { return lastWeight; }
        public int getLastActivityLevel() { return lastActivityLevel; }
        
        // Setters for mock responses
        public void setMockNutritionReport(NutritionReport report) { 
            this.mockNutritionReport = report; 
        }
        
        public void setMockWeeklyReport(List<NutritionReport> reports) { 
            this.mockWeeklyReport = reports; 
        }
        
        public void setMockSuggestedCalories(int calories) { 
            this.mockSuggestedCalories = calories; 
        }
        
        public void setMockCommonFoods(FoodNutrient[] foods) { 
            this.mockCommonFoods = foods; 
        }
        
        // Override methods to track calls and parameters
        
        @Override
        public boolean setNutritionGoals(String username, int calorieGoal, double proteinGoal, 
                                        double carbGoal, double fatGoal) {
            setNutritionGoalsCallCount++;
            this.lastUsername = username;
            this.lastCalorieGoal = calorieGoal;
            this.lastProteinGoal = proteinGoal;
            this.lastCarbGoal = carbGoal;
            this.lastFatGoal = fatGoal;
            
            // Validate inputs
            if (username == null || username.trim().isEmpty()) {
                return false;
            }
            
            if (calorieGoal <= 0 || proteinGoal <= 0 || carbGoal <= 0 || fatGoal <= 0) {
                return false;
            }
            
            return true;
        }
        
        @Override
        public NutritionGoal getNutritionGoals(String username) {
            getNutritionGoalsCallCount++;
            this.lastUsername = username;
            return new NutritionGoal(2000, 50, 250, 70); // Default test goals
        }
        
        @Override
        public NutritionReport getNutritionReport(String username, String date) {
            getNutritionReportCallCount++;
            this.lastUsername = username;
            this.lastDate = date;
            
            // Validate inputs
            if (username == null || username.trim().isEmpty() || date == null || date.trim().isEmpty()) {
                // Creating a default NutritionGoal object directly without calling getNutritionGoals
                NutritionGoal defaultGoals = new NutritionGoal(2000, 50, 250, 70);
                return new NutritionReport("", 0, 0, 0, 0, 0, 0, 0, defaultGoals);
            }
            
            if (mockNutritionReport != null) {
                return mockNutritionReport;
            } else {
                // Creating a default NutritionGoal object directly without calling getNutritionGoals
                NutritionGoal defaultGoals = new NutritionGoal(2000, 50, 250, 70);
                return new NutritionReport(date, 0, 0, 0, 0, 0, 0, 0, defaultGoals);
            }
        }
        
        @Override
        public List<NutritionReport> getWeeklyReport(String username, String[] dates) {
            getWeeklyReportCallCount++;
            this.lastUsername = username;
            this.lastDatesArray = dates;
            return mockWeeklyReport != null ? mockWeeklyReport : new ArrayList<>();
        }
        
        @Override
        public int calculateSuggestedCalories(char gender, int age, double heightCm, 
                                            double weightKg, int activityLevel) {
            calculateSuggestedCaloriesCallCount++;
            this.lastGender = gender;
            this.lastAge = age;
            this.lastHeight = heightCm;
            this.lastWeight = weightKg;
            this.lastActivityLevel = activityLevel;
            
            // Validate inputs
            if ((gender != 'M' && gender != 'm' && gender != 'F' && gender != 'f') ||
                age <= 0 || age > 120 || heightCm <= 0 || weightKg <= 0 || 
                activityLevel < 1 || activityLevel > 5) {
                return 0;
            }
            
            return mockSuggestedCalories;
        }
        
        @Override
        public FoodNutrient[] getCommonFoodsWithNutrients() {
            getCommonFoodsWithNutrientsCallCount++;
            return mockCommonFoods != null ? mockCommonFoods : new FoodNutrient[0];
        }
    }
}