package com.edu.usc.example.placessearch;

import java.util.Comparator;

public class CompareByDefault implements Comparator<ReviewRes> {
    @Override
    public int compare(ReviewRes o1, ReviewRes o2) {
        return o1.getId()-o2.getId();
    }
}
