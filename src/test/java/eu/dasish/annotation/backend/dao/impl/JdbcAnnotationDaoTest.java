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
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.PermissionsDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.ResourceREF;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jmock.Expectations;
import org.jmock.Mockery;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author olhsha
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-test-config/mockery.xml", "/spring-test-config/mockNotebookDao.xml",  "/spring-test-config/mockUserDao.xml", "/spring-test-config/mockPermissionsDao.xml", "/spring-config/annotationDao.xml"})
public class JdbcAnnotationDaoTest extends JdbcResourceDaoTest{
    
    @Autowired
    JdbcAnnotationDao jdbcAnnotationDao; 
    
    @Autowired
    private PermissionsDao permissionsDao;
    
    @Autowired
    private NotebookDao notebookDao;
    
    @Autowired
    private Mockery mockery;
    
    TestInstances testInstances = new TestInstances();
    
   
  

    /**
     * Test of getAnnotationInfos method, of class JdbcAnnotationDao.
     * List<AnnotationInfo> getAnnotationInfos(List<Number> annotationIDs)
     */
    @Test 
    public void testGetAnnotationInfos() {
        System.out.println("getAnnotationInfos");
        List<Number> annotIds = new ArrayList<Number>(); 
        annotIds.add(2);
        annotIds.add(3);
        annotIds.add(4);
        
        final List<AnnotationInfo> annotationInfos = jdbcAnnotationDao.getAnnotationInfos(annotIds);
        assertEquals(3, annotationInfos.size());
        
        assertEquals(TestBackendConstants._TEST_ANNOT_2_HEADLINE, annotationInfos.get(0).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_OWNER), annotationInfos.get(0).getOwner().getRef());
        //assertEquals(TestBackendConstants._TEST_ANNOT_1_TARGETS, annotationInfos.get(0).getTargetSources());
        
