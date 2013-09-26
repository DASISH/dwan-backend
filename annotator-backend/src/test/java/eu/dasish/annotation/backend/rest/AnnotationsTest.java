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
import com.sun.jersey.api.client.ClientResponse;
import eu.dasish.annotation.backend.Helpers;
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationResponseBody;
import eu.dasish.annotation.schema.AnnotationResponseContent;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.ResourceREF;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.SourceInfo;
import eu.dasish.annotation.schema.SourceInfoList;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import org.jmock.Expectations;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author olhsha
 */
public class AnnotationsTest extends ResourcesTest{
    
    private DBIntegrityService daoDispatcher;
    
    public AnnotationsTest() {
        super(AnnotationResource.class.getPackage().getName());        
        daoDispatcher = webAppContext.getBean(DBIntegrityService.class);
    }
    

     /**
     * Test of getAnnotation method, of class annotationResource. Get <aid>.
     * GET api/annotations/<aid>
     */
    @Test
    public void testGetAnnotation() throws SQLException, DatatypeConfigurationException{
        System.out.println("testGetAnnotation");
        final String externalIDstring= TestBackendConstants._TEST_ANNOT_2_EXT;
        final int annotationID = 2;
        final Annotation testAnnotation = new Annotation();
        ResourceREF owner = new ResourceREF();
        owner.setRef("5");
        testAnnotation.setOwner(owner);
        testAnnotation.setURI(externalIDstring);
        testAnnotation.setTimeStamp(Helpers.setXMLGregorianCalendar(Timestamp.valueOf("2013-08-12 11:25:00.383000")));
        
        //final Number annotationID = daoDispatcher.getAnnotationInternalIdentifier(UUID.fromString(UUID));
        //final Annotation annotation = daoDispatcher.getAnnotation(annotationID);
        mockery.checking(new Expectations() {
            {
                oneOf(daoDispatcher).setServiceURI(with(any(String.class)));
                will(doAll());
                
                oneOf(daoDispatcher).getAnnotationInternalIdentifier(with(aNonNull(UUID.class)));                
                will(returnValue(annotationID));                
                
                oneOf(daoDispatcher).getAnnotation(annotationID);                
                will(returnValue(testAnnotation));
            }
        });
        
        final String requestUrl = "annotations/" + externalIDstring;
        System.out.println("requestUrl: " + requestUrl);
        ClientResponse response = resource().path(requestUrl).accept(MediaType.TEXT_XML).get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        Annotation entity = response.getEntity(Annotation.class);
        assertEquals(testAnnotation.getBody(), entity.getBody());
        assertEquals(testAnnotation.getHeadline(), entity.getHeadline());
        assertEquals(testAnnotation.getOwner().getRef(), entity.getOwner().getRef());
        assertEquals(testAnnotation.getPermissions(), entity.getPermissions());
        assertEquals(testAnnotation.getTargetSources(), entity.getTargetSources());
        assertEquals(testAnnotation.getTimeStamp(), entity.getTimeStamp());
        assertEquals(testAnnotation.getURI(), entity.getURI());
    }  
    
