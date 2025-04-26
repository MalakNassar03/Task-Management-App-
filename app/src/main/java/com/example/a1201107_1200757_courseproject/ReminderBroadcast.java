package com.example.a1201107_1200757_courseproject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;
public class ReminderBroadcast extends BroadcastReceiver {
    private static final String ACTION_SNOOZE = "com.example.ACTION_SNOOZE";
    private static final String ACTION_SHOW_SNOOZE_DIALOG = "com.example.SHOW_SNOOZE_DIALOG";
    private static final int DEFAULT_SNOOZE_DURATION = 10; // Default 10 minutes

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("TASK_ID", -1);
        String taskTitle = intent.getStringExtra("TASK_TITLE");
//        boolean isSnoozeAction = ACTION_SNOOZE.equals(intent.getAction());
        String action = intent.getAction();

        Log.d("ReminderBroadcast", "Received reminder for task ID: " + taskId + ", title: " + taskTitle);

        // Ensure the task exists before proceeding
        Task task = getTaskFromContext(context, taskId);
        if (task == null) {
            Log.e("ReminderBroadcast", "Task not found!");
            return;
        }

        // Handle snooze action first
//        if (isSnoozeAction) {
//            int snoozeDuration = task.getSnoozeDuration();
//            if (snoozeDuration <= 0) {
//                snoozeDuration = DEFAULT_SNOOZE_DURATION; // Fallback to default if not set
//                Log.d("ReminderBroadcast", "Using default snooze duration: " + snoozeDuration);
//            }
//            setSnoozeAlarm(context, task, snoozeDuration);
//            return;
//        }
        if (ACTION_SHOW_SNOOZE_DIALOG.equals(action)) {
            showSnoozeDialog(context, task);
            return;
        }

        // Create the notification for the task reminder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "TASK_REMINDER")
                .setSmallIcon(R.drawable.ic_reminder)
                .setContentTitle("Task Reminder")
                .setContentText("Don't forget to complete: " + taskTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Set up the snooze action
//        Intent snoozeIntent = new Intent(context, ReminderBroadcast.class);
//        snoozeIntent.setAction(ACTION_SNOOZE);
//        snoozeIntent.putExtra("TASK_ID", taskId);
//        snoozeIntent.putExtra("TASK_TITLE", taskTitle);
//        snoozeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
//                context,
//                taskId,
//                snoozeIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//
//        builder.addAction(R.drawable.ic_reminder, "Snooze", snoozePendingIntent);
        Intent dialogIntent = new Intent(context, ReminderBroadcast.class);
        dialogIntent.setAction(ACTION_SHOW_SNOOZE_DIALOG);
        dialogIntent.putExtra("TASK_ID", taskId);
        dialogIntent.putExtra("TASK_TITLE", taskTitle);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent dialogPendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                dialogIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        builder.addAction(R.drawable.ic_reminder, "Snooze", dialogPendingIntent);

        // Send the notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(taskId, builder.build());
    }
    private void showSnoozeDialog(Context context, Task task) {
        // Create an alert dialog with number picker
        AlertDialog.Builder builder = new AlertDialog.Builder(context, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.snooze_dialog, null);
        NumberPicker numberPicker = dialogView.findViewById(R.id.numberPicker);

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(60);
        numberPicker.setValue(task.getSnoozeDuration());

        builder.setTitle("Set Snooze Duration")
                .setMessage("Choose snooze duration in minutes")
                .setView(dialogView)
                .setPositiveButton("Set", (dialog, which) -> {
                    int duration = numberPicker.getValue();
                    task.setSnoozeDuration(duration);
                    setSnoozeAlarm(context, task, duration);
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        dialog.show();
    }
    public static void setSnoozeAlarm(Context context, Task task, int snoozeDuration) {
        // Debug log
        Log.d("ReminderBroadcast", "Setting snooze alarm with duration: " + snoozeDuration + " minutes");

        // Ensure snooze duration is positive
        if (snoozeDuration <= 0) {
            snoozeDuration = DEFAULT_SNOOZE_DURATION;
        }

        int snoozeDurationMillis = snoozeDuration * 60 * 1000; // Convert to milliseconds

        Calendar calendar = Calendar.getInstance();
        long currentTimeMillis = calendar.getTimeInMillis();
        long snoozeTimeMillis = currentTimeMillis + snoozeDurationMillis;

        // Debug log
        Log.d("ReminderBroadcast", "Current time: " + currentTimeMillis);
        Log.d("ReminderBroadcast", "Snooze time: " + snoozeTimeMillis);

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
                    snoozeTimeMillis,
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTimeMillis,
                    pendingIntent
            );
        }
    }

    private Task getTaskFromContext(Context context, int taskId) {
        DataBaseHelper dbHelper = new DataBaseHelper(context, "DB_PROJECT", null, 3);
        return dbHelper.getTaskById(taskId);
    }
}