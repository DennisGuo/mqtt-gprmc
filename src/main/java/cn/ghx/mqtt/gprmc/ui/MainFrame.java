package cn.ghx.mqtt.gprmc.ui;

import net.miginfocom.swing.MigLayout;
import org.fusesource.mqtt.client.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;
import java.util.UUID;

/**
 * The main app frame.
 */
public class MainFrame extends JFrame {

    private static final String CHARSET = "UTF-8";

    private static final String INFO = "INF";
    private static final String ERROR = "ERR";
    private static final String WARN = "WAR";
    private static final String SUCCESS = "SUC";

    private JFileChooser _chooser;

    private JScrollPane _scroll;
    private JTextField _host;
    private JTextField _port;
    private JTextField _path;
    private JComboBox _box;
    private JButton _btnStart;
    private JTextField _topic;
    private JTextField _usr;
    private JPasswordField _pwd;
    private JSpinner _spinner;
    private JSpinner _spinnerSec;
    private JTextField _subscribe;
    private JButton _btnSubscribe;
    private JButton _btnConnect;

    private SubscribeListener _subscribeListener;
    private StartListener _startListener;



    private String _btnConstraints = "span,w 100!";

    private double[][] _data;
    private int _index;

    private String _subscribeTopic;
    private File _file;

    private BlockingConnection _connection;


    public MainFrame() {
        super("Mqtt GPRMC client.");

        initView();
        initEvent();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * init events.
     */
    private void initEvent() {
        _chooser.addActionListener((e) -> {
            if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                _file = _chooser.getSelectedFile();
                String filePath = _file.getAbsolutePath();
                message("Choose data file : " + filePath);
                _path.setText(filePath);
            }
        });
    }

    /**
     * init frame view
     */
    private void initView() {
        _chooser = new JFileChooser();
        Dimension min = new Dimension(800, 500);
        this.setSize(min);
        this.setMinimumSize(min);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);

        this.setLayout(new MigLayout("filly", "10[]10[grow]0", "0[top]0"));

        renderLeftPanel();
        renderRightPanel();

