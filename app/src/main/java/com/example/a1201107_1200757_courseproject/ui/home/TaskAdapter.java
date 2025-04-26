package com.example.a1201107_1200757_courseproject.ui.home;




import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a1201107_1200757_courseproject.DataBaseHelper;
import com.example.a1201107_1200757_courseproject.R;
import com.example.a1201107_1200757_courseproject.ReminderBroadcast;
import com.example.a1201107_1200757_courseproject.Task;

import java.util.Calendar;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private DataBaseHelper dbHelper;
    private Runnable refreshTaskListCallback;
    private Activity activity;
    private ActivityResultLauncher<Intent> editTaskLauncher;  // Add the launcher

    public TaskAdapter(List<Task> taskList, DataBaseHelper dbHelper, Runnable refreshTaskListCallback, Activity activity, ActivityResultLauncher<Intent> editTaskLauncher) {
        this.taskList = taskList;
        this.dbHelper = dbHelper;
        this.refreshTaskListCallback = refreshTaskListCallback;
        this.activity = activity;
        this.editTaskLauncher = editTaskLauncher;  // Initialize the launcher

    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        final Task task = taskList.get(position);
        holder.titleTextView.setText(task.getTitle());
        holder.descriptionTextView.setText(task.getDescription());
        holder.dueDateTextView.setText(task.getDueDate());
        holder.dueTimeTextView.setText(task.getDueTime());
        holder.priorityTextView.setText(task.getPriority());
//        holder.statusTextView.setText(task.getStatus());
        String statusText = task.isCompleted() ? "Completed" : "Incomplete";
        holder.statusTextView.setText(statusText);

        holder.editButton.setOnClickListener(v -> {
            Task currentTask = taskList.get(position);
            Intent intent = new Intent(v.getContext(), EditTaskActivity.class);
            intent.putExtra("TASK_ID", currentTask.getId());
            // Use the new launcher to start the activity for result
            editTaskLauncher.launch(intent);  // Launch the activity
        });
        holder.deleteButton.setOnClickListener(v -> {
            dbHelper.deleteTask(task.getId());
            refreshTaskListCallback.run();
            Toast.makeText(v.getContext(), "Task Deleted", Toast.LENGTH_SHORT).show();
        });


        holder.completedSwitch.setOnCheckedChangeListener(null);
        holder.completedSwitch.setChecked(task.isCompleted());
        holder.completedSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatusText = isChecked ? "Completed" : "Incomplete";
            boolean updated = dbHelper.updateTaskCompletion(task.getId(), isChecked);

            if (updated) {
                Toast.makeText(buttonView.getContext(), "Task marked as " + newStatusText, Toast.LENGTH_SHORT).show();
                task.setCompleted(isChecked);

                holder.statusTextView.setText(newStatusText);
                holder.itemView.post(() -> notifyItemChanged(position));
                refreshTaskListCallback.run();

            } else {
                holder.completedSwitch.setChecked(!isChecked);

                Toast.makeText(buttonView.getContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                refreshTaskListCallback.run();

            }
        });
        holder.shareButton.setOnClickListener(v -> {
            String taskDetails = "Task: " + task.getTitle() + "\n" +
                    "Description: " + task.getDescription() + "\n" +
                    "Due Date: " + task.getDueDate() + " " + task.getDueTime() + "\n" +
                    "Priority: " + task.getPriority() + "\n" +
                    "Status: " + task.getStatus();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shared Task from To-Do App");
            shareIntent.putExtra(Intent.EXTRA_TEXT, taskDetails);
            activity.startActivity(Intent.createChooser(shareIntent, "Share Task via"));
        });


        holder.reminderButton.setOnClickListener(v -> {

            // If the task doesn't have a reminder already set
            if (!task.isHasReminder()) {
                // Create a TimePickerDialog for the user to select the time for the reminder
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        v.getContext(),
                        (view, year, monthOfYear, dayOfMonth) -> {
                            // Once the user selects the date, show the TimePickerDialog
                            TimePickerDialog timePickerDialog = new TimePickerDialog(
                                    v.getContext(),
                                    (timeView, hourOfDay, minute) -> {
                                        String selectedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                                        task.setReminderTime(hourOfDay, minute, selectedDate);
                                        task.setHasReminder(true);
                                        // Store snooze duration in shared preferences or task object
                                        task.setSnoozeDuration(2);  // Example: Snooze duration set to 10 minutes

                                        // Create a Calendar object to set the alarm time
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);

                                        setReminderAlarm(v.getContext(), task, calendar);
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

            }  else {
                Toast.makeText(v.getContext(), "Reminder already set for: " + task.getTitle(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void setReminderAlarm(Context context, Task task, Calendar calendar) {
        Intent intent = new Intent(context, ReminderBroadcast.class);
        intent.putExtra("TASK_ID", task.getId());
        intent.putExtra("TASK_TITLE", task.getTitle());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(context, "Reminder Set for: " + task.getTitle(), Toast.LENGTH_SHORT).show();

    }
    @Override
    public int getItemCount() {
        return taskList == null ? 0 : taskList.size();
    }
    //
    public void updateTaskList(List<Task> updatedTaskList) {
        this.taskList = updatedTaskList;
        notifyDataSetChanged();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, dueDateTextView,dueTimeTextView, priorityTextView, statusTextView;
        ImageButton editButton, deleteButton, reminderButton, shareButton;
        Switch completedSwitch;

        public TaskViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.taskTitle);
            descriptionTextView = itemView.findViewById(R.id.taskDescription);
            dueDateTextView = itemView.findViewById(R.id.taskDueDate);
            dueTimeTextView = itemView.findViewById(R.id.taskDueTime);
            priorityTextView = itemView.findViewById(R.id.taskPriority);
            statusTextView = itemView.findViewById(R.id.taskStatus);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            reminderButton = itemView.findViewById(R.id.reminderButton);
            completedSwitch = itemView.findViewById(R.id.completedSwitch);
            shareButton = itemView.findViewById(R.id.shareButton);  // New Share Button

        }
    }
}