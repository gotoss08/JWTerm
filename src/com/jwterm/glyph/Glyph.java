package com.jwterm.glyph;

import java.awt.Color;

/**
 * Represents a character cell in the terminal screen
 */
public class Glyph {
    private final char character;
    private final Color foreground;
    private final Color background;

    public Glyph(char character, Color foreground, Color background) {
        this.character = character;
        this.foreground = foreground;
        this.background = background;
    }

    public char getCharacter() {
        return character;
    }

    public Color getForeground() {
        return foreground;
    }

    public Color getBackground() {
        return background;
    }

    // Pre-defined glyphs
    public static final Glyph PLAYER = new Glyph('@', Color.WHITE, Color.BLACK);
    public static final Glyph WALL = new Glyph('#', Color.LIGHT_GRAY, Color.BLACK);
    public static final Glyph SPACE = new Glyph('.', Color.DARK_GRAY, Color.BLACK);
}
