CREATE TABLE IF NOT EXISTS users(
    username varchar(512) not null primary key,
    password varchar(512) not null,
    email varchar(512) not null unique,
    name varchar(512) not null,
    is_archived boolean not null default false,
    created_on timestamp not null default now(),
    last_active timestamp
);