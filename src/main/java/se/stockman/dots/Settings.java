package se.stockman.dots;

public class Settings {
    private boolean stickToWall;
    private boolean stickToMiddle;
    private boolean stickToEachOther;

    private int dotCount;
    private int dotRadius;

    private int windowHeight;
    private int windowWidth;
    private int squaredDotRadius;

    public boolean isStickToWall() {
        return stickToWall;
    }

    public void setStickToWall(boolean stickToWall) {
        this.stickToWall = stickToWall;
    }

    public boolean isStickToMiddle() {
        return stickToMiddle;
    }

    public void setStickToMiddle(boolean stickToMiddle) {
        this.stickToMiddle = stickToMiddle;
    }

    public boolean isStickToEachOther() {
        return stickToEachOther;
    }

    public void setStickToEachOther(boolean stickToEachOther) {
        this.stickToEachOther = stickToEachOther;
    }

    public int getDotCount() {
        return dotCount;
    }

    public void setDotCount(int dotCount) {
        this.dotCount = dotCount;
    }

    public int getDotRadius() {
        return dotRadius;
    }

    public void setDotRadius(int dotRadius) {
        this.dotRadius = dotRadius;
        this.squaredDotRadius = dotRadius * dotRadius;
    }

    public int getSquaredDotRadius() {
        return squaredDotRadius;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }
}
