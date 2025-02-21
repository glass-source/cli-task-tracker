package me.asunder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Task {
    private int id;
    private int priority; //Low number means high priority
    private String description;
    private String status;
    private String created;
    private String updated;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Task(int id, int priority, String description) {
        this.id = id;
        this.priority = priority;
        this.description = description;
        this.status = TaskStatus.TODO.name();
        this.created = formattedNow();
        this.updated = formattedNow();
    }

    public int getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        this.updated = formattedNow();
    }

    public void setDescription(String description) {
        this.description = description;
        this.updated = formattedNow();
    }

    public void setStatus(String status) {
        this.status = status;
        this.updated = formattedNow();
    }

    public void setCreated(String created) {
        this.created = created;
        this.updated = formattedNow();
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public void setDone() {
        this.status = TaskStatus.COMPLETE.toString();
        this.updated = formattedNow();
    }

    public void setInProgress() {
        this.status = TaskStatus.IN_PROGRESS.toString();
        this.updated = formattedNow();
    }

    public String formattedNow() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
}
