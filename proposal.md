// Use DBML to define your database structure
// Docs: https://dbml.dbdiagram.io/docs

Table events {
  creator_user_id integer [not null]
  rsvp_user_id integer [not null]
  created_at timestamp
}

Table users {
  id integer [primary key]
  username varchar
  role varchar
  created_at timestamp
}

Table locations {
  id integer [primary key]
  location_name varchar
  address varchar
  desc text [note: 'Content of the post']
  user_id integer [not null]
  created_at timestamp
}

Ref user_posts: locations.user_id ?> users.id // many-to-one

Ref: users.id <? events.rsvp_user_id

Ref: users.id <? events.creator_user_id

Records users(id, username, role) {
  0, 'TestAdmin', 'admin'
  1, 'TestRSVP', 'member'
}

Records events(rsvp_user_id, creator_user_id, created_at) {
  1, 0, '2026-01-01'
  3, 2, '2026-02-28'
}

Records locations(id, location_name, address, desc, user_id) {
  0, 'CSUMB', '100 Campus Ctr, Seaside, CA', "College in Seaside, CA", 0
}