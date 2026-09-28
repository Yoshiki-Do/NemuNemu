package com.example.nemunemu;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class SleepRecordActivity extends AppCompatActivity
    implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private TextView dateTextView;
    private Spinner sleepHourSpinner, sleepMinuteSpinner, wakeHourSpinner, wakeMinuteSpinner;
    private Button dateSelectButton, recordButton;
    private MaterialToolbar toolbar;
    private SharedPreferences prefs;
    private Gson gson;
    private String currentUser;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sleep_record);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dateTextView = findViewById(R.id.dateTextView);
        sleepHourSpinner = findViewById(R.id.sleepHourSpinner);
        sleepMinuteSpinner = findViewById(R.id.sleepMinuteSpinner);
        wakeHourSpinner = findViewById(R.id.wakeHourSpinner);
        wakeMinuteSpinner = findViewById(R.id.wakeMinuteSpinner);
        dateSelectButton = findViewById(R.id.dateSelectButton);
        dateSelectButton.setOnClickListener(this);
        recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(this);

        prefs = getSharedPreferences("NemuNemuPrefs", MODE_PRIVATE);
        gson = new Gson();
        currentUser = prefs.getString("currentUser", "");
        Calendar calendar = Calendar.getInstance();
        date = String.format("%d%02d%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        dateTextView.setText(date);

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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.dateSelectButton) {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(SleepRecordActivity.this, SleepRecordActivity.this,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            long now = System.currentTimeMillis();
            long oneWeeksAgo = now - (6L * 24 * 60 * 60 * 1000);

            dialog.getDatePicker().setMinDate(oneWeeksAgo);
            dialog.getDatePicker().setMaxDate(now);

            dialog.show();
        } else if (id == R.id.recordButton) {
            if (saveRecord())
                finish();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = String.format("%d%02d%02d", year, month + 1, dayOfMonth);

        dateTextView.setText(date);
        loadSavedRecord(date);
    }

    private float calculateDuration(int sleepHour, int sleepMin, int wakeHour, int wakeMin) {
        int sleepTime = sleepHour * 60 + sleepMin;
        int wakeTime = wakeHour * 60 + wakeMin;

        if (wakeTime < sleepTime)
            wakeTime += 24 * 60;

        return (wakeTime - sleepTime) / 60f;
    }

    private boolean saveRecord() {
        String date = dateTextView.getText().toString();
        if (date.isEmpty()) {
            Toast.makeText(this, "Date is not selected.", Toast.LENGTH_SHORT).show();
            return false;
        }

        int sleepHour = Integer.parseInt(sleepHourSpinner.getSelectedItem().toString());
        int sleepMin = Integer.parseInt(sleepMinuteSpinner.getSelectedItem().toString());
        int wakeHour = Integer.parseInt(wakeHourSpinner.getSelectedItem().toString());
        int wakeMin = Integer.parseInt(wakeMinuteSpinner.getSelectedItem().toString());

        float duration = calculateDuration(sleepHour, sleepMin, wakeHour, wakeMin);

        String sleepTime = String.format("%02d:%02d", sleepHour, sleepMin);
        String wakeTime = String.format("%02d:%02d", wakeHour, wakeMin);

        SleepRecord record = new SleepRecord(date, sleepTime, wakeTime, duration);

        String json = prefs.getString("sleep_records" + currentUser, null);
        Type type = new TypeToken<HashMap<String, SleepRecord>>() {
        }.getType();

        HashMap<String, SleepRecord> map = json == null ? new HashMap<>() : gson.fromJson(json, type);

        long now = System.currentTimeMillis();
        long oneWeeksAgo = now - (7L * 24 * 60 * 60 * 1000);
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                Date d = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(key);
                if (d != null && d.getTime() < oneWeeksAgo) {
                    iterator.remove();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        map.put(date, record);

        prefs.edit().putString("sleep_records" + currentUser, gson.toJson(map)).apply();

        Toast.makeText(this, "Recorded.", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void loadSavedRecord(String date) {
        String json = prefs.getString("sleep_records" + currentUser, null);
        if (json == null)
            return;

        Type type = new TypeToken<HashMap<String, SleepRecord>>() {
        }.getType();
        HashMap<String, SleepRecord> map = gson.fromJson(json, type);

        if (map.containsKey(date)) {
            SleepRecord record = map.get(date);

            String[] sleep = record.getSleepTime().split(":");
            String[] wake = record.getWakeTime().split(":");

            sleepHourSpinner.setSelection(Integer.parseInt(sleep[0]));
            sleepMinuteSpinner.setSelection(Integer.parseInt(sleep[1]) / 10);
            wakeHourSpinner.setSelection(Integer.parseInt(wake[0]));
            wakeMinuteSpinner.setSelection(Integer.parseInt(wake[1]) / 10);

            Toast.makeText(this, "Records loaded.", Toast.LENGTH_SHORT).show();
        }
    }
}