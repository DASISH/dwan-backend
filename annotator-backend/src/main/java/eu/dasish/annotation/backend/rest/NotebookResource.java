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
import eu.dasish.annotation.schema.Notebook;
import eu.dasish.annotation.schema.NotebookInfoList;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.ReferenceList;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class NotebookResource {

    @Autowired
    private DBIntegrityService dbIntegrityService;
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;
    @Context
    private UriInfo uriInfo;
    @Context
    protected Providers providers;
    private final Logger logger = LoggerFactory.getLogger(AnnotationResource.class);
    private final Logger loggerServer = LoggerFactory.getLogger(HttpServletResponse.class);

    public NotebookResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    @Transactional(readOnly = true)
    public JAXBElement<NotebookInfoList> getNotebookInfos(@QueryParam("permission") String permissionMode) throws IOException {

        URI baseURI = uriInfo.getBaseUri();
        String baseURIstr = baseURI.toString();
        dbIntegrityService.setServiceURI(baseURIstr);

        String remoteUser = httpServletRequest.getRemoteUser();
        final Number userID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (userID != null) {
            if (permissionMode.equalsIgnoreCase("reader") || permissionMode.equalsIgnoreCase("writer") || permissionMode.equalsIgnoreCase("owner")) {
                NotebookInfoList notebookInfos = dbIntegrityService.getNotebooks(userID, permissionMode);
                return new ObjectFactory().createNotebookInfoList(notebookInfos);
            } else {
                loggerServer.debug(httpServletResponse.SC_BAD_REQUEST + ": '" + permissionMode + "' is an invalid permission value, which must be either owner, or reader, or writer.");
                httpServletResponse.sendError(httpServletResponse.SC_BAD_REQUEST, permissionMode + "' is an invalid permission value, which must be either owner, or reader, or writer.");
                return null;
            }
        } else {
            loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
            return null;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("owned")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getOwnedNotebooks() throws IOException {

        URI baseURI = uriInfo.getBaseUri();
        String baseURIstr = baseURI.toString();
        dbIntegrityService.setServiceURI(baseURIstr);

        String remoteUser = httpServletRequest.getRemoteUser();
        final Number userID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (userID != null) {
            ReferenceList references = dbIntegrityService.getNotebooksOwnedBy(userID);
            return new ObjectFactory().createReferenceList(references);
        } else {
            loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
            return null;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{notebookid: " + BackendConstants.regExpIdentifier + "}/targets")
    @Transactional(readOnly = true)
    public JAXBElement<Notebook> getNotebook(@PathParam("notebookid") String externalIdentifier) throws IOException {

        URI baseURI = uriInfo.getBaseUri();
        String baseURIstr = baseURI.toString();
        dbIntegrityService.setServiceURI(baseURIstr);

        String remoteUser = httpServletRequest.getRemoteUser();
        final Number userID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (userID != null) {
            Number notebookID = dbIntegrityService.getNotebookInternalIdentifier(UUID.fromString(externalIdentifier));
            if (notebookID != null) {
                Notebook notebook = dbIntegrityService.getNotebook(notebookID);
                return new ObjectFactory().createNotebook(notebook);
            } else {
                loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The notebook with the given id " + externalIdentifier + " is not found in the database");
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The notebook with the given id " + externalIdentifier + " is not found in the database");
                return null;
            }
        } else {
            loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
            return null;
        }
    }
}
