package com.example.a1201107_1200757_courseproject.ui.home;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1201107_1200757_courseproject.DataBaseHelper;
import com.example.a1201107_1200757_courseproject.MainActivity;
import com.example.a1201107_1200757_courseproject.R;
import com.example.a1201107_1200757_courseproject.Task;
import com.example.a1201107_1200757_courseproject.User;
import com.example.a1201107_1200757_courseproject.databinding.FragmentHomeBinding;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DataBaseHelper dbHelper;
    private TextView noTasksMessage;
    private TaskAdapter adapter;
    private List<Task> taskList;
    TextView TextViewEmail;
    private ActivityResultLauncher<Intent> editTaskLauncher;
    private RecyclerView recyclerView;
    private TextView textView8;
    ImageView imageView;
    ImageView imageView2;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TextViewEmail = root.findViewById(R.id.TextViewEmail);
        textView8 = root.findViewById(R.id.textView8);
        String dynamicTitle = "Today's Tasks";
        textView8.setText(dynamicTitle);
        LinearLayout linearLayout = root.findViewById(R.id.linearLayout_home);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        imageView =root.findViewById(R.id.imageView2);
        imageView2 =root.findViewById(R.id.imageView3);
        imageView.setVisibility(View.GONE);
        imageView2.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String sessionToken = sharedPreferences.getString("session_token", null);
        String userEmail = sharedPreferences.getString("logged_in_email", null);

        if (sessionToken == null || userEmail == null) {
            Toast.makeText(getContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
            return root;
        }
        Button sortButton = root.findViewById(R.id.sortButton);
        SearchView searchView = root.findViewById(R.id.searchView);
        sortButton.setOnClickListener(v -> sortTasksByPriority());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTasks(query);  // Search when user submits query
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    refreshTaskList();  // Reset when query is cleared
                }
                return false;
            }
        });
        searchView.setQueryHint("Search tasks...");

        editTaskLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        Toast.makeText(getContext(), "Task updated, refreshing list...", Toast.LENGTH_SHORT).show();
                        refreshTaskList();
                    }
                });








        dbHelper = new DataBaseHelper(getContext(), "DB_PROJECT", null, 3);
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        taskList = dbHelper.getTasksDueToday(todayDate, userEmail);

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(taskList, dbHelper, this::refreshTaskList, getActivity(), editTaskLauncher);  // Pass the launcher
        recyclerView.setAdapter(adapter);

        noTasksMessage = binding.getRoot().findViewById(R.id.noTasksMessage);
        toggleNoTasksMessage(taskList.isEmpty());

        if (dbHelper.areAllTodayTasksCompleted(userEmail)) {
            Toast.makeText(getContext(),
                    "🎉 Congratulations! You've completed all tasks for today!",
                    Toast.LENGTH_LONG).show();
            imageView.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.VISIBLE);

            // Set TransitionDrawable for imageView
            TransitionDrawable transitionDrawable1 = (TransitionDrawable) imageView.getDrawable();
            transitionDrawable1.startTransition(1000); // Smooth transition for 1 second

// Set TransitionDrawable for imageView2
            TransitionDrawable transitionDrawable2 = (TransitionDrawable) imageView2.getDrawable();
            transitionDrawable2.startTransition(1000); // Smooth transition for 1 second
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Set visibility to GONE after the animation
                imageView.setVisibility(View.GONE);
                imageView2.setVisibility(View.GONE);
            }, 1000); // Delay matches the animation duration
        }




        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTasks(query);  // Search when user submits query
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    refreshTaskList();  // Reset when query is cleared
                }
                return false;
            }
        });

        return root;
    }

    private void toggleNoTasksMessage(boolean isEmpty) {
        noTasksMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
    public void refreshTaskList() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("logged_in_email", null);

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<Task> updatedTaskList = dbHelper.getTasksDueToday(todayDate, userEmail);

        taskList.clear();
        taskList.addAll(updatedTaskList);
        if (recyclerView != null && adapter != null) {
            recyclerView.post(() -> adapter.notifyDataSetChanged());
        }        toggleNoTasksMessage(updatedTaskList.isEmpty());
        toggleNoTasksMessage(updatedTaskList.isEmpty());
        if (dbHelper.areAllTodayTasksCompleted(userEmail)) {
            Toast.makeText(getContext(),
                    "🎉 Congratulations! You've completed all tasks for today!",
                    Toast.LENGTH_LONG).show();
            imageView.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.VISIBLE);

            // Set TransitionDrawable for imageView
            TransitionDrawable transitionDrawable1 = (TransitionDrawable) imageView.getDrawable();
            transitionDrawable1.startTransition(1000); // Smooth transition for 1 second

// Set TransitionDrawable for imageView2
            TransitionDrawable transitionDrawable2 = (TransitionDrawable) imageView2.getDrawable();
            transitionDrawable2.startTransition(1000); // Smooth transition for 1 second
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Set visibility to GONE after the animation
                imageView.setVisibility(View.GONE);
                imageView2.setVisibility(View.GONE);
            }, 1000); // Delay matches the animation duration
        }
    }

    private void sortTasksByPriority() {
        if (taskList == null || taskList.isEmpty()) {
            Toast.makeText(getContext(), "No tasks to sort", Toast.LENGTH_SHORT).show();
            return;
        }

        Collections.sort(taskList, (task1, task2) -> {
            int priority1 = getPriorityValue(task1.getPriority());
            int priority2 = getPriorityValue(task2.getPriority());
            return Integer.compare(priority1, priority2);
        });

        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Tasks sorted by priority", Toast.LENGTH_SHORT).show();
    }

    private int getPriorityValue(String priority) {
        switch (priority.toLowerCase()) {
            case "high":
                return 1;
            case "medium":
                return 2;
            case "low":
                return 3;
            default:
                return Integer.MAX_VALUE;  // Unknown priority goes last
        }
    }


    private void searchTasks(String keyword) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("logged_in_email", null);

        List<Task> searchResults = dbHelper.getTasksBySearchMethod(
                "", "", keyword, userEmail, "Keyword");

        if (searchResults != null && !searchResults.isEmpty()) {
            taskList.clear();
            taskList.addAll(searchResults);
            adapter.notifyDataSetChanged();
            toggleNoTasksMessage(false);
        } else {
            taskList.clear();
            adapter.notifyDataSetChanged();
            toggleNoTasksMessage(true);
            Toast.makeText(getContext(), "No tasks found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}