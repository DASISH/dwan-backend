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
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.backend.dao.impl.JdbcResourceDaoTest;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.TargetInfoList;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
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
import org.springframework.mock.web.MockHttpServletRequest;
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
	return "classpath:spring-config/componentscan.xml, classpath:spring-config/annotationDao.xml, classpath:spring-config/userDao.xml, classpath:spring-config/targetDao.xml, classpath:spring-config/cachedRepresentationDao.xml, classpath:spring-config/dbIntegrityService.xml, classpath:spring-config/jaxbMarshallerFactory.xml, classpath:spring-test-config/dataSource.xml";
    }
    
    private String getNormalisedSql() throws FileNotFoundException, URISyntaxException {
        // remove the unsupported sql for the test
        final URL sqlUrl = JdbcResourceDaoTest.class.getResource("/sql/DashishAnnotatorCreate.sql");
        String sqlString = new Scanner(new File(sqlUrl.toURI()), "UTF8").useDelimiter("\\Z").next();
        for (String unknownToken : new String[]{
                    "SET client_encoding",
                    "CREATE DATABASE",
                    "\\\\connect",
                    "SET default_with_oids",
                    "ALTER SEQUENCE",
                    "ALTER TABLE ONLY",
                    "ADD CONSTRAINT",
                    "CREATE INDEX", // "ALTER TABLE ONLY [a-z]* ALTER COLUMN",
                // "ALTER TABLE ONLY [^A]* ADD CONSTRAINT"
                }) {
            sqlString = sqlString.replaceAll(unknownToken, "-- " + unknownToken);
        }
        // obsolete(?) Peter's stuff, before body has been decided to be a text with its mimetype: sqlString = sqlString.replaceAll("body_xml xml", "body_xml text");
        sqlString = sqlString.replaceAll("CACHE 1;", "; -- CACHE 1;");
        //sqlString = sqlString.replaceAll("UUID", "text");
        sqlString = sqlString.replaceAll("SERIAL NOT NULL", "INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY");
        return sqlString;
    }
    
    private String getTestDataInsertSql() throws FileNotFoundException, URISyntaxException {
        final URL sqlUrl = AnnotationsTest.class.getResource("/test-data/InsertTestData.sql");
        String sqlString = new Scanner(new File(sqlUrl.toURI()), "UTF8").useDelimiter("\\Z").next();
        return sqlString;
    }
    
    
    
    @Override
    @Before
    public void setUp() throws DataAccessException, FileNotFoundException, URISyntaxException, Exception {
        super.setUp();
        jdbcTemplate.execute("DROP SCHEMA PUBLIC CASCADE");
        // consume the DashishAnnotatorCreate sql script to create the database
        jdbcTemplate.execute(getNormalisedSql());
        jdbcTemplate.execute(getTestDataInsertSql());
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
        System.out.println("testGetAnnotation");
        final String externalIDstring = TestBackendConstants._TEST_ANNOT_2_EXT;
        final Annotation testAnnotation = (new TestInstances(resource().getURI().toString())).getAnnotationOne();        
        
        final String requestUrl = "annotations/" + externalIDstring;
        System.out.println("requestUrl: " + requestUrl);
        
        Builder responseBuilder = getAuthenticatedResource(resource().path(requestUrl)).accept(MediaType.TEXT_XML);
        
        ClientResponse response = responseBuilder.get(ClientResponse.class);        
        
        assertEquals(200, response.getStatus());
        Annotation entity = response.getEntity(Annotation.class);
        assertEquals(testAnnotation.getBody().getTextBody().getValue(), entity.getBody().getTextBody().getValue());
        assertEquals(testAnnotation.getHeadline(), entity.getHeadline());
        assertEquals(testAnnotation.getOwnerRef(), entity.getOwnerRef());
        assertEquals(3, entity.getPermissions().getUserWithPermission().size());
        assertEquals("owner", entity.getPermissions().getUserWithPermission().get(0).getPermission().value());        
        assertEquals(resource().getURI()+"users/"+TestBackendConstants._TEST_USER_3_EXT_ID, entity.getPermissions().getUserWithPermission().get(0).getRef()); 
        assertEquals("writer", entity.getPermissions().getUserWithPermission().get(1).getPermission().value());
        assertEquals(resource().getURI()+"users/"+TestBackendConstants._TEST_USER_4_EXT_ID, entity.getPermissions().getUserWithPermission().get(1).getRef()); 
        assertEquals("reader", entity.getPermissions().getUserWithPermission().get(2).getPermission().value()); 
        assertEquals(resource().getURI()+"users/"+TestBackendConstants._TEST_USER_5_EXT_ID, entity.getPermissions().getUserWithPermission().get(2).getRef()); 
        assertEquals(2, entity.getTargets().getTargetInfo().size());
        assertEquals(resource().getURI().toString()+"targets/"+TestBackendConstants._TEST_Target_1_EXT_ID, entity.getTargets().getTargetInfo().get(0).getRef());
        assertEquals(resource().getURI().toString()+"targets/"+TestBackendConstants._TEST_Target_2_EXT_ID, entity.getTargets().getTargetInfo().get(1).getRef());
        assertEquals(testAnnotation.getLastModified(), entity.getLastModified());
        assertEquals(resource().getURI()+requestUrl, entity.getURI());
    }

    /**
     * Test of deleteAnnotation method, of class AnnotationResource. Delete
     * <nid>. DELETE api/annotations/<aid>
     */
    @Test
    @Ignore
    public void testDeleteAnnotation() throws SQLException {
        System.out.println("testDeleteAnnotation");
        //final Number annotationID = daoDispatcher.getAnnotationInternalIdentifier(UUID.fromString(UUID));
        //int[] resultDelete = daoDispatcher.deleteAnnotation(annotationID);
        
        final int[] mockDelete = new int[4];
        mockDelete[0] = 1; // # deleted annotations
        mockDelete[3] = 1; // # deleted annotation_prinipal_permissions
        mockDelete[2] = 2; // # deleted  annotations_target_Targets, (5,3), (5,4)
        mockDelete[3] = 1; // # deletd Targets, 4
        
        final String baseURI = this.getBaseURI().toString();
        
        final String requestUrl = "annotations/" + TestBackendConstants._TEST_ANNOT_5_EXT;
        System.out.println("requestUrl: " + requestUrl);
        ClientResponse response = resource().path(requestUrl).delete(ClientResponse.class);
        assertEquals(200, response.getStatus());
        assertEquals("1 annotation(s) deleted.", response.getEntity(String.class));
        
        
    }

    /**
     * Test of createAnnotation method, of class AnnotationResource. POST
     * api/annotations/
     */
    @Test
    @Ignore
    public void testCreateAnnotation() throws SQLException, InstantiationException, IllegalAccessException, DatatypeConfigurationException, Exception {
        System.out.println("test createAnnotation");
        // Peter's workaround on absence of "ObjectFactory.create... for annotations        
        //final JAXBElement<Annotation> jaxbElement = new JAXBElement<Annotation>(new QName("http://www.dasish.eu/ns/addit", "annotation"), Annotation.class, null, annotationToAdd);
        
        final String ownerString = "5";
        final Number ownerID = 5;
        final Number newAnnotationID = 6;
        
        ResponseBody responseBody = new ResponseBody();
        final JAXBElement<ResponseBody> jaxbElement = (new ObjectFactory()).createResponseBody(responseBody);
        responseBody.setPermissions(null);
        final Annotation addedAnnotation = new Annotation();
        responseBody.setAnnotation(addedAnnotation);
        responseBody.setActionList(null);
        
        TargetInfoList TargetInfoList = new TargetInfoList();
        addedAnnotation.setTargets(TargetInfoList);
        addedAnnotation.setOwnerRef(ownerString);
        addedAnnotation.setURI(TestBackendConstants._TEST_SERVLET_URI_annotations + UUID.randomUUID().toString());        
        addedAnnotation.setLastModified(DatatypeFactory.newInstance().newXMLGregorianCalendar(TestBackendConstants._TEST_ANNOT_2_TIME_STAMP));        
        TargetInfo TargetInfo = new TargetInfo();
        TargetInfo.setLink("google.nl");
        TargetInfo.setRef(UUID.randomUUID().toString());
        TargetInfo.setVersion("vandaag");
        TargetInfoList.getTargetInfo().add(TargetInfo);        
        
        final List<Number> Targets = new ArrayList<Number>();
        Targets.add(6);
        
        final String baseURI = this.getBaseURI().toString();
        
        final String requestUrl = "annotations";
        System.out.println("requestUrl: " + requestUrl);
        ClientResponse response = resource().path(requestUrl).type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).post(ClientResponse.class, jaxbElement);
        assertEquals(200, response.getStatus());
        
        ResponseBody entity = response.getEntity(ResponseBody.class);        
        Annotation entityA = entity.getAnnotation();
        assertEquals(addedAnnotation.getBody(), entityA.getBody());
        assertEquals(addedAnnotation.getHeadline(), entityA.getHeadline());
        assertEquals(addedAnnotation.getPermissions(), entityA.getPermissions());
        assertEquals(addedAnnotation.getTargets().getTargetInfo().get(0).getLink(), entityA.getTargets().getTargetInfo().get(0).getLink());
        assertEquals(addedAnnotation.getTargets().getTargetInfo().get(0).getRef(), entityA.getTargets().getTargetInfo().get(0).getRef());
        assertEquals(addedAnnotation.getTargets().getTargetInfo().get(0).getVersion(), entityA.getTargets().getTargetInfo().get(0).getVersion());
        assertEquals(addedAnnotation.getLastModified(), entityA.getLastModified());
        assertEquals(addedAnnotation.getOwnerRef(), entityA.getOwnerRef());
    }
    
    
   
    protected Builder getAuthenticatedResource(WebResource resource) {
	return resource.header(HttpHeaders.AUTHORIZATION, "Basic "  + new String(Base64.encode(DummyPrincipal.DUMMY_PRINCIPAL.getName()+":olhapassword")));
    }
}
