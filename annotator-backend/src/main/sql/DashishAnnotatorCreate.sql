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

CREATE TABLE annotation (
    annotation_id SERIAL UNIQUE NOT NULL,
    external_id UUID UNIQUE NOT NULL,
    time_stamp timestamp with time zone default now(),
    owner_id integer,
    headline text,
    body_xml xml
);

CREATE TABLE notebook (
    notebook_id SERIAL UNIQUE NOT NULL,
    external_id UUID UNIQUE NOT NULL,
    time_stamp timestamp with time zone default now(),
    title text,
    owner_id integer NOT NULL
);

CREATE TABLE notebooks_annotations (
    notebook_id integer REFERENCES notebook(notebook_id),
    annotation_id integer REFERENCES annotation(annotation_id)
);

CREATE TABLE principal (
    principal_id SERIAL NOT NULL,
    external_id UUID UNIQUE NOT NULL,
    principal_name text
);

ALTER TABLE ONLY annotation
    ADD CONSTRAINT annotation_primary_key PRIMARY KEY (annotation_id);

ALTER TABLE ONLY notebooks_annotations
    ADD CONSTRAINT pk_notebooks_annotations PRIMARY KEY (notebook_id, annotation_id);

CREATE INDEX fki_annotation_owner_principal_id ON annotation USING btree (owner_id);

CREATE INDEX fki_owner_id_principal_id ON notebook USING btree (owner_id);

ALTER TABLE ONLY annotation
    ADD CONSTRAINT fk_annotation_owner_principal_id FOREIGN KEY (owner_id) REFERENCES principal(principal_id);

ALTER TABLE ONLY notebook
    ADD CONSTRAINT fk_notebook_owner_id_principal_id FOREIGN KEY (owner_id) REFERENCES principal(principal_id);

-- ALTER TABLE ONLY notebooks_annotations
--     ADD CONSTRAINT fk_notebooks_annotations_annotation_id FOREIGN KEY (annotation_id) REFERENCES annotation(annotation_id);

-- ALTER TABLE ONLY notebooks_annotations
--     ADD CONSTRAINT fk_notebooks_annotations_notebook_id FOREIGN KEY (notebook_id) REFERENCES notebook(notebook_id);


-- Completed on 2013-06-14 14:36:28 CEST

--
-- PostgreSQL database dump complete
--

