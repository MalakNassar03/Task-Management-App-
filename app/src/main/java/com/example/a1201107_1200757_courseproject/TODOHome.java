package com.example.a1201107_1200757_courseproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a1201107_1200757_courseproject.databinding.ActivityTodohomeBinding;

public class TODOHome extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityTodohomeBinding binding;
    TextView TextViewName;
    TextView TextViewEmail;
    private DataBaseHelper dbHelper;
    View headerView;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();

        binding = ActivityTodohomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sharedPreferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        dbHelper = new DataBaseHelper(this, "DB_PROJECT", null, 3);

        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        String userEmail = sharedPreferences.getString("logged_in_email", null);
        String[] userNames = dbHelper.getUserFirstAndLastName(userEmail);


//        TextViewEmail.setText();
//        if (!isLoggedIn || userEmail == null) {
//            // Redirect to login if no session exists
//            Intent intent = new Intent(TODOHome.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//            return;
//        }

        setSupportActionBar(binding.appBarTodohome.toolbar);
        binding.appBarTodohome.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        headerView = navigationView.getHeaderView(0);

        TextView navUserEmail = headerView.findViewById(R.id.TextViewEmail);
        TextView navUserName = headerView.findViewById(R.id.TextViewName);

        // Set the user data dynamically
        navUserEmail.setText(userEmail);
        navUserName.setText(userNames[0] + " " + userNames[1]);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_all,R.id.nav_completed, R.id.nav_searchtask, R.id.nav_logout, R.id.nav_profile, R.id.nav_new_task)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_todohome);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "TaskReminderChannel";
            String description = "Channel for task reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("TASK_REMINDER", name, importance);
            channel.setDescription(description);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission is required for snooze functionality",
                        Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.t_o_d_o_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_todohome);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void updateSidebarEmail(String email) {
        TextView emailTextView = headerView.findViewById(R.id.TextViewEmail); // Adjust this ID to match your sidebar layout
        emailTextView.setText(email);
    }

}