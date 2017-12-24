package se.stockman.dots.computation;

import se.stockman.dots.Dot;
import se.stockman.dots.Util;
import se.stockman.dots.settings.Settings;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class DotPositionUpdater {

    private CountDownLatch latch;
    private List<Dot> dots;
    private ExecutorService executorService;
    private Settings settings;
    private Random random;

    DotPositionUpdater(List<Dot> dots, ExecutorService executorService, Settings settings) {
        this.dots = dots;
        this.executorService = executorService;
        this.settings = settings;
        random = new Random();
    }

    void updatePositions() throws InterruptedException {
        latch = new CountDownLatch(Settings.SEGMENT_COUNT);
        int segmentSize = (int) Math.ceil(dots.size() / (float) Settings.SEGMENT_COUNT);

        for (int i = 0; i < Settings.SEGMENT_COUNT; i++) {
            int start = i * segmentSize;
            int end = Math.min(dots.size(), start + segmentSize);
            executorService.execute(new PositionUpdateRunnable(start, end));
        }

        latch.await();
    }

    private class PositionUpdateRunnable implements Runnable {

        private int start;
        private int end;

        PositionUpdateRunnable(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            updatePositions(start, end);
            latch.countDown();
        }

        private int generateMovementDiff() {
            return random.nextInt(11) - 5;
        }

        private void updatePositions(int start, int end) {
            for (int i = start; i < end; i++) {
                Dot dot = dots.get(i);
                if (!dot.isFrozenPastRound()) {
                    updatePosition(dot);
                }
            }
        }

        private void updatePosition(Dot dot) {
            int randx = dot.getX() + generateMovementDiff();
            int randy = dot.getY() + generateMovementDiff();
            randx = Util.clamp(randx, 0, settings.getWindowWidth());
            randy = Util.clamp(randy, 0, settings.getWindowHeight());
            dot.setPosition(randx, randy);
        }
    }
}
