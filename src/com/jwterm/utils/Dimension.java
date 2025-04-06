package com.jwterm.utils;

import java.util.Objects;

public class Dimension {
    private int cols;
    private int rows;

    public Dimension() {
        this(0);
    }

    public Dimension(Dimension dimension) {
        this(dimension.cols, dimension.rows);
    }

    public Dimension(int dimension) {
        this(dimension, dimension);
    }

    public Dimension(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
    }

    public Dimension set(int dimension) {
        this.cols = dimension;
        this.rows = dimension;
        return this;
    }

    public Dimension set(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        return this;
    }

    public int getCols() {
        return cols;
    }

    public Dimension setCols(int cols) {
        this.cols = cols;
        return this;
    }

    public int getRows() {
        return rows;
    }

    public Dimension setRows(int rows) {
        this.rows = rows;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dimension dimension = (Dimension) o;
        return getCols() == dimension.getCols() && getRows() == dimension.getRows();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCols(), getRows());
    }

    @Override
    public String toString() {
        return "Dimension{" +
                "cols=" + cols +
                ", rows=" + rows +
                '}';
    }

}
