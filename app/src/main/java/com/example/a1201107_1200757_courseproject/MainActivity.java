package com.example.a1201107_1200757_courseproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Button signUpButton;
    Button signInButton;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private CheckBox rememberMeCheckBox;
    private DataBaseHelper dataBaseHelper;
    private SharedPreferences sharedPreferences;
    private ImageView passwordVisibilityIcon;
     SwitchCompat switchMode;
    SharedPrefManager sharedPrefManager;

    private static final String DARK_MODE_KEY = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signUpButton = findViewById(R.id.signup);
        signInButton = findViewById(R.id.SignIn);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        passwordVisibilityIcon = findViewById(R.id.passwordVisibilityIcon);
        switchMode = findViewById(R.id.switchMode);

        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);


        dataBaseHelper = new DataBaseHelper(MainActivity.this, "DB_PROJECT", null, 3);

        sharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        boolean isRememberMeChecked = sharedPreferences.getBoolean("remember_me", false);


        if (sharedPreferences.contains("session_token")) {
            String email = sharedPreferences.getString("logged_in_email", "");
            if (!email.isEmpty()) {
                // Redirect to home screen if session is valid
                Intent intent = new Intent(MainActivity.this, TODOHome.class);
                startActivity(intent);
                finish();
            }
        }
        sharedPrefManager =SharedPrefManager.getInstance(this);

        boolean isDarkModeEnabled = sharedPrefManager.readBoolean(DARK_MODE_KEY, false);
        setDarkMode(isDarkModeEnabled);

        if (isRememberMeChecked) {
            String savedEmail = sharedPreferences.getString("logged_in_email", "");
            editTextEmail.setText(savedEmail);
            rememberMeCheckBox.setChecked(true);
        } else {
            editTextEmail.setText("");
            rememberMeCheckBox.setChecked(false);
        }

        passwordVisibilityIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextPassword.getInputType() == 129) {
                    editTextPassword.setInputType(1);
                    passwordVisibilityIcon.setImageResource(R.drawable.ic_eye);
                } else {
                    editTextPassword.setInputType(129);
                    passwordVisibilityIcon.setImageResource(R.drawable.ic_eye_off);
                }
            }
        });

        switchMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPrefManager.writeBoolean(DARK_MODE_KEY, isChecked);
            setDarkMode(isChecked);
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);

                startActivity(intent);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user =new User();

                if(editTextEmail.getText().toString().isEmpty()) user.setmEmail("No Email");
                else user.setmEmail(editTextEmail.getText().toString());

                if(editTextPassword.getText().toString().isEmpty()) user.setmPassword("No password");
                else user.setmPassword(editTextPassword.getText().toString());


                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                if (validateInputs( email, password)) {
                    user.setmEmail(email);
                    user.setmPassword(password);

                    try {
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        if (rememberMeCheckBox.isChecked()) {
                            editor.putBoolean("remember_me", true);

                        }else{
                            editor.putBoolean("remember_me", false);

                        }
                        editor.putString("logged_in_email", email);

                        // Generate and save session token
                        String sessionToken = UUID.randomUUID().toString();
                        editor.putString("session_token", sessionToken);
                        editor.apply();

                        Intent intent = new Intent(MainActivity.this, TODOHome.class);
                        startActivity(intent);
                        finish();
                    } catch (IllegalArgumentException e) {
                        editTextEmail.setError("user doesn't exist");
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();                    }
                }

            }
        });



    }
    private boolean validateInputs(String email, String password) {
        if (!isValidEmail(email)) {
            editTextEmail.setError("Invalid email format");
            return false;
        }
        if (email.isEmpty()) {
            editTextEmail.setError("Please Enter the email");
            return false;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Please Enter Password");
            return false;
        }

        if (!dataBaseHelper.validateUser(email, password)) {
            editTextEmail.setError("Incorrect Email or Password");
            editTextPassword.setError("Incorrect Email or Password");

            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }
    private void setDarkMode(boolean enable) {
        if (enable) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }




}