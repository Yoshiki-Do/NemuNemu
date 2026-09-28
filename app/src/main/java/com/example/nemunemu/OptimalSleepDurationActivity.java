package com.example.nemunemu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class OptimalSleepDurationActivity extends AppCompatActivity
    implements View.OnClickListener {

    private Spinner optimalSleepDurationSpinner;
    private Button saveButton;
    private MaterialToolbar toolbar;
    private SharedPreferences prefs;
    private String currentUser;
    private int savedDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_optimal_sleep_duration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        optimalSleepDurationSpinner = findViewById(R.id.optimalSleepDurationSpinner);
        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);
        prefs = getSharedPreferences("NemuNemuPrefs", MODE_PRIVATE);
        currentUser = prefs.getString("currentUser", "");
        savedDuration = (int) (prefs.getFloat("optimal_sleep_duration" + currentUser, 8f) / 0.5f) - 6;
        optimalSleepDurationSpinner.setSelection(savedDuration);

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
        if (id == R.id.saveButton) {
            float newDuration = optimalSleepDurationSpinner.getSelectedItemPosition() * 0.5f + 3f;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat("optimal_sleep_duration" + currentUser, newDuration);
            editor.apply();

            Toast.makeText(this, "Duration saved.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}