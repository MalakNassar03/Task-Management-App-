package com.example.a1201107_1200757_courseproject.ui.profile;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.a1201107_1200757_courseproject.DataBaseHelper;
import com.example.a1201107_1200757_courseproject.R;
import com.example.a1201107_1200757_courseproject.TODOHome;
import com.example.a1201107_1200757_courseproject.User;


public class profileFragment extends Fragment {


    private TextView firstNameTextView, lastNameTextView, emailTextView;
    private EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText;
    private Button updateButton;
    String sessionToken;
    private String currentEmail;
    private DataBaseHelper dbHelper;

    public profileFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        firstNameTextView = rootView.findViewById(R.id.text_first_name);
        lastNameTextView = rootView.findViewById(R.id.text_last_name);
        emailEditText = rootView.findViewById(R.id.edit_email);
        passwordEditText = rootView.findViewById(R.id.edit_password);
        updateButton = rootView.findViewById(R.id.button_update_profile);

        // Initialize DataBaseHelper
        dbHelper = new DataBaseHelper(getContext(), "DB_PROJECT", null, 3);

        // Get email from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        currentEmail = sharedPreferences.getString("logged_in_email", null);
         sessionToken = sharedPreferences.getString("session_token", null);


        if (currentEmail != null) {
            // Fetch user data from database using the email
            fetchUserDataFromDatabase(currentEmail);
        }

        // Handle Update button click
        updateButton.setOnClickListener(v -> {
            String newEmail = emailEditText.getText().toString().trim();
            String newPassword = passwordEditText.getText().toString().trim();


            if (newEmail.isEmpty() && newPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please enter new data to update.", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (!newEmail.isEmpty() && !isValidEmail(newEmail)) {
                Toast.makeText(getContext(), "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                return;
            }else if( !isValidPassword(newPassword) && !newPassword.isEmpty()){
                Toast.makeText(getContext(), "Please enter a valid password.", Toast.LENGTH_SHORT).show();
            }
            else{
                // Update the database with new data (email and password)
                updateUserInDatabase(newEmail, newPassword);
            }



        });

        return rootView;
    }

    private void fetchUserDataFromDatabase(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM USER WHERE EMAIL = ?", new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            // Check if column indices are valid before accessing them
            int firstNameIndex = cursor.getColumnIndex("FIRST");
            int lastNameIndex = cursor.getColumnIndex("LAST");
            int emailIndex = cursor.getColumnIndex("EMAIL");

            // Ensure column indices are valid (>= 0)
            if (firstNameIndex >= 0 && lastNameIndex >= 0 && emailIndex >= 0) {
                String firstName = cursor.getString(firstNameIndex);
                String lastName = cursor.getString(lastNameIndex);
                String emailFromDb = cursor.getString(emailIndex);

                // Populate the UI with user data (non-editable)
                firstNameTextView.setText(firstName);
                lastNameTextView.setText(lastName);
                emailEditText.setText(emailFromDb);
            }
        }

        if (cursor != null) {
            cursor.close();
        }
    }


    private void updateUserInDatabase(String newEmail, String newPassword) {
        if (currentEmail == null && sessionToken == null) {
            Toast.makeText(getContext(), "No user logged in. Update failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare the database for update
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // Add only the fields that the user entered
        if (!newEmail.isEmpty()) {
                contentValues.put("EMAIL", newEmail);
        }
        if (!newPassword.isEmpty()) {
               contentValues.put("PASSWORD", newPassword);


        }

        // Check if there's anything to update
        if (contentValues.size() == 0) {
            Toast.makeText(getContext(), "No changes to update.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.beginTransaction();
        try {
            // Update the user table
            int rowsUpdated = db.update("USER", contentValues, "EMAIL = ?", new String[]{currentEmail});
            if (rowsUpdated > 0 && !newEmail.isEmpty()) {
                // Update tasks or related tasks associated with this email
                ContentValues taskUpdate = new ContentValues();
                taskUpdate.put("USER_EMAIL", newEmail);
                db.update("TASKS", taskUpdate, "USER_EMAIL = ?", new String[]{currentEmail});

                // Update session in SharedPreferences
                currentEmail = newEmail;
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("logged_in_email", newEmail);
                editor.apply();

                // Commit transaction
                db.setTransactionSuccessful();
                fetchUserDataFromDatabase(newEmail);

                Toast.makeText(getContext(), "User information updated successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update user information. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            ((TODOHome) getActivity()).updateSidebarEmail(newEmail);

            db.endTransaction();
        }
    }
    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,12}$";
        return password.matches(passwordPattern);
    }
    private boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailPattern);
    }

}