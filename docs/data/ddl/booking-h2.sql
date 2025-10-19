create table if not exists users (
  id            bigint auto_increment primary key,
  username      varchar(64)  not null unique,
  password_hash varchar(200) not null,
  role          varchar(20)  not null check (role in ('USER','ADMIN')),
  created_at    timestamp default current_timestamp
);
create table if not exists bookings (
  id          bigint auto_increment primary key,
  user_id     bigint not null,
  room_id     bigint not null,
  start_date  date   not null,
  end_date    date   not null,
  status      varchar(20) not null check (status in ('PENDING','CONFIRMED','CANCELLED')),
  request_id  varchar(64) not null,
  created_at  timestamp default current_timestamp,
  constraint uq_bookings_request unique (request_id),
  constraint fk_bookings_user foreign key (user_id) references users(id)
);
create index if not exists idx_bookings_user_dates on bookings(user_id, start_date, end_date);
