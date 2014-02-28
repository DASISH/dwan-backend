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

import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.dao.DBIntegrityService;
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.schema.Action;
import eu.dasish.annotation.schema.ActionList;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationBody.TextBody;
import eu.dasish.annotation.schema.TargetInfo;
import eu.dasish.annotation.schema.TargetInfoList;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.AnnotationActionName;
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
@ContextConfiguration(locations = {"/spring-test-config/mockeryRest.xml", "/spring-test-config/mockDBIntegrityService.xml",
    "/spring-test-config/mockUriInfo.xml",
    "/spring-config/jaxbMarshallerFactory.xml"})
public class AnnotationResourceTest {

    @Autowired
    private Mockery mockeryRest;
    @Autowired
    private DBIntegrityService mockDbIntegrityService;
    @Autowired
    UriInfo mockUriInfo;
    @Autowired
    private AnnotationResource annotationResource;
    private MockHttpServletRequest mockRequest;

    public AnnotationResourceTest() {
        mockRequest = new MockHttpServletRequest();
    }
    
   
    @Test
    public void testGetAnnotation() throws SQLException, JAXBException, Exception {
        System.out.println("getAnnotation");
        final String externalIDstring = "00000000-0000-0000-0000-000000000021";
        final Annotation expectedAnnotation = (new TestInstances(null)).getAnnotationOne();
        annotationResource.setHttpServletRequest(mockRequest);
        annotationResource.setUriInfo(mockUriInfo);


        mockeryRest.checking(new Expectations() {
            {  
                oneOf(mockDbIntegrityService).getRemoteUser();
                will(returnValue("olhsha@mpi.nl"));
                        
                oneOf(mockDbIntegrityService).getUserInternalIDFromRemoteID("olhsha@mpi.nl");
                will(returnValue(3));

                oneOf(mockDbIntegrityService).getResourceInternalIdentifier(with(aNonNull(UUID.class)), with(aNonNull((Resource.class))));
                will(returnValue(1));
 

                oneOf(mockDbIntegrityService).canRead(3, 1);
                will(returnValue(true));

                oneOf(mockDbIntegrityService).getAnnotation(1);
                will(returnValue(expectedAnnotation));
            }
        });


        JAXBElement<Annotation> result = annotationResource.getAnnotation(externalIDstring);
        assertTrue(expectedAnnotation.equals(result.getValue()));
    }

    /**
     * Test of deleteAnnotation method, of class AnnotationResource.
     */
    @Test
    public void testDeleteAnnotation() throws SQLException, IOException {
        System.out.println("deleteAnnotation");

        final int[] mockDelete = new int[4];
        mockDelete[0] = 1; // # deleted annotations
        mockDelete[3] = 1; // # deleted annotation_prinipal_permissions
        mockDelete[2] = 2; // # deleted  annotations_target_Targets, (4,3), (4,4)
        mockDelete[3] = 1; // # deletd Targets, 4

        annotationResource.setHttpServletRequest(mockRequest);
        annotationResource.setUriInfo(mockUriInfo);

        mockeryRest.checking(new Expectations() {
            {
                oneOf(mockDbIntegrityService).getRemoteUser();
                will(returnValue("olhsha@mpi.nl"));
                
                oneOf(mockDbIntegrityService).getUserInternalIDFromRemoteID("olhsha@mpi.nl");
                will(returnValue(3));

                oneOf(mockDbIntegrityService).getResourceInternalIdentifier(with(aNonNull(UUID.class)), with(aNonNull((Resource.class))));
                will(returnValue(4));


                oneOf(mockDbIntegrityService).getAnnotationOwnerID(4);
                will(returnValue(3));

                oneOf(mockDbIntegrityService).deleteAnnotation(4);
                will(returnValue(mockDelete));
            }
        });


        String result = annotationResource.deleteAnnotation("00000000-0000-0000-0000-000000000024");
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
        annotationToAdd.setLastModified(DatatypeFactory.newInstance().newXMLGregorianCalendar("2013-08-12T09:25:00.383000Z"));
        annotationToAdd.setHeadline("headline");
        annotationToAdd.setTargets(TargetInfoList);


        AnnotationBody body = new AnnotationBody();
        annotationToAdd.setBody(body);
        TextBody textBody = new TextBody();
        body.setTextBody(textBody);
        textBody.setMimeType("text/plain");
        textBody.setBody("blah");

        TargetInfo TargetInfo = new TargetInfo();
        TargetInfo.setLink("google.nl");
        TargetInfo.setRef(UUID.randomUUID().toString());
        TargetInfo.setVersion("vandaag");

        final List<String> targets = new ArrayList<String>();
        targets.add("http://localhost:8080/annotator-backend/api/targets/00000000-0000-0000-0000-000000000036");

        final Annotation addedAnnotation = (new ObjectFactory()).createAnnotation(annotationToAdd).getValue();
        addedAnnotation.setURI("http://localhost:8080/annotator-backend/api/annotations/" + UUID.randomUUID().toString());
        addedAnnotation.setOwnerRef("http://localhost:8080/annotator-backend/api/users/" + "00000000-0000-0000-0000-000000000111");

        final ResponseBody mockEnvelope = new ResponseBody();
        final Action action = new Action();
        final ActionList actionList = new ActionList();
        mockEnvelope.setAnnotation(addedAnnotation);
        mockEnvelope.setActionList(actionList);
        actionList.getAction().add(action);
        action.setMessage(AnnotationActionName.CREATE_CACHED_REPRESENTATION.value());
        action.setObject("http://localhost:8080/annotator-backend/api/targets/00000000-0000-0000-0000-000000000036");

        annotationResource.setHttpServletRequest(mockRequest);
        annotationResource.setUriInfo(mockUriInfo);

        mockeryRest.checking(new Expectations() {
            {
                oneOf(mockDbIntegrityService).getRemoteUser();
                will(returnValue("olhsha@mpi.nl"));
                
             
                oneOf(mockDbIntegrityService).getUserInternalIDFromRemoteID("olhsha@mpi.nl");
                will(returnValue(3));

                oneOf(mockDbIntegrityService).addUsersAnnotation(3, annotationToAdd);
                will(returnValue(newAnnotationID));

                oneOf(mockDbIntegrityService).getAnnotation(newAnnotationID);
                will(returnValue(addedAnnotation));

                oneOf(mockDbIntegrityService).getTargetsWithNoCachedRepresentation(newAnnotationID);
                will(returnValue(targets));

                oneOf(mockDbIntegrityService).makeAnnotationResponseEnvelope(newAnnotationID);
                will(returnValue(mockEnvelope));

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
