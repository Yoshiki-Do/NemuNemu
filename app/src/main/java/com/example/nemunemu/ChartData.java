package com.example.nemunemu;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class ChartData {
    public ArrayList<BarEntry> entries;
    public ArrayList<String> labels;

    public ChartData(ArrayList<BarEntry> entries, ArrayList<String> labels) {
        this.entries = entries;
        this.labels = labels;
    }
}
