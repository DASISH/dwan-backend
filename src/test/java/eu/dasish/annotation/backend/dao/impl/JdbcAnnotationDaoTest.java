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
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.Access;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import static org.junit.Assert.*;
import org.junit.Ignore;
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
    TestInstances testInstances = new TestInstances(TestBackendConstants._TEST_SERVLET_URI);
    
     /**
     * Test of stringURItoExternalID method
     * public String stringURItoExternalID(String uri);
     */
    @Test
    public void testStringURItoExternalID() {
        System.out.println("test stringURItoExternalID");
        jdbcAnnotationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_annotations);
        String randomUUID = UUID.randomUUID().toString();
        String uri = TestBackendConstants._TEST_SERVLET_URI_annotations + randomUUID;
        String externalID = jdbcAnnotationDao.stringURItoExternalID(uri);
        assertEquals(randomUUID, externalID);
    }
    
    /**
     * Test of externalIDtoURI method
     * public String externalIDtoURI(String externalID);
     */
    @Test
    public void testExternalIDtoURI() {
        System.out.println("test stringURItoExternalID");
        jdbcAnnotationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_annotations);
        String randomUUID = UUID.randomUUID().toString();
        String uri = TestBackendConstants._TEST_SERVLET_URI_annotations+randomUUID;
        String uriResult = jdbcAnnotationDao.externalIDtoURI(randomUUID);
        assertEquals(uri, uriResult);
    }

     /**
     * Test of retrieveTargetIDs method, of class JdbcAnnotationDao.
     */
 
    
     /**
     * Test of getAnnotations method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetAnnotations() {
        System.out.println("test getAnnotations");
        List<Number> expResult = new ArrayList<Number>();
        expResult.add(1);
        expResult.add(2);
        List<Number> result = jdbcAnnotationDao.getAnnotations(1);
        assertEquals(expResult, result);
    }

    
    ///////////////////////////////////////////
    @Test
    public void testDeleteAllAnnotationTarget() throws SQLException{
        System.out.println("test deleteAllAnnotationTargets");
        assertEquals(2, jdbcAnnotationDao.deleteAllAnnotationTarget(1));
        assertEquals(0, jdbcAnnotationDao.deleteAllAnnotationTarget(1));
    }
    
    ///////////////////////////////////////////
    @Test
    public void testDeleteAnnotationPrincipleAccesss() throws SQLException{
        System.out.println("test deleteAllAnnotationTargets");
        int result = jdbcAnnotationDao.deleteAnnotationPrincipalAccesss(1);
        assertEquals(3, result);
        assertEquals(0, jdbcAnnotationDao.deleteAnnotationPrincipalAccesss(1));
    }
    
    ///////////////////////////////////////////
    @Test
    public void testAddAnnotationPrincipleAccess() throws SQLException{
        System.out.println("test addAnnotationTargets");
        int result = jdbcAnnotationDao.addAnnotationPrincipalAccess(1, 1, Access.READ);
        assertEquals(1, result);
    }
    
    ///////////////////////////////////////////
    @Test
    public void testAddAnnotationTarget() throws SQLException{
        System.out.println("test addAnnotationTargetPair");
        assertEquals(1, jdbcAnnotationDao.addAnnotationTarget(1,3));
    }
    
    ////////////////////////////////
    
//    @Test
//    public void testGetAnnotationInfos() {
//        System.out.println("getAnnotationInfos");
//        List<Number> annotIds = new ArrayList<Number>();
//        annotIds.add(2);
//        annotIds.add(3);
//        annotIds.add(4);
//
//        jdbcAnnotationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_annotations);
//        final List<AnnotationInfo> annotationInfos = jdbcAnnotationDao.getAnnotationInfos(annotIds);
//        assertEquals(3, annotationInfos.size());
//
//        assertEquals("Sagrada Famiglia", annotationInfos.get(0).getHeadline());
//        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_OWNER), annotationInfos.get(0).getOwnerRef());
//        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations +"00000000-0000-0000-0000-000000000021",
//           annotationInfos.get(0).getRef());          
//        //assertEquals(TestBackendConstants._TEST_ANNOT_1_TARGETS, annotationInfos.get(0).getTargetTargets());
//
//        assertEquals("Gaudi", annotationInfos.get(1).getHeadline());
//        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_3_OWNER), annotationInfos.get(1).getOwnerRef());
//        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations + "00000000-0000-0000-0000-000000000022",
//           annotationInfos.get(1).getRef()); 
//        //assertEquals(TestBackendConstants._TEST_ANNOT_2_TARGETS, annotationInfos.get(1).getTargetTargets());
//
//        assertEquals(TestBackendConstants._TEST_ANNOT_4_HEADLINE, annotationInfos.get(2).getHeadline());
//        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_4_OWNER), annotationInfos.get(2).getOwnerRef());
//        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations + "00000000-0000-0000-0000-000000000023",
//           annotationInfos.get(2).getRef()); 
//        //assertEquals(TestBackendConstants._TEST_ANNOT_3_TARGETS, annotationInfos.get(2).getTargetTargets());
//
//        final List<AnnotationInfo> annotationInfosNull = jdbcAnnotationDao.getAnnotationInfos(null);
//        assertEquals(null, annotationInfosNull);
//
//        final List<AnnotationInfo> annotationInfosZeroSize = jdbcAnnotationDao.getAnnotationInfos(new ArrayList<Number>());
//        assertEquals(0, annotationInfosZeroSize.size());
//
//
//    }

    /**
     * Test of getAnnotationREFs method, of class JdbcAnnotationDao.
     * List<ReTargetREF> getAnnotationREFs(List<Number> annotationIDs)
     */
    @Test
    public void testGetAnnotationREFs() {
        System.out.println("getAnnotationREFs");
        List<Number> annotIds = new ArrayList<Number>();
        annotIds.add(1);
        annotIds.add(2);
        annotIds.add(3);

        jdbcAnnotationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_annotations);
        final List<String> testList = jdbcAnnotationDao.getAnnotationREFs(annotIds);
        assertEquals(3, testList.size());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations+"00000000-0000-0000-0000-000000000021", testList.get(0));
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations+"00000000-0000-0000-0000-000000000022", testList.get(1));
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations+"00000000-0000-0000-0000-000000000023", testList.get(2));

        final List<String> testListTwo = jdbcAnnotationDao.getAnnotationREFs(new ArrayList<Number>());
        assertEquals(0, testListTwo.size());

        final List<String> testListThree = jdbcAnnotationDao.getAnnotationREFs(null);
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

        final Number annotaionId = jdbcAnnotationDao.getInternalID(UUID.fromString("00000000-0000-0000-0000-000000000021"));
        assertEquals(1, annotaionId.intValue());

        final Number annotaionIdNE = jdbcAnnotationDao.getInternalID(UUID.fromString("00000000-0000-0000-0000-0000000000cc"));
        assertEquals(null, annotaionIdNE);

       
    }
    
      /**
     * Test of getInternalIDFromURI method,
     * public Number getInternalIDFromURI(UUID externalID);
     */
    @Test
    public void testGetInternalIDFRomURI() {
        System.out.println("test getInternalIDFromURI");
        jdbcAnnotationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_annotations);
        String uri = TestBackendConstants._TEST_SERVLET_URI_annotations+"00000000-0000-0000-0000-000000000021";
        Number result = jdbcAnnotationDao.getInternalIDFromURI(uri);
        assertEquals(1, result.intValue());
    }

    /**
     *
     * Test of getAnnotationWithoutTargetsAndAccesss method, of class JdbcAnnotationDao. Annotation
     * getAnnotation(Number annotationlID)
     */
    @Test
    public void getAnnotationWithoutTargetsAndAccesss() throws SQLException, DatatypeConfigurationException {
        System.out.println("test getAnnotationWithoutTargets");
        jdbcAnnotationDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_annotations);
        final Annotation result= jdbcAnnotationDao.getAnnotationWithoutTargetsAndAccesss(1);
        
        assertEquals("Sagrada Famiglia", result.getHeadline());
        assertEquals("<html><body>some html 1</body></html>", result.getBody().getTextBody().getBody()); 
        assertEquals("text/html", result.getBody().getTextBody().getMimeType()); 
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations+
                "00000000-0000-0000-0000-000000000021", result.getURI());
        assertEquals(DatatypeFactory.newInstance().newXMLGregorianCalendar("2013-08-12T09:25:00.383000Z"), result.getLastModified());
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
        jdbcAnnotationDao.deleteAllAnnotationTarget(4);
        jdbcAnnotationDao.deleteAnnotationPrincipalAccesss(4);
        
        assertEquals(1, jdbcAnnotationDao.deleteAnnotation(4));
        assertEquals(0, jdbcAnnotationDao.deleteAnnotation(4));
    }

    /**
     * Test of addAnnotation method, of class JdbcAnnotationDao.
     */
    @Test  
    public void testAddAnnotation() throws SQLException, Exception {
        System.out.println("test_addAnnotation ");

        final Annotation annotationToAdd = testInstances.getAnnotationToAdd();// existing Targets
        assertEquals(null, annotationToAdd.getURI());
        assertEquals(null, annotationToAdd.getLastModified());
        
        Number newAnnotationID = jdbcAnnotationDao.addAnnotation(annotationToAdd, 3);
        assertEquals(5, newAnnotationID);
        
        // checking
        Annotation addedAnnotation= jdbcAnnotationDao.getAnnotationWithoutTargetsAndAccesss(5);
        assertFalse(null == addedAnnotation.getURI());
        assertFalse(null == addedAnnotation.getLastModified());
        assertEquals(annotationToAdd.getBody().getTextBody().getMimeType(), addedAnnotation.getBody().getTextBody().getMimeType());
        assertEquals(annotationToAdd.getBody().getTextBody().getBody(), addedAnnotation.getBody().getTextBody().getBody()); 
        assertEquals(annotationToAdd.getHeadline(), addedAnnotation.getHeadline());
        System.out.println("creation time "+addedAnnotation.getLastModified());
    }

  

    /**
     * testing public List<Number> retrieveAnnotationList(List<Number> TargetIDs);
    
    **/
    @Test    
    public void testRetrieveAnnotationList() {
        System.out.println("test retrieveAnnotationlist");
        List<Number> targets = new ArrayList<Number>();
        targets.add(1);
        targets.add(2);
        List<Number> result = jdbcAnnotationDao.getAnnotationIDsForTargets(targets);
        assertEquals (2, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
    }
    
    //////////////////////////////////
    
    
    @Test    
    public void testGetExternalID() {
        System.out.println("getExternalID");

        final UUID externalId = jdbcAnnotationDao.getExternalID(1);
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000021"), externalId);
        
        final UUID externalIdThree = jdbcAnnotationDao.getExternalID(null);
        assertEquals(null, externalIdThree);

    }
    
    
    /** test
     * public List<Number> getFilteredAnnotationIDs(String link, String text, String access, String namespace, UUID owner, Timestamp after, Timestamp before) {
  **/
    
    @Test    
    public void testGetFilteredAnnotationIDs(){
        System.out.println(" test getFilteredAnnotationIDs");
       
        List<Number> result_1 = jdbcAnnotationDao.getFilteredAnnotationIDs(null, "some html", null, null, null);        
        assertEquals(3, result_1.size());
        assertEquals(1, result_1.get(0));
        assertEquals(2, result_1.get(1));
        assertEquals(4, result_1.get(2));
        
             
        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();
 
        List<Number> result_2 = jdbcAnnotationDao.getFilteredAnnotationIDs(1, "some html", null, after, before);        
        assertEquals(1, result_2.size());
        assertEquals(1, result_2.get(0));
        
        final String after_1 = (new Timestamp(System.currentTimeMillis())).toString();// no annotations added after "now"       
        List<Number> result_3 = jdbcAnnotationDao.getFilteredAnnotationIDs(4, "some html", null, after_1, null);        
        assertEquals(0, result_3.size());
        
        
    }
    
    //////////////////////////////////
   
  
    
    // public List<Map<Number, String>> retrieveAccesss(Number annotationId)
    
    @Test
    public void testRetrieveAccesss (){
        System.out.println("test Accesss");
        List<Map<Number, String>> result = jdbcAnnotationDao.getPermissions(1);
        assertEquals(3, result.size());
        assertEquals("write", result.get(0).get(2));
        assertEquals("read", result.get(1).get(3));
        assertEquals("read", result.get(2).get(11));
        
    }
}
