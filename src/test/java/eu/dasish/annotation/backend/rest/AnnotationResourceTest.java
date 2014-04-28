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

import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.ResourceAction;
import eu.dasish.annotation.backend.dao.DBDispatcher;
import eu.dasish.annotation.backend.TestBackendConstants;
import eu.dasish.annotation.backend.TestInstances;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.Action;
import eu.dasish.annotation.schema.ActionList;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.AnnotationActionName;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationBody.TextBody;
import java.io.IOException;
import javax.xml.bind.JAXBElement;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.net.URI;
import java.util.UUID;
import javax.ws.rs.core.UriInfo;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 * @author olhsha
 */
@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-test-config/mockeryRest.xml", "/spring-test-config/mockDBDispatcher.xml",
    "/spring-test-config/mockUriInfo.xml",
    "/spring-config/jaxbMarshallerFactory.xml"})
public class AnnotationResourceTest {

    @Autowired
    private Mockery mockeryRest;
    @Autowired
    private DBDispatcher mockDbDispatcher;
    @Autowired
    UriInfo mockUriInfo;
    @Autowired
    private AnnotationResource annotationResource;
    private MockHttpServletRequest mockRequest;

    public AnnotationResourceTest() {
        mockRequest = new MockHttpServletRequest();
    }

//    public Number getPrincipalID() throws IOException {
//        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
//        verboseOutput = new VerboseOutput(httpServletResponse, loggerServer);
//        String remotePrincipal = httpServletRequest.getRemotePrincipal();
//        if (remotePrincipal != null) {
//            if (!remotePrincipal.equals(anonym)) {
//                final Number principalID = dbIntegrityService.getPrincipalInternalIDFromRemoteID(remotePrincipal);
//                if (principalID != null) {
//                    return principalID;
//                }
//                verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remotePrincipal, dbIntegrityService.getDataBaseAdmin().getDisplayName(), dbIntegrityService.getDataBaseAdmin().getEMail());
//                return null;
//            }
//        }
//
//        verboseOutput.NOT_LOGGED_IN(dbIntegrityService.getDataBaseAdmin().getDisplayName(), dbIntegrityService.getDataBaseAdmin().getEMail());
//        return null;
//
//    }
    @Test
    public void testGetAnnotation() throws NotInDataBaseException, IOException {
        System.out.println("getAnnotation");
        final String externalIDstring = "00000000-0000-0000-0000-000000000021";
        final Annotation expectedAnnotation = (new TestInstances(null)).getAnnotationOne();
        annotationResource.setHttpServletRequest(mockRequest);
        annotationResource.setUriInfo(mockUriInfo);
        mockRequest.setRemoteUser("olhsha@mpi.nl");

        final URI baseUri = URI.create(TestBackendConstants._TEST_SERVLET_URI);

        mockeryRest.checking(new Expectations() {
            {
                oneOf(mockUriInfo).getBaseUri();
                will(returnValue(baseUri));

                oneOf(mockDbDispatcher).setServiceURI(baseUri.toString());

                oneOf(mockDbDispatcher).getPrincipalInternalIDFromRemoteID("olhsha@mpi.nl");
                will(returnValue(3));

                oneOf(mockDbDispatcher).getResourceInternalIdentifier(with(aNonNull(UUID.class)), with(aNonNull((Resource.class))));
                will(returnValue(1));


                oneOf(mockDbDispatcher).canDo(ResourceAction.READ, 3, 1, Resource.ANNOTATION);
                will(returnValue(true));

                oneOf(mockDbDispatcher).getAnnotation(1);
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
    public void testDeleteAnnotation() throws NotInDataBaseException, IOException {
        System.out.println("deleteAnnotation");

        final int[] mockDelete = new int[4];
        mockDelete[0] = 1; // # deleted annotations
        mockDelete[3] = 1; // # deleted annotation_prinipal_accesss
        mockDelete[2] = 2; // # deleted  annotations_target_Targets, (4,3), (4,4)
        mockDelete[3] = 1; // # deletd Targets, 4

        annotationResource.setHttpServletRequest(mockRequest);
        annotationResource.setUriInfo(mockUriInfo);
        mockRequest.setRemoteUser("olhsha@mpi.nl");

        final URI baseUri = URI.create(TestBackendConstants._TEST_SERVLET_URI);


        mockeryRest.checking(new Expectations() {
            {
                oneOf(mockUriInfo).getBaseUri();
                will(returnValue(baseUri));

                oneOf(mockDbDispatcher).setServiceURI(baseUri.toString());

                oneOf(mockDbDispatcher).getPrincipalInternalIDFromRemoteID("olhsha@mpi.nl");
                will(returnValue(3));

                oneOf(mockDbDispatcher).getResourceInternalIdentifier(with(aNonNull(UUID.class)), with(aNonNull((Resource.class))));
                will(returnValue(4));


                oneOf(mockDbDispatcher).getAnnotationOwnerID(4);
                will(returnValue(3));
                
                oneOf(mockDbDispatcher).canDo(ResourceAction.DELETE, 3, 4, Resource.ANNOTATION);
                will(returnValue(true));

                oneOf(mockDbDispatcher).deleteAnnotation(4);
                will(returnValue(mockDelete));
            }
        });


        String result = annotationResource.deleteAnnotation("00000000-0000-0000-0000-000000000024");
        assertEquals("1 annotation(s) is(are) deleted.", result);
    }

    /**
     * Test of createAnnotation method, of class AnnotationResource.
     */
    @Test
    public void testCreateAnnotation() throws IOException, NotInDataBaseException {
        System.out.println("test createAnnotation");

        final Annotation annotationToAdd = (new TestInstances(TestBackendConstants._TEST_SERVLET_URI)).getAnnotationToAdd();
        final Number newAnnotationID = 5;


        final Annotation addedAnnotation = (new ObjectFactory()).createAnnotation(annotationToAdd).getValue();
        addedAnnotation.setURI("http://localhost:8080/annotator-backend/api/annotations/" + UUID.randomUUID().toString());
        addedAnnotation.setOwnerRef("http://localhost:8080/annotator-backend/api/principals/" + "00000000-0000-0000-0000-000000000113");

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
        mockRequest.setRemoteUser("olhsha@mpi.nl");
        final URI baseUri = URI.create(TestBackendConstants._TEST_SERVLET_URI);

//        final List<String> targets = new ArrayList<String>();
//        targets.add("http://localhost:8080/annotator-backend/api/targets/00000000-0000-0000-0000-000000000036");

        mockeryRest.checking(new Expectations() {
            {
                oneOf(mockUriInfo).getBaseUri();
                will(returnValue(baseUri));

                oneOf(mockDbDispatcher).setServiceURI(baseUri.toString());

                oneOf(mockDbDispatcher).getPrincipalInternalIDFromRemoteID("olhsha@mpi.nl");
                will(returnValue(3));

                oneOf(mockDbDispatcher).addPrincipalsAnnotation(3, annotationToAdd);
                will(returnValue(newAnnotationID));

                oneOf(mockDbDispatcher).getAnnotation(newAnnotationID);
                will(returnValue(addedAnnotation));
                
                oneOf(mockDbDispatcher).makeAnnotationResponseEnvelope(newAnnotationID);
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
        assertEquals(Access.WRITE, addedAnnotation.getPermissions().getPublic());
    }

    @Test
    public void testUpdateAnnotation() throws NotInDataBaseException, IOException{
        System.out.println("test updateAnnotation");

        final Annotation annotation = (new TestInstances(TestBackendConstants._TEST_SERVLET_URI)).getAnnotationOne();
        annotation.getPermissions().setPublic(Access.READ);
        annotation.setHeadline("updated annotation 1");
        annotation.getPermissions().getPermission().get(1).setLevel(Access.WRITE);
        AnnotationBody ab = new AnnotationBody();
        TextBody tb = new TextBody();
        ab.setTextBody(tb);
        tb.setMimeType("text/plain");
        tb.setBody("some text body l");
        annotation.setBody(ab);

        final ResponseBody mockEnvelope = new ResponseBody();
        final ActionList actionList = new ActionList();
        mockEnvelope.setAnnotation(annotation);
        mockEnvelope.setActionList(actionList);

        annotationResource.setHttpServletRequest(mockRequest);
        annotationResource.setUriInfo(mockUriInfo);
        mockRequest.setRemoteUser("twagoo@mpi.nl");
        final URI baseUri = URI.create(TestBackendConstants._TEST_SERVLET_URI);

        final UUID externalId = UUID.fromString("00000000-0000-0000-0000-000000000021");

        //  Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);
        //  if (principalID.equals(dbIntegrityService.getAnnotationOwnerID(annotationID)) || dbIntegrityService.getTypeOfPrincipalAccount(principalID).equals(admin)) {
        //  int updatedRows = dbIntegrityService.updateAnnotation(annotation);
        //  return new ObjectFactory().createResponseBody(dbIntegrityService.makeAnnotationResponseEnvelope(annotationID));


        mockeryRest.checking(new Expectations() {
            {
                oneOf(mockUriInfo).getBaseUri();
                will(returnValue(baseUri));

                oneOf(mockDbDispatcher).setServiceURI(baseUri.toString());

                oneOf(mockDbDispatcher).getPrincipalInternalIDFromRemoteID("twagoo@mpi.nl");
                will(returnValue(1));
                
                oneOf(mockUriInfo).getBaseUri();
                will(returnValue(baseUri));

                oneOf(mockDbDispatcher).getResourceInternalIdentifier(externalId, Resource.ANNOTATION);
                will(returnValue(1));
                
                oneOf(mockDbDispatcher).getAnnotationOwnerID(1);
                will(returnValue(1));
                
                oneOf(mockDbDispatcher).canDo(ResourceAction.WRITE_W_METAINFO, 1, 1, Resource.ANNOTATION);
                will(returnValue(true));
                
                oneOf(mockDbDispatcher).updateAnnotation(annotation);
                will(returnValue(1));
               
                oneOf(mockDbDispatcher).makeAnnotationResponseEnvelope(1);
                will(returnValue(mockEnvelope));

            }
        });



        JAXBElement<ResponseBody> result = annotationResource.updateAnnotation("00000000-0000-0000-0000-000000000021", annotation);
        Annotation newAnnotation = result.getValue().getAnnotation();
        assertEquals(annotation.getOwnerRef(), newAnnotation.getOwnerRef());
        assertEquals(annotation.getURI(), newAnnotation.getURI());
        assertEquals("updated annotation 1", newAnnotation.getHeadline());
        assertEquals("text/plain", newAnnotation.getBody().getTextBody().getMimeType());
        assertEquals("some text body l", newAnnotation.getBody().getTextBody().getBody());
        assertEquals(Access.WRITE, annotation.getPermissions().getPermission().get(1).getLevel());
        assertEquals(Access.READ, annotation.getPermissions().getPublic());
    }
    
    @Test
    public void testUpdateAnnotationBody() throws NotInDataBaseException, IOException{
        System.out.println("test updateAnnotationBody");

        Annotation annotation = (new TestInstances(TestBackendConstants._TEST_SERVLET_URI)).getAnnotationOne();
        
        final AnnotationBody ab = new AnnotationBody();
        TextBody tb = new TextBody();
        ab.setTextBody(tb);
        tb.setMimeType("text/plain");
        tb.setBody("some text body l");
        annotation.setBody(ab);

        final ResponseBody mockEnvelope = new ResponseBody();
        final ActionList actionList = new ActionList();
        mockEnvelope.setAnnotation(annotation);
        mockEnvelope.setActionList(actionList);

        annotationResource.setHttpServletRequest(mockRequest);
        annotationResource.setUriInfo(mockUriInfo);
        mockRequest.setRemoteUser("twagoo@mpi.nl");
        final URI baseUri = URI.create(TestBackendConstants._TEST_SERVLET_URI);

        final UUID externalId = UUID.fromString("00000000-0000-0000-0000-000000000021");

      
       //final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);
       // (dbIntegrityService.canDo(Access.WRITE, principalID, annotationID)) {
        // int updatedRows = dbIntegrityService.updateAnnotationBody(annotationID, annotationBody);
       //       return new ObjectFactory().createResponseBody(dbIntegrityService.makeAnnotationResponseEnvelope(annotationID));
       
        mockeryRest.checking(new Expectations() {
            {
                oneOf(mockUriInfo).getBaseUri();
                will(returnValue(baseUri));

                oneOf(mockDbDispatcher).setServiceURI(baseUri.toString());

                oneOf(mockDbDispatcher).getPrincipalInternalIDFromRemoteID("twagoo@mpi.nl");
                will(returnValue(1));
                
               
                oneOf(mockDbDispatcher).getResourceInternalIdentifier(externalId, Resource.ANNOTATION);
                will(returnValue(1));
                
                oneOf(mockDbDispatcher).canDo(ResourceAction.WRITE, 1, 1, Resource.ANNOTATION);
                will(returnValue(true));               
                
                
                oneOf(mockDbDispatcher).updateAnnotationBody(1, ab);
                will(returnValue(1));
               
                oneOf(mockDbDispatcher).makeAnnotationResponseEnvelope(1);
                will(returnValue(mockEnvelope));

            }
        });



        JAXBElement<ResponseBody> result = annotationResource.updateAnnotationBody("00000000-0000-0000-0000-000000000021", ab);
        Annotation newAnnotation = result.getValue().getAnnotation();
        assertEquals("text/plain", newAnnotation.getBody().getTextBody().getMimeType());
        assertEquals("some text body l", newAnnotation.getBody().getTextBody().getBody());
    }
    
    @Test
    public void testUpdateAnnotationHeadline() throws NotInDataBaseException, IOException{
        System.out.println("test updateAnnotationHeadline");

        Annotation annotation = (new TestInstances(TestBackendConstants._TEST_SERVLET_URI)).getAnnotationOne();
        
        final String newHeadline = "new Headline";        
        annotation.setHeadline(newHeadline);

        final ResponseBody mockEnvelope = new ResponseBody();
        final ActionList actionList = new ActionList();
        mockEnvelope.setAnnotation(annotation);
        mockEnvelope.setActionList(actionList);

        annotationResource.setHttpServletRequest(mockRequest);
        annotationResource.setUriInfo(mockUriInfo);
        mockRequest.setRemoteUser("twagoo@mpi.nl");
        final URI baseUri = URI.create(TestBackendConstants._TEST_SERVLET_URI);

        final UUID externalId = UUID.fromString("00000000-0000-0000-0000-000000000021");

     
        mockeryRest.checking(new Expectations() {
            {
                oneOf(mockUriInfo).getBaseUri();
                will(returnValue(baseUri));

                oneOf(mockDbDispatcher).setServiceURI(baseUri.toString());

                oneOf(mockDbDispatcher).getPrincipalInternalIDFromRemoteID("twagoo@mpi.nl");
                will(returnValue(1));
                
               
                oneOf(mockDbDispatcher).getResourceInternalIdentifier(externalId, Resource.ANNOTATION);
                will(returnValue(1));
                
                oneOf(mockDbDispatcher).canDo(ResourceAction.WRITE, 1, 1, Resource.ANNOTATION);
                will(returnValue(true));               
                
                
                oneOf(mockDbDispatcher).updateAnnotationHeadline(1, newHeadline);
                will(returnValue(1));
               
                oneOf(mockDbDispatcher).makeAnnotationResponseEnvelope(1);
                will(returnValue(mockEnvelope));

            }
        });



        JAXBElement<ResponseBody> result = annotationResource.updateAnnotationHeadline("00000000-0000-0000-0000-000000000021", newHeadline);
        Annotation newAnnotation = result.getValue().getAnnotation();
        assertEquals("new Headline", newAnnotation.getHeadline());
    }
}
