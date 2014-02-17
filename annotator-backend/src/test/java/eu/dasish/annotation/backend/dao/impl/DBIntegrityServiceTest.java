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
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.CachedRepresentationDao;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.TargetDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationBody.TextBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.NotebookInfoList;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.Target;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.User;
import eu.dasish.annotation.schema.UserWithPermission;
import eu.dasish.annotation.schema.UserWithPermissionList;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.rowset.serial.SerialException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
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
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-test-config/mockeryDao.xml", "/spring-test-config/mockAnnotationDao.xml",
    "/spring-test-config/mockUserDao.xml", "/spring-test-config/mockTargetDao.xml", "/spring-test-config/mockCachedRepresentationDao.xml",
    "/spring-test-config/mockNotebookDao.xml",
    "/spring-config/dbIntegrityService.xml"})
public class DBIntegrityServiceTest {

    @Autowired
    private DBIntegrityServiceImlp dbIntegrityService;
    @Autowired
    private Mockery mockeryDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private CachedRepresentationDao cachedRepresentationDao;
    @Autowired
    private TargetDao targetDao;
    @Autowired
    private AnnotationDao annotationDao;
    @Autowired
    private NotebookDao notebookDao;
    TestInstances testInstances = new TestInstances(TestBackendConstants._TEST_SERVLET_URI);

    public DBIntegrityServiceTest() {
    }