        message("Welcome to mqtt gprmc client.");
    }

    /**
     * render left configurer panel
     */
    private void renderLeftPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2,gap 5", "0[right]5[fill]0", ""));

        initMtqqConfigView(panel);
        panel.add(new JSeparator(), "span,growx");
        initSubscribeConfigView(panel);
        panel.add(new JSeparator(), "span,growx");
        initPushConfigView(panel);

        this.add(panel,"w 200!");
    }

    private void initPushConfigView(JPanel panel) {
        JLabel label = new JLabel("Topic:");
        _topic = new JTextField("/insp/put/xxx");
        label.setLabelFor(_topic);
        panel.add(label);
        panel.add(_topic,"growx");

        label = new JLabel("File Type:");
        _box = new JComboBox();
        ComboBoxModel typeModel = new FileTypeComboBoxModel();
        _box.setModel(typeModel);
        _box.setSelectedIndex(0);

        label.setLabelFor(_box);
        panel.add(label);
        panel.add(_box);

        label = new JLabel("File:");
        _path = new JTextField("/file/path");
        _path.setEnabled(false);
        label.setLabelFor(panel);
        JButton choose = new JButton("F");
        choose.addActionListener((e) -> {
            _chooser.showDialog(this, null);
        });

        panel.add(label);
        panel.add(_path,"split 2");
        panel.add(choose,"h 20!,w 20!");

        label = new JLabel("KM/H:");
        _spinner = new JSpinner();
        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setMaximum(230);
        model.setMinimum(10);
        model.setValue(70);
        _spinner.setModel(model);
        label.setLabelFor(_spinner);
        panel.add(label);
        panel.add(_spinner, "growx");

        label = new JLabel("PER/SEC:");
        _spinnerSec = new JSpinner();
        SpinnerNumberModel model2 = new SpinnerNumberModel();
        model2.setMaximum(60);
        model2.setMinimum(1);
        model2.setValue(5);
        _spinnerSec.setModel(model2);
        label.setLabelFor(_spinnerSec);
        panel.add(label);
        panel.add(_spinnerSec, "growx");

        panel.add(new JSeparator(), "span,growx");

        _startListener = new StartListener();
        _btnStart = new JButton("Start");
        _btnStart.addActionListener(_startListener);
        panel.add(_btnStart, _btnConstraints);
    }

    private void initSubscribeConfigView(JPanel panel) {

        JLabel label = new JLabel("Subscribe :");
        _subscribe = new JTextField("/insp/get/#");
        label.setLabelFor(_subscribe);
        panel.add(label);
        panel.add(_subscribe);

        _subscribeListener = new SubscribeListener();
        _btnSubscribe = new JButton("Subscribe");
        _btnSubscribe.addActionListener(_subscribeListener);
        panel.add(_btnSubscribe,_btnConstraints);

    }

    private void initMtqqConfigView(JPanel panel) {
        JLabel label = new JLabel("Host:");
        _host = new JTextField("sc.geobeans.cn");
        label.setLabelFor(_host);
        panel.add(label);
        panel.add(_host);

        label = new JLabel("Port:");
        _port = new JTextField("1883");
        label.setLabelFor(_port);
        panel.add(label);
        panel.add(_port);

        label = new JLabel("Username:");
        _usr = new JTextField();
        label.setLabelFor(_usr);
        panel.add(label);
        panel.add(_usr);

        label = new JLabel("Password:");
        _pwd = new JPasswordField();
        label.setLabelFor(_pwd);
        panel.add(label);
        panel.add(_pwd);

        _btnConnect = new JButton("Connect");
        _btnConnect.addActionListener( new ConnectListener());
        panel.add(_btnConnect,_btnConstraints);

    }

    private boolean isConnected() {
        return _connection != null && _connection.isConnected();
    }


    /**
     * render right message panel
     */
    private void renderRightPanel() {

        JPanel content = new JPanel(new MigLayout("wrap,gap 0", "10[]10", "0[top]0"));
        content.setBackground(Color.lightGray);

        _scroll = new JScrollPane(content);

        ScrollPaneLayout layout = new ScrollPaneLayout();
        _scroll.setLayout(layout);

        add(_scroll, "span 2, grow");
    }


    /**
     * show info message
     *
     * @param text message
     */
    private void message(String text) {
        message(text, INFO);
    }

    /**
     * show message on right panel
     *
     * @param text  message
     * @param level message level
     */
    private void message(String text, String level) {

        new Thread(new TimerTask() {
            @Override
            public void run() {
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                String msg = String.format("[ %s ] [ %s ] : %s", level, time, text);
                System.out.println(msg);
                JLabel label = new JLabel(msg);
                label.setFont(new Font("Serif", Font.PLAIN, 12));
                if (level.equalsIgnoreCase(ERROR)) {
                    label.setForeground(Color.RED);
                } else if (level.equalsIgnoreCase(WARN)) {
                    label.setForeground(Color.YELLOW);
                } else if (level.equalsIgnoreCase(SUCCESS)) {
                    label.setForeground(Color.GREEN);
                }
                JPanel panel = (JPanel) _scroll.getViewport().getView();
                panel.add(label, "h 20!");
                panel.validate();
                _scroll.validate();
            }
        }).start();
    }

    private class ConnectListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand(),con = "Connect",dis = "Disconnect";
            if (cmd.equalsIgnoreCase(con)) {
                String h = _host.getText().trim();
                String p = _port.getText().trim();
                String u = _usr.getText().trim();
                String w = new String(_pwd.getPassword()).trim();

                if (h.equalsIgnoreCase("")) {
                    message("Host is needed !", ERROR);
                    return;
                }
                if (p.equalsIgnoreCase("")) {
                    message("Port is needed !", ERROR);
                    return;
                }

                boolean rs = initMqtt(h, p, u, w);
                if(rs) {
                    startListenMessage();
                    _btnConnect.setText(dis);
                }

            } else if (cmd.equalsIgnoreCase(dis)) {
                if (_connection.isConnected()) {
                    try {
                        _connection.disconnect();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        message("Disconnect mqtt server error : " + ex.getMessage(), ERROR);
                        return;
                    }
                }
                stop();
                message("Disconnected .");
                _btnConnect.setText(con);
            }


        }

        private void stop() {
            _subscribeListener.unsubscribe();
            _startListener.toStart();
        }


        /**
         * init mqtt connect from input
         *
         * @param host     server _host
         * @param port     server _port
         * @param username username
         * @param password password
         */
        private boolean initMqtt(String host, String port, String username, String password) {

            try {
                String clientId = "MQTT-GPRMC-CLIENT-" + UUID.randomUUID().toString().split("-")[0];
                String uri = String.format("tcp://%s:%s", host, port);
                message(String.format("Trying to connect %s with clientId=%s", uri, clientId));

                MQTT mqtt = new MQTT();
                mqtt.setClientId(clientId);
                mqtt.setHost(new URI(uri));
                if (username != null && !"".equalsIgnoreCase(username)) {
                    mqtt.setUserName(username);
                    mqtt.setPassword(password);
                }
                mqtt.setConnectAttemptsMax(10);
                mqtt.setReconnectAttemptsMax(10);
                _connection = mqtt.blockingConnection();
                _connection.connect();
                message("Connected to mqtt server success !", SUCCESS);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message(e.getMessage(), ERROR);
                return false;
            }
        }

        private void startListenMessage() {
            new Thread(() -> {
                while (isConnected()) {
                    try {
                        Message msg = _connection.receive();
                        String t = msg.getTopic();
                        String m = new String(msg.getPayload(), CHARSET);
                        message(String.format("Receive topic=%s, payload=%s", t, m));
                        msg.ack();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        stop();
                    }
                }
            }).start();
        }
    }
    private class StartListener implements ActionListener{

        private String start = "Start",stop="Stop";

        private String _push;
        private int _kmh;
        private int _sec;
        private String _type;

        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if(cmd.equals(start)) {
                toStart();
            }else if(cmd.equalsIgnoreCase(stop)){
              toStop();
            }
        }

        void toStop() {
            stopPushInterval();
            _btnStart.setText(start);
        }

        private void toStart() {
            if (isConnected()) {
                _push = _topic.getText().trim();
                _kmh = (int) _spinner.getValue();
                _sec = (int) _spinnerSec.getValue();
                _type = (String) _box.getSelectedItem();

                if (_push.equalsIgnoreCase("")) {
                    message("Push Topic is needed !", ERROR);
                    return;
                }
                startPushInterval();
                _btnStart.setText(stop);
            }
        }

        private void stopPushInterval() {

        }

        private void startPushInterval() {

        }
    }
    private class SubscribeListener implements ActionListener{

        private String sub="Subscribe",unsub="UnSubscribe";

        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if(cmd.equalsIgnoreCase(sub)) {
               subscribe();
            }else if(cmd.equalsIgnoreCase(unsub)){
               unsubscribe();
            }
        }

        void unsubscribe() {
            if(isConnected()){
                try {
                    _connection.unsubscribe(new String[]{_subscribeTopic});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            _btnSubscribe.setText(sub);
        }

        private void subscribe() {
            if (isConnected()) {
                String pull = _subscribe.getText().trim();
                if ("".equalsIgnoreCase(pull)) {
                    message("Subscribe Topic is needed !", ERROR);
                    return;
                }
                startSubscribe(pull);
                _btnSubscribe.setText(unsub);
            }
        }

        /**
         * start to _subscribe _topic from server .
         *
         * @param topic
         */
        private void startSubscribe(String topic) {

            _subscribeTopic = topic;
            message("Trying to subscribe topic: " + topic);
            try {
                byte[] qoses = _connection.subscribe(new Topic[]{new Topic(topic, QoS.AT_LEAST_ONCE)});
            } catch (Exception e) {
                e.printStackTrace();
                message("Subscribe error: " + e.getMessage(), ERROR);
            }
        }

    }


}
