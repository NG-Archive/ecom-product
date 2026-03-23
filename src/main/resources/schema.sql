CREATE TABLE IF NOT EXISTS product (
    id bigint not null auto_increment primary key,
    name varchar(255) not null,
    price bigint not null default 0
);
