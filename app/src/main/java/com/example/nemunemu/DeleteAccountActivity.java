package com.example.nemunemu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class DeleteAccountActivity extends AppCompatActivity
    implements View.OnClickListener {

    private TextView userNameTextView;
    private Button deleteButton;
    private Button noButton;
    private SharedPreferences prefs;
    private String currentUser;
    private DatabaseHelper db;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delete_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userNameTextView = findViewById(R.id.userNameTextView);
        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);
        noButton = findViewById(R.id.noButton);
        noButton.setOnClickListener(this);
        prefs = getSharedPreferences("NemuNemuPrefs", MODE_PRIVATE);
        currentUser = prefs.getString("currentUser", "");
        db = new DatabaseHelper(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        userNameTextView.setText(currentUser);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.deleteButton) {
            if (currentUser != null) {
                boolean deleted = db.deleteUser(currentUser);
                if (deleted) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove("sleep_records" + currentUser);
                    editor.remove("optimal_sleep_duration" + currentUser);
                    editor.remove("currentUser");
                    editor.putBoolean("isSignedIn", false);
                    editor.apply();

                    Toast.makeText(this, "Account deleted.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(DeleteAccountActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(this, "Delete failed.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (id == R.id.noButton) {
            finish();
        }
    }
}