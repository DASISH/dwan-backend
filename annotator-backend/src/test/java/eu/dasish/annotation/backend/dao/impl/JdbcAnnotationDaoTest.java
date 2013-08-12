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
import eu.dasish.annotation.backend.dao.SourceDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.SourceIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfos;
import eu.dasish.annotation.schema.NewSourceInfo;
import eu.dasish.annotation.schema.ResourceREF;
import eu.dasish.annotation.schema.SourceInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jmock.Expectations;
import org.jmock.Mockery;
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
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-test-config/mockery.xml", "/spring-test-config/mockNotebookDao.xml",
    "/spring-test-config/mockUserDao.xml", "/spring-test-config/mockPermissionsDao.xml", "/spring-test-config/mockSourceDao.xml", "/spring-config/annotationDao.xml"})
public class JdbcAnnotationDaoTest extends JdbcResourceDaoTest {

    @Autowired
    JdbcAnnotationDao jdbcAnnotationDao;
    @Autowired
    private PermissionsDao permissionsDao;
    @Autowired
    private NotebookDao notebookDao;
    @Autowired
    private SourceDao sourceDao;
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
        final Annotation annotaionNull = jdbcAnnotationDao.getAnnotation(null);
        assertEquals(null, annotaionNull);
        ////

        final Number testAnnotationID = 2;


        SourceInfo sourceOneInfo = new SourceInfo();
        sourceOneInfo.setLink(TestBackendConstants._TEST_SOURCE_1_LINK);
        sourceOneInfo.setRef(TestBackendConstants._TEST_SOURCE_1_EXT_ID);
        sourceOneInfo.setVersion(Integer.toString(TestBackendConstants._TEST_SOURCE_1_VERSION_ID));

        SourceInfo sourceTwoInfo = new SourceInfo();
        sourceTwoInfo.setLink(TestBackendConstants._TEST_SOURCE_2_LINK);
        sourceTwoInfo.setRef(TestBackendConstants._TEST_SOURCE_2_EXT_ID);
        sourceTwoInfo.setVersion(Integer.toString(TestBackendConstants._TEST_SOURCE_2_VERSION_ID));

        final List<SourceInfo> sourceInfoList = new ArrayList<SourceInfo>();
        sourceInfoList.add(sourceOneInfo);
        sourceInfoList.add(sourceTwoInfo);

        NewOrExistingSourceInfo noeSourceOneInfo = new NewOrExistingSourceInfo();
        noeSourceOneInfo.setSource(sourceOneInfo);
        NewOrExistingSourceInfo noeSourceTwoInfo = new NewOrExistingSourceInfo();
        noeSourceTwoInfo.setSource(sourceTwoInfo);

