CREATE TABLE IF NOT EXISTS roles(
    role_id SERIAL not null primary key,
    name varchar(256) not null,
    normalize_name varchar(256) not null
);