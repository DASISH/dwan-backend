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
import eu.dasish.annotation.backend.MatchMode;
import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.PrincipalCannotBeDeleted;
import eu.dasish.annotation.backend.PrincipalExists;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.CachedRepresentationDao;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.TargetDao;
import eu.dasish.annotation.backend.dao.PrincipalDao;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationBody.TextBody;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.NotebookInfoList;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.Target;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.Principal;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.PermissionList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    "/spring-test-config/mockPrincipalDao.xml", "/spring-test-config/mockTargetDao.xml", "/spring-test-config/mockCachedRepresentationDao.xml",
    "/spring-test-config/mockNotebookDao.xml",
    "/spring-config/dbDispatcher.xml"})
public class DBDispatcherTest {

    @Autowired
    private DBDispatcherImlp dbDispatcher;
    @Autowired
    private Mockery mockeryDao;
    @Autowired
    private PrincipalDao principalDao;
    @Autowired
    private CachedRepresentationDao cachedRepresentationDao;
    @Autowired
    private TargetDao targetDao;
    @Autowired
    private AnnotationDao annotationDao;
    @Autowired
    private NotebookDao notebookDao;
    TestInstances testInstances = new TestInstances("/api");

    public DBDispatcherTest() {        
    }
    
   

    ///////// GETTERS /////////////
    /**
     * Test of getAnnotationInternalIdentifier method, of class
     * DBIntegrityServiceImlp.
     */
    
