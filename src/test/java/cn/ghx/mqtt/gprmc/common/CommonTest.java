package cn.ghx.mqtt.gprmc.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@RunWith(JUnit4.class)
public class CommonTest {
    @Test
    public void parseGMTDate() {
        SimpleDateFormat dateFormatLocal = new SimpleDateFormat("hhmmss.sss");
        dateFormatLocal.setTimeZone(TimeZone.getTimeZone("GMT"));
        String utc = dateFormatLocal.format(new Date());
        System.out.println(utc);
    }

    @Test
    public void doubleToString(){
        System.out.println(new Double(00212.23).toString());
    }

    @Test
    public void charVal(){
        String str = "GPRMC,082300.000,A,3959.5035,N,11622.3176,E,024.3,000.0,092319,,,A";

        int checksum = 0;
        for(int i=0;i<str.length();i++){
            checksum ^= str.codePointAt(i);
        }
        Assert.assertEquals("62",String.format("%x",checksum));
        //Assert.assertEquals(68,checksum);
    }

}
