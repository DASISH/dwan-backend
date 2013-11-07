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
import eu.dasish.annotation.backend.MockObjectsFactoryRest;
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationBody.TextBody;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.TargetInfoList;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.AnnotationActionName;
import eu.dasish.annotation.schema.Permission;
import java.io.IOException;
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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 * @author olhsha
 */

@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-test-config/mockeryRest.xml", "/spring-test-config/mockDBIntegrityService.xml", "/spring-config/jaxbMarshallerFactory.xml"})
public class AnnotationResourceTest {
    
    @Autowired
    private Mockery mockeryRest;
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
    public void testGetAnnotation() throws SQLException, JAXBException, Exception {
        System.out.println("getAnnotation");
        final String externalIDstring= TestBackendConstants._TEST_ANNOT_2_EXT;
        final Annotation expectedAnnotation = (new TestInstances()).getAnnotationOne();
        final ResourceJaxbMarshallerProvider rmp = new ResourceJaxbMarshallerProvider();
       
        Mockery newMockery = new Mockery();
        MockObjectsFactoryRest mockFactory = new MockObjectsFactoryRest(newMockery);
        final UriInfo uriInfo = mockFactory.newUriInfo();
        final Providers providers = mockFactory.newProviders();
        final HttpServletRequest httpServletRequest = mockFactory.newHttpServletRequest();
        newMockery.checking(new Expectations() {
            {
                
                oneOf(uriInfo).getBaseUri();
                will(returnValue(URI.create("http://localhost:8080/annotator-backend/api/")));
                
                oneOf(providers).getContextResolver(Marshaller.class, MediaType.WILDCARD_TYPE);
                will(returnValue(rmp));
                
                oneOf(httpServletRequest).getRemoteUser();
                will(returnValue("twan"));
              
            }
        });
        
        
        mockeryRest.checking(new Expectations() {
            {
                
                oneOf(daoDispatcher).setServiceURI(with(any(String.class)));
                will(doAll());
                
                oneOf(daoDispatcher).getUserInternalIDFromRemoteID("twan");
                will(returnValue(3)); 
                
                oneOf(daoDispatcher).getAnnotationInternalIdentifier(with(any(UUID.class)));                
                will(returnValue(2));    
                
                oneOf(daoDispatcher).getPermission(2, 3);
                will(returnValue(Permission.OWNER));  
                
                oneOf(daoDispatcher).getAnnotation(2);                
                will(returnValue(expectedAnnotation));
            }
        });
        

        
        annotationResource.setUriInfo(uriInfo);
        annotationResource.setProviders(providers);
        annotationResource.setHttpRequest(httpServletRequest);
        JAXBElement<Annotation> result = annotationResource.getAnnotation(externalIDstring);
        assertEquals(expectedAnnotation, result.getValue());
    }
    
    /**
     * Test of deleteAnnotation method, of class AnnotationResource.
     */
    @Test
    public void testDeleteAnnotation() throws SQLException, IOException {
        System.out.println("deleteAnnotation");
        //final Number annotationID = daoDispatcher.getAnnotationInternalIdentifier(UUID.fromString(UUID));
        //int[] resultDelete = daoDispatcher.deleteAnnotation(annotationID);
       
        final int[] mockDelete = new int[4];
        mockDelete[0]=1; // # deleted annotations
        mockDelete[3]=1; // # deleted annotation_prinipal_permissions
        mockDelete[2]=2; // # deleted  annotations_target_Targets, (5,3), (5,4)
        mockDelete[3]=1; // # deletd Targets, 4
        
        Mockery newMockery = new Mockery();
        MockObjectsFactoryRest mockFactory = new MockObjectsFactoryRest(newMockery);
        final UriInfo uriInfo = mockFactory.newUriInfo();
        final HttpServletRequest httpServletRequest = mockFactory.newHttpServletRequest();
        
        newMockery.checking(new Expectations() {
            {
                
                oneOf(uriInfo).getBaseUri();
                will(returnValue(URI.create("http://localhost:8080/annotator-backend/api/")));
                   
                oneOf(httpServletRequest).getRemoteUser();
                will(returnValue("twan"));
                
              
            }
        });
     
        mockeryRest.checking(new Expectations() {
            {  
                oneOf(daoDispatcher).getUserInternalIDFromRemoteID("twan");
                will(returnValue(3)); 
                
                oneOf(daoDispatcher).setServiceURI(with(any(String.class)));
                will(doAll());
                
                oneOf(daoDispatcher).getAnnotationInternalIdentifier(with(aNonNull(UUID.class)));              
                will(returnValue(5));    
                
                oneOf(daoDispatcher).getPermission(5, 3);
                will(returnValue(Permission.OWNER));  
                
                oneOf(daoDispatcher).deleteAnnotation(5);
                will(returnValue(mockDelete));
            }
        });
        
        annotationResource.setUriInfo(uriInfo);
        annotationResource.setHttpRequest(httpServletRequest);
        String result = annotationResource.deleteAnnotation(TestBackendConstants._TEST_ANNOT_5_EXT);
        assertEquals("1 annotation(s) deleted.", result);
    }
    
