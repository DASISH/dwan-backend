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
import eu.dasish.annotation.schema.Permission;
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
    private final Logger loggerServer = LoggerFactory.getLogger(HttpServletResponse.class);
    private final VerboseOutput verboseOutput = new VerboseOutput(httpServletResponse, loggerServer);

    public NotebookResource() {
    }

    // changed w.r.t.the spec, query parameter persmission is added
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    @Transactional(readOnly = true)
    public JAXBElement<NotebookInfoList> getNotebookInfos(@QueryParam("permission") String permissionMode) throws IOException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        String remoteUser = httpServletRequest.getRemoteUser();
        final Number principalID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (principalID != null) {
            if (permissionMode.equalsIgnoreCase("reader") || permissionMode.equalsIgnoreCase("writer") || permissionMode.equalsIgnoreCase("owner")) {
                NotebookInfoList notebookInfos = dbIntegrityService.getNotebooks(principalID, permissionMode);
                return new ObjectFactory().createNotebookInfoList(notebookInfos);
            } else {
                verboseOutput.INVALID_PERMISSION_MODE(permissionMode);
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }
        return (new ObjectFactory()).createNotebookInfoList(new NotebookInfoList());
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("owned")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getOwnedNotebooks() throws IOException {

        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());

        String remoteUser = httpServletRequest.getRemoteUser();
        final Number userID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (userID != null) {
            ReferenceList references = dbIntegrityService.getNotebooksOwnedBy(userID);
            return new ObjectFactory().createReferenceList(references);
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }
        return new ObjectFactory().createReferenceList(new ReferenceList());
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{notebookid: " + BackendConstants.regExpIdentifier + "}/{permission}")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getPrincipals(@PathParam("notebookid") String externalIdentifier, @PathParam("permission") String permissionMode) throws IOException {

        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());

        String remoteUser = httpServletRequest.getRemoteUser();
        final Number principalID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (principalID != null) {
            Number notebookID = dbIntegrityService.getNotebookInternalIdentifier(UUID.fromString(externalIdentifier));
            if (notebookID != null) {
                if (dbIntegrityService.hasAccess(notebookID, principalID, Permission.fromValue("reader"))) {
                    ReferenceList principals = dbIntegrityService.getPrincipals(notebookID, permissionMode);
                    return new ObjectFactory().createReferenceList(principals);
                } else {
                    verboseOutput.FORBIDDEN_NOTEBOOK_READING(externalIdentifier);
                }
            } else {
                verboseOutput.NOTEBOOK_NOT_FOUND(externalIdentifier);
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }

        return new ObjectFactory().createReferenceList(new ReferenceList());
    }

    // Notebook and NotebookInfo (metadata) schemata may be changed
    // 1) we do not have information "private notebook" directly in the xml, but we have readers and writers in the schema
    //so if both are empty then we see that it is private for the owner
    // or shall we change the scheme? for notebooks
    // 2) d we need to include the reference list of annotations in teh metadata of the notebook
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{notebookid: " + BackendConstants.regExpIdentifier + "}/metadata")
    @Transactional(readOnly = true)
    public JAXBElement<Notebook> getNotebook(@PathParam("notebookid") String externalIdentifier) throws IOException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        String remoteUser = httpServletRequest.getRemoteUser();
        final Number principalID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (principalID != null) {
            Number notebookID = dbIntegrityService.getNotebookInternalIdentifier(UUID.fromString(externalIdentifier));
            if (notebookID != null) {
                if (dbIntegrityService.hasAccess(notebookID, principalID, Permission.fromValue("reader"))) {
                    Notebook notebook = dbIntegrityService.getNotebook(notebookID);
                    return new ObjectFactory().createNotebook(notebook);
                } else {
                    verboseOutput.FORBIDDEN_NOTEBOOK_READING(externalIdentifier);
                }
            } else {
                verboseOutput.NOTEBOOK_NOT_FOUND(externalIdentifier);
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }
        return new ObjectFactory().createNotebook(new Notebook());
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{notebookid: " + BackendConstants.regExpIdentifier + "}")
    @Transactional(readOnly = true)
    public JAXBElement<ReferenceList> getNotebookAnnotations(@PathParam("notebookid") String externalIdentifier,
            @QueryParam("maximumAnnotations") int maximumAnnotations,
            @QueryParam("startAnnotation") int startAnnotations,
            @QueryParam("orderBy") String orderBy,
            @QueryParam("descending") boolean desc) throws IOException {

        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());

        String remoteUser = httpServletRequest.getRemoteUser();
        final Number principalID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (principalID != null) {
            Number notebookID = dbIntegrityService.getNotebookInternalIdentifier(UUID.fromString(externalIdentifier));
            if (notebookID != null) {
                if (dbIntegrityService.hasAccess(notebookID, principalID, Permission.fromValue("reader"))) {
                    ReferenceList annotations = dbIntegrityService.getAnnotationsForNotebook(notebookID, startAnnotations, maximumAnnotations, orderBy, desc);
                    return new ObjectFactory().createReferenceList(annotations);
                } else {
                    verboseOutput.FORBIDDEN_NOTEBOOK_READING(externalIdentifier);
                }
            } else {
                verboseOutput.NOTEBOOK_NOT_FOUND(externalIdentifier);
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }
        return new ObjectFactory().createReferenceList(new ReferenceList());
    }
}
