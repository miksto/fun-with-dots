package se.stockman.dots.settings;

import javax.swing.*;
import java.awt.*;

public class SettingsFrame extends JFrame {
    private Checkbox stickToWallCheckbox;
    private Checkbox stickToEachOtherCheckbox;
    private Checkbox stickToMiddleCheckbox;
    private JPanel boxPanel = new JPanel();
    private JButton startButton = new JButton("Start!");
    private TextField radiusField = new TextField("4");
    private TextField dotCountField = new TextField("10000");
    private TextField windowHeightField = new TextField("800");
    private TextField windowWidthField = new TextField("1200");

    private Listener listener;

    public SettingsFrame() {
        super("Settings");
        init();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void init() {

        stickToWallCheckbox = new Checkbox("Walls", true);
        // same but selected
        stickToEachOtherCheckbox = new Checkbox("Any other dot", false);
        stickToMiddleCheckbox = new Checkbox("A dot in the middle", false);
        boxPanel.setLayout(new GridLayout(3, 1));
        boxPanel.add(stickToWallCheckbox);
        boxPanel.add(stickToEachOtherCheckbox);
        boxPanel.add(stickToMiddleCheckbox);

        setLayout(new GridLayout(6, 2));
        add(new JLabel("Number of dots"));
        add(dotCountField);

        add(new JLabel("Dot raduis"));
        add(radiusField);

        add(new JLabel("Window height"));
        add(windowHeightField);

        add(new JLabel("Window width"));
        add(windowWidthField);

        add(new JLabel("Dots stick to"));
        add(boxPanel);

        startButton.addActionListener(e -> listener.onStartClicked(getSettings()));
        add(startButton);

        pack();
        setVisible(true);
    }

    private Settings getSettings() {
        Settings settings = new Settings();

        settings.setDotRadius(Integer.parseInt(radiusField.getText()));
        settings.setDotCount(Integer.parseInt(dotCountField.getText()));
        settings.setWindowHeight(Integer.parseInt(windowHeightField.getText()));
        settings.setWindowWidth(Integer.parseInt(windowWidthField.getText()));
        settings.setStickToWall(stickToWallCheckbox.getState());
        settings.setStickToEachOther(stickToEachOtherCheckbox.getState());
        settings.setStickToMiddle(stickToMiddleCheckbox.getState());

        return settings;
    }

    public interface Listener {
        void onStartClicked(Settings settings);
    }
}
