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
import javax.ws.rs.Consumes;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olhsha
 */
@Component
@Path("/users")
@Transactional(rollbackFor={Exception.class, SQLException.class, IOException.class, ParserConfigurationException.class})
public class UserResource {
    @Autowired
    private DBIntegrityService dbIntegrityService;
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private UriInfo uriInfo;

    public void setHttpRequest(HttpServletRequest request) {
        this.httpServletRequest = request;
    }

    public UserResource() {
    }
    
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{userid: " + BackendConstants.regExpIdentifier + "}")
    @Secured("ROLE_USER")    
    @Transactional(readOnly=true)
    public JAXBElement<User> getUser(@PathParam("userid") String ExternalIdentifier) throws SQLException {
         dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        final Number userID = dbIntegrityService.getUserInternalIdentifier(UUID.fromString(ExternalIdentifier));
        final User user = dbIntegrityService.getUser(userID);
        return new ObjectFactory().createUser(user);
    }
    
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("/info")
    @Secured("ROLE_USER")    
    @Transactional(readOnly=true)
    public JAXBElement<User> getUserByInfo(@QueryParam("email") String email) throws SQLException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        final User user = dbIntegrityService.getUserByInfo(email);
        return new ObjectFactory().createUser(user);
    }
    
    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("{userid: " + BackendConstants.regExpIdentifier + "}/current")
    @Secured("ROLE_USER")    
    @Transactional(readOnly=true)
    public JAXBElement<CurrentUserInfo> getCurrentUserInfo(@PathParam("userid") String ExternalIdentifier){
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());
        final Number userID = dbIntegrityService.getUserInternalIdentifier(UUID.fromString(ExternalIdentifier));
        final CurrentUserInfo userInfo = new CurrentUserInfo();
        userInfo.setRef(dbIntegrityService.getUserURI(userID));
        userInfo.setCurrentUser(ifLoggedIn(userID));
        return new ObjectFactory().createCurrentUserInfo(userInfo);
    }
    
    @POST
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    @Path("{remoteId: " + BackendConstants.regExpIdentifier + "}")
    @Secured("ROLE_ADMIN")
    public JAXBElement<User> addUser(@PathParam("userid") String remoteId, User user) throws SQLException {
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());        
        final Number userID = dbIntegrityService.addUser(user, remoteId);
        final User addedUser = dbIntegrityService.getUser(userID);
        return new ObjectFactory().createUser(addedUser);
    }
    
    @PUT
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    @Path("")
    @Secured("ROLE_ADMIN")
    public JAXBElement<User> updateUser(User user){
        dbIntegrityService.setServiceURI(uriInfo.getBaseUri().toString());        
        final Number userID = dbIntegrityService.updateUser(user);
        final User addedUser = dbIntegrityService.getUser(userID);
        return new ObjectFactory().createUser(addedUser);
    }
    
    
    
    private boolean ifLoggedIn(Number userID){
        return httpServletRequest.getRemoteUser().equals(dbIntegrityService.getUserRemoteID(userID));
    }
}
