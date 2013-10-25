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
import eu.dasish.annotation.backend.dao.TargetDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationBody.TextBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.Target;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.User;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;
import javax.xml.datatype.XMLGregorianCalendar;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
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
    private TargetDao TargetDao;
    @Autowired
    private AnnotationDao annotationDao;
    TestInstances testInstances = new TestInstances();

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
        final UUID externalID = UUID.fromString(TestBackendConstants._TEST_ANNOT_2_EXT);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getInternalID(externalID);
                will(returnValue(2));
            }
        });
        assertEquals(2, dbIntegrityService.getAnnotationInternalIdentifier(externalID));
    }

    /**
     * Test of getAnnotationExternalIdentifier method, of class
     * DBIntegrityServiceImlp.
     */
    @Test
    
    public void testGetAnnotationExternalIdentifier() {
        System.out.println("getAnnotationExternalIdentifier");
        final UUID externalID = UUID.fromString(TestBackendConstants._TEST_ANNOT_2_EXT);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getExternalID(2);
                will(returnValue(externalID));
            }
        });
        assertEquals(TestBackendConstants._TEST_ANNOT_2_EXT, dbIntegrityService.getAnnotationExternalIdentifier(2).toString());
    }

    /**
     * Test of getUserInternalIdentifier method, of class
     * DBIntegrityServiceImlp.
     */
    @Test
    
    public void testGetUserInternalIdentifier() {
        System.out.println("getUserInternalIdentifier");

        final UUID externalID = UUID.fromString(TestBackendConstants._TEST_USER_5_EXT_ID);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(userDao).getInternalID(externalID);
                will(returnValue(5));
            }
        });
        assertEquals(5, dbIntegrityService.getUserInternalIdentifier(externalID));
    }

    /**
     * Test of getUserExternalIdentifier method, of class
     * DBIntegrityServiceImlp.
     */
    @Test
    
    public void testGetUserExternalIdentifier() {
        System.out.println("getUserExternalIdentifier");
        final UUID externalID = UUID.fromString(TestBackendConstants._TEST_USER_5_EXT_ID);

        mockeryDao.checking(new Expectations() {
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
        mockAnnotation.setURI(TestBackendConstants._TEST_SERVLET_URI_annotations +TestBackendConstants._TEST_ANNOT_2_EXT);
        mockAnnotation.setHeadline(TestBackendConstants._TEST_ANNOT_2_HEADLINE);
        XMLGregorianCalendar mockTimeStamp = Helpers.setXMLGregorianCalendar(Timestamp.valueOf("2013-08-12 11:25:00.383000"));
        mockAnnotation.setTimeStamp(mockTimeStamp);
        mockAnnotation.setOwnerRef("3");

        AnnotationBody mockBody = new AnnotationBody();
        TextBody textBody = new AnnotationBody.TextBody();
        mockBody.setTextBody(textBody);
        textBody.setMimeType("text/plain");
        textBody.setValue(TestBackendConstants._TEST_ANNOT_2_BODY);
        mockAnnotation.setBody(mockBody);
        mockAnnotation.setTargets(null);


        final List<Number> mockTargetIDs = new ArrayList<Number>();
        mockTargetIDs.add(1);
        mockTargetIDs.add(2);

        final Target mockTargetOne = new Target();
        mockTargetOne.setLink(TestBackendConstants._TEST_Target_1_LINK);
        mockTargetOne.setURI(TestBackendConstants._TEST_SERVLET_URI_Targets +TestBackendConstants._TEST_Target_1_EXT_ID);
        mockTargetOne.setVersion(TestBackendConstants._TEST_Target_1_EXT_ID);

        final Target mockTargetTwo = new Target();
        mockTargetTwo.setLink(TestBackendConstants._TEST_Target_2_LINK);
        mockTargetTwo.setURI(TestBackendConstants._TEST_SERVLET_URI_Targets +TestBackendConstants._TEST_Target_2_EXT_ID);
        mockTargetTwo.setVersion(TestBackendConstants._TEST_Target_2_EXT_ID);

        final List<Map<Number, String>> listMap = new ArrayList<Map<Number, String>>();
        Map<Number, String> map3 = new HashMap<Number, String>();
        map3.put(3, "owner");
        listMap.add(map3);
        Map<Number, String> map4 = new HashMap<Number, String>();
        map4.put(4, "writer");
        listMap.add(map4);
        Map<Number, String> map5 = new HashMap<Number, String>();
        map5.put(5, "reader");
        listMap.add(map5);

        final UUID externalID3 = UUID.fromString(TestBackendConstants._TEST_USER_3_EXT_ID);
        final UUID externalID4 = UUID.fromString(TestBackendConstants._TEST_USER_4_EXT_ID);
        final UUID externalID5 = UUID.fromString(TestBackendConstants._TEST_USER_5_EXT_ID);

        final String uri3 = TestBackendConstants._TEST_SERVLET_URI_users +TestBackendConstants._TEST_USER_3_EXT_ID;
        final String uri4 = TestBackendConstants._TEST_SERVLET_URI_users +TestBackendConstants._TEST_USER_4_EXT_ID;
        final String uri5 = TestBackendConstants._TEST_SERVLET_URI_users +TestBackendConstants._TEST_USER_5_EXT_ID;


        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getAnnotationWithoutTargetsAndPermissions(2);
                will(returnValue(mockAnnotation));

                oneOf(userDao).getExternalID(3);
                will(returnValue(externalID3));

                oneOf(userDao).externalIDtoURI(TestBackendConstants._TEST_USER_3_EXT_ID);
                will(returnValue(uri3));
                
                oneOf(annotationDao).retrieveTargetIDs(2);
                will(returnValue(mockTargetIDs));

                oneOf(TargetDao).getTarget(1);
                will(returnValue(mockTargetOne));

                oneOf(TargetDao).getTarget(2);
                will(returnValue(mockTargetTwo));

                /// getPermissionsForAnnotation

                oneOf(annotationDao).getPermissions(2);
                will(returnValue(listMap));

                oneOf(userDao).getExternalID(3);
                will(returnValue(externalID3));

                oneOf(userDao).externalIDtoURI(TestBackendConstants._TEST_USER_3_EXT_ID);
                will(returnValue(uri3));


                oneOf(userDao).getExternalID(4);
                will(returnValue(externalID4));

                oneOf(userDao).externalIDtoURI(TestBackendConstants._TEST_USER_4_EXT_ID);
                will(returnValue(uri4));

                oneOf(userDao).getExternalID(5);
                will(returnValue(externalID5));

                oneOf(userDao).externalIDtoURI(TestBackendConstants._TEST_USER_5_EXT_ID);
                will(returnValue(uri5));
            }
        });

        Annotation result = dbIntegrityService.getAnnotation(2);
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_annotations+TestBackendConstants._TEST_ANNOT_2_EXT, result.getURI());
        assertEquals("text/plain", result.getBody().getTextBody().getMimeType());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_BODY, result.getBody().getTextBody().getValue());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_HEADLINE, result.getHeadline());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_TIME_STAMP, result.getTimeStamp().toString());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_users+TestBackendConstants._TEST_USER_3_EXT_ID, result.getOwnerRef());

        assertEquals(mockTargetOne.getLink(), result.getTargets().getTargetInfo().get(0).getLink());
        assertEquals(mockTargetOne.getURI(), result.getTargets().getTargetInfo().get(0).getRef());
        assertEquals(mockTargetOne.getVersion(), result.getTargets().getTargetInfo().get(0).getVersion());
        assertEquals(mockTargetTwo.getLink(), result.getTargets().getTargetInfo().get(1).getLink());
        assertEquals(mockTargetTwo.getURI(), result.getTargets().getTargetInfo().get(1).getRef());
        assertEquals(mockTargetTwo.getVersion(), result.getTargets().getTargetInfo().get(1).getVersion());

        assertEquals(Permission.OWNER, result.getPermissions().getUserWithPermission().get(0).getPermission());
        assertEquals(uri3, result.getPermissions().getUserWithPermission().get(0).getRef());

        assertEquals(Permission.WRITER, result.getPermissions().getUserWithPermission().get(1).getPermission());
        assertEquals(uri4, result.getPermissions().getUserWithPermission().get(1).getRef());

        assertEquals(Permission.READER, result.getPermissions().getUserWithPermission().get(2).getPermission());
        assertEquals(uri5, result.getPermissions().getUserWithPermission().get(2).getRef());
    }

    /**
     * Test of getFilteredAnnotationIDs method, of class DBIntegrityServiceImlp.
     */
    @Test
    
    public void testGetFilteredAnnotationIDs() {
        System.out.println("test getFilteredAnnotationIDs");

        final String word = "nl.wikipedia.org";

        final List<Number> mockTargetIDs = new ArrayList<Number>();
        mockTargetIDs.add(1);
        mockTargetIDs.add(2);

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

        mockeryDao.checking(new Expectations() {
            {
                oneOf(TargetDao).getTargetsReferringTo(word);
                will(returnValue(mockTargetIDs));

                oneOf(annotationDao).retrieveAnnotationList(mockTargetIDs);
                will(returnValue(mockAnnotationIDs));

                oneOf(userDao).getInternalID(owner);
                will(returnValue(3));

                oneOf(annotationDao).getFilteredAnnotationIDs(mockAnnotationIDs, text, access, namespace, 3, after, before);
                will(returnValue(mockRetval));

            }
        });


        List result = dbIntegrityService.getFilteredAnnotationIDs(word, text, access, namespace, owner, after, before);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0));
    }

    @Test
    
    public void testGetAnnotationTargets() throws SQLException{
        System.out.println("test getAnnotationTargets");
        final Number annotationID = 2;
        final List<Number> TargetIDs = new ArrayList<Number>();
        TargetIDs.add(1);
        TargetIDs.add(2);
        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).retrieveTargetIDs(annotationID);
                will(returnValue(TargetIDs));

                oneOf(TargetDao).getURIFromInternalID(1);
                will(returnValue(TestBackendConstants._TEST_SERVLET_URI_Targets+TestBackendConstants._TEST_Target_1_EXT_ID));

                oneOf(TargetDao).getURIFromInternalID(2);
                will(returnValue(TestBackendConstants._TEST_SERVLET_URI_Targets+TestBackendConstants._TEST_Target_2_EXT_ID));

            }
        });
       
        ReferenceList result = dbIntegrityService.getAnnotationTargets(annotationID);
        assertEquals(2, result.getRef().size());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_Targets+TestBackendConstants._TEST_Target_1_EXT_ID, result.getRef().get(0));
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_Targets+TestBackendConstants._TEST_Target_2_EXT_ID, result.getRef().get(1));
        
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
    public void testGetFilteredAnnotationInfos() throws SQLException{
        System.out.println("test getetFilteredAnnotationInfos");
        
        final String word = "nl.wikipedia.org";

        final List<Number> mockTargetIDs = new ArrayList<Number>();
        mockTargetIDs.add(1);
        mockTargetIDs.add(2);

        final List<Number> mockAnnotationIDs = new ArrayList<Number>();
        mockAnnotationIDs.add(2);
        mockAnnotationIDs.add(3);

        final String text = "some html";
        final String access = null;
        final String namespace = null;
        final UUID ownerUUID = UUID.fromString(TestBackendConstants._TEST_USER_3_EXT_ID);
        final Timestamp after = new Timestamp(0);
        final Timestamp before = new Timestamp(System.currentTimeMillis());

        final List<Number> mockAnnotIDs = new ArrayList<Number>();
        mockAnnotIDs.add(2);
        
        final AnnotationInfo mockAnnotInfo = new AnnotationInfo();
        
        mockAnnotInfo.setHeadline(TestBackendConstants._TEST_ANNOT_2_HEADLINE);        
        mockAnnotInfo.setRef(TestBackendConstants._TEST_SERVLET_URI_annotations + TestBackendConstants._TEST_ANNOT_2_EXT);
        
        final List<Number> TargetIDs = new ArrayList<Number>();
        TargetIDs.add(1);
        TargetIDs.add(2);
        
        final Map<AnnotationInfo,Number> mockPair = new HashMap<AnnotationInfo, Number>();
        mockPair.put(mockAnnotInfo, 3);
        
        mockeryDao.checking(new Expectations() {
            {
                // getFilteredAnnotationIds
                oneOf(TargetDao).getTargetsReferringTo(word);
                will(returnValue(mockTargetIDs));
                
                oneOf(annotationDao).retrieveAnnotationList(mockTargetIDs);
                will(returnValue(mockAnnotationIDs));
               
                oneOf(userDao).getInternalID(ownerUUID);
                will(returnValue(3));
              
                oneOf(annotationDao).getFilteredAnnotationIDs(mockAnnotationIDs, text, access, namespace, 3, after, before);
                will(returnValue(mockAnnotIDs));
                
                
//                ///////////////////////////////////
//                
                oneOf(annotationDao).getAnnotationInfoWithoutTargets(2);
                will(returnValue(mockPair));
                
                ////
                oneOf(annotationDao).retrieveTargetIDs(2);
                will(returnValue(TargetIDs));
                
                oneOf(TargetDao).getURIFromInternalID(1);
                will(returnValue(TestBackendConstants._TEST_SERVLET_URI_Targets +TestBackendConstants._TEST_Target_1_EXT_ID));
                
                oneOf(TargetDao).getURIFromInternalID(2);
                will(returnValue(TestBackendConstants._TEST_SERVLET_URI_Targets +TestBackendConstants._TEST_Target_2_EXT_ID));
                ////
                
                oneOf(userDao).getURIFromInternalID(3);
                will(returnValue(TestBackendConstants._TEST_SERVLET_URI_users +TestBackendConstants._TEST_USER_3_EXT_ID)); 
             
               
            }
        });
       
      
        AnnotationInfoList result = dbIntegrityService.getFilteredAnnotationInfos(word, text, access, namespace, ownerUUID, after, before);
        assertEquals(1, result.getAnnotationInfo().size()); 
        AnnotationInfo resultAnnotInfo = result.getAnnotationInfo().get(0);
        assertEquals(mockAnnotInfo.getHeadline(), resultAnnotInfo.getHeadline());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_users +TestBackendConstants._TEST_USER_3_EXT_ID, resultAnnotInfo.getOwnerRef());
        assertEquals(mockAnnotInfo.getRef(),result.getAnnotationInfo().get(0).getRef() );
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_Targets +TestBackendConstants._TEST_Target_1_EXT_ID, resultAnnotInfo.getTargets().getRef().get(0));
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_Targets +TestBackendConstants._TEST_Target_2_EXT_ID, resultAnnotInfo.getTargets().getRef().get(1));
          
    }
    
    @Test
    
    public void testGetTargetsWithNoCachedRepresentation(){
        System.out.println("test getTargetsWithNoCachedRepresentation");
        final Number annotationID = 4;
        final List<Number> TargetIDs = new ArrayList<Number>();
        TargetIDs.add(5);
        TargetIDs.add(7);
        
        final List<Number> cachedIDs5 = new ArrayList<Number>();
        cachedIDs5.add(7);
        final List<Number> cachedIDs7 = new ArrayList<Number>();
        
       
        
        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).retrieveTargetIDs(annotationID);
                will(returnValue(TargetIDs));

                oneOf(TargetDao).getCachedRepresentations(5);
                will(returnValue(cachedIDs5));
                
                oneOf(TargetDao).getCachedRepresentations(7);
                will(returnValue(cachedIDs7));
                
            }
        });
        
        List<Number> result = dbIntegrityService.getTargetsWithNoCachedRepresentation(annotationID);
        assertEquals(1, result.size());
        assertEquals(7, result.get(0)); // Target number 7 has no cached
    }
    
   
    ////////////// ADDERS /////////////////////////
    /**
     * Test of addCachedForVersion method, of class DBIntegrityServiceImlp.
     */
    @Test
    
    public void testAddCachedForVersion() throws SerialException, SQLException {
        System.out.println("addCachedForVersion");
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
        final Blob newCachedBlob = new SerialBlob(blobBytes);
        final Number newCachedID = 8;
        final Number versionID = 1;
        mockeryDao.checking(new Expectations() {
            {

                oneOf(cachedRepresentationDao).getInternalIDFromURI(newCachedInfo.getURI());
                will(returnValue(null));

                oneOf(cachedRepresentationDao).addCachedRepresentation(newCachedInfo, newCachedBlob);
                will(returnValue(newCachedID));

                one(TargetDao).addTargetCachedRepresentation(versionID, newCachedID);
                will(returnValue(1));

            }
        });


        Number[] result = dbIntegrityService.addCachedForTarget(versionID, newCachedInfo, newCachedBlob);
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(newCachedID, result[1]);
    }

    /**
     * Test of updateSiblingTargetClassForTarget method, of class
     * DBIntegrityServiceImlp.
     * 
  **/
   

    /**
     * Test of addTargetsForAnnotation method, of class DBIntegrityServiceImlp.
     */
    @Test
    
    public void testAddTargetsForAnnotation() throws Exception {
        System.out.println("test addTargetsForAnnotation");

//        @Override
//        public Map<String, String> addTargetsForAnnotation(Number annotationID, List<TargetInfo> Targets) throws SQLException {
//        Map<String, String> result = new HashMap<String, String>();
//        Number TargetIDRunner;
//        for (TargetInfo TargetInfo : Targets) {
//            TargetIDRunner = TargetDao.getInternalIDFromURI(TargetInfo.getRef());
//            if (TargetIDRunner != null) {
//                int affectedRows = annotationDao.addAnnotationTarget(annotationID, TargetIDRunner);
//            } else {
//                Target newTarget = createFreshTarget(TargetInfo);
//                Number TargetID = TargetDao.addTarget(newTarget);
//                String TargetTemporaryID = TargetDao.stringURItoExternalID(TargetInfo.getRef());
//                result.put(TargetTemporaryID, TargetDao.getExternalID(TargetID).toString());
//                int affectedRows = annotationDao.addAnnotationTarget(annotationID, TargetID);
//            }
//        }
//        return result;
//    }
        
        // test 1: adding an existing Target
        TargetInfo testTargetOne = new TargetInfo();
        testTargetOne.setLink(TestBackendConstants._TEST_Target_1_LINK);
        testTargetOne.setRef(TestBackendConstants._TEST_SERVLET_URI_Targets + TestBackendConstants._TEST_Target_1_EXT_ID);
        testTargetOne.setVersion(TestBackendConstants._TEST_Target_2_VERSION );
        final List<TargetInfo> mockTargetListOne = new ArrayList<TargetInfo>();
        mockTargetListOne.add(testTargetOne);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(TargetDao).getInternalIDFromURI(mockTargetListOne.get(0).getRef());
                will(returnValue(1));

                oneOf(annotationDao).addAnnotationTarget(1, 1);
                will(returnValue(1));
            }
        });

        Map<String, String> result = dbIntegrityService.addTargetsForAnnotation(1, mockTargetListOne);
        assertEquals(0, result.size());
        
        //        @Override
