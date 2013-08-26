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

import eu.dasish.annotation.backend.dao.DBIntegrityService;
import com.sun.jersey.api.client.GenericType;
import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.schema.Annotation;
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
import java.sql.Timestamp;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.xml.datatype.DatatypeConfigurationException;
import org.springframework.mock.web.MockHttpServletRequest;
/**
 *
 * @author olhsha
 */

@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-test-config/mockery.xml", "/spring-test-config/mockDBIntegrityService.xml", 
"/spring-test-config/mockAnnotationDao.xml","/spring-test-config/mockUserDao.xml", "/spring-test-config/mockNotebookDao.xml",
"/spring-test-config/mockSourceDao.xml", "/spring-test-config/mockVersionDao.xml", "/spring-test-config/mockCachedRepresentationDao.xml"})
public class AnnotationResourceTest {
    
    @Autowired
    private Mockery mockery;
    @Autowired
    private DBIntegrityService daoDispatcher;
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
        final String externalIDstring= TestBackendConstants._TEST_ANNOT_2_EXT;
        final int annotationID = 2;        
        final Annotation expectedAnnotation = (new TestInstances()).getAnnotationOne();
        
        mockery.checking(new Expectations() {
            {
                oneOf(daoDispatcher).setServiceURI(with(any(String.class)));
                will(doAll());
                
                oneOf(daoDispatcher).getAnnotationInternalIdentifier(with(any(UUID.class)));                
                will(returnValue(annotationID));                
                
                oneOf(daoDispatcher).getAnnotation(annotationID);                
                will(returnValue(expectedAnnotation));
            }
        });
        
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setServletPath(TestBackendConstants._TEST_SERVLET_URI);        
        annotationResource.setHttpRequest(httpServletRequest);
         
        JAXBElement<Annotation> result = annotationResource.getAnnotation(externalIDstring);
        assertEquals(expectedAnnotation, result.getValue());
    }
    
    /**
     * Test of deleteAnnotation method, of class AnnotationResource.
     */
    @Test
    public void testDeleteAnnotation() throws SQLException {
        System.out.println("deleteAnnotation");
        //final Number annotationID = daoDispatcher.getAnnotationInternalIdentifier(UUID.fromString(UUID));
        //int[] resultDelete = daoDispatcher.deleteAnnotation(annotationID);
       
        final int[] mockDelete = new int[4];
        mockDelete[0]=1; // # deleted annotations
        mockDelete[3]=1; // # deleted annotation_prinipal_permissions
        mockDelete[2]=2; // # deleted  annotations_target_sources, (5,3), (5,4)
        mockDelete[3]=1; // # deletd sources, 4
        mockery.checking(new Expectations() {
            {  
                oneOf(daoDispatcher).getAnnotationInternalIdentifier(with(aNonNull(UUID.class)));              
                will(returnValue(5));     
                
                oneOf(daoDispatcher).deleteAnnotation(5);
                will(returnValue(mockDelete));
            }
        });
        
        String result = annotationResource.deleteAnnotation(TestBackendConstants._TEST_ANNOT_5_EXT);
        assertEquals("1", result);
    }
    
    /**
     * Test of createAnnotation method, of class AnnotationResource.
     */
    @Test
    public void testCreateAnnotation() throws SQLException, InstantiationException, IllegalAccessException, ServletException, DatatypeConfigurationException {
        System.out.println("test createAnnotation");
        final Annotation annotationToAdd = new GenericType<Annotation>(){}.getRawClass().newInstance();
        
//        Number userID = null;
//        if (remoteUser != null) {
//            userID = daoDispatcher.getUserInternalIdentifier(UUID.fromString(remoteUser));
//        }
//        Number newAnnotationID =  daoDispatcher.addUsersAnnotation(annotation, userID);
//        Annotation newAnnotation = daoDispatcher.getAnnotation(newAnnotationID);
        final String ownerString = "5";
        final Number ownerID =  5;
        final Number newAnnotationID = 6;
        final Annotation addedAnnotation = annotationToAdd;
        ResourceREF owner = new ResourceREF();
        owner.setRef(ownerString);
        addedAnnotation.setOwner(owner);
        addedAnnotation.setURI((UUID.randomUUID()).toString());        
        addedAnnotation.setTimeStamp(Helpers.setXMLGregorianCalendar(Timestamp.valueOf("2013-08-12 11:25:00.383000")));
        
        mockery.checking(new Expectations() {
            {
                oneOf(daoDispatcher).setServiceURI(with(any(String.class)));
                will(doAll());
                
                oneOf(daoDispatcher).getUserInternalIdentifier(with(aNonNull(UUID.class)));
                will(returnValue(ownerID));
                
                oneOf(daoDispatcher).addUsersAnnotation(annotationToAdd, ownerID);
                will(returnValue(newAnnotationID));
                
                oneOf(daoDispatcher).getAnnotation(newAnnotationID);
                will(returnValue(addedAnnotation));
            }
        });
        
        
        
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteUser(TestBackendConstants._TEST_USER_5_EXT_ID);         
        httpServletRequest.setServletPath(TestBackendConstants._TEST_SERVLET_URI);     
        annotationResource.setHttpRequest(httpServletRequest);
        
        JAXBElement<Annotation> result = annotationResource.createAnnotation(annotationToAdd); 
        assertEquals(addedAnnotation.getOwner().getRef(), result.getValue().getOwner().getRef());
        assertEquals(addedAnnotation.getURI(), result.getValue().getURI());
        assertEquals(addedAnnotation.getHeadline(), result.getValue().getHeadline());
        assertEquals(addedAnnotation.getPermissions(), result.getValue().getPermissions());
        assertEquals(addedAnnotation.getTargetSources(), result.getValue().getTargetSources()); 
        assertEquals(addedAnnotation.getTimeStamp(), result.getValue().getTimeStamp());
        assertEquals(addedAnnotation.getBody(), result.getValue().getBody());
        
    }
}
