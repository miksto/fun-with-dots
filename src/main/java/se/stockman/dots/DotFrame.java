package se.stockman.dots;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class DotFrame extends JFrame {

    private Settings settings;
    private DotManager dotManager;
    private JPanel dotPanel;
    private final List<Dot> dots = new ArrayList<>();

    DotFrame(Settings settings, DotManager dotManager) {
        this.settings = settings;
        this.dotManager = dotManager;
        init();
    }

    private void init() {
        dotPanel = new MyPanel();
        add(dotPanel);
        pack();
        setVisible(true);
    }

    public void repaintDots() {
        synchronized (this) {
            dots.clear();
            dots.addAll(dotManager.getFrozenDots());
            dots.addAll(dotManager.getMovingDots());
        }
        dotPanel.repaint();
    }

    class MyPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            synchronized (DotFrame.this) {
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
