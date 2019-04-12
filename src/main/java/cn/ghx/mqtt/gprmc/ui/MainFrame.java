package cn.ghx.mqtt.gprmc.ui;

import cn.ghx.mqtt.gprmc.common.GeoTool;
import cn.ghx.mqtt.gprmc.data.LatLng;
import net.miginfocom.swing.MigLayout;
import org.fusesource.mqtt.client.*;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The main app frame.
 */
public class MainFrame extends JFrame {

    private static final String CHARSET = "UTF-8";

    private static final String INFO = "INF";
    private static final String ERROR = "ERR";
    private static final String WARN = "WRN";
    private static final String SUCCESS = "SUC";

    private JFileChooser _chooser;

    private JPanel _textArea;
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

    private String _subscribeTopic;
    private File _file;

    private BlockingConnection _connection;

    private ThreadPoolExecutor _pool;


    public MainFrame() {
        super("Mqtt GPRMC client.");

        _pool = new ThreadPoolExecutor(5, 10, 6000, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));

        initView();
        initEvent();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setVisible(true);
    }

    /**
     * init events.
     */
    private void initEvent() {
        _chooser.addActionListener((e) -> {
            if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                File file = _chooser.getSelectedFile();
                String filePath = file.getAbsolutePath();
                message("Choose data file : " + filePath);

                _file = file;
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
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

        this.setLayout(new MigLayout("filly", "10[]10[grow]0", "0[top]0"));

        renderLeftPanel();
        renderRightPanel();

        message("Welcome to mqtt gprmc client.");
    }

    /**
     * render left configurer panel
     */
    private void renderLeftPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2,gap 5", "0[right]5[fill,grow]0", ""));

        initMtqqConfigView(panel);
        panel.add(new JSeparator(), "span,growx");
        initSubscribeConfigView(panel);
        panel.add(new JSeparator(), "span,growx");
        initPushConfigView(panel);
        // clear log
        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e->{
            if(e.getActionCommand().equalsIgnoreCase("Clear")){
                _textArea.removeAll();
                message("Clear all log message success! ",SUCCESS);
            }
        });
        btnClear.setBackground(Color.lightGray);
        panel.add(btnClear, _btnConstraints);

        this.add(panel, "w 200!");
    }

    private void initPushConfigView(JPanel panel) {
        JLabel label = new JLabel("Topic:");
        _topic = new JTextField("/insp/get/TESTCQ01");
        label.setLabelFor(_topic);
        panel.add(label);
        panel.add(_topic, "growx");

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
        panel.add(_path, "split 2");
        panel.add(choose, "h 20!,w 20!");

        label = new JLabel("KM/H:");
        _spinner = new JSpinner();
        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setMaximum(230);
        model.setMinimum(10);
        model.setValue(70);
        _spinner.setModel(model);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(_spinner, "##.#");
        ((NumberFormatter) editor.getTextField().getFormatter()).setAllowsInvalid(false);
        _spinner.setEditor(editor);
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
        JSpinner.NumberEditor numberEditor = new JSpinner.NumberEditor(_spinnerSec, "#");
        ((NumberFormatter) numberEditor.getTextField().getFormatter()).setAllowsInvalid(false);
        _spinnerSec.setEditor(numberEditor);


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
        panel.add(_btnSubscribe, _btnConstraints);

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
        _btnConnect.addActionListener(new ConnectListener());
        panel.add(_btnConnect, _btnConstraints);

    }

    private boolean isConnected() {
        return _connection != null && _connection.isConnected();
    }


    /**
     * render right message panel
     */
    private void renderRightPanel() {

        _textArea = new JPanel();
        _textArea.setLayout(new MigLayout("wrap,gap 0","0[fill]0"));
        _textArea.setBackground(Color.lightGray);

        JScrollPane panel = new JScrollPane(_textArea);
        add(panel, "span 2, grow");
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

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                String msg = String.format("[%s][%s]: %s", level, time, text);
                System.out.println(msg);

                Color color = Color.BLACK;
                if (level.equalsIgnoreCase(ERROR)) {
                    color = Color.RED;
                } else if (level.equalsIgnoreCase(WARN)) {
                    color =Color.YELLOW;
                } else if (level.equalsIgnoreCase(SUCCESS)) {
                    color = Color.BLUE;
                }

                JTextPane txt = new JTextPane();
                txt.setFont(new Font(Font.MONOSPACED,Font.PLAIN,10));
                txt.setText(msg);
                txt.setForeground(color);
                txt.setBackground(null);
                txt.setBorder(null);

                _textArea.add(txt);
                _textArea.scrollRectToVisible(new Rectangle(0,0,10,10));
            }
        });
    }

    /**
     * Connect Button Listener
     */
    private class ConnectListener implements ActionListener {

        private static final String con = "Connect", dis = "Disconnect";
        private ReceiveWorker _worker;

        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equalsIgnoreCase(con)) {
                toConnect();

            } else if (cmd.equalsIgnoreCase(dis)) {
                toDisconnect();
            }
        }

        private void toDisconnect() {
            _worker.stop();
            if (_connection.isConnected()) {
                try {
                    _connection.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    message("Disconnect mqtt server error : " + ex.getMessage(), ERROR);
                    return;
                }
            }

            message("Disconnected .", WARN);
            _btnConnect.setText(con);
        }

        private void toConnect() {
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
            if (rs) {
                startListenMessage();
                _btnConnect.setText(dis);
            }
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
                String clientId = "mqtt-gprmc-client-" + UUID.randomUUID().toString().split("-")[0];
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

        // always listening message
        private void startListenMessage() {
            (_worker = new ReceiveWorker()).execute();
        }
    }

    /**
     * Start Button Listener
     */
    private class StartListener implements ActionListener {

        private static final String start = "Start", stop = "Stop";

        private String _push;
        private int _kmh;
        private int _sec;
        private String _type;

        private java.util.List<LatLng> _data = new ArrayList<>();


        private PushWorker _pushWorker;


        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals(start)) {
                toStart();
            } else if (cmd.equalsIgnoreCase(stop)) {
                toStop();
            }
        }

        void toStop() {
            stopPushInterval();
            message("Pushing message progress is stopped.", WARN);
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
                if (_file == null) {
                    message("Please choose data file first.", ERROR);
                    return;
                } else {
                    // parse file to verify the
                    boolean rs = parseFileData();
                    if (!rs) {
                        return;
                    }
                }
                startPushInterval();
                _btnStart.setText(stop);
            } else {
                message("Server is not connected.", WARN);
            }
        }

        private boolean parseFileData() {
            try {
                if (_type.equalsIgnoreCase(FileTypeComboBoxModel.TYPE.TXT.toString())) {
                    return parseTextData();
                } else if (_type.equalsIgnoreCase(FileTypeComboBoxModel.TYPE.KML.toString())) {
                    message("KML file type not supported yet.", WARN);
                    // TODO : support kmz and kml
                } else if (_type.equalsIgnoreCase(FileTypeComboBoxModel.TYPE.WKT.toString())) {
                    message("WKT file type not supported yet.", WARN);
                    // TODO : support wkt
                } else if (_type.equalsIgnoreCase(FileTypeComboBoxModel.TYPE.GEOJSON.toString())) {
                    message("GEOJSON file type not supported yet.", WARN);
                    // TODO: support geojson
                }
            } catch (Exception e) {
                message("Parse data file error: " + e.getLocalizedMessage(), ERROR);
                e.printStackTrace();
            }
            return false;
        }

        private boolean parseTextData() throws Exception {
            java.util.List<LatLng> items = new ArrayList<>();
            FileReader reader = new FileReader(_file);
            BufferedReader bfReader = new BufferedReader(reader);
            String tmp;
            while ((tmp = bfReader.readLine()) != null) {
                String[] arr = tmp.split(",");
                if (arr.length >= 2) {
                    items.add(new LatLng(Double.parseDouble(arr[0]), Double.parseDouble(arr[1])));
                }
            }
            if (items.size() == 0) {
                message("Data file contains no latitude and longitude data.", ERROR);
            } else if (items.size() == 1) {
                message("Data file must contain at least two latitude and longitude data.", ERROR);
            } else {
                _data.addAll(items);
                return true;
            }
            return false;
        }

        private void stopPushInterval() {
            if(_pushWorker != null) {
                _pushWorker.stop();
            }
        }

        /**
         * start to simulate gps data and push to server.
         */
        private void startPushInterval() {
            (_pushWorker = new PushWorker(_data,_push,_kmh,_sec)).execute();
        }


    }

    /**
     * Subscribe Button Listener
     */
    private class SubscribeListener implements ActionListener {

        private String sub = "Subscribe", unsub = "UnSubscribe";

        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equalsIgnoreCase(sub)) {
                subscribe();
            } else if (cmd.equalsIgnoreCase(unsub)) {
               unsubscribe();
            }
        }

        void unsubscribe() {
            if (isConnected()) {
                try {
                    _connection.unsubscribe(new String[]{_subscribeTopic});
                    message("UnSubscribe success !", WARN);
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
            } else {
                message("Server is not connected.", WARN);
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
                message("Subscribe result: " + new String(qoses, CHARSET));
            } catch (Exception e) {
                e.printStackTrace();
                message("Subscribe error: " + e.getMessage(), ERROR);
            }
        }

    }

    // Receive message worker
    private class ReceiveWorker extends SwingWorker{

        @Override
        protected Object doInBackground() throws Exception {
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
            return null;
        }

        private void stop() {
            _subscribeListener.unsubscribe();
            _startListener.toStop();
            this.cancel(true);
        }
    }

    // Push data workder
    private class PushWorker extends SwingWorker{

        private Timer _timer;
        private LatLng _pre;

        private java.util.List<LatLng> _data = new ArrayList<>();
        private String _push;
        private int _kmh;
        private int _sec;
        private int _index;

        /**
         *
         * @param _data data
         * @param _push topic
         * @param _kmh  speed km/h
         * @param _sec  send interval second.
         */
        PushWorker(List<LatLng> _data, String _push, int _kmh, int _sec) {
            this._data = _data;
            this._push = _push;
            this._kmh = _kmh;
            this._sec = _sec;
        }

        @Override
        protected Object doInBackground() throws Exception {
            _timer = new Timer();
            message("Starting to push message.");
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // compute the new lat lng by time and speed.

                    LatLng item = null;
                    if (_pre == null) {
                        item = _data.get(0);
                        _index = 0;
                    } else if (_index < _data.size()-1) {
                        // todo : find next point to send.
                        item = getNextPoint();
                    } else {
                        // send over
                        message("Data send over , stopping .",WARN);
                        stop();
                        return;
                    }
                    if (item != null) {
                        send(item);
                    } else {
                        message("Can not get next point , stopping .",ERROR);
                        stop();
                    }
                }
            };
            _timer.schedule(task, 0,_sec * 1000);
            return null;
        }

        void stop() {
            if(_timer != null) {
                _timer.cancel();
                _timer = null;
                _startListener.toStop();
                this.cancel(true);

            }
        }

        private void send(LatLng latLng) {
            message("Send point : "+latLng);
            try {
                double bearing = 0;
                if (_pre != null) {
                    // get direction from two point;
                    bearing = GeoTool.bearing(_pre.getLat(), _pre.getLng(), latLng.getLat(), latLng.getLng());
                }
                String gprmc = GeoTool.convertGprmc(latLng.getLat(), latLng.getLng(), _kmh, bearing);
                _connection.publish(_push, gprmc.getBytes(CHARSET), QoS.EXACTLY_ONCE, false);
                message(String.format("Send topic=%s, payload=%s", _push, gprmc));
                _pre = latLng;
            } catch (Exception e) {
                e.printStackTrace();
                message("Send msg error: " + e.getLocalizedMessage(), ERROR);
            }
        }

        private LatLng getNextPoint() {
            double distance = _sec * ( (_kmh * 1000) / (60 * 60));
            LatLng tmp = _pre;
            double disTmp = 0;
            for (int i = _index; i < _data.size()-1; i++) {
//                if (i + 1 < _data.size()) {
                    LatLng next = _data.get(i + 1);
                    double dis = GeoTool.getDistance(tmp.getLat(), tmp.getLng(), next.getLat(), next.getLng());
                    disTmp += dis;
                    if (disTmp < distance) {
                        tmp = next;
                    } else {
                        _index = i;
                        double[] rs = GeoTool.getNextPoint(tmp.getLat(), tmp.getLng(), next.getLat(), next.getLng(), distance - (disTmp - dis));
                        return new LatLng(rs[0], rs[1]);
                    }
//                } else {
//                    _index = _data.size() - 1;
//                    return _data.get(_index);
//                }
            }
            _index = _data.size() - 1;
            return _data.get(_index);
        }
    }
}
