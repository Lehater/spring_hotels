create table if not exists hotels (
  id      bigint auto_increment primary key,
  name    varchar(200) not null,
  address varchar(300)
);
create table if not exists rooms (
  id           bigint auto_increment primary key,
  hotel_id     bigint not null,
  number       varchar(50) not null,
  available    boolean not null default true,
  times_booked int not null default 0,
  constraint fk_rooms_hotel foreign key (hotel_id) references hotels(id),
  constraint uq_rooms_hotel_number unique (hotel_id, number)
);
create table if not exists room_holds (
  id          bigint auto_increment primary key,
  room_id     bigint not null,
  start_date  date   not null,
  end_date    date   not null,
  booking_id  varchar(64) not null,
  request_id  varchar(64) not null,
  status      varchar(20) not null check (status in ('HELD','RELEASED','COMMITTED')),
  expires_at  timestamp,
  constraint fk_holds_room foreign key (room_id) references rooms(id),
  constraint uq_holds_request unique (request_id)
);
create index if not exists idx_holds_room_dates on room_holds(room_id, start_date, end_date);
create index if not exists idx_rooms_hotel on rooms(hotel_id);
