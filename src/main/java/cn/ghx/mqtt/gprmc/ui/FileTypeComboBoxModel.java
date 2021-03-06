package cn.ghx.mqtt.gprmc.ui;

import javax.swing.*;

public class FileTypeComboBoxModel<T extends FileTypeComboBoxModel.TYPE> extends AbstractListModel implements ComboBoxModel {

    private String selected;

    public enum TYPE {
        TXT("txt"),
        WKT("wkt"),
        GEOJSON("geojson"),
        KML("kml"),
        ;

        private String text;

        TYPE(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selected = (String) anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }

    @Override
    public int getSize() {
        return TYPE.values().length;
    }

    @Override
    public Object getElementAt(int index) {
        return TYPE.values()[index].toString();
    }

}
