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
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.NotebookIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.Annotations;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.ResourceREF;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test-config/mockery.xml", "/spring-test-config/mockAnnotationDao.xml", 
    "/spring-test-config/dataSource.xml", "/spring-config/notebookDao.xml"})
public class JdbcNotebookDaoTest extends JdbcResourceDaoTest{

    @Autowired
    JdbcNotebookDao jdbcNotebookDao;
    @Autowired
    private AnnotationDao annotationDao;
    @Autowired
    private Mockery mockery;

    @Test
    public void testIsNotebookInTheDataBase(){
        super.testIsNotebookInTheDataBase(jdbcNotebookDao);
    }
    
    
    /**
     * Test of getNotebookInfos method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetNotebookInfos() {
        final List<NotebookInfo> notebookInfoList = jdbcNotebookDao.getNotebookInfos(new UserIdentifier(TestBackendConstants._TEST_UID_2_));
        assertEquals(2, notebookInfoList.size());
        assertEquals("a notebook", notebookInfoList.get(0).getTitle());
    }

    /**
     * Test of getUsersNotebooks method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetUsersNotebooks() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        
        ResourceREF testRef = new ResourceREF();
        testRef.setRef("1");
        final List<ResourceREF> testResult = Arrays.asList(new ResourceREF[] {testRef});
        
        mockery.checking(new Expectations() {
            {
                exactly(2).of(annotationDao).getAnnotationREFs(Arrays.asList(new Number[] {1}));// exactly 2 notebooks (their id-s 1 and 2) contains the annotation 1
                will(returnValue(testResult));
            }
        });
        
        final List<Notebook> notebooks = jdbcNotebookDao.getUsersNotebooks(new UserIdentifier(TestBackendConstants._TEST_UID_2_));


        assertEquals(2, notebooks.size());
        assertEquals("a notebook", notebooks.get(0).getTitle());
//        assertEquals("http://123456", notebooks.get(0).getURI());
        assertNotNull(notebooks.get(0).getTimeStamp());
        assertEquals(year, notebooks.get(0).getTimeStamp().getYear());
        assertEquals(month + 1, notebooks.get(0).getTimeStamp().getMonth());
        assertEquals(day, notebooks.get(0).getTimeStamp().getDay());
        
        
        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getAnnotationREFs(new ArrayList<Number>());
                will(returnValue(new ArrayList<ResourceREF>()));
            }
        });
        
        final List<Notebook> notebooksEmpty = jdbcNotebookDao.getUsersNotebooks(new UserIdentifier(TestBackendConstants._TEST_UID_1_));
        assertEquals(0, notebooksEmpty.size());
    }

    /**
     * Test of addNotebook method, of class JdbcNotebookDao.
     */
    @Test
    public void testAddNotebook() throws URISyntaxException {
        final NotebookIdentifier addedNotebookId = jdbcNotebookDao.addNotebook(new UserIdentifier(TestBackendConstants._TEST_UID_2_), "a title");
        assertEquals(36, addedNotebookId.getUUID().toString().length());
    }

    /**
     * Test of deleteNotebook method, of class JdbcNotebookDao.
     */
    @Test
    public void testDeleteNotebook() {
        System.out.println("deleteNotebook");
        NotebookIdentifier notebookId = new NotebookIdentifier(new UUID(0, 2));
        int result = jdbcNotebookDao.deleteNotebook(notebookId);
        assertEquals(1, result);
    }
    
      /**
     * Test of getAnnotationIDs method, of class JdbcAnnotationDao.
     * List<Number> getAnnotationIDs(Number notebookID)
     */
    @Test
    public void testGetAnnotationIDs() {
        System.out.println("getAnnotationIDs");
        
        // test one, 2-element notebook
        final List<Number> annotationIDs = jdbcNotebookDao.getAnnotationIDs(TestBackendConstants._TEST_NOTEBOOK_1_INT);
        assertEquals(2, annotationIDs.size());
        assertEquals(TestBackendConstants._TEST_ANNOT_1_INT, annotationIDs.get(0).intValue());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_INT, annotationIDs.get(1).intValue());
        
        // test two, 1-element notebook
        final List<Number> annotationIDsTwo = jdbcNotebookDao.getAnnotationIDs(TestBackendConstants._TEST_NOTEBOOK_2_INT);
        assertEquals(1, annotationIDsTwo.size());
        assertEquals(TestBackendConstants._TEST_ANNOT_3_INT, annotationIDsTwo.get(0).intValue());
        
        // test three, empty notebook
        final List<Number> annotationIDsThree = jdbcNotebookDao.getAnnotationIDs(TestBackendConstants._TEST_NOTEBOOK_3_INT);
        assertEquals(0, annotationIDsThree.size());
        
        // test four, null-notebook
        final List<Number> annotationIDsFour = jdbcNotebookDao.getAnnotationIDs(null);
        assertEquals(null, annotationIDsFour);
        
