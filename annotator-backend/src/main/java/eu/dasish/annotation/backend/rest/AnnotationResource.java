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
import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.PermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResponseBody;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.http.HTTPException;
import org.springframework.stereotype.Component;
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
        Number principalID = this.getPrincipalID();
        try {
            final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);

            if (dbIntegrityService.canDo(Access.READ, principalID, annotationID)) {
                final Annotation annotation = dbIntegrityService.getAnnotation(annotationID);
                return new ObjectFactory().createAnnotation(annotation);
            } else {
                verboseOutput.FORBIDDEN_ANNOTATION_READING(externalIdentifier, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return new ObjectFactory().createAnnotation(new Annotation());
            }

        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return new ObjectFactory().createAnnotation(new Annotation());
        }
    }

    //TODO: unit test
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/targets")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getAnnotationTargets(@PathParam("annotationid") String externalIdentifier) throws IOException {
        Number principalID = this.getPrincipalID();
        try {
            final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);

            if (dbIntegrityService.canDo(Access.READ, principalID, annotationID)) {
                final ReferenceList TargetList = dbIntegrityService.getAnnotationTargets(annotationID);
                return new ObjectFactory().createTargetList(TargetList);
            } else {
                verboseOutput.FORBIDDEN_ANNOTATION_READING(externalIdentifier, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return new ObjectFactory().createReferenceList(new ReferenceList());
            }

        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return new ObjectFactory().createReferenceList(new ReferenceList());
        }
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

        Number principalID = this.getPrincipalID();
        UUID ownerExternalUUID = (ownerExternalId != null) ? UUID.fromString(ownerExternalId) : null;
        if (access == null) {
            access = defaultAccess;
        }
        if (!Arrays.asList(admissibleAccess).contains(access)) {
            verboseOutput.INVALID_ACCESS_MODE(access);
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "ivalide mode acess " + access);
            return new ObjectFactory().createAnnotationInfoList(new AnnotationInfoList());
        }
        try {
            final AnnotationInfoList annotationInfoList = dbIntegrityService.getFilteredAnnotationInfos(ownerExternalUUID, link, text, principalID, access, namespace, after, before);
            return new ObjectFactory().createAnnotationInfoList(annotationInfoList);
        } catch (NotInDataBaseException e) {
            loggerServer.debug(e.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
            return new ObjectFactory().createAnnotationInfoList(new AnnotationInfoList());
        }
    }
// TODO Unit test    

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions")
    @Transactional(readOnly = true)
    public JAXBElement<PermissionList> getAnnotationPermissions(@PathParam("annotationid") String externalIdentifier) throws IOException {
        Number principalID = this.getPrincipalID();
        try {
            final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);
            if (dbIntegrityService.canDo(Access.READ, principalID, annotationID)) {
                final PermissionList permissionList = dbIntegrityService.getPermissions(annotationID, Resource.ANNOTATION);
                return new ObjectFactory().createPermissionList(permissionList);
            } else {
                verboseOutput.FORBIDDEN_ANNOTATION_READING(externalIdentifier, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return new ObjectFactory().createPermissionList(new PermissionList());
            }

        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return new ObjectFactory().createPermissionList(new PermissionList());
        }
    }
///////////////////////////////////////////////////////

    @DELETE
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public String deleteAnnotation(@PathParam("annotationid") String externalIdentifier) throws IOException {
        Number principalID = this.getPrincipalID();
        try {
            final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);
            if (principalID.equals(dbIntegrityService.getAnnotationOwnerID(annotationID)) || dbIntegrityService.getTypeOfPrincipalAccount(principalID).equals(admin)) {
                int[] result = dbIntegrityService.deleteAnnotation(annotationID);
                return result[0] + " annotation(s) deleted.";
            } else {
                verboseOutput.FORBIDDEN_ANNOTATION_DELETING(externalIdentifier, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return "Nothings is deleted.";
            }

        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return "Nothings is deleted.";
        }
    }

///////////////////////////////////////////////////////
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    public JAXBElement<ResponseBody> createAnnotation(Annotation annotation) throws IOException {
        Number principalID = this.getPrincipalID();
        try {
            Number annotationID = dbIntegrityService.addPrincipalsAnnotation(principalID, annotation);
            return new ObjectFactory().createResponseBody(dbIntegrityService.makeAnnotationResponseEnvelope(annotationID));
        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e2.toString());
            return new ObjectFactory().createResponseBody(new ResponseBody());
        }
    }
