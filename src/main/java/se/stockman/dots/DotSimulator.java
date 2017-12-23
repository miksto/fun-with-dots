package se.stockman.dots;

import javax.swing.*;
import java.awt.*;

public class DotSimulator {
    private JFrame counterFrame = new JFrame();
    private JLabel movingCountLabel = new JLabel();
    private JLabel frozenCountLabel = new JLabel();
    private JLabel collisionChecksCountLabel = new JLabel();
    private JLabel totalTimeLabel = new JLabel();
    private JLabel timePerCollisionCheckLabel = new JLabel();

    private DotFrame dotFrame;
    private DotManager dotManager;

    private DotSimulator() {
        SettingsFrame settingsFrame = new SettingsFrame();
        settingsFrame.setListener(this::startSimulation);
    }

    private Runnable simulationStepRunnable = new Runnable() {
        @Override
        public void run() {
            while (dotManager.getState().getMovingCount() > 0) {

                for (int i = 0; i < 10; i++) {
                    dotManager.executeNextStep();
                }
                dotFrame.repaintDots();

                DotManager.State state = dotManager.getState();
                movingCountLabel.setText("Moving: " + state.getMovingCount());
                frozenCountLabel.setText("Frozen: " + state.getFrozenCount());
                totalTimeLabel.setText("Total time: " + state.getProcessingStepTime());
                int numCollisionChecks = state.getMovingCount() * state.getFrozenCount();
                collisionChecksCountLabel.setText("Num collisions checks: " + numCollisionChecks);
                timePerCollisionCheckLabel.setText("Time per collision check: " + (state.getProcessingStepTime() / (float) numCollisionChecks));

            }
            System.err.println("Done");
        }
    };

    private void startSimulation(Settings settings) {
        dotManager = new DotManager(settings);
        frozenCountLabel.setText(String.valueOf(dotManager.getState().getMovingCount()));
        counterFrame.setLayout(new GridLayout(5, 1));
        counterFrame.add(movingCountLabel);
        counterFrame.add(frozenCountLabel);
        counterFrame.add(totalTimeLabel);
        counterFrame.add(collisionChecksCountLabel);
        counterFrame.add(timePerCollisionCheckLabel);
        counterFrame.pack();
        counterFrame.setVisible(true);
        dotFrame = new DotFrame(settings, dotManager);

        Thread thread = new Thread(simulationStepRunnable);
        thread.start();
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            DotSimulator dotSimulator = new DotSimulator();
        });
    }

}


