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

import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.NotebookIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.NotebookInfos;
import eu.dasish.annotation.schema.ObjectFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.JAXBElement;
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
@ContextConfiguration(locations = {"/spring-test-config/dataSource.xml", "/spring-test-config/mockAnnotationDao.xml", "/spring-test-config/mockUserDao.xml", 
    "/spring-test-config/mockSourceDao.xml", "/spring-test-config/mockNotebookDao.xml", "/spring-test-config/mockPermissionsDao.xml", "/spring-test-config/mockery.xml"})
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
        httpServletRequest.setRemoteUser(TestBackendConstants._TEST_UID_1_);
        
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).getNotebookInfos(new UserIdentifier(httpServletRequest.getRemoteUser()));                
                will(returnValue(new ArrayList<NotebookInfo>()));
            }
        });
        JAXBElement<NotebookInfos> result = notebookResource.getNotebookInfo(httpServletRequest);
        assertEquals(0, result.getValue().getNotebook().size()); // todo: shoudnt this return 3 infos?
    }

    /**
     * Test of getUsersNotebooks method, of class NotebookResource.
     */
    @Test
    public void testGetUsersNotebooks() {
        System.out.println("getUsersNotebooks");
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).getUsersNotebooks(new UserIdentifier(TestBackendConstants._TEST_UID_2_));
                will(returnValue(new ArrayList<Notebook>()));
            }
        });
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteUser(TestBackendConstants._TEST_UID_2_);
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
        httpServletRequest.setRemoteUser(TestBackendConstants._TEST_UID_2_);
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).addNotebook(new UserIdentifier(httpServletRequest.getRemoteUser()), null);
                will(returnValue(new NotebookIdentifier(new UUID(0, 1))));
            }
        });
        String expResult = "/api/notebooks/00000000-0000-0000-0000-000000000001";
        String result = notebookResource.createNotebook(httpServletRequest);  
        assertEquals(expResult, result.substring(result.length() - expResult.length()));
    }

    /**
     * Test of deleteNotebook method, of class NotebookResource.
     */
    @Test
    public void testDeleteNotebook() {
        System.out.println("deleteNotebook");
        final NotebookIdentifier notebookIdentifier = new NotebookIdentifier(new UUID(0, 1));
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
    
    /**
     * Test of getMetadata method, of class NotebookResource. Get all metadata
     * about a specified notebook <nid>, including the information if it is
     * private or not. GET api/notebooks/<nid>/metadata
     */
    @Test
    public void testGetMetadata() {
        System.out.println("test GetMetadata");
        
        final String notebookIdentifier= TestBackendConstants._TEST_NOTEBOOK_3_EXT;
        final int notebookID = 3;
        final NotebookInfo testInfo = new ObjectFactory().createNotebookInfo();
        
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).getNotebookID(new NotebookIdentifier(notebookIdentifier));                
                will(returnValue(notebookID));
                
                oneOf(notebookDao).getNotebookInfo(notebookID); 
               will(returnValue(testInfo)); 
            }
        });
        
        JAXBElement<NotebookInfo> result = notebookResource.getMetadata(notebookIdentifier);
        NotebookInfo entity = result.getValue();
        assertEquals(testInfo.getRef(), entity.getRef());
        assertEquals(testInfo.getTitle(), entity.getTitle());
    }

    
    @Test
    public void testGetAllAnnotations() {
        System.out.println("test getAllAnnotations");        
        final String notebookIdentifier= TestBackendConstants._TEST_NOTEBOOK_3_EXT; 
        final AnnotationIdentifier aIdOne= new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_2_EXT);
        final AnnotationIdentifier aIdTwo= new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_3_EXT);
        final List<AnnotationIdentifier> annotationIds = new ArrayList<AnnotationIdentifier>();
        annotationIds.add(aIdOne);
        annotationIds.add(aIdTwo);
        
        mockery.checking(new Expectations() {
            {
                oneOf(notebookDao).getAnnotationExternalIDs(new NotebookIdentifier(notebookIdentifier));                
                will(returnValue(annotationIds));
                
            }
        });
        
        List<AnnotationIdentifier> result= notebookResource.getAllAnnotations(notebookIdentifier, 0, 0, null, 0);
        assertFalse(null==result);
        assertEquals(aIdOne, result.get(0));
        assertEquals(aIdTwo, result.get(1));
        
        
    }
}