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


---------- BASE TABLES ----------------------------------------------

--<xs:complexType name="User">
--        <xs:sequence>
--            <xs:element name="additionalInfo">
--                <xs:complexType>
--                    <xs:sequence>
--                        <xs:any processContents="lax" maxOccurs="unbounded"/>
--                    </xs:sequence>
--                    <xs:anyAttribute processContents="lax"/>
--                </xs:complexType>
--            </xs:element>
--       </xs:sequence>
--        <xs:attribute name="URI" type="xs:anyURI" use="required"/>
--        <xs:attribute name="displayName" type="xs:string" use="required"/>
--        <xs:attribute name="eMail" type="xs:string" use="required"/>
--   </xs:complexType>


CREATE TABLE principal (
    principal_id SERIAL UNIQUE NOT NULL,
    external_id UUID UNIQUE NOT NULL,
    principal_name text,
    e_mail text,
    additional_info text
);


------------------------------------------------------------------------
-- <xs:complexType name="Annotation">
--        <xs:sequence>
--            <xs:element name="owner" type="ResourceREF" minOccurs="1"/>
--            <xs:element name="headline" type="xs:string" minOccurs="1"/>
--            <!-- schematron checks the length <== 100 -->
--            <xs:element name="body" type="AnnotationBody" minOccurs="1"/>
--            <xs:element name="targetSources" type="NewOrExistingSourceInfos" minOccurs="1"/>
--            <xs:element name="permissions" type="ResourceREF"/>
--        </xs:sequence>
--        <xs:attribute name="URI" type="xs:anyURI" use="required"/>
--        <xs:attribute name="timeStamp" type="xs:dateTime" use="required"/>
--    </xs:complexType>


CREATE TABLE annotation (
    annotation_id SERIAL UNIQUE NOT NULL, 
    external_id UUID UNIQUE NOT NULL,
    time_stamp timestamp with time zone default now(),
    owner_id integer REFERENCES principal(principal_id), 
    --  there must be exactly one owner ++ 
     -- and this owner must be in the table "permissions" as owner!!
    headline text,
    body_xml xml
);

-----------------------------------------------------------------------
-- <xs:complexType name="CachedRepresentationInfo">
--        <xs:attribute name="ref" type="xs:anyURI" use="required"/>
--        <xs:attribute name="mimeType" type="xs:string" use="required"/>
--        <xs:attribute name="tool" type="xs:string" use="required"/>
--        <xs:attribute name="type" type="xs:string" use="required"/>
--    </xs:complexType>

CREATE TABLE cached_representation_info (
    cached_representation_id SERIAL UNIQUE NOT NULL,
    external_id UUID UNIQUE NOT NULL,
    mime_type text,
    tool text,
    type_ text, 
    where_is_the_file text -- DIFFERS FROM the schema
);

-- soundness there must be at least one version referring to this cahced representation


----------------------------------------------------------------------
-- <xs:complexType name="Version">
--        <xs:sequence>
--            <xs:element name="version" type="xs:string"/>
--            <xs:element name="cachedRepresentations" type="CachedRepresentations"/>
--        </xs:sequence>
--    </xs:complexType>

CREATE TABLE version (
    version_id SERIAL UNIQUE NOT NULL,
    external_id UUID UNIQUE NOT NULL,
    version text,
    --  SOUNDNESS: there must be at least one row with this version_id in the verions_cached_representations table
);

----------------------------------------------------------------

 -- <xs:complexType name="Source">
 --       <xs:sequence>
 --           <xs:element name="versions-siblings" type="ResourceREF" minOccurs="1"/>
 --       </xs:sequence>
 --       <xs:attribute name="URI" type="xs:anyURI" use="required"/>
 --       <xs:attribute name="timeSatmp" type="xs:dateTime" use="required"/>
 --       <xs:attribute name="link" type="xs:anyURI" use="required"/>
 --       <xs:attribute name="version" type="xs:string" use="required"/>
 --   </xs:complexType>

CREATE TABLE target_source (
    source_id SERIAL UNIQUE NOT NULL,
    external_id UUID UNIQUE NOT NULL,
    time_stamp timestamp with time zone default now(),
    link_uri text,
    version_id integer REFERENCES version(version_id), ---- DIFFERS from the xml structure, 
    -- SOUNDNESS: there must be exactly version at the version table  ++   
    -- soundness: there must be at least one annotation referring to this source
);


-----------------------------------------------------------------------------------

CREATE TABLE notebook (
    notebook_id SERIAL UNIQUE NOT NULL,
    external_id UUID UNIQUE NOT NULL,
    time_stamp timestamp with time zone default now(),
    title text,
    owner_id integer NOT NULL
-- soundness:  there must be at least one target source in the annotations_target_sources table
);



-----------------------------------------------------------------------
--------------------- JOINT TABLES ------------------------------------

CREATE TABLE annotations_target_sources (
   annotation_id integer REFERENCES annotation(annotation_id), -- defining a foreign key: there must be a uniquely defined row in "annotation", that is defined by "annotation_id"
   -- source_id integer REFERENCES target_source(source_id),
   -- unique(annotation_id, source_id),
);



CREATE TABLE notebooks_annotations (
    notebook_id integer REFERENCES notebook(notebook_id),
    annotation_id integer REFERENCES annotation(annotation_id),
    unique(notebook_id, annotation_id),
);

CREATE TABLE sources_versions (
    source_id integer REFERENCES target_source(source_id),
    version_id integer REFERENCES version(version_id),
    unique(source_id, version_id),
);

CREATE TABLE versions_cached_representations (
    version_id integer REFERENCES version(version_id),
    cached_representation_id integer REFERENCES cached_representation_info(cached_representation_id),
    unique(version_id, cached_representation_id),
);


---------------------------------------------------------------------------------------------
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