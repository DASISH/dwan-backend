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
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfos;
import eu.dasish.annotation.schema.ResourceREF;
import eu.dasish.annotation.schema.SourceInfo;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jmock.Expectations;
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
     * Test of getAnnotationInfos method, of class JdbcAnnotationDao.
     * List<AnnotationInfo> getAnnotationInfos(List<Number> annotationIDs)
     */
    
      /**
     * Test of retrieveSourceIDs method, of class JdbcSourceDao.
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

        final List<ResourceREF> testList = jdbcAnnotationDao.getAnnotationREFs(annotIds);
        assertEquals(3, testList.size());
        assertEquals(String.valueOf(2), testList.get(0).getRef());
        assertEquals(String.valueOf(3), testList.get(1).getRef());
        assertEquals(String.valueOf(4), testList.get(2).getRef());

        final List<ResourceREF> testListTwo = jdbcAnnotationDao.getAnnotationREFs(new ArrayList<Number>());
        assertEquals(0, testListTwo.size());

        final List<ResourceREF> testListThree = jdbcAnnotationDao.getAnnotationREFs(null);
        assertEquals(null, testListThree);

    }

    /**
     *
     * Test of getAnnotationID method, of class JdbcAnnotationDao. Integer
     * getAnnotationID(AnnotationIdentifier externalID)
     */
    @Test
    public void getInternalID() throws SQLException {
        System.out.println("test getInternalID");

        final Number annotaionId = jdbcAnnotationDao.getInternalID(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_2_EXT));
        assertEquals(2, annotaionId.intValue());

        final Number annotaionIdNE = jdbcAnnotationDao.getInternalID(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_7_EXT_NOT_IN_DB));
        assertEquals(null, annotaionIdNE);

        final Number annotaionIdNull = jdbcAnnotationDao.getInternalID(null);
        assertEquals(null, annotaionIdNull);
    }

    /**
     *
     * Test of getAnnotation method, of class JdbcAnnotationDao. Annotation
     * getAnnotation(Number annotationlID)
     */
    @Test
    public void getAnnotation() throws SQLException {
        System.out.println("test getAnnotation");

        /// dummy test
        final Annotation annotaionNull = jdbcAnnotationDao.getAnnotationWithoutSources(null);
        assertEquals(null, annotaionNull);
        ////

        final Number testAnnotationID = 2;
        final Annotation annotation = jdbcAnnotationDao.getAnnotationWithoutSources(testAnnotationID);
        assertEquals(TestBackendConstants._TEST_ANNOT_2_HEADLINE, annotation.getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_OWNER), annotation.getOwner().getRef());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_BODY, annotation.getBody().getAny().get(0)); // when the body is elaborated it may be changed
        assertEquals(TestBackendConstants._TEST_ANNOT_2_EXT, annotation.getURI());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_TIME_STAMP, annotation.getTimeStamp().toString());
    }

    /**
     * Test of deletAnnotation method, of class JdbcAnnotationDao.
     */
    @Test
    public void testDeleteAnnotation() throws SQLException {
        System.out.println("deleteAnnotation"); 
        // result[0] = # removed "annotations_principals_perissions" rows
        // result[1] = # removed "annotatiobs_target_sources" rows
        // result[2] = # removed annotation rows (should be 1)
        
        int[] result = jdbcAnnotationDao.deleteAnnotation(5);
        assertEquals(3, result[0]);
        assertEquals(2, result[1]);
        assertEquals(1, result[2]);
        
        // now, try to delete the same annotation one more time
        // if it has been already deleted then the method under testing should return 0
    
        result = jdbcAnnotationDao.deleteAnnotation(5);
        assertEquals(0, result[0]);
        assertEquals(0, result[1]);
        assertEquals(0, result[2]);
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
        
        Annotation addedAnnotation= jdbcAnnotationDao.getAnnotationWithoutSources(6);
        assertFalse(null == addedAnnotation.getURI());
        assertFalse(null == addedAnnotation.getTimeStamp());
        assertEquals(5, addedAnnotation.getOwner().getRef());
        assertEquals(annotationToAdd.getBody().getAny().get(0), addedAnnotation.getBody().getAny().get(0)); // TODO: to be changed after serialization is fixed
        assertEquals(annotationToAdd.getHeadline(), addedAnnotation.getHeadline());
    }

  


