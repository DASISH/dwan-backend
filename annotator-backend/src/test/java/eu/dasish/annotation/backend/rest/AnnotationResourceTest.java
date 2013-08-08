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

import com.sun.jersey.api.client.GenericType;
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.PermissionsDao;
import eu.dasish.annotation.backend.dao.UserDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.ResourceREF;
import java.sql.SQLException;
import javax.xml.bind.JAXBElement;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.lang.InstantiationException;
import javax.servlet.ServletException;
import org.springframework.mock.web.MockHttpServletRequest;
/**
 *
 * @author olhsha
 */

@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-test-config/dataSource.xml", "/spring-test-config/mockAnnotationDao.xml", "/spring-test-config/mockUserDao.xml", "/spring-test-config/mockPermissionsDao.xml", "/spring-test-config/mockNotebookDao.xml", "/spring-test-config/mockery.xml"})
public class AnnotationResourceTest {
    
    @Autowired
    private Mockery mockery;
    @Autowired
    private AnnotationDao annotationDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private PermissionsDao permissionsDao;
    @Autowired
    private NotebookDao notebookDao;
    
    @Autowired
    private AnnotationResource annotationResource;
    
    public AnnotationResourceTest() {
    }
    
    
    /**
     * Test of getAnnotation method, of class AnnotationResource.
     */
    @Test
    public void testGetAnnotation() throws SQLException {
        System.out.println("getAnnotation");
        final String annotationIdentifier= TestBackendConstants._TEST_ANNOT_2_EXT;
        final int annotationID = 2;        
        final Annotation expectedAnnotation = (new TestInstances()).getAnnotationOne();
        // the result of the mocking chain is the same as the expected annotation.        
        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getInternalID(new AnnotationIdentifier(annotationIdentifier));                
                will(returnValue(annotationID));                
                
                oneOf(annotationDao).getAnnotation(annotationID);                
                will(returnValue(expectedAnnotation));
            }
        });
         
        JAXBElement<Annotation> result = annotationResource.getAnnotation(annotationIdentifier);
        assertEquals(expectedAnnotation, result.getValue());
    }
    
    /**
     * Test of deleteAnnotation method, of class AnnotationResource.
     */
    @Test
    public void testDeleteAnnotation() throws SQLException {
        System.out.println("deleteAnnotation");
        
        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getInternalID(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_5_EXT));                
                will(returnValue(5));     
                
                oneOf(annotationDao).deleteAnnotation(5);
                will(returnValue(1));
            }
        });
        
        String result = annotationResource.deleteAnnotation(TestBackendConstants._TEST_ANNOT_5_EXT);
        assertEquals("1", result);
        
         // now, try to delete the same annotation one more time
        // if it has been already deleted then the method under testing should return 0
        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getInternalID(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_5_EXT));                
                will(returnValue(5));
                
                oneOf(annotationDao).deleteAnnotation(5);
                will(returnValue(0));
            }
        });
        
        result = annotationResource.deleteAnnotation(TestBackendConstants._TEST_ANNOT_5_EXT);
        assertEquals("0", result);
    }
    
    /**
     * Test of createAnnotation method, of class AnnotationResource.
     */
    @Test
    public void testCreateAnnotation() throws SQLException, InstantiationException, IllegalAccessException, ServletException {
        System.out.println("test createAnnotation");
        final Annotation annotationToAdd = new GenericType<Annotation>(){}.getRawClass().newInstance();
        
        final Annotation addedAnnotation = annotationToAdd;
        final AnnotationIdentifier annotationIdentifier = new GenericType<AnnotationIdentifier>(){}.getRawClass().newInstance();
        addedAnnotation.setURI(annotationIdentifier.toString());
        ResourceREF ownerRef = new ResourceREF();
        ownerRef.setRef(String.valueOf(5));
        addedAnnotation.setOwner(ownerRef);
       
        final UserIdentifier owner = new UserIdentifier(TestBackendConstants._TEST_USER_5_EXT_ID);
        
        mockery.checking(new Expectations() {
            {
                oneOf(userDao).getInternalID(owner);
                will(returnValue(5));
                
                oneOf(annotationDao).addAnnotation(annotationToAdd, 5);
                will(returnValue(addedAnnotation));
            
                oneOf(permissionsDao).addAnnotationPrincipalPermission(annotationIdentifier, owner, Permission.OWNER);
                will(returnValue(1));
            }
        });
        
        
        
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteUser(TestBackendConstants._TEST_USER_5_EXT_ID);        
        annotationResource.setHttpRequest(httpServletRequest);
        
        JAXBElement<Annotation> result = annotationResource.createAnnotation(annotationToAdd); 
        assertEquals(String.valueOf(5), result.getValue().getOwner().getRef());
        assertEquals(annotationIdentifier.toString(), result.getValue().getURI());
        
        
    }
}
