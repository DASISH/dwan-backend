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
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.PermissionActionName;
import eu.dasish.annotation.schema.UserWithPermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResponseBody;
import eu.dasish.annotation.schema.UserWithPermission;
import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olhsha
 */
@Component
@Path("/annotations")
@Transactional(rollbackFor = {Exception.class})
public class AnnotationResource {

    @Autowired
    private DBIntegrityService dbIntegrityService;
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;
    @Context
    private UriInfo uriInfo;
    @Context
    private Providers providers;
    @Context
    private ServletContext context;
    final String default_permission = "reader";
    private final Logger logger = LoggerFactory.getLogger(AnnotationResource.class);
    public static final Logger loggerServer = LoggerFactory.getLogger(HttpServletResponse.class);
    final private String admin = "admin";

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public void setProviders(Providers providers) {
        this.providers = providers;
    }

    public AnnotationResource() {
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    @Transactional(readOnly = true)
    public JAXBElement<Annotation> getAnnotation(@PathParam("annotationid") String externalIdentifier) throws IOException {

        URI baseURI = uriInfo.getBaseUri();
        String baseURIstr = baseURI.toString();
        dbIntegrityService.setServiceURI(baseURIstr);

        try {
            final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(externalIdentifier));
            if (annotationID != null) {
                String remoteUser = httpServletRequest.getRemoteUser();
                final Number userID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
                if (userID != null) {
                    if (dbIntegrityService.getTypeOfUserAccount(userID).equals(admin) || canRead(userID, annotationID)) {
                        final Annotation annotation = dbIntegrityService.getAnnotation(annotationID);
                        JAXBElement<Annotation> rootElement = new ObjectFactory().createAnnotation(annotation);
                        return rootElement;
                    } else {
                        loggerServer.debug(httpServletResponse.SC_FORBIDDEN + ": The logged-in user cannot read the annotation.");
                        httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "The logged-in user cannot read the annotation.");
                        return null;
                    }
                } else {
                    loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
                    return null;
                }
            } else {
                loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The annotation with the given id " + externalIdentifier + " is not found in the database");
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The annotation with the given id " + externalIdentifier + " is not found in the database");
                return null;
            }
        } catch (IllegalArgumentException e) {
            loggerServer.debug(HttpServletResponse.SC_BAD_REQUEST + ": Illegal argument UUID " + externalIdentifier);
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal argument UUID " + externalIdentifier);
            return null;
        }

    }

    //TODO: unit test
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/targets")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getAnnotationTargets(@PathParam("annotationid") String externalIdentifier) throws IOException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());

        try {
            final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(externalIdentifier));
            if (annotationID != null) {
                String remoteUser = httpServletRequest.getRemoteUser();
                final Number userID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
                if (userID != null) {
                    if (dbIntegrityService.getTypeOfUserAccount(userID).equals(admin) || canRead(userID, annotationID)) {
                        final ReferenceList TargetList = dbIntegrityService.getAnnotationTargets(annotationID);
                        logger.info("getAnnotationTargets method: OK");
                        return new ObjectFactory().createTargetList(TargetList);
                    } else {
                        loggerServer.debug(httpServletResponse.SC_FORBIDDEN + ": The logged-in user cannot read the annotation.");
                        httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "The logged-in user cannot read the annotation.");
                        return null;
                    }
                } else {
                    loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
                    return null;
                }
            } else {
                loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The annotation with the given id " + externalIdentifier + " is not found in the database");
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The annotation with the given id " + externalIdentifier + " is not found in the database");
                return null;
            }
        } catch (IllegalArgumentException e) {
            loggerServer.debug(HttpServletResponse.SC_BAD_REQUEST + ": Illegal argument UUID " + externalIdentifier);
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal argument UUID " + externalIdentifier);
            return null;
        }
    }
