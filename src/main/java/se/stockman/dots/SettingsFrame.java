package se.stockman.dots;

import javax.swing.*;
import java.awt.*;

public class SettingsFrame extends JFrame {
    private Checkbox stickToWallCheckbox;
    private Checkbox stickToEachOtherCheckbox;
    private Checkbox stickToMiddleCheckbox;
    private JPanel boxPanel = new JPanel();
    private JButton startButton = new JButton("startButton!");
    private TextField radiusField = new TextField("4");
    private TextField dotCountField = new TextField("1000");
    private TextField windowHeightField = new TextField("500");
    private TextField windowWidthField = new TextField("800");

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public SettingsFrame() {
        super("Settings");
        init();
    }

    public void init() {

        stickToWallCheckbox = new Checkbox("Väggar, och andra stillaståend", true);
        // same but selected
        stickToEachOtherCheckbox = new Checkbox("Varann", false);
        stickToMiddleCheckbox = new Checkbox("Mittpunkten", false);
        boxPanel.setLayout(new GridLayout(3, 1));
        boxPanel.add(stickToWallCheckbox);
        boxPanel.add(stickToEachOtherCheckbox);
        boxPanel.add(stickToMiddleCheckbox);

        setLayout(new GridLayout(6, 2));
        add(new JLabel("Antal prickar:"));
        add(dotCountField);

        add(new JLabel("Radie"));
        add(radiusField);

        add(new JLabel("Fönsterhöjd"));
        add(windowHeightField);

        add(new JLabel("FönsterBredd"));
        add(windowWidthField);

        add(new JLabel("Prickarna ska fastna på:"));
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
