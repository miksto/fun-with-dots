package se.stockman.dots;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

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

                for (int i = 0; i < 4; i++) {
                    dotManager.executeNextStep();
                }
                dotFrame.repaintDots();

                NumberFormat format = NumberFormat.getNumberInstance();
                DotManager.State state = dotManager.getState();
                movingCountLabel.setText("Moving: " + format.format(state.getMovingCount()));
                frozenCountLabel.setText("Frozen: " + format.format(state.getFrozenCount()));
                totalTimeLabel.setText("Total time: " + format.format(state.getProcessingStepTime()));
                int numCollisionChecks = state.getMovingCount() * state.getFrozenCount();
                collisionChecksCountLabel.setText("Num collisions checks: " + format.format(numCollisionChecks));
                timePerCollisionCheckLabel.setText("Time per collision check: " + format.format((state.getProcessingStepTime() / (float) numCollisionChecks)));

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
        counterFrame.setSize(250, 300);
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