//        public Map<String, String> addTargetsForAnnotation(Number annotationID, List<TargetInfo> Targets) throws SQLException {
//        Map<String, String> result = new HashMap<String, String>();
//        Number TargetIDRunner;
//        for (TargetInfo TargetInfo : Targets) {
//            TargetIDRunner = TargetDao.getInternalIDFromURI(TargetInfo.getRef());
//            if (TargetIDRunner != null) {
//                int affectedRows = annotationDao.addAnnotationTarget(annotationID, TargetIDRunner);
//            } else {
//                Target newTarget = createFreshTarget(TargetInfo);
//                Number TargetID = TargetDao.addTarget(newTarget);
//                String TargetTemporaryID = TargetDao.stringURItoExternalID(TargetInfo.getRef());
//                result.put(TargetTemporaryID, TargetDao.getExternalID(TargetID).toString());
//                int affectedRows = annotationDao.addAnnotationTarget(annotationID, TargetID);
//            }
//        }
//        return result;
//    }
        

        // test 2: adding a new Target
        TargetInfo testTargetTwo = new TargetInfo();
        final String tempTargetID = UUID.randomUUID().toString();
        testTargetTwo.setRef(TestBackendConstants._TEST_SERVLET_URI_Targets + tempTargetID);
        testTargetTwo.setLink(TestBackendConstants._TEST_NEW_Target_LINK);
        testTargetTwo.setVersion("version 1.0");
        final List<TargetInfo> mockTargetListTwo = new ArrayList<TargetInfo>();
        mockTargetListTwo.add(testTargetTwo);

        final UUID mockNewTargetUUID = UUID.randomUUID();

        mockeryDao.checking(new Expectations() {
            {
                oneOf(TargetDao).getInternalIDFromURI(mockTargetListTwo.get(0).getRef());
                will(returnValue(null));

                oneOf(TargetDao).addTarget(with(aNonNull(Target.class)));
                will(returnValue(8)); //# the next new number is 8, we have already 7 Targets

                oneOf(TargetDao).stringURItoExternalID(mockTargetListTwo.get(0).getRef());
                will(returnValue(tempTargetID));

                oneOf(TargetDao).getExternalID(8);
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
                oneOf(annotationDao).addAnnotation(testAnnotation, 5);
                will(returnValue(6)); // the next free number is 6

                //  expectations for addTargetsForannotation
                oneOf(TargetDao).getInternalIDFromURI(with(aNonNull(String.class)));
                will(returnValue(1));

                oneOf(annotationDao).addAnnotationTarget(6, 1);
                will(returnValue(1));

                ///////////

                oneOf(annotationDao).updateBodyText(6, testAnnotation.getBody().getTextBody().getValue());
                will(returnValue(1)); // the DB update will be called at perform anyway, even if the body is not changed (can be optimized)

                oneOf(annotationDao).addAnnotationPrincipalPermission(6, 5, Permission.OWNER);
                will(returnValue(1));
            }
        });

        Number result = dbIntegrityService.addUsersAnnotation(5, testAnnotation);
        assertEquals(6, result);
    }

    @Test
    
    public void testAddUser() {
        System.out.println("test addUser");
        final User freshUser = new User();
        freshUser.setDisplayName("Guilherme");
        freshUser.setEMail("guisil@mpi.nl");
        mockeryDao.checking(new Expectations() {
            {
                oneOf(userDao).userExists(freshUser);
                will(returnValue(false));

                oneOf(userDao).addUser(freshUser, "xx");
                will(returnValue(7));
            }
        });


        assertEquals(7, dbIntegrityService.addUser(freshUser, "xx").intValue());

        /// user already exists
        final User user = new User();
        freshUser.setDisplayName("Olha");
        freshUser.setEMail("olhsha@mpi.nl");
        mockeryDao.checking(new Expectations() {
            {
                oneOf(userDao).userExists(user);
                will(returnValue(true));

            }
        });

        assertTrue(null == dbIntegrityService.addUser(user, "yy"));
    }

    //////////////////// DELETERS ////////////////
    @Test
    
    public void testDeleteUser() {
        System.out.println("test deleteUser");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(userDao).deleteUser(2);
                will(returnValue(0));

                oneOf(userDao).deleteUser(5);
                will(returnValue(0));

                oneOf(userDao).deleteUser(6);
                will(returnValue(1));

            }
        });

        assertEquals(0, dbIntegrityService.deleteUser(2));
        assertEquals(0, dbIntegrityService.deleteUser(5));
        assertEquals(1, dbIntegrityService.deleteUser(6));
    }

    /**
     * Test of deleteCachedForVersion method, of class DBIntegrityServiceImlp.
     */
    @Test
    
    public void testDeleteCachedRepresentationForTarget() throws SQLException{
        System.out.println("test deleteCachedRepresentationForTarget");
        mockeryDao.checking(new Expectations() {
            {
                oneOf(TargetDao).deleteTargetCachedRepresentation(5, 7);
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
    
    public void testDeleteAllCachedRepresentationsOfTarget() throws SQLException{
        System.out.println("test deleteAllCachedRepresentationsOfTarget");
        final List<Number> cachedList = new ArrayList<Number>();
        cachedList.add(1);
        cachedList.add(2);
        
        mockeryDao.checking(new Expectations() {
            {
                oneOf(TargetDao).getCachedRepresentations(1);
                will(returnValue(cachedList));
                
                oneOf(TargetDao).deleteTargetCachedRepresentation(1, 1);
                will(returnValue(1));
                
                oneOf(cachedRepresentationDao).deleteCachedRepresentation(1);
                will(returnValue(1));
                
                oneOf(TargetDao).deleteTargetCachedRepresentation(1, 2);
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
                oneOf(annotationDao).deleteAnnotationPrincipalPermissions(3);
                will(returnValue(3));

                oneOf(annotationDao).retrieveTargetIDs(3);
                will(returnValue(mockTargetIDs));                
              
                oneOf(annotationDao).deleteAllAnnotationTarget(3);
                will(returnValue(1));

                oneOf(annotationDao).deleteAnnotation(3);
                will(returnValue(1));
                
                oneOf(TargetDao).getCachedRepresentations(2);
                will(returnValue(mockCachedIDs));
                
                oneOf(TargetDao).deleteTargetCachedRepresentation(2, 3);
                will(returnValue(1));
                
                oneOf(cachedRepresentationDao).deleteCachedRepresentation(3);
                will(returnValue(1));
                
                oneOf(TargetDao).deleteTarget(2);
                will(returnValue(1));

               
            }
        });
        int[] result = dbIntegrityService.deleteAnnotation(3);// the Target will be deleted because it is not referred by any annotation
        assertEquals(4, result.length);
        assertEquals(1, result[0]); // annotation 3 is deleted
        assertEquals(3, result[1]); // 3 rows in "annotation principal permissions are deleted"
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
}
