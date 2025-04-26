package com.example.a1201107_1200757_courseproject;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class TaskJsonParser {
    private static final String TAG = "TaskJsonParser";

    public static List<Task> getObjectFromJson(String json) {
        List<Task> tasks = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Debug log to see the raw JSON for each task
                Log.d(TAG, "Raw JSON for task: " + jsonObject.toString());

                Task task = parseTask(jsonObject);
                if (task != null) {
                    tasks.add(task);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
            return null;
        }
        return tasks;
    }

    private static Task parseTask(JSONObject jsonObject) throws JSONException {
        // Create base task
        Task task = new Task(
                -1,
                jsonObject.getString("Title"),
                jsonObject.optString("description", ""),
                jsonObject.getString("dueDate"),
                jsonObject.getString("dueTime"),
                jsonObject.optString("Priority", "Medium"),
                jsonObject.optString("status", "Incomplete"),
                null
        );

        // Parse reminder information with validation
        try {
            boolean hasReminder = jsonObject.optInt("reminder", 0) == 1;
            task.setHasReminder(hasReminder);

            if (hasReminder) {
                // Note the capital 'D' in reminder_Date
                String reminderDate = jsonObject.optString("reminder_Date", "");
                String reminderTime = jsonObject.optString("reminder_time", "00:00");
                Log.d("TaskDetails", "Task ID: " + task.getId() + ", Title: " + task.getTitle() +
                        ", Description: " + task.getDescription() + ", Due Date: " + task.getDueDate() +
                        ", Due Time: " + task.getDueTime() + ", Priority: " + task.getPriority() +
                        ", Status: " + task.getStatus());


                // Log the reminder values
                Log.d(TAG, String.format("Task: %s, Reminder enabled: %b, Date: %s, Time: %s",
                        task.getTitle(), hasReminder, reminderDate, reminderTime));

                // Only process if we have valid reminder data
                if (!reminderDate.equals("null") && !reminderTime.equals("null") &&
                        !reminderDate.isEmpty() && !reminderTime.isEmpty()) {

                    String[] timeParts = reminderTime.split(":");
                    if (timeParts.length == 2) {
                        try {
                            int hour = Integer.parseInt(timeParts[0]);
                            int minute = Integer.parseInt(timeParts[1]);

                            // Validate hour and minute ranges
                            if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                                task.setReminderTime(hour, minute, reminderDate);
                                Log.d(TAG, String.format("Successfully set reminder - Task: %s, Date: %s, Time: %02d:%02d",
                                        task.getTitle(), reminderDate, hour, minute));
                            } else {
                                Log.w(TAG, "Invalid time values for task: " + task.getTitle());
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Error parsing reminder time for task: " + task.getTitle(), e);
                        }
                    }
                } else {
                    Log.d(TAG, "Reminder data is null or empty for task: " + task.getTitle());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing reminder data for task: " + task.getTitle(), e);
        }

        return task;
    }
}