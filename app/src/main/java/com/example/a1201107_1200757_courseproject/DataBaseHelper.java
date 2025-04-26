package com.example.a1201107_1200757_courseproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DataBaseHelper extends SQLiteOpenHelper {
    User user;
    public DataBaseHelper(Context context, String name,
                          SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE USER (" +
                        "EMAIL TEXT PRIMARY KEY UNIQUE NOT NULL, " +
                        "FIRST TEXT NOT NULL, " +
                        "LAST TEXT NOT NULL, " +
                        "PASSWORD TEXT NOT NULL)"
        );
        sqLiteDatabase.execSQL(
                "CREATE TABLE TASKS (" +
                        "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "TITLE TEXT NOT NULL, " +
                        "DESCRIPTION TEXT, " +
                        "DUE_DATE TEXT NOT NULL, " +
                        "DUE_TIME TEXT NOT NULL, " +   // maybe fix the not null see later
                        "PRIORITY TEXT DEFAULT 'Medium', " +
                        "STATUS TEXT DEFAULT 'Incomplete', " +
                        "USER_EMAIL TEXT NOT NULL, " +
                        "REMINDER INTEGER DEFAULT 0," +
                        "FOREIGN KEY(USER_EMAIL) REFERENCES USER(EMAIL))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS USER");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS TASKS");
        onCreate(sqLiteDatabase);
    }


    public void insertUser(User user) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        // Match column names with database table
        contentValues.put("EMAIL", user.getmEmail());
        contentValues.put("FIRST", user.getmFirstName());
        contentValues.put("LAST", user.getmLastName());
        contentValues.put("PASSWORD", user.getmPassword());


        try {
            sqLiteDatabase.insertOrThrow("USER", null, contentValues);
        } catch (SQLiteConstraintException e) {
            // Handle uniqueness
            throw new IllegalArgumentException("email must be unique.");
        }
    }

    public boolean validateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.rawQuery("SELECT 1 FROM USER WHERE EMAIL = ? AND PASSWORD = ?", new String[]{email, password});
            exists = cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return exists;
    }

    public long insertTask(String title, String description, String dueDate, String dueTime, String priority, String status, String userEmail) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = dateFormat.parse(dueDate);
            dueDate = dateFormat.format(date);  // Standardize the format
        } catch (ParseException e) {
            Log.e("DataBaseHelper", "Invalid date format", e);
        }
        Log.d("DataBaseHelper", "Inserting task with due date: " + dueDate);
        Log.d("DataBaseHelper", "Inserting task with due time: " + dueTime);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TITLE", title);
        values.put("DESCRIPTION", description);
        values.put("DUE_DATE", dueDate);
        values.put("DUE_TIME", dueTime);
        values.put("PRIORITY", priority);
        values.put("STATUS", status);
        values.put("USER_EMAIL", userEmail);

        Log.d("DataBaseHelper", "Inserting task: " + title + ", due date: " + dueDate + " for user: " + userEmail);

        long taskId =db.insert("TASKS", null, values);
        Log.d("DataBaseHelper", "Task inserted successfully.");
        return taskId;

    }

    public List<Task> getTasksDueToday(String date, String userEmail) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date parsedDate = dateFormat.parse(date);
            date = dateFormat.format(parsedDate);  // Standardize the format
        } catch (ParseException e) {
            Log.e("DataBaseHelper", "Invalid date format", e);
        }
        SQLiteDatabase db = this.getReadableDatabase();
        List<Task> taskList = new ArrayList<>();
        Log.d("DataBaseHelper", "Fetching tasks for date: " + date + " and user: " + userEmail);

        // Correct the query to use the parameter 'date' (instead of todayDate)
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE DUE_DATE = ? AND USER_EMAIL = ?", new String[]{date, userEmail});

        if (cursor != null && cursor.moveToFirst()) {
            // Get the column indices once (better performance)
            int idIndex = cursor.getColumnIndex("ID");
            int titleIndex = cursor.getColumnIndex("TITLE");
            int descriptionIndex = cursor.getColumnIndex("DESCRIPTION");
            int dueDateIndex = cursor.getColumnIndex("DUE_DATE");
            int dueTimeIndex = cursor.getColumnIndex("DUE_TIME");
            int priorityIndex = cursor.getColumnIndex("PRIORITY");
            int statusIndex = cursor.getColumnIndex("STATUS");
            int userEmailIndex = cursor.getColumnIndex("USER_EMAIL");

            // Loop through the rows of the result
            do {
                // Check if each column index is valid
                if (idIndex != -1 && titleIndex != -1 && descriptionIndex != -1 && dueDateIndex != -1 && dueTimeIndex != -1 &&
                        priorityIndex != -1 && statusIndex != -1 && userEmailIndex != -1) {

                    Task task = new Task(
                            cursor.getInt(idIndex),                     // ID
                            cursor.getString(titleIndex),               // TITLE
                            cursor.getString(descriptionIndex),         // DESCRIPTION
                            cursor.getString(dueDateIndex),             // DUE_DATE
                            cursor.getString(dueTimeIndex),
                            cursor.getString(priorityIndex),            // PRIORITY
                            cursor.getString(statusIndex),              // STATUS
                            cursor.getString(userEmailIndex)            // USER_EMAIL
                    );
                    taskList.add(task);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        Log.d("DataBaseHelper", "Number of tasks fetched: " + cursor.getCount());

        return taskList;
    }


    public List<Task> getAllTasks(String userEmail) {
        SQLiteDatabase db = getReadableDatabase();
        List<Task> taskList = new ArrayList<>();

        // Query to fetch tasks for the user, ordered by due date and time
        Cursor cursor = db.rawQuery(
                "SELECT * FROM TASKS WHERE USER_EMAIL = ? ORDER BY DUE_DATE, DUE_TIME",
                new String[]{userEmail}
        );

        Log.d("DB_QUERY", "Executed query for user: " + userEmail); // Log query execution

        if (cursor != null && cursor.moveToFirst()) {
            // Get column indices once (better performance)
            int idIndex = cursor.getColumnIndex("ID");
            int titleIndex = cursor.getColumnIndex("TITLE");
            int descriptionIndex = cursor.getColumnIndex("DESCRIPTION");
            int dueDateIndex = cursor.getColumnIndex("DUE_DATE");
            int dueTimeIndex = cursor.getColumnIndex("DUE_TIME");
            int priorityIndex = cursor.getColumnIndex("PRIORITY");
            int statusIndex = cursor.getColumnIndex("STATUS");
            int userEmailIndex = cursor.getColumnIndex("USER_EMAIL");

            // Loop through the rows of the result
            do {
                // Check if each column index is valid
                if (idIndex != -1 && titleIndex != -1 && descriptionIndex != -1 && dueDateIndex != -1 &&
                        priorityIndex != -1 && statusIndex != -1 && userEmailIndex != -1) {

                    Task task = new Task(
                            cursor.getInt(idIndex),                     // ID
                            cursor.getString(titleIndex),               // TITLE
                            cursor.getString(descriptionIndex),         // DESCRIPTION
                            cursor.getString(dueDateIndex),             // DUE_DATE
                            cursor.getString(dueTimeIndex),             // DUE_TIME
                            cursor.getString(priorityIndex),            // PRIORITY
                            cursor.getString(statusIndex),              // STATUS
                            cursor.getString(userEmailIndex)            // USER_EMAIL
                    );
                    taskList.add(task);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        Log.d("DB_QUERY", "Fetched tasks: " + taskList.size()); // Log the fetched task count

        return taskList;
    }



    // Delete a task
    public boolean deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete("TASKS", "ID = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TITLE", task.getTitle());
        values.put("DESCRIPTION", task.getDescription());
        values.put("DUE_DATE", task.getDueDate());
        values.put("DUE_TIME", task.getDueTime());
        values.put("PRIORITY", task.getPriority());
        values.put("STATUS", task.getStatus());
        values.put("REMINDER", task.isHasReminder() ? 1 : 0);

        try {
            int rowsAffected = db.update("TASKS", values, "ID = ?", new String[]{String.valueOf(task.getId())});
            Log.d("UpdateTask", "Task ID: " + task.getId() + ", Rows Affected: " + rowsAffected);
            Log.d("UpdateTask", "Updated Task Details: " +
                    "Title: " + task.getTitle() +
                    ", Description: " + task.getDescription() +
                    ", Due Date: " + task.getDueDate() +
                    ", Due Time: " + task.getDueTime() +
                    ", Priority: " + task.getPriority() +
                    ", Status: " + task.getStatus());

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e("UpdateTask", "Error updating task", e);
            return false;
        } finally {
            db.close();
        }
    }

    public Task getTaskById(int taskId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE ID = ?", new String[]{String.valueOf(taskId)});

        if (cursor != null && cursor.moveToFirst()) {
            Task task = new Task(
                    cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
                    cursor.getString(cursor.getColumnIndexOrThrow("TITLE")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DESCRIPTION")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DUE_DATE")),
                    cursor.getString(cursor.getColumnIndexOrThrow("DUE_TIME")),
                    cursor.getString(cursor.getColumnIndexOrThrow("PRIORITY")),
                    cursor.getString(cursor.getColumnIndexOrThrow("STATUS")),
                    cursor.getString(cursor.getColumnIndexOrThrow("USER_EMAIL"))
            );
            cursor.close();
            return task;
        }
        return null;
    }


    public Map<String, List<Task>> getAllTasksGroupedByDay(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String, List<Task>> groupedTasks = new HashMap<>();

        // Query to fetch tasks for the user, ordered by due date and time
        Cursor cursor = db.rawQuery("SELECT * FROM TASKS WHERE USER_EMAIL = ? ORDER BY DUE_DATE DESC, DUE_TIME", new String[]{userEmail});

        if (cursor != null && cursor.moveToFirst()) {
            // Get the column indices once (better performance)
            int idIndex = cursor.getColumnIndex("ID");
            int titleIndex = cursor.getColumnIndex("TITLE");
            int descriptionIndex = cursor.getColumnIndex("DESCRIPTION");
            int dueDateIndex = cursor.getColumnIndex("DUE_DATE");
            int dueTimeIndex = cursor.getColumnIndex("DUE_TIME");
            int priorityIndex = cursor.getColumnIndex("PRIORITY");
            int statusIndex = cursor.getColumnIndex("STATUS");
            int userEmailIndex = cursor.getColumnIndex("USER_EMAIL");

            // Loop through the rows of the result
            do {
                if (idIndex != -1 && titleIndex != -1 && descriptionIndex != -1 && dueDateIndex != -1 &&
                        dueTimeIndex != -1 && priorityIndex != -1 && statusIndex != -1 && userEmailIndex != -1) {

                    String dueDate = cursor.getString(dueDateIndex);

                    // Create a new task
                    Task task = new Task(
                            cursor.getInt(idIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(descriptionIndex),
                            dueDate,
                            cursor.getString(dueTimeIndex),
                            cursor.getString(priorityIndex),
                            cursor.getString(statusIndex),
                            cursor.getString(userEmailIndex)
                    );

                    // Group tasks by due date
                    if (!groupedTasks.containsKey(dueDate)) {
                        groupedTasks.put(dueDate, new ArrayList<>());
                    }
                    groupedTasks.get(dueDate).add(task);
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        return groupedTasks;
    }

//    // Retrieve tasks with filtering
//    public List<Task> getFilteredTasks(String userEmail, String filterType) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        List<Task> taskList = new ArrayList<>();
//        String query = "SELECT * FROM TASKS WHERE USER_EMAIL = ?";
//
//        // Apply filter based on filterType
//        switch (filterType) {
//            case "Today":
//                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                String today = dateFormat.format(new Date());
//                query += " AND DUE_DATE = '" + today + "'";
//                break;
//            case "High Priority":
//                query += " AND PRIORITY = 'High'";
//                break;
//            case "Completed":
//                query += " AND STATUS = 'Completed'";
//                break;
//            case "Incomplete":
//                query += " AND STATUS = 'Incomplete'";
//                break;
//        }
//
//        Cursor cursor = db.rawQuery(query, new String[]{userEmail});
//
//        if (cursor != null && cursor.moveToFirst()) {
//            do {
//                Task task = new Task(
//                        cursor.getInt(cursor.getColumnIndex("ID")),
//                        cursor.getString(cursor.getColumnIndex("TITLE")),
//                        cursor.getString(cursor.getColumnIndex("DESCRIPTION")),
//                        cursor.getString(cursor.getColumnIndex("DUE_DATE")),
//                        cursor.getString(cursor.getColumnIndex("PRIORITY")),
//                        cursor.getString(cursor.getColumnIndex("STATUS")),
//                        cursor.getString(cursor.getColumnIndex("USER_EMAIL"))
//                );
//
//                // Check if reminder is set
//                task.setHasReminder(cursor.getInt(cursor.getColumnIndex("REMINDER")) == 1);
//
//                taskList.add(task);
//            } while (cursor.moveToNext());
//
//            cursor.close();
//        }
//
//        db.close();
//        return taskList;
//    }


    public Map<String, List<Task>> getCompletedTasksGroupedByDay(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String, List<Task>> groupedTasks = new HashMap<>();

        // Query to fetch tasks for the user, ordered by due date and time
        Cursor cursor = db.rawQuery(
                "SELECT * FROM TASKS WHERE USER_EMAIL = ? AND STATUS = ? ORDER BY DUE_DATE, DUE_TIME",
                new String[]{userEmail, "Completed"}
        );
        if (cursor != null && cursor.moveToFirst()) {
            // Get the column indices once (better performance)
            int idIndex = cursor.getColumnIndex("ID");
            int titleIndex = cursor.getColumnIndex("TITLE");
            int descriptionIndex = cursor.getColumnIndex("DESCRIPTION");
            int dueDateIndex = cursor.getColumnIndex("DUE_DATE");
            int dueTimeIndex = cursor.getColumnIndex("DUE_TIME");
            int priorityIndex = cursor.getColumnIndex("PRIORITY");
            int statusIndex = cursor.getColumnIndex("STATUS");
            int userEmailIndex = cursor.getColumnIndex("USER_EMAIL");

            // Loop through the rows of the result
            do {
                if (idIndex != -1 && titleIndex != -1 && descriptionIndex != -1 && dueDateIndex != -1 &&
                        dueTimeIndex != -1 && priorityIndex != -1 && statusIndex != -1 && userEmailIndex != -1) {

                    String dueDate = cursor.getString(dueDateIndex);

                    // Create a new task
                    Task task = new Task(
                            cursor.getInt(idIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(descriptionIndex),
                            dueDate,
                            cursor.getString(dueTimeIndex),
                            cursor.getString(priorityIndex),
                            cursor.getString(statusIndex),
                            cursor.getString(userEmailIndex)
                    );

                    // Group tasks by due date
                    if (!groupedTasks.containsKey(dueDate)) {
                        groupedTasks.put(dueDate, new ArrayList<>());
                    }
                    groupedTasks.get(dueDate).add(task);
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        return groupedTasks;
    }

    //    public void updateTaskStatus(int taskId, boolean isCompleted) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("completed", isCompleted ? 1 : 0);
//        db.update("tasks", values, "id=?", new String[]{String.valueOf(taskId)});
//    }
    // DataBaseHelper.java
    public boolean updateTaskCompletion(int taskId, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("STATUS", isCompleted ? "Completed" : "Incomplete");

        int rowsAffected = db.update("TASKS", values, "ID = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return rowsAffected > 0;
    }


    //
//    public List<Task> getTasksBySearchMethod(String startDate, String endDate, String keyword, String userEmail, String searchMethod) {
//        List<Task> taskList = new ArrayList<>();
//        SQLiteDatabase db = getReadableDatabase();
//
//        String query;
//        String[] selectionArgs;
//
//        if ("Date".equals(searchMethod)) {
//            // If the search method is Date, filter by the date range
//            query = "SELECT * FROM TASKS WHERE DUE_DATE BETWEEN ? AND ? AND USER_EMAIL = ? ORDER BY DUE_DATE, DUE_TIME";
//            selectionArgs = new String[]{startDate, endDate, userEmail};
//        } else if ("Keyword".equals(searchMethod)) {
//            // If the search method is Keyword, filter by title or description
//            if (keyword == null || keyword.isEmpty()) {
//                return taskList; // If the keyword is empty, return empty list
//            }
//
//            query = "SELECT * FROM TASKS WHERE (TITLE LIKE ? OR DESCRIPTION LIKE ?) AND USER_EMAIL = ? ORDER BY DUE_DATE, DUE_TIME";
//            selectionArgs = new String[]{"%" + keyword + "%", "%" + keyword + "%", userEmail};
//        } else {
//            // Handle invalid searchMethod or no method selected
//            return taskList;
//        }
//
//        Cursor cursor = null;
//        try {
//            cursor = db.rawQuery(query, selectionArgs);
//
//            if (cursor != null && cursor.moveToFirst()) {
//                // Cache column indices for performance
//                int idIndex = cursor.getColumnIndex("ID");
//                int titleIndex = cursor.getColumnIndex("TITLE");
//                int descriptionIndex = cursor.getColumnIndex("DESCRIPTION");
//                int dueDateIndex = cursor.getColumnIndex("DUE_DATE");
//                int dueTimeIndex = cursor.getColumnIndex("DUE_TIME");
//                int priorityIndex = cursor.getColumnIndex("PRIORITY");
//                int statusIndex = cursor.getColumnIndex("STATUS");
//                int userEmailIndex = cursor.getColumnIndex("USER_EMAIL");
//
//                do {
//                    if (idIndex != -1 && titleIndex != -1 && descriptionIndex != -1 && dueDateIndex != -1 &&
//                            dueTimeIndex != -1 && priorityIndex != -1 && statusIndex != -1 && userEmailIndex != -1) {
//
//                        Task task = new Task(
//                                cursor.getInt(idIndex),                     // ID
//                                cursor.getString(titleIndex),               // TITLE
//                                cursor.getString(descriptionIndex),         // DESCRIPTION
//                                cursor.getString(dueDateIndex),             // DUE_DATE
//                                cursor.getString(dueTimeIndex),             // DUE_TIME
//                                cursor.getString(priorityIndex),            // PRIORITY
//                                cursor.getString(statusIndex),              // STATUS
//                                cursor.getString(userEmailIndex)            // USER_EMAIL
//                        );
//                        taskList.add(task);
//                    }
//                } while (cursor.moveToNext());
//            }
//        } catch (Exception e) {
//            Log.e("DataBaseHelper", "Error fetching tasks by search method", e);
//        }
//
//        return taskList;
//    }
    public List<Task> getTasksBySearchMethod(String startDate, String endDate, String keyword, String userEmail, String searchMethod) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String query;
        String[] selectionArgs;

        if ("Date".equals(searchMethod)) {
            // Date search remains the same
            query = "SELECT * FROM TASKS WHERE DUE_DATE BETWEEN ? AND ? AND USER_EMAIL = ? ORDER BY DUE_DATE, DUE_TIME";
            selectionArgs = new String[]{startDate, endDate, userEmail};
        } else if ("Keyword".equals(searchMethod)) {
            if (keyword == null || keyword.isEmpty()) {
                return taskList;
            }

            // Modified keyword search to find the exact word
            query = "SELECT * FROM TASKS WHERE " +
                    // Match keyword surrounded by spaces, beginning of text, or end of text in title
                    "(TITLE LIKE '% ' || ? || ' %' OR " +
                    "TITLE LIKE ? || ' %' OR " +
                    "TITLE LIKE '% ' || ? OR " +
                    "TITLE = ? OR " +
                    // Match keyword surrounded by spaces, beginning of text, or end of text in description
                    "DESCRIPTION LIKE '% ' || ? || ' %' OR " +
                    "DESCRIPTION LIKE ? || ' %' OR " +
                    "DESCRIPTION LIKE '% ' || ? OR " +
                    "DESCRIPTION = ?) " +
                    "AND USER_EMAIL = ? " +
                    "ORDER BY DUE_DATE, DUE_TIME";

            // Each ? needs its corresponding parameter
            selectionArgs = new String[]{
                    keyword, // for '% keyword %' in title
                    keyword, // for 'keyword %' in title
                    keyword, // for '% keyword' in title
                    keyword, // for exact match in title
                    keyword, // for '% keyword %' in description
                    keyword, // for 'keyword %' in description
                    keyword, // for '% keyword' in description
                    keyword, // for exact match in description
                    userEmail
            };
        } else {
            return taskList;
        }

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, selectionArgs);

            if (cursor != null && cursor.moveToFirst()) {
                // Cache column indices for performance
                int idIndex = cursor.getColumnIndex("ID");
                int titleIndex = cursor.getColumnIndex("TITLE");
                int descriptionIndex = cursor.getColumnIndex("DESCRIPTION");
                int dueDateIndex = cursor.getColumnIndex("DUE_DATE");
                int dueTimeIndex = cursor.getColumnIndex("DUE_TIME");
                int priorityIndex = cursor.getColumnIndex("PRIORITY");
                int statusIndex = cursor.getColumnIndex("STATUS");
                int userEmailIndex = cursor.getColumnIndex("USER_EMAIL");

                do {
                    if (idIndex != -1 && titleIndex != -1 && descriptionIndex != -1 && dueDateIndex != -1 &&
                            dueTimeIndex != -1 && priorityIndex != -1 && statusIndex != -1 && userEmailIndex != -1) {

                        Task task = new Task(
                                cursor.getInt(idIndex),
                                cursor.getString(titleIndex),
                                cursor.getString(descriptionIndex),
                                cursor.getString(dueDateIndex),
                                cursor.getString(dueTimeIndex),
                                cursor.getString(priorityIndex),
                                cursor.getString(statusIndex),
                                cursor.getString(userEmailIndex)
                        );
                        taskList.add(task);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DataBaseHelper", "Error fetching tasks by search method", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return taskList;
    }

//    public List<Task> getTasksByDateRange(String startDate, String endDate, String userEmail) {
//        List<Task> taskList = new ArrayList<>();
//        SQLiteDatabase db = getReadableDatabase();
//
//        String query = "SELECT * FROM TASKS WHERE DUE_DATE BETWEEN ? AND ? AND USER_EMAIL = ?";
//        String[] selectionArgs = {startDate, endDate, userEmail};
//
//        try (Cursor cursor = db.rawQuery(query, selectionArgs)) {
//            if (cursor != null && cursor.moveToFirst()) {
//                // Cache column indices for performance
//                int idIndex = cursor.getColumnIndex("ID");
//                int titleIndex = cursor.getColumnIndex("TITLE");
//                int descriptionIndex = cursor.getColumnIndex("DESCRIPTION");
//                int dueDateIndex = cursor.getColumnIndex("DUE_DATE");
//                int priorityIndex = cursor.getColumnIndex("PRIORITY");
//                int statusIndex = cursor.getColumnIndex("STATUS");
//                int userEmailIndex = cursor.getColumnIndex("USER_EMAIL");
//
//                do {
//                    // Construct Task object using the indices
//                    Task task = new Task(
//                            cursor.getInt(idIndex),
//                            cursor.getString(titleIndex),
//                            cursor.getString(descriptionIndex),
//                            cursor.getString(dueDateIndex),
//                            cursor.getString(priorityIndex),
//                            cursor.getString(statusIndex),
//                            cursor.getString(userEmailIndex)
//                    );
//                    taskList.add(task);
//                } while (cursor.moveToNext());
//            }
//        } catch (Exception e) {
//            Log.e("DataBaseHelper", "Error fetching tasks by date range", e);
//        }
//
//        return taskList;
//    }


    public String[] getUserFirstAndLastName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT FIRST, LAST FROM USER WHERE EMAIL = ?", new String[]{email});

        String[] names = new String[2]; // First index for FIRST, second for LAST
        if (cursor != null && cursor.moveToFirst()) {
            names[0] = cursor.getString(cursor.getColumnIndexOrThrow("FIRST"));
            names[1] = cursor.getString(cursor.getColumnIndexOrThrow("LAST"));
        }
        if (cursor != null) {
            cursor.close();
        }
        return names;
    }
    // check for completed tasks iftodays tasks are completed
    public boolean areAllTodayTasksCompleted(String userEmail) {
        SQLiteDatabase db = getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Count total tasks for today
        String totalQuery = "SELECT COUNT(*) FROM TASKS WHERE DUE_DATE = ? AND USER_EMAIL = ?";
        Cursor totalCursor = db.rawQuery(totalQuery, new String[]{today, userEmail});

        // Count completed tasks for today
        String completedQuery = "SELECT COUNT(*) FROM TASKS WHERE DUE_DATE = ? AND USER_EMAIL = ? AND STATUS = 'Completed'";
        Cursor completedCursor = db.rawQuery(completedQuery, new String[]{today, userEmail});

        int totalTasks = 0;
        int completedTasks = 0;

        if (totalCursor.moveToFirst()) {
            totalTasks = totalCursor.getInt(0);
        }

        if (completedCursor.moveToFirst()) {
            completedTasks = completedCursor.getInt(0);
        }

        totalCursor.close();
        completedCursor.close();

        // Return true if there are tasks today and all are completed
        return totalTasks > 0 && totalTasks == completedTasks;
    }


}
