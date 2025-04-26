package com.example.a1201107_1200757_courseproject.ui.SearchTask;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1201107_1200757_courseproject.DataBaseHelper;
import com.example.a1201107_1200757_courseproject.R;
import com.example.a1201107_1200757_courseproject.Task;
import com.example.a1201107_1200757_courseproject.databinding.FragmentSearchtaskBinding;
import com.example.a1201107_1200757_courseproject.ui.home.TaskAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchTaskFragment extends Fragment {
    private EditText editTextStartDate, editTextEndDate;
    private Button searchButton;
    private Button filterButton;
    private RecyclerView recyclerView;
    private TextView noInfoAvaliable;
    private TextView KeywordEditText;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private DataBaseHelper dbHelper;
    private LinearLayout SearchLayout;
    private LinearLayout FilterLayout;
    private TextView noTasksMessage;

    private ActivityResultLauncher<Intent> editTaskLauncher;



    private FragmentSearchtaskBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_searchtask, container, false);

        SearchTaskViewModel searchTaskViewModel =
                new ViewModelProvider(this).get(SearchTaskViewModel.class);

        binding = FragmentSearchtaskBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        editTextStartDate = root.findViewById(R.id.editTextStartDate);
        editTextEndDate = root.findViewById(R.id.editTextEndDate);
        searchButton = root.findViewById(R.id.Search_button);
        filterButton = root.findViewById(R.id.Filter_button);
        recyclerView = root.findViewById(R.id.SearchTaskView);
        KeywordEditText = root.findViewById(R.id.keywordEditText);
        noInfoAvaliable = root.findViewById(R.id.noInfoAvaliable);
        SearchLayout = root.findViewById(R.id.SearchLayout);
        FilterLayout = root.findViewById(R.id.filterLayout);
        noTasksMessage = root.findViewById(R.id.noTasksMessage);

        String[] searchMethods = {"Date", "Keyword"};
        ArrayAdapter<String> SearchMethodAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, searchMethods);
        SearchMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editTaskLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        Toast.makeText(getContext(), "Task updated, refreshing list...", Toast.LENGTH_SHORT).show();
                        updateTaskList();
                    }
                });
// Assuming `searchMethodSpinner` is the ID of your Spinner
        Spinner searchMethodSpinner = root.findViewById(R.id.searchMethodSpinner);
        searchMethodSpinner.setAdapter(SearchMethodAdapter);
        dbHelper = new DataBaseHelper(getContext(), "DB_PROJECT", null, 3);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(taskList, dbHelper, this::updateTaskList, getActivity(), editTaskLauncher);  // Pass the launcher
        recyclerView.setAdapter(adapter);


        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("logged_in_email", null);

        searchMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Show or hide views based on the selected search method
                if ("Keyword".equals(searchMethods[position])) {
                    FilterLayout.setVisibility(View.VISIBLE);
                    SearchLayout.setVisibility(View.GONE);
                } else if ("Date".equals(searchMethods[position])) {
                    FilterLayout.setVisibility(View.GONE);
                    SearchLayout.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                FilterLayout.setVisibility(View.GONE);
                SearchLayout.setVisibility(View.GONE);

            }
        });


        editTextStartDate.setOnClickListener(view -> showDatePickerDialog(editTextStartDate));
        editTextEndDate.setOnClickListener(view -> showDatePickerDialog(editTextEndDate));

        searchButton.setOnClickListener(v -> {
            String startDate = editTextStartDate.getText().toString();
            String endDate = editTextEndDate.getText().toString();

            // Validate inputs
            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(getContext(), "Please enter both start and end dates", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch tasks by date range
            taskList =  dbHelper.getTasksBySearchMethod(startDate, endDate, null, userEmail, "Date");


            // Update RecyclerView with the retrieved tasks
            updateTaskList();
        });

        filterButton.setOnClickListener(v -> {


            String keyword = KeywordEditText.getText().toString();

            Log.e("keyword", keyword);

            // Fetch tasks by date range
            taskList = dbHelper.getTasksBySearchMethod(null, null, keyword, userEmail, "Keyword");


            // Update RecyclerView with the retrieved tasks
            updateTaskList();
        });

        return root;
    }

    private void showDatePickerDialog(EditText dateInput) {
        // Get current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Set selected date to the input field
                    String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    dateInput.setText(formattedDate);
                },
                year,
                month,
                day
        );
        datePickerDialog.show();
    }
