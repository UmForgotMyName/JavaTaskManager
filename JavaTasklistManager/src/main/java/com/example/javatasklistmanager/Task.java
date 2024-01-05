package com.example.javatasklistmanager;

import java.util.Date;

public class Task {
    private String title;
    private String description;
    private Date creationDate;
    private String dueDate;
    private int importance;
    private boolean completed;

    Task(String title, String description, String dueDate, int importance) {
        this.title = title;
        this.description = description;
        this.creationDate = new Date();
        this.dueDate = dueDate;
        this.importance = importance;
        this.completed = false;
    }

    Task(String title, String description, String dueDate, Date creationDate, int importance, boolean completed) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.creationDate = creationDate;
        this.importance = importance;
        this.completed = completed;
    }

    public boolean getCompleted(){
        return completed;
    }

    public void setCompleted(boolean completed){
        this.completed = completed;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }
}

