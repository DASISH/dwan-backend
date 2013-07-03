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
package eu.dasish.annotation.backend.rest;

import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.identifiers.NotebookIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import java.util.ArrayList;
import java.util.List;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-test-config/dataSource.xml", "/spring-test-config/mockNotebookDao.xml", "/spring-test-config/mockery.xml"})
public class NotebookResourceTest {

    @Autowired
    private Mockery mockery;
    @Autowired
    private NotebookDao notebookDao;
    @Autowired
    private NotebookResource notebookResource;

    public NotebookResourceTest() {
    }

    /**
     * Test of getNotebookInfo method, of class NotebookResource.
     */
    @Test
    public void testGetNotebookInfo() {
        System.out.println("getNotebookInfo");
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteUser("_test_uid_2_");
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).getNotebookInfos(new UserIdentifier(httpServletRequest.getRemoteUser()));
                will(returnValue(new ArrayList<NotebookInfo>()));
            }
        });
        List result = notebookResource.getNotebookInfo(httpServletRequest);
        assertEquals(0, result.size());
    }

    /**
     * Test of getUsersNotebooks method, of class NotebookResource.
     */
    @Test
    public void testGetUsersNotebooks() {
        System.out.println("getUsersNotebooks");
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).getUsersNotebooks(null);
                will(returnValue(new ArrayList<Notebook>()));
            }
        });
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteUser("_test_uid_2_");
        List result = notebookResource.getUsersNotebooks(httpServletRequest);
        assertEquals(0, result.size());
    }

    /**
     * Test of createNotebook method, of class NotebookResource.
     */
    @Test
    public void testCreateNotebook() throws Exception {
        System.out.println("createNotebook");
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteUser("_test_uid_2_");
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).addNotebook(new UserIdentifier(httpServletRequest.getRemoteUser()), null);
                will(returnValue(new NotebookIdentifier("1")));
            }
        });
        String expResult = "/api/notebooks/1";
        String result = notebookResource.createNotebook(httpServletRequest);
        assertEquals(expResult, result.substring(result.length() - expResult.length()));
    }

    /**
     * Test of deleteNotebook method, of class NotebookResource.
     */
    @Test
    public void testDeleteNotebook() {
        System.out.println("deleteNotebook");
        final NotebookIdentifier notebookIdentifier = new NotebookIdentifier("_test_nid_2_");
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).deleteNotebook(notebookIdentifier);
                will(returnValue(1));
            }
        });
        String expResult = "1";
        String result = notebookResource.deleteNotebook(notebookIdentifier);
        assertEquals(expResult, result);
    }
}