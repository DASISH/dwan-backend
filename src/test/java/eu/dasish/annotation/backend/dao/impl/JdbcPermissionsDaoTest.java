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
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.UserWithPermission;
import java.util.List;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author olhsha
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-config/dataSource.xml", "/spring-config/permissionsDao.xml"})
public class JdbcPermissionsDaoTest extends JdbcResourceDaoTest{
    
    @Autowired
    JdbcPermissionsDao jdbcPermissionsDao; 
    TestInstances testInstances = new TestInstances();
    
    /**
     * Test of retrievePermissions method, of class PermissionsDao.
     * public List<UserWithPermission> retrievePermissions(Number annotationId)
     */
    @Test
    @Ignore
    public void testRetrievePermissions() {
        Number testAnnotationId = 2;
        final String external_1 = TestBackendConstants._TEST_USER_3_EXT_ID;
        final String external_2 = TestBackendConstants._TEST_USER_4_EXT_ID;
        final String external_3 = TestBackendConstants._TEST_USER_5_EXT_ID;
        final int principal_1 = 3;
        final int principal_2 = 4;
        final int principal_3 = 5;
        String permission_1 = "owner";
        String permission_2 = "writer";
        String permission_3 = "reader";
        
//        mockery.checking(new Expectations() {
//            {
//                oneOf(userDao).getExternalID(principal_1);
//                will(returnValue(new UserIdentifier(external_1)));
//                
//                oneOf(userDao).getExternalID(principal_2);
//                will(returnValue(new UserIdentifier(external_2)));
//                
//                oneOf(userDao).getExternalID(principal_3);
//                will(returnValue(new UserIdentifier(external_3)));
//                
//            }
//        });
        
                
        List<UserWithPermission> test = jdbcPermissionsDao.retrievePermissions(testAnnotationId);
        assertEquals(external_1, test.get(0).getRef());
        assertEquals(external_2, test.get(1).getRef());
        assertEquals(external_3, test.get(2).getRef());
        assertEquals(permission_1, test.get(0).getPermission().value());
        assertEquals(permission_2, test.get(1).getPermission().value());
        assertEquals(permission_3, test.get(2).getPermission().value());
        
    }

    
}
