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
import eu.dasish.annotation.schema.NotebookInfoList;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Access;
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfo;
import eu.dasish.annotation.schema.ReferenceList;
import eu.dasish.annotation.schema.ResponseBody;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.http.HTTPException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * A REST interface for GETting, POSTing, PUTting and DELETing notebooks or their substructures (child elements).
 * Every REST method in the case of successful completion produces the object of the declared output type
 * (a JAXB-element or a message string) or sends a HTTP-error with the corresponding diagnostics otherwise.
 * @author olhsha@mpi.nl
 */
@Component
@Path("/notebooks")
@Transactional(rollbackFor = {Exception.class, SQLException.class, IOException.class, ParserConfigurationException.class})
public class NotebookResource extends ResourceResource {

    public NotebookResource() {
    }

   /**
    * 
    * @param accessMode a string, representing an access mode: "none", "read", "write", "all".
    * @return the {@link NotebookInfoList} element containing the list of {@link NotebookInfo} elements
    * of all the notebooks to which the in-logged principal has "access" access.
    * @throws IOException if sending an error fails.
    */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    @Transactional(readOnly = true)
    public JAXBElement<NotebookInfoList> getNotebookInfos(@QueryParam("access") String accessMode) throws IOException {
        dbDispatcher.setResourcesPaths(this.getRelativeServiceURI());
        Number remotePrincipalID = this.getPrincipalID();
        if (accessMode.equalsIgnoreCase("read") || accessMode.equalsIgnoreCase("write")) {
            NotebookInfoList notebookInfos = dbDispatcher.getNotebooks(remotePrincipalID, Access.fromValue(accessMode));
            return new ObjectFactory().createNotebookInfoList(notebookInfos);
        } else {
            this.INVALID_ACCESS_MODE(accessMode);
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "ivalide mode acess " + accessMode);
            return new ObjectFactory().createNotebookInfoList(new NotebookInfoList());
        }
    }

    /**
     * 
     * @return the {@link ReferenceList} element containing the list of h-references of all the notebooks owned by the in-logged principal.
     * @throws IOException if sending an error fails.
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("owned")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getOwnedNotebooks() throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return new ObjectFactory().createReferenceList(new ReferenceList());
        }
        ReferenceList references = dbDispatcher.getNotebooksOwnedBy(remotePrincipalID);
        return new ObjectFactory().createReferenceList(references);
    }

    /**
     * 
     * @param externalIdentifier the external UUID identifier of a notebook.
     * @param accessMode the access mode on which principals must be filtered; 
     * can be "none", "read", "write", "all".
     * @return a {@link ReferenceList} element representing the list of h-references of the
     * principals that have access "accessMode".
     * @throws IOException if sending an error fails.
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{notebookid: " + BackendConstants.regExpIdentifier + "}/{access}")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getPrincipals(@PathParam("notebookid") String externalIdentifier, @PathParam("access") String accessMode) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return new ObjectFactory().createReferenceList(new ReferenceList());
        }
        try {
            Number notebookID = dbDispatcher.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.NOTEBOOK);
            if (dbDispatcher.hasAccess(notebookID, remotePrincipalID, Access.fromValue("read"))) {
                ReferenceList principals = dbDispatcher.getPrincipals(notebookID, accessMode);
                return new ObjectFactory().createReferenceList(principals);
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return new ObjectFactory().createReferenceList(new ReferenceList());
            }

        } catch (NotInDataBaseException e) {
            loggerServer.debug(e.toString());;
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.toString());
            return new ObjectFactory().createReferenceList(new ReferenceList());
        }
    }
    
  /**
   * 
   * @param externalIdentifier the external UUID identifier of a notebook.
   * @return a {@link Notebook} element representing the notebook with "externalIdentifier"; built up on the whole information 
   * (the "notebook" table and the corresponding junction tables) for the notebook with "externalIdentifier".
   * @throws IOException if sending an error fails.
   */
    

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{notebookid: " + BackendConstants.regExpIdentifier + "}/metadata")
    @Transactional(readOnly = true)
    public JAXBElement<Notebook> getNotebook(@PathParam("notebookid") String externalIdentifier) throws IOException{
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return new ObjectFactory().createNotebook(new Notebook());
        }
        try {
            Number notebookID = dbDispatcher.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.NOTEBOOK);
            if (dbDispatcher.hasAccess(notebookID, remotePrincipalID, Access.fromValue("read"))) {
                Notebook notebook = dbDispatcher.getNotebook(notebookID);
                return new ObjectFactory().createNotebook(notebook);
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return new ObjectFactory().createNotebook(new Notebook());
            }

        } catch (NotInDataBaseException e) {
            loggerServer.debug(e.toString());;
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.toString());
            return new ObjectFactory().createNotebook(new Notebook());
        }
    }

    /**
     * 
     * @param externalIdentifier the external UUID of a notebook.
     * @param maximumAnnotations the maximum amount of annotations from this notebook to output;
     * if the amount of annotations in the notebook is more than  (startAnnotations-1) + maximumAnnotations,
     * then exactly maximumAnnotations will be output, otherwise  the amount of annotations is limited by "# of annotations in the notebook" - (startAnnotation-1)
     * @param startAnnotations the index of the first annotation, min value is "1". 
     * @param orderBy the field in the table "notebook" on which annotations must be ordered.
     * @param desc if true then the annotations in the list must be ordered in descending order, otherwise in ascending order.
     * @return a {@link ReferenceList} element representing the list of annotations filtered according to the parameters.
     * @throws IOException if sending an error fails.
     */
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{notebookid: " + BackendConstants.regExpIdentifier + "}")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getNotebookAnnotations(@PathParam("notebookid") String externalIdentifier,
            @QueryParam("maximumAnnotations") int maximumAnnotations,
            @QueryParam("startAnnotation") int startAnnotations,
            @QueryParam("orderBy") String orderBy,
            @QueryParam("descending") boolean desc) throws IOException{

        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return new ObjectFactory().createReferenceList(new ReferenceList());
        }
        try {
            Number notebookID = dbDispatcher.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.NOTEBOOK);
            if (dbDispatcher.hasAccess(notebookID, remotePrincipalID, Access.fromValue("read"))) {
                ReferenceList annotations = dbDispatcher.getAnnotationsForNotebook(notebookID, startAnnotations, maximumAnnotations, orderBy, desc);
                return new ObjectFactory().createReferenceList(annotations);
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                return new ObjectFactory().createReferenceList(new ReferenceList());
            }

        } catch (NotInDataBaseException e) {
            loggerServer.debug(e.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.toString());
            return new ObjectFactory().createReferenceList(new ReferenceList());
        }

    }
    
    /**
     * 
     * @param externalIdentifier the external UUID identifier of a notebook.
     * @param notebookInfo the fresh {@link NotebookInfo} object.
     * @return a {@link ResponseBody} element containing the just updated {@link Notebook} element
     * and the list of actions (which are not yet specified for the notebooks).
     * @throws IOException if sending an error fails.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{notebookid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<ResponseBody> updateNotebookInfo(@PathParam("notebookid") String externalIdentifier, NotebookInfo notebookInfo) throws IOException{
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return new ObjectFactory().createResponseBody(new ResponseBody());
        }
        String path = this.getRelativeServiceURI();
        String notebookURI = notebookInfo.getHref();
        if (!(path + "/notebooks/" + externalIdentifier).equals(notebookURI)) {
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return new ObjectFactory().createResponseBody(new ResponseBody());
        };
        try {
            final Number notebookID = dbDispatcher.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.NOTEBOOK);
            try {
                if (remotePrincipalID.equals(dbDispatcher.getNotebookOwner(notebookID)) || dbDispatcher.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
                    boolean success = dbDispatcher.updateNotebookMetadata(notebookID, notebookInfo);
                    if (success) {
                        return new ObjectFactory().createResponseBody(dbDispatcher.makeNotebookResponseEnvelope(notebookID));
                    } else {
                        httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        return new ObjectFactory().createResponseBody(new ResponseBody());
                    }
                } else {
                    loggerServer.debug(" Ownership changing is the part of the full update of the notebook metadadata.");
                    httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return new ObjectFactory().createResponseBody(new ResponseBody());
                }
            } catch (NotInDataBaseException e1) {
                loggerServer.debug(e1.toString());
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
                return new ObjectFactory().createResponseBody(new ResponseBody());
            }
        } catch (NotInDataBaseException e) {
            loggerServer.debug(e.toString());
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.toString());
            return new ObjectFactory().createResponseBody(new ResponseBody());
        }
    }
}
