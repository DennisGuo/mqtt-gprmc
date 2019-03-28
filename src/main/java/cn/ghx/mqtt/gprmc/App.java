package cn.ghx.mqtt.gprmc;

import cn.ghx.mqtt.gprmc.ui.MainFrame;

import java.awt.*;


public class App {

    public static void main(String[] args) {

        EventQueue.invokeLater(()->{
            MainFrame main = new MainFrame();
            main.setVisible(true);
        });

    }
}
