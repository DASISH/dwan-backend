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
import eu.dasish.annotation.backend.ResourceAction;
import eu.dasish.annotation.backend.dao.ILambda;
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
import java.util.HashMap;
import java.util.Map;
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
        Map params = new HashMap();
        Annotation result = (Annotation) (new RequestWrappers(this)).wrapRequestResource(params, new GetAnnotation(), Resource.ANNOTATION, ResourceAction.READ, externalIdentifier, false);
        if (result != null) {
            return (new ObjectFactory()).createAnnotation(result);
        } else {
            return (new ObjectFactory()).createAnnotation(new Annotation());
        }
    }

    private class GetAnnotation implements ILambda<Map, Annotation> {

        @Override
        public Annotation apply(Map params) throws NotInDataBaseException {
            return dbIntegrityService.getAnnotation((Number) params.get("internalID"));
        }
    }

    //////////////////////////////////////////////////
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/targets")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getAnnotationTargets(@PathParam("annotationid") String externalIdentifier) throws IOException {
        Map params = new HashMap();
        ReferenceList result = (ReferenceList) (new RequestWrappers(this)).wrapRequestResource(params, new GetTargetList(), Resource.ANNOTATION, ResourceAction.READ, externalIdentifier, false);
        if (result != null) {
            return (new ObjectFactory()).createTargetList(result);
        } else {
            return (new ObjectFactory()).createTargetList(new ReferenceList());
        }
    }

    private class GetTargetList implements ILambda<Map, ReferenceList> {

        @Override
        public ReferenceList apply(Map params) throws NotInDataBaseException {
            return dbIntegrityService.getAnnotationTargets((Number) params.get("internalID"));
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
        if (principalID == null) {
            return new ObjectFactory().createAnnotationInfoList(new AnnotationInfoList());
        }

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
        Map params = new HashMap();
        PermissionList result = (PermissionList) (new RequestWrappers(this)).wrapRequestResource(params, new GetPermissionList(), Resource.ANNOTATION, ResourceAction.READ, externalIdentifier, false);
        if (result != null) {
            return (new ObjectFactory()).createPermissionList(result);
        } else {
            return (new ObjectFactory()).createPermissionList(new PermissionList());
        }
    }

    private class GetPermissionList implements ILambda<Map, PermissionList> {

        @Override
        public PermissionList apply(Map params) throws NotInDataBaseException {
            return dbIntegrityService.getPermissions((Number) params.get("internalID"), (Resource) params.get("resourceType"));
        }
    }
///////////////////////////////////////////////////////

    @DELETE
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public String deleteAnnotation(@PathParam("annotationid") String externalIdentifier) throws IOException {
        Map params = new HashMap();
        int[] result = (int[]) (new RequestWrappers(this)).wrapRequestResource(params, new DeleteAnnotation(), Resource.ANNOTATION, ResourceAction.DELETE, externalIdentifier, false);
        if (result != null) {
            return result[0] + " annotation(s) is(are) deleted.";
        } else {
            return "Nothing is deleted.";
        }
    }

    private class DeleteAnnotation implements ILambda<Map, int[]> {

        @Override
        public int[] apply(Map params) throws NotInDataBaseException {
            return dbIntegrityService.deleteAnnotation((Number) params.get("internalID"));
        }
    }

///////////////////////////////////////////////////////
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    public JAXBElement<ResponseBody> createAnnotation(Annotation annotation) throws IOException {
        Map params = new HashMap();
        params.put("annotation", annotation);
        ResponseBody result = (ResponseBody) (new RequestWrappers(this)).wrapRequestResource(params, new AddAnnotation());
        if (result != null) {
            return (new ObjectFactory()).createResponseBody(result);
        } else {
            return (new ObjectFactory()).createResponseBody(new ResponseBody());
        }
    }

    private class AddAnnotation implements ILambda<Map, ResponseBody> {

        @Override
        public ResponseBody apply(Map params) throws NotInDataBaseException {
            Number principalID = (Number) params.get("principalID");
            Annotation annotation = (Annotation) params.get("annotation");
            Number annotationID = dbIntegrityService.addPrincipalsAnnotation(principalID, annotation);
            return dbIntegrityService.makeAnnotationResponseEnvelope(annotationID);
        }
    }

///////////////////////////////////////////////////////
// TODO: unit test
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<ResponseBody> updateAnnotation(@PathParam("annotationid") String externalId, Annotation annotation) throws IOException {

        String annotationURI = annotation.getURI();
        String path = uriInfo.getBaseUri().toString();
        if (!(path + "annotations/" + externalId).equals(annotationURI)) {
            loggerServer.debug("Wrong request: the annotation (notebook) identifier   " + externalId + " and the annotation (notebook) ID from the request body do not match.");
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        Map params = new HashMap();
        params.put("annotation", annotation);
        ResponseBody result = (ResponseBody) (new RequestWrappers(this)).wrapRequestResource(params, new UpdateAnnotation(), Resource.ANNOTATION, ResourceAction.WRITE_W_METAINFO, externalId, false);
        if (result != null) {
            return (new ObjectFactory()).createResponseBody(result);
        } else {
            return (new ObjectFactory()).createResponseBody(new ResponseBody());
        }


    }

    ///////////////////////////////////////////////////////////
    private class UpdateAnnotation implements ILambda<Map, ResponseBody> {

        @Override
        public ResponseBody apply(Map params) throws NotInDataBaseException {
            Annotation annotation = (Annotation) params.get("annotation");
            Number annotationID = (Number) params.get("internalID");
            int updatedRows = dbIntegrityService.updateAnnotation(annotation);
            return dbIntegrityService.makeAnnotationResponseEnvelope(annotationID);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/body")
    public JAXBElement<ResponseBody> updateAnnotationBody(@PathParam("annotationid") String externalIdentifier, AnnotationBody annotationBody) throws IOException {
        Map params = new HashMap();
        params.put("annotationBody", annotationBody);
        ResponseBody result = (ResponseBody) (new RequestWrappers(this)).wrapRequestResource(params, new UpdateAnnotationBody(), Resource.ANNOTATION, ResourceAction.WRITE, externalIdentifier, false);
        if (result != null) {
            return (new ObjectFactory()).createResponseBody(result);
        } else {
            return (new ObjectFactory()).createResponseBody(new ResponseBody());
        }

    }

    ///////////////////////////////////////////////////////////
    private class UpdateAnnotationBody implements ILambda<Map, ResponseBody> {

        @Override
        public ResponseBody apply(Map params) throws NotInDataBaseException {
            Number resourceID = (Number) params.get("internalID");
            AnnotationBody annotationBody = (AnnotationBody) params.get("annotationBody");
            int updatedRows = dbIntegrityService.updateAnnotationBody(resourceID, annotationBody);
            return dbIntegrityService.makeAnnotationResponseEnvelope(resourceID);
        }
    }
    
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/headline")
    public JAXBElement<ResponseBody> updateAnnotationHeadline(@PathParam("annotationid") String externalIdentifier, String newHeadline) throws IOException {
        Map params = new HashMap();
        params.put("headline", newHeadline);
        ResponseBody result = (ResponseBody) (new RequestWrappers(this)).wrapRequestResource(params, new UpdateAnnotationHeadline(), Resource.ANNOTATION, ResourceAction.WRITE, externalIdentifier, false);
        if (result != null) {
            return (new ObjectFactory()).createResponseBody(result);
        } else {
            return (new ObjectFactory()).createResponseBody(new ResponseBody());
        }

    }

    ///////////////////////////////////////////////////////////
    private class UpdateAnnotationHeadline implements ILambda<Map, ResponseBody> {

        @Override
        public ResponseBody apply(Map params) throws NotInDataBaseException {
            Number resourceID = (Number) params.get("internalID");
            String newHeadline = (String) params.get("headline");
            int updatedRows = dbIntegrityService.updateAnnotationHeadline(resourceID, newHeadline);
            return dbIntegrityService.makeAnnotationResponseEnvelope(resourceID);
        }
    }
    ////////////////////////////////////////////////////

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/{principalid: " + BackendConstants.regExpIdentifier + "}")
    public String updateAccess(@PathParam("annotationid") String annotationExternalId,
            @PathParam("principalid") String principalExternalId, Access access) throws IOException {
        return genericUpdateDeleteAccess(annotationExternalId, principalExternalId, access);
    }
    ////////////////////////////////////////////

    private String genericUpdateDeleteAccess(String annotationId, String principalId, Access access) throws IOException {
        Map params = new HashMap();
        params.put("access", access);
        try {
            final Number inputPrincipalID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(principalId), Resource.PRINCIPAL);
            params.put("inputPrincipalID", inputPrincipalID);
            Integer result = (Integer) (new RequestWrappers(this)).wrapRequestResource(params, new UpdatePrincipalAccess(), Resource.ANNOTATION, ResourceAction.WRITE_W_METAINFO, annotationId, false);
            if (result != null) {
                return result + " row(s) is(are) updated.";
            } else {
                return "Nothing is updated.";
            }

        } catch (NotInDataBaseException e2) {
            loggerServer.debug(e2.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e2.toString());
            return "Nothing is deleted.";
        }
    }

    private class UpdatePrincipalAccess implements ILambda<Map, Integer> {

        @Override
        public Integer apply(Map params) throws NotInDataBaseException {
            Number annotationID = (Number) params.get("internalID");
            Number principalID = (Number) params.get("inputPrincipalID");
            Access access = (Access) params.get("access");
            return dbIntegrityService.updateAnnotationPrincipalAccess(annotationID, principalID, access);
        }
    }
    ///////////////////////////////////////////

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/")
    public JAXBElement<ResponseBody> updatePermissions(@PathParam("annotationid") String annotationExternalId, PermissionList permissions) throws IOException {

        Map params = new HashMap();
        params.put("permissions", permissions);

        ResponseBody result = (ResponseBody) (new RequestWrappers(this)).wrapRequestResource(params, new UpdatePermissions(), Resource.ANNOTATION, ResourceAction.WRITE_W_METAINFO, annotationExternalId, false);
        if (result != null) {
            return new ObjectFactory().createResponseBody(result);
        } else {
            return new ObjectFactory().createResponseBody(new ResponseBody());
        }


    }

    ////////////////////////////////////////////
    private class UpdatePermissions implements ILambda<Map, ResponseBody> {

        @Override
        public ResponseBody apply(Map params) throws NotInDataBaseException {
            Number annotationID = (Number) params.get("internalID");
            PermissionList permissions = (PermissionList) params.get("permissions");
            int updatedRows = dbIntegrityService.updatePermissions(annotationID, permissions);
            return dbIntegrityService.makeAccessResponseEnvelope(annotationID, Resource.ANNOTATION);
        }
    }

    ////////////////////////////////////////////////////////////
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{annotationId: " + BackendConstants.regExpIdentifier + "}/principal/{principalId}/delete")
    public String deletePrincipalsAccess(@PathParam("annotationId") String annotationId,
            @PathParam("principalId") String principalId) throws IOException {
        return genericUpdateDeleteAccess(annotationId, principalId, null);
    }
}
