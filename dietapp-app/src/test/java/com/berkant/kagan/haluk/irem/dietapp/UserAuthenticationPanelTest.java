package com.berkant.kagan.haluk.irem.dietapp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Unit tests for the UserAuthenticationPanel class.
 * These tests verify the functionality of login, registration,
 * and UI components of the authentication panel.
 */
public class UserAuthenticationPanelTest {

    // The panel under test
    private UserAuthenticationPanel authPanel;
    
    // Mock services and utilities
    private MockAuthenticationService mockAuthService;
    
    // UI components to be accessed via reflection
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    // Flag to track if callback was called
    private boolean callbackCalled;
    
    /**
     * Setup method that runs before each test.
     * Initializes the panel, mock services, and extracts UI components.
     */
    @Before
    public void setUp() throws Exception {
        // Initialize mock services
        mockAuthService = new MockAuthenticationService();
        
        // Reset callback flag
        callbackCalled = false;
        
        // Initialize the panel with mock service
        authPanel = new UserAuthenticationPanel(mockAuthService);
        
        // Set login success callback
        authPanel.setLoginSuccessCallback(() -> callbackCalled = true);
        
        // Extract UI components using reflection
        usernameField = (JTextField) getPrivateField(authPanel, "usernameField");
        passwordField = (JPasswordField) getPrivateField(authPanel, "passwordField");
    }
    
    /**
     * Cleanup method that runs after each test.
     * Clears all references.
     */
    @After
    public void tearDown() {
        authPanel = null;
        mockAuthService = null;
        usernameField = null;
        passwordField = null;
    }
    
    /**
     * Test that the panel initializes correctly with all UI components.
     */
    @Test
    public void testPanelInitialization() throws Exception {
        // Verify UI components are properly initialized
        assertNotNull("Username field should be initialized", usernameField);
        assertNotNull("Password field should be initialized", passwordField);
        
        // Check that the panel contains the necessary components
        boolean hasLoginButton = false;
        boolean hasRegisterButton = false;
        
        // Check for login and register buttons
        for (Component component : authPanel.getComponents()) {
            if (component instanceof JPanel) {
                for (Component subComp : ((JPanel) component).getComponents()) {
                    if (subComp instanceof JButton) {
                        JButton button = (JButton) subComp;
                        if ("Login".equals(button.getText())) {
                            hasLoginButton = true;
                        } else if ("Register".equals(button.getText())) {
                            hasRegisterButton = true;
                        }
                    }
                }
            }
        }
        
        assertTrue("Panel should contain a Login button", hasLoginButton);
        assertTrue("Panel should contain a Register button", hasRegisterButton);
    }
    
    /**
     * Test successful login functionality.
     */
    @Test
    public void testSuccessfulLogin() throws Exception {
        // Set username and password for successful login
        usernameField.setText("validUser");
        passwordField.setText("validPass");
        
        // Set mock service to return success
        mockAuthService.setNextLoginResult(true);
        
        // Call the login method directly
        invokePrivateMethod(authPanel, "handleLogin");
        
        // Verify service was called with correct parameters
        assertEquals("validUser", mockAuthService.getLastUsername());
        assertEquals("validPass", mockAuthService.getLastPassword());
        
        // Verify login success callback was called
        assertTrue("Login success callback should be called", callbackCalled);
    }
    
    /**
     * Test failed login functionality.
     */
    @Test
    public void testFailedLogin() throws Exception {
        // Set username and password for failed login
        usernameField.setText("invalidUser");
        passwordField.setText("invalidPass");
        
        // Set mock service to return failure
        mockAuthService.setNextLoginResult(false);
        
        // Call the login method directly
        invokePrivateMethod(authPanel, "handleLogin");
        
        // Verify service was called with correct parameters
        assertEquals("invalidUser", mockAuthService.getLastUsername());
        assertEquals("invalidPass", mockAuthService.getLastPassword());
        
        // Verify login success callback was not called
        assertFalse("Login success callback should not be called", callbackCalled);
    }
    
    /**
     * Test login with empty username.
     */
    @Test
    public void testLoginWithEmptyUsername() throws Exception {
        // Set empty username and valid password
        usernameField.setText("");
        passwordField.setText("validPass");
        
        // Call the login method directly
        invokePrivateMethod(authPanel, "handleLogin");
        
        // Verify service was not called
        assertNull("Auth service should not be called", mockAuthService.getLastUsername());
        
        // Verify login success callback was not called
        assertFalse("Login success callback should not be called", callbackCalled);
    }
    
    /**
     * Test login with empty password.
     */
    @Test
    public void testLoginWithEmptyPassword() throws Exception {
        // Set valid username and empty password
        usernameField.setText("validUser");
        passwordField.setText("");
        
        // Call the login method directly
        invokePrivateMethod(authPanel, "handleLogin");
        
        // Verify service was not called
        assertNull("Auth service should not be called", mockAuthService.getLastUsername());
        
        // Verify login success callback was not called
        assertFalse("Login success callback should not be called", callbackCalled);
    }
    