    @Test
    public void testGetAnnotationInternalIdentifier() throws NotInDataBaseException {
        System.out.println("getAnnotationInternalIdentifier");
        
        final UUID externalID = UUID.fromString("00000000-0000-0000-0000-000000000021");
        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getInternalID(externalID);
                will(returnValue(1));
            }
        });
        assertEquals(1, dbDispatcher.getResourceInternalIdentifier(externalID, Resource.ANNOTATION));
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
        assertEquals("00000000-0000-0000-0000-000000000021", dbDispatcher.getResourceExternalIdentifier(1, Resource.ANNOTATION).toString());
    }

    /**
     * Test of getPrincipalInternalIdentifier method, of class
     * DBIntegrityServiceImlp.
     */
    @Test
    public void testGetPrincipalInternalIdentifier() throws NotInDataBaseException {
        System.out.println("getPrincipalInternalIdentifier");
        
        final UUID externalID = UUID.fromString("00000000-0000-0000-0000-000000000111");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(principalDao).getInternalID(externalID);
                will(returnValue(1));
            }
        });
        assertEquals(1, dbDispatcher.getResourceInternalIdentifier(externalID, Resource.PRINCIPAL));
    }

    /**
     * Test of getPrincipalExternalIdentifier method, of class
     * DBIntegrityServiceImlp.
     */
    @Test
    public void testGetPrincipalExternalIdentifier() {
        System.out.println("getPrincipalExternalIdentifier");
        final UUID externalID = UUID.fromString("00000000-0000-0000-0000-000000000111");
        
        mockeryDao.checking(new Expectations() {
            {
                oneOf(principalDao).getExternalID(1);
                will(returnValue(externalID));
            }
        });
        assertEquals("00000000-0000-0000-0000-000000000111", dbDispatcher.getResourceExternalIdentifier(1, Resource.PRINCIPAL).toString());
    }

    /**
     * Test of getAnnotation method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testGetAnnotation() throws Exception {
        System.out.println("test getAnnotation");
        
        final Annotation mockAnnotation = new Annotation();// corresponds to the annotation # 1
        mockAnnotation.setHref("/api/annotations/00000000-0000-0000-0000-000000000021");
        mockAnnotation.setId("00000000-0000-0000-0000-000000000021");
        mockAnnotation.setHeadline("Sagrada Famiglia");
        XMLGregorianCalendar mockTimeStamp = DatatypeFactory.newInstance().newXMLGregorianCalendar("2013-08-12T09:25:00.383000Z");
        mockAnnotation.setLastModified(mockTimeStamp);
        mockAnnotation.setOwnerHref("/api/principals/00000000-0000-0000-0000-000000000111");

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
        mockTargetOne.setId("00000000-0000-0000-0000-000000000031");
        mockTargetOne.setHref("/api/targets/00000000-0000-0000-0000-000000000031");
        mockTargetOne.setVersion("version 1.0");

        final Target mockTargetTwo = new Target();
        mockTargetTwo.setLink("http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD");
        mockTargetTwo.setId("00000000-0000-0000-0000-000000000032");
        mockTargetTwo.setHref("/api/targets/00000000-0000-0000-0000-000000000032");
        mockTargetTwo.setVersion("version 1.1");

        final List<Map<Number, String>> listMap = new ArrayList<Map<Number, String>>();
        Map<Number, String> map2 = new HashMap<Number, String>();
        map2.put(2, "write");
        listMap.add(map2);
        Map<Number, String> map3 = new HashMap<Number, String>();
        map3.put(3, "read");
        listMap.add(map3);
        Map<Number, String> map4 = new HashMap<Number, String>();
        map4.put(11, "read");
        listMap.add(map4);

        final String uri1 = "/api/principals/00000000-0000-0000-0000-000000000111";
        final String uri2 = "/api/principals/00000000-0000-0000-0000-000000000112";
        final String uri3 = "/api/principals/00000000-0000-0000-0000-000000000113";
        final String uri4 = "/api/principals/00000000-0000-0000-0000-000000000221";


        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getAnnotationWithoutTargetsAndPemissions(1);
                will(returnValue(mockAnnotation));

                oneOf(annotationDao).getOwner(1);
                will(returnValue(1));

                oneOf(principalDao).getHrefFromInternalID(1);
                will(returnValue(uri1));

                oneOf(targetDao).getTargetIDs(1);
                will(returnValue(mockTargetIDs));

                oneOf(targetDao).getTarget(1);
                will(returnValue(mockTargetOne));

                oneOf(targetDao).getTarget(2);
                will(returnValue(mockTargetTwo));

                /// getPermissionsForAnnotation

                oneOf(annotationDao).getPermissions(1);
                will(returnValue(listMap));

                oneOf(annotationDao).getPublicAttribute(1);
                will(returnValue(Access.WRITE));

                oneOf(principalDao).getHrefFromInternalID(2);
                will(returnValue(uri2));

                oneOf(principalDao).getHrefFromInternalID(3);
                will(returnValue(uri3));

                oneOf(principalDao).getHrefFromInternalID(11);
                will(returnValue(uri4));
            }
        });

        Annotation result = dbDispatcher.getAnnotation(1);
        assertEquals("00000000-0000-0000-0000-000000000021", result.getId());
        assertEquals("/api/annotations/00000000-0000-0000-0000-000000000021", result.getHref());
        assertEquals("text/plain", result.getBody().getTextBody().getMimeType());
        assertEquals("<html><body>some html 1</body></html>", result.getBody().getTextBody().getBody());
        assertEquals("Sagrada Famiglia", result.getHeadline());
        assertEquals("2013-08-12T09:25:00.383000Z", result.getLastModified().toString());
        assertEquals("/api/principals/00000000-0000-0000-0000-000000000111", result.getOwnerHref());

        assertEquals(mockTargetOne.getLink(), result.getTargets().getTargetInfo().get(0).getLink());
        assertEquals(mockTargetOne.getHref(), result.getTargets().getTargetInfo().get(0).getHref());
        assertEquals(mockTargetOne.getVersion(), result.getTargets().getTargetInfo().get(0).getVersion());
        assertEquals(mockTargetTwo.getLink(), result.getTargets().getTargetInfo().get(1).getLink());
        assertEquals(mockTargetTwo.getHref(), result.getTargets().getTargetInfo().get(1).getHref());
        assertEquals(mockTargetTwo.getVersion(), result.getTargets().getTargetInfo().get(1).getVersion());

        assertEquals(3, result.getPermissions().getPermission().size());

        assertEquals(Access.WRITE, result.getPermissions().getPermission().get(0).getLevel());
        assertEquals(uri2, result.getPermissions().getPermission().get(0).getPrincipalHref());

        assertEquals(Access.READ, result.getPermissions().getPermission().get(1).getLevel());
        assertEquals(uri3, result.getPermissions().getPermission().get(1).getPrincipalHref());

        assertEquals(Access.READ, result.getPermissions().getPermission().get(2).getLevel());
        assertEquals(uri4, result.getPermissions().getPermission().get(2).getPrincipalHref());

        assertEquals(Access.WRITE, result.getPermissions().getPublic());
    }

    /**
     * Test of getFilteredAnnotationIDs method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testGetFilteredAnnotationIDsContains() throws NotInDataBaseException {
        System.out.println("test getFilteredAnnotationIDs");
        
        final List<Number> mockAnnotationIDs1 = new ArrayList<Number>();
        mockAnnotationIDs1.add(1);
     
        final List<Number> mockAnnotationIDsOwned = new ArrayList<Number>();
        //mockAnnotationIDsOwned.add(3);

        final List<Number> mockAnnotationIDsRead = new ArrayList<Number>();
        mockAnnotationIDsRead.add(1);
        mockAnnotationIDsRead.add(2);
        

        final List<Number> mockAnnotationIDsPublicRead = new ArrayList<Number>();
        mockAnnotationIDsPublicRead.add(1);
        mockAnnotationIDsPublicRead.add(2);
        
        final List<Number> mockTargets1 = new ArrayList<Number>();
        mockTargets1.add(1);
        mockTargets1.add(2);
        
        
        final String link1 = "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia";
        final String link2 = "http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD";

        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();

        final Number loggedIn = 3;

        mockeryDao.checking(new Expectations() {
            {
               

                oneOf(annotationDao).getFilteredAnnotationIDs(null, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));

                oneOf(annotationDao).getAnnotationIDsPermissionAtLeast(loggedIn, Access.READ);
                will(returnValue(mockAnnotationIDsRead));

                oneOf(annotationDao).getAnnotationIDsPublicAtLeast(Access.READ);
                will(returnValue(mockAnnotationIDsPublicRead));               

                oneOf(annotationDao).getFilteredAnnotationIDs(loggedIn, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDsOwned));
                
                oneOf(targetDao).getTargetIDs(1);
                will(returnValue(mockTargets1));
                
                oneOf(targetDao).getLink(1);
                will(returnValue(link1));
                
                oneOf(targetDao).getLink(2);
                will(returnValue(link2));
              

            }
        });


        List resultContains = dbDispatcher.getFilteredAnnotationIDs(null, "Sagrada_", MatchMode.CONTAINS, "some html 1", 3, "read", null, after, before);
        assertEquals(1, resultContains.size());
        assertEquals(1, resultContains.get(0));
        
        
        
    }
    
    
    /**
     * Test of getFilteredAnnotationIDs method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testGetFilteredAnnotationIDsExact() throws NotInDataBaseException {
        System.out.println("test getFilteredAnnotationIDs");
        
        final List<Number> mockAnnotationIDs1 = new ArrayList<Number>();
        mockAnnotationIDs1.add(1);
     
        final List<Number> mockAnnotationIDsOwned = new ArrayList<Number>();
        //mockAnnotationIDsOwned.add(3);

        final List<Number> mockAnnotationIDsRead = new ArrayList<Number>();
        mockAnnotationIDsRead.add(1);
        mockAnnotationIDsRead.add(2);
        

        final List<Number> mockAnnotationIDsPublicRead = new ArrayList<Number>();
        mockAnnotationIDsPublicRead.add(1);
        mockAnnotationIDsPublicRead.add(2);
        
        final List<Number> mockTargets1 = new ArrayList<Number>();
        mockTargets1.add(1);
        mockTargets1.add(2);
        
        
        final String link1 = "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia";
        final String link2 = "http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD";

        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();

        final Number loggedIn = 3;

        mockeryDao.checking(new Expectations() {
            {
               

                oneOf(annotationDao).getFilteredAnnotationIDs(null, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));

                oneOf(annotationDao).getAnnotationIDsPermissionAtLeast(loggedIn, Access.READ);
                will(returnValue(mockAnnotationIDsRead));

                oneOf(annotationDao).getAnnotationIDsPublicAtLeast(Access.READ);
                will(returnValue(mockAnnotationIDsPublicRead));               

                oneOf(annotationDao).getFilteredAnnotationIDs(loggedIn, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDsOwned));
                
                oneOf(targetDao).getTargetIDs(1);
                will(returnValue(mockTargets1));
                
                oneOf(targetDao).getLink(1);
                will(returnValue(link1));
                
                oneOf(targetDao).getLink(2);
                will(returnValue(link2));
              

            }
        });


        
        List resultExact = dbDispatcher.getFilteredAnnotationIDs(null, "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia", MatchMode.EXACT, "some html 1", 3, "read", null, after, before);
        assertEquals(1, resultExact.size());
        assertEquals(1, resultExact.get(0));
        
      
    }
    
    /**
     * Test of getFilteredAnnotationIDs method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testGetFilteredAnnotationIDsStartsWith() throws NotInDataBaseException {
        System.out.println("test getFilteredAnnotationIDs");
        
        final List<Number> mockAnnotationIDs1 = new ArrayList<Number>();
        mockAnnotationIDs1.add(1);
     
        final List<Number> mockAnnotationIDsOwned = new ArrayList<Number>();
        //mockAnnotationIDsOwned.add(3);

        final List<Number> mockAnnotationIDsRead = new ArrayList<Number>();
        mockAnnotationIDsRead.add(1);
        mockAnnotationIDsRead.add(2);
        

        final List<Number> mockAnnotationIDsPublicRead = new ArrayList<Number>();
        mockAnnotationIDsPublicRead.add(1);
        mockAnnotationIDsPublicRead.add(2);
        
        final List<Number> mockTargets1 = new ArrayList<Number>();
        mockTargets1.add(1);
        mockTargets1.add(2);
        
        
        final String link1 = "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia";
        final String link2 = "http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD";

        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();

        final Number loggedIn = 3;

        mockeryDao.checking(new Expectations() {
            {
               

                oneOf(annotationDao).getFilteredAnnotationIDs(null, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));

                oneOf(annotationDao).getAnnotationIDsPermissionAtLeast(loggedIn, Access.READ);
                will(returnValue(mockAnnotationIDsRead));

                oneOf(annotationDao).getAnnotationIDsPublicAtLeast(Access.READ);
                will(returnValue(mockAnnotationIDsPublicRead));               

                oneOf(annotationDao).getFilteredAnnotationIDs(loggedIn, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDsOwned));
                
                oneOf(targetDao).getTargetIDs(1);
                will(returnValue(mockTargets1));
                
                oneOf(targetDao).getLink(1);
                will(returnValue(link1));
                
                oneOf(targetDao).getLink(2);
                will(returnValue(link2));
              

            }
        });


        
        List resultStartsWith = dbDispatcher.getFilteredAnnotationIDs(null, "http://nl.wikipedia.org/wiki/Sagrada_", MatchMode.STARTS_WITH, "some html 1", 3, "read", null, after, before);
        assertEquals(1, resultStartsWith.size());
        assertEquals(1, resultStartsWith.get(0));
        
        
        
    }
    
    /**
     * Test of getFilteredAnnotationIDs method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testGetFilteredAnnotationIDsEndsWith() throws NotInDataBaseException {
        System.out.println("test getFilteredAnnotationIDs");
        
        final List<Number> mockAnnotationIDs1 = new ArrayList<Number>();
        mockAnnotationIDs1.add(1);
     
        final List<Number> mockAnnotationIDsOwned = new ArrayList<Number>();
        //mockAnnotationIDsOwned.add(3);

        final List<Number> mockAnnotationIDsRead = new ArrayList<Number>();
        mockAnnotationIDsRead.add(1);
        mockAnnotationIDsRead.add(2);
        

        final List<Number> mockAnnotationIDsPublicRead = new ArrayList<Number>();
        mockAnnotationIDsPublicRead.add(1);
        mockAnnotationIDsPublicRead.add(2);
        
        final List<Number> mockTargets1 = new ArrayList<Number>();
        mockTargets1.add(1);
        mockTargets1.add(2);
        
        
        final String link1 = "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia";
        final String link2 = "http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD";

        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();

        final Number loggedIn = 3;

        mockeryDao.checking(new Expectations() {
            {
               

                oneOf(annotationDao).getFilteredAnnotationIDs(null, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));

                oneOf(annotationDao).getAnnotationIDsPermissionAtLeast(loggedIn, Access.READ);
                will(returnValue(mockAnnotationIDsRead));

                oneOf(annotationDao).getAnnotationIDsPublicAtLeast(Access.READ);
                will(returnValue(mockAnnotationIDsPublicRead));               

                oneOf(annotationDao).getFilteredAnnotationIDs(loggedIn, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDsOwned));
                
                oneOf(targetDao).getTargetIDs(1);
                will(returnValue(mockTargets1));
                
                oneOf(targetDao).getLink(1);
                will(returnValue(link1));
                
                oneOf(targetDao).getLink(2);
                will(returnValue(link2));
              

            }
        });

        
        List resultEndsWith = dbDispatcher.getFilteredAnnotationIDs(null, "Fam%C3%ADlia", MatchMode.ENDS_WITH, "some html 1", 3, "read", null, after, before);
        assertEquals(1, resultEndsWith.size());
        assertEquals(1, resultEndsWith.get(0));
        
    }

   
    @Test
    public void testGetFilteredAnnotationIDs2Contains() throws NotInDataBaseException {
        System.out.println("test getFilteredAnnotationIDs");
       
        final List<Number> mockAnnotationIDs1 = new ArrayList<Number>();
        mockAnnotationIDs1.add(1);

     
        final List<Number> mockAnnotationIDsOwned = new ArrayList<Number>();
        //mockAnnotationIDsOwned.add(3);

        final List<Number> mockAnnotationIDsWrite = new ArrayList<Number>();
        mockAnnotationIDsWrite.add(2);
        

        final List<Number> mockAnnotationIDsPublicWrite = new ArrayList<Number>();
        mockAnnotationIDsPublicWrite.add(1);
        
        final List<Number> mockTargets1 = new ArrayList<Number>();
        mockTargets1.add(1);
        mockTargets1.add(2);
        
       
        final String link1 = "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia";
        final String link2 = "http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD";

        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();

        final Number loggedIn = 3;

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getFilteredAnnotationIDs(null, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));

                oneOf(annotationDao).getAnnotationIDsPermissionAtLeast(loggedIn, Access.WRITE);
                will(returnValue(mockAnnotationIDsWrite));

                oneOf(annotationDao).getAnnotationIDsPublicAtLeast(Access.WRITE);
                will(returnValue(mockAnnotationIDsPublicWrite));               

                oneOf(annotationDao).getFilteredAnnotationIDs(loggedIn, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDsOwned));
                
                oneOf(targetDao).getTargetIDs(1);
                will(returnValue(mockTargets1));
                
                oneOf(targetDao).getLink(1);
                will(returnValue(link1));
                
                oneOf(targetDao).getLink(2);
                will(returnValue(link2));
                
            }
        });


        
        List resultContains = dbDispatcher.getFilteredAnnotationIDs(null, "Sagrada_", MatchMode.CONTAINS, "some html 1", 3, "write", null, after, before);
        assertEquals(1, resultContains.size());
        assertEquals(1, resultContains.get(0));
    }
    
    @Test
    public void testGetFilteredAnnotationIDs2Exact() throws NotInDataBaseException {
        System.out.println("test getFilteredAnnotationIDs");
       
        final List<Number> mockAnnotationIDs1 = new ArrayList<Number>();
        mockAnnotationIDs1.add(1);

     
        final List<Number> mockAnnotationIDsOwned = new ArrayList<Number>();
        //mockAnnotationIDsOwned.add(3);

        final List<Number> mockAnnotationIDsWrite = new ArrayList<Number>();
        mockAnnotationIDsWrite.add(2);
        

        final List<Number> mockAnnotationIDsPublicWrite = new ArrayList<Number>();
        mockAnnotationIDsPublicWrite.add(1);
        
        final List<Number> mockTargets1 = new ArrayList<Number>();
        mockTargets1.add(1);
        mockTargets1.add(2);
        
       
        final String link1 = "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia";
        final String link2 = "http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD";

        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();

        final Number loggedIn = 3;

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getFilteredAnnotationIDs(null, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));

                oneOf(annotationDao).getAnnotationIDsPermissionAtLeast(loggedIn, Access.WRITE);
                will(returnValue(mockAnnotationIDsWrite));

                oneOf(annotationDao).getAnnotationIDsPublicAtLeast(Access.WRITE);
                will(returnValue(mockAnnotationIDsPublicWrite));               

                oneOf(annotationDao).getFilteredAnnotationIDs(loggedIn, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDsOwned));
                
                oneOf(targetDao).getTargetIDs(1);
                will(returnValue(mockTargets1));
                
                oneOf(targetDao).getLink(1);
                will(returnValue(link1));
                
                oneOf(targetDao).getLink(2);
                will(returnValue(link2));
                
            }
        });


        
        List resultExact = dbDispatcher.getFilteredAnnotationIDs(null, "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia", MatchMode.EXACT, "some html 1", 3, "write", null, after, before);
        assertEquals(1, resultExact.size());
        assertEquals(1, resultExact.get(0));
        
       
    }
    
    @Test
    public void testGetFilteredAnnotationIDs2StartsWith() throws NotInDataBaseException {
        System.out.println("test getFilteredAnnotationIDs");
       
        final List<Number> mockAnnotationIDs1 = new ArrayList<Number>();
        mockAnnotationIDs1.add(1);

     
        final List<Number> mockAnnotationIDsOwned = new ArrayList<Number>();
        //mockAnnotationIDsOwned.add(3);

        final List<Number> mockAnnotationIDsWrite = new ArrayList<Number>();
        mockAnnotationIDsWrite.add(2);
        

        final List<Number> mockAnnotationIDsPublicWrite = new ArrayList<Number>();
        mockAnnotationIDsPublicWrite.add(1);
        
        final List<Number> mockTargets1 = new ArrayList<Number>();
        mockTargets1.add(1);
        mockTargets1.add(2);
        
       
        final String link1 = "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia";
        final String link2 = "http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD";

        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();

        final Number loggedIn = 3;

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getFilteredAnnotationIDs(null, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));

                oneOf(annotationDao).getAnnotationIDsPermissionAtLeast(loggedIn, Access.WRITE);
                will(returnValue(mockAnnotationIDsWrite));

                oneOf(annotationDao).getAnnotationIDsPublicAtLeast(Access.WRITE);
                will(returnValue(mockAnnotationIDsPublicWrite));               

                oneOf(annotationDao).getFilteredAnnotationIDs(loggedIn, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDsOwned));
                
                oneOf(targetDao).getTargetIDs(1);
                will(returnValue(mockTargets1));
                
                oneOf(targetDao).getLink(1);
                will(returnValue(link1));
                
                oneOf(targetDao).getLink(2);
                will(returnValue(link2));
                
            }
        });


        List resultStartsWith = dbDispatcher.getFilteredAnnotationIDs(null, "http://nl.wikipedia.org/wiki/Sagrada_", MatchMode.STARTS_WITH, "some html 1", 3, "write", null, after, before);
        assertEquals(1, resultStartsWith.size());
        assertEquals(1, resultStartsWith.get(0));
        
       
    }
    
    @Test
    public void testGetFilteredAnnotationIDs2EndsWith() throws NotInDataBaseException {
        System.out.println("test getFilteredAnnotationIDs");
       
        final List<Number> mockAnnotationIDs1 = new ArrayList<Number>();
        mockAnnotationIDs1.add(1);

     
        final List<Number> mockAnnotationIDsOwned = new ArrayList<Number>();
        //mockAnnotationIDsOwned.add(3);

        final List<Number> mockAnnotationIDsWrite = new ArrayList<Number>();
        mockAnnotationIDsWrite.add(2);
        

        final List<Number> mockAnnotationIDsPublicWrite = new ArrayList<Number>();
        mockAnnotationIDsPublicWrite.add(1);
        
        final List<Number> mockTargets1 = new ArrayList<Number>();
        mockTargets1.add(1);
        mockTargets1.add(2);
        
       
        final String link1 = "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia";
        final String link2 = "http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD";

        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();

        final Number loggedIn = 3;

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getFilteredAnnotationIDs(null, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));

                oneOf(annotationDao).getAnnotationIDsPermissionAtLeast(loggedIn, Access.WRITE);
                will(returnValue(mockAnnotationIDsWrite));

                oneOf(annotationDao).getAnnotationIDsPublicAtLeast(Access.WRITE);
                will(returnValue(mockAnnotationIDsPublicWrite));               

                oneOf(annotationDao).getFilteredAnnotationIDs(loggedIn, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDsOwned));
                
                oneOf(targetDao).getTargetIDs(1);
                will(returnValue(mockTargets1));
                
                oneOf(targetDao).getLink(1);
                will(returnValue(link1));
                
                oneOf(targetDao).getLink(2);
                will(returnValue(link2));
                
            }
        });


        
        List resultEndsWith = dbDispatcher.getFilteredAnnotationIDs(null, "Fam%C3%ADlia", MatchMode.ENDS_WITH, "some html 1", 3, "write", null, after, before);
        assertEquals(1, resultEndsWith.size());
        assertEquals(1, resultEndsWith.get(0));

    }

    @Test
    public void testGetFilteredAnnotationIDs3() throws NotInDataBaseException {
        System.out.println("test getFilteredAnnotationIDs");
        
        final List<Number> mockAnnotationIDs1 = new ArrayList<Number>();
        
        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();

       
        mockeryDao.checking(new Expectations() {
            {               

                oneOf(annotationDao).getFilteredAnnotationIDs(3, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));
                
                oneOf(annotationDao).getFilteredAnnotationIDs(3, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));
                
                oneOf(annotationDao).getFilteredAnnotationIDs(3, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));
                
                oneOf(annotationDao).getFilteredAnnotationIDs(3, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));

            }
        });


        List resultContains = dbDispatcher.getFilteredAnnotationIDs(null, "Sagrada_", MatchMode.CONTAINS, "some html 1", 3, "owner", null, after, before);
        assertEquals(0, resultContains.size());
        
        List resultExact = dbDispatcher.getFilteredAnnotationIDs(null, "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia", MatchMode.EXACT, "some html 1", 3, "owner", null, after, before);
        assertEquals(0, resultExact.size());
        
        List resultStartsWith = dbDispatcher.getFilteredAnnotationIDs(null, "http://nl.wikipedia.org/wiki/Sagrada_", MatchMode.STARTS_WITH, "some html 1", 3, "owner", null, after, before);
        assertEquals(0, resultStartsWith.size());
        
        List resultEndsWith = dbDispatcher.getFilteredAnnotationIDs(null, "Fam%C3%ADlia", MatchMode.ENDS_WITH, "some html 1", 3, "owner", null, after, before);
        assertEquals(0, resultEndsWith.size());
    }

    
    @Test
    public void testGetFilteredAnnotationIDs4() throws NotInDataBaseException {
        System.out.println("test getFilteredAnnotationIDs");
        
        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();

//        final List<Number> mockRetval = new ArrayList<Number>();
//        mockRetval.add(1);

        final Number loggedIn = 3;

        mockeryDao.checking(new Expectations() {
            {

                oneOf(principalDao).getExternalID(loggedIn);
                will(returnValue(UUID.fromString("00000000-0000-0000-0000-000000000113")));


            }
        });


        List result = dbDispatcher.getFilteredAnnotationIDs(UUID.fromString("00000000-0000-0000-0000-000000000111"), "nl.wikipedia.org", MatchMode.CONTAINS, "some html 1", 3, "owner", null, after, before);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetAnnotationTargets() throws SQLException {
        System.out.println("test getAnnotationTargets");
        
        final List<Number> targetIDs = new ArrayList<Number>();
        targetIDs.add(1);
        targetIDs.add(2);
        mockeryDao.checking(new Expectations() {
            {
                oneOf(targetDao).getTargetIDs(1);
                will(returnValue(targetIDs));

                oneOf(targetDao).getHrefFromInternalID(1);
                will(returnValue("/api/targets/00000000-0000-0000-0000-000000000031"));

                oneOf(targetDao).getHrefFromInternalID(2);
                will(returnValue("/api/targets/00000000-0000-0000-0000-000000000032"));

            }
        });

        ReferenceList result = dbDispatcher.getAnnotationTargets(1);
        assertEquals(2, result.getHref().size());
        assertEquals("/api/targets/00000000-0000-0000-0000-000000000031", result.getHref().get(0));
        assertEquals("/api/targets/00000000-0000-0000-0000-000000000032", result.getHref().get(1));

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
    public void testGetFilteredAnnotationInfos() throws NotInDataBaseException {
        System.out.println("test getetFilteredAnnotationInfos");
        
        final List<Number> mockAnnotationIDs1 = new ArrayList<Number>();
        mockAnnotationIDs1.add(1);

       

        final UUID ownerUUID = UUID.fromString("00000000-0000-0000-0000-000000000111");
        final String after = (new Timestamp(0)).toString();
        final String before = (new Timestamp(System.currentTimeMillis())).toString();



        final List<Number> mockAnnotationIDsOwned = new ArrayList<Number>();
        //mockAnnotationIDsOwned.add(3);

        final List<Number> mockAnnotationIDsRead = new ArrayList<Number>();
        mockAnnotationIDsRead.add(1);
        mockAnnotationIDsRead.add(2);
       
        final List<Number> mockAnnotationIDsPublicRead = new ArrayList<Number>();
        mockAnnotationIDsPublicRead.add(1);
        mockAnnotationIDsPublicRead.add(2);

        
       
        
        final List<Number> mockTargets1 = new ArrayList<Number>();
        mockTargets1.add(1);
        mockTargets1.add(2);
        
        
        final String link1 = "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia";
        final String link2 = "http://nl.wikipedia.org/wiki/Antoni_Gaud%C3%AD";
        

        final AnnotationInfo mockAnnotInfo = new AnnotationInfo();

        mockAnnotInfo.setHeadline("Sagrada Famiglia");
        mockAnnotInfo.setHref("/api/annotations/00000000-0000-0000-0000-000000000021");
        mockAnnotInfo.setOwnerHref("/api/principals/00000000-0000-0000-0000-000000000111");

        final List<Number> targetIDs = new ArrayList<Number>();
        targetIDs.add(1);
        targetIDs.add(2);

        final Number loggedIn = 3;

        mockeryDao.checking(new Expectations() {
            {
                oneOf(principalDao).getInternalID(ownerUUID);
                will(returnValue(1));

             
                oneOf(annotationDao).getFilteredAnnotationIDs(1, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDs1));

                oneOf(annotationDao).getAnnotationIDsPermissionAtLeast(loggedIn, Access.READ);
                will(returnValue(mockAnnotationIDsRead));

                oneOf(annotationDao).getAnnotationIDsPublicAtLeast(Access.READ);
                will(returnValue(mockAnnotationIDsPublicRead));
              

                oneOf(annotationDao).getFilteredAnnotationIDs(3, "some html 1", null, after, before);
                will(returnValue(mockAnnotationIDsOwned));

                oneOf(targetDao).getTargetIDs(1);
                will(returnValue(mockTargets1));
                
                oneOf(targetDao).getLink(1);
                will(returnValue(link1));
                
                oneOf(targetDao).getLink(2);
                will(returnValue(link2));

//                ///////////////////////////////////
//                
                oneOf(annotationDao).getAnnotationInfoWithoutTargetsAndOwner(1);
                will(returnValue(mockAnnotInfo));

                oneOf(annotationDao).getOwner(1);
                will(returnValue(1));

                oneOf(principalDao).getHrefFromInternalID(1);
                will(returnValue("/api/principals/00000000-0000-0000-0000-000000000111"));

                ////
                oneOf(targetDao).getTargetIDs(1);
                will(returnValue(targetIDs));

                oneOf(targetDao).getHrefFromInternalID(1);
                will(returnValue("/api/targets/00000000-0000-0000-0000-000000000031"));


                oneOf(targetDao).getHrefFromInternalID(2);
                will(returnValue("/api/targets/00000000-0000-0000-0000-000000000032"));


            }
        });


        AnnotationInfoList result = dbDispatcher.getFilteredAnnotationInfos(ownerUUID, "http://nl.wikipedia.org/wiki/Sagrada_Fam%C3%ADlia", MatchMode.EXACT, "some html 1", 3, "read", null, after, before);
        assertEquals(1, result.getAnnotationInfo().size());
        AnnotationInfo resultAnnotInfo = result.getAnnotationInfo().get(0);
        assertEquals("Sagrada Famiglia", resultAnnotInfo.getHeadline());
        assertEquals("/api/principals/00000000-0000-0000-0000-000000000111", resultAnnotInfo.getOwnerHref());
        assertEquals("/api/annotations/00000000-0000-0000-0000-000000000021", result.getAnnotationInfo().get(0).getHref());
        assertEquals("/api/targets/00000000-0000-0000-0000-000000000031", resultAnnotInfo.getTargets().getHref().get(0));
        assertEquals("/api/targets/00000000-0000-0000-0000-000000000032", resultAnnotInfo.getTargets().getHref().get(1));

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
                oneOf(targetDao).getTargetIDs(3);
                will(returnValue(targetIDs));

                oneOf(cachedRepresentationDao).getCachedRepresentationsForTarget(5);
                will(returnValue(cachedIDs5));

                oneOf(cachedRepresentationDao).getCachedRepresentationsForTarget(7);
                will(returnValue(cachedIDs7));

                oneOf(targetDao).getHrefFromInternalID(7);
                will(returnValue("/api/targets/00000000-0000-0000-0000-000000000037"));

            }
        });

        List<String> result = dbDispatcher.getTargetsWithNoCachedRepresentation(3);
        assertEquals(1, result.size());
        assertEquals("/api/targets/00000000-0000-0000-0000-000000000037", result.get(0)); // Target number 7 has no cached
    }

    ////////////// ADDERS /////////////////////////
    /**
     * Test of addCachedForVersion method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testAddCached() throws SerialException, IOException, NotInDataBaseException {
        System.out.println("test addCached");
        
        String mime = "text/html";
        String type = "text";
        String tool = "latex";
        String externalID = Helpers.generateUUID().toString();
        final CachedRepresentationInfo newCachedInfo = new CachedRepresentationInfo();
        newCachedInfo.setMimeType(mime);
        newCachedInfo.setType(type);
        newCachedInfo.setTool(tool);
        newCachedInfo.setHref("/api/cached/" + externalID);
        newCachedInfo.setId(externalID);
        
        String blobString = "aaa";
        byte[] blobBytes = blobString.getBytes();
        final ByteArrayInputStream newCachedBlob = new ByteArrayInputStream(blobBytes);

        mockeryDao.checking(new Expectations() {
            {

                oneOf(cachedRepresentationDao).addCachedRepresentation(newCachedInfo, newCachedBlob);
                will(returnValue(8));

                one(targetDao).addTargetCachedRepresentation(1, 8, "#(1,2)");
                will(returnValue(1));

            }
        });


        Number[] result = dbDispatcher.addCachedForTarget(1, "#(1,2)", newCachedInfo, newCachedBlob);
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
        testTargetOne.setHref("/api/targets/00000000-0000-0000-0000-000000000031");
        testTargetOne.setVersion("version 1.0");
        final List<TargetInfo> mockTargetListOne = new ArrayList<TargetInfo>();
        mockTargetListOne.add(testTargetOne);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(targetDao).getInternalIDFromHref(mockTargetListOne.get(0).getHref());
                will(returnValue(1));

                oneOf(annotationDao).addAnnotationTarget(4, 1);
                will(returnValue(1));
            }
        });

        Map<String, String> result = dbDispatcher.addTargetsForAnnotation(4, mockTargetListOne);
        assertEquals(0, result.size());

        // test 2: adding a new Target
        TargetInfo testTargetTwo = new TargetInfo();
        final String tempTargetID = "/api/targets/"+Helpers.generateUUID().toString();
        testTargetTwo.setHref(tempTargetID);
        testTargetTwo.setLink("http://www.sagradafamilia.cat/docs_instit/historia.php");
        testTargetTwo.setVersion("version 1.0");
        final List<TargetInfo> mockTargetListTwo = new ArrayList<TargetInfo>();
        mockTargetListTwo.add(testTargetTwo);

        final UUID mockNewTargetUUID = Helpers.generateUUID();
        final NotInDataBaseException e = new NotInDataBaseException("target", "external ID", tempTargetID);

//        Target newTarget = this.createFreshTarget(targetInfo);
//                Number targetID = targetDao.addTarget(newTarget);
//                String targetTemporaryId = targetInfo.getHref();
//                result.put(targetTemporaryId, targetDao.getExternalID(targetID).toString());
//                int affectedRows = annotationDao.addAnnotationTarget(annotationID, targetID);
//        
        mockeryDao.checking(new Expectations() {
            {
                oneOf(targetDao).getInternalIDFromHref(mockTargetListTwo.get(0).getHref());
                will(throwException(e));

                oneOf(targetDao).addTarget(with(aNonNull(Target.class)));
                will(returnValue(8)); //# the next new number is 8, we have already 7 Targets

                oneOf(targetDao).getHrefFromInternalID(8);
                will(returnValue("/api/targets/"+mockNewTargetUUID.toString()));

                oneOf(annotationDao).addAnnotationTarget(1, 8);
                will(returnValue(1));

            }
        });

        Map<String, String> resultTwo = dbDispatcher.addTargetsForAnnotation(1, mockTargetListTwo);
        assertEquals(1, resultTwo.size());
        assertEquals("/api/targets/"+mockNewTargetUUID.toString(), resultTwo.get(tempTargetID));

    }

    /**
     * Test of addPrincipalsAnnotation method, of class DBIntegrityServiceImlp.
     */
    @Test
    public void testAddPrincipalsAnnotation() throws Exception {
        System.out.println("test addPrincipalsAnnotation");

        // expectations for addPrincipalsannotation itself
        final Annotation testAnnotation = testInstances.getAnnotationToAdd();

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).addAnnotation(testAnnotation, 3);
                will(returnValue(5)); // the next free number is 5

                //  expectations for addTargetsForannotation
                oneOf(targetDao).getInternalIDFromHref("/api/targets/00000000-0000-0000-0000-000000000031");
                will(returnValue(1));

                oneOf(annotationDao).addAnnotationTarget(5, 1);
                will(returnValue(1));

                ///////////

                oneOf(annotationDao).updateAnnotationBody(5, testAnnotation.getBody().getTextBody().getBody(), testAnnotation.getBody().getTextBody().getMimeType(), false);
                will(returnValue(1)); // the DB update will be called at perform anyway, even if the body is not changed (can be optimized)

                oneOf(annotationDao).updatePublicAccess(5, Access.WRITE);
                will(returnValue(1));
            }
        });

        Number result = dbDispatcher.addPrincipalsAnnotation(3, testAnnotation);
        assertEquals(5, result);

