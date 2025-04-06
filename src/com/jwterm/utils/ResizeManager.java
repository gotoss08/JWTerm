package com.jwterm.utils;

import com.jwterm.TermScreen;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Manages window resize events with debouncing to prevent excessive
 * terminal screen recalculations during resize operations.
 */
public class ResizeManager {
    private static final Logger LOGGER = LoggingUtility.getLogger(ResizeManager.class.getName());
    private static final int RESIZE_DEBOUNCE_MS = 150;
    
    private final Component component;
    private final TermScreen termScreen;
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> scheduledResize;
    
    /**
     * Creates a new resize manager for the given component and terminal screen.
     * 
     * @param component The component to watch for resize events
     * @param termScreen The terminal screen to update on resize
     */
    public ResizeManager(Component component, TermScreen termScreen) {
        this.component = component;
        this.termScreen = termScreen;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ResizeManager-Thread");
            t.setDaemon(true);
            return t;
        });
        
        setupResizeListener();
    }
    
    /**
     * Sets up the resize event listener with debouncing.
     */
    private void setupResizeListener() {
        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scheduleResize();
            }
        });
    }
    
    /**
     * Schedules a resize operation with debouncing.
     */
    private void scheduleResize() {
        int width = component.getWidth();
        int height = component.getHeight();
        
        if (width <= 0 || height <= 0) {
            return;
        }
        
        // Cancel any pending resize operation
        if (scheduledResize != null && !scheduledResize.isDone()) {
            scheduledResize.cancel(false);
        }
        
        // Schedule a new resize operation
        scheduledResize = executor.schedule(
            () -> handleResize(width, height), 
            RESIZE_DEBOUNCE_MS, 
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Handles the actual resize operation.
     * 
     * @param width New width
     * @param height New height
     */
    private void handleResize(int width, int height) {
        if (termScreen != null) {
            termScreen.withWriteLock(() -> {
                termScreen.resize(width, height);
                LOGGER.fine("Resized to: " + width + "x" + height);
            });
        }
    }
    
    /**
     * Forces an immediate resize update.
     */
    public void forceResize() {
        int width = component.getWidth();
        int height = component.getHeight();
        
        if (width > 0 && height > 0) {
            handleResize(width, height);
        }
    }
    
    /**
     * Shuts down the resize manager and its executor.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

