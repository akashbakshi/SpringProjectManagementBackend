CREATE TABLE tasks(
    task_id serial primary key not null,
    title varchar(512) not null,
    description varchar(4096),
    date_created timestamp not null default now(),
    status integer not null default 0,
    created_by varchar(512) not null references users(username),
    assigned_to varchar(512) references users(username)
);