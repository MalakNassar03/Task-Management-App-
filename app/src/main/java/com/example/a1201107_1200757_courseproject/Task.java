package com.example.a1201107_1200757_courseproject;

public class Task {
    private int id;
    private String title;
    private String description;
    private String dueDate;
    private String dueTime;
    private String priority;
    private String status;
    private String userEmail;
    private boolean hasReminder;
    private boolean completed;
    private int reminderHour;
    private int reminderMinute;
    private String reminderDate;
    private int snoozeDuration = 5;

    public Task(int id, String title, String description, String dueDate, String dueTime, String priority, String status, String userEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.priority = priority;
        this.status = status;
        this.userEmail = userEmail;
    }
    public void setReminderTime(int hour, int minute, String date) {
        this.reminderHour = hour;
        this.reminderMinute = minute;
        this.reminderDate = date;
    }
    public void setSnoozeDuration(int minutes) {
        if (minutes <= 0) {
            minutes = 5; // Enforce minimum snooze duration
        }
        this.snoozeDuration = minutes;
    }

    public int getSnoozeDuration() {
        return snoozeDuration;
    }
    // Optionally, add a getter to retrieve the reminder time if needed
    public int getReminderHour() {
        return reminderHour;
    }

    public int getReminderMinute() {
        return reminderMinute;
    }
    public String getReminderDate() { return reminderDate; }

    public boolean isCompleted() {
        return "Completed".equalsIgnoreCase(this.status);
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    public String getDueTime() {
        return dueTime;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isHasReminder() {
        return hasReminder;
    }

    public void setHasReminder(boolean hasReminder) {
        this.hasReminder = hasReminder;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}

