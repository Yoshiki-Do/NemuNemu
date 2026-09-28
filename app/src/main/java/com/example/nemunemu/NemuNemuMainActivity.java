package com.example.nemunemu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NemuNemuMainActivity extends AppCompatActivity
    implements View.OnClickListener {

    private TextView scoreTextView;
    private ImageView nemunemuImageView;
    private Button sleepRecordButton;
    private Button histroyButton;
    private Button optimalSleepDurationButton;
    private Button accountButton;
    private Button signOutButton;
    private MaterialToolbar toolbar;
    private SharedPreferences prefs;
    private Gson gson;
    private String currentUser;
    private float optimalSleepDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nemu_nemu_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        scoreTextView = findViewById(R.id.scoreTextView);
        nemunemuImageView = findViewById(R.id.nemunemuImageView);
        sleepRecordButton = findViewById(R.id.sleepRecordButton);
        sleepRecordButton.setOnClickListener(this);
        histroyButton = findViewById(R.id.historyButton);
        histroyButton.setOnClickListener(this);
        optimalSleepDurationButton = findViewById(R.id.optimalSleepDurationButton);
        optimalSleepDurationButton.setOnClickListener(this);
        signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(this);
        accountButton = findViewById(R.id.accountButton);
        accountButton.setOnClickListener(this);

        prefs = getSharedPreferences("NemuNemuPrefs", MODE_PRIVATE);
        gson = new Gson();
        currentUser = prefs.getString("currentUser", "");
        optimalSleepDuration = prefs.getFloat("optimal_sleep_duration" + currentUser, 8f);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        calculateScore(score -> scoreTextView.setText("Score: " + score));

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.sleepRecordButton) {
            Intent intent = new Intent(NemuNemuMainActivity.this, SleepRecordActivity.class);
            startActivity(intent);
        } else if (id == R.id.historyButton) {
            Intent intent = new Intent(NemuNemuMainActivity.this, HistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.optimalSleepDurationButton) {
            Intent intent = new Intent(NemuNemuMainActivity.this, OptimalSleepDurationActivity.class);
            startActivity(intent);
        } else if (id == R.id.accountButton) {
            Intent intent = new Intent(NemuNemuMainActivity.this, AccountActivity.class);
            startActivity(intent);
        } else if (id == R.id.signOutButton) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isSignedIn", false);
            editor.remove("currentUser");
            editor.apply();
            Intent intent = new Intent(NemuNemuMainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    private void calculateScore(ScoreCallback callback) {
        String json = prefs.getString("sleep_records" + currentUser, null);
        if (json == null) {
            callback.onResult(0);
            return;
        }
        Type type = new TypeToken<HashMap<String, SleepRecord>>() {
        }.getType();
        HashMap<String, SleepRecord> map = gson.fromJson(json, type);
        Calendar calendar = Calendar.getInstance();
        List<String> last7Days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String date = String.format("%d%02d%02d",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));
            last7Days.add(date);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        getFullMoonLast7Days(fullMoonDates -> {
            float totalScore = 0;
            int count = 0;
            for (String date : last7Days) {
                if (!map.containsKey(date)) continue;
                SleepRecord record = map.get(date);
                float sleepDuration = record.getDuration();
                float finalScoreBase = sleepDuration;
                if (fullMoonDates.contains(date)) {
                    finalScoreBase *= 1.2f;
                }
                totalScore += finalScoreBase / optimalSleepDuration;
                count++;
            }
            if (count != 0) {
                int finalScore = (int) ((totalScore / count) * 100);
                runOnUiThread(() -> callback.onResult(finalScore));
            } else
                runOnUiThread(() -> callback.onResult(0));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        calculateScore(new ScoreCallback() {
            @Override
            public void onResult(int score) {
                scoreTextView.setText("Score: " + score);
                updateImageByScore(score, nemunemuImageView);
            }
        });
    }

    private void getFullMoonLast7Days(FullMoonCallback callback) {
        OkHttpClient client = new OkHttpClient();
        ArrayList<String> fullMoonDates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        final int[] done = {0};
        final String location = "40.7128,-74.0060,";
        for (int i = 0; i < 7; i++) {
            String date = String.format("%04d%02d%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH));
            String url = "https://api.solunar.org/solunar/" + location + date + "-7";
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    done[0]++;
                    checkDone();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        String body = response.body().string();
                        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                        double illumination = json.get("moonIllumination").getAsDouble();
                        if (illumination > 0.995f) {
                            synchronized (fullMoonDates) {
                                fullMoonDates.add(date);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    done[0]++;
                    checkDone();
                }

                private void checkDone() {
                    if (done[0] == 7) {
                        callback.onResult(fullMoonDates);
                    }
                }
            });
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
    }

    private void updateImageByScore(int score, ImageView nemunemuImageView) {
        if (score >= 80)
            nemunemuImageView.setImageResource(R.drawable.character_80);
        else if (score >= 60)
            nemunemuImageView.setImageResource(R.drawable.character_60);
        else if (score >= 40)
            nemunemuImageView.setImageResource(R.drawable.character_40);
        else
            nemunemuImageView.setImageResource(R.drawable.character_20);
    }
}