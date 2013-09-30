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
import eu.dasish.annotation.schema.User;
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
    TestInstances testInstances = new TestInstances();

    /**
     * Test of getInternalID method, of class JdbcUserDao. Number
     * getInternalID(UUID UUID);
     */
    @Test
    public void testGetInternalID() {
        Number testOne = jdbcUserDao.getInternalID(UUID.fromString(TestBackendConstants._TEST_USER_3_EXT_ID));
        assertEquals(3, testOne.intValue());

        Number testTwo = jdbcUserDao.getInternalID(UUID.fromString(TestBackendConstants._TEST_USER_XXX_EXT_ID));
        assertEquals(null, testTwo);

        Number testThree = jdbcUserDao.getInternalID(null);
        assertEquals(null, testThree);
    }

    /**
     * public UUID getExternalID(Number internalId)
     */
    @Test
    public void testGetExternalID() {
        UUID testOne = jdbcUserDao.getExternalID(3);
        assertEquals(TestBackendConstants._TEST_USER_3_EXT_ID, testOne.toString());

        UUID testTwo = jdbcUserDao.getExternalID(null);
        assertEquals(null, testTwo);
    }

    @Test
    public void testGetUser() {
        System.out.println("test getUser");
        jdbcUserDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI);
        Number internalID = 1;
        User result = jdbcUserDao.getUser(internalID);
        assertEquals("a user", result.getDisplayName());
        assertEquals("a.user@gmail.com", result.getEMail());
        assertEquals(TestBackendConstants._TEST_SERVLET_URI + "/users/"+TestBackendConstants._TEST_UID_1_, result.getURI());
    }

    @Test
    public void testAddUser() {
        System.out.println("test addUser");
        jdbcUserDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI);
        String freshUserName = "Guilherme";
        String freshUserEmail = "guisil@mpi.nl";

        User freshUser = new User();
        freshUser.setDisplayName(freshUserName);
        freshUser.setEMail(freshUserEmail);

        Number result = jdbcUserDao.addUser(freshUser, "secret X");
        assertEquals(7, result.intValue());
        User addedUser = jdbcUserDao.getUser(result);
        assertEquals(freshUserName, addedUser.getDisplayName());
        assertEquals(freshUserEmail, addedUser.getEMail());
        assertFalse(null == jdbcUserDao.stringURItoExternalID(addedUser.getURI()));
    }

    @Test
    public void testDeleteUser() {
        System.out.println("test deleteUser");
        jdbcUserDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI);

        int result = jdbcUserDao.deleteUser(6);
        assertEquals(1, result);
        User check = jdbcUserDao.getUser(6);
        assertTrue(null==check);
    }
    
    
    @Test
    public void testUserIsInUse(){
        assertTrue(jdbcUserDao.userIsInUse(2));
        assertTrue(jdbcUserDao.userIsInUse(5));
        assertFalse(jdbcUserDao.userIsInUse(6));
    }

    @Test
    public void tesUserExists() {
        System.out.println("test userExists");
        jdbcUserDao.setServiceURI(TestBackendConstants._TEST_SERVLET_URI);

        User freshUser = new User();
        freshUser.setDisplayName("Guilherme");
        freshUser.setEMail("guisil@mpi.nl");
        assertEquals(false,jdbcUserDao.userExists(freshUser));

        User user = new User();
        user.setDisplayName("Olha");
        user.setEMail("olhsha@mpi.nl");
        assertTrue(jdbcUserDao.userExists(user));

    }
}
