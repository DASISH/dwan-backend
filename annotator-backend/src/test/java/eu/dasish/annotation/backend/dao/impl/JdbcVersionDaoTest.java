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
import eu.dasish.annotation.backend.dao.CachedRepresentationDao;
import eu.dasish.annotation.backend.identifiers.VersionIdentifier;
import eu.dasish.annotation.schema.Version;
import java.util.ArrayList;
import java.util.List;
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
    "/spring-test-config/mockUserDao.xml", "/spring-test-config/mockPermissionsDao.xml", "/spring-test-config/mockNotebookDao.xml",
    "/spring-test-config/mockCachedRepresentationDao.xml", "/spring-config/versionDao.xml"})

public class JdbcVersionDaoTest extends JdbcResourceDaoTest{
    
    @Autowired
    JdbcVersionDao jdbcVersionDao;
    
    @Autowired
    private CachedRepresentationDao cachedRepresentationDao;    
    
    @Autowired
    private Mockery mockery;
    
    
    /**
     * Test of getExternalId method, of class JdbcVersionDao.
     */
    @Test
    public void testGetExternalId() {
        System.out.println("getExternalId");
        Number internalID = 1;
        VersionIdentifier result = jdbcVersionDao.getExternalID(internalID);
        assertEquals(TestBackendConstants._TEST_VERSION_1_EXT_ID, result.toString());
    }

    /**
     * Test of getInternalId method, of class JdbcVersionDao.
     */
    @Test
    public void testGetInternalId() {
        System.out.println("getInternalId");
        VersionIdentifier externalID = new VersionIdentifier(TestBackendConstants._TEST_VERSION_1_EXT_ID);
        Number expResult = 1;
        Number result = jdbcVersionDao.getInternalID(externalID);
        assertEquals(expResult, result);
    }

    /**
     * Test of getVersion method, of class JdbcVersionDao.
     */
    @Test
    public void testGetVersion() {
        System.out.println("getVersion");
        Number internalID = 1;
        Version result = jdbcVersionDao.getVersion(internalID);
        assertEquals(TestBackendConstants._TEST_VERSION_1_EXT_ID, result.getVersion());
        //TODO: once the schems is fixed, test "version" and "URI/external-id" separately
        // at the moment "version" corresponds "external_id"
    }

    /**
     * Test of retrieveVersionList method, of class JdbcVersionDao.
     */
    @Test
    public void testRetrieveVersionList() {
        System.out.println("retrieveVersionList");
        Number sourceID = 1;
        // INSERT INTO sources_versions (source_id, version_id) VALUES (1, 1);
        // INSERT INTO sources_versions (source_id, version_id) VALUES (1, 2);
        List<Number> result = jdbcVersionDao.retrieveVersionList(sourceID);
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
    }

     /**
     * Test of deleteVersionCachedRepresentationRows method, of class JdbcVersionDao.
     */
    @Test
    public void testDeleteVersionCachedRepresentationRows() {
        System.out.println("deleteVersion");
        Number internalID = 6; // there is no sources (in target_source and sources_versions - sibling table) connected to this version in the test table
        int result = jdbcVersionDao.deleteVersionCachedRepresentationRow(internalID);
        assertEquals(1, result);
    }
    
    /**
     * Test of deleteVersion method, of class JdbcVersionDao.
     */
    @Test
    public void testDeleteVersion() {
        System.out.println("deleteVersion");
        final Number internalID = 6; // there is no sources (in target_source and sources_versions - sibling table) connected to this version in the test table
        final Number cachedID =5;
        final List<Number> versions = new ArrayList<Number>();
        versions.add(cachedID);
        
        //jdbcCachedRepresentationDao.retrieveCachedRepresentationList(internalID);
        //jdbcCachedRepresentationDao.deleteCachedRepresentationInfo(cachedID);
        
        mockery.checking(new Expectations() {
            { 
                oneOf(cachedRepresentationDao).retrieveCachedRepresentationList(internalID);
                will(returnValue(versions));
                
                oneOf(cachedRepresentationDao).deleteCachedRepresentationInfo(cachedID);
                will(returnValue(0));
                
            }
        });
        
        int result = jdbcVersionDao.deleteVersion(internalID); 
        assertEquals(1, result);
        
        // try to delete one more time
         mockery.checking(new Expectations() {
            { 
                oneOf(cachedRepresentationDao).retrieveCachedRepresentationList(internalID);
                will(returnValue(new ArrayList<Number>()));
                
            }
        });
        int resultTwo = jdbcVersionDao.deleteVersion(internalID);
        assertEquals(0, resultTwo);
        
        // the version in use, shoul not be deleted
        final Number internalIDNotToDelete = 4;          
        int resultThree = jdbcVersionDao.deleteVersion(internalIDNotToDelete);
        assertEquals(0, resultThree);
    }

    /**
     * Test of addVersion method, of class JdbcVersionDao.
     */
    @Test
    public void testAddVersion() {
        System.out.println("addVersion");
        
        Version freshVersion = new Version();   
        
        Version result = jdbcVersionDao.addVersion(freshVersion);
        assertFalse(null==result.getVersion());
        
        // check if it is a good UUID. the program breaks if the string is anot a good UUID
        assertEquals(result.getVersion(), (new VersionIdentifier(result.getVersion())).toString());
    }

  
 
  
}