    /**
     * Test successful registration functionality.
     */
    @Test
    public void testSuccessfulRegistration() throws Exception {
        // Set username and password for successful registration
        usernameField.setText("newUser");
        passwordField.setText("newPass");
        
        // Set mock service to return success
        mockAuthService.setNextRegisterResult(true);
        
        // Call the registration method directly
        invokePrivateMethod(authPanel, "handleRegister");
        
        // Verify service was called with correct parameters
        assertEquals("newUser", mockAuthService.getLastUsername());
        assertEquals("newPass", mockAuthService.getLastPassword());
        assertEquals("newUser@example.com", mockAuthService.getLastEmail());
        assertEquals("newUser", mockAuthService.getLastName());
        
        // Verify fields were cleared
        assertEquals("", usernameField.getText());
        assertEquals("", new String(passwordField.getPassword()));
    }
    
    /**
     * Test failed registration functionality.
     */
    @Test
    public void testFailedRegistration() throws Exception {
        // Set username and password for failed registration
        usernameField.setText("existingUser");
        passwordField.setText("newPass");
        
        // Set mock service to return failure
        mockAuthService.setNextRegisterResult(false);
        
        // Call the registration method directly
        invokePrivateMethod(authPanel, "handleRegister");
        
        // Verify service was called with correct parameters
        assertEquals("existingUser", mockAuthService.getLastUsername());
        assertEquals("newPass", mockAuthService.getLastPassword());
        
        // Verify fields were not cleared
        assertEquals("existingUser", usernameField.getText());
        assertEquals("newPass", new String(passwordField.getPassword()));
    }
    
    /**
     * Test registration with empty username.
     */
    @Test
    public void testRegistrationWithEmptyUsername() throws Exception {
        // Set empty username and valid password
        usernameField.setText("");
        passwordField.setText("newPass");
        
        // Call the registration method directly
        invokePrivateMethod(authPanel, "handleRegister");
        
        // Verify service was not called
        assertNull("Auth service should not be called for registration", mockAuthService.getLastEmail());
    }
    
    /**
     * Test registration with empty password.
     */
    @Test
    public void testRegistrationWithEmptyPassword() throws Exception {
        // Set valid username and empty password
        usernameField.setText("newUser");
        passwordField.setText("");
        
        // Call the registration method directly
        invokePrivateMethod(authPanel, "handleRegister");
        
        // Verify service was not called
        assertNull("Auth service should not be called for registration", mockAuthService.getLastEmail());
    }
    
    /**
     * Test the login button action.
     */
    @Test
    public void testLoginButtonAction() throws Exception {
        // Find the login button
        JButton loginButton = findButtonByText(authPanel, "Login");
        assertNotNull("Login button should exist", loginButton);
        
        // Set up for successful login
        usernameField.setText("validUser");
        passwordField.setText("validPass");
        mockAuthService.setNextLoginResult(true);
        
        // Simulate button click
        loginButton.getActionListeners()[0].actionPerformed(
            new ActionEvent(loginButton, ActionEvent.ACTION_PERFORMED, "Login")
        );
        
        // Verify login was attempted
        assertEquals("validUser", mockAuthService.getLastUsername());
        assertEquals("validPass", mockAuthService.getLastPassword());
        
        // Verify callback was called
        assertTrue("Login success callback should be called", callbackCalled);
    }
    
    /**
     * Test the register button action.
     */
    @Test
    public void testRegisterButtonAction() throws Exception {
        // Find the register button
        JButton registerButton = findButtonByText(authPanel, "Register");
        assertNotNull("Register button should exist", registerButton);
        
        // Set up for successful registration
        usernameField.setText("newUser");
        passwordField.setText("newPass");
        mockAuthService.setNextRegisterResult(true);
        
        // Simulate button click
        registerButton.getActionListeners()[0].actionPerformed(
            new ActionEvent(registerButton, ActionEvent.ACTION_PERFORMED, "Register")
        );
        
        // Verify registration was attempted
        assertEquals("newUser", mockAuthService.getLastUsername());
        assertEquals("newPass", mockAuthService.getLastPassword());
        assertEquals("newUser@example.com", mockAuthService.getLastEmail());
    }
    
    /**
     * Test the styled button creation.
     */
    @Test
    public void testCreateStyledButton() throws Exception {
        // Use reflection to call the createStyledButton method
        Method method = UserAuthenticationPanel.class.getDeclaredMethod(
            "createStyledButton", String.class, Color.class);
        method.setAccessible(true);
        
        // Create a test button
        JButton button = (JButton) method.invoke(authPanel, "TestButton", Color.BLUE);
        
        // Verify button properties
        assertEquals("TestButton", button.getText());
        assertEquals(Color.BLUE, button.getBackground());
        assertEquals(Color.WHITE, button.getForeground());
        assertFalse(button.isFocusPainted());
        assertFalse(button.isBorderPainted());
        
        // Verify dimensions
        Dimension preferredSize = button.getPreferredSize();
        assertEquals(120, preferredSize.width);
        assertEquals(35, preferredSize.height);
    }
    
