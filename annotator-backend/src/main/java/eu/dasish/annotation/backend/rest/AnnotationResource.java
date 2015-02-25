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
import eu.dasish.annotation.backend.ForbiddenException;
import eu.dasish.annotation.backend.MatchMode;
import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.backend.dao.ILambda;
import eu.dasish.annotation.schema.Annotation;
import eu.dasish.annotation.schema.AnnotationBody;
import eu.dasish.annotation.schema.AnnotationInfoList;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.AnnotationInfo;
import eu.dasish.annotation.schema.PermissionList;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResponseBody;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBElement;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * A REST class for GETting, POSTing, PUTting and DELETing annotations or their substructures (child elements).
 * Every REST method in the case of successful completion produces the object of the declared output type
 * (a JAXB-element or a message string) or sends a HTTP-error with the corresponding diagnostics otherwise.
 * @author olhsha
 */
@Component
@Path("/annotations")
@Transactional(rollbackFor = {Exception.class})
public class AnnotationResource extends ResourceResource {
    
    final private String defaultMatchMode = "exact";
    final private String[] admissibleMatchModes = {"exact", "starts_with", "ends_with", "contains"};

 
    /**
     * 
     * @param httpServletRequest a {@link HttpServletRequest} object; set explicitely in the unit tests.
     */
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * 
     * @param providers a {@link Providers} object, used by JAXB, also 
     * to validate input and output xml files w.r.t. the dasish schema.
     */
    public void setProviders(Providers providers) {
        this.providers = providers;
    }

    public AnnotationResource() {
    }
    

    /**
     * 
     * @param externalIdentifier the UUID of an annotation.
     * @return the xml-element representing the annotation with "externalIdentifier" built up 
     * from the "annotation" table and the corresponding junction tables. 
     * @throws IOException if sending an error fails.
     */
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    @Transactional(readOnly = true)
    public JAXBElement<Annotation> getAnnotation(@PathParam("annotationid") String externalIdentifier) throws IOException {
        Map params = new HashMap();
        try {
            Annotation result = (Annotation) (new RequestWrappers(this)).wrapRequestResource(params, new GetAnnotation(), Resource.ANNOTATION, Access.READ, externalIdentifier);
            if (result != null) {
                return (new ObjectFactory()).createAnnotation(result);
            } else {
                return (new ObjectFactory()).createAnnotation(new Annotation());
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return (new ObjectFactory()).createAnnotation(new Annotation());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return (new ObjectFactory()).createAnnotation(new Annotation());
        }
    }

    private class GetAnnotation implements ILambda<Map, Annotation> {

        @Override
        public Annotation apply(Map params) throws NotInDataBaseException {
            return dbDispatcher.getAnnotation((Number) params.get("internalID"));
        }
    }

    /**
     * 
     * @param externalIdentifier the string representing the UUID of an annotation.
     * @return the xml element representing the list of h-references of the targets of the annotation with "externalIdentifier".
     * @throws IOException if sending an error fails.
     */
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/targets")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getAnnotationTargets(@PathParam("annotationid") String externalIdentifier) throws IOException {
        Map params = new HashMap();
        try {
            ReferenceList result = (ReferenceList) (new RequestWrappers(this)).wrapRequestResource(params, new GetTargetList(), Resource.ANNOTATION, Access.READ, externalIdentifier);
            if (result != null) {
                return (new ObjectFactory()).createTargetList(result);
            } else {
                return (new ObjectFactory()).createTargetList(new ReferenceList());
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return (new ObjectFactory()).createReferenceList(new ReferenceList());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return (new ObjectFactory()).createReferenceList(new ReferenceList());
        }
    }

    private class GetTargetList implements ILambda<Map, ReferenceList> {

        @Override
        public ReferenceList apply(Map params) throws NotInDataBaseException {
            return dbDispatcher.getAnnotationTargets((Number) params.get("internalID"));
        }
    }

    /**
     * 
     * @param link the link representing one of the target sources of the annotation.
     * @param matchMode the relation of the actual target-source link to the "link" parameter: "exact", "starts_with", "ends_ith", "contains".
     * @param text the text fragment that must be present in the annotation body.
     * @param access the access mode of the logged in user to the requested annotations.
     * @param namespace not implemented.
     * @param ownerExternalId the external UUID of the owner of requested annotations.
     * @param after the minimal creation/update date of the annotation.
     * @param before the maximal creation/update date of the annotation.
     * @return the xml-element representing the list of {@link AnnotationInfo} objects representing 
     * the annotations satisfying the requirements defined by the parameters.
     * @throws IOException if sending an error fails.
     */
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("")
    @Transactional(readOnly = true)
    public JAXBElement<AnnotationInfoList> getFilteredAnnotations(@QueryParam("link") String link,
            @QueryParam("matchMode") String matchMode,
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
        
         if (matchMode == null) {
            matchMode = defaultMatchMode;
        }
        if (!Arrays.asList(admissibleMatchModes).contains(matchMode)) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "ivalide match mode " + matchMode);
            return new ObjectFactory().createAnnotationInfoList(new AnnotationInfoList());
        }

