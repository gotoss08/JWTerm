package com.jwterm.font;

import com.jwterm.utils.LoggingUtility;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages font loading and caching for the terminal
 */
public class FontManager {
    private static final Logger LOGGER = LoggingUtility.getLogger(FontManager.class.getName());
    private static final Map<String, Font> fontCache = new HashMap<>();
    
    /**
     * Gets a font from cache or loads it if not available
     * @param fontPath path to the font file
     * @param size font size
     * @return the loaded font
     * @throws RuntimeException if font loading fails
     */
    public static Font getFont(String fontPath, float size) {
        String cacheKey = fontPath + "_" + size;
        
        if (fontCache.containsKey(cacheKey)) {
            return fontCache.get(cacheKey);
        } else {
            // Load the font
            File fontFile = new File(fontPath);
            try {
                Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
                font = font.deriveFont(Font.PLAIN, size);
                fontCache.put(cacheKey, font);
                return font;
            } catch (FontFormatException | IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to load font: " + fontPath, e);
                throw new RuntimeException("Failed to load font: " + fontPath, e);
            }
        }
    }
    
    /**
     * Clears the font cache
     */
    public static void clearCache() {
        fontCache.clear();
    }
}
