ALTER TABLE projects_status RENAME COLUMN projectid TO project_id;
ALTER TABLE projects_status RENAME COLUMN statusid TO status_id;

ALTER TABLE tasks_projects RENAME COLUMN projectid TO project_id;
ALTER TABLE tasks_projects RENAME COLUMN taskId TO task_id;