package com.jwterm;

import com.jwterm.utils.Dimension;
import com.jwterm.utils.LoggingUtility;
import com.jwterm.utils.Padding;
import com.jwterm.utils.Size;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TermScreen {

    private static final Logger LOGGER = LoggingUtility.getLogger(TermScreen.class.getName());

    /**
     * Represents a character cell in the terminal screen
     */
    public static class Glyph {
        private final char character;
        private final Color foreground;
        private final Color background;

        public Glyph(char character, Color foreground, Color background) {
            this.character = character;
            this.foreground = foreground;
            this.background = background;
        }

        // Pre-defined glyphs
        public static final Glyph PLAYER = new Glyph('@', Color.WHITE, Color.BLACK);
        public static final Glyph WALL = new Glyph('#', Color.LIGHT_GRAY, Color.BLACK);
        public static final Glyph SPACE = new Glyph('.', Color.DARK_GRAY, Color.BLACK);
    }

    // Font management
    private Font font;
    private final Map<String, Font> fontCache = new HashMap<>();

    // Screen dimensions
    private final Size screenSize = new Size();
    private final Dimension dimension = new Dimension();
    private final Size cellSize = new Size();
    private final Padding screenInnerPadding = new Padding();
    private final Padding screenPadding = new Padding();

    // Screen content
    private Glyph[][] glyphs;
    public final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates a new TermScreen with default settings.
     * Use the builder pattern methods to configure and then call build().
     */
    public TermScreen() {
        // Default constructor for builder pattern
    }

    /**
     * Completes configuration by performing necessary calculations and initializations.
     * @return this TermScreen instance
     */
    public TermScreen build() {
        calculateScreenDimensions();
        calculateScreenPadding();
        initializeGlyphs();
        return this;
    }

    /**
     * Sets the inner padding of the screen.
     * @param padding amount of padding in pixels
     * @return this TermScreen instance for chaining
     */
    public TermScreen setInnerPadding(int padding) {
        this.screenInnerPadding.set(padding);
        return this;
    }

    /**
     * Sets the font for the terminal.
     * @param fontPath path to the font file
     * @param size font size
     * @return this TermScreen instance for chaining
     * @throws RuntimeException if font loading fails
     */
    public TermScreen setFont(String fontPath, float size) {
        // Try to get from cache first
        String cacheKey = fontPath + "_" + size;
        if (fontCache.containsKey(cacheKey)) {
            font = fontCache.get(cacheKey);
        } else {
            // Load the font
            File fontFile = new File(fontPath);
            try {
                font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                font = font.deriveFont(Font.PLAIN, size);
                fontCache.put(cacheKey, font);
            } catch (FontFormatException | IOException e) {
                throw new RuntimeException("Failed to load font: " + fontPath, e);
            }
        }

        calculateCellSize();
        return this;
    }

    /**
     * Calculates the cell size based on font metrics.
     */
    private void calculateCellSize() {
        JComponent dummyComponent = new JComponent() {};
        FontMetrics metrics = dummyComponent.getFontMetrics(this.font);

        // Calculate cell dimensions
        this.cellSize.setWidth(metrics.stringWidth("M"));
        this.cellSize.setHeight(metrics.getMaxAscent());

        LOGGER.log(Level.FINE, "Terminal cell width: " + this.cellSize.getWidth());
        LOGGER.log(Level.FINE, "Terminal cell height: " + this.cellSize.getHeight());
    }

    /**
     * Sets the size of the terminal screen in pixels.
     * @param screenWidth width in pixels
     * @param screenHeight height in pixels
     * @return this TermScreen instance for chaining
     */
    public TermScreen setScreenSize(int screenWidth, int screenHeight) {
        screenSize.set(screenWidth, screenHeight);
        logTerminalScreenSize();
        return this;
    }

    /**
     * Recalculates all dimensions based on current settings.
     * @return this TermScreen instance for chaining
     */
    public TermScreen recalculateScreenDimensions() {
        calculateScreenDimensions();
        calculateScreenPadding();
        return this;
    }

    private void logTerminalScreenSize() {
        LOGGER.log(Level.FINE, "Terminal screen width: " + screenSize.getWidth());
        LOGGER.log(Level.FINE, "Terminal screen height: " + screenSize.getHeight());
    }

    private void calculateScreenDimensions() {
        int cols = (screenSize.getWidth() - screenInnerPadding.getHorizontal() * 2) / cellSize.getWidth();
        int rows = (screenSize.getHeight() - screenInnerPadding.getVertical() * 2) / cellSize.getHeight();
        dimension.set(cols, rows);

        LOGGER.log(Level.FINE, "Terminal cols: " + cols);
        LOGGER.log(Level.FINE, "Terminal rows: " + rows);

        initializeGlyphs();

    }

    private void calculateScreenPadding() {
        int totalCellWidth = dimension.getCols() * cellSize.getWidth();
        int totalCellHeight = dimension.getRows() * cellSize.getHeight();

        int horizontalPadding = (screenSize.getWidth() - totalCellWidth - screenInnerPadding.getHorizontal() * 2) / 2;
        int verticalPadding = (screenSize.getHeight() - totalCellHeight - screenInnerPadding.getVertical() * 2) / 2;

        screenPadding.setHorizontal(horizontalPadding);
        screenPadding.setVertical(verticalPadding);
    }

    private void initializeGlyphs() {
        glyphs = new Glyph[dimension.getRows()][dimension.getCols()];
    }

    /**
     * Fills the entire screen with the specified glyph.
     * @param glyph the glyph to fill with
     * @return this TermScreen instance for chaining
     */
    public TermScreen fill(Glyph glyph) {
        for (int row = 0; row < dimension.getRows(); row++) {
            for (int col = 0; col < dimension.getCols(); col++) {
                glyphs[row][col] = glyph;
            }
        }
        return this;
    }

    /**
     * Draws an outline around the screen with the specified glyph.
     * @param glyph the glyph to use for the outline
     * @return this TermScreen instance for chaining
     */
    public TermScreen outline(Glyph glyph) {
        for (int row = 0; row < dimension.getRows(); row++) {
            for (int col = 0; col < dimension.getCols(); col++) {
                if (row == 0 || row == dimension.getRows() - 1 || col == 0 || col == dimension.getCols() - 1)
                    glyphs[row][col] = glyph;
            }
        }
        return this;
    }

    /**
     * Sets a specific glyph at the given position.
     * @param row the row index
     * @param col the column index
     * @param glyph the glyph to set
     * @return this TermScreen instance for chaining
     */
    public TermScreen setGlyph(int row, int col, Glyph glyph) {
        if (row >= 0 && row < dimension.getRows() && col >= 0 && col < dimension.getCols()) {
            glyphs[row][col] = glyph;
        }
        return this;
    }

    /**
     * Gets the glyph at the specified position.
     * @param row the row index
     * @param col the column index
     * @return the glyph at the specified position, or null if out of bounds
     */
    public Glyph getGlyph(int row, int col) {
        if (row >= 0 && row < dimension.getRows() && col >= 0 && col < dimension.getCols()) {
            return glyphs[row][col];
        }
        return null;
    }

    /**
     * Renders the terminal screen using the provided graphics context.
     * @param g the graphics context to render to
     */
    public void render(Graphics2D g) {
        if (font == null || glyphs == null) {
            LOGGER.log(Level.WARNING, "Cannot render: font or glyphs not initialized");
            return;
        }

        g.setFont(font);
        lock.readLock().lock();
        try {
            for (int row = 0; row < dimension.getRows(); row++) {
                for (int col = 0; col < dimension.getCols(); col++) {
                    Glyph glyph = glyphs[row][col];
                    if (glyph == null) continue;

                    renderGlyph(g, glyph, row, col);
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Renders a single glyph at the specified position.
     * @param g the graphics context
     * @param glyph the glyph to render
     * @param row the row index
     * @param col the column index
     */
    private void renderGlyph(Graphics2D g, Glyph glyph, int row, int col) {
        String charString = String.valueOf(glyph.character);

        FontMetrics metrics = g.getFontMetrics(font);
        int charWidth = metrics.stringWidth(charString);
        int charHeight = metrics.getHeight();

        float charX = screenPadding.getHorizontal() +
                screenInnerPadding.getHorizontal() +
                col * cellSize.getWidth() +
                cellSize.getWidth() / 2f -
                charWidth / 2f;

        float charY = screenPadding.getVertical() +
                screenInnerPadding.getVertical() +
                cellSize.getHeight() +
                row * cellSize.getHeight() +
                cellSize.getHeight() / 2f -
                charHeight / 2f;

        // Optional: Draw background if needed
        // g.setColor(glyph.background);
        // g.fillRect(...);

        g.setColor(glyph.foreground);
        g.drawString(charString, charX, charY);
    }

    /**
     * Gets the screen's inner padding.
     * @return the screen's inner padding
     */
    public Padding getScreenInnerPadding() {
        return screenInnerPadding;
    }

    /**
     * Gets the dimensions of the terminal in columns and rows.
     * @return the dimensions of the terminal
     */
    public Dimension getDimension() {
        return dimension;
    }

    /**
     * Gets the size of the terminal screen in pixels.
     * @return the size of the terminal screen
     */
    public Size getScreenSize() {
        return screenSize;
    }
}