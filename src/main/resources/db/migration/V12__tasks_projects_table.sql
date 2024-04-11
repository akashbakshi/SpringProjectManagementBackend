CREATE TABLE tasks_projects(
    taskId integer not null references tasks(task_id),
    projectId integer not null references projects(project_id)
);