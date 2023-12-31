CREATE TABLE IF NOT EXISTS users
(
    id       int auto_increment
        primary key,
    email    varchar(255) not null,
    name     varchar(255) not null,
    password varchar(255) not null,
    constraint unique_name_constraint
        unique (name),
    constraint unique_email_constraint
        unique (email)
);