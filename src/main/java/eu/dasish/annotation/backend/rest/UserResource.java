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
    public static final Logger loggerServer = LoggerFactory.getLogger(HttpServletResponse.class);
    private final VerboseOutput verboseOutput = new VerboseOutput(httpServletResponse, loggerServer);
    
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
                    verboseOutput.PRINCIPAL_NOT_FOUND(externalIdentifier);
                }
            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(externalIdentifier);
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }
        return new ObjectFactory().createUser(new User());
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
                verboseOutput.PRINCIPAL_NOT_FOUND_BY_INFO(email);
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }
        return new ObjectFactory().createUser(new User()); 
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
                    verboseOutput.PRINCIPAL_NOT_FOUND(externalIdentifier);
                }
            } catch (IllegalArgumentException e) {
                verboseOutput.ILLEGAL_UUID(externalIdentifier);
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }
        
         return new ObjectFactory().createCurrentUserInfo(new CurrentUserInfo());
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
                     verboseOutput.PRINCIPAL_IS_NOT_ADDED_TO_DB();
                }
            } else {
                verboseOutput.ADMIN_RIGHTS_EXPECTED();
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }
         return new ObjectFactory().createUser(new User());
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
                    verboseOutput.PRINCIPAL_NOT_FOUND(user.getURI());
                }
            } else {
                verboseOutput.ADMIN_RIGHTS_EXPECTED();
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser); 
        }
        
         return new ObjectFactory().createUser(new User());
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
                   verboseOutput.ACCOUNT_IS_NOT_UPDATED(); 
                }
            } else {
                verboseOutput.ADMIN_RIGHTS_EXPECTED();
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }
        
        return " ";
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
                    verboseOutput.PRINCIPAL_NOT_FOUND(externalIdentifier);
                }
            } else {
                verboseOutput.ADMIN_RIGHTS_EXPECTED();
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }
        
        return " ";
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
                    verboseOutput.PRINCIPAL_NOT_FOUND(externalIdentifier);
                }
            } else {
                verboseOutput.ADMIN_RIGHTS_EXPECTED();
            }
        } else {
            verboseOutput.REMOTE_PRINCIPAL_NOT_FOUND(remoteUser);
        }
        return " ";
    }

    private boolean ifLoggedIn(Number userID) {
        return httpServletRequest.getRemoteUser().equals(dbIntegrityService.getUserRemoteID(userID));
    }
    
    
}
