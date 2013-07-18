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
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.dao.AnnotationDao;
import eu.dasish.annotation.backend.identifiers.AnnotationIdentifier;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.NotebookInfos;
import eu.dasish.annotation.schema.ObjectFactory;
import java.sql.SQLException;
import javax.ws.rs.core.MediaType;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.Assert.*;
/**
 *
 * @author olhsha
 */
public class AnnotationsURLTest extends JerseyTest{
    
    private Mockery mockery;
    private AnnotationDao annotationDao;
    
    public AnnotationsURLTest() {
        
        super(new WebAppDescriptor.Builder(AnnotationResource.class.getPackage().getName())
                .servletClass(SpringServlet.class)
                .contextParam("contextConfigLocation", "classpath*:spring-test-config/**/*.xml")
                .contextListenerClass(ContextLoaderListener.class)
                .build());

        // Get the web application context that has been instantiated in the Grizzly container
        final WebApplicationContext webAppContext = ContextLoaderListener.getCurrentWebApplicationContext();

        // Get the context and mock objects from the context by their type
        mockery = webAppContext.getBean(Mockery.class);
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
}