//        Annotation newAnnotation = dbDispatcher.getAnnotation(5);
//        assertEquals("/api/principals/00000000-0000-0000-0000-000000000113", newAnnotation.getOwnerHref());
//        assertEquals(testAnnotation.getHeadline(), newAnnotation.getHeadline());
//        assertEquals(testAnnotation.getBody().getTextBody().getBody(), newAnnotation.getBody().getTextBody().getBody());
//        assertEquals(testAnnotation.getBody().getTextBody().getMimeType(), newAnnotation.getBody().getTextBody().getMimeType());
//        assertEquals(testAnnotation.getPermissions().getPermission().size(), newAnnotation.getPermissions().getPermission().size());
//        assertEquals(Access.WRITE, newAnnotation.getPermissions().getPublic());
//        assertEquals(testAnnotation.getTargets().getTargetInfo().size(), newAnnotation.getTargets().getTargetInfo().size());

    }

    @Test
    public void testAddPrincipal() throws NotInDataBaseException, PrincipalExists {
        System.out.println("test addPrincipal");
        final Principal freshPrincipal = new Principal();
        freshPrincipal.setDisplayName("Guilherme");
        freshPrincipal.setEMail("Guilherme.Silva@mpi.nl");
        mockeryDao.checking(new Expectations() {
            {
                oneOf(principalDao).principalExists("guisil@mpi.nl");
                will(returnValue(false));

                oneOf(principalDao).addPrincipal(freshPrincipal, "guisil@mpi.nl");
                will(returnValue(11));
            }
        });


        assertEquals(11, dbDispatcher.addPrincipal(freshPrincipal, "guisil@mpi.nl").intValue());

        /// principal already exists
        final Principal principal = new Principal();
        freshPrincipal.setDisplayName("Olha");
        freshPrincipal.setEMail("Olha.Shakaravska@mpi.nl");
        mockeryDao.checking(new Expectations() {
            {
                oneOf(principalDao).principalExists("olhsha@mpi.nl");
                will(returnValue(true));

            }
        });

        PrincipalExists ex = null;
        try {
            dbDispatcher.addPrincipal(principal, "olhsha@mpi.nl");
        } catch (PrincipalExists e) {
            ex = e;
        }
        assertFalse(ex == null);
    }

    //////////////////// DELETERS ////////////////
    @Test
    public void testDeletePrincipal() throws PrincipalCannotBeDeleted{
        System.out.println("test deletePrincipal");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(principalDao).deletePrincipal(1);
                will(returnValue(0));

                oneOf(principalDao).deletePrincipal(3);
                will(returnValue(0));

                oneOf(principalDao).deletePrincipal(10);
                will(returnValue(1));

            }
        });

        assertEquals(0, dbDispatcher.deletePrincipal(1));
        assertEquals(0, dbDispatcher.deletePrincipal(3));
        assertEquals(1, dbDispatcher.deletePrincipal(10));
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

        int[] result = dbDispatcher.deleteCachedRepresentationOfTarget(5, 7);
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
                oneOf(cachedRepresentationDao).getCachedRepresentationsForTarget(1);
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

        int[] result = dbDispatcher.deleteAllCachedRepresentationsOfTarget(1);
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
                oneOf(annotationDao).deletePermissions(2);
                will(returnValue(2));

                oneOf(targetDao).getTargetIDs(2);
                will(returnValue(mockTargetIDs));

                oneOf(annotationDao).deleteAllAnnotationTarget(2);
                will(returnValue(1));

                oneOf(annotationDao).deleteAnnotation(2);
                will(returnValue(1));

                oneOf(cachedRepresentationDao).getCachedRepresentationsForTarget(2);
                will(returnValue(mockCachedIDs));

                oneOf(targetDao).deleteTargetCachedRepresentation(2, 3);
                will(returnValue(1));

                oneOf(cachedRepresentationDao).deleteCachedRepresentation(3);
                will(returnValue(1));

                oneOf(annotationDao).targetIsInUse(2);
                will(returnValue(true));

                oneOf(targetDao).deleteTarget(2);
                will(returnValue(0));

                oneOf(annotationDao).deleteAnnotationFromAllNotebooks(2);
                will(returnValue(1));


            }
        });
        int[] result = dbDispatcher.deleteAnnotation(2);// the Target will be deleted because it is not referred by any annotation
        assertEquals(5, result.length);
        assertEquals(1, result[0]); // annotation 3 is deleted
        assertEquals(2, result[1]); // 2 rows in "annotation principal accesss are deleted"
        assertEquals(1, result[2]);  // row (3,2) in "annotations_Targets" is deleted
        assertEquals(0, result[3]); //  target 2 is not deleted deleted since it is used by annotation 1
        assertEquals(1, result[4]); // deleted from 1 notebook
    }

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
    public void testGetNotebookInternalIdentifier() throws NotInDataBaseException {

        final UUID mockUUID = UUID.fromString("00000000-0000-0000-0000-000000000021");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).getInternalID(mockUUID);
                will(returnValue(1));
            }
        });

        assertEquals(1, dbDispatcher.getResourceInternalIdentifier(mockUUID, Resource.NOTEBOOK));

    }

