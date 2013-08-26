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
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.CachedRepresentationDao;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.SourceDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.backend.dao.VersionDao;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.NewSourceInfo;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.ResourceREF;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
import eu.dasish.annotation.schema.Version;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.datatype.XMLGregorianCalendar;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author olhsha
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-test-config/mockery.xml", "/spring-test-config/mockAnnotationDao.xml",
    "/spring-test-config/mockUserDao.xml", "/spring-test-config/mockNotebookDao.xml",
    "/spring-test-config/mockSourceDao.xml", "/spring-test-config/mockVersionDao.xml", "/spring-test-config/mockCachedRepresentationDao.xml",
    "/spring-config/dbIntegrityService.xml"})
public class DBIntegrityServiceTest {

    @Autowired
    private DBIntegrityServiceImlp dbIntegrityService;
    @Autowired
    private Mockery mockery;
    @Autowired
    private UserDao userDao;
    @Autowired
    private CachedRepresentationDao cachedRepresentationDao;
    @Autowired
    private VersionDao versionDao;
    @Autowired
    private SourceDao sourceDao;
    @Autowired
    private AnnotationDao annotationDao;
    @Autowired
    private NotebookDao notebookDao;
    TestInstances testInstances = new TestInstances();

    public DBIntegrityServiceTest() {
    }

