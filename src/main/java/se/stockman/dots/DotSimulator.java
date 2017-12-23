package se.stockman.dots;

import javax.swing.*;

public class DotSimulator {
    private JFrame counterFrame = new JFrame();
    private JLabel counterLabel = new JLabel();

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
                counterLabel.setText(
                    state.getMovingCount()
                        + "\ndrawTime: " + state.getProcessingStepTime());
            }
            System.err.println("Done");
        }
    };

    private void startSimulation(Settings settings) {
        dotManager = new DotManager(settings);
        counterLabel.setText(String.valueOf(dotManager.getState().getMovingCount()));
        counterFrame.add(counterLabel);
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


