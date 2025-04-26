package com.example.a1201107_1200757_courseproject;

import static android.content.ContentValues.TAG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.example.a1201107_1200757_courseproject.ui.All.AllFragment;

import java.util.Calendar;
import java.util.List;

public class ConnectionAsyncTask extends AsyncTask<String, String, String> {
    private AllFragment fragment;
    private DataBaseHelper dbHelper;

    private String userEmail;

    public ConnectionAsyncTask(AllFragment fragment) {
        this.fragment = fragment;
        this.dbHelper = new DataBaseHelper(fragment.getContext(), "DB_PROJECT", null, 3);

        // Get user email from SharedPreferences
        SharedPreferences sharedPreferences = fragment.getContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        this.userEmail = sharedPreferences.getString("logged_in_email", null);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        fragment.setProgress(true);
    }

    @Override
    protected String doInBackground(String... params) {
        return HttpManager.getData(params[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (s != null && userEmail != null) {
            List<Task> tasks = TaskJsonParser.getObjectFromJson(s);
            if (tasks != null && !tasks.isEmpty()) {
                try {
                    for (Task task : tasks) {
                        // Insert task into database
                        long taskId = dbHelper.insertTask(
                                task.getTitle(),
                                task.getDescription(),
                                task.getDueDate(),
                                task.getDueTime(),
                                task.getPriority(),
                                task.getStatus(),
                                userEmail
                        );

// Set the ID of the task object to the inserted task's ID
                        task.setId((int) taskId);  // Assuming Task's ID is of type int


                        // Set up reminder if task has one
                        if (task.isHasReminder()) {
                            setupReminderFromParsedTask(fragment.getContext(), task);
                        }
                    }
                    Toast.makeText(fragment.getContext(), "Tasks updated successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(fragment.getContext(), "Error updating tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                fragment.refreshTaskList();
            }
        }
        fragment.setProgress(false);
    }

    private void setupReminderFromParsedTask(Context context, Task task) {
        if (!task.isHasReminder()) return;

        Calendar calendar = Calendar.getInstance();

        // Parsing reminder date from the API (reminder_Date)
        String[] dateParts = task.getReminderDate().split("-");
        // Parsing reminder time (already split into hour and minute)
        int hour = task.getReminderHour();
        int minute = task.getReminderMinute();

        if (dateParts.length != 3) return;

        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Calendar months are 0-based
        int day = Integer.parseInt(dateParts[2]);

        // Set the calendar object with parsed date and time
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Only set the alarm if the reminder time is in the future
        if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
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
        }
    }
}