//    public NotebookInfoList getNotebooks(Number principalID, String access) {
//        NotebookInfoList result = new NotebookInfoList();
//        if (access.equalsIgnoreCase("read") || access.equalsIgnoreCase("write")) {
//            List<Number> notebookIDs = notebookDao.getNotebookIDs(principalID, Access.fromValue(access));
//            for (Number notebookID : notebookIDs) {
//                NotebookInfo notebookInfo = notebookDao.getNotebookInfoWithoutOwner(notebookID);
//                Number ownerID = notebookDao.getOwner(notebookID);
//                notebookInfo.setOwnerRef(principalDao.getURIFromInternalID(ownerID));
//                result.getNotebookInfo().add(notebookInfo);
//            }
//        } else {
//            if (access.equalsIgnoreCase("owner")) {
//                List<Number> notebookIDs = notebookDao.getNotebookIDsOwnedBy(principalID);
//                String ownerRef = principalDao.getURIFromInternalID(principalID);
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
    public void testGetNotebooksREADBranch() {

        final List<Number> mockNotebookIDs = new ArrayList<Number>();
        mockNotebookIDs.add(1);

        final NotebookInfo mockNotebookInfo = new NotebookInfo();
        mockNotebookInfo.setHref("/api/notebooks/00000000-0000-0000-0000-000000000011");
        mockNotebookInfo.setTitle("Notebook 1");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).getNotebookIDs(3, Access.READ);
                will(returnValue(mockNotebookIDs));

                oneOf(notebookDao).getNotebookInfoWithoutOwner(1);
                will(returnValue(mockNotebookInfo));

                oneOf(notebookDao).getOwner(1);
                will(returnValue(1));

                oneOf(principalDao).getHrefFromInternalID(1);
                will(returnValue("/api/principals/00000000-0000-0000-0000-000000000111"));

            }
        });

        NotebookInfoList result = dbDispatcher.getNotebooks(3, Access.READ);
        assertEquals("/api/notebooks/00000000-0000-0000-0000-000000000011", result.getNotebookInfo().get(0).getHref());
        assertEquals("/api/principals/00000000-0000-0000-0000-000000000111", result.getNotebookInfo().get(0).getOwnerHref());
        assertEquals("Notebook 1", result.getNotebookInfo().get(0).getTitle());

    }

    @Test
    @Ignore
    public void testGetNotebooksOwnerBranch() {

        final List<Number> mockNotebookIDs = new ArrayList<Number>();
        mockNotebookIDs.add(3);
        mockNotebookIDs.add(4);

        final NotebookInfo mockNotebookInfo1 = new NotebookInfo();
        mockNotebookInfo1.setHref("/api/notebooks/00000000-0000-0000-0000-000000000013");
        mockNotebookInfo1.setTitle("Notebook 3");

        final NotebookInfo mockNotebookInfo2 = new NotebookInfo();
        mockNotebookInfo2.setHref("/api/notebooks/00000000-0000-0000-0000-000000000014");
        mockNotebookInfo2.setTitle("Notebook 4");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).getNotebookIDsOwnedBy(3);
                will(returnValue(mockNotebookIDs));

                oneOf(principalDao).getHrefFromInternalID(3);
                will(returnValue("/api/principals/00000000-0000-0000-0000-000000000113"));

                oneOf(notebookDao).getNotebookInfoWithoutOwner(3);
                will(returnValue(mockNotebookInfo1));

                oneOf(notebookDao).getNotebookInfoWithoutOwner(4);
                will(returnValue(mockNotebookInfo2));

            }
        });

        //??
