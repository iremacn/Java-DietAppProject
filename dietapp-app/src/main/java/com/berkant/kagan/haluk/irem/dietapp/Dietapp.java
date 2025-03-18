/**

@file dietapp.java
@brief This file serves as a demonstration file for the dietapp class.
@details This file contains the implementation of the dietapp class, which provides various mathematical operations.
*/

/**

@package com.berkant.kagan.haluk.irem.dietapp
@brief The com.berkant.kagan.haluk.irem.dietapp package contains all the classes and files related to the dietapp App.
*/
package com.berkant.kagan.haluk.irem.dietapp;

import java.util.Scanner;


/**

@class dietapp
@brief This class represents a dietapp that performs mathematical operations.
@details The dietapp class provides methods to perform mathematical operations such as addition, subtraction, multiplication, and division. It also supports logging functionality using the logger object.
@author ugur.coruh
*/
public class Dietapp {
    // Private fields for encapsulation
    private AuthenticationService authService;
    private Scanner scanner;
    
    /**
     * Default constructor for DietApp class.
     * Initializes the authentication service.
     */
    public Dietapp() {
        this.authService = new AuthenticationService();
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Gets the authentication service.
     * 
     * @return The AuthenticationService instance
     */
    public AuthenticationService getAuthService() {
        return authService;
    }
    
    /**
     * Registers a new user in the system.
     * 
     * @param username The username for registration
     * @param password The password for registration
     * @param email    The email for registration
     * @param name     The name for registration
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(String username, String password, String email, String name) {
        return authService.register(username, password, email, name);
    }
    
    /**
     * Attempts to login a user with the provided credentials.
     * 
     * @param username The username for login
     * @param password The password for login
     * @return true if login successful, false otherwise
     */
    public boolean loginUser(String username, String password) {
    	boolean success = authService.login(username, password);
        if (success) {
            System.out.println("Login successful! Redirecting to Main Menu...");
            showMainMenu();
        } else {
            System.out.println("Login failed. Invalid username or password.");
        }
        return success;
        
        
    }
    
    /**
     * Logs out the current user.
     */
    public void logoutUser() {
        authService.logout();
        System.out.println("You have been logged out.");
    }
    
    public boolean isUserLoggedIn() {
        return authService.isUserLoggedIn();
    }
    
    /**
     * Enables guest mode for the application.
     */
    
        
    public void enableGuestMode() {
        authService.enableGuestMode();
        System.out.println("Guest mode enabled. Some features may be restricted.");
    }
    
    /**
     * Checks if a user is currently logged in.
     * 
     * @return true if a user is logged in, false otherwise
     */
    /**
     * Gets the currently logged in user.
     * 
     * @return The current user or null if no user is logged in
     */
    public User getCurrentUser() {
        return authService.getCurrentUser();
    }
   
    public void showMainMenu() {
        if (!isUserLoggedIn()) {
            System.out.println("You are not logged in. Redirecting to login screen...");
            return;
        }
        
        while (isUserLoggedIn()) {
            System.out.println("\n===== Main Menu =====");
            System.out.println("1. Customizable Work/Break Intervals");
            System.out.println("2. Progress Tracking");
            System.out.println("3. Reminder Alarms");
            System.out.println("4. Statistics on Study Patterns");
            System.out.println("0. Logout");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    showWorkBreakMenu();
                    break;
                case 2:
                    showProgressTrackingMenu();
                    break;
                case 3:
                    showReminderMenu();
                    break;
                case 4:
                    showStatisticsMenu();
                    break;
                case 0:
                    logoutUser();
                    return;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private void showWorkBreakMenu() {
        while (true) {
            System.out.println("\n===== Customizable Work/Break Intervals =====");
            System.out.println("1. Set Work Interval");
            System.out.println("2. Set Break Interval");
            System.out.println("3. View Current Intervals");
            System.out.println("0. Back to Main Menu");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter work interval in minutes: ");
                    int workInterval = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Work Interval Set to " + workInterval + " minutes!");
                    break;
                case 2:
                    System.out.print("Enter break interval in minutes: ");
                    int breakInterval = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Break Interval Set to " + breakInterval + " minutes!");
                    break;
                case 3:
                    System.out.println("Current Intervals: Work = 50 min, Break = 10 min (Example)");
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private void showProgressTrackingMenu() {
        while (true) {
            System.out.println("\n===== Progress Tracking =====");
            System.out.println("1. Start Study Session");
            System.out.println("2. Pause Study Session");
            System.out.println("3. Resume Study Session");
            System.out.println("4. View Study History");
            System.out.println("0. Back to Main Menu");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Study Session Started!");
                    break;
                case 2:
                    System.out.println("Study Session Paused!");
                    break;
                case 3:
                    System.out.println("Study Session Resumed!");
                    break;
                case 4:
                    System.out.println("Study History: \n- 2h 30m on 12/03/2024\n- 3h on 13/03/2024 (Example)");
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private void showReminderMenu() {
        while (true) {
            System.out.println("\n===== Reminder Alarms =====");
            System.out.println("1. Meal Reminders");
            System.out.println("2. Water Reminder");
            System.out.println("3. Add Custom Reminder");
            System.out.println("4. View All Reminders");
            System.out.println("0. Back to Main Menu");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    setMealReminder();
                    break;
                case 2:
                    setWaterReminder();
                    break;
                case 3:
                    addCustomReminder();
                    break;
                case 4:
                    System.out.println("Reminders: \n- Breakfast at 08:00\n- Water every 2 hours (Example)");
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private void setMealReminder() {
        System.out.print("Enter meal reminder times (e.g., 08:00, 12:00, 19:00): ");
        String mealTimes = scanner.nextLine();
        System.out.println("Meal reminders set for: " + mealTimes);
    }

    private void setWaterReminder() {
        System.out.print("Enter water intake interval in minutes: ");
        int interval = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Water reminder set every " + interval + " minutes.");
    }

    private void addCustomReminder() {
        System.out.print("Enter reminder title: ");
        String title = scanner.nextLine();
        System.out.print("Enter reminder time (e.g., 15:30): ");
        String time = scanner.nextLine();
        System.out.println("Reminder '" + title + "' set for " + time + ".");
    }

    private void showStatisticsMenu() {
        while (true) {
            System.out.println("\n===== Statistics on Study Patterns =====");
            System.out.println("1. View Study Statistics");
            System.out.println("2. View Weekly Summary");
            System.out.println("3. View Monthly Summary");
            System.out.println("0. Back to Main Menu");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Total Study Time: 10h 45m (Example)");
                    break;
                case 2:
                    System.out.println("Weekly Summary: 15h total study time (Example)");
                    break;
                case 3:
                    System.out.println("Monthly Summary: 60h total study time (Example)");
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }
       
    
    
    
    
    
}