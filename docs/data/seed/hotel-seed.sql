insert into hotels (name, address) values
  ('Hotel Mercury', 'Berlin, Unter den Linden 1'),
  ('Hotel Aurora',  'Munich, Marienplatz 5');
insert into rooms (hotel_id, number, available, times_booked) values
  (1, '101', true, 0),
  (1, '102', true, 1),
  (1, '103', false, 0),
  (2, '201', true, 2),
  (2, '202', true, 0);
