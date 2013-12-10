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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 * @author olhsha
 */

@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-test-config/mockeryRest.xml", "/spring-test-config/mockDBIntegrityService.xml", 
    "/spring-test-config/mockUriInfo.xml",
    "/spring-config/jaxbMarshallerFactory.xml"})
public class AnnotationResourceTest {
    
    @Autowired
    private Mockery mockeryRest;     
    @Autowired
    private MockObjectsFactoryRest mockObjectFactory;
    @Autowired
    private DBIntegrityService mockDbIntegrityService;
    @Autowired
    UriInfo mockUriInfo;    
    @Autowired
    private AnnotationResource annotationResource;
    
    private MockHttpServletRequest mockRequest;
    
    public AnnotationResourceTest() { 
        mockRequest = new MockHttpServletRequest();
        mockRequest.setRemoteUser("twan");
        
    }
        
    
    /**
     * Test of getAnnotation method, of class AnnotationResource.
     */
    @Test
    public void testGetAnnotation() throws SQLException, JAXBException, Exception {
        System.out.println("getAnnotation");
        final String externalIDstring= TestBackendConstants._TEST_ANNOT_2_EXT;
        final Annotation expectedAnnotation = (new TestInstances(TestBackendConstants._TEST_SERVLET_URI)).getAnnotationOne();       
        annotationResource.setHttpServletRequest(mockRequest);        
        annotationResource.setUriInfo(mockUriInfo);       
        
      
        mockeryRest.checking(new Expectations() {
            {
                
               oneOf(mockUriInfo).getBaseUri();
               will(returnValue(URI.create("http://localhost:8080/annotator-backend/api/"))); 
               
               
                oneOf(mockDbIntegrityService).setServiceURI(with(any(String.class)));
                will(doAll());
                
                oneOf(mockDbIntegrityService).getUserInternalIDFromRemoteID("twan");
                will(returnValue(3)); 
                
                oneOf(mockDbIntegrityService).getAnnotationInternalIdentifier(with(any(UUID.class)));                
                will(returnValue(2));    
                
                oneOf(mockDbIntegrityService).getPermission(2, 3);
                will(returnValue(Permission.OWNER));  
                
                oneOf(mockDbIntegrityService).getAnnotation(2);                
                will(returnValue(expectedAnnotation));
            }
        });
        
       
        JAXBElement<Annotation> result = annotationResource.getAnnotation(externalIDstring);
        assertEquals(expectedAnnotation, result.getValue());
    }
    
    /**
     * Test of deleteAnnotation method, of class AnnotationResource.
     */
    @Test
    public void testDeleteAnnotation() throws SQLException, IOException {
        System.out.println("deleteAnnotation");
       
        final int[] mockDelete = new int[4];
        mockDelete[0]=1; // # deleted annotations
        mockDelete[3]=1; // # deleted annotation_prinipal_permissions
        mockDelete[2]=2; // # deleted  annotations_target_Targets, (5,3), (5,4)
        mockDelete[3]=1; // # deletd Targets, 4
        
        annotationResource.setHttpServletRequest(mockRequest);
        annotationResource.setUriInfo(mockUriInfo);  
       
        mockeryRest.checking(new Expectations() {
            {  
               oneOf(mockUriInfo).getBaseUri();
               will(returnValue(URI.create("http://localhost:8080/annotator-backend/api/"))); 
               
              
                oneOf(mockDbIntegrityService).getUserInternalIDFromRemoteID("twan");
                will(returnValue(3)); 
                
                oneOf(mockDbIntegrityService).setServiceURI(with(any(String.class)));
                will(doAll());
                
                oneOf(mockDbIntegrityService).getAnnotationInternalIdentifier(with(aNonNull(UUID.class)));              
                will(returnValue(5));    
                
                oneOf(mockDbIntegrityService).getPermission(5, 3);
                will(returnValue(Permission.OWNER));  
                
                oneOf(mockDbIntegrityService).deleteAnnotation(5);
                will(returnValue(mockDelete));
            }
        });
        
       
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
        annotationToAdd.setLastModified(DatatypeFactory.newInstance().newXMLGregorianCalendar(TestBackendConstants._TEST_ANNOT_2_TIME_STAMP));
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
     
        annotationResource.setHttpServletRequest(mockRequest); 
        annotationResource.setUriInfo(mockUriInfo); 
        
        mockeryRest.checking(new Expectations() {
            {
                oneOf(mockUriInfo).getBaseUri();
                will(returnValue(URI.create("http://localhost:8080/annotator-backend/api/"))); 
               
                
                oneOf(mockDbIntegrityService).setServiceURI(with(any(String.class)));
                will(doAll());
                
                oneOf(mockDbIntegrityService).getUserInternalIDFromRemoteID("twan");
                will(returnValue(3));
                
             
                oneOf(mockDbIntegrityService).addUsersAnnotation(3, annotationToAdd);
                will(returnValue(newAnnotationID));
                
                oneOf(mockDbIntegrityService).getAnnotation(newAnnotationID);
                will(returnValue(addedAnnotation));
                
                oneOf(mockDbIntegrityService).getTargetsWithNoCachedRepresentation(newAnnotationID);
                will(returnValue(targets));
                
          }
        });
        
       
       
        JAXBElement<ResponseBody> result = annotationResource.createAnnotation(annotationToAdd);
        Annotation newAnnotation = result.getValue().getAnnotation();
        String actionName = result.getValue().getActionList().getAction().get(0).getMessage();
        assertEquals(addedAnnotation.getOwnerRef(), newAnnotation.getOwnerRef());
        assertEquals(addedAnnotation.getURI(), newAnnotation.getURI());
        assertEquals(addedAnnotation.getHeadline(), newAnnotation.getHeadline());
        assertEquals(addedAnnotation.getTargets(), newAnnotation.getTargets()); 
        assertEquals(addedAnnotation.getLastModified(), newAnnotation.getLastModified());
        assertEquals(addedAnnotation.getBody(), newAnnotation.getBody());
        assertEquals(AnnotationActionName.CREATE_CACHED_REPRESENTATION.value(), actionName);
    }
}
