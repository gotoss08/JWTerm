package com.jwterm;

import com.jwterm.utils.LoggingUtility;
import com.jwterm.utils.ResizeListener;
import com.jwterm.utils.ResizeManager;
import com.jwterm.utils.Timer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base class for terminal-based applications.
 * Handles window management, rendering, and game loop timing.
 */
public abstract class JWTerm implements KeyListener, ResizeListener {

    // Constants
    private static final Logger LOGGER = LoggingUtility.getLogger(JWTerm.class.getName());
    private static final int BUFFER_STRATEGY_COUNT = 2;
    private static final int TARGET_FPS = 60;
    private static final long OPTIMAL_TIME_NS = 1_000_000_000 / TARGET_FPS; // nanoseconds per frame
    private static final long FPS_UPDATE_INTERVAL_NS = 1_000_000_000; // 1 second in nanoseconds
    private static final String DEFAULT_FONT_PATH = "FSEX302.ttf";
    private static final float DEBUG_FONT_SIZE = 16.0f;
    private static final float DEFAULT_TERMINAL_FONT_SIZE = 25.0f;
    private static final int DEFAULT_TERMINAL_PADDING = 10;

    // UI Components
    private final JFrame frame;
    private final Canvas canvas;
    private final BufferStrategy bufferStrategy;
    private final ResizeManager resizeManager;
    private final Font debugFont;

    // Terminal emulation
    protected final TermScreen termScreen;

    // State and configuration
    protected volatile boolean running = true;
    private boolean showDebug = BuildConfig.showDebug;

    // Performance metrics
    private final Timer updateTimer = new Timer();
    private final Timer renderTimer = new Timer();
    private int fps = 0;

