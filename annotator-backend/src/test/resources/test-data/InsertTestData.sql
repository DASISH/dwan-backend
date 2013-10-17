--
-- Copyright (C) 2013 DASISH
--
-- This program is free software; you can redistribute it and/or
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

INSERT INTO principal (principal_name, external_id, remote_id, e_mail) VALUES ('a user', '00000000-0000-0000-0000-000000000003', 'a1', 'a.user@gmail.com'); -- 1
INSERT INTO principal (principal_name, external_id, remote_id, e_mail) VALUES ('b user', '00000000-0000-0000-0000-000000000004', 'b2', 'b.user@gmail.com');-- 2

INSERT INTO notebook (title, owner_id, external_id) VALUES ('a notebook', 2, '00000000-0000-0000-0000-000000000001'); -- 1 
-- INSERT INTO notebook (title, owner_id, external_id) VALUES ('a notebook', 1, 1);

INSERT INTO annotation (owner_id,headline, body_text, body_mimetype, external_id, is_xml) VALUES (1, 'a headline', '<html><body>some html</body></html>', 'text/html' , '00000000-0000-0000-0000-000000000005', false); --1 

INSERT INTO notebook (title, owner_id, external_id) VALUES ('a second notebook', 2, '00000000-0000-0000-0000-000000000002'); --2
-- INSERT INTO notebook (title, owner_id, external_id) VALUES ('a second notebook', 1, 2);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (1,1);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (2,1);


-- Test data for jdbcAnnotationDao --

INSERT INTO principal (principal_name, external_id, remote_id, e_mail) VALUES ('Twan', '00000000-0000-0000-0000-000000000111', 'x3', 'twagoo@mpi.nl'); --3 
INSERT INTO principal (principal_name, external_id, remote_id, e_mail) VALUES ('Peter', '00000000-0000-0000-0000-000000000112', 'y4', 'Peter.Withers@mpi.nl'); -- 4
INSERT INTO principal (principal_name, external_id, remote_id, e_mail) VALUES ('Olha', '00000000-0000-0000-0000-000000000113', 'z5', 'olhsha@mpi.nl'); -- 5
INSERT INTO principal (principal_name, external_id, remote_id, e_mail) VALUES ('Eric', '00000000-0000-0000-0000-000000000114', 'v6', 'eriaue@mpi.nl'); -- 6


INSERT INTO notebook (title, owner_id, external_id) VALUES ('Notebook 3', 3, '00000000-0000-0000-0000-000000000011'); -- 3
INSERT INTO notebook (title, owner_id, external_id) VALUES ('Notebook 4', 4, '00000000-0000-0000-0000-000000000012'); --4
INSERT INTO notebook (title, owner_id, external_id) VALUES ('Notebook 5', 5, '00000000-0000-0000-0000-000000000013'); --5
INSERT INTO notebook (title, owner_id, external_id) VALUES ('Notebook 6', 5, '00000000-0000-0000-0000-000000000014'); --6

INSERT INTO annotation (owner_id,headline,body_text, body_mimetype, external_id, time_stamp, is_xml) VALUES (3, 'Sagrada Famiglia','<html><body>some html 1</body></html>', 'text/html' , '00000000-0000-0000-0000-000000000021', '2013-08-12 11:25:00.383+02:00', false); --2
INSERT INTO annotation (owner_id,headline,body_text, body_mimetype, external_id, is_xml) VALUES (4, 'Gaudi','<html><body>some html 2</body></html>', 'text/html' , '00000000-0000-0000-0000-000000000022',false); --3
INSERT INTO annotation (owner_id,headline,body_text, body_mimetype, external_id, is_xml) VALUES (5, 'Art Nuveau','some plain text', 'text/plain' , '00000000-0000-0000-0000-000000000023', false); --4
INSERT INTO annotation (owner_id,headline,body_text, body_mimetype, external_id, is_xml) VALUES (3, 'Annotation to delete','<html><body>some html 4</body></html>', 'text/html' , '00000000-0000-0000-0000-000000000024',false); --5



INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (3,2);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (3,3);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (4,4);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (5,4);


INSERT INTO target (external_id, link_uri, version) VALUES ('00000000-0000-0000-0000-000000000031', 'http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia', 'version 1.0'); -- 1
INSERT INTO target (external_id, link_uri, version) VALUES ('00000000-0000-0000-0000-000000000032', 'http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD', 'version 1.0'); --2
INSERT INTO target (external_id, link_uri, version) VALUES ('00000000-0000-0000-0000-000000000033', 'http://en.wikipedia.org/wiki/Art_Nouveau', 'june 1013'); --3
INSERT INTO target (external_id, link_uri, version) VALUES ('00000000-0000-0000-0000-000000000034', '???', 'back up'); --4
INSERT INTO target (external_id, link_uri, version) VALUES ('00000000-0000-0000-0000-000000000035', '???', 'back up'); --5
INSERT INTO target (external_id, link_uri, version) VALUES ('00000000-0000-0000-0000-000000000036', '???', 'version 2.0'); --6 not used by any annotation
INSERT INTO target (external_id, link_uri, version) VALUES ('00000000-0000-0000-0000-000000000037', '???', 'version 2.0')



INSERT INTO annotations_targets (annotation_id, target_id) VALUES (2, 1); 
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (2, 2);
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (3, 2); 
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (4, 3); 
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (5, 3); -- Target 3 should not be deleted when annot 5 is deleted
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (5, 4); -- Target 4 to be deleted when annot 5 is deleted
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (4, 5); 
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (4, 7); 

INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000051', 'text/html', 'latex', 'text', X'1001'); --1
INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000052', 'text/html', 'vi', 'text', X'1002'); -- 2
INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000053', 'image/png', 'screenshooter', 'image', X'1003'); -- 3
INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000054', 'text/html', 'oxygen', 'text', X'1004'); --4 
INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000055', 'image/jpg', 'photomaster', 'image', X'1005'); --5 
INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000056', 'text/plain', 'some tool', 'text', X'1006'); --6  not used
INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000057', 'text/html', 'some tool 2', 'text', X'1007'); --7 




INSERT INTO Targets_cached_representations (target_id,  cached_representation_id) VALUES (1, 1);
INSERT INTO Targets_cached_representations (target_id,  cached_representation_id) VALUES (1, 2);
INSERT INTO Targets_cached_representations (target_id,  cached_representation_id) VALUES (2, 3);
INSERT INTO Targets_cached_representations (target_id,  cached_representation_id) VALUES (3, 4);
INSERT INTO Targets_cached_representations (target_id,  cached_representation_id) VALUES (4, 5);
INSERT INTO Targets_cached_representations (target_id,  cached_representation_id) VALUES (5, 7);





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