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
import eu.dasish.annotation.schema.Target;
import eu.dasish.annotation.schema.TargetInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-config/TargetDao.xml"})
public class JdbcTargetDaoTest extends JdbcResourceDaoTest {

    @Autowired
    JdbcTargetDao jdbcTargetDao;
    
     /**
     * Test of stringURItoExternalID method
     * public String stringURItoExternalID(String uri);
     */
    @Test
    public void testStringURItoExternalID() {
        System.out.println("test stringURItoExternalID");
        jdbcTargetDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_Targets);
        String randomUUID = UUID.randomUUID().toString();
        String uri = TestBackendConstants._TEST_SERVLET_URI_Targets + randomUUID;
        String externalID = jdbcTargetDao.stringURItoExternalID(uri);
        assertEquals(randomUUID, externalID);
    }
    
    /**
     * Test of externalIDtoURI method
     * public String externalIDtoURI(String externalID);
     */
    @Test
    public void testExternalIDtoURI() {
        System.out.println("test stringURItoExternalID");
        jdbcTargetDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_Targets);
        String randomUUID = UUID.randomUUID().toString();
        String uri = TestBackendConstants._TEST_SERVLET_URI_Targets+randomUUID;
        String uriResult = jdbcTargetDao.externalIDtoURI(randomUUID);
        assertEquals(uri, uriResult);
    }

    /**
     * Test of getExternalID method, of class JdbcTargetDao.
     */
    @Test
    public void testGetExternalID() {
        System.out.println("getExternalID");
        Number internalID = 1;
        UUID result = jdbcTargetDao.getExternalID(internalID);
        assertEquals(TestBackendConstants._TEST_Target_1_EXT_ID, result.toString());
    }

    /**
     * Test of getInternalID method, of class JdbcTargetDao.
     */
    @Test
    public void testGetInternalId() {
        System.out.println("getInternalId");
        UUID externalID = UUID.fromString(TestBackendConstants._TEST_Target_1_EXT_ID);
        Number expResult = 1;
        Number result = jdbcTargetDao.getInternalID(externalID);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getInternalIDFromURI method,
     * public Number getInternalIDFromURI(UUID externalID);
     */
    @Test
    public void testGetInternalIDFRomURI() {
        System.out.println("test getInternalIDFromURI");
        jdbcTargetDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_Targets);
        String uri = TestBackendConstants._TEST_SERVLET_URI_Targets+TestBackendConstants._TEST_Target_1_EXT_ID;
        Number result = jdbcTargetDao.getInternalIDFromURI(uri);
        assertEquals(1, result.intValue());
    }
    

    /**
     * Test of getTarget method, of class JdbcTargetDao.
     */
    @Test
    public void testGetTarget() {
        System.out.println("getTarget");
        Number internalID = 1;
        jdbcTargetDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_Targets);
        Target result = jdbcTargetDao.getTarget(internalID);
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_Targets+TestBackendConstants._TEST_Target_1_EXT_ID, result.getURI());
        assertEquals(TestBackendConstants._TEST_Target_1_LINK, result.getLink());
        assertEquals(TestBackendConstants._TEST_Target_1_VERSION, result.getVersion());
        // TODO :add time stamp test
        
    }

    /**
     * Test of deleteTarget method, of class JdbcTargetDao.
     */
    @Test
    public void testDeleteTarget() {
        System.out.println("deleteTarget");
        // test 1
        // remove the rows from the joint table to keep integrity
        int result = jdbcTargetDao.deleteTarget(1); //the Target is in use, should not be deleted
        assertEquals(0, result); 

        // test 2
        int resultTwo = jdbcTargetDao.deleteTarget(6);// the Target will be deleted because it is not referred by any annotation
        assertEquals(1, resultTwo); 
    }
    
  

    /**
     * Test of addTargetCachedRepresentation method, of class JdbcTargetDao.
     */
    @Test
    public void testAddTargetCachedRepresentation() throws SQLException{
       System.out.println("test addTargetCachedRepresentation");
       assertEquals(1, jdbcTargetDao.addTargetCachedRepresentation(6, 7));

    }
    
    /**
     * Test of addTarget method, of class JdbcTargetDao.
     */
    @Test
    public void testAddTarget() throws SQLException {
        System.out.println("addTarget");

        String link = "http://www.sagradafamilia.cat/";
        Target freshTarget = new Target();
        freshTarget.setLink(link);
        freshTarget.setVersion(TestBackendConstants._TEST_Target_1_VERSION);
        freshTarget.setTimeStamp(null);
        
        Number result = jdbcTargetDao.addTarget(freshTarget);
        assertEquals(8, result);
        // detailed checking
        Target addedTarget = jdbcTargetDao.getTarget(result);
        assertEquals(link, addedTarget.getLink());
        assertEquals(TestBackendConstants._TEST_Target_1_VERSION, addedTarget.getVersion());
        assertTrue(addedTarget.getURI().startsWith(TestBackendConstants._TEST_SERVLET_URI_Targets));
    }

    /**
     * Test of getTargetInfos method, of class JdbcTargetDao.
     */
    @Test
    public void testGetTargetInfos() {
        System.out.println("getTargetInfos");        
        jdbcTargetDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_Targets);
        List<Number> test = new ArrayList<Number>();
        test.add(1);
        test.add(2);
        List<TargetInfo> result = jdbcTargetDao.getTargetInfos(test);
        assertEquals(2, result.size());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_Targets+TestBackendConstants._TEST_Target_1_EXT_ID, result.get(0).getRef());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_Targets+TestBackendConstants._TEST_Target_2_EXT_ID, result.get(1).getRef());
        assertEquals(TestBackendConstants._TEST_Target_1_VERSION, result.get(0).getVersion());
        assertEquals(TestBackendConstants._TEST_Target_2_VERSION, result.get(1).getVersion());
        assertEquals(TestBackendConstants._TEST_Target_1_LINK, result.get(0).getLink());
        assertEquals(TestBackendConstants._TEST_Target_2_LINK, result.get(1).getLink());

    }

    /**
     * test public List<Number> getTargetsForLink(String link)
     *
     *
     */
    @Test
    public void tesGetTargetsForLink() {
        System.out.println(" test getTargetsForLink");

        String substring = "http://nl.wikipedia.org";
        List<Number> result = jdbcTargetDao.getTargetsForLink(substring);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
    }

    /* Test of getCachedRepresentations method, of class JdbcTargetDao.
     */
    @Test
    public void testGetCachedRepresentations() {
        System.out.println("test getCachedRepresentations");
        Number TargetID = 1;
        List<Number> result = jdbcTargetDao.getCachedRepresentations(TargetID);
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
    }
}
