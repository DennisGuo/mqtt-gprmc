package cn.ghx.mqtt.gprmc.common;

import cn.ghx.mqtt.gprmc.data.LatLng;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Geo tool
 */
public class GeoTool {

    public static final Double METRES = 6371e3;

//    public static final String DDMM = "ddmm.mmmm";
//    public static final String DDDMM = "dddmm.mmmm";

    /**
     * convert lat to dd.mm format
     *
     * @param latitude
     * @return
     */
    public static String latToDdmm(Double latitude) {
        String str = Double.toString(latitude);
        String[] arr = str.split("\\.");
        String d = arr[0];
        String m = arr[1];

        double mm = Double.parseDouble("0." + m) * 60;
        DecimalFormat format = new DecimalFormat("00.0000");

        return d + format.format(mm);
    }

    /**
     * convert lng to ddm.mm format
     *
     * @param lng
     * @return
     */
    public static String lonToDddmm(Double lng) {
        String str = Double.toString(lng);
        String[] arr = str.split("\\.");
        String d = arr[0];
        String m = arr[1];

        double mm = Double.parseDouble("0." + m) * 60;
        DecimalFormat format = new DecimalFormat("00.0000");

        return d + format.format(mm);
    }


    public static String convertGprmc(double latitude, double longitude, int kmh, double bearing) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat();
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        // utc time
        format.applyPattern("hhmmss.sss");
        String utc_hms = format.format(date);
        // ddmm.mmmm
        String lat = GeoTool.latToDdmm(latitude);
        // South or North
        String y = "N";
        // dddmm.mmmm
        String lng = GeoTool.lonToDddmm(longitude);
        // East or West
        String x = "E";

        // 1.85 km/h = 1 knots
        // 1 km/h = 1 / 1.85 knots

        String speed = new DecimalFormat("000.0").format(kmh * (1 / 1.85)); // km/h to rate
        String dir = new DecimalFormat("000.0").format(bearing);
        // utc date
        format.applyPattern("ddmmyy");
        String utc_dmy = format.format(date);
        // $GPRMC,023543.00,A,2308.28715,N,11322.09875,E,0.195,,240213,,,A*78
        String toCheck = String.format("GPRMC,%s,A,%s,N,%s,E,%s,%s,%s,,,A", utc_hms, lat, lng, speed, dir, utc_dmy);
        return "$" + toCheck + "*" + getChecksum(toCheck);
    }

    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        /*
        var y = Math.sin(λ2-λ1) * Math.cos(φ2);
        var x = Math.cos(φ1)*Math.sin(φ2) -
                Math.sin(φ1)*Math.cos(φ2)*Math.cos(λ2-λ1);
        var brng = Math.atan2(y, x).toDegrees();
         */
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longitude1 = Math.toRadians(lon1);
        double longitude2 = Math.toRadians(lon2);
        double longDiff = longitude2 - longitude1; // 32.565421597242164
//        double longDiff = Math.toRadians(lon2 - lon1); // 32.565421597221075
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) -
                Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);

        return  Math.toDegrees(Math.atan2(y,x));//(Math.toDegrees(Math.atan2(y, x)) + 360) % 360;//
    }

    // get checksum of string.
    private static String getChecksum(String toCheck) {
        int checksum = 0;
        for (int i = 0; i < toCheck.length(); i++) {
            checksum ^= toCheck.codePointAt(i);
        }
        return String.format("%x", checksum);
    }


    public static double getDistance(double lat, double lng, double lat2, double lng2) {
        /*
        var R = 6371e3; // metres
        var φ1 = lat1.toRadians();
        var φ2 = lat2.toRadians();
        var Δφ = (lat2-lat1).toRadians();
        var Δλ = (lon2-lon1).toRadians();
        var a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                Math.cos(φ1) * Math.cos(φ2) *
                Math.sin(Δλ/2) * Math.sin(Δλ/2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        var d = R * c;
        */
        double a1 = Math.toRadians(lat);
        double a2 = Math.toRadians(lat2);
        double sa = Math.toRadians(lat2 - lat);
        double sy = Math.toRadians(lng2 - lng);
        double a = Math.sin(sa / 2) * Math.sin(sa / 2) +
                Math.cos(a1) * Math.cos(a2) *
                Math.sin(sy / 2) * Math.sin(sy / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return METRES * c;

    }

    /**
     *
     * @param lat
     * @param lng
     * @param lat2
     * @param lng2
     * @param distance meter
     * @return
     */
    public static double[] getNextPoint(double lat, double lng, double lat2, double lng2, double distance) {
        /*
            var φ2 = Math.asin( Math.sin(φ1)*Math.cos(d/R) +
                        Math.cos(φ1)*Math.sin(d/R)*Math.cos(brng) );
            var λ2 = λ1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(φ1),
                             Math.cos(d/R)-Math.sin(φ1)*Math.sin(φ2));
         */
//        double d = distance / 1000;
        double bearing = Math.toRadians(bearing(lat,lng,lat2,lng2));
        double latRadians = Math.toRadians(lat);
        double lngRadians = Math.toRadians(lng);
        double dm = distance / METRES;
        double latitude = Math.asin(Math.sin(latRadians) * Math.cos(dm) +
                            Math.cos(latRadians) * Math.sin(dm) * Math.cos(bearing));
        double longitude = lngRadians + Math.atan2(Math.sin(bearing) * Math.sin(dm) * Math.cos(latRadians),
                                Math.cos(dm) - Math.sin(latRadians) * Math.sin(latitude));
        return new double[]{Math.toDegrees(latitude),Math.toDegrees(longitude)};
    }
}
