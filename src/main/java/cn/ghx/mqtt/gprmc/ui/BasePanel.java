package cn.ghx.mqtt.gprmc.ui;

import javax.swing.*;
import java.awt.*;

public class BasePanel extends JPanel {

    protected JLabel getLabel(String text) {
        JLabel label = new JLabel(text);
//        label.setFont(new Font("Serif", Font.PLAIN, 14));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }
}
