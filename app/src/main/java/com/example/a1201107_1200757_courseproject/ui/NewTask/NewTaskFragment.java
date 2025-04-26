package com.example.a1201107_1200757_courseproject.ui.NewTask;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.a1201107_1200757_courseproject.DataBaseHelper;
import com.example.a1201107_1200757_courseproject.R;
import com.example.a1201107_1200757_courseproject.ReminderBroadcast;
import com.example.a1201107_1200757_courseproject.ui.home.HomeFragment;

import java.util.Calendar;

public class NewTaskFragment extends Fragment {

    private DataBaseHelper dbHelper;

    private EditText taskTitle, taskDescription, taskDueDate, taskDueTime;
    private Spinner taskPriority;
    private Switch taskStatus;
    private Button saveTaskButton;
    private ImageView reminderIcon;
    private boolean isReminderSet = false;
    private String reminderTime = ""; // To hold the reminder time if set

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_new_task, container, false);

        taskTitle = root.findViewById(R.id.taskTitle);
        taskDescription = root.findViewById(R.id.taskDescription);
        taskDueDate = root.findViewById(R.id.taskDueDate);
        taskDueTime = root.findViewById(R.id.taskDueTime);
        taskPriority = root.findViewById(R.id.taskPriority);
        taskStatus = root.findViewById(R.id.taskStatus);
        saveTaskButton = root.findViewById(R.id.saveTaskButton);
        reminderIcon = root.findViewById(R.id.reminderIcon);

        dbHelper = new DataBaseHelper(getContext(), "DB_PROJECT", null, 3);

        // Initialize Spinner with priority options
        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskPriority.setAdapter(adapter);
        taskPriority.setSelection(1);

        // Get the current user email from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("logged_in_email", null);

        // Reminder Icon Click Listener
        reminderIcon.setOnClickListener(v -> {
            isReminderSet = !isReminderSet;
            reminderIcon.setColorFilter(isReminderSet ?
                    ContextCompat.getColor(getContext(), android.R.color.holo_red_dark) :
                    ContextCompat.getColor(getContext(), android.R.color.darker_gray));
            if (isReminderSet) {
                // Show DatePickerDialog to select a date
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(),
                        (view, year, monthOfYear, dayOfMonth) -> {
                            // Once the user selects the date, show the TimePickerDialog
                            TimePickerDialog timePickerDialog = new TimePickerDialog(
                                    getContext(),
                                    (timeView, hourOfDay, minute) -> {
                                        String selectedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                        reminderTime = selectedDate + " " + hourOfDay + ":" + minute;
//                                        setReminderAlarm(reminderTime);  // Set alarm based on selected time
                                    },
                                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                                    Calendar.getInstance().get(Calendar.MINUTE),
                                    true
                            );
                            timePickerDialog.show();
                        },
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
            } else {
                // Clear reminder when user disables it
                reminderTime = "";
            }
        });
        // Date Picker for Due Date
        taskDueDate.setOnClickListener(v -> {
            // Get current date
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create and show DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    // Format the date and set it in the EditText
                    String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                    taskDueDate.setText(selectedDate);
                }
            }, year, month, day);

            datePickerDialog.show();
        });

        // Time Picker for Due Time
        taskDueTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Create and show TimePickerDialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            // Convert the time to 12-hour format
                            String timePeriod = (hourOfDay >= 12) ? "PM" : "AM";
                            int hourIn12HrFormat = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
                            String selectedTime = String.format("%02d:%02d %s", hourIn12HrFormat, minute, timePeriod);
                            taskDueTime.setText(selectedTime);
                        }
                    }, hour, minute, false); // 'false' for 12-hour format
            timePickerDialog.show();
        });
        // Save Task Button OnClickListener
        saveTaskButton.setOnClickListener(v -> {
            String title = taskTitle.getText().toString();
            String description = taskDescription.getText().toString();
            String dueDate = taskDueDate.getText().toString();
            String dueTime = taskDueTime.getText().toString();
            String priority = taskPriority.getSelectedItem() != null ? taskPriority.getSelectedItem().toString() : "Medium";
            String status = taskStatus.isChecked() ? "Completed" : "Incomplete";
            if (dueTime.isEmpty()) {
                // Handle empty input or set a default value
                dueTime = "12:00 AM"; // Default time
            }
            if (title.isEmpty() || dueDate.isEmpty() || dueTime.isEmpty()) {
                Toast.makeText(getContext(), "Title, Due Date and Time are required!", Toast.LENGTH_SHORT).show();
                return;
            }



            // Insert the new task into the database
//            dbHelper.insertTask(title, description, dueDate, dueTime, priority,status, userEmail);
            long newTaskId = dbHelper.insertTask(title, description, dueDate, dueTime, priority, status, userEmail);

            if (newTaskId != -1) {
                // If reminder is set, schedule it with the task information
                if (isReminderSet && !reminderTime.isEmpty()) {
                    scheduleReminder((int)newTaskId, title, reminderTime);
                }

                Toast.makeText(getContext(), "Task saved successfully!", Toast.LENGTH_SHORT).show();

                Fragment homeFragment = getParentFragmentManager().findFragmentByTag("HomeFragment");
                if (homeFragment != null && homeFragment instanceof HomeFragment) {
                    ((HomeFragment) homeFragment).refreshTaskList();
                }
            } else {
                Toast.makeText(getContext(), "Error saving task!", Toast.LENGTH_SHORT).show();
            }
        });

        return root;

    }

    private void scheduleReminder(int taskId, String taskTitle, String reminderTime) {
        String[] dateTime = reminderTime.split(" ");
        String[] dateParts = dateTime[0].split("-");
        String[] timeParts = dateTime[1].split(":");

        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1;
        int day = Integer.parseInt(dateParts[2]);
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);

        Intent intent = new Intent(getContext(), ReminderBroadcast.class);
        intent.putExtra("TASK_ID", taskId);
        intent.putExtra("TASK_TITLE", taskTitle);

        // Use taskId as request code to ensure unique PendingIntents
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }

        Toast.makeText(getContext(), "Reminder scheduled for: " + reminderTime, Toast.LENGTH_SHORT).show();
    }
}