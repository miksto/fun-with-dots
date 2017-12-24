package se.stockman.dots;

import se.stockman.dots.settings.Settings;

import java.awt.*;

public class Dot {
    private int x, y;
    private Color color;
    private boolean frozenCurrentRound;
    private boolean frozenPastRound;

    public Dot(int x, int y, Settings settings) {
        this.x = x;
        this.y = y;
        color = new Color(
            Util.mapToColor(x, settings.getWindowWidth()),
            Util.mapToColor(y, settings.getWindowHeight()),
            (int) (Math.random() * 255));
    }

    public void setPosition(int randx, int randy) {
        x = randx;
        y = randy;
    }

    public boolean isFrozenCurrentRound() {
        return frozenCurrentRound;
    }

    public void setFrozenCurrentRound(boolean frozen) {
        this.frozenCurrentRound = frozen;
    }

    public boolean isFrozenPastRound() {
        return frozenPastRound;
    }

    public void setFrozenPastRound(boolean frozenLastRound) {
        this.frozenPastRound = frozenLastRound;
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