//        NewOrExistingSourceInfo noesi = new NewOrExistingSourceInfo();
//        NewSourceInfo nsi = new NewSourceInfo();
//        nsi.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
//        nsi.setId(TestBackendConstants._TEST_TEMP_SOURCE_ID);
//        nsi.setVersion(null);
//        noesi.setNewSource(nsi);
//
//
//        NewOrExistingSourceInfo noesiTwo = new NewOrExistingSourceInfo();
//        SourceInfo si = new SourceInfo();
//        si.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
//        si.setRef((new SourceIdentifier()).toString());
//        si.setVersion(null);
//        noesiTwo.setSource(si);
//
//        final Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo> map = new HashMap<NewOrExistingSourceInfo, NewOrExistingSourceInfo>();
//        map.put(noesi, noesiTwo);
//
//
//        mockery.checking(new Expectations() {
//            {
//                oneOf(sourceDao).addTargetSources(with(aNonNull(Number.class)), with(aNonNull(List.class)));
//                will(returnValue(map));
//            }
//        });

  
    /**
     * testing public List<Number> getAnnotationIDsForSources(List<Number> sourceIDs);
    
    **/
    @Test    
    public void testGetAnnotationIDsForSources() {
        System.out.println("test getAnnotationIDs for sources");
        List<Number> sources = new ArrayList<Number>();
        sources.add(1);
        sources.add(2);
        List<Number> result = jdbcAnnotationDao.retrieveAnnotationList(sources);
        assertEquals (2, result.size());
        assertEquals(2, result.get(0));
        assertEquals(3, result.get(1));
    }
    
    @Test    
    public void testGetExternalID() {
        System.out.println("getExternalID");

        final AnnotationIdentifier externalId = jdbcAnnotationDao.getExternalID(2);
        assertEquals(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_2_EXT), externalId);


        final AnnotationIdentifier externalIdThree = jdbcAnnotationDao.getExternalID(null);
        assertEquals(null, externalIdThree.getUUID());

    }
    
    
    /** test
     * public List<Number> getFilteredAnnotationIDs(String link, String text, String access, String namespace, UserIdentifier owner, Timestamp after, Timestamp before) {
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
        
        
       
        List<Number> result_3 = jdbcAnnotationDao.getFilteredAnnotationIDs(annotationIDs, "some html", null, null, 1, null, null);        
        assertEquals(1, result_3.size());
        assertEquals(2, result_3.get(0));
        
       
        Timestamp after = new Timestamp(0); 
        Timestamp before = new Timestamp(System.currentTimeMillis());  
        List<Number> result_4 = jdbcAnnotationDao.getFilteredAnnotationIDs(annotationIDs, "some html", null, null, 1, after, before);        
        assertEquals(1, result_4.size());
        assertEquals(2, result_4.get(0));
        
        
        Timestamp after_1 = new Timestamp(System.currentTimeMillis());        
        List<Number> result_5 = jdbcAnnotationDao.getFilteredAnnotationIDs(annotationIDs, "some html", null, null, 1, after_1, null);        
        assertEquals(0, result_5.size());
        
        
    }
    
     /**
     * Test of contructNewOrExistingSourceInfo method, of class JdbcSourceDao.
     */
    @Test
    public void testContructNewOrExistingSourceInfo() {
        System.out.println("contructNewOrExistingSourceInfo");

        List<SourceInfo> sourceInfoList = new ArrayList<SourceInfo>();

        SourceInfo sourceInfoOne = new SourceInfo();
        sourceInfoOne.setLink(TestBackendConstants._TEST_SOURCE_1_LINK);
        sourceInfoOne.setRef(TestBackendConstants._TEST_SOURCE_1_EXT_ID);
        sourceInfoOne.setRef(TestBackendConstants._TEST_VERSION_1_EXT_ID);

        SourceInfo sourceInfoTwo = new SourceInfo();
        sourceInfoTwo.setLink(TestBackendConstants._TEST_SOURCE_2_LINK);
        sourceInfoTwo.setRef(TestBackendConstants._TEST_SOURCE_2_EXT_ID);
        sourceInfoTwo.setRef(TestBackendConstants._TEST_VERSION_3_EXT_ID);

        sourceInfoList.add(sourceInfoOne);
        sourceInfoList.add(sourceInfoTwo);

//        NewOrExistingSourceInfos result = jdbcSourceDao.contructNewOrExistingSourceInfo(sourceInfoList);
//        assertEquals(2, result.getTarget().size());
//        assertEquals(sourceInfoOne, result.getTarget().get(0).getSource());
//        assertEquals(sourceInfoTwo, result.getTarget().get(1).getSource());

    }

