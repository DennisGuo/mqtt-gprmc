package cn.ghx.mqtt.gprmc.ui;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {

    private static final String INFO = "INFO";
    private static final String ERROR = "ERROR";
    private static final String WARN = "WARN";

    private JFileChooser _chooser;

    private JScrollPane _scroll;
    private JTextField _path;

    private double[][] _data;
    private int _index;

    public MainFrame()  {
        super("Mqtt GPRMC client.");

        initView();
        initEvent();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void initEvent() {
        _chooser.addActionListener((e)->{
            if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                File file = _chooser.getSelectedFile();
                String filePath = file.getAbsolutePath();
                message("Choose data file : "+filePath);
                _path.setText(filePath);
            }
        });
    }


    private void initView() {
        _chooser = new JFileChooser();

        setSize(800, 400);
        setLayout(new MigLayout("filly","10[]10[grow]0","0[top]0"));


        renderLeftPanel();
        renderRightPanel();

        message("Welcome to mqtt gprmc client.");
    }


    private void renderLeftPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2,gap 0","0[right]0[fill]0",""));


        JLabel label = new JLabel("Host:");
        JTextField host = new JTextField("sc.geobeans.cn");
        label.setLabelFor(host);
        panel.add(label);
        panel.add(host);

        label = new JLabel("Port:");
        JTextField port = new JTextField("1883");
        label.setLabelFor(port);
        panel.add(label);
        panel.add(port);

        label = new JLabel("Username:");
        JTextField usr = new JTextField();
        label.setLabelFor(usr);
        panel.add(label);
        panel.add(usr);

        label = new JLabel("Password:");
        JTextField pwd = new JTextField();
        label.setLabelFor(pwd);
        panel.add(label);
        panel.add(pwd);

        panel.add(new JSeparator(), "span,growx");

        label = new JLabel("Topic:");
        JTextField topic = new JTextField("/insp/put/xxx");
        label.setLabelFor(topic);
        panel.add(label);
        panel.add(topic);

        label = new JLabel("File:");
        _path = new JTextField("*.wkt,*.txt,*.json");
        label.setLabelFor(panel);
        JButton choose = new JButton("Choose");
        choose.addActionListener((e)->{
            if(e.getActionCommand().equalsIgnoreCase("Choose")){
                _chooser.showDialog(this,null);
            }
        });

        panel.add(label);
        panel.add(_path,"split 2");
        panel.add(choose,"w 80!");

        label = new JLabel("Per/Second:");
        JSpinner spinner = new JSpinner();

        label.setLabelFor(spinner);
        panel.add(label);
        panel.add(spinner,"w 60!");

        panel.add(new JSeparator(), "span,growx");

        Button conn = new Button("Start");
        conn.addActionListener(e->{
            String cmd =e.getActionCommand();
            if(cmd.equalsIgnoreCase("Start")){
                String h = host.getText();
                String p = port.getText();
                String u = usr.getText();
                String w = pwd.getText();

                message(String.format("Try Connecting tcp://%s:%s",h,p));



                conn.setLabel("Cancel");
            }else if(cmd.equalsIgnoreCase("Cancel")){
                message("Cancel.");
                conn.setLabel("Start");
            }
        });
        panel.add(conn,"span , right,w 80!");

        add(panel);
    }



    private void renderRightPanel() {


        JPanel content = new JPanel(new MigLayout("wrap,gap 0", "10[]10", "0[top]0"));
        content.setBackground(Color.lightGray);

        _scroll = new JScrollPane(content);

        ScrollPaneLayout layout = new ScrollPaneLayout();
        _scroll.setLayout(layout);

        add(_scroll,"span 2, grow");
    }


    private void message(String text){
        message(text,INFO);
    }
    private void message(String text,String level){
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        JLabel label = new JLabel(String.format("[ %s ] [ %s ] : %s",level,time,text));
        label.setFont(new Font("Serif",Font.PLAIN,12));
        if(level.equalsIgnoreCase(ERROR)){
            label.setForeground(Color.RED);
        }else if(level.equalsIgnoreCase(WARN)){
            label.setForeground(Color.YELLOW);
        }
        JPanel panel = (JPanel) _scroll.getViewport().getView();
        panel.add(label,"h 20!");
        panel.validate();
    }

}
