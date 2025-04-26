package com.example.a1201107_1200757_courseproject;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUpActivity extends AppCompatActivity {
    private EditText editTextEmail_signUp;
    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextPassword;
    private EditText editTextConfPassword;
    Button SignUP_button;
    User newUser =new User();

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        DataBaseHelper dataBaseHelper = new DataBaseHelper(this, "DB_PROJECT", null, 3);
        editTextEmail_signUp = findViewById(R.id.editTextEmailAddress);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPassword = findViewById(R.id.editTextSignUpPassword);
        editTextConfPassword = findViewById(R.id.editTextConfPassword);
        SignUP_button = findViewById(R.id.SignUp_button);

        SignUP_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextEmail_signUp.getText().toString().isEmpty()) newUser.setmEmail("No Email");
                else newUser.setmEmail(editTextEmail_signUp.getText().toString());

                if (editTextFirstName.getText().toString().isEmpty()) newUser.setmFirstName("No FirstName");
                else newUser.setmFirstName(editTextFirstName.getText().toString());

                if (editTextLastName.getText().toString().isEmpty()) newUser.setmLastName("No LastName");
                else newUser.setmLastName(editTextLastName.getText().toString());

                if (editTextPassword.getText().toString().isEmpty()) newUser.setmPassword("No password");
                else newUser.setmPassword(editTextPassword.getText().toString());

                String email = editTextEmail_signUp.getText().toString();
                String FirstName = editTextFirstName.getText().toString();
                String LastName = editTextLastName.getText().toString();
                String password = editTextPassword.getText().toString();
                String Confpassword = editTextConfPassword.getText().toString();

                if (validateInputs(email, FirstName, LastName, password, Confpassword)) {
                    newUser.setmEmail(email);
                    newUser.setmFirstName(FirstName);
                    newUser.setmLastName(LastName);
                    newUser.setmPassword(password);

                    try {
                        dataBaseHelper.insertUser(newUser);
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (IllegalArgumentException e) {
                        editTextEmail_signUp.setError("User already exists");
                        Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignUpActivity.this, "Cannot register due to incorrect fields", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    private boolean validateInputs(String email, String FirstName, String LastName,String password, String Confpassword) {
        boolean isValid = true;

        if (!isValidEmail(email)) {
            editTextEmail_signUp.setError("Invalid email format");
            isValid = false;
        }
        if (email.isEmpty()) {
            editTextEmail_signUp.setError("Please enter email");
            isValid = false;
        }
        if (FirstName.isEmpty()) {
            editTextFirstName.setError("please enter FirstName");
            isValid = false;
        }
        if (FirstName.length() < 5 || FirstName.length() > 20) {
            editTextFirstName.setError("First name must be between 5 and 20 characters");
            isValid = false;
        }
        if (LastName.isEmpty()) {
            editTextLastName.setError("please enter LastName");
            isValid = false;
        }
        if (LastName.length() < 5 || LastName.length() > 20) {
            editTextLastName.setError("Last name must be between 5 and 20 characters");
            isValid = false;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("enter password");
            isValid = false;
        }
        if (!isValidPassword(password)) {
            editTextPassword.setError("Password must be 6-12 characters and include 1 uppercase, 1 lowercase, and 1 number");
            isValid = false;
        }
        if (Confpassword.isEmpty()) {
            editTextConfPassword.setError("enter password");
            isValid = false;
        }

        if (!Confpassword.equals(password)) {
            editTextConfPassword.setError("Password and Confirm Password must match");
            isValid = false;

        }
        return isValid;
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,12}$";
        return password.matches(passwordPattern);
    }

}