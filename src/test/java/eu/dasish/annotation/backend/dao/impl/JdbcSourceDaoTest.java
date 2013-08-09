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
import eu.dasish.annotation.schema.NewOrExistingSourceInfo;
import eu.dasish.annotation.schema.NewOrExistingSourceInfos;
import eu.dasish.annotation.schema.NewSourceInfo;
import eu.dasish.annotation.schema.Source;
import eu.dasish.annotation.schema.SourceInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
     * Test of retrieveSourceIDs method, of class JdbcSourceDao.
     */
    @Test
    public void testRetrieveSourceIDs() {
        System.out.println("retrieveSourceIDs");
        Number annotationID = 2;
        List<Number> result = jdbcSourceDao.retrieveSourceIDs(annotationID);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
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
        //TODO: time stamp is not check: do not know with what to compare :\
    }

    /**
     * Test of deleteSourceVersionRows method, of class JdbcSourceDao.
     */
    @Test
    public void testDeleteSourceVersionRows() {
        System.out.println("DeleteSourceVersionRows");
        Number internalID = 4;
        int result = jdbcSourceDao.deleteSourceVersionRows(internalID);
        assertEquals(1, result);

        Number internalIDNoExist = 5;
        int resultTwo = jdbcSourceDao.deleteSourceVersionRows(internalIDNoExist);
        assertEquals(0, resultTwo);
    }

    /**
     * Test of deleteSource method, of class JdbcSourceDao.
     */
    @Test
    public void testDeleteSource() {
        System.out.println("deleteSource");
        Number internalID = 1;
        int result = jdbcSourceDao.deleteSource(internalID);
        assertEquals(0, result); // the source is in use, should not be deleted

        final Number internalIDToBeDeleted = 4;
        final List<Number> versions = new ArrayList<Number>();
        versions.add(5);
        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).retrieveVersionList(internalIDToBeDeleted);
                will(returnValue(versions));

                oneOf(versionDao).deleteVersion(5);
                will(returnValue(1)); // no other sources refer to this version # 5
            }
        });

        int resultTwo = jdbcSourceDao.deleteSource(internalIDToBeDeleted);
        assertEquals(1, resultTwo); // the source will be deleted because it is not referred by any annotation
    }

    /**
     * Test of addSource method, of class JdbcSourceDao.
     */
    @Test
    public void testAddSource() {
        System.out.println("addSource");

        String link = "http://www.sagradafamilia.cat/";
        String version = null;

        Source freshSource = new Source();
        freshSource.setLink(link);
        freshSource.setVersion(version);

        Source result = jdbcSourceDao.addSource(freshSource);
        assertEquals(link, result.getLink());
        assertEquals(version, result.getVersion());
        assertEquals(5, jdbcSourceDao.getInternalID(new SourceIdentifier(result.getURI())));
        // TODO time stamp is not checked        
    }

    /**
     * Test of getSourceInfos method, of class JdbcSourceDao.
     */
    @Test
    public void testGetSourceInfos() {
        System.out.println("getSourceInfos");
        Number annotationID = 2;

        mockery.checking(new Expectations() {
            {
                oneOf(versionDao).getExternalID(1);
                will(returnValue(new VersionIdentifier(TestBackendConstants._TEST_VERSION_1_EXT_ID)));

                oneOf(versionDao).getExternalID(3);
                will(returnValue(new VersionIdentifier(TestBackendConstants._TEST_VERSION_3_EXT_ID)));
            }
        });

        List<SourceInfo> result = jdbcSourceDao.getSourceInfos(annotationID);
        assertEquals(2, result.size());
        assertEquals(TestBackendConstants._TEST_SOURCE_1_EXT_ID, result.get(0).getRef());
        assertEquals(TestBackendConstants._TEST_SOURCE_2_EXT_ID, result.get(1).getRef());
        assertEquals(TestBackendConstants._TEST_VERSION_1_EXT_ID, result.get(0).getVersion());
        assertEquals(TestBackendConstants._TEST_VERSION_3_EXT_ID, result.get(1).getVersion());
        assertEquals(TestBackendConstants._TEST_SOURCE_1_LINK, result.get(0).getLink());
        assertEquals(TestBackendConstants._TEST_SOURCE_2_LINK, result.get(1).getLink());

    }

    /**
     * Test of contructNewOrExistingSourceInfo method, of class JdbcSourceDao.
     */
    @Test
    public void testContructNewOrExistingSourceInfo() {
        System.out.println("contructNewOrExistingSourceInfo");

        List<SourceInfo> sourceInfoList = new ArrayList<SourceInfo>();

        SourceInfo sourceInfoOne = new SourceInfo();
        sourceInfoOne.setLink(TestBackendConstants._TEST_SOURCE_1_LINK);
        sourceInfoOne.setRef(TestBackendConstants._TEST_SOURCE_1_EXT_ID);
        sourceInfoOne.setRef(TestBackendConstants._TEST_VERSION_1_EXT_ID);

        SourceInfo sourceInfoTwo = new SourceInfo();
        sourceInfoTwo.setLink(TestBackendConstants._TEST_SOURCE_2_LINK);
        sourceInfoTwo.setRef(TestBackendConstants._TEST_SOURCE_2_EXT_ID);
        sourceInfoTwo.setRef(TestBackendConstants._TEST_VERSION_3_EXT_ID);

        sourceInfoList.add(sourceInfoOne);
        sourceInfoList.add(sourceInfoTwo);

        NewOrExistingSourceInfos result = jdbcSourceDao.contructNewOrExistingSourceInfo(sourceInfoList);
        assertEquals(2, result.getTarget().size());
        assertEquals(sourceInfoOne, result.getTarget().get(0).getSource());
        assertEquals(sourceInfoTwo, result.getTarget().get(1).getSource());

    }

    /**
     * Test of addTargetSources method, of class JdbcSourceDao. public
     * Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo>
     * addTargetSources(Number annotationID, List<NewOrExistingSourceInfo>
     * sources)
     */
    @Test
    public void testAddTargetSourcesOnExistingSource() {
        System.out.println("addTargetSources : adding the old source");
   
        NewOrExistingSourceInfo noesi = new NewOrExistingSourceInfo();
        SourceInfo si = new SourceInfo();
        si.setLink(TestBackendConstants._TEST_SOURCE_1_LINK);
        si.setRef(TestBackendConstants._TEST_SOURCE_1_EXT_ID);
        si.setVersion(TestBackendConstants._TEST_VERSION_1_EXT_ID);
        noesi.setSource(si);
        
        List<NewOrExistingSourceInfo> listnoesi = new ArrayList<NewOrExistingSourceInfo>();
        listnoesi.add(noesi);
        
        Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo> result = jdbcSourceDao.addTargetSources(5, listnoesi);
        assertEquals(1, result.size());
        assertEquals(result.get(noesi), noesi);
    }    
        
       
     /**
     * Test of addTargetSources method, of class JdbcSourceDao. public
     * Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo>
     * addTargetSources(Number annotationID, List<NewOrExistingSourceInfo>
     * sources)
     */
    @Test
     public void testAddTargetSourcesOnNewSource() {       
        System.out.println("addTargetSources : adding the new source");
   
        NewOrExistingSourceInfo noesi = new NewOrExistingSourceInfo();
        NewSourceInfo nsi = new NewSourceInfo();
        nsi.setLink(TestBackendConstants._TEST_NEW_SOURCE_LINK);
        nsi.setId(TestBackendConstants._TEST_TEMP_SOURCE_ID);
        nsi.setVersion(null);
        noesi.setNewSource(nsi);
        
        List<NewOrExistingSourceInfo> listnoesiTwo = new ArrayList<NewOrExistingSourceInfo>();
        listnoesiTwo.add(noesi);
        
        Map<NewOrExistingSourceInfo, NewOrExistingSourceInfo> result = jdbcSourceDao.addTargetSources(5, listnoesiTwo);
        assertEquals(1, result.size());
        assertEquals(noesi.getNewSource().getLink(), result.get(noesi).getSource().getLink());
        assertEquals(noesi.getNewSource().getVersion(), result.get(noesi).getSource().getVersion());
        
        SourceIdentifier sourceIdentifier = new SourceIdentifier(result.get(noesi).getSource().getRef());
        assertFalse(null == sourceIdentifier.getUUID()); // check if a proper uuid has been assigned 
        
    }
}
