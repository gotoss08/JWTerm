package com.jwterm;

import com.jwterm.buffer.TerminalBuffer;
import com.jwterm.font.FontManager;
import com.jwterm.geometry.Dimension;
import com.jwterm.geometry.Padding;
import com.jwterm.geometry.Size;
import com.jwterm.glyph.Glyph;
import com.jwterm.render.TerminalRenderer;
import com.jwterm.utils.LoggingUtility;
import com.jwterm.utils.Timer;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the terminal screen rendering, dimensions, and buffer operations.
 * Uses a builder pattern for configuration.
 */
public class TermScreen {

    private static final Logger LOGGER = LoggingUtility.getLogger(TermScreen.class.getName());

    // Font
    private Font font;

    // Screen dimensions
    private final Size screenSize = new Size();
    private final Dimension dimension = new Dimension();
    private final Size cellSize = new Size();
    private final Padding screenInnerPadding = new Padding();
    private final Padding screenPadding = new Padding();

    // Screen content
    private TerminalBuffer buffer;
    
    // Renderer
    private final TerminalRenderer renderer;
    
    // Performance tracking
    private final Timer operationTimer = new Timer();
    private boolean enablePerformanceTracking = false;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /**
     * Creates a new TermScreen with default settings.
     * Use the builder pattern methods to configure and then call build().
     */
    public TermScreen() {
        this.renderer = new TerminalRenderer();
    }
    
    /**
     * Creates a new TermScreen with the specified configuration.
     * 
     * @param fontPath path to the font file
     * @param fontSize font size
     * @param innerPadding inner padding in pixels
     * @param width screen width in pixels
     * @param height screen height in pixels
     */
    public TermScreen(String fontPath, float fontSize, int innerPadding, int width, int height) {
        this();
        setFont(fontPath, fontSize);
        setInnerPadding(innerPadding);
        setScreenSize(width, height);
        build();
    }

    //--------------------------------------------------------------------------
    // Configuration Methods
    //--------------------------------------------------------------------------

    /**
     * Completes configuration by performing necessary calculations and initializations.
     * @return this TermScreen instance
     */
    public TermScreen build() {
        calculateScreenDimensions();
        calculateScreenPadding();
        initializeBuffer();
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
        startTimingOperation("setFont");
        font = FontManager.getFont(fontPath, size);
        calculateCellSize();
        endTimingOperation("setFont");
        return this;
    }