// TODO Unit test 

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("")
    @Transactional(readOnly = true)
    public JAXBElement<AnnotationInfoList> getFilteredAnnotations(@QueryParam("link") String link,
            @QueryParam("text") String text,
            @QueryParam("access") String permission,
            @QueryParam("namespace") String namespace,
            @QueryParam("owner") String ownerExternalId,
            @QueryParam("after") String after,
            @QueryParam("before") String before) throws IOException {

        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        String remoteUser = httpServletRequest.getRemoteUser();
        Number userID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (userID != null) {
            try {
                UUID ownerExternalUUID = (ownerExternalId != null) ? UUID.fromString(ownerExternalId) : null;
                String access = (permission != null) ? permission : default_permission;
                final AnnotationInfoList annotationInfoList = dbIntegrityService.getFilteredAnnotationInfos(link, text, userID, makeAccessModeChain(access), namespace, ownerExternalUUID, after, before);
                logger.info("getFilteredAnnotations method: OK");
                return new ObjectFactory().createAnnotationInfoList(annotationInfoList);
            } catch (IllegalArgumentException e) {
                loggerServer.debug(HttpServletResponse.SC_BAD_REQUEST + ": Illegal argument UUID " + ownerExternalId);
                httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal argument UUID " + ownerExternalId);
                return null;
            }
        } else {
            loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
            return null;
        }

    }

    // TODO Unit test    
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions")
    @Transactional(readOnly = true)
    public JAXBElement<UserWithPermissionList> getAnnotationPermissions(@PathParam("annotationid") String externalIdentifier) throws IOException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        try {
            final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(externalIdentifier));
            String remoteUser = httpServletRequest.getRemoteUser();
            Number userID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
            if (userID != null) {
                if (annotationID != null) {
                    if (dbIntegrityService.getTypeOfUserAccount(userID).equals(admin) || canRead(userID, annotationID)) {
                        final UserWithPermissionList permissionList = dbIntegrityService.getPermissionsForAnnotation(annotationID);
                        logger.debug("getAnnotationPermissions method: OK");
                        return new ObjectFactory().createPermissionList(permissionList);
                    } else {
                        loggerServer.debug(httpServletResponse.SC_FORBIDDEN + ": The logged-in user cannot read the annotation.");
                        httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "The logged-in user cannot read the annotation.");
                        return null;
                    }
                } else {
                    loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The annotation with the given id " + externalIdentifier + " is not found in the database");
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The annotation with the given id " + externalIdentifier + " is not found in the database");
                    return null;
                }
            } else {
                loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
                return null;
            }
        } catch (IllegalArgumentException e) {
            loggerServer.debug(HttpServletResponse.SC_BAD_REQUEST + ": Illegal argument UUID " + externalIdentifier);
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal argument UUID " + externalIdentifier);
            return null;
        }
    }
///////////////////////////////////////////////////////
// TODO: how to return the status code? 

    @DELETE
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public String deleteAnnotation(@PathParam("annotationid") String externalIdentifier) throws IOException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        try {
            final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(externalIdentifier));
            String remoteUser = httpServletRequest.getRemoteUser();
            Number userID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
            if (userID != null) {
                if (annotationID != null) {
                    if (isOwner(userID, annotationID)) {
                        int[] resultDelete = dbIntegrityService.deleteAnnotation(annotationID);
                        String result = Integer.toString(resultDelete[0]);
                        logger.info("deleteAnnotation method: OK");
                        return result + " annotation(s) deleted.";
                    } else {
                        loggerServer.debug(httpServletResponse.SC_FORBIDDEN + ": The logged-in user cannot delete the annotation. Only the owner can delete the annotation.");
                        httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "The logged-in user cannot delete the annotation. Only the owner can delete the annotation.");
                        return null;
                    }
                } else {
                    loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The annotation with the given id " + externalIdentifier + " is not found in the database");
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The annotation with the given id  " + externalIdentifier + " is not found in the database.");
                    return null;
                }

            } else {
                loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database.");
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database.");
                return null;
            }
        } catch (IllegalArgumentException e) {
            loggerServer.debug(HttpServletResponse.SC_BAD_REQUEST + ": Illegal argument UUID " + externalIdentifier);
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal argument UUID " + externalIdentifier);
            return null;
        }
    }

    // TODO: how to return the status code? 
///////////////////////////////////////////////////////
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    public JAXBElement<ResponseBody> createAnnotation(Annotation annotation) throws IOException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        String remoteUser = httpServletRequest.getRemoteUser();
        Number userID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (userID != null) {
            Number annotationID = dbIntegrityService.addUsersAnnotation(userID, annotation);
            return new ObjectFactory().createResponseBody(makeAnnotationResponseEnvelope(annotationID));
        } else {
            loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database.");
            return null;
        }
    }