    /**
     * Test the login success callback setting.
     */
    @Test
    public void testSetLoginSuccessCallback() {
        // Create new flag for this test
        final boolean[] testFlag = {false};
        
        // Set a new callback
        authPanel.setLoginSuccessCallback(() -> testFlag[0] = true);
        
        // Set up for successful login
        usernameField.setText("validUser");
        passwordField.setText("validPass");
        mockAuthService.setNextLoginResult(true);
        
        // Call the login method directly
        try {
            invokePrivateMethod(authPanel, "handleLogin");
        } catch (Exception e) {
            fail("Exception while calling handleLogin: " + e.getMessage());
        }
        
        // Verify new callback was called
        assertTrue("New login success callback should be called", testFlag[0]);
    }
    
    /**
     * Test the login method with a service exception.
     */
    @Test
    public void testLoginWithServiceException() throws Exception {
        // Set username and password
        usernameField.setText("errorUser");
        passwordField.setText("errorPass");
        
        // Set mock service to throw exception
        mockAuthService.setThrowException(true);
        
        // Call the login method directly
        invokePrivateMethod(authPanel, "handleLogin");
        
        // Verify login success callback was not called
        assertFalse("Login success callback should not be called", callbackCalled);
    }
    
    /**
     * Test the register method with a service exception.
     */
    @Test
    public void testRegisterWithServiceException() throws Exception {
        // Set username and password
        usernameField.setText("errorUser");
        passwordField.setText("errorPass");
        
        // Set mock service to throw exception
        mockAuthService.setThrowException(true);
        
        // Call the register method directly
        invokePrivateMethod(authPanel, "handleRegister");
        
        // Fields should not be cleared after exception
        assertEquals("errorUser", usernameField.getText());
        assertEquals("errorPass", new String(passwordField.getPassword()));
    }
    
    /**
     * Helper method to get a private field from an object using reflection.
     */
    private Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
    
    /**
     * Helper method to invoke a private method on an object using reflection.
     */
    private Object invokePrivateMethod(Object obj, String methodName, Object... args) throws Exception {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }
        
        Method method = obj.getClass().getDeclaredMethod(methodName, argTypes);
        method.setAccessible(true);
        return method.invoke(obj, args);
    }
    
    /**
     * Helper method to find a button by its text.
     */
    private JButton findButtonByText(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton && text.equals(((JButton) component).getText())) {
                return (JButton) component;
            } else if (component instanceof Container) {
                JButton button = findButtonByText((Container) component, text);
                if (button != null) {
                    return button;
                }
            }
        }
        return null;
    }
    
    /**
     * Mock implementation of AuthenticationService for testing.
     */
    private static class MockAuthenticationService extends AuthenticationService {
        private String lastUsername;
        private String lastPassword;
        private String lastEmail;
        private String lastName;
        
        private boolean nextLoginResult = false;
        private boolean nextRegisterResult = false;
        private boolean throwException = false;
        
        /**
         * Set the result to be returned by the next login attempt.
         */
        public void setNextLoginResult(boolean result) {
            this.nextLoginResult = result;
        }
        
        /**
         * Set the result to be returned by the next registration attempt.
         */
        public void setNextRegisterResult(boolean result) {
            this.nextRegisterResult = result;
        }
        
        /**
         * Set whether to throw an exception on next service call.
         */
        public void setThrowException(boolean throwException) {
            this.throwException = throwException;
        }
        
        /**
         * Get the username from the last login/register attempt.
         */
        public String getLastUsername() {
            return lastUsername;
        }
        
        /**
         * Get the password from the last login/register attempt.
         */
        public String getLastPassword() {
            return lastPassword;
        }
        
        /**
         * Get the email from the last register attempt.
         */
        public String getLastEmail() {
            return lastEmail;
        }
        
        /**
         * Get the name from the last register attempt.
         */
        public String getLastName() {
            return lastName;
        }
        
        @Override
        public boolean login(String username, String password) {
            if (throwException) {
                throw new RuntimeException("Mock login exception");
            }
            
            this.lastUsername = username;
            this.lastPassword = password;
            return nextLoginResult;
        }
        
        @Override
        public boolean register(String username, String password, String email, String name) {
            if (throwException) {
                throw new RuntimeException("Mock register exception");
            }
            
            this.lastUsername = username;
            this.lastPassword = password;
            this.lastEmail = email;
            this.lastName = name;
            return nextRegisterResult;
        }
        
        @Override
        public User getCurrentUser() {
            if (nextLoginResult) {
                User mockUser = new User(lastUsername, lastPassword, "test@example.com", "Test User");
                mockUser.setLoggedIn(true);
                return mockUser;
            }
            return null;
        }
    }
}