    ///////// GETTERS /////////////
    /**
     * Test of getAnnotationInternalIdentifier method, of class
     * DBIntegrityServiceImlp.
     */
    @Test
    public void testGetAnnotationInternalIdentifier() {
        System.out.println("getAnnotationInternalIdentifier");
        final UUID externalID = UUID.fromString("00000000-0000-0000-0000-000000000021");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getInternalID(externalID);
                will(returnValue(1));
            }
        });
        assertEquals(1, dbIntegrityService.getAnnotationInternalIdentifier(externalID));
    }

    /**
     * Test of getAnnotationExternalIdentifier method, of class
     * DBIntegrityServiceImlp.
     */
    @Test
    public void testGetAnnotationExternalIdentifier() {
        System.out.println("getAnnotationExternalIdentifier");
        final UUID externalID = UUID.fromString("00000000-0000-0000-0000-000000000021");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getExternalID(1);
                will(returnValue(externalID));
            }
        });
        assertEquals("00000000-0000-0000-0000-000000000021", dbIntegrityService.getAnnotationExternalIdentifier(1).toString());
    }

    /**
     * Test of getUserInternalIdentifier method, of class
     * DBIntegrityServiceImlp.
     */
    @Test
    public void testGetUserInternalIdentifier() {
        System.out.println("getUserInternalIdentifier");

        final UUID externalID = UUID.fromString("00000000-0000-0000-0000-000000000111");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(userDao).getInternalID(externalID);
                will(returnValue(1));
            }
        });
        assertEquals(1, dbIntegrityService.getUserInternalIdentifier(externalID));
    }

    /**
     * Test of getUserExternalIdentifier method, of class
     * DBIntegrityServiceImlp.
     */
    @Test
    public void testGetUserExternalIdentifier() {
        System.out.println("getUserExternalIdentifier");
        final UUID externalID = UUID.fromString("00000000-0000-0000-0000-000000000111");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(userDao).getExternalID(1);
                will(returnValue(externalID));
            }
        });
        assertEquals("00000000-0000-0000-0000-000000000111", dbIntegrityService.getUserExternalIdentifier(1).toString());
    }

    /**
     * Test of getAnnotation method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testGetAnnotation() throws Exception {
        System.out.println("test getAnnotation");

        final Annotation mockAnnotation = new Annotation();// corresponds to the annotation # 1
        mockAnnotation.setURI(TestBackendConstants._TEST_SERVLET_URI_annotations + "00000000-0000-0000-0000-000000000021");
        mockAnnotation.setHeadline("Sagrada Famiglia");
        XMLGregorianCalendar mockTimeStamp = DatatypeFactory.newInstance().newXMLGregorianCalendar("2013-08-12T09:25:00.383000Z");
        mockAnnotation.setLastModified(mockTimeStamp);
        mockAnnotation.setOwnerRef(TestBackendConstants._TEST_SERVLET_URI_annotations + "00000000-0000-0000-0000-000000000111");

        AnnotationBody mockBody = new AnnotationBody();
        TextBody textBody = new AnnotationBody.TextBody();
        mockBody.setTextBody(textBody);
        textBody.setMimeType("text/plain");
        textBody.setBody("<html><body>some html 1</body></html>");
        mockAnnotation.setBody(mockBody);
        mockAnnotation.setTargets(null);


        final List<Number> mockTargetIDs = new ArrayList<Number>();
        mockTargetIDs.add(1);
        mockTargetIDs.add(2);

        final Target mockTargetOne = new Target();
        mockTargetOne.setLink("http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia");
        mockTargetOne.setURI(TestBackendConstants._TEST_SERVLET_URI_Targets + "00000000-0000-0000-0000-000000000031");
        mockTargetOne.setVersion("version 1.0");

        final Target mockTargetTwo = new Target();
        mockTargetTwo.setLink("http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD");
        mockTargetTwo.setURI(TestBackendConstants._TEST_SERVLET_URI_Targets + "00000000-0000-0000-0000-000000000032");
        mockTargetTwo.setVersion("version 1.1");

        final List<Map<Number, String>> listMap = new ArrayList<Map<Number, String>>();
        Map<Number, String> map2 = new HashMap<Number, String>();
        map2.put(2, "writer");
        listMap.add(map2);
        Map<Number, String> map3 = new HashMap<Number, String>();
        map3.put(3, "reader");
        listMap.add(map3);

        final String uri1 = TestBackendConstants._TEST_SERVLET_URI_users + "00000000-0000-0000-0000-000000000111";
        final String uri2 = TestBackendConstants._TEST_SERVLET_URI_users + "00000000-0000-0000-0000-000000000112";
        final String uri3 = TestBackendConstants._TEST_SERVLET_URI_users + "00000000-0000-0000-0000-000000000113";


        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getAnnotationWithoutTargetsAndPermissions(1);
                will(returnValue(mockAnnotation));

                oneOf(annotationDao).getOwner(1);
                will(returnValue(1));

                oneOf(userDao).getURIFromInternalID(1);
                will(returnValue(uri1));

                oneOf(annotationDao).retrieveTargetIDs(1);
                will(returnValue(mockTargetIDs));

                oneOf(targetDao).getTarget(1);
                will(returnValue(mockTargetOne));

                oneOf(targetDao).getTarget(2);
                will(returnValue(mockTargetTwo));

                /// getPermissionsForAnnotation

                oneOf(annotationDao).getPermissions(1);
                will(returnValue(listMap));

                oneOf(userDao).getURIFromInternalID(2);
                will(returnValue(uri2));

                oneOf(userDao).getURIFromInternalID(3);
                will(returnValue(uri3));
            }
        });

        Annotation result = dbIntegrityService.getAnnotation(1);
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations + "00000000-0000-0000-0000-000000000021", result.getURI());
        assertEquals("text/plain", result.getBody().getTextBody().getMimeType());
        assertEquals("<html><body>some html 1</body></html>", result.getBody().getTextBody().getBody());
        assertEquals("Sagrada Famiglia", result.getHeadline());
        assertEquals("2013-08-12T09:25:00.383000Z", result.getLastModified().toString());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_users + "00000000-0000-0000-0000-000000000111", result.getOwnerRef());

        assertEquals(mockTargetOne.getLink(), result.getTargets().getTargetInfo().get(0).getLink());
        assertEquals(mockTargetOne.getURI(), result.getTargets().getTargetInfo().get(0).getRef());
        assertEquals(mockTargetOne.getVersion(), result.getTargets().getTargetInfo().get(0).getVersion());
        assertEquals(mockTargetTwo.getLink(), result.getTargets().getTargetInfo().get(1).getLink());
        assertEquals(mockTargetTwo.getURI(), result.getTargets().getTargetInfo().get(1).getRef());
        assertEquals(mockTargetTwo.getVersion(), result.getTargets().getTargetInfo().get(1).getVersion());

        assertEquals(Permission.WRITER, result.getPermissions().getUserWithPermission().get(0).getPermission());
        assertEquals(uri2, result.getPermissions().getUserWithPermission().get(0).getRef());

        assertEquals(Permission.READER, result.getPermissions().getUserWithPermission().get(1).getPermission());
        assertEquals(uri3, result.getPermissions().getUserWithPermission().get(1).getRef());
    }

    /**
     * Test of getFilteredAnnotationIDs method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testGetFilteredAnnotationIDs() {
        System.out.println("test getFilteredAnnotationIDs");

        final List<Number> mockTargetIDs = new ArrayList<Number>();
        mockTargetIDs.add(1);
        mockTargetIDs.add(2);

        final List<Number> mockAnnotationIDs1 = new ArrayList<Number>();
        mockAnnotationIDs1.add(1);

        final List<Number> mockAnnotationIDs2 = new ArrayList<Number>();
        mockAnnotationIDs2.add(1);
        mockAnnotationIDs2.add(2);

        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();

        final List<Number> mockRetval = new ArrayList<Number>();
        mockRetval.add(1);


        mockeryDao.checking(new Expectations() {
            {
                oneOf(targetDao).getTargetsReferringTo("nl.wikipedia.org");
                will(returnValue(mockTargetIDs));

                oneOf(annotationDao).getAnnotationIDsForTargets(mockTargetIDs);
                will(returnValue(mockAnnotationIDs2));

                oneOf(annotationDao).getFilteredAnnotationIDs(null, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));

                oneOf(annotationDao).getAnnotationIDsForUserWithPermission(3, "reader");
                will(returnValue(mockAnnotationIDs1));



            }
        });


        List result = dbIntegrityService.getFilteredAnnotationIDs(null, "nl.wikipedia.org", "some html 1", 3, "reader", null, after, before);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0));
    }

    @Test
    public void testGetAnnotationTargets() throws SQLException {
        System.out.println("test getAnnotationTargets");
        final List<Number> TargetIDs = new ArrayList<Number>();
        TargetIDs.add(1);
        TargetIDs.add(2);
        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).retrieveTargetIDs(1);
                will(returnValue(TargetIDs));

                oneOf(targetDao).getURIFromInternalID(1);
                will(returnValue(TestBackendConstants._TEST_SERVLET_URI_Targets + "00000000-0000-0000-0000-000000000031"));

                oneOf(targetDao).getURIFromInternalID(2);
                will(returnValue(TestBackendConstants._TEST_SERVLET_URI_Targets + "00000000-0000-0000-0000-000000000032"));

            }
        });

        ReferenceList result = dbIntegrityService.getAnnotationTargets(1);
        assertEquals(2, result.getRef().size());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_Targets + "00000000-0000-0000-0000-000000000031", result.getRef().get(0));
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_Targets + "00000000-0000-0000-0000-000000000032", result.getRef().get(1));

    }

//     @Override
//    public AnnotationInfoList getFilteredAnnotationInfos(String link, String text, String access, String namespace, UUID
//            owner, Timestamp after, Timestamp before){
//        List<Number> annotationIDs = getFilteredAnnotationIDs(link, text, access, namespace, owner, after, before);
//        List<AnnotationInfo> listAnnotationInfo = annotationDao.getAnnotationInfos(annotationIDs);
    //       AnnotationInfoList result = new AnnotationInfoList();
    //       result.getAnnotation().addAll(listAnnotationInfo);
    //       return result;
//    }
    @Test
    public void testGetFilteredAnnotationInfos() throws SQLException {
        System.out.println("test getetFilteredAnnotationInfos");

        final List<Number> mockTargetIDs = new ArrayList<Number>();
        mockTargetIDs.add(1);
        mockTargetIDs.add(2);

        final List<Number> mockAnnotationIDs1 = new ArrayList<Number>();
        mockAnnotationIDs1.add(1);

        final List<Number> mockAnnotationIDs2 = new ArrayList<Number>();
        mockAnnotationIDs2.add(1);
        mockAnnotationIDs2.add(2);

        final UUID ownerUUID = UUID.fromString("00000000-0000-0000-0000-000000000111");
        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();

        final List<Number> mockAnnotIDs = new ArrayList<Number>();
        mockAnnotIDs.add(1);

        final AnnotationInfo mockAnnotInfo = new AnnotationInfo();

        mockAnnotInfo.setHeadline("Sagrada Famiglia");
        mockAnnotInfo.setRef(TestBackendConstants._TEST_SERVLET_URI_annotations + "00000000-0000-0000-0000-000000000021");
        mockAnnotInfo.setOwnerRef(TestBackendConstants._TEST_SERVLET_URI_users + "00000000-0000-0000-0000-000000000111");

        final List<Number> targetIDs = new ArrayList<Number>();
        targetIDs.add(1);
        targetIDs.add(2);



        mockeryDao.checking(new Expectations() {
            {
                oneOf(userDao).getInternalID(ownerUUID);
                will(returnValue(1));

                oneOf(targetDao).getTargetsReferringTo("nl.wikipedia.org");
                will(returnValue(mockTargetIDs));

                oneOf(annotationDao).getAnnotationIDsForTargets(mockTargetIDs);
                will(returnValue(mockAnnotationIDs2));

                oneOf(annotationDao).getFilteredAnnotationIDs(1, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));

                oneOf(annotationDao).getAnnotationIDsForUserWithPermission(3, "reader");
                will(returnValue(mockAnnotationIDs1));


//                ///////////////////////////////////
//                
                oneOf(annotationDao).getAnnotationInfoWithoutTargets(1);
                will(returnValue(mockAnnotInfo));

                oneOf(annotationDao).getOwner(1);
                will(returnValue(1));

                oneOf(userDao).getURIFromInternalID(1);
                will(returnValue(TestBackendConstants._TEST_SERVLET_URI_users + "00000000-0000-0000-0000-000000000111"));

                ////
                oneOf(annotationDao).retrieveTargetIDs(1);
                will(returnValue(targetIDs));

                oneOf(targetDao).getURIFromInternalID(1);
                will(returnValue(TestBackendConstants._TEST_SERVLET_URI_Targets + "00000000-0000-0000-0000-000000000031"));


                oneOf(targetDao).getURIFromInternalID(2);
                will(returnValue(TestBackendConstants._TEST_SERVLET_URI_Targets + "00000000-0000-0000-0000-000000000032"));


            }
        });


        AnnotationInfoList result = dbIntegrityService.getFilteredAnnotationInfos(ownerUUID, "nl.wikipedia.org", "some html 1", 3, "reader", null, after, before);
        assertEquals(1, result.getAnnotationInfo().size());
        AnnotationInfo resultAnnotInfo = result.getAnnotationInfo().get(0);
        assertEquals(mockAnnotInfo.getHeadline(), resultAnnotInfo.getHeadline());
        assertEquals(mockAnnotInfo.getRef(), resultAnnotInfo.getRef());
        assertEquals(mockAnnotInfo.getOwnerRef(), resultAnnotInfo.getOwnerRef());
        assertEquals(mockAnnotInfo.getRef(), result.getAnnotationInfo().get(0).getRef());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_Targets + "00000000-0000-0000-0000-000000000031", resultAnnotInfo.getTargets().getRef().get(0));
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_Targets + "00000000-0000-0000-0000-000000000032", resultAnnotInfo.getTargets().getRef().get(1));

    }

    @Test
    public void testGetTargetsWithNoCachedRepresentation() {
        System.out.println("test getTargetsWithNoCachedRepresentation");
        final List<Number> targetIDs = new ArrayList<Number>();
        targetIDs.add(5);
        targetIDs.add(7);

        final List<Number> cachedIDs5 = new ArrayList<Number>();
        cachedIDs5.add(7);
        final List<Number> cachedIDs7 = new ArrayList<Number>();



        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).retrieveTargetIDs(3);
                will(returnValue(targetIDs));

                oneOf(targetDao).getCachedRepresentations(5);
                will(returnValue(cachedIDs5));

                oneOf(targetDao).getCachedRepresentations(7);
                will(returnValue(cachedIDs7));

                oneOf(targetDao).getURIFromInternalID(7);
                will(returnValue("00000000-0000-0000-0000-000000000037"));

            }
        });

        List<String> result = dbIntegrityService.getTargetsWithNoCachedRepresentation(3);
        assertEquals(1, result.size());
        assertEquals("00000000-0000-0000-0000-000000000037", result.get(0)); // Target number 7 has no cached
    }

    ////////////// ADDERS /////////////////////////
    /**
     * Test of addCachedForVersion method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testAddCached() throws SerialException, SQLException {
        System.out.println("addCached");
        String mime = "text/html";
        String type = "text";
        String tool = "latex";
        String externalID = UUID.randomUUID().toString();
        final CachedRepresentationInfo newCachedInfo = new CachedRepresentationInfo();
        newCachedInfo.setMimeType(mime);
        newCachedInfo.setType(type);
        newCachedInfo.setTool(tool);
        newCachedInfo.setURI(TestBackendConstants._TEST_SERVLET_URI_cached + externalID);

        String blobString = "aaa";
        byte[] blobBytes = blobString.getBytes();
        final ByteArrayInputStream newCachedBlob = new ByteArrayInputStream(blobBytes);

        mockeryDao.checking(new Expectations() {
            {

                oneOf(cachedRepresentationDao).getInternalIDFromURI(newCachedInfo.getURI());
                will(returnValue(null));

                oneOf(cachedRepresentationDao).addCachedRepresentation(newCachedInfo, newCachedBlob);
                will(returnValue(8));

                one(targetDao).addTargetCachedRepresentation(1, 8, "#(1,2)");
                will(returnValue(1));

            }
        });


        Number[] result = dbIntegrityService.addCachedForTarget(1, "#(1,2)", newCachedInfo, newCachedBlob);
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(8, result[1]);
    }

    /**
     * Test of updateSiblingTargetClassForTarget method, of class
     * DBIntegrityServiceImlp.
     *
     *
     */
    /**
     * Test of addTargetsForAnnotation method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testAddTargetsForAnnotation() throws Exception {
        System.out.println("test addTargetsForAnnotation");

        // test 1: adding an existing target
        TargetInfo testTargetOne = new TargetInfo();
        testTargetOne.setLink("http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia");
        testTargetOne.setRef(TestBackendConstants._TEST_SERVLET_URI_Targets + "00000000-0000-0000-0000-000000000031");
        testTargetOne.setVersion("version 1.0");
        final List<TargetInfo> mockTargetListOne = new ArrayList<TargetInfo>();
        mockTargetListOne.add(testTargetOne);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(targetDao).getInternalIDFromURI(mockTargetListOne.get(0).getRef());
                will(returnValue(1));

                oneOf(annotationDao).addAnnotationTarget(4, 1);
                will(returnValue(1));
            }
        });

        Map<String, String> result = dbIntegrityService.addTargetsForAnnotation(4, mockTargetListOne);
        assertEquals(0, result.size());

        // test 2: adding a new Target
        TargetInfo testTargetTwo = new TargetInfo();
        final String tempTargetID = UUID.randomUUID().toString();
        testTargetTwo.setRef(TestBackendConstants._TEST_SERVLET_URI_Targets + tempTargetID);
        testTargetTwo.setLink("http://www.sagradafamilia.cat/docs_instit/historia.php");
        testTargetTwo.setVersion("version 1.0");
        final List<TargetInfo> mockTargetListTwo = new ArrayList<TargetInfo>();
        mockTargetListTwo.add(testTargetTwo);

        final UUID mockNewTargetUUID = UUID.randomUUID();

        mockeryDao.checking(new Expectations() {
            {
                oneOf(targetDao).getInternalIDFromURI(mockTargetListTwo.get(0).getRef());
                will(returnValue(null));

                oneOf(targetDao).addTarget(with(aNonNull(Target.class)));
                will(returnValue(8)); //# the next new number is 8, we have already 7 Targets

                oneOf(targetDao).stringURItoExternalID(mockTargetListTwo.get(0).getRef());
                will(returnValue(tempTargetID));

                oneOf(targetDao).getExternalID(8);
                will(returnValue(mockNewTargetUUID));

                oneOf(annotationDao).addAnnotationTarget(1, 8);
                will(returnValue(1));

            }
        });

        Map<String, String> resultTwo = dbIntegrityService.addTargetsForAnnotation(1, mockTargetListTwo);
        assertEquals(1, resultTwo.size());
        assertEquals(mockNewTargetUUID.toString(), resultTwo.get(tempTargetID));

    }

    /**
     * Test of addUsersAnnotation method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testAddUsersAnnotation() throws Exception {
        System.out.println("test addUsersAnnotation");

        // expectations for addUsersannotation itself
        final Annotation testAnnotation = testInstances.getAnnotationToAdd();

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).addAnnotation(testAnnotation, 3);
                will(returnValue(5)); // the next free number is 5

                //  expectations for addTargetsForannotation
                oneOf(targetDao).getInternalIDFromURI(TestBackendConstants._TEST_SERVLET_URI_Targets + "00000000-0000-0000-0000-000000000031");
                will(returnValue(1));

                oneOf(annotationDao).addAnnotationTarget(5, 1);
                will(returnValue(1));

                ///////////

                oneOf(annotationDao).updateAnnotationBody(5, testAnnotation.getBody().getTextBody().getBody(), testAnnotation.getBody().getTextBody().getMimeType(), false);
                will(returnValue(1)); // the DB update will be called at perform anyway, even if the body is not changed (can be optimized)


            }
        });

        Number result = dbIntegrityService.addUsersAnnotation(3, testAnnotation);
        assertEquals(5, result);
    }

    @Test
    public void testAddUser() {
        System.out.println("test addUser");
        final User freshUser = new User();
        freshUser.setDisplayName("Guilherme");
        freshUser.setEMail("Guilherme.Silva@mpi.nl");
        mockeryDao.checking(new Expectations() {
            {
                oneOf(userDao).userExists(freshUser);
                will(returnValue(false));

                oneOf(userDao).addUser(freshUser, "guisil@mpi.nl");
                will(returnValue(11));
            }
        });


        assertEquals(11, dbIntegrityService.addUser(freshUser, "guisil@mpi.nl").intValue());

        /// user already exists
        final User user = new User();
        freshUser.setDisplayName("Olha");
        freshUser.setEMail("Olha.Shakaravska@mpi.nl");
        mockeryDao.checking(new Expectations() {
            {
                oneOf(userDao).userExists(user);
                will(returnValue(true));

            }
        });

        assertTrue(null == dbIntegrityService.addUser(user, "olhsha@mpi.nl"));
    }

    //////////////////// DELETERS ////////////////
    @Test
    public void testDeleteUser() {
        System.out.println("test deleteUser");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(userDao).deleteUser(1);
                will(returnValue(0));

                oneOf(userDao).deleteUser(3);
                will(returnValue(0));

                oneOf(userDao).deleteUser(10);
                will(returnValue(1));

            }
        });

        assertEquals(0, dbIntegrityService.deleteUser(1));
        assertEquals(0, dbIntegrityService.deleteUser(3));
        assertEquals(1, dbIntegrityService.deleteUser(10));
    }

    /**
     * Test of deleteCachedForVersion method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testDeleteCachedRepresentationForTarget() throws SQLException {
        System.out.println("test deleteCachedRepresentationForTarget");
        mockeryDao.checking(new Expectations() {
            {
                oneOf(targetDao).deleteTargetCachedRepresentation(5, 7);
                will(returnValue(1));

                oneOf(cachedRepresentationDao).deleteCachedRepresentation(7);
                will(returnValue(1)); // cached is used by another version

            }
        });

        int[] result = dbIntegrityService.deleteCachedRepresentationOfTarget(5, 7);
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(1, result[1]);
    }

    /////////////////////////////////////////////
    @Test
    public void testDeleteAllCachedRepresentationsOfTarget() throws SQLException {
        System.out.println("test deleteAllCachedRepresentationsOfTarget");
        final List<Number> cachedList = new ArrayList<Number>();
        cachedList.add(1);
        cachedList.add(2);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(targetDao).getCachedRepresentations(1);
                will(returnValue(cachedList));

                oneOf(targetDao).deleteTargetCachedRepresentation(1, 1);
                will(returnValue(1));

                oneOf(cachedRepresentationDao).deleteCachedRepresentation(1);
                will(returnValue(1));

                oneOf(targetDao).deleteTargetCachedRepresentation(1, 2);
                will(returnValue(1));

                oneOf(cachedRepresentationDao).deleteCachedRepresentation(2);
                will(returnValue(1));

            }
        });

        int[] result = dbIntegrityService.deleteAllCachedRepresentationsOfTarget(1);
        assertEquals(2, result[0]); // # affected rows in Targets_cacheds
        assertEquals(2, result[1]); // # affected rows in cacheds 
    }

    /**
     * Test of deleteAnnotationWithTargets method, of class
     * DBIntegrityServiceImlp.
     */
    @Test
    public void testDeleteAnnotation() throws Exception {
        System.out.println("deleteAnnotation ");

        // deleting annotation 3, which has its target Target 2  (used by annotation # 1)
        final List<Number> mockTargetIDs = new ArrayList<Number>();
        mockTargetIDs.add(2);

        final List<Number> mockCachedIDs = new ArrayList<Number>();
        mockCachedIDs.add(3);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).deleteAnnotationPrincipalPermissions(2);
                will(returnValue(2));

                oneOf(annotationDao).retrieveTargetIDs(2);
                will(returnValue(mockTargetIDs));

                oneOf(annotationDao).deleteAllAnnotationTarget(2);
                will(returnValue(1));

                oneOf(annotationDao).deleteAnnotation(2);
                will(returnValue(1));

                oneOf(targetDao).getCachedRepresentations(2);
                will(returnValue(mockCachedIDs));

                oneOf(targetDao).deleteTargetCachedRepresentation(2, 3);
                will(returnValue(1));

                oneOf(cachedRepresentationDao).deleteCachedRepresentation(3);
                will(returnValue(1));

                oneOf(targetDao).deleteTarget(2);
                will(returnValue(1));


            }
        });
        int[] result = dbIntegrityService.deleteAnnotation(2);// the Target will be deleted because it is not referred by any annotation
        assertEquals(4, result.length);
        assertEquals(1, result[0]); // annotation 3 is deleted
        assertEquals(2, result[1]); // 2 rows in "annotation principal permissions are deleted"
        assertEquals(1, result[2]);  // row (3,2) in "annotations_Targets" is deleted
        assertEquals(1, result[3]); //  Target 3 is deleted 
    }
