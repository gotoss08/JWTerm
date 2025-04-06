package com.jwterm.demo;

import com.jwterm.JWTerm;
import com.jwterm.TermScreen;
import com.jwterm.utils.LoggingUtility;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

public class TerminalAppDemo extends JWTerm {

    private static final Logger LOGGER = LoggingUtility.getLogger(TerminalAppDemo.class.getName());

    public TerminalAppDemo() {
        super("JWTerm Demo", 1280, 720);
    }

    public void start() {
        // Initialize your application here
        LOGGER.info("Starting terminal application");
        runGameLoop();
    }

    @Override
    protected void update(double deltaTime) {
        // Update your application logic here
    }

    @Override
    protected void render(Graphics2D g, double deltaTime) {
        // Add custom rendering here if needed
        // But for now there is no API for custom rendering
        // It will be added in the future
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used in this implementation
        // But you can handle character input here if needed
    }

    @Override
    public void keyPressed(KeyEvent e) {

        // Handle general key events here

        int modifiers = e.getModifiersEx();
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) {
            LOGGER.info("ESC pressed, exiting application");
            this.running = false;
        }

        // Example: check for modifier keys and specific key combinations

        if (isCtrlDown(modifiers) && isShiftDown(modifiers) && keyCode == KeyEvent.VK_A) {
            LOGGER.info("Ctrl+Shift+A: Clearing screen");
            termScreen.fill(null);
        }

        if (isCtrlDown(modifiers) && isShiftDown(modifiers) && keyCode == KeyEvent.VK_B) {
            LOGGER.info("Ctrl+Shift+B: Filling screen with walls");
            termScreen.fill(TermScreen.Glyph.WALL);
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used in this implementation
        // But you can handle key release events here if needed
    }

    public static void main(String[] args) {
        TerminalAppDemo app = new TerminalAppDemo();
        app.start();
    }

}
