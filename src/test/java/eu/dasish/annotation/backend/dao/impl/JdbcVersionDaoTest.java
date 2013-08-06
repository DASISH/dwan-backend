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
import eu.dasish.annotation.backend.identifiers.VersionIdentifier;
import eu.dasish.annotation.schema.Version;
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
//@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-test-config/mockery.xml", "/spring-test-config/mockAnnotationDao.xml",
//    "/spring-test-config/mockUserDao.xml", "/spring-test-config/mockPermissionsDao.xml", "/spring-test-config/mockNotebookDao.xml", "/spring-config/cachedRepresentationDao.xml"})
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-config/cachedRepresentationDao.xml"})

public class JdbcVersionDaoTest extends JdbcResourceDaoTest{
    
    @Autowired
    JdbcVersionDao jdbcVersionDao;

    /**
     * Test of getExternalId method, of class JdbcVersionDao.
     */
    @Test
    public void testGetExternalId() {
        System.out.println("getExternalId");
        Number internalID = 1;
        VersionIdentifier expResult = null;
        VersionIdentifier result = jdbcVersionDao.getExternalId(internalID);
        assertEquals(TestBackendConstants._TEST_VERSION_1_EXT_ID, result.toString());
    }

    /**
     * Test of getInternalId method, of class JdbcVersionDao.
     */
    @Test
    @Ignore
    public void testGetInternalId() {
        System.out.println("getInternalId");
        VersionIdentifier externalID = null;
        JdbcVersionDao instance = null;
        Number expResult = null;
        Number result = instance.getInternalId(externalID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVersion method, of class JdbcVersionDao.
     */
    @Test
    @Ignore
    public void testGetVersion() {
        System.out.println("getVersion");
        Number internalID = null;
        JdbcVersionDao instance = null;
        Version expResult = null;
        Version result = instance.getVersion(internalID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of retrieveVersionList method, of class JdbcVersionDao.
     */
    @Test
    @Ignore
    public void testRetrieveVersionList() {
        System.out.println("retrieveVersionList");
        Number sourceID = null;
        JdbcVersionDao instance = null;
        List expResult = null;
        List result = instance.retrieveVersionList(sourceID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteVersion method, of class JdbcVersionDao.
     */
    @Test
    @Ignore
    public void testDeleteVersion() {
        System.out.println("deleteVersion");
        Number internalID = null;
        JdbcVersionDao instance = null;
        int expResult = 0;
        int result = instance.deleteVersion(internalID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addVersion method, of class JdbcVersionDao.
     */
    @Test
    @Ignore
    public void testAddVersion() {
        System.out.println("addVersion");
        Version freshVersion = null;
        JdbcVersionDao instance = null;
        Version expResult = null;
        Version result = instance.addVersion(freshVersion);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of purge method, of class JdbcVersionDao.
     */
    @Test
    @Ignore
    public void testPurge() {
        System.out.println("purge");
        Number internalID = null;
        JdbcVersionDao instance = null;
        int expResult = 0;
        int result = instance.purge(internalID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of versionIDs method, of class JdbcVersionDao.
     */
    @Test
    @Ignore
    public void testVersionIDs() {
        System.out.println("versionIDs");
        JdbcVersionDao instance = null;
        List expResult = null;
        List result = instance.versionIDs();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of purgeAll method, of class JdbcVersionDao.
     */
    @Test
    @Ignore
    public void testPurgeAll() {
        System.out.println("purgeAll");
        JdbcVersionDao instance = null;
        int expResult = 0;
        int result = instance.purgeAll();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
