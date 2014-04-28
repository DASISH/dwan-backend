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
 * Created on : Jun 11, 2013, 5:10:55 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
@Component
@Path("/notebooks")
@Transactional(rollbackFor = {Exception.class, SQLException.class, IOException.class, ParserConfigurationException.class})
public class NotebookResource extends ResourceResource {

    public NotebookResource() {
    }

    // changed w.r.t.the spec, query parameter persmission is added
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    @Transactional(readOnly = true)
    public JAXBElement<NotebookInfoList> getNotebookInfos(@QueryParam("access") String accessMode) throws IOException {
        dbDispatcher.setServiceURI(uriInfo.getBaseUri().toString());
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
// Notebook and NotebookInfo (metadata) schemata may be changed
// 1) we do not have information "private notebook" directly in the xml, but we have reads and writes in the schema
//so if both are empty then we see that it is private for the owner
// or shall we change the scheme? for notebooks
// 2) d we need to include the reference list of annotations in teh metadata of the notebook

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{notebookid: " + BackendConstants.regExpIdentifier + "}/metadata")
    @Transactional(readOnly = true)
    public JAXBElement<Notebook> getNotebook(@PathParam("notebookid") String externalIdentifier) throws IOException, HTTPException {
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

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{notebookid: " + BackendConstants.regExpIdentifier + "}")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getNotebookAnnotations(@PathParam("notebookid") String externalIdentifier,
            @QueryParam("maximumAnnotations") int maximumAnnotations,
            @QueryParam("startAnnotation") int startAnnotations,
            @QueryParam("orderBy") String orderBy,
            @QueryParam("descending") boolean desc) throws IOException, HTTPException {

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

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{notebookid: " + BackendConstants.regExpIdentifier + "}")
    public JAXBElement<ResponseBody> updateNotebookInfo(@PathParam("notebookid") String externalIdentifier, NotebookInfo notebookInfo) throws IOException, HTTPException {
        Number remotePrincipalID = this.getPrincipalID();
        if (remotePrincipalID == null) {
            return new ObjectFactory().createResponseBody(new ResponseBody());
        }
        String path = uriInfo.getBaseUri().toString();
        String notebookURI = notebookInfo.getRef();
        if (!(path + "notebook/" + externalIdentifier).equals(notebookURI)) {
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
