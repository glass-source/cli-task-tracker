package me.asunder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaskManager {

    private List<Task> tasks;
    private final String filePath = "tasks.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public TaskManager() {
        tasks = loadTasks(readTasksFromFile());
    }

    public Task getTaskById(int id) {
        if (tasks.isEmpty()) {
            System.out.print("No tasks to get");
            return null;
        }

        Task task = tasks.stream().filter(t -> t.getId() == id).findFirst().orElse(null);

        if (task == null) {
            System.out.print("Task not found");
        }

        return task;
    }

    public void addTask(int priority, String description) {
        tasks.add(new Task(tasks.size() + 1, priority, description));
        saveTasksToFile();
    }

    public void deleteTask(int id) {
        if (tasks.isEmpty()) {
            System.out.print("No tasks to delete");
            return;
        }

        tasks.removeIf(task -> task.getId() == id);

        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setId(i + 1); // IDs are reassigned starting from 1
        }

        saveTasksToFile();
    }

    public void listTasks(String filter) {
        if (tasks.isEmpty()) {
            System.out.print("No tasks to list");
            return;
        }

        try {
            filter = filter.toUpperCase();

            if (!filter.equals("ALL")) TaskStatus.valueOf(filter);

            for (Task task : tasks) {
                if (filter.equals("ALL") || filter.equals(task.getStatus()))
                    System.out.println(task.getDescription() + ", Status: " + task.getStatus());
            }
        } catch (Exception e) {
            System.out.print("Invalid task filter, use 'ALL' to list all tasks. List of filters: \n-TODO \n-IN_PROGRESS \n-COMPLETE");
        }

    }

    public void updateTask(int id, int priority, String description) {
        Task task = getTaskById(id);
        if (task == null) {
            System.out.print("Task not found");
            return;
        }

        task.setPriority(priority);
        task.setDescription(description);
        saveTasksToFile();
        System.out.print("Task updated");
    }


    private String readTasksFromFile() {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                Files.createFile(path);
                return "[]"; // Return empty JSON array if file doesn't exist
            }

            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes);
        } catch (IOException e) {
            System.err.println("Failed to read tasks from file: " + e.getMessage());
            return "[]"; // Fallback to empty JSON array
        }
    }

    private String convertTasksToJson(List<Task> tasks) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("["); // Start JSON array

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            jsonBuilder.append("{"); // Start JSON object

            // Append task fields
            appendField(jsonBuilder, "id", task.getId());
            jsonBuilder.append(",");
            appendField(jsonBuilder, "priority", task.getPriority());
            jsonBuilder.append(",");
            appendField(jsonBuilder, "description", escapeJson(task.getDescription()));
            jsonBuilder.append(",");
            appendField(jsonBuilder, "status", task.getStatus());
            jsonBuilder.append(",");
            appendField(jsonBuilder, "created", task.getCreated());
            jsonBuilder.append(",");
            appendField(jsonBuilder, "updated", task.getUpdated());

            jsonBuilder.append("}"); // End JSON object

            if (i < tasks.size() - 1) {
                jsonBuilder.append(","); // Add comma between objects
            }
        }

        jsonBuilder.append("]"); // End JSON array
        return jsonBuilder.toString();
    }

    // Helper method to append a field
    private void appendField(StringBuilder builder, String key, Object value) {
        builder.append("\"").append(key).append("\":");
        if (value instanceof String) {
            builder.append("\"").append(value).append("\"");
        } else {
            builder.append(value);
        }
    }

    // Helper method to escape special JSON characters in strings
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public void saveTasksToFile() {
        try {
            String json = convertTasksToJson(tasks); // Use the StringBuilder method from earlier
            Path path = Paths.get(filePath);
            Files.write(path, json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to save tasks to file: " + e.getMessage());
        }
    }

    private List<Task> loadTasks(String json) {
        List<Task> tasks = new ArrayList<>();
        json = json.trim();

        // Handle empty or invalid JSON
        if (!json.startsWith("[") || !json.endsWith("]")) {
            return tasks; // Return empty list
        }

        // Remove the outer array brackets
        json = json.substring(1, json.length() - 1).trim();

        // Split JSON objects
        String[] taskStrings = json.split("},\\s*\\{");
        for (String taskString : taskStrings) {
            taskString = taskString.trim();
            if (taskString.startsWith("{")) {
                taskString = taskString.substring(1); // Remove leading {
            }

            if (taskString.endsWith("}")) {
                taskString = taskString.substring(0, taskString.length() - 1); // Remove trailing
            }

            Task task = parseTaskObject(taskString);
            if (task == null) continue;
            if (task.getDescription() == null || task.getStatus() == null || task.getCreated() == null || task.getUpdated() == null) continue;
            tasks.add(task);
        }

        if (tasks.isEmpty()) System.out.print("No tasks loaded");

        return tasks;
    }

    private Task parseTaskObject(String taskString) {
        try {
            int id = 0;
            int priority = 0;
            String description = null;
            String status = null;
            LocalDate created = null;
            LocalDate updated = null;

            String[] fields = taskString.split(",");
            for (String field : fields) {
                String[] keyValue = field.split(":", 2);
                if (keyValue.length != 2) continue;

                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim().replace("\"", "");

                switch (key) {
                    case "id":
                        id = Integer.parseInt(value);
                        break;
                    case "priority":
                        priority = Integer.parseInt(value);
                        break;
                    case "description":
                        description = value;
                        break;
                    case "status":
                        status = TaskStatus.valueOf(value).name();
                        break;
                    case "created":
                        created = LocalDate.parse(value, DATE_FORMATTER);
                        break;
                    case "updated":
                        updated = LocalDate.parse(value, DATE_FORMATTER);
                        break;
                }
            }

            Task task = new Task(id, priority, description);
            task.setStatus(status);
            task.setCreated(created.format(DATE_FORMATTER));
            task.setUpdated(updated.format(DATE_FORMATTER));
            return task;
        } catch (Exception e) {
            System.err.print("Failed to parse task: " + e.getMessage());
            return null; // Skip invalid tasks
        }
    }
}
