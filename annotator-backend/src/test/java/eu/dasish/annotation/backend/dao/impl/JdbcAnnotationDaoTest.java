/*
 * Copyright (C) 2013 DASISH
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.dasish.annotation.backend.dao.impl;

import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.Annotations;
import eu.dasish.annotation.schema.ResourceREF;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author olhsha
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-config/annotationDao.xml"})

public class JdbcAnnotationDaoTest extends JdbcResourceDaoTest{
    
    @Autowired
    JdbcAnnotationDao jdbcAnnotationDao;    
    
    @Test
    public void testIsNotebookInTheDataBase(){
        super.testIsNotebookInTheDataBase(jdbcAnnotationDao);
    }
    
    /**
     * Test of getAnnotationIDs method, of class JdbcAnnotationDao.
     * List<Number> getAnnotationIDs(Number notebookID)
     */
    @Test
    public void testGetAnnotationIDs() {
        System.out.println("getAnnotationIDs");
        
        // test one, 2-element notebook
        final List<Number> annotationIDs = jdbcAnnotationDao.getAnnotationIDs(TestBackendConstants._TEST_NOTEBOOK_1_INT);
        assertEquals(2, annotationIDs.size());
        assertEquals(TestBackendConstants._TEST_ANNOT_1_INT, annotationIDs.get(0).intValue());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_INT, annotationIDs.get(1).intValue());
        
        // test two, 1-element notebook
        final List<Number> annotationIDsTwo = jdbcAnnotationDao.getAnnotationIDs(TestBackendConstants._TEST_NOTEBOOK_2_INT);
        assertEquals(1, annotationIDsTwo.size());
        assertEquals(TestBackendConstants._TEST_ANNOT_3_INT, annotationIDsTwo.get(0).intValue());
        
        // test three, empty notebook
        final List<Number> annotationIDsThree = jdbcAnnotationDao.getAnnotationIDs(TestBackendConstants._TEST_NOTEBOOK_3_INT);
        assertEquals(0, annotationIDsThree.size());
        
        // test four, null-notebook
        final List<Number> annotationIDsFour = jdbcAnnotationDao.getAnnotationIDs(null);
        assertEquals(null, annotationIDsFour);
        
        //test five, the notebook not in the DB
        final List<Number> annotationIDsFive = jdbcAnnotationDao.getAnnotationIDs(TestBackendConstants._TEST_ANNOT_4_INT_NOT_IN_THE_DB);
        assertEquals(null, annotationIDsFive);
        
    }

    /**
     * Test of getAnnotationInfos method, of class JdbcAnnotationDao.
     * List<AnnotationInfo> getAnnotationInfos(List<Number> annotationIDs)
     */
    @Test 
    public void testGetAnnotationInfos() {
        System.out.println("getAnnotationInfos");
        List<Number> annotIds = new ArrayList<Number>(); 
        annotIds.add(TestBackendConstants._TEST_ANNOT_1_INT);
        annotIds.add(TestBackendConstants._TEST_ANNOT_2_INT);
        annotIds.add(TestBackendConstants._TEST_ANNOT_3_INT);
        
        final List<AnnotationInfo> annotationInfos = jdbcAnnotationDao.getAnnotationInfos(annotIds);
        assertEquals(3, annotationInfos.size());
        
        assertEquals(TestBackendConstants._TEST_ANNOT_1_HEADLINE, annotationInfos.get(0).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_1_OWNER), annotationInfos.get(0).getOwner().getRef());
        //assertEquals(TestBackendConstants._TEST_ANNOT_1_TARGETS, annotationInfos.get(0).getTargetSources());
        
        assertEquals(TestBackendConstants._TEST_ANNOT_2_HEADLINE, annotationInfos.get(1).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_OWNER), annotationInfos.get(1).getOwner().getRef());
        //assertEquals(TestBackendConstants._TEST_ANNOT_2_TARGETS, annotationInfos.get(1).getTargetSources());
        
        assertEquals(TestBackendConstants._TEST_ANNOT_3_HEADLINE, annotationInfos.get(2).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_3_OWNER), annotationInfos.get(2).getOwner().getRef());
        //assertEquals(TestBackendConstants._TEST_ANNOT_3_TARGETS, annotationInfos.get(2).getTargetSources());
        
        final List<AnnotationInfo> annotationInfosNull = jdbcAnnotationDao.getAnnotationInfos(null);
        assertEquals(null, annotationInfosNull);
        
        final List<AnnotationInfo> annotationInfosZeroSize = jdbcAnnotationDao.getAnnotationInfos(new ArrayList<Number>());
        assertEquals(0, annotationInfosZeroSize.size());
        
        
    }

    /**
     * Test of getAnnotationInfosOfNotebook method, of class JdbcAnnotationDao.
     * List<AnnotationInfo> getAnnotationInfosOfNotebook(Number notebookID)
     */
    @Test 
    public void testGetAnnotationInfosOfNotebook() {
        System.out.println("getAnnotationInfosOfNotebook");
        
        //test One
        final List<AnnotationInfo> annotationInfos = jdbcAnnotationDao.getAnnotationInfosOfNotebook(TestBackendConstants._TEST_NOTEBOOK_1_INT);
        assertEquals(2, annotationInfos.size());
        
        assertEquals(TestBackendConstants._TEST_ANNOT_1_HEADLINE, annotationInfos.get(0).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_1_OWNER), annotationInfos.get(0).getOwner().getRef());
        //assertEquals(TestBackendConstants._TEST_ANNOT_1_TARGETS, annotationInfos.get(0).getTargetSources());
        
        assertEquals(TestBackendConstants._TEST_ANNOT_2_HEADLINE, annotationInfos.get(1).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_OWNER), annotationInfos.get(1).getOwner().getRef());
        //assertEquals(TestBackendConstants._TEST_ANNOT_2_TARGETS, annotationInfos.get(1).getTargetSources());
        
        
        //test Two
        final List<AnnotationInfo> annotationInfosTwo = jdbcAnnotationDao.getAnnotationInfosOfNotebook(TestBackendConstants._TEST_NOTEBOOK_2_INT);
        assertEquals(1, annotationInfosTwo.size());
        
        assertEquals(TestBackendConstants._TEST_ANNOT_3_HEADLINE, annotationInfosTwo.get(0).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_3_OWNER), annotationInfosTwo.get(0).getOwner().getRef());
        //assertEquals(TestBackendConstants._TEST_ANNOT_3_TARGETS, annotationInfosTwo.get(0).getTargetSources());
        
        //test notebook with no annotations
        final List<AnnotationInfo> annotationInfosThree = jdbcAnnotationDao.getAnnotationInfosOfNotebook(TestBackendConstants._TEST_NOTEBOOK_3_INT);
        assertEquals(0, annotationInfosThree.size());
        
        //non-existing notebook
        final List<AnnotationInfo> annotationInfosFour = jdbcAnnotationDao.getAnnotationInfosOfNotebook(TestBackendConstants._TEST_ANNOT_4_INT_NOT_IN_THE_DB);
        assertEquals(null, annotationInfosFour);
        
        
        //null notebook
        final List<AnnotationInfo> annotationInfosFive = jdbcAnnotationDao.getAnnotationInfosOfNotebook(null);
        assertEquals(null, annotationInfosFive);
        
        
    }

    /**
     * Test of getAnnotationREFs method, of class JdbcAnnotationDao.
     * List<ResourceREF> getAnnotationREFs(List<Number> annotationIDs)
     */
    @Test  
    public void testGetAnnotationREFs() {
        System.out.println("getAnnotationREFs");
        List<Number> annotIds = new ArrayList<Number>(); 
        annotIds.add(TestBackendConstants._TEST_ANNOT_1_INT);
        annotIds.add(TestBackendConstants._TEST_ANNOT_2_INT);
        annotIds.add(TestBackendConstants._TEST_ANNOT_3_INT);
        annotIds.add(TestBackendConstants._TEST_ANNOT_4_INT_NOT_IN_THE_DB);
        
        final  List<ResourceREF> testList = jdbcAnnotationDao.getAnnotationREFs(annotIds);
        assertEquals(3, testList.size());        
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_1_INT), testList.get(0).getRef());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_INT), testList.get(1).getRef());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_3_INT), testList.get(2).getRef());
        
        final  List<ResourceREF> testListTwo = jdbcAnnotationDao.getAnnotationREFs(new ArrayList<Number>());
        assertEquals(0, testListTwo.size());
        
        final List<ResourceREF> testListThree = jdbcAnnotationDao.getAnnotationREFs(null);
        assertEquals(null, testListThree);
        
    }

    /**
     * Test of getAnnotationREFsOfNotebook method, of class JdbcAnnotationDao.
     * List<ResourceREF> getAnnotationREFsOfNotebook(Number notebookID)
     */
    @Test
    public void testGetAnnotationREFsOfNotebook() {
        System.out.println("getAnnotationREFsOfNotebook");
        
        // test One 
        List<ResourceREF> testList = jdbcAnnotationDao.getAnnotationREFsOfNotebook(TestBackendConstants._TEST_NOTEBOOK_1_INT);
        assertEquals(2, testList.size());        
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_1_INT), testList.get(0).getRef());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_INT), testList.get(1).getRef());
        
        // test One 
        List<ResourceREF> testListTwo = jdbcAnnotationDao.getAnnotationREFsOfNotebook(TestBackendConstants._TEST_NOTEBOOK_2_INT);
        assertEquals(1, testListTwo.size());        
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_3_INT), testListTwo.get(0).getRef());
        
        // test Three  "empty"
        List<ResourceREF> testListThree = jdbcAnnotationDao.getAnnotationREFsOfNotebook(TestBackendConstants._TEST_NOTEBOOK_3_INT);
        assertEquals(0, testListThree.size()); 
        
        // test Four, non-existing notebook
        List<ResourceREF> testListFour = jdbcAnnotationDao.getAnnotationREFsOfNotebook(TestBackendConstants._TEST_ANNOT_4_INT_NOT_IN_THE_DB);
        assertEquals(null, testListFour); 
        
        // test Five Null-notebook
        List<ResourceREF> testListFive = jdbcAnnotationDao.getAnnotationREFsOfNotebook(null);
        assertEquals(null, testListFive); 
    }

    /**
     * Test of getAnnotations method, of class JdbcAnnotationDao.
     * Annotations getAnnotations(Number notebookID)
     */
    @Test      
    public void testGetAnnotations() {
        System.out.println("getAnnotations");
        
         // test One 
        Annotations annotations = jdbcAnnotationDao.getAnnotations(TestBackendConstants._TEST_NOTEBOOK_1_INT);
        assertEquals(2, annotations.getAnnotation().size());        
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_1_INT), annotations.getAnnotation().get(0).getRef());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_INT), annotations.getAnnotation().get(1).getRef());
        
        // test One 
        Annotations annotationsTwo = jdbcAnnotationDao.getAnnotations(TestBackendConstants._TEST_NOTEBOOK_2_INT);
        assertEquals(1, annotationsTwo.getAnnotation().size());        
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_3_INT), annotationsTwo.getAnnotation().get(0).getRef());
        
        // test Three  "empty" list of annotations
        // according to dasish.xsd if an Annotation is created then its list of annotations must contain at least one element!
        // therefore: no annotations in the notebook ==> Annotations-pbject must be null :(
        Annotations annotationsThree = jdbcAnnotationDao.getAnnotations(TestBackendConstants._TEST_NOTEBOOK_3_INT);
        assertEquals(null, annotationsThree); 
        
        // test Five, non-existing notebook
        Annotations annotationsFour = jdbcAnnotationDao.getAnnotations(TestBackendConstants._TEST_ANNOT_4_INT_NOT_IN_THE_DB);
        assertEquals(null, annotationsFour); 
        
        // test Five Null-notebook
        Annotations annotationsFive = jdbcAnnotationDao.getAnnotations(null);
        assertEquals(null, annotationsFive);
    }
}
