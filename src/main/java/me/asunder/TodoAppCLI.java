package me.asunder;

import java.util.Scanner;

public class TodoAppCLI {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TaskManager taskManager = new TaskManager();

        System.out.println("Todo CLI Tool. Type 'help' for a list of commands.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                break; // Exit the loop
            }

            processCommand(input, taskManager);
        }

        scanner.close();
    }

    private static void processCommand(String input, TaskManager taskManager) {
        String[] parts = input.split("\\s+", 2); // Split into command and arguments
        String command = parts[0].toLowerCase();
        String arguments = (parts.length > 1) ? parts[1] : "";

        switch (command) {
            case "add":
                handleAddCommand(arguments, taskManager);
                break;
            case "list":
                handleListCommand(arguments, taskManager);
                break;
            case "update":
                handleUpdateDescriptionCommand(arguments, taskManager);
                break;
            case "status":
                handleUpdateStatusCommand(arguments, taskManager);
                break;
            case "delete":
                handleDeleteCommand(arguments, taskManager);
                break;
            case "help":
                displayHelp();
                break;
            default:
                System.out.println("Unknown command. Type 'help' for a list of commands.");
        }
    }

    private static void handleAddCommand(String arguments, TaskManager taskManager) {
        String[] args = arguments.split("\\s+", 2); // Split into description and priority
        if (args.length < 2) {
            System.out.println("Usage: add <description> <priority>");
            return;
        }

        try {
            String description = args[0];
            int priority = Integer.parseInt(args[1]);
            taskManager.addTask(priority, description);
            System.out.println("Task added successfully.");
        } catch (NumberFormatException e) {
            System.out.println("Priority must be a number.");
        }
    }

    private static void handleListCommand(String argument, TaskManager taskManager) {
        String[] args = argument.split("\\s+", 1); // Split argument to check for syntaxis error
        if (args.length > 1) {
            System.out.println("Usage: list <filter>");
            return;
        }

        taskManager.listTasks(args[0]);
    }

    private static void handleUpdateStatusCommand(String arguments, TaskManager taskManager) {
        String[] args = arguments.split("\\s+", 2); // Split into id and status
        if (args.length < 2) {
            System.out.println("Usage: update <id> <status>");
            return;
        }

        try {
            int id = Integer.parseInt(args[0]);
            Task task = taskManager.getTaskById(id);
            TaskStatus status = TaskStatus.valueOf(args[1].toUpperCase());

            switch (status) {
                case COMPLETE -> task.setDone();
                case IN_PROGRESS -> task.setInProgress();
                default -> throw new IllegalArgumentException("Invalid status.");
            }

            System.out.println("Task status updated successfully.");
        } catch (NumberFormatException e) {
            System.out.println("ID must be a number.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status. Use TODO, IN_PROGRESS, or COMPLETE.\n");
        }
    }

    private static void handleUpdateDescriptionCommand(String arguments, TaskManager taskManager) {
        String[] args = arguments.split("\\s+", 2);        // tus
        if (args.length < 2) {
            System.out.println("Usage: update <id> <description>");
            return;
        }

        try {
            int id = Integer.parseInt(args[0]);
            Task task = taskManager.getTaskById(id);
            task.setDescription(args[1]);

            System.out.println("Task description updated successfully.");
        } catch (NumberFormatException e) {
            System.out.println("ID must be a number.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid syntaxes, do not use special characters in the description.");
        }
    }

    private static void handleDeleteCommand(String arguments, TaskManager taskManager) {
        try {
            int id = Integer.parseInt(arguments.trim());
            taskManager.deleteTask(id);
            System.out.println("Task deleted successfully.");
        } catch (NumberFormatException e) {
            System.out.println("ID must be a number.");
        }
    }

    private static void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("  add <description> <priority> - Add a new task");
        System.out.println("  list <filter> - List all tasks. Valid filters are: ALL, TODO, IN_PROGRESS and COMPLETE");
        System.out.println("  status <id> <status> - Update a task's status");
        System.out.println("  update <id> <description> - Update a task's description");
        System.out.println("  delete <id> - Delete a task");
        System.out.println("  help - Show this help message");
        System.out.println("  exit - Exit the application");
    }
}
