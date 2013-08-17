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
package eu.dasish.annotation.backend.dao.integration;

import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.NewSourceInfo;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author olhsha
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-test-config/mockery.xml", "/spring-test-config/mockAnnotationDao.xml",
    "/spring-test-config/mockUserDao.xml", "/spring-test-config/mockPermissionsDao.xml", "/spring-test-config/mockNotebookDao.xml",
    "/spring-test-config/mockSourceDao.xml", "/spring-test-config/mockVersionDao.xml", "/spring-config/cachedRepresentationDao.xml"})
public class DaoRequestorTest {
    
    public DaoRequestorTest() {
    }
    
   
    /**
     * Test of getAnnotationInternalIdentifier method, of class DaoRequestor.
     */
    @Test
    public void testGetAnnotationInternalIdentifier() {
        System.out.println("getAnnotationInternalIdentifier");
        AnnotationIdentifier annotationIdentifier = null;
        DaoRequestor instance = new DaoRequestor();
        Number expResult = null;
        Number result = instance.getAnnotationInternalIdentifier(annotationIdentifier);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAnnotationExternalIdentifier method, of class DaoRequestor.
     */
    @Test
    public void testGetAnnotationExternalIdentifier() {
        System.out.println("getAnnotationExternalIdentifier");
        Number annotationID = null;
        DaoRequestor instance = new DaoRequestor();
        AnnotationIdentifier expResult = null;
        AnnotationIdentifier result = instance.getAnnotationExternalIdentifier(annotationID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUserInternalIdentifier method, of class DaoRequestor.
     */
    @Test
    public void testGetUserInternalIdentifier() {
        System.out.println("getUserInternalIdentifier");
        UserIdentifier userIdentifier = null;
        DaoRequestor instance = new DaoRequestor();
        Number expResult = null;
        Number result = instance.getUserInternalIdentifier(userIdentifier);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUserExternalIdentifier method, of class DaoRequestor.
     */
    @Test
    public void testGetUserExternalIdentifier() {
        System.out.println("getUserExternalIdentifier");
        Number userID = null;
        DaoRequestor instance = new DaoRequestor();
        UserIdentifier expResult = null;
        UserIdentifier result = instance.getUserExternalIdentifier(userID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteCachedForVersion method, of class DaoRequestor.
     */
    @Test
    public void testDeleteCachedForVersion() {
        System.out.println("deleteCachedForVersion");
        Number versionID = null;
        Number cachedID = null;
        DaoRequestor instance = new DaoRequestor();
        int[] expResult = null;
        int[] result = instance.deleteCachedForVersion(versionID, cachedID);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addCachedForVersion method, of class DaoRequestor.
     */
    @Test
    public void testAddCachedForVersion() {
        System.out.println("addCachedForVersion");
        Number versionID = null;
        CachedRepresentationInfo cached = null;
        DaoRequestor instance = new DaoRequestor();
        Number[] expResult = null;
        Number[] result = instance.addCachedForVersion(versionID, cached);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    
    
     /**
     * Test of addVersion method, of class JdbcVersionDao.
     */
//    @Test
//    public void testAddCachedForVersion() {
//        System.out.println("test addCachedForVersion");
//
//        final CachedRepresentationInfo cached = new CachedRepresentationInfo();
//        cached.setMimeType("text/plain");
//        cached.setTool("vi");
//        cached.setType("text");
//        cached.setRef(null);
//       
//        Number[] result = jdbcCachedRepresentationDao.addCachedForVersion(6, cached);
//        assertEquals(8, result[0].intValue());
//        assertEquals(1, result[1].intValue());
//    }

     /**
     *
     */
//    @Test
//    public void tesDeleteCachedForVersion() {
//        System.out.println("test delete CachedRepresentationForVersion");
//        System.out.println("deleteVersion");
//        
//        int[] result = jdbcCachedRepresentationDao.deleteCachedForVersion(6, 5);
//        assertEquals(1, result[0]); //versions-cached
//        assertEquals(0, result[1]);//cached 5 is in use
//        
//        int[] resultTwo = jdbcCachedRepresentationDao.deleteCachedForVersion(6, 4); // no such pair
//        assertEquals(0, resultTwo[0]); 
//        assertEquals(0, resultTwo[1]); 
//
//
//    }
//
//    /**
//     * Test of deleteVersionWithCachedRepresentations method, of class DaoRequestor.
//     */
//    @Test
//    public void testDeleteVersionWithCachedRepresentations() {
//        System.out.println("deleteVersionWithCachedRepresentations");
//        Number versionID = null;
//        DaoRequestor instance = new DaoRequestor();
//        int[] expResult = null;
//        int[] result = instance.deleteVersionWithCachedRepresentations(versionID);
//        assertArrayEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of deleteSourceWithVersions method, of class DaoRequestor.
     */
    @Test
    public void testDeleteSourceWithVersions() {
        System.out.println("deleteSourceWithVersions");
        Number sourceID = null;
        DaoRequestor instance = new DaoRequestor();
        int[] expResult = null;
        int[] result = instance.deleteSourceWithVersions(sourceID);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addSourceAndPairSourceVersion method, of class DaoRequestor.
     */
    @Test
    public void testAddSourceAndPairSourceVersion() throws Exception {
        System.out.println("addSourceAndPairSourceVersion");
        NewSourceInfo newSource = null;
        DaoRequestor instance = new DaoRequestor();
        Number expResult = null;
        Number result = instance.addSourceAndPairSourceVersion(newSource);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addTargetSourcesToAnnotation method, of class DaoRequestor.
     */
    @Test
    public void testAddTargetSourcesToAnnotation() throws Exception {
        System.out.println("addTargetSourcesToAnnotation");
        Number annotationID = null;
        List<NewOrExistingSourceInfo> sources = null;
        DaoRequestor instance = new DaoRequestor();
        Map expResult = null;
        Map result = instance.addTargetSourcesToAnnotation(annotationID, sources);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFilteredAnnotationIDs method, of class DaoRequestor.
     */
    @Test
    public void testGetFilteredAnnotationIDs() {
        System.out.println("getFilteredAnnotationIDs");
        String link = "";
        String text = "";
        String access = "";
        String namespace = "";
        UserIdentifier owner = null;
        Timestamp after = null;
        Timestamp before = null;
        DaoRequestor instance = new DaoRequestor();
        List expResult = null;
        List result = instance.getFilteredAnnotationIDs(link, text, access, namespace, owner, after, before);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteAnnotationWithSources method, of class DaoRequestor.
     */
    @Test
    public void testDeleteAnnotationWithSources() throws Exception {
        System.out.println("deleteAnnotationWithSources");
        Number annotationID = null;
        DaoRequestor instance = new DaoRequestor();
        int[] expResult = null;
        int[] result = instance.deleteAnnotationWithSources(annotationID);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAnnotation method, of class DaoRequestor.
     */
    @Test
    public void testGetAnnotation() throws Exception {
        System.out.println("getAnnotation");
        Number annotationID = null;
        DaoRequestor instance = new DaoRequestor();
        Annotation expResult = null;
        Annotation result = instance.getAnnotation(annotationID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addAnnotationWithTargetSources method, of class DaoRequestor.
     */
    @Test
    public void testAddAnnotationWithTargetSources() throws Exception {
        System.out.println("addAnnotationWithTargetSources");
        Annotation annotation = null;
        Number userID = null;
        DaoRequestor instance = new DaoRequestor();
        Annotation expResult = null;
        Annotation result = instance.addAnnotationWithTargetSources(annotation, userID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
