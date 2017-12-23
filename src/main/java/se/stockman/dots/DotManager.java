package se.stockman.dots;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DotManager {

    private final List<Dot> movingDots = new ArrayList<>();
    private final List<Dot> frozenDots = new ArrayList<>();
    private final Settings settings;

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private CountDownLatch latch;

    private State state;

    private Random generator = new Random();

    public DotManager(Settings settings) {
        this.settings = settings;
        if (settings.isStickToMiddle()) {

            Dot middleDot = new Dot(settings.getWindowWidth() / 2, settings.getWindowHeight() / 2);
            frozenDots.add(middleDot);
        }

        for (int i = 0; i < settings.getDotCount(); i++) {
            movingDots.add(new Dot(generator.nextInt(settings.getWindowWidth()), generator.nextInt(settings.getWindowHeight())));
        }
    }

    public List<Dot> getMovingDots() {
        return movingDots;
    }

    public List<Dot> getFrozenDots() {
        return frozenDots;
    }

    public void executeNextStep() {
        long t1 = System.currentTimeMillis();

        final int segmentSize = 500;
        int numberOfSegments = (int) Math.ceil(movingDots.size() / (float) segmentSize);
        latch = new CountDownLatch(numberOfSegments);

        for (int i = 0; i < movingDots.size(); i += segmentSize) {
            int start = i;
            int end = Math.min(movingDots.size(), i + segmentSize);
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

            state = new State(movingDots.size(), timeDiff);
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
            if ((movingDots.get(i).getX() < settings.getDotRadius() || movingDots.get(i).getY() < settings.getDotRadius()
                || movingDots.get(i).getX() > settings.getWindowWidth() - settings.getDotRadius()
                || movingDots.get(i).getY() > settings.getWindowHeight() - settings.getDotRadius())
                && settings.isStickToWall()) {
                movingDots.get(i).setFrozen(true);
            }

            //Kolla efter krock med stillstående
            for (int j = 0; j < frozenDots.size() && i < movingDots.size(); j++) {
                if (isCollision(i, j) && i != j) {
                    movingDots.get(i).setFrozen(true);
                    break;
                }
            }

            //Om krock mellan 2 stycken rörande
            if (settings.isStickToEachOther() && movingDots.size() > 0) {
                for (int j = 0; j < movingDots.size() && i < movingDots.size(); j++) {
                    if ((dist(movingDots.get(i), movingDots.get(j)) < settings.getDotRadius() * 1.2) && i != j) {
                        movingDots.get(i).setFrozen(true);
                        movingDots.get(j).setFrozen(true);
                    }
                }

            }
        }
    }

    private boolean isCollision(int i, int j) {
        if (i == j) {
            return false;
        }


        int dotRadius = settings.getDotRadius();
        Dot p1 = movingDots.get(i);
        Dot p2 = frozenDots.get(j);

        int dotDiameter = 2 * dotRadius;
        if (Math.abs(p1.getX() - p2.getX()) > dotDiameter || Math.abs(p1.getY() - p2.getY()) > dotDiameter) {
            return false;
        }

        double distance = dist(p1, p2);

        return distance < dotRadius;
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

    private double dist(Dot p1, Dot p2) {
        double tal1 = Math.pow((p1.getX() - p2.getX()), 2);
        double tal2 = Math.pow((p1.getY() - p2.getY()), 2);
        return Math.sqrt(tal1 + tal2);
    }

    public State getState() {
        if (state == null) {
            return new State(movingDots.size(), -1);
        }
        return state;
    }

    public static class State {
        private int movingCount;
        private int processingStepTime;

        public State(int movingCount, int processingStepTime) {
            this.movingCount = movingCount;
            this.processingStepTime = processingStepTime;
        }

        public int getMovingCount() {
            return movingCount;
        }

        public int getProcessingStepTime() {
            return processingStepTime;
        }
    }

}