        //test five, the notebook not in the DB
        final List<Number> annotationIDsFive = jdbcNotebookDao.getAnnotationIDs(TestBackendConstants._TEST_ANNOT_4_INT_NOT_IN_THE_DB);
        assertEquals(null, annotationIDsFive);
        
    }
    
    
    
    /**
     * Test of getAnnotationREFsOfNotebook method, of class JdbcAnnotationDao.
     * List<ResourceREF> getAnnotationREFsOfNotebook(Number notebookID)
     */
    @Test
    public void testGetAnnotationREFsOfNotebook() {
        System.out.println("getAnnotationREFsOfNotebook");
        
        // test One         
        setMockeryNotebookOne();         
        List<ResourceREF> testList = jdbcNotebookDao.getAnnotationREFsOfNotebook(TestBackendConstants._TEST_NOTEBOOK_1_INT);
        assertEquals(2, testList.size());        
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_1_INT), testList.get(0).getRef());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_INT), testList.get(1).getRef());
        
        // test Two
        setMockeryNotebookTwo(); 
        List<ResourceREF> testListTwo = jdbcNotebookDao.getAnnotationREFsOfNotebook(TestBackendConstants._TEST_NOTEBOOK_2_INT);
        assertEquals(1, testListTwo.size());        
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_3_INT), testListTwo.get(0).getRef());
        
        // test Three  "empty" 
        setMockeryNotebookThreeEmpty();         
        List<ResourceREF> testListThree = jdbcNotebookDao.getAnnotationREFsOfNotebook(TestBackendConstants._TEST_NOTEBOOK_3_INT);
        assertEquals(0, testListThree.size()); 
        
        // test Four, non-existing notebook
        setMockeryNotebookNonExisting();
        List<ResourceREF> testListFour = jdbcNotebookDao.getAnnotationREFsOfNotebook(TestBackendConstants._TEST_ANNOT_4_INT_NOT_IN_THE_DB);
        assertEquals(null, testListFour); 
        
        // test Five Null-notebook
        setMockeryNotebookNonExisting();
        List<ResourceREF> testListFive = jdbcNotebookDao.getAnnotationREFsOfNotebook(null);
        assertEquals(null, testListFive); 
    }

    /**
     * Test of getAnnotations method, of class JdbcNotebookDao.
     * Annotations getAnnotations(Number notebookID)
     */
    @Test      
    public void testGetAnnotations() {
        System.out.println("getAnnotations");
        
         // test One
        setMockeryNotebookOne(); 
        Annotations annotations = jdbcNotebookDao.getAnnotations(TestBackendConstants._TEST_NOTEBOOK_1_INT);
        assertEquals(2, annotations.getAnnotation().size());        
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_1_INT), annotations.getAnnotation().get(0).getRef());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_INT), annotations.getAnnotation().get(1).getRef());
        
        // test Two
        setMockeryNotebookTwo(); 
        Annotations annotationsTwo = jdbcNotebookDao.getAnnotations(TestBackendConstants._TEST_NOTEBOOK_2_INT);
        assertEquals(1, annotationsTwo.getAnnotation().size());        
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_3_INT), annotationsTwo.getAnnotation().get(0).getRef());
        
        // test Three  "empty" list of annotations
        // according to dasish.xsd if an Annotation is created then its list of annotations must contain at least one element!
        // therefore: no annotations in the notebook ==> Annotations-pbject must be null :(
        setMockeryNotebookThreeEmpty(); 
        Annotations annotationsThree = jdbcNotebookDao.getAnnotations(TestBackendConstants._TEST_NOTEBOOK_3_INT);
        assertEquals(null, annotationsThree); 
        
        // test Five, non-existing notebook
        setMockeryNotebookNonExisting();
        Annotations annotationsFour = jdbcNotebookDao.getAnnotations(TestBackendConstants._TEST_ANNOT_4_INT_NOT_IN_THE_DB);
        assertEquals(null, annotationsFour); 
        
        // test Five Null-notebook
        setMockeryNotebookNonExisting();
        Annotations annotationsFive = jdbcNotebookDao.getAnnotations(null);
        assertEquals(null, annotationsFive);
    }
    
    /** Test of getNotebookInfo method, of class JdbcNotebookDao.
     * 
     */
    @Test  
    public void testGetNotebookInfo() {
        System.out.println("test getNotebookInfo");
        
         // test One        
        NotebookInfo info = jdbcNotebookDao.getNotebookInfo(TestBackendConstants._TEST_NOTEBOOK_1_INT);
        assertEquals(String.valueOf(TestBackendConstants._TEST_NOTEBOOK_1_EXT), info.getRef());
        assertEquals(String.valueOf(TestBackendConstants._TEST_NOTEBOOK_1_TITLE), info.getTitle());
        
        // test Two, non-existing notebook
        NotebookInfo infoTwo = jdbcNotebookDao.getNotebookInfo(TestBackendConstants._TEST_ANNOT_4_INT_NOT_IN_THE_DB);
        assertEquals(null, infoTwo); 
        
        // test Three Null-notebook
       NotebookInfo infoThree = jdbcNotebookDao.getNotebookInfo(null);
       assertEquals(null, infoThree);
    }
    
    /** Test of getNotebookID method, of class JdbcNotebookDao.
     * 
     */
    @Test  
    public void testGetNotebookID() {
        System.out.println("test getNotebookID");
        
         // test One        
        Number resultOne= jdbcNotebookDao.getNotebookID(new NotebookIdentifier(TestBackendConstants._TEST_NOTEBOOK_1_EXT));
        assertEquals(TestBackendConstants._TEST_NOTEBOOK_1_INT, resultOne.intValue());
        
        // test Two, non-existing notebook
        Number resultTwo= jdbcNotebookDao.getNotebookID(new NotebookIdentifier(TestBackendConstants._TEST_ANNOT_4_EXT_NOT_IN_THE_DB));
        assertEquals(null, resultTwo); 
        
        // test Three Null-notebook
       Number resultThree= jdbcNotebookDao.getNotebookID(null);
       assertEquals(null, resultThree);
    }
    
    
    @Test  
    public void testGetAnnotationExternalIDs() {
        System.out.println("test getExternalAnnotationIds");
        
         // test One 
        mockery.checking(new Expectations() {
            {
              oneOf(annotationDao).getExternalID(TestBackendConstants._TEST_ANNOT_1_INT);
              will(returnValue(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_1_EXT)));
              
              oneOf(annotationDao).getExternalID(TestBackendConstants._TEST_ANNOT_2_INT);
              will(returnValue(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_2_EXT)));
            }
        }); 
        
        List<AnnotationIdentifier> resultOne= jdbcNotebookDao.getAnnotationExternalIDs(new NotebookIdentifier(TestBackendConstants._TEST_NOTEBOOK_1_EXT));
        assertEquals(TestBackendConstants._TEST_ANNOT_1_EXT, resultOne.get(0).toString());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_EXT, resultOne.get(1).toString());
        
        
        List<AnnotationIdentifier> resultTwo= jdbcNotebookDao.getAnnotationExternalIDs(new NotebookIdentifier(TestBackendConstants._TEST_ANNOT_4_EXT_NOT_IN_THE_DB));
        assertEquals(null, resultTwo);
        
        // test Two, non-existing notebook
        List<AnnotationIdentifier> resultThree= jdbcNotebookDao.getAnnotationExternalIDs(null);
        assertEquals(null, resultThree);
       
    }
    
    
    ////////////////////////////////////////////////////////////////////
    //////// Setting Mockeries /////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////
    
    private void setMockeryNotebookOne(){        
        ResourceREF testRefOne = new ResourceREF();
        testRefOne.setRef(String.valueOf(TestBackendConstants._TEST_ANNOT_1_INT));
        ResourceREF testRefTwo = new ResourceREF();
        testRefTwo.setRef(String.valueOf(TestBackendConstants._TEST_ANNOT_2_INT));
        final List<ResourceREF> testResult = Arrays.asList(new ResourceREF[] {testRefOne, testRefTwo});
        
        mockery.checking(new Expectations() {
            {
              oneOf(annotationDao).getAnnotationREFs(Arrays.asList(new Number[] {TestBackendConstants._TEST_ANNOT_1_INT, TestBackendConstants._TEST_ANNOT_2_INT}));
              will(returnValue(testResult));
            }
        });    
    }

     private void setMockeryNotebookTwo(){ 
        ResourceREF testRef = new ResourceREF();
        testRef.setRef(String.valueOf(TestBackendConstants._TEST_ANNOT_3_INT));
        final List<ResourceREF> testResultTwo = Arrays.asList(new ResourceREF[] {testRef});
        
        mockery.checking(new Expectations() {
            {
              oneOf(annotationDao).getAnnotationREFs(Arrays.asList(new Number[] {TestBackendConstants._TEST_ANNOT_3_INT}));
              will(returnValue(testResultTwo));
            }
        }); 
     }   
     
     private void setMockeryNotebookThreeEmpty(){ 
         mockery.checking(new Expectations() {
            {
              oneOf(annotationDao).getAnnotationREFs(new ArrayList<Number>());
              will(returnValue(new ArrayList<ResourceREF>()));
            }
        });        
     }
     
     private void setMockeryNotebookNonExisting() {         
         mockery.checking(new Expectations() {
            {
              oneOf(annotationDao).getAnnotationREFs(null);
              will(returnValue(null));
            }
        }); 
     }
        
     
}