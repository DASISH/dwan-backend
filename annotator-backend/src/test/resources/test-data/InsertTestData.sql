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

INSERT INTO principal (principal_name, external_id) VALUES ('a user', '_test_uid_1_');
INSERT INTO principal (principal_name, external_id) VALUES ('b user', '_test_uid_2_');

INSERT INTO notebook (title, owner_id, external_id) VALUES ('a notebook', 1, '_test_nid_1_');
INSERT INTO annotation (owner_id,headline,body_xml, external_id) VALUES (1, 'a headline','<html><body>some html</body></html>', '_test_aid_1_');

INSERT INTO notebook (title, owner_id, external_id) VALUES ('a second notebook', 1, '_test_nid_2_');
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (1,1);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (2,1);
