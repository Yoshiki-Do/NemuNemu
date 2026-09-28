package com.example.nemunemu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class SigninActivity extends AppCompatActivity
    implements TextView.OnEditorActionListener, View.OnClickListener {

    private EditText userNameEditText;
    private EditText passwordEditText;
    private Button signinButton;
    private MaterialToolbar toolbar;
    private DatabaseHelper db;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userNameEditText = findViewById(R.id.userNameEditText);
        userNameEditText.setOnEditorActionListener(this);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordEditText.setOnEditorActionListener(this);
        signinButton = findViewById(R.id.signinButton);
        signinButton.setOnClickListener(this);

        db = new DatabaseHelper(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefs = getSharedPreferences("NemuNemuPrefs", MODE_PRIVATE);
        boolean isSignedIn = prefs.getBoolean("isSignedIn", false);

        if (isSignedIn) {
            Intent intent = new Intent(SigninActivity.this, NemuNemuMainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.signinButton) {

            String userNameString = userNameEditText.getText().toString().trim();
            String passwordString = passwordEditText.getText().toString().trim();

            if (userNameString.isEmpty() || passwordString.isEmpty()) {
                Toast.makeText(this, "Please enter all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = db.checkUser(userNameString, passwordString);

            if (success) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isSignedIn", true);
                editor.putString("currentUser", userNameString);
                editor.apply();

                Toast.makeText(this, "Sign in successfully.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(SigninActivity.this, NemuNemuMainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }
}