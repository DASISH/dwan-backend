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


INSERT INTO access(access_mode) VALUES ('write');
INSERT INTO access(access_mode) VALUES ('read');
INSERT INTO access(access_mode) VALUES ('none');

INSERT INTO principal (principal_name, external_id, remote_id, e_mail, account) VALUES ('Twan', '00000000-0000-0000-0000-000000000111', 'twagoo@mpi.nl', 'Twan.Goosen@mpi.nl', 'developer'); --1 
INSERT INTO principal (principal_name, external_id, remote_id, e_mail, account) VALUES ('Peter', '00000000-0000-0000-0000-000000000112', 'petwit@mpi.nl', 'Peter.Withers@mpi.nl', 'developer'); -- 2
INSERT INTO principal (principal_name, external_id, remote_id, e_mail, account) VALUES ('Olha', '00000000-0000-0000-0000-000000000113', 'olhsha@mpi.nl', 'Olha.Shakaravska@mpi.nl', 'admin'); -- 3
INSERT INTO principal (principal_name, external_id, remote_id, e_mail, account) VALUES ('Eric', '00000000-0000-0000-0000-000000000114', 'ericaprincipal@mpi.nl', 'Eric.Auer@mpi.nl', 'developer'); -- 4
INSERT INTO principal (principal_name, external_id, remote_id, e_mail, account) VALUES ('Daan', '00000000-0000-0000-0000-000000000115', 'broeder@mpi.ml', 'Daan.Broeder@mpi.nl', 'developer'); --5 
INSERT INTO principal (principal_name, external_id, remote_id, e_mail, account) VALUES ('Menzo', '00000000-0000-0000-0000-000000000116', 'menwin@mpi.nl', 'Menzo.Windhouwer@mpi.nl', 'developer'); -- 6
INSERT INTO principal (principal_name, external_id, remote_id, e_mail, account) VALUES ('Przemek', '00000000-0000-0000-0000-000000000117', 'przlen@mpi,nl', 'Przemek.Lenkiewicz@mpi.nl', 'principal'); -- 7
INSERT INTO principal (principal_name, external_id, remote_id, e_mail, account) VALUES ('Stephanie', '00000000-0000-0000-0000-000000000118', 'gusrotst@gu.se', 'stephanie.roth@gu.se', 'developer'); -- 8
INSERT INTO principal (principal_name, external_id, remote_id, e_mail, account) VALUES ('Olof', '00000000-0000-0000-0000-000000000119', 'gusolsol@gu.se', 'olof.olsson.2@gu.se', 'developer'); -- 9
INSERT INTO principal (principal_name, external_id, remote_id, e_mail, account) VALUES ('Willem', '00000000-0000-0000-0000-000000000220', 'wilelb@mpi.nl', 'Willem.Elbers@mpi.nl', 'developer'); -- 10
INSERT INTO principal (principal_name, external_id, remote_id, e_mail, account) VALUES ('Olaf', '00000000-0000-0000-0000-000000000221', 'olasei@mpi.nl', 'Olaf.Seibert@mpi.nl', 'developer'); -- 11


INSERT INTO notebook (title, owner_id, external_id, last_modified) VALUES ('Notebook 1', 1, '00000000-0000-0000-0000-000000000011', '2013-08-12 09:25:00.383'); -- 1
INSERT INTO notebook (title, owner_id, external_id) VALUES ('Notebook 2', 2, '00000000-0000-0000-0000-000000000012'); --2
INSERT INTO notebook (title, owner_id, external_id) VALUES ('Notebook 3', 3, '00000000-0000-0000-0000-000000000013'); --3
INSERT INTO notebook (title, owner_id, external_id) VALUES ('Notebook 4', 3, '00000000-0000-0000-0000-000000000014'); --4