    ///////// GETTERS /////////////
    /**
     * Test of getAnnotationInternalIdentifier method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testGetAnnotationInternalIdentifier() {
        System.out.println("getAnnotationInternalIdentifier");
        final UUID externalID = UUID.fromString(TestBackendConstants._TEST_ANNOT_2_EXT);

        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getInternalID(externalID);
                will(returnValue(2));
            }
        });
        assertEquals(2, dbIntegrityService.getAnnotationInternalIdentifier(externalID));
    }

    /**
     * Test of getAnnotationExternalIdentifier method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testGetAnnotationExternalIdentifier() {
        System.out.println("getAnnotationExternalIdentifier");
        final UUID externalID = UUID.fromString(TestBackendConstants._TEST_ANNOT_2_EXT);

        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getExternalID(2);
                will(returnValue(externalID));
            }
        });
        assertEquals(TestBackendConstants._TEST_ANNOT_2_EXT, dbIntegrityService.getAnnotationExternalIdentifier(2).toString());
    }

    /**
     * Test of getUserInternalIdentifier method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testGetUserInternalIdentifier() {
        System.out.println("getUserInternalIdentifier");

        final UUID externalID = UUID.fromString(TestBackendConstants._TEST_USER_5_EXT_ID);

        mockery.checking(new Expectations() {
            {
                oneOf(userDao).getInternalID(externalID);
                will(returnValue(5));
            }
        });
        assertEquals(5, dbIntegrityService.getUserInternalIdentifier(externalID));
    }

    /**
     * Test of getUserExternalIdentifier method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testGetUserExternalIdentifier() {
        System.out.println("getUserExternalIdentifier");
        final UUID externalID = UUID.fromString(TestBackendConstants._TEST_USER_5_EXT_ID);

        mockery.checking(new Expectations() {
            {
                oneOf(userDao).getExternalID(5);
                will(returnValue(externalID));
            }
        });
        assertEquals(TestBackendConstants._TEST_USER_5_EXT_ID, dbIntegrityService.getUserExternalIdentifier(5).toString());
    }

    /**
     * Test of getAnnotation method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testGetAnnotation() throws Exception {
        System.out.println("test getAnnotation");

        final Annotation mockAnnotation = new Annotation();// corresponds to the annotation # 2
        mockAnnotation.setURI(TestBackendConstants._TEST_ANNOT_2_EXT);
        mockAnnotation.setHeadline(TestBackendConstants._TEST_ANNOT_2_HEADLINE);
        XMLGregorianCalendar mockTimeStamp = Helpers.setXMLGregorianCalendar(Timestamp.valueOf("2013-08-12 11:25:00.383000"));
        mockAnnotation.setTimeStamp(mockTimeStamp);
        ResourceREF mockOwner = new ResourceREF();
        mockOwner.setRef(Integer.toString(TestBackendConstants._TEST_ANNOT_2_OWNER));
        mockAnnotation.setOwner(mockOwner);
        AnnotationBody mockBody = new AnnotationBody();
        mockBody.getAny().add(TestBackendConstants._TEST_ANNOT_2_BODY); // TODO: will be changed after body serialization is fixed 
        mockAnnotation.setBody(mockBody);
        mockAnnotation.setTargetSources(null);


        final List<Number> mockSourceIDs = new ArrayList<Number>();
        mockSourceIDs.add(1);
        mockSourceIDs.add(2);

        final Source mockSourceOne = new Source();
        mockSourceOne.setLink(TestBackendConstants._TEST_SOURCE_1_LINK);
        mockSourceOne.setURI(TestBackendConstants._TEST_SOURCE_1_EXT_ID);
        mockSourceOne.setVersion(TestBackendConstants._TEST_SOURCE_1_EXT_ID);

        final Source mockSourceTwo = new Source();
        mockSourceTwo.setLink(TestBackendConstants._TEST_SOURCE_2_LINK);
        mockSourceTwo.setURI(TestBackendConstants._TEST_SOURCE_2_EXT_ID);
        mockSourceTwo.setVersion(TestBackendConstants._TEST_SOURCE_2_EXT_ID);


        // NOT CHECKED: time stamp for sources

        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getAnnotationWithoutSources(1);
                will(returnValue(mockAnnotation));

                oneOf(annotationDao).retrieveSourceIDs(1);
                will(returnValue(mockSourceIDs));

                oneOf(sourceDao).getSource(1);
                will(returnValue(mockSourceOne));

                oneOf(sourceDao).getSource(2);
                will(returnValue(mockSourceTwo));

            }
        });

        Annotation result = dbIntegrityService.getAnnotation(1);
        assertEquals(TestBackendConstants._TEST_ANNOT_2_EXT, result.getURI());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_BODY, result.getBody().getAny().get(0));
        assertEquals(TestBackendConstants._TEST_ANNOT_2_HEADLINE, result.getHeadline());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_TIME_STAMP, result.getTimeStamp().toString());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_OWNER, Integer.parseInt(result.getOwner().getRef()));
        assertEquals(mockSourceOne.getLink(), result.getTargetSources().getTarget().get(0).getSource().getLink());
        assertEquals(mockSourceOne.getURI(), result.getTargetSources().getTarget().get(0).getSource().getRef());
        assertEquals(mockSourceOne.getVersion(), result.getTargetSources().getTarget().get(0).getSource().getVersion());
        assertEquals(mockSourceTwo.getLink(), result.getTargetSources().getTarget().get(1).getSource().getLink());
        assertEquals(mockSourceTwo.getURI(), result.getTargetSources().getTarget().get(1).getSource().getRef());
        assertEquals(mockSourceTwo.getVersion(), result.getTargetSources().getTarget().get(1).getSource().getVersion());
    }

    /**
     * Test of getFilteredAnnotationIDs method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testGetFilteredAnnotationIDs() {
        System.out.println("test getFilteredAnnotationIDs");

        final String link = "nl.wikipedia.org";

        final List<Number> mockSourceIDs = new ArrayList<Number>();
        mockSourceIDs.add(1);
        mockSourceIDs.add(2);

        final List<Number> mockAnnotationIDs = new ArrayList<Number>();
        mockAnnotationIDs.add(2);
        mockAnnotationIDs.add(3);

        final String text = "some html";
        final String access = null;
        final String namespace = null;
        final UUID owner = UUID.fromString(TestBackendConstants._TEST_USER_3_EXT_ID);
        final Timestamp after = new Timestamp(0);
        final Timestamp before = new Timestamp(System.currentTimeMillis());

        final List<Number> mockRetval = new ArrayList<Number>();
        mockRetval.add(2);

        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).getSourcesForLink(link);
                will(returnValue(mockSourceIDs));

                oneOf(annotationDao).retrieveAnnotationList(mockSourceIDs);
                will(returnValue(mockAnnotationIDs));

                oneOf(userDao).getInternalID(owner);
                will(returnValue(3));

                oneOf(annotationDao).getFilteredAnnotationIDs(mockAnnotationIDs, text, access, namespace, 3, after, before);
                will(returnValue(mockRetval));

            }
        });


        List result = dbIntegrityService.getFilteredAnnotationIDs(link, text, access, namespace, owner, after, before);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0));
    }

    ////////////// ADDERS /////////////////////////
    /**
     * Test of addCachedForVersion method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testAddCachedForVersion() {
        System.out.println("addCachedForVersion");
        String mime = "text/html";
        String type = "text";
        String tool = "latex";
        final CachedRepresentationInfo newCached = new CachedRepresentationInfo();
        newCached.setMimeType(mime);
        newCached.setType(type);
        newCached.setTool(tool);
        final Number newCachedID = 8;
        final Number versionID = 1;
        mockery.checking(new Expectations() {
            {
                oneOf(cachedRepresentationDao).getInternalID(null);
                will(returnValue(null));

                oneOf(cachedRepresentationDao).addCachedRepresentationInfo(newCached);
                will(returnValue(newCachedID));

                one(versionDao).addVersionCachedRepresentation(versionID, newCachedID);
                will(returnValue(1));

            }
        });


        Number[] result = dbIntegrityService.addCachedForVersion(versionID, newCached);
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(newCachedID, result[1]);
    }

    /**
     * Test of addSiblingVersionForSource method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testAddSiblingVersionForSource() throws Exception {
        System.out.println("test addSiblingVersionForSource ");

        // test adding completely new version 
        final Version mockVersion = new Version(); // should be # 8
        final UUID mockUUID = UUID.randomUUID();
        mockVersion.setVersion(mockUUID.toString());

        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).getInternalID(mockUUID);
                will(returnValue(null));

                oneOf(versionDao).addVersion(mockVersion);
                will(returnValue(8));

                oneOf(sourceDao).addSourceVersion(1, 8);
                will(returnValue(1));

            }
        });

        Number[] result = dbIntegrityService.addSiblingVersionForSource(1, mockVersion);
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(8, result[1]);


        // Another test: connecting the existing version to the source

        final Version mockVersionTwo = new Version(); // should be # 3
        final UUID mockUUIDTwo = UUID.fromString(TestBackendConstants._TEST_VERSION_3_EXT_ID);
        mockVersionTwo.setVersion(mockUUIDTwo.toString());

        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).getInternalID(mockUUIDTwo);
                will(returnValue(3));

                oneOf(sourceDao).addSourceVersion(1, 3);
                will(returnValue(1));
            }
        });
        Number[] resultTwo = dbIntegrityService.addSiblingVersionForSource(1, mockVersionTwo);
        assertEquals(2, resultTwo.length);
        assertEquals(1, resultTwo[0]);
        assertEquals(3, resultTwo[1]);
    }

    /**
     * Test of addSourcesForAnnotation method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testAddSourcesForAnnotation() throws Exception {
        System.out.println("test addSourcesForAnnotation");

        // test 1: adding an existing source
        NewOrExistingSourceInfo testSourceOne = new NewOrExistingSourceInfo();
        final UUID mockUUIDOne = UUID.fromString(TestBackendConstants._TEST_SOURCE_1_EXT_ID);
        SourceInfo testOldSource = new SourceInfo();
        testOldSource.setLink(TestBackendConstants._TEST_SOURCE_1_LINK);
        testOldSource.setRef(TestBackendConstants._TEST_SOURCE_1_EXT_ID);
        testOldSource.setVersion(TestBackendConstants._TEST_VERSION_1_EXT_ID);
        testSourceOne.setSource(testOldSource);
        List<NewOrExistingSourceInfo> mockSourceSOne = new ArrayList<NewOrExistingSourceInfo>();
        mockSourceSOne.add(testSourceOne);

        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).getInternalID(with(aNonNull(UUID.class)));
                will(returnValue(1));

                oneOf(annotationDao).addAnnotationSourcePair(1, 1);
                will(returnValue(1));
            }
        });

        Map<String, String> result = dbIntegrityService.addSourcesForAnnotation(1, mockSourceSOne);
        assertEquals(0, result.size());

        // test 2: adding a new source

        NewOrExistingSourceInfo testSourceTwo = new NewOrExistingSourceInfo();
        NewSourceInfo testNewSource = new NewSourceInfo();
        testNewSource.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
        testNewSource.setVersion(null);
        testSourceTwo.setNewSource(testNewSource);
        List<NewOrExistingSourceInfo> mockSourceSTwo = new ArrayList<NewOrExistingSourceInfo>();
        mockSourceSTwo.add(testSourceTwo);

//        final Source mockNewSource = dbIntegrityService.createSource(testNewSource);
//        final Version mockNewVersion = dbIntegrityService.createVersion(testNewSource);

        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).addSource(with(aNonNull(Source.class)));
                will(returnValue(6)); //# the next new number is 6, we have already 5 sources

                ////////////  mockery in the call addSiblingVersionForSource //////////////

                oneOf(versionDao).getInternalID(with(aNonNull(UUID.class)));
                will(returnValue(null));

                oneOf(versionDao).addVersion(with(aNonNull(Version.class)));
                will(returnValue(8)); // next free nummber

                oneOf(sourceDao).addSourceVersion(6, 8);
                will(returnValue(1));

                //////////////////////////////////////

                oneOf(annotationDao).addAnnotationSourcePair(1, 6);
                will(returnValue(1));

            }
        });

        Map<String, String> resultTwo = dbIntegrityService.addSourcesForAnnotation(1, mockSourceSTwo);
        assertEquals(1, resultTwo.size());
        assertFalse(null == resultTwo.get(testNewSource.getId()));

    }

    /**
     * Test of addUsersAnnotation method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testAddUsersAnnotation() throws Exception {
        System.out.println("test addUsersAnnotation");

        // expectations for addUsersannotation itself
        final Annotation testAnnotation = testInstances.getAnnotationToAdd();

        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).addAnnotation(testAnnotation, 5);
                will(returnValue(6)); // the next free number is 6

                //  expectations for addSourcesForannotation
                oneOf(sourceDao).getInternalID(with(aNonNull(UUID.class)));
                will(returnValue(1));

                oneOf(annotationDao).addAnnotationSourcePair(6, 1);
                will(returnValue(1)); // the source is old, no new persistent identifier were produced

                ///////////

                oneOf(annotationDao).updateBody(6, testAnnotation.getBody().getAny().get(0).toString());// will be changed after serialization is fixed
                will(returnValue(1)); // the DB update will be called at perform anyway, even if the body is not changed (can be optimized)

                oneOf(annotationDao).addAnnotationPrincipalPermission(6, 5, Permission.OWNER);
                will(returnValue(1));
            }
        });

        Number result = dbIntegrityService.addUsersAnnotation(testAnnotation, 5);
        assertEquals(6, result);
    }

    //////////////////// DELETERS ////////////////
    /**
     * Test of deleteCachedForVersion method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testDeleteCachedForVersion() {
        System.out.println("deleteCachedForVersion");
        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).deleteVersionCachedRepresentation(6, 5);
                will(returnValue(1));

                oneOf(cachedRepresentationDao).deleteCachedRepresentationInfo(5);
                will(returnValue(0)); // cached is used by another version

            }
        });

        int[] result = dbIntegrityService.deleteCachedOfVersion(6, 5);
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(0, result[1]);
    }

    /////////////////////////////////////////////
    @Test
    public void testDeleteAalCachedOfVersion() {
        System.out.println("deleteVersion");
        final List<Number> cachedList = new ArrayList<Number>();
        cachedList.add(5);

        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).versionIsInUse(6);
                will(returnValue(false));

                oneOf(versionDao).retrieveCachedRepresentationList(6);
                will(returnValue(cachedList));

                oneOf(versionDao).deleteAllVersionCachedRepresentation(6);
                will(returnValue(1));

                oneOf(versionDao).deleteVersion(6);
                will(returnValue(1));

                oneOf(cachedRepresentationDao).deleteCachedRepresentationInfo(5);
                will(returnValue(0)); // cached is used by another version
            }
        });

        int[] result = dbIntegrityService.deleteAllCachedOfVersion(6);
        assertEquals(1, result[0]); //version
        assertEquals(1, result[1]); // versions-cached
        assertEquals(0, result[2]);//cached 5 is in use

        //Another test


        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).versionIsInUse(5);
                will(returnValue(true));
            }
        });


        int[] resultTwo = dbIntegrityService.deleteAllCachedOfVersion(5); // version is in use by the source 4
        assertEquals(0, resultTwo[0]);
        assertEquals(0, resultTwo[1]);
        assertEquals(0, resultTwo[2]);

    }

    @Test
    public void testDeleteAllVersionsOfSource() throws SQLException {
        // test 1: source in use by some annotation
        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).sourceIsInUse(1);
                will(returnValue(true));
            }
        });

        int[] result = dbIntegrityService.deleteAllVersionsOfSource(1); //the source is in use, should not be deleted
        assertEquals(0, result[0]); // 
        assertEquals(0, result[1]);

        // test 2 : source is not used by annotations     
        final List<Number> versionList = new ArrayList<Number>();
        versionList.add(7);
        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).sourceIsInUse(5);
                will(returnValue(false));

                oneOf(sourceDao).retrieveVersionList(5);
                will(returnValue(versionList));

                oneOf(sourceDao).deleteAllSourceVersion(5);
                will(returnValue(1));

                oneOf(sourceDao).deleteSource(5);
                will(returnValue(1));

                oneOf(versionDao).versionIsInUse(7);
                will(returnValue(false)); //not mentioned in the table "sources_versions" after deleteing(5,7)

                oneOf(versionDao).retrieveCachedRepresentationList(7);
                will(returnValue(new ArrayList<Number>())); // no cached representations for version 7, the list is just empty

                oneOf(versionDao).deleteAllVersionCachedRepresentation(7);

                oneOf(versionDao).deleteVersion(7);
                will(returnValue(1));
            }
        });


        int[] resultTwo = dbIntegrityService.deleteAllVersionsOfSource(5);// the source will be deleted because it is not referred by any annotation
        assertEquals(3, resultTwo.length);
        assertEquals(1, resultTwo[0]); // source 7 is deleted
        assertEquals(1, resultTwo[1]); // row (5,7) in "sorces_versions" is deleted
        assertEquals(1, resultTwo[2]); // version 7 is deleted
    }

    /**
     * Test of deleteAnnotationWithSources method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testDeleteAnnotation() throws Exception {
        System.out.println("deleteAnnotation ");

        // deleting annotation 3, which has its target source 2  (used by annotation # 1)
        final List<Number> mockSourceIDs = new ArrayList<Number>();
        mockSourceIDs.add(2);

        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).deleteAnnotationPrincipalPermissions(3);
                will(returnValue(3));

                oneOf(annotationDao).retrieveSourceIDs(3);
                will(returnValue(mockSourceIDs));

                oneOf(annotationDao).deleteAllAnnotationSource(3);
                will(returnValue(1));

                oneOf(annotationDao).deleteAnnotation(3);
                will(returnValue(1));

                oneOf(sourceDao).sourceIsInUse(2); // expectation for deleteAllVersionsOfSource
                will(returnValue(true));
            }
        });
        int[] result = dbIntegrityService.deleteAnnotation(3);// the source will be deleted because it is not referred by any annotation
        assertEquals(4, result.length);
        assertEquals(1, result[0]); // annotation 3 is deleted
        assertEquals(3, result[1]); // 3 rows in "annotation proncipal permissions are deleted"
        assertEquals(1, result[2]);  // row (3,2) in "annotations_sources" is deleted
        assertEquals(0, result[3]); //the source 2 is not deleted because it is still in use by anntation 2
    }
    
    
//    @Test
//    public void testCreateSource(){  
//        NewSourceInfo newSourceInfo = new NewSourceInfo();
//        newSourceInfo.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
//        newSourceInfo.setVersion(null);
//        
//        Source result = dbIntegrityService.createSource(newSourceInfo);
//        assertEquals(TestBackendConstants._TEST_NEW_SOURCE_LINK, result.getLink());
//        assertFalse(null == result.getURI());
//        
//    }
//    
//    @Test
//    public void testCreateVersion(){  
//        NewSourceInfo newSourceInfo = new NewSourceInfo();
//        newSourceInfo.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
//        newSourceInfo.setVersion(null);
//        
//        Version result = dbIntegrityService.createVersion(newSourceInfo);
//        assertFalse(null == result.getVersion()); // will be chnaged once the schema for version is fixed: ID is added
//        
//    }
}


