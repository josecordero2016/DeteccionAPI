package com.e.deteccionapi;

public class Cara
{
    private float p1x;
    private float p1y;
    private float p2x;
    private float p2y;

    public Cara(float p1x, float p1y, float p2x, float p2y) {
        this.p1x = p1x;
        this.p1y = p1y;
        this.p2x = p2x;
        this.p2y = p2y;
    }

    public float getP1x() {
        return p1x;
    }

    public void setP1x(float p1x) {
        this.p1x = p1x;
    }

    public float getP1y() {
        return p1y;
    }

    public void setP1y(float p1y) {
        this.p1y = p1y;
    }

    public float getP2x() {
        return p2x;
    }

    public void setP2x(float p2x) {
        this.p2x = p2x;
    }

    public float getP2y() {
        return p2y;
    }

    public void setP2y(float p2y) {
        this.p2y = p2y;
    }
}