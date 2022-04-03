create table if not exists posts(
	id serial primary key,
	title text,
	link text unique not null,
	description text,
	created timestamp
);