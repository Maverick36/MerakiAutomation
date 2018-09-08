package com.meraki.automation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by miguelmorales on 2/2/16.
 */
public class DateTimeConverter {


    public String convertLocalTimeToUTC(String p_localDateTime) throws Exception {

        String dateFormateInUTC = "";
        Date localDate = null;
        //String localTimeZone ="";
        SimpleDateFormat formatter;
        SimpleDateFormat parser;
        //localTimeZone = saleTimeZone;

        //create a new Date object using the timezone of the specified city
        parser = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        parser.setTimeZone(TimeZone.getTimeZone("EST"));
        localDate = parser.parse(p_localDateTime);
        formatter = new SimpleDateFormat("yyy-MM-dd HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("EST"));
        //System.out.println("convertLocalTimeToUTC: "+saleTimeZone+": "+" The Date in the local time zone " +   formatter.format(localDate));

        //Convert the date from the local timezone to UTC timezone
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormateInUTC = formatter.format(localDate);
        //System.out.println("convertLocalTimeToUTC: "+saleTimeZone+": "+" The Date in the UTC time zone " +  dateFormateInUTC);

        return dateFormateInUTC;
    }


}

