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
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-config/versionDao.xml"})
public class JdbcVersionDaoTest extends JdbcResourceDaoTest {

    @Autowired
    JdbcVersionDao jdbcVersionDao;
    

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
     *
     *
     * /**
     * Test of deleteVersion method, of class JdbcVersionDao.
     */
    @Test
    @Ignore
    public void testDeleteVersion() {
        System.out.println("deleteVersion");
//        mockery.checking(new Expectations() {
//            {
//                oneOf(cachedRepresentationDao).deleteCachedRepresentationInfo(5);
//                will(returnValue(0));
//
//            }
//        });
        int[] result = jdbcVersionDao.deleteVersion(6);
        assertEquals(1, result[0]); //versions-cached
        assertEquals(1, result[1]); // version
        assertEquals(0, result[2]);//cached 5 is in use


        int[] resultTwo = jdbcVersionDao.deleteVersion(5); // version is in use by the source 4
        assertEquals(0, resultTwo[0]);
        assertEquals(0, resultTwo[1]);
        assertEquals(0, resultTwo[2]);

    }

    /**
     * Test of addVersion method, of class JdbcVersionDao.
     */
    @Test
    public void testAddVersion() {
        System.out.println("addVersion");

        Version freshVersion = new Version();

        Number result = jdbcVersionDao.addVersion(freshVersion);
        assertEquals(8, result);
        Version addedVersion = jdbcVersionDao.getVersion(result);
        assertFalse(null == addedVersion.getVersion());
    }
    
    
   
   

    /**
     * Test of retrieveCachedRepresentationList method, of class JdbcVersionDao.
     * public List<Number> retrieveCachedRepresentationList(Number versionID);
     */
    @Test
    public void testRetrieveCachedRepresentationList() {

        System.out.println("retrieveCachedRepresentationList");
        Number versionID = 1;

        List expResult = new ArrayList<Number>();
        expResult.add(1);
        expResult.add(5);

        List result = jdbcVersionDao.retrieveCachedRepresentationList(versionID);
        assertEquals(expResult, result);
    }
}
