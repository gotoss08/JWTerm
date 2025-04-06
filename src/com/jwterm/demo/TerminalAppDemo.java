package com.jwterm.demo;

import com.jwterm.JWTerm;
import com.jwterm.TermScreen;
import com.jwterm.utils.LoggingUtility;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TerminalAppDemo extends JWTerm {

    private static final Logger LOGGER = LoggingUtility.getLogger(TerminalAppDemo.class.getName());
    private boolean drawResizeInfo = true;
    private String resizeMessage = "";
    private long resizeMessageTime = 0;
    private static final long RESIZE_MESSAGE_DURATION = 3000; // 3 seconds

    private static final TermScreen.Glyph topLeftCorner = new TermScreen.Glyph('╔', Color.CYAN, Color.BLACK);
    private static final TermScreen.Glyph topRightCorner = new TermScreen.Glyph('╗', Color.CYAN, Color.BLACK);
    private static final TermScreen.Glyph bottomLeftCorner = new TermScreen.Glyph('╚', Color.CYAN, Color.BLACK);
    private static final TermScreen.Glyph bottomRightCorner = new TermScreen.Glyph('╝', Color.CYAN, Color.BLACK);

    public TerminalAppDemo() {
        super("JWTerm Demo", 1280, 720);
        LoggingUtility.setLogLevel(Level.INFO);
    }

    public void start() {
        // Initialize your application here
        LOGGER.info("Starting terminal application");
        runGameLoop();
    }

    @Override
    protected void resize(int width, int height) {
        // This method is called when the window is resized
        LOGGER.info("handleResize called with dimensions: " + width + "x" + height);

        // Add your application-specific resize handling logic here
        resizeMessage = "Window resized to: " + width + "x" + height +
                " | Terminal: " + termScreen.getDimension().getCols() + "x" + termScreen.getDimension().getRows();
        drawResizeInfo = true;
        resizeMessageTime = System.currentTimeMillis();
    }

    @Override
    protected void update(double deltaTime) {
        // Update your application logic here

        // Example: fill space and draw border
        termScreen.fill(TermScreen.Glyph.SPACE);
        termScreen.outline(TermScreen.Glyph.WALL);

        // Example: Add characters at the corners
        termScreen.setGlyph(0, 0, topLeftCorner);
        termScreen.setGlyph(0, termScreen.getDimension().getCols() - 1, topRightCorner);
        termScreen.setGlyph(termScreen.getDimension().getRows() - 1, 0, bottomLeftCorner);
        termScreen.setGlyph(termScreen.getDimension().getRows() - 1, termScreen.getDimension().getCols() - 1, bottomRightCorner);

        // Clear resize message after duration
        if (drawResizeInfo && System.currentTimeMillis() - resizeMessageTime > RESIZE_MESSAGE_DURATION) {
            drawResizeInfo = false;
        }
    }

    @Override
    protected void render(Graphics2D g, double deltaTime) {
        // Render your application here

        // Display resize information if available
        if (drawResizeInfo) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Monospaced", Font.BOLD, 14));
            g.drawString(resizeMessage, 20, 60);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used in this implementation
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

        if (e.getKeyCode() == KeyEvent.VK_F5) {
            // Example: Trigger a manual resize event
            LOGGER.info("F5 pressed, forcing resize event");
            forceResize();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used in this implementation
    }

    public static void main(String[] args) {
        TerminalAppDemo app = new TerminalAppDemo();
        app.start();
    }
}