//    /**
//     * Test of addTargetSources method, of class JdbcSourceDao. public
//     * Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo>
//     * addTargetSources(Number annotationID, List<NewOrExistingSourceInfo>
//     * sources)
//     */
//    @Test
//    public void testAddTargetSourcesOnExistingSource() {
//        System.out.println("addTargetSources : adding the old source");
//
//        NewOrExistingSourceInfo noesi = new NewOrExistingSourceInfo();
//        SourceInfo si = new SourceInfo();
//        si.setLink(TestBackendConstants._TEST_SOURCE_1_LINK);
//        si.setRef(TestBackendConstants._TEST_SOURCE_1_EXT_ID);
//        si.setVersion(TestBackendConstants._TEST_VERSION_1_EXT_ID);
//        noesi.setSource(si);
//
//        List<NewOrExistingSourceInfo> listnoesi = new ArrayList<NewOrExistingSourceInfo>();
//        listnoesi.add(noesi);
//
//        try {
//            Map<String, String> result = jdbcSourceDao.addTargetSources(5, listnoesi);
//            assertEquals(0, result.size()); // no new peristsent source IDs are produced
//        } catch (SQLException e) {
//            System.out.println(e);
//        }
//    }
//
//    /**
//     * Test of addTargetSources method, of class JdbcSourceDao. public
//     * Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo>
//     * addTargetSources(Number annotationID, List<NewOrExistingSourceInfo>
//     * sources)
//     */
//    @Test
//    public void testAddTargetSourcesOnNewSource() {
//        System.out.println("addTargetSources : adding the new source");
//
//        NewOrExistingSourceInfo noesi = new NewOrExistingSourceInfo();
//        NewSourceInfo nsi = new NewSourceInfo();
//        nsi.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
//        nsi.setId(TestBackendConstants._TEST_TEMP_SOURCE_ID);
//        nsi.setVersion(TestBackendConstants._TEST_VERSION_1_EXT_ID);
//        noesi.setNewSource(nsi);
//
//        List<NewOrExistingSourceInfo> listnoesiTwo = new ArrayList<NewOrExistingSourceInfo>();
//        listnoesiTwo.add(noesi);
//        
//        mockery.checking(new Expectations() {
//            {
//                oneOf(versionDao).getInternalID(new VersionIdentifier(TestBackendConstants._TEST_VERSION_1_EXT_ID));
//                will(returnValue(1));
//            }
//        });
//
//        try {
//            Map<String, String> result = jdbcSourceDao.addTargetSources(5, listnoesiTwo);
//            assertEquals(1, result.size());// a new identifier must be produced
//            SourceIdentifier sourceIdentifier = new SourceIdentifier(result.get(TestBackendConstants._TEST_TEMP_SOURCE_ID));
//            assertFalse(null == sourceIdentifier.getUUID()); // check if a proper uuid has been assigned 
//        } catch (SQLException e) {
//            System.out.print(e);
//        }
//
//    }
//    
}
