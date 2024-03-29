CREATE TABLE IF NOT EXISTS users_roles(
    userId varchar(512) references users (username),
    roleId integer references roles(role_id)
);