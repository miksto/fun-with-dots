package se.stockman.dots;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DotManager {

    private final List<Dot> movingDots = new ArrayList<>();
    private final List<Dot> frozenDots = new ArrayList<>();
    private final Settings settings;

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private CountDownLatch latch;

    private final State state = new State();

    private Random generator = new Random();

    public DotManager(Settings settings) {
        this.settings = settings;
        if (settings.isStickToMiddle()) {

            Dot middleDot = new Dot(settings.getWindowWidth() / 2, settings.getWindowHeight() / 2, settings);
            frozenDots.add(middleDot);
        }

        for (int i = 0; i < settings.getDotCount(); i++) {
            movingDots.add(new Dot(generator.nextInt(settings.getWindowWidth()), generator.nextInt(settings.getWindowHeight()), settings));
        }

        state.movingCount = movingDots.size();
        state.frozenCount = frozenDots.size();
        state.processingStepTime = 0;
    }

    public List<Dot> getMovingDots() {
        return movingDots;
    }

    public List<Dot> getFrozenDots() {
        return frozenDots;
    }

    public void executeNextStep() {
        long t1 = System.currentTimeMillis();
        movingDots.sort(Comparator.comparingInt(Dot::getX));
        frozenDots.sort(Comparator.comparingInt(Dot::getX));

        final int segmentSize = (int) Math.ceil(settings.getDotCount() / (float) 16);
        int numberOfSegments = (int) Math.ceil(movingDots.size() / (float) segmentSize);
        latch = new CountDownLatch(numberOfSegments);

        for (int i = 0; i < numberOfSegments; i++) {
            int start = i * segmentSize;
            int end = Math.min(movingDots.size(), start + segmentSize);
            executorService.execute(new ListSegmentRunnable(start, end));
        }

        try {
            latch.await();

            Iterator<Dot> iter = movingDots.iterator();
            while (iter.hasNext()) {
                Dot p = iter.next();
                if (p.isFrozen()) {
                    iter.remove();
                    frozenDots.add(p);
                }
            }

            long t2 = System.currentTimeMillis();
            int timeDiff = (int) (t2 - t1);

            state.setFrozenCount(frozenDots.size());
            state.setMovingCount(movingDots.size());
            state.setProcessingStepTime(timeDiff);
        } catch (InterruptedException E) {
            System.err.println("Interrupted");
        }
    }

    public class ListSegmentRunnable implements Runnable {

        int start, end;

        ListSegmentRunnable(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            processDots(start, end);
            latch.countDown();
        }
    }

    private void detectCollisions(int start, int end) {
        for (int i = start; i < end; i++) {
            //Kolla om krock med vägg
            Dot movingDot = movingDots.get(i);
            if (settings.isStickToWall()) {
                if (isWallCollision(movingDot)) {
                    movingDot.setFrozen(true);
                }
            }

            if (!movingDot.isFrozen()) {
                //Kolla efter krock med stillstående
                for (int j = 0; j < frozenDots.size(); j++) {
                    Dot frozenDot = frozenDots.get(j);
                    if (frozenDot.getX() > movingDot.getX() + settings.getDotRadius()) {
                        break;
                    }

                    if (isCollision(movingDot, frozenDot)) {
                        movingDot.setFrozen(true);
                        break;
                    }

                }
            }

            //Om krock mellan 2 stycken rörande
            if (settings.isStickToEachOther()) {
                for (int j = 0; j < movingDots.size() && i < movingDots.size(); j++) {
                    Dot movingDot2 = movingDots.get(j);
                    if (isCollision(movingDot, movingDot2)) {
                        movingDot.setFrozen(true);
                        movingDot2.setFrozen(true);
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


    private void processDots(int start, int end) {
        updatePositions(start, end);
        detectCollisions(start, end);
    }


    private void updatePositions(int start, int end) {
        for (int i = start; i < end; i++) {
            Dot dot = movingDots.get(i);
            int randx = dot.getX() + generateMovementDiff();
            int randy = dot.getY() + generateMovementDiff();
            randx = Util.clamp(randx, 0, settings.getWindowWidth());
            randy = Util.clamp(randy, 0, settings.getWindowHeight());
            dot.setPosition(randx, randy);
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