//    @Test
//    public void testCreateTarget(){  
//        NewTargetInfo newTargetInfo = new NewTargetInfo();
//        newTargetInfo.setLink(TestBackendConstants._TEST_NEW_Target_LINK);
//        newTargetInfo.setVersion(null);
//        
//        Target result = dbIntegrityService.createTarget(newTargetInfo);
//        assertEquals(TestBackendConstants._TEST_NEW_Target_LINK, result.getLink());
//        assertFalse(null == result.getURI());
//        
//    }
//    
//    @Test
//    public void testCreateVersion(){  
//        NewTargetInfo newTargetInfo = new NewTargetInfo();
//        newTargetInfo.setLink(TestBackendConstants._TEST_NEW_Target_LINK);
//        newTargetInfo.setVersion(null);
//        
//        Version result = dbIntegrityService.createVersion(newTargetInfo);
//        assertFalse(null == result.getVersion()); // will be chnaged once the schema for version is fixed: ID is added
//        
//    }

    /**
     * NOTEBOOKS
     */
    /**
     * Getters
     */
   
//     public Number getNotebookInternalIdentifier(UUID externalIdentifier){
//        return notebookDao.getInternalID(externalIdentifier);
//    }
    
    @Test
    public void testGetNotebookInternalIdentifier() {
        
        final UUID mockUUID = UUID.fromString("00000000-0000-0000-0000-000000000021");
        
        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).getInternalID(mockUUID);
                will(returnValue(1));
            }
        });

       assertEquals(1, dbIntegrityService.getNotebookInternalIdentifier(mockUUID));
    
    }  
       
    
    
