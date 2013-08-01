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

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.dao.PermissionsDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Permission;
import java.sql.SQLException;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.jmock.Expectations;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author olhsha
 */
public class AnnotationsTest extends ResourcesTest{
    
    private AnnotationDao annotationDao;
    private PermissionsDao permissionsDao;
    private NotebookDao notebookDao;
    
    public AnnotationsTest() {
        super(AnnotationResource.class.getPackage().getName());        
        annotationDao = webAppContext.getBean(AnnotationDao.class);
        permissionsDao = webAppContext.getBean(PermissionsDao.class);
        notebookDao = webAppContext.getBean(NotebookDao.class);
    }
    

     /**
     * Test of getAnnotation method, of class annotationResource. Get <aid>.
     * GET api/annotations/<aid>
     */
    @Test
    public void testGetAnnotation() throws SQLException{
        System.out.println("testGetAnnotation");
        final String annotationIdentifier= TestBackendConstants._TEST_ANNOT_2_EXT;
        final int annotationID = 2;
        final Annotation testAnnotation = new ObjectFactory().createAnnotation();
        
        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getAnnotationID(new AnnotationIdentifier(annotationIdentifier));                
                will(returnValue(annotationID));
                
                oneOf(annotationDao).getAnnotation(annotationID); 
               will(returnValue(testAnnotation)); 
            }
        });
        
        final String requestUrl = "annotations/" + annotationIdentifier;
        System.out.println("requestUrl: " + requestUrl);
        ClientResponse response = resource().path(requestUrl).accept(MediaType.TEXT_XML).get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        Annotation entity = response.getEntity(Annotation.class);
        assertEquals(testAnnotation.getBody(), entity.getBody());
        assertEquals(testAnnotation.getHeadline(), entity.getHeadline());
        assertEquals(testAnnotation.getOwner(), entity.getOwner());
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
        
        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getAnnotationID(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_5_EXT));                
                will(returnValue(5));
                
                oneOf(annotationDao).deleteAnnotation(5);
                will(returnValue(1));
            }
        });
        
        final String requestUrl = "annotations/" + TestBackendConstants._TEST_ANNOT_5_EXT;
        System.out.println("requestUrl: " + requestUrl);
        ClientResponse response = resource().path(requestUrl).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("1", response.getEntity(String.class));
        
         // now, try to delete the same annotation one more time
        // if it has been already deleted then the method under testing should return 0
        
        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getAnnotationID(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_5_EXT));                
                will(returnValue(5));
                
                oneOf(annotationDao).deleteAnnotation(5);
                will(returnValue(0));
            }
        });
       
        response = resource().path(requestUrl).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("0", response.getEntity(String.class));
    }
    /**
     * Test of createAnnotation method, of class AnnotationResource. 
     * POST api/annotations/
     */
    @Test
    //@Ignore
    public void testCreateAnnotation() throws SQLException, InstantiationException, IllegalAccessException{
        System.out.println("test createAnnotation");
        final Annotation annotationToAdd = new ObjectFactory().createAnnotation();
        
         // Peter's workaround on absence of "ObjectFactory.create... for annotations        
        final JAXBElement<Annotation> jaxbElement = new JAXBElement<Annotation>(new QName("http://www.dasish.eu/ns/addit", "annotation"), Annotation.class, null, annotationToAdd);
        
        
        final Annotation addedAnnotation = annotationToAdd;
        final AnnotationIdentifier annotationIdentifier = new GenericType<AnnotationIdentifier>(){}.getRawClass().newInstance();
        addedAnnotation.setURI(annotationIdentifier.toString());
        final UserIdentifier owner = new UserIdentifier(TestBackendConstants._TEST_USER_5_EXT_ID);
        mockery.checking(new Expectations() {
            {   
                
            // TODO sould be mpre strict demands on  inputs  when the user handling mechanism is settled
                oneOf(annotationDao).addAnnotation(with(aNonNull(Annotation.class)), with(any(Number.class)));
                will(returnValue(addedAnnotation));
                
                oneOf(permissionsDao).addAnnotationPrincipalPermission(with(aNonNull(AnnotationIdentifier.class)), with(aNonNull(UserIdentifier.class)), with(aNonNull(Permission.class)));
                will(returnValue(1));
            }
        });
       
     
        
        final String requestUrl = "annotations";
        System.out.println("requestUrl: " + requestUrl);
        
        // Peter's workaround on absence of "ObjectFactory.create... for annotations
        
        ClientResponse response = resource().path(requestUrl).accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_XML).post(ClientResponse.class, jaxbElement);
        assertEquals(200, response.getStatus());
        
        Annotation entity = response.getEntity(Annotation.class);
        assertEquals(annotationToAdd.getBody(), entity.getBody());
        assertEquals(annotationToAdd.getHeadline(), entity.getHeadline());
        assertEquals(annotationToAdd.getPermissions(), entity.getPermissions());
        assertEquals(annotationToAdd.getTargetSources(), entity.getTargetSources());
        assertEquals(annotationToAdd.getTimeStamp(), entity.getTimeStamp());
    }
}
