package se.stockman.dots.simulation;

import se.stockman.dots.computation.DotManager;
import se.stockman.dots.settings.Settings;
import se.stockman.dots.settings.SettingsFrame;

public class Simulation {
    private static final long REPAINT_INTERVAL = 2000;
    private SimulationFrame simulationFrame;
    private DotManager dotManager;
    private Runnable simulationStepRunnable = new Runnable() {
        @Override
        public void run() {
            simulationFrame.update();

            long t1 = System.currentTimeMillis();
            while (dotManager.getState().getMovingCount() > 0) {

                try {
                    dotManager.executeNextStep();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long t2 = System.currentTimeMillis();

                if (t2 - t1 > REPAINT_INTERVAL) {
                    simulationFrame.update();
                    t1 = System.currentTimeMillis();
                }
            }
            simulationFrame.update();
        }
    };

    private Simulation() {
        SettingsFrame settingsFrame = new SettingsFrame();
        settingsFrame.setListener(this::startSimulation);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            Simulation simulation = new Simulation();
        });
    }

    private void startSimulation(Settings settings) {
        dotManager = new DotManager(settings);
        simulationFrame = new SimulationFrame(settings, dotManager);
        Thread thread = new Thread(simulationStepRunnable);
        thread.start();
    }
}


