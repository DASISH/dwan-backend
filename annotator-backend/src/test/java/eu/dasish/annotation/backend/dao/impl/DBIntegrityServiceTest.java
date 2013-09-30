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
import eu.dasish.annotation.backend.dao.SourceDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.ResourceREF;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
import eu.dasish.annotation.schema.SourceList;
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
    "/spring-test-config/mockSourceDao.xml", "/spring-test-config/mockCachedRepresentationDao.xml",
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
    private SourceDao sourceDao;
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

        mockery.checking(new Expectations() {
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

        mockery.checking(new Expectations() {
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

        mockery.checking(new Expectations() {
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
        mockAnnotation.setURI(TestBackendConstants._TEST_SERVLET_URI +"/"+TestBackendConstants._TEST_ANNOT_2_EXT);
        mockAnnotation.setHeadline(TestBackendConstants._TEST_ANNOT_2_HEADLINE);
        XMLGregorianCalendar mockTimeStamp = Helpers.setXMLGregorianCalendar(Timestamp.valueOf("2013-08-12 11:25:00.383000"));
        mockAnnotation.setTimeStamp(mockTimeStamp);
        ResourceREF mockOwner = new ResourceREF();
        mockOwner.setRef("3");
        mockAnnotation.setOwner(mockOwner);

        AnnotationBody mockBody = new AnnotationBody();
        mockBody.setMimeType("text/plain");
        mockBody.setValue(TestBackendConstants._TEST_ANNOT_2_BODY);
        mockAnnotation.setBody(mockBody);
        mockAnnotation.setTargetSources(null);


        final List<Number> mockSourceIDs = new ArrayList<Number>();
        mockSourceIDs.add(1);
        mockSourceIDs.add(2);

        final Source mockSourceOne = new Source();
        mockSourceOne.setLink(TestBackendConstants._TEST_SOURCE_1_LINK);
        mockSourceOne.setURI(TestBackendConstants._TEST_SERVLET_URI +"/"+TestBackendConstants._TEST_SOURCE_1_EXT_ID);
        mockSourceOne.setVersion(TestBackendConstants._TEST_SOURCE_1_EXT_ID);

        final Source mockSourceTwo = new Source();
        mockSourceTwo.setLink(TestBackendConstants._TEST_SOURCE_2_LINK);
        mockSourceTwo.setURI(TestBackendConstants._TEST_SERVLET_URI +"/"+TestBackendConstants._TEST_SOURCE_2_EXT_ID);
        mockSourceTwo.setVersion(TestBackendConstants._TEST_SOURCE_2_EXT_ID);

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

        final String uri3 = TestBackendConstants._TEST_SERVLET_URI + "/"+ TestBackendConstants._TEST_USER_3_EXT_ID;
        final String uri4 = TestBackendConstants._TEST_SERVLET_URI + "/"+ TestBackendConstants._TEST_USER_4_EXT_ID;
        final String uri5 = TestBackendConstants._TEST_SERVLET_URI + "/"+ TestBackendConstants._TEST_USER_5_EXT_ID;


        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getAnnotationWithoutSourcesAndPermissions(2);
                will(returnValue(mockAnnotation));

                oneOf(userDao).getExternalID(3);
                will(returnValue(externalID3));

                oneOf(userDao).externalIDtoURI(TestBackendConstants._TEST_USER_3_EXT_ID);
                will(returnValue(uri3));
                
                oneOf(annotationDao).retrieveSourceIDs(2);
                will(returnValue(mockSourceIDs));

                oneOf(sourceDao).getSource(1);
                will(returnValue(mockSourceOne));

                oneOf(sourceDao).getSource(2);
                will(returnValue(mockSourceTwo));

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
        assertEquals(TestBackendConstants._TEST_SERVLET_URI+"/"+TestBackendConstants._TEST_ANNOT_2_EXT, result.getURI());
        assertEquals("text/plain", result.getBody().getMimeType());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_BODY, result.getBody().getValue());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_HEADLINE, result.getHeadline());
        assertEquals(TestBackendConstants._TEST_ANNOT_2_TIME_STAMP, result.getTimeStamp().toString());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI+"/"+TestBackendConstants._TEST_USER_3_EXT_ID, result.getOwner().getRef());

        assertEquals(mockSourceOne.getLink(), result.getTargetSources().getTargetSource().get(0).getLink());
        assertEquals(mockSourceOne.getURI(), result.getTargetSources().getTargetSource().get(0).getRef());
        assertEquals(mockSourceOne.getVersion(), result.getTargetSources().getTargetSource().get(0).getVersion());
        assertEquals(mockSourceTwo.getLink(), result.getTargetSources().getTargetSource().get(1).getLink());
        assertEquals(mockSourceTwo.getURI(), result.getTargetSources().getTargetSource().get(1).getRef());
        assertEquals(mockSourceTwo.getVersion(), result.getTargetSources().getTargetSource().get(1).getVersion());

        assertEquals(Permission.OWNER, result.getPermissions().getUser().get(0).getPermission());
        assertEquals(uri3, result.getPermissions().getUser().get(0).getRef());

        assertEquals(Permission.WRITER, result.getPermissions().getUser().get(1).getPermission());
        assertEquals(uri4, result.getPermissions().getUser().get(1).getRef());

        assertEquals(Permission.READER, result.getPermissions().getUser().get(2).getPermission());
        assertEquals(uri5, result.getPermissions().getUser().get(2).getRef());
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

    @Test
    public void testGetAnnotationSources() throws SQLException{
        System.out.println("test getAnnotationSources");
        final Number annotationID = 2;
        final List<Number> sourceIDs = new ArrayList<Number>();
        sourceIDs.add(1);
        sourceIDs.add(2);
        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).retrieveSourceIDs(annotationID);
                will(returnValue(sourceIDs));

                oneOf(sourceDao).getExternalID(1);
                will(returnValue(UUID.fromString(TestBackendConstants._TEST_SOURCE_1_EXT_ID)));

                oneOf(sourceDao).getExternalID(2);
                will(returnValue(UUID.fromString(TestBackendConstants._TEST_SOURCE_2_EXT_ID)));


            }
        });
       
        SourceList result = dbIntegrityService.getAnnotationSources(annotationID);
        assertEquals(2, result.getTargetSource().size());
        assertEquals(TestBackendConstants._TEST_SOURCE_1_EXT_ID, result.getTargetSource().get(0).getRef());
        assertEquals(TestBackendConstants._TEST_SOURCE_2_EXT_ID, result.getTargetSource().get(1).getRef());
        
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
    public void testGetFilteredAnnotationInfos(){
        System.out.println("test getetFilteredAnnotationInfos");
        
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

        final List<Number> mockAnnotIDs = new ArrayList<Number>();
        mockAnnotIDs.add(2);
        
        final AnnotationInfoList mockRetval = new AnnotationInfoList();
        AnnotationInfo annotationInfo = new AnnotationInfo(); 
        mockRetval.getAnnotation().add(annotationInfo);
        
        ResourceREF ownerAnnot = new ResourceREF();
        annotationInfo.setOwner(ownerAnnot);
        ownerAnnot.setRef(TestBackendConstants._TEST_SERVLET_URI + TestBackendConstants._TEST_USER_3_EXT_ID);
        
        SourceList targetSources = new SourceList();
        ResourceREF targetOne  = new ResourceREF();
        ResourceREF targetTwo  = new ResourceREF();
        targetSources.getTargetSource().add(targetOne);
        targetSources.getTargetSource().add(targetTwo);        
        annotationInfo.setTargetSources(targetSources);
        targetOne.setRef(TestBackendConstants._TEST_SERVLET_URI + TestBackendConstants._TEST_SOURCE_1_EXT_ID);
        targetTwo.setRef(TestBackendConstants._TEST_SERVLET_URI + TestBackendConstants._TEST_SOURCE_2_EXT_ID);
        
        annotationInfo.setHeadline(TestBackendConstants._TEST_ANNOT_2_HEADLINE);        
        annotationInfo.setRef(TestBackendConstants._TEST_SERVLET_URI + TestBackendConstants._TEST_ANNOT_2_EXT);
        

        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).getSourcesForLink(link);
                will(returnValue(mockSourceIDs));

                oneOf(annotationDao).retrieveAnnotationList(mockSourceIDs);
                will(returnValue(mockAnnotationIDs));

                oneOf(userDao).getInternalID(owner);
                will(returnValue(3));

                oneOf(annotationDao).getFilteredAnnotationIDs(mockAnnotationIDs, text, access, namespace, 3, after, before);
                will(returnValue(mockAnnotIDs));
                
                oneOf(annotationDao).getAnnotationInfos(mockAnnotIDs);
                will(returnValue(mockRetval.getAnnotation()));

            }
        });


        AnnotationInfoList result = dbIntegrityService.getFilteredAnnotationInfos(link, text, access, namespace, owner, after, before);
        assertEquals(1, result.getAnnotation().size()); 
        AnnotationInfo resultAnnotInfo = result.getAnnotation().get(0);
        assertEquals(annotationInfo.getHeadline(), resultAnnotInfo.getHeadline());
        assertEquals(annotationInfo.getOwner().getRef(), resultAnnotInfo.getOwner().getRef());
        assertEquals(annotationInfo.getRef(),result.getAnnotation().get(0).getRef() );
        assertEquals(annotationInfo.getTargetSources().getTargetSource().get(0).getRef(), resultAnnotInfo.getTargetSources().getTargetSource().get(0).getRef());
        assertEquals(annotationInfo.getTargetSources().getTargetSource().get(1).getRef(), resultAnnotInfo.getTargetSources().getTargetSource().get(1).getRef());
          
    }
    
    @Test
    public void testGetSourcesWithNoCachedRepresentation(){
        System.out.println("test getSourcesWithNoCachedRepresentation");
        final Number annotationID = 4;
        final List<Number> sourceIDs = new ArrayList<Number>();
        sourceIDs.add(5);
        sourceIDs.add(7);
        
        final List<Number> cachedIDs5 = new ArrayList<Number>();
        cachedIDs5.add(7);
        final List<Number> cachedIDs7 = new ArrayList<Number>();
        
       
        
        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).retrieveSourceIDs(annotationID);
                will(returnValue(sourceIDs));

                oneOf(sourceDao).getCachedRepresentations(5);
                will(returnValue(cachedIDs5));
                
                oneOf(sourceDao).getCachedRepresentations(7);
                will(returnValue(cachedIDs7));
                
            }
        });
        
        List<Number> result = dbIntegrityService.getSourcesWithNoCachedRepresentation(annotationID);
        assertEquals(1, result.size());
        assertEquals(7, result.get(0)); // source number 7 has no cached
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
        newCachedInfo.setRef(TestBackendConstants._TEST_SERVLET_URI + externalID);

        String blobString = "aaa";
        byte[] blobBytes = blobString.getBytes();
        final Blob newCachedBlob = new SerialBlob(blobBytes);
        final Number newCachedID = 8;
        final Number versionID = 1;
        mockery.checking(new Expectations() {
            {

                oneOf(cachedRepresentationDao).getInternalIDFromURI(newCachedInfo.getRef());
                will(returnValue(null));

                oneOf(cachedRepresentationDao).addCachedRepresentation(newCachedInfo, newCachedBlob);
                will(returnValue(newCachedID));

                one(sourceDao).addSourceCachedRepresentation(versionID, newCachedID);
                will(returnValue(1));

            }
        });


        Number[] result = dbIntegrityService.addCachedForSource(versionID, newCachedInfo, newCachedBlob);
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(newCachedID, result[1]);
    }

    /**
     * Test of updateSiblingSourceClassForSource method, of class
     * DBIntegrityServiceImlp.
     * 
  **/
    
    @Test
    public void testUpdateSiblingSourceClassForSource() throws Exception {
        System.out.println("test addSiblingVersionForSource ");

       
        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).getSourceSiblingClass(2);
                will(returnValue(2));

                oneOf(sourceDao).updateSiblingClass(1, 2);
                will(returnValue(1));

            }
        });

        
        assertEquals(1, dbIntegrityService.updateSiblingSourceClassForSource(1, 2));
    }

    /**
     * Test of addSourcesForAnnotation method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testAddSourcesForAnnotation() throws Exception {
        System.out.println("test addSourcesForAnnotation");

//        @Override
//        public Map<String, String> addSourcesForAnnotation(Number annotationID, List<SourceInfo> sources) throws SQLException {
//        Map<String, String> result = new HashMap<String, String>();
//        Number sourceIDRunner;
//        for (SourceInfo sourceInfo : sources) {
//            sourceIDRunner = sourceDao.getInternalIDFromURI(sourceInfo.getRef());
//            if (sourceIDRunner != null) {
//                int affectedRows = annotationDao.addAnnotationSource(annotationID, sourceIDRunner);
//            } else {
//                Source newSource = createFreshSource(sourceInfo);
//                Number sourceID = sourceDao.addSource(newSource);
//                String sourceTemporaryID = sourceDao.stringURItoExternalID(sourceInfo.getRef());
//                result.put(sourceTemporaryID, sourceDao.getExternalID(sourceID).toString());
//                int affectedRows = annotationDao.addAnnotationSource(annotationID, sourceID);
//            }
//        }
//        return result;
//    }
        
        // test 1: adding an existing source
        SourceInfo testSourceOne = new SourceInfo();
        testSourceOne.setLink(TestBackendConstants._TEST_SOURCE_1_LINK);
        testSourceOne.setRef(TestBackendConstants._TEST_SERVLET_URI + TestBackendConstants._TEST_SOURCE_1_EXT_ID);
        testSourceOne.setVersion(TestBackendConstants._TEST_SOURCE_2_VERSION );
        final List<SourceInfo> mockSourceListOne = new ArrayList<SourceInfo>();
        mockSourceListOne.add(testSourceOne);

        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).getInternalIDFromURI(mockSourceListOne.get(0).getRef());
                will(returnValue(1));

                oneOf(annotationDao).addAnnotationSource(1, 1);
                will(returnValue(1));
            }
        });

        Map<String, String> result = dbIntegrityService.addSourcesForAnnotation(1, mockSourceListOne);
        assertEquals(0, result.size());
        
        //        @Override
//        public Map<String, String> addSourcesForAnnotation(Number annotationID, List<SourceInfo> sources) throws SQLException {
//        Map<String, String> result = new HashMap<String, String>();
//        Number sourceIDRunner;
//        for (SourceInfo sourceInfo : sources) {
//            sourceIDRunner = sourceDao.getInternalIDFromURI(sourceInfo.getRef());
//            if (sourceIDRunner != null) {
//                int affectedRows = annotationDao.addAnnotationSource(annotationID, sourceIDRunner);
//            } else {
//                Source newSource = createFreshSource(sourceInfo);
//                Number sourceID = sourceDao.addSource(newSource);
//                String sourceTemporaryID = sourceDao.stringURItoExternalID(sourceInfo.getRef());
//                result.put(sourceTemporaryID, sourceDao.getExternalID(sourceID).toString());
//                int affectedRows = annotationDao.addAnnotationSource(annotationID, sourceID);
//            }
//        }
//        return result;
//    }
        

        // test 2: adding a new source
        SourceInfo testSourceTwo = new SourceInfo();
        final String tempSourceID = UUID.randomUUID().toString();
        testSourceTwo.setRef(TestBackendConstants._TEST_SERVLET_URI + tempSourceID);
        testSourceTwo.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
        testSourceTwo.setVersion("version 1.0");
        final List<SourceInfo> mockSourceListTwo = new ArrayList<SourceInfo>();
        mockSourceListTwo.add(testSourceTwo);

        final UUID mockNewSourceUUID = UUID.randomUUID();

        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).getInternalIDFromURI(mockSourceListTwo.get(0).getRef());
                will(returnValue(null));

                oneOf(sourceDao).addSource(with(aNonNull(Source.class)));
                will(returnValue(8)); //# the next new number is 8, we have already 7 sources

                oneOf(sourceDao).stringURItoExternalID(mockSourceListTwo.get(0).getRef());
                will(returnValue(tempSourceID));

                oneOf(sourceDao).getExternalID(8);
                will(returnValue(mockNewSourceUUID));

                oneOf(annotationDao).addAnnotationSource(1, 8);
                will(returnValue(1));

            }
        });

        Map<String, String> resultTwo = dbIntegrityService.addSourcesForAnnotation(1, mockSourceListTwo);
        assertEquals(1, resultTwo.size());
        assertEquals(mockNewSourceUUID.toString(), resultTwo.get(tempSourceID));

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
                oneOf(sourceDao).getInternalIDFromURI(with(aNonNull(String.class)));
                will(returnValue(1));

                oneOf(annotationDao).addAnnotationSource(6, 1);
                will(returnValue(1));

                ///////////

                oneOf(annotationDao).updateBodyText(6, testAnnotation.getBody().getValue());
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
        mockery.checking(new Expectations() {
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
        mockery.checking(new Expectations() {
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

        mockery.checking(new Expectations() {
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
    public void testDeleteCachedRepresentationForSource() throws SQLException{
        System.out.println("test deleteCachedRepresentationForSource");
        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).deleteSourceCachedRepresentation(5, 7);
                will(returnValue(1));

                oneOf(cachedRepresentationDao).deleteCachedRepresentation(7);
                will(returnValue(1)); // cached is used by another version

            }
        });

        int[] result = dbIntegrityService.deleteCachedRepresentationOfSource(5, 7);
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(1, result[1]);
    }

    /////////////////////////////////////////////
    @Test
    public void testDeleteAllCachedRepresentationsOfSource() throws SQLException{
        System.out.println("test deleteAllCachedRepresentationsOfSource");
        final List<Number> cachedList = new ArrayList<Number>();
        cachedList.add(1);
        cachedList.add(2);
        
        mockery.checking(new Expectations() {
            {
                oneOf(sourceDao).getCachedRepresentations(1);
                will(returnValue(cachedList));
                
                oneOf(sourceDao).deleteSourceCachedRepresentation(1, 1);
                will(returnValue(1));
                
                oneOf(cachedRepresentationDao).deleteCachedRepresentation(1);
                will(returnValue(1));
                
                oneOf(sourceDao).deleteSourceCachedRepresentation(1, 2);
                will(returnValue(1));
                
                 oneOf(cachedRepresentationDao).deleteCachedRepresentation(2);
                 will(returnValue(1));

            }
        });

        int[] result = dbIntegrityService.deleteAllCachedRepresentationsOfSource(1);
        assertEquals(2, result[0]); // # affected rows in sources_cacheds
        assertEquals(2, result[1]); // # affected rows in cacheds 
    }

    

    /**
     * Test of deleteAnnotationWithSources method, of class
     * DBIntegrityServiceImlp.
     */
    @Test
    public void testDeleteAnnotation() throws Exception {
        System.out.println("deleteAnnotation ");

        // deleting annotation 3, which has its target source 2  (used by annotation # 1)
        final List<Number> mockSourceIDs = new ArrayList<Number>();
        mockSourceIDs.add(2);
        
        final List<Number> mockCachedIDs = new ArrayList<Number>();
        mockCachedIDs.add(3);

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
                
                oneOf(sourceDao).getCachedRepresentations(2);
                will(returnValue(mockCachedIDs));
                
                oneOf(sourceDao).deleteSourceCachedRepresentation(2, 3);
                will(returnValue(1));
                
                oneOf(cachedRepresentationDao).deleteCachedRepresentation(3);
                will(returnValue(1));
                
                oneOf(sourceDao).deleteSource(2);
                will(returnValue(1));

               
            }
        });
        int[] result = dbIntegrityService.deleteAnnotation(3);// the source will be deleted because it is not referred by any annotation
        assertEquals(4, result.length);
        assertEquals(1, result[0]); // annotation 3 is deleted
        assertEquals(3, result[1]); // 3 rows in "annotation principal permissions are deleted"
        assertEquals(1, result[2]);  // row (3,2) in "annotations_sources" is deleted
        assertEquals(1, result[3]); //  source 3 is deleted 
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
