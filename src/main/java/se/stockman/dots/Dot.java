package se.stockman.dots;

import java.awt.*;

public class Dot {
    private int x, y;
    private Color color;
    private boolean frozen;

    public Dot(int x, int y) {
        this.x = x;
        this.y = y;
        color = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }

    public void setPosition(int randx, int randy) {
        x = randx;
        y = randy;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }
}