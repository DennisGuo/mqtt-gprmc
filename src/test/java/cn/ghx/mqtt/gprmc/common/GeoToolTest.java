package cn.ghx.mqtt.gprmc.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class GeoToolTest {


    @Test
    public void latToDdmm() {
        double lat = 39.991725;
        System.out.println(lat);
        System.out.println(GeoTool.latToDdmm(lat));
    }

    @Test
    public void lonToDddmm() {
        double lat = 116.37196;
        System.out.println(lat);
        System.out.println(GeoTool.latToDdmm(lat));
    }

    @Test
    public void convertGprmc() {
        System.out.println(GeoTool.convertGprmc(39.991725,116.37196,45,0));
    }

    @Test
    public void bearing() {
        double[] p1 = {29.79709093,106.379092862};
        double[] p2 ={ 29.806521578,106.386034419};
        System.out.println(GeoTool.bearing(p1[0],p1[1],p2[0],p2[1]));
    }

    @Test
    public void getDistance() {
        double[] p1 = {29.79709093,106.379092862};
        double[] p2 ={ 29.806521578,106.386034419};
        System.out.println(GeoTool.getDistance(p1[0],p1[1],p2[0],p2[1]));
    }

    @Test
    public void getNextPoint() {
        double[] p1 = {29.79709093,106.379092862};
        double[] p2 ={ 29.806521578,106.386034419};
        double distance = 60;

        System.out.println(Arrays.toString(GeoTool.getNextPoint(p1[0],p1[1],p2[0],p2[1],distance)));
    }
}