//        NotebookInfoList result = dbDispatcher.getNotebooks(3, "owner");
//        assertEquals("00000000-0000-0000-0000-000000000013", result.getNotebookInfo().get(0).getRef());//        assertEquals("00000000-0000-0000-0000-000000000113", result.getNotebookInfo().get(0).getOwnerRef());
//        assertEquals("Notebook 3", result.getNotebookInfo().get(0).getTitle());
//        assertEquals("00000000-0000-0000-0000-000000000014", result.getNotebookInfo().get(1).getRef());
//        assertEquals("00000000-0000-0000-0000-000000000113", result.getNotebookInfo().get(1).getOwnerRef());
//        assertEquals("Notebook 4", result.getNotebookInfo().get(1).getTitle());

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

                oneOf(notebookDao).getHrefFromInternalID(3);
                will(returnValue("/api/notebooks/00000000-0000-0000-0000-000000000013"));

                oneOf(notebookDao).getHrefFromInternalID(4);
                will(returnValue("/api/notebooks/00000000-0000-0000-0000-000000000014"));

            }
        });

        ReferenceList result = dbDispatcher.getNotebooksOwnedBy(3);
        assertEquals(2, result.getHref().size());
        assertEquals("/api/notebooks/00000000-0000-0000-0000-000000000013", result.getHref().get(0));
        assertEquals("/api/notebooks/00000000-0000-0000-0000-000000000014", result.getHref().get(1));
    }

    /*      public boolean hasAccess(Number notebookID, Number principalID, Access access){
     List<Number> notebookIDs = notebookDao.getNotebookIDs(principalID, access);
     if (notebookIDs == null) {
     return false;
     } 
     return notebookIDs.contains(notebookID);
     } */
    @Test
    public void testHasAccess() {


        final Access write = Access.fromValue("write");
        final List<Number> mockNotebookIDwrite = new ArrayList<Number>();
        mockNotebookIDwrite.add(1);
        mockNotebookIDwrite.add(4);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).getNotebookIDs(2, write);
                will(returnValue(mockNotebookIDwrite));

                oneOf(notebookDao).getNotebookIDs(2, write);
                will(returnValue(mockNotebookIDwrite));

            }
        });

        assertTrue(dbDispatcher.hasAccess(4, 2, write));
        assertFalse(dbDispatcher.hasAccess(5, 2, write));
    }

    /*
     public ReferenceList getPrincipals(Number notebookID, String access) {
     ReferenceList result = new ReferenceList();
     List<Number> principalIDs = notebookDao.getPrincipalIDsWithAccess(notebookID, Access.fromValue(access));
     for (Number principalID : principalIDs) {
     String reference = principalDao.getURIFromInternalID(principalID);
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
                oneOf(principalDao).getPrincipalIDsWithAccessForNotebook(1, Access.WRITE);
                will(returnValue(mockPrincipalIDs));

                oneOf(principalDao).getHrefFromInternalID(2);
                will(returnValue("/api/principals/00000000-0000-0000-0000-000000000112"));

                oneOf(principalDao).getHrefFromInternalID(4);
                will(returnValue("/api/principals/00000000-0000-0000-0000-000000000114"));


            }
        });

        ReferenceList result = dbDispatcher.getPrincipals(1, "write");
        assertEquals("/api/principals/00000000-0000-0000-0000-000000000112", result.getHref().get(0).toString());
        assertEquals("/api/principals/00000000-0000-0000-0000-000000000114", result.getHref().get(1).toString());

    }

//   @Override
//    public Notebook getNotebook(Number notebookID) {
//        Notebook result = notebookDao.getNotebookWithoutAnnotationsAndAccesssAndOwner(notebookID);
//
//        result.setOwnerRef(principalDao.getURIFromInternalID(notebookDao.getOwner(notebookID)));
//
//        ReferenceList annotations = new ReferenceList();
//        List<Number> annotationIDs = notebookDao.getAnnotations(notebookID);
//        for (Number annotationID : annotationIDs) {
//            annotations.getRef().add(annotationDao.getURIFromInternalID(annotationID));
//        }
//        result.setAnnotations(annotations);
//
//        PermissionList ups = new PermissionList();
//        List<Access> accesss = new ArrayList<Access>();
//        accesss.add(Access.READ);
//        accesss.add(Access.WRITE);
//        for (Access access : accesss) {
//            List<Number> principals = notebookDao.getPrincipalIDsWithAccess(notebookID, access);
//            if (principals != null) {
//                for (Number principal : principals) {
//                    Permission up = new Permission();
//                    up.setRef(principalDao.getURIFromInternalID(principal));
//                    up.setAccess(access);
//                    ups.getPermission().add(up);
//                }
//            }
//        }
//
//        result.setAccesss(ups);
//        return result;
//    }
    @Test
    public void testGetNotebook() throws DatatypeConfigurationException {

        final Notebook mockNotebook = new Notebook();
        mockNotebook.setHref("/api/notebooks/00000000-0000-0000-0000-000000000012");
        mockNotebook.setId("00000000-0000-0000-0000-000000000012");
        mockNotebook.setTitle("Notebook 2");
        mockNotebook.setLastModified(DatatypeFactory.newInstance().newXMLGregorianCalendar("2014-02-12T09:25:00.383000Z"));

        final List<Number> mockAnnotations = new ArrayList<Number>();
        mockAnnotations.add(3);

        final List<Number> mockREADs = new ArrayList<Number>();
        mockREADs.add(1);

        final List<Number> mockWRITEs = new ArrayList<Number>();
        mockWRITEs.add(3);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).getNotebookWithoutAnnotationsAndAccesssAndOwner(2);
                will(returnValue(mockNotebook));

                oneOf(notebookDao).getOwner(2);
                will(returnValue(2));

                oneOf(principalDao).getHrefFromInternalID(2);
                will(returnValue("/api/principals/00000000-0000-0000-0000-000000000112"));

                oneOf(annotationDao).getAnnotations(2);
                will(returnValue(mockAnnotations));

                oneOf(annotationDao).getHrefFromInternalID(3);
                will(returnValue("/api/annotations/00000000-0000-0000-0000-000000000023"));

                oneOf(principalDao).getPrincipalIDsWithAccessForNotebook(2, Access.READ);
                will(returnValue(mockREADs));

                oneOf(principalDao).getHrefFromInternalID(1);
                will(returnValue("/api/principals/00000000-0000-0000-0000-000000000111"));

                oneOf(principalDao).getPrincipalIDsWithAccessForNotebook(2, Access.WRITE);
                will(returnValue(mockWRITEs));

                oneOf(principalDao).getHrefFromInternalID(3);
                will(returnValue("/api/principals/00000000-0000-0000-0000-000000000113"));

               oneOf(principalDao).getPrincipalIDsWithAccessForNotebook(2, Access.ALL);
                will(returnValue(new ArrayList<Number>()));

            }
        });

        Notebook result = dbDispatcher.getNotebook(2);
        assertEquals("/api/notebooks/00000000-0000-0000-0000-000000000012", result.getHref());
        assertEquals("00000000-0000-0000-0000-000000000012", result.getId());
        assertEquals("/api/principals/00000000-0000-0000-0000-000000000112", result.getOwnerRef());
        assertEquals("2014-02-12T09:25:00.383000Z", result.getLastModified().toString());
        assertEquals("Notebook 2", result.getTitle());
        assertEquals(1, result.getAnnotations().getHref().size());
        assertEquals("/api/annotations/00000000-0000-0000-0000-000000000023", result.getAnnotations().getHref().get(0));
        assertEquals(2, result.getPermissions().getPermission().size());
        assertEquals("/api/principals/00000000-0000-0000-0000-000000000111", result.getPermissions().getPermission().get(0).getPrincipalHref());
        assertEquals("read", result.getPermissions().getPermission().get(0).getLevel().value());
        assertEquals("/api/principals/00000000-0000-0000-0000-000000000113", result.getPermissions().getPermission().get(1).getPrincipalHref());
        assertEquals("write", result.getPermissions().getPermission().get(1).getLevel().value());

    }

    @Test
    public void testAnnotationsForNotebook() {
        final List<Number> mockAnnotationIDs = new ArrayList<Number>();
        mockAnnotationIDs.add(1);
        mockAnnotationIDs.add(2);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getAnnotations(1);
                will(returnValue(mockAnnotationIDs));

                oneOf(annotationDao).sublistOrderedAnnotationIDs(mockAnnotationIDs, 0, 3, "last_modified", "DESC");
                will(returnValue(mockAnnotationIDs));

                oneOf(annotationDao).getHrefFromInternalID(1);
                will(returnValue("/api/annotations/00000000-0000-0000-0000-000000000021"));

                oneOf(annotationDao).getHrefFromInternalID(2);
                will(returnValue("/api/annotations/00000000-0000-0000-0000-000000000022"));



            }
        });

        ReferenceList result = dbDispatcher.getAnnotationsForNotebook(1, -1, 3, "last_modified", true);
        assertEquals(2, result.getHref().size());
        assertEquals("/api/annotations/00000000-0000-0000-0000-000000000021", result.getHref().get(0).toString());
        assertEquals("/api/annotations/00000000-0000-0000-0000-000000000022", result.getHref().get(1).toString());

    }

    /**
     * Updaters
     */