//    private void updateTaskList() {
//        if (dbHelper == null) {
//            dbHelper = new DataBaseHelper(getContext(), "DB_PROJECT", null, 3);
//        }
//
//        // Fetch the updated task list
//        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
//        String userEmail = sharedPreferences.getString("logged_in_email", null);
//
//        // Example: Fetch all tasks for the user (update `getAllTasksForUser` based on your implementation)
//        List<Task> updatedTaskList = taskList;
//
//        // Ensure taskList is initialized
//        if (taskList == null) {
//            taskList = new ArrayList<>();
//        }
//
//        // Clear the old list and update with the new one
//        taskList.clear();
//        taskList.addAll(updatedTaskList);
//
//        // Post UI updates to the main thread
//        new Handler(Looper.getMainLooper()).post(() -> {
//            if (taskList.isEmpty()) {
//                noInfoAvaliable.setVisibility(View.VISIBLE);
//                recyclerView.setVisibility(View.GONE);
//            } else {
//                noInfoAvaliable.setVisibility(View.GONE);
//                recyclerView.setVisibility(View.VISIBLE);
//
//                // Update the adapter
//                if (adapter != null) {
//                    adapter.notifyDataSetChanged();
//                }
//            }
//        });
//    }
private void updateTaskList() {
    if (dbHelper == null) {
        dbHelper = new DataBaseHelper(getContext(), "DB_PROJECT", null, 3);
    }

    SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
    String userEmail = sharedPreferences.getString("logged_in_email", null);

    // Get current search criteria from views
    String startDate = editTextStartDate.getText().toString();
    String endDate = editTextEndDate.getText().toString();
    String keyword = KeywordEditText.getText().toString();

    // Determine which search method is currently selected
    Spinner searchMethodSpinner = getView().findViewById(R.id.searchMethodSpinner);
    String searchMethod = searchMethodSpinner.getSelectedItem().toString();

    // Fetch updated task list based on current search criteria
    List<Task> updatedTaskList = dbHelper.getTasksBySearchMethod(
            startDate.isEmpty() ? null : startDate,
            endDate.isEmpty() ? null : endDate,
            keyword.isEmpty() ? null : keyword,
            userEmail,
            searchMethod
    );

    // Update the taskList
    taskList = updatedTaskList != null ? updatedTaskList : new ArrayList<>();

    // Post UI updates to main thread
    new Handler(Looper.getMainLooper()).post(() -> {
        if (taskList.isEmpty()) {
            noInfoAvaliable.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noInfoAvaliable.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // Update the adapter
            if (adapter != null) {
                adapter.updateTaskList(taskList);
            }
        }
    });
}
//    private void updateTaskList() {
//        if (dbHelper == null) {
//            dbHelper = new DataBaseHelper(getContext(), "DB_PROJECT", null, 3);
//        }
//
//        // Ensure taskList is not null
//        if (taskList == null) {
//            taskList = new ArrayList<>();
//        }
//
//        // Post UI updates to main thread
//        new Handler(Looper.getMainLooper()).post(() -> {
//            if (taskList.isEmpty()) {
//                noInfoAvaliable.setVisibility(View.VISIBLE);
//                recyclerView.setVisibility(View.GONE);
//            } else {
//                noInfoAvaliable.setVisibility(View.GONE);
//                recyclerView.setVisibility(View.VISIBLE);
//
//                // Update the adapter
//                if (adapter != null) {
//                    adapter.updateTaskList(taskList);
//                }
//            }
//        });
//
//    }

    private void toggleNoTasksMessage(boolean isEmpty) {
        noTasksMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
//    public void refreshTaskList() {
//        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
//        String userEmail = sharedPreferences.getString("logged_in_email", null);
//
//        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
////        List<Task> updatedTaskList = dbHelper.getTasksDueToday(todayDate, userEmail);
////        taskList =  dbHelper.getTasksBySearchMethod(startDate, endDate, null, userEmail, "Date");
//
//        taskList.clear();
//        taskList.addAll(TaskList);
//        if (recyclerView != null && adapter != null) {
//            recyclerView.post(() -> adapter.notifyDataSetChanged());
//        }        toggleNoTasksMessage(updatedTaskList.isEmpty());
//        toggleNoTasksMessage(updatedTaskList.isEmpty());
//
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}