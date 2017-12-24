package se.stockman.dots;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DotManager {

    private final List<Dot> dots = new ArrayList<>();
    //    private final List<Dot> frozenDots = new ArrayList<>();
    private final Settings settings;

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private CountDownLatch latch;

    private final State state = new State();

    private Random generator = new Random();

    public DotManager(Settings settings) {
        this.settings = settings;
        if (settings.isStickToMiddle()) {

            Dot middleDot = new Dot(settings.getWindowWidth() / 2, settings.getWindowHeight() / 2, settings);
            middleDot.setFrozenPastRound(true);
            dots.add(middleDot);
        }

        for (int i = 0; i < settings.getDotCount(); i++) {
            dots.add(new Dot(generator.nextInt(settings.getWindowWidth()), generator.nextInt(settings.getWindowHeight()), settings));
        }

        state.movingCount = settings.getDotCount();
        state.frozenCount = settings.isStickToMiddle() ? 1 : 0;
        state.processingStepTime = 0;
    }

    public List<Dot> getDots() {
        return dots;
    }

    public void executeNextStep() {
        long t1 = System.currentTimeMillis();

        final int segmentSize = (int) Math.ceil(settings.getDotCount() / (float) 16);
        int numberOfSegments = (int) Math.ceil(dots.size() / (float) segmentSize);

        updateAllPositions(segmentSize, numberOfSegments);
        dots.sort(Comparator.comparingInt(Dot::getX));
        detectAllCollisions(segmentSize, numberOfSegments);

        int frozen = 0;
        for (Dot dot : dots) {
            if (dot.isFrozenCurrentRound()) {
                dot.setFrozenPastRound(true);
                dot.setFrozenCurrentRound(false);
            }
            if (dot.isFrozenPastRound()) {
                frozen++;
            }
        }

        long t2 = System.currentTimeMillis();
        int timeDiff = (int) (t2 - t1);
        state.setFrozenCount(frozen);
        state.setMovingCount(settings.getDotCount() - frozen);
        state.setProcessingStepTime(timeDiff);
    }

    private void updateAllPositions(int segmentSize, int numberOfSegments) {
        latch = new CountDownLatch(numberOfSegments);
        for (int i = 0; i < numberOfSegments; i++) {
            int start = i * segmentSize;
            int end = Math.min(dots.size(), start + segmentSize);
            executorService.execute(new UpdatePositionRunnable(start, end));
        }

        try {
            latch.await();
        } catch (InterruptedException E) {
            System.err.println("Interrupted");
        }
    }

    private void detectAllCollisions(int segmentSize, int numberOfSegments) {
        latch = new CountDownLatch(numberOfSegments);
        for (int i = 0; i < numberOfSegments; i++) {
            int start = i * segmentSize;
            int end = Math.min(dots.size(), start + segmentSize);
            executorService.execute(new DetectCollisionsRunnable(start, end));
        }

        try {
            latch.await();
        } catch (InterruptedException E) {
            System.err.println("Interrupted");
        }
    }

    public class UpdatePositionRunnable implements Runnable {

        int start, end;

        UpdatePositionRunnable(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            updatePositions(start, end);
            latch.countDown();
        }
    }

    public class DetectCollisionsRunnable implements Runnable {

        int start, end;

        DetectCollisionsRunnable(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            detectCollisions(start, end);
            latch.countDown();
        }
    }

    private void detectCollisions(int start, int end) {
        for (int i = start; i < end; i++) {
            //Kolla om krock med vägg
            Dot movingDot = dots.get(i);
            if (settings.isStickToWall()) {
                if (isWallCollision(movingDot)) {
                    movingDot.setFrozenCurrentRound(true);
                }
            }

            if (!movingDot.isFrozenCurrentRound()) {
                //Backtrack untill we find a dot enough far to the left
                int j = i - 1;
                while (j > 0) {
                    if (dots.get(j).getX() < movingDot.getX() - settings.getDotRadius()) {
                        break;
                    }
                    j--;
                }

                for (; j < dots.size(); j++) {
                    Dot dot2 = dots.get(j);

                    if (dot2.isFrozenPastRound()) {
                        if (dot2.getX() > movingDot.getX() + settings.getDotRadius()) {
                            break;
                        }

                        if (isCollision(movingDot, dot2)) {
                            movingDot.setFrozenCurrentRound(true);
                            break;
                        }
                    }
                }
            }

            //Om krock mellan 2 stycken rörande
            if (settings.isStickToEachOther()) {
                for (int j = 0; j < dots.size() && i < dots.size(); j++) {
                    Dot dot2 = dots.get(j);
                    if (isCollision(movingDot, dot2)) {
                        movingDot.setFrozenCurrentRound(true);
                        dot2.setFrozenCurrentRound(true);
                    }
                }
            }
        }
    }

    private boolean isWallCollision(Dot dot) {
        return dot.getX() <= settings.getDotRadius()
            || dot.getY() <= settings.getDotRadius()
            || dot.getX() >= settings.getWindowWidth() - settings.getDotRadius()
            || dot.getY() >= settings.getWindowHeight() - settings.getDotRadius();
    }

    private boolean isCollision(Dot p1, Dot p2) {
        return p1 != p2
            && squaredDist(p1, p2) < settings.getSquaredDotRadius();
    }


    private void updatePositions(int start, int end) {
        for (int i = start; i < end; i++) {
            Dot dot = dots.get(i);
            if (!dot.isFrozenPastRound()) {
                int randx = dot.getX() + generateMovementDiff();
                int randy = dot.getY() + generateMovementDiff();
                randx = Util.clamp(randx, 0, settings.getWindowWidth());
                randy = Util.clamp(randy, 0, settings.getWindowHeight());
                dot.setPosition(randx, randy);
            }
        }
    }

    private int generateMovementDiff() {
        return generator.nextInt(11) - 5;
    }

    private double squaredDist(Dot p1, Dot p2) {
        int dist1 = p1.getX() - p2.getX();
        int dist2 = p1.getY() - p2.getY();

        return dist1 * dist1 + dist2 * dist2;
    }

    public State getState() {
        return state;
    }

    public static class State {
        private int movingCount;
        private int frozenCount;
        private int processingStepTime;

        public void setMovingCount(int movingCount) {
            this.movingCount = movingCount;
        }

        public void setProcessingStepTime(int processingStepTime) {
            this.processingStepTime = processingStepTime;
        }

        public void setFrozenCount(int frozenCount) {
            this.frozenCount = frozenCount;
        }

        public State() {
        }

        public int getMovingCount() {
            return movingCount;
        }

        public int getProcessingStepTime() {
            return processingStepTime;
        }

        public int getFrozenCount() {
            return frozenCount;
        }
    }

}
