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
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.backend.identifiers.NotebookIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.ObjectFactory;
import java.sql.SQLException;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.jmock.Expectations;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author olhsha
 */
public class AnnotationsTest extends ResourcesTest{
    
    private AnnotationDao annotationDao;
    
    public AnnotationsTest() {
        super(AnnotationResource.class.getPackage().getName());
        annotationDao = webAppContext.getBean(AnnotationDao.class);
    }
    

     /**
     * Test of getAnnotation method, of class annotationResource. Get <aid>.
     * GET api/annotations/<aid>
     */
    @Test
    public void testGetAnnotation() throws SQLException{
        System.out.println("testGetAnnotation");
        final String annotationIdentifier= TestBackendConstants._TEST_ANNOT_1_EXT;
        final int annotationID = TestBackendConstants._TEST_ANNOT_1_INT;
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
                oneOf(annotationDao).getAnnotationID(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_5_EXT_TO_BE_DELETED));                
                will(returnValue(TestBackendConstants._TEST_ANNOT_5_INT_TO_BE_DELETED));
                
                oneOf(annotationDao).deleteAnnotation(TestBackendConstants._TEST_ANNOT_5_INT_TO_BE_DELETED);
                will(returnValue(1));
            }
        });
        
        final String requestUrl = "annotations/" + TestBackendConstants._TEST_ANNOT_5_EXT_TO_BE_DELETED;
        System.out.println("requestUrl: " + requestUrl);
        ClientResponse response = resource().path(requestUrl).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("1", response.getEntity(String.class));
        
         // now, try to delete the same annotation one more time
        // if it has been already deleted then the method under testing should return 0
        
        mockery.checking(new Expectations() {
            {
                oneOf(annotationDao).getAnnotationID(new AnnotationIdentifier(TestBackendConstants._TEST_ANNOT_5_EXT_TO_BE_DELETED));                
                will(returnValue(TestBackendConstants._TEST_ANNOT_5_INT_TO_BE_DELETED));
                
                oneOf(annotationDao).deleteAnnotation(TestBackendConstants._TEST_ANNOT_5_INT_TO_BE_DELETED);
                will(returnValue(0));
            }
        });
       
        response = resource().path(requestUrl).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("0", response.getEntity(String.class));
    }
}