//    public boolean updateNotebookMetadata(Number notebookID, NotebookInfo upToDateNotebookInfo) {
//        Number ownerID = principalDao.getInternalIDFromURI(upToDateNotebookInfo.getOwnerRef());
//        return notebookDao.updateNotebookMetadata(notebookID, upToDateNotebookInfo.getTitle(), ownerID);
//    }
    @Test
    public void testUpdateNotebookMetadata() throws NotInDataBaseException {

        final NotebookInfo mockNotebookInfo = new NotebookInfo();
        mockNotebookInfo.setOwnerHref("/api/principals/00000000-0000-0000-0000-000000000113");
        mockNotebookInfo.setTitle("New Title");

        mockeryDao.checking(new Expectations() {
            {
                oneOf(principalDao).getInternalIDFromHref("/api/principals/00000000-0000-0000-0000-000000000113");
                will(returnValue(3));

                oneOf(notebookDao).updateNotebookMetadata(1, "New Title", 3);
                will(returnValue(true));
            }
        });

        boolean result = dbDispatcher.updateNotebookMetadata(1, mockNotebookInfo);
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

        assertTrue(dbDispatcher.addAnnotationToNotebook(1, 3));
    }

    /**
     * Adders
     */
//    public Number createNotebook(Notebook notebook, Number ownerID) {
//        Number notebookID = notebookDao.createNotebookWithoutAccesssAndAnnotations(notebook, ownerID);
//        boolean updateOwner = notebookDao.setOwner(notebookID, ownerID);
//        List<Permission> accesss = notebook.getPermissions().getPermission();
//        for (Permission principalAccess : accesss) {
//            Number principalID = principalDao.getInternalIDFromURI(principalAccess.getRef());
//            Access access = principalAccess.getAccess();
//            boolean updateAccesss = notebookDao.addAccessToNotebook(notebookID, principalID, access);
//        }
//        return notebookID;
//    }
    @Test
    public void testCreateNotebook() throws NotInDataBaseException {

        final Notebook notebook = new Notebook();
        notebook.setOwnerRef("tmpXXX");
        notebook.setTitle("(Almost) Copy of Notebook 1");
        notebook.setId("tmpYYY");
        notebook.setHref("whatever");

        PermissionList accesss = new PermissionList();
        Permission p1 = new Permission();
        p1.setLevel(Access.WRITE);
        p1.setPrincipalHref("/api/principals/00000000-0000-0000-0000-000000000112");
        accesss.getPermission().add(p1);
        Permission p2 = new Permission();
        p2.setLevel(Access.READ);
        p2.setPrincipalHref("/api/principals/00000000-0000-0000-0000-000000000113");
        accesss.getPermission().add(p2);
        notebook.setPermissions(accesss);

        mockeryDao.checking(new Expectations() {
            {
                oneOf(notebookDao).createNotebookWithoutAccesssAndAnnotations(notebook, 1);
                will(returnValue(5));

                oneOf(notebookDao).setOwner(5, 1);
                will(returnValue(true));

                oneOf(principalDao).getInternalIDFromHref("/api/principals/00000000-0000-0000-0000-000000000112");
                will(returnValue(2));

                oneOf(principalDao).getInternalIDFromHref("/api/principals/00000000-0000-0000-0000-000000000113");
                will(returnValue(3));

                oneOf(notebookDao).addAccessToNotebook(5, 2, Access.WRITE);
                will(returnValue(true));

                oneOf(notebookDao).addAccessToNotebook(5, 3, Access.READ);
                will(returnValue(true));

            }
        });

        Number result = dbDispatcher.createNotebook(notebook, 1);
        assertEquals(5, result);

    }

