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
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.User;
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
@ContextConfiguration({"/spring-test-config/dataSource.xml", "/spring-config/userDao.xml"})
public class JdbcUserDaoTest extends JdbcResourceDaoTest {

    @Autowired
    JdbcUserDao jdbcUserDao;
    TestInstances testInstances = new TestInstances(TestBackendConstants._TEST_SERVLET_URI);
    
     /**
     * Test of stringURItoExternalID method
     * public String stringURItoExternalID(String uri);
     */
    @Test
    public void testStringURItoExternalID() {
        System.out.println("test stringURItoExternalID");
        jdbcUserDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_users);
        String randomUUID = UUID.randomUUID().toString();
        String uri = TestBackendConstants._TEST_SERVLET_URI_users + randomUUID;
        String externalID = jdbcUserDao.stringURItoExternalID(uri);
        assertEquals(randomUUID, externalID);
    }
    
    /**
     * Test of externalIDtoURI method
     * public String externalIDtoURI(String externalID);
     */
    @Test
    public void testExternalIDtoURI() {
        System.out.println("test stringURItoExternalID");
        jdbcUserDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_users);
        String randomUUID = UUID.randomUUID().toString();
        String uri = TestBackendConstants._TEST_SERVLET_URI_users+randomUUID;
        String uriResult = jdbcUserDao.externalIDtoURI(randomUUID);
        assertEquals(uri, uriResult);
    }

    /**
     * Test of getInternalID method, of class JdbcUserDao. Number
     * getInternalID(UUID UUID);
     */
    @Test
    public void testGetInternalID() {
        Number testOne = jdbcUserDao.getInternalID(UUID.fromString("00000000-0000-0000-0000-000000000113"));
        assertEquals(3, testOne.intValue());

        Number testTwo = jdbcUserDao.getInternalID(UUID.fromString("00000000-0000-0000-0000-000000000ccc"));
        assertEquals(null, testTwo);

    }

    /**
     * public UUID getExternalID(Number internalId)
     */
    @Test
    public void testGetExternalID() {
        UUID testOne = jdbcUserDao.getExternalID(3);
        assertEquals("00000000-0000-0000-0000-000000000113", testOne.toString());

        UUID testTwo = jdbcUserDao.getExternalID(null);
        assertEquals(null, testTwo);
    }

    @Test
    public void testGetUser() {
        System.out.println("test getUser");
        jdbcUserDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_users);
        User result = jdbcUserDao.getUser(1);
        assertEquals("Twan", result.getDisplayName());
        assertEquals("Twan.Goosen@mpi.nl", result.getEMail());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI_users+"00000000-0000-0000-0000-000000000111", result.getURI());
    }

    @Test
    public void testAddUser() {
        System.out.println("test addUser");
        jdbcUserDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_users);
        String freshUserName = "Guilherme";
        String freshUserEmail = "guisil@mpi.nl";

        User freshUser = new User();
        freshUser.setDisplayName(freshUserName);
        freshUser.setEMail(freshUserEmail);

        Number result = jdbcUserDao.addUser(freshUser, "secret X");
        assertEquals(12, result.intValue());
        User addedUser = jdbcUserDao.getUser(result);
        assertEquals(freshUserName, addedUser.getDisplayName());
        assertEquals(freshUserEmail, addedUser.getEMail());
        assertFalse(null == jdbcUserDao.stringURItoExternalID(addedUser.getURI()));
    }

    @Test
    public void testDeleteUser() {
        System.out.println("test deleteUser");
        jdbcUserDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_users);

        int result = jdbcUserDao.deleteUser(10);
        assertEquals(1, result);
        User check = jdbcUserDao.getUser(10);
        assertTrue(null==check);
    }
    
    
    @Test
    public void testUserIsInUse(){
        assertTrue(jdbcUserDao.userIsInUse(1));
        assertTrue(jdbcUserDao.userIsInUse(3));
        assertFalse(jdbcUserDao.userIsInUse(10));
    }

    @Test
    public void tesUserExists() {
        System.out.println("test userExists");
        jdbcUserDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI_users);

        User freshUser = new User();
        freshUser.setDisplayName("Guilherme");
        freshUser.setEMail("guisil@mpi.nl");
        assertEquals(false,jdbcUserDao.userExists(freshUser));

        User user = new User();
        user.setDisplayName("Olha");
        user.setEMail("Olha.Shakaravska@mpi.nl");
        assertTrue(jdbcUserDao.userExists(user));
    }
    
      /**
     * Test of getPrincipalIDsWithPermission method, of class JdbcNotebookDao.
     */
    @Test
    public void testGetPrincipalIDsWithPermission() {
        System.out.println("gtest getPrincipalIDsWithPermission");
        List<Number> expResult = new ArrayList<Number>();
        expResult.add(2);
        expResult.add(4);
        List result = jdbcUserDao.getPrincipalIDsWithPermissionForNotebook(1, Permission.WRITER);
        assertEquals(expResult, result);
    }

}
