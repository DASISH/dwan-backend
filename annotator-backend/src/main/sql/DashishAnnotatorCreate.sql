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
    annotation_id integer NOT NULL,
    uri text,
    time_stamp timestamp with time zone,
    owner_id integer,
    headline text,
    body_xml xml
);

CREATE SEQUENCE annotation_annotation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE annotation_annotation_id_seq OWNED BY annotation.annotation_id;

CREATE TABLE notebook (
    notebook_id integer NOT NULL,
    "URI" text,
    time_stamp timestamp with time zone,
    title text,
    owner_id integer NOT NULL
);

CREATE SEQUENCE "notebook_notebookId_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE "notebook_notebookId_seq" OWNED BY notebook.notebook_id;

CREATE TABLE notebooks_annotations (
    notebook_id integer NOT NULL,
    annotation_id integer NOT NULL
);

CREATE TABLE principal (
    principal_id integer NOT NULL,
    principal_name text
);

CREATE SEQUENCE principal_principal_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE principal_principal_id_seq OWNED BY principal.principal_id;

ALTER TABLE ONLY annotation ALTER COLUMN annotation_id SET DEFAULT nextval('annotation_annotation_id_seq'::regclass);

ALTER TABLE ONLY notebook ALTER COLUMN notebook_id SET DEFAULT nextval('"notebook_notebookId_seq"'::regclass);

ALTER TABLE ONLY principal ALTER COLUMN principal_id SET DEFAULT nextval('principal_principal_id_seq'::regclass);

ALTER TABLE ONLY annotation
    ADD CONSTRAINT annotation_primary_key PRIMARY KEY (annotation_id);

ALTER TABLE ONLY notebook
    ADD CONSTRAINT notebook_primary_key PRIMARY KEY (notebook_id);

ALTER TABLE ONLY notebooks_annotations
    ADD CONSTRAINT pk_notebooks_annotations PRIMARY KEY (notebook_id, annotation_id);

ALTER TABLE ONLY principal
    ADD CONSTRAINT principal_primary_key PRIMARY KEY (principal_id);

CREATE INDEX fki_annotation_owner_principal_id ON annotation USING btree (owner_id);

CREATE INDEX fki_owner_id_principal_id ON notebook USING btree (owner_id);

ALTER TABLE ONLY annotation
    ADD CONSTRAINT fk_annotation_owner_principal_id FOREIGN KEY (owner_id) REFERENCES principal(principal_id);

ALTER TABLE ONLY notebook
    ADD CONSTRAINT fk_notebook_owner_id_principal_id FOREIGN KEY (owner_id) REFERENCES principal(principal_id);

ALTER TABLE ONLY notebooks_annotations
    ADD CONSTRAINT fk_notebooks_annotations_annotation_id FOREIGN KEY (annotation_id) REFERENCES annotation(annotation_id);

ALTER TABLE ONLY notebooks_annotations
    ADD CONSTRAINT fk_notebooks_annotations_notebook_id FOREIGN KEY (notebook_id) REFERENCES notebook(notebook_id);


-- Completed on 2013-06-14 14:36:28 CEST

--
-- PostgreSQL database dump complete
--

