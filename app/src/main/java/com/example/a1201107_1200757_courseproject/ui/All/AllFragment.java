package com.example.a1201107_1200757_courseproject.ui.All;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1201107_1200757_courseproject.ConnectionAsyncTask;
import com.example.a1201107_1200757_courseproject.DataBaseHelper;
import com.example.a1201107_1200757_courseproject.MainActivity;
import com.example.a1201107_1200757_courseproject.R;
import com.example.a1201107_1200757_courseproject.Task;
import com.example.a1201107_1200757_courseproject.databinding.FragmentHomeBinding;
import com.example.a1201107_1200757_courseproject.ui.home.TaskAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllFragment extends Fragment {


    private FragmentHomeBinding binding;
    private DataBaseHelper dbHelper;
    private TextView noTasksMessage;
    private TaskAdapter adapter;
    private List<Task> taskList;
    TextView TextViewEmail;
    private ActivityResultLauncher<Intent> editTaskLauncher;
    private RecyclerView recyclerView;
    Button connectButton;
    LinearLayout linearLayout;
    LinearLayout linearLayout2;
    private TextView textView8;
    ImageView imageView;
    ImageView imageView2;
    private ViewGroup mainContainer;
    String userEmail;


    private static final String LIGHT_BLUE_COLOR = "#C4A2BFDA";
    private static final String NAVY_COLOR = "#D01F3A60";
    private boolean alternateColor = true;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextViewEmail = root.findViewById(R.id.TextViewEmail);
        textView8 = root.findViewById(R.id.textView8);
        imageView =root.findViewById(R.id.imageView2);
        imageView2 =root.findViewById(R.id.imageView3);
        imageView.setVisibility(View.GONE);
        imageView2.setVisibility(View.GONE);
        String dynamicTitle = "All Tasks";
        textView8.setText(dynamicTitle);
        recyclerView = binding.recyclerView;
        linearLayout = root.findViewById(R.id.linearLayout_home); // A LinearLayout in your XML to hold dynamic elements
        linearLayout2 = root.findViewById(R.id.linear_l); // A LinearLayout in your XML to hold dynamic elements

//        // Dynamically create the button
//        connectButton = new Button(getContext());
//        connectButton.setText("Fetch Data");
//        connectButton.setLayoutParams(new LayoutParams(
//                LayoutParams.MATCH_PARENT,
//                LayoutParams.WRAP_CONTENT
//        ));
//        linearLayout.addView(connectButton);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );


        Button connectButton = new Button(requireContext()); // Using requireContext() is safer
        connectButton.setText("Fetch Data");
        connectButton.setLayoutParams(params);
        // Set text color to white
        connectButton.setTextColor(Color.WHITE);

        connectButton.setBackgroundColor(Color.parseColor("#8CB1D5"));  // Light blue color

        params.setMargins(16, 8, 16, 8);  // Left, Top, Right, Bottom margins
        connectButton.setPadding(20, 12, 20, 12);  // Internal padding

        connectButton.setBackgroundResource(android.R.drawable.btn_default);
        connectButton.getBackground().setColorFilter(
                Color.parseColor("#8CB1D5"),
                PorterDuff.Mode.SRC_ATOP
        );

        connectButton.setElevation(4);  // 4dp elevation


        linearLayout2.addView(connectButton);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String sessionToken = sharedPreferences.getString("session_token", null);
         userEmail = sharedPreferences.getString("logged_in_email", null);

        if (sessionToken == null || userEmail == null) {
            Toast.makeText(getContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
            return root;
        }
        dbHelper = new DataBaseHelper(getContext(), "DB_PROJECT", null, 3);

        editTaskLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        Toast.makeText(getContext(), "Task updated, refreshing list...", Toast.LENGTH_SHORT).show();
                        refreshTaskList();
                    }
                });

        Button sortButton = root.findViewById(R.id.sortButton);
        SearchView searchView = root.findViewById(R.id.searchView);

        sortButton.setVisibility(View.GONE);
        setupSearch();


        mainContainer = (ViewGroup) binding.recyclerView.getParent();
        setupInitialTaskList();