        List<NewOrExistingSourceInfo> noeSourceInfoList = new ArrayList<NewOrExistingSourceInfo>();
        noeSourceInfoList.add(noeSourceOneInfo);
        noeSourceInfoList.add(noeSourceTwoInfo);
        final NewOrExistingSourceInfos noeSourceInfos = new NewOrExistingSourceInfos();
        noeSourceInfos.getTarget().addAll(noeSourceInfoList);

        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).getSourceInfos(testAnnotationID);
                will(returnValue(sourceInfoList));

                oneOf(sourceDao).contructNewOrExistingSourceInfo(sourceInfoList);
                will(returnValue(noeSourceInfos));
            }
        });


        final Annotation annotation = jdbcAnnotationDao.getAnnotation(testAnnotationID);
        assertEquals(TestBackendConstants._TEST_ANNOT_2_HEADLINE, annotation.getHeadline());
        assertEquals(String.valueOf(TestBackendConstants._TEST_ANNOT_2_OWNER), annotation.getOwner().getRef());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_BODY, annotation.getBody().getAny().get(0)); // when the body is elaborated it may be changed

        assertEquals(sourceOneInfo.getRef(), annotation.getTargetSources().getTarget().get(0).getSource().getRef());
        assertEquals(sourceOneInfo.getLink(), annotation.getTargetSources().getTarget().get(0).getSource().getLink());
        assertEquals(sourceOneInfo.getVersion(), annotation.getTargetSources().getTarget().get(0).getSource().getVersion());

        assertEquals(sourceTwoInfo.getRef(), annotation.getTargetSources().getTarget().get(1).getSource().getRef());
        assertEquals(sourceTwoInfo.getLink(), annotation.getTargetSources().getTarget().get(1).getSource().getLink());
        assertEquals(sourceTwoInfo.getVersion(), annotation.getTargetSources().getTarget().get(1).getSource().getVersion());

        assertEquals(TestBackendConstants._TEST_ANNOT_2_EXT, annotation.getURI());

        assertEquals(TestBackendConstants._TEST_ANNOT_2_TIME_STAMP, annotation.getTimeStamp().toString());


    }

    /**
     * Test of deletAnnotation method, of class JdbcAnnotationDao.
     */
    @Test
    public void testDeleteAnnotation() throws SQLException {
        System.out.println("deleteAnnotation");
        final List<Number> sourceIDs = new ArrayList<Number>();
        sourceIDs.add(3);
        sourceIDs.add(4);

        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).retrieveSourceIDs(5);
                will(returnValue(sourceIDs));
                
                oneOf(sourceDao).deleteSource(sourceIDs.get(0));
                will(returnValue(0));
                
                oneOf(sourceDao).deleteSource(sourceIDs.get(1));
                will(returnValue(1));
            }
        });

        int result = jdbcAnnotationDao.deleteAnnotation(5);
        assertEquals(1, result);
        // now, try to delete the same annotation one more time
        // if it has been already deleted then the method under testing should return 0
        
        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).retrieveSourceIDs(5);
                will(returnValue(new ArrayList<Number>()));                
            }
        });
        result = jdbcAnnotationDao.deleteAnnotation(5);
        assertEquals(0, result);
    }

    /**
     * Test of addAnnotation method, of class JdbcAnnotationDao.
     */
    @Test
    public void testAddAnnotationExistingSource() throws SQLException {
        System.out.println("test_addAnnotation with an existing source");

        Annotation annotationToAdd = testInstances.getAnnotationToAdd();// existing sources
        assertEquals(null, annotationToAdd.getURI());
        assertEquals(null, annotationToAdd.getTimeStamp());

        NewOrExistingSourceInfo noesi = new NewOrExistingSourceInfo();
        SourceInfo si = new SourceInfo();
        si.setLink(TestBackendConstants._TEST_SOURCE_1_LINK);
        si.setRef(TestBackendConstants._TEST_SOURCE_1_EXT_ID);
        si.setVersion(TestBackendConstants._TEST_VERSION_1_EXT_ID);
        noesi.setSource(si);

        final Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo> map = new HashMap<NewOrExistingSourceInfo, NewOrExistingSourceInfo>();
        map.put(noesi, noesi);


        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).addTargetSources(with(aNonNull(Number.class)), with(aNonNull(List.class)));
                will(returnValue(map));
            }
        });

        Annotation result = jdbcAnnotationDao.addAnnotation(annotationToAdd, 5);
        assertFalse(null == result.getURI());
        assertFalse(null == result.getTimeStamp());
        assertEquals(annotationToAdd.getBody().getAny().get(0), result.getBody().getAny().get(0));
        assertEquals(annotationToAdd.getHeadline(), result.getHeadline());
        assertEquals(String.valueOf(5), result.getOwner().getRef());
        assertEquals(annotationToAdd.getPermissions(), result.getPermissions());

        SourceInfo expectedSi = annotationToAdd.getTargetSources().getTarget().get(0).getSource();
        SourceInfo resultSi = result.getTargetSources().getTarget().get(0).getSource();
        assertEquals(expectedSi.getLink(), resultSi.getLink());
        assertEquals(expectedSi.getRef(), resultSi.getRef());
        assertEquals(expectedSi.getVersion(), resultSi.getVersion());
    }

    /**
     * Test of addAnnotation method, of class JdbcAnnotationDao.
     */
    @Test
    public void testAddAnnotationNewSource() throws SQLException {
        System.out.println("test_addAnnotation with a new source");


        Annotation annotationToAdd = testInstances.getAnnotationToAddNewSource();// existing sources
        assertEquals(null, annotationToAdd.getURI());
        assertEquals(null, annotationToAdd.getTimeStamp());


        NewOrExistingSourceInfo noesi = new NewOrExistingSourceInfo();
        NewSourceInfo nsi = new NewSourceInfo();
        nsi.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
        nsi.setId(TestBackendConstants._TEST_TEMP_SOURCE_ID);
        nsi.setVersion(null);
        noesi.setNewSource(nsi);


        NewOrExistingSourceInfo noesiTwo = new NewOrExistingSourceInfo();
        SourceInfo si = new SourceInfo();
        si.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
        si.setRef((new SourceIdentifier()).toString());
        si.setVersion(null);
        noesiTwo.setSource(si);

        final Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo> map = new HashMap<NewOrExistingSourceInfo, NewOrExistingSourceInfo>();
        map.put(noesi, noesiTwo);


        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).addTargetSources(with(aNonNull(Number.class)), with(aNonNull(List.class)));
                will(returnValue(map));
            }
        });

        Annotation result = jdbcAnnotationDao.addAnnotation(annotationToAdd, 5);
        assertFalse(null == result.getURI());
        assertFalse(null == result.getTimeStamp());
        assertEquals(annotationToAdd.getHeadline(), result.getHeadline());
        assertEquals(String.valueOf(5), result.getOwner().getRef());
        assertEquals(annotationToAdd.getPermissions(), result.getPermissions());

        NewSourceInfo expectedSi = annotationToAdd.getTargetSources().getTarget().get(0).getNewSource();
        SourceInfo resultSi = result.getTargetSources().getTarget().get(0).getSource();
        assertEquals(expectedSi.getLink(), resultSi.getLink());
        assertEquals(expectedSi.getVersion(), resultSi.getVersion());
        //the reference is replaced with the persistent one
        assertEquals(si.getRef(), resultSi.getRef());
        /////  

        // checking the bodies: the temporary reference should be replaced
        String expBody = annotationToAdd.getBody().getAny().get(0).toString().replaceAll(TestBackendConstants._TEST_TEMP_SOURCE_ID, si.getRef());
        assertEquals(expBody, result.getBody().getAny().get(0).toString());
    }

    @Test
    @Ignore
    public void testGetExternalID() {
        System.out.println("getAnnotationID");

        final AnnotationIdentifier externalId = jdbcAnnotationDao.getExternalID(2);
        assertEquals(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_2_EXT), externalId);


        final AnnotationIdentifier externalIdThree = jdbcAnnotationDao.getExternalID(null);
        assertEquals(null, externalIdThree.getUUID());

    }
    //////////// helpers //////////////////////
}
