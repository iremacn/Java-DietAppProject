/**
 * @file Main.java
 * @brief Main entry point for the Diet Planner application
 * 
 * @details The Main class serves as the entry point for the Diet Planner application.
 *          It initializes the core services and menu system, and provides
 *          functionality for both normal operation and testing modes.
 * 
 * @author berkant
 * @version 1.0
 * @date 2024
 * @copyright Diet Planner Application
 */
package com.berkant.kagan.haluk.irem.dietapp;

/**
 * @class Main
 * @brief Main application class for Diet Planner
 * 
 * @details This class serves as the entry point for the Diet Planner application.
 *          It handles the initialization of core services and the menu system.
 *          The class supports both normal operation and testing modes through
 *          a test mode flag.
 */
public class Main {
    /** @brief Flag indicating whether the application is running in test mode */
    private static boolean testMode = false;
    
    /**
     * @brief Sets the test mode status
     * @details Updates the test mode flag that controls whether the application
     *          should run in test mode or normal operation mode.
     * 
     * @param mode The new test mode status
     */
    public static void setTestMode(boolean mode) {
        testMode = mode;
    }
    
    /**
     * @brief Retrieves the current test mode status
     * @details Returns whether the application is currently running in test mode.
     * 
     * @return true if the application is in test mode, false otherwise
     */
    public static boolean isTestMode() {
        return testMode;
    }
    
    /**
     * @brief Main entry point for the application
     * @details Initializes the core services and menu system, then starts
     *          the application's main menu if not in test mode.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Create services
        PersonalizedDietRecommendationService dietService = new PersonalizedDietRecommendationService(null, null);
        
        // Create menu class
        PersonalizedDietRecommendationMenu menu = new PersonalizedDietRecommendationMenu(dietService);
        
        // Start menu
        if (!testMode) {
            menu.displayMenu();
        }
    }
    
    /**
     * @brief Alternative main entry point for testing
     * @details Provides an alternative entry point that accepts a test menu instance.
     *          This allows for easier testing of the menu system by injecting
     *          a test menu implementation.
     * 
     * @param args Command line arguments (not used)
     * @param testMenu Optional test menu instance to use instead of creating a new one
     */
    public static void main(String[] args, PersonalizedDietRecommendationMenu testMenu) {
        PersonalizedDietRecommendationMenu menu = testMenu != null ? testMenu
            : new PersonalizedDietRecommendationMenu(new PersonalizedDietRecommendationService(null, null));
        if (!testMode) {
            menu.displayMenu();
        }
    }
} 