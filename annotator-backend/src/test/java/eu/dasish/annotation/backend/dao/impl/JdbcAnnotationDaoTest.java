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

import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.ResourceREF;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-config/annotationDao.xml"})
public class JdbcAnnotationDaoTest extends JdbcResourceDaoTest {

    @Autowired
    JdbcAnnotationDao jdbcAnnotationDao;    
    TestInstances testInstances = new TestInstances();

    
      /**
     * Test of retrieveSourceIDs method, of class JdbcAnnotationDao.
     */
    @Test
    public void testRetrieveSourceIDs() {
        System.out.println("retrieveSourceIDs");
        Number annotationID = 2;
        List<Number> result = jdbcAnnotationDao.retrieveSourceIDs(annotationID);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
    }
    
    ///////////////////////////////////////////
    @Test
    public void testDeleteAllAnnotationSource() throws SQLException{
        System.out.println("test deleteAllAnnotationSources");
        assertEquals(2, jdbcAnnotationDao.deleteAllAnnotationSource(2));
        assertEquals(0, jdbcAnnotationDao.deleteAllAnnotationSource(2));
    }
    
    ///////////////////////////////////////////
    @Test
    public void testDeleteAnnotationPrinciplePermissions() throws SQLException{
        System.out.println("test deleteAllAnnotationSources");
        int result = jdbcAnnotationDao.deleteAnnotationPrincipalPermissions(2);
        assertEquals(3, result);
        assertEquals(0, jdbcAnnotationDao.deleteAnnotationPrincipalPermissions(2));
    }
    
    ///////////////////////////////////////////
    @Test
    public void testAddAnnotationPrinciplePermission() throws SQLException{
        System.out.println("test addAnnotationSources");
        int result = jdbcAnnotationDao.addAnnotationPrincipalPermission(2, 1, Permission.READER);
        assertEquals(1, result);
    }
    
    ///////////////////////////////////////////
    @Test
    public void testAddAnnotationSource() throws SQLException{
        System.out.println("test addAnnotationSourcePair");
        assertEquals(1, jdbcAnnotationDao.addAnnotationSource(1,2));
    }
    
    ////////////////////////////////
    
    @Test
    public void testGetAnnotationInfos() {
        System.out.println("getAnnotationInfos");
        List<Number> annotIds = new ArrayList<Number>();
        annotIds.add(2);
        annotIds.add(3);
        annotIds.add(4);

        jdbcAnnotationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI);
        final List<AnnotationInfo> annotationInfos = jdbcAnnotationDao.getAnnotationInfos(annotIds);
        assertEquals(3, annotationInfos.size());

