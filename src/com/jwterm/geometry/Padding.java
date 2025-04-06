package com.jwterm.geometry;

import java.util.Objects;

public class Padding {
    private int horizontal;
    private int vertical;

    public Padding() {
        this(0);
    }

    public Padding(Padding padding) {
        this(padding.horizontal, padding.vertical);
    }

    public Padding(int padding) {
        this(padding, padding);
    }

    public Padding(int horizontal, int vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public Padding set(int size) {
        this.horizontal = size;
        this.vertical = size;
        return this;
    }

    public Padding set(int width, int height) {
        this.horizontal = width;
        this.vertical = height;
        return this;
    }

    public int getHorizontal() {
        return horizontal;
    }

    public Padding setHorizontal(int horizontal) {
        this.horizontal = horizontal;
        return this;
    }

    public int getVertical() {
        return vertical;
    }

    public Padding setVertical(int vertical) {
        this.vertical = vertical;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Padding padding = (Padding) o;
        return getHorizontal() == padding.getHorizontal() && getVertical() == padding.getVertical();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHorizontal(), getVertical());
    }

    @Override
    public String toString() {
        return "Padding{" +
                "horizontal=" + horizontal +
                ", vertical=" + vertical +
                '}';
    }

}
