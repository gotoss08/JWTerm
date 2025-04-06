package com.jwterm.utils;

/**
 * Interface for components that need to be notified about resize events.
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
