Here is a draft for your README file based on the provided information:

---

# cli-task-tracker

cli-task-tracker is a lightweight CLI tool to track tasks, made in Java for learning purposes.

## Features

- Add, update, and delete tasks
- Mark a task as in progress or done
- List all tasks
- List all tasks that are done
- List all tasks sorted by priority
- List all tasks that are not done
- List all tasks that are in progress

## Installation

Clone the repository and compile the project:

```sh
git clone https://github.com/glass-source/cli-task-tracker.git
cd cli-task-tracker
# Compile the project
```

## Usage

- `add <description> <priority>`: Add a new task
- `list <filter>`: List all tasks. Valid filters are: ALL, TODO, IN_PROGRESS, and COMPLETE
- `status <id> <status>`: Update a task's status
- `update <id> <description>`: Update a task's description
- `delete <id>`: Delete a task
- `help`: Show the help message
- `exit`: Exit the application

## Examples

```sh
> add Buy Milk 10
> list all
> delete 1
```

---

You can now create a new README.md file in your repository with this content.
