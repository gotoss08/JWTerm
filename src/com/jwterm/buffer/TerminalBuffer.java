package com.jwterm.buffer;

import com.jwterm.geometry.Dimension;
import com.jwterm.glyph.Glyph;
import com.jwterm.utils.LoggingUtility;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Manages the terminal screen buffer with thread-safe operations.
 * Encapsulates the 2D glyph array and synchronization mechanisms.
 */
public class TerminalBuffer {
    private static final Logger LOGGER = LoggingUtility.getLogger(TerminalBuffer.class.getName());
    
    private Glyph[][] glyphs;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Dimension dimension = new Dimension();
    
    /**
     * Creates a new terminal buffer with the specified dimensions.
     *
     * @param rows Number of rows
     * @param cols Number of columns
     */
    public TerminalBuffer(int rows, int cols) {
        resize(rows, cols);
    }
    
    /**
     * Resizes the buffer to the specified dimensions.
     *
     * @param rows Number of rows
     * @param cols Number of columns
     */
    public void resize(int rows, int cols) {
        lock.writeLock().lock();
        try {
            dimension.set(cols, rows);
            glyphs = new Glyph[rows][cols];
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Gets the current dimensions of the buffer.
     *
     * @return The buffer dimensions
     */
    public Dimension getDimension() {
        lock.readLock().lock();
        try {
            return new Dimension(dimension);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Sets a specific glyph at the given position.
     *
     * @param row The row index
     * @param col The column index
     * @param glyph The glyph to set
     * @return This TerminalBuffer instance for chaining
     */
    public TerminalBuffer setGlyph(int row, int col, Glyph glyph) {
        lock.writeLock().lock();
        try {
            if (isInBounds(row, col)) {
                glyphs[row][col] = glyph;
            }
            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Gets the glyph at the specified position.
     *
     * @param row The row index
     * @param col The column index
     * @return The glyph at the specified position, or null if out of bounds
     */
    public Glyph getGlyph(int row, int col) {
        lock.readLock().lock();
        try {
            if (isInBounds(row, col)) {
                return glyphs[row][col];
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Fills the entire buffer with the specified glyph.
     *
     * @param glyph The glyph to fill with
     * @return This TerminalBuffer instance for chaining
     */
    public TerminalBuffer fill(Glyph glyph) {
        lock.writeLock().lock();
        try {
            for (int row = 0; row < dimension.getRows(); row++) {
                for (int col = 0; col < dimension.getCols(); col++) {
                    glyphs[row][col] = glyph;
                }
            }
            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Draws an outline around the buffer with the specified glyph.
     *
     * @param glyph The glyph to use for the outline
     * @return This TerminalBuffer instance for chaining
     */
    public TerminalBuffer outline(Glyph glyph) {
        lock.writeLock().lock();
        try {
            for (int row = 0; row < dimension.getRows(); row++) {
                for (int col = 0; col < dimension.getCols(); col++) {
                    if (row == 0 || row == dimension.getRows() - 1 || 
                        col == 0 || col == dimension.getCols() - 1) {
                        glyphs[row][col] = glyph;
                    }
                }
            }
            return this;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Executes a read operation with the buffer lock held.
     *
     * @param operation The operation to execute
     */
    public void withReadLock(Runnable operation) {
        lock.readLock().lock();
        try {
            operation.run();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Executes a write operation with the buffer lock held.
     *
     * @param operation The operation to execute
     */
    public void withWriteLock(Runnable operation) {
        lock.writeLock().lock();
        try {
            operation.run();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Checks if the specified coordinates are within the buffer bounds.
     *
     * @param row The row index
     * @param col The column index
     * @return true if the coordinates are valid, false otherwise
     */
    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < dimension.getRows() && col >= 0 && col < dimension.getCols();
    }
}