        assertEquals(TestBackendConstants._TEST_ANNOT_3_HEADLINE, annotationInfos.get(1).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_3_OWNER), annotationInfos.get(1).getOwner().getRef());
        //assertEquals(TestBackendConstants._TEST_ANNOT_2_TARGETS, annotationInfos.get(1).getTargetSources());
        
        assertEquals(TestBackendConstants._TEST_ANNOT_4_HEADLINE, annotationInfos.get(2).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_4_OWNER), annotationInfos.get(2).getOwner().getRef());
        //assertEquals(TestBackendConstants._TEST_ANNOT_3_TARGETS, annotationInfos.get(2).getTargetSources());
        
        final List<AnnotationInfo> annotationInfosNull = jdbcAnnotationDao.getAnnotationInfos(null);
        assertEquals(null, annotationInfosNull);
        
        final List<AnnotationInfo> annotationInfosZeroSize = jdbcAnnotationDao.getAnnotationInfos(new ArrayList<Number>());
        assertEquals(0, annotationInfosZeroSize.size());
        
        
    }

    
    /**
     * Test of getAnnotationREFs method, of class JdbcAnnotationDao.
     * List<ResourceREF> getAnnotationREFs(List<Number> annotationIDs)
     */
    @Test  
    public void testGetAnnotationREFs() {
        System.out.println("getAnnotationREFs");
        List<Number> annotIds = new ArrayList<Number>(); 
        annotIds.add(2);
        annotIds.add(3);
        annotIds.add(4);
        
        final  List<ResourceREF> testList = jdbcAnnotationDao.getAnnotationREFs(annotIds);
        assertEquals(3, testList.size());        
        assertEquals(String.valueOf(2), testList.get(0).getRef());
        assertEquals(String.valueOf(3), testList.get(1).getRef());
        assertEquals(String.valueOf(4), testList.get(2).getRef());
        
        final  List<ResourceREF> testListTwo = jdbcAnnotationDao.getAnnotationREFs(new ArrayList<Number>());
        assertEquals(0, testListTwo.size());
        
        final List<ResourceREF> testListThree = jdbcAnnotationDao.getAnnotationREFs(null);
        assertEquals(null, testListThree);
        
    }
    /**
     * 
     * Test of getAnnotationID method, of class JdbcAnnotationDao.
     * Integer getAnnotationID(AnnotationIdentifier externalID)
     */
    @Test
    public void getAnnotationID() throws SQLException{
       System.out.println("getAnnotationID"); 
       
       final Number annotaionId = jdbcAnnotationDao.getAnnotationID(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_2_EXT));
       assertEquals(2, annotaionId.intValue());
       
       final Number annotaionIdNE = jdbcAnnotationDao.getAnnotationID(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_7_EXT_NOT_IN_DB));
       assertEquals(null, annotaionIdNE);    
      
       final Number annotaionIdNull = jdbcAnnotationDao.getAnnotationID(null);
       assertEquals(null, annotaionIdNull);
    }
    
     /**
     * 
     * Test of getAnnotation method, of class JdbcAnnotationDao.
     * Annotation getAnnotation(Number annotationlID)
     */
    @Test
    public void getAnnotation() throws SQLException{
       System.out.println("getAnnotation"); 
       
       final Annotation annotaion = jdbcAnnotationDao.getAnnotation(2);
       assertEquals(TestBackendConstants._TEST_ANNOT_2_HEADLINE, annotaion.getHeadline());
       assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_OWNER), annotaion.getOwner().getRef());
       assertEquals(TestBackendConstants._TEST_ANNOT_2_BODY, annotaion.getBody().getAny().get(0)); // when the body is elaborated it will be changed
              
       final Annotation annotaionNull = jdbcAnnotationDao.getAnnotation(null);
       assertEquals(null, annotaionNull);
    }
    
    /**
     * Test of deletAnnotation method, of class JdbcAnnotationDao.
     */
    @Test
    public void testDeleteAnnotation() throws SQLException{
        System.out.println("deleteAnnotation"); 
        
         mockery.checking(new Expectations() {
            { 
                oneOf(permissionsDao).removeAnnotation(5);
                will(returnValue(1));
                
                oneOf(notebookDao).removeAnnotation(5);
                will(returnValue(3));
            }
        });
        
        int result = jdbcAnnotationDao.deleteAnnotation(5);
        assertEquals(1, result);
        // now, try to delete the same annotation one more time
        // if it has been already deleted then the method under testing should return 0
        result = jdbcAnnotationDao.deleteAnnotation(5);
        assertEquals(0, result);
    }
    
    
    /**
     * Test of addAnnotation method, of class JdbcAnnotationDao.
     */
    @Test
    public void testAddAnnotation() throws SQLException{
        System.out.println("test_addAnnotation"); 
        final Annotation annotationToAdd = testInstances.getAnnotationToAdd();
        
        
        Annotation result = jdbcAnnotationDao.addAnnotation(annotationToAdd, 5);
        
        AnnotationIdentifier generatedAnnotationExternalID  = new AnnotationIdentifier(result.getURI());
        Annotation addedAnnotation = jdbcAnnotationDao.getAnnotation(jdbcAnnotationDao.getAnnotationID(generatedAnnotationExternalID));        
        assertEquals(annotationToAdd.getBody().getAny().get(0), addedAnnotation.getBody().getAny().get(0));
        assertEquals(annotationToAdd.getHeadline(), addedAnnotation.getHeadline());
        assertEquals(String.valueOf(5), addedAnnotation.getOwner().getRef());
        assertEquals(annotationToAdd.getPermissions(), addedAnnotation.getPermissions());
        assertEquals(annotationToAdd.getTargetSources(), addedAnnotation.getTargetSources());
        assertEquals(annotationToAdd.getTimeStamp(), addedAnnotation.getTimeStamp());
        
        // try to add an already existing annptation, should produce null
        //Annotation annotationOne = testInstances.getAnnotationOne();
        //AnnotationIdentifier resultOne = jdbcAnnotationDao.addAnnotation(annotationOne);
        // TODO: why it doesp produce an annotation, it already exists!!
        //assertTrue(resultOne == null);
        
    }
    
    @Test 
    public void testGetExternalID(){
       System.out.println("getAnnotationID"); 
       
       final AnnotationIdentifier externalId = jdbcAnnotationDao.getExternalID(2);
       assertEquals(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_2_EXT), externalId);
       
       
       final AnnotationIdentifier externalIdThree = jdbcAnnotationDao.getExternalID(null);
       assertEquals(null, externalIdThree);
       
    }
}


