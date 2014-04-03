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

import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.PrincipalCannotBeDeleted;
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.Principal;
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
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-config/principalDao.xml"})
public class JdbcPrincipalDaoTest extends JdbcResourceDaoTest {

    @Autowired
    JdbcPrincipalDao jdbcPrincipalDao;
    TestInstances testInstances = new TestInstances(TestBackendConstants._TEST_SERVLET_URI);
    
     /**
     * Test of stringURItoExternalID method
     * public String stringURItoExternalID(String uri);
     */
    @Test
    public void testStringURItoExternalID() {
        System.out.println("test stringURItoExternalID");
        jdbcPrincipalDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_principals);
        String randomUUID = UUID.randomUUID().toString();
        String uri = TestBackendConstants._TEST_SERVLET_URI_principals + randomUUID;
        String externalID = jdbcPrincipalDao.stringURItoExternalID(uri);
        assertEquals(randomUUID, externalID);
    }
    
    /**
     * Test of externalIDtoURI method
     * public String externalIDtoURI(String externalID);
     */
    @Test
    public void testExternalIDtoURI() {
        System.out.println("test stringURItoExternalID");
        jdbcPrincipalDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_principals);
        String randomUUID = UUID.randomUUID().toString();
        String uri = TestBackendConstants._TEST_SERVLET_URI_principals+randomUUID;
        String uriResult = jdbcPrincipalDao.externalIDtoURI(randomUUID);
        assertEquals(uri, uriResult);
    }

    /**
     * Test of getInternalID method, of class JdbcPrincipalDao. Number
     * getInternalID(UUID UUID);
     */
    @Test
    public void testGetInternalID() throws NotInDataBaseException{
        Number testOne = jdbcPrincipalDao.getInternalID(UUID.fromString("00000000-0000-0000-0000-000000000113"));
        assertEquals(3, testOne.intValue());

        try {
        Number testTwo = jdbcPrincipalDao.getInternalID(UUID.fromString("00000000-0000-0000-0000-000000000ccc"));
        assertEquals(null, testTwo);
        } catch (NotInDataBaseException e){
            System.out.println(e);
        }

    }

    /**
     * public UUID getExternalID(Number internalId)
     */
    @Test
    public void testGetExternalID() {
        UUID testOne = jdbcPrincipalDao.getExternalID(3);
        assertEquals("00000000-0000-0000-0000-000000000113", testOne.toString());

    }

    @Test
    public void testGetPrincipal() {
        System.out.println("test getPrincipal");
        jdbcPrincipalDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_principals);
        Principal result = jdbcPrincipalDao.getPrincipal(1);
        assertEquals("Twan", result.getDisplayName());
        assertEquals("Twan.Goosen@mpi.nl", result.getEMail());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_principals+"00000000-0000-0000-0000-000000000111", result.getURI());
    }

    @Test
    public void testAddPrincipal() throws NotInDataBaseException{
        System.out.println("test addPrincipal");
        jdbcPrincipalDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_principals);
        String freshPrincipalName = "Guilherme";
        String freshPrincipalEmail = "guisil@mpi.nl";

        Principal freshPrincipal = new Principal();
        freshPrincipal.setDisplayName(freshPrincipalName);
        freshPrincipal.setEMail(freshPrincipalEmail);

        Number result = jdbcPrincipalDao.addPrincipal(freshPrincipal, "secret X");
        assertEquals(12, result.intValue());
        Principal addedPrincipal = jdbcPrincipalDao.getPrincipal(result);
        assertEquals(freshPrincipalName, addedPrincipal.getDisplayName());
        assertEquals(freshPrincipalEmail, addedPrincipal.getEMail());
        assertFalse(null == jdbcPrincipalDao.stringURItoExternalID(addedPrincipal.getURI()));
    }

    @Test
    public void testDeletePrincipal() throws PrincipalCannotBeDeleted{
        System.out.println("test deletePrincipal");
        jdbcPrincipalDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_principals);

        int result = jdbcPrincipalDao.deletePrincipal(10);
        assertEquals(1, result);
        assertEquals(0, jdbcPrincipalDao.deletePrincipal(10));
    }
    
    
    @Test
    public void testPrincipalIsInUse(){
        assertTrue(jdbcPrincipalDao.principalIsInUse(1));
        assertTrue(jdbcPrincipalDao.principalIsInUse(3));
        assertFalse(jdbcPrincipalDao.principalIsInUse(10));
    }

    @Test
    public void tesPrincipalExists() {
        System.out.println("test principalExists");
        jdbcPrincipalDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_principals);

        Principal freshPrincipal = new Principal();
        freshPrincipal.setDisplayName("Guilherme");
        freshPrincipal.setEMail("guisil@mpi.nl");
        assertEquals(false,jdbcPrincipalDao.principalExists(freshPrincipal));

        Principal principal = new Principal();
        principal.setDisplayName("Olha");
        principal.setEMail("Olha.Shakaravska@mpi.nl");
        assertTrue(jdbcPrincipalDao.principalExists(principal));
    }
    
      /**
     * Test of getPrincipalIDsWithAccess method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetPrincipalIDsWithAccess() {
        System.out.println("gtest getPrincipalIDsWithAccess");
        List<Number> expResult = new ArrayList<Number>();
        expResult.add(2);
        expResult.add(4);
        List result = jdbcPrincipalDao.getPrincipalIDsWithAccessForNotebook(1, Access.WRITE);
        assertEquals(expResult, result);
    }

}
