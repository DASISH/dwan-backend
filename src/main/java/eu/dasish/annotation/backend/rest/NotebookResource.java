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

import eu.dasish.annotation.backend.identifiers.UserIdentifier;
import eu.dasish.annotation.backend.dao.NotebookDao;
import eu.dasish.annotation.backend.identifiers.NotebookIdentifier;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.NotebookInfos;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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
    private NotebookDao notebookDao;

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("")
    // Returns notebook-infos for the notebooks accessible to the current user.
    public NotebookInfos getNotebookInfo(@Context HttpServletRequest httpServletRequest) {
        final NotebookInfos notebookInfos = new NotebookInfos();
        notebookInfos.getNotebook().addAll(notebookDao.getNotebookInfos(new UserIdentifier(httpServletRequest.getRemoteUser())));
        return notebookInfos;
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("test")
    // This is not in the standards definition and is only used for testing
    public NotebookInfos getNotebookInfo(@QueryParam("userid") String userId) {
        final NotebookInfos notebookInfos = new NotebookInfos();
        notebookInfos.getNotebook().addAll(notebookDao.getNotebookInfos(new UserIdentifier(userId)));
        return notebookInfos;
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("owned")
    // Returns the list of all notebooks owned by the current logged user.
    public List<Notebook> getUsersNotebooks(@Context HttpServletRequest httpServletRequest) {
        // todo: sort out how the user id is obtained and how it is stored it the db
        return notebookDao.getUsersNotebooks(new UserIdentifier(httpServletRequest.getRemoteUser()));
    }

    @GET
    @Produces("text/html")
    @Path("{notebookid: [a-zA-Z0-9_]*}/readers")
    // Returns the list of _uid_ who allowed to read the annotations from notebook.
    public String getReaders(@PathParam("notebookid") String notebookId) {
        return "readers for " + notebookId;
    }

    @GET
    @Produces("text/html")
    @Path("{notebookid: [a-zA-Z0-9_]*}/writers")
    // Returns the list of _uid_ that can add annotations to the notebook.
    public String getWriters(@PathParam("notebookid") String notebookId) {
        return "writers for " + notebookId;
    }

    @GET
    @Produces("text/html")
    @Path("{notebookid: [a-zA-Z0-9_]*}/metadata")
    // Get all metadata about a specified notebook _nid_, including the information if it is private or not.
    public String getMetadata(@PathParam("notebookid") String notebookId) {
        return "metadata for " + notebookId;
    }

    @GET
    @Path("{notebookid: [a-zA-Z0-9_]*}")
    /*
     * Get the list of all annotations _aid_-s contained within a Notebook with related metadata. 
     * Parameters: _nid_, 
     * optional maximumAnnotations specifies the maximum number of annotations to retrieve (default -1, all annotations), 
     * optional startAnnotation specifies the starting point from which the annotations will be retrieved (default: -1, start from the first annotation), 
     * optional orderby, specifies the RDF property used to order the annotations (default: dc:created ), 
     * optional orderingMode specifies if the results should be sorted using a descending order desc=1 or an ascending order desc=0 (default: 0 ).
     * */
    @Produces("text/html")
    public String getAllAnnotations(@PathParam("notebookid") String notebookId, @DefaultValue("-1") @QueryParam(value = "maximumAnnotations") final int maximumAnnotations,
            @DefaultValue("-1") @QueryParam(value = "startAnnotation") final int startAnnotation,
            @DefaultValue("dc:created") @QueryParam(value = "orderby") final String orderby,
            @DefaultValue("0") @QueryParam(value = "orderingMode") final int orderingMode) {
        return "all annotations for " + notebookId + " : " + maximumAnnotations + " : " + startAnnotation + " : " + orderby + " : " + orderingMode;
    }

    @PUT
    @Path("{notebookid: [a-zA-Z0-9_]*}")
    @Consumes(MediaType.APPLICATION_XML)
    /*
     Modify metadata of _nid_. The new notebook?s name must be sent in request?s body.
     */
    public String modifyNotebook(@PathParam("notebookid") String notebookId, Notebook notebook) {
        return "modifyNotebook " + notebookId + notebook.getTitle();
    }

    @PUT
    @Path("{notebookid: [a-zA-Z0-9_]*}/{annotationid: [a-zA-Z0-9_]*}")
    /*
     Adds an annotation _aid_ to the list of annotations of _nid_.
     */
    public String addAnnotation(@PathParam("notebookid") String notebookId, @PathParam("annotationid") String annotationId) {
        return "addAnnotation " + notebookId + " : " + annotationId;
    }

//    @PUT
//    @Path("{notebookid: [a-zA-Z0-9_]*}/setPrivate={isPrivate: true|false}")
//    /*
//     Sets the specified Notebook as private or not private.
//     */
//    public String setPrivate(@PathParam("notebookid") String notebookId, @PathParam("isPrivate") String isPrivate) {
//        return "modifyNotebook " + notebookId + " : " + isPrivate;
//    }
    @POST
    @Path("")
    /*
     * Creates a new notebook. 
     * This API returns the _nid_ of the created Notebook in response?s payload and the full URL of the notebook adding a Location header into the HTTP response. 
     * The name of the new notebook can be specified sending a specific payload.
     */
    public String createNotebook(@Context HttpServletRequest httpServletRequest) throws URISyntaxException {
        NotebookIdentifier notebookId = notebookDao.addNotebook(new UserIdentifier(httpServletRequest.getRemoteUser()), null);
        final URI serverUri = new URI(httpServletRequest.getRequestURL().toString());
        String fullUrlString = "/api/notebooks/" + notebookId.getUUID().toString();
        return serverUri.resolve(fullUrlString).toString();
    }

    @POST
    @Path("{notebookid: [a-zA-Z0-9_]*}")
    /*
     * Creates a new annotation in _nid_. 
     * The content of an annotation is given in the request body. In fact this is a short cut of two actions:
     */
    public String createAnnotation() {
        String notebookId = "_nid_";
        String fullUrlString = "api/notebooks/_nid_";
        return "annotation " + notebookId + " : " + fullUrlString;
    }

    @DELETE
    @Path("{notebookid: [a-zA-Z0-9_-]*}")
    /*
     Delete _nid_. Annotations stay, they just lose connection to _nid_.<br>
     */
    public String deleteNotebook(@PathParam("notebookid") NotebookIdentifier notebookId) {
        // todo: sort out how the id string passsed in here is mapped eg db column for _nid_
        return Integer.toString(notebookDao.deleteNotebook(notebookId));
    }
}
