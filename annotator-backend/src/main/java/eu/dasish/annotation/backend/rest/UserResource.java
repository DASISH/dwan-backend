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
import eu.dasish.annotation.schema.CurrentUserInfo;
import eu.dasish.annotation.schema.ObjectFactory;
import eu.dasish.annotation.schema.User;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olhsha
 */
@Component
@Path("/users")
@Transactional(rollbackFor = {Exception.class, SQLException.class, IOException.class, ParserConfigurationException.class})
public class UserResource {

    @Autowired
    private DBIntegrityService dbIntegrityService;
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;
    @Context
    private UriInfo uriInfo;
    private final Logger logger = LoggerFactory.getLogger(UserResource.class);
    final private String admin = "admin";

    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    public UserResource() {
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{userid}")
    @Transactional(readOnly = true)
    public JAXBElement<User> getUser(@PathParam("userid") String externalIdentifier) throws SQLException, IOException {
        String remoteUser = httpServletRequest.getRemoteUser();
        Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (remoteUserID != null) {
            dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
            try {
                final Number userID = dbIntegrityService.getUserInternalIdentifier(UUID.fromString(externalIdentifier));
                if (userID != null) {
                    final User user = dbIntegrityService.getUser(userID);
                    return new ObjectFactory().createUser(user);
                } else {
                    AnnotationResource.loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The user with the given id " + externalIdentifier + " is not found in the database");
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The user with the given id   " + externalIdentifier + " is not found in the database");
                    return null;
                }
            } catch (IllegalArgumentException e) {
                AnnotationResource.loggerServer.debug(HttpServletResponse.SC_BAD_REQUEST + ": Illegal argument UUID " + externalIdentifier);
                httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal argument UUID " + externalIdentifier);
                return null;
            }
        } else {
            AnnotationResource.loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
            return null;
        }
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("/info")
    @Transactional(readOnly = true)
    public JAXBElement<User> getUserByInfo(@QueryParam("email") String email) throws SQLException, IOException {
        String remoteUser = httpServletRequest.getRemoteUser();
        Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (remoteUserID != null) {
            dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
            final User user = dbIntegrityService.getUserByInfo(email);
            if (user != null) {
                return new ObjectFactory().createUser(user);
            } else {
                AnnotationResource.loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The user with the given info is not found in the database");
                httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The user with the given info is not found in the database");
                return null;
            }
        } else {
            AnnotationResource.loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
            return null;
        }
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{userid}/current")
    @Transactional(readOnly = true)
    public JAXBElement<CurrentUserInfo> getCurrentUserInfo(@PathParam("userid") String externalIdentifier) throws IOException {
        String remoteUser = httpServletRequest.getRemoteUser();
        Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (remoteUserID != null) {
            dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
            try {
                final Number userID = dbIntegrityService.getUserInternalIdentifier(UUID.fromString(externalIdentifier));
                if (userID != null) {
                    final CurrentUserInfo userInfo = new CurrentUserInfo();
                    userInfo.setRef(dbIntegrityService.getUserURI(userID));
                    userInfo.setCurrentUser(ifLoggedIn(userID));
                    return new ObjectFactory().createCurrentUserInfo(userInfo);
                } else {
                    AnnotationResource.loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The user with the given id " + externalIdentifier + " is not found in the database");
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The user with the given id   " + externalIdentifier + " is not found in the database");
                    return null;
                }
            } catch (IllegalArgumentException e) {
                AnnotationResource.loggerServer.debug(HttpServletResponse.SC_BAD_REQUEST + ": Illegal argument UUID " + externalIdentifier);
                httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal argument UUID " + externalIdentifier);
                return null;
            }
        } else {
            AnnotationResource.loggerServer.debug(httpServletResponse.SC_NOT_FOUND + ": the logged-in user is not found in the database");
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
            return null;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("{remoteId}")
    public JAXBElement<User> addUser(@PathParam("remoteId") String remoteId, User user) throws SQLException, IOException {
        String remoteUser = httpServletRequest.getRemoteUser();
        Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (remoteUserID != null) {
            if (dbIntegrityService.getTypeOfUserAccount(remoteUserID).equals(admin)) {
                dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
                final Number userID = dbIntegrityService.addUser(user, remoteId);
                if (userID != null) {
                    final User addedUser = dbIntegrityService.getUser(userID);
                    return new ObjectFactory().createUser(addedUser);
                } else {
                    httpServletResponse.sendError(HttpServletResponse.SC_CONFLICT, "The user canot be added to the database, (possibly) because a user with the given e-mail already exist in the database.");
                    return null;
                }
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The logged-in user does not have admin rights to add a user to the database");
                return null;
            }
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
            return null;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("")
    public JAXBElement<User> updateUser(User user) throws IOException {
        String remoteUser = httpServletRequest.getRemoteUser();
        Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (remoteUserID != null) {
            if (dbIntegrityService.getTypeOfUserAccount(remoteUserID).equals(admin)) {
                dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
                final Number userID = dbIntegrityService.updateUser(user);
                if (userID != null) {
                    final User addedUser = dbIntegrityService.getUser(userID);
                    return new ObjectFactory().createUser(addedUser);
                } else {
                    AnnotationResource.loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The user with the given id is not found in the database");
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The user with the given id  is not found in the database");
                    return null;
                }
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The logged-in user does not have admin rights to update a user info in the database");
                return null;
            }
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
            return null;
        }
    }
    
    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    @Path("{externalId}/account/{accountType}")
    public String updateUserAccount(@PathParam("externalId") String externalId, @PathParam("accountType") String accountType) throws IOException {
        String remoteUser = httpServletRequest.getRemoteUser();
        Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (remoteUserID != null) {
            if (dbIntegrityService.getTypeOfUserAccount(remoteUserID).equals(admin)) {
                dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
                final boolean updated = dbIntegrityService.updateAccount(UUID.fromString(externalId), accountType);
                if (updated) {
                    return "The account was updated to "+dbIntegrityService.getTypeOfUserAccount(dbIntegrityService.getUserInternalIdentifier(UUID.fromString(externalId)));
                } else {
                    httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The account was not updated.");
                    return "The account was not updated.";
                }
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The logged-in user does not have admin rights to update an account type in the database");
                return null;
            }
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
            return null;
        }
    }

    @DELETE
    @Path("{userId}")
    public String deleteUser(@PathParam("userId") String externalIdentifier) throws IOException {
        String remoteUser = httpServletRequest.getRemoteUser();
        Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (remoteUserID != null) {
            if (dbIntegrityService.getTypeOfUserAccount(remoteUserID).equals(admin)) {
                dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
                final Number userID = dbIntegrityService.getUserInternalIdentifier(UUID.fromString(externalIdentifier));
                if (userID != null) {
                    final Integer result = dbIntegrityService.deleteUser(userID);
                    return "There is " + result.toString() + " row deleted";
                } else {
                    AnnotationResource.loggerServer.debug(HttpServletResponse.SC_NOT_FOUND + ": The user with the given id " + externalIdentifier + " is not found in the database");
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The user with the given id   " + externalIdentifier + " is not found in the database");
                    return null;
                }
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The logged-in user does not have admin rights to update a user info in the database");
                return null;
            }
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
            return null;
        }
    }

    @DELETE
    @Path("{userId}/safe")
    public String deleteUserSafe(@PathParam("userId") String externalIdentifier) throws IOException {
        String remoteUser = httpServletRequest.getRemoteUser();
        Number remoteUserID = dbIntegrityService.getUserInternalIDFromRemoteID(remoteUser);
        if (remoteUserID != null) {
            if (dbIntegrityService.getTypeOfUserAccount(remoteUserID).equals(admin)) {
                dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
                final Number userID = dbIntegrityService.getUserInternalIdentifier(UUID.fromString(externalIdentifier));
                if (userID != null) {
                    final Integer result = dbIntegrityService.deleteUserSafe(userID);
                    return "There is " + result.toString() + " row deleted";
                } else {
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The user with the given id   " + externalIdentifier + " is not found in the database");
                    return null;
                }
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The logged-in user does not have admin rights to update a user info in the database");
                return null;
            }
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The logged-in user is not found in the database");
            return null;
        }
    }

    private boolean ifLoggedIn(Number userID) {
        return httpServletRequest.getRemoteUser().equals(dbIntegrityService.getUserRemoteID(userID));
    }
    
    
}
