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
import eu.dasish.annotation.schema.AnnotationActionName;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.Action;
import eu.dasish.annotation.schema.ActionList;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.PermissionActionName;
import eu.dasish.annotation.schema.UserWithPermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResponseBody;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
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
    @Context
    private UriInfo uriInfo;
    @Context
    protected Providers providers;

    /// used in testing
    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public void setProviders(Providers providers) {
        this.providers = providers;
    }
    ////////////////

    public AnnotationResource() {
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    @Secured("ROLE_USER")
    public JAXBElement<Annotation> getAnnotation(@PathParam("annotationid") String ExternalIdentifier) throws SQLException, JAXBException, Exception {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(ExternalIdentifier));
        final Annotation annotation = dbIntegrityService.getAnnotation(annotationID);

        //ReTargetJaxbMarshallerProvider contextResolver = (ReTargetJaxbMarshallerProvider) providers.getContextResolver(Marshaller.class, MediaType.WILDCARD_TYPE);
        //contextResolver.setReTargetJaxbFactory(new JaxbMarshallerFactory());

        JAXBElement<Annotation> rootElement = new ObjectFactory().createAnnotation(annotation);
        return rootElement;

    }

    //TODO: unit test
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/targets")
    @Secured("ROLE_USER")
    public JAXBElement<ReferenceList> getAnnotationTargets(@PathParam("annotationid") String ExternalIdentifier) throws SQLException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(ExternalIdentifier));
        final ReferenceList TargetList = dbIntegrityService.getAnnotationTargets(annotationID);
        return new ObjectFactory().createTargetList(TargetList);
    }

    // TODO Unit test 
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("")
    @Secured("ROLE_USER")
    public JAXBElement<AnnotationInfoList> getFilteredAnnotations(@QueryParam("link") String link,
            @QueryParam("text") String text,
            @QueryParam("access") String permission,
            @QueryParam("access") String namespace,
            @QueryParam("after") Timestamp after,
            @QueryParam("before") Timestamp before) throws SQLException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());

        String remoteUser = httpServletRequest.getRemoteUser();
        UUID userExternalID = (remoteUser != null) ? UUID.fromString(remoteUser) : null;

        final AnnotationInfoList annotationInfoList = dbIntegrityService.getFilteredAnnotationInfos(link, text, text, namespace, userExternalID, after, before);
        return new ObjectFactory().createAnnotationInfoList(annotationInfoList);
    }

    // TODO Unit test    
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions")
    @Secured("ROLE_USER")
    public JAXBElement<UserWithPermissionList> getAnnotationPermissions(@PathParam("annotationid") String ExternalIdentifier) throws SQLException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());

        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(ExternalIdentifier));
        final UserWithPermissionList permissionList = dbIntegrityService.getPermissionsForAnnotation(annotationID);
        return new ObjectFactory().createPermissionList(permissionList);
    }

    ///////////////////////////////////////////////////////
    // TODO: how to return the status code? 
    @DELETE
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    @Secured("ROLE_USER")
    public String deleteAnnotation(@PathParam("annotationid") String externalIdentifier) throws SQLException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
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
    @Secured("ROLE_USER")
    public JAXBElement<ResponseBody> createAnnotation(Annotation annotation) throws SQLException, Exception {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());

        //testing mode 
        //String remoteUser = httpServletRequest.getRemoteUser();
        // if (remoteUser == null) { throw new Exception();}
        //UUID externalID = getExternalIDforREmoteUSer(remoteUser);
        UUID userExternalID = UUID.fromString("00000000-0000-0000-0000-0000000000111");

        Number userID = dbIntegrityService.getUserInternalIdentifier(userExternalID);
        Number annotationID = dbIntegrityService.addUsersAnnotation(userID, annotation);
        return new ObjectFactory().createResponseBody(makeAnnotationResponseEnvelope(annotationID));
    }

    ///////////////////////////////////////////////////////
    // TODO: unit test
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    @Secured("ROLE_USER")
    public JAXBElement<ResponseBody> updateAnnotation(@PathParam("annotationid") String externalIdentifier, Annotation annotation) throws SQLException, Exception {
        String path = uriInfo.getBaseUri().toString();
        dbIntegrityService.setServiceURI(path);
        String annotationURI = annotation.getURI();
        if (!(path + "annotations/" + externalIdentifier).equals(annotationURI)) {
            throw new Exception("External annotation id and the annotation id from the request body do not match");
        }

        //testing mode 
        //String remoteUser = httpServletRequest.getRemoteUser();
        // if (remoteUser == null) { throw new Exception();}
        //UUID externalID = getExternalIDforREmoteUSer(remoteUser);
        UUID userExternalID = UUID.fromString("00000000-0000-0000-0000-0000000000111");

        Number userID = dbIntegrityService.getUserInternalIdentifier(userExternalID);
        int updatedRows = dbIntegrityService.updateUsersAnnotation(userID, annotation);
        Number annotationID=dbIntegrityService.getAnnotationInternalIdentifierFromURI(annotationURI); 
        return new ObjectFactory().createResponseBody(makeAnnotationResponseEnvelope(annotationID));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)    
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/{userid: " + BackendConstants.regExpIdentifier + "}")
    @Secured("ROLE_USER")
    public String updatePermission(@PathParam("annotationid") String annotationExternalId, @PathParam("userid") String userExternalId, Permission permission) throws SQLException, Exception {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        //String remoteUser = httpServletRequest.getRemoteUser();
        //UUID externalID = getExternalIDforREmoteUSer(remoteUser);
        //testing mode 
        UUID remoteUserExternalID = UUID.fromString("00000000-0000-0000-0000-0000000000111");
        Number remoteUserID = dbIntegrityService.getUserInternalIdentifier(remoteUserExternalID);
        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(annotationExternalId));
        final Number userID = dbIntegrityService.getUserInternalIdentifier(UUID.fromString(userExternalId));
        int result;
        if (dbIntegrityService.getPermission(annotationID, userID) != null) {
            result = dbIntegrityService.updateAnnotationPrincipalPermission(annotationID, userID, permission, remoteUserID);
        } else {
            result = dbIntegrityService.addAnnotationPrincipalPermission(annotationID, userID, permission, remoteUserID);
        }

        return (result + " rows are updated/added");
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/")
    @Secured("ROLE_USER")
    public JAXBElement<ResponseBody> updatePermissions(@PathParam("annotationid") String annotationExternalId, UserWithPermissionList permissions) throws SQLException, Exception {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        //String remoteUser = httpServletRequest.getRemoteUser();
        //UUID externalID = getExternalIDforREmoteUSer(remoteUser);
        //testing mode 
        UUID remoteUserExternalID = UUID.fromString("00000000-0000-0000-0000-0000000000111");
        Number remoteUserID = dbIntegrityService.getUserInternalIdentifier(remoteUserExternalID);
        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(annotationExternalId));
        int updatedRows = dbIntegrityService.updatePermissions(annotationID, permissions, remoteUserID);
        return new ObjectFactory().createResponseBody(makePermissionResponseEnvelope(annotationID));
    }

    /////////////////////////////////////////
   
    private ResponseBody makeAnnotationResponseEnvelope(Number annotationID) throws SQLException {
        ResponseBody result = new ResponseBody();
        result.setPermissions(null);        
        Annotation annotation = dbIntegrityService.getAnnotation(annotationID);
        result.setAnnotation(annotation);
        List<String> targetsNoCached = dbIntegrityService.getTargetsWithNoCachedRepresentation(annotationID);
        ActionList actionList = new ActionList();
        result.setActionList(actionList);
        actionList.getAction().addAll(makeActionList(targetsNoCached,  AnnotationActionName.CREATE_CACHED_REPRESENTATION.value()));        
        return result;
    }

    /////////////////////////////////////////
    private ResponseBody makePermissionResponseEnvelope(Number annotationID) throws SQLException {
        ResponseBody result = new ResponseBody();
        result.setAnnotation(null);        
        UserWithPermissionList permissions = dbIntegrityService.getPermissionsForAnnotation(annotationID);
        result.setPermissions(permissions);
        List<String> usersWithNoInfo = dbIntegrityService.getUsersWithNoInfo(annotationID);
        ActionList actionList = new ActionList();
        result.setActionList(actionList);
        actionList.getAction().addAll(makeActionList(usersWithNoInfo, PermissionActionName.PROVIDE_USER_INFO.value()));
        return result;
    }
    
    private List<Action>  makeActionList(List<String> resourceURIs, String message){
        if (resourceURIs!=null){
          if (resourceURIs.isEmpty()) {
                return (new ArrayList<Action>());
            } else {
                List<Action> result = new ArrayList<Action>();                
                for (String resourceURI : resourceURIs) {
                    Action action = new Action();
                    result.add(action);
                    action.setMessage(message);
                    action.setObject(resourceURI);
                }
                return result;
            }  
        }
        else {
            return null;
        }
    }
}
