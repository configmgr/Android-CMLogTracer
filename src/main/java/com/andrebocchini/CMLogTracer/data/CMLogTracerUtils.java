package com.andrebocchini.CMLogTracer.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * User: Andre Bocchini
 * Date: 11/11/13
 * Time: 3:32 PM
 */
public class CMLogTracerUtils {

    public static Date dateFromDateTimeString(String dateTimeString) throws ParseException {
        String dateTimeStringPattern;
        Date date;
        SimpleDateFormat dateFormatter;

        dateTimeStringPattern = "HH:mm:ss.SSS MM-dd-yyy";
        dateFormatter = new SimpleDateFormat(dateTimeStringPattern);
        date = dateFormatter.parse(dateTimeString);

        return date;
    }

    public static String stringFromDateTime(Date dateTime) throws ParseException {
        String dateTimeString;
        DateFormat dateFormatter;

        dateFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
        dateTimeString = dateFormatter.format(dateTime);

        return dateTimeString;
    }
}
