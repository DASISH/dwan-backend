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

INSERT INTO principal (principal_name, external_id) VALUES ('a user', '00000000-0000-0000-0000-000000000003');
INSERT INTO principal (principal_name, external_id) VALUES ('b user', '00000000-0000-0000-0000-000000000004');

INSERT INTO notebook (title, owner_id, external_id) VALUES ('a notebook', 1, '00000000-0000-0000-0000-000000000001');
-- INSERT INTO notebook (title, owner_id, external_id) VALUES ('a notebook', 1, 1);
INSERT INTO annotation (owner_id,headline,body_xml, external_id) VALUES (1, 'a headline','<html><body>some html</body></html>', '00000000-0000-0000-0000-000000000005');

INSERT INTO notebook (title, owner_id, external_id) VALUES ('a second notebook', 1, '00000000-0000-0000-0000-000000000002');
-- INSERT INTO notebook (title, owner_id, external_id) VALUES ('a second notebook', 1, 2);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (1,1);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (2,1);


-- Test data for jdbcAnnotationDao --

INSERT INTO notebook (notebook_id, title, owner_id, external_id) VALUES (11, 'Notebook 11', 111, '00000000-0000-0000-0000-000000000011');
INSERT INTO notebook (notebook_id, title, owner_id, external_id) VALUES (12, 'Notebook 12', 112, '00000000-0000-0000-0000-000000000012');
INSERT INTO notebook (notebook_id, title, owner_id, external_id) VALUES (13, 'Notebook 13', 113, '00000000-0000-0000-0000-000000000013');

INSERT INTO annotation (annotation_id, owner_id,headline,body_xml, external_id) VALUES (21, 111, 'Sagrada Famiglia','<html><body>some html 1</body></html>', '00000000-0000-0000-0000-000000000021');
INSERT INTO annotation (annotation_id, owner_id,headline,body_xml, external_id) VALUES (22, 112, 'Gaudi','<html><body>some html 2 </body></html>', '00000000-0000-0000-0000-000000000022');
INSERT INTO annotation (annotation_id, owner_id,headline,body_xml, external_id) VALUES (23, 113, 'Art Nuveau','<html><body>some html 3</body></html>', '00000000-0000-0000-0000-000000000023');
INSERT INTO annotation (annotation_id, owner_id,headline,body_xml, external_id) VALUES (25, 111, 'Annotation to delete','<html><body>some html 4</body></html>', '00000000-0000-0000-0000-000000000025');



INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (11,21);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (11,22);
INSERT INTO notebooks_annotations (notebook_id,annotation_id) VALUES (12,23);


