package com.edu.usc.example.placessearch;

import java.util.Comparator;

public class CompareByRateD implements Comparator<ReviewRes>{

    @Override
    public int compare(ReviewRes o1, ReviewRes o2) {
        return o2.getRate()-o1.getRate();
    }
}