//    public boolean createAnnotationInNotebook(Number notebookID, Annotation annotation, Number ownerID) {
//        Number newAnnotationID = this.addPrincipalsAnnotation(ownerID, annotation);
//        return notebookDao.addAnnotationToNotebook(notebookID, newAnnotationID);
//    }
    @Test
    public void testCreateAnnotationInNotebook() throws NotInDataBaseException {

        final Annotation testAnnotation = testInstances.getAnnotationToAdd();

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).addAnnotation(testAnnotation, 3);
                will(returnValue(5)); // the next free number is 5

                //  expectations for addTargetsForannotation
                oneOf(targetDao).getInternalIDFromHref("/api/targets/00000000-0000-0000-0000-000000000031");
                will(returnValue(1));

                oneOf(annotationDao).addAnnotationTarget(5, 1);
                will(returnValue(1));

                oneOf(annotationDao).updateAnnotationBody(5, testAnnotation.getBody().getTextBody().getBody(), testAnnotation.getBody().getTextBody().getMimeType(), false);
                will(returnValue(1)); // the DB update will be called at perform anyway, even if the body is not changed (can be optimized)

                oneOf(annotationDao).updatePublicAccess(5, Access.WRITE);
                will(returnValue(1));

                /////////////////////////

                oneOf(notebookDao).addAnnotationToNotebook(1, 5);
                will(returnValue(true));
            }
        });

        assertTrue(dbDispatcher.createAnnotationInNotebook(1, testAnnotation, 3));

    }

    /**
     * Deleters
     */
//      public boolean deleteNotebook(Number notebookID) {
//        if (notebookDao.deleteAllAccesssForNotebook(notebookID) || notebookDao.deleteAllAnnotationsFromNotebook(notebookID)) {
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

                oneOf(notebookDao).deleteAllAccesssForNotebook(1);
                will(returnValue(true));

                oneOf(notebookDao).deleteAllAnnotationsFromNotebook(1);
                will(returnValue(true));

                oneOf(notebookDao).deleteNotebook(1);
                will(returnValue(true));
            }
        });

        assertTrue(dbDispatcher.deleteNotebook(1));
    }

    @Test
    public void testGetAccess() {
        System.out.println("test getAccess");

        mockeryDao.checking(new Expectations() {
            {

                oneOf(annotationDao).getAccess(1, 3);
                will(returnValue(Access.READ));

                oneOf(annotationDao).getPublicAttribute(1);
                will(returnValue(Access.WRITE));

            }
        });

        assertEquals(Access.WRITE, dbDispatcher.getAccess(1, 3));

        //////

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getAccess(2, 3);
                will(returnValue(Access.READ));

                oneOf(annotationDao).getPublicAttribute(2);
                will(returnValue(Access.READ));

            }
        });
        assertEquals(Access.READ, dbDispatcher.getAccess(2, 3));

        //////

        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getAccess(3, 3);
                will(returnValue(Access.NONE));

                oneOf(annotationDao).getPublicAttribute(3);
                will(returnValue(Access.NONE));

            }
        });
        assertEquals(Access.NONE, dbDispatcher.getAccess(3, 3));

        //////
    }

    @Test
    public void testPublicAttribute() {

        System.out.println("test getPublicAttribute");
        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getPublicAttribute(2);
                will(returnValue(Access.READ));

            }
        });
        assertEquals(Access.READ, dbDispatcher.getPublicAttribute(2));
    }

