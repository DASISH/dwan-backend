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
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.dao.DBIntegrityService;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Permission;
import eu.dasish.annotation.schema.UserWithPermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResponseBody;
import java.io.IOException;
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
public class AnnotationResource extends ResourceResource {

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
        Number userID = this.getUserID();
        if (userID != null) {
            try {
                final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);
                if (annotationID != null) {

                    if (dbIntegrityService.canRead(userID, annotationID)) {
                        final Annotation annotation = dbIntegrityService.getAnnotation(annotationID);
                        return new ObjectFactory().createAnnotation(annotation);
                    } else {
                        verboseOutput.FORBIDDEN_ANNOTATION_READING(externalIdentifier, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                    }

                } else {
                    verboseOutput.ANNOTATION_NOT_FOUND(externalIdentifier);
                }
            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(externalIdentifier);
            }
        }

        return new ObjectFactory().createAnnotation(new Annotation());
    }

    //TODO: unit test
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/targets")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getAnnotationTargets(@PathParam("annotationid") String externalIdentifier) throws IOException {
        Number userID = this.getUserID();
        if (userID != null) {
            try {
                final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);
                if (annotationID != null) {

                    if (dbIntegrityService.canRead(userID, annotationID)) {
                        final ReferenceList TargetList = dbIntegrityService.getAnnotationTargets(annotationID);
                        return new ObjectFactory().createTargetList(TargetList);
                    } else {
                        verboseOutput.FORBIDDEN_ANNOTATION_READING(externalIdentifier, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                    }

                } else {
                    verboseOutput.ANNOTATION_NOT_FOUND(externalIdentifier);

                }
            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(externalIdentifier);
            }
        }
        return new ObjectFactory().createTargetList(new ReferenceList());
    }
// TODO Unit test 

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("")
    @Transactional(readOnly = true)
    public JAXBElement<AnnotationInfoList> getFilteredAnnotations(@QueryParam("link") String link,
            @QueryParam("text") String text,
            @QueryParam("access") String access,
            @QueryParam("namespace") String namespace,
            @QueryParam("owner") String ownerExternalId,
            @QueryParam("after") String after,
            @QueryParam("before") String before) throws IOException {

        Number userID = this.getUserID();
        if (userID != null) {
            try {
                UUID ownerExternalUUID = (ownerExternalId != null) ? UUID.fromString(ownerExternalId) : null;

                final AnnotationInfoList annotationInfoList = dbIntegrityService.getFilteredAnnotationInfos(ownerExternalUUID, link, text, userID, access, namespace, after, before);
                return new ObjectFactory().createAnnotationInfoList(annotationInfoList);
            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(ownerExternalId);
            }
        }
        return new ObjectFactory().createAnnotationInfoList(new AnnotationInfoList());
    }

    // TODO Unit test    
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions")
    @Transactional(readOnly = true)
    public JAXBElement<UserWithPermissionList> getAnnotationPermissions(@PathParam("annotationid") String externalIdentifier) throws IOException {
        Number userID = this.getUserID();
        if (userID != null) {
            try {
                final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);
                if (annotationID != null) {
                    if (dbIntegrityService.canRead(userID, annotationID)) {
                        final UserWithPermissionList permissionList = dbIntegrityService.getPermissions(annotationID, Resource.ANNOTATION);
                        return new ObjectFactory().createPermissionList(permissionList);
                    } else {
                        verboseOutput.FORBIDDEN_ANNOTATION_READING(externalIdentifier, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                    }
                } else {
                    verboseOutput.ANNOTATION_NOT_FOUND(externalIdentifier);
                }

            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(externalIdentifier);
            }
        }
        return new ObjectFactory().createUserWithPermissionList(new UserWithPermissionList());
    }
