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

INSERT INTO notebook (title, owner_id, external_id) VALUES ('a notebook', 2, '00000000-0000-0000-0000-000000000001'); -- 1 
-- INSERT INTO notebook (title, owner_id, external_id) VALUES ('a notebook', 1, 1);
INSERT INTO annotation (owner_id,headline,body_xml, external_id) VALUES (1, 'a headline','<html><body>some html</body></html>', '00000000-0000-0000-0000-000000000005'); --1 

INSERT INTO notebook (title, owner_id, external_id) VALUES ('a second notebook', 2, '00000000-0000-0000-0000-000000000002'); --2
-- INSERT INTO notebook (title, owner_id, external_id) VALUES ('a second notebook', 1, 2);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (1,1);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (2,1);


-- Test data for jdbcAnnotationDao --

INSERT INTO principal (principal_name, external_id) VALUES ('Twan', '00000000-0000-0000-0000-000000000111'); --3 
INSERT INTO principal (principal_name, external_id) VALUES ('Peter', '00000000-0000-0000-0000-000000000112'); -- 4
INSERT INTO principal (principal_name, external_id) VALUES ('Olha', '00000000-0000-0000-0000-000000000113'); -- 5

INSERT INTO notebook (title, owner_id, external_id) VALUES ('Notebook 3', 3, '00000000-0000-0000-0000-000000000011'); -- 3
INSERT INTO notebook (title, owner_id, external_id) VALUES ('Notebook 4', 4, '00000000-0000-0000-0000-000000000012'); --4
INSERT INTO notebook (title, owner_id, external_id) VALUES ('Notebook 5', 5, '00000000-0000-0000-0000-000000000013'); --5
INSERT INTO notebook (title, owner_id, external_id) VALUES ('Notebook 6', 5, '00000000-0000-0000-0000-000000000014'); --6

INSERT INTO annotation (owner_id,headline,body_xml, external_id, time_stamp) VALUES (3, 'Sagrada Famiglia','<html><body>some html 1</body></html>', '00000000-0000-0000-0000-000000000021', '2013-08-12 11:25:00.383+02:00'); --2
INSERT INTO annotation (owner_id,headline,body_xml, external_id) VALUES (4, 'Gaudi','<html><body>some html 2 </body></html>', '00000000-0000-0000-0000-000000000022'); --3
INSERT INTO annotation (owner_id,headline,body_xml, external_id) VALUES (5, 'Art Nuveau','<html><body>some html 3</body></html>', '00000000-0000-0000-0000-000000000023'); --4
INSERT INTO annotation (owner_id,headline,body_xml, external_id) VALUES (3, 'Annotation to delete','<html><body>some html 4</body></html>', '00000000-0000-0000-0000-000000000024'); --5



INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (3,2);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (3,3);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (4,4);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (5,5);




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
INSERT INTO version (external_id, version) VALUES ('00000000-0000-0000-0000-000000000045', 'Art Nuveau wiki -version 2012'); --5 
INSERT INTO version (external_id, version) VALUES ('00000000-0000-0000-0000-000000000046', 'Art Nuveau wiki -version 2011'); --6  not used
INSERT INTO version (external_id, version) VALUES ('00000000-0000-0000-0000-000000000047', 'Art Nuveau wiki -version 2010'); --7

-- CREATE TABLE target_source (
--     source_id SERIAL UNIQUE NOT NULL,
--   external_id UUID UNIQUE NOT NULL,
--     time_stamp timestamp with time zone default now(),
--     link_uri text,
--     version_id integer REFERENCES version(version_id), ---- DIFFERS from the xml structure, 
--     -- SOUNDNESS: there must be exactly version at the version table  ++   
--     -- soundness: there must be at least one annotation referring to this source
-- );


INSERT INTO target_source (external_id, link_uri, version) VALUES ('00000000-0000-0000-0000-000000000031', 'http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia', '00000000-0000-0000-0000-000000000041'); -- 1
INSERT INTO target_source (external_id, link_uri, version) VALUES ('00000000-0000-0000-0000-000000000032', 'http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD', '00000000-0000-0000-0000-000000000043'); --2
INSERT INTO target_source (external_id, link_uri, version) VALUES ('00000000-0000-0000-0000-000000000033', 'http://en.wikipedia.org/wiki/Art_Nouveau', '00000000-0000-0000-0000-000000000044'); --3
INSERT INTO target_source (external_id, link_uri, version) VALUES ('00000000-0000-0000-0000-000000000034', '???', '00000000-0000-0000-0000-000000000045'); --4
INSERT INTO target_source (external_id, link_uri, version) VALUES ('00000000-0000-0000-0000-000000000035', '???', '00000000-0000-0000-0000-000000000045'); --5

-- CREATE TABLE annotations_target_sources (
--    annotation_id integer REFERENCES annotation(annotation_id), -- defining a foreign key: there must be a uniquely defined row in "annotation", that is defined by "annotation_id"
--    source_id integer REFERENCES target_source(source_id),
--    unique(annotation_id, source_id),
-- );

INSERT INTO annotations_target_sources (annotation_id, source_id) VALUES (2, 1); 
INSERT INTO annotations_target_sources (annotation_id, source_id) VALUES (2, 2); 
INSERT INTO annotations_target_sources (annotation_id, source_id) VALUES (3, 2); 
INSERT INTO annotations_target_sources (annotation_id, source_id) VALUES (4, 3); 
INSERT INTO annotations_target_sources (annotation_id, source_id) VALUES (5, 3); -- source 3 should not be deleted when annot 5 is deleted
INSERT INTO annotations_target_sources (annotation_id, source_id) VALUES (5, 4); -- source 4 to be deleted when annot 5 is deleted