//        dbHelper = new DataBaseHelper(getContext(), "DB_PROJECT", null, 3);
//
//        Map<String, List<Task>> groupedTasks = dbHelper.getAllTasksGroupedByDay(userEmail);
//        taskList = flattenTaskMap(groupedTasks);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        adapter = new TaskAdapter(taskList, dbHelper, this::refreshTaskList, getActivity(), editTaskLauncher);
//        recyclerView.setAdapter(adapter);
//
//        noTasksMessage = binding.getRoot().findViewById(R.id.noTasksMessage);
//        toggleNoTasksMessage(taskList.isEmpty());
//        // Set an OnClickListener for the button
        connectButton.setOnClickListener(v -> {
            ConnectionAsyncTask connectionAsyncTask = new ConnectionAsyncTask(this);
            connectionAsyncTask.execute("https://run.mocky.io/v3/38e20f45-fc30-4555-b460-97852155ff4b");
        });

        return root;
    }

    private void setupInitialTaskList() {
        // Get initial data
        Map<String, List<Task>> groupedTasks = dbHelper.getAllTasksGroupedByDay(userEmail);
        taskList = flattenTaskMap(groupedTasks);

        // Hide the default RecyclerView
        recyclerView.setVisibility(View.GONE);

        // Create and add the groups container
        createAndAddGroupsContainer(groupedTasks);

        // Update empty state
        toggleNoTasksMessage(taskList.isEmpty());
    }

    public void refreshTaskList() {
        if (getContext() == null) return;

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("logged_in_email", null);

        Map<String, List<Task>> updatedGroupedTasks = dbHelper.getAllTasksGroupedByDay(userEmail);
        taskList = flattenTaskMap(updatedGroupedTasks);

        // Remove all views except the RecyclerView (which is hidden)
        for (int i = mainContainer.getChildCount() - 1; i >= 0; i--) {
            View child = mainContainer.getChildAt(i);
            if (child != recyclerView) {
                mainContainer.removeView(child);
            }
        }

        // Create and add new groups container
        createAndAddGroupsContainer(updatedGroupedTasks);

        // Update empty state
        toggleNoTasksMessage(taskList.isEmpty());

        // Request layout update
        mainContainer.requestLayout();
    }

    private void createAndAddGroupsContainer(Map<String, List<Task>> groupedTasks) {
        // Create a ScrollView
        ScrollView scrollView = new ScrollView(getContext());
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        // Create the groups container
        LinearLayout groupsContainer = new LinearLayout(getContext());
        groupsContainer.setOrientation(LinearLayout.VERTICAL);
        groupsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Add groups to the groups container
        alternateColor = true;
        for (Map.Entry<String, List<Task>> entry : groupedTasks.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                CardView dayGroup = createDayGroup(entry.getKey(), entry.getValue());
                groupsContainer.addView(dayGroup);
            }
        }

        // Add the groups container to the ScrollView
        scrollView.addView(groupsContainer);

        // Add the ScrollView to the main container
        mainContainer.addView(scrollView);
    }

    private CardView createDayGroup(String date, List<Task> tasksForDay) {
        CardView dayGroup = new CardView(getContext());
        dayGroup.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        dayGroup.setCardElevation(4);
        dayGroup.setRadius(8);
        dayGroup.setUseCompatPadding(true);

        dayGroup.setCardBackgroundColor(android.graphics.Color.parseColor(
                alternateColor ? NAVY_COLOR : LIGHT_BLUE_COLOR));
        alternateColor = !alternateColor;

        LinearLayout dayContent = new LinearLayout(getContext());
        dayContent.setOrientation(LinearLayout.VERTICAL);
        dayContent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        dayContent.setPadding(16, 16, 16, 16);

        TextView dateHeader = new TextView(getContext());
        dateHeader.setText(date);
        dateHeader.setTypeface(null, Typeface.BOLD);
        dateHeader.setTextSize(16);
        dayContent.addView(dateHeader);

        RecyclerView dayTasksList = new RecyclerView(getContext());
        dayTasksList.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        dayTasksList.setLayoutManager(new LinearLayoutManager(getContext()));

        TaskAdapter dayAdapter = new TaskAdapter(tasksForDay, dbHelper,
                () -> getActivity().runOnUiThread(this::refreshTaskList),
                getActivity(), editTaskLauncher);
        dayTasksList.setAdapter(dayAdapter);
        dayContent.addView(dayTasksList);
        dayGroup.addView(dayContent);

        return dayGroup;
    }

    private void toggleNoTasksMessage(boolean isEmpty) {
        if (noTasksMessage != null) {
            noTasksMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void setupSearch() {
        SearchView searchView = binding.getRoot().findViewById(R.id.searchView);
        searchView.setQueryHint("Search tasks...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTasks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    refreshTaskList();
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public void setProgress(boolean show) {
        if (binding != null && binding.progressBar != null) {
            binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void searchTasks(String keyword) {
        List<Task> searchResults = dbHelper.getTasksBySearchMethod(
                "", "", keyword, userEmail, "Keyword");

        taskList.clear();
        if (searchResults != null && !searchResults.isEmpty()) {
            taskList.addAll(searchResults);
            Map<String, List<Task>> groupedResults = new HashMap<>();
            groupedResults.put("Search Results", searchResults);

            // Remove existing views and create new ones for search results
            for (int i = mainContainer.getChildCount() - 1; i >= 0; i--) {
                View child = mainContainer.getChildAt(i);
                if (child != recyclerView) {
                    mainContainer.removeView(child);
                }
            }
            createAndAddGroupsContainer(groupedResults);
            toggleNoTasksMessage(false);
        } else {
            // Clear views and show no tasks message
            for (int i = mainContainer.getChildCount() - 1; i >= 0; i--) {
                View child = mainContainer.getChildAt(i);
                if (child != recyclerView) {
                    mainContainer.removeView(child);
                }
            }
            toggleNoTasksMessage(true);
            Toast.makeText(getContext(), "No tasks found", Toast.LENGTH_SHORT).show();
        }
    }

    private List<Task> flattenTaskMap(Map<String, List<Task>> groupedTasks) {
        List<Task> flattenedTasks = new ArrayList<>();
        for (List<Task> tasks : groupedTasks.values()) {
            flattenedTasks.addAll(tasks);
        }
        return flattenedTasks;
    }

}
