package com.jwterm.render;

import com.jwterm.buffer.TerminalBuffer;
import com.jwterm.geometry.Dimension;
import com.jwterm.geometry.Padding;
import com.jwterm.geometry.Size;
import com.jwterm.glyph.Glyph;

import java.awt.*;

/**
 * Responsible for rendering terminal content to a graphics context.
 * This class separates rendering logic from terminal state management.
 */
public class TerminalRenderer {

    /**
     * Renders the entire terminal buffer using the provided graphics context.
     * 
     * @param g the graphics context to render to
     * @param buffer the terminal buffer to render
     * @param font the font to use for rendering
     * @param screenPadding the outer padding of the terminal
     * @param screenInnerPadding the inner padding of the terminal
     * @param cellSize the size of each character cell
     */
    public void renderBuffer(Graphics2D g, TerminalBuffer buffer, Font font,
                             Padding screenPadding, Padding screenInnerPadding, Size cellSize) {
        Dimension bufferDim = buffer.getDimension();
        for (int row = 0; row < bufferDim.getRows(); row++) {
            for (int col = 0; col < bufferDim.getCols(); col++) {
                Glyph glyph = buffer.getGlyph(row, col);
                if (glyph == null) continue;

                renderGlyph(g, glyph, row, col, font, screenPadding, screenInnerPadding, cellSize);
            }
        }
    }

    /**
     * Renders a single glyph at the specified position.
     * 
     * @param g the graphics context
     * @param glyph the glyph to render
     * @param row the row index
     * @param col the column index
     * @param font the font to use
     * @param screenPadding the outer padding
     * @param screenInnerPadding the inner padding
     * @param cellSize the size of each character cell
     */
    public void renderGlyph(Graphics2D g, Glyph glyph, int row, int col, Font font,
                           Padding screenPadding, Padding screenInnerPadding, Size cellSize) {
        String charString = String.valueOf(glyph.getCharacter());

        FontMetrics metrics = g.getFontMetrics(font);
        int charWidth = metrics.stringWidth(charString);
        int charHeight = metrics.getHeight();

        // Calculate the position to center the character in its cell
        float charX = calculateGlyphX(col, charWidth, screenPadding, screenInnerPadding, cellSize);
        float charY = calculateGlyphY(row, charHeight, metrics, screenPadding, screenInnerPadding, cellSize);

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
    private float calculateGlyphX(int col, int charWidth, 
                                 Padding screenPadding, Padding screenInnerPadding, Size cellSize) {
        return screenPadding.getHorizontal() +
                screenInnerPadding.getHorizontal() +
                col * cellSize.getWidth() +
                cellSize.getWidth() / 2f -
                charWidth / 2f;
    }

    /**
     * Calculates the Y coordinate for a glyph.
     */
    private float calculateGlyphY(int row, int charHeight, FontMetrics metrics,
                                 Padding screenPadding, Padding screenInnerPadding, Size cellSize) {
        return screenPadding.getVertical() +
                screenInnerPadding.getVertical() +
                row * cellSize.getHeight() +
                (cellSize.getHeight() + metrics.getAscent() - metrics.getDescent()) / 2f;
    }
}

