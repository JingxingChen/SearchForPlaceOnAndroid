package com.edu.usc.example.placessearch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class CompareByTimeI implements Comparator<ReviewRes> {

    @Override
    public int compare(ReviewRes o1, ReviewRes o2) {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date1 = null;
        Date date2 = null;

        try {
            date1 = ft.parse(o1.getTime());
            date2 = ft.parse(o2.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1.compareTo(date2);
    }
}