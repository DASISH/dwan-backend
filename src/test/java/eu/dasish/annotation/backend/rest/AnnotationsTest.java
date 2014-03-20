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
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.backend.dao.impl.JdbcResourceDaoTest;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationBody.TextBody;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.TargetInfoList;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;

/**
 *
 * @author olhsha
 */
@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring-test-config/dataSource.xml"})
public class AnnotationsTest extends JerseyTest {    
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    
    
    
    @Override
    protected AppDescriptor configure() {
       return new WebAppDescriptor.Builder(AnnotationResource.class.getPackage().getName())
                .servletClass(SpringServlet.class)
                .contextParam("contextConfigLocation", getApplicationContextFile())
                .addFilter(DummySecurityFilter.class, "DummySecurityFilter")
                .requestListenerClass(RequestContextListener.class)
                .contextListenerClass(ContextLoaderListener.class)
                .build();
        
    }
    
    private String getApplicationContextFile() {
	// sorry for the duplication, but JerseyTest is not aware of
	// @ContextConfiguration
	return "classpath:spring-config/componentscan.xml, classpath:spring-config/notebookDao.xml, classpath:spring-config/annotationDao.xml, classpath:spring-config/principalDao.xml, classpath:spring-config/targetDao.xml, classpath:spring-config/cachedRepresentationDao.xml, classpath:spring-config/dbIntegrityService.xml, classpath:spring-config/jaxbMarshallerFactory.xml, classpath:spring-test-config/dataSource.xml";
    }
    
    
    
    @Override
    @Before
    public void setUp() throws DataAccessException, FileNotFoundException, URISyntaxException, Exception {
        super.setUp();
        jdbcTemplate.execute("DROP SCHEMA PUBLIC CASCADE");
        // consume the DashishAnnotatorCreate sql script to create the database
        jdbcTemplate.execute(JdbcResourceDaoTest.getNormalisedSql());
        jdbcTemplate.execute(JdbcResourceDaoTest.getTestDataInsertSql());
    }

    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
    /**
     * Test of getAnnotation method, of class annotationResource. Get <aid>. GET
     * api/annotations/<aid>
     */
    @Test
    public void testGetAnnotation() throws SQLException, DatatypeConfigurationException {
        
        // Authentication  
        Builder responseBuilderAu = getAuthenticatedResource(resource().path("authentication/login")).accept(MediaType.TEXT_XML);        
        ClientResponse responseAu = responseBuilderAu.get(ClientResponse.class); 
        assertEquals(200, responseAu.getStatus());
        
        // Getting annotation
        System.out.println("testGetAnnotation");
        final String externalIDstring = "00000000-0000-0000-0000-000000000021";
        final Annotation testAnnotation = (new TestInstances(resource().getURI().toString())).getAnnotationOne();        
        
        final String requestUrl = "annotations/" + externalIDstring;
        System.out.println("requestUrl: " + requestUrl);
        
        Builder responseBuilder = getAuthenticatedResource(resource().path(requestUrl)).accept(MediaType.TEXT_XML);        
        ClientResponse response = responseBuilder.get(ClientResponse.class);        
        
        
        assertEquals(200, response.getStatus());
        Annotation entity = response.getEntity(Annotation.class);
        assertEquals(testAnnotation.getBody().getTextBody().getBody(), entity.getBody().getTextBody().getBody());
        assertEquals(testAnnotation.getHeadline(), entity.getHeadline());
        assertEquals(testAnnotation.getOwnerRef(), entity.getOwnerRef());
        assertEquals(3, entity.getPermissions().getPermission().size());
        assertEquals("write", entity.getPermissions().getPermission().get(0).getLevel().value());
        assertEquals(resource().getURI()+"principals/"+"00000000-0000-0000-0000-000000000112", entity.getPermissions().getPermission().get(0).getPrincipalRef()); 
        assertEquals("read", entity.getPermissions().getPermission().get(1).getLevel().value()); 
        assertEquals(resource().getURI()+"principals/"+"00000000-0000-0000-0000-000000000113", entity.getPermissions().getPermission().get(1).getPrincipalRef()); 
        assertEquals("read", entity.getPermissions().getPermission().get(1).getLevel().value()); 
        assertEquals(resource().getURI()+"principals/"+"00000000-0000-0000-0000-000000000221", entity.getPermissions().getPermission().get(2).getPrincipalRef()); 
        assertEquals(2, entity.getTargets().getTargetInfo().size());
        assertEquals(resource().getURI().toString()+"targets/"+"00000000-0000-0000-0000-000000000031", entity.getTargets().getTargetInfo().get(0).getRef());
        assertEquals(resource().getURI().toString()+"targets/"+"00000000-0000-0000-0000-000000000032", entity.getTargets().getTargetInfo().get(1).getRef());
        assertEquals(testAnnotation.getLastModified(), entity.getLastModified());
        assertEquals(resource().getURI()+requestUrl, entity.getURI());
    }

