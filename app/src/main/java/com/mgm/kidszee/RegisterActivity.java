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

public class RegisterActivity extends AppCompatActivity {
    EditText etName, etMbNo, etPassword;
    String name, mbNo, password;
    Button bRegister, bLogin;

    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        bRegister = findViewById(R.id.bRegister);
        bLogin = findViewById(R.id.bLogin);
        etName = findViewById(R.id.etName);
        etMbNo = findViewById(R.id.etMbno);
        etPassword = findViewById(R.id.etPassword);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


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
        name = etName.getText().toString();
        mbNo = etMbNo.getText().toString();
        password = etPassword.getText().toString();
        mbNo = etMbNo.getText().toString();
        password = etPassword.getText().toString();

        if (name.equals("") || mbNo.equals("") || password.equals("")) {
            Toast.makeText(this, "Fields Compulsory", Toast.LENGTH_LONG).show();
        } else if (mbNo.length() != 10) {
            Toast.makeText(this, "Mobile No must be 10 digits", Toast.LENGTH_LONG).show();
        } else if (password.length() < 6) {
            Toast.makeText(this, "Password must be greater than 6 digits", Toast.LENGTH_LONG).show();
        } else {
            SharedPreferences.Editor editor = sharedpreferences.edit();

            editor.putString("name", name);
            editor.putString("mbno", mbNo);
            editor.putString("password", password);
            editor.commit();
            Toast.makeText(RegisterActivity.this, "Register Success", Toast.LENGTH_LONG).show();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        }
    }

    public void login() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }
}