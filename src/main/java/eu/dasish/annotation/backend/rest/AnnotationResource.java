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

import eu.dasish.annotation.backend.BackendConstants;
import eu.dasish.annotation.backend.dao.DBIntegrityService;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationAction;
import eu.dasish.annotation.schema.AnnotationActionList;
import eu.dasish.annotation.schema.AnnotationActionName;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.AnnotationResponseBody;
import eu.dasish.annotation.schema.AnnotationResponseContent;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.PermissionList;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.SourceList;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author olhsha
 */
@Component
@Path("/annotations")
public class AnnotationResource {

    @Autowired
    private DBIntegrityService dbIntegrityService;
    @Context
    private HttpServletRequest httpServletRequest;

    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    public AnnotationResource() {
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<Annotation> getAnnotation(@PathParam("annotationid") String ExternalIdentifier) throws SQLException {
        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(ExternalIdentifier));
        dbIntegrityService.setServiceURI(httpServletRequest.getServletPath());
        final Annotation annotation = dbIntegrityService.getAnnotation(annotationID);
        return new ObjectFactory().createAnnotation(annotation);
    }

     //TODO: unit test
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/sources")
    public JAXBElement<SourceList> getAnnotationSources(@PathParam("annotationid") String ExternalIdentifier) throws SQLException {
        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(ExternalIdentifier));
        dbIntegrityService.setServiceURI(httpServletRequest.getServletPath());
        final SourceList sourceList = dbIntegrityService.getAnnotationSources(annotationID);
        return new ObjectFactory().createSourceList(sourceList);
    }
    
    // TODO Unit test 
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("")
    public JAXBElement<AnnotationInfoList> getFilteredAnnotations(@QueryParam("link") String link,
    @QueryParam("text") String text,
    @QueryParam("access") String permission,
    @QueryParam("access") String namespace,
    @QueryParam("after")  Timestamp after,
    @QueryParam("before") Timestamp before
    ) throws SQLException {
        dbIntegrityService.setServiceURI(httpServletRequest.getServletPath());
        
        String remoteUser = httpServletRequest.getRemoteUser();
        UUID userExternalID = (remoteUser != null) ? UUID.fromString(remoteUser) : null;
        
        final AnnotationInfoList annotationInfoList = dbIntegrityService.getFilteredAnnotationInfos(link, text, text, namespace, userExternalID, after, before);
        return new ObjectFactory().createAnnotationInfoList(annotationInfoList);
    }
    
    // TODO Unit test    
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions")
    public JAXBElement<PermissionList> getAnnotationPermissions(@PathParam("annotationid") String ExternalIdentifier) throws SQLException {
        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(ExternalIdentifier));
        dbIntegrityService.setServiceURI(httpServletRequest.getServletPath());
        final PermissionList permissionList = dbIntegrityService.getPermissionsForAnnotation(annotationID);
        return new ObjectFactory().createPermissionList(permissionList);
    }

    ///////////////////////////////////////////////////////
    // TODO: how to return the status code? 
    @DELETE
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public String deleteAnnotation(@PathParam("annotationid") String externalIdentifier) throws SQLException {
        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(externalIdentifier));
        int[] resultDelete = dbIntegrityService.deleteAnnotation(annotationID);
        String result = Integer.toString(resultDelete[0]);
        return result + " annotation(s) deleted.";
    }

    ///////////////////////////////////////////////////////
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    public JAXBElement<ResponseBody> createAnnotation(Annotation annotation) throws SQLException {        
        return new ObjectFactory().createResponseBody(addORupdateAnnotation(annotation, true));
    }
    
    
    ///////////////////////////////////////////////////////
    // TODO: unit test
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<ResponseBody> updateAnnotation(@PathParam("annotationid") String externalIdentifier, Annotation annotation) throws SQLException, Exception { 
        if (!(httpServletRequest.getServletPath()+externalIdentifier).equals(annotation.getURI())){
            throw new Exception("External annotation id and the annotation id from the request body do not match");
        }
        return new ObjectFactory().createResponseBody(addORupdateAnnotation(annotation, false));
    }
    
    
    
    private ResponseBody makeResponseEnvelope(Number annotationID) throws SQLException{
        ResponseBody result = new ResponseBody();
        result.setPermissionResponse(null);        
        AnnotationResponseBody subresult = new AnnotationResponseBody();
        result.setAnnotationResponse(subresult);
        AnnotationActionList actions = new AnnotationActionList();
        AnnotationResponseContent content = new AnnotationResponseContent();
        AnnotationAction action = new AnnotationAction();
        actions.setAction(action);
        subresult.setActions(actions);
        subresult.setContent(content);
        
        if (annotationID != null) {
            Annotation annotation = dbIntegrityService.getAnnotation(annotationID);
            content.setAnnotation(annotation);
            List<Number> cached = dbIntegrityService.getSourcesWithNoCachedRepresentation(annotationID);
            if (cached == null) {
                subresult.setActions(null);
            } else {
                if (cached.isEmpty()) {
                    action.setAction(null);
                }
                else {
                   action.setAction(AnnotationActionName.CREATE_CACHED_REPRESENTATION); 
                }
            }

        } else {
            content.setAnnotation(null);
            action.setAction(null);
        }
        return result;
    }
    
    
    
    private ResponseBody addORupdateAnnotation(Annotation annotation, boolean newAnnotation) throws SQLException {
        String remoteUser = httpServletRequest.getRemoteUser();
        UUID userExternalID = (remoteUser != null) ? UUID.fromString(remoteUser) : null;
        Number userID = dbIntegrityService.getUserInternalIdentifier(userExternalID);    
        dbIntegrityService.setServiceURI(httpServletRequest.getServletPath());        
        Number newAnnotationID = newAnnotation ? dbIntegrityService.addUsersAnnotation(userID, annotation) : dbIntegrityService.updateUsersAnnotation(userID, annotation);
        return makeResponseEnvelope(newAnnotationID);
    }
}