///////////////////////////////////////////////////////
// TODO: unit test

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<ResponseBody> updateAnnotation(@PathParam("annotationid") String externalIdentifier, Annotation annotation) throws IOException {
        String path = uriInfo.getBaseUri().toString();
        dbIntegrityService.setServiceURI(path);
        String annotationURI = annotation.getURI();

        if (!(path + "annotations/" + externalIdentifier).equals(annotationURI)) {
            loggerServer.debug(httpServletResponse.SC_CONFLICT + "Wrong request: the annotation identifier   " + externalIdentifier + " and the annotation ID from the request body do not match. Correct the request and resend.");
            httpServletResponse.sendError(HttpServletResponse.SC_CONFLICT, "Wrong request: the annotation identifier   " + externalIdentifier + " and the annotation ID from the request body do not match. Correct the request and resend.");
            return null;
        }

        List<UserWithPermission> permissions = annotation.getPermissions().getUserWithPermission();
        String ownerURI = annotation.getOwnerRef();
        if (!ownerIsListed(ownerURI, permissions)) {
            loggerServer.debug(httpServletResponse.SC_CONFLICT + "Wrong request body: the  owner URI's is not listed in the list of permissions as 'owner'. Correct the request and resend.");
            httpServletResponse.sendError(HttpServletResponse.SC_CONFLICT, "Wrong request body: the  owner URI's is not listed in the list of permissions as 'owner'. Correct the request and resend.");
            return null;
        }

        try {
            final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(externalIdentifier));
            if (annotationID != null) {
                String remoteUser = httpServletRequest.getRemoteUser();
                Number userID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
                if (userID != null) {
                    if (isOwner(userID, annotationID)) {
                        int updatedRows = dbIntegrityService.updateUsersAnnotation(userID, annotation);
                        return new ObjectFactory().createResponseBody(makeAnnotationResponseEnvelope(annotationID));

                    } else {
                        loggerServer.debug(httpServletResponse.SC_UNAUTHORIZED + "Only the owner can update the annotation fully (incl. permissions). Only writer can update the annotation's body.");
                        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Only the owner can update the annotation fully (incl. permissions). Only writer can update the annotation's body.");
                        return null;
                    }
                } else {
                    loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database.");
                    return null;
                }
            } else {
                loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The annotation with the given id " + externalIdentifier + " is not found in the database");
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The annotation with the given id   " + externalIdentifier + " is not found in the database.");
                return null;
            }
        } catch (IllegalArgumentException e) {
            loggerServer.debug(HttpServletResponse.SC_BAD_REQUEST + ": Illegal argument UUID " + externalIdentifier);
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal argument UUID " + externalIdentifier);
            return null;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/body")
    public JAXBElement<ResponseBody> updateAnnotationBody(@PathParam("annotationid") String externalIdentifier, AnnotationBody annotationBody) throws IOException {
        String path = uriInfo.getBaseUri().toString();
        dbIntegrityService.setServiceURI(path);
        try {
            final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(externalIdentifier));
            String remoteUser = httpServletRequest.getRemoteUser();
            Number userID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
            if (userID != null) {
                if (annotationID != null) {
                    if (canWrite(userID, annotationID)) {
                        int updatedRows = dbIntegrityService.updateAnnotationBody(annotationID, annotationBody);
                        return new ObjectFactory().createResponseBody(makeAnnotationResponseEnvelope(annotationID));
                    } else {
                        loggerServer.debug(httpServletResponse.SC_UNAUTHORIZED + "The logged-in user cannot change the body of this annotation because (s)he is  not its 'writer'.");
                        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The logged-in user cannot change the body of this annotation because (s)he is  not its 'writer'.");
                        return null;
                    }
                } else {
                    loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The annotation with the given id " + externalIdentifier + " is not found in the database");
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The annotation with the given id   " + externalIdentifier + " is not found in the database.");
                    return null;
                }
            } else {
                loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database.");
                return null;
            }
        } catch (IllegalArgumentException e) {
            loggerServer.debug(HttpServletResponse.SC_BAD_REQUEST + ": Illegal argument UUID " + externalIdentifier);
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal argument UUID " + externalIdentifier);
            return null;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/{userid: " + BackendConstants.regExpIdentifier + "}")
    public String updatePermission(@PathParam("annotationid") String annotationExternalId,
            @PathParam("userid") String userExternalId, Permission permission) throws IOException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        String remoteUser = httpServletRequest.getRemoteUser();
        Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (remoteUserID != null) {
            try {
                final Number userID = dbIntegrityService.getUserInternalIdentifier(UUID.fromString(userExternalId));
                if (userID != null) {
                    try {
                        final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(annotationExternalId));
                        if (annotationID != null) {
                            if (isOwner(remoteUserID, annotationID)) {
                                int result = (dbIntegrityService.getPermission(annotationID, userID) != null)
                                        ? dbIntegrityService.updateAnnotationPrincipalPermission(annotationID, userID, permission)
                                        : dbIntegrityService.addAnnotationPrincipalPermission(annotationID, userID, permission);
                                logger.info("updatePermission method: OK");
                                return result + " rows are updated/added";

                            } else {
                                loggerServer.debug(httpServletResponse.SC_UNAUTHORIZED + "The logged-in user cannot change the body of this annotation because (s)he is  not its 'writer'.");
                                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The logged-in user cannot change the rights on this annotation because (s)he is  not its owner.");
                                return null;
                            }
                        } else {
                            loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The annotation with the given id " + annotationExternalId + " is not found in the database");
                            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The annotation with the given id  " + annotationExternalId + "  is not found in the database.");
                            return null;
                        }
                    } catch (IllegalArgumentException e) {
                        loggerServer.debug(HttpServletResponse.SC_BAD_REQUEST + ": Illegal argument UUID " + annotationExternalId);
                        httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal argument UUID " + annotationExternalId);
                        return null;
                    }
                } else {
                    loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The user with the given id   " + userExternalId + " is not found in the database");
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The user with the given id   " + userExternalId + " is not found in the database.");
                    return null;
                }
            } catch (IllegalArgumentException e) {
                loggerServer.debug(HttpServletResponse.SC_BAD_REQUEST + ": Illegal argument UUID " + userExternalId);
                httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal argument UUID " + userExternalId);
                return null;
            }

        } else {
            loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database.");
            return null;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/")
    public JAXBElement<ResponseBody> updatePermissions(@PathParam("annotationid") String annotationExternalId, UserWithPermissionList permissions) throws IOException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        try {
            final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(annotationExternalId));
            String remoteUser = httpServletRequest.getRemoteUser();
            Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
            if (remoteUserID != null) {
                if (annotationID != null) {
                    if (isOwner(remoteUserID, annotationID)) {
                        int updatedRows = dbIntegrityService.updatePermissions(annotationID, permissions);
                        return new ObjectFactory().createResponseBody(makePermissionResponseEnvelope(annotationID));
                    } else {
                        loggerServer.debug(httpServletResponse.SC_UNAUTHORIZED + "The logged-in user cannot change the access rights on this annotation because (s)he is  not its owner.");
                        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The logged-in user cannot change the access rights on this annotation because (s)he is  not its owner.");
                        return null;
                    }
                } else {
                    loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The annotation with the given id " + annotationExternalId + " is not found in the database");
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The annotation with the given id  " + annotationExternalId + "  is not found in the database.");
                    return null;
                }
            } else {
                loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database.");
                return null;
            }
        } catch (IllegalArgumentException e) {
            loggerServer.debug(HttpServletResponse.SC_BAD_REQUEST + ": Illegal argument UUID " + annotationExternalId);
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal argument UUID " + annotationExternalId);
            return null;
        }
    }
    
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{annotationId: " + BackendConstants.regExpIdentifier + "}/user/{userId}/delete")
    public String deleteUsersPermission(@PathParam("annotationId") String annotationId, @PathParam("userId") String userId) throws IOException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        int deletedRows = 0;
        try {
            final Number annotationID = dbIntegrityService.getAnnotationInternalIdentifier(UUID.fromString(annotationId));
            String remoteUser = httpServletRequest.getRemoteUser();
            Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
            if (remoteUserID != null) {
                if (annotationID != null) {
                    if (isOwner(remoteUserID, annotationID)) {
                        Number userID = dbIntegrityService.getUserInternalIdentifier(UUID.fromString(userId));
                        if (userID != null) {
                            deletedRows = dbIntegrityService.updateAnnotationPrincipalPermission(annotationID, userID, null);

                        } else {
                            loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the user external identifier " + userId + " is not found the the databse.");
                            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The user external identifier " + userId + " is not found the the databse.");

                        }
                    } else {
                        loggerServer.debug(httpServletResponse.SC_UNAUTHORIZED + "The logged-in user cannot change the access rights on this annotation because (s)he is  not its owner.");
                        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The logged-in user cannot change the access rights on this annotation because (s)he is  not its owner.");

                    }
                } else {
                    loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The annotation with the given id " + annotationId + " is not found in the database");
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The annotation with the given id  " + annotationId + "  is not found in the database.");

                }
            } else {
                loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database.");

            }
        } catch (IllegalArgumentException e) {
            loggerServer.debug(HttpServletResponse.SC_BAD_REQUEST + ": Illegal argument UUID " + annotationId);
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal argument UUID " + annotationId);

        }
        return (deletedRows + " is deleted.");
    }
