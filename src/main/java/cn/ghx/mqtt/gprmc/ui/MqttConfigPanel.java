package cn.ghx.mqtt.gprmc.ui;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class MqttConfigPanel extends BasePanel {


    public MqttConfigPanel() {

        setLayout(new MigLayout("", "0[right]0[grow]0", ""));


        add(getLabel("Host:"));
        JTextField host = new JTextField("sc.geobeans.cn");
        add(host, "wrap ,growx");

        add(getLabel("Port:"));
        JTextField port = new JTextField("1883");
        add(port, "wrap ,growx");

        add(getLabel("Username:"));
        JTextField usr = new JTextField();
        add(usr, "wrap,growx");

        add(getLabel("Password:"));
        JTextField pwd = new JTextField();
        add(pwd, "wrap,growx");

        Button conn = new Button("Connect");
        add(conn, "wrap,gapleft rel");

        add(new JSeparator(), "span,growx");
    }
}
