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
import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.backend.MockObjectsFactory;
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationActionName;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.ResourceREF;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.SourceInfo;
import eu.dasish.annotation.schema.SourceInfoList;
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
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.ws.rs.core.UriInfo;
import javax.xml.datatype.DatatypeConfigurationException;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 * @author olhsha
 */

@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-test-config/mockery.xml", "/spring-test-config/mockDBIntegrityService.xml", 
"/spring-test-config/mockAnnotationDao.xml","/spring-test-config/mockUserDao.xml", "/spring-test-config/mockNotebookDao.xml",
"/spring-test-config/mockSourceDao.xml", "/spring-test-config/mockCachedRepresentationDao.xml"})
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
        
       
        Mockery newMockery = new Mockery();
        MockObjectsFactory mockFactory = new MockObjectsFactory(newMockery);
        final UriInfo uriInfo = mockFactory.newUriInfo();
       
        newMockery.checking(new Expectations() {
            {
                
                oneOf(uriInfo).getBaseUri();
                will(returnValue(URI.create("http://localhost:8080/annotator-backend/api/")));
              
            }
        });
        
        
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
        
//        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
//        httpServletRequest.setContextPath("annotator-backend");
//        httpServletRequest.setServletPath(TestBackendConstants._TEST_SERVLET_URI);        
//        annotationResource.setHttpRequest(httpServletRequest);
        
        
        annotationResource.setUriInfo(uriInfo);
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
        
        Mockery newMockery = new Mockery();
        MockObjectsFactory mockFactory = new MockObjectsFactory(newMockery);
        final UriInfo uriInfo = mockFactory.newUriInfo();
       
        newMockery.checking(new Expectations() {
            {
                
                oneOf(uriInfo).getBaseUri();
                will(returnValue(URI.create("http://localhost:8080/annotator-backend/api/")));
              
            }
        });
     
        mockery.checking(new Expectations() {
            {  
             
                oneOf(daoDispatcher).setServiceURI(with(any(String.class)));
                will(doAll());
                
                oneOf(daoDispatcher).getAnnotationInternalIdentifier(with(aNonNull(UUID.class)));              
                will(returnValue(5));     
                
                oneOf(daoDispatcher).deleteAnnotation(5);
                will(returnValue(mockDelete));
            }
        });
        
        annotationResource.setUriInfo(uriInfo);
        String result = annotationResource.deleteAnnotation(TestBackendConstants._TEST_ANNOT_5_EXT);
        assertEquals("1 annotation(s) deleted.", result);
    }
    
    /**
     * Test of createAnnotation method, of class AnnotationResource.
     */
    @Test
    public void testCreateAnnotation() throws SQLException, InstantiationException, IllegalAccessException, ServletException, DatatypeConfigurationException {
        System.out.println("test createAnnotation");
        
        final Annotation annotationToAdd = new Annotation();
        final String ownerString = "5";
        final Number ownerID =  5;
        final Number newAnnotationID = 6;
        String bodyMimeType = "text/plain";
        String bodyText = "blah";
        String headline = "headline";         
        ResourceREF owner = new ResourceREF();
        AnnotationBody body   = new AnnotationBody();
        SourceInfoList sourceInfoList = new SourceInfoList();
        annotationToAdd.setTargetSources(sourceInfoList);
        annotationToAdd.setBody(body);
        annotationToAdd.setOwner(owner);      
        annotationToAdd.setTimeStamp(Helpers.setXMLGregorianCalendar(Timestamp.valueOf("2013-08-12 11:25:00.383000")));
        annotationToAdd.setHeadline(headline);
        annotationToAdd.setTargetSources(sourceInfoList);
        owner.setRef(ownerString);
        body.setMimeType(bodyMimeType);
        body.setValue(bodyText);
        SourceInfo sourceInfo = new SourceInfo();
        sourceInfo.setLink("google.nl");
        sourceInfo.setRef(UUID.randomUUID().toString());
        sourceInfo.setVersion("vandaag");
        
        final List<Number> sources = new ArrayList<Number>();
        sources.add(6);
        
        final Annotation addedAnnotation = (new ObjectFactory()).createAnnotation(annotationToAdd).getValue();
        addedAnnotation.setURI("http://localhost:8080/annotator-backend/api/annotations/"+UUID.randomUUID().toString());
        
        Mockery newMockery = new Mockery();
        MockObjectsFactory mockFactory = new MockObjectsFactory(newMockery);
        final UriInfo uriInfo = mockFactory.newUriInfo();
       
        newMockery.checking(new Expectations() {
            {
                
                oneOf(uriInfo).getBaseUri();
                will(returnValue(URI.create("http://localhost:8080/annotator-backend/api/")));
              
            }
        });
        
        mockery.checking(new Expectations() {
            {
                  
                
                
                oneOf(daoDispatcher).setServiceURI(with(any(String.class)));
                will(doAll());
                
                oneOf(daoDispatcher).getUserInternalIdentifier(with(aNonNull(UUID.class)));
                will(returnValue(ownerID));
                
                oneOf(daoDispatcher).addUsersAnnotation(ownerID, annotationToAdd);
                will(returnValue(newAnnotationID));
                
                oneOf(daoDispatcher).getAnnotation(newAnnotationID);
                will(returnValue(addedAnnotation));
                
                oneOf(daoDispatcher).getSourcesWithNoCachedRepresentation(newAnnotationID);
                will(returnValue(sources));
            }
        });
        
       
        annotationResource.setUriInfo(uriInfo);
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteUser(TestBackendConstants._TEST_USER_3_EXT_ID);      
        annotationResource.setHttpRequest(httpServletRequest);
      
        
        JAXBElement<ResponseBody> result = annotationResource.createAnnotation(annotationToAdd);
        Annotation newAnnotation = result.getValue().getAnnotationResponse().getContent().getAnnotation();
        AnnotationActionName actionName = result.getValue().getAnnotationResponse().getActions().getAction().getAction();
        assertEquals(addedAnnotation.getOwner().getRef(), newAnnotation.getOwner().getRef());
        assertEquals(addedAnnotation.getURI(), newAnnotation.getURI());
        assertEquals(addedAnnotation.getHeadline(), newAnnotation.getHeadline());
        assertEquals(addedAnnotation.getTargetSources(), newAnnotation.getTargetSources()); 
        assertEquals(addedAnnotation.getTimeStamp(), newAnnotation.getTimeStamp());
        assertEquals(addedAnnotation.getBody(), newAnnotation.getBody());
        assertEquals(AnnotationActionName.CREATE_CACHED_REPRESENTATION, actionName);
    }
}