    /**
     * Test of createAnnotation method, of class AnnotationResource.
     */
    @Test
    public void testCreateAnnotation() throws SQLException, InstantiationException, IllegalAccessException, ServletException, DatatypeConfigurationException, Exception {
        System.out.println("test createAnnotation");
        
        final Annotation annotationToAdd = new Annotation();
        final Number newAnnotationID = 6;
        
        TargetInfoList TargetInfoList = new TargetInfoList();
        annotationToAdd.setTargets(TargetInfoList);
        annotationToAdd.setOwnerRef(null);      
        annotationToAdd.setTimeStamp(Helpers.setXMLGregorianCalendar(Timestamp.valueOf("2013-08-12 11:25:00.383000")));
        annotationToAdd.setHeadline("headline");
        annotationToAdd.setTargets(TargetInfoList);
        
       
        AnnotationBody body   = new AnnotationBody();        
        annotationToAdd.setBody(body);
        TextBody textBody = new TextBody();
        body.setTextBody(textBody);
        textBody.setMimeType("text/plain");
        textBody.setValue("blah");
        
        TargetInfo TargetInfo = new TargetInfo();
        TargetInfo.setLink("google.nl");
        TargetInfo.setRef(UUID.randomUUID().toString());
        TargetInfo.setVersion("vandaag");
        
        final List<String> targets = new ArrayList<String>();
        targets.add("http://localhost:8080/annotator-backend/api/targets/00000000-0000-0000-0000-000000000036");
        
        final Annotation addedAnnotation = (new ObjectFactory()).createAnnotation(annotationToAdd).getValue();
        addedAnnotation.setURI("http://localhost:8080/annotator-backend/api/annotations/"+UUID.randomUUID().toString());
        addedAnnotation.setOwnerRef("http://localhost:8080/annotator-backend/api/users/"+TestBackendConstants._TEST_USER_3_EXT_ID);
        
        Mockery newMockery = new Mockery();
        MockObjectsFactoryRest mockFactory = new MockObjectsFactoryRest(newMockery);
        final UriInfo uriInfo = mockFactory.newUriInfo();
        final HttpServletRequest httpServletRequest = mockFactory.newHttpServletRequest();
        
        newMockery.checking(new Expectations() {
            {
                
                oneOf(uriInfo).getBaseUri();
                will(returnValue(URI.create("http://localhost:8080/annotator-backend/api/")));
                
                
                oneOf(httpServletRequest).getRemoteUser();
                will(returnValue("twan"));
                
            }
        });
        
        mockeryRest.checking(new Expectations() {
            {
                
                oneOf(daoDispatcher).setServiceURI(with(any(String.class)));
                will(doAll());
                
                oneOf(daoDispatcher).getUserInternalIDFromRemoteID("twan");
                will(returnValue(3));
                
             
                oneOf(daoDispatcher).addUsersAnnotation(3, annotationToAdd);
                will(returnValue(newAnnotationID));
                
                oneOf(daoDispatcher).getAnnotation(newAnnotationID);
                will(returnValue(addedAnnotation));
                
                oneOf(daoDispatcher).getTargetsWithNoCachedRepresentation(newAnnotationID);
                will(returnValue(targets));
                
          }
        });
        
       
        annotationResource.setUriInfo(uriInfo);      
        annotationResource.setHttpRequest(httpServletRequest);    
        
        JAXBElement<ResponseBody> result = annotationResource.createAnnotation(annotationToAdd);
        Annotation newAnnotation = result.getValue().getAnnotation();
        String actionName = result.getValue().getActionList().getAction().get(0).getMessage();
        assertEquals(addedAnnotation.getOwnerRef(), newAnnotation.getOwnerRef());
        assertEquals(addedAnnotation.getURI(), newAnnotation.getURI());
        assertEquals(addedAnnotation.getHeadline(), newAnnotation.getHeadline());
        assertEquals(addedAnnotation.getTargets(), newAnnotation.getTargets()); 
        assertEquals(addedAnnotation.getTimeStamp(), newAnnotation.getTimeStamp());
        assertEquals(addedAnnotation.getBody(), newAnnotation.getBody());
        assertEquals(AnnotationActionName.CREATE_CACHED_REPRESENTATION.value(), actionName);
    }
}
