package com.example.nemunemu;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class HistoryActivity extends AppCompatActivity
implements View.OnClickListener {
    private BarChart historyBarChart;
    private Button backButton;
    private SharedPreferences prefs;
    private Gson gson;
    private String currentUser;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);

        prefs = getSharedPreferences("NemuNemuPrefs", MODE_PRIVATE);
        gson = new Gson();
        currentUser = prefs.getString("currentUser", "");

        historyBarChart = findViewById(R.id.historyBarChart);
        ChartData data = getChartData();
        ArrayList<Integer> colors = new ArrayList<>();
        //modify x-axis
        XAxis xAxis = historyBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(data.labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        //modify y-axis
        YAxis yAxis = historyBarChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(12f);
        yAxis.setGranularity(1f);
        yAxis.setTextColor(Color.WHITE);
        historyBarChart.getAxisRight().setEnabled(false);
        //limit line
        float optimalSleep = prefs.getFloat("optimal_sleep_duration" + currentUser, 8f);
        LimitLine limitLine = new LimitLine(optimalSleep, "Optimal Sleep");
        limitLine.setLineColor(Color.GREEN);
        limitLine.setLineWidth(2f);
        limitLine.setTextColor(Color.GREEN);
        limitLine.setTextSize(12f);
        limitLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        yAxis.addLimitLine(limitLine);

        for (BarEntry entry : data.entries) {
            if (entry.getY() >= optimalSleep) {
                colors.add(Color.CYAN);
            } else {
                colors.add(Color.GRAY);
            }
        }
        historyBarChart.getLegend().setEnabled(false);
        BarDataSet dataset = new BarDataSet(data.entries, "Sleep Hours");
        dataset.setValueTextColor(Color.WHITE);
        dataset.setValueTextSize(12f);
        dataset.setColors(colors);
        historyBarChart.setData(new BarData(dataset));
        historyBarChart.invalidate();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private ChartData getChartData() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        String json = prefs.getString("sleep_records" + currentUser, null);
        HashMap<String, SleepRecord> map = new HashMap<>();
        if (json != null) {
            Type type = new TypeToken<HashMap<String, SleepRecord>>() {
            }.getType();
            map = gson.fromJson(json, type);
            if (map == null)
                map = new HashMap<>();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -6);

        for (int i = 0; i < 7; i++) {
            String date = String.format("%d%02d%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            labels.add(String.format("%02d-%02d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
            float duration = 0;
            if (map.containsKey(date)) {
                duration = map.get(date).getDuration();
            }
            entries.add(new BarEntry(i, duration));

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return new ChartData(entries, labels);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.backButton) {
            finish();
        }
    }
}