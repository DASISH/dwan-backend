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

import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.Permission;
import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
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
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-config/notebookDao.xml"})
public class JdbcNotebookDaoTest extends JdbcResourceDaoTest {

    @Autowired
    JdbcNotebookDao jdbcNotebookDao;

    /**
     * Test of getOwner method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetOwner() {
        System.out.println("test getOwner");
        assertEquals(3, jdbcNotebookDao.getOwner(4));
    }

    /**
     * Test of getNotebookIDs method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetNotebookIDs() {
        System.out.println("test getNotebookIDs for a principal with Permission");
        List<Number> expResult = new ArrayList<Number>();
        expResult.add(1);
        expResult.add(4);
        List<Number> result = jdbcNotebookDao.getNotebookIDs(2, Permission.WRITER);
        assertEquals(expResult, result);
    }

    /**
     * Test of getNotebookIDsOwnedBy method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetNotebookIDsOwnedBy() {
        System.out.println("test getNotebookIDsOwnedBy");
        List<Number> expResult = new ArrayList<Number>();
        expResult.add(3);
        expResult.add(4);
        List<Number> result = jdbcNotebookDao.getNotebookIDsOwnedBy(3);
        assertEquals(expResult, result);
    }

  
    /**
     * Test of getNotebookInfoWithoutOwner method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetNotebookInfoWithoutOwner() {
        System.out.println("test getNotebookInfoWithoutOwner");
        NotebookInfo result = jdbcNotebookDao.getNotebookInfoWithoutOwner(1);
        assertEquals("00000000-0000-0000-0000-000000000011", result.getRef());
        assertEquals("Notebook 1", result.getTitle());
        assertEquals(null, result.getOwnerRef());
    }

    /**
     * Test of getNotebookWithoutAnnotationsAndPermissionsAndOwner method, of
     * class JdbcNotebookDao.
     */
    @Test
    public void testGetNotebookWithoutAnnotationsAndPermissionsAndOwner() {
        System.out.println("test getNotebookWithoutAnnotationsAndPermissionsAndOwner");
        Notebook result = jdbcNotebookDao.getNotebookWithoutAnnotationsAndPermissionsAndOwner(1);
        assertEquals("00000000-0000-0000-0000-000000000011", result.getURI());
        assertEquals("Notebook 1", result.getTitle());
        assertEquals("2013-08-12T09:25:00.383000Z", result.getLastModified().toString());
    }

   
    /**
     * Test of updateNotebookMetadata method, of class JdbcNotebookDao.
     */
    @Test
    public void testUpdateNotebookMetadata() {
        System.out.println("test updateNotebookMetadata");
        boolean result = jdbcNotebookDao.updateNotebookMetadata(1, "Gaudi and his work", 3);
        assertEquals(true, result);
        assertEquals("Gaudi and his work", jdbcNotebookDao.getNotebookWithoutAnnotationsAndPermissionsAndOwner(1).getTitle());
        assertEquals(3, jdbcNotebookDao.getOwner(1));
    }

    /**
     * Test of setOwner method, of class JdbcNotebookDao.
     */
    @Test
    public void testSetOwner() {
        System.out.println("test setOwner");
        boolean result = jdbcNotebookDao.setOwner(1, 2);
        assertTrue(result);
        assertEquals(2, jdbcNotebookDao.getOwner(2));
    }

    /**
     * Test of updateUserPermissionForNotebook method, of class JdbcNotebookDao.
     */
    @Test
    public void testUpdateUserPermissionForNotebook() {
        System.out.println("test updateUserPermissionForNotebook");
        boolean result = jdbcNotebookDao.updateUserPermissionForNotebook(1, 2, Permission.READER);
        assertTrue(result);

        // in the next test the update should fail
        //assertFalse(jdbcNotebookDao.updateUserPermissionForNotebook(1, 2, Permission.OWNER));
        //SQL throws an error, which is good
    }

    /**
     * Test of createNotebookWithoutPermissionsAndAnnotations method, of class
     * JdbcNotebookDao.
     */
    @Test
    public void testCreateNotebookWithoutPermissionsAndAnnotations() throws DatatypeConfigurationException {
        System.out.println("test createNotebookWithoutPermissionsAndAnnotations");
        Notebook notebook = new Notebook();
        notebook.setTitle("New test notebook");
        notebook.setLastModified(DatatypeFactory.newInstance().newXMLGregorianCalendar("2014-02-12T09:25:00.383000Z"));
        Number result = jdbcNotebookDao.createNotebookWithoutPermissionsAndAnnotations(notebook, 3);
        assertEquals(5, result);
        assertEquals(3, jdbcNotebookDao.getOwner(result));
        assertNotNull(jdbcNotebookDao.getExternalID(result));
        assertEquals("New test notebook", jdbcNotebookDao.getNotebookWithoutAnnotationsAndPermissionsAndOwner(result).getTitle());
    }

    /**
     * Test of addAnnotationToNotebook method, of class JdbcNotebookDao.
     */
    @Test
    public void testAddAnnotationToNotebook() {
        System.out.println("test addAnnotationToNotebook");
        boolean result = jdbcNotebookDao.addAnnotationToNotebook(2, 4);
        assertTrue(result);
    }

    /**
     * Test of addPermissionToNotebook method, of class JdbcNotebookDao.
     */
    @Test
    public void testAddPermissionToNotebook() {
        System.out.println("test addPermissionToNotebook");
        boolean result = jdbcNotebookDao.addPermissionToNotebook(2, 4, Permission.WRITER);
        assertTrue(result);
    }

    /**
     * Test of deleteAnnotationFromNotebook method, of class JdbcNotebookDao.
     */
    @Test
    public void testDeleteAnnotationFromNotebook() {
        System.out.println("test deleteAnnotationFromNotebook");
        boolean result = jdbcNotebookDao.deleteAnnotationFromNotebook(1, 2);
        assertTrue(result);
    }

    /**
     * Test of deleteNotebookPrincipalPermission method, of class
     * JdbcNotebookDao.
     */
    @Test
    public void testDeleteNotebookPrincipalPermission() {
        System.out.println("deleteNotebookPrincipalPermission");
        boolean result = jdbcNotebookDao.deleteNotebookPrincipalPermission(1, 2);
        assertTrue(result);
    }

    /**
     * Test of deleteAllAnnotationsFromNotebook method, of class
     * JdbcNotebookDao.
     */
    @Test
    public void testDeleteAllAnnotationsFromNotebook() {
        System.out.println("test deleteAllAnnotationsFromNotebook");
        boolean result = jdbcNotebookDao.deleteAllAnnotationsFromNotebook(1);
        assertTrue(result);
    }

    /**
     * Test of deleteAllPermissionsForNotebook method, of class JdbcNotebookDao.
     */
    @Test
    public void testDeleteAllPermissionsForNotebook() {
        System.out.println("test deleteAllPermissionsForNotebook");
        boolean result = jdbcNotebookDao.deleteAllPermissionsForNotebook(1);
        assertTrue(result);
    }

    /**
     * Test of deleteNotebook method, of class JdbcNotebookDao.
     */
    @Test
    public void testDeleteNotebook() {
        System.out.println("test deleteNotebook");
        boolean result = jdbcNotebookDao.deleteNotebook(3);
        assertTrue(result);
        assertNull(jdbcNotebookDao.getExternalID(3));
    }
}
