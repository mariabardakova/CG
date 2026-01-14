package ru.kirill.task4_4_1.utils;


import java.util.Objects;

import ru.kirill.task4_4_1.math.Matrix4f;


public class Vector3f {
    private static final float EPSILON = 1e-7f;
    private final float x, y, z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f transform(Matrix4f matrix) {
        return matrix.mul(this);
    }
    
    public Vector3f add(Vector3f other) {
        return new Vector3f(x + other.x, y + other.y, z + other.z);
    }
    
    public Vector3f subtract(Vector3f other) {
        return new Vector3f(x - other.x, y - other.y, z - other.z);
    }
    
    public Vector3f multiply(float scalar) {
        return new Vector3f(x * scalar, y * scalar, z * scalar);
    }
    
    public Vector3f divide(float scalar) {
        if (Math.abs(scalar) < EPSILON) {
            throw new ArithmeticException("Division by zero");
        }
        return new Vector3f(x / scalar, y / scalar, z / scalar);
    }
    
    public float dot(Vector3f other) {
        return x * other.x + y * other.y + z * other.z;
    }
    
    public Vector3f cross(Vector3f other) {
        return new Vector3f(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }
    
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }
    
    public Vector3f normalize() {
        float len = length();
        if (len < EPSILON) {
            return new Vector3f(0, 0, 0);
        }
        return divide(len);
    }
    
    public float distanceTo(Vector3f other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector3f other = (Vector3f) obj;
        return Math.abs(x - other.x) < EPSILON &&
                Math.abs(y - other.y) < EPSILON &&
                Math.abs(z - other.z) < EPSILON;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                Float.hashCode(x),
                Float.hashCode(y),
                Float.hashCode(z)
        );
    }

    public boolean equals(Vector3f other) {
        if (other == null) return false;
        return Math.abs(x - other.x) < EPSILON &&
                Math.abs(y - other.y) < EPSILON &&
                Math.abs(z - other.z) < EPSILON;
    }

    @Override
    public String toString() {
        return String.format("Vector3f(%.6f, %.6f, %.6f)", x, y, z);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
}
