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

    private final List<Task> tasks;
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

            if (filter.equals("PRIORITY")) {
                List<Task> sortedTasks = new ArrayList<>(tasks);
                sortedTasks.sort((t1, t2) -> t2.getPriority() - t1.getPriority());
                for (Task task : sortedTasks) {
                    System.out.println("Task: " + task.getDescription() + "\nStatus: " + task.getStatus() + "\nPriority: " + task.getPriority());
                    System.out.println("--------------------");
                }

                return;
            }

            if (!filter.equals("ALL")) TaskStatus.valueOf(filter);

            for (Task task : tasks) {
                if (filter.equals("ALL") || filter.equals(task.getStatus()))
                    System.out.println("Task: " + task.getDescription() + "\nStatus: " + task.getStatus() + "\nPriority: " + task.getPriority());
            }
        } catch (Exception e) {
            System.out.print("Invalid task filter, use 'ALL' to list all tasks or 'Priority' to list them by priority. List of filters: \n-TODO \n-IN_PROGRESS \n-COMPLETE");
        }

    }

    // Method to obtain a string of tasks from tasks.json
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

    // Method to convert tasks into a json string
    private String convertTasksToJson(List<Task> tasks) {
        StringBuilder jsonBuilder = new StringBuilder();
        String indent = "  "; // 2 spaces per indentation level
        jsonBuilder.append("[\n"); // Start JSON array with newline

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            jsonBuilder.append(indent).append("{\n"); // Indent task object

            // Append task fields with increased indentation
            String fieldIndent = indent.repeat(2);
            appendField(jsonBuilder, fieldIndent, "id", task.getId());
            appendField(jsonBuilder, fieldIndent, "priority", task.getPriority());
            appendField(jsonBuilder, fieldIndent, "description", escapeJson(task.getDescription()));
            appendField(jsonBuilder, fieldIndent, "status", task.getStatus());
            appendField(jsonBuilder, fieldIndent, "created", task.getCreated());
            appendField(jsonBuilder, fieldIndent, "updated", task.getUpdated());
            fixTrailingComma(jsonBuilder);
            jsonBuilder.append("\n").append(indent).append("}"); // Close task object

            // Add comma unless it's the last task
            if (i < tasks.size() - 1) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("\n");
        }

        jsonBuilder.append("]"); // Close JSON array
        return jsonBuilder.toString();
    }

    // Helper method to handle indentation
    private void appendField(StringBuilder builder, String indent, String key, Object value) {
        builder.append(indent)
                .append("\"").append(key).append("\": ");

        if (value instanceof String) {
            builder.append("\"").append(value).append("\"");
        } else {
            builder.append(value);
        }

        builder.append(",\n"); // Temporary comma (we'll remove the last one later)
    }

    private void fixTrailingComma(StringBuilder builder) {
        if (builder.length() >= 2) {
            builder.delete(builder.length() - 2, builder.length()); // Remove ",\n"
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

    private void saveTasksToFile() {
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
            if (description == null || status == null || created == null || updated == null) return null;

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