     /**
     * Test of deleteAnnotation method, of class AnnotationResource. Delete <nid>.
     * DELETE api/annotations/<aid>
     */
    @Test
    public void testDeleteAnnotation() throws SQLException{
        System.out.println("testDeleteAnnotation");
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
        final String requestUrl = "annotations/" + TestBackendConstants._TEST_ANNOT_5_EXT;
        System.out.println("requestUrl: " + requestUrl);
        ClientResponse response = resource().path(requestUrl).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("1 annotation(s) deleted.", response.getEntity(String.class));
        
      
    }
    /**
     * Test of createAnnotation method, of class AnnotationResource. 
     * POST api/annotations/
     */
    @Test
    public void testCreateAnnotation() throws SQLException, InstantiationException, IllegalAccessException, DatatypeConfigurationException{
        System.out.println("test createAnnotation");
        // Peter's workaround on absence of "ObjectFactory.create... for annotations        
        //final JAXBElement<Annotation> jaxbElement = new JAXBElement<Annotation>(new QName("http://www.dasish.eu/ns/addit", "annotation"), Annotation.class, null, annotationToAdd);
        
        final String ownerString = "5";
        final Number ownerID =  5;
        final Number newAnnotationID = 6;
        
        ResponseBody responseBody = new ResponseBody();
        final JAXBElement<ResponseBody> jaxbElement = (new ObjectFactory()).createResponseBody(responseBody);
        responseBody.setPermissionResponse(null);
        AnnotationResponseBody arb =new AnnotationResponseBody();
        responseBody.setAnnotationResponse(arb);
        arb.setActions(null);
        AnnotationResponseContent content = new AnnotationResponseContent();
        arb.setContent(content);        
        final Annotation addedAnnotation = new Annotation();
        content.setAnnotation(addedAnnotation);
        ResourceREF owner = new ResourceREF();
        owner.setRef(ownerString);        
        SourceInfoList sourceInfoList = new SourceInfoList();
        addedAnnotation.setTargetSources(sourceInfoList);
        addedAnnotation.setOwner(owner);
        addedAnnotation.setURI(TestBackendConstants._TEST_SERVLET_URI+UUID.randomUUID().toString());        
        addedAnnotation.setTimeStamp(Helpers.setXMLGregorianCalendar(Timestamp.valueOf("2013-08-12 11:25:00.383000")));        
        SourceInfo sourceInfo = new SourceInfo();
        sourceInfo.setLink("google.nl");
        sourceInfo.setRef(UUID.randomUUID().toString());
        sourceInfo.setVersion("vandaag");
        sourceInfoList.getTargetSource().add(sourceInfo); 
        
        final List<Number> sources = new ArrayList<Number>();
        sources.add(6);
        
        mockery.checking(new Expectations() {
            {
                oneOf(daoDispatcher).setServiceURI(with(any(String.class)));
                will(doAll());
                
                oneOf(daoDispatcher).getUserInternalIdentifier(with(any(UUID.class)));
                will(returnValue(ownerID));
                
                //oneOf(daoDispatcher).addUsersAnnotation(annotToAddJB, ownerID);
                oneOf(daoDispatcher).addUsersAnnotation(with(aNonNull(Number.class)), with(aNonNull(Annotation.class)));
                will(returnValue(newAnnotationID));
                
                oneOf(daoDispatcher).getAnnotation(newAnnotationID);
                will(returnValue(addedAnnotation));
                
                oneOf(daoDispatcher).getSourcesWithNoCachedRepresentation(newAnnotationID);
                will(returnValue(sources));
            }
        });
        
              
        
        final String requestUrl = "annotations";
        System.out.println("requestUrl: " + requestUrl);
        ClientResponse response = resource().path(requestUrl).type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).post(ClientResponse.class, jaxbElement);
        assertEquals(200, response.getStatus());
        
        ResponseBody entity = response.getEntity(ResponseBody.class); 
        Annotation entityA = entity.getAnnotationResponse().getContent().getAnnotation();
        assertEquals(addedAnnotation.getBody(), entityA.getBody());
        assertEquals(addedAnnotation.getHeadline(), entityA.getHeadline());
        assertEquals(addedAnnotation.getPermissions(), entityA.getPermissions());
        assertEquals(addedAnnotation.getTargetSources().getTargetSource().get(0).getLink(), entityA.getTargetSources().getTargetSource().get(0).getLink());
        assertEquals(addedAnnotation.getTargetSources().getTargetSource().get(0).getRef(), entityA.getTargetSources().getTargetSource().get(0).getRef());
        assertEquals(addedAnnotation.getTargetSources().getTargetSource().get(0).getVersion(), entityA.getTargetSources().getTargetSource().get(0).getVersion());
        assertEquals(addedAnnotation.getTimeStamp(), entityA.getTimeStamp());
        assertEquals(addedAnnotation.getOwner().getRef(), entityA.getOwner().getRef());
    }
}
