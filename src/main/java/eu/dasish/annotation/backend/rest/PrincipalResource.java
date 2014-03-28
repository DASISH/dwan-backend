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

import eu.dasish.annotation.backend.NotInDataBaseException;
import eu.dasish.annotation.backend.PrincipalExists;
import eu.dasish.annotation.backend.Resource;
import eu.dasish.annotation.schema.CurrentPrincipalInfo;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.Principal;
import java.io.IOException;
import java.sql.SQLException;
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
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.http.HTTPException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olhsha
 */
@Component
@Path("/principals")
@Transactional(rollbackFor = {Exception.class, SQLException.class, IOException.class, ParserConfigurationException.class})
public class PrincipalResource extends ResourceResource {

    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    public PrincipalResource() {
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{principalid}")
    @Transactional(readOnly = true)
    public JAXBElement<Principal> getPrincipal(@PathParam("principalid") String externalIdentifier) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        try {
            final Number principalID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.PRINCIPAL);
            final Principal principal = dbIntegrityService.getPrincipal(principalID);
            return new ObjectFactory().createPrincipal(principal);
        } catch (NotInDataBaseException e) {
            verboseOutput.PRINCIPAL_NOT_FOUND(externalIdentifier);
            throw new HTTPException(HttpServletResponse.SC_FORBIDDEN);
        }

    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("admin")
    @Transactional(readOnly = true)
    public String getAdmin() throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        return "The admin of the server database " + dbIntegrityService.getDataBaseAdmin().getDisplayName() + " is availiable via e-mail " + dbIntegrityService.getDataBaseAdmin().getEMail();
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("/info")
    @Transactional(readOnly = true)
    public JAXBElement<Principal> getPrincipalByInfo(@QueryParam("email") String email) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        try {
            final Principal principal = dbIntegrityService.getPrincipalByInfo(email);
            return new ObjectFactory().createPrincipal(principal);
        } catch (NotInDataBaseException e) {
            verboseOutput.PRINCIPAL_NOT_FOUND_BY_INFO(email);
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }

    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{principalid}/current")
    @Transactional(readOnly = true)
    public JAXBElement<CurrentPrincipalInfo> getCurrentPrincipalInfo(@PathParam("principalid") String externalIdentifier) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        try {
            final Number principalID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.PRINCIPAL);
            final CurrentPrincipalInfo principalInfo = new CurrentPrincipalInfo();
            principalInfo.setRef(dbIntegrityService.getResourceURI(principalID, Resource.PRINCIPAL));
            principalInfo.setCurrentPrincipal(ifLoggedIn(principalID));
            return new ObjectFactory().createCurrentPrincipalInfo(principalInfo);
        } catch (NotInDataBaseException e) {
            verboseOutput.PRINCIPAL_NOT_FOUND(externalIdentifier);
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }

    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{remoteId}")
    public JAXBElement<Principal> addPrincipal(@PathParam("remoteId") String remoteId, Principal principal) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
            try {
                try {
                    final Number principalID = dbIntegrityService.addPrincipal(principal, remoteId);
                    final Principal addedPrincipal = dbIntegrityService.getPrincipal(principalID);
                    return new ObjectFactory().createPrincipal(addedPrincipal);
                } catch (NotInDataBaseException e1) {
                    verboseOutput.PRINCIPAL_IS_NOT_ADDED_TO_DB();
                    throw new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } catch (PrincipalExists e) {
                verboseOutput.PRINCIPAL_IS_NOT_ADDED_TO_DB();
                throw new HTTPException(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            verboseOutput.ADMIN_RIGHTS_EXPECTED();
            throw new HTTPException(HttpServletResponse.SC_FORBIDDEN);
        }

    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    public JAXBElement<Principal> updatePrincipal(Principal principal) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
            try {
                final Number principalID = dbIntegrityService.updatePrincipal(principal);
                final Principal addedPrincipal = dbIntegrityService.getPrincipal(principalID);
                return new ObjectFactory().createPrincipal(addedPrincipal);
            } catch (NotInDataBaseException e) {
                verboseOutput.PRINCIPAL_NOT_FOUND(principal.getURI());
                throw new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            verboseOutput.ADMIN_RIGHTS_EXPECTED();
            throw new HTTPException(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{externalId}/account/{accountType}")
    public String updatePrincipalAccount(@PathParam("externalId") String externalId, @PathParam("accountType") String accountType) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
            try {
                final boolean updated = dbIntegrityService.updateAccount(UUID.fromString(externalId), accountType);
                if (updated) {
                    return "The account was updated to " + dbIntegrityService.getTypeOfPrincipalAccount(dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalId), Resource.PRINCIPAL));
                } else {
                    verboseOutput.ACCOUNT_IS_NOT_UPDATED();
                    throw new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } catch (NotInDataBaseException e) {
                throw new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            verboseOutput.ADMIN_RIGHTS_EXPECTED();
            throw new HTTPException(HttpServletResponse.SC_FORBIDDEN);
        }

    }

    @DELETE
    @Path("{principalId}")
    public String deletePrincipal(@PathParam("principalId") String externalIdentifier) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
            try {
                final Number principalID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.PRINCIPAL);
                final Integer result = dbIntegrityService.deletePrincipal(principalID);
                return "There is " + result.toString() + " row deleted";
            } catch (NotInDataBaseException e) {
                verboseOutput.PRINCIPAL_NOT_FOUND(externalIdentifier);
                throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            verboseOutput.ADMIN_RIGHTS_EXPECTED();
            throw new HTTPException(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @DELETE
    @Path("{principalId}/safe")
    public String deletePrincipalSafe(@PathParam("principalId") String externalIdentifier) throws IOException {
        Number remotePrincipalID = this.getPrincipalID();
        if (dbIntegrityService.getTypeOfPrincipalAccount(remotePrincipalID).equals(admin)) {
            try {
                final Number principalID = dbIntegrityService.getResourceInternalIdentifier(UUID.fromString(externalIdentifier), Resource.PRINCIPAL);
                final int result = dbIntegrityService.deletePrincipalSafe(principalID);
                return "There is " + result + " row deleted";
            } catch (NotInDataBaseException e) {
                verboseOutput.PRINCIPAL_NOT_FOUND(externalIdentifier);
                throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            verboseOutput.ADMIN_RIGHTS_EXPECTED();
            throw new HTTPException(HttpServletResponse.SC_FORBIDDEN);
        }

    }

    private boolean ifLoggedIn(Number principalID) {
        return (httpServletRequest.getRemoteUser()).equals(dbIntegrityService.getPrincipalRemoteID(principalID));
    }
}
