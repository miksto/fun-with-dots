package se.stockman.dots.simulation;

import se.stockman.dots.Dot;
import se.stockman.dots.computation.DotManager;
import se.stockman.dots.computation.State;
import se.stockman.dots.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class SimulationFrame extends JFrame {

    private final List<Dot> dots = new ArrayList<>();
    private Settings settings;
    private DotManager dotManager;
    private JPanel dotPanel;
    private StatsPanel statsPanel;

    SimulationFrame(Settings settings, DotManager dotManager) {
        this.settings = settings;
        this.dotManager = dotManager;
        init();
    }

    private void init() {
        dotPanel = new DotPanel();
        statsPanel = new StatsPanel();
        setLayout(new FlowLayout());
        add(statsPanel);
        add(dotPanel);
        pack();
        setVisible(true);
    }

    public void update() {
        synchronized (this) {
            dots.clear();
            dots.addAll(dotManager.getDots());
        }
        dotPanel.repaint();
        statsPanel.updateState(dotManager.getState());

    }

    static class StatsPanel extends JPanel {
        private JLabel movingCountLabel = new JLabel();
        private JLabel frozenCountLabel = new JLabel();
        private JLabel totalTimeLabel = new JLabel();
        private JLabel refreshRateLabel = new JLabel();
        private NumberFormat format = NumberFormat.getNumberInstance();
        private long t1, t2;

        StatsPanel() {
            setLayout(new GridLayout(5, 1));
            add(movingCountLabel);
            add(frozenCountLabel);
            add(totalTimeLabel);
            add(refreshRateLabel);
        }

        void updateState(State state) {
            t1 = t2;
            t2 = System.currentTimeMillis();

            movingCountLabel.setText("Moving: " + format.format(state.getMovingCount()));
            frozenCountLabel.setText("Frozen: " + format.format(state.getFrozenCount()));
            totalTimeLabel.setText("Simulation step time: " + format.format(state.getProcessingStepTime()) + " ms");
            refreshRateLabel.setText("Refresh rate: " + format.format(1000 / (t2 - t1)) + " Hz");
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(250, 300);
        }

    }

    class DotPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            synchronized (SimulationFrame.this) {
                for (Dot dot : dots) {
                    g.setColor(dot.getColor());
                    g.fillOval(dot.getX(), dot.getY(), settings.getDotRadius(), settings.getDotRadius());
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(settings.getWindowWidth(), settings.getWindowHeight());
        }
    }
}
