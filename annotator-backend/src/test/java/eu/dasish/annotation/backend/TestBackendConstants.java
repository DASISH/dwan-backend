/**
 * Copyright (C) 2013 DASISH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.dasish.annotation.backend;

public class TestBackendConstants {
    
    public static final String _TEST_NOTEBOOK_1_EXT_ID ="00000000-0000-0000-0000-000000000001";
    public static final String _TEST_NOTEBOOK_2_EXT_ID ="00000000-0000-0000-0000-000000000002";
    public static final String _TEST_UID_1_ = "00000000-0000-0000-0000-000000000003";
    public static final String _TEST_UID_2_ = "00000000-0000-0000-0000-000000000004";
    public static final String _TEST_AID_1_ =  "00000000-0000-0000-0000-000000000005"; 
    public static final String _TEST_NULL_UUID_ = "00000000-0000-0000-0000-000000000006";
    
    // testing jdbcAnnotationDao
    public static final String _TEST_NOTEBOOK_3_EXT = "00000000-0000-0000-0000-000000000011";
    public static final String _TEST_NOTEBOOK_4_EXT = "00000000-0000-0000-0000-000000000012";
    public static final String _TEST_NOTEBOOK_5_EXT = "00000000-0000-0000-0000-000000000013";
    
    public static final String _TEST_NOTEBOOK_3_TITLE = "Notebook 3";
    public static final String _TEST_NOTEBOOK_4_TITLE = "Notebook 4";
    public static final String _TEST_NOTEBOOK_5_TITLE = "Notebook 5";
    
    public static final String _TEST_ANNOT_2_EXT = "00000000-0000-0000-0000-000000000021";
    public static final String _TEST_ANNOT_3_EXT = "00000000-0000-0000-0000-000000000022";
    public static final String _TEST_ANNOT_4_EXT = "00000000-0000-0000-0000-000000000023";
    public static final String _TEST_ANNOT_5_EXT  = "00000000-0000-0000-0000-000000000024";
    public static final String _TEST_ANNOT_7_EXT_NOT_IN_DB = "00000000-0000-0000-0000-000000000026";
   
    public static final String _TEST_ANNOT_2_HEADLINE = "Sagrada Famiglia";
    public static final String _TEST_ANNOT_3_HEADLINE = "Gaudi";
    public static final String _TEST_ANNOT_4_HEADLINE = "Art Nuveau";
    public static final String _TEST_ANNOT_TO_ADD_HEADLINE = "Annotation to add to test DAO";
    
    public static final int _TEST_ANNOT_2_OWNER = 3;
    public static final int _TEST_ANNOT_3_OWNER = 4;
    public static final int _TEST_ANNOT_4_OWNER = 5;
    
    public static final String _TEST_ANNOT_2_BODY = "<html><body>some html 1</body></html>";
    public static final String _TEST_ANNOT_TO_ADD_BODY = "<html><body>the stuff to be added</body></html>";
    
    public static final String _TEST_ANNOT_2_TIME_STAMP = "2013-08-12T11:25:00.383+02:00";
    
    public static final String annotaiontoDeleteInDB="INSERT INTO annotation (annotation_id, owner_id,headline,body_xml, external_id) VALUES (25, 111, 'Annotation to delete','<html><body>some html 4</body></html>', '00000000-0000-0000-0000-000000000025');";
    
    public static final String _TEST_USER_3_EXT_ID = "00000000-0000-0000-0000-000000000111";
    public static final String _TEST_USER_4_EXT_ID = "00000000-0000-0000-0000-000000000112";  
    public static final String _TEST_USER_5_EXT_ID = "00000000-0000-0000-0000-000000000113";  
    
    public static final String _TEST_USER_XXX_EXT_ID = "00000000-0000-0000-0000-000000000666"; 
    
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////   
    
//    INSERT INTO target_source (external_id, link_uri, version_id) VALUES ('00000000-0000-0000-0000-000000000031', 'http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia', 1); -- 1
//    INSERT INTO target_source (external_id, link_uri, version_id) VALUES ('00000000-0000-0000-0000-000000000032', 'http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD', 3); --2

    
    public static final String _TEST_SOURCE_1_EXT_ID = "00000000-0000-0000-0000-000000000031";
    public static final String _TEST_SOURCE_2_EXT_ID = "00000000-0000-0000-0000-000000000032";
    
    public static final String _TEST_SOURCE_1_LINK = "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia";
    public static final String _TEST_SOURCE_2_LINK = "http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD";
    
    //public static final int _TEST_SOURCE_1_VERSION_ID = 1;
    //public static final int _TEST_SOURCE_2_VERSION_ID = 3;
    
    public static final String _TEST_CACHED_REPRESENTATION_1_EXT_ID_ = "00000000-0000-0000-0000-000000000051";
    // INSERT INTO cached_representation_info (external_id, mime_type, tool, type_, where_is_the_file) VALUES ('00000000-0000-0000-0000-000000000051', 'text/html', 'latex', 'text', 'corpus1'); --1
    public static final String _TEST_CACHED_REPRESENTATION_1_MIME_TYPE_ =  "text/html";
    public static final String _TEST_CACHED_REPRESENTATION_1_TOOL_ =  "latex";
    public static final String _TEST_CACHED_REPRESENTATION_1_TYPE_ =  "text";
    
    public static final String _TEST_VERSION_1_EXT_ID = "00000000-0000-0000-0000-000000000041";
    public static final String _TEST_VERSION_2_EXT_ID = "00000000-0000-0000-0000-000000000042";
    public static final String _TEST_VERSION_3_EXT_ID = "00000000-0000-0000-0000-000000000043";
    public static final String _TEST_VERSION_NONEXIST_EXT_ID = "00000000-0000-0000-0000-000000000049";
    
    public static final String _TEST_TEMP_SOURCE_ID = "Barcelona-1";
    public static final String _TEST_ANNOT_TO_ADD_NEW_SOURCE_BODY = "refers to "+_TEST_TEMP_SOURCE_ID;
    public static final String _TEST_ANNOT_TO_ADD_NEW_SOURCE_HEADLINE = "SF in Catalan";
    public static final String _TEST_NEW_SOURCE_LINK = "http://www.sagradafamilia.cat/docs_instit/historia.php ";
    
    public static final String _TEST_SERVLET_URI = "/dasish/";
}

