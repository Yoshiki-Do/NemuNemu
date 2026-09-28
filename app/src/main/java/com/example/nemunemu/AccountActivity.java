package com.example.nemunemu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class AccountActivity extends AppCompatActivity
    implements View.OnClickListener {

    private TextView userNameTextView;
    private Button signOutButton;
    private Button deleteAccountButton;
    private SharedPreferences prefs;
    private String currentUser;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userNameTextView = findViewById(R.id.userNameTextView);
        signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(this);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
        deleteAccountButton.setOnClickListener(this);

        prefs = getSharedPreferences("NemuNemuPrefs", MODE_PRIVATE);
        currentUser = prefs.getString("currentUser", "GUEST");
        userNameTextView.setText("User: " + currentUser);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.signOutButton) {
            SharedPreferences prefs = getSharedPreferences("NemuNemuPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putBoolean("isSignedIn", false);
            editor.remove("currentUser");
            editor.apply();
            Intent intent = new Intent(AccountActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.deleteAccountButton) {
            Intent intent = new Intent(AccountActivity.this, DeleteAccountActivity.class);
            startActivity(intent);
        }
    }
}