/////////////////////////////////////////

    private ResponseBody makeAnnotationResponseEnvelope(Number annotationID) {
        ResponseBody result = new ResponseBody();
        result.setPermissions(null);
        Annotation annotation = dbIntegrityService.getAnnotation(annotationID);
        result.setAnnotation(annotation);
        List<String> targetsNoCached = dbIntegrityService.getTargetsWithNoCachedRepresentation(annotationID);
        ActionList actionList = new ActionList();
        result.setActionList(actionList);
        actionList.getAction().addAll(makeActionList(targetsNoCached, AnnotationActionName.CREATE_CACHED_REPRESENTATION.value()));
        return result;
    }

    /////////////////////////////////////////
    private ResponseBody makePermissionResponseEnvelope(Number annotationID) {
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

    // REFACTOR : move to the integrity service all te methods below  
    private List<Action> makeActionList(List<String> resourceURIs, String message) {
        if (resourceURIs != null) {
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
        } else {
            return null;
        }
    }

    private boolean canRead(Number userID, Number annotationID) {
        final Permission permission = dbIntegrityService.getPermission(annotationID, userID);
        if (permission != null) {
            return (permission.value().equals(Permission.OWNER.value()) || permission.value().equals(Permission.WRITER.value()) || permission.value().equals(Permission.READER.value()));
        } else {
            return false;
        }
    }

    private boolean canWrite(Number userID, Number annotationID) {
        final Permission permission = dbIntegrityService.getPermission(annotationID, userID);
        if (permission != null) {
            return (permission.value().equals(Permission.OWNER.value()) || permission.value().equals(Permission.WRITER.value()));
        } else {
            return false;
        }
    }

    private boolean isOwner(Number userID, Number annotationID) {
        final Permission permission = dbIntegrityService.getPermission(annotationID, userID);
        if (permission != null) {
            return (permission.value().equals(Permission.OWNER.value()));
        } else {
            return false;
        }
    }

    private String[] makeAccessModeChain(String accessMode) {
        if (accessMode != null) {
            if (accessMode.contains(Permission.OWNER.value())) {
                String[] result = new String[1];
                result[0] = accessMode;
                return result;
            } else {
                if (accessMode.equals(Permission.WRITER.value())) {
                    String[] result = new String[2];
                    result[0] = Permission.WRITER.value();
                    result[1] = Permission.OWNER.value();
                    return result;
                } else {
                    if (accessMode.equals(Permission.READER.value())) {
                        String[] result = new String[3];
                        result[0] = Permission.READER.value();
                        result[1] = Permission.WRITER.value();
                        result[2] = Permission.OWNER.value();
                        return result;
                    } else {
                        logger.error("Invalide access " + accessMode);
                        return null;
                    }

                }
            }

        } else {
            return null;
        }
    }

    private boolean ownerIsListed(String uri, List<UserWithPermission> permissions) {
        for (UserWithPermission permissionPair : permissions) {
            if (permissionPair.getRef().equals(uri) && permissionPair.getPermission().equals(Permission.OWNER)) {
                return true;
            }
        }
        return false;
    }
}