///////////////////////////////////////////////////////
// TODO: how to return the status code? 

    @DELETE
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public String deleteAnnotation(@PathParam("annotationid") String externalIdentifier) throws IOException {
        Number userID = this.getUserID();
        if (userID != null) {
            try {
                final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);
                if (annotationID != null) {
                    if (userID.equals(dbIntegrityService.getAnnotationOwnerID(annotationID)) || dbIntegrityService.getTypeOfUserAccount(userID).equals(admin)) {
                        int[] resultDelete = dbIntegrityService.deleteAnnotation(annotationID);
                        String result = Integer.toString(resultDelete[0]);
                        return result + " annotation(s) deleted.";
                    } else {
                        verboseOutput.FORBIDDEN_ANNOTATION_WRITING(externalIdentifier, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                    }
                } else {
                    verboseOutput.ANNOTATION_NOT_FOUND(externalIdentifier);
                }

            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(externalIdentifier);
            }
        }
        return "Due to the failure no annotation is deleted.";
    }

    // TODO: how to return the status code? 
///////////////////////////////////////////////////////
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    public JAXBElement<ResponseBody> createAnnotation(Annotation annotation) throws IOException {
        Number userID = this.getUserID();
        if (userID != null) {
            Number annotationID = dbIntegrityService.addUsersAnnotation(userID, annotation);
            return new ObjectFactory().createResponseBody(dbIntegrityService.makeAnnotationResponseEnvelope(annotationID));

        }
        return new ObjectFactory().createResponseBody(new ResponseBody());
    }
