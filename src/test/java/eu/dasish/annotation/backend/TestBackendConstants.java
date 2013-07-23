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

import java.util.UUID;

public class TestBackendConstants {
    
    public static final String _TEST_UID_1_ = "00000000-0000-0000-0000-000000000003";
    public static final String _TEST_UID_2_ = "00000000-0000-0000-0000-000000000004";
    public static final String _TEST_AID_1_ =  "00000000-0000-0000-0000-000000000005"; 
    public static final String _TEST_NULL_UUID_ = "00000000-0000-0000-0000-000000000006";
    
    // testing jdbcAnnotationDao
    public static final String _TEST_NOTEBOOK_1_EXT = "00000000-0000-0000-0000-000000000011";
    public static final String _TEST_NOTEBOOK_2_EXT = "00000000-0000-0000-0000-000000000012";
    public static final String _TEST_NOTEBOOK_3_EXT = "00000000-0000-0000-0000-000000000013";
    
    public static final int _TEST_NOTEBOOK_1_INT = 11;
    public static final int _TEST_NOTEBOOK_2_INT = 12;
    public static final int _TEST_NOTEBOOK_3_INT = 13;
    
    public static final String _TEST_ANNOT_1_EXT = "00000000-0000-0000-0000-000000000021";
    public static final String _TEST_ANNOT_2_EXT = "00000000-0000-0000-0000-000000000022";
    public static final String _TEST_ANNOT_3_EXT = "00000000-0000-0000-0000-000000000023";
    public static final String _TEST_ANNOT_4_EXT_NOT_IN_THE_DB = "00000000-0000-0000-0000-000000000024";
    public static final String _TEST_ANNOT_5_EXT_TO_BE_DELETED = "00000000-0000-0000-0000-000000000025";
    
    
    public static final int _TEST_ANNOT_1_INT = 21;
    public static final int _TEST_ANNOT_2_INT = 22;
    public static final int _TEST_ANNOT_3_INT = 23;    
    public static final int _TEST_ANNOT_4_INT_NOT_IN_THE_DB = 24;
    public static final int _TEST_ANNOT_5_INT_TO_BE_DELETED = 25;
    
    public static final String _TEST_ANNOT_1_HEADLINE = "Sagrada Famiglia";
    public static final String _TEST_ANNOT_2_HEADLINE = "Gaudi";
    public static final String _TEST_ANNOT_3_HEADLINE = "Art Nuveau";
    public static final String _TEST_ANNOT_TO_ADD_HEADLINE = "Annotation to add to test DAO";
    
    public static final int _TEST_ANNOT_1_OWNER = 111;
    public static final int _TEST_ANNOT_2_OWNER = 112;
    public static final int _TEST_ANNOT_3_OWNER = 113;
    public static final int _TEST_ANNOT_TO_ADD_OWNER = 117; 
    
    public static final String _TEST_ANNOT_1_BODY = "<html><body>some html 1</body></html>";
    public static final String _TEST_ANNOT_TO_ADD_BODY = "<html><body>the stuff to be added</body></html>";
    
    public static final String annotaiontoDeleteInDB="INSERT INTO annotation (annotation_id, owner_id,headline,body_xml, external_id) VALUES (25, 111, 'Annotation to delete','<html><body>some html 4</body></html>', '00000000-0000-0000-0000-000000000025');";
    
}

