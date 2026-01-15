package com.cgvsu.math;


import java.util.Objects;


public class Vector2f {
    private static final float EPSILON = 1e-7f;
    private final float x, y;

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f add(Vector2f other) {
        return new Vector2f(x + other.x, y + other.y);
    }
    
    public Vector2f subtract(Vector2f other) {
        return new Vector2f(x - other.x, y - other.y);
    }
    
    public Vector2f multiply(float scalar) {
        return new Vector2f(x * scalar, y * scalar);
    }
    
    public Vector2f divide(float scalar) {
        if (Math.abs(scalar) < EPSILON) {
            throw new ArithmeticException("Division by zero");
        }
        return new Vector2f(x / scalar, y / scalar);
    }
    
    public float dot(Vector2f other) {
        return x * other.x + y * other.y;
    }
    
    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }
    
    public Vector2f normalize() {
        float len = length();
        if (len < EPSILON) {
            return new Vector2f(0, 0);
        }
        return divide(len);
    }
    
    public float distanceTo(Vector2f other) {
        float dx = x - other.x;
        float dy = y - other.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector2f other = (Vector2f) obj;
        return Math.abs(x - other.x) < EPSILON &&
                Math.abs(y - other.y) < EPSILON;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Float.hashCode(x),
                Float.hashCode(y)
        );
    }

    public boolean equals(Vector2f other) {
        if (other == null) return false;
        return Math.abs(x - other.x) < EPSILON &&
                Math.abs(y - other.y) < EPSILON;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("Vector2f(%.6f, %.6f)", x, y);
    }
}
