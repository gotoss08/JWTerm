package com.jwterm;

import com.jwterm.utils.LoggingUtility;
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
public abstract class JWTerm implements KeyListener {

    private static final Logger LOGGER = LoggingUtility.getLogger(JWTerm.class.getName());
    private static final int BUFFER_STRATEGY_COUNT = 2;
    private static final int TARGET_FPS = 60;
    private static final long OPTIMAL_TIME_NS = 1_000_000_000 / TARGET_FPS; // nanoseconds per frame
    private static final long FPS_UPDATE_INTERVAL_NS = 1_000_000_000; // 1 second in nanoseconds

    // UI Components
    private final JFrame frame;
    private final Canvas canvas;
    private final BufferStrategy bufferStrategy;

    // Terminal emulation
    protected final TermScreen termScreen;

    // State and configuration
    protected volatile boolean running = true;
    private boolean showDebug = BuildConfig.showDebug;
    private Font debugFont;

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
        configureHardwareAcceleration();
        calculateAndLogDpiScaling();

        // Initialize UI components
        frame = createFrame(title);
        canvas = createCanvas(windowWidth, windowHeight);
        frame.add(canvas);
        frame.pack();

        // Initialize rendering
        bufferStrategy = createBufferStrategy();
        debugFont = loadFont("FSEX302.ttf", 16.0f);

        // Initialize terminal
        termScreen = createTerminalEmulator();

        // Display the window
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Enables hardware acceleration for rendering if available.
     */
    private void configureHardwareAcceleration() {
        // Force hardware acceleration (if available)
        // This tells the Java2D pipeline to use OpenGL if supported, potentially reducing rendering overhead
        System.setProperty("sun.java2d.opengl", "true");
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

        // Handle window resizing
        newFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                handleResize();
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
            return Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(Font.PLAIN, size);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException("Failed to load font: " + path, e);
        }
    }

    /**
     * Creates and initializes the terminal emulator.
     *
     * @return The configured TermScreen
     */
    private TermScreen createTerminalEmulator() {
        return new TermScreen()
                .setInnerPadding(10)
                .setFont("FSEX302.ttf", 25f)
                .setScreenSize(canvas.getWidth(), canvas.getHeight())
                .build()
                .fill(TermScreen.Glyph.SPACE)
                .outline(TermScreen.Glyph.WALL);
    }

    /**
     * Handles window resize events.
     */
    private void handleResize() {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        if (termScreen != null) {
            termScreen.lock.writeLock().lock();
            try {
                termScreen.setScreenSize(width, height)
                        .recalculateScreenDimensions()
                        .fill(TermScreen.Glyph.SPACE)
                        .outline(TermScreen.Glyph.WALL);
            } finally {
                termScreen.lock.writeLock().unlock();
            }
        }

        LOGGER.fine("Resized to: " + width + "x" + height);
    }

    /**
     * Runs the main game loop with fixed timing.
     * This will continuously update and render until running is set to false.
     */
    public void runGameLoop() {
        long lastUpdateTime = System.nanoTime();
        int frameCount = 0;
        long fpsTimer = System.nanoTime();

        while (running) {

//            canvas.requestFocusInWindow();

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
                g.setFont(debugFont);
                String debugText = String.format(
                        "[F1 - hide] fps: %3d | frame: %04d (%.4fms) | update: %.4fms | render: %.4fms",
                        fps, frameCount, deltaTimeMs, updateTimer.getElapsedTimeMs(), renderTimer.getElapsedTimeMs()
                );
                renderDebugText(g, debugText);
            }
        } finally {
            g.dispose();
        }
        bufferStrategy.show();
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
     * Cleans up resources and exits the application.
     */
    private void shutdown() {
        LOGGER.log(Level.INFO, "Shutting down application");
        frame.dispose();
        System.exit(0);
    }

    protected boolean isCtrlDown(int modifiers) {
        return (modifiers & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK;
    }

    protected boolean isAltDown(int modifiers) {
        return (modifiers & KeyEvent.ALT_DOWN_MASK) == KeyEvent.ALT_DOWN_MASK;
    }

    protected boolean isShiftDown(int modifiers) {
        return (modifiers & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK;
    }

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