//    public NotebookInfoList getNotebooks(Number principalID, String permission) {
//        NotebookInfoList result = new NotebookInfoList();
//        if (permission.equalsIgnoreCase("reader") || permission.equalsIgnoreCase("writer")) {
//            List<Number> notebookIDs = notebookDao.getNotebookIDs(principalID, Permission.fromValue(permission));
//            for (Number notebookID : notebookIDs) {
//                NotebookInfo notebookInfo = notebookDao.getNotebookInfoWithoutOwner(notebookID);
//                Number ownerID = notebookDao.getOwner(notebookID);
//                notebookInfo.setOwnerRef(userDao.getURIFromInternalID(ownerID));
//                result.getNotebookInfo().add(notebookInfo);
//            }
//        } else {
//            if (permission.equalsIgnoreCase("owner")) {
//                List<Number> notebookIDs = notebookDao.getNotebookIDsOwnedBy(principalID);
//                String ownerRef = userDao.getURIFromInternalID(principalID);
//                for (Number notebookID : notebookIDs) {
//                    NotebookInfo notebookInfo = notebookDao.getNotebookInfoWithoutOwner(notebookID);
//                    notebookInfo.setOwnerRef(ownerRef);
//                    result.getNotebookInfo().add(notebookInfo);
//                }
//            } else {
//                return null;
//            }
//        }
//        return result;
//    }
    @Test
    public void testGetNotebooksReaderBranch() {

        final List<Number> mockNotebookIDs = new ArrayList<Number>();
        mockNotebookIDs.add(1);

        final NotebookInfo mockNotebookInfo = new NotebookInfo();
        mockNotebookInfo.setRef("00000000-0000-0000-0000-000000000011");
        mockNotebookInfo.setTitle("Notebook 1");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).getNotebookIDs(3, Permission.READER);
                will(returnValue(mockNotebookIDs));

                oneOf(notebookDao).getNotebookInfoWithoutOwner(1);
                will(returnValue(mockNotebookInfo));

                oneOf(notebookDao).getOwner(1);
                will(returnValue(1));

                oneOf(userDao).getURIFromInternalID(1);
                will(returnValue("00000000-0000-0000-0000-000000000111"));

            }
        });

        NotebookInfoList result = dbIntegrityService.getNotebooks(3, "reader");
        assertEquals("00000000-0000-0000-0000-000000000011", result.getNotebookInfo().get(0).getRef());
        assertEquals("00000000-0000-0000-0000-000000000111", result.getNotebookInfo().get(0).getOwnerRef());
        assertEquals("Notebook 1", result.getNotebookInfo().get(0).getTitle());

    }

    @Test
    public void testGetNotebooksOwnerBranch() {

        final List<Number> mockNotebookIDs = new ArrayList<Number>();
        mockNotebookIDs.add(3);
        mockNotebookIDs.add(4);

        final NotebookInfo mockNotebookInfo1 = new NotebookInfo();
        mockNotebookInfo1.setRef("00000000-0000-0000-0000-000000000013");
        mockNotebookInfo1.setTitle("Notebook 3");

        final NotebookInfo mockNotebookInfo2 = new NotebookInfo();
        mockNotebookInfo2.setRef("00000000-0000-0000-0000-000000000014");
        mockNotebookInfo2.setTitle("Notebook 4");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).getNotebookIDsOwnedBy(3);
                will(returnValue(mockNotebookIDs));

                oneOf(userDao).getURIFromInternalID(3);
                will(returnValue("00000000-0000-0000-0000-000000000113"));

                oneOf(notebookDao).getNotebookInfoWithoutOwner(3);
                will(returnValue(mockNotebookInfo1));

                oneOf(notebookDao).getNotebookInfoWithoutOwner(4);
                will(returnValue(mockNotebookInfo2));

            }
        });

        NotebookInfoList result = dbIntegrityService.getNotebooks(3, "owner");
        assertEquals("00000000-0000-0000-0000-000000000013", result.getNotebookInfo().get(0).getRef());
        assertEquals("00000000-0000-0000-0000-000000000113", result.getNotebookInfo().get(0).getOwnerRef());
        assertEquals("Notebook 3", result.getNotebookInfo().get(0).getTitle());
        assertEquals("00000000-0000-0000-0000-000000000014", result.getNotebookInfo().get(1).getRef());
        assertEquals("00000000-0000-0000-0000-000000000113", result.getNotebookInfo().get(1).getOwnerRef());
        assertEquals("Notebook 4", result.getNotebookInfo().get(1).getTitle());

    }

