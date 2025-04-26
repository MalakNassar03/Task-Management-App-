package com.example.a1201107_1200757_courseproject.ui.logout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.a1201107_1200757_courseproject.MainActivity;
import com.example.a1201107_1200757_courseproject.MainActivity;
import com.example.a1201107_1200757_courseproject.R;

public class logoutFragment extends Fragment {

    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                          @Nullable android.view.ViewGroup container,
                                          @Nullable Bundle savedInstanceState) {
        // Call logout method directly
        logoutUser();
        return null;  // No need to inflate a layout for this fragment
    }

    private void logoutUser() {
        // Clear user session or authentication data
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        // Remove session-related data
        editor.remove("session_token");

        // Check the "Remember Me" state
        boolean isRememberMeChecked = sharedPreferences.getBoolean("remember_me", false);
        if (!isRememberMeChecked) {
            // If "Remember Me" is unchecked, clear the saved email
            editor.remove("logged_in_email");
        }

        editor.apply();

        // Redirect to SignInActivity
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finish current activity
        requireActivity().finish();
    }
}