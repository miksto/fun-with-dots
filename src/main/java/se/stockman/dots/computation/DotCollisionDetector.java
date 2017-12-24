package se.stockman.dots.computation;

import se.stockman.dots.Dot;
import se.stockman.dots.settings.Settings;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

public class DotCollisionDetector {

    private Executor executorService;
    private List<Dot> dots;
    private Settings settings;
    private CountDownLatch latch;

    DotCollisionDetector(List<Dot> dots, Executor executorService, Settings settings) {
        this.executorService = executorService;
        this.dots = dots;
        this.settings = settings;
    }

    void detectCollisions() throws InterruptedException {
        latch = new CountDownLatch(Settings.SEGMENT_COUNT);
        int segmentSize = (int) Math.ceil(dots.size() / (float) Settings.SEGMENT_COUNT);
        for (int i = 0; i < Settings.SEGMENT_COUNT; i++) {
            int start = i * segmentSize;
            int end = Math.min(dots.size(), start + segmentSize);
            executorService.execute(new CollisionDetectionRunnable(start, end));
        }
        latch.await();
    }

    private void detectCollisions(int start, int end) {
        for (int i = start; i < end; i++) {

            Dot movingDot = dots.get(i);
            if (settings.isStickToWall()) {
                if (isWallCollision(movingDot)) {
                    movingDot.setFrozenCurrentRound(true);
                }
            }

            if (!movingDot.isFrozenCurrentRound()) {
                int collisionZoneStart = getCollisionZoneStartIndex(i, movingDot);

                for (int j = collisionZoneStart; j < dots.size(); j++) {
                    Dot dot2 = dots.get(j);

                    if (dot2.isFrozenPastRound()) {
                        boolean isPastCollisionZone = dot2.getX() > movingDot.getX() + settings.getDotRadius();
                        if (isPastCollisionZone) {
                            break;
                        }

                        if (isCollision(movingDot, dot2)) {
                            movingDot.setFrozenCurrentRound(true);
                            break;
                        }
                    }
                }
            }

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

    private int getCollisionZoneStartIndex(int i, Dot movingDot) {
        int j = Math.max(i - 1, 0);
        while (j > 0) {
            if (dots.get(j).getX() < movingDot.getX() - settings.getDotRadius()) {
                break;
            }
            j--;
        }
        return j;
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

    private double squaredDist(Dot p1, Dot p2) {
        int dist1 = p1.getX() - p2.getX();
        int dist2 = p1.getY() - p2.getY();

        return dist1 * dist1 + dist2 * dist2;
    }

    private class CollisionDetectionRunnable implements Runnable {
        int start;
        int end;

        CollisionDetectionRunnable(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            detectCollisions(start, end);
            latch.countDown();
        }
    }
}
