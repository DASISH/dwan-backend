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

INSERT INTO principal (principal_name, external_id) VALUES ('a user', '00000000-0000-0000-0000-000000000003'); -- 1
INSERT INTO principal (principal_name, external_id) VALUES ('b user', '00000000-0000-0000-0000-000000000004');-- 2

INSERT INTO notebook (title, owner_id, external_id) VALUES ('a notebook', 2, '00000000-0000-0000-0000-000000000001');
-- INSERT INTO notebook (title, owner_id, external_id) VALUES ('a notebook', 1, 1);
INSERT INTO annotation (owner_id,headline,body_xml, external_id) VALUES (1, 'a headline','<html><body>some html</body></html>', '00000000-0000-0000-0000-000000000005');

INSERT INTO notebook (title, owner_id, external_id) VALUES ('a second notebook', 2, '00000000-0000-0000-0000-000000000002');
-- INSERT INTO notebook (title, owner_id, external_id) VALUES ('a second notebook', 1, 2);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (1,1);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (2,1);


-- Test data for jdbcAnnotationDao --

INSERT INTO principal (principal_name, external_id) VALUES ('Twan', '00000000-0000-0000-0000-000000000111'); --3 
INSERT INTO principal (principal_name, external_id) VALUES ('Peter', '00000000-0000-0000-0000-000000000112'); -- 4
INSERT INTO principal (principal_name, external_id) VALUES ('Olha', '00000000-0000-0000-0000-000000000113'); -- 5

INSERT INTO notebook (notebook_id, title, owner_id, external_id) VALUES (11, 'Notebook 11', 3, '00000000-0000-0000-0000-000000000011');
INSERT INTO notebook (notebook_id, title, owner_id, external_id) VALUES (12, 'Notebook 12', 4, '00000000-0000-0000-0000-000000000012');
INSERT INTO notebook (notebook_id, title, owner_id, external_id) VALUES (13, 'Notebook 13', 5, '00000000-0000-0000-0000-000000000013');

INSERT INTO annotation (annotation_id, owner_id,headline,body_xml, external_id) VALUES (21, 3, 'Sagrada Famiglia','<html><body>some html 1</body></html>', '00000000-0000-0000-0000-000000000021');
INSERT INTO annotation (annotation_id, owner_id,headline,body_xml, external_id) VALUES (22, 4, 'Gaudi','<html><body>some html 2 </body></html>', '00000000-0000-0000-0000-000000000022');
INSERT INTO annotation (annotation_id, owner_id,headline,body_xml, external_id) VALUES (23, 5, 'Art Nuveau','<html><body>some html 3</body></html>', '00000000-0000-0000-0000-000000000023');
INSERT INTO annotation (annotation_id, owner_id,headline,body_xml, external_id) VALUES (25, 3, 'Annotation to delete','<html><body>some html 4</body></html>', '00000000-0000-0000-0000-000000000025');



INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (11,21);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (11,22);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (12,23);




-- CREATE TABLE version (
--     version_id SERIAL UNIQUE NOT NULL,
--     external_id UUID UNIQUE NOT NULL,
--     version text,
--     --  SOUNDNESS: there must be at least one row with this version_id in the verions_cached_representations table
-- );

INSERT INTO version (external_id, version) VALUES ('00000000-0000-0000-0000-000000000041', 'SF-version 2013'); -- 1
INSERT INTO version (external_id, version) VALUES ('00000000-0000-0000-0000-000000000042', 'SF-version 2012'); -- 2
INSERT INTO version (external_id, version) VALUES ('00000000-0000-0000-0000-000000000043', 'Gaudi wiki -version 2013'); -- 3
INSERT INTO version (external_id, version) VALUES ('00000000-0000-0000-0000-000000000044', 'Art Nuveau wiki -version 2013'); --4 

-- CREATE TABLE target_source (
--     source_id SERIAL UNIQUE NOT NULL,
--   external_id UUID UNIQUE NOT NULL,
--     time_stamp timestamp with time zone default now(),
--     link_uri text,
--     version_id integer REFERENCES version(version_id), ---- DIFFERS from the xml structure, 
--     -- SOUNDNESS: there must be exactly version at the version table  ++   
--     -- soundness: there must be at least one annotation referring to this source
-- );


INSERT INTO target_source (external_id, link_uri, version_id) VALUES ('00000000-0000-0000-0000-000000000031', 'http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia', 1);
INSERT INTO target_source (external_id, link_uri, version_id) VALUES ('00000000-0000-0000-0000-000000000032', 'http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD', 3);
INSERT INTO target_source (external_id, link_uri, version_id) VALUES ('00000000-0000-0000-0000-000000000033', 'http://en.wikipedia.org/wiki/Art_Nouveau', 4);