//      @Override
//    public int updateAnnotation(Annotation annotation) throws NotInDataBaseException {
//        Number annotationID = annotationDao.getInternalIDFromURI(annotation.getURI());
//        int updatedAnnotations = annotationDao.updateAnnotation(annotation, annotationID, principalDao.getInternalIDFromURI(annotation.getOwnerRef()));
//        int deletedTargets = annotationDao.deleteAllAnnotationTarget(annotationID);
//        int deletedPrinsipalsAccesss = annotationDao.deleteAnnotationPermissions(annotationID);
//        int addedTargets = addTargets(annotation, annotationID);
//        int addedPrincipalsAccesss = addPermissions(annotation.getPermissions().getPermission(), annotationID);
//        int updatedPublicAttribute = annotationDao.updatePublicAttribute(annotationID, annotation.getPermissions().getPublic());
//        return updatedAnnotations;
//    }
//    for (TargetInfo targetInfo : targets) {
//            try {
//                Number targetIDRunner = targetDao.getInternalIDFromURI(targetInfo.getRef());
//                int affectedRows = annotationDao.addAnnotationTarget(annotationID, targetIDRunner);
//            } catch (NotInDataBaseException e) {
//                Target newTarget = this.createFreshTarget(targetInfo);
//                Number targetID = targetDao.addTarget(newTarget);
//                String targetTemporaryID = targetDao.stringURItoExternalID(targetInfo.getRef());
//                result.put(targetTemporaryID, targetDao.getExternalID(targetID).toString());
//                int affectedRows = annotationDao.addAnnotationTarget(annotationID, targetID);
//            }
//        }
    @Test
    public void testUpdateAnnotation() throws NotInDataBaseException {

        System.out.println("test updateAnnotation");

        final Annotation annotation = (new TestInstances("/api")).getAnnotationOne();
        final NotInDataBaseException e = new NotInDataBaseException("annotation", "external ID", "00000000-0000-0000-0000-000000000031");
        final String mockTempID = "00000000-0000-0000-0000-000000000031";
        final UUID mockNewID = Helpers.generateUUID();
        final PermissionList permissions = annotation.getPermissions();


        System.out.println("test updateAnnotation");
        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).getInternalID(UUID.fromString(annotation.getId()));
                will(returnValue(1));

                oneOf(principalDao).getInternalIDFromHref(annotation.getOwnerHref());
                will(returnValue(1));

                oneOf(annotationDao).updateAnnotation(annotation, 1, 1);
                will(returnValue(1));

                oneOf(annotationDao).deleteAllAnnotationTarget(1);
                will(returnValue(1));

                oneOf(annotationDao).deletePermissions(1);
                will(returnValue(3));


                /// adding the first target, not found in the DB

                oneOf(targetDao).getInternalIDFromHref(annotation.getTargets().getTargetInfo().get(0).getHref());
                will(throwException(e));

                oneOf(targetDao).addTarget(with(aNonNull(Target.class)));
                will(returnValue(8));

                oneOf(targetDao).getHrefFromInternalID(8);
                will(returnValue("/api/targets/"+mockNewID.toString()));

                oneOf(annotationDao).addAnnotationTarget(1, 8);
                will(returnValue(1));

                /////////
                oneOf(targetDao).getInternalIDFromHref(annotation.getTargets().getTargetInfo().get(1).getHref());
                will(returnValue(2));

                oneOf(annotationDao).addAnnotationTarget(1, 2);
                will(returnValue(1));
                
                /////
                
                oneOf(principalDao).getPrincipalInternalIDFromRemoteID("userello");
                will(returnValue(1));

                /////
                oneOf(principalDao).getInternalIDFromHref(permissions.getPermission().get(0).getPrincipalHref());
                will(returnValue(2));

                oneOf(annotationDao).addPermission(1, 2, Access.WRITE);
                will(returnValue(1));

                oneOf(principalDao).getInternalIDFromHref(permissions.getPermission().get(1).getPrincipalHref());
                will(returnValue(3));

                oneOf(annotationDao).addPermission(1, 3, Access.READ);
                will(returnValue(1));


                ////

                oneOf(annotationDao).updateAnnotationBody(1, annotation.getBody().getTextBody().getBody(), "text/html", false);
                will(returnValue(1));

                ///

                oneOf(annotationDao).updatePublicAccess(1, permissions.getPublic());
                will(returnValue(1));


            }
        });
        assertEquals(1, dbDispatcher.updateAnnotation(annotation, "userello"));
    }

    @Test
    public void testUpdateHeadline() throws NotInDataBaseException {

        System.out.println("test updateAnnotationHeadline  ");
        mockeryDao.checking(new Expectations() {
            { oneOf(annotationDao).updateAnnotationHeadline(1, "new Headline");
                will(returnValue(1));

                
            }
        });
        assertEquals(1, dbDispatcher.updateAnnotationHeadline(1, "new Headline"));
    }
    
//    public int updateAnnotationPrincipalAccess(Number annotationID, Number principalID, Access access) {
//        int result;
//        Access currentAccess = annotationDao.getAccess(annotationID, principalID);
//        if (currentAccess != Access.NONE) {
//            result = annotationDao.updateAnnotationPrincipalAccess(annotationID, principalID, access);
//        } else {
//            if (!access.equals(Access.NONE)) {
//                result = annotationDao.deleteAnnotationPrincipalAccess(annotationID, principalID);
//                result = annotationDao.addAnnotationPrincipalAccess(annotationID, principalID, access);
//            } else {
//                result = 0;
//            }
//        }
//        return result;
//    }
    @Test
    public void testUpdatePermission() {
        System.out.println("test updateAnnotationPrincipalAccess");
        mockeryDao.checking(new Expectations() {
            {
                oneOf(annotationDao).hasExplicitAccess(1, 2);
                will(returnValue(true));

                oneOf(annotationDao).updatePermission(1, 2, Access.READ);
                will(returnValue(1));

                oneOf(annotationDao).hasExplicitAccess(1, 4);
                will(returnValue(false));
                
                oneOf(annotationDao).addPermission(1, 4, Access.WRITE);
                will(returnValue(1));

            }
        });

        assertEquals(1, dbDispatcher.updatePermission(1, 2, Access.READ));
        assertEquals(1, dbDispatcher.updatePermission(1, 4, Access.WRITE));
    }

    @Test
    public void testUpdatePermissions() throws NotInDataBaseException {
        System.out.println("test updatePermissions");

        final PermissionList permissions = new PermissionList();
        
        Permission permission2 = new Permission();
        permission2.setPrincipalHref("/api/principals/ref2");
        permission2.setLevel(Access.WRITE);

        Permission permission3 = new Permission();
        permission3.setPrincipalHref("/api/principals/ref3");
        permission3.setLevel(Access.READ);
              
        Permission permission4 = new Permission();
        permission4.setLevel(Access.READ);
        permission4.setPrincipalHref("/api/principals/ref4");
        
        permissions.getPermission().add(permission2);
        permissions.getPermission().add(permission3);
        permissions.getPermission().add(permission4);        
        permissions.setPublic(Access.WRITE);

        mockeryDao.checking(new Expectations() {
            {
                /////
                oneOf(annotationDao).updatePublicAccess(1, permissions.getPublic());
                will(returnValue(1));

                oneOf(principalDao).getInternalIDFromHref(permissions.getPermission().get(0).getPrincipalHref());
                will(returnValue(2));

                oneOf(annotationDao).hasExplicitAccess(1, 2);
                will(returnValue(true));

                oneOf(principalDao).getInternalIDFromHref(permissions.getPermission().get(1).getPrincipalHref());
                will(returnValue(3));

                oneOf(annotationDao).hasExplicitAccess(1, 3);
                will(returnValue(true));
                
                oneOf(principalDao).getInternalIDFromHref(permissions.getPermission().get(2).getPrincipalHref());
                will(returnValue(4));

                oneOf(annotationDao).hasExplicitAccess(1, 4);
                will(returnValue(false));
                
                oneOf(annotationDao).updatePermission(1, 2, Access.WRITE);
                will(returnValue(1));

                oneOf(annotationDao).updatePermission(1, 3, Access.READ);
                will(returnValue(1));
                
                oneOf(annotationDao).addPermission(1, 4, Access.READ);
                will(returnValue(1));

            }
        });

        assertEquals(3, dbDispatcher.updatePermissions(1, permissions));

    }

   

    @Test
    public void testUpdatePublicAttribute(){
        System.out.println("test updatePublicAttribute");

        
        mockeryDao.checking(new Expectations() {
            {
                /////
                oneOf(annotationDao).updatePublicAccess(1, Access.NONE);
                will(returnValue(1));


            }
        });

        assertEquals(1, dbDispatcher.updatePublicAttribute(1, Access.NONE));

    }
}