//    public ReferenceList getNotebooksOwnedBy(Number principalID) {
//        ReferenceList result = new ReferenceList();
//        List<Number> notebookIDs = notebookDao.getNotebookIDsOwnedBy(principalID);
//        for (Number notebookID : notebookIDs) {
//            String reference = notebookDao.getURIFromInternalID(notebookID);
//            result.getRef().add(reference);
//        }
//        return result;
//    }
    @Test
    public void testGetNotebooksOwnedBy() {

        final List<Number> mockNotebookIDs = new ArrayList<Number>();
        mockNotebookIDs.add(3);
        mockNotebookIDs.add(4);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).getNotebookIDsOwnedBy(3);
                will(returnValue(mockNotebookIDs));

                oneOf(notebookDao).getURIFromInternalID(3);
                will(returnValue(TestBackendConstants._TEST_SERVLET_URI_notebooks + "00000000-0000-0000-0000-000000000013"));

                oneOf(notebookDao).getURIFromInternalID(4);
                will(returnValue(TestBackendConstants._TEST_SERVLET_URI_notebooks + "00000000-0000-0000-0000-000000000014"));

            }
        });

        ReferenceList result = dbIntegrityService.getNotebooksOwnedBy(3);
        assertEquals(2, result.getRef().size());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_notebooks + "00000000-0000-0000-0000-000000000013", result.getRef().get(0));
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_notebooks + "00000000-0000-0000-0000-000000000014", result.getRef().get(1));
    }
    
 /*      public boolean hasAccess(Number notebookID, Number principalID, Permission permission){
        List<Number> notebookIDs = notebookDao.getNotebookIDs(principalID, permission);
        if (notebookIDs == null) {
            return false;
        } 
        return notebookIDs.contains(notebookID);
    } */
    
    @Test
    public void testHasAccess() {

        
        final Permission writer = Permission.fromValue("writer"); 
        final List<Number> mockNotebookIDwriter = new ArrayList<Number>();
        mockNotebookIDwriter.add(1);
        mockNotebookIDwriter.add(4);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).getNotebookIDs(2, writer);
                will(returnValue(mockNotebookIDwriter));
                
                oneOf(notebookDao).getNotebookIDs(2, writer);
                will(returnValue(mockNotebookIDwriter));

            }
        });

        assertTrue(dbIntegrityService.hasAccess(4, 2, writer));
        assertFalse(dbIntegrityService.hasAccess(5, 2, writer));
    }
    
    /*
     public ReferenceList getPrincipals(Number notebookID, String permission) {
        ReferenceList result = new ReferenceList();
        List<Number> principalIDs = notebookDao.getPrincipalIDsWithPermission(notebookID, Permission.fromValue(permission));
        for (Number principalID : principalIDs) {
            String reference = userDao.getURIFromInternalID(principalID);
            result.getRef().add(reference);
        }
        return result;
    }
     }*/
    @Test
    public void testGetPrincipals() {
        final List<Number> mockPrincipalIDs = new ArrayList<Number>();
        mockPrincipalIDs.add(2);
        mockPrincipalIDs.add(4);
        
        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).getPrincipalIDsWithPermission(1, Permission.WRITER);
                will(returnValue(mockPrincipalIDs));

                oneOf(userDao).getURIFromInternalID(2);
                will(returnValue("serviceURI/users/00000000-0000-0000-0000-000000000112"));

                oneOf(userDao).getURIFromInternalID(4);
                will(returnValue("serviceURI/users/00000000-0000-0000-0000-000000000114"));


            }
        });

        ReferenceList result = dbIntegrityService.getPrincipals(1, "writer");
        assertEquals("serviceURI/users/00000000-0000-0000-0000-000000000112", result.getRef().get(0).toString());
        assertEquals("serviceURI/users/00000000-0000-0000-0000-000000000114", result.getRef().get(1).toString());

    }

