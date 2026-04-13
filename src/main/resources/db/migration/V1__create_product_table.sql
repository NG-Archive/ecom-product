CREATE TABLE product (
    id bigint not null auto_increment primary key,
    name varchar(255) not null,
    price bigint not null default 0,
    status varchar(20) not null,
    member_id bigint not null
);

CREATE INDEX idx_product_status_id ON product (status, id DESC);
