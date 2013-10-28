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
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.UserWithPermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.Target;
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
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
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
    public JAXBElement<ResponseBody> createAnnotation(Annotation annotation) throws SQLException, Exception {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());

        return new ObjectFactory().createResponseBody(addORupdateAnnotation(annotation, true));
    }

    ///////////////////////////////////////////////////////
    // TODO: unit test
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<ResponseBody> updateAnnotation(@PathParam("annotationid") String externalIdentifier, Annotation annotation) throws SQLException, Exception {
        String path = uriInfo.getBaseUri().toString();
        dbIntegrityService.setServiceURI(path);


        if (!(path + externalIdentifier).equals(annotation.getURI())) {
            throw new Exception("External annotation id and the annotation id from the request body do not match");
        }
        return new ObjectFactory().createResponseBody(addORupdateAnnotation(annotation, false));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/{userid: " + BackendConstants.regExpIdentifier + "}")
    public int updatePermission(@PathParam("annotationid") String annotationExternalId, @PathParam("userid") String userExternalId, Permission permission) throws SQLException, Exception {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());

        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(annotationExternalId));
        final Number userID = dbIntegrityService.getUserInternalIdentifier(UUID.fromString(userExternalId));
        int result;
        if (dbIntegrityService.getPermission(annotationID, userID) != null) {
            result = dbIntegrityService.updateAnnotationPrincipalPermission(annotationID, userID, permission);
        } else {
            result = dbIntegrityService.updateAnnotationPrincipalPermission(annotationID, userID, permission);
        }
        return result;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/")
    public int updatePermissions(@PathParam("annotationid") String annotationExternalId, UserWithPermissionList permissions) throws SQLException, Exception {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());

        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(annotationExternalId));
        return dbIntegrityService.updatePermissions(annotationID, permissions);
    }

    private ResponseBody makeResponseEnvelope(Number annotationID) throws SQLException {
        ResponseBody result = new ResponseBody();
        result.setPermissionResponse(null);
        AnnotationResponseBody subresult = new AnnotationResponseBody();
        result.setAnnotationResponse(subresult);
        AnnotationResponseContent content = new AnnotationResponseContent();
        subresult.setContent(content);

        if (annotationID != null) {
            Annotation annotation = dbIntegrityService.getAnnotation(annotationID);
            content.setAnnotation(annotation);
            List<Number> targetsNoCached = dbIntegrityService.getTargetsWithNoCachedRepresentation(annotationID);
            if (targetsNoCached == null) {
                subresult.setActions(null);
            } else {
                if (targetsNoCached.isEmpty()) {
                    subresult.setActions(new AnnotationActionList());
                } else {

                    AnnotationActionList actions = new AnnotationActionList();
                    subresult.setActions(actions);
                    for (Number target : targetsNoCached) {
                        AnnotationAction action = new AnnotationAction();
                        actions.getAction().add(action);
                        action.setAction(AnnotationActionName.CREATE_CACHED_REPRESENTATION);
                        action.setObject(dbIntegrityService.getTargetURI(target));
                    }
                }
            }

        } else {
            content.setAnnotation(null);
        }
        return result;
    }

    private ResponseBody addORupdateAnnotation(Annotation annotation, boolean newAnnotation) throws SQLException, Exception {
        // Where the map remoteUSer-->externalID is saved and how to get it?
        String remoteUser = httpServletRequest.getRemoteUser();
        // if (remoteUser == null) { throw new Exception();}
        //UUID externalID = getExternalIDforREmoteUSer(remoteUser);
        //testing mode 
        UUID userExternalID = UUID.fromString("00000000-0000-0000-0000-0000000000111");
        Number userID = dbIntegrityService.getUserInternalIdentifier(userExternalID);
        Number annotationID;
        if (newAnnotation) {
            annotationID = dbIntegrityService.addUsersAnnotation(userID, annotation);
        } else {
            dbIntegrityService.updateUsersAnnotation(userID, annotation);
            annotationID = dbIntegrityService.getAnnotationInternalIdentifierFromURI(annotation.getURI());
        }
        return makeResponseEnvelope(annotationID);
    }
}