///////////////////////////////////////////////////////
// TODO: unit test

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<ResponseBody> updateAnnotation(@PathParam("annotationid") String externalIdentifier, Annotation annotation) throws IOException {
        Number principalID = this.getPrincipalID();
        String path = uriInfo.getBaseUri().toString();
        String annotationURI = annotation.getURI();
        if (!(path + "annotations/" + externalIdentifier).equals(annotationURI)) {
            verboseOutput.IDENTIFIER_MISMATCH(externalIdentifier);
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return new ObjectFactory().createResponseBody(new ResponseBody());
        }
        try {
            final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);
            try {
                if (principalID.equals(dbIntegrityService.getAnnotationOwnerID(annotationID)) || dbIntegrityService.getTypeOfPrincipalAccount(principalID).equals(admin)) {
                    int updatedRows = dbIntegrityService.updateAnnotation(annotation);
                    return new ObjectFactory().createResponseBody(dbIntegrityService.makeAnnotationResponseEnvelope(annotationID));
                } else {
                    verboseOutput.FORBIDDEN_ACCESS_CHANGING(externalIdentifier, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                    loggerServer.debug(" Access changing is the part of the full update of the annotation.");
                    httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return new ObjectFactory().createResponseBody(new ResponseBody());
                }
            } catch (NotInDataBaseException e) {
                loggerServer.debug(e.toString());
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                return new ObjectFactory().createResponseBody(new ResponseBody());
            }
        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return new ObjectFactory().createResponseBody(new ResponseBody());
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/body")
    public JAXBElement<ResponseBody> updateAnnotationBody(@PathParam("annotationid") String externalIdentifier, AnnotationBody annotationBody) throws IOException {
        Number principalID = this.getPrincipalID();
        try {
            final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.ANNOTATION);
            if (dbIntegrityService.canDo(Access.WRITE, principalID, annotationID)) {
                int updatedRows = dbIntegrityService.updateAnnotationBody(annotationID, annotationBody);
                return new ObjectFactory().createResponseBody(dbIntegrityService.makeAnnotationResponseEnvelope(annotationID));
            } else {
                verboseOutput.FORBIDDEN_ANNOTATION_BODY_WRITING(externalIdentifier, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return new ObjectFactory().createResponseBody(new ResponseBody());
            }
        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return new ObjectFactory().createResponseBody(new ResponseBody());
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/{principalid: " + BackendConstants.regExpIdentifier + "}")
    public String updateAccess(@PathParam("annotationid") String annotationExternalId,
            @PathParam("principalid") String principalExternalId, Access access) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        try {
            final Number principalID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(principalExternalId), Resource.PRINCIPAL);
            try {
                final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(annotationExternalId), Resource.ANNOTATION);
                if (remotePrincipalID.equals(dbIntegrityService.getAnnotationOwnerID(annotationID)) || dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
                    int result = dbIntegrityService.updateAnnotationPrincipalAccess(annotationID, principalID, access);
                    return result + " rows are updated/added";
                } else {
                    verboseOutput.FORBIDDEN_ACCESS_CHANGING(annotationExternalId, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                    httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return "Nothing is updated.";
                }

            } catch (NotInDataBaseException e1) {
                loggerServer.debug(e1.toString());
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.toString());
                return "Nothing is updated.";
            }
        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return "Nothing is updated.";
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/")
    public JAXBElement<ResponseBody> updatePermissions(@PathParam("annotationid") String annotationExternalId, PermissionList permissions) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        try {
            final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(annotationExternalId), Resource.ANNOTATION);
            try {
                if (remotePrincipalID.equals(dbIntegrityService.getAnnotationOwnerID(annotationID)) || dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
                    int updatedRows = dbIntegrityService.updatePermissions(annotationID, permissions);
                    return new ObjectFactory().createResponseBody(dbIntegrityService.makeAccessResponseEnvelope(annotationID, Resource.ANNOTATION));
                } else {
                    verboseOutput.FORBIDDEN_ACCESS_CHANGING(annotationExternalId, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                    httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return new ObjectFactory().createResponseBody(new ResponseBody());
                }
            } catch (NotInDataBaseException e) {
                loggerServer.debug(e.toString());
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                return new ObjectFactory().createResponseBody(new ResponseBody());
            }
        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());;
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return new ObjectFactory().createResponseBody(new ResponseBody());
        }
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{annotationId: " + BackendConstants.regExpIdentifier + "}/principal/{principalId}/delete")
    public String deletePrincipalsAccess(@PathParam("annotationId") String annotationId,
            @PathParam("principalId") String principalId) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        int deletedRows = 0;
        try {
            final Number annotationID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(annotationId), Resource.ANNOTATION);
            try {
                if (remotePrincipalID.equals(dbIntegrityService.getAnnotationOwnerID(annotationID)) || dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
                    Number principalID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(principalId), Resource.PRINCIPAL);
                    deletedRows = dbIntegrityService.updateAnnotationPrincipalAccess(annotationID, principalID, null);
                    return (deletedRows + " is deleted.");
                } else {
                    verboseOutput.FORBIDDEN_ACCESS_CHANGING(annotationId, dbIntegrityService.getAnnotationOwner(annotationID).getDisplayName(), dbIntegrityService.getAnnotationOwner(annotationID).getEMail());
                    httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return "Nothing is deleted.";
                }
            } catch (NotInDataBaseException e) {
                loggerServer.debug(e.toString());
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                return "Nothing is deleted.";
            }
        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return "Nothing is deleted.";
        }
    }
}
