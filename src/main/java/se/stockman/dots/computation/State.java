package se.stockman.dots.computation;

public class State {
    private int movingCount;
    private int frozenCount;
    private int processingStepTime;

    State() {
    }

    public int getMovingCount() {
        return movingCount;
    }

    public void setMovingCount(int movingCount) {
        this.movingCount = movingCount;
    }

    public int getProcessingStepTime() {
        return processingStepTime;
    }

    public void setProcessingStepTime(int processingStepTime) {
        this.processingStepTime = processingStepTime;
    }

    public int getFrozenCount() {
        return frozenCount;
    }

    public void setFrozenCount(int frozenCount) {
        this.frozenCount = frozenCount;
    }
}