INSERT INTO annotation (owner_id, headline,body_text, body_mimetype, external_id, last_modified, is_xml, public_) VALUES (1, 'Sagrada Famiglia','<html><body>some html 1</body></html>', 'text/html' , '00000000-0000-0000-0000-000000000021', '2013-08-12 09:25:00.383', false, 'write'); --1
INSERT INTO annotation (owner_id, headline,body_text, body_mimetype, external_id, last_modified, is_xml, public_) VALUES (2, 'Gaudi','<html><body>some html 2</body></html>', 'text/html' , '00000000-0000-0000-0000-000000000022', '2013-08-12 10:30:00.383', false, 'read'); --2
INSERT INTO annotation (owner_id, headline,body_text, body_mimetype, external_id, last_modified, is_xml) VALUES (3, 'Art Nuveau','some plain text', 'text/plain' , '00000000-0000-0000-0000-000000000023', '2013-08-12 11:30:00.383', false); --3
INSERT INTO annotation (owner_id, headline,body_text, body_mimetype, external_id, is_xml) VALUES (3, 'Annotation to delete','<html><body>some html 4</body></html>', 'text/html' , '00000000-0000-0000-0000-000000000024',false); --4



INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (1,1);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (1,2);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (2,3);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (4,3);


INSERT INTO target (external_id, link_uri, version, fragment_descriptor) VALUES ('00000000-0000-0000-0000-000000000031', 'http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia', 'version 1.0', 'de_Opdracht'); -- 1
INSERT INTO target (external_id, link_uri, version, fragment_descriptor) VALUES ('00000000-0000-0000-0000-000000000032', 'http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD', 'version 1.1', 'Vroege_werk'); --2
INSERT INTO target (external_id, link_uri, version, fragment_descriptor) VALUES ('00000000-0000-0000-0000-000000000033', 'http://en.wikipedia.org/wiki/Art_Nouveau', 'june 1013', 'Spain'); --3
INSERT INTO target (external_id, link_uri, version, fragment_descriptor) VALUES ('00000000-0000-0000-0000-000000000034', '???', 'back up', '1111'); --4
INSERT INTO target (external_id, link_uri, version, fragment_descriptor) VALUES ('00000000-0000-0000-0000-000000000035', '???', 'back up', '111111'); --5
INSERT INTO target (external_id, link_uri, version, fragment_descriptor) VALUES ('00000000-0000-0000-0000-000000000036', '???', 'version 2.0', '1111111'); --6 not used by any annotation
INSERT INTO target (external_id, link_uri, version, fragment_descriptor) VALUES ('00000000-0000-0000-0000-000000000037', '???', 'version 2.0', '11111111'); --7



INSERT INTO annotations_targets (annotation_id, target_id) VALUES (1, 1); 
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (1, 2);
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (2, 2); 
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (3, 3); 
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (4, 3); -- Target 3 should not be deleted when annot 4 is deleted
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (4, 4); -- Target 4 to be deleted when annot 4 is deleted
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (3, 5); 
INSERT INTO annotations_targets (annotation_id, target_id) VALUES (3, 7); 

-- HSQL ---
INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000051', 'image/png', 'screen-shot', 'image', NULL); --1
INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000052', 'text/html', 'vi', 'text', X'1002'); -- 2
INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000053', 'image/png', 'screenshooter', 'image', NULL); -- 3
INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000054', 'image/png', 'screen-shot', 'image', NULL); --4
INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000055', 'image/jpg', 'photomaster', 'image', X'1005'); --5 
INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000056', 'text/plain', 'some tool', 'text', X'1006'); --6  not used
INSERT INTO cached_representation (external_id, mime_type, tool, type_, file_) VALUES ('00000000-0000-0000-0000-000000000057', 'text/html', 'some tool 2', 'text', X'1007'); --7 

 -- PostgreSQL --
-- INSERT INTO cached_representation (external_id, mime_type, tool, type_) VALUES ('00000000-0000-0000-0000-000000000051', 'image/png', 'some tool 1', 'image'); --1
-- INSERT INTO cached_representation (external_id, mime_type, tool, type_) VALUES ('00000000-0000-0000-0000-000000000052', 'image/png', 'some tool 2', 'image'); -- 2
-- INSERT INTO cached_representation (external_id, mime_type, tool, type_) VALUES ('00000000-0000-0000-0000-000000000053', 'image/png', 'some tool 3', 'image'); -- 3
-- INSERT INTO cached_representation (external_id, mime_type, tool, type_) VALUES ('00000000-0000-0000-0000-000000000054', 'image/png', 'some tool 4', 'image'); --4 
-- INSERT INTO cached_representation (external_id, mime_type, tool, type_) VALUES ('00000000-0000-0000-0000-000000000055', 'image/png', 'some tool 5', 'image'); --5 
-- INSERT INTO cached_representation (external_id, mime_type, tool, type_) VALUES ('00000000-0000-0000-0000-000000000056', 'image/png', 'some tool 6', 'image'); --6  not used
-- INSERT INTO cached_representation (external_id, mime_type, tool, type_) VALUES ('00000000-0000-0000-0000-000000000057', 'image/png', 'some tool 7', 'image'); --7 



