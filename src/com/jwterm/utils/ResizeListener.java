package com.jwterm.utils;

/**
 * Interface for classes that need to be notified of resize events.
 * This interface is used by the ResizeManager to notify registered listeners
 * when the terminal window has been resized.
 */
public interface ResizeListener {
    
    /**
     * Called when a resize event occurs.
     * 
     * @param width The new width in pixels
     * @param height The new height in pixels
     */
    void onResize(int width, int height);
}
