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
import eu.dasish.annotation.backend.identifiers.CachedRepresentationIdentifier;
import eu.dasish.annotation.schema.CachedRepresentationInfo;
import java.util.ArrayList;
import java.util.List;
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
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-test-config/mockery.xml", "/spring-test-config/mockAnnotationDao.xml",
    "/spring-test-config/mockUserDao.xml", "/spring-test-config/mockPermissionsDao.xml", "/spring-test-config/mockNotebookDao.xml", "/spring-config/cachedRepresentationDao.xml"})
public class JdbcCachedRepresentationDaoTest extends JdbcResourceDaoTest{
    
    @Autowired
    JdbcCachedRepresentationDao jdbcCachedRepresentationDao; 
    
   
    public JdbcCachedRepresentationDaoTest() {
    }
    
    

    /**
     * Test of getExternalId method, of class JdbcCachedRepresentationDao.
     * public CachedRepresentationIdentifier getExternalId(Number internalID);
    
     */
    @Test  
    public void testGetExternalId() {
        // INSERT INTO cached_representation_info (external_id, mime_type, tool, type_, where_is_the_file) VALUES ('00000000-0000-0000-0000-000000000051', 'text/html', 'latex', 'text', 'corpus1'); --1
        System.out.println("getExternalId");
        Number internalID = 1;
        CachedRepresentationIdentifier expResult = new CachedRepresentationIdentifier(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_EXT_ID_);
        CachedRepresentationIdentifier result = jdbcCachedRepresentationDao.getExternalId(internalID);
        assertEquals(expResult, result);
    }

    /**
     * Test of getInternalId method, of class JdbcCachedRepresentationDao.
     * public  Number getInternalId(CachedRepresentationIdentifier externalID);
     */
    @Test
    public void testGetInternalId() {
        System.out.println("getInternalId");
        CachedRepresentationIdentifier externalID = new CachedRepresentationIdentifier(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_EXT_ID_);
        Number expResult = 1;
        Number result = jdbcCachedRepresentationDao.getInternalId(externalID);
        assertEquals(expResult, result);
    }

    /**
     * Test of getCachedRepresentationInfo method, of class JdbcCachedRepresentationDao.
     *  public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID);
     */
    @Test  
    public void testGetCachedRepresentationInfo() {
        // INSERT INTO cached_representation_info (external_id, mime_type, tool, type_, where_is_the_file) VALUES ('00000000-0000-0000-0000-000000000051', 'text/html', 'latex', 'text', 'corpus1'); --1
        System.out.println("getCachedRepresentationInfo");
        Number internalID = 1;
        
        CachedRepresentationInfo expResult = new CachedRepresentationInfo();
        expResult.setMimeType(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_MIME_TYPE_);
        expResult.setRef(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_EXT_ID_);
        expResult.setTool(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_TOOL_);
        expResult.setType(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_TYPE_);
        
        CachedRepresentationInfo result = jdbcCachedRepresentationDao.getCachedRepresentationInfo(internalID);
        assertEquals(expResult.getMimeType(), result.getMimeType());
        assertEquals(expResult.getType(), result.getType());
        assertEquals(expResult.getTool(), result.getTool());
        assertEquals(expResult.getRef(), result.getRef());
    }

    /**
     * Test of retrieveCachedRepresentationList method, of class JdbcCachedRepresentationDao.
     *  public List<Number> retrieveCachedRepresentationList(Number versionID);
     */
    @Test 
    public void testRetrieveCachedRepresentationList() {
        
        System.out.println("retrieveCachedRepresentationList");
        Number versionID = 1;
        
        List expResult = new ArrayList<Number>();
        expResult.add(1);
        expResult.add(5);
        
        List result = jdbcCachedRepresentationDao.retrieveCachedRepresentationList(versionID);
        assertEquals(expResult, result);
    }

    /**
     * Test of deleteCachedRepresentationInfo method, of class JdbcCachedRepresentationDao.
     *  public int deleteCachedRepresentationInfo(Number internalID);
     */
    @Test  
    public void testDeleteCachedRepresentationInfo() {
        System.out.println("deleteCachedRepresentationInfo");
        Number internalID = 6; /// safe to deleate becasue no version refers to it
        int result = jdbcCachedRepresentationDao.deleteCachedRepresentationInfo(internalID);
        assertEquals(1, result);
        
        int resultTwo = jdbcCachedRepresentationDao.deleteCachedRepresentationInfo(internalID);
        assertEquals(0, resultTwo);
    }

    /**
     * Test of addCachedRepresentationInfo method, of class JdbcCachedRepresentationDao.
     * public CachedRepresentationInfo addCachedRepresentationInfo(CachedRepresentationInfo cached);    
     */
    @Test  
    public void testAddCachedRepresentationInfo() {
        System.out.println("addCachedRepresentationInfo");
        
        CachedRepresentationInfo cached = new CachedRepresentationInfo();
        cached.setMimeType("text/plain");
        cached.setTool("vi");
        cached.setType("text");
        
        CachedRepresentationInfo result = jdbcCachedRepresentationDao.addCachedRepresentationInfo(cached);
        assertEquals("text/plain", result.getMimeType());
        assertEquals("vi", result.getTool());
        assertEquals("text", result.getType());
        assertFalse(result.getRef() == null); // new non-null external identifier should be assigned
    }
    
    /**
     * Test of purge, of class JdbcCachedRepresentationDao.
     *  public int purge(Number internalID) 
     */
    @Test  
    public void testPurge() {
        System.out.println("test purge");
        int result = jdbcCachedRepresentationDao.purge(6);
        assertEquals(1, result);
    }
    
    /**
     * test public List<Number> cachedRepresentationIDs()
     * public List<Number> cachedRepresentationIDs()
     */
    @Test  
    public void testCachedRepresentationIDs() {
        System.out.println(" test cachedRepresentationIDs");
        List<Number>  result = jdbcCachedRepresentationDao.cachedRepresentationIDs();
        assertEquals(7, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
        assertEquals(3, result.get(2));
        assertEquals(4, result.get(3));
        assertEquals(5, result.get(4));
        assertEquals(6, result.get(5));        
        assertEquals(7, result.get(6));
    }
    
     /**
     * Test of purgeAll, of class JdbcCachedRepresentationDao.
     * public  int purgeAll() 
     */
    @Test  
    public void testPurgeAll() {
        System.out.println("test purge All");
        int result = jdbcCachedRepresentationDao.purgeAll();
        assertEquals(2, result);
    }
}