INSERT INTO targets_cached_representations (target_id,  cached_representation_id, fragment_descriptor_in_cached) VALUES (1, 1, 'De Opdracht');
INSERT INTO targets_cached_representations (target_id,  cached_representation_id, fragment_descriptor_in_cached) VALUES (1, 2, '(0,0)');
INSERT INTO targets_cached_representations (target_id,  cached_representation_id, fragment_descriptor_in_cached) VALUES (2, 3, 'Vroeger Werk');
INSERT INTO targets_cached_representations (target_id,  cached_representation_id, fragment_descriptor_in_cached) VALUES (3, 4, 'Spain');
INSERT INTO targets_cached_representations (target_id,  cached_representation_id, fragment_descriptor_in_cached) VALUES (4, 5, '(1,1)');
INSERT INTO targets_cached_representations (target_id,  cached_representation_id, fragment_descriptor_in_cached) VALUES (5, 7, '(0,1)');


---- ACCESSS --------------------------------------------------------------------------------------------



INSERT INTO annotations_principals_accesss (annotation_id, principal_id, access_) VALUES (1, 2, 'write');
INSERT INTO annotations_principals_accesss (annotation_id, principal_id, access_) VALUES (1, 3, 'read');
INSERT INTO annotations_principals_accesss (annotation_id, principal_id, access_) VALUES (1, 11, 'read');

INSERT INTO annotations_principals_accesss (annotation_id, principal_id, access_) VALUES (2, 1, 'read');
INSERT INTO annotations_principals_accesss (annotation_id, principal_id, access_) VALUES (2, 3, 'write');

INSERT INTO annotations_principals_accesss (annotation_id, principal_id, access_) VALUES (3, 1, 'read');
INSERT INTO annotations_principals_accesss (annotation_id, principal_id, access_) VALUES (3, 2, 'read');

INSERT INTO annotations_principals_accesss (annotation_id, principal_id, access_) VALUES (4, 2, 'write');
INSERT INTO annotations_principals_accesss (annotation_id, principal_id, access_) VALUES (4, 1, 'write');
-- checking integrity control:
-- INSERT INTO annotations_principals_accesss (annotation_id, principal_id, access_) VALUES (5, 5, 'read');

 INSERT INTO notebooks_principals_accesss (notebook_id, principal_id, access_) VALUES (1, 2, 'write');
 INSERT INTO notebooks_principals_accesss (notebook_id, principal_id, access_) VALUES (1, 3, 'read');
 INSERT INTO notebooks_principals_accesss (notebook_id, principal_id, access_) VALUES (1, 4, 'write');
 INSERT INTO notebooks_principals_accesss (notebook_id, principal_id, access_) VALUES (1, 5, 'read');
 INSERT INTO notebooks_principals_accesss (notebook_id, principal_id, access_) VALUES (1, 11, 'read');

 INSERT INTO notebooks_principals_accesss (notebook_id, principal_id, access_) VALUES (2, 1, 'read');
 INSERT INTO notebooks_principals_accesss (notebook_id, principal_id, access_) VALUES (2, 3, 'write');
 INSERT INTO notebooks_principals_accesss (notebook_id, principal_id, access_) VALUES (2, 11, 'read');


 INSERT INTO notebooks_principals_accesss (notebook_id, principal_id, access_) VALUES (4, 2, 'write');
 INSERT INTO notebooks_principals_accesss (notebook_id, principal_id, access_) VALUES (4, 1, 'write');
 INSERT INTO notebooks_principals_accesss (notebook_id, principal_id, access_) VALUES (4, 11, 'read');
