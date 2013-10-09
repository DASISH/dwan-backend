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

import eu.dasish.annotation.backend.dao.DBIntegrityService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created on : Jun 11, 2013, 5:10:55 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@Component
@Path("/notebooks")
public class NotebookResource {

    @Autowired
    private DBIntegrityService dbIntegrityService;
    @Context
    private HttpServletRequest httpServletRequest;
    
    @Context
    private UriInfo uriInfo;
    
    @Context
    protected Providers providers;

//    @GET
//    @Produces(MediaType.TEXT_XML)
//    @Path("")
//    // Returns notebook-infos for the notebooks accessible to the current user.
//    public JAXBElement<NotebookInfoList> getNotebookInfo(@Context HttpServletRequest httpServletRequest) {
//        final NotebookInfoList notebookInfoList = new NotebookInfoList();
//        String remoteUser = httpServletRequest.getRemoteUser();
//        UUID remoteUserUUID = (remoteUser != null) ? UUID.fromString(remoteUser) : null;
//        notebookInfoList.getNotebook().addAll(notebookDao.getNotebookInfos(remoteUserUUID));
//        return new ObjectFactory().createNotebookInfoList(notebookInfoList);
//    }
//
//    @GET
//    @Produces(MediaType.TEXT_XML)
//    @Path("test")
//    // This is not in the standards definition and is only used for testing
//    public JAXBElement<NotebookInfoList> getNotebookInfo(@QueryParam("userid") String userId) {
//        final NotebookInfoList notebookInfos = new NotebookInfoList();
//        notebookInfos.getNotebook().addAll(notebookDao.getNotebookInfos(UUID.fromString(userId)));
//        return new ObjectFactory().createNotebookInfoList(notebookInfos);
//    }
//
//    @GET
//    @Produces(MediaType.TEXT_XML)
//    @Path("owned")
//    // Returns the list of all notebooks owned by the current logged user.
//    public List<Notebook> getUsersNotebooks(@Context HttpServletRequest httpServletRequest) {
//        // todo: sort out how the user id is obtained and how it is stored it the db
//        String remoteUser = httpServletRequest.getRemoteUser();
//        UUID remoteUserUUID = (remoteUser != null) ? UUID.fromString(remoteUser) : null;
//        return notebookDao.getUsersNotebooks(remoteUserUUID);
//    }
//
//    @GET
//    @Produces("text/html")
//    @Path("{notebookid: [a-zA-Z0-9_]*}/readers")
//    // Returns the list of _uid_ who allowed to read the annotations from notebook.
//    public String getReaders(@PathParam("notebookid") String notebookId) {
//        return "readers for " + notebookId;
//    }
//
//    @GET
//    @Produces("text/html")
//    @Path("{notebookid: [a-zA-Z0-9_]*}/writers")
//    // Returns the list of _uid_ that can add annotations to the notebook.
//    public String getWriters(@PathParam("notebookid") String notebookId) {
//        return "writers for " + notebookId;
//    }
//
//    @GET
//    @Produces(MediaType.TEXT_XML)
//    @Path("{notebookid: " + BackendConstants.regExpIdentifier + "}/metadata")
//    // Get all metadata about a specified notebook _nid_, including the information if it is private or not.
//    public JAXBElement<NotebookInfo> getMetadata(@PathParam("notebookid") String notebookId) {
//        NotebookInfo result = notebookDao.getNotebookInfo(notebookDao.getInternalID(UUID.fromString(notebookId)));
//        // TODO change the name of the create method to createNotebookInfo!
//        return new ObjectFactory().createNotebookInfo(result);
//
//    }
//
//    @GET
//    @Path("{notebookid: " + BackendConstants.regExpIdentifier + "}")
//    /*
//     * Get the list of all annotations _aid_-s contained within a Notebook with related metadata. 
//     * Parameters: _nid_, 
//     * optional maximumAnnotations specifies the maximum number of annotations to retrieve (default -1, all annotations), 
//     * optional startAnnotation specifies the starting point from which the annotations will be retrieved (default: -1, start from the first annotation), 
//     * optional orderby, specifies the RDF property used to order the annotations (default: dc:created ), 
//     * optional orderingMode specifies if the results should be sorted using a descending order desc=1 or an ascending order desc=0 (default: 0 ).
//     * */
//    @Produces(MediaType.TEXT_XML)
//    public List<JAXBElement<UUID>> getAllAnnotations(@PathParam("notebookid") String notebookId, @DefaultValue("-1") @QueryParam(value = "maximumAnnotations") final int maximumAnnotations,
//            @DefaultValue("-1") @QueryParam(value = "startAnnotation") final int startAnnotation,
//            @DefaultValue("dc:created") @QueryParam(value = "orderby") final String orderby,
//            @DefaultValue("0") @QueryParam(value = "orderingMode") final int orderingMode) {
//        UUID notebookUUID = UUID.fromString(notebookId);
//        List<UUID> annotationIDs = notebookDao.getAnnotationExternalIDs(notebookUUID);
//        List<JAXBElement<UUID>> result = new ArrayList<JAXBElement<UUID>>();
//        for (UUID annotationID : annotationIDs) {
//            final JAXBElement<UUID> jaxbElement = new JAXBElement<UUID>(new QName("http://www.dasish.eu/ns/addit", "uuid"), UUID.class, null, annotationID);
//            result.add(jaxbElement);
//        }
//        return result;
//        // TODO implement optional parameters!!
//    }
//
//    @PUT
//    @Path("{notebookid: [a-zA-Z0-9_]*}")
//    @Consumes(MediaType.APPLICATION_XML)
//    /*
//     Modify metadata of _nid_. The new notebook?s name must be sent in request?s body.
//     */
//    public String modifyNotebook(@PathParam("notebookid") String notebookId, Notebook notebook) {
//        return "modifyNotebook " + notebookId + notebook.getTitle();
//    }
//
//    @PUT
//    @Path("{notebookid: [a-zA-Z0-9_]*}/{annotationid: [a-zA-Z0-9_]*}")
//    /*
//     Adds an annotation _aid_ to the list of annotations of _nid_.
//     */
//    public String addAnnotation(@PathParam("notebookid") String notebookId, @PathParam("annotationid") String annotationId) {
//        return "addAnnotation " + notebookId + " : " + annotationId;
//    }
//
////    @PUT
////    @Path("{notebookid: [a-zA-Z0-9_]*}/setPrivate={isPrivate: true|false}")
////    /*
////     Sets the specified Notebook as private or not private.
////     */
////    public String setPrivate(@PathParam("notebookid") String notebookId, @PathParam("isPrivate") String isPrivate) {
////        return "modifyNotebook " + notebookId + " : " + isPrivate;
////    }
//    @POST
//    @Path("")
//    /*
//     * Creates a new notebook. 
//     * This API returns the _nid_ of the created Notebook in response?s payload and the full URL of the notebook adding a Location header into the HTTP response. 
//     * The name of the new notebook can be specified sending a specific payload.
//     */
//    public String createNotebook(@Context HttpServletRequest httpServletRequest) throws URISyntaxException {
//        String remoteUser = httpServletRequest.getRemoteUser();
//        UUID remoteUserUUID = (remoteUser != null) ? UUID.fromString(remoteUser) : null;
//        UUID notebookId = notebookDao.addNotebook(remoteUserUUID, null);
//        final URI serverUri = new URI(httpServletRequest.getRequestURL().toString());
//        String fullUrlString = "/api/notebooks/" + notebookId.toString();
//        return serverUri.resolve(fullUrlString).toString();
//    }
//
//    @POST
//    @Path("{notebookid: [a-zA-Z0-9_]*}")
//    /*
//     * Creates a new annotation in _nid_. 
//     * The content of an annotation is given in the request body. In fact this is a short cut of two actions:
//     */
//    public String createAnnotation() {
//        String notebookId = "_nid_";
//        String fullUrlString = "api/notebooks/_nid_";
//        return "annotation " + notebookId + " : " + fullUrlString;
//    }
//
//    @DELETE
//    @Path("{notebookid: [a-zA-Z0-9_-]*}")
//    /*
//     Delete _nid_. Annotations stay, they just lose connection to _nid_.<br>
//     */
//    public String deleteNotebook(@PathParam("notebookid") UUID notebookId) {
//        // todo: sort out how the id string passsed in here is mapped eg db column for _nid_
//        return Integer.toString(notebookDao.deleteNotebook(notebookId));
//    }
}