//   @Override
//    public Notebook getNotebook(Number notebookID) {
//        Notebook result = notebookDao.getNotebookWithoutAnnotationsAndPermissionsAndOwner(notebookID);
//
//        result.setOwnerRef(userDao.getURIFromInternalID(notebookDao.getOwner(notebookID)));
//
//        ReferenceList annotations = new ReferenceList();
//        List<Number> annotationIDs = notebookDao.getAnnotations(notebookID);
//        for (Number annotationID : annotationIDs) {
//            annotations.getRef().add(annotationDao.getURIFromInternalID(annotationID));
//        }
//        result.setAnnotations(annotations);
//
//        UserWithPermissionList ups = new UserWithPermissionList();
//        List<Permission> permissions = new ArrayList<Permission>();
//        permissions.add(Permission.READER);
//        permissions.add(Permission.WRITER);
//        for (Permission permission : permissions) {
//            List<Number> users = notebookDao.getPrincipalIDsWithPermission(notebookID, permission);
//            if (users != null) {
//                for (Number user : users) {
//                    UserWithPermission up = new UserWithPermission();
//                    up.setRef(userDao.getURIFromInternalID(user));
//                    up.setPermission(permission);
//                    ups.getUserWithPermission().add(up);
//                }
//            }
//        }
//
//        result.setPermissions(ups);
//        return result;
//    }


    @Test
    public void testGetNotebook() throws DatatypeConfigurationException{

        final Notebook mockNotebook = new Notebook();
        mockNotebook.setURI("serviceURI/notebooks/00000000-0000-0000-0000-000000000012");
        mockNotebook.setTitle("Notebook 2");
        mockNotebook.setLastModified(DatatypeFactory.newInstance().newXMLGregorianCalendar("2014-02-12T09:25:00.383000Z"));
        
        final List<Number> mockAnnotations = new ArrayList<Number>();
        mockAnnotations.add(3);
        
        final List<Number> mockReaders = new ArrayList<Number>();
        mockReaders.add(1);
        
        final List<Number> mockWriters = new ArrayList<Number>();
        mockWriters.add(3);
        
        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).getNotebookWithoutAnnotationsAndPermissionsAndOwner(2);
                will(returnValue(mockNotebook));

                oneOf(notebookDao).getOwner(2);
                will(returnValue(2));

                oneOf(userDao).getURIFromInternalID(2);
                will(returnValue("serviceURI/users/00000000-0000-0000-0000-000000000112"));

                oneOf(notebookDao).getAnnotations(2);
                will(returnValue(mockAnnotations));
                
                oneOf(annotationDao).getURIFromInternalID(3);
                will(returnValue("serviceURI/annotations/00000000-0000-0000-0000-000000000023"));
                
                oneOf(notebookDao).getPrincipalIDsWithPermission(2, Permission.READER);
                will(returnValue(mockReaders));
                
                oneOf(userDao).getURIFromInternalID(1);
                will(returnValue("serviceURI/users/00000000-0000-0000-0000-000000000111"));
                
                oneOf(notebookDao).getPrincipalIDsWithPermission(2, Permission.WRITER);
                will(returnValue(mockWriters));
                
                oneOf(userDao).getURIFromInternalID(3);
                will(returnValue("serviceURI/users/00000000-0000-0000-0000-000000000113"));

                

            }
        });

        Notebook result = dbIntegrityService.getNotebook(2);
        assertEquals("serviceURI/notebooks/00000000-0000-0000-0000-000000000012", result.getURI());
        assertEquals("serviceURI/users/00000000-0000-0000-0000-000000000112", result.getOwnerRef());
        assertEquals("2014-02-12T09:25:00.383000Z", result.getLastModified().toString());
        assertEquals("Notebook 2", result.getTitle());
        assertEquals(1, result.getAnnotations().getRef().size());
        assertEquals("serviceURI/annotations/00000000-0000-0000-0000-000000000023", result.getAnnotations().getRef().get(0));
        assertEquals(2, result.getPermissions().getUserWithPermission().size());
        assertEquals("serviceURI/users/00000000-0000-0000-0000-000000000111", result.getPermissions().getUserWithPermission().get(0).getRef());
        assertEquals("reader", result.getPermissions().getUserWithPermission().get(0).getPermission().value());
        assertEquals("serviceURI/users/00000000-0000-0000-0000-000000000113", result.getPermissions().getUserWithPermission().get(1).getRef());
        assertEquals("writer", result.getPermissions().getUserWithPermission().get(1).getPermission().value());
        
    }

