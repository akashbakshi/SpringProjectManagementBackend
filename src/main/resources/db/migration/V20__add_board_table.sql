CREATE TABLE boards
(
    board_id        serial primary key not null,
    name            varchar(256)       not null,
    goal            varchar(512),
    date_created    timestamp default now(),
    target_due_date timestamp,
    is_active boolean not null default true
);