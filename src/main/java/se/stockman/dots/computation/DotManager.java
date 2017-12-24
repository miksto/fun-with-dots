package se.stockman.dots.computation;

import se.stockman.dots.Dot;
import se.stockman.dots.settings.Settings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DotManager {

    private final List<Dot> dots = new ArrayList<>();
    private final Settings settings;
    private final State state = new State();

    private DotPositionUpdater dotPositionUpdater;
    private DotCollisionDetector dotCollisionDetector;

    public DotManager(Settings settings) {
        this.settings = settings;
        ExecutorService executorService = Executors.newCachedThreadPool();
        dotPositionUpdater = new DotPositionUpdater(dots, executorService, settings);
        dotCollisionDetector = new DotCollisionDetector(dots, executorService, settings);

        if (settings.isStickToMiddle()) {
            Dot middleDot = new Dot(settings.getWindowWidth() / 2, settings.getWindowHeight() / 2, settings);
            middleDot.setFrozenPastRound(true);
            dots.add(middleDot);
        }

        Random generator = new Random();
        for (int i = 0; i < settings.getDotCount(); i++) {
            int randx = generator.nextInt(settings.getWindowWidth());
            int randy = generator.nextInt(settings.getWindowHeight());
            dots.add(new Dot(randx, randy, settings));
        }

        state.setMovingCount(settings.getDotCount());
        state.setFrozenCount(settings.isStickToMiddle() ? 1 : 0);
        state.setProcessingStepTime(-1);
    }

    public List<Dot> getDots() {
        return dots;
    }

    public void executeNextStep() throws InterruptedException {
        long t1 = System.currentTimeMillis();

        dotPositionUpdater.updatePositions();
        dots.sort(Comparator.comparingInt(Dot::getX));
        dotCollisionDetector.detectCollisions();

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

    public State getState() {
        return state;
    }
}
