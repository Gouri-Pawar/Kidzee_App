package com.mgm.kidszee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText etMbNo, etPassword;
    String mbNo, password;
    Button bRegister, bLogin;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        bRegister = findViewById(R.id.bRegister);
        bLogin = findViewById(R.id.bLogin);
        etMbNo = findViewById(R.id.etMbno);
        etPassword = findViewById(R.id.etPassword);

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    public void register() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    public void login() {
        mbNo = etMbNo.getText().toString();
        password = etPassword.getText().toString();

        if (mbNo.equals("") || password.equals("")) {
            Toast.makeText(this, "Fields Compulsory", Toast.LENGTH_LONG).show();
        } else if (mbNo.length() != 10) {
            Toast.makeText(this, "Mobile No must be 10 digits", Toast.LENGTH_LONG).show();
        } else {
            sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            if (mbNo.equals(sharedpreferences.getString("mbNo", "").toString()) &&
                    password.equals(sharedpreferences.getString("password", "").toString())) {
                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            } else {
                Toast.makeText(this, "Invalid Mobile / Password", Toast.LENGTH_LONG).show();
            }
        }
    }
}