///////////////////////////////////////////////////////
// TODO: unit test

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<ResponseBody> updateAnnotation(@PathParam("annotationid") String externalIdentifier, Annotation annotation) throws IOException {
        Number userID = this.getUserID();
        if (userID != null) {
            String path = uriInfo.getBaseUri().toString();
            String annotationURI = annotation.getURI();

            if (!(path + "annotations/" + externalIdentifier).equals(annotationURI)) {
                verboseOutput.IDENTIFIER_MISMATCH(externalIdentifier);
                return new ObjectFactory().createResponseBody(new ResponseBody());
            }

            try {
                final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);
                if (annotationID != null) {

                    if (userID != null) {
                        if (userID.equals(dbIntegrityService.getAnnotationOwnerID(annotationID)) || dbIntegrityService.getTypeOfUserAccount(userID).equals(admin)) {
                            int updatedRows = dbIntegrityService.updateAnnotation(annotation);
                            return new ObjectFactory().createResponseBody(dbIntegrityService.makeAnnotationResponseEnvelope(annotationID));
                        } else {
                            verboseOutput.FORBIDDEN_PERMISSION_CHANGING(externalIdentifier, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                            loggerServer.debug(" Permission changing is the part of the full update of the annotation.");
                        }
                    }
                } else {
                    verboseOutput.ANNOTATION_NOT_FOUND(externalIdentifier);
                }
            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(externalIdentifier);
            }
        }
        return new ObjectFactory().createResponseBody(new ResponseBody());
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/body")
    public JAXBElement<ResponseBody> updateAnnotationBody(@PathParam("annotationid") String externalIdentifier, AnnotationBody annotationBody) throws IOException {
        Number userID = this.getUserID();
        if (userID != null) {
            try {
                final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);
                if (annotationID != null) {
                    if (dbIntegrityService.canWrite(userID, annotationID)) {
                        int updatedRows = dbIntegrityService.updateAnnotationBody(annotationID, annotationBody);
                        return new ObjectFactory().createResponseBody(dbIntegrityService.makeAnnotationResponseEnvelope(annotationID));
                    } else {
                        verboseOutput.FORBIDDEN_ANNOTATION_WRITING(externalIdentifier, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                    }
                } else {
                    verboseOutput.ANNOTATION_NOT_FOUND(externalIdentifier);
                }

            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(externalIdentifier);
            }
        }
        return new ObjectFactory().createResponseBody(new ResponseBody());
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/{userid: " + BackendConstants.regExpIdentifier + "}")
    public String updatePermission(@PathParam("annotationid") String annotationExternalId,
            @PathParam("userid") String userExternalId, Permission permission) throws IOException {
        Number remoteUserID = this.getUserID();
        if (remoteUserID != null) {
            try {
                final Number userID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(userExternalId), Resource.PRINCIPAL);
                if (userID != null) {
                    try {
                        final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(annotationExternalId), Resource.ANNOTATION);
                        if (annotationID != null) {
                            if (remoteUserID.equals(dbIntegrityService.getAnnotationOwnerID(annotationID)) || dbIntegrityService.getTypeOfUserAccount(remoteUserID).equals(admin)) {
                                int result = (dbIntegrityService.getPermission(annotationID, userID) != null)
                                        ? dbIntegrityService.updateAnnotationPrincipalPermission(annotationID, userID, permission)
                                        : dbIntegrityService.addAnnotationPrincipalPermission(annotationID, userID, permission);
                                return result + " rows are updated/added";

                            } else {
                                verboseOutput.FORBIDDEN_PERMISSION_CHANGING(annotationExternalId, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                            }
                        } else {
                            verboseOutput.ANNOTATION_NOT_FOUND(annotationExternalId);
                        }
                    } catch (IllegalArgumentException e) {
                        verboseOutput.ILLEGAL_UUID(annotationExternalId);
                    }
                } else {
                    verboseOutput.PRINCIPAL_NOT_FOUND(userExternalId);
                }
            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(userExternalId);
            }
        }
        return "Due to the failure no permissionis updated.";
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/")
    public JAXBElement<ResponseBody> updatePermissions(@PathParam("annotationid") String annotationExternalId, UserWithPermissionList permissions) throws IOException {
        Number remoteUserID = this.getUserID();
        if (remoteUserID != null) {
            final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(annotationExternalId), Resource.ANNOTATION);
            if (annotationID != null) {
                if (remoteUserID.equals(dbIntegrityService.getAnnotationOwnerID(annotationID)) || dbIntegrityService.getTypeOfUserAccount(remoteUserID).equals(admin)) {
                    int updatedRows = dbIntegrityService.updatePermissions(annotationID, permissions);
                    return new ObjectFactory().createResponseBody(dbIntegrityService.makePermissionResponseEnvelope(annotationID, Resource.ANNOTATION));
                } else {
                    verboseOutput.FORBIDDEN_PERMISSION_CHANGING(annotationExternalId, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                }
            } else {
                verboseOutput.ANNOTATION_NOT_FOUND(annotationExternalId);
            }

        }
        return new ObjectFactory().createResponseBody(new ResponseBody());

    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{annotationId: " + BackendConstants.regExpIdentifier + "}/user/{userId}/delete")
    public String deleteUsersPermission(@PathParam("annotationId") String annotationId, @PathParam("userId") String userId) throws IOException {
        Number remoteUserID = this.getUserID();
        int deletedRows = 0;
        if (remoteUserID != null) {
            try {
                final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(annotationId), Resource.ANNOTATION);

                if (annotationID != null) {
                    if (remoteUserID.equals(dbIntegrityService.getAnnotationOwnerID(annotationID)) || dbIntegrityService.getTypeOfUserAccount(remoteUserID).equals(admin)) {
                        Number userID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(userId), Resource.PRINCIPAL);
                        if (userID != null) {
                            deletedRows = dbIntegrityService.updateAnnotationPrincipalPermission(annotationID, userID, null);
                        } else {
                            verboseOutput.PRINCIPAL_NOT_FOUND(userId);
                        }
                    } else {
                        verboseOutput.FORBIDDEN_PERMISSION_CHANGING(annotationId, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());

                    }
                } else {
                    verboseOutput.ANNOTATION_NOT_FOUND(annotationId);
                }

            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(annotationId);
            }
        }
        return (deletedRows + " is deleted.");
    }
}
