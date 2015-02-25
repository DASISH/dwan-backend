--
-- Copyright (C) 2013 DASISH
--
-- This program is free software; you can redistribute it and/or
-- modify it under the terms of the GNU General Public License
-- as published by the Free Software Foundation; either version 2
-- of the License, or (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this program; if not, write to the Free Software
-- Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
--

CREATE TABLE roles (
role text UNIQUE NOT NULL
);


create table users(
username text not null primary key,
password text not null,
enabled boolean not null default 'true');

create table authorities (
username text not null,
authority text REFERENCES roles(role) not null default 'ROLE_USER',
constraint fk_authorities_users foreign key(username) references users(username));
create unique index ix_auth_username on authorities (username,authority);


INSERT INTO roles(role) VALUES ('ROLE_USER');

INSERT INTO  users(username, password) VALUES 
('alice@mail.domain','someverylonghash...');
INSERT INTO  users(username, password) VALUES ('bob@mail.domain','anotherlonghash...');

INSERT INTO  authorities(username, authority) VALUES ('alice@mail.domain', 'ROLE_USER');
INSERT INTO  authorities(username, authority) VALUES ('bob@mail.domain', 'ROLE_USER');