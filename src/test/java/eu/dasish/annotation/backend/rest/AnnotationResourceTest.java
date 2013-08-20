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
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.Annotation;
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
@ContextConfiguration(locations = {"/spring-test-config/dataSource.xml", "/spring-test-config/mockDaoDispatcher.xml", 
    "/spring-test-config/mockery.xml", })
public class AnnotationResourceTest {
    
    @Autowired
    private Mockery mockery;
    @Autowired
    private DaoDispatcher daoDispatcher;
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
        
        //final Number annotationID = daoDispatcher.getAnnotationInternalIdentifier(new AnnotationIdentifier(annotationIdentifier));
        //final Annotation annotation = daoDispatcher.getAnnotation(annotationID);
        mockery.checking(new Expectations() {
            {
                oneOf(daoDispatcher).getAnnotationInternalIdentifier(with(aNonNull(AnnotationIdentifier.class)));                
                will(returnValue(annotationID));                
                
                oneOf(daoDispatcher).getAnnotation(annotationID);                
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
        //final Number annotationID = daoDispatcher.getAnnotationInternalIdentifier(new AnnotationIdentifier(annotationIdentifier));
        //int[] resultDelete = daoDispatcher.deleteAnnotation(annotationID);
       
        mockery.checking(new Expectations() {
            {  
                oneOf(daoDispatcher).getAnnotationInternalIdentifier(with(aNonNull(AnnotationIdentifier.class)));              
                will(returnValue(5));     
                
                oneOf(daoDispatcher).deleteAnnotation(5);
                will(returnValue(1));
            }
        });
        
        String result = annotationResource.deleteAnnotation(TestBackendConstants._TEST_ANNOT_5_EXT);
        assertEquals("1", result);
    }
    
    /**
     * Test of createAnnotation method, of class AnnotationResource.
     */
    @Test
    public void testCreateAnnotation() throws SQLException, InstantiationException, IllegalAccessException, ServletException {
        System.out.println("test createAnnotation");
        final Annotation annotationToAdd = new GenericType<Annotation>(){}.getRawClass().newInstance();
        
//        Number userID = null;
//        if (remoteUser != null) {
//            userID = daoDispatcher.getUserInternalIdentifier(new UserIdentifier(remoteUser));
//        }
//        Number newAnnotationID =  daoDispatcher.addUsersAnnotation(annotation, userID);
//        Annotation newAnnotation = daoDispatcher.getAnnotation(newAnnotationID);
        
        mockery.checking(new Expectations() {
            {
                oneOf(daoDispatcher).getUserInternalIdentifier(with(aNonNull(UserIdentifier.class)));
                will(returnValue(5));
                
                oneOf(daoDispatcher).addUsersAnnotation(annotationToAdd, 5);
                will(returnValue(1));
            }
        });
        
        
        
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteUser(TestBackendConstants._TEST_USER_5_EXT_ID);        
        annotationResource.setHttpRequest(httpServletRequest);
        
        JAXBElement<Annotation> result = annotationResource.createAnnotation(annotationToAdd); 
        assertEquals(String.valueOf(5), result.getValue().getOwner().getRef());
        assertFalse(null == result.getValue().getURI());
        
    }
}