----------------------------------------------------------------

-- CREATE TABLE sources_versions (
--     source_id integer REFERENCES target_source(source_id),
--     version_id integer REFERENCES version(version_id),
--     unique(source_id, version_id),
-- );

INSERT INTO sources_versions (source_id, version_id) VALUES (1, 1);
INSERT INTO sources_versions (source_id, version_id) VALUES (1, 2);
INSERT INTO sources_versions (source_id, version_id) VALUES (2, 3);
INSERT INTO sources_versions (source_id, version_id) VALUES (3, 4);
INSERT INTO sources_versions (source_id, version_id) VALUES (4, 5);
INSERT INTO sources_versions (source_id, version_id) VALUES (5, 7);

--------------------------------------------------------------

-- CREATE TABLE cached_representation_info (
--     cached_representation_id SERIAL UNIQUE NOT NULL,
--     external_id UUID UNIQUE NOT NULL,
--     mime_type text,
--     tool text,
--     type_ text, 
--     where_is_the_file text -- DIFFERS FROM the schema
-- );

INSERT INTO cached_representation_info (external_id, mime_type, tool, type_, where_is_the_file) VALUES ('00000000-0000-0000-0000-000000000051', 'text/html', 'latex', 'text', 'corpus1'); --1
INSERT INTO cached_representation_info (external_id, mime_type, tool, type_, where_is_the_file) VALUES ('00000000-0000-0000-0000-000000000052', 'text/html', 'vi', 'text', 'corpus1'); -- 2
INSERT INTO cached_representation_info (external_id, mime_type, tool, type_, where_is_the_file) VALUES ('00000000-0000-0000-0000-000000000053', 'image/png', 'screenshooter', 'image', 'corpus1'); -- 3
INSERT INTO cached_representation_info (external_id, mime_type, tool, type_, where_is_the_file) VALUES ('00000000-0000-0000-0000-000000000054', 'text/html', 'oxygen', 'text', 'corpus1'); --4 
INSERT INTO cached_representation_info (external_id, mime_type, tool, type_, where_is_the_file) VALUES ('00000000-0000-0000-0000-000000000055', 'image/jpg', 'photomaster', 'image', 'TLAscratch'); --5 
INSERT INTO cached_representation_info (external_id, mime_type, tool, type_, where_is_the_file) VALUES ('00000000-0000-0000-0000-000000000056', 'text/plain', 'some tool', 'text', 'TLAscratch'); --6 
INSERT INTO cached_representation_info (external_id, mime_type, tool, type_, where_is_the_file) VALUES ('00000000-0000-0000-0000-000000000057', 'text/html', 'some tool 2', 'text', 'TLAscratch'); --7 

--------------------------------------------------------------



-- CREATE TABLE versions_cached_representations (
--     version_id integer REFERENCES version(version_id),
--     cached_representation_id integer REFERENCES cached_representation_info(cached_representation_id),
--     unique(version_id, cached_representation_id),
-- );

INSERT INTO versions_cached_representations (version_id, cached_representation_id) VALUES (1, 1);
INSERT INTO versions_cached_representations (version_id, cached_representation_id) VALUES (2, 3);
INSERT INTO versions_cached_representations (version_id, cached_representation_id) VALUES (3, 2);
INSERT INTO versions_cached_representations (version_id, cached_representation_id) VALUES (4, 4);
INSERT INTO versions_cached_representations (version_id, cached_representation_id) VALUES (1, 5);
INSERT INTO versions_cached_representations (version_id, cached_representation_id) VALUES (5, 5);
INSERT INTO versions_cached_representations (version_id, cached_representation_id) VALUES (6, 5);


---- PERMISSIONS --------------------------------------------------------------------------------------------

INSERT INTO permission_(permission_mode) VALUES ('owner');
INSERT INTO permission_(permission_mode) VALUES ('writer');
INSERT INTO permission_(permission_mode) VALUES ('reader');

INSERT INTO annotations_principals_permissions (annotation_id, principal_id, permission_) VALUES (2, 3, 'owner');
INSERT INTO annotations_principals_permissions (annotation_id, principal_id, permission_) VALUES (2, 4, 'writer');
INSERT INTO annotations_principals_permissions (annotation_id, principal_id, permission_) VALUES (2, 5, 'reader');

INSERT INTO annotations_principals_permissions (annotation_id, principal_id, permission_) VALUES (3, 4, 'owner');
INSERT INTO annotations_principals_permissions (annotation_id, principal_id, permission_) VALUES (3, 3, 'reader');
INSERT INTO annotations_principals_permissions (annotation_id, principal_id, permission_) VALUES (3, 5, 'writer');

INSERT INTO annotations_principals_permissions (annotation_id, principal_id, permission_) VALUES (4, 5, 'owner');
INSERT INTO annotations_principals_permissions (annotation_id, principal_id, permission_) VALUES (4, 3, 'reader');
INSERT INTO annotations_principals_permissions (annotation_id, principal_id, permission_) VALUES (4, 4, 'reader');

INSERT INTO annotations_principals_permissions (annotation_id, principal_id, permission_) VALUES (5, 3, 'owner');
INSERT INTO annotations_principals_permissions (annotation_id, principal_id, permission_) VALUES (5, 4, 'writer');
INSERT INTO annotations_principals_permissions (annotation_id, principal_id, permission_) VALUES (5, 5, 'writer');
-- checking integrity control:
-- INSERT INTO annotations_principals_permissions (annotation_id, principal_id, permission_) VALUES (5, 5, 'reader');