//    public List<UUID> getAnnotationsForNotebook(Number notebookID, int startAnnotation, int maximumAnnotations, String orderedBy, boolean desc) {
//        List<Number> annotationIDs = notebookDao.getAnnotations(notebookID);
//
//        if (startAnnotation < -1) {
//            logger.info("Variable's startAnnotation value " + startAnnotation + " is invalid. I will return null.");
//            return null;
//        }
//
//        if (maximumAnnotations < -1) {
//            logger.info("Variable's maximumAnnotations value " + maximumAnnotations + " is invalid. I will return null.");
//            return null;
//        }
//
//        int offset = (startAnnotation > 0) ? startAnnotation - 1 : 0;
//        String direction = desc ? " DESC " : " ASC ";
//        List<Number> selectedAnnotIDs = annotationDao.sublistOrderedAnnotationIDs(annotationIDs, offset, maximumAnnotations, orderedBy, direction);
//        List<UUID> annotationUUIDs = new ArrayList<UUID>();
//        for (Number annotationID : selectedAnnotIDs) {
//            annotationUUIDs.add(annotationDao.getExternalID(annotationID));
//        }
//        return annotationUUIDs;
//    }
    @Test
    public void testAnnotationsForNotebook() {
        final List<Number> mockAnnotationIDs = new ArrayList<Number>();
        mockAnnotationIDs.add(1);
        mockAnnotationIDs.add(2);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).getAnnotations(1);
                will(returnValue(mockAnnotationIDs));

                oneOf(annotationDao).sublistOrderedAnnotationIDs(mockAnnotationIDs, 0, 3, "last_modified", "DESC");
                will(returnValue(mockAnnotationIDs));

                oneOf(annotationDao).getURIFromInternalID(1);
                will(returnValue("serviceURI/annotations/00000000-0000-0000-0000-000000000021"));

                oneOf(annotationDao).getURIFromInternalID(2);
                will(returnValue("serviceURI/annotations/00000000-0000-0000-0000-000000000022"));



            }
        });

        ReferenceList result = dbIntegrityService.getAnnotationsForNotebook(1, -1, 3, "last_modified", true);
        assertEquals(2, result.getRef().size());
        assertEquals("serviceURI/annotations/00000000-0000-0000-0000-000000000021", result.getRef().get(0).toString());
        assertEquals("serviceURI/annotations/00000000-0000-0000-0000-000000000022", result.getRef().get(1).toString());

    }

    /**
     * Updaters
     */