    /**
     * Creates a new terminal window with the specified title and dimensions.
     *
     * @param title Title of the window
     * @param windowWidth Initial width of the window
     * @param windowHeight Initial height of the window
     */
    public JWTerm(String title, int windowWidth, int windowHeight) {
        // Configure system properties
        configureSystemProperties();
        
        // Log system information
        logSystemInformation();

        // Initialize UI components
        frame = createFrame(title);
        canvas = createCanvas(windowWidth, windowHeight);
        frame.add(canvas);
        frame.pack();

        // Initialize rendering
        bufferStrategy = createBufferStrategy();
        debugFont = loadFont(DEFAULT_FONT_PATH, DEBUG_FONT_SIZE);

        // Initialize terminal
        termScreen = createTerminalEmulator();

        // Initialize resize manager
        resizeManager = new ResizeManager(canvas, termScreen);
        resizeManager.addListener(this);

        // Display the window
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    //--------------------------------------------------------------------------
    // Initialization Methods
    //--------------------------------------------------------------------------
    
    /**
     * Configures system properties for rendering optimization.
     */
    private void configureSystemProperties() {
        // Force hardware acceleration (if available)
        System.setProperty("sun.java2d.opengl", "true");
    }
    
    /**
     * Logs system information for debugging purposes.
     */
    private void logSystemInformation() {
        calculateAndLogDpiScaling();
    }

    /**
     * Calculates and logs DPI scaling information for the current display.
     *
     * @return The calculated DPI scaling factor
     */
    private float calculateAndLogDpiScaling() {
        // Get the screen DPI (dots per inch)
        int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        // Standard DPI is typically 96 for most screens, so calculate the scaling factor
        float scalingFactor = screenDpi / 96f;
        LOGGER.info("Screen DPI: " + screenDpi);
        LOGGER.info("DPI Scaling Factor: " + scalingFactor);

        return scalingFactor;
    }

    /**
     * Creates and configures the application window.
     *
     * @param title Title of the window
     * @return The configured JFrame
     */
    private JFrame createFrame(String title) {
        JFrame newFrame = new JFrame(title);
        newFrame.setIgnoreRepaint(true);
        newFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Handle window closing
        newFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                running = false;
            }
        });

        return newFrame;
    }

    /**
     * Creates and configures the canvas for rendering.
     *
     * @param width Canvas width
     * @param height Canvas height
     * @return The configured Canvas
     */
    private Canvas createCanvas(int width, int height) {
        Canvas newCanvas = new Canvas();
        newCanvas.setIgnoreRepaint(true);
        newCanvas.setPreferredSize(new Dimension(width, height));

        // Add key listeners
        newCanvas.addKeyListener(this);
        newCanvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F1) {
                    showDebug = !showDebug;
                }
            }
        });

        return newCanvas;
    }

    /**
     * Creates a buffer strategy for double-buffered rendering.
     *
     * @return The created BufferStrategy
     */
    private BufferStrategy createBufferStrategy() {
        canvas.createBufferStrategy(BUFFER_STRATEGY_COUNT);
        return canvas.getBufferStrategy();
    }

    /**
     * Creates and initializes the terminal emulator.
     *
     * @return The configured TermScreen
     */
    private TermScreen createTerminalEmulator() {
        return new TermScreen()
                .setInnerPadding(DEFAULT_TERMINAL_PADDING)
                .setFont(DEFAULT_FONT_PATH, DEFAULT_TERMINAL_FONT_SIZE)
                .setScreenSize(canvas.getWidth(), canvas.getHeight())
                .build();
    }

    /**
     * Loads a font from a file.
     *
     * @param path Path to the font file
     * @param size Font size
     * @return The loaded Font
     * @throws RuntimeException if font loading fails
     */
    private Font loadFont(String path, float size) {
        File fontFile = new File(path);
        try {
            if (!fontFile.exists()) {
                LOGGER.warning("Font file not found: " + path);
                return new Font(Font.MONOSPACED, Font.PLAIN, (int)size);
            }
            return Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(Font.PLAIN, size);
        } catch (FontFormatException | IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load font: " + path + ". Using fallback font.", e);
            return new Font(Font.MONOSPACED, Font.PLAIN, (int)size);
        }
    }

    //--------------------------------------------------------------------------
    // Resize Management Methods
    //--------------------------------------------------------------------------

    /**
     * Adds a resize listener that will be notified when the window is resized.
     * The listener's onResize method will be called after the terminal has been resized.
     *
     * @param listener The listener to add
     * @return This JWTerm instance for chaining
     */
    public JWTerm addResizeListener(ResizeListener listener) {
        resizeManager.addListener(listener);
        return this;
    }

    /**
     * Removes a previously added resize listener.
     *
     * @param listener The listener to remove
     * @return true if the listener was found and removed, false otherwise
     */
    public boolean removeResizeListener(ResizeListener listener) {
        return resizeManager.removeListener(listener);
    }

    /**
     * Forces an immediate resize event, useful when you want to trigger
     * resize handling programmatically.
     */
    public void forceResize() {
        resizeManager.forceResize();
    }

    /**
     * Called when the window is resized.
     * This method is part of the ResizeListener interface.
     * The JWTerm base class handles basic resize operations, but
     * subclasses should override this to handle application-specific resize logic.
     *
     * @param width The new width in pixels
     * @param height The new height in pixels
     */
    @Override
    public void onResize(int width, int height) {
        LOGGER.fine("Base onResize called: " + width + "x" + height);
        // Subclasses should implement `resize` method to handle application-specific resize logic
        resize(width, height);
    }

    //--------------------------------------------------------------------------
    // Game Loop and Rendering Methods
    //--------------------------------------------------------------------------

    /**
     * Runs the main game loop with fixed timing.
     * This will continuously update and render until running is set to false.
     */
    public void runGameLoop() {
        long lastUpdateTime = System.nanoTime();
        int frameCount = 0;
        long fpsTimer = System.nanoTime();

        while (running) {
            // Calculate delta time
            long currentTime = System.nanoTime();
            double deltaTimeSeconds = (currentTime - lastUpdateTime) / 1_000_000_000.0;
            double deltaTimeMs = (currentTime - lastUpdateTime) / 1_000_000.0;
            lastUpdateTime = currentTime;

            // Update game state
            updateTimer.start();
            update(deltaTimeSeconds);
            updateTimer.stop();

            // Render frame
            renderTimer.start();
            renderFrame(deltaTimeSeconds, deltaTimeMs, frameCount);
            renderTimer.stop();

            // Sleep to maintain target framerate
            long elapsedTime = System.nanoTime() - currentTime;
            sleepIfNeeded(elapsedTime);

            // Update FPS counter
            frameCount++;
            if (System.nanoTime() - fpsTimer >= FPS_UPDATE_INTERVAL_NS) {
                fps = frameCount;
                frameCount = 0;
                fpsTimer = System.nanoTime();
            }

            // Allow other threads to run
            Thread.yield();
        }

        shutdown();
    }

    /**
     * Renders a single frame.
     *
     * @param deltaTimeSeconds Time since last frame in seconds
     * @param deltaTimeMs Time since last frame in milliseconds
     * @param frameCount Current frame count
     */
    private void renderFrame(double deltaTimeSeconds, double deltaTimeMs, int frameCount) {
        Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
        try {
            // Clear the screen
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // Render terminal and game content
            termScreen.render(g);
            render(g, deltaTimeSeconds);

            // Render debug overlay if enabled
            if (showDebug) {
                renderDebugOverlay(g, deltaTimeMs, frameCount);
            }
        } finally {
            g.dispose();
        }
        bufferStrategy.show();
    }
    
    /**
     * Renders the debug information overlay.
     * 
     * @param g Graphics context
     * @param deltaTimeMs Time since last frame in milliseconds
     * @param frameCount Current frame count
     */
    private void renderDebugOverlay(Graphics2D g, double deltaTimeMs, int frameCount) {
        g.setFont(debugFont);
        String debugText = String.format(
                "[F1 - hide] fps: %3d | frame: %04d (%.4fms) | update: %.4fms | render: %.4fms",
                fps, frameCount, deltaTimeMs, updateTimer.getElapsedTimeMs(), renderTimer.getElapsedTimeMs()
        );
        renderDebugText(g, debugText);
    }

    /**
     * Renders debug text overlay.
     *
     * @param g Graphics context
     * @param debugText Debug text to render
     */
    private void renderDebugText(Graphics2D g, String debugText) {
        FontMetrics metrics = g.getFontMetrics(debugFont);
        int debugTextWidth = metrics.stringWidth(debugText);
        int debugTextHeight = metrics.getHeight();
        int debugTextX = 10;
        int debugTextY = 10;
        int debugTextPadding = 5;

        // Draw background
        g.setColor(new Color(0.1f, 0, 0, 0.95f));
        g.fillRect(debugTextX, debugTextY,
                debugTextWidth + debugTextPadding * 2,
                debugTextHeight + debugTextPadding * 2);

        // Draw text
        g.setColor(Color.YELLOW);
        g.drawString(debugText,
                debugTextX + debugTextPadding,
                debugTextY + debugTextPadding + metrics.getAscent());
    }

    /**
     * Sleeps the current thread to maintain target framerate.
     *
     * @param elapsedTime Elapsed time for the current frame in nanoseconds
     */
    private void sleepIfNeeded(long elapsedTime) {
        long sleepTime = (OPTIMAL_TIME_NS - elapsedTime) / 1_000_000; // convert to milliseconds
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Game loop sleep interrupted", ex);
            }
        }
    }

    //--------------------------------------------------------------------------
    // Utility Methods
    //--------------------------------------------------------------------------
    
    /**
     * Checks if the Control key is pressed in the given key event modifiers.
     * 
     * @param modifiers The key event modifiers
     * @return True if Control is pressed, false otherwise
     */
    protected boolean isCtrlDown(int modifiers) {
        return (modifiers & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK;
    }

    /**
     * Checks if the Alt key is pressed in the given key event modifiers.
     * 
     * @param modifiers The key event modifiers
     * @return True if Alt is pressed, false otherwise
     */
    protected boolean isAltDown(int modifiers) {
        return (modifiers & KeyEvent.ALT_DOWN_MASK) == KeyEvent.ALT_DOWN_MASK;
    }

    /**
     * Checks if the Shift key is pressed in the given key event modifiers.
     * 
     * @param modifiers The key event modifiers
     * @return True if Shift is pressed, false otherwise
     */
    protected boolean isShiftDown(int modifiers) {
        return (modifiers & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK;
    }

    /**
     * Cleans up resources and exits the application.
     */
    private void shutdown() {
        LOGGER.log(Level.INFO, "Shutting down application");
        if (resizeManager != null) {
            resizeManager.shutdown();
        }
        frame.dispose();
        System.exit(0);
    }

    //--------------------------------------------------------------------------
    // Abstract Methods - Must be implemented by subclasses
    //--------------------------------------------------------------------------

    /**
     * Abstract method that subclasses must implement to handle application-specific resize operations.
     * This is called after the base class has handled the resize event.
     *
     * @param width The new width in pixels
     * @param height The new height in pixels
     */
    protected abstract void resize(int width, int height);

    /**
     * Updates the game state.
     * Must be implemented by subclasses.
     *
     * @param deltaTime Time since last update in seconds
     */
    protected abstract void update(double deltaTime);

    /**
     * Renders game content.
     * Must be implemented by subclasses.
     *
     * @param g Graphics context
     * @param deltaTime Time since last update in seconds
     */
    protected abstract void render(Graphics2D g, double deltaTime);
}
