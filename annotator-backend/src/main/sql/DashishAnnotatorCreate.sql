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

--
-- PostgreSQL database dump
--

-- Dumped from database version 9.2.4
-- Dumped by pg_dump version 9.2.2
-- Started on 2013-06-14 14:36:28 CEST

SET client_encoding = 'UTF8';

CREATE DATABASE "DASISHAnnotator" WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'C' LC_CTYPE = 'C';


\connect "DASISHAnnotator"

SET client_encoding = 'UTF8';

SET default_with_oids = false;

SET TIME ZONE LOCAL;

CREATE TABLE access (
  access_mode text UNIQUE NOT NULL
);

CREATE TABLE principal (
    principal_id SERIAL UNIQUE NOT NULL,
    external_id text UNIQUE NOT NULL,
    remote_id text UNIQUE NOT NULL,
    principal_name text,
    e_mail text,
    account text NOT NULL
);

CREATE TABLE notebook (
    notebook_id SERIAL UNIQUE NOT NULL,
    external_id text UNIQUE NOT NULL,
    owner_id integer REFERENCES principal(principal_id),
    last_modified timestamp default (current_timestamp AT TIME ZONE 'UTC'),
    title text
);


CREATE TABLE annotation (
    annotation_id SERIAL UNIQUE NOT NULL, 
    external_id text UNIQUE NOT NULL,
    owner_id integer REFERENCES principal(principal_id),
    last_modified timestamp default (current_timestamp AT TIME ZONE 'UTC'),
    headline text,
    body_text text,
    body_mimetype text,
    is_xml BOOLEAN,
    public_ text REFERENCES access(access_mode) NOT NULL default 'none'
);



CREATE TABLE target (
    target_id SERIAL UNIQUE NOT NULL,
    external_id text UNIQUE NOT NULL,
    last_modified timestamp default (current_timestamp AT TIME ZONE 'UTC'),
    link_uri text, 
    version text,
    fragment_descriptor text
);

CREATE TABLE cached_representation (
    cached_representation_id SERIAL UNIQUE NOT NULL,
    external_id text UNIQUE NOT NULL,
    mime_type text,
    tool text,
    type_ text, 
    file_ bytea 
);





-----------------------------------------------------------------------
--------------------- JOINT TABLES ------------------------------------

CREATE TABLE annotations_targets (
   annotation_id integer REFERENCES annotation(annotation_id) NOT NULL, -- defining a foreign key: there must be a uniquely defined row in "annotation", that is defined by "annotation_id"
   target_id integer REFERENCES target(target_id) NOT NULL,
   unique(annotation_id, target_id)
);



CREATE TABLE notebooks_annotations (
    notebook_id integer REFERENCES notebook(notebook_id) NOT NULL,
    annotation_id integer REFERENCES annotation(annotation_id) NOT NULL,
    unique(notebook_id, annotation_id)
);

CREATE TABLE targets_cached_representations (
    target_id integer REFERENCES target(target_id) NOT NULL,
    cached_representation_id integer REFERENCES cached_representation(cached_representation_id) NOT NULL,
    fragment_descriptor_in_cached text,
    unique(target_id, cached_representation_id)
);



CREATE TABLE annotations_principals_accesss (
annotation_id integer REFERENCES annotation(annotation_id) NOT NULL,
principal_id integer REFERENCES principal(principal_id) NOT NULL,
access_  text REFERENCES access(access_mode) NOT NULL,
unique(annotation_id, principal_id)
);

CREATE TABLE notebooks_principals_accesss (
notebook_id integer REFERENCES notebook(notebook_id) NOT NULL,
principal_id integer REFERENCES principal(principal_id) NOT NULL,
access_  text REFERENCES access(access_mode) NOT NULL,
unique(notebook_id, principal_id)
);


---------------------------------------------------------------------------------------------
-- ALTER TABLE ONLY annotation
    -- ADD CONSTRAINT annotation_primary_key PRIMARY KEY (annotation_id);

-- ALTER TABLE ONLY notebooks_annotations
   -- ADD CONSTRAINT pk_notebooks_annotations PRIMARY KEY (notebook_id, annotation_id);


-- ALTER TABLE ONLY annotation
 -- ADD CONSTRAINT fk_annotation_owner_principal_id FOREIGN KEY (owner_id) REFERENCES principal(principal_id);

-- ALTER TABLE ONLY notebook
   -- ADD CONSTRAINT fk_notebook_owner_id_principal_id FOREIGN KEY (owner_id) REFERENCES principal(principal_id);

 --ALTER TABLE ONLY notebooks_annotations
 --   ADD CONSTRAINT fk_notebooks_annotations_annotation_id FOREIGN KEY (annotation_id) REFERENCES annotation(annotation_id);

-- ALTER TABLE ONLY notebooks_annotations
--     ADD CONSTRAINT fk_notebooks_annotations_notebook_id FOREIGN KEY (notebook_id) REFERENCES notebook(notebook_id);


-- Completed on 2013-06-14 14:36:28 CEST

--
-- PostgreSQL database dump complete
--