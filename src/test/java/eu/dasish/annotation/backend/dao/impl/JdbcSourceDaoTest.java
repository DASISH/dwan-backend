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
import eu.dasish.annotation.backend.dao.VersionDao;
import eu.dasish.annotation.backend.identifiers.SourceIdentifier;
import eu.dasish.annotation.backend.identifiers.VersionIdentifier;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-test-config/mockery.xml", "/spring-test-config/mockAnnotationDao.xml",
    "/spring-test-config/mockVersionDao.xml", "/spring-test-config/mockUserDao.xml", "/spring-test-config/mockPermissionsDao.xml",
    "/spring-test-config/mockNotebookDao.xml",
    "/spring-config/sourceDao.xml"})
public class JdbcSourceDaoTest extends JdbcResourceDaoTest {

    @Autowired
    JdbcSourceDao jdbcSourceDao;
    @Autowired
    private VersionDao versionDao;
    @Autowired
    private Mockery mockery;

    /**
     * Test of getExternalID method, of class JdbcSourceDao.
     */
    @Test
    public void testGetExternalID() {
        System.out.println("getExternalID");
        Number internalID = 1;
        SourceIdentifier result = jdbcSourceDao.getExternalID(internalID);
        assertEquals(TestBackendConstants._TEST_SOURCE_1_EXT_ID, result.toString());
    }

    /**
     * Test of getInternalID method, of class JdbcSourceDao.
     */
    @Test
    public void testGetInternalId() {
        System.out.println("getInternalId");
        SourceIdentifier externalID = new SourceIdentifier(TestBackendConstants._TEST_SOURCE_1_EXT_ID);
        Number expResult = 1;
        Number result = jdbcSourceDao.getInternalID(externalID);
        assertEquals(expResult, result);
    }

    /**
     * Test of getSource method, of class JdbcSourceDao.
     */
    @Test
    public void testGetSource() {
        System.out.println("getSource");
        Number internalID = 1;
        final Number internalVersionID = 1;
        final VersionIdentifier externalVersionID = new VersionIdentifier(TestBackendConstants._TEST_VERSION_1_EXT_ID);

        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).getExternalID(internalVersionID);
                will(returnValue(externalVersionID));
            }
        });

        Source result = jdbcSourceDao.getSource(internalID);
        assertEquals(TestBackendConstants._TEST_SOURCE_1_EXT_ID, result.getURI());
        assertEquals(TestBackendConstants._TEST_SOURCE_1_LINK, result.getLink());
        assertEquals(TestBackendConstants._TEST_VERSION_1_EXT_ID, result.getVersion());
        //TODO: time stamp is not checked: do not know with what to compare 
    }

    /**
     * Test of deleteSource method, of class JdbcSourceDao.
     */
    @Test
    public void testDeleteSource() {
        System.out.println("deleteSource");

        // test 1
        int[] result = jdbcSourceDao.deleteSource(1); //the source is in use, should not be deleted
        assertEquals(0, result[0]); // 
        assertEquals(0, result[1]);

        // test 2
        final int[] versionDeleted = new int[3];
        versionDeleted[0] = 0; // versions_cahced_representations
        versionDeleted[1] = 1; // version deleted
        versionDeleted[2] = 0; // deleted cached representations; version 7 does not have them
        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).deleteVersion(7);
                will(returnValue(versionDeleted)); // no other sources refer to this version # 5
            }
        });

        int[] resultTwo = jdbcSourceDao.deleteSource(5);// the source will be deleted because it is not referred by any annotation
        assertEquals(1, resultTwo[0]); // row (5,7) in "sorces_versions" is deleted
        assertEquals(1, resultTwo[1]); //source 5 is deleted from "source" table
    }

    /**
     * Test of addSource method, of class JdbcSourceDao.
     */
    @Test
    public void testAddSource() throws SQLException {
        System.out.println("addSource");

        String link = "http://www.sagradafamilia.cat/";

        // test 1: existing version
        Source freshSource = new Source();
        freshSource.setLink(link);
        freshSource.setVersion(TestBackendConstants._TEST_VERSION_1_EXT_ID);
        freshSource.setURI(null);
        freshSource.setTimeSatmp(null);

        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).getInternalID(new VersionIdentifier(TestBackendConstants._TEST_VERSION_1_EXT_ID));
                will(returnValue(1));
            }
        });

        Number result = jdbcSourceDao.addSource(freshSource);
        assertEquals(6, result);
        
        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).getExternalID(1); // used in getSource
                will(returnValue(new VersionIdentifier(TestBackendConstants._TEST_VERSION_1_EXT_ID)));
                
                
            }
        });
        Source addedSource = jdbcSourceDao.getSource(result);
        assertEquals(link, addedSource.getLink());
        assertEquals(TestBackendConstants._TEST_VERSION_1_EXT_ID, addedSource.getVersion());
        assertFalse(null == addedSource.getURI());

        ////////// test 2 non-existing version
        freshSource.setVersion(TestBackendConstants._TEST_VERSION_NONEXIST_EXT_ID);
        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).getInternalID(new VersionIdentifier(TestBackendConstants._TEST_VERSION_NONEXIST_EXT_ID));
                will(returnValue(null));
            }
        });


        Number resultTwo = jdbcSourceDao.addSource(freshSource);
        assertEquals(-1, resultTwo); // addversion (preferably with cached representation

    }

    /**
     * Test of getSourceInfos method, of class JdbcSourceDao.
     */
    @Test
    public void testGetSourceInfos() {
        System.out.println("getSourceInfos");
        List<Number> test = new ArrayList<Number>();
        test.add(1);
        test.add(2);

        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).getExternalID(1);
                will(returnValue(new VersionIdentifier(TestBackendConstants._TEST_VERSION_1_EXT_ID)));

                oneOf(versionDao).getExternalID(3);
                will(returnValue(new VersionIdentifier(TestBackendConstants._TEST_VERSION_3_EXT_ID)));
            }
        });

        List<SourceInfo> result = jdbcSourceDao.getSourceInfos(test);
        assertEquals(2, result.size());
        assertEquals(TestBackendConstants._TEST_SOURCE_1_EXT_ID, result.get(0).getRef());
        assertEquals(TestBackendConstants._TEST_SOURCE_2_EXT_ID, result.get(1).getRef());
        assertEquals(TestBackendConstants._TEST_VERSION_1_EXT_ID, result.get(0).getVersion());
        assertEquals(TestBackendConstants._TEST_VERSION_3_EXT_ID, result.get(1).getVersion());
        assertEquals(TestBackendConstants._TEST_SOURCE_1_LINK, result.get(0).getLink());
        assertEquals(TestBackendConstants._TEST_SOURCE_2_LINK, result.get(1).getLink());

    }

    /**
     * test public List<Number> getSourcesForLink(String link)
     *
     *
     */
    @Test
    public void tesGetSourcesForLink() {
        System.out.println(" test getSourcesForLink");

        String substring = "http://nl.wikipedia.org";
        List<Number> result = jdbcSourceDao.getSourcesForLink(substring);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
    }

    /* Test of retrieveVersionList method, of class JdbcSourceDao.
     */
    @Test
    public void testRetrieveVersionList() {
        System.out.println("retrieveVersionList");
        Number sourceID = 1;
        List<Number> result = jdbcSourceDao.retrieveVersionList(sourceID);
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
    }
}
