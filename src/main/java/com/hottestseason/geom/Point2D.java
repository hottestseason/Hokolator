package com.hottestseason.geom;

public class Point2D extends Vector2D {
    public Point2D() {
        super();
    }

    public Point2D(Point2D point) {
        super(point.x, point.y);
    }

    public Point2D(double x, double y) {
        super(x, y);
    }

    @Override
    public Point2D clone() {
        return (Point2D) super.clone();
    }

    @Override
    public Point2D add(double x, double y) {
        return super.add(x, y).toPoint2D();
    }

    @Override
    public Point2D add(Vector2D vector) {
        return super.add(vector).toPoint2D();
    }

    public Vector2D getVectorTo(Point2D point) {
        return reverse().add(point);
    }

    public Double getDistanceFrom(Point2D point) {
        return getVectorTo(point).getNorm();
    }
}