    /**
     * Sets the size of the terminal screen in pixels.
     * @param screenWidth width in pixels
     * @param screenHeight height in pixels
     * @return this TermScreen instance for chaining
     */
    public TermScreen setScreenSize(int screenWidth, int screenHeight) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            LOGGER.warning("Invalid screen dimensions: " + screenWidth + "x" + screenHeight);
            return this;
        }
        
        screenSize.set(screenWidth, screenHeight);
        logTerminalScreenSize();
        return this;
    }
    
    /**
     * Enables or disables performance tracking for operations.
     * 
     * @param enabled true to enable tracking, false to disable
     * @return this TermScreen instance for chaining
     */
    public TermScreen setPerformanceTracking(boolean enabled) {
        this.enablePerformanceTracking = enabled;
        return this;
    }

    //--------------------------------------------------------------------------
    // Dimension Calculation Methods
    //--------------------------------------------------------------------------

    /**
     * Calculates the cell size based on font metrics.
     */
    private void calculateCellSize() {
        if (font == null) {
            LOGGER.warning("Cannot calculate cell size: font not set");
            return;
        }

        JComponent dummyComponent = new JComponent() {};
        FontMetrics metrics = dummyComponent.getFontMetrics(this.font);

        // Calculate cell dimensions
        this.cellSize.setWidth(metrics.stringWidth("M"));
        this.cellSize.setHeight(metrics.getMaxAscent());

        LOGGER.log(Level.FINE, "Terminal cell width: " + this.cellSize.getWidth());
        LOGGER.log(Level.FINE, "Terminal cell height: " + this.cellSize.getHeight());
    }

    /**
     * Recalculates all dimensions based on current settings.
     * This should be called after changing screen size or font.
     * 
     * @return this TermScreen instance for chaining
     */
    public TermScreen recalculateScreenDimensions() {
        if (font == null) {
            LOGGER.warning("Cannot recalculate dimensions: font not set");
            return this;
        }

        startTimingOperation("recalculateScreenDimensions");        
        calculateScreenDimensions();
        calculateScreenPadding();
        endTimingOperation("recalculateScreenDimensions");
        return this;
    }

    private void logTerminalScreenSize() {
        LOGGER.log(Level.FINE, "Terminal screen width: " + screenSize.getWidth());
        LOGGER.log(Level.FINE, "Terminal screen height: " + screenSize.getHeight());
    }

    private void calculateScreenDimensions() {
        if (cellSize.getWidth() <= 0 || cellSize.getHeight() <= 0) {
            LOGGER.warning("Cannot calculate screen dimensions: invalid cell size");
            return;
        }

        int cols = (screenSize.getWidth() - screenInnerPadding.getHorizontal() * 2) / cellSize.getWidth();
        int rows = (screenSize.getHeight() - screenInnerPadding.getVertical() * 2) / cellSize.getHeight();
        dimension.set(cols, rows);

        LOGGER.log(Level.FINE, "Terminal cols: " + cols);
        LOGGER.log(Level.FINE, "Terminal rows: " + rows);

        if (buffer != null) {
            buffer.resize(rows, cols);
        }
    }

    private void calculateScreenPadding() {
        int totalCellWidth = dimension.getCols() * cellSize.getWidth();
        int totalCellHeight = dimension.getRows() * cellSize.getHeight();

        int horizontalPadding = (screenSize.getWidth() - totalCellWidth - screenInnerPadding.getHorizontal() * 2) / 2;
        int verticalPadding = (screenSize.getHeight() - totalCellHeight - screenInnerPadding.getVertical() * 2) / 2;

        screenPadding.setHorizontal(horizontalPadding);
        screenPadding.setVertical(verticalPadding);
    }

    //--------------------------------------------------------------------------
    // Buffer Management Methods
    //--------------------------------------------------------------------------

    private void initializeBuffer() {
        if (buffer == null) {
            buffer = new TerminalBuffer(dimension.getRows(), dimension.getCols());
        } else {
            buffer.resize(dimension.getRows(), dimension.getCols());
        }
    }

    /**
     * Fills the entire screen with the specified glyph.
     * @param glyph the glyph to fill with
     * @return this TermScreen instance for chaining
     */
    public TermScreen fill(Glyph glyph) {
        startTimingOperation("fill");
        if (buffer != null) {
            buffer.fill(glyph);
        }
        endTimingOperation("fill");
        return this;
    }

    /**
     * Draws an outline around the screen with the specified glyph.
     * @param glyph the glyph to use for the outline
     * @return this TermScreen instance for chaining
     */
    public TermScreen outline(Glyph glyph) {
        startTimingOperation("outline");
        if (buffer != null) {
            buffer.outline(glyph);
        }
        endTimingOperation("outline");
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
        if (buffer != null) {
            buffer.setGlyph(row, col, glyph);
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
        if (buffer != null) {
            return buffer.getGlyph(row, col);
        }
        return null;
    }

    /**
     * Executes a read operation with the buffer lock held.
     * @param operation The operation to execute
     */
    public void withReadLock(Runnable operation) {
        if (buffer != null) {
            buffer.withReadLock(operation);
        }
    }

    /**
     * Executes a write operation with the buffer lock held.
     * @param operation The operation to execute
     */
    public void withWriteLock(Runnable operation) {
        if (buffer != null) {
            buffer.withWriteLock(operation);
        }
    }

    //--------------------------------------------------------------------------
    // Rendering Methods
    //--------------------------------------------------------------------------

    /**
     * Renders the terminal screen using the provided graphics context.
     * @param g the graphics context to render to
     */
    public void render(Graphics2D g) {
        if (font == null || buffer == null) {
            LOGGER.log(Level.WARNING, "Cannot render: font or buffer not initialized");
            return;
        }

        startTimingOperation("render");
        g.setFont(font);
        buffer.withReadLock(() -> {
            renderer.renderBuffer(g, buffer, font, 
                    screenPadding, screenInnerPadding, cellSize);
        });
        endTimingOperation("render");
    }

    //--------------------------------------------------------------------------
    // Performance Monitoring Methods
    //--------------------------------------------------------------------------
    
    private void startTimingOperation(String operationName) {
        if (enablePerformanceTracking) {
            operationTimer.start();
        }
    }
    
    private void endTimingOperation(String operationName) {
        if (enablePerformanceTracking) {
            operationTimer.stop();
            LOGGER.fine(operationName + " took " + operationTimer.getElapsedTimeMs() + "ms");
        }
    }
    
    /**
     * Returns performance statistics for the last timed operation.
     * Only available if performance tracking is enabled.
     * 
     * @return a string containing performance information, or null if tracking is disabled
     */
    public String getLastOperationStats() {
        if (!enablePerformanceTracking) {
            return null;
        }
        return String.format("Last operation: %.4f ms", operationTimer.getElapsedTimeMs());
    }
    
    /**
     * Resets all performance counters.
     */
    public void resetPerformanceCounters() {
        operationTimer.stop(); // Ensure timer is stopped
    }

    //--------------------------------------------------------------------------
    // Batch Operations
    //--------------------------------------------------------------------------

    /**
     * Sets multiple glyphs in a batch operation for better performance.
     * This is more efficient than setting glyphs individually when updating many cells.
     * 
     * @param positions array of row/col pairs where even indices are rows and odd indices are columns
     * @param glyphs array of glyphs to set at the specified positions
     * @return this TermScreen instance for chaining
     * @throws IllegalArgumentException if the positions array length is not even or doesn't match glyphs length*2
     */
    public TermScreen setGlyphsBatch(int[] positions, Glyph[] glyphs) {
        if (positions.length % 2 != 0) {
            throw new IllegalArgumentException("Positions array must have an even length");
        }
        
        if (positions.length / 2 != glyphs.length) {
            throw new IllegalArgumentException("Positions and glyphs arrays must match in length");
        }
        
        startTimingOperation("setGlyphsBatch");
        if (buffer != null) {
            buffer.withWriteLock(() -> {
                for (int i = 0; i < glyphs.length; i++) {
                    int row = positions[i * 2];
                    int col = positions[i * 2 + 1];
                    buffer.setGlyph(row, col, glyphs[i]);
                }
            });
        }
        endTimingOperation("setGlyphsBatch");
        return this;
    }
    
    /**
     * Fills a rectangular area with the specified glyph.
     * 
     * @param startRow top row of the rectangle
     * @param startCol left column of the rectangle
     * @param endRow bottom row of the rectangle (inclusive)
     * @param endCol right column of the rectangle (inclusive)
     * @param glyph the glyph to fill with
     * @return this TermScreen instance for chaining
     */
    public TermScreen fillRect(int startRow, int startCol, int endRow, int endCol, Glyph glyph) {
        startTimingOperation("fillRect");
        if (buffer != null) {
            buffer.withWriteLock(() -> {
                for (int row = startRow; row <= endRow; row++) {
                    for (int col = startCol; col <= endCol; col++) {
                        buffer.setGlyph(row, col, glyph);
                    }
                }
            });
        }
        endTimingOperation("fillRect");
        return this;
    }
    
    /**
     * Draws a rectangle outline with the specified glyph.
     * 
     * @param startRow top row of the rectangle
     * @param startCol left column of the rectangle
     * @param endRow bottom row of the rectangle (inclusive)
     * @param endCol right column of the rectangle (inclusive)
     * @param glyph the glyph to draw with
     * @return this TermScreen instance for chaining
     */
    public TermScreen drawRect(int startRow, int startCol, int endRow, int endCol, Glyph glyph) {
        startTimingOperation("drawRect");
        if (buffer != null) {
            buffer.withWriteLock(() -> {
                // Draw horizontal lines
                for (int col = startCol; col <= endCol; col++) {
                    buffer.setGlyph(startRow, col, glyph); // Top line
                    buffer.setGlyph(endRow, col, glyph);   // Bottom line
                }
                
                // Draw vertical lines (skip corners to avoid duplicating them)
                for (int row = startRow + 1; row < endRow; row++) {
                    buffer.setGlyph(row, startCol, glyph); // Left line
                    buffer.setGlyph(row, endCol, glyph);   // Right line
                }
            });
        }
        endTimingOperation("drawRect");
        return this;
    }
    
    /**
     * Writes a string horizontally starting at the specified position.
     * 
     * @param row starting row
     * @param col starting column
     * @param text the text to write
     * @param foreground foreground color for the text
     * @param background background color for the text (can be null for transparent)
     * @return this TermScreen instance for chaining
     */
    public TermScreen writeString(int row, int col, String text, Color foreground, Color background) {
        startTimingOperation("writeString");
        if (buffer != null && text != null) {
            buffer.withWriteLock(() -> {
                for (int i = 0; i < text.length(); i++) {
                    if (col + i >= dimension.getCols()) {
                        break; // Don't write past the right edge
                    }
                    buffer.setGlyph(row, col + i, new Glyph(text.charAt(i), foreground, background));
                }
            });
        }
        endTimingOperation("writeString");
        return this;
    }

    //--------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------

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
    
    /**
     * Gets the current font.
     * @return the current font
     */
    public Font getFont() {
        return font;
    }
    
    /**
     * Gets the cell size.
     * @return the cell size
     */
    public Size getCellSize() {
        return cellSize;
    }
    
    /**
     * Gets the screen padding.
     * @return the screen padding
     */
    public Padding getScreenPadding() {
        return screenPadding;
    }

    /**
     * Resizes the terminal screen to the specified dimensions.
     * This is a convenience method that performs all necessary resize operations in a single call.
     * 
     * @param width the new width in pixels
     * @param height the new height in pixels
     * @return this TermScreen instance for chaining
     */
    public TermScreen resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            LOGGER.warning("Invalid resize dimensions: " + width + "x" + height);
            return this;
        }
        
        startTimingOperation("resize");
        setScreenSize(width, height);
        recalculateScreenDimensions();
        endTimingOperation("resize");
        return this;
    }
}
