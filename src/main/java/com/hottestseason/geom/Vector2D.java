package com.hottestseason.geom;

public class Vector2D implements Cloneable {
    public final double x, y;

    public Vector2D() {
        this(0, 0);
    }

    public Vector2D(double x, double y) {
        this.x = x; this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public Vector2D clone() {
        return new Vector2D(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vector2D) {
            Vector2D vector = (Vector2D) obj;
            return x == vector.x && y == vector.y;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int) x << 16 ^ (int) y >>> 16;
    }

    public Point2D toPoint2D() {
        return new Point2D(x, y);
    }

    public double getNorm() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2D expandTo(Double norm) {
        return normalize().multiply(norm);
    }

    public Vector2D reverse() {
        return new Vector2D(-x, -y);
    }

    public Vector2D add(double x, double y) {
        return new Vector2D(this.x + x, this.y + y);
    }

    public Vector2D add(Vector2D vector) {
        return add(vector.x, vector.y);
    }

    public Vector2D multiply(double product) {
        return new Vector2D(x * product, y * product);
    }

    public Vector2D divide(double division) {
        return multiply(1.0 / division);
    }

    public Vector2D half() {
        return divide(2.0);
    }

    public Double innerProduct(Vector2D vector) {
        return x * vector.x + y * vector.y;
    }

    public Double exteriorProduct(Vector2D vector) {
        return x * vector.y - vector.x * y;
    }

    public Vector2D normalize() {
        Double norm = getNorm();
        if (norm == 0) {
            return multiply(1.0);
        } else {
            return multiply(1.0 / norm);
        }
    }

    public Vector2D rotate(double angle) {
        Double cos = Math.cos(angle);
        Double sin = Math.sin(angle);
        return new Vector2D(cos * x - sin * y, x * sin + y * cos);
    }

    public Double getAngle() {
        return Math.atan2(y, x);
    }

    public Double getAngle(Vector2D vector) {
        return Math.acos(innerProduct(vector) / (getNorm() * vector.getNorm()));
    }
}
