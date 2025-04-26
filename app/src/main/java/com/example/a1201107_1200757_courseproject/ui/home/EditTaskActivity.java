package com.example.a1201107_1200757_courseproject.ui.home;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.a1201107_1200757_courseproject.DataBaseHelper;
import com.example.a1201107_1200757_courseproject.R;
import com.example.a1201107_1200757_courseproject.Task;

public class EditTaskActivity extends AppCompatActivity {

    private DataBaseHelper dbHelper;
    private EditText titleEditText, descriptionEditText, dueDateEditText, dueTimeEditText;
    private Spinner prioritySpinner;
    private Switch statusSwitch;
    private Button saveButton;
    String originalEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        titleEditText = findViewById(R.id.editTextTitle);
        descriptionEditText = findViewById(R.id.editTextDescription);
        dueDateEditText = findViewById(R.id.editTextDueDate);
        dueTimeEditText = findViewById(R.id.editTextDueTime);
        prioritySpinner = findViewById(R.id.spinnerPriority);
        statusSwitch = findViewById(R.id.switchStatus);
        saveButton = findViewById(R.id.buttonSave);


        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array,  // You'll need to define this array in strings.xml
                android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        int taskId = getIntent().getIntExtra("TASK_ID", -1);
        dbHelper = new DataBaseHelper(this, "DB_PROJECT", null, 3);

        Task task = dbHelper.getTaskById(taskId); // Implement this method in DataBaseHelper

        if (task != null) {
            titleEditText.setText(task.getTitle());
            descriptionEditText.setText(task.getDescription());
            dueDateEditText.setText(task.getDueDate());
            dueTimeEditText.setText(task.getDueTime());
            String taskPriority = task.getPriority();
            int priorityPosition = getPriorityPosition(taskPriority);  // Use a method to map priority to the spinner position
            prioritySpinner.setSelection(priorityPosition);
            statusSwitch.setChecked(task.getStatus().equals("Completed"));
            originalEmail = task.getUserEmail();
        }

        saveButton.setOnClickListener(v -> {
            // Validate inputs
            if (validateInputs()) {
                String status = statusSwitch.isChecked() ? "Completed" : "Incomplete";
                String selectedPriority = (String) prioritySpinner.getSelectedItem();

                Task updatedTask = new Task(
                        taskId,
                        titleEditText.getText().toString().trim(),
                        descriptionEditText.getText().toString().trim(),
                        dueDateEditText.getText().toString().trim(),
                        dueTimeEditText.getText().toString().trim(),
                        selectedPriority,
                        status,
                        originalEmail
                );

                boolean updateResult = dbHelper.updateTask(updatedTask);
                if (updateResult) {
                    Toast.makeText(this, "Task Updated Successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);  // Set result for potential callback
                    finish();
                } else {
                    Toast.makeText(this, "Failed to Update Task. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private boolean validateInputs() {
        boolean isValid = true;

        if (titleEditText.getText().toString().trim().isEmpty()) {
            titleEditText.setError("Title cannot be empty");
            isValid = false;
        }
        if (dueDateEditText.getText().toString().trim().isEmpty()) {
            dueDateEditText.setError("Due Date cannot be empty");
            isValid = false;
        }
        if (dueTimeEditText.getText().toString().trim().isEmpty()) {
            dueTimeEditText.setError("Due Time cannot be empty");
            isValid = false;
        }

        return isValid;
    }
    private int getPriorityPosition(String priority) {
        // Use a switch case or if-else to map string values to spinner positions
        switch (priority) {
            case "High":
                return 0;
            case "Medium":
                return 1;
            case "Low":
                return 2;
            default:
                return 1;  // Default to Medium if priority is unknown
        }
    }
}
