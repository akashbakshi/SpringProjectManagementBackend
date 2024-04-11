CREATE TABLE projects_status(
    projectId integer not null references projects(project_id),
    statusId integer not null references status(status_id)
);