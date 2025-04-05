package com.jwterm;

import com.jwterm.utils.LoggingUtility;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

/**
 * Main application entry point for JWTerm demo.
 */
public class Main {
    private static final Logger LOGGER = LoggingUtility.getLogger(Main.class.getName());
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;
    private static final String WINDOW_TITLE = "JWTerm Demo";

    /**
     * Application entry point.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        TerminalApplication app = new TerminalApplication();
        app.start();
    }

    /**
     * Terminal application implementation that handles key events and rendering.
     */
    private static class TerminalApplication extends JWTerm {

        /**
         * Initializes the terminal application with default window settings.
         */
        public TerminalApplication() {
            super(WINDOW_TITLE, WINDOW_WIDTH, WINDOW_HEIGHT);
        }

        /**
         * Starts the application.
         */
        public void start() {
            LOGGER.info("Starting terminal application");
            runGameLoop();
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // Not used in this implementation
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int modifiers = e.getModifiersEx();
            int keyCode = e.getKeyCode();

            logModifierKeys(modifiers);
            handleKeyboardShortcuts(modifiers, keyCode);
        }

        /**
         * Logs modifier keys being pressed.
         *
         * @param modifiers The keyboard modifiers from the key event
         */
        private void logModifierKeys(int modifiers) {
            if (isCtrlDown(modifiers)) {
                LOGGER.fine("CTRL key pressed");
            }

            if (isShiftDown(modifiers)) {
                LOGGER.fine("SHIFT key pressed");
            }
        }

        /**
         * Handles keyboard shortcuts for terminal commands.
         *
         * @param modifiers The keyboard modifiers from the key event
         * @param keyCode The key code from the key event
         */
        private void handleKeyboardShortcuts(int modifiers, int keyCode) {
            // Handle Ctrl+Shift combinations
            if (isCtrlShiftDown(modifiers)) {
                handleCtrlShiftShortcuts(keyCode);
            }

            // Handle ESC key
            if (keyCode == KeyEvent.VK_ESCAPE) {
                LOGGER.info("ESC pressed, exiting application");
                this.running = false;
            }
        }

        /**
         * Handles Ctrl+Shift key combinations.
         *
         * @param keyCode The key code from the key event
         */
        private void handleCtrlShiftShortcuts(int keyCode) {
            switch (keyCode) {
                case KeyEvent.VK_A:
                    LOGGER.info("Ctrl+Shift+A: Clearing screen");
                    termScreen.fill(null);
                    break;

                case KeyEvent.VK_B:
                    LOGGER.info("Ctrl+Shift+B: Filling screen with walls");
                    termScreen.fill(TermScreen.Glyph.WALL);
                    break;
            }
        }

        /**
         * Checks if Ctrl key is pressed.
         *
         * @param modifiers Keyboard modifiers
         * @return true if Ctrl is pressed
         */
        private boolean isCtrlDown(int modifiers) {
            return (modifiers & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK;
        }

        /**
         * Checks if Shift key is pressed.
         *
         * @param modifiers Keyboard modifiers
         * @return true if Shift is pressed
         */
        private boolean isShiftDown(int modifiers) {
            return (modifiers & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK;
        }

        /**
         * Checks if both Ctrl and Shift keys are pressed.
         *
         * @param modifiers Keyboard modifiers
         * @return true if both Ctrl and Shift are pressed
         */
        private boolean isCtrlShiftDown(int modifiers) {
            int ctrlShiftMask = KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK;
            return (modifiers & ctrlShiftMask) == ctrlShiftMask;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // Not used in this implementation
        }

        @Override
        protected void update(double deltaTime) {
            // No update logic required for this demo
        }

        @Override
        protected void render(Graphics2D g, double deltaTime) {
            // No additional rendering required for this demo
        }
    }
}