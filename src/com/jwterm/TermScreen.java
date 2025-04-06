package com.jwterm;

import com.jwterm.buffer.TerminalBuffer;
import com.jwterm.font.FontManager;
import com.jwterm.geometry.Dimension;
import com.jwterm.geometry.Padding;
import com.jwterm.geometry.Size;
import com.jwterm.glyph.Glyph;
import com.jwterm.utils.LoggingUtility;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        font = FontManager.getFont(fontPath, size);
        calculateCellSize();
        return this;
    }

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
        
        calculateScreenDimensions();
        calculateScreenPadding();
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
        if (buffer != null) {
            buffer.fill(glyph);
        }
        return this;
    }

    /**
     * Draws an outline around the screen with the specified glyph.
     * @param glyph the glyph to use for the outline
     * @return this TermScreen instance for chaining
     */
    public TermScreen outline(Glyph glyph) {
        if (buffer != null) {
            buffer.outline(glyph);
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

    /**
     * Renders the terminal screen using the provided graphics context.
     * @param g the graphics context to render to
     */
    public void render(Graphics2D g) {
        if (font == null || buffer == null) {
            LOGGER.log(Level.WARNING, "Cannot render: font or buffer not initialized");
            return;
        }

        g.setFont(font);
        buffer.withReadLock(() -> renderBuffer(g));
    }

    /**
     * Renders the entire buffer using the provided graphics context.
     * @param g the graphics context to render to
     */
    private void renderBuffer(Graphics2D g) {
        Dimension bufferDim = buffer.getDimension();
        for (int row = 0; row < bufferDim.getRows(); row++) {
            for (int col = 0; col < bufferDim.getCols(); col++) {
                Glyph glyph = buffer.getGlyph(row, col);
                if (glyph == null) continue;

                renderGlyph(g, glyph, row, col);
            }
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
        String charString = String.valueOf(glyph.getCharacter());

        FontMetrics metrics = g.getFontMetrics(font);
        int charWidth = metrics.stringWidth(charString);
        int charHeight = metrics.getHeight();

        // Calculate the position to center the character in its cell
        float charX = calculateGlyphX(col, charWidth);
        float charY = calculateGlyphY(row, charHeight, metrics);

        // Draw background if needed
        if (glyph.getBackground() != null && !glyph.getBackground().equals(Color.BLACK)) {
            g.setColor(glyph.getBackground());
            g.fillRect(
                screenPadding.getHorizontal() + screenInnerPadding.getHorizontal() + col * cellSize.getWidth(),
                screenPadding.getVertical() + screenInnerPadding.getVertical() + row * cellSize.getHeight(),
                cellSize.getWidth(),
                cellSize.getHeight()
            );
        }

        g.setColor(glyph.getForeground());
        g.drawString(charString, charX, charY);
    }

    /**
     * Calculates the X coordinate for a glyph.
     */
    private float calculateGlyphX(int col, int charWidth) {
        return screenPadding.getHorizontal() +
                screenInnerPadding.getHorizontal() +
                col * cellSize.getWidth() +
                cellSize.getWidth() / 2f -
                charWidth / 2f;
    }

    /**
     * Calculates the Y coordinate for a glyph.
     */
    private float calculateGlyphY(int row, int charHeight, FontMetrics metrics) {
        return screenPadding.getVertical() +
                screenInnerPadding.getVertical() +
                row * cellSize.getHeight() +
                (cellSize.getHeight() - metrics.getDescent()) / 2f +
                metrics.getAscent() / 2f;
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

    /**
     * Resizes the terminal screen to the specified dimensions.
     * This is a convenience method that performs all necessary resize operations in a single call.
     * 
     * @param width the new width in pixels
     * @param height the new height in pixels
     * @return this TermScreen instance for chaining
     */
    public TermScreen resize(int width, int height) {
        setScreenSize(width, height);
        recalculateScreenDimensions();
        return this;
    }
}
