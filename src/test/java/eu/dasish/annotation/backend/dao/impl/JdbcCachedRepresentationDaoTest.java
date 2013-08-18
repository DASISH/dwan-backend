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
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-config/cachedRepresentationDao.xml"})
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
        System.out.println("getExternalId");
        Number internalID = 1;
        CachedRepresentationIdentifier expResult = new CachedRepresentationIdentifier(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_EXT_ID_);
        CachedRepresentationIdentifier result = jdbcCachedRepresentationDao.getExternalID(internalID);
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
        Number result = jdbcCachedRepresentationDao.getInternalID(externalID);
        assertEquals(1, result.intValue());
    }

    /**
     * Test of getCachedRepresentationInfo method, of class JdbcCachedRepresentationDao.
     *  public CachedRepresentationInfo getCachedRepresentationInfo(Number internalID);
     */
    @Test  
    public void testGetCachedRepresentationInfo() {
        System.out.println("getCachedRepresentationInfo");
        
        CachedRepresentationInfo result = jdbcCachedRepresentationDao.getCachedRepresentationInfo(1);
        assertEquals(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_MIME_TYPE_, result.getMimeType());
        assertEquals(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_EXT_ID_, result.getType());
        assertEquals(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_TOOL_, result.getTool());
        assertEquals(TestBackendConstants._TEST_CACHED_REPRESENTATION_1_TYPE_, result.getRef());
    }

    
    /**
     * Test of deleteCachedRepresentationInfo method, of class JdbcCachedRepresentationDao.
     *  public int deleteCachedRepresentationInfo(Number internalID);
     */
    @Test  
    public void testDeleteCachedRepresentationInfo() {
        System.out.println("deleteCachedRepresentationInfo");
        Number internalID = 6; /// deleted because no version refers to it
        int result = jdbcCachedRepresentationDao.deleteCachedRepresentationInfo(internalID);
        assertEquals(1, result);
        
        int resultTwo = jdbcCachedRepresentationDao.deleteCachedRepresentationInfo(internalID);
        assertEquals(0, resultTwo);
        
        Number internalIDDoNotDelete = 5;
        int resultThree =jdbcCachedRepresentationDao.deleteCachedRepresentationInfo(internalIDDoNotDelete);
        assertEquals(0, resultThree);
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
        cached.setRef(null);
        
        Number result = jdbcCachedRepresentationDao.addCachedRepresentationInfo(cached);
        CachedRepresentationInfo addedCached = jdbcCachedRepresentationDao.getCachedRepresentationInfo(result);
        assertEquals(8, result.intValue());
        assertEquals("text/plain", addedCached.getMimeType());
        assertEquals("vi", addedCached.getTool());
        assertEquals("text", addedCached.getType());
        assertFalse(addedCached.getRef() == null); // new non-null external identifier should be assigned
    }
    
  
}
