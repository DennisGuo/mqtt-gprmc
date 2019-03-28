package cn.ghx.mqtt.gprmc.ui;

import com.sun.org.apache.xml.internal.security.utils.JDKXPathAPI;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class TopicAndDataPanel extends BasePanel {

    public TopicAndDataPanel() {

        setLayout(new MigLayout("","0[right]0[grow]0","0[]0[]0"));

        add(getLabel("Push topic:"));
        JTextField host = new JTextField("/insp/put/xxx");
        add(host,"wrap,w 120!");

        add(getLabel("Data File:"));
        JButton choose = new JButton("Choose");
        add(choose);

    }
}
