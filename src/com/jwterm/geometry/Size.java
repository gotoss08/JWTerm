package com.jwterm.geometry;

import java.util.Objects;

public class Size {
    private int width;
    private int height;

    public Size() {
        this(0);
    }

    public Size(Size size) {
        this(size.width, size.height);
    }

    public Size(int size) {
        this(size, size);
    }

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Size set(int size) {
        return set(size, size);
    }

    public Size set(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public Size setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public Size setHeight(int height) {
        this.height = height;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Size size = (Size) o;
        return getWidth() == size.getWidth() && getHeight() == size.getHeight();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWidth(), getHeight());
    }

    @Override
    public String toString() {
        return "Size{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }

}
