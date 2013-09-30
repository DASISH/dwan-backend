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
import eu.dasish.annotation.schema.AnnotationList;
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
import org.junit.Ignore;

/**
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
// TODO: Move integrating methods from JdbcNotebookDao to DaoDispatcher, and move corresponding tests to DaoDistatcherTest.class,  
//remove from here all the  mockery and retest.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test-config/mockery.xml", "/spring-test-config/mockAnnotationDao.xml", 
    "/spring-test-config/mockSourceDao.xml", "/spring-test-config/mockUserDao.xml",
    "/spring-test-config/mockCachedRepresentationDao.xml", "/spring-test-config/mockUriInfo.xml",
    "/spring-test-config/dataSource.xml", "/spring-test-config/mockDBIntegrityService.xml","/spring-config/notebookDao.xml"})
public class JdbcNotebookDaoTest extends JdbcResourceDaoTest{

    @Autowired
    JdbcNotebookDao jdbcNotebookDao;
    @Autowired
    private AnnotationDao annotationDao;
    @Autowired
    private Mockery mockery;

    /**
     * Test of stringURItoExternalID method
     * public String stringURItoExternalID(String uri);
     */
    @Test
    @Ignore
    public void testStringURItoExternalID() {
        System.out.println("test stringURItoExternalID");
        jdbcNotebookDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_notebooks);
        String randomUUID = UUID.randomUUID().toString();
        String uri = TestBackendConstants._TEST_SERVLET_URI_notebooks + randomUUID;
        String externalID = jdbcNotebookDao.stringURItoExternalID(uri);
        assertEquals(randomUUID, externalID);
    }
    
    /**
     * Test of externalIDtoURI method
     * public String externalIDtoURI(String externalID);
     */
    @Test
    @Ignore
    public void testExternalIDtoURI() {
        System.out.println("test stringURItoExternalID");
        jdbcNotebookDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_notebooks);
        String randomUUID = UUID.randomUUID().toString();
        String uri = TestBackendConstants._TEST_SERVLET_URI_notebooks+randomUUID;
        String uriResult = jdbcNotebookDao.externalIDtoURI(randomUUID);
        assertEquals(uri, uriResult);
    }
    
    /**
     * Test of getNotebookInfos method, of class JdbcNotebookDao.
     */
    @Test
    @Ignore
    public void testGetNotebookInfos() {
        final List<NotebookInfo> notebookInfoList = jdbcNotebookDao.getNotebookInfos(UUID.fromString(TestBackendConstants._TEST_UID_2_));
        assertEquals(2, notebookInfoList.size());
        assertEquals("a notebook", notebookInfoList.get(0).getTitle());
    }

    /**
     * Test of getUsersNotebooks method, of class JdbcNotebookDao.
     */
    @Test
    @Ignore
    public void testGetUsersNotebooks() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        
        ResourceREF testRef = new ResourceREF();
        testRef.setRef(TestBackendConstants._TEST_SERVLET_URI_notebooks+TestBackendConstants._TEST_AID_1_);
        final List<ResourceREF> testResult = Arrays.asList(new ResourceREF[] {testRef});
        
        mockery.checking(new Expectations() {
            {
                exactly(2).of(annotationDao).getAnnotationREFs(Arrays.asList(new Number[] {1}));// exactly 2 notebooks (their id-s 1 and 2) contain the annotation 1
                will(returnValue(testResult));
                // necessary to set annotations reference lists in the notebook
            }
        });
        
        jdbcNotebookDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_notebooks);
        final List<Notebook> notebooks = jdbcNotebookDao.getUsersNotebooks(UUID.fromString(TestBackendConstants._TEST_UID_2_));


        assertEquals(2, notebooks.size());
        assertEquals("a notebook", notebooks.get(0).getTitle());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_notebooks+TestBackendConstants._TEST_NOTEBOOK_1_EXT_ID, notebooks.get(0).getURI());
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
        
        final List<Notebook> notebooksEmpty = jdbcNotebookDao.getUsersNotebooks(UUID.fromString(TestBackendConstants._TEST_UID_1_));
        assertEquals(0, notebooksEmpty.size());
    }

    /**
     * Test of addNotebook method, of class JdbcNotebookDao.
     */
    @Test    
    @Ignore
    public void testAddNotebook() throws URISyntaxException {
        final UUID addedNotebookId = jdbcNotebookDao.addNotebook(UUID.fromString(TestBackendConstants._TEST_UID_2_), "a title");
        assertEquals(36, addedNotebookId.toString().length());
    }

    /**
     * Test of deleteNotebook method, of class JdbcNotebookDao.
     */
    @Test    
    @Ignore
    public void testDeleteNotebook() {
        System.out.println("deleteNotebook");
        UUID notebookId = UUID.fromString(TestBackendConstants._TEST_NOTEBOOK_2_EXT_ID);
        int result = jdbcNotebookDao.deleteNotebook(notebookId);
        assertEquals(1, result);
    }
    
      /**
     * Test of getAnnotationIDs method, of class JdbcAnnotationDao.
     * List<Number> getAnnotationIDs(Number notebookID)
     */
    @Test    
    @Ignore
    public void testGetAnnotationIDs() {
        System.out.println("getAnnotationIDs");
        
        // test one, 2-element notebook
        final List<Number> annotationIDs = jdbcNotebookDao.getAnnotationIDs(3);
        assertEquals(2, annotationIDs.size());
        assertEquals(2, annotationIDs.get(0).intValue());
        assertEquals(3, annotationIDs.get(1).intValue());
        
        // test two, 1-element notebook
        final List<Number> annotationIDsTwo = jdbcNotebookDao.getAnnotationIDs(4);
        assertEquals(1, annotationIDsTwo.size());
        assertEquals(4, annotationIDsTwo.get(0).intValue());
        
        // test three, empty notebook
        final List<Number> annotationIDsThree = jdbcNotebookDao.getAnnotationIDs(6);
        assertEquals(0, annotationIDsThree.size());
        
        // test four, null-notebook
        final List<Number> annotationIDsFour = jdbcNotebookDao.getAnnotationIDs(null);
        assertEquals(0, annotationIDsFour.size());
        
        
    }
    
    
    
    /**
     * Test of getAnnotationREFsOfNotebook method, of class JdbcAnnotationDao.
     * List<ResourceREF> getAnnotationREFsOfNotebook(Number notebookID)
     */
    @Test
    @Ignore    
    public void testGetAnnotationREFsOfNotebook() {
        System.out.println("getAnnotationREFsOfNotebook");
        
        jdbcNotebookDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_notebooks);
        annotationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_annotations);
        // test One         
        setMockeryNotebookOne(); 
        List<ResourceREF> testList = jdbcNotebookDao.getAnnotationREFsOfNotebook(3);
        assertEquals(2, testList.size());        
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations+TestBackendConstants._TEST_ANNOT_2_EXT, testList.get(0).getRef());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations+TestBackendConstants._TEST_ANNOT_3_EXT, testList.get(1).getRef());
        
        // test Two
        setMockeryNotebookTwo(); 
        List<ResourceREF> testListTwo = jdbcNotebookDao.getAnnotationREFsOfNotebook(4);
        assertEquals(1, testListTwo.size());        
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations+TestBackendConstants._TEST_ANNOT_4_EXT, testListTwo.get(0).getRef());
        
        // test Three  "empty" 
        setMockeryNotebookThreeEmpty();         
        List<ResourceREF> testListThree = jdbcNotebookDao.getAnnotationREFsOfNotebook(6);
        assertEquals(0, testListThree.size()); 
       
        // test Five Null-notebook
        setMockeryNotebookNonExisting();
        List<ResourceREF> testListFive = jdbcNotebookDao.getAnnotationREFsOfNotebook(null);
        assertEquals(0, testListFive.size()); 
    }

    /**
     * Test of getAnnotations method, of class JdbcNotebookDao.
     * Annotations getAnnotations(Number notebookID)
     */
    @Test    
    @Ignore
    public void testGetAnnotations() {
        System.out.println("getAnnotations");
        
        jdbcNotebookDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_notebooks);
        
         // test One
        setMockeryNotebookOne(); 
        AnnotationList annotations = jdbcNotebookDao.getAnnotations(3);
        assertEquals(2, annotations.getAnnotation().size());        
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations+TestBackendConstants._TEST_ANNOT_2_EXT, annotations.getAnnotation().get(0).getRef());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations+TestBackendConstants._TEST_ANNOT_3_EXT, annotations.getAnnotation().get(1).getRef());
        
        // test Two
        setMockeryNotebookTwo(); 
        AnnotationList annotationsTwo = jdbcNotebookDao.getAnnotations(4);
        assertEquals(1, annotationsTwo.getAnnotation().size());        
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations+TestBackendConstants._TEST_ANNOT_4_EXT, annotationsTwo.getAnnotation().get(0).getRef());
        
        // test Three  "empty" list of annotations
        // according to dasish.xsd if an Annotation is created then its list of annotations must contain at least one element!
        // therefore: no annotations in the notebook ==> Annotations-pbject must be null :(
        setMockeryNotebookThreeEmpty(); 
        AnnotationList annotationsThree = jdbcNotebookDao.getAnnotations(6);
        assertEquals(null, annotationsThree); 
        
       
        // test Five Null-notebook
        setMockeryNotebookNonExisting();
        AnnotationList annotationsFive = jdbcNotebookDao.getAnnotations(null);
        assertEquals(null, annotationsFive);
    }
    
    /** Test of getNotebookInfo method, of class JdbcNotebookDao.
     * 
     */
    @Test     
    @Ignore
    public void testGetNotebookInfo() {
        System.out.println("test getNotebookInfo");
        
        jdbcNotebookDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_notebooks);
        
         // test One        
        NotebookInfo info = jdbcNotebookDao.getNotebookInfo(3);
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_notebooks+"/"+TestBackendConstants._TEST_NOTEBOOK_3_EXT, info.getRef());
        assertEquals(TestBackendConstants._TEST_NOTEBOOK_3_TITLE, info.getTitle());
        
               
        // test Three Null-notebook
       NotebookInfo infoThree = jdbcNotebookDao.getNotebookInfo(null);
       assertEquals(null, infoThree);
    }
    
    /** Test of getNotebookID method, of class JdbcNotebookDao.
     * 
     */
    @Test
    @Ignore    
    public void testGetNotebookID() {
        System.out.println("test getNotebookID");
        
         // test One        
        Number resultOne= jdbcNotebookDao.getInternalID(UUID.fromString(TestBackendConstants._TEST_NOTEBOOK_3_EXT));
        assertEquals(3, resultOne.intValue());
        
      
        // test Three Null-notebook
       Number resultThree= jdbcNotebookDao.getInternalID(null);
       assertEquals(null, resultThree);
    }
    
    
    @Test    
    @Ignore
    public void testGetAnnotationExternalIDs() {
        System.out.println("test getExternalAnnotationIds");
        
         // test One 
        mockery.checking(new Expectations() {
            {
              oneOf(annotationDao).getExternalID(2);
              will(returnValue(UUID.fromString(TestBackendConstants._TEST_ANNOT_2_EXT)));
              
              oneOf(annotationDao).getExternalID(3);
              will(returnValue(UUID.fromString(TestBackendConstants._TEST_ANNOT_3_EXT)));
            }
        }); 
        
        List<UUID> resultOne= jdbcNotebookDao.getAnnotationExternalIDs(UUID.fromString(TestBackendConstants._TEST_NOTEBOOK_3_EXT));
        assertEquals(TestBackendConstants._TEST_ANNOT_2_EXT, resultOne.get(0).toString());
        assertEquals(TestBackendConstants._TEST_ANNOT_3_EXT, resultOne.get(1).toString());
        
        
      
        // test Two, non-existing notebook
        List<UUID> resultThree= jdbcNotebookDao.getAnnotationExternalIDs(null);
        assertEquals(0, resultThree.size());
       
    }
    
    
    ////////////////////////////////////////////////////////////////////
    //////// Setting Mockeries /////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////
    
    private void setMockeryNotebookOne(){        
        ResourceREF testRefOne = new ResourceREF();
        testRefOne.setRef(TestBackendConstants._TEST_SERVLET_URI_annotations+TestBackendConstants._TEST_ANNOT_2_EXT);
        ResourceREF testRefTwo = new ResourceREF();
        testRefTwo.setRef(TestBackendConstants._TEST_SERVLET_URI_annotations+TestBackendConstants._TEST_ANNOT_3_EXT);
        final List<ResourceREF> testResult = Arrays.asList(new ResourceREF[] {testRefOne, testRefTwo});
        
        mockery.checking(new Expectations() {
            {
              oneOf(annotationDao).getAnnotationREFs(Arrays.asList(new Number[] {2, 3}));
              will(returnValue(testResult));
            }
        });    
    }

     private void setMockeryNotebookTwo(){ 
        ResourceREF testRef = new ResourceREF();
        testRef.setRef(String.valueOf(TestBackendConstants._TEST_SERVLET_URI_annotations+TestBackendConstants._TEST_ANNOT_4_EXT));
        final List<ResourceREF> testResultTwo = Arrays.asList(new ResourceREF[] {testRef});
        
        mockery.checking(new Expectations() {
            {
              oneOf(annotationDao).getAnnotationREFs(Arrays.asList(new Number[] {4}));
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