//    public boolean updateNotebookMetadata(Number notebookID, NotebookInfo upToDateNotebookInfo) {
//        Number ownerID = userDao.getInternalIDFromURI(upToDateNotebookInfo.getOwnerRef());
//        return notebookDao.updateNotebookMetadata(notebookID, upToDateNotebookInfo.getTitle(), ownerID);
//    }
    @Test
    public void testUpdateNotebookMetadata() {

        final NotebookInfo mockNotebookInfo = new NotebookInfo();
        mockNotebookInfo.setOwnerRef(TestBackendConstants._TEST_SERVLET_URI_users + "00000000-0000-0000-0000-000000000113");
        mockNotebookInfo.setTitle("New Title");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(userDao).getInternalIDFromURI(TestBackendConstants._TEST_SERVLET_URI_users + "00000000-0000-0000-0000-000000000113");
                will(returnValue(3));

                oneOf(notebookDao).updateNotebookMetadata(1, "New Title", 3);
                will(returnValue(true));
            }
        });

        boolean result = dbIntegrityService.updateNotebookMetadata(1, mockNotebookInfo);
        assertTrue(result);
    }

// 
//    public boolean addAnnotationToNotebook(Number notebookID, Number annotationID) {
//        return notebookDao.addAnnotationToNotebook(notebookID, annotationID);
//    }
    @Test
    public void testAddAnnotationToNotebook() {

        mockeryDao.checking(new Expectations() {
            {

                oneOf(notebookDao).addAnnotationToNotebook(1, 3);
                will(returnValue(true));
            }
        });

        assertTrue(dbIntegrityService.addAnnotationToNotebook(1, 3));
    }

    /**
     * Adders
     */
//    public Number createNotebook(Notebook notebook, Number ownerID) {
//        Number notebookID = notebookDao.createNotebookWithoutPermissionsAndAnnotations(notebook, ownerID);
//        boolean updateOwner = notebookDao.setOwner(notebookID, ownerID);
//        List<UserWithPermission> permissions = notebook.getPermissions().getUserWithPermission();
//        for (UserWithPermission principalPermission : permissions) {
//            Number principalID = userDao.getInternalIDFromURI(principalPermission.getRef());
//            Permission permission = principalPermission.getPermission();
//            boolean updatePermissions = notebookDao.addPermissionToNotebook(notebookID, principalID, permission);
//        }
//        return notebookID;
//    }
    @Test
    public void testCreateNotebook() {

        final Notebook notebook = new Notebook();
        notebook.setOwnerRef("tmpXXX");
        notebook.setTitle("(Almost) Copy of Notebook 1");
        notebook.setURI("tmpYYY");

        UserWithPermissionList permissions = new UserWithPermissionList();
        UserWithPermission p1 = new UserWithPermission();
        p1.setPermission(Permission.WRITER);
        p1.setRef(TestBackendConstants._TEST_SERVLET_URI_users + "00000000-0000-0000-0000-000000000112");
        permissions.getUserWithPermission().add(p1);
        UserWithPermission p2 = new UserWithPermission();
        p2.setPermission(Permission.READER);
        p2.setRef(TestBackendConstants._TEST_SERVLET_URI_users + "00000000-0000-0000-0000-000000000113");
        permissions.getUserWithPermission().add(p2);
        notebook.setPermissions(permissions);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).createNotebookWithoutPermissionsAndAnnotations(notebook, 1);
                will(returnValue(5));

                oneOf(notebookDao).setOwner(5, 1);
                will(returnValue(true));

                oneOf(userDao).getInternalIDFromURI(TestBackendConstants._TEST_SERVLET_URI_users + "00000000-0000-0000-0000-000000000112");
                will(returnValue(2));

                oneOf(userDao).getInternalIDFromURI(TestBackendConstants._TEST_SERVLET_URI_users + "00000000-0000-0000-0000-000000000113");
                will(returnValue(3));

                oneOf(notebookDao).addPermissionToNotebook(5, 2, Permission.WRITER);
                will(returnValue(true));

                oneOf(notebookDao).addPermissionToNotebook(5, 3, Permission.READER);
                will(returnValue(true));

            }
        });

        Number result = dbIntegrityService.createNotebook(notebook, 1);
        assertEquals(5, result);

    }

//    public boolean createAnnotationInNotebook(Number notebookID, Annotation annotation, Number ownerID) {
//        Number newAnnotationID = this.addUsersAnnotation(ownerID, annotation);
//        return notebookDao.addAnnotationToNotebook(notebookID, newAnnotationID);
//    }
    @Test
    public void testCreateAnnotationInNotebook() {

        final Annotation testAnnotation = testInstances.getAnnotationToAdd();

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).addAnnotation(testAnnotation, 3);
                will(returnValue(5)); // the next free number is 5

                //  expectations for addTargetsForannotation
                oneOf(targetDao).getInternalIDFromURI(TestBackendConstants._TEST_SERVLET_URI_Targets + "00000000-0000-0000-0000-000000000031");
                will(returnValue(1));

                oneOf(annotationDao).addAnnotationTarget(5, 1);
                will(returnValue(1));

                oneOf(annotationDao).updateAnnotationBody(5, testAnnotation.getBody().getTextBody().getBody(), testAnnotation.getBody().getTextBody().getMimeType(), false);
                will(returnValue(1)); // the DB update will be called at perform anyway, even if the body is not changed (can be optimized)

                /////////////////////////

                oneOf(notebookDao).addAnnotationToNotebook(1, 5);
                will(returnValue(true));
            }
        });

        assertTrue(dbIntegrityService.createAnnotationInNotebook(1, testAnnotation, 3));

    }

    /**
     * Deleters
     */
//      public boolean deleteNotebook(Number notebookID) {
//        if (notebookDao.deleteAllPermissionsForNotebook(notebookID) || notebookDao.deleteAllAnnotationsFromNotebook(notebookID)) {
//            return notebookDao.deleteNotebook(notebookID);
//        } else {
//            return false;
//        }
//   
//   }
    @Test
    public void testDeleteNotebook() {

        mockeryDao.checking(new Expectations() {
            {

                oneOf(notebookDao).deleteAllPermissionsForNotebook(1);
                will(returnValue(true));

                oneOf(notebookDao).deleteAllAnnotationsFromNotebook(1);
                will(returnValue(true));

                oneOf(notebookDao).deleteNotebook(1);
                will(returnValue(true));
            }
        });

        assertTrue(dbIntegrityService.deleteNotebook(1));
    }
}