        assertEquals(TestBackendConstants._TEST_ANNOT_2_HEADLINE, annotationInfos.get(0).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_OWNER), annotationInfos.get(0).getOwner().getRef());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI + TestBackendConstants._TEST_ANNOT_2_EXT,
           annotationInfos.get(0).getRef());          
        //assertEquals(TestBackendConstants._TEST_ANNOT_1_TARGETS, annotationInfos.get(0).getTargetSources());

        assertEquals(TestBackendConstants._TEST_ANNOT_3_HEADLINE, annotationInfos.get(1).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_3_OWNER), annotationInfos.get(1).getOwner().getRef());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI + TestBackendConstants._TEST_ANNOT_3_EXT,
           annotationInfos.get(1).getRef()); 
        //assertEquals(TestBackendConstants._TEST_ANNOT_2_TARGETS, annotationInfos.get(1).getTargetSources());

        assertEquals(TestBackendConstants._TEST_ANNOT_4_HEADLINE, annotationInfos.get(2).getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_4_OWNER), annotationInfos.get(2).getOwner().getRef());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI + TestBackendConstants._TEST_ANNOT_4_EXT,
           annotationInfos.get(2).getRef()); 
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

        jdbcAnnotationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI);
        final List<ResourceREF> testList = jdbcAnnotationDao.getAnnotationREFs(annotIds);
        assertEquals(3, testList.size());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI +TestBackendConstants._TEST_ANNOT_2_EXT, testList.get(0).getRef());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI +TestBackendConstants._TEST_ANNOT_3_EXT, testList.get(1).getRef());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI + TestBackendConstants._TEST_ANNOT_4_EXT, testList.get(2).getRef());

        final List<ResourceREF> testListTwo = jdbcAnnotationDao.getAnnotationREFs(new ArrayList<Number>());
        assertEquals(0, testListTwo.size());

        final List<ResourceREF> testListThree = jdbcAnnotationDao.getAnnotationREFs(null);
        assertEquals(null, testListThree);

    }

    /**
     *
     * Test of getAnnotationID method, of class JdbcAnnotationDao. Integer
     * getAnnotationID(UUID externalID)
     */
    @Test
    public void getInternalID() throws SQLException {
        System.out.println("test getInternalID");

        final Number annotaionId = jdbcAnnotationDao.getInternalID(UUID.fromString(TestBackendConstants._TEST_ANNOT_2_EXT));
        assertEquals(2, annotaionId.intValue());

        final Number annotaionIdNE = jdbcAnnotationDao.getInternalID(UUID.fromString(TestBackendConstants._TEST_ANNOT_7_EXT_NOT_IN_DB));
        assertEquals(null, annotaionIdNE);

        final Number annotaionIdNull = jdbcAnnotationDao.getInternalID(null);
        assertEquals(null, annotaionIdNull);
    }
    
      /**
     * Test of getInternalIDFromURI method,
     * public Number getInternalIDFromURI(UUID externalID);
     */
    @Test
    public void testGetInternalIDFRomURI() {
        System.out.println("test getInternalIDFromURI");
        jdbcAnnotationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI);
        String uri = TestBackendConstants._TEST_SERVLET_URI + TestBackendConstants._TEST_ANNOT_2_EXT;
        Number result = jdbcAnnotationDao.getInternalIDFromURI(uri);
        assertEquals(2, result.intValue());
    }

    /**
     *
     * Test of getAnnotationWithoutSourcesAndPermissions method, of class JdbcAnnotationDao. Annotation
     * getAnnotation(Number annotationlID)
     */
    @Test
    public void getAnnotationWithoutSourcesAndPermissions() throws SQLException {
        System.out.println("test getAnnotationWithoutSources");

        /// dummy test
        final Annotation annotaionNull = jdbcAnnotationDao.getAnnotationWithoutSourcesAndPermissions(null);
        assertEquals(null, annotaionNull);
        ////
             
        final Number testAnnotationID = 2;
        jdbcAnnotationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI);
        final Annotation annotation = jdbcAnnotationDao.getAnnotationWithoutSourcesAndPermissions(testAnnotationID);
        assertEquals(TestBackendConstants._TEST_ANNOT_2_HEADLINE, annotation.getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_OWNER), annotation.getOwner().getRef());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_BODY, annotation.getBody().getValue()); 
        assertEquals(TestBackendConstants._TEST_BODY_MIMETYPE_HTML, annotation.getBody().getMimeType()); 
        assertEquals(TestBackendConstants._TEST_SERVLET_URI+TestBackendConstants._TEST_ANNOT_2_EXT, annotation.getURI());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_TIME_STAMP, annotation.getTimeStamp().toString());
    }

    /**
     * Test of deletAnnotation method, of class JdbcAnnotationDao.
     */
     /**
     * 
     * @param annotationId
     * @return removed annotation rows (should be 1)
     */
    @Test
    public void testDeleteAnnotation() throws SQLException {
        System.out.println("deleteAnnotation"); 
        
        // to provide integrity, first delete rows in the joint tables
        jdbcAnnotationDao.deleteAllAnnotationSource(5);
        jdbcAnnotationDao.deleteAnnotationPrincipalPermissions(5);
        
        assertEquals(1, jdbcAnnotationDao.deleteAnnotation(5));
        assertEquals(0, jdbcAnnotationDao.deleteAnnotation(5));
    }

    /**
     * Test of addAnnotation method, of class JdbcAnnotationDao.
     */
    @Test
    public void testAddAnnotation() throws SQLException {
        System.out.println("test_addAnnotation ");

        final Annotation annotationToAdd = testInstances.getAnnotationToAdd();// existing sources
        assertEquals(null, annotationToAdd.getURI());
        assertEquals(null, annotationToAdd.getTimeStamp());
        
        Number newAnnotationID = jdbcAnnotationDao.addAnnotation(annotationToAdd, 5);
        assertEquals(6, newAnnotationID);
        
        // checking
        Annotation addedAnnotation= jdbcAnnotationDao.getAnnotationWithoutSourcesAndPermissions(6);
        assertFalse(null == addedAnnotation.getURI());
        assertFalse(null == addedAnnotation.getTimeStamp());
        assertEquals(Integer.toString(5), addedAnnotation.getOwner().getRef());
        assertEquals(annotationToAdd.getBody().getMimeType(), addedAnnotation.getBody().getMimeType());
        assertEquals(annotationToAdd.getBody().getValue(), addedAnnotation.getBody().getValue()); 
        assertEquals(annotationToAdd.getHeadline(), addedAnnotation.getHeadline());
    }

  

    /**
     * testing public List<Number> retrieveAnnotationList(List<Number> sourceIDs);
    
    **/
    @Test    
    public void testRetrieveAnnotationList() {
        System.out.println("test retrieveAnnotationlist");
        List<Number> sources = new ArrayList<Number>();
        sources.add(1);
        sources.add(2);
        List<Number> result = jdbcAnnotationDao.retrieveAnnotationList(sources);
        assertEquals (2, result.size());
        assertEquals(2, result.get(0));
        assertEquals(3, result.get(1));
    }
    
    //////////////////////////////////
    
    
    @Test    
    public void testGetExternalID() {
        System.out.println("getExternalID");

        final UUID externalId = jdbcAnnotationDao.getExternalID(2);
        assertEquals(UUID.fromString(TestBackendConstants._TEST_ANNOT_2_EXT), externalId);


        final UUID externalIdThree = jdbcAnnotationDao.getExternalID(null);
        assertEquals(null, externalIdThree);

    }
    
    
    /** test
     * public List<Number> getFilteredAnnotationIDs(String link, String text, String access, String namespace, UUID owner, Timestamp after, Timestamp before) {
  **/
    
    @Test    
    public void testGetFilteredAnnotationIDs(){
        System.out.println(" test getFilteredAnnotationIDs");
        
        
        //////////////////////////////////////////
        // TEST 1 
        //final String link = "nl.wikipedia.org";
        final List<Number> annotationIDs = new ArrayList<Number>();
        annotationIDs.add(2);
        annotationIDs.add(3);
        
        List<Number> result_1 = jdbcAnnotationDao.getFilteredAnnotationIDs(annotationIDs, null, null, null, null, null, null);        
        assertEquals(2, result_1.size());
        assertEquals(2, result_1.get(0));
        assertEquals(3, result_1.get(1));
        
       
        List<Number> result_2 = jdbcAnnotationDao.getFilteredAnnotationIDs(annotationIDs, "some html", null, null, null, null, null);        
        assertEquals(2, result_2.size());
        assertEquals(2, result_2.get(0));
        assertEquals(3, result_2.get(1));
        
        
       
        List<Number> result_3 = jdbcAnnotationDao.getFilteredAnnotationIDs(annotationIDs, "some html", null, null, 3, null, null);        
        assertEquals(1, result_3.size());
        assertEquals(2, result_3.get(0));
        
       
        Timestamp after = new Timestamp(0); 
        Timestamp before = new Timestamp(System.currentTimeMillis());  
        List<Number> result_4 = jdbcAnnotationDao.getFilteredAnnotationIDs(annotationIDs, "some html", null, null, 3, after, before);        
        assertEquals(1, result_4.size());
        assertEquals(2, result_4.get(0));
        
        
        Timestamp after_1 = new Timestamp(System.currentTimeMillis()); // no annotations added after "now"       
        List<Number> result_5 = jdbcAnnotationDao.getFilteredAnnotationIDs(annotationIDs, "some html", null, null, 3, after_1, null);        
        assertEquals(0, result_5.size());
        
        
    }
    
    //////////////////////////////////
    @Test
    public void testUpdateBodyText() throws SQLException{
        System.out.println("test updateBodyText");
        String newBodyText = "new body";
        int result = jdbcAnnotationDao.updateBodyText(2, newBodyText);
        assertEquals(1, result);
        Annotation updatedAnnotation= jdbcAnnotationDao.getAnnotationWithoutSourcesAndPermissions(2);
        assertEquals(newBodyText, updatedAnnotation.getBody().getValue());
    }

    //////////////////////////////////
    @Test
    public void testUpdateMimeType() throws SQLException{
        System.out.println("test updateBodyMimeType");
        String newBodyMimeType = "text/xml";
        int result = jdbcAnnotationDao.updateBodyMimeType(2, newBodyMimeType);
        assertEquals(1, result);
        Annotation updatedAnnotation= jdbcAnnotationDao.getAnnotationWithoutSourcesAndPermissions(2);
        assertEquals(newBodyMimeType, updatedAnnotation.getBody().getMimeType());
    }
    
    // public List<Map<Number, String>> retrievePermissions(Number annotationId)
    
    @Test
    public void testRetrievePermissions (){
        // VALUES (2, 3, 'owner'); 
        //VALUES (2, 4, 'writer');
        //VALUES (2, 5, 'reader');
        System.out.println("test Permissions");
        List<Map<Number, String>> result = jdbcAnnotationDao.retrievePermissions(2);
        assertEquals(3, result.size());
        assertEquals("owner", result.get(0).get(3));
        assertEquals("writer", result.get(1).get(4));
        assertEquals("reader", result.get(2).get(5));
        
        
    }
}
