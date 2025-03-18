/**

@file DietappApp.java
@brief This file serves as the main application file for the Dietapp App.
@details This file contains the entry point of the application, which is the main method. It initializes the necessary components and executes the Dietapp App.
*/
/**

@package com.berkant.kagan.haluk.irem.dietapp
@brief The com.berkant.kagan.haluk.irem.dietapp package contains all the classes and files related to the Dietapp App.
*/
package com.berkant.kagan.haluk.irem.dietapp;

import java.util.Scanner;

/**
 *
 * @class DietappApp
 * @brief This class represents the main application class for the Dietapp
 *        App.
 * @details The DietappApp class provides the entry point for the Dietapp
 *          App. It initializes the necessary components, performs calculations,
 *          and handles exceptions.
 * @author ugur.coruh
 */
public class DietappApp {
	// Private fields for encapsulation
    private Dietapp dietapp;
    private Scanner scanner;
    
    /**
     * Constructor for DietappApp class.
     * Initializes the Dietapp and Scanner objects.
     */
    public DietappApp() {
        this.dietapp = new Dietapp();
        this.scanner = new Scanner(System.in);
    }

  /**
   * @brief The main entry point of the Dietapp App.
   *
   * @details The main method is the starting point of the Dietapp App. It
   *          initializes the logger, performs logging, displays a greeting
   *          message, and handles user input.
   *
   * @param args The command-line arguments passed to the application.
   */
  public static void main(String[] args) {
    
	  // Create an instance of the application
      DietappApp app = new DietappApp();
      
      // Run the application
      app.run();
  }
  
  /**
   * Runs the main application loop.
   */
  public void run() {
      boolean running = true;
      
      System.out.println("Welcome to Diet Planner Application!");
      
      while (running) {
          printMainMenu();
          int choice = getUserChoice();
          
          switch (choice) {
              case 1:
                  handleLogin();
                  break;
              case 2:
                  handleRegistration();
                  break;
              case 3:
                  handleGuestMode();
                  break;
              case 0:
                  running = false;
                  System.out.println("Thank you for using Diet Planner. Goodbye!");
                  break;
              default:
                  System.out.println("Invalid choice. Please try again.");
          }
      }
      
      // Close the scanner
      scanner.close();
  }
  
  /**
   * Prints the main authentication menu.
   */
  private void printMainMenu() {
      System.out.println("\n===== Diet Planner Authentication =====");
      if (dietapp.isUserLoggedIn()) {
          User currentUser = dietapp.getCurrentUser();
          System.out.println("Logged in as: " + currentUser.getUsername());
          System.out.println("1. Log out");
          System.out.println("0. Exit");
      } else {
          System.out.println("1. Login");
          System.out.println("2. Register");
          System.out.println("3. Continue as Guest");
          System.out.println("0. Exit");
      }
      System.out.print("Enter your choice: ");
  }
  
  /**
   * Gets the user's menu choice from the console.
   * 
   * @return The user's choice as an integer
   */
  private int getUserChoice() {
      try {
          return Integer.parseInt(scanner.nextLine());
      } catch (NumberFormatException e) {
          return -1; // Invalid input
      }
  }
  
  /**
   * Handles the user login process.
   */
  private void handleLogin() {
      if (dietapp.isUserLoggedIn()) {
          // Log out if already logged in
          dietapp.logoutUser();
          System.out.println("You have been logged out.");
          return;
      }
      
      System.out.println("\n===== Login =====");
      
      System.out.print("Enter username: ");
      String username = scanner.nextLine();
      
      System.out.print("Enter password: ");
      String password = scanner.nextLine();
      
      boolean success = dietapp.loginUser(username, password);
      
      if (success) {
          System.out.println("Login successful! Welcome, " + dietapp.getCurrentUser().getName() + "!");
      } else {
          System.out.println("Login failed. Invalid username or password.");
      }
  }
  
  /**
   * Handles the user registration process.
   */
  private void handleRegistration() {
      System.out.println("\n===== Registration =====");
      
      System.out.print("Enter username: ");
      String username = scanner.nextLine();
      
      System.out.print("Enter password: ");
      String password = scanner.nextLine();
      
      System.out.print("Enter email: ");
      String email = scanner.nextLine();
      
      System.out.print("Enter your name: ");
      String name = scanner.nextLine();
      
      boolean success = dietapp.registerUser(username, password, email, name);
      
      if (success) {
          System.out.println("Registration successful! You can now log in.");
      } else {
          System.out.println("Registration failed. Username already exists.");
      }
  }
  
  /**
   * Handles guest mode login.
   */
  private void handleGuestMode() {
      dietapp.enableGuestMode();
      System.out.println("You are now using the application as a guest. Some features may be limited.");
  }
  
  public void showMainMenu() {
      while (true) {
          System.out.println("\n===== Main Menu =====");
          System.out.println("1. Customizable Work/Break Intervals");
          System.out.println("2. Progress Tracking");
          System.out.println("3. Reminder Alarms");
          System.out.println("4. Statistics on Study Patterns");
          System.out.println("0. Exit");
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
                  System.out.println("Exiting... Goodbye!");
                  return;
              default:
                  System.out.println("Invalid choice! Please try again.");
          }
      }
  }
  
  private void showWorkBreakMenu() {
      System.out.println("\n===== Customizable Work/Break Intervals =====");
      System.out.println("1. Set Work Interval");
      System.out.println("2. Set Break Interval");
  }
  
  private void showProgressTrackingMenu() {
      System.out.println("\n===== Progress Tracking =====");
      System.out.println("1. Start Study Session");
      System.out.println("2. Pause Study Session");
      System.out.println("3. Resume Study Session");
  }
  
  private void showReminderMenu() {
      System.out.println("\n===== Reminder Alarms =====");
      System.out.println("1. Set Alarms");
  }
  
  private void showStatisticsMenu() {
      System.out.println("\n===== Statistics on Study Patterns =====");
      System.out.println("1. View Study Statistics");
  }
  
  
  
  
  
  

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
}