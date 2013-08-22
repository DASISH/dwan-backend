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

public class JdbcUserDaoTest extends JdbcResourceDaoTest{
    
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
    
    
}