    /**
     * Test of deleteAnnotation method, of class AnnotationResource. Delete
     * <nid>. DELETE api/annotations/<aid>
     */
    @Test
    public void testDeleteAnnotation() throws SQLException {
        
          
        // Authentication  
        Builder responseBuilderAu = getAuthenticatedResource(resource().path("authentication/login")).accept(MediaType.TEXT_XML);        
        ClientResponse responseAu = responseBuilderAu.get(ClientResponse.class); 
        assertEquals(200, responseAu.getStatus());
        
        // Deleting annotation 
        System.out.println("testDeleteAnnotation");
        String externalIDstring  =  "00000000-0000-0000-0000-000000000024";
        final String requestUrl = "annotations/" + externalIDstring;
        System.out.println("requestUrl: " + requestUrl);
        
        Builder responseBuilder = getAuthenticatedResource(resource().path(requestUrl)).accept(MediaType.TEXT_XML);        
        ClientResponse response = responseBuilder.delete(ClientResponse.class);   
        assertEquals(200, response.getStatus());
        assertEquals("1 annotation(s) deleted.", response.getEntity(String.class));
    }

    /**
     * Test of createAnnotation method, of class AnnotationResource. POST
     * api/annotations/
     */
    @Test
    public void testCreateAnnotation() throws SQLException, InstantiationException, IllegalAccessException, DatatypeConfigurationException, Exception {
        
          
        // Authentication  
        Builder responseBuilderAu = getAuthenticatedResource(resource().path("authentication/login")).accept(MediaType.TEXT_XML);        
        ClientResponse responseAu = responseBuilderAu.get(ClientResponse.class); 
        assertEquals(200, responseAu.getStatus());
        
        
        // Adding annotation
        System.out.println("test createAnnotation");
        System.out.println("POST "+resource().getURI().toString()+"annotations/");
        final String ownerString = resource().getURI().toString()+"principals/"+"00000000-0000-0000-0000-000000000113";
        final Annotation annotationToAdd = new Annotation();
        final JAXBElement<Annotation> jaxbElement = (new ObjectFactory()).createAnnotation(annotationToAdd);
        annotationToAdd.setPermissions(null);
        annotationToAdd.setOwnerRef(ownerString);
        annotationToAdd.setURI(resource().getURI().toString()+"annotations/"+ UUID.randomUUID().toString());        
        annotationToAdd.setLastModified(DatatypeFactory.newInstance().newXMLGregorianCalendar("2013-08-12T09:25:00.383000Z"));        
        
        TargetInfoList targetInfoList = new TargetInfoList();
        annotationToAdd.setTargets(targetInfoList);
        TargetInfo targetInfo = new TargetInfo();
        targetInfo.setLink("http://nl.wikipedia.org/wiki/Viktor_Janoekovytsj#Biografie");
        targetInfo.setRef(resource().getURI().toString()+"targets/"+UUID.randomUUID().toString());
        targetInfo.setVersion("5 apr 2013 om 18:42");
        targetInfoList.getTargetInfo().add(targetInfo);       
        
        AnnotationBody annotationBody = new AnnotationBody();
        annotationBody.setXmlBody(null);
        TextBody textBody = new TextBody();
        textBody.setMimeType("plain/text");
        textBody.setBody("yanuk - zek");
        annotationBody.setTextBody(textBody);
        annotationToAdd.setBody(annotationBody);
      
        Builder responseBuilder = getAuthenticatedResource(resource().path("annotations/")).type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML);        
        ClientResponse response = responseBuilder.post(ClientResponse.class, jaxbElement);
        assertEquals(200, response.getStatus());
        
        ResponseBody entity = response.getEntity(ResponseBody.class);        
        Annotation entityA = entity.getAnnotation();
        assertEquals(annotationToAdd.getBody().getTextBody().getBody(), entityA.getBody().getTextBody().getBody());
        assertEquals(annotationToAdd.getBody().getTextBody().getMimeType(), entityA.getBody().getTextBody().getMimeType());
        assertEquals(annotationToAdd.getHeadline(), entityA.getHeadline());
        assertEquals(0, entityA.getPermissions().getPermission().size());
        assertEquals(annotationToAdd.getOwnerRef(), entityA.getOwnerRef());
        assertEquals(annotationToAdd.getTargets().getTargetInfo().get(0).getLink(), entityA.getTargets().getTargetInfo().get(0).getLink());
        // new ref is generated
        //assertEquals(annotationToAdd.getTargets().getTargetInfo().get(0).getPrincipalRef(), entityA.getTargets().getTargetInfo().get(0).getPrincipalRef());
        assertEquals(annotationToAdd.getTargets().getTargetInfo().get(0).getVersion(), entityA.getTargets().getTargetInfo().get(0).getVersion());
        //last modified is updated by the server
        //assertEquals(annotationToAdd.getLastModified(), entityA.getLastModified());
        assertEquals(annotationToAdd.getOwnerRef(), entityA.getOwnerRef());
    }
    
    
   
    protected Builder getAuthenticatedResource(WebResource resource) {
	return resource.header(HttpHeaders.AUTHORIZATION, "Basic "  + new String(Base64.encode(DummyPrincipal.DUMMY_PRINCIPAL.getName()+":olhapassword")));
    }
    
    
}