        UUID ownerExternalUUID = (ownerExternalId != null) ? UUID.fromString(ownerExternalId) : null;
        
        
        if (access == null) {
            access = defaultAccess;
        }
        if (!Arrays.asList(admissibleAccess).contains(access)) {
            this.INVALID_ACCESS_MODE(access);
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "ivalide mode acess " + access);
            return new ObjectFactory().createAnnotationInfoList(new AnnotationInfoList());
        }
        
        
        try {
            final AnnotationInfoList annotationInfoList = dbDispatcher.getFilteredAnnotationInfos(ownerExternalUUID, link, MatchMode.valueOf(matchMode.toUpperCase()), text, principalID, access, namespace, after, before);
            return new ObjectFactory().createAnnotationInfoList(annotationInfoList);
        } catch (NotInDataBaseException e) {
            loggerServer.debug(e.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
            return new ObjectFactory().createAnnotationInfoList(new AnnotationInfoList());
        }
    }
    
    /**
     * The request can be sent by principals with "developer" or "admin" account.
     * @param n # of threads.
     * @return a message reporting test success.
     * @throws IOException if sending an error fails.
     * @throws NotInDataBaseException if getting annotation fails.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("stressTest")
    @Transactional(readOnly = true)
    public String getAnnotationsMultithread(@QueryParam("n") int n) throws IOException, NotInDataBaseException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return "You are not logged in";
        }
        String typeOfAccount = dbDispatcher.getTypeOfPrincipalAccount(remotePrincipalID);
        if (typeOfAccount.equals(admin) || typeOfAccount.equals(DebugResource.developer)) {
            
            System.out.print("Preparing the data: getting the list of all annotations, picking up "+n+" of them randomly, and initializing threads");            
            final List<Number> annotationIDs = dbDispatcher.getFilteredAnnotationIDs(null, null, null, null, remotePrincipalID, "read", null, null, null);
            final int size = annotationIDs.size();
            List<GetThread> threads = new ArrayList<GetThread>(n);
            Random rand = new Random();
            for (int i=0; i<n; i++) {
               int r = rand.nextInt(size);
               String annotationExternalId = dbDispatcher.getResourceExternalIdentifier(annotationIDs.get(r), Resource.ANNOTATION).toString();
               GetThread thread = new GetThread(this, annotationExternalId);
               threads.add(thread);            
            }
                        
            System.out.print("Running on getAnnotation(id) (no serialized output is shown to save time) on randomly selected annotation ids."); 
            for (int i=0; i<n; i++) {
               threads.get(i).run();            
            }
            
            return "Stress-tested annotationrResource's getAnnotation(xxx): ok";
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return "You cannot enjoy this priviledged service because youe are neither admin nor developer. Ask the admin for more priviledges";
        }
    }
    
    

   /**
    * 
    * @param externalIdentifier the external UUID of an annotation.
    * @return the xml-element representing the list of permissions, i.e. pairs (principalId, accessMode),
    * built upon the table "annotations_principals_accesses".
    * @throws IOException if sending an error fails.
    */
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions")
    @Transactional(readOnly = true)
    public JAXBElement<PermissionList> getAnnotationPermissions(@PathParam("annotationid") String externalIdentifier) throws IOException {
        Map params = new HashMap();
        try {
            PermissionList result = (PermissionList) (new RequestWrappers(this)).wrapRequestResource(params, new GetPermissionList(), Resource.ANNOTATION, Access.READ, externalIdentifier);
            if (result != null) {
                return (new ObjectFactory()).createPermissionList(result);
            } else {
                return (new ObjectFactory()).createPermissionList(new PermissionList());
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return (new ObjectFactory()).createPermissionList(new PermissionList());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return (new ObjectFactory()).createPermissionList(new PermissionList());
        }
    }

    private class GetPermissionList implements ILambda<Map, PermissionList> {

        @Override
        public PermissionList apply(Map params) throws NotInDataBaseException {
            return dbDispatcher.getPermissions((Number) params.get("internalID"), (Resource) params.get("resourceType"));
        }
    }

    /**
     * 
     * @param externalIdentifier the external UUID of the annotation to be deleted.
     * @return the message telling if the annotation is deleted or not.
     * @throws IOException if sending an error fails.
     */
    @DELETE
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public String deleteAnnotation(@PathParam("annotationid") String externalIdentifier) throws IOException {
        Map params = new HashMap();
        try {
            int[] result = (int[]) (new RequestWrappers(this)).wrapRequestResource(params, new DeleteAnnotation(), Resource.ANNOTATION, Access.ALL, externalIdentifier);
            if (result != null) {
                return result[0] + " annotation(s) is(are) deleted.";
            } else {
                return "Nothing is deleted.";
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return e1.getMessage();
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return e2.getMessage();
        }
    }

    private class DeleteAnnotation implements ILambda<Map, int[]> {

        @Override
        public int[] apply(Map params) throws NotInDataBaseException {
            return dbDispatcher.deleteAnnotation((Number) params.get("internalID"));
        }
    }

    /**
     * 
     * @param annotation an {@link Annotation} object.
     * @return the {@link ResponseBody} element that contains the xml element representing 
     * the fresh annotation (with its freshly generated by the back-end external UUID), and the list
     * of action-elements representing the actions the client should care for,
     * e.g. add a cached representation for a certain target.
     * @throws IOException if sending an error fails.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    public JAXBElement<ResponseBody> createAnnotation(Annotation annotation) throws IOException {

        Map params = new HashMap();
        params.put("annotation", annotation);
        try {
            ResponseBody result = (ResponseBody) (new RequestWrappers(this)).wrapRequestResource(params, new AddAnnotation());
            if (result != null) {
                return (new ObjectFactory()).createResponseBody(result);
            } else {
                return (new ObjectFactory()).createResponseBody(new ResponseBody());
            }
        } catch (NotInDataBaseException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return (new ObjectFactory()).createResponseBody(new ResponseBody());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return (new ObjectFactory()).createResponseBody(new ResponseBody());
        }
    }

    private class AddAnnotation implements ILambda<Map, ResponseBody> {

        @Override
        public ResponseBody apply(Map params) throws NotInDataBaseException {
            Number principalID = (Number) params.get("principalID");
            Annotation annotation = (Annotation) params.get("annotation");
            Number annotationID = dbDispatcher.addPrincipalsAnnotation(principalID, annotation);
            return dbDispatcher.makeAnnotationResponseEnvelope(annotationID);
        }
    }

   /**
    * 
    * @param externalId the external UUID of the annotation to be updated.
    * @param annotation the {@link Annotation} object representing the new annotation that should replace the annotation with "externalId".
    * @return the {@link ResponseBody} element that contains the xml element representing 
     * the updated annotation, and the list of action-elements representing the actions the client should care for,
     * e.g. add a cached representation for a certain target.
     * @throws IOException if sending an error fails. 
    */
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<ResponseBody> updateAnnotation(@PathParam("annotationid") String externalId, Annotation annotation) throws IOException {

        String annotationExtId = annotation.getId();
        if (!(externalId).equals(annotationExtId)) {
            loggerServer.debug("Wrong request: the annotation identifier   " + externalId + " and the annotation (notebook) ID from the request body do not match.");
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        try {
            Map params = new HashMap();            
            params.put("annotation", annotation);
            params.put("remoteUser",httpServletRequest.getRemoteUser()); 
            ResponseBody result = (ResponseBody) (new RequestWrappers(this)).wrapRequestResource(params, new UpdateAnnotation(), Resource.ANNOTATION, Access.WRITE, externalId);
            if (result != null) {
                return (new ObjectFactory()).createResponseBody(result);
            }
            else {
                return (new ObjectFactory()).createResponseBody(new ResponseBody());
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return (new ObjectFactory()).createResponseBody(new ResponseBody());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return (new ObjectFactory()).createResponseBody(new ResponseBody());
        }
    }

    
    private class UpdateAnnotation implements ILambda<Map, ResponseBody> {

        @Override 
        public ResponseBody apply(Map params) throws NotInDataBaseException, ForbiddenException {
            Annotation annotation = (Annotation) params.get("annotation");
            Number annotationID = (Number) params.get("internalID");
            String remoteUser = (String) params.get("remoteUser");
            int updatedRows = dbDispatcher.updateAnnotation(annotation, remoteUser);
            return dbDispatcher.makeAnnotationResponseEnvelope(annotationID);
        }
    }
    
   /**
    * 
    * @param externalIdentifier the external UUID of the annotation whose body must be updated.
    * @param annotationBody an {@link AnnotationBody} object representation the new body of the annotation,
    * which should replace the old body.
    * @return the {@link ResponseBody} element that contains the xml element representing 
    * the updated annotation, and the list of action-elements representing the actions the client should care for.
    * @throws IOException if sending an error fails.  
    */
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/body")
    public JAXBElement<ResponseBody> updateAnnotationBody(@PathParam("annotationid") String externalIdentifier, AnnotationBody annotationBody) throws IOException {
        Map params = new HashMap();
        params.put("annotationBody", annotationBody);
        try {
            ResponseBody result = (ResponseBody) (new RequestWrappers(this)).wrapRequestResource(params, new UpdateAnnotationBody(), Resource.ANNOTATION, Access.WRITE, externalIdentifier);
            if (result != null) {
                return (new ObjectFactory()).createResponseBody(result);
            } else {
                return (new ObjectFactory()).createResponseBody(new ResponseBody());
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return (new ObjectFactory()).createResponseBody(new ResponseBody());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return (new ObjectFactory()).createResponseBody(new ResponseBody());
        }

    }

    ///////////////////////////////////////////////////////////
    private class UpdateAnnotationBody implements ILambda<Map, ResponseBody> {

        @Override
        public ResponseBody apply(Map params) throws NotInDataBaseException {
            Number resourceID = (Number) params.get("internalID");
            AnnotationBody annotationBody = (AnnotationBody) params.get("annotationBody");
            int updatedRows = dbDispatcher.updateAnnotationBody(resourceID, annotationBody);
            return dbDispatcher.makeAnnotationResponseEnvelope(resourceID);
        }
    }

    /**
     * 
     * @param externalIdentifier the external UUID of the annotation whose headline is to be updated.
     * @param newHeadline a new headline.
     * @return the {@link ResponseBody} element that contains the xml element representing 
     * the updated annotation, and the list of action-elements representing the actions the client should care for.
     * @throws IOException if sending an error fails.  
     */
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/headline")
    public JAXBElement<ResponseBody> updateAnnotationHeadline(@PathParam("annotationid") String externalIdentifier, String newHeadline) throws IOException {
        Map params = new HashMap();
        params.put("headline", newHeadline);
        try {
            ResponseBody result = (ResponseBody) (new RequestWrappers(this)).wrapRequestResource(params, new UpdateAnnotationHeadline(), Resource.ANNOTATION, Access.WRITE, externalIdentifier);
            if (result != null) {
                return (new ObjectFactory()).createResponseBody(result);
            } else {
                return (new ObjectFactory()).createResponseBody(new ResponseBody());
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return (new ObjectFactory()).createResponseBody(new ResponseBody());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return (new ObjectFactory()).createResponseBody(new ResponseBody());
        }

    }

    ///////////////////////////////////////////////////////////
    private class UpdateAnnotationHeadline implements ILambda<Map, ResponseBody> {

        @Override
        public ResponseBody apply(Map params) throws NotInDataBaseException {
            Number resourceID = (Number) params.get("internalID");
            String newHeadline = (String) params.get("headline");
            int updatedRows = dbDispatcher.updateAnnotationHeadline(resourceID, newHeadline);
            return dbDispatcher.makeAnnotationResponseEnvelope(resourceID);
        }
    }

   /**
    * 
    * @param annotationDatabaseId the internal database Id of the annotation to be updated.
    * @param annotationHeadline the annotation's headline.
    * @param access the new value for the public attribute of the annotation.
    * @return the message telling if the database has been updated and how many rows have been updated;
    * if "annotationDatabaseId" == null or empty then all the annotations with "annotationHeadline"
    * must be updated.
    * @throws IOException if sending an error fails.
    */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("publicaccessform")
    public String updatePubliAccessFromForm(
            @FormParam("annotationId") String annotationDatabaseId,
            @FormParam("annotationHeadline") String annotationHeadline,
            @FormParam("access") String access)
            throws IOException {

        if (access.trim().equals("")) {
            access = "none";
        }

        try {

            Access accessTyped = Access.fromValue(access);
            if (annotationDatabaseId == null || annotationDatabaseId.trim().equals("")) {
                List<UUID> annotationIds = dbDispatcher.getAnnotationExternalIdsFromHeadline(annotationHeadline);
                if (annotationIds == null || annotationIds.isEmpty()) {
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "No annotations with this headline have been found");
                    return "No annotations with this headline have been found";
                };
                int updatedAnnotations = 0;            
                for (UUID annotationId : annotationIds) {
                    Map params = new HashMap();
                    params.put("access", accessTyped);
                    Integer result = (Integer) (new RequestWrappers(this)).wrapRequestResource(params, new UpdatePublicAccess(), Resource.ANNOTATION, Access.ALL, annotationId.toString());
                    updatedAnnotations = (result != null) ? updatedAnnotations + result.intValue() : updatedAnnotations;
                }
                return (updatedAnnotations + " row(s) are updated");
            } else {
                Map params = new HashMap();
                params.put("access", accessTyped);
                Integer result = (Integer) (new RequestWrappers(this)).wrapRequestResource(params, new UpdatePublicAccess(), Resource.ANNOTATION, Access.ALL, annotationDatabaseId);
                if (result != null) {
                    return result + " row(s) is(are) updated.";
                } else {
                    return "0 rows are updated.";
                }
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.toString());
            return "0 rows are updated.";
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.toString());
            return "0 rows are updated.";
        }
    }

    private class UpdatePublicAccess implements ILambda<Map, Integer> {

        @Override
        public Integer apply(Map params) throws NotInDataBaseException {
            Number annotationID = (Number) params.get("internalID");
            Access access = (Access) params.get("access");
            return dbDispatcher.updatePublicAttribute(annotationID, access);
        }
    }

    /**
     * 
     * @param annotationExternalId the external UUID of an annotation.
     * @param principalExternalId the external UUID of a principal whose access mode to the annotation must be updated.
     * @param access the access mode that should be assigned to the principal.
     * @return the message about the amount of updated rows.
     * @throws IOException if sending an error fails.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/{principalid: " + BackendConstants.regExpIdentifier + "}")
    public String updatePermission(@PathParam("annotationid") String annotationExternalId,
            @PathParam("principalid") String principalExternalId, Access access) throws IOException {
        try {
            return this.genericUpdateDeletePermission(annotationExternalId, principalExternalId, access);
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return e1.getMessage();
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return e2.getMessage();
        }
    }

    /**
     * One of 3 principal-related parameters must be non-null and non-empty; one of 2 annotation-related parameters must be non-null and non-empty.
     * @param remoteID the remote ID of the principal whose access mode to the annotation (see below) must be updated.
     * @param fullName the full name of the principal whose access mode to the annotation must be updated.
     * @param principalDatabaseId the internal database identifier of the principal whose access mode to the annotation must be updated. 
     * @param annotationDatabaseId the internal database id of the annotation for which the access mode must be updated.
     * @param annotationHeadline the headline of the annotation for which the access mode must be updated.
     * @param access the new access mode.
     * @return the message explaining how many rows have been updated. 
     * @throws IOException  if sending an error fails.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("permissionform")
    public String updatePermissionFromForm(@FormParam("login") String remoteID,
            @FormParam("fullName") String fullName,
            @FormParam("userId") String principalDatabaseId,
            @FormParam("annotationId") String annotationDatabaseId,
            @FormParam("annotationHeadline") String annotationHeadline,
            @FormParam("access") String access)
            throws IOException {

        try {

            if (access.trim().equals("")) {
                access = null;
            }

            if (principalDatabaseId == null || principalDatabaseId.trim().equals("")) {
                if (remoteID != null && !remoteID.trim().equals("")) {
                    principalDatabaseId = dbDispatcher.getPrincipalExternalIDFromRemoteID(remoteID).toString();
                } else {
                    if (fullName != null) {
                        principalDatabaseId = dbDispatcher.getPrincipalExternalIdFromName(fullName).toString();
                    } else {
                        httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "No user information is given");
                    }
                }
            }

            if (annotationDatabaseId == null || annotationDatabaseId.trim().equals("")) {
                List<UUID> annotationIds = dbDispatcher.getAnnotationExternalIdsFromHeadline(annotationHeadline);
                if (annotationIds == null || annotationIds.isEmpty()) {
                    return "No annotations with this headline found";
                };
                int count = 0;
                String tmp = null;
                for (UUID annotationId : annotationIds) {
                    tmp = this.genericUpdateDeletePermission(annotationId.toString(), principalDatabaseId, Access.fromValue(access));
                    if (!tmp.startsWith("0")) {
                        count++;
                    }
                }
                return (count + " row(s) are updated");
            } else {
                return this.genericUpdateDeletePermission(annotationDatabaseId, principalDatabaseId, Access.fromValue(access));
            }

        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return e1.getMessage();
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return e2.getMessage();
        }
    }

    ////////////////////////////////////////////
    private String genericUpdateDeletePermission(String annotationId, String principalId, Access access) throws IOException, NotInDataBaseException, ForbiddenException {
        Map params = new HashMap();
        params.put("access", access);
        final Number inputPrincipalID = dbDispatcher.getResourceInternalIdentifier(UUID.fromString(principalId), Resource.PRINCIPAL);
        params.put("inputPrincipalID", inputPrincipalID);
        Integer result = (Integer) (new RequestWrappers(this)).wrapRequestResource(params, new UpdatePermissionHelper(), Resource.ANNOTATION, Access.ALL, annotationId);
        if (result != null) {
            return result + " row(s) is(are) updated.";
        } else {
            return "0 rows are updated.";
        }
    }

    private class UpdatePermissionHelper implements ILambda<Map, Integer> {

        @Override
        public Integer apply(Map params) throws NotInDataBaseException {
            Number annotationID = (Number) params.get("internalID");
            Number principalID = (Number) params.get("inputPrincipalID");
            Access access = (Access) params.get("access");
            return dbDispatcher.updatePermission(annotationID, principalID, access);
        }
    }
    
    
    /**
     * 
     * @param annotationExternalId the external UUID of an annotation.
     * @param permissions a {@link PermissionList} object representing a list of pairs (principal UUID, access mode) of the
     * new list of permissions for the annotation.
     * @return the {@link ResponseBody} element that contains the xml element representing 
     * the updated annotation, and the list of action-elements representing the actions the client should care for,
     * e.g. add the e-mail of a certain principal.
     * @throws IOException if sending an error fails.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{annotationid: " + BackendConstants.regExpIdentifier + "}/permissions/")
    public JAXBElement<ResponseBody> updatePermissions(@PathParam("annotationid") String annotationExternalId, PermissionList permissions) throws IOException {

        Map params = new HashMap();
        params.put("permissions", permissions);
        try {
            ResponseBody result = (ResponseBody) (new RequestWrappers(this)).wrapRequestResource(params, new UpdatePermissions(), Resource.ANNOTATION, Access.ALL, annotationExternalId);
            if (result != null) {
                return new ObjectFactory().createResponseBody(result);
            } else {
                return new ObjectFactory().createResponseBody(new ResponseBody());
            }
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return new ObjectFactory().createResponseBody(new ResponseBody());
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return new ObjectFactory().createResponseBody(new ResponseBody());
        }

    }

    ////////////////////////////////////////////
    private class UpdatePermissions implements ILambda<Map, ResponseBody> {

        @Override
        public ResponseBody apply(Map params) throws NotInDataBaseException {
            Number annotationID = (Number) params.get("internalID");
            PermissionList permissions = (PermissionList) params.get("permissions");
            int updatedRows = dbDispatcher.updateOrAddPermissions(annotationID, permissions);
            return dbDispatcher.makeAccessResponseEnvelope(annotationID, Resource.ANNOTATION);
        }
    }

    /**
     * 
     * @param annotationId the external UUID of the annotation.
     * @param principalId the external UUID of a principal whose access mode must be deleted.
     * @return the amount of removed rows in the database.
     * @throws IOException if sending an error fails.
     */
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{annotationId: " + BackendConstants.regExpIdentifier + "}/principal/{principalId}/delete")
    public String deletePermission(@PathParam("annotationId") String annotationId,
            @PathParam("principalId") String principalId) throws IOException {
        try {
            return this.genericUpdateDeletePermission(annotationId, principalId, null);
        } catch (NotInDataBaseException e1) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e1.getMessage());
            return e1.getMessage();
        } catch (ForbiddenException e2) {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e2.getMessage());
            return e2.getMessage();
        }
    }
    
    
}
