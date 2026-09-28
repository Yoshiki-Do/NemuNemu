package com.example.nemunemu;

import android.annotation.SuppressLint;
import android.content.Intent;
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

public class RegisterActivity extends AppCompatActivity
    implements TextView.OnEditorActionListener, View.OnClickListener {

    private EditText userNameEditText;
    private EditText passwordEditText;
    private Button confirmButton;
    private MaterialToolbar toolbar;

    private DatabaseHelper db;

    String userNameString;
    String passwordString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userNameEditText = findViewById(R.id.userNameEditText);
        userNameEditText.setOnEditorActionListener(this);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordEditText.setOnEditorActionListener(this);
        confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(this);

        db = new DatabaseHelper(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
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
        if (id == R.id.confirmButton){
            String userNameString = userNameEditText.getText().toString().trim();
            String passwordString = passwordEditText.getText().toString().trim();

            if (userNameString.isEmpty() || passwordString.isEmpty()) {
                Toast.makeText(this, "Please fill all field.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = db.registerUser(userNameString, passwordString);
            if (success) {
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "The username already exists.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }
}